package org.purc.purcforms.client.widget;

import org.purc.purcforms.client.controller.AggregateFunctionListener;
import org.purc.purcforms.client.model.QuestionDef;

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
public class AggregateFunctionHyperlink extends Hyperlink {

	public static final String FUNC_TEXT_SUM = "Sum"; //LocaleText.get("???");
	public static final String FUNC_TEXT_AVG = "Average";
	public static final String FUNC_TEXT_MIN = "Minimum";
	public static final String FUNC_TEXT_MAX = "Maximum";
	public static final String FUNC_TEXT_COUNT = "Count";

	private PopupPanel popup;
	private AggregateFunctionListener actionListener;
	private QuestionDef questionDef;


	public AggregateFunctionHyperlink(String text, String targetHistoryToken ,AggregateFunctionListener actionListener){
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
		if(questionDef == null)
			return;

		popup = new PopupPanel(true,true);

		MenuBar menuBar = new MenuBar(true);

		final Widget w = this;

		//LocaleText.get("???")
		if(questionDef.getDataType() == QuestionDef.QTN_TYPE_NUMERIC ||
				questionDef.getDataType() == QuestionDef.QTN_TYPE_DECIMAL){

			menuBar.addItem(FUNC_TEXT_SUM,true, new Command(){
				public void execute() {popup.hide(); ((Hyperlink)w).setText(FUNC_TEXT_SUM); actionListener.onSum(w);}});

			menuBar.addItem(FUNC_TEXT_AVG,true, new Command(){
				public void execute() {popup.hide(); ((Hyperlink)w).setText(FUNC_TEXT_AVG); actionListener.onAverage(w);}});
		}

		if(questionDef.getDataType() == QuestionDef.QTN_TYPE_NUMERIC ||
				questionDef.getDataType() == QuestionDef.QTN_TYPE_DECIMAL ||
				questionDef.getDataType() == QuestionDef.QTN_TYPE_DATE ||
				questionDef.getDataType() == QuestionDef.QTN_TYPE_TIME ||
				questionDef.getDataType() == QuestionDef.QTN_TYPE_DATE_TIME){

			menuBar.addSeparator();
			menuBar.addItem(FUNC_TEXT_MIN,true, new Command(){ //LocaleText.get("???")
				public void execute() {popup.hide(); ((Hyperlink)w).setText(FUNC_TEXT_MIN); actionListener.onMinimum(w);}});

			menuBar.addItem(FUNC_TEXT_MAX,true, new Command(){ //LocaleText.get("???")
				public void execute() {popup.hide(); ((Hyperlink)w).setText(FUNC_TEXT_MAX); actionListener.onMaximum(w);}});
			
			menuBar.addSeparator();
		}

		menuBar.addItem(FUNC_TEXT_COUNT,true, new Command(){ //LocaleText.get("???")
			public void execute() {popup.hide(); ((Hyperlink)w).setText(FUNC_TEXT_COUNT); actionListener.onCount(w);}});

		popup.setWidget(menuBar);
	}

	public void setQuestionDef(QuestionDef questionDef){
		this.questionDef = questionDef;
	}
	
	public String getAggregateFunction(){
		String text = getText();
		
		if(text.equals(FUNC_TEXT_SUM))
			return "SUM";
		else if(text.equals(FUNC_TEXT_AVG))
			return "AVG";
		else if(text.equals(FUNC_TEXT_MIN))
			return "MIN";
		else if(text.equals(FUNC_TEXT_MAX))
			return "MAX";
		
		return "COUNT";
	}
	
	public void setAggregateFunction(String aggFunc){
		setText(aggFunc);
	}
}
