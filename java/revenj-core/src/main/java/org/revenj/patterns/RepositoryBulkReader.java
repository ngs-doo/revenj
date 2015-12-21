package org.revenj.patterns;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

public interface RepositoryBulkReader {
	void reset();

	<T extends Identifiable> Callable<Optional<T>> find(Class<T> manifest, String uri);

	<T extends Identifiable> Callable<List<T>> find(Class<T> manifest, String[] uri);

	<T extends DataSource> Callable<List<T>> search(Class<T> manifest, Specification<T> filter, Integer limit, Integer offset);

	default <T extends DataSource> Callable<List<T>> search(Class<T> manifest, Specification<T> filter) {
		return search(manifest, filter, null, null);
	}

	default <T extends DataSource> Callable<List<T>> search(Class<T> manifest) {
		return search(manifest, null, null, null);
	}

	default <T extends DataSource> Callable<List<T>> search(Class<T> manifest, int limit) {
		return search(manifest, null, limit, null);
	}

	<T extends DataSource> Callable<Long> count(Class<T> manifest, Specification<T> filter);

	default <T extends DataSource> Callable<Long> count(Class<T> manifest) {
		return count(manifest, null);
	}

	<T extends DataSource> Callable<Boolean> exists(Class<T> manifest, Specification<T> filter);

	default <T extends DataSource> Callable<Boolean> exists(Class<T> manifest) {
		return exists(manifest, null);
	}

	void execute() throws IOException;
}
