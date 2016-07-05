package osgi6.command

import osgi6.akka.http.AkkaHttpActivator
import osgi6.common.MultiActivator

/**
  * Created by martonpapp on 05/07/16.
  */

class CommandActivator extends AkkaHttpActivator(
  (ctx, system, mat) => {
    val route = OsgiCommand.init(ctx)(system, mat)

    () => route
  },
  classLoader = Some(classOf[CommandActivator].getClassLoader)
)


