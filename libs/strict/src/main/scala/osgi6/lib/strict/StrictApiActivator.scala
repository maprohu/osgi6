package osgi6.lib.strict

import org.osgi.framework.BundleContext
import osgi6.common.AsyncActivator
import osgi6.strict.api.StrictApi

/**
  * Created by pappmar on 05/07/2016.
  */
import StrictApiActivator._
class StrictApiActivator(starter: Start) extends AsyncActivator({ ctx =>
  activate(ctx, starter)
})

object StrictApiActivator {

  type Start = BundleContext => (StrictApi.Handler, AsyncActivator.Stop)

  def activate(
    ctx: BundleContext,
    starter: Start
  ) = {

    val (handler, stop) = starter(ctx)

    val reg = StrictApi.registry.register(handler)

    () => {
      reg.remove

      stop()
    }

  }

}
