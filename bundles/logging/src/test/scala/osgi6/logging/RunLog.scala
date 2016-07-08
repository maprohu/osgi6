package osgi6.logging

import java.io.File

import org.slf4j.LoggerFactory
import osgi6.api.{Context, OsgiApi}

/**
  * Created by martonpapp on 26/06/16.
  */
object RunLog {

  def main(args: Array[String]) {

    OsgiApi.context = new Context {
      override def name: String = "appname"

      override def log: File = new File("target/logs")

      override def data: File = ???

      override def debug: Boolean = false

      override def console: Boolean = false

      override def stdout: Boolean = true
    }
    val logger = LoggerFactory.getLogger(RunLog.getClass)

    (0 to 1000000) foreach { i =>
      logger.debug(s"boo at ${i}")
    }
  }

}
