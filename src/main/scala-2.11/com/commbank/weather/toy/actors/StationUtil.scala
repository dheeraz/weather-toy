package com.commbank.weather.toy.actors

import akka.actor.{Actor, Props}

/**
  * Created by Dheeraj on 24/10/16.
  */
class StationUtil extends Actor {

  override def receive: Receive = {
    case msg: Tuple2[String, String] => StationUtil.stationMap += (msg._1 -> msg._2)
  }
}

object StationUtil {
  private var stationMap = Map[String, String]()

  def props = Props[StationUtil]

  def stationCode(name: String): Option[String] = stationMap.get(name)
}
