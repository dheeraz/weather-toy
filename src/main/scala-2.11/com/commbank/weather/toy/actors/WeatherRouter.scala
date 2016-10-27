package com.commbank.weather.toy.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.commbank.weather.toy.{Station, Tick}

/**
  * Created by Dheeraj on 21/10/16.
  */
class WeatherRouter(stations: Seq[Station]) extends Actor {
  implicit val system = ActorSystem()
  val config = system.settings.config
  val key = config.getString("weather-toy.services.key")
  val weatherDataActors = stations.map(s => context.actorOf(WeatherData.props(key), s"${s.name}WeatherData"))

  override def receive: Receive = {
    case Tick => {
      (stations zip weatherDataActors).map(data => data._2.asInstanceOf[ActorRef] forward data._1)
    }
    case x: WeatherReport => {
      // TODO Output need to be improved
      println(x)
    }
  }
}

object WeatherRouter {
  def props(stations: Seq[Station]) = Props(classOf[WeatherRouter], stations)
}

