package com.tomliddle

import java.io.File
import java.util.concurrent.TimeUnit
import _root_.akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.Timeout
import com.tomliddle.entity._
import org.scalatra._
import org.slf4j.LoggerFactory
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

import scala.concurrent.{Future, ExecutionContext}
import _root_.akka.pattern.ask

/**
	* Very simple API for getting and setting heating and weather information
	* @param system
	* @param heatingActor
	*/
class HeatingServlet(system: ActorSystem, heatingActor: ActorRef, weatherActor: ActorRef) extends ScalatraServlet with FutureSupport {

	private val logger = LoggerFactory.getLogger(getClass)
	protected implicit val jsonFormats: Formats = DefaultFormats
	protected implicit def executor: ExecutionContext = system.dispatcher


	put("/api/on") {
		heatingActor ! HeatingStatus(Status.ON, None)
		Ok("OK")
	}
	put("/api/off") {
		heatingActor ! HeatingStatus(Status.OFF, None)
		Ok("OK")
	}
	put("/api/thermostat") {
		heatingActor ! HeatingStatus(Status.THERMOSTAT, None)
		Ok("OK")
	}
	put("/api/set/:temp") {
		try {
			val temp = BigDecimal(params("temp")).setScale(2)
			heatingActor ! HeatingStatus(Status.SET_TO, Some(temp))
		} catch {
			case e: NumberFormatException =>
				logger.error("Number format exception", e)

		}
		Ok("OK")
	}
	get("/api/status") {
		contentType = "application/json"
		implicit val timeoutVal = Timeout(5, TimeUnit.SECONDS)

		val heatingStatusFut = (heatingActor ? GetStatus).mapTo[HeatingStatusAll]
		val weatherStatusFut = (weatherActor ? GetWeatherStatus).mapTo[WeatherStatus]

		for {
			h <- heatingStatusFut
			w <- weatherStatusFut
		} yield {
			compact(render(JObject(
				"status" -> h.status.toString,
				"currentTemp" -> h.currentTemp,
				"targetTemp" -> h.targetTemp,
				"outsideTemp" -> w.outsideTemp,
				"outlook" -> w.outlook
			)))
		}
	}


	get("/") {
		contentType="text/html"
		new File("src/main/webapp/heating.html")
	}
}

