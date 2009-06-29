package org.purc.purcforms.client.controller;

import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author daniel
 *
 */
public interface AggregateFunctionListener {

	public void onSum(Widget sender);
	public void onAverage(Widget sender);
	public void onMinimum(Widget sender);
	public void onMaximum(Widget sender);
	public void onCount(Widget sender);
}
