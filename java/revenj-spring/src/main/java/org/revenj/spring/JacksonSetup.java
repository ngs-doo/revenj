package org.revenj.spring;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.revenj.patterns.ServiceLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Component
public class JacksonSetup {
	@Autowired
	private RequestMappingHandlerAdapter handlerAdapter;
	@Autowired
	private ServiceLocator locator;

	public static Optional<MappingJackson2HttpMessageConverter> findJackson(RequestMappingHandlerAdapter handlerAdapter) {
		for (HttpMessageConverter<?> messageConverter : handlerAdapter.getMessageConverters()) {
			if (messageConverter instanceof MappingJackson2HttpMessageConverter) {
				return Optional.of((MappingJackson2HttpMessageConverter) messageConverter);
			}
		}
		return Optional.empty();
	}

	@PostConstruct
	public void init() {
		configure(handlerAdapter, locator);
	}

	public static void configure(RequestMappingHandlerAdapter handlerAdapter, ServiceLocator locator) {
		findJackson(handlerAdapter).ifPresent(m -> {
			ObjectMapper mapper = m.getObjectMapper();
			if (mapper == null) mapper = new ObjectMapper();
			mapper.setInjectableValues(new InjectableValues.Std().addValue("__locator", locator));
			m.setObjectMapper(mapper);
		});
	}
}