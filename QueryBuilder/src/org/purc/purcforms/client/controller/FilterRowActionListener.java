package org.purc.purcforms.client.controller;

import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author daniel
 *
 */
public interface FilterRowActionListener {
	
	public void addCondition(Widget sender);
	public void addBracket(Widget sender);
	public void deleteCurrentRow(Widget sender);
}
