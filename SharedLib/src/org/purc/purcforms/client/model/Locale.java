package org.purc.purcforms.client.model;


/**
 * 
 * @author daniel
 *
 */
public class Locale {

	private String key;
	private String name;
	
	public Locale(String key, String name){
		this.key = key;
		this.name = name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
