package org.purc.purcforms.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.purc.purcforms.client.controller.ILocaleListChangeListener;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.Locale;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.widget.DesignWidgetWrapper;


/**
 * Contains shared information that has the notion of being current (eg currently
 * selected form, current locale, curent mode (design or preview), and more. 
 * It represents the runtime context of the form designer.
 * Contexts are associated with the current thread.
 * 
 * @author daniel
 *
 */
public class Context {
	
	/** State of the form designer being in neither preview or design mode. */
	public static final byte MODE_NONE = 0;
	
	/** State when setting questions properties in the properties tab. */
	public static final byte MODE_QUESTION_PROPERTIES = 1;
	
	/** State when the form designer is in design mode. 
	 * As in used dragging around widgets on the design surface.
	 */
	public static final byte MODE_DESIGN = 2;
	
	/** State when the user is previewing their form designs. */
	public static final byte MODE_PREVIEW = 3;
	
	/** State when displaying the xforms source. */
	public static final byte MODE_XFORMS_SOURCE = 4;
	
	/** The default locale key. */
	private static Locale defaultLocale = new Locale("en","English");
	
	/** The current locale. */
	private static Locale locale = defaultLocale;
	
	/** A list of supported locales. */
	private static List<Locale> locales = new ArrayList<Locale>();
	
	/**Determines if we should allow changing of question bindings.
	 * This is useful for cases where users are not allowed to change the question binding
	 * which affected the names of the xml model.
	 */
	private static boolean allowBindEdit = true;
	
	/** The current mode of the form designer. */
	private static byte currentMode = MODE_NONE;
	
	/** The form having focus. */
	private static FormDef formDef;
	
	/** Flag telling whether widgets and locked and hence allow no movement. */
	private static boolean lockWidgets = false;
	
	/** A list of widgets that have been cut or copied to the clipboard and ready for pasting. */
	public static List<DesignWidgetWrapper> clipBoardWidgets = new Vector<DesignWidgetWrapper>();
	
	private static boolean offlineMode = true;
	
	/** List of those interested in being notified whenever the locale list changes. */
	private static List<ILocaleListChangeListener> localeListeners = new ArrayList<ILocaleListChangeListener>();
	
	/** A mapping for form locale text. The key is the formId while the value is a map of locale 
	 * key and text, where locale key is the value map key and text is the value map value.
	 */
	private static HashMap<Integer,HashMap<String,String>> languageText = new HashMap<Integer,HashMap<String,String>>();

	
	/**
	 * Sets the default locale.
	 * 
	 * @param locale the default locale key.
	 */
	public static void setDefaultLocale(Locale locale){
		Context.defaultLocale = locale;
	}
	
	/**
	 * Gets the default locale.
	 * 
	 * @return the default locale key.
	 */
	public static Locale getDefaultLocale(){
		return defaultLocale;
	}
	
	/**
	 * Sets the current locale.
	 * 
	 * @param locale the locale.
	 */
	public static void setLocale(Locale locale){
		Context.locale = locale;
	}
	
	/**
	 * Gets the current locale.
	 * 
	 * @return the locale.
	 */
	public static Locale getLocale(){
		return locale;
	}
	
	/**
	 * Gets the form that has focus.
	 * 
	 * @return the form definition object.
	 */
	public static FormDef getFormDef() {
		return formDef;
	}

	/**
	 * Sets the form that has focus.
	 * 
	 * @param formDef the form definition object.
	 */
	public static void setFormDef(FormDef formDef) {
		Context.formDef = formDef;
	}

	/**
	 * Checks if the form designer is in text locale or language translation mode.
	 * 
	 * @return true if in localization mode, else false.
	 */
	public static boolean inLocalizationMode(){
		return !defaultLocale.getKey().equalsIgnoreCase(locale.getKey());
	}
	
	/**
	 * Gets the list of supported locales.
	 * 
	 * @return the locale list
	 */
	public static List<Locale> getLocales(){
		return locales;
	}
	
	/**
	 * Sets the list of supported locales.
	 * 
	 * @param locales the locale list.
	 */
	public static void setLocales(List<Locale> locales){
		Context.locales = locales;
		
		for(ILocaleListChangeListener listener : localeListeners)
			listener.onLocaleListChanged();
	}
	
	/**
	 * Adds a listener to locale list change event.
	 * 
	 * @param listener the listener.
	 */
	public static void addLocaleListChangeListener(ILocaleListChangeListener listener){
		localeListeners.add(listener);
	}
	
	/**
	 * Check if the current form allows changes for both structure and text.
	 * 
	 * @return true if readonly else false.
	 */
	public static boolean isReadOnly(){
		return (formDef != null && formDef.isReadOnly());
	}
	
	/**
	 * Checks whether the current form structure allows changes.
	 * 
	 * @return true if readonly else false.
	 */
	public static boolean isStructureReadOnly(){
		if((formDef != null && formDef.isReadOnly()) || Context.inLocalizationMode())
			return true;
		return false;
	}
	
	/**
	 * Checks if we should allow changing of question bindings.
	 * 
	 * @return true if yes, else false.
	 */
	public static boolean allowBindEdit(){
		return allowBindEdit;
	}
	
	/**
	 * Turns off or on question binding.
	 * 
	 * @param allowBindEdit set to true if we should allow editing bindings, else false.
	 */
	public static void setAllowBindEdit(boolean allowBindEdit){
		Context.allowBindEdit = allowBindEdit;
	}
	
	/**
	 * Gets the current form designer mode.
	 * 
	 * @return can be (MODE_DESIGN,MODE_PREVIEW,MODE_NONE)
	 */
	public static byte getCurrentMode(){
		return currentMode;
	}
	
	/**
	 * Sets the current form designer mode.
	 * 
	 * @param currentMode should be (MODE_DESIGN or MODE_PREVIEW,MODE_NONE)
	 */
	public static void setCurrentMode(byte currentMode){
		Context.currentMode = currentMode;
	}
	
	public static boolean getLockWidgets(){
		return lockWidgets;
	}
	
	public static void setLockWidgets(boolean lockWidgets){
		Context.lockWidgets = lockWidgets;
	}
	
	public static boolean isOfflineMode(){
		return offlineMode;
	}
	
	public static void setOfflineModeStatus(){
		try{
			String formId = FormUtil.getFormId();
			if(formId != null && Integer.parseInt(formId) >= 0)
				offlineMode = false;
		}
		catch(Exception ex){}
	}
	
	public static HashMap<Integer,HashMap<String,String>> getLanguageText(){
		return languageText;
	}
}
