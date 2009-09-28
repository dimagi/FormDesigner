package org.purc.purcforms.client.controller;



/**
 * This interface is implemented by those interested in listening to
 * events during the opening of a file from the file system.
 * 
 * @author daniel
 *
 */
public interface OpenFileDialogEventListener {
	
	/**
	 * Called when the file contents have been retrieved.
	 * 
	 * @param contents the file contents.
	 */
	public void onSetFileContents(String contents);
}
