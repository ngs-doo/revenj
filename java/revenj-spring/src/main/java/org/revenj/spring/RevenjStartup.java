package org.revenj.spring;

import org.revenj.Revenj;
import org.revenj.patterns.DataChangeNotification;
import org.revenj.patterns.DataContext;
import org.revenj.patterns.ServiceLocator;
import org.revenj.security.PermissionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

@Configuration
public class RevenjStartup {

	@Autowired
	private DataSource dataSource;
	@Autowired
	private ApplicationContext context;
	@Autowired
	private Properties properties;

	@Bean
	public ServiceLocator serviceLocator() throws IOException {
		Function<ServiceLocator, Connection> connectionFactory = sl -> {
			try {
				return dataSource.getConnection();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		};
		String path = properties.getProperty("revenj.pluginsPath");
		File file = path != null ? new File(path) : null;
		return Revenj.setup(
				connectionFactory,
				properties,
				file != null && file.exists() && file.isDirectory() ? Optional.of(file) : Optional.<File>empty(),
				Optional.of(context.getClassLoader()));
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
}