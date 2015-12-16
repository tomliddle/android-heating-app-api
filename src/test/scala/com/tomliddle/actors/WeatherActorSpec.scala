package com.tomliddle.actors

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import com.tomliddle.actors.WeatherActor.{WeatherStatus, GetWeatherStatus}
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, WordSpecLike}

import scala.util.{Failure, Success}

class WeatherActorSpec extends TestKit(ActorSystem("WeatherActorSpec"))
with DefaultTimeout
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterEach
with BeforeAndAfterAll {

	private val currDateTime = DateTime.now


	"Weather Actor" should {

		"CallWeatherURL" in {
			val actorRef = weatherActor()

			actorRef ! WeatherActor.CallWeatherURL

			val heatingStatusFut = actorRef ? GetWeatherStatus
			val result = heatingStatusFut.value.get

			result should be(Success(WeatherStatus(Some(14), Some("Windy"))))

		}
	}

	def weatherActor() = TestActorRef(new WeatherActor {
		override def getWeatherURL(): WeatherStatus = {
			WeatherStatus(Some(14), Some("Windy"))
		}
	})


	override def afterAll() {
		super.afterAll
		TestKit.shutdownActorSystem(system)
	}

}