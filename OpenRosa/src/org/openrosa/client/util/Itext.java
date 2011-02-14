package org.openrosa.client.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openrosa.client.model.ItextModel;

import com.extjs.gxt.ui.client.store.ListStore;

/**
 * Rename me to parseItext
 * @author adewinter
 *
 */
public class Itext {
	private static ListStore<ItextModel> itextRows;
	public static List<ItextLocale> locales;
	
	public Itext(){
		itextRows = new ListStore<ItextModel>();
		locales = new ArrayList<ItextLocale>();
	}
	
	/**
	 * Returns a row of ONLY the itext values (for all languages) specified by
	 * the FULL id (ie, in the style of 'id;form' or 'id_hint' where id=textID of the element)
	 * e.g.
	 * getItextValues('ID3KAB') -> ItextModel representing: ['ID3KAB','yes','ja','da'] for ID, english, afrikaans
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
	 * Adds a new translation item.  Also updates the ListStore (for GUI use)
	 * @param language
	 * @param ID
	 * @param value
	 */
	public static void addText(String language, String ID, String value){
		ItextLocale lang = Itext.getLocale(language);
		lang.setTranslation(ID, null, value); //since we don't know what the form is, we'll pretend it's a full ID and move on
		
		updateListStoreFromModel();
	}
	
	/**
	 * Returns the locale specified by name.
	 * WARNING: If the locale does not exist, a new one will be created
	 * @param name
	 * @return
	 */
	public static ItextLocale getLocale(String name){
		for(ItextLocale language : locales){
			if(language.getName().equals(name))
				return language;
		}
		
		//If we get here, then obviously then language doesn't exist, so create it
		ItextLocale language = new ItextLocale(name);
		locales.add(language);
		updateListStoreFromModel(); //The new locale should show up in the gui as well
		return language;
	}
	
	/**
	 * THIS METHOD REMOVES ALL LOCALES (where all the language data is stored)
	 */
	public static void clearLocales(){
		locales = new ArrayList<ItextLocale>();
		updateListStoreFromModel();
	}
	

	
	private static void updateListStoreFromModel(){
		//TODO WRITE THE UPDATE CODE!
		
	}
	
	
	/**
	 * Usually used in conjunction with setListStore (when the itext has been updated through the GUI the model
	 * needs to be updated too)
	 */
	private static void updateModelFromListStore(){
		//TODO WRITE THE UPDATE CODE!
	}
	
	public static void setItextRows(ListStore<ItextModel> itextRows){
		Itext.itextRows = itextRows;
		updateModelFromListStore();
	}
	
	/**
	 * @return the structure of the ListStore<ItextModel> for use in the GUI
	 */
	public static ListStore<ItextModel> getItextRows(){
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
		
		// then mark only the one specified as default
		getLocale(localeName).setDefault(true);
		
		// to ensure that we only ever have one default locale
	}
}
