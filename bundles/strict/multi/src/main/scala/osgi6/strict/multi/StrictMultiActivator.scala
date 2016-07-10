package osgi6.strict.multi

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import org.osgi.framework.BundleContext
import osgi6.common.{BaseActivator, HygienicThread}
import osgi6.multi.api.MultiApi
import osgi6.strict.api.StrictApi
import osgi6.strict.api.StrictApi.{Callback, Response}

import scala.collection.JavaConversions._

/**
  * Created by martonpapp on 07/07/16.
  */
class StrictMultiActivator extends BaseActivator({ ctx =>
  StrictMultiActivator.activate(ctx)
})

object StrictMultiActivator {
  case class Request(
    method : String,
    requestUri : String,
    queryString : String,
    headers: java.util.Map[String, java.lang.Iterable[String]],
    contentType : String,
    contentProvider : () => java.lang.Iterable[Array[Byte]],
    protocol : String
  ) extends StrictApi.Request {
    override lazy val content = contentProvider()
  }

  val chunkSize = 16 * 1024

  def parseRequest(req: HttpServletRequest) : Request = {

    val contentProvider : () => java.lang.Iterable[Array[Byte]] = () => {

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
          .take(chunkSize * chunkSize)
          .force

      require(is.read() == -1, "request too large")

      is.close()

      content
    }



    val headers : Map[String, java.lang.Iterable[String]] =
      req.getHeaderNames.asInstanceOf[java.util.Enumeration[String]].map[(String, java.lang.Iterable[String])]({ headerName =>
        headerName -> asJavaIterable(req.getHeaders(headerName).asInstanceOf[java.util.Enumeration[String]].toIterable)
      }).toMap

    Request(
      method = req.getMethod,
      requestUri = req.getRequestURI,
      queryString = req.getQueryString,
      contentType = req.getContentType,
      contentProvider = contentProvider,
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
    res.setContentType(response.contentType)

    val os = res.getOutputStream
    response.content.foreach({ bytes =>
      os.write(bytes)
    })
    os.close()

  }

  def activate(ctx: BundleContext) = {

    implicit val (ec, pool) = HygienicThread.createExecutionContext

    val reg = MultiApi.registry.register(new MultiApi.Handler {
      override def dispatch(req: HttpServletRequest, res: HttpServletResponse, callback: MultiApi.Callback): Unit = {

        val request = parseRequest(req)

        def process0(handers: Seq[StrictApi.Handler]) : Unit = handers match {
          case handler +: tail =>
            handler.dispatch(
              request,
              new StrictApi.Callback {
                override def handled(response: Response): Unit = {
                  writeResponse(res, response)
                  callback.handled(true)
                }
                override def next: Unit = {
                  process0(tail)
                }
              }
            )
          case _ =>
            callback.handled(false)
        }

        process0(StrictApi.registry.iterate.to[Seq])

      }
    })

    () => {
      reg.remove
      pool()
      ()
    }

  }


}
