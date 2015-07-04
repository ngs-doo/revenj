package org.revenj.server.servlet;

import org.revenj.Revenj;
import org.revenj.patterns.Container;
import org.revenj.server.ProcessingEngine;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;

@WebListener
public class Application implements ServletContextListener {

	private final Container container;

	public Application() throws IOException {
		Properties properties = new Properties();
		File revProps = new File("revenj.properties");
		if (revProps.exists() && revProps.isFile()) {
			properties.load(new FileReader(revProps));
		} else {
			String location = System.getProperty("revenj.properties");
			if (location != null) {
				revProps = new File(location);
				if (revProps.exists() && revProps.isFile()) {
					properties.load(new FileReader(revProps));
				} else {
					throw new IOException("Unable to find revenj.properties in alternative location. Searching in: " + revProps.getAbsolutePath());
				}
			} else {
				throw new IOException("Unable to find revenj.properties. Searching in: " + revProps.getAbsolutePath());
			}
		}
		String jdbcUrl = properties.getProperty("jdbcUrl");
		if (jdbcUrl == null) {
			throw new IOException("jdbcUrl is missing from revenj.properties");
		}
		String plugins = properties.getProperty("pluginsPath");
		File pluginsPath = null;
		if (plugins != null) {
			File pp = new File(plugins);
			pluginsPath = pp.isDirectory() ? pp : null;
		}
		Container.Factory<Connection> factory = c -> {
			try {
				return DriverManager.getConnection(jdbcUrl, properties);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		};
		container = Revenj.setup(factory, properties, Optional.ofNullable(pluginsPath), Optional.<ClassLoader>empty());
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		ProcessingEngine engine = container.resolve(ProcessingEngine.class);
		context.addServlet("crud", new CrudServlet(container, engine)).addMapping("/Crud.svc/*");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}
}