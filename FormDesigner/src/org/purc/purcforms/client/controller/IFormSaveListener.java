package org.purc.purcforms.client.controller;


/**
 * This interface is implemented by those who want to listen to the form designer's events
 * of saving xforms and widget layout xml together with xforms and layout locale text.
 * 
 * @author daniel
 *
 */
public interface IFormSaveListener {
	
	/**
	 * Called to save a forms' xform and widget layout xml.
	 * 
	 * @param formId the identifier of the form.
	 * @param xformsXml the xforms xml for the form.
	 * @param layoutXml the layout xml of the form.
	 * @param javaScriptSrc the JavaScript source.
	 * @return true if saving was successful, else false.
	 */
	public boolean onSaveForm(int formId, String xformsXml, String layoutXml, String javaScriptSrc);
	
	/**
	 * Called to save a form's xforms and layout locale text.
	 * 
	 * @param formId the identifier of the form.
	 * @param xformsLocaleText the xforms locale text.
	 * @param layoutLocaleText the layout xml locale text.
	 */
	public void onSaveLocaleText(int formId, String xformsLocaleText, String layoutLocaleText);
}
