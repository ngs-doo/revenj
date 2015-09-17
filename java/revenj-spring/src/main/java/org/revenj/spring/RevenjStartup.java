package org.revenj.spring;

import org.revenj.Revenj;
import org.revenj.patterns.DataContext;
import org.revenj.patterns.ServiceLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

public class RevenjStartup {

	@Autowired
	private DataSource dataSource;
	@Autowired
	private ApplicationContext context;
	@Autowired
	private Properties properties;

	@PostConstruct
	public void setup() throws IOException {
		Function<ServiceLocator, Connection> connectionFactory = sl -> {
			try {
				return dataSource.getConnection();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		};
		String path = properties.getProperty("revenj.pluginsPath");
		File file = path != null ? new File(path) : null;
		ServiceLocator locator =
				Revenj.setup(
						connectionFactory,
						properties,
						file != null && file.exists() && file.isDirectory() ? Optional.of(file) : Optional.<File>empty(),
						Optional.of(context.getClassLoader()));
		ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) context).getBeanFactory();
		beanFactory.registerSingleton(ServiceLocator.class.getCanonicalName(), locator);
		beanFactory.registerSingleton(DataContext.class.getCanonicalName(), locator.resolve(DataContext.class));
	}
}