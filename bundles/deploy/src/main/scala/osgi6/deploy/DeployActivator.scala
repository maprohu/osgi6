package osgi6.deploy

import osgi6.akka.http.{AkkaHttpStrictActivator}
import osgi6.akka.slf4j.AkkaSlf4j

/**
  * Created by martonpapp on 05/07/16.
  */

class DeployActivator extends AkkaHttpStrictActivator(
  (ctx, system, mat) => {
    val route = OsgiDeploy.init(ctx)

    () => route
  },
  classLoader = Some(classOf[DeployActivator].getClassLoader),
  config = AkkaSlf4j.config
)


