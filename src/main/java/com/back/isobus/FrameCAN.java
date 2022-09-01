package com.back.isobus;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


/**
 * This class defines a datatype for a CAN frame with timestamp.
 */
public class FrameCAN {
	
	private DateTimeFormatter formatter;
	private String timestamp;
	private String payload;
	private long id;

	
	/**
	 * Class constructor. It assigns a timestamp based on UTC.
	 */
	public FrameCAN() { 				// Igual podría tener de parámetros el formato y la zona horaria ( Locale sitio )	
		this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
						.withZone(ZoneId.of("UTC"));
	}
	
	/**
	 * Removes the empty spaces of the payload and assigns now() to the timestamp.
	 */
	void correctPayloadAndMark() {
		this.payload = this.payload.trim();
		this.timestamp = formatter.format(Instant.now());
	}

	/**
	 * Parses a given frame into its ID and payload.
	 * @param frame Frame to parse.
	 */
	public void setFrame(String frame) {
		this.timestamp = formatter.format(Instant.now());
		try {
			this.id = Long.parseLong(frame.substring(0, 8), 16); 
			this.payload = frame.substring(8, frame.length());
		}
		catch (Exception e) {
			System.err.println("Incorrect frame format.");
		}
	}
	
	/**
	 * Retrieves the frame ID in hexadecimal format.
	 * @return Hexadecimal ID.
	 */
	public String getFrameIdHex() {
		//return Long.toHexString(this.id);
		return String.format("%08X", this.id);
	}
	
	/**
	 * Retrieves the frame ID in decimal format.
	 * @return Decimal ID.
	 */
	public long getFrameIdLong() {
		return this.id;
	}
	
	/**
	 * Retrieves the frame payload in hexadecimal format.
	 * @return Hexadecimal payload.
	 */
	public String getFramePayloadHex() {
		return this.payload;
	}
	
	/**
	 * Retrieves the frame payload in decimal format.
	 * @return Decimal payload.
	 */
	public long getFramePayloadLong() {
		return Long.parseLong(this.payload, 16);
	}
	
	/**
	 * Retrieves the complete frame in hexadecimal format.
	 * @return Hexadecimal complete frame.
	 */
	public String getFrame() {
		return String.format("%08X", this.id) + this.payload;
	}
	
	/**
	 * Retrieves the timestamp associated to the FrameCAN object.
	 * @return Timestamp.
	 */
	public String getTimestamp() {
		return this.timestamp;
	}

	/**
	 * Retrieves the instance of the FrameCAN object.
	 * @return FrameCAN object.
	 */
	public FrameCAN getInstance() {
		return this;
	}
	
}