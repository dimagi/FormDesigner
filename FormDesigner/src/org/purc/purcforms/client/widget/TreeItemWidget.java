package org.purc.purcforms.client.widget;

import org.purc.purcforms.client.controller.IFormActionListener;
import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.util.FormUtil;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel;


/**
 * Widget for tree items which gives them a context menu.
 * 
 * @author daniel
 *
 */
public class TreeItemWidget extends Composite{

	/** Popup panel for the context menu. */
	private PopupPanel popup;

	/** Listener for form action events. */
	private IFormActionListener formActionListener;


	/**
	 * Creates a new tree item.
	 * 
	 * @param imageProto the item image.
	 * @param caption the time caption or text.
	 * @param popup the pop up panel for context menu.
	 * @param formActionListener listener to form action events.
	 */
	public TreeItemWidget(ImageResource imageProto, String caption, PopupPanel popup,IFormActionListener formActionListener){

		this.popup = popup;
		this.formActionListener = formActionListener;

		HorizontalPanel hPanel = new HorizontalPanel();
		hPanel.setSpacing(0);

		hPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		hPanel.add(FormUtil.createImage(imageProto));
		HTML headerText = new HTML(caption);
		hPanel.add(headerText);
		hPanel.setStyleName("gwt-noWrap");
		initWidget(hPanel);

		DOM.sinkEvents(getElement(), DOM.getEventsSunk(getElement()) | Event.ONMOUSEDOWN | Event.ONKEYDOWN );
	}

	@Override
	public void onBrowserEvent(Event event) {
		if (DOM.eventGetType(event) == Event.ONMOUSEDOWN) {
			if( (event.getButton() & Event.BUTTON_RIGHT) != 0 /*&& !Context.isStructureReadOnly()*/){	  
				
				int ypos = event.getClientY();
				if(Window.getClientHeight() - ypos < 350)
					ypos = event.getClientY() - 350;
					
				popup.setPopupPosition(event.getClientX(), ypos);
				FormDesignerUtil.disableContextMenu(popup.getElement());
				popup.show();
			}
		}
		else if(DOM.eventGetType(event) == Event.ONKEYDOWN){
			if(event.getKeyCode() == KeyCodes.KEY_DELETE)
				formActionListener.deleteSelectedItem();
		}
	}
}
