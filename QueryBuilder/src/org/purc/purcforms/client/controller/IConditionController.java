package org.purc.purcforms.client.controller;

import org.purc.purcforms.client.widget.ConditionWidget;

import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author daniel
 *
 */
public interface IConditionController {

	public void addCondition(Widget sender);
	public void addBracket(Widget sender);
	public void deleteCondition(Widget sender,ConditionWidget conditionWidget);
}
