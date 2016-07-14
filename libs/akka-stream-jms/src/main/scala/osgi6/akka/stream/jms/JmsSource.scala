package osgi6.akka.stream.jms

import javax.jms.{Message, MessageConsumer, MessageListener, Session}

import akka.actor.ActorSystem
import akka.stream.hack.HackSource
import akka.stream.scaladsl.{Keep, Source}
import maprohu.scalaext.common.Stateful
import osgi6.actor.Retry
import osgi6.akka.stream.Stages.MapMat

import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * Created by pappmar on 14/07/2016.
  */
object JmsSource {

  def listen(
    connecter : JmsSink.Connecter
  )(implicit
    actorSystem: ActorSystem
  ) = {
    import actorSystem.dispatcher


    case class State(
      consumer: MessageConsumer
    ) {
      def close() : Unit = ???
    }




    Source.repeat(())
      .viaMat(
        MapMat({ () =>

          var state : Future[State] = null
          val cancels = Stateful.cancels

          val receive = { () =>
            if (state == null) {
              state = connecter().map( ??? )
            }

            state
              .map[Option[Message]]({ s => {
              try {
                Some(s.consumer.receive())
              } catch {
                case ex : Throwable =>
                  s.close()
                  throw ex
              }
            } })
              .andThen({
                case ex : Throwable =>
                  state.close()
                  state = null
              })
          }

          val retryReceive = Retry(
            receive,
            cancels
          )

          val stopper = { () =>
            cancels.cancel.perform
          }

          (retryReceive, stopper)

        })( (mat, _) => mat )
      )(Keep.right)

    val fut = connecter()

    fut
      .map({
        case (connectionFactory, destination) =>
          val connection = connectionFactory.createConnection()
          val session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
          val consumer = session.createConsumer(destination)
          connection.start()
      })









  }

}
