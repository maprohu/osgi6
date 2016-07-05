package osgi6.command

import java.io._

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server._
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, Materializer, OverflowStrategy}
import org.apache.felix.service.command.CommandProcessor
import org.osgi.framework.BundleContext
import org.osgi.util.tracker.ServiceTracker

import scala.concurrent.{Future, Promise}


/**
  * Created by pappmar on 24/06/2016.
  */
object OsgiCommand {

  def init(ctx: BundleContext)(implicit
    actorSystem : ActorSystem,
    actorMaterializer : Materializer
  ) : Route = {

    import Directives._
    import actorSystem.dispatcher


    val tracker = new ServiceTracker[CommandProcessor, CommandProcessor](
      ctx,
      classOf[CommandProcessor],
      null
    )



    tracker.open()


    val route =
      path( "command" ) {
        extractRequest { request =>
          parameter("cmd") { cmd =>
            val is =
              request.entity.dataBytes
                .runWith(StreamConverters.asInputStream())

            complete(
              HttpEntity(
                ContentTypes.`text/plain(UTF-8)`,
                StreamConverters.asOutputStream()
                  .mapMaterializedValue({ os =>
                    Future {
                      val osf = new FilterOutputStream(os) {
                        override def close(): Unit = {
                          flush()
                        }
                      }
                      val out = new PrintStream(osf)
                      val err = new PrintStream(osf)
                      val session = tracker.getService.createSession(is, out, err)
                      try {
                        try {
                          session.execute(cmd)
                        } catch {
                          case ex: Throwable =>
                            val err2 = new PrintStream(osf)
                            ex.printStackTrace(err2)
                            err2.close()
                        }
                      } finally {
                        session.close()
                        out.close()
                        err.close()
                        os.close()
                      }
                    }
                  })
              )
            )
          }

        }

      }

    route

  }



}
