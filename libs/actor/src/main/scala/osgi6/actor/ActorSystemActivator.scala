package osgi6.actor

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import org.osgi.framework.BundleContext
import osgi6.common.{AsyncActivator, BaseActivator}

import scala.concurrent.ExecutionContext.Implicits

/**
  * Created by pappmar on 05/07/2016.
  */
import ActorSystemActivator._

class ActorSystemActivator(
  starter : Start,
  classLoader: Option[ClassLoader] = None,
  name: Option[String] = None,
  config: Config = ConfigFactory.empty()
) extends AsyncActivator({ ctx =>
  activate(ctx, starter, classLoader, name, config)
})

object ActorSystemActivator {
  type Start = (BundleContext, ActorSystem) => AsyncActivator.Stop

  def activate(
    ctx: BundleContext,
    starter : Start,
    classLoader: Option[ClassLoader] = None,
    name: Option[String] = None,
    config: Config = ConfigFactory.empty()
  ) = {
    val actorSystem = create(
      name.getOrElse(ctx.getBundle.getSymbolicName.collect({
        case ch if Character.isLetterOrDigit(ch) => ch
        case other => '_'
      })).dropWhile(ch => !Character.isLetterOrDigit(ch)),
      config,
      classLoader
    )
    import Implicits.global

    val stop = starter(ctx, actorSystem)

    () => {
      stop().andThen({ case _ =>
        actorSystem.shutdown()
      })
    }
  }

  def create(
    name: String,
    config: Config = ConfigFactory.empty(),
    classLoader: Option[ClassLoader] = None
  ) : ActorSystem = {
    val forcedConfig = ConfigFactory.parseString(
      """
        |akka {
        |  loglevel = "DEBUG"
        |  jvm-exit-on-fatal-error = false
        |}
      """.stripMargin
    )

    ActorSystem(
      name,
      Some(
        forcedConfig.withFallback(
          config.withFallback(
            ConfigFactory.load()
          )
        )
      ),
      classLoader
    )

  }
}
