package com.back.isobus;

/**
 * This class contains all the information of an SPN and converts the data received to the real value.
 */
public class SPN {
	String pgn;
    String spn_name;
    String description;
    int pos;
    int nbits;
    float scale;
    int offset;
    boolean signed;
    String units;
    int byte_offs;
    int bit_offs;
    
    /**
     * SPN constructor with numeric parameters.
     * @param pgn Parameter group number.
     * @param spn_name Name of the SPN.
     * @param description Description of the SPN.
     * @param position First bit position of the SPN.
     * @param num_bits Number of bits of the SPN.
     * @param scale Scale factor of the value associated.
     * @param offset Offset of the value associated.
     * @param signed Defines whether the value is signed or unsigned.
     * @param units Units of the value.
     */
    public SPN (String pgn, String spn_name, String description, float position, int num_bits, float scale, int offset, boolean signed, String units) {
        this.pgn = pgn;
        this.spn_name = spn_name;
        this.description = description;
        this.nbits = num_bits;
        this.scale = scale;
        this.offset = offset;
        this.signed = signed;
        this.units = units;

        // 8.2 -> 8 - 1 = 7 |  Starts at the 8º byte, i.e. byte 7
        byte_offs = (int)position - 1;
        // 8.2 -> (8.2 * 10) % 10 - 1 = 1 | Starts at the 2º bit, i.e. bit 1
        bit_offs = Math.max(0, (int)((position * 10) % 10 - 1) ); 
        this.pos = byte_offs * 8 + bit_offs;
    }
    
    /**
     * SPN constructor with string parameters.
     * @param pgn Parameter group number.
     * @param spn_name Name of the SPN.
     * @param description Description of the SPN.
     * @param position First bit position of the SPN.
     * @param num_bits Number of bits of the SPN.
     * @param scale Scale factor of the value associated.
     * @param offset Offset of the value associated.
     * @param signed Defines whether the value is signed or unsigned.
     * @param units Units of the value.
     */
    public SPN (String pgn, String spn_name, String description, String position, String num_bits, String scale, String offset, boolean signed, String units) {
        this.pgn = pgn;
        this.spn_name = spn_name;
        this.description = description;
        this.nbits = Integer.parseInt(num_bits);
        
        try {
        	this.scale = Float.parseFloat(scale);
        }
        catch (Exception e){
        	this.scale = 1f;
        }
        
        try {
        	this.offset = Integer.parseInt(offset);
        }
        catch (Exception e){
        	this.offset = 0;
        }
        
        this.signed = signed;
        this.units = units;
        float f = Float.parseFloat(position);  
        // 8.2 -> 8 - 1 = 7 |  Starts at the 8º byte, i.e. byte 7
        byte_offs = (int)f - 1;
        // 8.2 -> (8.2 * 10) % 10 - 1 = 1 | Starts at the 2º bit, i.e. bit 1
        bit_offs = Math.max(0, (int)((f * 10) % 10 - 1) ); 
        this.pos = byte_offs * 8 + bit_offs;
    }

    /**
     * Parses the value from the frame to the real data applying the scale and offset.
     * @param payload_int Integer form of the frame.
     * @return Returns the real value of the treated variable.
     */
    public float parse_from_int(long payload_int) {
        long x = payload_int >> this.pos;
        x &= (1L << this.nbits) - 1;
        if ( (this.signed) && ( x&(1L<<(this.nbits-1)) ) != 0) {
            x -= 1L << this.nbits;
        }
        return x * this.scale + this.offset;
    }
    
    /**
     * Prints the parameters associated to the specific SPN.
     */
    public void pp() {
    	System.out.println("pgn " + this.pgn);
    	System.out.println("spn_name " + this.spn_name);
    	System.out.println("description + " + this.description);
    	System.out.println("num_bits " + this.nbits);
    	System.out.println("scale " + this.scale);
    	System.out.println("offset " + this.offset);
    	System.out.println("signed " + this.signed);
    	System.out.println("units " + this.units);
    }
}
