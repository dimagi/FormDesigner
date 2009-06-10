package org.purc.purcforms.client.view;

import org.purc.purcforms.client.controller.FilterRowActionListener;
import org.purc.purcforms.client.controller.IConditionController;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.Condition;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.model.SkipRule;
import org.purc.purcforms.client.widget.ActionHyperlink;
import org.purc.purcforms.client.widget.ConditionWidget;
import org.purc.purcforms.client.widget.GroupHyperlink;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author daniel
 *
 */
public class FilterConditionsView  extends Composite implements IConditionController, FilterRowActionListener{

	private static final int HORIZONTAL_SPACING = 5;
	private static final int VERTICAL_SPACING = 0;
	
	
	private VerticalPanel verticalPanel = new VerticalPanel();
	private Hyperlink addConditionLink = new Hyperlink(LocaleText.get("clickToAddNewCondition"),null);
	private GroupHyperlink groupHyperlink = new GroupHyperlink(GroupHyperlink.CONDITIONS_OPERATOR_TEXT_ALL,null);
	private ActionHyperlink actionHyperlink;
	
	private FormDef formDef;
	private QuestionDef questionDef;
	private SkipRule skipRule;
	private boolean enabled = true;
	
	public FilterConditionsView(){
		setupWidgets();
	}
	
	private void setupWidgets(){
		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setSpacing(HORIZONTAL_SPACING);

		actionHyperlink = new ActionHyperlink("<>",null,this);
		
		horizontalPanel.add(new Label("Choose records where")); //LocaleText.get("when")
		horizontalPanel.add(groupHyperlink);
		horizontalPanel.add(new Label(LocaleText.get("ofTheFollowingApply")));
		verticalPanel.add(horizontalPanel);

		//verticalPanel.add(new ConditionWidget(FormDefTest.getPatientFormDef(),this));
		verticalPanel.add(addConditionLink);

		addConditionLink.addClickListener(new ClickListener(){
			public void onClick(Widget sender){
				addCondition();
			}
		});
		
		verticalPanel.setSpacing(VERTICAL_SPACING);
		initWidget(verticalPanel);
	}
	
	public void addCondition(){
		if(formDef != null && enabled){
			verticalPanel.remove(addConditionLink);
			ConditionWidget conditionWidget = new ConditionWidget(formDef,this,true,questionDef);
			//conditionWidget.setQuestionDef(questionDef);
			verticalPanel.add(conditionWidget);
			verticalPanel.add(addConditionLink);

		}
	}

	public void addBracket(){

	}
	
	public void deleteCurrentRow(){
		
	}

	public void deleteCondition(ConditionWidget conditionWidget){
		if(skipRule != null)
			skipRule.removeCondition(conditionWidget.getCondition());
		verticalPanel.remove(conditionWidget);
	}
	
	public void setFormDef(FormDef formDef){
		updateSkipRule();
		this.formDef = formDef;
		this.questionDef = null;
		clearConditions();
		verticalPanel.add(addConditionLink);
	}
	
	private void clearConditions(){
		if(questionDef != null)
			updateSkipRule();
		
		questionDef = null;
		
		while(verticalPanel.getWidgetCount() > 1)
			verticalPanel.remove(verticalPanel.getWidget(1));
	}
	
	public void updateSkipRule(){
		//if(questionDef == null){
		//	skipRule = null;
		//	return;
		//}

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
			//if(!skipRule.containsActionTarget(questionDef.getId()))
			//	skipRule.addActionTarget(questionDef.getId());
			skipRule.setConditionsOperator(groupHyperlink.getConditionsOperator());
			//skipRule.setAction(getAction());
		}

		if(skipRule != null && !formDef.containsSkipRule(skipRule))
			formDef.addSkipRule(skipRule);
	}
	
	public FormDef getFormDef(){
		return formDef;
	}
	
	public SkipRule getSkipRule(){
		return skipRule;
	}
}
