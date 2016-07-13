package osgi6.multi

import java.io.File
import javax.servlet.ServletConfig
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import osgi6.api.OsgiApi
import osgi6.common.{BaseActivator, HygienicThread}
import osgi6.lib.multi.MultiProcessor
import osgi6.multi.api.{Context, ContextApi, MultiApi}

import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.concurrent.duration._

/**
  * Created by martonpapp on 04/07/16.
  */
//@deprecated("do not use global executioncontext in osgi - fails to unload")
class MultiActivator extends BaseActivator({ ctx =>

//  import scala.concurrent.ExecutionContext.Implicits.global


  val c = OsgiApi.context

  val mc = new Context {
    override def name: String = c.name

    override def rootPath: String = ""

    override def log: File = c.log

    override def data: File = c.data

    override def debug: Boolean = c.debug

    override def stdout: Boolean = c.stdout

    override def servletConfig: ServletConfig = OsgiApi.servletConfig
  }

  val unset = ContextApi.registry.set(
    mc
  )

  implicit val (ec, pool) = HygienicThread.createExecutionContext


  val proc = MultiActivator.osgiHandler

  val reg = OsgiApi.registry.register(proc)

  () => {
    reg.remove
    unset.remove
    pool()
  }

})
object MultiActivator {

  def osgiHandler(implicit executionContext: ExecutionContext) : OsgiApi.Handler = {
    new OsgiApi.Handler {
      override def process(request: HttpServletRequest, response: HttpServletResponse): Unit = {
        MultiProcessor.processSync(request, response)
      }
    }

  }



}
