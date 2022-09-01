package com.front.api;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

import io.swagger.annotations.ApiModelProperty;

@Component
public class PostConfig {
	@ApiModelProperty(value = "devices", dataType= "list", example = "[\"can0\", \"vcan1\", \"vcan0\"]", required = true)
	private ArrayList<String> devices;
	
	public ArrayList<String> getDevices() {
		return this.devices;
	}
}
