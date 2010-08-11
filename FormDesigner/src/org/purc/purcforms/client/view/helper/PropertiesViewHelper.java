package org.purc.purcforms.client.view.helper;

import com.google.gwt.i18n.client.DateTimeFormat;


/**
 * Aids the PropertiesView class by acting as a delegate for some tasks.
 * 
 * @author Angel
 *
 */
public class PropertiesViewHelper {

	/**
	 * Checks if the entered value is Numeric
	 * 
	 * @param defaultValue Value to check
	 * @return True only and only if the string can be parsed to a digit.
	 */
	public static boolean isDefaultValueNumeric(String defaultValue) {
		try{
			Integer.parseInt(defaultValue);
		}
		catch(NumberFormatException ex){
			return false;
		}
		return true;
	}

	/**
	 * Checks if the entered value is Decimal.
	 * 
	 * @param defaultValue Value to check
	 * @return True only and only if the string can be parsed to a Double.
	 */
	public static boolean isDefaultValueDecimal(String defaultValue) {
		try{
			Double.parseDouble(defaultValue);
		}
		catch(NumberFormatException ex){
			return false;
		}
		return true;
	}

	/**
	 * Checks if the entered value is Date.
	 * 
	 * @param defaultValue Value to check
	 * @return True only and only if the string can be parsed to a Date.
	 */
	public static boolean isDefaultValueDate(String defaultValue) {
		try{			
			DateTimeFormat.getFormat("yyyy.MM.dd").parse(defaultValue);// Using the Date Submit Format defined in the FormDesigner.html
		}
		catch(IllegalArgumentException ex){
			return false;
		}
		return true;
	}

	/**
	 * Checks if the entered value is DateTime.
	 * 
	 * @param defaultValue Value to check
	 * @return True only and only if the string can be parsed to a Date Time.
	 */
	public static boolean isDefaultValueDateTime(String defaultValue) {
		try{			
			DateTimeFormat.getFormat("yyyy.MM.dd hh:mm a").parse(defaultValue);// Using the Date Time Submit Format defined in the FormDesigner.html
		}
		catch(IllegalArgumentException ex){
			return false;
		}
		return true;
	}

	/**
	 * Checks if the entered value is Time.
	 * 
	 * @param defaultValue Value to check
	 * @return True only and only if the string can be parsed to a Time.
	 */
	public static boolean isDefaultValueTime(String defaultValue) {
		try{			
			DateTimeFormat.getFormat("hh:mm a").parse(defaultValue);// Using the Time Submit Format defined in the FormDesigner.html
		}
		catch(IllegalArgumentException ex){
			return false;
		}
		return true;
	}

	/**
	 * Checks if the entered value is Boolean.
	 * 
	 * @param defaultValue Value to check
	 * @return True only and only if the string can be parsed to a Boolean.
	 */
	public static boolean isDefaultValueBoolean(String defaultValue) {
		return Boolean.parseBoolean(defaultValue);
	}	
}
