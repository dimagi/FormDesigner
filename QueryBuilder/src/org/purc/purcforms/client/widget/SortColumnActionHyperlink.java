package org.purc.purcforms.client.widget;

import org.purc.purcforms.client.controller.SortColumnActionListener;
import org.purc.purcforms.client.model.SortField;

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
public class SortColumnActionHyperlink extends Hyperlink {

	private PopupPanel popup;
	private SortColumnActionListener actionListener;
	
	public SortColumnActionHyperlink(String text, String targetHistoryToken ,SortColumnActionListener actionListener){
		super(text,targetHistoryToken);
		this.actionListener = actionListener;
		DOM.sinkEvents(getElement(), DOM.getEventsSunk(getElement()) | Event.ONMOUSEDOWN );
	}

	public void onBrowserEvent(Event event) {
		if(DOM.eventGetType(event) == Event.ONMOUSEDOWN) {
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
		menuBar.addItem("Ascending",true, new Command(){
			public void execute() {popup.hide(); actionListener.changeSortOrder(w, SortField.SORT_ASCENDING);}});

		menuBar.addItem("Descending",true, new Command(){
			public void execute() {popup.hide(); actionListener.changeSortOrder(w, SortField.SORT_DESCENDING);}});

		menuBar.addSeparator();
		menuBar.addItem("Move up",true, new Command(){ //LocaleText.get("???")
			public void execute() {popup.hide(); actionListener.moveColumnUp(w);}});

		menuBar.addItem("Move down",true, new Command(){ //LocaleText.get("???")
			public void execute() {popup.hide(); actionListener.moveColumnDown(w);}});

		menuBar.addSeparator();
		menuBar.addItem("Delete Sorting",true, new Command(){ //LocaleText.get("???")
			public void execute() {popup.hide(); actionListener.deleteColumn(w);}});

		popup.setWidget(menuBar);
	}
}
