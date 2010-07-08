package org.purc.purcforms.client.widget.skiprule;

import org.purc.purcforms.client.controller.IConditionController;
import org.purc.purcforms.client.controller.ItemSelectionListener;
import org.purc.purcforms.client.model.Condition;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.ModelConstants;
import org.purc.purcforms.client.model.QuestionDef;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;


/**
 * This widget is used to display a skip or validation rule widget.
 * 
 * @author daniel
 *
 */
public class ConditionWidget extends Composite implements ItemSelectionListener{

	/** Value for horizontal panel spacing. */
	private static final int HORIZONTAL_SPACING = 5;

	/** The form that this condition belongs to. */
	private FormDef formDef;
	
	/** The field selection widget. */
	private FieldWidget fieldWidget;
	
	/** The operator selection widget. */
	private OperatorHyperlink operatorHyperlink;
	
	/** The value selection or entry widget. */
	private ValueWidget valueWidget = new ValueWidget();
	
	private HorizontalPanel horizontalPanel;
	
	/** The condition action widget. */
	private ActionHyperlink actionHyperlink;

	/** The question that this widget condition references. */
	private QuestionDef questionDef;
	
	/** The selected operator for the condition. */
	private int operator;
	
	/** Listener to condition events. */
	private IConditionController view;
	
	/** The condition that this widget references. */
	private Condition condition;
	
	//private Label lbLabel = new Label(LocaleText.get("value"));
	
	/** The function selection widget for condition. */
	FunctionHyperlink funcHyperlink;
	
	/** The selected validation function. Could be Length or just Value. */
	private int function = ModelConstants.FUNCTION_VALUE;

	/** Flag determining whether we should allow field selection for the condition.
	 *  Skip rule conditions have filed selection while validation rules normally
	 *  just have values. eg skip rule (have allowFieldSelection = true) may be Current Pregnancy question is skipped 
	 *  when Sex field = Male, while validation (have allowFieldSelection = false) for the current say Weight question
	 *  is valid when Value > 0
	 */
	private boolean allowFieldSelection = false;

	
	/**
	 * Creates a new instance of the condition widget.
	 * 
	 * @param formDef the form whose condition this widget represents.
	 * @param view listener to condition events.
	 * @param allowFieldSelection a flag to determine if we should allow selection for the condition.
	 * @param questionDef the question that this condition references.
	 */
	public ConditionWidget(FormDef formDef, IConditionController view, boolean allowFieldSelection, QuestionDef questionDef){
		this.formDef = formDef;
		this.view = view;
		this.allowFieldSelection = allowFieldSelection;
		this.questionDef = questionDef;
		setupWidgets();
	}

	/**
	 * Creates the condition widgets.
	 */
	private void setupWidgets(){
		actionHyperlink = new ActionHyperlink("<>","",this,allowFieldSelection);

		if(allowFieldSelection){
			fieldWidget = new FieldWidget(this);
			fieldWidget.setQuestion(questionDef);
		}

		operatorHyperlink = new OperatorHyperlink(OperatorHyperlink.OP_TEXT_EQUAL,"",this);
		funcHyperlink = new FunctionHyperlink(FunctionHyperlink.FUNCTION_TEXT_VALUE,"",this);
		
		horizontalPanel = new HorizontalPanel();
		horizontalPanel.setSpacing(HORIZONTAL_SPACING);
		horizontalPanel.add(actionHyperlink);

		if(allowFieldSelection)
			horizontalPanel.add(fieldWidget);
		else
			horizontalPanel.add(funcHyperlink);

		horizontalPanel.add(operatorHyperlink);
		horizontalPanel.add(valueWidget);

		initWidget(horizontalPanel);

		//This should be before the next line as fieldWidget.setFormDef() will set questionDef to a new value of the condition instead of parent.
		valueWidget.setParentQuestionDef(questionDef);
		
		if(allowFieldSelection)
			fieldWidget.setFormDef(formDef);
		
		valueWidget.setFormDef(formDef);

		operator = ModelConstants.OPERATOR_EQUAL;
		valueWidget.setOperator(operator);
	}

	/**
	 * @see org.purc.purcforms.client.controller.ItemSelectionListener#onItemSelected(java.lang.Object, java.lang.Object)
	 */
	public void onItemSelected(Object sender, Object item) {
		if(sender == fieldWidget /*fieldHyperlink*/){
			questionDef = (QuestionDef)item;
			//operatorHyperlink.setDataType(questionDef.getDataType());
			setOperatorDataType(questionDef);
			valueWidget.setQuestionDef(questionDef);
		}
		else if(sender == operatorHyperlink){
			operator = ((Integer)item).intValue();
			valueWidget.setOperator(operator);

			if(allowFieldSelection)
				fieldWidget.stopSelection();
		}
		else if(sender == funcHyperlink){
			function = ((Integer)item).intValue();
			valueWidget.setFunction(function);
			setOperatorDataType(questionDef);
			//operatorHyperlink.setDataType(function == ModelConstants.FUNCTION_LENGTH ? QuestionDef.QTN_TYPE_NUMERIC : questionDef.getDataType());
		}
		else if(sender == valueWidget){
			
		}
	}

	/**
	 * @see org.purc.purcforms.client.controller.ItemSelectionListener#onStartItemSelection(Object)
	 */
	public void onStartItemSelection(Object sender){
		if(sender != valueWidget)
			valueWidget.stopEdit(false); //Temporary hack to turn off edits when focus goes off the edit widget

		if(allowFieldSelection && sender != fieldWidget)
			fieldWidget.stopSelection();
	}

	/**
	 * Adds a new condition.
	 */
	public void addCondition(){
		view.addCondition();
	}

	/**
	 * Adds a new bracket or condition grouping.
	 */
	public void addBracket(){
		view.addBracket();
	}

	/**
	 * Deletes this condition.
	 */
	public void deleteCurrentRow(){
		view.deleteCondition(this);
	}

	/**
	 * Gets the condition for this widget.
	 * 
	 * @return the condition object.
	 */
	public Condition getCondition(){
		if(condition == null)
			condition = new Condition();

		condition.setQuestionId(questionDef.getId());
		condition.setOperator(operator);
		condition.setValue(valueWidget.getValue());
		condition.setValueQtnDef(valueWidget.getValueQtnDef());
		condition.setFunction(function);

		if(condition.getValue() == null)
			return null;

		return condition;
	}

	/**
	 * Sets the condition for this widget.
	 * 
	 * @param condition the condition object.
	 * @return true if the question referenced by the condition exists, else false.
	 */
	public boolean setCondition(Condition condition){
		this.condition = condition;
		questionDef = formDef.getQuestion(condition.getQuestionId());
		if(questionDef == null)
			return false;

		setQuestionDef(questionDef);

		return true;
	}

	/**
	 * Sets the question for this widget.
	 * 
	 * @param questionDef the question id definition object.
	 */
	public void setQuestionDef(QuestionDef questionDef){
		this.questionDef = questionDef;
		
		/*//operatorHyperlink.setDataType(questionDef.getDataType());
		setOperatorDataType(questionDef);*/
		
		//if(allowFieldSelection)
			valueWidget.setQuestionDef(questionDef);
		
		/*//operatorHyperlink.setDataType(questionDef.getDataType());
		setOperatorDataType(questionDef);*/

		setOperatorDataType(questionDef);
		
		if(condition != null){
			operator = condition.getOperator();
			function = condition.getFunction();
			setOperatorDataType(questionDef);

			if(allowFieldSelection)
				fieldWidget.setQuestion(questionDef);

			funcHyperlink.setFunction(function);
			operatorHyperlink.setOperator(operator);
			valueWidget.setOperator(operator);
			valueWidget.setValueQtnDef(condition.getValueQtnDef()); //Should be set before value such that value processing finds it.
			valueWidget.setValue(condition.getValue());
		}
	}
	
	
	private void setOperatorDataType(QuestionDef questionDef){
		operatorHyperlink.setDataType(function == ModelConstants.FUNCTION_LENGTH ? QuestionDef.QTN_TYPE_NUMERIC : questionDef.getDataType());
	}
}
