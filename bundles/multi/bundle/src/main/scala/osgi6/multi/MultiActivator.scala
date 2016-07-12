package osgi6.multi

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import osgi6.api.OsgiApi
import osgi6.common.{BaseActivator, HygienicThread}
import osgi6.lib.multi.MultiProcessor
import osgi6.multi.api.MultiApi

import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.concurrent.duration._

/**
  * Created by martonpapp on 04/07/16.
  */
//@deprecated("do not use global executioncontext in osgi - fails to unload")
class MultiActivator extends BaseActivator({ ctx =>

//  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val (ec, pool) = HygienicThread.createExecutionContext

  val proc = MultiActivator.osgiHandler

  val reg = OsgiApi.registry.register(proc)

  () => {
    reg.remove
    pool()
  }

})
object MultiActivator {

  def osgiHandler(implicit executionContext: ExecutionContext) : OsgiApi.Handler = {
    new OsgiApi.Handler {
      override def process(request: HttpServletRequest, response: HttpServletResponse): Unit = {

        val processed = Await.result(
          MultiProcessor.process(request, response),
          1.minute
        )

        if (!processed) {
          response.setStatus(HttpServletResponse.SC_NOT_FOUND)
        }

      }
    }

  }



}
