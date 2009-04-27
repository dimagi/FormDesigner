package org.purc.purcforms.client.controller;

import java.util.List;

import org.purc.purcforms.client.model.OptionDef;


/**
 * 
 * @author daniel
 *
 */
public interface QuestionChangeListener {
	public void onEnabledChanged(boolean enabled);
	public void onVisibleChanged(boolean visible);
	public void onRequiredChanged(boolean required);
	public void onLockedChanged(boolean locked);
	public void onBindingChanged(String newValue);
	public void onDataTypeChanged(int dataType);
	public void onOptionsChanged(List<OptionDef> optionList);
}
