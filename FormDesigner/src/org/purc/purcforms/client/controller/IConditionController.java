package org.purc.purcforms.client.controller;

import org.purc.purcforms.client.widget.skiprule.ConditionWidget;


/**
 * This interface is implemented by those classes that want to listen to events which
 * happen when the user manipulates conditions for validation and skip rules.
 * 
 * @author daniel
 *
 */
public interface IConditionController {

	/**
	 * Called to add a new condition.
	 */
	public void addCondition();
	
	/**
	 * Called to add a bracket for grouping of related conditions.
	 */
	public void addBracket();
	
	/**
	 * Called to delete a condition.
	 * 
	 * @param conditionWidget the widget for the condition to delete.
	 */
	public void deleteCondition(ConditionWidget conditionWidget);
}
