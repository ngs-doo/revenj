package org.revenj.spring;

import com.dslplatform.json.DslJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.revenj.Revenj;
import org.revenj.extensibility.Container;
import org.revenj.serialization.json.DslJsonSerialization;
import org.revenj.patterns.DataChangeNotification;
import org.revenj.patterns.DataContext;
import org.revenj.patterns.ServiceLocator;
import org.revenj.database.postgres.QueryProvider;
import org.revenj.database.postgres.jinq.transform.MetamodelUtil;
import org.revenj.security.PermissionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Properties;

@Configuration
public class RevenjStartup {

	@Autowired(required = false)
	private DataSource dataSource;
	@Autowired
	private ApplicationContext context;
	@Autowired
	private Properties properties;
	@Autowired
	private RequestMappingHandlerAdapter handlerAdapter;

	@Bean
	public ServiceLocator serviceLocator() throws IOException {
		String path = properties.getProperty("revenj.pluginsPath");
		File file = path != null ? new File(path) : null;
		if (dataSource == null) {
			try {
				dataSource = Revenj.dataSource(properties);
			} catch (Exception e) {
				throw new IOException("Unable to setup Revenj. Unable to autowire Spring DataSource or setup Revenj Datasource. \n" +
						"Either configure DataSource within Spring or add revenj.jdbcUrl property.", e);
			}
		}
		Container container =
				Revenj.setup(
						dataSource,
						properties,
						file != null && file.exists() && file.isDirectory() ? Optional.of(file) : Optional.<File>empty(),
						Optional.of(context.getClassLoader()));
		container.registerInstance(DataSource.class, dataSource, false);
		setup(container);
		return container;
	}

	public static void setup(Container container) throws IOException {
		MetamodelUtil metamodel = container.resolve(MetamodelUtil.class);
		DataSource dataSource = container.resolve(DataSource.class);
		ClassLoader loader = container.resolve(ClassLoader.class);
		container.registerInstance(QueryProvider.class, new JinqQueryProvider(metamodel, loader, dataSource), false);
	}

	@Bean
	public DataContext dataContext(ServiceLocator locator) {
		return locator.resolve(DataContext.class);
	}

	@Bean
	public DataChangeNotification dataChange(ServiceLocator locator) {
		return locator.resolve(DataChangeNotification.class);
	}

	@Bean
	public PermissionManager permissionManager(ServiceLocator locator) {
		return locator.resolve(PermissionManager.class);
	}

	private static Optional<DslJson.Fallback<ServiceLocator>> buildFallback(ObjectMapper mapper) {
		if (mapper == null) {
			return Optional.empty();
		}
		return Optional.of(new DslJson.Fallback<ServiceLocator>() {
			@Override
			public void serialize(Object instance, OutputStream stream) throws IOException {
				mapper.writeValue(stream, instance);
			}

			@Override
			public Object deserialize(ServiceLocator serviceLocator, Type type, byte[] bytes, int len) throws IOException {
				return mapper.readValue(bytes, 0, len, mapper.getTypeFactory().constructType(type));
			}

			@Override
			public Object deserialize(ServiceLocator serviceLocator, Type type, InputStream stream) throws IOException {
				return mapper.readValue(stream, mapper.getTypeFactory().constructType(type));
			}
		});
	}

	@Bean
	public DslJsonSerialization dslJsonSerialization(ServiceLocator locator) {
		MappingJackson2HttpMessageConverter converter = JacksonSetup.findJackson(handlerAdapter).orElse(null);
		ObjectMapper mapper = converter == null ? null : converter.getObjectMapper();
		return new DslJsonSerialization(locator, buildFallback(mapper));
	}
}