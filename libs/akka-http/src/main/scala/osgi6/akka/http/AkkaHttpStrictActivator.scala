package osgi6.akka.http
import javax.servlet.http.HttpServletRequest

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.{Config, ConfigFactory}
import org.osgi.framework.BundleContext
import osgi6.actor.ActorSystemActivator
import osgi6.common.AsyncActivator
import osgi6.lib.multi.MultiApiActivator
import osgi6.lib.strict.StrictApiActivator

/**
  * Created by pappmar on 05/07/2016.
  */
import AkkaHttpStrictActivator._

class AkkaHttpStrictActivator(
  route: (BundleContext, ActorSystem, Materializer) => () => Route,
  classLoader: Option[ClassLoader] = None,
  config : Config = ConfigFactory.empty()
) extends AsyncActivator({ ctx =>

  activate(ctx, route, classLoader, config)

})


object AkkaHttpStrictActivator {

  def activate(
    ctx: BundleContext,
    route: (BundleContext, ActorSystem, Materializer) => () => Route,
    classLoader: Option[ClassLoader] = None,
    config : Config = ConfigFactory.empty()
  ) = {

    ActorSystemActivator.activate(
      ctx,
      (_, system) => {
        implicit val actorSystem = system
        implicit val materializer = ActorMaterializer()

        val routeProvider = route(ctx, actorSystem, materializer)

        StrictApiActivator.activate(
          ctx,
          _ => {
            AkkaHttpStrictApiHandler(routeProvider)
          }
        )

      },
      classLoader,
      config = config
    )
  }

}
