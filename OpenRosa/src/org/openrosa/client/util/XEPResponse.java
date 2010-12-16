package org.openrosa.client.util;

import com.google.gwt.core.client.JavaScriptObject;

public class XEPResponse extends JavaScriptObject {

	// Overlay types always have protected, zero argument constructors.
	protected XEPResponse() {
	}

	// JSNI methods to get stock data.
	public final native String getCallback() /*-{
		return this.callback;
	}-*/; 

	public final native String getStatus() /*-{
		return this.status;
	}-*/;

//	public final native boolean getContine() /*-{
//		return this.continue;
//	}-*/;
	public final native boolean getContinue() /*-{ return this['continue']; }-*/;
}
