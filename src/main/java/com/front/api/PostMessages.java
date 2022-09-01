package com.front.api;

import org.springframework.stereotype.Component;

import io.swagger.annotations.ApiModelProperty;

@Component
public class PostMessages {

	@ApiModelProperty(value = "message", example = "18FEF31C3D422397722E724B", required = true)
	private String message;

	public String getMessage() {
		return this.message;
	}
	
}
