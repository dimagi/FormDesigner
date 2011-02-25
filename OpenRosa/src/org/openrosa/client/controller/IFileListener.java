package org.openrosa.client.controller;


/**
 * Interface for listening to file New, Open and Save events.
 * 
 */
public interface IFileListener {

	void onNew();
	void onOpen();
	void onPreview(boolean showWindow);
	void showItext();
	boolean saveFile(boolean showWindow);
	void onOpenFile();
	void onSubmit(boolean continueEdit);
}
