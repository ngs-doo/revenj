package org.revenj.server.servlet;

import org.revenj.Revenj;
import org.revenj.extensibility.Container;
import org.revenj.extensibility.PluginLoader;
import org.revenj.security.PermissionManager;
import org.revenj.serialization.WireSerialization;
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
		if ("0".equals(properties.getProperty("revenj.aspectsCount"))) {
			String pluginsPath = properties.getProperty("revenj.pluginsPath");
			if (pluginsPath == null) {
				throw new IOException("System aspects not configured. Probably an error in the configuration.\n" +
						"Specify pluginsPath in Properties file (currently not set).");
			} else if (!new File(pluginsPath).isDirectory()) {
				throw new IOException("System aspects not configured. Probably an error in the configuration.\n" +
						"Specified pluginsPath: " + pluginsPath + " is not an directory.");
			}
			throw new IOException("System aspects not configured. Probably an error in the configuration.\n" +
					"Check if revenj.pluginsPath (" + pluginsPath + ") is correctly set in the Properties file.");
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		try {
			configure(context, container);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void setup(Container container) throws Exception {
		Optional<PluginLoader> plugins = container.tryResolve(PluginLoader.class);
		PermissionManager permissions = container.resolve(PermissionManager.class);
		WireSerialization serialization = new RevenjSerialization(container);
		container.registerInstance(WireSerialization.class, serialization, false);
		container.register(new ProcessingEngine(container, serialization, permissions, plugins));
	}

	public static void configure(ServletContext context, Container container) throws Exception {
		setup(container);
		context.addServlet("rpc", new RpcServlet(container)).addMapping("/RestApplication.svc/*");
		context.addServlet("crud", new CrudServlet(container)).addMapping("/Crud.svc/*");
		context.addServlet("domain", new DomainServlet(container)).addMapping("/Domain.svc/*");
		context.addServlet("standard", new StandardServlet(container)).addMapping("/Commands.svc/*");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}
}