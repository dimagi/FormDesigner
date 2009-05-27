package org.purc.purcforms.client;


/**
 * 
 * @author daniel
 *
 */
public class Context {
	
	private static String defaultLocale = "en";
	private static String locale = "en";
	
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
}
