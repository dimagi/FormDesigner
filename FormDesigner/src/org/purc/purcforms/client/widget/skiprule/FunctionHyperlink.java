package org.purc.purcforms.client.widget.skiprule;

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
 * 
 * @author daniel
 *
 */
public class FunctionHyperlink extends Hyperlink implements ItemSelectionListener {
	
	public static final String FUNCTION_TEXT_LENGTH = "Length";
	public static final String FUNCTION_TEXT_VALUE = LocaleText.get("value");
	
	private PopupPanel popup;
	private int dataType =  QuestionDef.QTN_TYPE_TEXT;
	private ItemSelectionListener itemSelectionListener;
	
	public FunctionHyperlink(String text, String targetHistoryToken,ItemSelectionListener itemSelectionListener){
		super(text,targetHistoryToken);
		this.itemSelectionListener = itemSelectionListener;
		DOM.sinkEvents(getElement(), DOM.getEventsSunk(getElement()) | Event.ONMOUSEDOWN );
	}
	
	public void setDataType(int dataType){
		this.dataType = dataType;
		
		setText((dataType == QuestionDef.QTN_TYPE_REPEAT) ? FUNCTION_TEXT_LENGTH : FUNCTION_TEXT_VALUE);
	}
	  
	public void onBrowserEvent(Event event) {
		  if (DOM.eventGetType(event) == Event.ONMOUSEDOWN) {
			  itemSelectionListener.onStartItemSelection(this);
			  setupPopup();
		      popup.setPopupPosition(event.getClientX(), event.getClientY());
		      popup.show();
		  }
	}
	
	/*public void startSelection(){
		  setupPopup();
	      popup.setPopupPosition(this.getAbsoluteLeft(), this.getAbsoluteTop());
	      popup.show();
	}*/
	
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
		scrollPanel.setWidth("70px");
		scrollPanel.setHeight(height+"px");
		//scrollPanel.setHeight((Window.getClientHeight() - getAbsoluteTop() - 25)+"px");
		
		popup.setWidget(scrollPanel);
	}
	
	public void onItemSelected(Object sender, Object item) {
		if(sender instanceof SelectItemCommand){
			popup.hide();
			setText((String)item);
			itemSelectionListener.onItemSelected(this, fromFunctionText2Value((String)item));
		}
	}
	
	private Byte fromFunctionText2Value(String text){
		if(text.equals(FUNCTION_TEXT_LENGTH))
			return ModelConstants.FUNCTION_LENGTH;
		return ModelConstants.FUNCTION_VALUE;
	}
	
	public void onStartItemSelection(Object sender){
		
	}
	
	public void setFunction(int function){
		String functionText = FUNCTION_TEXT_VALUE;
		
		if(function == ModelConstants.FUNCTION_LENGTH)
			functionText = FUNCTION_TEXT_LENGTH;
		
		setText(functionText);
	}

}
