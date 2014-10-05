package oiw

import akka.actor._
import oiw.Protocol._
import oiw.Studio._
import spray.can.Http.ConnectionClosed
import spray.http._
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.routing.RequestContext

abstract class ServerPushEvents(ctx: RequestContext) extends Actor {

  protected object ACK

  protected val streamEnd = "\r\n\r\n"
  protected val responseStart =
    HttpResponse(entity = HttpEntity(
    ContentType(MediaType.custom("text/event-stream")), streamEnd))

  protected def serverMessage(msg: String)
    = s"""data: $msg$streamEnd"""

  def sendChunk(content: String) {
    ctx.responder ! MessageChunk(serverMessage(content)).withAck(ACK)
  }

  override def preStart() {
    ctx.responder ! ChunkedResponseStart(responseStart).withAck(ACK)
  }
}


class Performer(name: String, ctx: RequestContext) extends ServerPushEvents(ctx)  with DefaultJsonProtocol with SprayJsonSupport {

  implicit val PerformanceFormat = jsonFormat3(Performance)

  def receive = {

    case p: Performance =>
      val chunk = PerformanceFormat.write(p).compactPrint
      sendChunk(chunk)

    case ev: ConnectionClosed => secretary ! ArtistGone(name)

    case ACK =>

  }
}

class BossInformer(ctx: RequestContext) extends ServerPushEvents(ctx) {

  def receive = {

    case OrgStructure(connections, labour) =>
      import Marshalling.employes2String
      sendChunk(connections, labour)

    case ev: ConnectionClosed => secretary ! BossGone(ctx)

    case ACK =>

  }
}