package org.revenj.spring;

import org.revenj.Revenj;
import org.revenj.extensibility.Container;
import org.revenj.patterns.DataChangeNotification;
import org.revenj.patterns.DataContext;
import org.revenj.patterns.ServiceLocator;
import org.revenj.patterns.UnitOfWork;
import org.revenj.postgres.QueryProvider;
import org.revenj.postgres.jinq.transform.MetamodelUtil;
import org.revenj.security.PermissionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

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
		String path = properties.getProperty("revenj.pluginsPath");
		File file = path != null ? new File(path) : null;
		Container container =
				Revenj.setup(
						dataSource,
						properties,
						file != null && file.exists() && file.isDirectory() ? Optional.of(file) : Optional.<File>empty(),
						Optional.of(context.getClassLoader()));
		container.registerInstance(DataSource.class, dataSource, false);
		MetamodelUtil metamodel = container.resolve(MetamodelUtil.class);
		container.registerInstance(QueryProvider.class, new JinqQueryProvider(metamodel, dataSource), false);
		return container;
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

	@Bean
	@Scope("prototype")
	public UnitOfWork unitOfWork(ServiceLocator locator) {
		return locator.resolve(UnitOfWork.class);
	}

}