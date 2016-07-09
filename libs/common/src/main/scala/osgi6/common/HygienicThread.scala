package osgi6.common

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.util.Try

/**
  * Created by martonpapp on 09/07/16.
  */
object HygienicThread {


  def execute[T]( task: => T, timeout: Duration = Duration.Inf ) : T = {
    val promise = Promise[T]()
    new Thread() {
      override def run(): Unit = {
        promise.complete(Try(task))
      }
    }.start()
    Await.result(promise.future, timeout)
  }

}

