package org.purc.purcforms.client.controller;

import com.google.gwt.user.client.ui.Widget;


/**
 * This interface is implemented by those who want to listening to changes in
 * widget selection.
 * 
 * @author daniel
 *
 */
public interface WidgetSelectionListener {
	
	/**
	 * Called when a widgets is selected.
	 * 
	 * @param widget the selected widget.
	 * @param multipleSel has a value of true if we are doing multiple selection, else false.
	 */
	public void onWidgetSelected(Widget widget, boolean multipleSel);
}
