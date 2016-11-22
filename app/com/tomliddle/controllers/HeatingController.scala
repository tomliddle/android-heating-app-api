package com.tomliddle.controllers

import java.util.concurrent.TimeUnit
import javax.inject.Inject
import _root_.akka.actor.{ActorSystem, Props}
import _root_.akka.pattern.ask
import akka.util.Timeout
import com.tomliddle.actors.{HeatingActor, WeatherActor}
import com.tomliddle.actors.HeatingActor.{GetStatus, HeatingStatus, HeatingStatusAll, Result}
import com.tomliddle.actors.WeatherActor.{GetWeatherStatus, WeatherStatus}
import org.slf4j.LoggerFactory
import play.api.{Configuration, Environment}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import scala.concurrent.ExecutionContext



/**
	* Very simple API for getting and setting heating and weather information
	*/
class HeatingController @Inject() (system: ActorSystem, environment: Environment, configuration: Configuration) extends Controller {

	private val logger = LoggerFactory.getLogger(getClass)
	protected implicit def executor = ExecutionContext.Implicits.global
	implicit val res = Json.format[Result]

	private val heatingActor = system.actorOf(Props(new HeatingActor(environment, configuration)))
	private val weatherActor = system.actorOf(Props[WeatherActor])

	def index = Action {
		Ok(views.html.heating.render())
	}

	def on = Action {
		heatingActor ! HeatingStatus(com.tomliddle.entity.Status.ON, None)
		Ok("")
	}

	def off = Action {
		heatingActor ! HeatingStatus(com.tomliddle.entity.Status.OFF, None)
		Ok("")
	}

	def thermostat = Action {
		heatingActor ! HeatingStatus(com.tomliddle.entity.Status.THERMOSTAT, None)
		Ok("")
	}

	def temp(t: String) = Action {
		try {
			val temp = BigDecimal(t).setScale(2)
			heatingActor ! HeatingStatus(com.tomliddle.entity.Status.SET_TO, Some(temp))
		} catch {
			case e: NumberFormatException =>
				logger.error("Number format exception", e)

		}
		Ok("")
	}

	def status = Action.async {
		implicit val timeoutVal = Timeout(5, TimeUnit.SECONDS)

		val heatingStatusFut = (heatingActor ? GetStatus).mapTo[HeatingStatusAll]
		val weatherStatusFut = (weatherActor ? GetWeatherStatus).mapTo[WeatherStatus]

		for {
			h <- heatingStatusFut
			w <- weatherStatusFut
		} yield {
			Ok(Json.toJson(h.toResult(w)))
		}
	}
}

