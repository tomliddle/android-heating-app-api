package com.tomliddle;

/**
 * Created by tom on 18/12/14.
 */
object I2CController {


	val ON: Byte  = 1
	val OFF: Byte  = 1
	val OVERRIDE_OFF_ADDRESS: Int = 10
	val OVERRIDE_ON_ADDRESS: Int = 11

	val relay: I2CDevice = {
		def bus = I2CFactory.getInstance(I2CBus.BUS_1)
		bus.getDevice(0x32)
	}

	def heatingOn = relay.write(OVERRIDE_ON_ADDRESS, ON)
	def heatingOff = {
		relay.write(OVERRIDE_ON_ADDRESS, OFF)
		wait(1000)
		relay.write(OVERRIDE_OFF_ADDRESS, ON)

	}
	def heatingThermostat = {
		relay.write(OVERRIDE_ON_ADDRESS, OFF)
		wait(1000)
		relay.write(OVERRIDE_OFF_ADDRESS, OFF)
	}

}
