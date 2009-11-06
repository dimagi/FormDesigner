package org.purc.purcforms.client.widget;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.RadioButton;


/**
 * 
 * @author daniel
 *
 */
public class RadioButtonWidget extends RadioButton{

	public RadioButtonWidget(String name){
		super(name);
	}

	public RadioButtonWidget(String name, String label){
		super(name,label);
	}

	public RadioButtonWidget(String name, String label, boolean asHTML){
		super(name,label,asHTML);
	}

	@Override
	public void onBrowserEvent(Event event){
		if(DOM.eventGetType(event) == Event.ONMOUSEUP){
			if(getValue() == true){
				event.stopPropagation();
				event.preventDefault();
				setValue(false);
				return;
			}
		}
		
		super.onBrowserEvent(event);
	}
}
