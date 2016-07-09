package osgi6.command

import java.io._
import java.util.concurrent.{ConcurrentHashMap, ConcurrentMap}

import akka.actor.ActorSystem
import akka.http.scaladsl.server._
import akka.stream.{ActorMaterializer, Materializer, OverflowStrategy}
import akka.util.ByteString
import org.apache.felix.gogo.runtime.threadio.ThreadIOImpl
import org.apache.felix.gogo.runtime.{CommandProcessorImpl, CommandProxy}
import org.apache.felix.gogo.shell._
import org.apache.felix.service.command.CommandProcessor
import org.apache.felix.service.threadio.ThreadIO
import org.osgi.framework._
import org.osgi.util.tracker.ServiceTracker

import scala.concurrent.{Future, Promise}
import scala.concurrent.duration._


/**
  * Created by pappmar on 24/06/2016.
  */
object OsgiCommand {

  def init(ctx: BundleContext)(implicit
    actorSystem : ActorSystem,
    actorMaterializer : Materializer
  ) : (Route, () => Unit) = {

    import Directives._
    import actorSystem.dispatcher

//    val tio = new ThreadIO {
//      override def setStreams(in: InputStream, out: PrintStream, err: PrintStream): Unit = {}
//      override def close(): Unit = {}
//    }
    val tio = new ThreadIOImpl
    tio.start

    val processor = OsgiCommandJava.start(tio, ctx)

    FelixShellJava.startShell(ctx, processor)

    val felixCommandActivator = new org.apache.felix.gogo.command.Activator
    felixCommandActivator.start(ctx)

//    val processor = new CommandProcessorImpl(tio)

//    processor.addConstant(".context", ctx)
//    processor.addConverter(new Converters(ctx))
//
//    def cmd(scope: String, target: AnyRef, commands: Seq[String]) = {
//      commands.foreach { c =>
//        processor.addCommand(scope, target, c)
//      }
//    }
//
//    cmd(
//      "gogo",
//      processor,
//      Seq("addCommand", "removeCommand", "eval")
//    )
//
//    cmd(
//      "gogo",
//      new Builtin,
//      Seq( "format", "getopt", "new", "set", "tac", "type" )
//    )
//    cmd(
//      "gogo",
//      new Procedural,
//      Seq( "each", "if", "not", "throw", "try", "until", "while" )
//    )
//    cmd(
//      "gogo",
//      new Posix,
//      Seq( "cat", "echo", "grep" )
//    )
//    cmd(
//      "gogo",
//      new Telnet(processor),
//      Seq( "telnetd" )
//    )
//    cmd(
//      "gogo",
//      new Shell(ctx, processor),
//      Seq( "gosh", "sh", "source", "history" )
//    )




//    val tracker = new ServiceTracker[CommandProcessor, CommandProcessor](
//      ctx,
//      classOf[CommandProcessor],
//      null
//    )
//
//
//
//    tracker.open()


    val route =
      path( "command" ) {
        extractRequest { request =>
          parameter("cmd") { cmd =>
            onSuccess(
              for {
                input <- request.entity.dataBytes.runFold(ByteString())(_ ++ _)
                output <- {
                  val promise = Promise[Array[Byte]]()

                  val thread = new Thread {
                    override def run(): Unit = {
                      val is = new ByteArrayInputStream(input.toArray)
                      val osf = new ByteArrayOutputStream()
                      val out = new PrintStream(osf)
                      val err = new PrintStream(osf)
                      val session = processor.createSession(is, out, err)
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
                        osf.close()

                        promise.trySuccess(osf.toByteArray)
                      }

                    }
                  }
                  thread.start()


                  val cancel = actorSystem.scheduler.scheduleOnce(1.minute) {
                    thread.interrupt()

                    promise.tryFailure(new RuntimeException("timeout"))
                  }

                  promise.future.onComplete { _ =>
                    cancel.cancel()
                  }

                  promise.future
                }
              } yield output
            ) { data =>
              complete(
                new String(data)
              )
            }

          }

        }

      }

//    val route =
//      path( "command" ) {
//        extractRequest { request =>
//          parameter("cmd") { cmd =>
//            val is =
//              request.entity.dataBytes
//                .runWith(StreamConverters.asInputStream())
//
//            complete(
//              HttpEntity(
//                ContentTypes.`text/plain(UTF-8)`,
//                StreamConverters.asOutputStream()
//                  .mapMaterializedValue({ os =>
//                    Future {
//                      val osf = new FilterOutputStream(os) {
//                        override def close(): Unit = {
//                          flush()
//                        }
//                      }
//                      val out = new PrintStream(osf)
//                      val err = new PrintStream(osf)
//                      val session = tracker.getService.createSession(is, out, err)
//                      try {
//                        try {
//                          session.execute(cmd)
//                        } catch {
//                          case ex: Throwable =>
//                            val err2 = new PrintStream(osf)
//                            ex.printStackTrace(err2)
//                            err2.close()
//                        }
//                      } finally {
//                        session.close()
//                        out.close()
//                        err.close()
//                        os.close()
//                      }
//                    }
//                  })
//              )
//            )
//          }
//
//        }
//
//      }

    (
      route,
      () => {
        felixCommandActivator.stop(ctx)
        tio.stop()
        processor.stop()
      }
    )

  }

}
