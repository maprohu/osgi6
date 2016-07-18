package osgi6.akka.http.multi

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import osgi6.common.AsyncActivator
import osgi6.multi.api.MultiApi

/**
  * Created by pappmar on 05/07/2016.
  */
object AkkaHttpMultiApiHandler {

  def apply(
    route: Route,
    filter: HttpServletRequest => Boolean = _ => true
  )(implicit
    actorSystem: ActorSystem,
    materializer: Materializer
  ) : (MultiApi.Handler, AsyncActivator.Stop) = {
    import actorSystem.dispatcher

    val (processor, cancel) = AkkaHttpServlet.processor(route, filter)

    val handler =
      new MultiApi.Handler {
        override def dispatch(request: HttpServletRequest, response: HttpServletResponse, callback: MultiApi.Callback): Unit = {
          processor(request, response)
            .foreach(callback.handled)
        }
      }

    (handler, cancel)

  }

}
