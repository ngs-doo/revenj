package org.revenj.server;

import org.revenj.extensibility.Container;
import org.revenj.security.PermissionManager;
import org.revenj.serialization.WireSerialization;

import javax.sql.DataSource;

public class TestProcessingEngine {
	public static ProcessingEngine create(
			Container container,
			DataSource dataSource,
			WireSerialization serialization,
			PermissionManager permissions,
			ServerCommand ...commands) throws Exception {
		return new ProcessingEngine(container, dataSource, serialization, permissions, commands);
	}
}
