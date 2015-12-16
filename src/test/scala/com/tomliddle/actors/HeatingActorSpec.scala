package com.tomliddle.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import com.tomliddle.actors.HeatingActor.{HeatingStatus, HeatingStatusAll, GetStatus, CheckAndSetTemp}
import com.tomliddle.actors.WeatherActor.WeatherStatus
import com.tomliddle.entity.Status
import com.tomliddle.entity._
import org.joda.time.DateTime
import org.scalatest._
import scala.concurrent.duration._
import scala.concurrent.Await
import akka.pattern.ask

import scala.util.{Try, Failure, Success}

class HeatingActorSpec extends TestKit(ActorSystem("HeatingActorSpec"))
with DefaultTimeout
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterEach
with BeforeAndAfterAll
with TryValues {

	private val currDateTime = DateTime.now

	"Heating Actor" should {

		"CheckAndSetTemp" in {
			val actorRef = heatingActor()

			actorRef ! HeatingStatus(Status.SET_TO, Some(20))

			val heatingStatusFut = actorRef ? GetStatus
			val Success(result: HeatingStatusAll) = heatingStatusFut.value.get

			result.targetTemp should be(Some(20))

		}

		"GetStatus after initial load" in {
			val actorRef = heatingActor()

			val heatingStatusFut = actorRef ? GetStatus
			heatingStatusFut.value.get should be(Success(HeatingStatusAll(Status.UNKNOWN, None, Some(15))))
		}
	}


	def heatingActor() = TestActorRef(new HeatingActor {
		currentTemp = Some(15)
	})


	override def afterAll() {
		super.afterAll
		TestKit.shutdownActorSystem(system)
	}

}