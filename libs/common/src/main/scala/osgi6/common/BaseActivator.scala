package osgi6.common

import org.osgi.framework.{BundleActivator, BundleContext}

import scala.concurrent.Future

/**
  * Created by martonpapp on 04/07/16.
  */
import BaseActivator._

class BaseActivator(starter: Start) extends BundleActivator {
  var stop : Stop = () => ()

  override def start(context: BundleContext): Unit = {
    stop = HygienicThread.execute {
      starter(context)
    }
  }

  override def stop(context: BundleContext): Unit = {
    try {
      HygienicThread.execute {
        stop()
      }
    } finally {
      stop = null
    }
  }

}

object BaseActivator {
  type Stop = () => Unit
  type Start = BundleContext => Stop


}
