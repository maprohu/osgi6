package osgi6.api

import java.io.File
import javax.servlet.{ServletConfig, ServletContext}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import osgi6.common.BaseRegistry

/**
  * Created by pappmar on 23/06/2016.
  */
trait Context {
  def name: String
  def data: File
  // this should not be here. it is only information for OsgiRuntime
  def version: Int
  def log: File
  def debug: Boolean
  def stdout: Boolean
  def console : Boolean
}

object OsgiApi {

  var context : Context = null

  var servletConfig : ServletConfig = null

  trait Handler {
    def process(req: HttpServletRequest, res: HttpServletResponse): Unit
  }

  trait Registration {
    def remove : Unit
  }

  trait Registry {
    def register(handler: Handler) : Registration
    def first : Handler
  }

  val registry : Registry = new BaseRegistry[Handler, Registration](
    unreg = remover => new Registration {
      override def remove: Unit = remover()
    }
  ) with Registry

}


