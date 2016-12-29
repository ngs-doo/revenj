package org.revenj.server.servlet;

import org.revenj.patterns.DomainModel;
import org.revenj.security.UserPrincipal;
import org.revenj.serialization.WireSerialization;
import org.revenj.server.CommandResult;
import org.revenj.server.ProcessingEngine;
import org.revenj.server.ProcessingResult;
import org.revenj.server.ServerCommandDescription;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.security.Principal;
import java.time.*;
import java.util.*;

abstract class Utility {
	private static final Charset UTF8 = Charset.forName("UTF-8");

	static void execute(
			ProcessingEngine engine,
			HttpServletRequest request,
			HttpServletResponse response,
			WireSerialization serialization,
			Class<?> commandType,
			Object argument) throws IOException {
		ServerCommandDescription[] scd = new ServerCommandDescription[]{
				new ServerCommandDescription<>(null, commandType, argument)
		};
		ProcessingResult<Object> result = engine.execute(Object.class, Object.class, scd, toPrincipal(request));
		returnResponse(request, response, serialization, result);
	}

	static void returnResponse(HttpServletRequest request, HttpServletResponse response, WireSerialization serialization, ProcessingResult<Object> result) throws IOException {
		if (result.executedCommandResults.length == 1) {
			CommandResult<Object> command = result.executedCommandResults[0].result;
			response.setStatus(command.status);
			response.setHeader("X-Duration", BigDecimal.valueOf(result.duration, 3).toPlainString());
			if (command.data != null) {
				response.setContentType(serialization.serialize(command.data, response.getOutputStream(), request.getHeader("accept")));
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
				response.setContentType("text/plain; charset=UTF-8");
				response.getOutputStream().write(result.message.getBytes(UTF8));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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

	public static Principal toPrincipal(HttpServletRequest req) {
		Principal principal = req.getUserPrincipal();
		return principal != null && !(principal instanceof UserPrincipal)
				? new UserPrincipal(principal.getName(), req::isUserInRole)
				: principal;
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

	static Optional<Object> objectFromQuery(Class<?> manifest, HttpServletRequest req, HttpServletResponse res) throws IOException {
		try {
			Object instance = manifest.newInstance();
			String queryString = req.getQueryString();
			if (queryString == null || queryString.length() == 0) {
				return Optional.of(instance);
			}
			Map<String, Method> methods = new HashMap<>();
			for (Method m : manifest.getMethods()) {
				if (m.getName().startsWith("set")) {
					methods.put(m.getName().substring(3), m);
				}
			}
			for (String p : queryString.split("&")) {
				int eqInd = p.indexOf('=');
				if (eqInd < 1) {
					continue;
				}
				String key = p.substring(0, eqInd);
				Method m = methods.get(key.substring(0, 1).toUpperCase() + key.substring(1));
				if (m != null) {
					m.invoke(instance, changeType(req.getParameter(p), m.getParameterTypes()[0]));
				}
			}
			return Optional.of(instance);
		} catch (ReflectiveOperationException ex) {
			res.sendError(400, "Unable to initialize argument: " + ex.getMessage());
			return Optional.empty();
		}
	}

	static Optional<Object> specificationFromQuery(
			String parent,
			String name,
			DomainModel model,
			HttpServletRequest req,
			HttpServletResponse res) throws IOException {
		if (name == null || name.length() == 0) return Optional.empty();
		Optional<Class<?>> specManifest = model.find(parent + "+" + name);
		if (!specManifest.isPresent()) {
			specManifest = model.find(name);
			if (!specManifest.isPresent()) {
				res.sendError(400, "Unknown domain object: " + name);
				return Optional.empty();
			}
		}
		return Utility.objectFromQuery(specManifest.get(), req, res);
	}

	static <T> Optional<T> deserializeOrBadRequest(
			WireSerialization serialization,
			Class<T> manifest,
			HttpServletRequest req,
			HttpServletResponse res) throws IOException {
		try {
			return Optional.of(serialization.deserialize(req.getInputStream(), req.getContentType(), manifest));
		} catch (IOException e) {
			res.sendError(400, "Error deserializing input: " + e.getMessage());
			return Optional.empty();
		}
	}

	static <T> Optional<T> deserializeOrBadRequest(
			WireSerialization serialization,
			HttpServletRequest req,
			HttpServletResponse res,
			Class<T> container,
	        Type manifest) throws IOException {
		try {
			return Optional.of(serialization.deserialize(req.getInputStream(), req.getContentType(), container, manifest));
		} catch (IOException e) {
			res.sendError(400, "Error deserializing input: " + e.getMessage());
			return Optional.empty();
		}
	}

	static Optional<Object> specificationFromStream(
			WireSerialization serialization,
			String parent,
			String name,
			DomainModel model,
			InputStream stream,
			HttpServletResponse res) throws IOException {
		if (name == null || name.length() == 0) return Optional.empty();
		Optional<Class<?>> specManifest = model.find(parent + "+" + name);
		if (!specManifest.isPresent()) {
			specManifest = model.find(name);
			if (!specManifest.isPresent()) {
				res.sendError(400, "Unknown domain object: " + name);
				return Optional.empty();
			}
		}
		try {
			return Optional.of(serialization.deserialize(specManifest.get(), stream, "application/json"));
		} catch (IOException e) {
			res.sendError(400, "Error deserializing specification: " + e.getMessage());
			return Optional.empty();
		}
	}

	static Boolean returnInstance(HttpServletRequest request) throws IOException {
		String result = request.getParameter("result");
		if (result == null) {
			result = request.getHeader("x-revenj-result");
		}
		return "instance".equals(result) ? Boolean.TRUE : "uri".equals(result) ? Boolean.FALSE : null;
	}

	public static List<Map.Entry<String, Boolean>> parseOrder(String order) {
		if (order == null || order.isEmpty()) return null;
		List<Map.Entry<String, Boolean>> sortOrder = new ArrayList<>();
		String[] parts = order.split(",");
		for (String p : parts) {
			if (p.startsWith("+") || p.startsWith("-")) {
				sortOrder.add(new AbstractMap.SimpleEntry<>(p.substring(1), p.startsWith("+")));
			} else {
				sortOrder.add(new AbstractMap.SimpleEntry<>(p, true));
			}
		}
		return sortOrder;
	}

	static class OlapInfo {
		public final String[] dimensions;
		public final String[] facts;
		public final List<Map.Entry<String, Boolean>> order;
		public final Integer limit;
		public final Integer offset;

		public OlapInfo(HttpServletRequest req) {
			this.dimensions = req.getParameter("dimensions") == null ? null : req.getParameter("dimensions").split(",");
			this.facts = req.getParameter("facts") == null ? null : req.getParameter("facts").split(",");
			this.order = parseOrder(req.getParameter("order"));
			this.limit = req.getParameter("limit") != null ? Integer.parseInt(req.getParameter("limit")) : null;
			this.offset = req.getParameter("offset") != null ? Integer.parseInt(req.getParameter("offset")) : null;
		}
	}

	private static Object changeType(String argument, Class<?> target) throws ReflectiveOperationException {
		if (String.class.equals(target)) return argument;
		if (Integer.class.equals(target) || int.class.equals(target)) return Integer.parseInt(argument);
		if (Long.class.equals(target) || long.class.equals(target)) return Long.parseLong(argument);
		if (Float.class.equals(target) || float.class.equals(target)) return Float.parseFloat(argument);
		if (Double.class.equals(target) || double.class.equals(target)) return Double.parseDouble(argument);
		if (BigDecimal.class.equals(target)) return new BigDecimal(argument);
		if (LocalDate.class.equals(target)) return LocalDate.parse(argument);
		if (OffsetDateTime.class.equals(target)) return OffsetDateTime.parse(argument);
		if (LocalDateTime.class.equals(target)) return LocalDateTime.parse(argument);
		if (UUID.class.equals(target)) return UUID.fromString(argument);
		throw new ReflectiveOperationException("Unsupported target type: " + target);
	}
}
