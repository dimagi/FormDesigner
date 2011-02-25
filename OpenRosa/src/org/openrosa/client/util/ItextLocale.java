package org.openrosa.client.util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This object represents an itext locale (or language).  Stores the name of the language, plus a hashmap containing the itextID:value pairs.
 * @author adewinter
 *
 */
public class ItextLocale {
	public String name;
	public HashMap<String,String> values;
	private boolean isDefault;
	
	public ItextLocale(String name) {
		super();
		this.name = name;
		this.values = new HashMap<String, String>();
		this.isDefault = false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private HashMap<String, String> getValues() {
		return values;
	}
	
	/**
	 * Returns the available itext forms for the specified textID
	 * @param textID
	 * @return
	 */
	public ArrayList<String> getAvailableForms(String textID){
		ArrayList<String> keys = new ArrayList<String>();
		for (String key: getValues().keySet()){
			keys.add(key);
		}
		return keys;
	}
	
	/**
	 * THIS METHOD WILL DELETE ALL THE LANGUAGE DATA (KEY;VALUE)s 
	 */
	public void clearLanguageData(){
		this.values = new HashMap<String, String>();
	}
	
	/**
	 * Don't use this unless you know what you're doing.
	 * @param values
	 */
	private void setValues(HashMap<String, String> values) {
		this.values = values;
	}
	
	/**
	 * Add or modify a translation for a specific ID (full ID including form, if available)
	 * @param fullID
	 * @param value
	 */
	public void setTranslation(String fullID, String value){
		values.put(fullID, value);
	}
	
	/**
	 * Add or modify a tranlsation for an ID, a text form can be specified. If there
	 * is no additionl form present use null.
	 * @param ID
	 * @param form - can be null
	 * @param value
	 */
	private void setTranslation(String ID, String form, String value){
		if(form!=null)
			setTranslation(ID+";"+form, value);
		else
			setTranslation(ID,value);
	}
	
	public String getTranslation(String fullID){
		return values.get(fullID);
	}
	
	public boolean hasID(String fullID){
		return values.get(fullID)!=null;
	}
	
	/**
	 * Gets a translation. If no form is present, use null
	 * @param ID
	 * @param form - null allowed
	 * @return
	 */
	private String getTranslation(String ID, String form){
		return getTranslation(ID+";"+form);
	}
	
	/**
	 * Convenience method. Get the default translation value for this ID
	 * (ie. the translation with no specified form)
	 * @param ID
	 */
	public String getDefaultTranslation(String ID){
		return getTranslation(ID,null);
	}
	
	/**
	 * Convenience method. Get the 'long' translation value for this ID
	 * (ie. the translation with the specified form 'long')
	 * @param ID
	 */
	public String getLongTranslation(String ID){
		return getTranslation(ID,"long");
	}
	
	/**
	 * Convenience method. Get the 'short' translation value for this ID
	 * (ie. the translation with the specified form 'short')
	 * @param ID
	 */
	public String getShortTranslation(String ID){
		return getTranslation(ID,"short");
	}

	/**
	 * Determines if this locale has been set to be the 
	 * default locale
	 * @return
	 */
	public boolean isDefault() {
		return isDefault;
	}

	/**
	 * Set whether this locale should be the 'default'
	 * locale
	 * @param isDefault
	 */
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

}

