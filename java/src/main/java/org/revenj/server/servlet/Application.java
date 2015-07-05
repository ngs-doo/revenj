package org.revenj.server.servlet;

import org.revenj.Revenj;
import org.revenj.patterns.Container;
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
		context.addServlet("crud", new CrudServlet(container)).addMapping("/Crud.svc/*");
		context.addServlet("domain", new DomainServlet(container)).addMapping("/Domain.svc/*");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}
}