package com.tomliddle.actors

import java.io.IOException

import akka.actor.{Actor, ActorLogging}
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.math.BigDecimal.RoundingMode

object WeatherActor {
	case object CallWeatherURL
	case object GetWeatherStatus
	case class WeatherStatus(outsideTemp: Option[BigDecimal], outlook: Option[String])
	private val WEATHER_URL = "http://api.wunderground.com/api/33949e36ea94ffcd/conditions/q/GB/London.json"
}

/**
	* Actor to get current temp & weather outlook
	*/
class WeatherActor extends Actor with ActorLogging {

	import WeatherActor._
	implicit val ec = ExecutionContext.Implicits.global
	private val weatherTimer = context.system.scheduler.schedule(5 seconds, 30 minutes, self, CallWeatherURL)
	private var weatherStatus: WeatherStatus = WeatherStatus(None, None)

	override def postStop() = weatherTimer.cancel()

	def receive = {

		case CallWeatherURL =>
				try {
					weatherStatus = getWeatherURL()
				}
				catch {
					case ioe: IOException => log.error(ioe, s"Cannot request weather from ${WEATHER_URL}")
				}

			case GetWeatherStatus =>
				sender() ! weatherStatus
	}

	def getWeatherURL(): WeatherStatus = {
		val json = parse(scala.io.Source.fromURL(WEATHER_URL).mkString)
		implicit lazy val formats = org.json4s.DefaultFormats
		val outsideTemp = (json \ "current_observation" \ "temp_c").extractOpt[BigDecimal].map(value => value.setScale(2, RoundingMode.HALF_UP))
		val outlook = (json \ "current_observation" \ "weather").extractOpt[String]
		log.info(s"outlook fetched as: $outlook")
		WeatherStatus(outsideTemp, outlook)
	}
}
