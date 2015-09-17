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

@Component
public class JacksonSetup {
	@Autowired
	private RequestMappingHandlerAdapter handlerAdapter;
	@Autowired
	private ServiceLocator locator;

	@PostConstruct
	public void init() {
		for (HttpMessageConverter<?> messageConverter : handlerAdapter.getMessageConverters()) {
			if (messageConverter instanceof MappingJackson2HttpMessageConverter) {
				MappingJackson2HttpMessageConverter m = (MappingJackson2HttpMessageConverter) messageConverter;
				ObjectMapper mapper = m.getObjectMapper();
				if (mapper == null) mapper = new ObjectMapper();
				mapper.setInjectableValues(new InjectableValues.Std().addValue("__locator", locator));
				m.setObjectMapper(mapper);
			}
		}
	}
}