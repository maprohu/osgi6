package osgi6.logging

import java.io.File

import ch.qos.logback.classic.{Level, LoggerContext}
import ch.qos.logback.classic.layout.TTLLLayout
import ch.qos.logback.classic.spi.{Configurator, ILoggingEvent}
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import ch.qos.logback.core.rolling.{FixedWindowRollingPolicy, RollingFileAppender, SizeBasedTriggeringPolicy}
import ch.qos.logback.core.spi.ContextAwareBase
import org.slf4j.Logger
import osgi6.api.OsgiApi

/**
  * Created by martonpapp on 26/06/16.
  */
class LogbackConfigurator extends ContextAwareBase with Configurator {

  def createEncoder(lc: LoggerContext) = {
    val layout: TTLLLayout = new TTLLLayout
    layout.setContext(lc)
    layout.start

    val encoder: LayoutWrappingEncoder[ILoggingEvent] = new LayoutWrappingEncoder[ILoggingEvent]
    encoder.setContext(lc)
    encoder.setLayout(layout)
    encoder.start()

    encoder
  }


  override def configure(lc: LoggerContext): Unit = {

    val logdir = OsgiApi.context.log
    val appname = OsgiApi.context.name


    val fa = new RollingFileAppender[ILoggingEvent]
    fa.setContext(lc)
    fa.setName("file")
    fa.setFile(new File(logdir, s"$appname.log").getAbsolutePath)

    val tp = new SizeBasedTriggeringPolicy[ILoggingEvent]()
    tp.setMaxFileSize("5MB")
    tp.start()

    val rp = new FixedWindowRollingPolicy
    rp.setContext(lc)
    rp.setParent(fa)
    rp.setFileNamePattern(
      new File(
        logdir,
        s"${appname}.%i.log"
      ).getAbsolutePath
    )

    rp.start()


    fa.setEncoder(createEncoder(lc))
    fa.setRollingPolicy(rp)
    fa.setTriggeringPolicy(tp)

    fa.start


    val rootLogger = lc.getLogger(Logger.ROOT_LOGGER_NAME)
    rootLogger.addAppender(fa)

    if (OsgiApi.context.stdout) {

      val ca: ConsoleAppender[ILoggingEvent] = new ConsoleAppender[ILoggingEvent]
      ca.setContext(lc)
      ca.setName("console")
      ca.setEncoder(createEncoder(lc))
      ca.start
      rootLogger.addAppender(ca)

    }

    if (OsgiApi.context.debug) {


      rootLogger.setLevel(Level.DEBUG)
    } else {
      rootLogger.setLevel(Level.INFO)
    }

  }

}