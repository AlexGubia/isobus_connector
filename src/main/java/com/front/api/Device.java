package com.front.api;

import java.util.ArrayList;
import java.util.List;

import com.elliotcloud.isobus.FrameCAN;

public class Device {
	private String deviceID ;
	private int fileDescriptor;
	private List<FrameCAN> frames = new ArrayList<FrameCAN>();
	private FrameCAN lastFrame;
	
	public Device(String device, int fd) {
		this.deviceID = device;
		this.fileDescriptor = fd;
	}
	
	public int getFD() {
		return this.fileDescriptor;
	}
	
	public FrameCAN lastFrame() {
		return this.lastFrame;
	}
	 
	public void addFrame(FrameCAN fr) {
		this.lastFrame = fr;	
		this.frames.add(fr);
	}
	
	public void setFD(int fd) {
		this.fileDescriptor = fd;
	}
	
	public List<FrameCAN> getFrames() {
		return this.frames;
	}
	
	public String getDeviceID() {
		return this.deviceID;
	}
}
