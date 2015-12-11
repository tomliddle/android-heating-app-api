package com.tomliddle.entity

import com.tomliddle.entity.Status.Status


object Status extends Enumeration {
	type Status = Value
	val UNKNOWN, ON, OFF, THERMOSTAT, SET_TO = Value
}


