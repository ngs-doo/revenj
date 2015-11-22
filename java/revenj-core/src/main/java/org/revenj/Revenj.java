package org.revenj;

import org.postgresql.ds.PGPoolingDataSource;
import org.revenj.extensibility.Container;
import org.revenj.json.DslJsonSerialization;
import org.revenj.patterns.*;
import org.revenj.security.PermissionManager;
import org.revenj.extensibility.PluginLoader;
import org.revenj.extensibility.SystemAspect;
import org.revenj.serialization.Serialization;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
		String jdbcUrl = properties.getProperty("revenj.jdbcUrl");
		if (jdbcUrl == null) {
			throw new IOException("revenj.jdbcUrl is missing from revenj.properties");
		}
		String plugins = properties.getProperty("revenj.pluginsPath");
		File pluginsPath = null;
		if (plugins != null) {
			File pp = new File(plugins);
			pluginsPath = pp.isDirectory() ? pp : null;
		}
		org.postgresql.ds.PGPoolingDataSource dataSource = new PGPoolingDataSource();
		dataSource.setUrl(jdbcUrl);
		String user = properties.getProperty("user");
		String revUser = properties.getProperty("revenj.user");
		if (revUser != null && revUser.length() > 0) {
			dataSource.setUser(revUser);
		} else if (user != null && user.length() > 0) {
			dataSource.setUser(user);
		}
		String password = properties.getProperty("password");
		String revPassword = properties.getProperty("revenj.password");
		if (revPassword != null && revPassword.length() > 0) {
			dataSource.setPassword(revPassword);
		} else if (password != null && password.length() > 0) {
			dataSource.setPassword(password);
		}
		return setup(
				dataSource,
				properties,
				Optional.ofNullable(pluginsPath),
				Optional.ofNullable(Thread.currentThread().getContextClassLoader()));
	}

	public static Container setup(
			DataSource dataSource,
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
		return setup(dataSource, properties, Optional.of(loader), aspects.iterator());
	}

	private static class SimpleDomainModel implements DomainModel {

		private String namespace;
		private final ClassLoader loader;
		private final ConcurrentMap<String, Class<?>> cache = new ConcurrentHashMap<>();

		public SimpleDomainModel(String namespace, ClassLoader loader) {
			this.namespace = namespace != null && namespace.length() > 0 ? namespace + "." : "";
			this.loader = loader;
		}

		void updateNamespace(String namespace) {
			this.namespace = namespace != null && namespace.length() > 0 ? namespace + "." : "";
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
			DataSource dataSource,
			Properties properties,
			Optional<ClassLoader> classLoader,
			Iterator<SystemAspect> aspects) throws IOException {
		ClassLoader loader = classLoader.orElse(Thread.currentThread().getContextClassLoader());
		SimpleContainer container = new SimpleContainer("true".equals(properties.getProperty("revenj.resolveUnknown")));
		container.registerInstance(properties);
		container.registerInstance(ServiceLocator.class, container, false);
		container.registerInstance(DataSource.class, dataSource, false);
		String ns = properties.getProperty("revenj.namespace");
		SimpleDomainModel domainModel = new SimpleDomainModel(ns, loader);
		container.registerInstance(DomainModel.class, domainModel, false);
		container.registerFactory(DataContext.class, LocatorDataContext::asDataContext, false);
		container.registerFactory(UnitOfWork.class, LocatorDataContext::asUnitOfWork, false);
		PluginLoader plugins = new ServicesPluginLoader(loader);
		container.registerInstance(PluginLoader.class, plugins, false);
		PostgresDatabaseNotification databaseNotification =
				new PostgresDatabaseNotification(
						dataSource,
						Optional.of(domainModel),
						properties,
						container);
		container.registerInstance(EagerNotification.class, databaseNotification, false);
		container.registerInstance(DataChangeNotification.class, databaseNotification, true);
		ChangeNotification.registerContainer(container, databaseNotification);
		container.registerInstance(PermissionManager.class, new RevenjPermissionManager(container), false);
		container.registerInstance(ClassLoader.class, loader, false);
		container.registerClass(new Generic<Serialization<String>>() {
		}.type, DslJsonSerialization.class, false);
		int total = 0;
		if (aspects != null) {
			while (aspects.hasNext()) {
				aspects.next().configure(container);
				total++;
			}
		}
		String nsAfter = properties.getProperty("revenj.namespace");
		if (!Objects.equals(ns, nsAfter)) {
			domainModel.updateNamespace(nsAfter);
		}
		properties.setProperty("revenj.aspectsCount", Integer.toString(total));
		return container;
	}
}
