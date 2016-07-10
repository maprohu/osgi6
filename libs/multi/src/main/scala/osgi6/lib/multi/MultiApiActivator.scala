package osgi6.lib.multi

import org.osgi.framework.BundleContext
import osgi6.common.AsyncActivator
import osgi6.multi.api.{MultiApi}

/**
  * Created by pappmar on 05/07/2016.
  */
import osgi6.lib.multi.MultiApiActivator._

class MultiApiActivator(starter: Start) extends AsyncActivator({ ctx =>
  activate(ctx, starter)
})

object MultiApiActivator {

  type Start = BundleContext => (MultiApi.Handler, AsyncActivator.Stop)

  def activate(
    ctx: BundleContext,
    starter: Start
  ) = {

    val (handler, stop) = starter(ctx)

    val reg = MultiApi.registry.register(handler)

    () => {
      reg.remove

      stop()
    }

  }

}
