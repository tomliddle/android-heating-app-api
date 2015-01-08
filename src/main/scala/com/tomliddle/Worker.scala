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
	private var currentTemp: Option[BigDecimal] = None
	private var outsideTemp: Option[BigDecimal] = None
	private var outlook: Option[String] = None
	private var burnerOn: Option[Boolean] = None

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
					setBurnerOff()
				case Status.ON =>
					setBurnerOn()
				case Status.THERMOSTAT =>
					setToThermostat()
				case Status.SET_TO =>
					//log.debug(s"Setting temp to $targetTemp")
					cancellable = Some(context.system.scheduler.schedule(10 seconds, 5 minutes, self, CheckAndSetTemp(status.targetTemp.get)))
			}
		}

		case CheckAndSetTemp(targetTemp: BigDecimal) =>
			currentTemp match {
				case Some(currTemp) =>
					burnerOn match {
						// We have a burner status
						case Some(burnOn) =>
							if (burnOn && currTemp > targetTemp) setBurnerOff()
							else if (!burnOn && currTemp < targetTemp) setBurnerOn()

						// We don't have a burner status
						case None =>
							if (currTemp < targetTemp) setBurnerOn()
							else setBurnerOff()
					}

				case None =>
					log.error("No current temperature found, cannot set")
			}

		case GetStatus =>
			sender() ! HeatingStatusAll(status.status, status.targetTemp, currentTemp, outsideTemp, outlook)

		// Only called to read the current temp and set variable
		case GetTemp =>
			val strTemp = s"/usr/local/bin/temp"!!

			try {
				this.currentTemp = Some(BigDecimal(strTemp.replace("\n", "")).setScale(2, RoundingMode.HALF_UP))
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

	private def setBurnerOn() {
		burnerOn = Some(true)
		callCommand("heating_on")
	}

	private def setBurnerOff() {
		burnerOn = Some(false)
		callCommand("heating_off")
	}

	private def setToThermostat() {
		burnerOn = None
		callCommand("heating_thermostat")
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
