package osgi6.command;

import org.apache.felix.gogo.api.CommandSessionListener;
import org.apache.felix.gogo.runtime.CommandProcessorImpl;
import org.apache.felix.gogo.runtime.CommandProxy;
import org.apache.felix.gogo.runtime.activator.EventAdminListener;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.Converter;
import org.apache.felix.service.threadio.ThreadIO;
import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by martonpapp on 09/07/16.
 */
public class OsgiCommandJava {

    public static final String CONTEXT = ".context";

    public static CommandProcessorImpl newProcessor(ThreadIO tio, BundleContext context)
    {
        CommandProcessorImpl processor = new CommandProcessorImpl(tio);

        try
        {
            processor.addListener(new EventAdminListener(context));
        }
        catch (NoClassDefFoundError error)
        {
            // Ignore the listener if EventAdmin package isn't present
        }

        // Setup the variables and commands exposed in an OSGi environment.
        processor.addConstant(CONTEXT, context);
        processor.addCommand("osgi", processor, "addCommand");
        processor.addCommand("osgi", processor, "removeCommand");
        processor.addCommand("osgi", processor, "eval");

        context.registerService(CommandProcessor.class.getName(), processor, null);

        return processor;
    }

    public static ServiceTracker trackOSGiCommands(final CommandProcessorImpl processor, final BundleContext context)
            throws InvalidSyntaxException
    {
        Filter filter = context.createFilter(String.format("(&(%s=*)(%s=*))",
                CommandProcessor.COMMAND_SCOPE, CommandProcessor.COMMAND_FUNCTION));

        return new ServiceTracker(context, filter, null)
        {
            private final ConcurrentMap<ServiceReference, Map<String, CommandProxy>> proxies
                    = new ConcurrentHashMap<ServiceReference, Map<String, CommandProxy>>();

            @Override
            public Object addingService(ServiceReference reference)
            {
                Object scope = reference.getProperty(CommandProcessor.COMMAND_SCOPE);
                Object function = reference.getProperty(CommandProcessor.COMMAND_FUNCTION);
                Object ranking = reference.getProperty(Constants.SERVICE_RANKING);
                List<Object> commands = new ArrayList<Object>();

                int rank = 0;
                if (ranking != null)
                {
                    try
                    {
                        rank = Integer.parseInt(ranking.toString());
                    }
                    catch (NumberFormatException e)
                    {
                        // Ignore
                    }
                }
                if (scope != null && function != null)
                {
                    Map<String, CommandProxy> proxyMap = new HashMap<String, CommandProxy>();
                    if (function.getClass().isArray())
                    {
                        for (Object f : ((Object[]) function))
                        {
                            CommandProxy target = new CommandProxy(context, reference, f.toString());
                            proxyMap.put(f.toString(), target);
                            processor.addCommand(scope.toString(), target, f.toString(), rank);
                            commands.add(target);
                        }
                    }
                    else
                    {
                        CommandProxy target = new CommandProxy(context, reference, function.toString());
                        proxyMap.put(function.toString(), target);
                        processor.addCommand(scope.toString(), target, function.toString(), rank);
                        commands.add(target);
                    }
                    proxies.put(reference, proxyMap);
                    return commands;
                }
                return null;
            }

            @Override
            public void removedService(ServiceReference reference, Object service)
            {
                Object scope = reference.getProperty(CommandProcessor.COMMAND_SCOPE);
                Object function = reference.getProperty(CommandProcessor.COMMAND_FUNCTION);

                if (scope != null && function != null)
                {
                    Map<String, CommandProxy> proxyMap = proxies.remove(reference);
                    for (Map.Entry<String, CommandProxy> entry : proxyMap.entrySet())
                    {
                        processor.removeCommand(scope.toString(), entry.getKey(), entry.getValue());
                    }
                }

                super.removedService(reference, service);
            }
        };
    }

    public static CommandProcessorImpl start(ThreadIO tio, final BundleContext context) throws Exception
    {
        final CommandProcessorImpl processor = newProcessor(tio, context);

        ServiceTracker commandTracker = trackOSGiCommands(processor, context);
        commandTracker.open();

        ServiceTracker converterTracker = new ServiceTracker(context, Converter.class.getName(), null)
        {
            @Override
            public Object addingService(ServiceReference reference)
            {
                Converter converter = (Converter) super.addingService(reference);
                processor.addConverter(converter);
                return converter;
            }

            @Override
            public void removedService(ServiceReference reference, Object service)
            {
                processor.removeConverter((Converter) service);
                super.removedService(reference, service);
            }
        };
        converterTracker.open();

        ServiceTracker listenerTracker = new ServiceTracker(context, CommandSessionListener.class.getName(), null)
        {
            @Override
            public Object addingService(ServiceReference reference) {
                CommandSessionListener listener = (CommandSessionListener) super.addingService(reference);
                processor.addListener(listener);
                return listener;
            }

            @Override
            public void removedService(ServiceReference reference, Object service) {
                processor.removeListener((CommandSessionListener) service);
                super.removedService(reference, service);
            }
        };
        listenerTracker.open();

        return processor;
    }


}
