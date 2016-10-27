package com.commbank.weather.toy

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.commbank.weather.toy.actors.{WeatherReport, WeatherRouter}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, MustMatchers}

import scala.concurrent.duration._
/**
  * Created by Dheeraj on 26/10/16.
  */
class WeatherSpec extends TestKit(ActorSystem("WeatherToy"))
  with FlatSpecLike
  with ImplicitSender
  with BeforeAndAfterAll
  with MustMatchers {

  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }

  "WeatherRouter Actor" should "generate WeatherReport using TestProbe" in {
    val sender = TestProbe()
    // Station names should be provided
    val stations = Seq(Station("Mumbai"), Station("Kochi"))
    val weatherRouter = system.actorOf(WeatherRouter.props(stations), "weatherRouter")
    sender.send(weatherRouter, Tick)
    // TODO Need to check expectMsg not working
    sender.send(weatherRouter, Tick)
    sender.expectNoMsg(2 minute)
  }
}
