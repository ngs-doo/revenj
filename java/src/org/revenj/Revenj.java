package org.revenj;

import org.revenj.patterns.Container;
import org.revenj.patterns.ServiceLocator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public abstract class Revenj {

	public interface SystemAspect {
		void configure(Container container) throws IOException;
	}

	public static ServiceLocator setup(String connectionString) throws IOException {
		Properties properties = new Properties();
		File revProps = new File("revenj.properties");
		if (revProps.exists() && revProps.isFile()) {
			properties.load(new FileReader(revProps));
		}
		Container.Factory<Connection> factory = c -> {
			try {
				return DriverManager.getConnection(connectionString, properties);
			} catch (SQLException ignore) {
				return null;
			}
		};
		return setup(factory, ".", properties, Optional.<ClassLoader>empty());
	}

	public static ServiceLocator setup(
			Container.Factory<Connection> connectionFactory,
			String pluginsPath,
			Properties properties,
			Optional<ClassLoader> classLoader) throws IOException {
		File loc = new File(pluginsPath);
		File[] jars = loc.listFiles(f -> f.getPath().toLowerCase().endsWith(".jar"));
		List<URL> urls = new ArrayList<URL>(jars.length);
		for (File j : jars) {
			try {
				urls.add(j.toURI().toURL());
			} catch (MalformedURLException ex) {
				throw new IOException(ex);
			}
		}
		URLClassLoader ucl = classLoader.isPresent()
				? new URLClassLoader(urls.toArray(new URL[urls.size()]), classLoader.get())
				: new URLClassLoader(urls.toArray(new URL[urls.size()]));
		ServiceLoader<SystemAspect> plugins = ServiceLoader.load(SystemAspect.class, ucl);
		SimpleContainer container = new SimpleContainer();
		container.register(properties);
		container.register(Connection.class, false, connectionFactory);
		for (SystemAspect aspect : plugins) {
			aspect.configure(container);
		}
		ucl.close();
		return container;
	}
}
