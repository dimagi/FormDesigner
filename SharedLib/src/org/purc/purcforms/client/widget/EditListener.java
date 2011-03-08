package org.purc.purcforms.client.widget;

import com.google.gwt.user.client.ui.Widget;


/**
 * This interface is used to communicate events that happen 
 * during editing of form values. These could be keyboard tab events,
 * changes of answer values, or adding and removing of items from repeat widgets.
 * 
 * @author daniel
 *
 */
public interface EditListener {
	
	/**
	 * Called when the user wants to move to the next widget in the order of tab index.
	 * 
	 * @param widget the widget that currently has focus.
	 */
	public void onMoveToNextWidget(Widget widget);
	
	/**
	 * Called when the user wants to move to the previous widget in the order of tab index.
	 * 
	 * @param widget the widget that currently has focus.
	 */
	public void onMoveToPrevWidget(Widget widget);
	
	/**
	 * Called when the answer or value of a widget changes.
	 * 
	 * @param widget the widget whose answer has changed.
	 */
	public void onValueChanged(RuntimeWidgetWrapper widget);
	
	/**
	 * Called after a new row has been added to a repeat widget.
	 * 
	 * @param widget the repeat widget that has the new row.
	 * @param increment the increment in height due to the adding of the new row.
	 */
	public void onRowAdded(RuntimeWidgetWrapper rptWidget, int increment);
	
	/**
	 * Called after a row has been removed from a repeat widget.
	 * 
	 * @param widget the repeat widget.
	 * @param decrement the decrease in height due to the removal of the row.
	 */
	public void onRowRemoved(RuntimeWidgetWrapper rptWidget, int decrement);
}
