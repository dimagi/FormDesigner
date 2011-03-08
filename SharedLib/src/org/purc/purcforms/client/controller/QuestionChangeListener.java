package org.purc.purcforms.client.controller;

import java.util.List;

import org.purc.purcforms.client.model.OptionDef;
import org.purc.purcforms.client.model.QuestionDef;


/**
 * This interface is implemented by those interested in listening to changes
 * on a question definition.
 * 
 * @author daniel
 *
 */
public interface QuestionChangeListener {
	
	/**
	 * Called when the enabled property changes.
	 * 
	 * @param sender the question whose property value has changed.
	 * @param enabled the new value of the property.
	 */
	public void onEnabledChanged(QuestionDef sender,boolean enabled);
	
	/**
	 * Called when the visible property changes.
	 * 
	 * @param sender the question whose property value has changed.
	 * @param visible the new value of the property.
	 */
	public void onVisibleChanged(QuestionDef sender,boolean visible);
	
	/**
	 * Called when the required property changes.
	 * 
	 * @param sender the question whose property value has changed.
	 * @param required the new value of the property.
	 */
	public void onRequiredChanged(QuestionDef sender,boolean required);
	
	/**
	 * Called when the locked property changes.
	 * 
	 * @param sender the question whose property value has changed.
	 * @param locked
	 */
	public void onLockedChanged(QuestionDef sender,boolean locked);
	
	/**
	 * Called when the binding property changes.
	 * 
	 * @param sender the question whose property value has changed.
	 * @param binding  the new value of the property.
	 */
	public void onBindingChanged(QuestionDef sender,String binding);
	
	/**
	 * Called when the data type property changes.
	 * 
	 * @param sender the question whose property value has changed.
	 * @param dataType the new value of the property.
	 */
	public void onDataTypeChanged(QuestionDef sender,int dataType);
	
	/**
	 * Called when the list of options changes for a single or multiple select question.
	 * 
	 * @param sender the question whose list of options has changed.
	 * @param optionList the new list of options.
	 */
	public void onOptionsChanged(QuestionDef sender,List<OptionDef> optionList);
}
