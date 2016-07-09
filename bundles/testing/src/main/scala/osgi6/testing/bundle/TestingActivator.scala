package osgi6.testing.bundle

import akka.actor.ActorSystem
import akka.osgi.ActorSystemActivator
import com.typesafe.config.impl.ConfigImpl
import org.osgi.framework.{BundleActivator, BundleContext}
import osgi6.common.HygienicThread

import scala.concurrent.{Await, Promise}
import scala.concurrent.duration.Duration
import scala.util.control.NonFatal
import scala.concurrent.duration._

/**
  * Created by martonpapp on 08/07/16.
  */
class TestingActivator extends BundleActivator {

  var system : ActorSystem = null
//

//  val stopper = Promise[Unit]()
//
//  val thread = new Thread {
//    override def run(): Unit = {
//      val actorSystem = ActorSystem("boo", classLoader = Some(classOf[TestingActivator].getClassLoader))
//
//      Await.result(stopper.future, Duration.Inf)
//
//      Await.result(actorSystem.terminate(), 30.seconds)
//    }
//  }

  override def start(context: BundleContext): Unit = {
    system = HygienicThread.execute {
      ActorSystem("boo", classLoader = Some(classOf[TestingActivator].getClassLoader))
    }
    TestingActivator.activate(context)
//    thread.start()
  }
//

  override def stop(context: BundleContext): Unit = {
    println(context.getBundle.getSymbolicName + " stopping")
//    stopper.success(())
//    thread.join()
//    super.stop(context)
//    Await.result(system.terminate(), Duration.Inf)

    HygienicThread.execute {
      system.shutdown()
      system.awaitTermination()
      system = null
    }

//    try {
//      ConfigImpl.computeCachedConfig(null, null, null)
//    } catch {
//      case NonFatal(ex) =>
//        ex.printStackTrace()
//    }

  }

//  override def configure(context: BundleContext, system: ActorSystem): Unit = {
//    TestingActivator.activate(context)
//  }


}

object TestingActivator {

  def activate(context: BundleContext) = {
    println(context.getBundle.getSymbolicName + " starting")
  }

}
