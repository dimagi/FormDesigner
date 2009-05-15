package org.purc.purcforms.client.widget.skiprule;


import org.purc.purcforms.client.locale.LocaleText;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;


/**
 * 
 * @author daniel
 *
 */
public class ActionHyperlink extends Hyperlink {

	private PopupPanel popup;
	private ConditionWidget condWidget;
	private boolean allowBrackets;

	public ActionHyperlink(String text, String targetHistoryToken,ConditionWidget condWidget, boolean allowBrackets){
		super(text,targetHistoryToken);
		this.condWidget = condWidget;
		this.allowBrackets = allowBrackets;
		DOM.sinkEvents(getElement(), DOM.getEventsSunk(getElement()) | Event.ONMOUSEDOWN );
	}

	public void onBrowserEvent(Event event) {
		if (DOM.eventGetType(event) == Event.ONMOUSEDOWN) {
			setupPopup();
			popup.setPopupPosition(event.getClientX(), event.getClientY());
			popup.show();
		}
	}

	private void setupPopup(){
		popup = new PopupPanel(true,true);

		MenuBar menuBar = new MenuBar(true);
		
		menuBar.addItem(LocaleText.get("deleteCondition"),true, new Command(){
			public void execute() {popup.hide(); condWidget.deleteCurrentRow();}});
		
		menuBar.addItem(LocaleText.get("addCondition"),true, new Command(){
			public void execute() {popup.hide(); condWidget.addCondition();}});

		/*if(allowBrackets){
			menuBar.addItem("Add Bracket",true, new Command(){
				public void execute() {popup.hide(); condWidget.addBracket();}});
		}*/

		popup.setWidget(menuBar);
	}

}
