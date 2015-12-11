package com.tomliddle.controllers

import akka.actor.{ActorRef, Props, Actor, ActorSystem}
import akka.testkit.{TestProbe, TestKit, TestActorRef}
import com.tomliddle.actors.HeatingActor.{HeatingStatusAll, GetStatus}
import com.tomliddle.actors.WeatherActor.{WeatherStatus, GetWeatherStatus}
import com.tomliddle.actors.{WeatherActor, HeatingActor}
import com.tomliddle.entity._
import org.scalatra.test.scalatest._
import org.scalatest.{Ignore, FunSuiteLike}

/**
	* Test the rest endpoints.
	* The actors are tested elsewhere, so this spec is mostly concerned that
	* the endpoints are accessible and return 200's
	* TODO - get("/api/status") needs to be fixed. See notes below
	*/
class HeatingControllerSpec extends ScalatraSuite with FunSuiteLike {

	implicit val system = ActorSystem()

	val mockActor = TestActorRef[TestReplyActor]
	val heatingController = new HeatingController(mockActor, mockActor)
	addServlet(heatingController, "/*")

	test("heating on") {
		put("/api/on") {
			status should equal (200)
			body should include ("")
		}
	}

	test("heating off") {
		put("/api/off") {
			status should equal (200)
			body should include ("")
		}
	}

	test("heating thermostat") {
		put("/api/thermostat") {
			status should equal (200)
			body should include ("")
		}
	}

		test("heating set to 19") {
		put("/api/set/19") {
			status should equal (200)
			body should include ("")
		}
	}

	test("heating status") {
		get("/api/status") {
			status should equal (200)
			body should include ("")
		}
	}

	test("heating html page") {
		get("/") {
			status should equal (200)
			body should include ("")
		}
	}

	// TODO this isn't working correctly
	// There is a problem testing Akka with ScalatraSuite which needs to be resolved.
	class TestReplyActor extends Actor {
		override def receive = {
			case GetStatus =>	sender() ! HeatingStatusAll(Status.ON, Some(19), Some(18))
			case GetWeatherStatus => sender() ! WeatherStatus(Some(15), Some("Windy"))
		}
	}
}


