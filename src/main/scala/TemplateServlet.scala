import org.scalatra._
import scalate.ScalateSupport
import scala.sys.process._

class MyServlet extends ScalatraServlet {

    get("/temperature") {
		"pcsensor -c".!!
    }
	get("/on") {
		I2CController.heatingOn
		"heating on"
	}
	get("/off") {
		I2CController.heatingOn
		"heating off"
	}
	get("/thermostat") {
		I2CController.heatingThermostat
		"heating thermostat"
	}

}

