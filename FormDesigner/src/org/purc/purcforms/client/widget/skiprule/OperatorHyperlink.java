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
 * Widget used to display the condition operators (eg equal to, less than, greater than, etc)
 * for skip and validation rules.
 * 
 * @author daniel
 *
 */
public class OperatorHyperlink extends Hyperlink implements ItemSelectionListener {
	
	/** The operator text: is equal to */
	public static final String OP_TEXT_EQUAL = LocaleText.get("isEqualTo");
	
	/** The operator text: is not equal to */
	public static final String OP_TEXT_NOT_EQUAL = LocaleText.get("isNotEqual");
	
	/** The operator text: is less than */
	public static final String OP_TEXT_LESS_THAN = LocaleText.get("isLessThan");
	
	/** The operator text: is less than or equal to */
	public static final String OP_TEXT_LESS_THAN_EQUAL = LocaleText.get("isLessThanOrEqual");
	
	/** The operator text: is greater than */
	public static final String OP_TEXT_GREATER_THAN = LocaleText.get("isGreaterThan");
	
	/** The operator text: is greater than or equal to */
	public static final String OP_TEXT_GREATER_THAN_EQUAL = LocaleText.get("isGreaterThanOrEqual");
	
	/** The operator text: is null */
	public static final String OP_TEXT_NULL = LocaleText.get("isNull");
	
	/** The operator text: is null */
	public static final String OP_TEXT_NOT_NULL = LocaleText.get("isNotNull");
	
	/** The operator text: is in list */
	public static final String OP_TEXT_IN_LIST = LocaleText.get("isInList");
	
	/** The operator text: is not in list */
	public static final String OP_TEXT_NOT_IN_LIST = LocaleText.get("isNotInList");
	
	/** The operator text: starts with */
	public static final String OP_TEXT_STARTS_WITH = LocaleText.get("startsWith");
	
	/** The operator text: does not start with */
	public static final String OP_TEXT_NOT_START_WITH = LocaleText.get("doesNotStartWith");
	
	/** The operator text: contains */
	public static final String OP_TEXT_CONTAINS = LocaleText.get("contains");
	
	/** The operator text: does not contain */
	public static final String OP_TEXT_NOT_CONTAIN = LocaleText.get("doesNotContain");
	
	/** The operator text: is between */
	public static final String OP_TEXT_BETWEEN = LocaleText.get("isBetween");
	
	/** The operator text: is not between */
	public static final String OP_TEXT_NOT_BETWEEN = LocaleText.get("isNotBetween");

	/** The popup to display the allowed operators for the current question data type. */
	private PopupPanel popup;
	
	/** The current question data type. */
	private int dataType =  QuestionDef.QTN_TYPE_TEXT;
	
	/** The selection change listener. */
	private ItemSelectionListener itemSelectionListener;
	
	
	/**
	 * Creates a new instance of the operator hyperlink.
	 * 
	 * @param text the default display text.
	 * @param targetHistoryToken the history token to which it will link.
	 * @param itemSelectionListener the listener to selection change events.
	 */
	public OperatorHyperlink(String text, String targetHistoryToken,ItemSelectionListener itemSelectionListener){
		super(text,targetHistoryToken);
		this.itemSelectionListener = itemSelectionListener;
		DOM.sinkEvents(getElement(), DOM.getEventsSunk(getElement()) | Event.ONMOUSEDOWN );
	}
	
	/**
	 * Sets the data type of the question to which the operator is being applied.
	 * 
	 * @param dataType the data type.
	 */
	public void setDataType(int dataType){
		this.dataType = dataType;
		
		//We set the universal operator which is valid for all questions,
		//as the one to start with or display by default.
		setText(OP_TEXT_EQUAL);
	}
	  
	@Override
	public void onBrowserEvent(Event event) {
		  if (DOM.eventGetType(event) == Event.ONMOUSEDOWN) {
			  itemSelectionListener.onStartItemSelection(this);
			  int height = setupPopup();
		      popup.setPopupPosition(event.getClientX(), event.getClientY()-height);
		      popup.show();
		  }
	}
	
	/**
	 * Creates the operator popup for the currently selected question.
	 * 
	 * @return the height of the popup.
	 */
	private int setupPopup(){
		popup = new PopupPanel(true,true);
		
		int count = 0;
		
		MenuBar menuBar = new MenuBar(true);
		
		if(!(dataType == QuestionDef.QTN_TYPE_GPS || dataType == QuestionDef.QTN_TYPE_VIDEO ||
				dataType == QuestionDef.QTN_TYPE_AUDIO || dataType == QuestionDef.QTN_TYPE_IMAGE ||
				dataType == QuestionDef.QTN_TYPE_BARCODE)){
			menuBar.addItem(OP_TEXT_EQUAL,true, new SelectItemCommand(OP_TEXT_EQUAL,this));
			menuBar.addItem(OP_TEXT_NOT_EQUAL,true, new SelectItemCommand(OP_TEXT_NOT_EQUAL,this));
			count += 2;
		}
		  
		if(dataType == QuestionDef.QTN_TYPE_DATE || dataType == QuestionDef.QTN_TYPE_DATE_TIME ||
			dataType == QuestionDef.QTN_TYPE_DECIMAL || dataType == QuestionDef.QTN_TYPE_NUMERIC ||
			dataType == QuestionDef.QTN_TYPE_TIME || dataType == QuestionDef.QTN_TYPE_REPEAT){
			
			menuBar.addItem(OP_TEXT_GREATER_THAN,true,new SelectItemCommand(OP_TEXT_GREATER_THAN,this));	  
			menuBar.addItem(OP_TEXT_GREATER_THAN_EQUAL,true, new SelectItemCommand(OP_TEXT_GREATER_THAN_EQUAL,this));	  
			menuBar.addItem(OP_TEXT_LESS_THAN,true,new SelectItemCommand(OP_TEXT_LESS_THAN,this));			  	  
			menuBar.addItem(OP_TEXT_LESS_THAN_EQUAL,true, new SelectItemCommand(OP_TEXT_LESS_THAN_EQUAL,this));		  
			menuBar.addItem(OP_TEXT_BETWEEN,true,new SelectItemCommand(OP_TEXT_BETWEEN,this));	  
			menuBar.addItem(OP_TEXT_NOT_BETWEEN,true, new SelectItemCommand(OP_TEXT_NOT_BETWEEN,this));
			count += 6;
		}
		
		if(dataType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || dataType == QuestionDef.QTN_TYPE_LIST_MULTIPLE){		  
			menuBar.addItem(OP_TEXT_IN_LIST,true,new SelectItemCommand(OP_TEXT_IN_LIST,this));	  
			menuBar.addItem(OP_TEXT_NOT_IN_LIST,true, new SelectItemCommand(OP_TEXT_NOT_IN_LIST,this));
			count += 2;
		}
			  
		menuBar.addItem(OP_TEXT_NULL,true, new SelectItemCommand(OP_TEXT_NULL,this));
		menuBar.addItem(OP_TEXT_NOT_NULL,true, new SelectItemCommand(OP_TEXT_NOT_NULL,this));
 		
		if(dataType == QuestionDef.QTN_TYPE_TEXT ){	  
			menuBar.addItem(OP_TEXT_STARTS_WITH,true,new SelectItemCommand(OP_TEXT_STARTS_WITH,this));	  
			menuBar.addItem(OP_TEXT_NOT_START_WITH,true, new SelectItemCommand(OP_TEXT_NOT_START_WITH,this));	  
			menuBar.addItem(OP_TEXT_CONTAINS,true,new SelectItemCommand(OP_TEXT_CONTAINS,this));	  
			menuBar.addItem(OP_TEXT_NOT_CONTAIN,true, new SelectItemCommand(OP_TEXT_NOT_CONTAIN,this));
			count += 4;
		}
		 
		int height = count*42;
		if(height > 200)
			height = 200;
		
		ScrollPanel scrollPanel = new ScrollPanel();
		scrollPanel.setWidget(menuBar);
		scrollPanel.setWidth("300"+PurcConstants.UNITS);
		scrollPanel.setHeight(height+PurcConstants.UNITS);
		//scrollPanel.setHeight((Window.getClientHeight() - getAbsoluteTop() - 25)+PurcConstants.UNITS);
		
		popup.setWidget(scrollPanel);
		
		return height;
	}
	
	/**
	 * @see org.purc.purcforms.client.controller.ItemSelectionListener#onItemSelected(Object, Object)
	 */
	public void onItemSelected(Object sender, Object item) {
		if(sender instanceof SelectItemCommand){
			popup.hide();
			setText((String)item);
			itemSelectionListener.onItemSelected(this, fromOperatorText2Value((String)item));
		}
	}
	
	/**
	 * Converts operator text to its int representation.
	 * 
	 * @param text the operator text.
	 * @return the operator int value.
	 */
	private int fromOperatorText2Value(String text){
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
		else if(text.equals(OP_TEXT_NOT_NULL))
			return ModelConstants.OPERATOR_IS_NOT_NULL;
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
	
	/**
	 * @see org.purc.purcforms.client.controller.ItemSelectionListener#onStartItemSelection(Object)
	 */
	public void onStartItemSelection(Object sender){
		
	}
	
	/**
	 * Converts the operator int value to its text representation.
	 * 
	 * @param operator the operator int value.
	 */
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
		else if(operator == ModelConstants.OPERATOR_IS_NOT_NULL)
			operatorText = OP_TEXT_NOT_NULL;
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
