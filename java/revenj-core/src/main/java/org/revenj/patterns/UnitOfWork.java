package org.revenj.patterns;

import java.io.Closeable;

public interface UnitOfWork extends DataContext, Closeable {
	void commit();

	void rollback();
}
