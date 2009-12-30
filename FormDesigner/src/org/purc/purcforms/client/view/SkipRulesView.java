package org.purc.purcforms.client.view;

import java.util.List;
import java.util.Vector;

import org.purc.purcforms.client.controller.IConditionController;
import org.purc.purcforms.client.controller.QuestionSelectionListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.Condition;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.ModelConstants;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.model.SkipRule;
import org.purc.purcforms.client.widget.skiprule.ConditionWidget;
import org.purc.purcforms.client.widget.skiprule.GroupHyperlink;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * This widget enables creation of skip rules.
 * 
 * @author daniel
 *
 */
public class SkipRulesView extends Composite implements IConditionController, QuestionSelectionListener{

	/** The widget horizontal spacing in horizontal panels. */
	private static final int HORIZONTAL_SPACING = 5;

	/** The widget vertical spacing in vertical panels. */
	private static final int VERTICAL_SPACING = 0;

	/** The main or root widget. */
	private VerticalPanel verticalPanel = new VerticalPanel();

	/** Widget for adding new conditions. */
	private Hyperlink addConditionLink = new Hyperlink(LocaleText.get("clickToAddNewCondition"),"");

	/** Widget for grouping conditions. Has all,any, none, and not all. */
	private GroupHyperlink groupHyperlink = new GroupHyperlink(GroupHyperlink.CONDITIONS_OPERATOR_TEXT_ALL,"");

	/** The form definition object that this skip rule belongs to. */
	private FormDef formDef;

	/** The question definition object which is the target of the skip rule. 
	 *  As for now, the form designer supports only one skip rule target. But the
	 *  skip rule object supports an un limited number.
	 */
	private QuestionDef questionDef;

	/** The skip rule definition object. */
	private SkipRule skipRule;

	/** Flag determining whether to enable this widget or not. */
	private boolean enabled;

	/** Widget for the skip rule action to enable a question. */
	private RadioButton rdEnable = new RadioButton("action","Enable");

	/** Widget for the skip rule action to disable a question. */
	private RadioButton rdDisable = new RadioButton("action","Disable");

	/** Widget for the skip rule action to show a question. */
	private RadioButton rdShow = new RadioButton("action","Show");

	/** Widget for the skip rule action to hide a question. */
	private RadioButton rdHide = new RadioButton("action","Hide");

	/** Widget for the skip rule action to make a question required. */
	private CheckBox chkMakeRequired = new CheckBox("Make Required");

	/** Widget for Label "for question". */
	private Label lblAction = new Label(LocaleText.get("forQuestion"));

	/** Widget for Label "and". */
	private Label lblAnd = new Label(LocaleText.get("and"));


	/**
	 * Creates a new instance of the skip logic widget.
	 */
	public SkipRulesView(){
		setupWidgets();
	}

	/**
	 * Sets up the widgets.
	 */
	private void setupWidgets(){
		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setSpacing(HORIZONTAL_SPACING);

		HorizontalPanel actionPanel = new HorizontalPanel();
		actionPanel.add(rdEnable);
		actionPanel.add(rdDisable);
		actionPanel.add(rdShow);
		actionPanel.add(rdHide);
		actionPanel.add(chkMakeRequired);
		actionPanel.setSpacing(5);

		Hyperlink hyperlink = new Hyperlink(LocaleText.get("clickForOtherQuestions"),"");
		hyperlink.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event){
				showOtherQuestions();
			}
		});

		HorizontalPanel horzPanel = new HorizontalPanel();
		horzPanel.setSpacing(10);
		horzPanel.add(lblAction);
		horzPanel.add(lblAnd);
		horzPanel.add(hyperlink);

		verticalPanel.add(horzPanel);
		verticalPanel.add(actionPanel);

		horizontalPanel.add(new Label(LocaleText.get("when")));
		horizontalPanel.add(groupHyperlink);
		horizontalPanel.add(new Label(LocaleText.get("ofTheFollowingApply")));
		verticalPanel.add(horizontalPanel);

		verticalPanel.add(addConditionLink);

		addConditionLink.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event){
				addCondition();
			}
		});

		rdEnable.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event){
				updateMakeRequired();
			}
		});
		rdDisable.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event){
				updateMakeRequired();
			}
		});
		rdShow.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event){
				updateMakeRequired();
			}
		});
		rdHide.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event){
				updateMakeRequired();
			}
		});

		verticalPanel.setSpacing(VERTICAL_SPACING);
		initWidget(verticalPanel);
	}

	/**
	 * Enables the make required widget if the enable or show widget is ticked, 
	 * else disables and unticks it.
	 */
	private void updateMakeRequired(){
		chkMakeRequired.setEnabled(rdEnable.getValue() == true || rdShow.getValue() == true);
		if(!chkMakeRequired.isEnabled())
			chkMakeRequired.setValue(false);
	}

	/**
	 * Adds a new condition.
	 */
	public void addCondition(){
		if(formDef != null && enabled){
			verticalPanel.remove(addConditionLink);
			ConditionWidget conditionWidget = new ConditionWidget(formDef,this,true,questionDef);
			verticalPanel.add(conditionWidget);
			verticalPanel.add(addConditionLink);

			if(!(rdEnable.getValue() == true||rdDisable.getValue() == true||rdShow.getValue() == true||rdHide.getValue() == true)){
				rdEnable.setValue(true);
				updateMakeRequired();
			}
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
		if(skipRule != null){
			Condition condition = conditionWidget.getCondition();
			if(condition != null){
				if(skipRule.getConditionCount() == 1 && skipRule.getActionTargetCount() > 1)
					skipRule.removeActionTarget(questionDef);
				else
					skipRule.removeCondition(condition);
			}
		}
		verticalPanel.remove(conditionWidget);
	}

	/**
	 * Sets or updates the values of the skip rule object from the user's widget selections.
	 */
	public void updateSkipRule(){
		if(questionDef == null){
			skipRule = null;
			return;
		}

		if(skipRule == null)
			skipRule = new SkipRule();

		int conditionCount = 0;
		int count = verticalPanel.getWidgetCount();
		for(int i=0; i<count; i++){
			Widget widget = verticalPanel.getWidget(i);
			if(widget instanceof ConditionWidget){
				Condition condition = ((ConditionWidget)widget).getCondition();
				if(condition != null && !skipRule.containsCondition(condition))
					skipRule.addCondition(condition);
				else if(condition != null && skipRule.containsCondition(condition))
					skipRule.updateCondition(condition);
				conditionCount++;
			}
		}

		if(skipRule.getConditions() == null || conditionCount == 0)
			skipRule = null;
		else{
			skipRule.setConditionsOperator(groupHyperlink.getConditionsOperator());
			skipRule.setAction(getAction());
			
			if(!skipRule.containsActionTarget(questionDef.getId()))
				skipRule.addActionTarget(questionDef.getId());
		}

		if(skipRule != null && !formDef.containsSkipRule(skipRule))
			formDef.addSkipRule(skipRule);
	}

	/**
	 * Gets the skip rule action based on the user's widget selections.
	 * 
	 * @return the skip rule action.
	 */
	private int getAction(){
		int action = 0;
		if(rdEnable.getValue() == true)
			action |= ModelConstants.ACTION_ENABLE;
		else if(rdShow.getValue() == true)
			action |= ModelConstants.ACTION_SHOW;
		else if(rdHide.getValue() == true)
			action |= ModelConstants.ACTION_HIDE;
		else
			action |= ModelConstants.ACTION_DISABLE;

		if(chkMakeRequired.getValue() == true)
			action |= ModelConstants.ACTION_MAKE_MANDATORY;
		else
			action |= ModelConstants.ACTION_MAKE_OPTIONAL;

		return action;
	}

	/**
	 * Updates the widgets basing on a given skip rule action.
	 * 
	 * @param action the skip rule action.
	 */
	private void setAction(int action){
		rdEnable.setValue((action & ModelConstants.ACTION_ENABLE) != 0);
		rdDisable.setValue((action & ModelConstants.ACTION_DISABLE) != 0);
		rdShow.setValue((action & ModelConstants.ACTION_SHOW) != 0);
		rdHide.setValue((action & ModelConstants.ACTION_HIDE) != 0);
		chkMakeRequired.setValue((action & ModelConstants.ACTION_MAKE_MANDATORY) != 0);
		updateMakeRequired();
	}

	/**
	 * Sets the question definition object which is the target of the skip rule.
	 * For now we support only one target for the skip rule.
	 * 
	 * @param questionDef the question definition object.
	 */
	public void setQuestionDef(QuestionDef questionDef){
		clearConditions();

		formDef = questionDef.getParentFormDef();

		if(questionDef != null)
			lblAction.setText(LocaleText.get("forQuestion") + questionDef.getDisplayText());
		else
			lblAction.setText(LocaleText.get("forQuestion"));

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

	/**
	 * Sets the form definition object to which this skip rule belongs.
	 * 
	 * @param formDef the form definition object.
	 */
	public void setFormDef(FormDef formDef){
		updateSkipRule();
		this.formDef = formDef;
		this.questionDef = null;
		clearConditions();
	}

	/**
	 * Removes all skip rule conditions.
	 */
	private void clearConditions(){
		if(questionDef != null)
			updateSkipRule();

		questionDef = null;
		lblAction.setText(LocaleText.get("forQuestion"));

		while(verticalPanel.getWidgetCount() > 4)
			verticalPanel.remove(verticalPanel.getWidget(3));

		rdEnable.setValue(false);
		rdDisable.setValue(false);
		rdShow.setValue(false);
		rdHide.setValue(false);
		chkMakeRequired.setValue(false);
		updateMakeRequired();
	}

	/**
	 * Sets whether to enable this widget or not.
	 * 
	 * @param enabled set to true to enable, else false.
	 */
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


	/**
	 * Shows a list of other questions that are targets of the current skip rule.
	 */
	private void showOtherQuestions(){
		if(enabled){
			SkipQtnsDialog dialog = new SkipQtnsDialog(this);
			dialog.setData(formDef,questionDef,skipRule);
			dialog.center();
		}
	}


	/**
	 * @see org.purc.purcforms.client.controller.QuestionSelectionListener#onQuestionsSelected(List)
	 */
	public void onQuestionsSelected(List<String> questions){
		if(skipRule == null)
			skipRule = new SkipRule();

		//Check if we have any action targets. If we do not, just add all as new.
		List<Integer> actnTargets = skipRule.getActionTargets();
		if(actnTargets == null){
			for(String varName : questions)
				skipRule.addActionTarget(formDef.getQuestion(varName).getId());
			
			return;
		}

		//Remove any de selected action targets from the skip rule.
		for(int index = 0; index < actnTargets.size(); index++){
			Integer qtnId = actnTargets.get(index);

			QuestionDef qtnDef = formDef.getQuestion(qtnId);
			if(qtnDef == questionDef)
				continue; //Ignore the question for which we are editing the skip rule.

			if(qtnDef == null || !questions.contains(qtnDef.getVariableName())){
				actnTargets.remove(index);
				index = index - 1;
			}
		}
		
		//Add any newly added questions as action targets.
		for(String varName : questions){
			QuestionDef qtnDef = formDef.getQuestion(varName);
			if(!skipRule.containsActionTarget(qtnDef.getId()))
				skipRule.addActionTarget(qtnDef.getId());
		}
	}
}
