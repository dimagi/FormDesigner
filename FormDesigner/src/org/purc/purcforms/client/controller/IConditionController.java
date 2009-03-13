package org.purc.purcforms.client.controller;

import org.purc.purcforms.client.widget.skiprule.ConditionWidget;


/**
 * 
 * @author daniel
 *
 */
public interface IConditionController {

	public void addCondition();
	public void addBracket();
	public void deleteCondition(ConditionWidget conditionWidget);
}
