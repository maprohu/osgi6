package osgi6.console

import osgi6.common.MultiActivator


/**
  * Created by martonpapp on 05/07/16.
  */
class ConsoleActivator extends MultiActivator(
  new org.apache.felix.bundlerepository.impl.Activator,
  new org.apache.felix.gogo.command.Activator,
  new org.apache.felix.gogo.runtime.activator.Activator,
  new org.apache.felix.gogo.shell.Activator
)

