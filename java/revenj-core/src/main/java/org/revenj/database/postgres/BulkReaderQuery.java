package org.revenj.database.postgres;

import java.sql.PreparedStatement;
import java.util.function.Consumer;

public interface BulkReaderQuery {
	PostgresWriter getWriter();
	
	PostgresReader getReader();

	StringBuilder getBuilder();

	int getArgumentIndex();

	void addArgument(Consumer<PreparedStatement> statement);
}
