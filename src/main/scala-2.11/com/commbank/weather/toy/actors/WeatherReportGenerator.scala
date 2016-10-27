package com.commbank.weather.toy.actors

import akka.actor.{Actor, ActorSystem, Props}
import org.joda.time.format.DateTimeFormat


/**
  * Created by Dheeraj on 23/10/16.
  */
class WeatherReportGenerator extends Actor {
  lazy val config = system.settings.config
  lazy val IATAKey = config.getString("weather-toy.IATA.key")
  lazy val stationService = context.actorOf(StationData.props(IATAKey), s"${self.path.name}StationService")
  implicit val system = ActorSystem()
  val weatherRouter = context.actorSelection("akka://WeatherToy/user/weatherRouter")

  override def receive: Receive = {
    case msg: WeatherDataRoot => {
      val stnName = msg.name
      val stnCode = StationUtil.stationCode(stnName)
      stnCode match {
        case Some(s) => {
          val weatherReport = genReport(msg)
          weatherRouter ! weatherReport.copy(code = s)
        }
        case None => {
          stationService ! genReport(msg)
        }
      }
    }
    case msg: WeatherReport => {
      weatherRouter ! msg
    }
  }

  private def genReport(msg: WeatherDataRoot) = {
    val stnName = msg.name
    val coord = (msg.coord.lon, msg.coord.lat)
    val dt = msg.dt
    val desc = msg.weather.head.description
    val temp = msg.main.temp
    val press = msg.main.pressure
    val humi = msg.main.humidity
    WeatherReport(null, stnName, coord, dt, desc, temp, press, humi)
  }
}

object WeatherReportGenerator {
  def props = Props[WeatherReportGenerator]
}

case class WeatherReport(code: String, name: String, coord: (Double, Double), date: Long,
                         desc: String, temp: Double, press: Double, humi: Double) {

  override def toString: String = {
    def dateFormatter(d: Long) = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ").print(d * 1000L)
    val d = dateFormatter(date)
    def bigDecimalFormatter(x: Double) = BigDecimal(x).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    val c = bigDecimalFormatter(temp - 273.15)
    s"${code}|${coord._1},${coord._2}|$d|$desc|$c|$press|$humi"
  }
}