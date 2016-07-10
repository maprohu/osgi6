package osgi6.multi.api

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import osgi6.common.{BaseRegistry, MultiRegistry}

/**
  * Created by martonpapp on 04/07/16.
  */
object MultiApi {

  trait Callback {
    def handled(result: Boolean) : Unit
  }

  trait Handler {
    def dispatch(request: HttpServletRequest, response: HttpServletResponse, callback: Callback) : Unit
  }

  trait Registration {
    def remove : Unit
  }

  trait Registry {
    def register(handler: Handler) : Registration
    def iterate : java.util.Enumeration[Handler]
  }

  val registry : Registry = new BaseRegistry[Handler, Registration](
    unreg = remover => new Registration {
      override def remove: Unit = remover()
    }
  ) with Registry

}

