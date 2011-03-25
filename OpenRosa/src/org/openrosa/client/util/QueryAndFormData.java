package org.openrosa.client.util;

import java.util.HashMap;

import com.google.gwt.http.client.URL;

public class QueryAndFormData {

	public static String buildQueryString(HashMap<String, String> queryEntries) {
		StringBuffer sb = new StringBuffer();
		int i=0;
		for( String key: queryEntries.keySet()){
			String value = queryEntries.get(key);

			if (i > 0) {
				sb.append("&");
			}

			// encode the characters in the name
			String encodedName = URL.encodeComponent(key);
			sb.append(encodedName);

			sb.append("=");

			// encode the characters in the value
			String encodedValue = URL.encodeComponent(value);
			sb.append(encodedValue);
			i++;
		}

		return sb.toString();
	}
	
}