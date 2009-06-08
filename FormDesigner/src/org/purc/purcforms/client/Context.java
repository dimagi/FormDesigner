package org.purc.purcforms.client;

import java.util.ArrayList;
import java.util.List;

import org.purc.purcforms.client.model.Locale;


/**
 * 
 * @author daniel
 *
 */
public class Context {
	
	private static String defaultLocale = "en";
	private static String locale = defaultLocale;
	private static List<Locale> locales = new ArrayList<Locale>();
	
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
	
	public static boolean inLocalizationMode(){
		return !defaultLocale.equalsIgnoreCase(locale);
	}
	
	public static List<Locale> getLocales(){
		return locales;
	}
	
	public static void setLocales(List<Locale> locales){
		Context.locales = locales;
	}
}
