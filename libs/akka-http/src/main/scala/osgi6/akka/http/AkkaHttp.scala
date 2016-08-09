package osgi6.akka.http

import akka.http.scaladsl.model.{HttpMessage, HttpRequest, HttpResponse}
import akka.http.scaladsl.server.{PathMatcher0, PathMatchers}
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

object Implicits {

  implicit def segmentsToPathMatcher(segments: TraversableOnce[String]) : PathMatcher0 = {
    if (segments.isEmpty) {
      PathMatchers.Neutral
    } else {
      import akka.http.scaladsl.server.Directives._
      segments.map(segmentStringToPathMatcher).reduce(_ / _)
    }
  }


}
