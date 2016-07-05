package osgi6.common

import java.io.{InputStream, PrintWriter, StringWriter}
import java.net.URL

import org.osgi.framework.BundleContext
import org.osgi.framework.launch.Framework
import org.osgi.framework.startlevel.BundleStartLevel

import scala.util.Try

/**
  * Created by pappmar on 05/07/2016.
  */
object OsgiTools {


  def deploy(fw: Framework, bundles: URL*) : Unit = {
    val ctx = fw.getBundleContext
    deployBundle(ctx, bundles:_*)
  }

  def deployBundle(ctx: BundleContext, bundles: URL*) : Unit = {

    val installed =
      bundles
        .map({ url =>
          ctx.installBundle(url.toExternalForm, url.openStream())
        })

    installed.foreach({ bundle =>
      bundle.adapt(classOf[BundleStartLevel]).setStartLevel(1)
      bundle.start()
    })
  }

  def deployBundle(ctx: BundleContext, stream: InputStream) : String = {
    try {
      val bundle = ctx.installBundle("file:stream", stream)
      bundle.adapt(classOf[BundleStartLevel]).setStartLevel(1)
      bundle.start()
      bundle.getBundleId.toString
    } catch {
      case ex: Throwable =>
        val sw = new StringWriter()
        val pw = new PrintWriter(sw)
        ex.printStackTrace(pw)
        pw.close()
        sw.close()
        sw.toString
    }
  }
}
