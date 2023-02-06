package com.bot.uni.config;

import com.fasterxml.jackson.databind.*;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class JacksonMapperConfig {
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	public ObjectMapper jacksonObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		return mapper;
	}
	
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	public ObjectWriter jacksonObjectWriter(ObjectMapper objectMapper) {
		return objectMapper.writer();
	}
	
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	public ObjectReader jacksonObjectReader(ObjectMapper objectMapper) {
		return objectMapper.reader();
	}

}
