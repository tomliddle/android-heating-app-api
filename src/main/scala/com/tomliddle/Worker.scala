package com.tomliddle

import akka.actor.{Cancellable, ActorLogging, Actor}
import scala.sys.process._
import com.tomliddle.Status.Status
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

case class Work(status: HeatingStatus)
case object GetStatus
case object GetTemp
case class HeatingStatus(status: Status, temp: Option[Float])
case class CheckAndSetTemp(temp: Float)

object Status extends Enumeration {
	type Status = Value
	val UNKNOWN, ON, OFF, THERMOSTAT, SET_TO = Value
}

class Worker extends Actor with ActorLogging {

	implicit val ec = ExecutionContext.Implicits.global
	var status: HeatingStatus = HeatingStatus(Status.UNKNOWN, None)
	var cancellable: Option[Cancellable] = None
	private var temp: Option[Float] = None

	context.system.scheduler.schedule(1 second, 1 minute, self, GetTemp)

	def receive = {
		case status : HeatingStatus ⇒ {
			this.status = status
			log.debug(s"Scheduled status $status")

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
					cancellable = Some(context.system.scheduler.schedule(10 seconds, 5 minutes, self, CheckAndSetTemp(status.temp.get)))
					sender() ! s"setting thermostat to ${status.temp.get}"
			}
		}

		case CheckAndSetTemp(targetTemp: Float) =>
			temp match {
				case Some(currentTemp) =>
					log.debug(s"Setting temp to $targetTemp")
					if (currentTemp < targetTemp) callCommand(s"heating_on")
					else callCommand(s"heating_off")
				case None =>
					log.error("No current temperature found, cannot set")
			}

		case GetStatus =>
			sender() ! s"${status.status} ${status.temp.getOrElse(-1)} \n ${temp.getOrElse(-1)}"

		// Only called to read the current temp and set variable
		case GetTemp =>
			val strTemp = s"/usr/local/bin/temp"!!

			try {
				this.temp = Some(strTemp.toFloat)
			} catch {
				case e: NumberFormatException => {
					log.error("Number format exception", e)
				}
			}
	}

	private def callCommand(command: String): String = {
		val ret = s"/usr/local/bin/$command"!

		if (ret == 0) {
			val text = s"$command successful"
			log.debug(text)
			text
		}
		else {
			val text = s"error cannot run command: $command"
			log.error(text)
			text
		}
	}
}
