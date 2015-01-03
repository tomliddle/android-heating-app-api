package com.tomliddle

import akka.actor.{Actor, ActorLogging, Cancellable}
import com.tomliddle.Status.Status
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.math.BigDecimal.RoundingMode
import scala.sys.process._
import org.json4s._
import org.json4s.jackson.JsonMethods._

case class Work(status: HeatingStatus)
case object GetStatus
case object GetTemp
case object GetWeather
case class HeatingStatus(status: Status, targetTemp: Option[BigDecimal])
case class HeatingStatusAll(status: Status, targetTemp: Option[BigDecimal], currentTemp: Option[BigDecimal], outsideTemp: Option[BigDecimal], outlook: Option[String])
case class CheckAndSetTemp(temp: BigDecimal)

object Status extends Enumeration {
	type Status = Value
	val UNKNOWN, ON, OFF, THERMOSTAT, SET_TO = Value
}

class Worker extends Actor with ActorLogging {

	implicit val ec = ExecutionContext.Implicits.global
	var status: HeatingStatus = HeatingStatus(Status.UNKNOWN, None)
	var cancellable: Option[Cancellable] = None
	private var temp: Option[BigDecimal] = None
	private var outsideTemp: Option[BigDecimal] = None
	private var outlook: Option[String] = None

	context.system.scheduler.schedule(1 second, 1 minute, self, GetTemp)
	context.system.scheduler.schedule(4 seconds, 30 minutes, self, GetWeather)

	def receive = {
		case status : HeatingStatus ⇒ {
			this.status = status
			log.info(s"Scheduled status $status")

			cancellable.foreach(c => c.cancel())
			cancellable = None

			status.status match {
				case Status.OFF =>
					sender() ! callCommand("heating_off")
				case Status.ON =>
					sender() ! callCommand("heating_on")
				case Status.THERMOSTAT =>
					sender() ! callCommand("heating_thermostat")
				case Status.SET_TO =>
					cancellable = Some(context.system.scheduler.schedule(10 seconds, 5 minutes, self, CheckAndSetTemp(status.targetTemp.get)))
					sender() ! s"setting thermostat to ${status.targetTemp.get}"
			}
		}

		case CheckAndSetTemp(targetTemp: BigDecimal) =>
			temp match {
				case Some(currentTemp) =>
					log.debug(s"Setting temp to $targetTemp")
					if (currentTemp < targetTemp) callCommand(s"heating_on")
					else callCommand(s"heating_off")
				case None =>
					log.error("No current temperature found, cannot set")
			}

		case GetStatus =>
			sender() ! HeatingStatusAll(status.status, status.targetTemp, temp, outsideTemp, outlook)

		// Only called to read the current temp and set variable
		case GetTemp =>
			val strTemp = s"/usr/local/bin/temp"!!

			try {
				this.temp = Some(BigDecimal(strTemp.replace("\n", "")).setScale(2, RoundingMode.HALF_UP))
			} catch {
				case e: NumberFormatException => {
					log.error("Number format exception", e)
				}
			}

		case GetWeather =>
			val src = scala.io.Source.fromURL("http://api.wunderground.com/api/33949e36ea94ffcd/conditions/q/GB/London.json")
			val json = parse(src.mkString)
			src.close()

			implicit lazy val formats = org.json4s.DefaultFormats
			(json \ "current_observation" \ "temp_c").extractOpt[BigDecimal].foreach(value => outsideTemp = Some(value.setScale(2, RoundingMode.HALF_UP)))
			outlook = (json \ "current_observation" \ "weather").extractOpt[String]
	}

	private def callCommand(command: String): String = {
		val ret = s"/usr/local/bin/$command"!

		if (ret == 0) {
			val text = s"$command successful"
			log.info(text)
			text
		}
		else {
			val text = s"error cannot run command: $command"
			log.error(text)
			text
		}
	}
}
