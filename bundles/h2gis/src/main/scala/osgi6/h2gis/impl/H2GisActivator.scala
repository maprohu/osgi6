package osgi6.h2gis.impl

import java.io.File
import javax.sql.DataSource

import ogsi6.libs.h2gis.H2GisUtil
import org.apache.commons.dbcp.BasicDataSource
import org.h2.Driver
import org.h2gis.h2spatialext.CreateSpatialExtension
import osgi6.actor.ActorSystemActivator
import osgi6.akka.slf4j.AkkaSlf4j
import osgi6.common.{AsyncActivator, MultiActivator}
import osgi6.h2gis.H2GisApi
import osgi6.h2gis.H2GisApi.{ClosableDataSource, Provider}
import osgi6.lib.multi.ContextApiActivator
import osgi6.multi.api.{Context, ContextApi}

import scala.concurrent.Future

/**
  * Created by pappmar on 19/07/2016.
  */
class H2GisActivator extends ActorSystemActivator(
  { ctx =>
    import ctx.actorSystem.dispatcher
    ContextApiActivator.activateNonNull({ apiCtx =>

      val unset = H2GisActivator.activate(apiCtx)

      { () =>
        unset.remove

        Driver.unload()

        Future.successful()
      }
    })
  },
  Some(classOf[H2GisActivator].getClassLoader),
  config = AkkaSlf4j.config
)

object H2GisActivator {

  def activate(ctx: Context) = {
    H2GisApi.registry.set(new Provider {
      override def create(): ClosableDataSource = synchronized {
        createDataSourceFromContext(ctx)
      }
    })
  }

  def createDataSourceFromContext(ctx: Context) = {
    val dbFile = new File(ctx.data.getParentFile, s"storage/${ctx.name}/h2gis/h2gis")
    val (ds, dbClose) = H2GisUtil.createDataSource(dbFile)

    new ClosableDataSource {
      override def dataSource(): DataSource = ds
      override def close(): Unit = dbClose()
    }
  }




}

