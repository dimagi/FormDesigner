package org.purc.purcforms.client.widget.skiprule;

import org.purc.purcforms.client.model.ModelConstants;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;


/**
 * 
 * @author daniel
 *
 */
public class GroupHyperlink extends Hyperlink{

	public static final String CONDITIONS_OPERATOR_TEXT_ALL = "all";
	public static final String CONDITIONS_OPERATOR_TEXT_ANY = "any";
	public static final String CONDITIONS_OPERATOR_TEXT_NONE = "none";
	public static final String CONDITIONS_OPERATOR_TEXT_NOT_ALL= "not all";
	
	private PopupPanel popup;
	private boolean enabled;
	
	public GroupHyperlink(String text, String targetHistoryToken){
		super(text,targetHistoryToken);
		  
		DOM.sinkEvents(getElement(), DOM.getEventsSunk(getElement()) | Event.ONMOUSEDOWN );
	}
	  
	public void onBrowserEvent(Event event) {
		  if (DOM.eventGetType(event) == Event.ONMOUSEDOWN && enabled) {
			  setupPopup();
		      popup.setPopupPosition(event.getClientX(), event.getClientY());
		      popup.show();
		  }
	}
	
	private void setupPopup(){
		popup = new PopupPanel(true,true);
		
		 MenuBar menuBar = new MenuBar(true);
		  menuBar.addItem("all",true, new Command(){
		    	public void execute() {popup.hide(); setText(CONDITIONS_OPERATOR_TEXT_ALL);}});
		  
		  menuBar.addSeparator();		  
		  menuBar.addItem("any",true, new Command(){
 		    	public void execute() {popup.hide(); setText(CONDITIONS_OPERATOR_TEXT_ANY);}});
		  
		  menuBar.addSeparator();		  
		  menuBar.addItem("none",true,new Command(){
 		    	public void execute() {popup.hide(); setText(CONDITIONS_OPERATOR_TEXT_NONE);}});
		  
		  menuBar.addSeparator();		  
		  menuBar.addItem("not all",true, new Command(){
 		    	public void execute() {popup.hide(); setText(CONDITIONS_OPERATOR_TEXT_NOT_ALL);}});
		  
		  popup.setWidget(menuBar);
	}
	
	public byte getConditionsOperator(){
		if(getText().equals(CONDITIONS_OPERATOR_TEXT_ALL))
			return ModelConstants.CONDITIONS_OPERATOR_AND;
		else if(getText().equals(CONDITIONS_OPERATOR_TEXT_ANY))
			return ModelConstants.CONDITIONS_OPERATOR_OR;
		
		return ModelConstants.CONDITIONS_OPERATOR_NULL;
	}
	
	public void setCondionsOperator(int operator){
		if(operator == ModelConstants.CONDITIONS_OPERATOR_AND)
			setText(CONDITIONS_OPERATOR_TEXT_ALL);
		else if(operator == ModelConstants.CONDITIONS_OPERATOR_OR)
			setText(CONDITIONS_OPERATOR_TEXT_ANY);	
	}
	
	public void setEnabled(boolean enable){
		this.enabled = enable;
		
		/*DeferredCommand.addCommand(new Command(){
	        public void execute() {
	        	setEnabled(enabled);
	        }
	      });*/
	}
}
