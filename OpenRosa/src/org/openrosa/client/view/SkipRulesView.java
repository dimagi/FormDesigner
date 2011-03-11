package org.openrosa.client.view;

import java.util.List;
import java.util.Vector;

import org.openrosa.client.controller.IConditionController;
import org.openrosa.client.model.Condition;
import org.openrosa.client.model.FormDef;
import org.openrosa.client.model.IFormElement;
import org.openrosa.client.model.OptionDef;
import org.openrosa.client.model.QuestionDef;
import org.openrosa.client.model.SkipRule;
import org.openrosa.client.widget.skiprule.ConditionWidget;
import org.openrosa.client.widget.skiprule.GroupHyperlink;
import org.openrosa.client.controller.QuestionSelectionListener;
import org.openrosa.client.locale.LocaleText;
import org.openrosa.client.model.ModelConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
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
	private IFormElement questionDef;

	/** The skip rule definition object. */
	private SkipRule skipRule;

	/** Flag determining whether to enable this widget or not. */
	private boolean enabled;

	/** Widget for the skip rule action to make a question required. */
	private CheckBox chkMakeRequired = new CheckBox("Make Required");

	/** Widget for Label "for question". */
	private Label lblAction = new Label(LocaleText.get("forQuestion"));

	/** Widget for Label "and". */
	private Label lblAnd = new Label(LocaleText.get("and"));
	
	private Label lblrelevant;
	
	private TextArea txtrelevant;
	
	private CheckBox chkUseAdvancedRelevant;

	private IFormElement selectedObj;
	
	private boolean advancedMode;
	/**
	 * Creates a new instance of the skip logic widget.
	 */
	
	private HorizontalPanel p1, p2;
	private FlexTable p3;
	public SkipRulesView(){
		setupWidgets();
	}

	/**
	 * Sets up the widgets.
	 */
	private void setupWidgets(){
		
		FlexTable advanced = new FlexTable();
		lblrelevant=new Label("Relevant:");
		txtrelevant = new TextArea();
		txtrelevant.setEnabled(false);
		chkUseAdvancedRelevant = new CheckBox("Use Advanced Skip Logic Text");
		txtrelevant.setText("Not Supported yet! Does Not Work.");
		
		advanced.setWidget(0,0,new Label(""));
		advanced.setWidget(1, 0, chkUseAdvancedRelevant);
		advanced.getFlexCellFormatter().setColSpan(1, 0, 2);
		advanced.setWidget(2, 0, lblrelevant);
		advanced.setWidget(2, 1, txtrelevant);
		p3 = advanced;
		
		advanced.setCellPadding(5);
		verticalPanel.add(advanced);
		verticalPanel.setSpacing(VERTICAL_SPACING);
		
		HorizontalPanel horizontalPanel = new HorizontalPanel();
		p1 = horizontalPanel;
		horizontalPanel.setSpacing(HORIZONTAL_SPACING);
		

		Hyperlink hyperlink = new Hyperlink(LocaleText.get("clickForOtherQuestions"),"");
		hyperlink.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event){
				showOtherQuestions();
			}
		});

		HorizontalPanel horzPanel = new HorizontalPanel();
		p2 = horzPanel;
		horzPanel.setSpacing(10);
		horzPanel.add(lblAction);
		horzPanel.add(lblAnd);
		horzPanel.add(hyperlink);

		verticalPanel.add(horzPanel);
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

		verticalPanel.setSpacing(VERTICAL_SPACING);
		

		
		chkUseAdvancedRelevant.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				selectedObj.setHasAdvancedRelevant(chkUseAdvancedRelevant.getValue());
				txtrelevant.setEnabled(chkUseAdvancedRelevant.getValue());
				setAdvancedMode(chkUseAdvancedRelevant.getValue());
			}
		});
		
		txtrelevant.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				selectedObj.setAdvancedRelevant(txtrelevant.getText());
				//and if we've done that the hasAdvanced() should definitely return true so:
				selectedObj.setHasAdvancedRelevant(true);
			}
		});
		
		initWidget(verticalPanel);
		setAdvancedMode(false);
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
		GWT.log("Updating skip rule (SkipRulesView.java)");
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
		
		if(chkUseAdvancedRelevant.getValue()){
			//this means that we're using advanced mode!
			skipRule = new SkipRule(questionDef.getId(),new Vector(),ModelConstants.ACTION_NONE, new Vector()); 
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
		chkMakeRequired.setValue((action & ModelConstants.ACTION_MAKE_MANDATORY) != 0);
	}

	/**
	 * Sets the question definition object which is the target of the skip rule.
	 * For now we support only one target for the skip rule.
	 * 
	 * @param questionDef the question definition object.
	 */
	public void setQuestionDef(IFormElement questionDef){
		clearConditions();

		formDef = questionDef.getFormDef();

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
				if(conditionWidget.setCondition((Condition)conditions.elementAt(i)) && !questionDef.hasAdvancedRelevant())
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

		chkMakeRequired.setValue(false);
	}

	/**
	 * Sets whether to enable this widget or not.
	 * 
	 * @param enabled set to true to enable, else false.
	 */
	public void setEnabled(boolean enabled){
		this.enabled = enabled;

		groupHyperlink.setEnabled(enabled);

		chkMakeRequired.setEnabled(enabled);

		if(!enabled)
			clearConditions();
	}


	/**
	 * Shows a list of other questions that are targets of the current skip rule.
	 */
	private void showOtherQuestions(){
		GWT.log("ShowOtherQuestion() enabled="+enabled);
		if(enabled){
			SkipQtnsDialog dialog = new SkipQtnsDialog(this);
			dialog.setData(formDef,questionDef,skipRule);
			dialog.center();
		}
	}


	/**
	 * @see org.openrosa.client.controller.QuestionSelectionListener#onQuestionsSelected(List)
	 */
	public void onQuestionsSelected(List<String> questions){
		if(skipRule == null)
			skipRule = new SkipRule();

		//Check if we have any action targets. If we do not, just add all as new.
		List<Integer> actnTargets = skipRule.getActionTargets();
		if(actnTargets == null){
			for(String varName : questions)
				skipRule.addActionTarget(formDef.getElement(varName).getId());
			
			return;
		}

		//Remove any de selected action targets from the skip rule.
		for(int index = 0; index < actnTargets.size(); index++){
			Integer qtnId = actnTargets.get(index);

			QuestionDef qtnDef = formDef.getQuestion(qtnId);
			if(qtnDef == questionDef)
				continue; //Ignore the question for which we are editing the skip rule.

			if(qtnDef == null || !questions.contains(qtnDef.getBinding())){
				actnTargets.remove(index);
				index = index - 1;
			}
		}
		
		//Add any newly added questions as action targets.
		for(String varName : questions){
			IFormElement qtnDef = formDef.getElement(varName);
			if(!skipRule.containsActionTarget(qtnDef.getId()))
				skipRule.addActionTarget(qtnDef.getId());
		}
	}
	
	/**
	 * This entire class should not support selecting multiple questions
	 * @param senderWidget
	 * @param item
	 */
	public void onItemSelected(Object senderWidget, Object item){
		this.selectedObj = (IFormElement)item; //should always be an IFormElement or we're in trouble.
		
		//select the checkboxes according to the flags set in the selected items.
		//unless they're of the type that can't have any kind of bind logic
		if(selectedObj instanceof FormDef || selectedObj instanceof OptionDef){
			chkUseAdvancedRelevant.setEnabled(false);
			chkUseAdvancedRelevant.setValue(false);
			this.setAdvancedMode(false);
			this.setEnabled(false);
		}else{
			chkUseAdvancedRelevant.setEnabled(true);
			chkUseAdvancedRelevant.setValue(selectedObj.hasAdvancedRelevant());
			if(chkUseAdvancedRelevant.getValue()){
				groupHyperlink.setEnabled(false);
				chkMakeRequired.setEnabled(false);
				txtrelevant.setEnabled(true);
				this.setEnabled(false);
				txtrelevant.setText(selectedObj.getAdvancedRelevant());
				setAdvancedMode(true);
			}else{
				groupHyperlink.setEnabled(true);
				chkMakeRequired.setEnabled(true);
				txtrelevant.setEnabled(false);
				this.setEnabled(true);
				setAdvancedMode(false);
			}
			
			setAdvancedMode(chkUseAdvancedRelevant.getValue());
		
		}
		
	}
	
	
	public void setAdvancedMode(boolean enabled){
		this.advancedMode = enabled;
		groupHyperlink.setEnabled(!enabled);
		p1.setVisible(!enabled);
		p2.setVisible(!enabled);
		lblrelevant.setVisible(enabled);
		txtrelevant.setVisible(enabled);
		this.setEnabled(!enabled);
		addConditionLink.setVisible(!enabled);
		
	}
	
	public boolean isAdvancedMode(){
		return advancedMode;
	}

}
