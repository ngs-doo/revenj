package org.revenj;

import org.revenj.extensibility.Container;
import org.revenj.storage.S3Repository;

import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

public abstract class Storage {
	public void configure(Container container, Optional<ExecutorService> executorService) {
		Properties properties = container.resolve(Properties.class);
		container.registerInstance(S3Repository.class, new AmazonS3Repository(properties, executorService), true);
	}
}
