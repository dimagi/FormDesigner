package org.openrosa.client.controller;

import org.openrosa.client.model.IFormElement;


/**
 * Used for notifications of element (question or group) data type changes.
 * 
 * @author daniel
 *
 */
public interface IDataTypeChangeListener {
	
	/**
	 * Called when a element's data type changes.
	 * 
	 * @param element the element whose data type has changed.
	 * @param prevDataType the previous data type before the change.
	 */
	void onDataTypeChanged(IFormElement element, int prevDataType);
}
