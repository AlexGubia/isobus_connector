package com.back.isobus;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Class for handling JSONObjects and JSONArrays
 */
public class JSONObjectIterator {

    private Map<String, Object> keyValuePairs;

    /**
     * Class constructor. Instantiates a new map to store the keys and values.
     */
    public JSONObjectIterator() {
        keyValuePairs = new HashMap<>();
    }
    
    /**
     * Inserts the value "value" into the parameter "key"
     * @param key Key parameter.
     * @param value Value to associate to the key.
     */
    public void handleValue(String key, Object value) {
        if (value instanceof JSONArray) {
            handleJSONArray(key, (JSONArray) value);
        }
        else if (value instanceof JSONObject) {
            handleJSONObject((JSONObject) value);
        }
        keyValuePairs.put(key, value);
    }

    /**
     * Inserts the JSONObject into a map.
     * @param jsonObject JSONObject to iterate and insert into the map.
     */
    public void handleJSONObject(JSONObject jsonObject) {
        Iterator<String> jsonObjectIterator = jsonObject.keys();
        jsonObjectIterator.forEachRemaining(key -> {
            Object value = jsonObject.get(key);
            handleValue(key, value);
        });
    }

    /**
     * Inserts a JSONArray into the value of the specified key inside a map.
     * @param key Key value.
     * @param jsonArray Array to insert into the value field associated to the key.
     */
    public void handleJSONArray(String key, JSONArray jsonArray) {
        Iterator<Object> jsonArrayIterator = jsonArray.iterator();
        jsonArrayIterator.forEachRemaining(element -> {
            handleValue(key, element);
        });
    }

    /**
     * Getter of the keys and values.
     * @return Retrieves the information of the "keyValuePairs" map.
     */
    public Map<String, Object> getKeyValuePairs() {
        return keyValuePairs;
    }

    /**
     * Setter of the keyValuePairs map 
     * @param keyValuePairs Inserts into "keyValuePairs" the keys and values specified.
     */
    public void setKeyValuePairs(Map<String, Object> keyValuePairs) {
        this.keyValuePairs = keyValuePairs;
    }

}