package org.revenj;

import org.revenj.patterns.Container;

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

	public static Container setup(String jdbcUrl) throws IOException {
		Properties properties = new Properties();
		File revProps = new File("revenj.properties");
		if (revProps.exists() && revProps.isFile()) {
			properties.load(new FileReader(revProps));
		}
		Container.Factory<Connection> factory = c -> {
			try {
				return DriverManager.getConnection(jdbcUrl, properties);
			} catch (SQLException ignore) {
				return null;
			}
		};
		return setup(factory, new File("."), properties, Optional.<ClassLoader>empty());
	}

	public static Container setup(
			Container.Factory<Connection> connectionFactory,
			File pluginsPath,
			Properties properties,
			Optional<ClassLoader> classLoader) throws IOException {
		File[] jars = pluginsPath.listFiles(f -> f.getPath().toLowerCase().endsWith(".jar"));
		List<URL> urls = new ArrayList<>(jars.length);
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
		Container container = setup(connectionFactory, properties, plugins.iterator());
		ucl.close();
		return container;
	}

	public static Container setup(
			Container.Factory<Connection> connectionFactory,
			Properties properties,
			Iterator<SystemAspect> aspects) throws IOException {
		SimpleContainer container = new SimpleContainer();
		container.register(properties);
		container.register(Connection.class, connectionFactory);
		while (aspects.hasNext()) {
			aspects.next().configure(container);
		}
		return container;
	}
}
