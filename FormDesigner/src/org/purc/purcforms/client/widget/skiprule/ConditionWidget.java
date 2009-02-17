package org.purc.purcforms.client.widget.skiprule;

import org.purc.purcforms.client.controller.ItemSelectionListener;
import org.purc.purcforms.client.model.Condition;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.PurcConstants;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.view.SkipRulesView;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;


/**
 * 
 * @author daniel
 *
 */
public class ConditionWidget extends Composite implements ItemSelectionListener{

	private static final int HORIZONTAL_SPACING = 5;
	
	private FormDef formDef;
	private FieldWidget fieldWidget;
	private OperatorHyperlink operatorHyperlink;
	private ValueWidget valueWidget = new ValueWidget();
	private HorizontalPanel horizontalPanel;
	private ActionHyperlink actionHyperlink;
	
	private QuestionDef questionDef;
	private int operator;
	private SkipRulesView view;
	private Condition condition;
	
	public ConditionWidget(FormDef formDef, SkipRulesView view){
		this.formDef = formDef;
		this.view = view;
		setupWidgets();
	}
	
	private void setupWidgets(){
		actionHyperlink = new ActionHyperlink("<>",null,this);
		fieldWidget = new FieldWidget(this);
		operatorHyperlink = new OperatorHyperlink(OperatorHyperlink.OP_TEXT_EQUAL,null,this);

		horizontalPanel = new HorizontalPanel();
		horizontalPanel.setSpacing(HORIZONTAL_SPACING);
		horizontalPanel.add(actionHyperlink);
		horizontalPanel.add(fieldWidget);
		horizontalPanel.add(operatorHyperlink);
		horizontalPanel.add(valueWidget);
		
		initWidget(horizontalPanel);
		
		fieldWidget.setFormDef(formDef);
		operator = PurcConstants.OPERATOR_EQUAL;
		valueWidget.setOperator(operator);
	}

	/**
	 * @see org.purc.purcforms.client.controller.ItemSelectionListener#ontemSelected(java.lang.Object, java.lang.Object)
	 */
	public void onItemSelected(Object sender, Object item) {
		if(sender == fieldWidget /*fieldHyperlink*/){
			questionDef = (QuestionDef)item;
			operatorHyperlink.setDataType(questionDef.getDataType());
			valueWidget.setQuestionDef(questionDef);
		}
		else if(sender == operatorHyperlink){
			operator = ((Byte)item).byteValue();
			valueWidget.setOperator(operator);
			fieldWidget.stopSelection();
		}
		else if(sender == valueWidget){
			
		}
	}
	
	public void onStartItemSelection(Object sender){
		if(sender != valueWidget)
			valueWidget.stopEdit(false); //Temporary hack to turn off edits when focus goes off the edit widget
		
		if(sender != fieldWidget)
			fieldWidget.stopSelection();
		/*if(sender == operatorHyperlink){
			fieldWidget.stopSelection();
			//operatorHyperlink.startSelection();
		}*/
	}

	public void addCondition(){
		view.addCondition();
	}
	
	public void addBracket(){
		view.addBracket();
	}

	public void deleteCurrentRow(){
		view.deleteCondition(this);
	}
	
	public Condition getCondition(){
		if(condition == null)
			condition = new Condition();
		
		condition.setQuestionId(questionDef.getId());
		condition.setOperator(operator);
		condition.setValue(valueWidget.getValue());
		
		if(condition.getValue() == null)
			return null;
		
		return condition;
	}
	
	public boolean setCondition(Condition condition){
		this.condition = condition;
		questionDef = formDef.getQuestion(condition.getQuestionId());
		if(questionDef == null)
			return false;
		
		operator = condition.getOperator();
		
		fieldWidget.setQuestion(questionDef);
		operatorHyperlink.setDataType(questionDef.getDataType());
		operatorHyperlink.setOperator(operator);
		valueWidget.setQuestionDef(questionDef);
		valueWidget.setOperator(operator);
		valueWidget.setValue(condition.getValue());
		return true;
	}
}
