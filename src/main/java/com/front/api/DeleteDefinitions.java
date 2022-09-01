package com.front.api;

import io.swagger.annotations.ApiModelProperty;

public class DeleteDefinitions {
	@ApiModelProperty(value = "pgn_id", example = "65352", required = true)
	private String pgn_id = null;;
	@ApiModelProperty(value = "source_address", example = "128", required = true)
	private String source_address = null;
	@ApiModelProperty(value = "spn_start_position", example = "6.5", required = true)
	private String spn_start_position = null;
	@ApiModelProperty(value = "opcode", example = "1")
	private String opcode = null;
	
	public String getPgn_id() {
		return pgn_id;
	}

	public String getSource_address() {
		return source_address;
	}

	public String getSpn_start_position() {
		return spn_start_position;
	}

	public String getOpcode() {
		return opcode;
	}

	boolean requestedParameters() {
		return !(this.pgn_id == null || this.source_address == null || this.spn_start_position == null);
	}
	
	String getSPNid() {
		return String.join(".", pgn_id, source_address, spn_start_position, opcode);
	}
	
	String [] getData() {
		return (opcode == null) ? new String[]{pgn_id, source_address, spn_start_position} :
									new String[]{pgn_id, source_address, spn_start_position, opcode};
	}
}
