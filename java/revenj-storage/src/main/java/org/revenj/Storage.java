package org.revenj;

import org.revenj.extensibility.Container;
import org.revenj.storage.S3Repository;

import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

public abstract class Storage {
	public static void configure(Container container) {
		Properties properties = container.resolve(Properties.class);
		Optional<ExecutorService> executorService = container.tryResolve(ExecutorService.class);
		configure(container, properties, executorService);
	}

	public static void configure(Container container, Properties properties, Optional<ExecutorService> executorService) {
		container.registerInstance(S3Repository.class, new AmazonS3Repository(properties, executorService), true);
	}
}
