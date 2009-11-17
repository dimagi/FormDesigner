package org.purc.purcforms.client.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.CheckBox;



/**
 * This class is only to enable us have check boxes that can be locked
 * 
 * @author daniel
 *
 */
public class CheckBoxWidget extends CheckBox{
	
	public CheckBoxWidget(String label){
		super(label);

		sinkEvents(Event.getTypeInt(ClickEvent.getType().getName()));
	}

	
	@Override
	public void onBrowserEvent(Event event){
		if(DOM.eventGetType(event) == Event.ONCLICK){
			if(getParent().getParent() instanceof RuntimeWidgetWrapper &&
					((RuntimeWidgetWrapper)getParent().getParent()).isLocked()){
				event.preventDefault();
				event.stopPropagation();
				return;
			}
		}
		
		super.onBrowserEvent(event);
	}
}
