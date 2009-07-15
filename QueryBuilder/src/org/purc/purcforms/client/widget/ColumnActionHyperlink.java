package org.purc.purcforms.client.widget;

import org.purc.purcforms.client.controller.DisplayColumnActionListener;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author daniel
 *
 */
public class ColumnActionHyperlink extends Hyperlink {

	private PopupPanel popup;
	private DisplayColumnActionListener actionListener;
	private boolean aggregateFunction = false;
	

	public ColumnActionHyperlink(String text, String targetHistoryToken ,DisplayColumnActionListener actionListener){
		super(text,targetHistoryToken);
		this.actionListener = actionListener;
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

		final Widget w = this;

		//LocaleText.get("???")
		menuBar.addItem("Sorting",true, new Command(){
			public void execute() {popup.hide(); ;}});

		menuBar.addItem("Distinct on/off",true, new Command(){
			public void execute() {popup.hide(); ;}});

		menuBar.addSeparator();
		menuBar.addItem("Move up",true, new Command(){ //LocaleText.get("???")
			public void execute() {popup.hide(); actionListener.moveColumnUp(w);}});

		menuBar.addItem("Move down",true, new Command(){ //LocaleText.get("???")
			public void execute() {popup.hide(); actionListener.moveColumnDown(w);}});

		menuBar.addSeparator();
		menuBar.addItem("Delete column",true, new Command(){ //LocaleText.get("???")
			public void execute() {popup.hide(); actionListener.deleteColumn(w);}});

		menuBar.addSeparator();
		menuBar.addItem((aggregateFunction ? "Simple" : "Aggregate") + " Column",true, new Command(){ //LocaleText.get("???")
			public void execute() {
				popup.hide(); 
				if(aggregateFunction)
					actionListener.showSimpleColum(w);
				else
					actionListener.showAggregateColumn(w);
				
				aggregateFunction = !aggregateFunction;
				
				setupPopup();
			}
		});

		popup.setWidget(menuBar);
	}
	
	public void setAggregateFunctionMode(boolean set){
		aggregateFunction = set;
	}
}
