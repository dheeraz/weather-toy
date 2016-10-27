package com.commbank.weather.toy.actors

import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by Dheeraj on 24/10/16.
  */

trait StnJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val keyFormat = jsonFormat9(Key)
  implicit val paramsFormat = jsonFormat2(Params)
  implicit val deviceFormat = jsonFormat1(Device)
  implicit val agentFormat = jsonFormat3(Agent)
  implicit val clientFormat = jsonFormat8(Client)
  implicit val requestFormat = jsonFormat11(Request)

  implicit val airportFormat = jsonFormat3(Airports)
  implicit val citiesFormat = jsonFormat3(Cities)
  implicit val citiesByArptFormat = jsonFormat3(Cities_by_airports)
  implicit val countriesFormat = jsonFormat5(Countries)
  implicit val countriesByCntryFormat = jsonFormat3(Cities_by_countries)
  implicit val airportByCntryFormat = jsonFormat3(Airports_by_countries)
  implicit val responseFormat = jsonFormat7(Response)
  implicit val stnDataRootFormat = jsonFormat2(StnDataRoot)
}

class StationData(key: String) extends Actor with Directives with StnJsonSupport {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executor = system.dispatcher

  val parent = context.actorSelection(self.path.parent)
  val stationUtil = context.actorOf(StationUtil.props)

  override def receive: Receive = {
    case report: WeatherReport => {
      val responseFuture: Future[HttpResponse] =
        Http().singleRequest(HttpRequest(uri = s"https://iatacodes.org/api/v6/autocomplete?api_key=${key}&query=${report.name}"))
      responseFuture.onComplete {

        case Success(value) => {
          val data = Unmarshal(value.entity).to[StnDataRoot]
          data.onComplete {
            case Success(data) => {
              val cities = data.response.cities

              cities.foreach(c => {
                stationUtil ! (report.name, c.code)
                parent ! report.copy(code = c.code)
              })
            }
            case Failure(value) => println(value)
          }
        }
        case Failure(value) => println(value)
      }
    }
  }
}

object StationData {
  def props(key: String) = Props(classOf[StationData], key)
}


case class Airports(code: String, name: String, country_name: String)

case class Cities(code: String, name: String, country_name: String)

case class Cities_by_airports(code: String, name: String, country_name: String)

case class Countries(code: String, code3: String, iso_numeric: Double, name: String, languages: List[String])

case class Cities_by_countries(code: String, name: String, country_name: String)

case class Airports_by_countries(code: String, name: String, country_code: String)

case class Key(id: Double, api_key: String, `type`: String, expired: Option[String], registered: String,
               limits_by_hour: Double, limits_by_minute: Double, usage_by_hour: Double, usage_by_minute: Double)

case class Params(query: String, lang: String)

case class Device(`type`: String)

case class Agent(browser: String, os: String, platform: String)

case class Client(country_code: String, country: String, city: String, lat: Double, lng: Double, ip: String,
                  device: Device, agent: Agent)

case class Request(lang: String, currency: String, time: Double, id: Double, server: String, pid: Double, key: Key,
                   params: Params, version: Double, method: String, client: Client)

case class Response(airports: List[Airports], cities: List[Cities], countries: List[Countries],
                    cities_by_countries: List[Cities_by_countries], airports_by_countries: List[Airports_by_countries],
                    cities_by_airports: List[Cities_by_airports], airports_by_cities: List[Cities])

case class StnDataRoot(request: Request, response: Response)
