package osgi6.testing.bundle

import akka.actor.ActorSystem
import akka.osgi.ActorSystemActivator
import com.typesafe.config.impl.ConfigImpl
import org.osgi.framework.{BundleActivator, BundleContext}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.control.NonFatal

/**
  * Created by martonpapp on 08/07/16.
  */
class TestingActivator extends ActorSystemActivator {

//  var system : ActorSystem = null
//
//  override def start(context: BundleContext): Unit = {
//    system = ActorSystem()
//    TestingActivator.activate(context)
//  }
//

  override def stop(context: BundleContext): Unit = {
    println(context.getBundle.getSymbolicName + " stopping")
    super.stop(context)
//    Await.result(system.terminate(), Duration.Inf)
//    system.shutdown()
//    system.awaitTermination()
//    system = null

    try {
      ConfigImpl.computeCachedConfig(null, null, null)
    } catch {
      case NonFatal(ex) =>
        ex.printStackTrace()
    }

  }

  override def configure(context: BundleContext, system: ActorSystem): Unit = {
    TestingActivator.activate(context)
  }


}

object TestingActivator {

  def activate(context: BundleContext) = {
    println(context.getBundle.getSymbolicName + " starting")
  }

}
