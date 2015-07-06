package org.revenj.server.servlet;

import org.revenj.Revenj;
import org.revenj.patterns.Container;
import org.revenj.patterns.ServiceLocator;
import org.revenj.server.ProcessingEngine;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;

@WebListener
public class Application implements ServletContextListener {

	private final Container container;

	public Application() throws IOException {
		container = Revenj.setup();
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		configure(context, container);
	}

	public static void configure(ServletContext context, ServiceLocator locator) {
		context.addServlet("crud", new CrudServlet(locator)).addMapping("/Crud.svc/*");
		context.addServlet("domain", new DomainServlet(locator)).addMapping("/Domain.svc/*");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}
}