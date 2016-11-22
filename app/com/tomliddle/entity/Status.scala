package com.tomliddle.entity



object Status extends Enumeration {
	type Status = Value
	val UNKNOWN, ON, OFF, THERMOSTAT, SET_TO = Value
}