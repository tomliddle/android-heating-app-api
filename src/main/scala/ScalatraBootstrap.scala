import javax.servlet.ServletContext

import _root_.akka.actor.{Props, ActorSystem}
import com.tomliddle.{Worker, MyServlet}
import org.scalatra._

class ScalatraBootstrap extends LifeCycle {

	private val system = ActorSystem("actor_system")
	private val myActor = system.actorOf(Props[Worker])

	override def init(context: ServletContext) {
		context.mount(new MyServlet(system, myActor), "/*")
	}

	override def destroy(context: ServletContext) {
		system.shutdown() // shut down the actor system
	}
}

