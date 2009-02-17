package org.purc.purcforms.client.widget.skiprule;


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
	
	public ActionHyperlink(String text, String targetHistoryToken,ConditionWidget condWidget){
		super(text,targetHistoryToken);
		this.condWidget = condWidget;
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
		menuBar.addItem("Add Condition",true, new Command(){
		    public void execute() {popup.hide(); condWidget.addCondition();}});
	  
		menuBar.addItem("Add Bracket",true, new Command(){
 		    public void execute() {popup.hide(); condWidget.addBracket();}});
		
		menuBar.addItem("Delete Condition",true, new Command(){
 		    public void execute() {popup.hide(); condWidget.deleteCurrentRow();}});
			  
		popup.setWidget(menuBar);
	}

}
