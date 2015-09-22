package org.revenj.server.servlet;

import org.revenj.security.UserPrincipal;
import org.revenj.server.CommandResult;
import org.revenj.server.ProcessingEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.Principal;

abstract class Utility {
	private static final Charset UTF8 = Charset.forName("UTF-8");

	static void executeJson(
			ProcessingEngine engine,
			HttpServletRequest req,
			HttpServletResponse res,
			Class<?> command,
			Object argument) {
		Principal principal = req.getUserPrincipal() != null && !(req.getUserPrincipal() instanceof UserPrincipal)
				? new UserPrincipal(req.getUserPrincipal().getName(), req::isUserInRole)
				: req.getUserPrincipal();
		CommandResult<String> result = engine.executeJson(command, argument, principal);
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
