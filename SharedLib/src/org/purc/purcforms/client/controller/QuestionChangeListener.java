package org.purc.purcforms.client.controller;

import java.util.List;

import org.purc.purcforms.client.model.OptionDef;
import org.purc.purcforms.client.model.QuestionDef;


/**
 * 
 * @author daniel
 *
 */
public interface QuestionChangeListener {
	public void onEnabledChanged(QuestionDef sender,boolean enabled);
	public void onVisibleChanged(QuestionDef sender,boolean visible);
	public void onRequiredChanged(QuestionDef sender,boolean required);
	public void onLockedChanged(QuestionDef sender,boolean locked);
	public void onBindingChanged(QuestionDef sender,String newValue);
	public void onDataTypeChanged(QuestionDef sender,int dataType);
	public void onOptionsChanged(QuestionDef sender,List<OptionDef> optionList);
}
