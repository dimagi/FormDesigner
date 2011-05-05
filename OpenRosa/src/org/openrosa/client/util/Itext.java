package org.openrosa.client.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.openrosa.client.model.FormDef;
import org.openrosa.client.model.IFormElement;
import org.openrosa.client.model.ItextModel;
import org.openrosa.client.model.OptionDef;
import org.openrosa.client.model.QuestionDef;

import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.core.client.GWT;

/**
 * This static objects holds *all* itext data, and is the only
 * object that should be used for storage getting/setting itext values.
 * 
 * (It acts as the CONTROLLER and internally as a model for all Itext related
 * business)
 * 
 * There are two types of storage going on.  One for the internal model
 * of the itexts, the other (a ListStore) for GUI use.
 * Both are automatically updated/changed when using the methods of this object.
 * @author adewinter
 *
 */
public class Itext {
	private static ListStore<ItextModel> itextRows = new ListStore<ItextModel>();
	public static List<ItextLocale> locales = new ArrayList<ItextLocale>();
	public static ItextLocale currentLocale = null;
	
	
	/**
	 * Cleans out everything in this static Itext object and starts fresh.
	 */
	public static void reset(){
		itextRows = new ListStore<ItextModel>();
		locales = new ArrayList<ItextLocale>();
	}
	
	/**
	 * Used to determine if there is /any/ itextData
	 * @return true if any itext data is preset, false if no itext data exists.
	 */
	public static boolean hasItext(){
		for(ItextLocale locale : locales){
			if(locale.hasItext()){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Update the itextModel with the given row.
	 * Also updates the internal itext model representation
	 * @param row
	 * @param oldID - the old Question ID (used for if the ID has been changes by this update)
	 */
	public static void updateRow(ItextModel row, String oldID){
		for(ItextModel r : itextRows.getModels()){
			if(r.get("id").equals(oldID)){
				for(String k: row.getPropertyNames()){
					r.set(k, row.get(k));
				}
			}
		}
		updateModel(itextRows);
	}
	
	/**
	 * Returns a row of ONLY the itext values (for all languages) specified by
	 * the FULL id (ie, in the style of 'id;form' or 'id_hint' where id=textID of the element).
	 * This row can be used by the GUI.
	 * e.g.
	 * getItextValueRow('ID3KAB') -> ItextModel representing: ['ID3KAB','yes','ja','da'] for ID, english, afrikaans
	 * and russian translations respectively
	 * 
	 * The order of the row corresponds to the locales list (a static list located in THIS class only)
	 * @param fullID (FULL ID of the element)
	 * @return
	 */
	public static ItextModel getItextValueRow(String fullID){
		ItextModel row = new ItextModel();
		row.set("id", fullID);
		for (ItextLocale language : locales){
			String val = language.getTranslation(fullID);
			if (val != null){
				row.set(language.name, val);
			}
		}
		return row;
	}
	
	/**
	 * Adds a new translation item to the row specified by ID.  Also updates the ListStore (for GUI use)
	 * Will create new everything (row, language, ID) if they don't exist, or just update them if they do.
	 * @param language
	 * @param ID
	 * @param value
	 */
	public static void addText(String language, String ID, String value){
		ItextLocale lang = Itext.getLocale(language);
		lang.setTranslation(ID, value);
		itextRowsAddText(language,ID,value);
	}
	
	/**
	 * renames a specific Itext ID (including those with special
	 * forms)
	 * @param oldID - BASE ID ONLY. DO NOT INCLUDE SPECIAL FORM
	 * @param newID - BASE ID ONLY. DO NOT INCLUDE SPECIAL FORM
	 */
	public static void renameID(String oldID, String newID){
		for(ItextLocale locale : locales){
			locale.renameID(oldID, newID);
		}
		
		syncItextRowsToLocale();
	}
	
	/**
	 * Returns a list of special text forms used by this ID
	 * (across ALL locales).  Only returns the actual forms
	 * (i.e. "long") as opposed to the entire string (i.e. "someID;long").
	 * Will use a blank string "" for no form (i.e. there's a default Itext value).
	 * @param baseID
	 * @return
	 */
	public static List<String> getAvailableTextForms(String baseID){
		if(baseID == null || baseID.isEmpty()){ return new ArrayList<String>(); }
		List<String> forms = new ArrayList<String>();
		for(ItextLocale locale:locales){
			forms.addAll(locale.getAvailableForms(baseID));
			String defaultTrans = locale.getDefaultTranslation(baseID);
			if(defaultTrans != null && !defaultTrans.isEmpty()){
				forms.add(""); //add blank to indicate there's a default val also
			}
		}
		return forms;
	}
	
	/**
	 * Same as <code>getAvailableTextForms()</code> except
	 * the IDs are fully qualified (i.e. instead of a list of
	 * "long","short", etc you get "someID;long","someID;short", etc.
	 * @param baseID
	 * @return
	 */
	public static List<String> getFullAvailableTextForms(String baseID){
		if(baseID == null || baseID.isEmpty()){ return new ArrayList<String>(); }
		List<String> shortList = getAvailableTextForms(baseID);
		List<String> longList = new ArrayList<String>();
		for(String s:shortList){
			if(s.equals("")){
				longList.add(baseID);
			}else{
				longList.add(baseID+";"+s);
			}
		}
		return longList;
	}
	
	private static void renameIdInItextRows(String oldID, String newID){
		for(ItextModel row: getItextRows().getModels()){
			if(hasID((String)row.get("id"), oldID)){
				String id = row.remove("id");
				id = id.replace(oldID, newID);
				row.set("id",id);
			}
		}
	}
	
	static boolean hasID(String key, String testVal){
		boolean hasID = key.contains(testVal + ";") ||
						key.equals(testVal);
		
		return hasID;
	}
	
	private static void itextRowsAddText(String language, String ID, String value){
		ItextModel row = itextRows.findModel("id", ID); //gets the first one that matches, but there *should* only ever be one if coder abides by contract of this method
		if(row == null){
			row = new ItextModel();
			row.set("id", ID);
			itextRows.add(row);
		}
		row.set(language, value);
	}
	
	/**
	 * Removes a row specified by ID (full ID with text form if it exists!)
	 * Updated the internal model as well as the GUI ListStore
	 * @param id
	 * @return True if row was successfully removed, False if row does not exist
	 */
	public static boolean removeRow(String id){
		ItextModel row = itextRows.findModel("id", id);
		
		if(row != null){
			itextRows.remove(row);
			
			//find entry in each locale corresponding to this id
			//and remove it
			for(ItextLocale locale : locales){
				if(locale.hasID(id)){
					locale.removeTranslation(id);
				}
			}
			
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Returns the locale specified by name.
	 * WARNING: If the locale does not exist, a new one will be created
	 * @param name
	 * @return
	 */
	public static ItextLocale getLocale(String name){
		ItextLocale locale = getLocaleNoAdd(name);
		return (locale == null) ? addLocale(name) : locale;
	}
	
	/**
	 * Does the same thing as getLocale(String name)
	 * but without automatically adding a locale if it
	 * doesn't exist.
	 * @param name
	 * @return
	 */
	private static ItextLocale getLocaleNoAdd(String name){
		for(ItextLocale language : locales){
			if(language == null){ return null; }
			if(language.getName().equals(name))
				return language;
		}
		return null;
	}
	
	/**
	 * Removes the specified locale (updating both the internal model and the ListStore).
	 * 
	 * If locale does not exist, this method does nothing.
	 * @param name - The Locale name
	 * @return
	 */
	public static void removeLocale(String name){
		//First clear locale from locales list
		ItextLocale lang = null;
		for(ItextLocale language : locales){
			if(language.getName().equals(name))
				lang = language;
		}
		if(lang != null){
			locales.remove(lang);
		}
		
		//loop through all itextrows and remove specified language key-value pair.
		for(ItextModel row: itextRows.getModels()){
			row.remove(name);
		}
		
	}
	
	/**
	 * Renames a locale.
	 * @param name
	 * @return True if locale exists (and rename succesful), else returns false (locale does not exist)
	 */
	public static boolean renameLocale(String oldName, String newName){
		ItextLocale lang = null;
		for(ItextLocale language : locales){
			if(language.getName().equals(oldName))
				lang = language;
		}
		
		if(lang != null){
			lang.setName(newName);
		}else{
			return false; //something is wrong.
		}
		
		//loop through all itextrows and remove specified language key-value pair.
		for(ItextModel row: itextRows.getModels()){
			row.set(newName, row.remove(oldName));
		}
		
		return (lang != null);
		
	}
	
	/**
	 * Add a new locale to the internal model (as well as the ListStore (for the GUI)) and
	 * return it
	 * Does nothing if the locale already exists!
	 * @param name - The name of the new language to be added.
	 * @return
	 */
	public static ItextLocale addLocale(String name){
		if(!localeExists(name)){
			ItextLocale language = new ItextLocale(name);
			locales.add(language);	
		
			//update each row to have a new key-value pair for the language. Init with null value
			//assumes ItextRows and Locales are in sync, otherwise we're wiping out existing data.
			for(ItextModel row: itextRows.getModels()){ 
				boolean languageAlreadyInRow = row.getPropertyNames().contains(name);
				if(!languageAlreadyInRow)
					row.set(name, null);
			}
			return language;
		}else{
			return Itext.getLocaleNoAdd(name); //should always return a locale (as opposed to null)
		}
	}
	
	/**
	 * Checks to see if the locale by the given name already exists in the model.
	 * (CASE INSENSITIVE)
	 * @param name
	 * @return true if exists, false if not
	 */
	private static boolean localeExists(String name){
		for(ItextLocale locale: locales){
			if(locale.getName().toLowerCase().equals(name)) return true; //already exists so do nothing.
		}
		return false;
	}
	
	/**
	 * THIS METHOD REMOVES ALL LOCALES and ITEXTROWS
	 */
	public static void clearLocales(){
		locales = new ArrayList<ItextLocale>();
		itextRows = new ListStore<ItextModel>();
	}
	
	
	
	
	private static void setItextRows(ListStore<ItextModel> itextRows){
		Itext.itextRows = itextRows;
	}
	
	/**
	 * @return the structure of the ListStore<ItextModel> for use in the GUI
	 */
	public static ListStore<ItextModel> getItextRows(){
		syncItextRowsToLocale();
		return itextRows;
	}
	
	
	/**
	 * Sets the default locale to the one specified
	 * @param localeName
	 */
	public static void setDefaultLocale(String localeName){
		// Loop through all locales and mark them as NOT default
		for (ItextLocale locale : locales){
			locale.setDefault(false);
		}
		
		// then mark only the one specified as default...
		getLocale(localeName).setDefault(true);
		// ...to ensure that we only ever have one default locale
	}
	
	
	public static ItextLocale getDefaultLocale(){
		ItextLocale defLocale = null;
		if(locales.size() == 0){
			getLocale("en");
		}
		for (ItextLocale locale : locales){
			if(locale.isDefault()) defLocale = locale;
		}
		
		//if no default locale is found set the first one as default and continue
		if(defLocale==null){
			defLocale = locales.get(0);
			setDefaultLocale(defLocale.name);
		}
		
		return defLocale;
	}
	
	
	/**
	 * Takes in a liststore of itext rows
	 * and updates the internal model + internal row store (in the
	 * event that the one passed and the one stored here are different)
	 * @param rows
	 */
	public static void updateModel(ListStore<ItextModel> rows){
		//for the ListStore
		//actually we'll just switch the pointer to point to this new ListStore, it's
		//computationally less expensive and achieves the same goal
		itextRows = rows;
		itextRows.commitChanges();
		for (ItextModel row : itextRows.getModels()){
		//first go by row	
			
			String id = (String)row.get("id");
			if(id == null){
				itextRows.remove(row);
				GWT.log("Removing row from Itextrows as it does not have an ID(?)");
				continue; //Don't really want to store something against a null key
			}
			for (ItextLocale locale : locales){
			//then by column
				locale.setTranslation(id,(String)row.get(locale.name));
			}
		}
	}
	
	/**
	 * Causes the ItextRows to have the same data as 
	 * that stored in the Locales list.
	 */
	private static void syncItextRowsToLocale(){
		setItextRows(new ListStore<ItextModel>());
		for(ItextLocale locale : locales){
			for(String id: locale.getAllFULLIds()){
				itextRowsAddText(locale.getName(),id,locale.getTranslation(id));
			}
		}
	}
	
	/**
	 * Set's the itext's idea of what is the 'current' locale to the one specified.
	 * NB: If the locale does not exist in the internal store already it will be automatically added!
	 * @param locale
	 */
	public static void setCurrentLocale(ItextLocale locale){
		for(ItextLocale pLocale: locales){
			if(pLocale.name.equals(locale.name)){
				currentLocale = locale;
				return;
			}
		}
		
		//if we're here, then the locale doesn't exist in the Locales List, so add it
		addLocale(locale);
		setCurrentLocale(locale);  //recursive call 
		
	}
	
	
	/** 
	 * Adds the given locale to the internal itext model.
	 * NB: if a locale by that name already exists in the model it will be overwritten!
	 * 
	 * Updates the itextRows as well.
	 * @param locale
	 */
	public static void addLocale(ItextLocale locale){
		int oldLocaleIndex = -1;
		for(int i=0;i<locales.size();i++){
			if(locales.get(i).name.toLowerCase().equals(locale.name.toLowerCase())){
				oldLocaleIndex = i;
			}
		}
		
		//There's already a locale by this name, so replace it
		if(oldLocaleIndex != -1){
			locales.remove(oldLocaleIndex);
			locales.add(oldLocaleIndex, locale);
		}else{
			locales.add(locale);
		}
	}
	
	public static String getDisplayText(IFormElement elementDef){
		String displayText = getDefaultIText(elementDef.getItextId());
		if(displayText != null && !displayText.isEmpty()){
			return displayText;
		}else{
			displayText = elementDef.getDisplayText();
			if((displayText == null || displayText.isEmpty()) && !elementDef.hasUINode()){
				return "DATA NODE";
			}
			return displayText;
		}
	}
	
	private static String getDefaultIText(String ID){
		ItextLocale defLocale = Itext.getDefaultLocale();
		String dText = "";
		if(defLocale != null){
			dText = defLocale.getLongTranslation(ID);

			if(dText == null || dText.isEmpty()){
				dText = defLocale.getShortTranslation(ID);
			}
			if(dText == null || dText.isEmpty()){
				dText = defLocale.getDefaultTranslation(ID);
			}
			if(dText == null || dText.isEmpty()){
				//give up
				dText = "";
			}
			
		}
		
		return dText;
	}
	
}

