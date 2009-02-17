package org.purc.purcforms.client.widget;

import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author daniel
 *
 */
public interface EditListener {
	public void onMoveToNextWidget(Widget widget);
	public void onMoveToPrevWidget(Widget widget);
	public void onValueChanged(Widget sender, Object oldValue, Object newValue);
}
