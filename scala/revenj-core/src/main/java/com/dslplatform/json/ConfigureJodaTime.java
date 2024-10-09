package com.dslplatform.json;

public class ConfigureJodaTime implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerReader(org.joda.time.DateTime.class, JodaTimeConverter.DATE_TIME_READER);
		json.registerWriter(org.joda.time.DateTime.class, JodaTimeConverter.DATE_TIME_WRITER);
		json.registerReader(org.joda.time.LocalDate.class, JodaTimeConverter.LOCAL_DATE_READER);
		json.registerWriter(org.joda.time.LocalDate.class, JodaTimeConverter.LOCAL_DATE_WRITER);
	}
}