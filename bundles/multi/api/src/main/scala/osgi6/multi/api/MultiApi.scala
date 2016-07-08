package osgi6.multi.api

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import osgi6.common.MultiRegistry

/**
  * Created by martonpapp on 04/07/16.
  */
object MultiApi extends MultiRegistry[MultiApiHandler] {

}

trait MultiApiHandler {

  def process(request: HttpServletRequest, response: HttpServletResponse, callback: MultiApiHandlerCallback) : Unit

}

trait MultiApiHandlerCallback {
  def handled(result: Boolean) : Unit
}
