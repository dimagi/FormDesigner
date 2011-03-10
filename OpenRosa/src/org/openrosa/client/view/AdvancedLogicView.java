package org.openrosa.client.view;

import org.openrosa.client.model.FormDef;
import org.openrosa.client.model.IFormElement;
import org.openrosa.client.model.OptionDef;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;

public class AdvancedLogicView extends Composite {

	/** Labels for the TextAreas below **/
	private Label lblcalculate;
	
	/**
	 * The text areas where the user can manually enter logic
	 */
	private TextArea txtcalculate;
	
	/** Used to set the use Advanced text flags in IFormElements **/
	private CheckBox chkUseAdvancedCalculate;
	
	private IFormElement selectedObj;
	
	
	public AdvancedLogicView(){
		setupWidgets();
		setupHandlers();
	}
	
	/**
	 * Sets up the widgets.
	 */
	private void setupWidgets(){
		FlexTable table = new FlexTable();
		txtcalculate = new TextArea(); 
		txtcalculate.setText("Not supported yet!");
		chkUseAdvancedCalculate = new CheckBox("Use Advanced Calculate Text");
		
		//Set all to false intially
		chkUseAdvancedCalculate.setValue(false);
		txtcalculate.setEnabled(false);
		
		//REQUIRED
		table.setWidget(4, 0, chkUseAdvancedCalculate);
		table.getFlexCellFormatter().setColSpan(4, 0, 2);
		table.setWidget(5, 0, lblcalculate);
		table.setWidget(5, 1, txtcalculate);
		
		initWidget(table);
	}
	
	private void setupHandlers(){
				
		chkUseAdvancedCalculate.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			public void onValueChange(ValueChangeEvent<Boolean> event) {
//				selectedObj.setHasAdvancedCalculate(event.getValue()); 
				txtcalculate.setEnabled(event.getValue());
			}
		});
		
		txtcalculate.addChangeHandler(new ChangeHandler() {
			
			public void onChange(ChangeEvent event) {
//				selectedObj.setAdvancedConstraint(txtcalculate.getText());
			}
		});
	}
	
	public void onItemSelected(Object senderWidget, Object item){
		this.selectedObj = (IFormElement)item; //should always be an IFormElement or we're in trouble.
		
		//select the checkboxes according to the flags set in the selected items.
		//unless they're of the type that can't have any kind of bind logic
		if(selectedObj instanceof FormDef || selectedObj instanceof OptionDef){
			chkUseAdvancedCalculate.setEnabled(false);
			chkUseAdvancedCalculate.setValue(false);
		}else{
			chkUseAdvancedCalculate.setEnabled(true);
//			chkUseAdvancedCalculate.setValue(selectedObj.getHasAdvancedCalculate());
		}
	}
}
