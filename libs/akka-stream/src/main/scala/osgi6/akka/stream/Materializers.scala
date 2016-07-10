package osgi6.akka.stream

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}

import scala.util.control.NonFatal

/**
  * Created by pappmar on 08/07/2016.
  */
object Materializers {

  def resume(implicit
    actorSystem: ActorSystem
  ) = {
    val decider : Supervision.Decider = {
      case NonFatal(ex) =>
        actorSystem.log.error(ex, "error in stream processing")
        Supervision.Resume
      case ex =>
        actorSystem.log.error(ex, "fatal error in stream processing")
        Supervision.Stop
    }

    ActorMaterializer(
      ActorMaterializerSettings(actorSystem)
        .withSupervisionStrategy(decider)
    )
  }

}
