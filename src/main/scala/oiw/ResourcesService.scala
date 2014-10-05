package oiw


import spray.http.StatusCodes
import spray.routing.HttpService
import spray.routing.directives.CachingDirectives._

import scala.concurrent.duration.Duration

trait ResourcesService extends HttpService {

  val simpleCache = routeCache(maxCapacity = 1000, timeToIdle = Duration("30 min"))

  val assetsDir = "src/main/resources/www"

  val resourcesRoute = {
    get {
      pathSingleSlash {
        redirect("assets/index.html", StatusCodes.Found)
      }
    } ~
    path("assets" / "index_.html") {
      cache(simpleCache) {
        complete {
          `index.html`.html
        }
      }
    } ~
    pathPrefix("assets") {
      compressResponse() {
        getFromBrowseableDirectory(assetsDir)
      }
    }
  }
}

object `index.html` {

  def scriptTag(name: String) =
    <script type='text/javascript'
            src='js/{name}.js'></script>

  val scripts = Seq("angular", "angular-route", "jquery-1.9.0.min",
    "bootstrap", "app.js", "services.js", "controllers.js",
    "gauge.js", "map.js", "head.js")

  val head =
    <head>
      <meta charset="utf-8"/>
      <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
      <title>Scalable Pictures</title>
      <link rel='stylesheet' type='text/css' href='css/bootstrap.min.css'/>
      <link rel='stylesheet' type='text/css' href='css/main.css'/>
    </head>

  val html =
    <html>
      {head}
      {body}
      {scripts map scriptTag}
    </html>

  val body =
    <body ng-app="SpraySpaDemo">
      <div class="navbar navbar-fixed-top">
        <div class="navbar-inner">
          <div class="container">
            <a class="brand" href="#">Scalable Pictures</a>
            <div class="nav-collapse collapse">
              <ul class="nav">
                <li>
                  <a href="#/config">Configuration</a>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
      <ng-view></ng-view>
    </body>

}