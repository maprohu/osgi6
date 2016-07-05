package osgi6.console

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import osgi6.actor.ActorSystemActivator
import osgi6.akka.http.AkkaHttpActivator
import osgi6.common.{BaseActivator, MultiActivator}
import osgi6.multi.api.{MultiApi, MultiApiHandler, MultiApiHandlerCallback}

/**
  * Created by martonpapp on 05/07/16.
  */
import ConsoleActivator._
class ConsoleActivator extends MultiActivator(
  new org.apache.felix.bundlerepository.impl.Activator,
  new org.apache.felix.gogo.command.Activator,
  new org.apache.felix.gogo.runtime.activator.Activator,
  new org.apache.felix.gogo.shell.Activator,
  new AkkaHttpActivator(
    () => route,
    classLoader = Some(classOf[ConsoleActivator].getClassLoader)
  )
)

object ConsoleActivator {
  import Directives._

  val route = path("test") {
    complete("OK")
  }

}
