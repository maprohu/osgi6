package osgi6.multi

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import osgi6.api.OsgiApi
import osgi6.multi.api.MultiApi

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future, Promise}

/**
  * Created by martonpapp on 04/07/16.
  */
object MultiProcessor {

  def apply(implicit executionContext: ExecutionContext) : OsgiApi.Handler = {
    new OsgiApi.Handler {
      override def process(request: HttpServletRequest, response: HttpServletResponse): Unit = {
        import scala.collection.JavaConversions._

        val handlers: Iterator[MultiApi.Handler] = MultiApi.registry.iterate

        def processNext : Future[Boolean] = {
          if (handlers.hasNext) {
            val handler = handlers.next()
            val promise = Promise[Boolean]()

            handler.dispatch(request, response, new MultiApi.Callback {
              override def handled(result: Boolean): Unit = {
                if (result) {
                  promise.success(true)
                } else {
                  promise.completeWith(processNext)
                }
              }
            })

            promise.future

          } else {
            Future.successful(false)
          }

        }

        val processed = Await.result(processNext, 1.minute)

        if (!processed) {
          response.setStatus(HttpServletResponse.SC_NOT_FOUND)
        }

      }
    }

  }
}
