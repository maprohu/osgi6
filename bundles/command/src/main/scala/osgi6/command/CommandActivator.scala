package osgi6.command

import osgi6.akka.http.{AkkaHttpStrictActivator}
import osgi6.akka.slf4j.AkkaSlf4j

/**
  * Created by martonpapp on 05/07/16.
  */

class CommandActivator extends AkkaHttpStrictActivator(
  (ctx, system, mat) => {
    val route = OsgiCommand.init(ctx)(system, mat)

    () => route
  },
  classLoader = Some(classOf[CommandActivator].getClassLoader),
  config = AkkaSlf4j.config
)


