package org.revenj;

import org.revenj.extensibility.PluginLoader;
import org.revenj.patterns.Container;
import org.revenj.patterns.DomainModel;
import org.revenj.server.ProcessingEngine;
import org.revenj.server.ServerCommand;

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
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		};
		return setup(factory, properties, Optional.<File>empty(), Optional.<ClassLoader>empty());
	}

	public static Container setup(
			Container.Factory<Connection> connectionFactory,
			Properties properties,
			Optional<File> pluginsPath,
			Optional<ClassLoader> classLoader) throws IOException {
		ClassLoader loader;
		if (pluginsPath.isPresent()) {
			File[] jars = pluginsPath.get().listFiles(f -> f.getPath().toLowerCase().endsWith(".jar"));
			List<URL> urls = new ArrayList<>(jars.length);
			for (File j : jars) {
				try {
					urls.add(j.toURI().toURL());
				} catch (MalformedURLException ex) {
					throw new IOException(ex);
				}
			}
			loader = classLoader.isPresent()
					? new URLClassLoader(urls.toArray(new URL[urls.size()]), classLoader.get())
					: new URLClassLoader(urls.toArray(new URL[urls.size()]));
		} else {
			loader = ClassLoader.getSystemClassLoader();
		}
		ServiceLoader<SystemAspect> aspects = ServiceLoader.load(SystemAspect.class, loader);
		return setup(connectionFactory, properties, Optional.of(loader), aspects.iterator());
	}

	private static class SimpleDomainModel implements DomainModel {

		private final String namespace;

		public SimpleDomainModel(String namespace) {
			this.namespace = namespace != null && namespace.length() > 0 ? namespace + "." : "";
		}

		@Override
		public Optional<Class<?>> find(String name) {
			try {
				return Optional.of(Class.forName(namespace + name));
			} catch (ClassNotFoundException ignore) {
				return Optional.empty();
			}
		}
	}

	public static Container setup(
			Container.Factory<Connection> connectionFactory,
			Properties properties,
			Optional<ClassLoader> classLoader,
			Iterator<SystemAspect> aspects) throws IOException {
		SimpleContainer container = new SimpleContainer();
		container.register(properties);
		container.register(Connection.class, connectionFactory);
		container.registerInstance(DomainModel.class, new SimpleDomainModel(properties.getProperty("namespace")), false);
		PluginLoader plugins = new PluginLoader(classLoader.orElse(null));
		container.register(plugins);
		try {
			container.register(new ProcessingEngine(container, Optional.of(plugins), classLoader));
		} catch (Exception e) {
			throw new IOException(e);
		}
		if (classLoader.isPresent()) {
			container.registerInstance(ClassLoader.class, classLoader.get(), false);
		}
		if (aspects != null) {
			while (aspects.hasNext()) {
				aspects.next().configure(container);
			}
		}
		return container;
	}
}
