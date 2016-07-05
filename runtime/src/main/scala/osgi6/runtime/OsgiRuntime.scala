package osgi6.runtime

import java.io.File
import java.net.URL

import org.apache.felix.main.AutoProcessor
import org.osgi.framework.Constants
import org.osgi.framework.launch.{Framework, FrameworkFactory}
import org.osgi.framework.startlevel.BundleStartLevel
import org.osgi.service.startlevel.StartLevel
import osgi6.api.{Context, OsgiApi}
import sbt.io.IO
import sun.misc.Service
import sbt.io.Path._

import scala.collection.JavaConversions._

/**
  * Created by pappmar on 22/06/2016.
  */
object OsgiRuntime {

  case class Ctx(
    name: String,
    data: File,
    log: File,
    debug: Boolean,
    init: File => Unit = _ => (),
    console: Boolean = false
  ) extends Context

  def context(dir: File, app: String) : Ctx = {


    val data = dir / "data" / app
    val log = dir / "logs"

    Ctx(
      name = app,
      data = data,
      log = log,
      debug = false
    )
  }

  def init(ctx: Context, deploy: Framework => Unit) = {
    OsgiApi.context = ctx

    val data = ctx.data

    val first = !data.exists()

    if (first) {
      data.mkdirs()

//      IO.unzipURL(getClass.getResource("resources.zip"), data)
//      ctx.init(data)
    }

    val autoDeployDir = data / "bundle"


    val props = Map[String, String](
      Constants.FRAMEWORK_STORAGE -> (data / "felix-cache").getAbsolutePath,
      Constants.FRAMEWORK_BOOTDELEGATION ->
        """
          |sun.misc,
          |sun.security.util,
          |sun.security.x509
        """.stripMargin.replaceAll("\\s", ""),
      Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA ->
        """
          |osgi6.api,
          |javax.servlet;version="2.5.0",
          |javax.servlet.http;version="2.5.0"
          |""".stripMargin.replaceAll("\\s", ""),
      AutoProcessor.AUTO_DEPLOY_DIR_PROPERTY -> autoDeployDir.getAbsolutePath,
      AutoProcessor.AUTO_DEPLOY_ACTION_PROPERTY -> "install,start",
      "obr.repository.url" -> (data / "repo" / "repository.xml").toURI.toString,
      "gosh.args" -> (if (ctx.console) "" else "--nointeractive")
    )

    val factory = Service.providers(classOf[FrameworkFactory]).asInstanceOf[java.util.Iterator[FrameworkFactory]]
    val fw = factory.next().newFramework(props)
    fw.init()
    AutoProcessor.process(props, fw.getBundleContext)

    deploy(fw)

    fw.start()

    if (first) {
      IO.delete(autoDeployDir)
    }

    fw
  }

//  def initTerminal(fw: Framework) = {
//
//    val tracker = new ServiceTracker[CommandProcessor, CommandProcessor](
//      fw.getBundleContext,
//      classOf[CommandProcessor],
//      null
//    )
//
//    tracker.open()
//
//    tracker
//
//  }

  val defaultBundles = Seq(
    "console.jar"
  )

  def deployDefault(fw: Framework) : Unit = {
    deploy(
      fw,
      defaultBundles.map({ jar =>
        getClass.getResource(jar)
      }):_*
    )

  }

  def deploy(fw: Framework, bundles: URL*) : Unit = {
    val ctx = fw.getBundleContext
//    val sl: StartLevel = ctx.getService(ctx.getServiceReference(classOf[StartLevel].getName)).asInstanceOf[StartLevel]

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
}
