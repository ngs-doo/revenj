package org.revenj;

import org.revenj.extensibility.PluginLoader;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;

final class ServicesPluginLoader implements PluginLoader {

	private static final String PREFIX = "META-INF/services/";
	private static final Charset UTF8 = Charset.forName("UTF-8");

	private final ClassLoader loader;

	ServicesPluginLoader(ClassLoader loader) {
		this.loader = loader != null ? loader : ClassLoader.getSystemClassLoader();
	}

	public <T> List<Class<T>> find(Class<T> manifest, Type... types) throws Exception {
		String fullName;
		if (types.length == 0) {
			fullName = PREFIX + manifest.getName();
		} else if (types.length == 1) {
			fullName = PREFIX + URLEncoder.encode(Utils.makeGenericType(manifest, types[0]).toString(), "UTF-8");
		} else {
			fullName = PREFIX + URLEncoder.encode(Utils.makeGenericType(manifest, types[0], Arrays.stream(types).skip(1).toArray(i -> new Type[types.length - 1])).toString(), "UTF-8");
		}
		//TODO: release class loader to avoid locking up jars on Windows
		Enumeration<URL> configs = loader.getResources(fullName);
		List<Class<T>> plugins = new ArrayList<>();
		while (configs.hasMoreElements()) {
			URL url = configs.nextElement();
			lookupServices(manifest, url, plugins);
		}
		return plugins;
	}

	private <T> void lookupServices(Class<T> manifest, URL u, List<Class<T>> plugins) throws Exception {
		try (InputStream stream = u.openStream();
		     BufferedReader reader = new BufferedReader(new InputStreamReader(stream, UTF8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				int ci = line.indexOf('#');
				if (ci >= 0) line = line.substring(0, ci);
				line = line.trim();
				int n = line.length();
				if (n != 0) {
					if ((line.indexOf(' ') >= 0) || (line.indexOf('\t') >= 0)) {
						throw new IOException("Invalid configuration for " + manifest + " in " + u);
					}
					int cp = line.codePointAt(0);
					if (!Character.isJavaIdentifierStart(cp)) {
						throw new IOException("Invalid configuration for " + manifest + " in " + u);
					}
					for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
						cp = line.codePointAt(i);
						if (!Character.isJavaIdentifierPart(cp) && (cp != '.')) {
							throw new IOException("Invalid configuration for " + manifest + " in " + u);
						}
					}
					Class<?> service = loader.loadClass(line);
					plugins.add((Class<T>) service);
				}
			}
		}
	}
}
