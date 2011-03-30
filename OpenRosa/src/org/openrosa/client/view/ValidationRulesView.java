package org.openrosa.client.view;

import java.util.Iterator;
import java.util.Vector;

import org.openrosa.client.controller.IConditionController;
import org.openrosa.client.model.Condition;
import org.openrosa.client.model.FormDef;
import org.openrosa.client.model.IFormElement;
import org.openrosa.client.model.QuestionDef;
import org.openrosa.client.model.ValidationRule;
import org.openrosa.client.widget.skiprule.ConditionWidget;
import org.openrosa.client.widget.skiprule.GroupHyperlink;
import org.openrosa.client.PurcConstants;
import org.openrosa.client.locale.LocaleText;
import org.openrosa.client.util.FormUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * This widget enables creation of validation rules.
 * 
 * @author daniel
 *
 */
public class ValidationRulesView extends Composite implements IConditionController{

	/** The widget horizontal spacing in horizontal panels. */
	private static final int HORIZONTAL_SPACING = 5;

	/** The widget vertical spacing in vertical panels. */
	private static final int VERTICAL_SPACING = 5;

	/** The main or root widget. */
	private VerticalPanel verticalPanel = new VerticalPanel();

	/** Widget for adding new conditions. */
	private Hyperlink addConditionLink = new Hyperlink(LocaleText.get("clickToAddNewCondition"),"");

	/** Widget for grouping conditions. Has all,any, none, and not all. */
	private GroupHyperlink groupHyperlink = new GroupHyperlink(GroupHyperlink.CONDITIONS_OPERATOR_TEXT_ALL,"");

	/** The form definition object that this validation rule belongs to. */
	private FormDef formDef;

	/** The question definition object which is the target of the validation rule. */
	private IFormElement questionDef;

	/** The validation rule definition object. */
	private ValidationRule validationRule;

	/** Flag determining whether to enable this widget or not. */
	private boolean enabled;

	/** Widget for the validation rule error message. */
	private TextArea txtErrorMessage;

	/** Widget for Label "Question: ". */
	private Label lblAction = new Label(LocaleText.get("question")+": " /*"Question: "*/);
	
	private HorizontalPanel regularPanel1,regularPanel2;
	private FlexTable advancedPanel;

	private TextArea advtxtconstraint;
	private Label advlblconstraint;
	private CheckBox chkUseAdvanced;
	/**
	 * Creates a new instance of the validation rule widget.
	 */
	public ValidationRulesView(){
		setupWidgets();
	}


	/**
	 * Sets up the widgets.
	 */
	private void setupWidgets(){
		advancedPanel = new FlexTable();
		advtxtconstraint = new TextArea();
		advtxtconstraint.setText("Not Implemented Yet!");
		advlblconstraint = new Label("Advanced Validation Text");
		chkUseAdvanced = new CheckBox("Use Advanced Validation Logic");
		verticalPanel.setSpacing(5);
		verticalPanel.setVisible(true);

		advancedPanel.setWidget(0, 0, advlblconstraint);
		advancedPanel.setWidget(0, 1, advtxtconstraint);
		verticalPanel.add(chkUseAdvanced);
		verticalPanel.add(advancedPanel);
		advancedPanel.setVisible(false);
		


		txtErrorMessage = new TextArea();
		regularPanel2 = new HorizontalPanel();
		regularPanel2.setVisible(true);
		regularPanel2.setWidth("100%");
//		FormUtil.maximizeWidget(txtErrorMessage);
		regularPanel2.add(new Label("Validation Error Message:"));
		regularPanel2.add(txtErrorMessage);
		regularPanel2.setSpacing(5);
		verticalPanel.add(regularPanel2);
		verticalPanel.add(lblAction);


		regularPanel1 = new HorizontalPanel();
		regularPanel1.setSpacing(HORIZONTAL_SPACING);
		regularPanel1.add(groupHyperlink);
		regularPanel1.add(new Label(LocaleText.get("ofTheFollowingApply")));
		verticalPanel.add(regularPanel1);

		//verticalPanel.add(new ConditionWidget(FormDefTest.getPatientFormDef(),this));
		verticalPanel.add(addConditionLink);


		setupHandlers();

		verticalPanel.setSpacing(VERTICAL_SPACING);
		initWidget(verticalPanel);
	}
	
	private void setupHandlers(){
		chkUseAdvanced.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				setAdvancedMode(chkUseAdvanced.getValue());
//				questionDef.setHasAdvancedConstraint(chkUseAdvanced.getValue());
			}
		});
		
		advtxtconstraint.addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent event) {
//				questionDef.setAdvancedText(advtxtconstraint.getText());
			}
		});
		
		addConditionLink.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event){
				addCondition();
			}
		});

	}
	
	private void setAdvancedMode(boolean enabled){
		advancedPanel.setVisible(enabled);
		addConditionLink.setVisible(!enabled);
		lblAction.setVisible(!enabled);
		regularPanel1.setVisible(!enabled);
		regularPanel2.setVisible(!enabled);
		setConditionWidgetVisible(!enabled);
	}
	
	private void setConditionWidgetVisible(boolean enable){

		int count = verticalPanel.getWidgetCount();
		for(int i=0; i<count; i++){
			Widget widget = verticalPanel.getWidget(i);
			if(widget instanceof ConditionWidget){
				widget.setVisible(enable);
			}
		}
	}


	/**
	 * Adds a new condition.
	 */
	public void addCondition(){
		if(formDef != null && enabled){
			verticalPanel.remove(addConditionLink);
			ConditionWidget conditionWidget = new ConditionWidget(formDef,this,false,questionDef);
			conditionWidget.setQuestionDef(questionDef);
			verticalPanel.add(conditionWidget);
			verticalPanel.add(addConditionLink);

//			txtErrorMessage.setFocus(true);

			/*String text = txtErrorMessage.getText();
			if(text != null && text.trim().length() == 0){
				txtErrorMessage.setText(LocaleText.get("errorMessage"));
				//txtErrorMessage.selectAll();
				txtErrorMessage.setFocus(true);
			}*/
		}
	}


	/**
	 * Supposed to add a bracket or nested set of related conditions which are 
	 * currently not supported.
	 */
	public void addBracket(){

	}


	/**
	 * Deletes a condition.
	 * 
	 * @param conditionWidget the widget having the condition to delete.
	 */
	public void deleteCondition(ConditionWidget conditionWidget){
		if(validationRule != null)
			validationRule.removeCondition(conditionWidget.getCondition());
		verticalPanel.remove(conditionWidget);
	}


	/**
	 * Sets or updates the values of the validation rule object from the user's widget selections.
	 */
	public void updateValidationRule(){
		if(questionDef == null){
			validationRule = null;
			return;
		}

		if(validationRule == null)
			validationRule = new ValidationRule(questionDef.getId(),formDef);

		validationRule.setErrorMessage(txtErrorMessage.getText());

		int count = verticalPanel.getWidgetCount();
		for(int i=0; i<count; i++){
			Widget widget = verticalPanel.getWidget(i);
			if(widget instanceof ConditionWidget){
				Condition condition = ((ConditionWidget)widget).getCondition();

				if(condition != null && !validationRule.containsCondition(condition) && condition.getValue() != null)
					validationRule.addCondition(condition);
				else if(condition != null && validationRule.containsCondition(condition)){
					if(condition.getValue() != null)
						validationRule.updateCondition(condition);
					else
						validationRule.removeCondition(condition);
				}
			}
		}

		if(validationRule.getConditions() == null || validationRule.getConditionCount() == 0){
			formDef.removeValidationRule(validationRule);
			validationRule = null;
		}
		else
			validationRule.setConditionsOperator(groupHyperlink.getConditionsOperator());

		if(validationRule != null && !formDef.containsValidationRule(validationRule))
			formDef.addValidationRule(validationRule);
	}


	/**
	 * Sets the question definition object which is the target of the validation rule.
	 * 
	 * @param questionDef the question definition object.
	 */
	public void setQuestionDef(IFormElement questionDef){
		clearConditions();

		if(questionDef != null)
			formDef = questionDef.getParentFormDef();
		else
			formDef = null;

		/*if(questionDef.getParent() instanceof PageDef)
			formDef = ((PageDef)questionDef.getParent()).getParent();
		else
			formDef = ((PageDef)((QuestionDef)questionDef.getParent()).getParent()).getParent();*/

		if(questionDef != null)
			lblAction.setText(questionDef.getDisplayText() + "  "+LocaleText.get("isValidWhen"));
		else
			lblAction.setText(LocaleText.get("No question selected!"));

		this.questionDef = questionDef;

		if(formDef != null){
			validationRule = formDef.getValidationRule(questionDef);
			if(validationRule != null){
				groupHyperlink.setCondionsOperator(validationRule.getConditionsOperator());
				txtErrorMessage.setText(validationRule.getErrorMessage());
				verticalPanel.remove(addConditionLink);
				Vector conditions = validationRule.getConditions();
				Vector lostConditions = new Vector();
				for(int i=0; i<conditions.size(); i++){
					ConditionWidget conditionWidget = new ConditionWidget(formDef,this,false,questionDef);
					if(conditionWidget.setCondition((Condition)conditions.elementAt(i)))
						verticalPanel.add(conditionWidget);
					else
						lostConditions.add((Condition)conditions.elementAt(i));
				}
				for(int i=0; i<lostConditions.size(); i++)
					validationRule.removeCondition((Condition)lostConditions.elementAt(i));
				if(validationRule.getConditionCount() == 0){
					formDef.removeValidationRule(validationRule);
					validationRule = null;
				}

				verticalPanel.add(addConditionLink);
				
//				chkUseAdvanced.setValue(questionDef.hasAdvancedDConstraint());
//				advtxtconstraint.setText(questionDef.advancedConstraintText());
			}
		}
	}


	/**
	 * Sets the form definition object to which this validation rule belongs.
	 * 
	 * @param formDef the form definition object.
	 */
	public void setFormDef(FormDef formDef){
		updateValidationRule();
		this.formDef = formDef;
		this.questionDef = null;
		clearConditions();
	}


	/**
	 * Removes all validation rule conditions.
	 */
	private void clearConditions(){
		if(questionDef != null)
			updateValidationRule();

		questionDef = null;
		lblAction.setText(LocaleText.get("question")+": ");
		
		Iterator widgetIter = verticalPanel.iterator();
		while(widgetIter.hasNext()){
			Widget widget = (Widget)widgetIter.next();
			if(widget instanceof ConditionWidget){
				widgetIter.remove();
			}
		}
		

		txtErrorMessage.setText(null);
	}


	/**
	 * Sets whether to enable this widget or not.
	 * 
	 * @param enabled set to true to enable, else false.
	 */
	public void setEnabled(boolean enabled){
		this.enabled = enabled;
		this.groupHyperlink.setEnabled(enabled);

		txtErrorMessage.setEnabled(enabled);

		if(!enabled)
			clearConditions();
	}


	/**
	 * @see com.google.gwt.user.client.WindowResizeListener#onWindowResized(int, int)
	 */
	public void onWindowResized(int width, int height){
//		if(width - 700 > 0)
//			txtErrorMessage.setWidth(width - 700 + PurcConstants.UNITS);
	}
}
