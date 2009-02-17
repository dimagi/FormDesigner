package org.purc.purcforms.client.widget;

import org.purc.purcforms.client.controller.IFormActionListener;
import org.purc.purcforms.client.util.FormDesignerUtil;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.PopupPanel;


/**
 * Represents a tree item.
 * 
 * @author daniel
 *
 */
public class TreeItemWidget extends Composite{

	private PopupPanel popup;
	private IFormActionListener formActionListener;
	
	public TreeItemWidget(AbstractImagePrototype imageProto, String caption, PopupPanel popup,IFormActionListener formActionListener){
		
		this.popup = popup;
		this.formActionListener = formActionListener;
		
		HorizontalPanel hPanel = new HorizontalPanel();
		hPanel.setSpacing(0);
		
		hPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		hPanel.add(imageProto.createImage());
		HTML headerText = new HTML(caption);
		hPanel.add(headerText);
		hPanel.setStyleName("gwt-noWrap");
		initWidget(hPanel);
		
		DOM.sinkEvents(getElement(), DOM.getEventsSunk(getElement()) | Event.ONMOUSEDOWN | Event.ONKEYDOWN );
	}
	
	public void onBrowserEvent(Event event) {
	    if (DOM.eventGetType(event) == Event.ONMOUSEDOWN) {
	    	  if( (event.getButton() & Event.BUTTON_RIGHT) != 0){	  
	    		  popup.setPopupPosition(event.getClientX(), event.getClientY());
	    		  FormDesignerUtil.disableContextMenu(popup.getElement());
	    		  popup.show();
	    	  }
	    }
	    else if(DOM.eventGetType(event) == Event.ONKEYDOWN){
	    	if(event.getKeyCode() == KeyboardListener.KEY_DELETE)
	    		formActionListener.deleteSelectedItem();
	    }
	}
}
