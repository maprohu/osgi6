package osgi6.h2gis.impl

import java.io.File
import javax.sql.DataSource

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

      H2GisActivator.activate(apiCtx)

      { () =>
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
        createDataSourceFromContex(ctx)
      }
    })
  }

  def createDataSourceFromContex(ctx: Context) = {
    val dbFile = new File(ctx.data.getParentFile, s"storage/${ctx.name}/h2gis/h2gis")
    createDataSource(dbFile)
  }

  def createDataSource(dbFile: File) : ClosableDataSource = {
    val dbDir = dbFile.getParentFile
    val isNew = !dbDir.exists()
//    dbDir.mkdirs()

    val basicDataSource: BasicDataSource = new BasicDataSource
    basicDataSource.setDriverClassLoader(getClass.getClassLoader)
    basicDataSource.setDriverClassName(classOf[Driver].getName)
    basicDataSource.setPoolPreparedStatements(false)
    basicDataSource.setUrl("jdbc:h2:" + dbFile.toURI.toURL.toExternalForm.replaceAllLiterally("\\", "/"))
//    basicDataSource.setUrl("jdbc:h2:" + dbFile.toURI.toURL.toExternalForm.replaceAllLiterally("\\", "/") + ";AUTO_SERVER=TRUE")

    if (isNew) {
      val conn = basicDataSource.getConnection
      try {
        CreateSpatialExtension.initSpatialExtension(conn)
      } finally {
        conn.close()
      }
    }

    new ClosableDataSource {
      override def dataSource(): DataSource = basicDataSource
      override def close(): Unit = basicDataSource.close()
    }

  }


}

