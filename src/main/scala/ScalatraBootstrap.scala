import javax.servlet.ServletContext

import _root_.akka.actor.{Props, ActorSystem}
import com.tomliddle.{WeatherActor, HeatingActor, HeatingServlet}
import org.scalatra._

class ScalatraBootstrap extends LifeCycle {

	private val system = ActorSystem("actor_system")
	private val heatingActor = system.actorOf(Props[HeatingActor])
	private val weatherActor = system.actorOf(Props[WeatherActor])

	override def init(context: ServletContext) {
		context.mount(new HeatingServlet(system, heatingActor, weatherActor), "/*")
	}

	override def destroy(context: ServletContext) {
		system.shutdown() // shut down the actor system
	}
}

