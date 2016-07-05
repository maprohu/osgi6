package osgi6.akka.http

import javax.servlet.http.HttpServletRequest

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, Materializer}
import org.osgi.framework.BundleContext
import osgi6.actor.ActorSystemActivator
import osgi6.common.AsyncActivator
import osgi6.lib.multi.MultiApiActivator

/**
  * Created by pappmar on 05/07/2016.
  */
import AkkaHttpActivator._

class AkkaHttpActivator(
  route: (BundleContext, ActorSystem, Materializer) => () => Route,
  filter: HttpServletRequest => Boolean = _ => true,
  classLoader: Option[ClassLoader] = None
) extends AsyncActivator({ ctx =>

  activate(ctx, route, filter, classLoader)

})


object AkkaHttpActivator {

  def activate(
    ctx: BundleContext,
    route: (BundleContext, ActorSystem, Materializer) => () => Route,
    filter: HttpServletRequest => Boolean = _ => true,
    classLoader: Option[ClassLoader] = None
  ) = {

    ActorSystemActivator.activate(
      ctx,
      (_, system) => {
        implicit val actorSystem = system
        implicit val materializer = ActorMaterializer()

        val routeProvider = route(ctx, actorSystem, materializer)

        MultiApiActivator.activate(
          ctx,
          _ => {
            AkkaHttpMultiApiHandler(routeProvider, filter)
          }
        )

      },
      classLoader
    )
  }

}
