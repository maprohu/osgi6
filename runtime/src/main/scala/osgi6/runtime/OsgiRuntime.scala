package osgi6.runtime

import java.io.File
import java.net.URL

import org.osgi.framework.Constants
import org.osgi.framework.launch.{Framework, FrameworkFactory}
import org.osgi.framework.startlevel.BundleStartLevel
import org.osgi.service.startlevel.StartLevel
import osgi6.api.{Context, OsgiApi}
import osgi6.common.OsgiTools
import sbt.io.IO
import sun.misc.Service
import sbt.io.Path._

import scala.collection.JavaConversions._
import scala.util.Try

/**
  * Created by pappmar on 22/06/2016.
  */
object OsgiRuntime {

  case class Ctx(
    name: String,
    version: Option[Int],
    data: File,
    log: File,
    debug: Boolean,
    stdout: Boolean = false,
    init: File => Unit = _ => (),
    console: Boolean = false
  ) extends Context

  def context(dir: File, app: String, version: Option[Int] = None) : Ctx = {

    val data = dir / "data" / app
    val log = dir / "logs"

    Ctx(
      name = app,
      version = version,
      data = data,
      log = log,
      debug = false
    )
  }


  val versionFileName = "version.txt"

  def init(ctx: Context, deploy: Framework => Unit) = {
    OsgiApi.context = ctx

    val data = ctx.data

    val versionFile = data / versionFileName

    def writeVersion : Unit = {
      IO.write(versionFile, ctx.version.toString)
    }

    def readVersion : Option[Int] = {
      Try(IO.read(versionFile).trim.toInt).toOption
    }

    ctx.version.foreach { softwareVersion =>
      if (readVersion.forall( { dataFoundVersion =>
        dataFoundVersion < softwareVersion
      })) {
        IO.delete(data)
      }
    }

    val first = !data.exists()

    if (first) {
      data.mkdirs()

      writeVersion
    }

//    """
//      |javax.naming,
//      |javax.naming.*,
//      |javax.xml,
//      |javax.xml.*,
//      |org.xml,
//      |org.xml.*,
//      |org.w3c,
//      |org.w3c.*,
//      |sun.misc,
//      |sun.security.util,
//      |sun.security.x509,
//      |com.singularity.*
//    """.stripMargin.replaceAll("\\s", ""),

//    """
//      |osgi6.api,
//      |javax.servlet;version="2.5.0",
//      |javax.servlet.descriptor;version="2.5.0",
//      |javax.servlet.http;version="2.5.0"
//      |""".stripMargin.replaceAll("\\s", ""),

    val props = Map[String, String](
      Constants.FRAMEWORK_STORAGE -> (data / "felix-cache").getAbsolutePath,
      Constants.FRAMEWORK_BOOTDELEGATION ->
        """
          |sun.misc,
          |sun.security.util,
          |sun.security.x509,
          |com.singularity.*
        """.stripMargin.replaceAll("\\s", ""),
      Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA ->
        """
          |osgi6.api
          |""".stripMargin.replaceAll("\\s", ""),
      "obr.repository.url" -> (data / "repo" / "repository.xml").toURI.toString,
      "gosh.args" -> ("--noshutdown " + (if (ctx.console) "" else "--nointeractive"))
    )

    val factory = Service.providers(classOf[FrameworkFactory]).asInstanceOf[java.util.Iterator[FrameworkFactory]]
    val fw = factory.next().newFramework(props)
    fw.init()

    if (first) {
      deploy(fw)
    }

    fw.start()

    fw
  }


  val defaultBundles = Seq(
    "servlet.jar",
    "logging.jar",
    "multi-api.jar",
    "multi-bundle.jar",
    "strict-api.jar",
    "strict-bundle.jar",
    "jolokia.jar",
    "admin.jar"
  )

  def deployDefault(fw: Framework) : Unit = {
    deployBundles(
      fw,
      OsgiRuntime.getClass,
      defaultBundles
    )
  }

  def deployBundles(fw: Framework, clazz: Class[_], bundles: Seq[String]) : Unit = {
    OsgiTools.deploy(
      fw,
      bundles.map({ jar =>
        clazz.getResource(jar)
      }):_*
    )

  }

}
