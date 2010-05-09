package org.openrosa.client.controller;

import org.openrosa.client.model.FormDef;



/**
 * 
 * @author daniel
 *
 */
public interface ICenterPanel {

	void commitChanges();
	String getJavaScriptSource();
	FormDef getFormDef();
}
