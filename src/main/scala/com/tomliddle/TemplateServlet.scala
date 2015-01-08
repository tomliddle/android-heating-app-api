package com.tomliddle

import java.io.File
import java.util.concurrent.TimeUnit
import _root_.akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.Timeout
import org.scalatra._
import org.slf4j.LoggerFactory
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

import scala.concurrent.ExecutionContext


class MyServlet(system: ActorSystem, myActor: ActorRef) extends ScalatraServlet with FutureSupport {

	protected implicit val timeout = Timeout(5, TimeUnit.SECONDS)
	protected implicit val jsonFormats: Formats = DefaultFormats
	protected implicit def executor: ExecutionContext = system.dispatcher

	import _root_.akka.pattern.ask

	private val logger = LoggerFactory.getLogger(getClass)

	get("/heating/on") {
		// Should have async result here really
		myActor ! HeatingStatus(Status.ON, None)
		""
	}
	get("/heating/off") {
		myActor ! HeatingStatus(Status.OFF, None)
		""
	}
	get("/heating/thermostat") {
		myActor ! HeatingStatus(Status.THERMOSTAT, None)
		""
	}
	get("/heating/set/:temp") {
		try {
			val temp = BigDecimal(params("temp")).setScale(2)
			myActor ! HeatingStatus(Status.SET_TO, Some(temp))
		} catch {
			case e: NumberFormatException => {
				logger.error("Number format exception", e)
			}
		}
		""
	}
	get("/heating/status") {
		contentType = "application/json"
		implicit val timeoutT = Timeout(5, TimeUnit.SECONDS)
		new AsyncResult {
			val is = (myActor ? GetStatus).mapTo[HeatingStatusAll].map {
				statusAll =>
					compact(render(JObject(
						"status" -> statusAll.status.toString,
						"currentTemp" -> statusAll.currentTemp,
						"targetTemp" -> statusAll.targetTemp,
						"outsideTemp" -> statusAll.outsideTemp,
						"outlook" -> statusAll.outlook
					)))
			}
		}
	}

	get("/heating") {
		contentType="text/html"
		new File("src/main/webapp/heating.html")
	}
}

