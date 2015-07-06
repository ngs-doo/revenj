package org.revenj.server.servlet;

import org.revenj.Revenj;
import org.revenj.patterns.ServiceLocator;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

//@WebListener
public abstract class Application /*implements ServletContextListener*/ {

	/*private final ServiceLocator locator;

	public Application() throws IOException {
		locator = Revenj.setup();
		Properties properties = locator.resolve(Properties.class);
		if ("0".equals(properties.getProperty("aspects-count"))) {
			String pluginsPath = properties.getProperty("pluginsPath");
			if (pluginsPath == null) {
				throw new IOException("System aspects not configured. Probably an error in configuration.\n" +
						"Specify pluginsPath in Properties file (currently not set).");
			} else if (!new File(pluginsPath).isDirectory()) {
				throw new IOException("System aspects not configured. Probably an error in configuration.\n" +
						"Specified pluginsPath: " + pluginsPath + " is not an directory.");
			}
			throw new IOException("System aspects not configured. Probably an error in configuration.\n" +
					"Check if pluginsPath (" + pluginsPath + ") is correctly set in Properties file.");
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		configure(context, locator);
	}
*/
	public static void configure(ServletContext context, ServiceLocator locator) {
		context.addServlet("crud", new CrudServlet(locator)).addMapping("/Crud.svc/*");
		context.addServlet("domain", new DomainServlet(locator)).addMapping("/Domain.svc/*");
	}
/*
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}*/
}