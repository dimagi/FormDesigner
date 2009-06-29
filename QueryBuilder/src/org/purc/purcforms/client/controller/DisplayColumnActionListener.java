package org.purc.purcforms.client.controller;

import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author daniel
 *
 */
public interface DisplayColumnActionListener {
	
	public void moveColumnUp(Widget sender);
	public void moveColumnDown(Widget sender);
	public void deleteColumn(Widget sender);
	public void showSimpleColum(Widget sender);
	public void showAggregateColumn(Widget sender);
	public void changeSortOrder(Widget sender, int sortOrder);
}
