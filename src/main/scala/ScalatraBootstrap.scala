import javax.servlet.ServletContext

import _root_.akka.actor.{Props, ActorSystem}
import com.tomliddle.actors.{HeatingActor, WeatherActor}
import com.tomliddle.controllers.HeatingController
import org.scalatra._

class ScalatraBootstrap extends LifeCycle {

	private val system = ActorSystem("actor_system")
	private val heatingActor = system.actorOf(Props[HeatingActor])
	private val weatherActor = system.actorOf(Props[WeatherActor])

	override def init(context: ServletContext) {
		context.mount(new HeatingController(heatingActor, weatherActor), "/*")
	}

	override def destroy(context: ServletContext) {
		system.shutdown() // shut down the actor system
	}
}

