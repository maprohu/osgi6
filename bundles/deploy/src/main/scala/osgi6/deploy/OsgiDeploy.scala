package osgi6.deploy

import java.io._

import akka.http.scaladsl.server.Directives
import akka.stream.Materializer
import akka.stream.scaladsl.{FileIO, StreamConverters}
import org.osgi.framework.BundleContext
import osgi6.common.OsgiTools

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by pappmar on 06/07/2016.
  */
object OsgiDeploy {

  def init(ctx: BundleContext) = {
    import Directives._

    path( "deploy" ) {
      extractRequest { request =>
        extractMaterializer { implicit mat =>
          val is =
            request
              .entity
              .dataBytes
              .runWith(
                StreamConverters.asInputStream()
              )

          val pb = new PushbackInputStream(is, 1024 * 1024)

          val deployResult =
            OsgiTools.deployBundle(ctx, pb)

          complete(
            deployResult
          )
        }

      }
    }


  }

}
