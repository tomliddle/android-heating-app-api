package com.tomliddle

import java.io.IOException

import akka.actor.{ActorLogging, Actor}
import com.tomliddle.WeatherActor._
import com.tomliddle.entity.{WeatherStatus, GetWeatherStatus}
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.concurrent.ExecutionContext
import scala.math.BigDecimal.RoundingMode
import scala.concurrent.duration._

object WeatherActor {
	private case object CallWeatherURL
	private val WEATHER_URL = "http://api.wunderground.com/api/33949e36ea94ffcd/conditions/q/GB/London.json"
}

/**
	* Actor to get current weather outlook
	*/
class WeatherActor extends Actor with ActorLogging {

	implicit val ec = ExecutionContext.Implicits.global
	private val weatherTimer = context.system.scheduler.schedule(5 seconds, 30 minutes, self, CallWeatherURL)
	private var weatherStatus: WeatherStatus = WeatherStatus(None, None)

	override def postStop() = weatherTimer.cancel()

	def receive = {

		case CallWeatherURL =>

				try {
					val json = parse(scala.io.Source.fromURL(WEATHER_URL).mkString)
					implicit lazy val formats = org.json4s.DefaultFormats
					val outsideTemp = (json \ "current_observation" \ "temp_c").extractOpt[BigDecimal].map(value => value.setScale(2, RoundingMode.HALF_UP))
					val outlook = (json \ "current_observation" \ "weather").extractOpt[String]
					weatherStatus = WeatherStatus(outsideTemp, outlook)
					log.info(s"outlook fetched as: $outlook")
				}
				catch {
					case ioe: IOException => log.error(ioe, s"Cannot request weather from ${WEATHER_URL}")
				}

			case GetWeatherStatus =>
				sender() ! weatherStatus
	}
}
