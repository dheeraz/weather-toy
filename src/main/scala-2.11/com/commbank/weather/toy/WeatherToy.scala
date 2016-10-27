package com.commbank.weather.toy

import akka.actor.ActorSystem
import com.commbank.weather.toy.actors.WeatherRouter
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

/**
  * Created by Dheeraj on 20/10/16.
  */
object WeatherToy extends App {

  val config = ConfigFactory.load("weather-toy")
  val stations = Seq(Station("Mumbai"), Station("Kochi"))
  val weatherStations = WeatherStations(stations)
  val system = ActorSystem("WeatherToy", config)
  val weatherRouter = system.actorOf(WeatherRouter.props(stations), "weatherRouter")
  import system.dispatcher
  system.scheduler.schedule(0 milliseconds, 1 minutes, weatherRouter, Tick)
}

case class WeatherStations(stations: Seq[Station])

case class Station(name: String = null, id: String = null, cord: (Double, Double) = (0.0, 0.0)) {
  require(name != null || id != null || cord != (0.0, 0.0))
}

case class Tick()