package osgi6.akka.stream

import akka.stream.stage.{GraphStageLogic, GraphStageWithMaterializedValue, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}

import scala.concurrent.{Future, Promise}

/**
  * Created by martonpapp on 29/06/16.
  */
object Stages {

  private object TerminationWatcher extends GraphStageWithMaterializedValue[FlowShape[Any, Any], Future[Unit]] {
    val in = Inlet[Any]("terminationWatcher.in")
    val out = Outlet[Any]("terminationWatcher.out")
    override val shape = FlowShape(in, out)
    override def initialAttributes: Attributes = Attributes.name("terminationWatcher")

    override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[Unit]) = {
      val finishPromise = Promise[Unit]()

      (new GraphStageLogic(shape) {
        setHandler(in, new InHandler {
          override def onPush(): Unit = push(out, grab(in))

          override def onUpstreamFinish(): Unit = {
            finishPromise.success(Unit)
            completeStage()
          }

          override def onUpstreamFailure(ex: Throwable): Unit = {
            finishPromise.failure(ex)
            failStage(ex)
          }
        })
        setHandler(out, new OutHandler {
          override def onPull(): Unit = pull(in)
          override def onDownstreamFinish(): Unit = {
            finishPromise.success(Unit)
            completeStage()
          }
        })
      }, finishPromise.future)
    }

    override def toString = "TerminationWatcher"
  }

  def terminationWatcher[T]: GraphStageWithMaterializedValue[FlowShape[T, T], Future[Unit]] =
    TerminationWatcher.asInstanceOf[GraphStageWithMaterializedValue[FlowShape[T, T], Future[Unit]]]

  case class MapMat[In, Out, Mat](factory: () => Mat)(map: (Mat, In) => Out) extends GraphStageWithMaterializedValue[FlowShape[In, Out], Mat] {
    val in = Inlet[In]("mapmat.in")
    val out = Outlet[Out]("mapmat.out")
    override val shape = FlowShape(in, out)
    override def initialAttributes: Attributes = Attributes.name("mapmat")

    override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Mat) = {
      val mat = factory()


      (new GraphStageLogic(shape) {
        setHandler(in, new InHandler {
          override def onPush(): Unit = {
            push(out, map(mat, grab(in)))
          }
        })
        setHandler(out, new OutHandler {
          override def onPull(): Unit = {
            pull(in)
          }
        })
      }, mat)
    }

    override def toString = "MapMat"
  }

}
