package org.revenj.server.servlet;

import org.revenj.Revenj;
import org.revenj.extensibility.Container;
import org.revenj.extensibility.PluginLoader;
import org.revenj.patterns.WireSerialization;
import org.revenj.server.ProcessingEngine;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

public class Application implements ServletContextListener {

	private final Container container;

	public Application() throws IOException {
		container = Revenj.setup();
		Properties properties = container.resolve(Properties.class);
		if ("0".equals(properties.getProperty("aspects-count"))) {
			String pluginsPath = properties.getProperty("pluginsPath");
			if (pluginsPath == null) {
				throw new IOException("System aspects not configured. Probably an error in the configuration.\n" +
						"Specify pluginsPath in Properties file (currently not set).");
			} else if (!new File(pluginsPath).isDirectory()) {
				throw new IOException("System aspects not configured. Probably an error in the configuration.\n" +
						"Specified pluginsPath: " + pluginsPath + " is not an directory.");
			}
			throw new IOException("System aspects not configured. Probably an error in the configuration.\n" +
					"Check if pluginsPath (" + pluginsPath + ") is correctly set in Properties file.");
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		configure(context, container);
	}

	public static void configure(ServletContext context, Container container) {
		Optional<PluginLoader> plugins = container.tryResolve(PluginLoader.class);
		WireSerialization serialization = container.resolve(WireSerialization.class);
		try {
			container.register(new ProcessingEngine(container, serialization, plugins));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		context.addServlet("crud", new CrudServlet(container)).addMapping("/Crud.svc/*");
		context.addServlet("domain", new DomainServlet(container)).addMapping("/Domain.svc/*");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}
}