package osgi6.runtime

import java.io.{File, FileInputStream}

import org.osgi.framework.wiring.FrameworkWiring
import osgi6.common.OsgiTools
import sbt.io.IO

import scala.collection.JavaConversions._
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


    val bnd = OsgiTools.deployBundle0(
      fw.getBundleContext,
      new FileInputStream("bundles/testing/target/testing-bundle.jar")

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
