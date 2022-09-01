package com.front.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.elliotcloud.isobus.FrameCAN;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


// https://www.baeldung.com/swagger-apiparam-vs-apimodelproperty
// _italic_, *italic*, __bold__, **bold**, `monospace`.


@RestController
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT})
public class Configuration {
	
	CanManager clInstance = CanManager.getInstance();

	
    //**************************************** 
    //                    MESSAGE
    //****************************************     

    @ApiOperation(tags="Message", value = "Post a message from an specific device", 
    		notes = "Frame can be specified as a whole, or separated as frame id and payload. "
    		+ "If you use the full-message format do not put the other two fields.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Message sent successfully."),
            @ApiResponse(code = 400, message = "An error ocurred while sending the message.")
    })
    @PostMapping("/message/{deviceID}")
    public Map<String, Object> sendMessage(@PathVariable String deviceID, @RequestBody PostMessages body, HttpServletResponse response) { //deviceID String o int
    	JSONObject map = new JSONObject();
    	JSONObject content = new JSONObject();
    	FrameCAN fr = new FrameCAN();
    	fr.setFrame(body.getMessage());
    	
    	if (clInstance.can.canSendFrame(clInstance.getFD(deviceID), fr)) {
    		content.put("fromDevice", deviceID);
    		content.put("frame", body.getMessage());
    		content.put("sentAt", fr.getTimestamp());
    		map.put("ISOMessage", content);
    		return JSONtoMAP.toMap(map);
    	}
    	else {
    		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			content.put("error", "could not send message");
			content.put("frame", body.getMessage());
			map.put("ISOMessage", content);
			return JSONtoMAP.toMap(map);
    	}
    }
    
    @ApiOperation(tags="Message", value = "Get last message from an specific device")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Request accomplished successfully."),
            @ApiResponse(code = 400, message = "Device not found or an unexpected error ocurred.")
    })
    @GetMapping("/message/{deviceID}")
    public HashMap<String, Object> getMessage(@PathVariable String deviceID, HttpServletResponse response) {
    	HashMap<String, Object> map = new HashMap<>();
    	HashMap<String, Object> content = new HashMap<>();
    	
        if (clInstance.checkConnection(deviceID)) {
	    	content.put("fromDevice", deviceID);
	        content.put("lastMessage", clInstance.getLastMessage(deviceID));
	        content.put("receivedAt", clInstance.getLastMessageTimestamp(deviceID));
	     
	    }
    	else {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	content.clear();
        	content.put("error", "device not found"); 
        	content.put("device", deviceID); 
        }
    	map.put("ISOMessage", content);
        return map;
        
    }
    
    @ApiOperation(tags="Messages", value = " Get all the messages that are being sent over the bus")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Request accomplished successfully."),
            @ApiResponse(code = 400, message = "Could not list the received messages.")
    })
    @GetMapping("/messages/{limit}")
    public Map<String, Object> getMessages(@PathVariable String limit) { 
        return clInstance.getMessages(Integer.parseInt(limit)); 
    } 
    
    
    //****************************************  
    //                     CONFIG
    //****************************************  
    
    /** POST **/
    
    @ApiOperation(tags="Configuration", value = "Open or close devices", 
    		notes= "And array of devices may be provided to interact with multiple connections (including virtual can interfaces) such as *can0* or *vcan1*.") 
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Connection(s) opened successfully."),
            @ApiResponse(code = 400, message = "The body request is not correctly formatted or the request could not be fully accomplished.")
    })
    @PostMapping("/config/devices")  //devices, server, 
    public Map<String, Object> addDevice(@RequestBody PostConfig body, HttpServletResponse response) {
    	ArrayList<String> devices = body.getDevices();
    	JSONArray array_bad = new JSONArray();
    	JSONArray array_good = new JSONArray();
    	JSONObject content = new JSONObject();
    	long error_code = clInstance.openConnection(devices);

		for (String dev : devices) {
			// 00001101
			if ( (error_code & ( 1<< devices.indexOf(dev))) > 0) array_bad.put(dev);
			else array_good.put(dev);
		}

		if (array_bad.length() > 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			//content.put("error", "all or some connections could not be opened");
		}
    	content.put("succesOpening", array_good);
    	content.put("errorOpening", array_bad);
        return JSONtoMAP.toMap(content);
    }
    
    /** DELETE **/
    
    @ApiOperation(tags="Configuration", value = "Open or close devices", 
    		notes= "And array of devices may be provided to interact with multiple connections (including virtual can interfaces) such as *can0* or *vcan1*.") 
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Connection(s) closed successfully."),
            @ApiResponse(code = 400, message = "The body request is not correctly formatted or the keys provided are not enough.")
    })
    @DeleteMapping("/config/devices")  //devices, server, 
    public Map<String, Object> deleteDevice(@RequestBody PostConfig body, HttpServletResponse response) {
    	ArrayList<String> devices = body.getDevices();
    	JSONArray array_bad = new JSONArray();
    	JSONArray array_good = new JSONArray();
    	JSONObject content = new JSONObject();
    	long error_code = clInstance.closeConnection(body.getDevices());
		
		for (String dev : devices) {
			// 00001101
			if ( (error_code & ( 1<< devices.indexOf(dev))) > 0) array_bad.put(dev);
			else array_good.put(dev);
		}
    	
    	if (array_bad.length() > 0) {
    		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    		//content.put("error", "all or some connections could not be closed");
    	}
    	content.put("succesClosing", array_good);
    	content.put("errorClosing", array_bad);
        return JSONtoMAP.toMap(content);
    }
    
    
    //****************************************  
    //              DEFINITIONS
    //****************************************  
    
    /** POST **/
    @ApiOperation(tags="Definitions", value = "Add an item to the definitions dictionary",
    		notes = "As stated in the model, only *pgn_id*, *source_address*, *spn_start_position* and *spn_name* are mandatory except in the cases where *opcode* is also necessary.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Item added successfully."),
            @ApiResponse(code = 400, message = "The body request is not correctly formatted or the keys provided are not enough.")
    })
    @PostMapping("/config/definitions") 
    public Map<String, Object> postDefinitionsConfig(@RequestBody PostDefinitions postBody, HttpServletResponse response) {

    	JSONObject content = new JSONObject();
    	CSVFormatter csv_editor = new CSVFormatter();

		if (!postBody.requestedParameters()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			content.put("error", "necessary parameters were not provided");	
			return JSONtoMAP.toMap( new JSONObject().put("config", content) ); 
		}
		content.put("result", csv_editor.addItem(postBody.getData()) );
		content.put("item", postBody.getData());
		return JSONtoMAP.toMap( new JSONObject().put("definitions", content) );	
    }
    
    
    /** DELETE **/
    //https://stackoverflow.com/questions/299628/is-an-entity-body-allowed-for-an-http-delete-request
    @ApiOperation(tags="Definitions", 
    		value = "Delete an item from the definitions dictionary", 
    		notes="*opcode* is only necessary when it exists, if in that case it is not provided, "
    				+ "the first item that match the search of the other parameters __will be removed until "
    				+ "there are no more matches to remove__.\n"
    				+ "If *opcode* is not used, __do not include it in the body__.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Item removed successfully."),
            @ApiResponse(code = 400, message = "The body request is not correctly formatted or the keys provided are not enough.")
    })
    @DeleteMapping("/config/definitions") 
    public Map<String, Object> deleteDefinitionsConfig(@RequestBody DeleteDefinitions delBody, HttpServletResponse response) {
    	JSONObject content = new JSONObject();
    	CSVFormatter csv_editor = new CSVFormatter();
    	
    	if (!delBody.requestedParameters()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			content.put("error", "necessary parameters were not provided");	
			return JSONtoMAP.toMap( new JSONObject().put("config", content) ); 
		}
		content.put("result", csv_editor.deleteItem(delBody.getData()) );
		content.put("item", delBody.getData());
		return JSONtoMAP.toMap( new JSONObject().put("definitions", content) );
    }
    
    //TODO: 	REVISAR, ESTA MAL LO QUE DEVUELVE
    /** GET **/
    @ApiOperation(tags="Definitions", value = "Get all items from the definitions dictionary")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Request accomplished successfully."),
    })
    @GetMapping("/config/definitions")
    public Map<String, Object> getDefinitionsConfig(HttpServletResponse response) {
    	Map<String, Object> content;
    	CSVFormatter csv_editor = new CSVFormatter();
    	
    	if ( (content = csv_editor.listExisting()) == null) {
    		return JSONtoMAP.toMap(new JSONObject().put("error", "something wrong happened"));
    	}
    	return content;
    }

    
    //**************************************** 
    //                     DEVICE
    //****************************************  
    
    @ApiOperation(tags="Devices", value = "Get current devices")
    @GetMapping("/devices")
    public Map<String, Object> getDevicesID() {
    	Map<String, Object> response = new HashMap<>();
//    	Set<String> list = clInstance.listConnections();
//    	JSONArray array = new JSONArray();
//    	JSONObject content = new JSONObject();
//
//    	for (String dev : list) {
//    		array.put(dev);
//    	}
//    	content.put("devices", array);
    	response = JSONtoMAP.toMap(clInstance.listConnections());
        return response;
    
    }
}