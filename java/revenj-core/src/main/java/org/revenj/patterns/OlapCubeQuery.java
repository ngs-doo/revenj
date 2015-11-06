package org.revenj.patterns;

import java.util.*;

public interface OlapCubeQuery<TSource extends DataSource> {
	Set<String> getDimensions();

	Set<String> getFacts();

	List<Map<String, Object>> analyze(
			List<String> dimensions,
			List<String> facts,
			Collection<Map.Entry<String, Boolean>> order,
			Specification<TSource> filter,
			Integer limit,
			Integer offset);

	default List<Map<String, Object>> analyze(
			List<String> dimensions,
			List<String> facts,
			Specification<TSource> filter) {
		return analyze(dimensions, facts, null, filter, null, null);
	}

	default List<Map<String, Object>> analyze(
			List<String> dimensions,
			List<String> facts) {
		return analyze(dimensions, facts, null, null, null, null);
	}

	default OlapCubeQueryBuilder<TSource> builder() {
		return new OlapCubeQueryBuilder(this);
	}

	class OlapCubeQueryBuilder<TSource extends DataSource> {

		private final OlapCubeQuery<TSource> query;
		private final List<String> dimensions = new ArrayList<>();
		private final List<String> facts = new ArrayList<>();
		private Integer resultLimit;
		private Integer resultOfset;
		private final Map<String, Boolean> order = new LinkedHashMap<>();

		OlapCubeQueryBuilder(OlapCubeQuery<TSource> query) {
			this.query = query;
		}

		public OlapCubeQueryBuilder<TSource> use(String dimensionOrFact) {
			if (query.getDimensions().contains(dimensionOrFact)) {
				dimensions.add(dimensionOrFact);
			} else if (query.getFacts().contains(dimensionOrFact)) {
				facts.add(dimensionOrFact);
			} else {
				throw new IllegalArgumentException("Unknown dimension or fact: " + dimensionOrFact +
						". Use getDimensions or getFacts method for available dimensions and facts");
			}
			return this;
		}

		public OlapCubeQueryBuilder<TSource> ascending(String result) {
			return orderBy(result, true);
		}

		public OlapCubeQueryBuilder<TSource> descending(String result) {
			return orderBy(result, false);
		}

		private OlapCubeQueryBuilder<TSource> orderBy(String result, boolean ascending) {
			if(!query.getDimensions().contains(result) && !query.getFacts().contains(result)) {
				throw new IllegalArgumentException("Unknown result: " + result
						+ ". Result can be only field from used dimensions and facts.");
			}
			order.put(result, ascending);
			return this;
		}

		public OlapCubeQueryBuilder<TSource> take(int limit) {
			return limit(limit);
		}

		public OlapCubeQueryBuilder<TSource> limit(int limit) {
			if (limit < 1) {
				throw new IllegalArgumentException("Invalid limit value. Limit must be positive");
			}
			resultLimit = limit;
			return this;
		}

		public OlapCubeQueryBuilder<TSource> skip(int offset) {
			return offset(offset);
		}

		public OlapCubeQueryBuilder<TSource> offset(int offset) {
			if (offset < 1) {
				throw new IllegalArgumentException("Invalid offset value. Offset must be positive");
			}
			resultOfset = offset;
			return this;
		}

		public List<Map<String, Object>> analyze() {
			return query.analyze(
					dimensions,
					facts,
					order.entrySet(),
					null,
					resultLimit,
					resultOfset
			);
		}

		public List<Map<String, Object>> analyze(Specification<TSource> specification) {
			return query.analyze(
					dimensions,
					facts,
					order.entrySet(),
					specification,
					resultLimit,
					resultOfset
			);
		}
	}
}