package com.tomliddle

import _root_.akka.dispatch._
import _root_.akka.actor.{Props, Cancellable, ActorSystem}
import akka.util.Timeout
import org.scalatra._
import scala.sys.process._
import _root_.akka.pattern.ask
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit


class MyServlet extends ScalatraServlet with FutureSupport {

	//implicit val timeout = Timeout(5, TimeUnit.SECONDS)
	protected implicit def executor: ExecutionContext = system.dispatcher

	import _root_.akka.pattern.ask
	implicit val timeout = Timeout(5, TimeUnit.SECONDS)
	val logger = LoggerFactory.getLogger(getClass)
	val system = ActorSystem("actor_system")
	val myActor = system.actorOf(Props[Worker])


	get("/heating/on") {
		// Should have async result here really
		myActor ? HeatingStatus(Status.ON, None)
	}
	get("/heating/off") {
		(myActor ? HeatingStatus(Status.OFF, None))
	}
	get("/heating/thermostat") {
		(myActor ? HeatingStatus(Status.THERMOSTAT, None))
	}
	get("/heating/set/:temp") {
		try {
			val temp = params("temp").toFloat
			(myActor ? HeatingStatus(Status.SET_TO, Some(temp)))
		} catch {
			case e: NumberFormatException => {
				logger.error("Number format exception", e)
				e.getMessage
			}
		}
	}
	get("/heating/status") {
		(myActor ? GetStatus)
	}
}

