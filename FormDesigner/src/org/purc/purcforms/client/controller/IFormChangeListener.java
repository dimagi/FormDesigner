package org.purc.purcforms.client.controller;


/**
 * Interfac for listening to form changes during design.
 * 
 * @author daniel
 *
 */
public interface IFormChangeListener {

	/**
	 * Called when a form item (form,page,question or question option) is changed.
	 * 
	 * @param formItem the item which has been changed.
	 */
	public void onFormItemChanged(Object formItem);
	
	public void onDeleteChildren(Object formItem);
}
