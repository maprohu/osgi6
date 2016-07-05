package osgi6.akka.http

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import osgi6.multi.api.{MultiApiHandler, MultiApiHandlerCallback}

/**
  * Created by pappmar on 05/07/2016.
  */
object AkkaHttpMultiApiHandler {

  def apply(
    route: () => Route,
    filter: HttpServletRequest => Boolean = _ => true
  )(implicit
    actorSystem: ActorSystem,
    materializer: Materializer
  ) = {
    import actorSystem.dispatcher

    val (processor, cancel) = AkkaHttpServlet.processor(route, filter)

    val handler =
      new MultiApiHandler {
        override def process(request: HttpServletRequest, response: HttpServletResponse, callback: MultiApiHandlerCallback): Unit = {
          processor(request, response)
            .foreach(callback.handled)
        }
      }

    (handler, cancel)

  }

}
