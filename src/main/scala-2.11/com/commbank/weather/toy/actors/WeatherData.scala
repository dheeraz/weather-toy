package com.commbank.weather.toy.actors

import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.commbank.weather.toy.Station
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future
import scala.util.{Failure, Success}


trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val coordFormat = jsonFormat2(Coord)
  implicit val weatherFormat = jsonFormat4(Weather)
  implicit val mainFormat = jsonFormat7(Main)
  implicit val windFormat = jsonFormat2(Wind)
  implicit val couldsFormat = jsonFormat1(Clouds)
  implicit val sysFormat = jsonFormat4(Sys)
  implicit val wtrDataRootFormat = jsonFormat11(WeatherDataRoot)

}

/**
  * Created by Dheeraj on 20/10/16.
  */
class WeatherData(key: String) extends Actor with Directives with JsonSupport {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executor = system.dispatcher
  val reportGen = context.actorOf(WeatherReportGenerator.props, s"${self.path.name}ReportGen")

  override def receive: Receive = {
    case Station(name, id, cord) => {
      val responseFuture: Future[HttpResponse] =
        Http().singleRequest(HttpRequest(uri = s"http://api.openweathermap.org//data/2.5/weather?APPID=${key}&q=${name}"))

      responseFuture.onComplete {
        case Success(value) => {
          val data = Unmarshal(value.entity).to[WeatherDataRoot]
          data.onComplete {
            case Success(d) => {
              reportGen forward d
            }
            case Failure(value) => println(value)
          }
        }
        case Failure(value) => println(value) // TODO Need to improve error handling
      }
    }
  }
}

object WeatherData {
  def props(key: String) = Props(classOf[WeatherData], key)
}

case class Coord(lon: Double, lat: Double)

case class Weather(id: Double, main: String, description: String, icon: String)

case class Main(temp: Double, pressure: Double, humidity: Double, temp_min: Double, temp_max: Double,
                sea_level: Double, grnd_level: Double)

case class Wind(speed: Double, deg: Double)

case class Clouds(all: Double)

case class Sys(message: Double, country: String, sunrise: Double, sunset: Double)

case class WeatherDataRoot(coord: Coord, weather: List[Weather], base: String, main: Main, wind: Wind, clouds: Clouds,
                           dt: Long, sys: Sys, id: Double, name: String, cod: Double)

