package org.purc.purcforms.client.widget.skiprule;

import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.ModelConstants;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;


/**
 * Widget used to display the conditions grouping operators (all,any,none,notAll)
 * for skip and validation conditions.
 *  
 * @author daniel
 *
 */
public class GroupHyperlink extends Hyperlink{

	/** The conditions grouping operator text: all */
	public static final String CONDITIONS_OPERATOR_TEXT_ALL = LocaleText.get("all");
	
	/** The conditions grouping operator text: any */
	public static final String CONDITIONS_OPERATOR_TEXT_ANY = LocaleText.get("any");
	
	/** The conditions grouping operator text: none */
	public static final String CONDITIONS_OPERATOR_TEXT_NONE = LocaleText.get("none");
	
	/** The conditions grouping operator text: not all */
	public static final String CONDITIONS_OPERATOR_TEXT_NOT_ALL= LocaleText.get("notAll");

	/** The panel to dispay the grouping operators. */
	private PopupPanel popup;
	
	/** Flag to tell if we are enabled or not. */
	private boolean enabled;

	
	/**
	 * Creates a new instance of the conditions grouping operator.
	 * 
	 * @param text the default display text.
	 * @param targetHistoryToken the history token to which it will link.
	 */
	public GroupHyperlink(String text, String targetHistoryToken){
		super(text,targetHistoryToken);

		DOM.sinkEvents(getElement(), DOM.getEventsSunk(getElement()) | Event.ONMOUSEDOWN );
	}

	@Override
	public void onBrowserEvent(Event event) {
		if (DOM.eventGetType(event) == Event.ONMOUSEDOWN && enabled) {
			setupPopup();
			popup.setPopupPosition(event.getClientX(), event.getClientY());
			popup.show();
		}
	}

	/**
	 * Creates the conditions group operator popup.
	 */
	private void setupPopup(){
		popup = new PopupPanel(true,true);

		MenuBar menuBar = new MenuBar(true);
		menuBar.addItem(CONDITIONS_OPERATOR_TEXT_ALL,true, new Command(){
			public void execute() {popup.hide(); setText(CONDITIONS_OPERATOR_TEXT_ALL);}});

		menuBar.addSeparator();		  
		menuBar.addItem(CONDITIONS_OPERATOR_TEXT_ANY,true, new Command(){
			public void execute() {popup.hide(); setText(CONDITIONS_OPERATOR_TEXT_ANY);}});

		menuBar.addSeparator();		  
		menuBar.addItem(CONDITIONS_OPERATOR_TEXT_NONE,true,new Command(){
			public void execute() {popup.hide(); setText(CONDITIONS_OPERATOR_TEXT_NONE);}});

		menuBar.addSeparator();		  
		menuBar.addItem(CONDITIONS_OPERATOR_TEXT_NOT_ALL,true, new Command(){
			public void execute() {popup.hide(); setText(CONDITIONS_OPERATOR_TEXT_NOT_ALL);}});

		popup.setWidget(menuBar);
	}

	/**
	 * Gets the currently selected conditions grouping operator.
	 * 
	 * @return the operator value. Can only be ModelConstants.CONDITIONS_OPERATOR_AND,
	 *        ModelConstants.CONDITIONS_OPERATOR_OR or ModelConstants.CONDITIONS_OPERATOR_NULL
	 */
	public byte getConditionsOperator(){
		if(getText().equals(CONDITIONS_OPERATOR_TEXT_ALL))
			return ModelConstants.CONDITIONS_OPERATOR_AND;
		else if(getText().equals(CONDITIONS_OPERATOR_TEXT_ANY))
			return ModelConstants.CONDITIONS_OPERATOR_OR;

		return ModelConstants.CONDITIONS_OPERATOR_NULL;
	}

	
	/**
	 * Sets the conditions grouping operator.
	 * 
	 * @param operator the operator value. Should be ModelConstants.CONDITIONS_OPERATOR_AND,
	 *        ModelConstants.CONDITIONS_OPERATOR_OR or ModelConstants.CONDITIONS_OPERATOR_NULL
	 */
	public void setCondionsOperator(int operator){
		if(operator == ModelConstants.CONDITIONS_OPERATOR_AND)
			setText(CONDITIONS_OPERATOR_TEXT_ALL);
		else if(operator == ModelConstants.CONDITIONS_OPERATOR_OR)
			setText(CONDITIONS_OPERATOR_TEXT_ANY);	
	}

	/** 
	 * Sets whether we enable this widgets or not.
	 * 
	 * @param enable set to true to enable, else false.
	 */
	public void setEnabled(boolean enable){
		this.enabled = enable;
	}
}
