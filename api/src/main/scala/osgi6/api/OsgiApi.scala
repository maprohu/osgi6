package osgi6.api

import java.io.File
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

/**
  * Created by pappmar on 23/06/2016.
  */
trait Context {
  def name: String
  def data: File
  def log: File
  def debug: Boolean
  def console : Boolean
}

object OsgiApi {

  var context : Context = null

  private [api] val defaultHandler = new OsgiApiHandler {
    override def process(req: HttpServletRequest, res: HttpServletResponse): Unit = {
      res.getWriter.println("no handler")
    }
  }

  private [api] var handlers = List[OsgiApiHandler](defaultHandler)

  private [api] def activeHandler = this.synchronized { handlers.head }

  def dispatch(req: HttpServletRequest, res: HttpServletResponse): Unit = {
    activeHandler.process(req, res)
  }


  def register(handler: OsgiApiHandler) : Unit = this.synchronized {
    handlers = handler +: handlers
  }

  def unregister(handler: OsgiApiHandler) : Unit = this.synchronized {
    handlers = handlers diff Seq(handler)
  }

}

trait OsgiApiHandler {

  def process(request: HttpServletRequest, response: HttpServletResponse)

}

