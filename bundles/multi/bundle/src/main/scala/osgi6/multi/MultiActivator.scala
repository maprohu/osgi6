package osgi6.multi

import osgi6.api.OsgiApi
import osgi6.common.BaseActivator

/**
  * Created by martonpapp on 04/07/16.
  */
class MultiActivator extends BaseActivator({ ctx =>

  import scala.concurrent.ExecutionContext.Implicits.global

  val proc = MultiProcessor.apply

  val reg = OsgiApi.registry.register(proc)

  () => reg.remove

})
