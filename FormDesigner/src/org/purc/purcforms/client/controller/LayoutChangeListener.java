package org.purc.purcforms.client.controller;


/**
 * This interface is implemented by those interested in listening to changes
 * in the form widget layout.
 * 
 * @author daniel
 *
 */
public interface LayoutChangeListener {
	
	/**
	 * Called when the form widget layout changes.
	 * 
	 * @param xml the new widget layout xml.
	 */
	void onLayoutChanged(String xml);
}
