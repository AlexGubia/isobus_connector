package com.back.isobus;

import java.util.Map;
import java.util.HashMap;


/**
 * This class receives an ISOBUS frame and decodes it into its fundamental parameters
 * such as PGN, source address, priority, payloadInt, payload, header and msg
 * it also prints it.
 */
public class Decoder {

	/**
	 * Masks the message bitwise to separate each parameter from a given frame and inserts them into a map.
	 * @param frame Frame to decode.
	 * @return Map with values for each parameter.
	 */
	public static Map<String, String> deco_info(String frame) {
		Map<String, String> map = new HashMap<String, String>();
		
        String hex_message = frame;
        int MASK_2_BIT = ((1 << 2) - 1);
        int MASK_3_BIT = ((1 << 3) - 1);
        int MASK_8_BIT = ((1 << 8) - 1);
        
        String header_hex = null;
        long header = 0;
        
        try {
			header_hex = hex_message.substring(0, 8);
			header = Long.parseLong(header_hex, 16);
        }
        catch (Exception e) {
        	System.err.println("Could not parse header");
        }

		long src = header & MASK_8_BIT;
		header >>= 8;

		long pdu_ps = header & MASK_8_BIT;
	    header >>= 8;

	    long pdu_pf = header & MASK_8_BIT;
	    header >>= 8;

	    long res_dp = header & MASK_2_BIT;
	    header >>= 2;

	    long priority = header & MASK_3_BIT;

	    int pgn = (int)res_dp;
	    pgn <<= 8;
	    pgn |= pdu_pf;
	    pgn <<= 8;

	    if (pdu_pf >= 240) {
	    	pgn |= pdu_ps;
	    }
	    String payload_hex = null;
	    try {
	    	payload_hex = hex_message.substring(8); 
	    }
	    catch (Exception e) {
	    	System.err.println("Could not extract payload");
	    }
	    
	    String n_payload_hex = "";
	    int payload_length = payload_hex.length();
	    long n_payload_int = 0;
	    try {
		    for (int i = 0; i < (payload_length); i=i+2) {
		    	n_payload_hex = payload_hex.substring(i,i+2) + n_payload_hex; 
		    }
		    n_payload_int = Long.parseUnsignedLong(n_payload_hex, 16);
	    }
	    catch (Exception e) {
	    	System.err.println("error :" + e);
	    }
        

        map.put("pgn", "" + pgn);
        map.put("source", "" + src);
        map.put("priority", "" + priority);
        map.put("payloadInt", "" + n_payload_int);
        map.put("payload", n_payload_hex);
        map.put("header", header_hex);
        map.put("msg", hex_message);
		return map;
	}
	

	/**
	 * Prints the specified map, this method is only used for debugging purpose.
	 * @param draw Map to print.
	 */
	public static void represents(Map<String, String> draw) {
        for (Map.Entry<String, String> entry : draw.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue().toString());
        }
	}
}
