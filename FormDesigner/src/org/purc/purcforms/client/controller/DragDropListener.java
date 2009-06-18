package org.purc.purcforms.client.controller;

import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author daniel
 *
 */
public interface DragDropListener {
	public void onDragStart(Widget widget);
	public void onDragEnd(Widget widget);
	public void onDrop(Widget widget,int x, int y);
}
