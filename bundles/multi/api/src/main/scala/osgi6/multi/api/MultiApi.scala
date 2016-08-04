package osgi6.multi.api

import java.io.File
import javax.servlet.ServletConfig
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import osgi6.common.{BaseRegistry, MultiRegistry, PromiseRegistry}
import osgi6.scalarx.ListenableRegistry

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

object ContextApi {

  trait Handler {
    def dispatch(ctx: Context) : Unit
  }

  trait Registration {
    def remove : Unit
  }

  trait Registry {
    def listen(handler: Handler) : Registration
    def set(ctx: Context) : Unit
  }

  val registry : Registry = new PromiseRegistry[Context, Handler, Registration](
    notify = (handler, value) => handler.dispatch(value),
    unreg = remover => new Registration {
      override def remove: Unit = remover()
    }
  ) with Registry

}

trait Context {
  def name: String
  def data: File
  def log: File
  def debug: Boolean
  def stdout: Boolean
  def rootPath : String
  def servletConfig: ServletConfig
}

