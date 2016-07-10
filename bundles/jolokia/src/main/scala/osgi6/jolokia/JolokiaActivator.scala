package osgi6.jolokia

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import org.jolokia.http.AgentServlet
import org.springframework.mock.web.{MockServletConfig, MockServletContext}
import osgi6.api.OsgiApi
import osgi6.lib.multi.MultiApiActivator
import osgi6.multi.api.MultiApi
import osgi6.multi.api.MultiApi.Callback

import scala.concurrent.Future
import scala.util.Try

/**
  * Created by martonpapp on 10/07/16.
  */
class JolokiaActivator extends MultiApiActivator({ ctx =>

  val servlet = new AgentServlet()
  servlet.init(

    (try {
      Option(OsgiApi.servletConfig)
    } catch {
      case _ : NoSuchMethodError =>
        None
    })
      .getOrElse({
        val servletContext = new MockServletContext()
        val servletConfig = new MockServletConfig(servletContext, "jolokia-mock")
        servletConfig.getServletContext
        servletConfig
      })
  )

  val handler = new MultiApi.Handler {
    override def dispatch(request: HttpServletRequest, response: HttpServletResponse, callback: Callback): Unit = {
      val servletPath = Option(request.getServletPath)

      servletPath match {
        case Some(path) if
          path == "/jolokia" ||
          path.startsWith("/jolokia/") =>

          servlet.service(request, response)
          callback.handled(true)
        case _ =>
          callback.handled(false)
      }
    }
  }

  (
    handler,
    () => {
      servlet.destroy()
      Future.successful(())
    }
  )


})
