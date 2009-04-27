package org.purc.purcforms.client.widget;

import org.purc.purcforms.client.model.QuestionDef;

import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author daniel
 *
 */
public interface EditListener {
	public void onMoveToNextWidget(Widget widget);
	public void onMoveToPrevWidget(Widget widget);
	public void onValueChanged(QuestionDef questionDef);
}
