package org.purc.purcforms.client.widget.skiprule;

import org.purc.purcforms.client.PurcConstants;
import org.purc.purcforms.client.controller.ItemSelectionListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.ModelConstants;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.widget.SelectItemCommand;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;


/**
 * This widget is used to display a list of function which can be used in a
 * validation rule conditions question value. eg Length
 * 
 * @author daniel
 *
 */
public class FunctionHyperlink extends Hyperlink implements ItemSelectionListener {
	
	/** The length function text: Length */
	public static final String FUNCTION_TEXT_LENGTH = LocaleText.get("length");
	
	/** The value function text: Length */
	public static final String FUNCTION_TEXT_VALUE = LocaleText.get("value");
	
	/** The popup to display functions. */
	private PopupPanel popup;
	
	/** The data type of the currently selected question. */
	private int dataType =  QuestionDef.QTN_TYPE_TEXT;
	
	/** The listener to item selection events. */
	private ItemSelectionListener itemSelectionListener;
	
	
	/**
	 * Creates a new instance of the question value widget.
	 * 
	 * @param text the display text.
	 * @param targetHistoryToken the history token to which it will link.
	 * @param itemSelectionListener the listener to item selection events.
	 */
	public FunctionHyperlink(String text, String targetHistoryToken,ItemSelectionListener itemSelectionListener){
		super(text,targetHistoryToken);
		this.itemSelectionListener = itemSelectionListener;
		DOM.sinkEvents(getElement(), DOM.getEventsSunk(getElement()) | Event.ONMOUSEDOWN );
	}
	
	/** 
	 * Sets the data type for the currently selected question.
	 * 
	 * @param dataType the data type.
	 */
	public void setDataType(int dataType){
		this.dataType = dataType;
		
		setText((dataType == QuestionDef.QTN_TYPE_REPEAT) ? FUNCTION_TEXT_LENGTH : FUNCTION_TEXT_VALUE);
	}
	  
	@Override
	public void onBrowserEvent(Event event) {
		  if (DOM.eventGetType(event) == Event.ONMOUSEDOWN) {
			  itemSelectionListener.onStartItemSelection(this);
			  setupPopup();
		      popup.setPopupPosition(event.getClientX(), event.getClientY() - 65);
		      popup.show();
		  }
	}
	
	/**
	 * Creates the popup of the question value functions.
	 */
	private void setupPopup(){
		popup = new PopupPanel(true,true);
		
		int count = 0;
		
		MenuBar menuBar = new MenuBar(true);
		
		if(dataType != QuestionDef.QTN_TYPE_REPEAT){
			menuBar.addItem(FUNCTION_TEXT_VALUE,true, new SelectItemCommand(FUNCTION_TEXT_VALUE,this));
			count += 1;
		}
		
		if(dataType == QuestionDef.QTN_TYPE_TEXT || dataType == QuestionDef.QTN_TYPE_REPEAT){
			menuBar.addItem(FUNCTION_TEXT_LENGTH,true, new SelectItemCommand(FUNCTION_TEXT_LENGTH,this));
			count += 1;
		}
		 
		int height = count*30;
		if(height > 200)
			height = 200;
		
		ScrollPanel scrollPanel = new ScrollPanel();
		scrollPanel.setWidget(menuBar);
		scrollPanel.setWidth("70"+PurcConstants.UNITS);
		scrollPanel.setHeight(height+PurcConstants.UNITS);
		//scrollPanel.setHeight((Window.getClientHeight() - getAbsoluteTop() - 25)+PurcConstants.UNITS);
		
		popup.setWidget(scrollPanel);
	}
	
	/**
	 * Converts the function text to its int value.
	 * 
	 * @param text the function text.
	 * @return the function int value.
	 */
	private int fromFunctionText2Value(String text){
		if(text.equals(FUNCTION_TEXT_LENGTH))
			return ModelConstants.FUNCTION_LENGTH;
		return ModelConstants.FUNCTION_VALUE;
	}
	
	/**
	 * @see org.purc.purcforms.client.controller.ItemSelectionListener#onItemSelected(Object, Object)
	 */
	public void onItemSelected(Object sender, Object item) {
		if(sender instanceof SelectItemCommand){
			popup.hide();
			setText((String)item);
			itemSelectionListener.onItemSelected(this, fromFunctionText2Value((String)item));
		}
	}
	
	/**
	 * @see org.purc.purcforms.client.controller.ItemSelectionListener#onStartItemSelection(Object)
	 */
	public void onStartItemSelection(Object sender){
		
	}
	
	/**
	 * Sets the selected function.
	 * 
	 * @param function the selected function. Can only be ModelConstants.FUNCTION_LENGTH
	 * 		  or ModelConstants.FUNCTION_VALUE
	 */
	public void setFunction(int function){
		String functionText = FUNCTION_TEXT_VALUE;
		
		if(function == ModelConstants.FUNCTION_LENGTH)
			functionText = FUNCTION_TEXT_LENGTH;
		
		setText(functionText);
	}

}
