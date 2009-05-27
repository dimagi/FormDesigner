package org.purc.purcforms.client.controller;


/**
 * 
 * @author daniel
 *
 */
public interface IFormSaveListener {
	public void onSaveForm(int formId, String xformsXml, String layoutXml);
	public void onSaveLocaleText(int formId, String xformsLocaleText, String layoutLocaleText);
}
