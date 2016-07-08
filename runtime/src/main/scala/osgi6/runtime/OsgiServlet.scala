package osgi6.runtime

import java.io.{File, PrintWriter}

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import org.osgi.framework.Bundle
import org.osgi.framework.launch.Framework
import org.osgi.framework.wiring.FrameworkWiring
import osgi6.api.{Context, OsgiApi}
import osgi6.common.OsgiTools

import scala.concurrent.duration._
import scala.util.control.NonFatal
import scala.collection.JavaConversions._

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

  def getStateString(bundle: Bundle) = {
    val state = bundle.getState()
      if (state == 32) "Active     "
      else if (state == 2) "Installed  "
      else if (state == 4) "Resolved   "
      else if (state == 8) "Starting   "
      else if (state == 16) "Stopping   "
      else "Unknown    "
  }

  override def service(req: HttpServletRequest, resp: HttpServletResponse): Unit = {

    def processAdminRequest(fn: => String) = {
      resp.setContentType("text/plain")
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

    }

    val servletPath = Option(req.getServletPath)

    servletPath match {
      case Some("/_admin/deploy") =>
        processAdminRequest {
          OsgiTools.deployBundle(
            fw.getBundleContext,
            req.getInputStream
          )
        }
      case Some("/_admin/list") =>
        processAdminRequest {
          fw
            .getBundleContext
            .getBundles
            .map({ bundle =>
              f"${bundle.getBundleId}%4d - ${getStateString(bundle)} - ${bundle.getSymbolicName} / ${bundle.getVersion}"
            })
            .mkString("\n")
        }
      case Some("/_admin/undeploy") =>
        processAdminRequest {
          OsgiTools.undeployBundle(
            fw,
            Option(req.getParameter("id"))
              .getOrElse(throw new RuntimeException("'id' parameter missing"))
              .toLong
          )
        }
      case Some("/_admin/refresh") =>
        processAdminRequest {
          fw.adapt(classOf[FrameworkWiring]).refreshBundles(
            fw.getBundleContext.getBundles.toSeq
          )
          "bundles refreshed"
        }

      case _ =>
        OsgiApi.registry.first.process(req, resp)
    }
  }
}
