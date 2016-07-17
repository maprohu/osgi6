package akka.stream.hack

import akka.stream.impl.AcknowledgeSource
import akka.stream.impl.Stages.DefaultAttributes
import akka.stream.scaladsl.Source
import akka.stream.{OverflowStrategy, SourceQueue}

import scala.concurrent.duration._

/**
  * Created by pappmar on 11/07/2016.
  */
object HackSource {

  def queue[T](
    bufferSize: Int,
    overflowStrategy: OverflowStrategy,
    timeout: FiniteDuration = 5.seconds
  ): Source[T, SourceQueue[T]] = {
    require(bufferSize >= 0, "bufferSize must be greater than or equal to 0")
    new Source(new AcknowledgeSource(
      bufferSize,
      overflowStrategy,
      DefaultAttributes.acknowledgeSource,
      Source.shape("AcknowledgeSource"),
      timeout
    ))
  }

}
