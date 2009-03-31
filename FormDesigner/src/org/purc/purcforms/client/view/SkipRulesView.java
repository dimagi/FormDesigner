package org.purc.purcforms.client.view;

import java.util.Vector;

import org.purc.purcforms.client.controller.IConditionController;
import org.purc.purcforms.client.model.Condition;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.PageDef;
import org.purc.purcforms.client.model.ModelConstants;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.model.SkipRule;
import org.purc.purcforms.client.widget.skiprule.ConditionWidget;
import org.purc.purcforms.client.widget.skiprule.GroupHyperlink;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author daniel
 *
 */
public class SkipRulesView extends Composite implements IConditionController{

	private static final int HORIZONTAL_SPACING = 5;
	private static final int VERTICAL_SPACING = 0;

	private VerticalPanel verticalPanel = new VerticalPanel();
	private Hyperlink addConditionLink = new Hyperlink("< Click here to add new condition >",null);
	private GroupHyperlink groupHyperlink = new GroupHyperlink(GroupHyperlink.CONDITIONS_OPERATOR_TEXT_ALL,null);

	private FormDef formDef;
	private QuestionDef questionDef;
	private SkipRule skipRule;
	private boolean enabled;
	private RadioButton rdEnable = new RadioButton("action","Enable");
	private RadioButton rdDisable = new RadioButton("action","Disable");
	private RadioButton rdShow = new RadioButton("action","Show");
	private RadioButton rdHide = new RadioButton("action","Hide");
	private CheckBox chkMakeRequired = new CheckBox("Make Required");
	private Label lblAction = new Label("For question: ");

	public SkipRulesView(){
		setupWidgets();
	}

	private void setupWidgets(){
		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setSpacing(HORIZONTAL_SPACING);

		HorizontalPanel actionPanel = new HorizontalPanel();
		actionPanel.add(rdEnable);
		actionPanel.add(rdDisable);
		actionPanel.add(rdShow);
		actionPanel.add(rdHide);
		actionPanel.add(chkMakeRequired);
		actionPanel.setSpacing(10);

		verticalPanel.add(lblAction);
		//verticalPanel.add(new Label(" "));
		verticalPanel.add(actionPanel);
		//verticalPanel.add(new Label(" "));

		//verticalPanel.setCellHeight(lblAction, "30px");
		//verticalPanel.setCellHeight(actionPanel, "30px");

		horizontalPanel.add(new Label("When"));
		horizontalPanel.add(groupHyperlink);
		horizontalPanel.add(new Label("of the following apply"));
		verticalPanel.add(horizontalPanel);

		//verticalPanel.add(new ConditionWidget(FormDefTest.getPatientFormDef(),this));
		verticalPanel.add(addConditionLink);

		addConditionLink.addClickListener(new ClickListener(){
			public void onClick(Widget sender){
				addCondition();
			}
		});

		rdEnable.addClickListener(new ClickListener(){
			public void onClick(Widget sender){
				updateMakeRequired();
			}
		});
		rdDisable.addClickListener(new ClickListener(){
			public void onClick(Widget sender){
				updateMakeRequired();
			}
		});
		rdShow.addClickListener(new ClickListener(){
			public void onClick(Widget sender){
				updateMakeRequired();
			}
		});
		rdHide.addClickListener(new ClickListener(){
			public void onClick(Widget sender){
				updateMakeRequired();
			}
		});

		verticalPanel.setSpacing(VERTICAL_SPACING);
		initWidget(verticalPanel);
	}

	private void updateMakeRequired(){
		chkMakeRequired.setEnabled(rdEnable.isChecked() || rdShow.isChecked());
		if(!chkMakeRequired.isEnabled())
			chkMakeRequired.setChecked(false);
	}

	public void addCondition(){
		if(formDef != null && enabled){
			verticalPanel.remove(addConditionLink);
			ConditionWidget conditionWidget = new ConditionWidget(formDef,this,true,questionDef);
			//conditionWidget.setQuestionDef(questionDef);
			verticalPanel.add(conditionWidget);
			verticalPanel.add(addConditionLink);

			if(!(rdEnable.isChecked()||rdDisable.isChecked()||rdShow.isChecked()||rdHide.isChecked()))
				rdEnable.setChecked(true);
		}
	}

	public void addBracket(){

	}

	public void deleteCondition(ConditionWidget conditionWidget){
		if(skipRule != null)
			skipRule.removeCondition(conditionWidget.getCondition());
		verticalPanel.remove(conditionWidget);
	}

	public void updateSkipRule(){
		if(questionDef == null){
			skipRule = null;
			return;
		}

		if(skipRule == null)
			skipRule = new SkipRule();

		int count = verticalPanel.getWidgetCount();
		for(int i=0; i<count; i++){
			Widget widget = verticalPanel.getWidget(i);
			if(widget instanceof ConditionWidget){
				Condition condition = ((ConditionWidget)widget).getCondition();
				//if(!(rdEnable.isChecked() || rdShow.isChecked()))
				//	condition.setOperator(getInvertedOperator(condition.getOperator()));

				if(condition != null && !skipRule.containsCondition(condition))
					skipRule.addCondition(condition);
				else if(condition != null && skipRule.containsCondition(condition))
					skipRule.updateCondition(condition);
			}
		}

		if(skipRule.getConditions() == null)
			skipRule = null;
		else{
			if(!skipRule.containsActionTarget(questionDef.getId()))
				skipRule.addActionTarget(questionDef.getId());
			skipRule.setConditionsOperator(groupHyperlink.getConditionsOperator());
			skipRule.setAction(getAction());
		}

		if(skipRule != null && !formDef.containsSkipRule(skipRule))
			formDef.addSkipRule(skipRule);
	}

	private int getAction(){
		int action = 0;
		if(rdEnable.isChecked())
			action |= ModelConstants.ACTION_ENABLE;
		else if(rdShow.isChecked())
			action |= ModelConstants.ACTION_SHOW;
		else if(rdHide.isChecked())
			action |= ModelConstants.ACTION_HIDE;
		else
			action |= ModelConstants.ACTION_DISABLE;

		if(chkMakeRequired.isChecked())
			action |= ModelConstants.ACTION_MAKE_MANDATORY;
		else
			action |= ModelConstants.ACTION_MAKE_OPTIONAL;

		return action;
	}

	private void setAction(int action){
		rdEnable.setChecked((action & ModelConstants.ACTION_ENABLE) != 0);
		rdDisable.setChecked((action & ModelConstants.ACTION_DISABLE) != 0);
		rdShow.setChecked((action & ModelConstants.ACTION_SHOW) != 0);
		rdHide.setChecked((action & ModelConstants.ACTION_HIDE) != 0);
		chkMakeRequired.setChecked((action & ModelConstants.ACTION_MAKE_MANDATORY) != 0);
		updateMakeRequired();
	}

	public void setQuestionDef(QuestionDef questionDef){
		clearConditions();

		if(questionDef.getParent() instanceof PageDef)
			formDef = ((PageDef)questionDef.getParent()).getParent();
		else
			formDef = ((PageDef)((QuestionDef)questionDef.getParent()).getParent()).getParent();

		if(questionDef != null)
			lblAction.setText("For question:  " + questionDef.getText());
		else
			lblAction.setText("For question: ");

		this.questionDef = questionDef;

		skipRule = formDef.getSkipRule(questionDef);
		if(skipRule != null){
			groupHyperlink.setCondionsOperator(skipRule.getConditionsOperator());
			setAction(skipRule.getAction());
			verticalPanel.remove(addConditionLink);
			Vector conditions = skipRule.getConditions();
			Vector lostConditions = new Vector();
			for(int i=0; i<conditions.size(); i++){
				ConditionWidget conditionWidget = new ConditionWidget(formDef,this,true,questionDef);
				if(conditionWidget.setCondition((Condition)conditions.elementAt(i)))
					verticalPanel.add(conditionWidget);
				else
					lostConditions.add((Condition)conditions.elementAt(i));
			}
			for(int i=0; i<lostConditions.size(); i++)
				skipRule.removeCondition((Condition)lostConditions.elementAt(i));
			if(skipRule.getConditionCount() == 0){
				formDef.removeSkipRule(skipRule);
				skipRule = null;
			}

			verticalPanel.add(addConditionLink);
		}
	}

	public void setFormDef(FormDef formDef){
		updateSkipRule();
		this.formDef = formDef;
		this.questionDef = null;
		clearConditions();
	}

	private void clearConditions(){
		if(questionDef != null)
			updateSkipRule();
		
		questionDef = null;
		lblAction.setText("For question: ");
		
		while(verticalPanel.getWidgetCount() > 4)
			verticalPanel.remove(verticalPanel.getWidget(3));

		rdEnable.setChecked(false);
		rdDisable.setChecked(false);
		rdShow.setChecked(false);
		rdHide.setChecked(false);
		chkMakeRequired.setChecked(false);
		updateMakeRequired();
	}

	public void setEnabled(boolean enabled){
		this.enabled = enabled;
		
		groupHyperlink.setEnabled(enabled);
		
		rdEnable.setEnabled(enabled);
		rdDisable.setEnabled(enabled);
		rdShow.setEnabled(enabled);
		rdHide.setEnabled(enabled);
		chkMakeRequired.setEnabled(enabled);
		
		if(!enabled)
			clearConditions();
	}

	/*private int getInvertedOperator(int operator){
		if(operator == ModelConstants.OPERATOR_EQUAL)
			return ModelConstants.OPERATOR_NOT_EQUAL;
		else if(operator == ModelConstants.OPERATOR_NOT_EQUAL)
			return ModelConstants.OPERATOR_EQUAL;
		else if(operator == ModelConstants.OPERATOR_LESS)
			return ModelConstants.OPERATOR_GREATER;
		else if(operator == ModelConstants.OPERATOR_LESS_EQUAL)
			return ModelConstants.OPERATOR_GREATER_EQUAL;
		else if(operator == ModelConstants.OPERATOR_GREATER)
			return ModelConstants.OPERATOR_LESS;
		else if(operator == ModelConstants.OPERATOR_GREATER_EQUAL)
			return ModelConstants.OPERATOR_LESS_EQUAL;
		else if(operator == ModelConstants.OPERATOR_IS_NULL)
			return ModelConstants.OPERATOR_IS_NOT_NULL;
		else if(operator == ModelConstants.OPERATOR_IN_LIST)
			return ModelConstants.OPERATOR_NOT_IN_LIST;
		else if(operator == ModelConstants.OPERATOR_NOT_IN_LIST)
			return ModelConstants.OPERATOR_IN_LIST;
		else if(operator == ModelConstants.OPERATOR_STARTS_WITH)
			return ModelConstants.OPERATOR_NOT_START_WITH;
		else if(operator == ModelConstants.OPERATOR_NOT_START_WITH)
			return ModelConstants.OPERATOR_STARTS_WITH;
		else if(operator == ModelConstants.OPERATOR_CONTAINS)
			return ModelConstants.OPERATOR_NOT_CONTAIN;
		else if(operator == ModelConstants.OPERATOR_NOT_CONTAIN)
			return ModelConstants.OPERATOR_CONTAINS;
		else if(operator == ModelConstants.OPERATOR_BETWEEN)
			return ModelConstants.OPERATOR_NOT_BETWEEN;
		else if(operator == ModelConstants.OPERATOR_NOT_BETWEEN)
			return ModelConstants.OPERATOR_BETWEEN;

		return ModelConstants.OPERATOR_NOT_EQUAL;
	}*/
}
