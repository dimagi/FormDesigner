package org.purc.purcforms.client.locale;

import com.google.gwt.i18n.client.Dictionary;


/**
 * 
 * @author daniel
 *
 */
public class LocaleText {

	private static Dictionary purcformsText = Dictionary.getDictionary("PurcformsText");
	
	public static String get(String key){
		return purcformsText.get(key);
	}
}
