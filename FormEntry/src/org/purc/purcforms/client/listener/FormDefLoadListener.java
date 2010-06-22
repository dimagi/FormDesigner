package org.purc.purcforms.client.listener;

public interface FormDefLoadListener {
	void onFormDefLoaded(boolean designForm, String xformXml, String layoutXml, String javaScriptSrc);
}
