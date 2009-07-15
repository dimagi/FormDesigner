package org.purc.purcforms.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.Locale;
import org.purc.purcforms.client.widget.DesignWidgetWrapper;


/**
 * Contains shared information that has the notion of being current (eg currently
 * selected form, current locale)
 * 
 * @author daniel
 *
 */
public class Context {
	
	private static String defaultLocale = "en";
	private static String locale = defaultLocale;
	private static List<Locale> locales = new ArrayList<Locale>();
	private static boolean allowBindEdit = true;
	
	/** The form having focus. */
	private static FormDef formDef;
	
	public static List<DesignWidgetWrapper> clipBoardWidgets = new Vector<DesignWidgetWrapper>();
	
	
	public static void setDefaultLocale(String locale){
		Context.defaultLocale = locale;
	}
	
	public static String getDefaultLocale(){
		return defaultLocale;
	}
	
	public static void setLocale(String locale){
		Context.locale = locale;
	}
	
	public static String getLocale(){
		return locale;
	}
	
	public FormDef getFormDef() {
		return formDef;
	}

	public static void setFormDef(FormDef formDef) {
		Context.formDef = formDef;
	}

	public static boolean inLocalizationMode(){
		return !defaultLocale.equalsIgnoreCase(locale);
	}
	
	public static List<Locale> getLocales(){
		return locales;
	}
	
	public static void setLocales(List<Locale> locales){
		Context.locales = locales;
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
	
	public static boolean allowBindEdit(){
		return allowBindEdit;
	}
	
	public static void setAllowBindEdit(boolean allowBindEdit){
		Context.allowBindEdit = allowBindEdit;
	}
}
