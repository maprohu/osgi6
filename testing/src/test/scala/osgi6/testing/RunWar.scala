package osgi6.testing

import java.io.File

import emsa.felix.tomcat.MainTomcat
import sbt.io.IO

/**
  * Created by pappmar on 22/06/2016.
  */
object RunWar {

  def main(args: Array[String]) {

    IO.delete(new File("target/osgitest"))

    RunWarNoClean.main(args)


  }

}
