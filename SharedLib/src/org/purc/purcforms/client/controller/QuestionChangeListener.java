package org.purc.purcforms.client.controller;


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
}
