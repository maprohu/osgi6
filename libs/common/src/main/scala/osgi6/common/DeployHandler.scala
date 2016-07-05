package osgi6.common

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import org.osgi.framework.BundleContext
import osgi6.api.OsgiApiHandler

/**
  * Created by pappmar on 05/07/2016.
  */
object DeployHandler {
  def process(ctx: BundleContext, request: HttpServletRequest, response: HttpServletResponse): Boolean = {
    if (request.getPathInfo == "/deployer") {
      return false
    } else {



      return true
    }
  }
}
