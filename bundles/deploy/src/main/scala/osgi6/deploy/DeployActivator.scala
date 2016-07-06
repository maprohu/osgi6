package osgi6.deploy

import osgi6.akka.http.AkkaHttpActivator

/**
  * Created by martonpapp on 05/07/16.
  */

class DeployActivator extends AkkaHttpActivator(
  (ctx, system, mat) => {
    val route = OsgiDeploy.init(ctx)

    () => route
  },
  classLoader = Some(classOf[DeployActivator].getClassLoader)
)


