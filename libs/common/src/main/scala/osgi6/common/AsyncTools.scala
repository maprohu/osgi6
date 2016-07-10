package osgi6.common

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by martonpapp on 10/07/16.
  */
object AsyncTools {

  def foldSeq[T, V](zero: V, items: Seq[T])(fn: (T, V) => Future[V])(
    implicit executionContext: ExecutionContext
  ) : Future[V] = {
    items match {
      case head +: tail =>
        fn(head, zero).flatMap({ v =>
          foldSeq(v, tail)(fn)
        })
      case _ =>
        Future.successful(zero)
    }
  }

  def runSeq[T](items: Seq[T])(fn: T => Future[Any])(
    implicit executionContext: ExecutionContext
  ) : Future[Unit] = {
    foldSeq((), items)((item, _) => fn(item).map(_ => ()))
  }

}
