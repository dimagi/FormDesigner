package org.purc.purcforms.client.widget;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ListBox;


/**
 * This class is only to enable us have list boxes that can be locked
 * 
 * @author daniel
 *
 */
public class ListBoxWidget extends ListBox{
	
	private int selectedIndex = -1;
	
	public ListBoxWidget(boolean isMultipleSelect){
		super(isMultipleSelect);
	    sinkEvents(Event.getTypeInt(ChangeEvent.getType().getName()));
	}

	
	@Override
	public void onBrowserEvent(Event event){
		if(DOM.eventGetType(event) == Event.ONCHANGE){
			if(getParent().getParent() instanceof RuntimeWidgetWrapper &&
					((RuntimeWidgetWrapper)getParent().getParent()).isLocked()){
				super.setSelectedIndex(selectedIndex);
				return;
			}
		}

		super.onBrowserEvent(event);
	}
	
	public void setSelectedIndex(int index) {
		 selectedIndex = index;
		 super.setSelectedIndex(index);
	}
}
