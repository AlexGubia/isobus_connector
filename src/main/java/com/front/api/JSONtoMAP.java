package com.front.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class JSONtoMAP {
	// https://stackoverflow.com/questions/21720759/convert-a-json-string-to-a-hashmap
    static SortedMap<String, Object> toMap(JSONObject object) throws JSONException {
        SortedMap<String, Object> map = new TreeMap<String, Object>();
        Iterator<String> keysItr = object.keys();
        
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }
            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            
            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }
            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }
}
