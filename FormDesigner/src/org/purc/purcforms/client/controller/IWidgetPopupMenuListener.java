package org.purc.purcforms.client.controller;

import com.google.gwt.user.client.ui.Widget;


/**
 * This interface is implemented by those classes which want to listen to 
 * widget context menu commands.
 * 
 * @author daniel
 *
 */
public interface IWidgetPopupMenuListener {
	
	/**
	 * Called when the user wants to cut the selected widget.
	 * 
	 * @param sender the selected widget.
	 */
	void onCut(Widget sender);
	
	/**
	 * Called when the user wants to copy the selected widget.
	 * 
	 * @param sender the selected widget.
	 */
	void onCopy(Widget sender);
	
	/**
	 * Called when the user wants to delete the selected widget.
	 * 
	 * @param sender the selected widget.
	 */
	void onDelete(Widget sender);
}
