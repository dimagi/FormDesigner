package org.purc.purcforms.client.view;

import java.util.Vector;

import org.purc.purcforms.client.controller.IConditionController;
import org.purc.purcforms.client.model.Condition;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.PageDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.model.ValidationRule;
import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.widget.skiprule.ConditionWidget;
import org.purc.purcforms.client.widget.skiprule.GroupHyperlink;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author daniel
 *
 */
public class ValidationRulesView extends Composite implements IConditionController{

	private static final int HORIZONTAL_SPACING = 5;
	private static final int VERTICAL_SPACING = 0;
	
	private VerticalPanel verticalPanel = new VerticalPanel();
	private Hyperlink addConditionLink = new Hyperlink("< Click here to add new condition >",null);
	private GroupHyperlink groupHyperlink = new GroupHyperlink(GroupHyperlink.CONDITIONS_OPERATOR_TEXT_ALL,null);
	
	private FormDef formDef;
	private QuestionDef questionDef;
	private ValidationRule validationRule;
	private boolean enabled;
	private TextBox txtErrorMessage = new TextBox();

	private Label lblAction = new Label("Question: ");
	
	public ValidationRulesView(){
		setupWidgets();
	}
	
	private void setupWidgets(){
		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setSpacing(HORIZONTAL_SPACING);
		
		HorizontalPanel actionPanel = new HorizontalPanel();
		actionPanel.setWidth("100%");
		FormDesignerUtil.maximizeWidget(txtErrorMessage);
		actionPanel.add(new Label("Error Message"));
		actionPanel.add(txtErrorMessage);
		actionPanel.setSpacing(10);
		
		verticalPanel.add(lblAction);
		verticalPanel.add(actionPanel);
		
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
		
		
		verticalPanel.setSpacing(VERTICAL_SPACING);
		initWidget(verticalPanel);
	}
	
	public void addCondition(){
		if(formDef != null){
			verticalPanel.remove(addConditionLink);
			ConditionWidget conditionWidget = new ConditionWidget(formDef,this,false);
			conditionWidget.setQuestionDef(questionDef);
			verticalPanel.add(conditionWidget);
			verticalPanel.add(addConditionLink);
		}
	}
	
	public void addBracket(){
		
	}
	
	public void deleteCondition(ConditionWidget conditionWidget){
		if(validationRule != null)
			validationRule.removeCondition(conditionWidget.getCondition());
		verticalPanel.remove(conditionWidget);
	}
	
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
				
				if(condition != null && !validationRule.containsCondition(condition))
					validationRule.addCondition(condition);
				else if(condition != null && validationRule.containsCondition(condition))
					validationRule.updateCondition(condition);
			}
		}
		
		if(validationRule.getConditions() == null)
			validationRule = null;
		else
			validationRule.setConditionsOperator(groupHyperlink.getConditionsOperator());
		
		if(validationRule != null && !formDef.containsValidationRule(validationRule))
			formDef.addValidationRule(validationRule);
	}
	
	public void setQuestionDef(QuestionDef questionDef){
		clearConditions();
		
		if(questionDef.getParent() instanceof PageDef)
			formDef = ((PageDef)questionDef.getParent()).getParent();
		else
			formDef = ((PageDef)((QuestionDef)questionDef.getParent()).getParent()).getParent();
		
		if(questionDef != null)
			lblAction.setText("Question:  " + questionDef.getText() + "  is valid when:");
		else
			lblAction.setText("Question: ");
		
		this.questionDef = questionDef;
		
		validationRule = formDef.getValidationRule(questionDef);
		if(validationRule != null){
			groupHyperlink.setCondionsOperator(validationRule.getConditionsOperator());
			txtErrorMessage.setText(validationRule.getErrorMessage());
			verticalPanel.remove(addConditionLink);
			Vector conditions = validationRule.getConditions();
			Vector lostConditions = new Vector();
			for(int i=0; i<conditions.size(); i++){
				ConditionWidget conditionWidget = new ConditionWidget(formDef,this,false);
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
		}
	}
	
	public void setFormDef(FormDef formDef){
		updateValidationRule();
		this.formDef = formDef;
		this.questionDef = null;
		clearConditions();
	}
	
	private void clearConditions(){
		if(questionDef != null)
			updateValidationRule();
		questionDef = null;
		
		while(verticalPanel.getWidgetCount() > 4)
			verticalPanel.remove(verticalPanel.getWidget(3));
		
		txtErrorMessage.setText(null);
	}
	
	public void setEnabled(boolean enabled){
		this.enabled = enabled;
		this.groupHyperlink.setEnabled(enabled);
		
		if(!enabled)
			clearConditions();
	}
	
	public void onWindowResized(int width, int height){
		if(width - 700 > 0)
			txtErrorMessage.setWidth(width - 700 + "px");
	}
}
