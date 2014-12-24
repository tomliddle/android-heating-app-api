package com.tomliddle

import _root_.akka.dispatch._
import _root_.akka.actor.{Props, Cancellable, ActorSystem}
import akka.util.Timeout
import org.json4s.{Formats, DefaultFormats}
import org.scalatra._
import org.scalatra.json.JacksonJsonSupport
import scala.sys.process._
import _root_.akka.pattern.ask
import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.duration._
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import org.scalatra.scalate.ScalateSupport


class MyServlet extends ScalatraServlet with FutureSupport with ScalateSupport with JacksonJsonSupport {

	protected implicit val timeout = Timeout(5, TimeUnit.SECONDS)
	protected implicit val jsonFormats: Formats = DefaultFormats
	protected implicit def executor: ExecutionContext = system.dispatcher

	import _root_.akka.pattern.ask

	private val logger = LoggerFactory.getLogger(getClass)
	private val system = ActorSystem("actor_system")
	private val myActor = system.actorOf(Props[Worker])


	get("/heating/on") {
		// Should have async result here really
		myActor ? HeatingStatus(Status.ON, None)
		redirect("/heating/status")
	}
	get("/heating/off") {
		(myActor ? HeatingStatus(Status.OFF, None))
		redirect("/heating/status")
	}
	get("/heating/thermostat") {
		(myActor ? HeatingStatus(Status.THERMOSTAT, None))
		redirect("/heating/status")
	}
	get("/heating/set/:temp") {
		try {
			val temp = params("temp").toFloat
			(myActor ? HeatingStatus(Status.SET_TO, Some(temp)))
		} catch {
			case e: NumberFormatException => {
				logger.error("Number format exception", e)
			}
		}
		redirect("/heating/status")
	}
	get("/heating/status") {
		contentType = formats("json")
		implicit val timeoutT = Timeout(5, TimeUnit.SECONDS)
		new AsyncResult {
			val is = (myActor ? GetStatus).mapTo[HeatingStatusAll].map {
				statusAll =>
					Map("status" -> statusAll.status.toString, "currentTemp" -> statusAll.currentTemp, "targetTemp" -> statusAll.targetTemp)
			}
		}
	}

	get("/heating") {
		contentType="text/html"
		ssp("/heating")
	}
}

