package osgi6.multi

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import osgi6.api.OsgiApiHandler
import osgi6.multi.api.MultiApi

/**
  * Created by martonpapp on 04/07/16.
  */
object MultiProcessor extends OsgiApiHandler {
  override def process(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    import scala.collection.JavaConversions._

    MultiApi.iterate.find(_.process(request, response))
      .getOrElse({
        response.setStatus(HttpServletResponse.SC_NOT_FOUND)
      })

  }
}
