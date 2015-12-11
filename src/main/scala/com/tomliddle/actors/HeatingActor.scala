package com.tomliddle.actors

import akka.actor.{Actor, ActorLogging, Cancellable}
import com.tomliddle.actors.HeatingActor._
import com.tomliddle.entity.Status.Status
import com.tomliddle.entity._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.math.BigDecimal.RoundingMode
import scala.sys.process._

object HeatingActor {
	case object GetStatus
	case object GetTemp

	case class HeatingStatus(status: Status, targetTemp: Option[BigDecimal])
	case class HeatingStatusAll(status: Status, targetTemp: Option[BigDecimal], currentTemp: Option[BigDecimal])

	private case class CheckAndSetTemp(temp: BigDecimal)

}

/**
	* Actor to get/set heating settings
	*/
class HeatingActor extends Actor with ActorLogging {

	implicit val ec = ExecutionContext.Implicits.global
	private var status: HeatingStatus = HeatingStatus(Status.UNKNOWN, None)

	protected var currentTemp: Option[BigDecimal] = None
	private var burnerOn: Option[Boolean] = None

	// Read the temperature from the USB thermometer at specified intervals
	private val readTempTimer: Cancellable = context.system.scheduler.schedule(1 second, 1 minute, self, GetTemp)

	// The heating state has a timer to avoid cycling the heating on and off too often.
	var heatingStateTimer: Option[Cancellable] = None

	override def postStop() = {
		readTempTimer.cancel()
		heatingStateTimer.foreach(c => c.cancel())
	}

	def receive = {
		case status : HeatingStatus => {
			this.status = status
			log.info(s"Scheduled status $status")

			heatingStateTimer.foreach(c => c.cancel())
			heatingStateTimer = None

			status.status match {
				case Status.OFF =>
					setBurnerOff()

				case Status.ON =>
					setBurnerOn()

				// Set the heating to local controls (bypassing this heating application).
				case Status.THERMOSTAT =>
					setToThermostat()

				// Set the heating to a particular temperature. Schedule the timer to check and set the temperature
				case Status.SET_TO =>
					log.info(s"Setting temp to $status.targetTemp.get")
					status.targetTemp.foreach { targetTemp =>
						heatingStateTimer = Some(context.system.scheduler.schedule(10 seconds, 5 minutes, self, CheckAndSetTemp(targetTemp)))
					}
			}
		}

		case CheckAndSetTemp(targetTemp: BigDecimal) =>
			log.info(s"checking and setting temp target= $targetTemp curr temp $currentTemp")
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
			sender() ! HeatingStatusAll(status.status, status.targetTemp, currentTemp)

		// Only called to read the current temp and set variable
		case GetTemp =>
			val strTemp = s"./bin/temp"!!

			try {
				currentTemp = Some(BigDecimal(strTemp.replace("\n", "")).setScale(2, RoundingMode.HALF_UP))
			} catch {
				case e: NumberFormatException => {
					log.error("Number format exception", e)
				}
			}
	}

	private def setBurnerOn() {
		burnerOn = Some(true)
		callCommand("bin/heating_on")
	}

	private def setBurnerOff() {
		burnerOn = Some(false)
		callCommand("bin/heating_off")
	}

	private def setToThermostat() {
		burnerOn = None
		callCommand("bin/heating_thermostat")
	}

	/**
		* For simplicity we have a series of shell commands which are run to control the relay switches.
		* @param command shell command
		*/
	private def callCommand(command: String) {
		val ret = s"./bin/$command"!

		if (ret != 0) log.error(s"error cannot run command: $command")
	}
}
