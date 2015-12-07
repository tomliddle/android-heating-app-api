package com.tomliddle.entity

import com.tomliddle.entity.Status.Status


object Status extends Enumeration {
	type Status = Value
	val UNKNOWN, ON, OFF, THERMOSTAT, SET_TO = Value
}

case object GetWeatherStatus
case class WeatherStatus(outsideTemp: Option[BigDecimal], outlook: Option[String])

case object GetStatus
case object GetTemp

case class HeatingStatus(status: Status, targetTemp: Option[BigDecimal])
case class HeatingStatusAll(status: Status, targetTemp: Option[BigDecimal], currentTemp: Option[BigDecimal])

case class CheckAndSetTemp(temp: BigDecimal)
