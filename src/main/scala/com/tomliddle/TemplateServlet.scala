package com.tomliddle

import org.scalatra._

import scala.sys.process._

class MyServlet extends ScalatraServlet {

    get("/temp") {
		"pcsensor -c".!!
    }
	get("/heating_on") {
		I2CController.heatingOn
		"heating on"
	}
	get("/heating_off") {
		I2CController.heatingOn
		"heating off"
	}
	get("/heating_thermostat") {
		I2CController.heatingThermostat
		"heating thermostat"
	}

}

