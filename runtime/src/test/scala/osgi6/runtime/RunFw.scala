package osgi6.runtime

import java.io.{File, FileInputStream}
import javax.servlet.http.HttpServlet

import osgi6.common.OsgiTools
import sbt.io.IO

import scala.io.StdIn

/**
  * Created by pappmar on 08/07/2016.
  */
object RunFw {

  def main(args: Array[String]) {
    IO.delete(new File("target/osgitest"))

    run

    System.gc()
    println("- run returned")
    StdIn.readLine()

  }

  def run : Unit = {
    val ctx =
      OsgiRuntime
        .context(new File("target/osgitest"), "osgitest")
        .copy(
          debug = true,
          console = false
        )

    val fw = OsgiRuntime.init(ctx, _ => ())


//    StdIn.readLine()
//      OsgiRuntime.deployDefault(fw)


//    StdIn.readLine()
//
//    fw
//      .getBundleContext
//      .getBundles
//      .filter(_.getBundleId != 0)
//      .foreach { bnd =>
//        bnd.uninstall()
//      }
//
//    StdIn.readLine()
//    OsgiTools.refresh(fw)
//
//    StdIn.readLine()
//
//    fw.stop()
//    fw.waitForStop(10000)
//    fw = null
//
//    StdIn.readLine()

    OsgiTools.deployFragment(
      fw.getBundleContext,
      new FileInputStream("fragments/servlet-2.5/target/osgi-bundle.jar")
    )
    OsgiTools.deployFragment(
      fw.getBundleContext,
      new FileInputStream("fragments/jms-1.1/target/osgi-bundle.jar")
    )

//    OsgiTools.refresh(fw)
//    Thread.sleep(1000)


    OsgiTools.deployBundle0(
      fw.getBundleContext,
      new FileInputStream("bundles/multi/api/target/osgi-bundle.jar")
    )
//    OsgiTools.deployBundle0(
//      fw.getBundleContext,
//      new FileInputStream("bundles/strict/api/target/strict-api-bundle.jar")
//    )

    OsgiTools.deployBundle0(
      fw.getBundleContext,
      new FileInputStream("bundles/logging/target/osgi-bundle.jar")
    )

    val bnd = OsgiTools.deployBundle0(
      fw.getBundleContext,
      new FileInputStream("../vdm2cdf-osgi/bundles/core/target/vdm2cdf-core-bundle.jar")
//      new FileInputStream("../frontex-osgi/bundles/ovr/target/frontex-ovr-bundle.jar")
//    new FileInputStream("bundles/admin/target/admin-bundle.jar")
//    new FileInputStream("bundles/jolokia/target/jolokia-bundle.jar")
//    new FileInputStream("bundles/command/target/command-bundle.jar")
//      new FileInputStream("bundles/testing/target/testing-bundle.jar")

    )

    println("- started")
    StdIn.readLine()

    bnd.stop()
    System.gc()
    println("- stopped")
    StdIn.readLine()

    bnd.uninstall()
    System.gc()
    println("- uninstalled")
    StdIn.readLine()

    OsgiTools.refresh(fw)
    System.gc()
    println("- refreshed")
    StdIn.readLine()

    fw.stop()
    fw.waitForStop(2000)
    System.gc()
    println("- fw stopped")
    StdIn.readLine()

//    fw = null
//    System.gc()
//    println("- fw nulled")
//    StdIn.readLine()
  }

}
