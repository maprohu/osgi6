package osgi6.akka.http.multi

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import akka.http.scaladsl.model.{ContentType, ContentTypes, HttpProtocols, _}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, StreamConverters}
import maprohu.scalaext.common.Stateful
import osgi6.common.AsyncActivator

import scala.collection.JavaConversions
import scala.concurrent.Future

/**
  * Created by pappmar on 05/07/2016.
  */
object AkkaHttpServlet {

  def wrapRequest(req: HttpServletRequest) : HttpRequest = {
    import JavaConversions._

    HttpRequest(
      method = HttpMethods.getForKey(req.getMethod).get,
      uri = Uri(req.getRequestURI).copy(rawQueryString = Option(req.getQueryString)),
      headers = req.getHeaderNames.asInstanceOf[java.util.Enumeration[String]].toIterable.map({ headerName =>
        HttpHeader.parse(headerName, req.getHeader(headerName)).asInstanceOf[Ok].header
      })(collection.breakOut),
      entity = HttpEntity(
        contentType =
          Option(req.getContentType)
            .map(ct => ContentType.parse(ct).right.get)
            .getOrElse(ContentTypes.`application/octet-stream`),
        data =
          StreamConverters
            .fromInputStream(() => req.getInputStream)
      ),
      protocol = HttpProtocols.getForKey(req.getProtocol).get
    )

  }


  def unwrapResponse(httpResponse: HttpResponse, res: HttpServletResponse)(implicit
    materializer: Materializer
  ) : Future[Any] = {

    httpResponse.headers.foreach { h =>
      res.setHeader(h.name(), h.value())
    }
    res.setStatus(httpResponse.status.intValue())
    res.setContentType(httpResponse.entity.contentType.toString())
    httpResponse.entity.contentLengthOption.foreach { cl =>
      res.setContentLength(cl.toInt)
    }

    httpResponse.entity.dataBytes
      .toMat(
        StreamConverters.fromOutputStream(
          () => res.getOutputStream
        )
      )(Keep.right)
      .run()

  }

  type RequestProcessorFuture = Future[Boolean]
  type RequestProcessor = (HttpServletRequest, HttpServletResponse) => RequestProcessorFuture

  val NotHandled = StatusCodes.custom(
    1007,
    "not handled",
    "not handled",
    false,
    false
  )

  val notHandledResponse =
    HttpResponse(
      status = NotHandled,
      entity = HttpEntity.empty(ContentTypes.`text/plain(UTF-8)`)
    )

  def processor(
    route: Route,
    filter: HttpServletRequest => Boolean = _ => true
  )(implicit
    actorSystem: ActorSystem,
    materializer: Materializer
  ) : (RequestProcessor, AsyncActivator.Stop) = {
    import actorSystem.dispatcher

    @volatile var cancelled = false

    val routeHandler = Route.asyncHandler(
      pathPrefix( Segment ) { _ =>
        route ~ complete(
          notHandledResponse
        )
      }
    )

    val futures = Stateful.futures[Any]

    val requestProcessor : RequestProcessor = { (req, res) =>
      if (cancelled || !filter(req)) {
        Future.successful(false)
      } else {
        val future =
          routeHandler(wrapRequest(req))
            .flatMap({ httpResponse =>
              if (httpResponse.status == NotHandled) {
                Future.successful(false)
              } else {
                unwrapResponse(httpResponse, res).map(_ => true)
              }
            })

        futures.add(future)

        future
      }
    }


    val cancel : AsyncActivator.Stop = () => {
      cancelled = true
      futures.future.recover({ case o => o })
    }

    (requestProcessor, cancel)
  }


}