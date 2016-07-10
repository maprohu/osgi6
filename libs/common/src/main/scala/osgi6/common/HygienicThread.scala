package osgi6.common

import java.util.concurrent.{SynchronousQueue, ThreadPoolExecutor, TimeUnit}

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

  def createExecutionContext = {
    val pool = new ThreadPoolExecutor(
      0,
      Integer.MAX_VALUE,
      10L, TimeUnit.SECONDS,
      new SynchronousQueue[Runnable]
    )
    val ec = ExecutionContext.fromExecutor(pool)

    val shut = () => {
      pool.shutdown()
      if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
        pool.shutdownNow()
        pool.awaitTermination(30, TimeUnit.SECONDS)
      }
      ()
    }

    (ec, shut)
  }

}

