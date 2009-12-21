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

	/** Flag to tell whether this radio button is checked or not. */
	private boolean checked = false;


	/**
	 * @see com.google.gwt.user.client.ui.RadioButton#RadioButton(String)
	 */
	public RadioButtonWidget(String name){
		super(name);
		sinkEvents(Event.getTypeInt(ClickEvent.getType().getName()));		
		addClickHandler(this);
	}


	/**
	 * @see com.google.gwt.user.client.ui.RadioButton#RadioButton(String, String)
	 */
	public RadioButtonWidget(String name, String label){
		super(name,label);

		addClickHandler(this);
	}


	/**
	 * Adds the click event handler for a radio button widget.
	 * 
	 * @param widget the widget to add the handler for.
	 */
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

			if((getParent().getParent() instanceof RuntimeWidgetWrapper &&
					((RuntimeWidgetWrapper)getParent().getParent()).isLocked()) ||
					!(getParent().getParent() instanceof RuntimeWidgetWrapper)){

				event.preventDefault();
				event.stopPropagation();
				return;
			}
		}

		if(DOM.eventGetType(event) == Event.ONMOUSEUP || DOM.eventGetType(event) == Event.ONMOUSEDOWN){
			if(!(getParent().getParent() instanceof RuntimeWidgetWrapper)){
				event.preventDefault();
				event.stopPropagation();
				return;
			}
		}

		super.onBrowserEvent(event);
	}
}
