package osgi6.akka.http.multi

import javax.servlet.http.HttpServletRequest

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.{Config, ConfigFactory}
import org.osgi.framework.BundleContext
import osgi6.actor.ActorSystemActivator
import osgi6.akka.stream.AkkaStreamActivator
import osgi6.common.{AsyncActivator, BaseActivator}
import osgi6.lib.multi.MultiApiActivator
import osgi6.multi.api.MultiApi

/**
  * Created by pappmar on 05/07/2016.
  */
import osgi6.akka.http.multi.AkkaHttpMultiActivator._

class AkkaHttpMultiActivator(
  starter: Start,
  filter: HttpServletRequest => Boolean = _ => true,
  classLoader: Option[ClassLoader] = None,
  config : Config = ConfigFactory.empty()
) extends AkkaStreamActivator(
  { ctx =>
    import ctx._
    import actorSystem.dispatcher

    val (route, routeStop) = starter(ctx)

    AsyncActivator.stops(
      MultiApiActivator.activate(
        AkkaHttpMultiApiHandler(route, filter)
      ),
      routeStop
    )

  },
  classLoader = classLoader,
  config = config
)


object AkkaHttpMultiActivator {

  type Input = ActorSystemActivator.Input
  type Run = (Route, AsyncActivator.Stop)
  type Start = Input => Run

}
