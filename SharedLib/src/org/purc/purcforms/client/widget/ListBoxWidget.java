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
	
	/** 
	 * This allows us keep track of the selected index such that we can restore it
	 * whenever the user tries to change the selected value of a locked list box.
	 * We had to do this because for now we have not been successful at disabling 
	 * mouse clicks on locked list boxes.
	 */
	private int selectedIndex = -1;
	
	/**
	 * Creates a new instance of the list box widget.
	 * 
	 * @param isMultipleSelect set to true if you want to allow multiple selection.
	 */
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
	
	
	/**
	 * @see com.google.gwt.user.client.ui.ListBox#setSelectedIndex(int)
	 */
	public void setSelectedIndex(int index) {
		 selectedIndex = index;
		 super.setSelectedIndex(index);
	}
}
