package org.purc.purcforms.client.widget.skiprule;

import org.purc.purcforms.client.controller.ItemSelectionListener;
import org.purc.purcforms.client.model.ModelConstants;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.widget.SelectItemCommand;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;


/**
 * 
 * @author daniel
 *
 */
public class OperatorHyperlink extends Hyperlink implements ItemSelectionListener {
	
	public static final String OP_TEXT_EQUAL = "is equal to";
	public static final String OP_TEXT_NOT_EQUAL = "is not equal to";
	public static final String OP_TEXT_LESS_THAN = "is less than";
	public static final String OP_TEXT_LESS_THAN_EQUAL = "is less than or equal to";
	public static final String OP_TEXT_GREATER_THAN = "is greater than";
	public static final String OP_TEXT_GREATER_THAN_EQUAL = "is greater than or equal to";
	public static final String OP_TEXT_NULL = "is null";
	public static final String OP_TEXT_IN_LIST = "is in list";
	public static final String OP_TEXT_NOT_IN_LIST = "is not in list";
	public static final String OP_TEXT_STARTS_WITH = "starts with";
	public static final String OP_TEXT_NOT_START_WITH = "does not start with";
	public static final String OP_TEXT_CONTAINS = "contains";
	public static final String OP_TEXT_NOT_CONTAIN = "does not contain";
	public static final String OP_TEXT_BETWEEN = "is between";
	public static final String OP_TEXT_NOT_BETWEEN = "is not between";

	private PopupPanel popup;
	private int dataType =  QuestionDef.QTN_TYPE_TEXT;
	private ItemSelectionListener itemSelectionListener;
	
	public OperatorHyperlink(String text, String targetHistoryToken,ItemSelectionListener itemSelectionListener){
		super(text,targetHistoryToken);
		this.itemSelectionListener = itemSelectionListener;
		DOM.sinkEvents(getElement(), DOM.getEventsSunk(getElement()) | Event.ONMOUSEDOWN );
	}
	
	public void setDataType(int dataType){
		this.dataType = dataType;
		
		//We set the universal operator which is valid for all questions.
		setText(OP_TEXT_EQUAL);
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
		
		MenuBar menuBar = new MenuBar(true);
		menuBar.addItem(OP_TEXT_EQUAL,true, new SelectItemCommand(OP_TEXT_EQUAL,this));
		menuBar.addItem(OP_TEXT_NOT_EQUAL,true, new SelectItemCommand(OP_TEXT_NOT_EQUAL,this));
		  
		if(dataType == QuestionDef.QTN_TYPE_DATE || dataType == QuestionDef.QTN_TYPE_DATE_TIME ||
			dataType == QuestionDef.QTN_TYPE_DECIMAL || dataType == QuestionDef.QTN_TYPE_NUMERIC ||
			dataType == QuestionDef.QTN_TYPE_TIME || dataType == QuestionDef.QTN_TYPE_REPEAT){
				  
			menuBar.addItem(OP_TEXT_LESS_THAN,true,new SelectItemCommand(OP_TEXT_LESS_THAN,this));			  	  
			menuBar.addItem(OP_TEXT_LESS_THAN_EQUAL,true, new SelectItemCommand(OP_TEXT_LESS_THAN_EQUAL,this));		  
			menuBar.addItem(OP_TEXT_GREATER_THAN,true,new SelectItemCommand(OP_TEXT_GREATER_THAN,this));	  
			menuBar.addItem(OP_TEXT_GREATER_THAN_EQUAL,true, new SelectItemCommand(OP_TEXT_GREATER_THAN_EQUAL,this));	  
			menuBar.addItem(OP_TEXT_BETWEEN,true,new SelectItemCommand(OP_TEXT_BETWEEN,this));	  
			menuBar.addItem(OP_TEXT_NOT_BETWEEN,true, new SelectItemCommand(OP_TEXT_NOT_BETWEEN,this));
		}
		
		if(dataType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || dataType == QuestionDef.QTN_TYPE_LIST_MULTIPLE){		  
			menuBar.addItem(OP_TEXT_IN_LIST,true,new SelectItemCommand(OP_TEXT_IN_LIST,this));	  
			menuBar.addItem(OP_TEXT_NOT_IN_LIST,true, new SelectItemCommand(OP_TEXT_NOT_IN_LIST,this));
		}
			  
		menuBar.addItem(OP_TEXT_NULL,true, new SelectItemCommand(OP_TEXT_NULL,this));
 		
		if(dataType == QuestionDef.QTN_TYPE_TEXT ){	  
			menuBar.addItem(OP_TEXT_STARTS_WITH,true,new SelectItemCommand(OP_TEXT_STARTS_WITH,this));	  
			menuBar.addItem(OP_TEXT_NOT_START_WITH,true, new SelectItemCommand(OP_TEXT_NOT_START_WITH,this));	  
			menuBar.addItem(OP_TEXT_CONTAINS,true,new SelectItemCommand(OP_TEXT_CONTAINS,this));	  
			menuBar.addItem(OP_TEXT_NOT_CONTAIN,true, new SelectItemCommand(OP_TEXT_NOT_CONTAIN,this));
		}
		 
		ScrollPanel scrollPanel = new ScrollPanel();
		scrollPanel.setWidget(menuBar);
		scrollPanel.setWidth("300px");
		scrollPanel.setHeight((Window.getClientHeight() - getAbsoluteTop() - 25)+"px");
		
		popup.setWidget(scrollPanel);
	}
	
	public void onItemSelected(Object sender, Object item) {
		if(sender instanceof SelectItemCommand){
			popup.hide();
			setText((String)item);
			itemSelectionListener.onItemSelected(this, fromOperatorText2Value((String)item));
		}
	}
	
	private Byte fromOperatorText2Value(String text){
		if(text.equals(OP_TEXT_EQUAL))
			return ModelConstants.OPERATOR_EQUAL;
		else if(text.equals(OP_TEXT_NOT_EQUAL))
			return ModelConstants.OPERATOR_NOT_EQUAL;
		else if(text.equals(OP_TEXT_LESS_THAN))
			return ModelConstants.OPERATOR_LESS;
		else if(text.equals(OP_TEXT_LESS_THAN_EQUAL))
			return ModelConstants.OPERATOR_LESS_EQUAL;
		else if(text.equals(OP_TEXT_GREATER_THAN))
			return ModelConstants.OPERATOR_GREATER;
		else if(text.equals(OP_TEXT_GREATER_THAN_EQUAL))
			return ModelConstants.OPERATOR_GREATER_EQUAL;
		else if(text.equals(OP_TEXT_NULL))
			return ModelConstants.OPERATOR_IS_NULL;
		else if(text.equals(OP_TEXT_IN_LIST))
			return ModelConstants.OPERATOR_IN_LIST;
		else if(text.equals(OP_TEXT_NOT_IN_LIST))
			return ModelConstants.OPERATOR_NOT_IN_LIST;
		else if(text.equals(OP_TEXT_STARTS_WITH))
			return ModelConstants.OPERATOR_STARTS_WITH;
		else if(text.equals(OP_TEXT_NOT_START_WITH))
			return ModelConstants.OPERATOR_NOT_START_WITH;
		else if(text.equals(OP_TEXT_CONTAINS))
			return ModelConstants.OPERATOR_CONTAINS;
		else if(text.equals(OP_TEXT_NOT_CONTAIN))
			return ModelConstants.OPERATOR_NOT_CONTAIN;
		else if(text.equals(OP_TEXT_BETWEEN))
			return ModelConstants.OPERATOR_BETWEEN;
		else if(text.equals(OP_TEXT_NOT_BETWEEN))
			return ModelConstants.OPERATOR_NOT_BETWEEN;
		return ModelConstants.OPERATOR_NULL;
	}
	
	public void onStartItemSelection(Object sender){
		
	}
	
	public void setOperator(int operator){
		String operatorText = null;
		
		if(operator == ModelConstants.OPERATOR_EQUAL)
			operatorText = OP_TEXT_EQUAL;
		else if(operator == ModelConstants.OPERATOR_NOT_EQUAL)
			operatorText = OP_TEXT_NOT_EQUAL;
		else if(operator == ModelConstants.OPERATOR_LESS)
			operatorText = OP_TEXT_LESS_THAN;
		else if(operator == ModelConstants.OPERATOR_LESS_EQUAL)
			operatorText = OP_TEXT_LESS_THAN_EQUAL;
		else if(operator == ModelConstants.OPERATOR_GREATER)
			operatorText = OP_TEXT_GREATER_THAN;
		else if(operator == ModelConstants.OPERATOR_GREATER_EQUAL)
			operatorText = OP_TEXT_GREATER_THAN_EQUAL;
		else if(operator == ModelConstants.OPERATOR_IS_NULL)
			operatorText = OP_TEXT_NULL;
		else if(operator == ModelConstants.OPERATOR_IN_LIST)
			operatorText = OP_TEXT_IN_LIST;
		else if(operator == ModelConstants.OPERATOR_NOT_IN_LIST)
			operatorText = OP_TEXT_NOT_IN_LIST;
		else if(operator == ModelConstants.OPERATOR_STARTS_WITH)
			operatorText = OP_TEXT_STARTS_WITH;
		else if(operator == ModelConstants.OPERATOR_NOT_START_WITH)
			operatorText = OP_TEXT_NOT_START_WITH;
		else if(operator == ModelConstants.OPERATOR_CONTAINS)
			operatorText = OP_TEXT_CONTAINS;
		else if(operator == ModelConstants.OPERATOR_NOT_CONTAIN)
			operatorText = OP_TEXT_NOT_CONTAIN;
		else if(operator == ModelConstants.OPERATOR_BETWEEN)
			operatorText = OP_TEXT_BETWEEN;
		else if(operator == ModelConstants.OPERATOR_NOT_BETWEEN)
			operatorText = OP_TEXT_NOT_BETWEEN;
		
		setText(operatorText);
	}
}
