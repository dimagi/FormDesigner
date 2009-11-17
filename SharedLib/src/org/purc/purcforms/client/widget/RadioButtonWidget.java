package org.purc.purcforms.client.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.RadioButton;


/**
 * This class is only to enable us have radio buttons that can be unchecked
 * on click
 * 
 * @author daniel
 *
 */
public class RadioButtonWidget extends RadioButton{

	private boolean checked = false;
	
	public RadioButtonWidget(String name){
		super(name);
		
		sinkEvents(Event.getTypeInt(ClickEvent.getType().getName()));
		
		addClickHandler(this);
	}

	public RadioButtonWidget(String name, String label){
		super(name,label);
		
		addClickHandler(this);
	}
	
	private void addClickHandler(final RadioButtonWidget widget){
		addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event){
				if(getParent().getParent() instanceof RuntimeWidgetWrapper &&
						((RuntimeWidgetWrapper)getParent().getParent()).isLocked()){
					setValue(checked);
				}
				else if(((CheckBox)event.getSource()).getValue() == true && checked)
					((CheckBox)event.getSource()).setValue(false);	
			}
		});
	}

	@Override
	public void onBrowserEvent(Event event){
		if(DOM.eventGetType(event) == Event.ONMOUSEUP)
			checked = getValue();
		else if(DOM.eventGetType(event) == Event.ONCLICK){
			
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
