package oiw

import akka.actor.{Actor, ActorSystem, Props}
import akka.io.IO
import spray.can.Http

object Boot extends App {

  def address = "192.168.0.106"
  def port    = 8888
  def URL = s"""http://${address}:${port}"""

  implicit val system = ActorSystem("ScalablePictures")

  Studio initWith system

  val httpService = system.actorOf(Props[ServiceActor], "ServiceActor")

  IO(Http) ! Http.Bind(httpService, address, port)
}

class ServiceActor extends Actor
  with ResourcesService with DirectorService
  with BossService with LabourService
  with PerformerService {

  def actorRefFactory = context

  def receive = runRoute(resourcesRoute ~
    directorRoute ~
    performerRoute ~ bossRoute ~ labourRoute)

}

