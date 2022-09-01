package com.front.api;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

// metadata in http://localhost:8080/v2/api-docs
// swagger in http://localhost:8080/swagger-ui.html#/

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	@Bean
	public Docket apiDocket() {
		return new Docket(DocumentationType.SWAGGER_2)
				.useDefaultResponseMessages(false)						// Remove default HTTP responses
				.select()
				.apis(RequestHandlerSelectors.basePackage("com.front.api"))
				.paths(PathSelectors.any())
				.build()
				.apiInfo(getApiInfo())
				;
		
	}
	$TODO$
	private ApiInfo getApiInfo() {
		return new ApiInfo(
				"EISOBUS enabler",
				"This API offers the capability of sending and recieving data from truck implements such as engine speed, fuel temperature, atmospheric pressure, grain moisture and other parameters stated at  \"J1939-71 - Vehicle Application Layer\"",
				"1.0",
				new Contact("Alex Inza Gubía", "https://elliotcloud.com", "alexgubia@outlook.es"),
				"Contact",
				"https://elliotcloud.com/contacta/",
				Collections.emptyList()
				);
	}
}

