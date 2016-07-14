package osgi6.akka.http

import akka.http.scaladsl.model.{HttpMessage, HttpRequest, HttpResponse}
import akka.stream.Materializer
import akka.util.Timeout

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

/**
  * Created by pappmar on 14/07/2016.
  */
object AkkaHttp {


  def toStrict[T <: HttpMessage]()(implicit
    materializer: Materializer,
    executionContext: ExecutionContext,
    timeout: Timeout = Timeout(1.minute)
  ) : T => Future[T] = { req =>
    req.entity.toStrict(timeout.duration).map({ strict =>
      req.withEntity(strict).asInstanceOf[T]
    })
  }

  def toString[T <: HttpMessage]()(implicit
    materializer: Materializer,
    executionContext: ExecutionContext,
    timeout: Timeout = Timeout(1.minute)
  ) : T => Future[T] = { req =>
    req.entity.toStrict(timeout.duration).map({ strict =>
      req.withEntity(strict.data.utf8String).asInstanceOf[T]
    })
  }
}
