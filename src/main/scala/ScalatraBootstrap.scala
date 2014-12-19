import javax.servlet.ServletContext

import com.tomliddle.MyServlet
import org.scalatra._

class ScalatraBootstrap extends LifeCycle {
    override def init(context: ServletContext) {
        context.mount(new MyServlet(), "/*")
    }
}

