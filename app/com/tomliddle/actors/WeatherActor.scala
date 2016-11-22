package com.tomliddle.actors

import akka.actor.{Actor, ActorLogging}
import play.api.libs.json.Json
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

object WeatherActor {
	case object CallWeatherURL
	case object GetWeatherStatus
	case class WeatherStatus(outsideTemp: Option[BigDecimal], outlook: Option[String])
	private val WeatherUrl = "http://api.wunderground.com/api/33949e36ea94ffcd/conditions/q/GB/London.json"
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
			val self = context.self
			getWeatherURL.onComplete {
				case Success(w) => self ! w
				case Failure(ioe) => log.error(ioe, s"Cannot request weather from $WeatherUrl")
			}

			case w: WeatherStatus =>
				weatherStatus = w

			case GetWeatherStatus =>
				log.info(s"$weatherStatus")
				sender() ! weatherStatus
	}

	def getWeatherURL: Future[WeatherStatus] = {
		Future[WeatherStatus] {
			val json = Json.parse(scala.io.Source.fromURL(WeatherUrl).mkString)

			val outsideTemp = (json \ "current_observation" \ "temp_c").toOption.map(t => BigDecimal(t.toString))
			val outlook = (json \ "current_observation" \ "weather").toOption.map(_.toString)
			log.info(s"outlook fetched as: $outlook")
			WeatherStatus(outsideTemp, outlook)
		}
	}
}
