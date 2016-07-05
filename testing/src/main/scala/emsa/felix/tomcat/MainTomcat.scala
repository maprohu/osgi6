package emsa.felix.tomcat

import java.io.File

import org.apache.catalina.core.StandardContext
import org.apache.catalina.loader.WebappLoader
import org.apache.catalina.realm.MemoryRealm
import org.apache.catalina.startup.Embedded
import org.apache.naming.resources.VirtualDirContext
import sbt.io.Path._

import scala.xml.XML

/**
  * Created by pappmar on 22/06/2016.
  */
object MainTomcat {

  def run(
    name: String,
    base: File,
    webapp: File
  ) = {

    base.mkdirs()

    val users =
      <tomcat-users>
        <user name="tomcat" password="tomcat" roles="tomcat" />
        <user name="role1"  password="tomcat" roles="role1"  />
        <user name="both"   password="tomcat" roles="tomcat,role1" />
      </tomcat-users>

    val tomcatUsers = base / "tomcat-users.xml"

    XML.save(tomcatUsers.getAbsolutePath, users)

    val tomcat = new Embedded()
    tomcat.setCatalinaHome(base.getAbsolutePath)
    val realm = new MemoryRealm()
    realm.setPathname(tomcatUsers.getAbsolutePath)
    tomcat.setRealm(realm)

    val loader = new WebappLoader(getClass.getClassLoader)

    val context = tomcat.createContext(s"/${name}", webapp.getAbsolutePath)
    context.setLoader(loader)
    context.setReloadable(true)


    val host = tomcat.createHost("localhost", (base / "host").getAbsolutePath)
    host.addChild(context)

    val engine = tomcat.createEngine()
    engine.setName("localEngine")
    engine.addChild(host)
    engine.setDefaultHost(host.getName)
    tomcat.addEngine(engine)

    val connector = tomcat.createConnector(null.asInstanceOf[String], 9977, false)
    tomcat.addConnector(connector)

    tomcat.setAwait(true)

    tomcat.start()


  }

}
