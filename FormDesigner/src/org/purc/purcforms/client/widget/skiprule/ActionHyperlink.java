package org.purc.purcforms.client.widget.skiprule;


import org.purc.purcforms.client.locale.LocaleText;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;


/**
 * Widgets used to display the list of possible actions for a skip or validation
 * rule condition. For now, these actions are "Add Condition" and "Delete Condition"
 * This widget is displayed on the left hand side of each condition and its text is "<>"
 * which on being clicked displays the list of possible actions for the condition.
 * 
 * @author daniel
 *
 */
public class ActionHyperlink extends Hyperlink {

	/** The popup to display the condition actions. */
	private PopupPanel popup;
	
	/** The condition to apply this action to. */
	private ConditionWidget condWidget;
	
	/** Flag determining whether to allow conditions grouping or nested conditions
	 *  using add bracket.
	 */
	private boolean allowBrackets;

	
	/**
	 * Creates a new instance of the condition actions widget.
	 * 
	 * @param text the display text.
	 * @param targetHistoryToken the history token to which it will link.
	 * @param condWidget the condition widget.
	 * @param allowBrackets flag determining whether we allow conditions grouping or not.
	 */
	public ActionHyperlink(String text, String targetHistoryToken,ConditionWidget condWidget, boolean allowBrackets){
		super(text,targetHistoryToken);
		this.condWidget = condWidget;
		this.allowBrackets = allowBrackets;
		DOM.sinkEvents(getElement(), DOM.getEventsSunk(getElement()) | Event.ONMOUSEDOWN );
	}

	@Override
	public void onBrowserEvent(Event event) {
		if (DOM.eventGetType(event) == Event.ONMOUSEDOWN) {
			setupPopup();
			popup.setPopupPosition(event.getClientX(), event.getClientY() - 60);
			popup.show();
		}
	}

	/**
	 * Creates the condition actions widget popup.
	 */
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
