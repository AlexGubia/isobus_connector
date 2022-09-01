package com.back.isobus;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.opencsv.CSVReader;

/**
 *  The <code>Parser</code> class is the main class used to parse messages from J1939-71 to a readable high level information. To achieve this, it uses .csv file that 
 *  contains all the parameters needed. 
 */
public class Parser {

    File csv_file = ResourceUtils.getResourceAsFile("decoding_definitions.csv"); 
    

	CSVReader reader = null;
	String [] nextLine;
	
	String [] identifier = new String [4];			
	String [] identifier_aux = new String [4];	

	String opcode;
	boolean primer_ciclo = true;
	
	Map<String, PGN> pgn_src_to_parser = new HashMap<String, PGN>();
	
	/**
	 * Constructor of the class. It reads all the .csv file and creates a JSONObject with a list of different SPN under the same PGN, manufacturer, pgn_length_bytes and src_address.
	 */
	public Parser() {
		try {
			JSONObject json_parser = new JSONObject();
			ArrayList<JSONObject> json_composer = new ArrayList<JSONObject>();
			
			reader = new CSVReader(new FileReader(csv_file));
			nextLine = reader.readNext();

			while ((nextLine = reader.readNext()) != null ) {
				JSONObject mini_json_parser = new JSONObject();
				identifier[0] = nextLine[0];			//pgn_id
				identifier[1] = nextLine[1];			//manufacturer
				identifier[2] = nextLine[4];			//pgn_lenght_bytes
				identifier[3] = nextLine[3];			//src_address
				
				if (compares(identifier, identifier_aux) || primer_ciclo) {	
					primer_ciclo = false;
					mini_json_parser.put("opcode", nextLine[2]);
					mini_json_parser.put("spn_name", nextLine[8].replace(" ", "").replace("/", "-"));
					mini_json_parser.put("spn_description", nextLine[9]);
					mini_json_parser.put("spn_start_position", nextLine[5]);
					mini_json_parser.put("spn_bit_length", nextLine[6]);
					mini_json_parser.put("scale_factor", nextLine[10]);
					mini_json_parser.put("offset", nextLine[11]);
					mini_json_parser.put("units", nextLine[12]);
					json_composer.add(mini_json_parser);
					mini_json_parser = null;
				}
				else {
					json_parser.put(identifier_aux[0]+"-"+identifier_aux[1]+"-"+identifier_aux[2]+"-"+identifier_aux[3], json_composer);
					json_composer.clear();
					mini_json_parser.put("opcode", nextLine[2]);
					mini_json_parser.put("spn_name", nextLine[8].replace(" ", "").replace("/", "-"));
					mini_json_parser.put("spn_description", nextLine[9]);
					mini_json_parser.put("spn_start_position", nextLine[5]);
					mini_json_parser.put("spn_bit_length", nextLine[6]);
					mini_json_parser.put("scale_factor", nextLine[10]);
					mini_json_parser.put("offset", nextLine[11]);
					mini_json_parser.put("units", nextLine[12]);
					json_composer.add(mini_json_parser);
					mini_json_parser = null;
				}
				
				identifier_aux[0] = nextLine[0];			//pgn_id
				identifier_aux[1] = nextLine[1];			//manufacturer
				identifier_aux[2] = nextLine[4];			//pgn_lenght_bytes
				identifier_aux[3] = nextLine[3];			//src_address
			}
			json_parser.put(identifier_aux[0]+"-"+identifier_aux[1]+"-"+identifier_aux[2]+"-"+identifier_aux[3], json_composer);
			
			JSONArray key = json_parser.names();
			
			for (int i = 0; i < key.length(); ++i) {		// Iterate over dictionaries of "65330-Case IH-8-0" (with list inside after it)

			   String keys = key.getString(i); 
			   String[] sentences = keys.split("\\-");
			   String pgn = sentences[0];
			   String mfr = sentences[1];							// Manufacturer
			   String num_bytes = sentences[2];
			   String src = sentences[3];
			   JSONArray val = json_parser.getJSONArray(keys);    	// Each of the arrays comes from here (disordered)
			   SPN opcode_parser = null;
			   SPN_data SPN_objects = new SPN_data();
			   ArrayList<SPN_data> opcode_to_spns = new ArrayList<SPN_data>();
			   
			   for (int j = 0; j < val.length(); ++j) {				// Iterate the JSONArray inside "65330-Case IH-8-0" (inside are the JSONObjects that include each element)
				   JSONObject asd = val.getJSONObject(j);			// The JSON of the arrays come from here (disordered)

				   SPN spn_decoder = new SPN(pgn, 
						   asd.getString("spn_name"), 
						   asd.getString("spn_description"), 
						   asd.getString("spn_start_position"), 
						   asd.getString("spn_bit_length"), 
						   asd.getString("scale_factor"), 
						   asd.getString("offset"), 
						   false,
						   asd.getString("units"));
				   
				   if ((asd.getString("spn_name").toLowerCase()).equals("pgn usage opcode")) {
	                    opcode_parser = spn_decoder;
				   }
				   else {
	                    opcode = asd.getString("opcode");
	                    if (opcode.equals("")) {
	                    	opcode = "null";
	                    }
	                    SPN_objects.spns_list.add(spn_decoder);
	                    SPN_objects.key_list = opcode;
	               }
			   }
			   opcode_to_spns.add(SPN_objects);
			   
			   for (@SuppressWarnings("unused") SPN_data iterador : opcode_to_spns) {   
				   try {
					   PGN pgn_object;
					   if (opcode_parser == null) {
						   pgn_object = new PGN(pgn, num_bytes, src, mfr, opcode_to_spns);
					   }
					   else {
						   pgn_object = new PGN(pgn, num_bytes, src, mfr, opcode_to_spns, opcode_parser);
 					   }
					   this.pgn_src_to_parser.put(pgn+"-"+src, pgn_object);
				   }
				   catch (Exception e) {
					   e.printStackTrace();
				   }
			   }
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Parses the message specified into its fundamental parameters.
	 * @param hex_str Hexadecimal frame to parse.
	 * @param timestamp Timestamp.
	 * @return Returns an error if the message could not be parsed or a JSONObject containing all the information about the frame.
	 */
	public JSONObject parse_message(String hex_str, String timestamp) throws Exception{
		JSONObject parsed_content = new JSONObject();
		Map<String, String> info = new HashMap<String, String>(); ;
		
		try {
			info = Decoder.deco_info(hex_str);
		}
		catch (Exception e) {
			System.err.println("Could not parse message: " + hex_str);
			return parsed_content;
		}
		String key = info.get("pgn")+"-"+info.get("source");
		
		try {
			info.put("manufacturer", this.pgn_src_to_parser.get(key).mfr);    //Adding manufacturer to info dict
		}
		catch (Exception e) {
			info.put("manufacturer", "");
		}
		
		try {
			parsed_content = this.pgn_src_to_parser.get(key).parse_from_info_dict(info);
		}
		catch (Exception e) {
			System.err.println("Error while parsing message: " + hex_str);
		}
		
		return parsed_content;
	}

	/**
	 * Compares two string arrays.
	 * @param m_element First string to compare.
	 * @param n_element Second string to compare.
	 * @return Returns false if they are different and true if they are equal
	 */
	public boolean compares(String [] m_element, String [] n_element) {
		for (int i = 0; i < 4; i++) {
			if (!m_element[i].equals(n_element[i])) return false;
		}
		return true;
	}
}
