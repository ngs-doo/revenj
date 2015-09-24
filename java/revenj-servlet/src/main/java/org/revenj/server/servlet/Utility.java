package org.revenj.server.servlet;

import org.revenj.security.UserPrincipal;
import org.revenj.server.CommandResult;
import org.revenj.server.ProcessingEngine;
import org.revenj.server.ProcessingResult;
import org.revenj.server.ServerCommandDescription;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.Principal;

abstract class Utility {
	private static final Charset UTF8 = Charset.forName("UTF-8");

	static void executeJson(
			ProcessingEngine engine,
			HttpServletRequest request,
			HttpServletResponse ressponse,
			Class<?> command,
			Object argument) {
		ServerCommandDescription[] scd = new ServerCommandDescription[]{
				new ServerCommandDescription<>(null, command, argument)
		};
		ProcessingResult<String> result = engine.execute(Object.class, String.class, scd, toPrincipal(request));
		returnJSON(ressponse, result);
	}

	static void returnJSON(HttpServletResponse response, ProcessingResult<String> result) {
		response.setStatus(result.status);
		if (result.executedCommandResults.length == 1) {
			CommandResult<String> command = result.executedCommandResults[0].result;
			if (command.data != null) {
				response.setContentType("application/json");
				try {
					response.getOutputStream().write(command.data.getBytes(UTF8));
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (result.message != null) {
				try {
					response.getOutputStream().write(result.message.getBytes(UTF8));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (result.message != null) {
			try {
				response.getOutputStream().write(result.message.getBytes(UTF8));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static Principal toPrincipal(HttpServletRequest req) {
		return req.getUserPrincipal() != null && !(req.getUserPrincipal() instanceof UserPrincipal)
				? new UserPrincipal(req.getUserPrincipal().getName(), req::isUserInRole)
				: req.getUserPrincipal();
	}
}
