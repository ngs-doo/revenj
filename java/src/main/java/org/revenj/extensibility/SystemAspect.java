package org.revenj.extensibility;

import java.io.IOException;

public interface SystemAspect {
	void configure(Container container) throws IOException;
}