package com.front.api;

import io.swagger.annotations.ApiModelProperty;

//https://stackoverflow.com/questions/46584658/how-can-i-set-a-description-and-an-example-in-swagger-with-swagger-annotations
public class PostDefinitions {
	@ApiModelProperty(value = "pgn_id", example = "65352", required = true)
	private String pgn_id = null;;
	@ApiModelProperty(value = "manufacturer", example = "Case IH")
	private String manufacturer = null;
	@ApiModelProperty(value = "opcode", example = "1")
	private String opcode = null;
	@ApiModelProperty(value = "source_address", example = "128", required = true)
	private String source_address = null;
	@ApiModelProperty(value = "pgn_length_bytes", example = "8")
	private String pgn_length_bytes = null;
	@ApiModelProperty(value = "spn_start_position", example = "6.5", required = true)
	private String spn_start_position = null;
	@ApiModelProperty(value = "spn_bit_length", example = "2")
	private String spn_bit_length = null;
	@ApiModelProperty(value = "spn_name", example = "Reel Down Status", required = true)
	private String spn_name = null;
	@ApiModelProperty(value = "spn_description", example = "Status Off = 00; Status On = 01; Defect = 10; Not Available = 11")
	private String spn_description = null;
	@ApiModelProperty(value = "scale_factor", example = "1")
	private String scale_factor = null;
	@ApiModelProperty(value = "offset", example = "0")
	private String offset = null;
	@ApiModelProperty(value = "units", example = "none")
	private String units = null;

	public String getPgn_id() {
		return pgn_id;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public String getOpcode() {
		return opcode;
	}

	public String getSource_address() {
		return source_address;
	}

	public String getPgn_length_bytes() {
		return pgn_length_bytes;
	}

	public String getSpn_start_position() {
		return spn_start_position;
	}

	public String getSpn_bit_length() {
		return spn_bit_length;
	}

	public String getSpn_name() {
		return spn_name;
	}

	public String getSpn_description() {
		return spn_description;
	}

	public String getScale_factor() {
		return scale_factor;
	}

	public String getOffset() {
		return offset;
	}

	public String getUnits() {
		return units;
	}
	
	String getSPNid() {
		return String.join(".", pgn_id, source_address, spn_start_position, opcode);
	}

	boolean requestedParameters() {
		return !(this.pgn_id == null || this.source_address == null || this.spn_name == null || this.spn_start_position == null);
	}
	
	String [] getData() {
		return new String[]{pgn_id, manufacturer, opcode, source_address, pgn_length_bytes, spn_start_position, spn_bit_length, null, spn_name, spn_description, scale_factor, offset, units};
	}

}
