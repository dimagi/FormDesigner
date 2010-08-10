package org.purc.purcforms.client.view;

import java.util.HashMap;

import org.purc.purcforms.client.controller.ConditionController;
import org.purc.purcforms.client.controller.FilterRowActionListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.Condition;
import org.purc.purcforms.client.model.FilterCondition;
import org.purc.purcforms.client.model.FilterConditionGroup;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.sql.XmlBuilder;
import org.purc.purcforms.client.widget.AddConditionHyperlink;
import org.purc.purcforms.client.widget.ConditionActionHyperlink;
import org.purc.purcforms.client.widget.ConditionWidget;
import org.purc.purcforms.client.widget.GroupHyperlink;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;


/**
 * 
 * @author daniel
 *
 */
public class FilterConditionsView  extends Composite implements ConditionController, FilterRowActionListener{

	private static final int HORIZONTAL_SPACING = 5;
	private static final int VERTICAL_SPACING = 5;


	private VerticalPanel verticalPanel = new VerticalPanel();
	private AddConditionHyperlink addConditionAnchor = new AddConditionHyperlink(LocaleText.get("clickToAddNewCondition"), "#", 1);
	private GroupHyperlink groupHyperlink = new GroupHyperlink(GroupHyperlink.CONDITIONS_OPERATOR_TEXT_ALL ,1);
	private ConditionActionHyperlink actionHyperlink;

	private FormDef formDef;
	private QuestionDef questionDef;
	private boolean enabled = true;


	public FilterConditionsView(){
		setupWidgets();
	}

	private void setupWidgets(){
		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setSpacing(HORIZONTAL_SPACING);

		actionHyperlink = new ConditionActionHyperlink("<>", "#", false, 1, addConditionAnchor, this);

		horizontalPanel.add(actionHyperlink);
		horizontalPanel.add(new Label("Choose records where")); //LocaleText.get("when")
		horizontalPanel.add(groupHyperlink);
		horizontalPanel.add(new Label(LocaleText.get("ofTheFollowingApply")));
		verticalPanel.add(horizontalPanel);

		addConditionAnchor.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event){
				addCondition((Widget)event.getSource());
			}
		});

		verticalPanel.setSpacing(VERTICAL_SPACING);
		initWidget(verticalPanel);
	}

	public ConditionWidget addCondition(Widget sender){
		ConditionWidget conditionWidget = null;
		
		if(formDef != null && enabled){
			Widget widget = conditionWidget = new ConditionWidget(formDef,this,true,questionDef,1,addConditionAnchor);
			int index = verticalPanel.getWidgetIndex(sender);
			if(index == -1){
				AddConditionHyperlink addConditionHyperlink = (AddConditionHyperlink)sender;
				if(sender instanceof ConditionActionHyperlink)
					addConditionHyperlink = ((ConditionActionHyperlink)sender).getAddConditionHyperlink();

				index = verticalPanel.getWidgetIndex(addConditionHyperlink);
				if(index == -1)
					index = verticalPanel.getWidgetIndex(addConditionHyperlink.getParent());

				HorizontalPanel horizontalPanel = new HorizontalPanel();
				int depth = addConditionHyperlink.getDepth();
				horizontalPanel.add(getSpace(depth));
				conditionWidget = new ConditionWidget(formDef,this,true,questionDef,depth,addConditionHyperlink);
				horizontalPanel.add(conditionWidget);
				widget = horizontalPanel;
			}

			verticalPanel.insert(widget, index);
		}
		
		return conditionWidget;
	}

	public ConditionActionHyperlink addBracket(Widget sender, String operator, boolean addCondition){
		int depth = ((ConditionActionHyperlink)sender).getDepth() + 1;

		int index = verticalPanel.getWidgetIndex(((ConditionActionHyperlink)sender).getAddConditionHyperlink());
		if(index == -1)
			index = verticalPanel.getWidgetIndex(((ConditionActionHyperlink)sender).getAddConditionHyperlink().getParent());

		AddConditionHyperlink addConditionLink = new AddConditionHyperlink(LocaleText.get("clickToAddNewCondition") , "#", depth);
		ConditionActionHyperlink actionHyperlink = new ConditionActionHyperlink("<>", "#",true,depth,addConditionLink,this);

		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setSpacing(HORIZONTAL_SPACING);
		if(depth > 2)
			horizontalPanel.add(getSpace3(depth-1));
		horizontalPanel.add(new CheckBox());
		horizontalPanel.add(actionHyperlink);

		GroupHyperlink groupHyperlink = new GroupHyperlink(operator != null ? operator : GroupHyperlink.CONDITIONS_OPERATOR_TEXT_ALL, depth);
		horizontalPanel.add(groupHyperlink);
		horizontalPanel.add(new Label(LocaleText.get("ofTheFollowingApply")));

		verticalPanel.insert(horizontalPanel, index);

		if(addCondition){
			horizontalPanel = new HorizontalPanel();
			horizontalPanel.add(getSpace(depth));
			horizontalPanel.add(new ConditionWidget(formDef,this,true,questionDef,depth,addConditionLink));
			verticalPanel.insert(horizontalPanel, ++index);
		}

		horizontalPanel = new HorizontalPanel();
		horizontalPanel.add(getSpace2(depth));
		horizontalPanel.add(addConditionLink);
		verticalPanel.insert(horizontalPanel, ++index);

		addConditionLink.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event){
				addCondition((Widget)event.getSource());
			}
		});

		return actionHyperlink;
	}

	public void deleteCurrentRow(Widget sender){
		int startIndex = verticalPanel.getWidgetIndex(sender.getParent());

		ConditionActionHyperlink actionHyperlink = (ConditionActionHyperlink)sender;
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
		addAddConditionLink();
	}

	public void addAddConditionLink(){
		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setSpacing(HORIZONTAL_SPACING);
		horizontalPanel.add(addConditionAnchor);
		verticalPanel.add(horizontalPanel);
	}

	private void clearConditions(){
		questionDef = null;
		while(verticalPanel.getWidgetCount() > 1)
			verticalPanel.remove(verticalPanel.getWidget(1));
	}

	public FilterConditionGroup getFilterConditionRows(){

		HashMap<String,FilterConditionGroup> groupDepth = new HashMap<String,FilterConditionGroup>();

		FilterConditionGroup retGroup = new FilterConditionGroup();
		retGroup.setConditionsOperator(groupHyperlink.getConditionsOperator());
		groupDepth.put(""+groupHyperlink.getDepth()+"", retGroup);

		int count = verticalPanel.getWidgetCount();
		for(int i=1; i<count; i++)
			getFilterConditionRow((HorizontalPanel)verticalPanel.getWidget(i),groupDepth);

		return retGroup;
	}

	private void getFilterConditionRow(HorizontalPanel horizontalPanel,HashMap<String,FilterConditionGroup> groupDepth){
		for(int index = 0; index < horizontalPanel.getWidgetCount(); index++){
			Widget widget = horizontalPanel.getWidget(index);
			if(widget instanceof ConditionWidget){
				ConditionWidget conditionWidget = (ConditionWidget)widget;
				Condition condition = conditionWidget.getCondition();
				if(condition == null)
					return;

				QuestionDef questionDef = formDef.getQuestion(condition.getQuestionId());
				if(questionDef == null)
					return;

				FilterCondition row = new FilterCondition();
				row.setFieldName(getFieldName(questionDef));
				row.setFirstValue(condition.getValue());
				row.setSecondValue(condition.getSecondValue());
				row.setOperator(condition.getOperator());
				row.setDataType(questionDef.getDataType());
				groupDepth.get(((ConditionWidget)widget).getDepth()+"").addCondition(row);
				return;
			}
			else if(widget instanceof GroupHyperlink){
				GroupHyperlink groupHyperlink = (GroupHyperlink)widget;
				FilterConditionGroup row = new FilterConditionGroup();
				row.setConditionsOperator(groupHyperlink.getConditionsOperator());
				groupDepth.put(""+groupHyperlink.getDepth()+"", row);
				groupDepth.get((groupHyperlink.getDepth()-1)+"").addCondition(row);
				return;
			}
		}
	}

	private static String getFieldName(QuestionDef questionDef){
		int index = questionDef.getBinding().lastIndexOf('/');
		if(index > -1)
			return questionDef.getBinding().substring(index+1);
		return questionDef.getBinding();
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

	public void loadQueryDef(String xml){
		Document doc = XMLParser.parse(xml);
		Element rootNode = doc.getDocumentElement();
		if(!rootNode.getNodeName().equalsIgnoreCase(XmlBuilder.NODE_NAME_QUERYDEF))
			return;
		
		NodeList nodes = rootNode.getElementsByTagName(XmlBuilder.NODE_NAME_FILTER_CONDITIONS);
		if(nodes == null || nodes.getLength() == 0)
			return;
		
		rootNode = (Element)nodes.item(0);
		nodes = rootNode.getChildNodes();
		for(int index = 0; index < nodes.getLength(); index++){
			Node node = nodes.item(index);
			if(node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equalsIgnoreCase(XmlBuilder.NODE_NAME_GROUP)){
				clearConditions();
				groupHyperlink.setText(((Element)node).getAttribute(XmlBuilder.ATTRIBUTE_NAME_OPERATOR));
				addAddConditionLink();
				loadConditions((Element)node,actionHyperlink);
				break;
			}
		}
	}

	private void loadConditions(Element element,ConditionActionHyperlink actionHyperlink){
		NodeList nodes = element.getChildNodes();
		for(int index = 0; index < nodes.getLength(); index++){
			Node node = nodes.item(index);
			if(node.getNodeType() == Node.ELEMENT_NODE){
				if(node.getNodeName().equalsIgnoreCase(XmlBuilder.NODE_NAME_GROUP))
					loadConditions((Element)node,addBracket(actionHyperlink,element.getAttribute(XmlBuilder.ATTRIBUTE_NAME_OPERATOR),false));
				else if(node.getNodeName().equalsIgnoreCase(XmlBuilder.NODE_NAME_CONDITION)){
					ConditionWidget conditionWidget = addCondition(actionHyperlink);
					conditionWidget.setQuestionDef(formDef.getQuestion(((Element)node).getAttribute(XmlBuilder.ATTRIBUTE_NAME_FIELD)));
					conditionWidget.setOparator(Integer.parseInt(((Element)node).getAttribute(XmlBuilder.ATTRIBUTE_NAME_OPERATOR)));
					conditionWidget.setValue(((Element)node).getAttribute(XmlBuilder.ATTRIBUTE_NAME_VALUE));
				}
			}
		}
	}
}
