package org.purc.purcforms.client.widget;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.TextBox;


/**
 * 
 * @author daniel
 *
 */
public class TextBoxWidget extends TextBox{


	public TextBoxWidget(){
		super();

		sinkEvents(Event.getTypeInt(ClickEvent.getType().getName()));
		sinkEvents(Event.getTypeInt(KeyDownEvent.getType().getName()));
		sinkEvents(Event.getTypeInt(KeyUpEvent.getType().getName()));
		sinkEvents(Event.getTypeInt(KeyPressEvent.getType().getName()));
		sinkEvents(Event.getTypeInt(ChangeEvent.getType().getName()));
	}


	@Override
	public void onBrowserEvent(Event event){

		switch(DOM.eventGetType(event)){
		case Event.ONCLICK:
		case Event.ONKEYDOWN:
		case Event.ONKEYPRESS:
		case Event.ONKEYUP:
		case Event.ONCHANGE:
			if(!(getParent().getParent() instanceof RuntimeWidgetWrapper ||
				(getParent().getParent() instanceof DateTimeWidget &&
				getParent().getParent().getParent().getParent() instanceof RuntimeWidgetWrapper))){
				
				event.preventDefault();
				return;
			}
		}

		super.onBrowserEvent(event);
	}
}
