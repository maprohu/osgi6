package osgi6.admin

import java.io.PrintWriter
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import org.osgi.framework.Bundle
import osgi6.api.OsgiApi
import osgi6.common.OsgiTools
import osgi6.lib.multi.MultiApiActivator
import osgi6.multi.api.MultiApi
import osgi6.multi.api.MultiApi.Callback

import scala.concurrent.Future
import scala.util.control.NonFatal

/**
  * Created by martonpapp on 10/07/16.
  */
import AdminActivator._
class AdminActivator extends MultiApiActivator({ input =>
  val ctx = input.bundleContext


  val handler = new MultiApi.Handler {
    override def dispatch(req: HttpServletRequest, resp: HttpServletResponse, callback: Callback): Unit = {
      def processAdminRequestHtml(fn: => String) =
        processAdminRequest(fn, "text/html")

      def processAdminRequest(fn: => String, ct: String = "text/plain") = {
        resp.setContentType(ct)
        val os = resp.getOutputStream
        try {
          try {
            resp.getOutputStream.print(fn)
          } catch {
            case NonFatal(ex) =>
              val pw = new PrintWriter(resp.getOutputStream)
              ex.printStackTrace(pw)
              pw.close()
          }
        } finally {
          os.close()
        }
        callback.handled(true)
      }

      val servletPath = Option(req.getServletPath)

      servletPath match {
        case Some("/_admin/deploy") =>
          processAdminRequest {
            OsgiTools.deployBundle(
              ctx,
              req.getInputStream
            )
          }
        case Some("/_admin/list") =>
          processAdminRequest {
            ctx
              .getBundles
              .map({ bundle =>
                f"${bundle.getBundleId}%4d - ${getStateString(bundle)} - ${bundle.getSymbolicName} / ${bundle.getVersion}"
              })
              .mkString("\n")
          }
        case Some("/_admin/undeploy") =>
          processAdminRequest {
            OsgiTools.undeployBundle(
              ctx,
              Option(req.getParameter("id"))
                .getOrElse(throw new RuntimeException("'id' parameter missing"))
                .toLong
            )
          }
        case Some("/_admin/interrupt") =>
          processAdminRequest {
            Option(req.getParameter("id"))
              .map(_.toLong)
              .map({ id =>
                val threads = Array.ofDim[Thread](Thread.activeCount())

                val found =
                  threads
                    .take(
                      Thread.enumerate(threads)
                    )
                    .find(_.getId == id)

                found
                  .map({ t =>
                    t.interrupt()
                    s"thread ${id} interrupted"
                  })
                  .getOrElse({
                    s"thread ${id} not found"
                  })

              })
              .getOrElse("'id' parameter missing")

          }
        case Some("/_admin/stopThread") =>
          processAdminRequest {
            Option(req.getParameter("id"))
              .map(_.toLong)
              .map({ id =>
                val threads = Array.ofDim[Thread](Thread.activeCount())

                val found =
                  threads
                    .take(
                      Thread.enumerate(threads)
                    )
                    .find(_.getId == id)

                found
                  .map({ t =>
                    t.stop()
                    s"thread ${id} stopped"
                  })
                  .getOrElse({
                    s"thread ${id} not found"
                  })

              })
              .getOrElse("'id' parameter missing")

          }
        case Some("/_admin/threads") =>
          processAdminRequestHtml {
            val threads = Array.ofDim[Thread](Thread.activeCount())

            val found =
              threads
                .take(
                  Thread.enumerate(threads)
                )
                .sortBy(_.getName)

            s"""
              |<table border=1>
              |${
                found
                  .map(t =>
                    s"<tr><td>${t.getId}</td><td>${t.getName}</td>" +
                      s"<td><a href='interrupt?id=${t.getId}' target='admin'>interrupt</a></td>" +
                      s"<td><a href='stopThread?id=${t.getId}' target='admin'>stop</a></td></tr>"
                  )
                  .mkString("\n")
              }
              |
              |</table>
            """.stripMargin

          }



        case _ =>
          callback.handled(false)
      }
    }
  }

  (
    handler,
    () => {
      Future.successful(())
    }
  )


})

object AdminActivator {

  def getStateString(bundle: Bundle) : String = {
    getStateString(bundle.getState)
  }
  def getStateString(state: Int) : String = {
    if (state == 32) "Active     "
    else if (state == 2) "Installed  "
    else if (state == 4) "Resolved   "
    else if (state == 8) "Starting   "
    else if (state == 16) "Stopping   "
    else "Unknown    "
  }
}
