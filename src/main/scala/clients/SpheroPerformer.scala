package clients

import akka.actor.{Actor, ActorSystem, Props}
import akka.io.IO
import oiw.Boot
import spray.can.Http
import spray.client.pipelining._
import spray.http._
import spray.json.JsonParser

import scala.util.{Failure, Success}

object SpheroSClient extends App {

  implicit val system = ActorSystem()
  import clients.SpheroSClient.system.dispatcher // execution context for futures

  val clientName = "Sphero_Ball"

  val pipeline = sendReceive

  val responseFuture = pipeline {
    Post(Boot.URL + "/labour/" + clientName + "/Sphero%20Performer")
  }

  responseFuture onComplete {
    case Success(HttpResponse(status, entity, headers, protocol)) =>
      val get = Get(Boot.URL + "/performer/" + clientName)
      SpheroServerObserver(get, new SpheroClient())

    case Failure(error) => shutdown()
  }
  def shutdown(): Unit = system.shutdown()
}

object SpheroServerObserver {

  import spray.json.DefaultJsonProtocol

  case class Message(direction: String, speed: Int)

  object JsonProtocol extends DefaultJsonProtocol {
    implicit val messageFormat = jsonFormat2(Message)
  }

  import clients.SpheroServerObserver.JsonProtocol.messageFormat

  class ResponseReader(performer: SpheroClient) extends Actor {
    override def receive: Receive = {
      case MessageChunk(data, extension) =>
        val message = data.asString(HttpCharsets.`UTF-8`).replaceAll("data:", "").trim
        if (message.nonEmpty) {
          val action = JsonParser(message).convertTo[Message]
          performer.perform(action.direction, action.speed)
        }

      case ChunkedMessageEnd(extension, trailer) => performer.shutdown()

      case ChunkedResponseStart(response) =>
      case Failure(exception) =>
    }
  }

  def apply(request: HttpRequest, performer: SpheroClient)(implicit actorSystem: ActorSystem) {
    val reader = actorSystem.actorOf(Props(new ResponseReader(performer)), "EV3PushActor")
    val processor = new SendTo(IO(Http)(actorSystem)).withResponsesReceivedBy(reader)
    processor(request)
  }
}
