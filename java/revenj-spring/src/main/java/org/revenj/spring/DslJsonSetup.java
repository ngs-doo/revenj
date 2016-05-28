package org.revenj.spring;

import org.revenj.serialization.json.DslJsonSerialization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.annotation.PostConstruct;

@Component
public class DslJsonSetup {
	@Autowired
	private RequestMappingHandlerAdapter handlerAdapter;
	@Autowired
	private DslJsonSerialization serialization;

	@PostConstruct
	public void init() {
		handlerAdapter.getMessageConverters().add(0, new DslJsonMessageConverter(serialization));
	}
}