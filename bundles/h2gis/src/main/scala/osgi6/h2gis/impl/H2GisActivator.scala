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
import osgi6.h2gis.H2GisApi.Provider
import osgi6.lib.multi.ContextApiActivator
import osgi6.multi.api.{Context, ContextApi}

/**
  * Created by pappmar on 19/07/2016.
  */
class H2GisActivator extends ActorSystemActivator(
  { ctx =>
    import ctx.actorSystem.dispatcher
    ContextApiActivator.activateNonNull({ apiCtx =>

      H2GisActivator.activate(apiCtx)

      AsyncActivator.Noop
    })
  },
  Some(classOf[H2GisActivator].getClassLoader),
  config = AkkaSlf4j.config
)

object H2GisActivator {

  def activate(ctx: Context) = {
    H2GisApi.registry.set(new Provider {
      override def create(): DataSource = synchronized {
        createDataSourceFromContex(ctx)
      }
    })
  }

  def createDataSourceFromContex(ctx: Context) = {
    val dbFile = new File(ctx.data.getParentFile, s"storage/${ctx.name}/h2gis/h2gis")
    createDataSource(dbFile)
  }

  def createDataSource(dbFile: File) : DataSource = {
    val dbDir = dbFile.getParentFile
    val isNew = !dbDir.exists()
//    dbDir.mkdirs()

    val dataSource: BasicDataSource = new BasicDataSource
    dataSource.setDriverClassLoader(getClass.getClassLoader)
    dataSource.setDriverClassName(classOf[Driver].getName)
    dataSource.setPoolPreparedStatements(false)
    dataSource.setUrl("jdbc:h2:" + dbFile.toURI.toURL.toExternalForm.replaceAllLiterally("\\", "/") + ";AUTO_SERVER=TRUE")

    if (isNew) {
      val conn = dataSource.getConnection
      try {
        CreateSpatialExtension.initSpatialExtension(conn)
      } finally {
        conn.close()
      }
    }

    dataSource
  }


}
