package org.openrosa.client.view;

import org.openrosa.client.model.Calculation;
import org.openrosa.client.model.FormDef;
import org.openrosa.client.model.IFormElement;
import org.openrosa.client.model.OptionDef;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
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
	

	
	private IFormElement selectedObj;
	private FormDef formDef;
	private Calculation currentCalc;
	
	/**
	 * Used to determine if this widget can be used or not
	 * For example, if an OptionDef is selected, this widget is not valid.
	 */
	private boolean disabled;
	
	
	public AdvancedLogicView(){
		disabled = false;
		setupWidgets();
		setupHandlers();
	}
	
	/**
	 * Sets up the widgets.
	 */
	private void setupWidgets(){
		FlexTable table = new FlexTable();
		txtcalculate = new TextArea(); 
		txtcalculate.setText("");
		
		//Set all to false intially
		txtcalculate.setEnabled(false);
		
		lblcalculate = new Label("Calculate Expression: ");
		table.setWidget(5, 0, lblcalculate);
		table.setWidget(5, 1, txtcalculate);
		
		initWidget(table);
	}
	
	private void setupHandlers(){
				
		
		txtcalculate.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				updateQuestionFromTextBox();
			}
		});
	}
	
	public void onItemSelected(Object senderWidget, Object item){
		this.selectedObj = (IFormElement)item; //should always be an IFormElement or we're in trouble.
		
		this.formDef = selectedObj.getFormDef();
		
		//select the checkboxes according to the flags set in the selected items.
		//unless they're of the type that can't have any kind of bind logic
		if(selectedObj instanceof FormDef || selectedObj instanceof OptionDef){
			disabled = true;
			txtcalculate.setEnabled(false);
		}else{
			disabled = false;
			txtcalculate.setEnabled(true);
		}
		updateTextBoxFromQuestion();
	}
	
	/**
	 * Fills the textbox with the existing calculate expression
	 * (if any) for this question.  If no calculate exists, it will be created
	 * and initialized with an empty string.
	 */
	private void updateTextBoxFromQuestion(){
		if(disabled){ return ; }
		createCalculateForQuestion();
		txtcalculate.setText(currentCalc.getCalculateExpression());
	}
	
	/**
	 * Grabs the text entered into the textbox and
	 * updates the stored expression in Calculation 
	 * (stored by the FormDef).
	 */
	private void updateQuestionFromTextBox(){
		if(disabled){ return ; }
		String expr = txtcalculate.getText();
		currentCalc.setCalculateExpression(expr);
	}
	
	/**
	 * Creates a new calculate for a question and
	 * adds it to the list of calculates maintained by the formDef.
	 * If a calculate for this question already exists, does nothing.
	 */
	private void createCalculateForQuestion(){
		if(disabled){ return; }
		Calculation calc = formDef.getCalculation(selectedObj);
		if(calc == null){
			calc = new Calculation(selectedObj.getId(), "");
			formDef.addCalculation(calc);
		}else{
			return;
		}
		
		currentCalc = calc;
	}
	
	public void setFormDef(FormDef formDef){
		this.formDef = formDef;
	}
}
