package com.back.isobus;

import java.util.Map;
import java.util.ArrayList;
import org.json.*;

public class PGN {
    int pgn;
    int nbytes;
    int source_address;
    String mfr;
    ArrayList<SPN_data> opcode_to_spns;
    SPN opcode_parser;
    

    /**
     * PGN constructor.
     * @param pgn Parameter group number.
     * @param num_bytes Number of bytes.
     * @param source_address Source address.
     * @param manufacturer Manufacturer.
     * @param opcode_to_spns Group SPN decoders by opcode.
     * @param opcode_parser Iterates opcodes inside opcode_to_spns ArrayList.
     */
    
    public PGN(String pgn, String num_bytes, String source_address, String manufacturer, ArrayList<SPN_data> opcode_to_spns, SPN opcode_parser) {
        this.pgn = Integer.parseInt(pgn);
        this.nbytes = Integer.parseInt(num_bytes);
        this.source_address = Integer.parseInt(source_address);
        this.mfr = manufacturer;
        this.opcode_to_spns = opcode_to_spns;
        this.opcode_parser = opcode_parser;
    }
    
    
    /**
     * PGN constructor without opcode_parser.
     * @param pgn Parameter group number.
     * @param num_bytes Number of bytes.
     * @param source_address Source address.
     * @param manufacturer Manufacturer.
     * @param opcode_to_spns Group SPN decoders by opcode.
     */
    public PGN(String pgn, String num_bytes, String source_address, String manufacturer, ArrayList<SPN_data> opcode_to_spns) {
        this.pgn = Integer.parseInt(pgn);
        this.nbytes = Integer.parseInt(num_bytes);
        this.source_address = Integer.parseInt(source_address);
        this.mfr = manufacturer;
        this.opcode_to_spns = opcode_to_spns;
        this.opcode_parser = null;
    }
    
    /**
     * PGN constructor with opcode_parser and integer num_bytes and source_address.
     * @param pgn Parameter group number.
     * @param num_bytes Number of bytes.
     * @param source_address Source address
     * @param opcode_to_spns Group SPN decoders by opcode.
     * @param opcode_parser Iterates opcodes inside opcode_to_spns ArrayList.
     */
    public PGN(String pgn, int num_bytes, int source_address, ArrayList<SPN_data> opcode_to_spns, SPN opcode_parser) {
        this.pgn = Integer.parseInt(pgn);
        this.nbytes = num_bytes;
        this.source_address = source_address;
        this.opcode_to_spns = opcode_to_spns;
        this.opcode_parser = opcode_parser;
    }

    /**
     * Parses the specified message.
     * @param message Message to parse.
     * @return Returns the parsed message.
     */
    public JSONObject parse_message(String message) {
        Map<String, String> info = Decoder.deco_info(message);
        return this.parse_from_info_dict(info);
    }

    /**
     * Parses the map of SPNs associated to the PGN group.
     * @param info Map to be parsed.
     * @return Returns a JSONObject containing all the information about the group and SPNs.
     */
    public JSONObject parse_from_info_dict(Map<String, String> info) {
        if (!(Integer.parseInt(info.get("pgn")) == this.pgn)) {
            System.err.println("Values don't match");
        }
        
        if (!(Integer.parseInt(info.get("source")) == this.source_address)) {
            System.err.println("Values don't match");
        }
        
        String opcode = new String();
        
        if (this.opcode_parser == null) {
        	opcode = "null";
        }
        else {
        	float opcode_aux = this.opcode_parser.parse_from_int(Long.parseLong(info.get("payloadInt")));
        	opcode = Float.toString(opcode_aux);
        }
        
        JSONObject json_res = new JSONObject();
        JSONObject json_info = new JSONObject();
        JSONObject json_vals = new JSONObject();
        
        json_res.put("pgn", this.pgn);

        for (Map.Entry<String, String> el : info.entrySet()) {
        	json_info.put(el.getKey(), el.getValue().toString());
        }
        
        json_res.put("info", json_info);
        
        for (SPN_data element : opcode_to_spns) {
        	String a = element.key_list;
        	if (a.equals(opcode)) {
        		for (SPN spn : element.spns_list) {
        			json_vals.put(spn.spn_name, spn.parse_from_int(Long.parseLong(info.get("payloadInt"))));
        		}
        	}
        }
        
        json_res.put("spnValues", json_vals);
        
        return json_res;
    }
    /*
     * Response format
    {
    	'pgn': 65267, 
    	'info': {
    		'pgn': 65267, 
    		'source': 28, 
    		'priority': 6, 
    		'payloadInt': 5436458769886429757, 
    		'payload': ['3D', '42', '23', '97', '72', '2E', '72', '4B'], 
    		'header': '18FEF31C', 
    		'msg': '18FEF31C3D422397722E724B', 
    		'timestamp': 0
    	}, 
    	'spnValues': {
    		'Latitude': 43.56703329999999, 
    		'Longitude': -83.4225806
    	}
    }

    */
}
