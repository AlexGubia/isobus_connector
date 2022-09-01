package com.front.api;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;

import com.elliotcloud.isobus.ResourceUtils;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class CSVFormatter {
	
	//Used in production
	//final static String MAIN_CSV_FILE = new File("./").getAbsolutePath().replace(".", "") + "isobus_api_lib/decoding_definitions.csv";
	
	//Used for testing
	//final static String MAIN_CSV_FILE = "/home/alex/message_definitions_ordenado.csv";

	File MAIN_CSV_FILE = ResourceUtils.getResourceAsFile("decoding_definitions.csv");
    
	
	public boolean existsInCSV(String user_input) {
		CSVReader reader = null;
		String [] nextLine;
		
		try {
			String spn_id = null;
			reader = new CSVReader( new FileReader(MAIN_CSV_FILE) );
			nextLine = reader.readNext();
			
			while ( (nextLine = reader.readNext() ) != null ) {
				spn_id = String.join(".", nextLine[0], nextLine[3], nextLine[5], nextLine[2]);
				if (spn_id.equals(user_input)) {
					reader.close();
					return true;
				}
			}
			reader.close();
			return false;
		}
		catch (Exception e) {
			System.err.println(e);
			return true;
		}			
	}
	
	public Map<String, Object> listExisting() {
		CSVReader reader = null;
		String [] nextLine;
    	JSONArray rows = new JSONArray();
		
		try {
			reader = new CSVReader( new FileReader(MAIN_CSV_FILE) );
			nextLine = reader.readNext();
			
			while ( (nextLine = reader.readNext() ) != null ) {
				JSONObject row = new JSONObject();
				row.put("pgn_id", nextLine[0]);
				row.put("manufacturer", nextLine[1]);
				row.put("opcode", nextLine[2]);
				row.put("source_address", nextLine[3]);
				row.put("pgn_length_bytes", nextLine[4]);
				row.put("spn_start_position", nextLine[5]);
				row.put("spn_bit_length", nextLine[6]);
				row.put("spn_name", nextLine[8]);
				row.put("spn_description", nextLine[9]);
				row.put("scale_factor", nextLine[10]);
				row.put("offset", nextLine[11]);
				row.put("units", nextLine[12]);
				rows.put(row);
				System.out.println(row);
			}
			
			reader.close();
			return JSONtoMAP.toMap(new JSONObject().put("existingElements", rows));
		}
		catch (Exception e) {
			System.err.println(e);
			return null;
		}
	}
		
	public String addItem(String [] data) {
		Integer index = null;
		List<String[]> rows;
		
		try {
			CSVReader reader = new CSVReader(new FileReader(MAIN_CSV_FILE));
			rows = reader.readAll();
			reader.close();
			
			for (String[] row : rows) {
				if (row[0].equals(data[0]) ) {  		//First, we check if the PGN exists
					index = rows.indexOf(row);
					if (row[3].equals(data[3])) { 		//If it does, we search for a source adress group and end the iteration
						index = rows.indexOf(row);
					}
				}										//In case we didn't find a group, we will use the last index
			}
			
			if (index != null) {
				rows.add(index+1, data);
				CSVWriter writer = new CSVWriter(new FileWriter(MAIN_CSV_FILE));
				writer.writeAll(rows);
				writer.flush();
				writer.close();
				return "pgn group already exists, adding item to it";
			}
			else {
				FileWriter writer = new FileWriter(MAIN_CSV_FILE, true);
				writer.append(convertToCSV(data));
				writer.flush();
				writer.close();
				return "new pgn, appending item";
			}
		}
		catch (Exception e) {
			System.err.println("(add) Error in CSV read/write operation: " + e);
			return "error";
		}
		
	}
	
	public String deleteItem(String [] data) {
		List<String[]> rows;
		boolean found = false;
		
		try {
			CSVReader reader = new CSVReader(new FileReader(MAIN_CSV_FILE));
			rows = reader.readAll();
			reader.close();
			
			for (String[] row : rows) {
				if (data.length == 4) {
					if (row[0].equals(data[0]) && row[3].equals(data[1]) && row[5].equals(data[2]) && row[2].equals(data[3])) {
						rows.remove(row);
						found = true;
						break;
					} 
				}
				else if ( row[0].equals(data[0]) && row[3].equals(data[1]) && row[5].equals(data[2]) ) { 		
					rows.remove(row);
					found = true;
					break;
				}
			}
			
			if (found) {
				CSVWriter writer = new CSVWriter(new FileWriter(MAIN_CSV_FILE));
				writer.writeAll(rows);
				writer.flush();
				writer.close();
				return "item removed successfully";
			}
			return "item not found";
		}
		catch (Exception e) {
			System.err.println("(del) Error in CSV read/write operation: " + e);
			return "error";
		}
			
	}
	
	public String convertToCSV(String[] data) {
	    return Stream.of(data)
	    		.map(this::escapeSpecialCharacters)
	    		.collect(Collectors.joining(","));
	}
	
	public String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
        } 
        escapedData = "\"" + data + "\"";
        return escapedData;
    }
	
}
