package osgi6.testing

import java.io.File

import osgi6.api.Context
import osgi6.runtime.{OsgiRuntime, OsgiServlet}

/**
  * Created by martonpapp on 04/07/16.
  */
class OsgiTestingServlet extends OsgiServlet {
  override def ctx: Context = {
    val c = OsgiRuntime
      .context(new File("target/osgitest"), "osgitest")

    c
      .copy(
        debug = true,
        console = true
      )

  }
}
