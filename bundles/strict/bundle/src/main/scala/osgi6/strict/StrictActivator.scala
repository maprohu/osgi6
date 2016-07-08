package osgi6.strict

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import org.osgi.framework.BundleContext
import osgi6.api.OsgiApi
import osgi6.api.OsgiApi.Handler
import osgi6.common.BaseActivator
import osgi6.strict.api.StrictApi
import osgi6.strict.api.StrictApi.{Callback, Response}

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration._

/**
  * Created by martonpapp on 07/07/16.
  */
class StrictActivator extends BaseActivator({ ctx =>
  StrictActivator.activate(ctx)
})

object StrictActivator {

  case class Request(
    method : String,
    requestUri : String,
    queryString : String,
    headers: java.util.Map[String, java.lang.Iterable[String]],
    contentType : String,
    content : java.lang.Iterable[Array[Byte]],
    protocol : String
  ) extends StrictApi.Request

  val chunkSize = 16 * 1024

  def parseRequest(req: HttpServletRequest) : Request = {

    val is = req.getInputStream

    val content =
      Stream
        .continually({
          val array = Array.ofDim[Byte](chunkSize)
          (array, is.read(array))
        })
        .takeWhile(_._2 != -1)
        .map({
          case (array, size) =>
            if (array.length != size) {
              array.take(size)
            } else {
              array
            }
        })
        .force

    is.close()

    val headers : Map[String, java.lang.Iterable[String]] =
      req.getHeaderNames.asInstanceOf[java.util.Enumeration[String]].map[(String, java.lang.Iterable[String])]({ headerName =>
        headerName -> asJavaIterable(req.getHeaders(headerName).asInstanceOf[java.util.Enumeration[String]].toIterable)
      }).toMap

    Request(
      method = req.getMethod,
      requestUri = req.getRequestURI,
      queryString = req.getQueryString,
      contentType = req.getContentType,
      content = content,
      headers = headers,
      protocol = req.getProtocol
    )
  }

  def writeResponse(res: HttpServletResponse, response: Response) : Unit = {
    res.setStatus(response.status)
    for {
      (header, values) <- response.headers
      value <- values
    } {
      res.setHeader(header, value)
    }
//    Option(response.contentLength).foreach({ contentLength =>
//      res.setContentLength(contentLength)
//    })
    res.setContentType(response.contentType)

    val os = res.getOutputStream
    response.content.foreach({ bytes =>
      os.write(bytes)
    })
    os.close()

  }


  def activate(ctx: BundleContext) = {

    val reg = OsgiApi.registry.register(new Handler {
      override def process(req: HttpServletRequest, res: HttpServletResponse): Unit = {

        val request = parseRequest(req)

        Await.result(
          Future.find(
            StrictApi.registry.iterate.map({ handler =>
              val promise = Promise[Option[Response]]
              handler.dispatch(
                request,
                new Callback {
                  override def handled(response: Response): Unit = promise.success(Some(response))
                  override def next: Unit = promise.success(None)
                }
              )
              promise.future
            })
          )(_.isDefined).map({ opt =>
            opt
              .flatten
              .map({ response =>
                writeResponse(res, response)
              })
              .getOrElse({
                res.setStatus(HttpServletResponse.SC_NOT_FOUND)
              })
          }),
          1.minute
        )

      }
    })

    () => reg.remove

  }


}
