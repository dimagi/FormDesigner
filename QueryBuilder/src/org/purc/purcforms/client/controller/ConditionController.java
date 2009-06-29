package org.purc.purcforms.client.controller;

import org.purc.purcforms.client.widget.ConditionActionHyperlink;
import org.purc.purcforms.client.widget.ConditionWidget;

import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author daniel
 *
 */
public interface ConditionController {

	public ConditionWidget addCondition(Widget sender);
	public ConditionActionHyperlink addBracket(Widget sender, String operator, boolean addCondition);
	public void deleteCondition(Widget sender,ConditionWidget conditionWidget);
}
