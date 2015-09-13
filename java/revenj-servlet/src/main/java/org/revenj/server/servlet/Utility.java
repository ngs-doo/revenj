package org.revenj.server.servlet;

import org.revenj.server.CommandResult;
import org.revenj.server.ProcessingEngine;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;

abstract class Utility {
	private static final Charset UTF8 = Charset.forName("UTF-8");

	static void executeJson(ProcessingEngine engine, HttpServletResponse res, Class<?> command, Object argument) {
		CommandResult<String> result = engine.executeJson(command, argument);
		res.setStatus(result.status);
		if (result.data != null) {
			res.setContentType("application/json");
			try {
				res.getOutputStream().write(result.data.getBytes(UTF8));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (result.message != null) {
			try {
				res.getOutputStream().write(result.message.getBytes(UTF8));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
