package org.purc.purcforms.client.view;

import java.util.ArrayList;
import java.util.List;

import org.purc.purcforms.client.controller.FilterRowActionListener;
import org.purc.purcforms.client.controller.IConditionController;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.Condition;
import org.purc.purcforms.client.model.FilterCondition;
import org.purc.purcforms.client.model.FilterConditionGroup;
import org.purc.purcforms.client.model.FilterConditionRow;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.widget.ActionHyperlink;
import org.purc.purcforms.client.widget.AddConditionHyperlink;
import org.purc.purcforms.client.widget.ConditionWidget;
import org.purc.purcforms.client.widget.GroupHyperlink;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
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
	private static final int VERTICAL_SPACING = 5;
	
	
	private VerticalPanel verticalPanel = new VerticalPanel();
	private AddConditionHyperlink addConditionLink = new AddConditionHyperlink(LocaleText.get("clickToAddNewCondition"),null,1);
	private GroupHyperlink groupHyperlink = new GroupHyperlink(GroupHyperlink.CONDITIONS_OPERATOR_TEXT_ALL,null);
	
	private FormDef formDef;
	private QuestionDef questionDef;
	private boolean enabled = true;
	
	
	public FilterConditionsView(){
		setupWidgets();
	}
	
	private void setupWidgets(){
		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setSpacing(HORIZONTAL_SPACING);

		ActionHyperlink actionHyperlink = new ActionHyperlink("<>",null,false,1,addConditionLink,this);
		
		horizontalPanel.add(actionHyperlink);
		horizontalPanel.add(new Label("Choose records where")); //LocaleText.get("when")
		horizontalPanel.add(groupHyperlink);
		horizontalPanel.add(new Label(LocaleText.get("ofTheFollowingApply")));
		verticalPanel.add(horizontalPanel);

		//verticalPanel.add(new ConditionWidget(FormDefTest.getPatientFormDef(),this));
		/*horizontalPanel = new HorizontalPanel();
		horizontalPanel.setSpacing(HORIZONTAL_SPACING);
		horizontalPanel.add(addConditionLink);
		verticalPanel.add(horizontalPanel);*/

		addConditionLink.addClickListener(new ClickListener(){
			public void onClick(Widget sender){
				addCondition(sender);
			}
		});
		
		verticalPanel.setSpacing(VERTICAL_SPACING);
		initWidget(verticalPanel);
	}
	
	public void addCondition(Widget sender){
		if(formDef != null && enabled){
			/*verticalPanel.remove(addConditionLink);
			ConditionWidget conditionWidget = new ConditionWidget(formDef,this,true,questionDef);
			verticalPanel.add(conditionWidget);
			verticalPanel.add(addConditionLink);*/
			
			Widget conditionWidget = new ConditionWidget(formDef,this,true,questionDef,1,addConditionLink);
			int index = verticalPanel.getWidgetIndex(sender);
			if(index == -1){
				AddConditionHyperlink addConditionHyperlink = (AddConditionHyperlink)sender;
				if(sender instanceof ActionHyperlink)
					addConditionHyperlink = ((ActionHyperlink)sender).getAddConditionHyperlink();
				
				index = verticalPanel.getWidgetIndex(addConditionHyperlink);
				if(index == -1)
					index = verticalPanel.getWidgetIndex(addConditionHyperlink.getParent());
				
				HorizontalPanel horizontalPanel = new HorizontalPanel();
				int depth = addConditionHyperlink.getDepth();
				horizontalPanel.add(getSpace(depth));
				horizontalPanel.add(new ConditionWidget(formDef,this,true,questionDef,depth,addConditionHyperlink));
				conditionWidget = horizontalPanel;
			}
			verticalPanel.insert(conditionWidget, index);
		}
	}

	public void addBracket(Widget sender){
		int depth = ((ActionHyperlink)sender).getDepth() + 1;
		
		int index = verticalPanel.getWidgetIndex(((ActionHyperlink)sender).getAddConditionHyperlink());
		if(index == -1)
			index = verticalPanel.getWidgetIndex(((ActionHyperlink)sender).getAddConditionHyperlink().getParent());
		
		AddConditionHyperlink addConditionLink = new AddConditionHyperlink(LocaleText.get("clickToAddNewCondition"),null,depth);
		ActionHyperlink actionHyperlink = new ActionHyperlink("<>",null,true,depth,addConditionLink,this);
		
		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setSpacing(HORIZONTAL_SPACING);
		if(depth > 2)
			horizontalPanel.add(getSpace3(depth-1));
		horizontalPanel.add(new CheckBox());
		horizontalPanel.add(actionHyperlink);
		
		GroupHyperlink groupHyperlink = new GroupHyperlink(GroupHyperlink.CONDITIONS_OPERATOR_TEXT_ALL,null);
		horizontalPanel.add(groupHyperlink);
		horizontalPanel.add(new Label(LocaleText.get("ofTheFollowingApply")));
		
		//verticalPanel.remove(addConditionLink);
		//verticalPanel.add(horizontalPanel);
		verticalPanel.insert(horizontalPanel, index);
		
		horizontalPanel = new HorizontalPanel();
		//horizontalPanel.setSpacing(HORIZONTAL_SPACING);
		horizontalPanel.add(getSpace(depth));
		horizontalPanel.add(new ConditionWidget(formDef,this,true,questionDef,depth,addConditionLink));
		//verticalPanel.add(horizontalPanel);
		verticalPanel.insert(horizontalPanel, ++index);
		
		horizontalPanel = new HorizontalPanel();
		//horizontalPanel.setSpacing(HORIZONTAL_SPACING);
		horizontalPanel.add(getSpace2(depth));
		horizontalPanel.add(addConditionLink);
		//verticalPanel.add(horizontalPanel);
		verticalPanel.insert(horizontalPanel, ++index);
		
		//verticalPanel.add(this.addConditionLink);
		
		addConditionLink.addClickListener(new ClickListener(){
			public void onClick(Widget sender){
				addCondition(sender);
			}
		});
		
		//groupHyperlink.addCondition();
		//groupHyperlink.setAddConditionLink(addConditionLink);
	}
	
	public void deleteCurrentRow(Widget sender){
		int startIndex = verticalPanel.getWidgetIndex(sender.getParent());
		
		ActionHyperlink actionHyperlink = (ActionHyperlink)sender;
		int sendIndex = verticalPanel.getWidgetIndex(actionHyperlink.getAddConditionHyperlink().getParent());
		
		int count = sendIndex - startIndex;
		for(int index = 0; index <= count; index++)
			verticalPanel.remove(startIndex);
	}

	public void deleteCondition(Widget sender,ConditionWidget conditionWidget){
		verticalPanel.remove(conditionWidget.getParent());
	}
	
	public void setFormDef(FormDef formDef){
		this.formDef = formDef;
		this.questionDef = null;
		clearConditions();
		
		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setSpacing(HORIZONTAL_SPACING);
		horizontalPanel.add(addConditionLink);
		
		verticalPanel.add(horizontalPanel);
	}
	
	private void clearConditions(){
		
		questionDef = null;
		
		while(verticalPanel.getWidgetCount() > 1)
			verticalPanel.remove(verticalPanel.getWidget(1));
	}
	
	public FilterConditionGroup getFilterConditionRows(){
		
		FilterConditionGroup group = new FilterConditionGroup();
		group.setConditionsOperator(groupHyperlink.getConditionsOperator());

		FilterConditionGroup retGroup = group;
		
		int count = verticalPanel.getWidgetCount();
		for(int i=1; i<count; i++){
			FilterConditionRow row = getFilterConditionRow((HorizontalPanel)verticalPanel.getWidget(i));
			if(row == null)
				continue;
			
			if(row instanceof FilterCondition)
				group.addCondition(row);
			else{
				group.addCondition(row);
				group = (FilterConditionGroup)row;
			}
		}
		
		return retGroup;
	}
	
	private FilterConditionRow getFilterConditionRow(HorizontalPanel horizontalPanel){
		for(int index = 0; index < horizontalPanel.getWidgetCount(); index++){
			Widget widget = horizontalPanel.getWidget(index);
			if(widget instanceof ConditionWidget){
				ConditionWidget conditionWidget = (ConditionWidget)widget;
				Condition condition = conditionWidget.getCondition();
				if(condition == null)
					return null;
				
				QuestionDef questionDef = formDef.getQuestion(condition.getQuestionId());
				if(questionDef == null)
					return null;
				
				FilterCondition row = new FilterCondition();
				row.setFieldName(getFieldName(questionDef));
				row.setFirstValue(condition.getValue());
				row.setSecondValue(condition.getSecondValue());
				row.setOperator(condition.getOperator());
				row.setDataType(questionDef.getDataType());
				return row;
			}
			else if(widget instanceof GroupHyperlink){
				GroupHyperlink groupHyperlink = (GroupHyperlink)widget;
				FilterConditionGroup row = new FilterConditionGroup();
				row.setConditionsOperator(groupHyperlink.getConditionsOperator());
				return row;
			}
		}
		return null;
	}
	
	private static String getFieldName(QuestionDef questionDef){
		int index = questionDef.getVariableName().lastIndexOf('/');
		if(index > -1)
			return questionDef.getVariableName().substring(index+1);
		return questionDef.getVariableName();
	}
	
	public FormDef getFormDef(){
		return formDef;
	}
	
	private HTML getSpace(int depth){
		String s = "";
		for(int i = 1; i < depth; i++)
			s += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		return new HTML(s);
	}
	
	private HTML getSpace2(int depth){
		String s = "";
		for(int i = 1; i < depth; i++)
			s += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		return new HTML(s);
	}
	
	private HTML getSpace3(int depth){
		String s = "";
		for(int i = 1; i < depth; i++)
			s += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		return new HTML(s);
	}
}
