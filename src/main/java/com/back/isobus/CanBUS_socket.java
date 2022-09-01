package com.back.isobus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;



/**
 * Class for opening and closing CAN bus interfaces and sending and receiving frames over the bus. 
 * It uses JNI wrapper to access C code and manage easily the CAN socket.
 */
@Component
public class CanBUS_socket {
	
	// CAN utilities load
	private String can_util = ResourceUtils.getResourceAsFile("can_utilities.sh").getAbsolutePath();
	
	static {
		System.load(ResourceUtils.getResourceAsFile("libisocan.so").getAbsolutePath());
	}
	
	
	private String can_interface;
	private int bitrate;
	private int file_descriptor;
	private Map<String, String> last_received_frame = new HashMap<String, String>();
	
	public CanBUS_socket() {}

	
	private native int __canOpenRaw(String can_interface);														// Open can socket using RAW protocol
	private native boolean __canOpenBCM(String can_interface);													// Open can socket using broadcast manager
	private native boolean __canClose(int fd);																	// Close any open communications
	private native boolean __canSendFrame(int fd, long frame_id, String frame_payload, int payload_length);		// Send frames separately (in case it has to iterate?)
	private native boolean __canReceiveFrame(int fd, FrameCAN frame);
	
	/**
	 * Opens a CAN socket using RAW protocol.
	 * @param can_interface CAN socket to open.
	 * @return Returns the file descriptor or -1 if it could not be opened.
	 */
	public int canOpenRaw(String can_interface) {		 // Añadir comprobación de que ya existe esta abierta esa comunicación
		return (this.canExist(can_interface) ? __canOpenRaw(can_interface) : -1);
	}
	
	/**
	 * Closes a CAN socket if it is already opened.
	 * @param fd File descriptor of the active CAN socket.
	 * @return Returns true if closing was successful or false if it can not be closed.
	 */
	public boolean canClose(int fd) {		
		return (fd < 0 ? false : __canClose(fd));
	}
	
	/**
	 * Receives a CAN bus frame.
	 * @param fd File descriptor of the CAN socket instantiated.
	 * @param frame_obj Datatype in which the frame is stored.
	 * @return False if an error occurs or True if a CAN frame is receiver successfully.
	 */
	public boolean canReceiveFrame(int fd, FrameCAN frame_obj) {
		if (this.__canReceiveFrame(fd, frame_obj) == false) return false;
		frame_obj.correctPayloadAndMark();
		return true;
	}
	
	/**
	 * Sends a frame over a CAN bus interface using a FrameCAN object.
	 * @param fd File descriptor of the active CAN socket.
	 * @param frame_obj Datatype containing the ISOBUS CAN frame.
	 * @return	Returns true if the frame has been successfully sent or false if an error occurs.
	 */
	public boolean canSendFrame(int fd, FrameCAN frame_obj) {
		long frame_id;
		String payload;
		frame_id = frame_obj.getFrameIdLong(); 
		payload = frame_obj.getFramePayloadHex();
		
		try {
			if (!this.__canSendFrame(fd, frame_id, payload, payload.length() / 2)) {
				throw new Exception("Frame error");
			}
			return true;
		}
		catch (Exception e) {
			System.err.println("Error occured " + e);
			return false;
		}
	}
	
	/**
	 * Sends a frame over a CAN bus interface using an String.
	 * @param fd File descriptor of the active CAN socket.
	 * @param full_frame String containing the frame.
	 * @return Returns true if the frame has been successfully sent or false if an error occurs.
	 */
	public boolean canSendFrame(int fd, String full_frame) {	// Mejorar gestión de errores, más variedad e identificativos (error parseo trama, error envío, ...)
		long frame_id;
		String payload;
		
		try {
			frame_id = Long.parseLong(full_frame.substring(0, 8), 16);
			payload = full_frame.substring(8, full_frame.length());
			
			return this.__canSendFrame(fd, frame_id, payload, payload.length() / 2);
		}
		catch (Exception e) {
			System.err.println("Error occured " + e);
			return false;
		}
	}

	/**
	 * Executes a bash script which initializes a real CAN bus interface with an specified bitrate (ISOBUS, for example has a default baudrate of 250kb/s).
	 * @param can_interface CAN interface to initialize.
	 * @param bitrate Bitrate specified to initialize the interface.
	 * @return Returns true if the initialization of the CAN bus was successful or false if an error occurs.
	 */
	public boolean canInit(String can_interface, int bitrate) {
		this.can_interface = can_interface;
		this.bitrate = bitrate;

	    if (!this.canExist(can_interface)) {
	    	ProcessBuilder processBuilder = new ProcessBuilder();
	    	processBuilder.command("bash", "-c", can_util + " -a " + this.can_interface + " " + bitrate);
	    	
	    	try {
		        Process process = processBuilder.start();
		        int exitVal = process.waitFor();
		        
		        if (exitVal == 0) {
		            return true;
		        }
		        else {
		            System.err.println(this.can_interface + " already exists or and error has ocurred.");
		        }
		    } 
	    	catch (Exception e) {
		    	e.printStackTrace();
		    	return false;
		    }
	    }
	    return false;
	}
	
	/**
	 * Executes a bash script which initializes a virtual CAN bus interface with an specified bitrate.
	 * @param can_interface Virtual CAN interface to initialize.
	 * @return Returns true if the initialization of the CAN bus was successful or false if an error occurs.
	 */
	public boolean canInitV(String can_interface) {
		this.can_interface = can_interface;

	    if (!this.canExist(can_interface)) {
	    	ProcessBuilder processBuilder = new ProcessBuilder();
	    	processBuilder.command("bash", "-c", can_util + " -i " + this.can_interface);
	    	
	    	try {
		        Process process = processBuilder.start();
		        int exitVal = process.waitFor();
		        
		        if (exitVal == 0) {
		            return true;
		        }
		        else {
		            System.err.println(this.can_interface + " already exists or and error has ocurred.");
			    }
		    } 
	    	catch (Exception e) {
		    	e.printStackTrace();
		    	return false;
		    }
	    }
	    return false;
	}
	
	/**
	 * List every active CAN and virtual CAN device using "ip link show" system command.
	 * @return Returns every CAN interface or -1 if an error occurs.
	 */
	public ArrayList<String> listCanDevices() {		
		ArrayList<String> devices = new ArrayList<String>();
		ProcessBuilder processBuilder = new ProcessBuilder();
	    processBuilder.command("bash", "-c", "ip link show");

	    try {
	    	String line;
	    	Process process = processBuilder.start();
	        BufferedReader reader = new BufferedReader(
	                new InputStreamReader(process.getInputStream()));

	        while ((line = reader.readLine()) != null) {		// Iterate over every line of the response
	        	String[] line_splt = line.split(":");
	        	if (line_splt.length == 3) {						// No se si esto contempla todos los casos
	        		String aux = line_splt[1].replaceAll("\\s", "");
	        		if (aux.contains("can")) devices.add(aux);
	        	}
	        }
	        return devices;
	    }
	    catch (Exception e) {
	    	System.err.println("Error! " +e);
	    	devices.clear();
	    	devices.add("-1");
	    	return devices;
	    }
	}
	
	/**
	 * Delete the specified CAN interface executing a bash script.
	 * @param can_interface CAN interface to be deleted.
	 * @return Returns true if the interface was successfully deleted or false if an error occurred.
	 */
	public boolean canDelete(String can_interface) {
		if (this.canExist(can_interface)) {
			ProcessBuilder processBuilder = new ProcessBuilder();
	    	processBuilder.command("bash", "-c", can_util + " -d " + can_interface);
	    	
	    	try {
		        Process process = processBuilder.start();
		        int exitVal = process.waitFor();
		        
		        if (exitVal == 0) {
		            return true;
		        } 
		        else {
		            System.err.println(can_interface + " does not exist or and error has ocurred.");
			    }	
		    } 
	    	catch (Exception e) {
		    	e.printStackTrace();
		    	return false;
		    }
		}
		return false;
	}
	
	/**
	 * Checks if a CAN interface exists.
	 * @param can_interface CAN interface to check its existence.
	 * @return Returns true if the interface exists or false if it does not.
	 */
	private boolean canExist(String can_interface) {
		return this.listCanDevices().contains(can_interface);
	}
}
