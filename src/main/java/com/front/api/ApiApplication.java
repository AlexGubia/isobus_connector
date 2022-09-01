package com.front.api;

import java.net.NetworkInterface;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

import com.elliotcloud.isobus.CanBUS_socket;
import com.elliotcloud.isobus.FrameCAN;
import com.elliotcloud.isobus.Parser;

/**
 * 
 * ApiApplication class is in charge of launching the main application which includes the CAN listener an the REST API, each one in its own thread
 *
 */
@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
public class ApiApplication {
	public static void main(String[] args) {
			
		CanManager cm = CanManager.getInstance();
		Thread tcm = new Thread(cm);			 

		tcm.start();
		
		System.out.println("-*- Starting SpringBoot API -*-");
		SpringApplication.run(ApiApplication.class, args);
	}
}

/**
 * 
 * CanListener class creates an all-time CAN frames receiver that stores them for later usage which its identifier.
 *
 */
class CanManager implements Runnable {
	
	private static CanManager clInstance;
	private String macAddress = null;

	public CanBUS_socket can = new CanBUS_socket();
	HashMap <String, Device> openDevices = new HashMap<String, Device>();
	Parser parser = new Parser();
	private HashMap<String, CanListener> threadPool = new HashMap<String, CanListener>();
	private CanManager() {}
	
	/**
	 * Access a existing CanListener instance.
	 * @return Instance.
	 */
	public static CanManager getInstance() {
		if (clInstance == null) clInstance = new CanManager();
		return clInstance;
	}
	
	/**
	 * Implements the thread for listening CAN bus.
	 */
	@Override
	public void run()  {
		ArrayList<String> inames =  new ArrayList<String>(Arrays.asList("eth0", "eth1", "eno0", "eno1"));
		
		try {
			Iterator<String> it = inames.iterator();
			NetworkInterface ni;
			do {
				String a = it.next();
				ni = NetworkInterface.getByName(a);
			} while (ni == null && it.hasNext());

            byte[] hardwareAddress = ni.getHardwareAddress();
            if (hardwareAddress != null) {
                String[] hexadecimalFormat = new String[hardwareAddress.length];
                for (int i = 0; i < hardwareAddress.length; i++) {
                    hexadecimalFormat[i] = String.format("%02X", hardwareAddress[i]);
                }
                this.macAddress = String.join("-", hexadecimalFormat);
            }
            System.out.println(macAddress);
		} catch (Exception e) {
			System.err.println("Couldn't get MAC adrress, using FF:FF:FF:FF:FF:FF instead.\n"+e);
			this.macAddress = "FF-FF-FF-FF-FF-FF";
		}
		
		postEntityISODevice(this.macAddress);
		
		while (true) {
			System.out.print("");
			try {
				if (!openDevices.isEmpty()) {
					infoDisplay();
				}	
			} catch (Exception e) {
				System.err.println("Something wrong happened! ");
				e.printStackTrace();
			} finally {			
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}	
		}	
	}
	
	class CanListener implements Runnable {
		private String device;
	    private AtomicBoolean running = new AtomicBoolean(false);
	    boolean existingEntity = false;
	    @SuppressWarnings("unused")
		private int messageIndex = 0;
	    
		CanListener (String dev) {
			this.device = dev;
		}

		@Override
		public void run() {
	        running.set(true);

			while(running.get()) {
				FrameCAN aux = new FrameCAN();
				can.canReceiveFrame(openDevices.get(this.device).getFD(), aux);
				openDevices.get(this.device).addFrame(aux);	
				
				threadMessage(aux.getFrame() + " in " + this.device);
				postEntityISOMessage(this.messageIndex, aux, this.device, macAddress);
				postEntitySPNValues(this.messageIndex, aux, device);
				this.messageIndex++;
			}
		}
		
		String getListenerDevice () {
			return this.device;
		}
		
		void kill() {
			running.set(false);
			Thread.currentThread().interrupt();
		}
	}

	void postEntityISODevice(String mac) {
		String isoname;
		isoname = System.getenv("ISOBUSDEVICE_NAME");
		if (isoname == null) {
			System.err.println("ISODevice name not specified, using deafault instead...");
			isoname = "Generic tractor implement";
		}


		String body = String.format("[\n" +
				"{\n" + 
				"        \"id\": \"urn:ngsi-ld:ISODevice:%s\",\n" + 
				"        \"type\": \"ISODevice\",\n" + 
				"        \"alternateName\": {\n" + 
				"            \"type\": \"Property\",\n" + 
				"            \"value\": \"%s\"\n" + 
				"        },\n" + 
				"        \"@context\": [\n" + 
				"            \"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\n" + 
				"            \"https://w3id.org/demeter/agri-context.jsonld\"\n" + 
				"        ]\n" + 
				"    }\n" +
				"]", mac, isoname); 
		//System.out.println(body);
		HttpClient client = HttpClient.newHttpClient();
	    HttpRequest request = null;
		request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:1026/ngsi-ld/v1/entityOperations/upsert"))
                .headers("Content-Type", "application/ld+json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
		
		try {
			HttpResponse<String> response = client.send(request,
			        HttpResponse.BodyHandlers.ofString());
			System.out.println("ISODevice: " + response);

		} catch (Exception e) {
			System.err.println(e);
		} 
	}	

	void postEntityISOMessage(int message_index, FrameCAN fr, String device, String mac) {
		String body = null;	
		try {
			JSONObject data = parser.parse_message(fr.getFrame(), "");
			JSONObject info = data.getJSONObject("info");
			body = String.format("[\n" +
					"    {\n" + 
					"        \"id\": \"urn:ngsi-ld:ISOMessage:%s\",\n" + 
					"        \"type\": \"ISOMessage\",\n" + 
					"        \"isoBusManufacturer\": {\n" + 
					"            \"type\": \"Property\",\n" + 
					"            \"value\": \"%s\"\n" + 
					"        },\n" + 
					"        \"msg\": {\n" + 
					"            \"type\": \"Property\",\n" + 
					"            \"value\": \"%s\"\n" + 
					"        },\n" + 
					"        \"header\": {\n" + 
					"            \"type\": \"Property\",\n" + 
					"            \"value\": \"%s\"\n" + 
					"        },\n" + 
					"        \"payload\": {\n" + 
					"            \"type\": \"Property\",\n" + 
					"            \"value\": \"%s\"\n" + 
					"        },\n" + 
					"        \"pgn\": {\n" + 
					"            \"type\": \"Property\",\n" + 
					"            \"value\": %s\n" + 
					"        },\n" + 
					"        \"sourceAddress\": {\n" + 
					"            \"type\": \"Property\",\n" + 
					"            \"value\": %S\n" + 
					"        },\n" + 
					"        \"priority\": {\n" + 
					"            \"type\": \"Property\",\n" + 
					"            \"value\": %S\n" + 
					"        },\n" + 
					"        \"payloadInt\": {\n" + 
					"            \"type\": \"Property\",\n" + 
					"            \"value\": %s\n" + 
					"        },\n" + 
					"        \"hasTimestamp\": {\n" + 
					"            \"type\": \"Property\",\n" + 
					"            \"value\": \"%s\"\n" + 
					"        },\n" + 
					"        \"refCAN\": {\n" + 
					"            \"type\": \"Relationship\",\n" + 
					"            \"object\": \"urn:ngsi-ld:CANBus:%s\"\n" + 
					"        },\n" + 
					"        \"@context\": [\n" + 
					"            \"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\n" + 
					"            \"https://w3id.org/demeter/agri-context.jsonld\"\n" + 
					"        ]\n" + 
					"    }\n" +
					"]", message_index, info.get("manufacturer"), 
										info.get("msg"),
										info.get("header"),
										info.get("payload"),
										info.get("pgn"),
										info.get("source"),
										info.get("priority"),
										info.get("payloadInt"),
										fr.getTimestamp(),
										device);
			//System.out.println(body);
		} catch (Exception e){
			System.err.println("Message not parsed. ISOMessage POST request avoided.");
			return;
		}
				
		HttpClient client = HttpClient.newHttpClient();
	    HttpRequest request = null;
		request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:1026/ngsi-ld/v1/entityOperations/upsert"))
                .headers("Content-Type", "application/ld+json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
		
		
		try {
			HttpResponse<String> response = client.send(request,
			        HttpResponse.BodyHandlers.ofString());
			System.out.println("ISOMessage: " + response);

		} catch (Exception e) {
			System.err.println(e);
		} 
	}		
	
	void postEntitySPNValues(int message_index, FrameCAN fr, String device) {
		String body = null;
		try {
			JSONObject data = parser.parse_message(fr.getFrame(), "");
			JSONObject spnValues = data.getJSONObject("spnValues");
			String midBody = "";
			for (String key : spnValues.keySet()) {
				midBody = midBody.concat(String.format("        \"%s\": {\n" + 
						"            \"type\": \"Property\",\n" + 
						"            \"value\": %s\n" + 
						"        },\n" , key, spnValues.get(key)) );
			}
			body = String.format("[\n" +
					" {\n" + 
					"        \"id\": \"urn:ngsi-ld:SPNValues:%s\",\n" + 
					"        \"type\": \"SPNValues\",\n" + 
					midBody + 
					"        \"refISOMessage\": {\n" + 
					"            \"type\": \"Relationship\",\n" + 
					"            \"object\": \"urn:ngsi-ld:ISOMessage:%s\"\n" + 
					"        },\n" + 
					"        \"@context\": [\n" + 
					"            \"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\",\n" + 
					"            \"https://w3id.org/demeter/agri-context.jsonld\"\n" + 
					"        ]\n" + 
					"    }\n" +
					"]", message_index, message_index);
			System.out.println(body);
		} catch (Exception e) {
			System.err.println("Message not parsed. SPNValues POST request avoided.");
			return;
		}
		HttpClient client = HttpClient.newHttpClient();
	    HttpRequest request = null;
		request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:1026/ngsi-ld/v1/entityOperations/upsert"))
                .headers("Content-Type", "application/ld+json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
		
		
		try {
			HttpResponse<String> response = client.send(request,
			        HttpResponse.BodyHandlers.ofString());
			System.out.println("SPNValues:" + response);

		} catch (Exception e) {
			System.err.println(e);
		} 
	}		
	

	private void removeThread(String poolDevice) {
		if(this.threadPool.containsKey(poolDevice)) {
			this.threadPool.get(poolDevice).kill();
			this.threadPool.remove(poolDevice);
		}
	}
	
	
	static void threadMessage(String message) {
		String threadName = Thread.currentThread().getName();
		System.err.format("%s: %s%n", threadName, message);
	}
	
	
	int getFD(String interf) {
		int fd = this.openDevices.get(interf).getFD();
		return (checkConnection(interf) ? fd : -1);
	}
	
	private void postEntityCANBus(String device, String mac) {
		String body = String.format("[\n" +
				"{\n" + 
				"        \"@id\": \"urn:ngsi-ld:CANBus:%s\",\n" + 
				"        \"@type\": \"CANBus\",\n" + 
				"        \"name\": {\n" + 
				"            \"value\": \"%s\",\n" + 
				"            \"type\": \"Property\"\n" + 
				"        },\n" + 
				"        \"bitrate\": {\n" + 
				"            \"value\": 250000,\n" + 
				"            \"type\": \"Property\"\n" + 
				"        },\n" + 
				"        \"refISODevice\": {\n" + 
				"            \"type\": \"Relationship\",\n" + 
				"            \"object\": \"urn:ngsi-ld:ISODevice:%s\"\n" + 
				"        },\n" + 
				"        \"@context\": [\n" + 
				"            \"https://w3id.org/demeter/agri-context.jsonld\"\n" + 
				"        ]\n" + 
				"    }\n" +
				"]", device, device, mac);
		//System.out.println(body);		
		HttpClient client = HttpClient.newHttpClient();
	    HttpRequest request = null;
		request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:1026/ngsi-ld/v1/entityOperations/upsert"))
                .headers("Content-Type", "application/ld+json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
		
		try {
			HttpResponse<String> response = client.send(request,
			        HttpResponse.BodyHandlers.ofString());
			System.out.println("CANBus:" + response);
		} catch (Exception e) {
			System.err.println(e); 
		} 
	}
	
	/**
	 * Open a new CAN bus socket and creates a thread for it to listen for frames.
	 * @param interfaces List of interfaces to open.
	 * @return Error code.
	 */
	public long openConnection(ArrayList<String> interfaces) {
		Device dev;
		int fd;
		int error = 0;
		
		for (String com : interfaces) {
			if (checkConnection(com)) {
				System.err.println("Connection " + com + " already opened");
				error |= 1<<interfaces.indexOf(com);
			}
			else if ( (fd = can.canOpenRaw(com)) == -1) {
				System.err.println("Invalid file descriptor: " + fd + " in " + com);
				error |= 1<<interfaces.indexOf(com);
			}
			else {
				dev = new Device(com, fd);
				this.openDevices.put(com, dev);
				CanListener cl = new CanListener(com);
				this.threadPool.put(com, cl);	
				postEntityCANBus(com, macAddress);
			}
		}
		// 0 no errors
		// 1 error
		// >1 multiple errors
		return error;
	}
	
	/**
	 * 
	 * @param interfaz List of interfaces to close.
	 * @return Error code.
	 */
	public long closeConnection(ArrayList<String> interfaces) {
		int error = 0;

		for (String com : interfaces) {
			if (checkConnection(com)) {
				removeThread(com);
				can.canClose(this.openDevices.get(com).getFD());
				openDevices.remove(com);
			}
			else error |= 1<<interfaces.indexOf(com);
		}
		return error;
	}
	
	/**
	 * 
	 * @return Returns all the available CAN connections in the system and the opened by the application.
	 */
	public JSONObject listConnections() {
		JSONArray availableDevices = new JSONArray();
		JSONArray openedDevices = new JSONArray();
		JSONObject content = new JSONObject();
		for (String a : this.can.listCanDevices()) {
			availableDevices.put(a);
		}
		for (String a : this.openDevices.keySet()) {
			openedDevices.put(a);
		}
		content.put("available", availableDevices);
		content.put("opened", openedDevices);
		return new JSONObject().put("devices", content);
	}

	/**
	 * 
	 * @param number The n number of last messages to read.
	 * @return Returns a map with a full information of the n last messages.
	 */
	public Map<String, Object> getMessages(int number) {
		Map<String, Object> response = new HashMap<String, Object>();
		JSONObject content = constructJSONMessage(number);
		response = JSONtoMAP.toMap(content);
    	
    	return response;
	}

	public boolean checkConnection(String interf) {
		return this.openDevices.containsKey(interf);
	}
	
	public String getLastMessage(String device) {
		String result;
		try {
			result = this.openDevices.get(device).lastFrame().getFrame();
		} catch(Exception e) {
			result = "none";
		} 
		return result;
	}
	
	public String getLastMessageTimestamp(String device) {
		String result;
		try {
			result = this.openDevices.get(device).lastFrame().getTimestamp();
		} catch(Exception e) {
			result = "none";
		} 
		return result;
	}
	
	private JSONObject constructJSONMessage(int limit) {
		List<FrameCAN> messages = new ArrayList<FrameCAN>();
		JSONObject content = new JSONObject();

		for (String key : openDevices.keySet()) {
			messages = openDevices.get(key).getFrames();
		    JSONArray array = new JSONArray();

		    for (int i = 0; i < limit && i < messages.size() ; i++) {
		    	JSONObject aux = new JSONObject();
		    	JSONObject aux_ = new JSONObject();
		    	try {
		    		aux_ = parser.parse_message(messages.get(i).getFrame(), "0");
		    	} catch (Exception e) {
		    		aux_ = formatErrorISOMessage(messages.get(i));
		    		System.err.println(aux_);
		    	}
		    	//System.out.println(messages.get(i).getFrame());
		    	if (aux_.length() != 0) {	
		    		aux.put("ISOMessage", formatISOMessage( aux_,  messages.get(i).getTimestamp()) );
		    		array.put(aux);
		    	}
		    	
		    }
		    content.put(key, array);
    	}
		return content;
	}
	
	private JSONObject formatErrorISOMessage(FrameCAN frame) {
		JSONObject n_msg = new JSONObject();
		n_msg.put("msg", frame.getFrame());
		n_msg.put("timestamp", frame.getTimestamp());
		return n_msg;
	}
	
	private JSONObject formatISOMessage(JSONObject msg, String ts) {
		JSONObject a = (JSONObject) msg.get("info");
		JSONObject n_msg = msg;
		n_msg.remove("info");

		for (String keys : a.keySet()) {
			n_msg.put(keys, a.get(keys));
		}
		n_msg.put("timestamp", ts);
		return(n_msg);
	}


	private String boxFormat(String text, int boxSize) {
		return text + " ".repeat(boxSize - text.length());
	}
	
	private void infoDisplay() {
		System.out.println(java.time.LocalDate.now() + "  " + java.time.LocalTime.now());
		System.out.format("┌%s┬%s┬%s┬%s┐%n", "-".repeat(12), "-".repeat(30), "-".repeat(30), "-".repeat(12));
		System.out.format("|%s|%s|%s|%s|%n", 
				boxFormat("DEVICE", 12), 
				boxFormat("LAST MESSAGE", 30), 
				boxFormat("AT TIME", 30), 
				boxFormat("AT THREAD", 12));
		
	
		for (String key : openDevices.keySet()) {
			System.out.format("├%s┼%s┼%s┼%s┤%n", "-".repeat(12), "-".repeat(30), "-".repeat(30), "-".repeat(12));
			System.out.format("|%s|%s|%s|%s|%n", 
					boxFormat(key, 12),
					boxFormat(""+getLastMessage(key), 30), 
					boxFormat(""+getLastMessageTimestamp(key), 30), 
					boxFormat(""+threadPool.get(key).getListenerDevice(), 12));
			
	
		}
		System.out.format("└%s┴%s┴%s┴%s┘%n%n%n", "-".repeat(12), "-".repeat(30), "-".repeat(30), "-".repeat(12));
	}

}
    