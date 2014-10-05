package oiw

import oiw.Model.Employee
import oiw.Protocol._
import oiw.Studio._
import spray.routing.{HttpService, RequestContext}


trait PerformerService extends HttpService {

  val performerRoute = {
    get {
      path("performer" / Segment) { name =>
        informAboutDirections(name)
      }
    }
  }

  def informAboutDirections(name: String)(ctx: RequestContext) {
    secretary ! ArtistActive(name, ctx)
  }

}

trait DirectorService extends HttpService {

  val directorRoute = {
    post {
      path("arrowEvent" / Segment / Segment / Segment) { (name, action, direction) =>
        val performance = arrowPerformance(name, action, direction)
        perform(performance)
      }
    } ~
    post {
      path("angleEvent" / Segment) { name =>
        parameters('angle.as[Double]) { angle =>
          validate(math.abs(angle) < 90, "Angle should be in range [-90; 90]")
          val performance = headPerformance(name, angle)
          perform(performance)
        }
      }
    } ~
    get {
      path("pebble_data" / Segment / DoubleNumber / DoubleNumber) { (name, x, y) =>
        val angle = y / 1000 * 30
        validate(math.abs(angle) < 90, "Angle should be in range [-90; 90]")
        val performance = headPerformance(name, angle)
        perform(performance)
      }
    }

  }

  def arrowPerformance(name: String, action: String, direction: String) = action match {
    case "pressed" => Performance(direction, 100, name)
    case "released" => Performance(direction, 0, name)
  }

  def headPerformance(name: String, angle: Double) = {
    val direction = if (angle < 0) "left" else "right"
    val speed = math.abs(math.round(angle * 3d).toInt)
    Performance(direction, speed, name)
  }

  private def perform(p: Performance) = {
    Studio.secretary ! p
    complete(202, "OK")
  }
}

trait LabourService extends HttpService {

  val labourRoute = {
    post {
      path("labour" / Segment / Segment) { (name, clientType) =>
        val worker = Employee.byTypeName(clientType, name).getOrElse {
          complete(501, "Unrecognized worker type")
        }
        Studio.secretary ! worker
        complete(202, "OK")
      }
    }
  }
}

trait BossService extends HttpService {

  val bossRoute = {
    get {
      path("subscribe") {
        newBoss
      }
    } ~
      post {
        path("disconnect" / Segment / Segment) { (producer, actor) =>
          secretary ! Disconnect(producer, actor)
          complete(202, "OK")
        }
      } ~
      post {
        path("connect" / Segment / Segment) { (producer, actor) =>
          secretary ! Connect(producer, actor)
          complete(202, "OK")
        }
      }
  }

  def newBoss(ctx: RequestContext) {
    secretary ! NewBoss(ctx)
  }
}
