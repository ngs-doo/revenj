package org.revenj;

import org.revenj.extensibility.Container;
import org.revenj.patterns.*;
import org.revenj.security.PermissionManager;
import org.revenj.serialization.RevenjSerialization;
import org.revenj.extensibility.PluginLoader;
import org.revenj.extensibility.SystemAspect;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public abstract class Revenj {
	public static Container setup() throws IOException {
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
		String jdbcUrl = properties.getProperty("revanj.jdbcUrl");
		if (jdbcUrl == null) {
			throw new IOException("revenj.jdbcUrl is missing from revenj.properties");
		}
		String plugins = properties.getProperty("revenj.pluginsPath");
		File pluginsPath = null;
		if (plugins != null) {
			File pp = new File(plugins);
			pluginsPath = pp.isDirectory() ? pp : null;
		}
		Function<ServiceLocator, Connection> factory = c -> {
			try {
				return DriverManager.getConnection(jdbcUrl, properties);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		};
		return setup(
				factory,
				properties,
				Optional.ofNullable(pluginsPath),
				Optional.ofNullable(Thread.currentThread().getContextClassLoader()));
	}

	public static Container setup(
			Function<ServiceLocator, Connection> connectionFactory,
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
		} else if (classLoader.isPresent()) {
			loader = classLoader.get();
		} else {
			loader = Thread.currentThread().getContextClassLoader();
		}
		ServiceLoader<SystemAspect> aspects = ServiceLoader.load(SystemAspect.class, loader);
		return setup(connectionFactory, properties, Optional.of(loader), aspects.iterator());
	}

	private static class SimpleDomainModel implements DomainModel {

		private final String namespace;
		private final ClassLoader loader;
		private final ConcurrentMap<String, Class<?>> cache = new ConcurrentHashMap<>();

		public SimpleDomainModel(String namespace, ClassLoader loader) {
			this.namespace = namespace != null && namespace.length() > 0 ? namespace + "." : "";
			this.loader = loader;
		}

		@Override
		public Optional<Class<?>> find(String name) {
			Class<?> found = cache.get(name);
			if (found != null) {
				return Optional.of(found);
			}
			try {
				Class<?> manifest = Class.forName(namespace + name, true, loader);
				cache.put(name, manifest);
				return Optional.of(manifest);
			} catch (ClassNotFoundException ignore) {
				return Optional.empty();
			}
		}
	}

	public static Container setup(
			Function<ServiceLocator, Connection> connectionFactory,
			Properties properties,
			Optional<ClassLoader> classLoader,
			Iterator<SystemAspect> aspects) throws IOException {
		ClassLoader loader = classLoader.orElse(Thread.currentThread().getContextClassLoader());
		SimpleContainer container = new SimpleContainer("true".equals(properties.getProperty("resolveUnknown")));
		container.register(properties);
		container.register(Connection.class, connectionFactory);
		DomainModel domainModel = new SimpleDomainModel(properties.getProperty("namespace"), loader);
		container.registerInstance(DomainModel.class, domainModel, false);
		container.registerClass(DataContext.class, LocatorDataContext.class, false);
		PluginLoader plugins = new ServicesPluginLoader(loader);
		container.registerInstance(PluginLoader.class, plugins, false);
		WireSerialization serialization = new RevenjSerialization(container);
		container.registerInstance(WireSerialization.class, serialization, false);
		PostgresDatabaseNotification databaseNotification =
				new PostgresDatabaseNotification(
						connectionFactory,
						Optional.of(domainModel),
						properties,
						container);
		container.registerInstance(EagerNotification.class, databaseNotification, false);
		container.registerInstance(DataChangeNotification.class, databaseNotification, true);
		ChangeNotification.registerContainer(container, databaseNotification);
		container.registerInstance(PermissionManager.class, new RevenjPermissionManager(container), false);
		if (classLoader.isPresent()) {
			container.registerInstance(ClassLoader.class, classLoader.get(), false);
		}
		if (aspects == null) {
			throw new IOException("aspects not provided");
		}
		int total = 0;
		while (aspects.hasNext()) {
			aspects.next().configure(container);
			total++;
		}
		properties.setProperty("aspects-count", Integer.toString(total));
		return container;
	}
}
