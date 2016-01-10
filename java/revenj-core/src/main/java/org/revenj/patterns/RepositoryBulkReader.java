package org.revenj.patterns;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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

	<TSource extends DataSource, TCube extends OlapCubeQuery<TSource>> Callable<List<Map<String, Object>>> analyze(
			Class<TCube> manifest,
			List<String> dimensionsAndFacts,
			Collection<Map.Entry<String, Boolean>> order,
			Specification<TSource> filter,
			Integer limit,
			Integer offset);

	default <TSource extends DataSource, TCube extends OlapCubeQuery<TSource>> Callable<List<Map<String, Object>>> analyze(
			Class<TCube> manifest,
			List<String> dimensionsAndFacts,
			Specification<TSource> filter) {
		return analyze(manifest, dimensionsAndFacts, null, filter, null, null);
	}

	void execute() throws IOException;
}
