package osgi6.strict.api


import osgi6.common.BaseRegistry

/**
  * Created by martonpapp on 04/07/16.
  */
object StrictApi {

  trait Request {
    def method : String
    def requestUri : String
    def queryString : String
    def headers: java.util.Map[String, java.lang.Iterable[String]]
    def contentType : String
    def content : java.lang.Iterable[Array[Byte]]
    def protocol : String
  }

  trait Response {
    def headers: java.util.Map[String, java.lang.Iterable[String]]
    def status : Int
    def contentType : String
    def content : java.lang.Iterable[Array[Byte]]
  }

  trait Callback {
    def handled(response: Response) : Unit
    def next : Unit
  }

  trait Handler {
    def dispatch(request: Request, callback: Callback) : Unit
  }

  trait Registration {
    def remove : Unit
  }

  trait Registry {
    def register(handler: Handler) : Registration
    def iterate : java.util.Enumeration[Handler]
  }

  val registry : Registry = new BaseRegistry[Handler, Registration](
    unreg = remover => new Registration {
      override def remove: Unit = remover()
    }
  ) with Registry


}



