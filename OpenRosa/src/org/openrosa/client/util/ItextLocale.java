package org.openrosa.client.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
			if(key.contains(";")){
				if(!key.split(";")[0].equals(textID))continue;
				keys.add(key.split(";")[1]);
			}
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
	public String getTranslation(String ID, String form){
		if(form == null){
			return getTranslation(ID);
		}
		return getTranslation(ID+";"+form);
	}
	
	/**
	 * Convenience method. Get the default translation value for this ID
	 * (ie. the translation with no specified form)
	 * @param ID
	 */
	public String getDefaultTranslation(String ID){
		return getTranslation(ID);
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
	
	/**
	 * Returns a list of all the unique ItextIDs contained in this locale (sans special TextForms)
	 * @return
	 */
	public HashSet<String> getAvailableItextIDs(){
		HashSet<String> keys = new HashSet<String>();
		for(String k : values.keySet()){
			if(k.contains(";")){
				keys.add(k.split(";")[0]);
			}else{
				keys.add(k);
			}
		}
		
		return keys;
	}
	
	public Set<String> getAllFULLIds(){
		return values.keySet();
	}

}

