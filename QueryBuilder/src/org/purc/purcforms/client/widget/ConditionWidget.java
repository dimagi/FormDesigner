package org.purc.purcforms.client.widget;

import org.purc.purcforms.client.controller.FilterRowActionListener;
import org.purc.purcforms.client.controller.IConditionController;
import org.purc.purcforms.client.controller.ItemSelectionListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.Condition;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.ModelConstants;
import org.purc.purcforms.client.model.QuestionDef;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author daniel
 *
 */
public class ConditionWidget extends Composite implements ItemSelectionListener, FilterRowActionListener{

	private static final int HORIZONTAL_SPACING = 5;

	private FormDef formDef;
	private FieldWidget fieldWidget;
	private OperatorHyperlink operatorHyperlink;
	private ValueWidget valueWidget = new ValueWidget();
	private HorizontalPanel horizontalPanel;
	private ActionHyperlink actionHyperlink;
	private CheckBox chkSelect = new CheckBox();

	private QuestionDef questionDef;
	private int operator;
	private IConditionController view;
	private Condition condition;
	private Label lbLabel = new Label(LocaleText.get("value"));

	private boolean allowFieldSelection = false;
	private int depth = 1;
	
	public ConditionWidget(FormDef formDef, IConditionController view, boolean allowFieldSelection, QuestionDef questionDef, int depth,AddConditionHyperlink addConditionHyperlink){
		this.formDef = formDef;
		this.view = view;
		this.allowFieldSelection = allowFieldSelection;
		this.questionDef = questionDef;
		this.depth = depth;
		setupWidgets(addConditionHyperlink);
	}

	private void setupWidgets(AddConditionHyperlink addConditionHyperlink){
		actionHyperlink = new ActionHyperlink("<>",null,true,depth,addConditionHyperlink,this);
		chkSelect.setChecked(true);
		
		if(allowFieldSelection)
			fieldWidget = new FieldWidget(this);

		operatorHyperlink = new OperatorHyperlink(OperatorHyperlink.OP_TEXT_EQUAL,null,this);

		horizontalPanel = new HorizontalPanel();
		horizontalPanel.add(chkSelect);
		horizontalPanel.setSpacing(HORIZONTAL_SPACING);
		horizontalPanel.add(actionHyperlink);

		if(allowFieldSelection)
			horizontalPanel.add(fieldWidget);
		else{
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
				lbLabel.setText(LocaleText.get("count"));
			horizontalPanel.add(lbLabel);
		}

		horizontalPanel.add(operatorHyperlink);
		horizontalPanel.add(valueWidget);

		initWidget(horizontalPanel);

		if(allowFieldSelection)
			fieldWidget.setFormDef(formDef);
		valueWidget.setFormDef(formDef);

		operator = ModelConstants.OPERATOR_EQUAL;
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

			if(allowFieldSelection)
				fieldWidget.stopSelection();
		}
		else if(sender == valueWidget){

		}
	}

	public void onStartItemSelection(Object sender){
		if(sender != valueWidget)
			valueWidget.stopEdit(false); //Temporary hack to turn off edits when focus goes off the edit widget

		if(allowFieldSelection && sender != fieldWidget)
			fieldWidget.stopSelection();
		/*if(sender == operatorHyperlink){
			fieldWidget.stopSelection();
			//operatorHyperlink.startSelection();
		}*/
	}

	public void addCondition(Widget sender){
		view.addCondition(sender);
	}

	public void addBracket(Widget sender){
		view.addBracket(sender);
	}

	public void deleteCurrentRow(Widget sender){
		view.deleteCondition(sender,this);
	}

	public Condition getCondition(){
		if(condition == null)
			condition = new Condition();

		condition.setQuestionId(questionDef.getId());
		condition.setOperator(operator);
		condition.setValue(valueWidget.getValue());
		condition.setValueQtnDef(valueWidget.getValueQtnDef());

		if(condition.getValue() == null)
			return null;

		return condition;
	}

	public boolean setCondition(Condition condition){
		this.condition = condition;
		questionDef = formDef.getQuestion(condition.getQuestionId());
		if(questionDef == null)
			return false;

		setQuestionDef(questionDef);

		return true;
	}

	public void setQuestionDef(QuestionDef questionDef){
		this.questionDef = questionDef;
		
		operatorHyperlink.setDataType(questionDef.getDataType());
		
		//if(allowFieldSelection)
			valueWidget.setQuestionDef(questionDef);
		
		operatorHyperlink.setDataType(questionDef.getDataType());

		if(condition != null){
			operator = condition.getOperator();

			if(allowFieldSelection)
				fieldWidget.setQuestion(questionDef);


			operatorHyperlink.setOperator(operator);
			valueWidget.setOperator(operator);
			valueWidget.setValueQtnDef(condition.getValueQtnDef()); //Should be set before value such that value processing finds it.
			valueWidget.setValue(condition.getValue());
		}
	}
}
