import org.scalatra._
import scalate.ScalateSupport

class MyServlet extends ScalatraServlet {

    get("/") {
        "Hello world!"
    }

}

