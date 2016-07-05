package osgi6.runtime

import java.io.File
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import org.osgi.framework.launch.Framework
import osgi6.api.{Context, OsgiApi}

import scala.concurrent.duration._

/**
  * Created by pappmar on 23/06/2016.
  */
abstract class OsgiServlet extends HttpServlet {

  def ctx : Context

  def deploy(fw: Framework) : Unit = {
    OsgiRuntime.deployDefault(fw)
  }

  var fw : Framework = null

  override def init(): Unit = {
    fw = OsgiRuntime.init(ctx, deploy _)
  }

  override def destroy(): Unit = {
    fw.stop()
    fw.waitForStop(15.seconds.toMillis)
    fw = null
    super.destroy()
  }

  override def service(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    OsgiApi.dispatch(req, resp)
  }
}
