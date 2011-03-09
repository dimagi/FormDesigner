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
	private Label lblrelevant, lblconstraint, lblrequired;
	
	/**
	 * The text areas where the user can manually enter logic
	 */
	private TextArea txtrelevant,txtconstraint,txtrequired;
	
	/** Used to set the use Advanced text flags in IFormElements **/
	private CheckBox chkUseAdvancedRelevant, chkUseAdvancedConstraint, chkUseAdvancedRequired;
	public AdvancedLogicView(){
		setupWidgets();
		setupHandlers();
	}
	
	private IFormElement selectedObj;
	
	/**
	 * Sets up the widgets.
	 */
	private void setupWidgets(){
		FlexTable table = new FlexTable();
		
		lblrelevant=new Label("Relevant:");
		lblconstraint = new Label("Constraint:");
		lblrequired = new Label("Required:");
		
		txtrelevant = new TextArea();
		txtconstraint = new TextArea();
		txtrequired = new TextArea();
		
		chkUseAdvancedConstraint = new CheckBox("Use Advanced Constraint Text");
		chkUseAdvancedRelevant = new CheckBox("Use Advanced Relevant Text");
		chkUseAdvancedRequired = new CheckBox("Use Advanced Required Text");
		
		//Set all to false intially
		chkUseAdvancedConstraint.setValue(false);
		chkUseAdvancedRelevant.setValue(false);
		chkUseAdvancedRequired.setValue(false);
		
		txtconstraint.setEnabled(false);
		txtrelevant.setEnabled(false);
		txtrequired.setEnabled(false);
		
		//CONSTRAINT
		table.setWidget(0, 0, chkUseAdvancedConstraint);
		table.getFlexCellFormatter().setColSpan(0, 0, 2);
		table.setWidget(1, 0, lblconstraint);
		table.setWidget(1, 1, txtconstraint);
		
		//RELEVANT
		table.setWidget(2, 0, chkUseAdvancedRelevant);
		table.getFlexCellFormatter().setColSpan(2, 0, 2);
		table.setWidget(3, 0, lblrelevant);
		table.setWidget(3, 1, txtrelevant);
		
		//REQUIRED
		table.setWidget(4, 0, chkUseAdvancedRequired);
		table.getFlexCellFormatter().setColSpan(4, 0, 2);
		table.setWidget(5, 0, lblrequired);
		table.setWidget(5, 1, txtrequired);
		
		initWidget(table);
	}
	
	private void setupHandlers(){
		chkUseAdvancedConstraint.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
//				selectedObj.setHasAdvancedConstraint(event.getValue()); 
				txtconstraint.setEnabled(event.getValue());
			}
		});
		
		chkUseAdvancedRelevant.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
//				selectedObj.setHasAdvancedRelevant(event.getValue());
				txtrelevant.setEnabled(event.getValue());
			}
		});
		
		chkUseAdvancedRequired.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
//				selectedObj.setHasAdvancedRequired(event.getValue()); 
				txtrequired.setEnabled(event.getValue());
			}
		});
		
		txtconstraint.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
//				selectedObj.setAdvancedConstraint(txtconstraint.getText());
			}
		});
		
		txtrelevant.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
//				selectedObj.setAdvancedConstraint(txtrelevant.getText());
			}
		});
		
		txtrequired.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
//				selectedObj.setAdvancedConstraint(txtrequired.getText());
			}
		});
	}
	
	
	public void onItemSelected(Object senderWidget, Object item){
		this.selectedObj = (IFormElement)item; //should always be an IFormElement or we're in trouble.
		
		//select the checkboxes according to the flags set in the selected items.
		//unless they're of the type that can't have any kind of bind logic
		if(selectedObj instanceof FormDef || selectedObj instanceof OptionDef){
			chkUseAdvancedConstraint.setEnabled(false);
			chkUseAdvancedRelevant.setEnabled(false);
			chkUseAdvancedRequired.setEnabled(false);
			chkUseAdvancedConstraint.setValue(false);
			chkUseAdvancedRelevant.setValue(false);
			chkUseAdvancedRequired.setValue(false);
		}else{
			chkUseAdvancedConstraint.setEnabled(true);
			chkUseAdvancedRelevant.setEnabled(true);
			chkUseAdvancedRequired.setEnabled(true);
//			chkUseAdvancedConstraint.setValue(selectedObj.getHasAdvancedConstraint());
//			chkUseAdvancedRelevant.setValue(selectedObj.getHasAdvancedRelevant());
//			chkUseAdvancedRequired.setValue(selectedObj.getHasAdvancedRequired());
			//fill the text areas with data from the selected item
//			txtconstraint.setText(selectedObj.getadvancedconstrainttxt);
//			txtrequired.setText(selectedObj.getadvancedrequiredtxt);
//			txtrelevant.setText(selectedObj.getadvancedrelevanttxt);
		}
		
		
		
	}
}
