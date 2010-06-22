package org.purc.purcforms.client.listener;


/**
 * 
 * @author daniel
 *
 */
public interface FormDataLoadListener {
	void onFormDataLoaded(String xformXml, String layoutXml, String javaScriptSrc, String modelXml);
}
