package org.revenj.server.servlet;

import org.revenj.patterns.DomainModel;
import org.revenj.security.UserPrincipal;
import org.revenj.server.CommandResult;
import org.revenj.server.ProcessingEngine;
import org.revenj.server.ProcessingResult;
import org.revenj.server.ServerCommandDescription;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.Optional;

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

	static String readString(InputStream stream, String encoding) throws IOException {
		if (stream == null) {
			return null;
		}
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final byte[] buffer = new byte[1024];
		int len;
		while ((len = stream.read(buffer)) != -1) {
			baos.write(buffer, 0, len);
		}
		if (baos.size() == 0) {
			return null;
		}
		return new String(baos.toByteArray(), encoding != null && encoding.length() > 0 ? encoding : "UTF-8");
	}

	static void returnJSON(HttpServletResponse response, ProcessingResult<String> result) {
		if (result.executedCommandResults.length == 1) {
			CommandResult<String> command = result.executedCommandResults[0].result;
			response.setStatus(command.status);
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
			response.setStatus(result.status);
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

	static Optional<String> findName(DomainModel model, String path, String prefix, HttpServletResponse res) throws IOException {
		String name = path.substring(prefix.length(), path.length());
		Optional<Class<?>> manifest = model.find(name);
		if (!manifest.isPresent()) {
			res.sendError(400, "Unknown domain object: " + name);
			return Optional.empty();
		}
		return Optional.of(name);
	}

	static Optional<Class<?>> findType(DomainModel model, String path, String prefix, HttpServletResponse res) throws IOException {
		String name = path.substring(prefix.length(), path.length());
		Optional<Class<?>> manifest = model.find(name);
		if (!manifest.isPresent()) {
			res.sendError(400, "Unknown domain object: " + name);
		}
		return manifest;
	}
}
