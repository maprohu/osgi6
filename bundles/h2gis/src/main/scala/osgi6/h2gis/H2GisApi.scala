package osgi6.h2gis

import javax.sql.DataSource

import osgi6.scalarx.ListenableRegistry

/**
  * Created by pappmar on 19/07/2016.
  */
object H2GisApi {

  trait ClosableDataSource {
    def dataSource() : DataSource
    def close() : Unit
  }

  trait Provider {
    def create() : ClosableDataSource
  }

  trait Handler {
    def dispatch(ctx: Provider) : Unit
  }

  trait Registration {
    def remove : Unit
  }

  trait Registry {
    def listen(handler: Handler) : Registration
    def set(ctx: Provider) : Unset
  }

  trait Unset {
    def remove : Unit
  }

  val registry : Registry = new ListenableRegistry[Provider, Handler, Registration, Unset](
    notify = (handler, value) => handler.dispatch(value),
    unregister = remover => new Registration {
      override def remove: Unit = remover()
    },
    unset = unsetter => new Unset {
      override def remove: Unit = unsetter()
    }
  ) with Registry

}
