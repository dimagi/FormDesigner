package org.openrosa.client.controller;


/**
 * Interface for listening to file New, Open and Save events.
 * 
 * @author daniel
 *
 */
public interface IFileListener {

	void onNew();
	void onOpen();
	void onSave(boolean showWindow);
	void showIText();
}
