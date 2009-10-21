package org.purc.purcforms.client.controller;


/**
 * This interface is implemented by those interested in listening to form level
 * events to do with data submission and cancelling of data submission.
 * 
 * @author daniel
 *
 */
public interface SubmitListener {
	
	/**
	 * Called when one has finished filling a form and wants to send data to the server.
	 * In preview mode, one just wants to see the xforms model as it will appear at runtime.
	 * 
	 * @param xml the xforms model filled with data.
	 */
	public void onSubmit(String xml);
	
	/**
	 * Called when one wants to close the form and discard all the data entered or
	 * changes made on the form.
	 */
	public void onCancel();
}
