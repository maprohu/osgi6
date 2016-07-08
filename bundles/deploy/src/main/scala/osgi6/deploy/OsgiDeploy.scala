package osgi6.deploy

import java.io._

import akka.http.scaladsl.server.Directives
import akka.util.ByteString
import org.osgi.framework.BundleContext
import osgi6.common.OsgiTools


/**
  * Created by pappmar on 06/07/2016.
  */
object OsgiDeploy {

  def init(ctx: BundleContext) = {
    import Directives._

    path( "deploy" ) {
      extractRequest { request =>
        extractMaterializer { implicit mat =>
          onSuccess(
            request.entity.dataBytes.runFold(ByteString())(_ ++ _)
          ) { bytes =>

            val is =
              new ByteArrayInputStream(bytes.toArray)

//            val pb = new PushbackInputStream(is, 1024 * 1024)

            val deployResult =
              OsgiTools.deployBundle(ctx, is)

            complete(
              deployResult
            )

          }
        }

      }
    }


  }

}
