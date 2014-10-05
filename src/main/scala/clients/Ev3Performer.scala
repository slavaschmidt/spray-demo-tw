package clients

import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging
import akka.io.IO
import oiw.Boot
import spray.can.Http
import spray.client.pipelining._
import spray.http._
import spray.json.JsonParser

import scala.util.{Failure, Success}

object Ev3SClient extends App {

  implicit val system = ActorSystem()
  import clients.Ev3SClient.system.dispatcher // execution context for futures

  val clientName = "EV3_Tank"

  val log = Logging(system, getClass)
  val pipeline = sendReceive

  val responseFuture = pipeline {
    Post(Boot.URL + "/labour/" + clientName + "/EV3%20Performer")
  }

  val performer = new Ev3Client()

  responseFuture onComplete {
    case Success(HttpResponse(status, entity, headers, protocol)) =>
      log.warning(status.toString())
      val get = Get(Boot.URL + "/performer/" + clientName)
      Ev3ServerObserver(get, performer)

    case Failure(error) =>
      log.error(error, "Something terrible happened")
      shutdown()

    case another => println(another)
  }

  def shutdown() {
    system.shutdown()
  }

}

object Ev3ServerObserver {

  import spray.json.DefaultJsonProtocol

  case class Message(direction: String, speed: Int)

  object JsonProtocol extends DefaultJsonProtocol {
    implicit val messageFormat = jsonFormat2(Message)
  }

  import JsonProtocol.messageFormat

  class ResponseReader(performer: Ev3Client) extends Actor {
    override def receive: Receive = {
      case MessageChunk(data, extension) =>
        val message = data.asString(HttpCharsets.`UTF-8`).replaceAll("data:", "").trim
        if (message.nonEmpty) {
          val action = JsonParser(message).convertTo[Message]
          performer.perform(action.direction, action.speed)
        }

      case ChunkedResponseStart(response) =>
      case ChunkedMessageEnd(extension, trailer) =>
        performer.shutdown()
      case Failure(exception) => // observer
    }
  }

  def apply(request: HttpRequest, performer: Ev3Client)(implicit actorSystem: ActorSystem) {
    val reader = actorSystem.actorOf(Props(new ResponseReader(performer)), "EV3PushActor")
    val processor = new SendTo(IO(Http)(actorSystem)).withResponsesReceivedBy(reader)
    processor(request)
  }
}
