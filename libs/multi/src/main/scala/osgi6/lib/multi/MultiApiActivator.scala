package osgi6.lib.multi

import org.osgi.framework.BundleContext
import osgi6.common.{AsyncActivator, HasBundleContext}
import osgi6.multi.api.MultiApi

/**
  * Created by pappmar on 05/07/2016.
  */
import osgi6.lib.multi.MultiApiActivator._

class MultiApiActivator(starter: Start) extends AsyncActivator({ ctx =>
  activate(starter(ctx))
})

object MultiApiActivator {

  type Start = HasBundleContext => Run
  type Run = (MultiApi.Handler, AsyncActivator.Stop)

  def activate(
    run: Run
  ) : AsyncActivator.Stop = {
    val (handler, stop) = run

    val reg = MultiApi.registry.register(handler)

    { () =>
      reg.remove

      stop()
    }

  }

}
