package com.back.isobus;

import java.util.ArrayList;

/**
 * This class defines a datatype for a SPN group formed by opcodes.
 */
public class SPN_data {
	ArrayList<SPN> spns_list;
	String key_list;

	public SPN_data() {
		this.spns_list = new ArrayList<SPN>();
		this.key_list = new String();
	}
}
