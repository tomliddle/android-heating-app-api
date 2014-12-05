

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

object I2CController {


	val ON: Byte  = 1
	val OFF: Byte  = 1
	val OVERRIDE_OFF_ADDRESS: Int = 10
	val OVERRIDE_ON_ADDRESS: Int = 11

	def getRelay: I2CDevice = {
		def bus = I2CFactory.getInstance(I2CBus.BUS_1)
		bus.getDevice(0x32)
	}
	
	def heatingOn = getRelay.write(OVERRIDE_ON_ADDRESS, ON)
	def heatingOff = {
		getRelay.write(OVERRIDE_ON_ADDRESS, OFF)
		getRelay.write(OVERRIDE_OFF_ADDRESS, ON)
	}
	def heatingThermostat = {
		getRelay.write(OVERRIDE_ON_ADDRESS, OFF)
		getRelay.write(OVERRIDE_OFF_ADDRESS, OFF)
	}

}