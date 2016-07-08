package osgi6.runtime

import java.io.File

import org.osgi.framework.wiring.FrameworkWiring
import osgi6.common.OsgiTools
import sbt.io.IO

import scala.collection.JavaConversions._

/**
  * Created by pappmar on 08/07/2016.
  */
object RunFw {

  def main(args: Array[String]) {
    IO.delete(new File("target/osgitest"))

    val ctx =
      OsgiRuntime
        .context(new File("target/osgitest"), "osgitest")
        .copy(
          debug = true,
          console = true
        )

    val fw = OsgiRuntime.init(ctx, _ => ())

//    while (true) {
      OsgiRuntime.deployDefault(fw)

//      fw
//        .getBundleContext
//        .getBundles
//        .filter(_.getBundleId != 0)
//        .foreach { bnd =>
//          bnd.uninstall()
//        }
//    }
  }

}
