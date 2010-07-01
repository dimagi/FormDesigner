package org.purc.purcforms.client.controller;


/**
 * Interface for listening to form item (form,page,question or question option)
 *  property changes during design.
 * 
 * @author daniel
 *
 */
public interface IFormChangeListener {

	/**
	 * Called when the property of a form item (form,page,question or question option) 
	 * is changed. Such properties could be, Text, Help Text, Binding, Data Type, Visibility, and more.
	 * 
	 * @param formItem the item which has been changed.
	 * @return the new item in case the called has changed it.
	 */
	public Object onFormItemChanged(Object formItem);
	
	/**
	 * Called when it is time to deleted the kids of a form item (QuestionDef,PageDef).
	 * 
	 * @param formItem the form item whose kids are to be deleted.
	 */
	public void onDeleteChildren(Object formItem);
}
