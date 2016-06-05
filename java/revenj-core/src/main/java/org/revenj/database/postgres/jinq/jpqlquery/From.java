package org.revenj.database.postgres.jinq.jpqlquery;

public abstract class From implements JinqPostgresFragment {
	abstract void generateFromString(QueryGenerationState queryState, boolean isFirst);

	public boolean isPrecededByComma() {
		return true;
	}

	protected void prepareQueryGeneration(
			Expression.QueryGenerationPreparationPhase preparePhase,
			QueryGenerationState queryState) {
		if (preparePhase == Expression.QueryGenerationPreparationPhase.FROM) {
			// Generate aliases for each of the FROM entries
			queryState.generateFromAlias(this);
		}
	}

	public static From forDataSource(String name) {
		FromDataSource from = new FromDataSource();
		from.name = name;
		return from;
	}

	public static From forNavigationalLinks(Expression linksExpr) {
		FromNavigationalLinks from = new FromNavigationalLinks();
		from.links = linksExpr;
		return from;
	}

	public static FromNavigationalLinksLeftOuterJoin forNavigationalLinksLeftOuterJoin(FromNavigationalLinks link) {
		FromNavigationalLinksLeftOuterJoin from = new FromNavigationalLinksLeftOuterJoin();
		from.links = link.links;
		return from;
	}

	public static FromNavigationalLinksLeftOuterJoinFetch forNavigationalLinksLeftOuterJoinFetch(FromNavigationalLinks link) {
		FromNavigationalLinksLeftOuterJoinFetch from = new FromNavigationalLinksLeftOuterJoinFetch();
		from.links = link.links;
		return from;
	}

	public static FromNavigationalLinksJoinFetch forNavigationalLinksJoinFetch(FromNavigationalLinks link) {
		FromNavigationalLinksJoinFetch from = new FromNavigationalLinksJoinFetch();
		from.links = link.links;
		return from;
	}

	public static class FromDataSource extends From {
		public String name;

		@Override
		void generateFromString(QueryGenerationState queryState, boolean isFirst) {
			queryState.appendQuery(name);
		}
	}

	public static abstract class FromNavigationalLinksGeneric extends From {
		public Expression links;
	}

	public static class FromNavigationalLinks extends FromNavigationalLinksGeneric {
		@Override
		void generateFromString(QueryGenerationState queryState, boolean isFirst) {
			if (!isFirst)
				queryState.appendQuery(" JOIN ");
			links.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
		}

		@Override
		public boolean isPrecededByComma() {
			return false;
		}
	}

	public static class FromNavigationalLinksLeftOuterJoin extends FromNavigationalLinksGeneric {
		@Override
		void generateFromString(QueryGenerationState queryState, boolean isFirst) {
			if (!isFirst)
				queryState.appendQuery(" LEFT OUTER JOIN ");
			links.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
		}

		@Override
		public boolean isPrecededByComma() {
			return false;
		}
	}

	public static class FromNavigationalLinksLeftOuterJoinFetch extends FromNavigationalLinksGeneric {
		@Override
		void generateFromString(QueryGenerationState queryState, boolean isFirst) {
			if (!isFirst)
				queryState.appendQuery(" LEFT OUTER JOIN FETCH ");
			links.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
		}

		@Override
		public boolean isPrecededByComma() {
			return false;
		}
	}

	public static class FromNavigationalLinksJoinFetch extends FromNavigationalLinksGeneric {
		@Override
		void generateFromString(QueryGenerationState queryState, boolean isFirst) {
			if (!isFirst)
				queryState.appendQuery(" JOIN FETCH ");
			links.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
		}

		@Override
		public boolean isPrecededByComma() {
			return false;
		}
	}
}
