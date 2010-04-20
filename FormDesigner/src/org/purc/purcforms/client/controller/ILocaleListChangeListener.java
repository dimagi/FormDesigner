package org.purc.purcforms.client.controller;


/**
 * Interface for notification whenever the list of locales changes.
 * 
 * @author daniel
 *
 */
public interface ILocaleListChangeListener {
	
	/**
	 * Called whenever the locale list changes.
	 */
	void onLocaleListChanged();
}
