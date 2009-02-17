package org.purc.purcforms.client.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.model.RepeatQtnsDef;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.view.FormRunnerView.Images;
import org.zenika.widget.client.datePicker.DatePicker;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;

/**
 * 
 * @author daniel
 *
 */
public class RuntimeGroupWidget extends Composite{

	private final Images images;
	private RepeatQtnsDef repeatQtnsDef;
	private HashMap<String,RuntimeWidgetWrapper> widgetMap = new HashMap<String,RuntimeWidgetWrapper>();
	private EditListener editListener;
	private FlexTable table;
	private List<RuntimeWidgetWrapper> buttons = new ArrayList<RuntimeWidgetWrapper>();
	private List<RuntimeWidgetWrapper> widgets = new ArrayList<RuntimeWidgetWrapper>();
	private VerticalPanel verticalPanel = new VerticalPanel();
	private List<Element> dataNodes = new ArrayList<Element>();

	public RuntimeGroupWidget(Images images,RepeatQtnsDef repeatQtnsDef,EditListener editListener){
		this.images = images;
		this.repeatQtnsDef = repeatQtnsDef;
		this.editListener = editListener;

		table = new FlexTable();
		FormUtil.maximizeWidget(table);		
		verticalPanel.add(table);

		initWidget(verticalPanel);
		//setupEventListeners();

		//table.setStyleName("cw-FlexTable");
		this.addStyleName("purcforms-repeat-border");
	}

	//TODO The code below needs great refactoring together with PreviewView
	private RuntimeWidgetWrapper getParentWrapper(Widget widget, Element node){
		RuntimeWidgetWrapper parentWrapper = widgetMap.get(node.getAttribute("ParentBinding"));
		if(parentWrapper == null){
			QuestionDef qtn = repeatQtnsDef.getQuestion(node.getAttribute("ParentBinding"));
			if(qtn != null){
				parentWrapper = new RuntimeWidgetWrapper(widget,images.error(),editListener);
				parentWrapper.setQuestionDef(qtn);
				widgetMap.put(node.getAttribute("ParentBinding"), parentWrapper);
				addWidget(parentWrapper);
			}
		}	 
		return parentWrapper;
	}

	public void loadWidgets(NodeList nodes){
		HashMap<Integer,RuntimeWidgetWrapper> widgets = new HashMap<Integer,RuntimeWidgetWrapper>();
		int maxTabIndex = 0;

		for(int i=0; i<nodes.getLength(); i++){
			if(nodes.item(i).getNodeType() != Node.ELEMENT_NODE)
				continue;
			try{
				Element node = (Element)nodes.item(i);
				int index = loadWidget(node,widgets);
				if(index > maxTabIndex)
					maxTabIndex = index;
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}

		//We are adding widgets to the panel according to the tab index.
		for(int index = 0; index <= maxTabIndex; index++){
			RuntimeWidgetWrapper widget = widgets.get(new Integer(index));
			if(widget != null)
				addWidget(widget);
		}

		HorizontalPanel panel = new HorizontalPanel();
		panel.setSpacing(5);
		for(int index = 0; index < buttons.size(); index++)
			panel.add(buttons.get(index));
		verticalPanel.add(panel);
		FormUtil.maximizeWidget(panel);
	}

	private int loadWidget(Element node,HashMap<Integer,RuntimeWidgetWrapper> widgets){
		RuntimeWidgetWrapper parentWrapper = null;

		String s = node.getAttribute("WidgetType");
		int tabIndex = (node.getAttribute("TabIndex") != null ? Integer.parseInt(node.getAttribute("TabIndex")) : 0);

		QuestionDef questionDef = null;
		String binding = node.getAttribute("Binding");
		if(binding != null && binding.trim().length() > 0){
			questionDef = repeatQtnsDef.getQuestion(binding);
			if(questionDef != null)
				questionDef.setAnswer(questionDef.getDefaultValue()); //Just incase we are refreshing and had already set the answer
		}

		Widget widget = null;
		if(s.equalsIgnoreCase("RadioButton")){
			widget = new RadioButton(node.getAttribute("ParentBinding"),node.getAttribute("Text"));
			parentWrapper = getParentWrapper(widget,node);
			((RadioButton)widget).setTabIndex(tabIndex);
		}
		else if(s.equalsIgnoreCase("CheckBox")){
			widget = new CheckBox(node.getAttribute("Text"));
			parentWrapper = getParentWrapper(widget,node);
			((CheckBox)widget).setTabIndex(tabIndex);
		}
		else if(s.equalsIgnoreCase("Button")){
			widget = new Button(node.getAttribute("Text"));
			((Button)widget).setTabIndex(tabIndex);
		}
		else if(s.equalsIgnoreCase("ListBox")){
			widget = new ListBox(false);
			((ListBox)widget).setTabIndex(tabIndex);
		}
		else if(s.equalsIgnoreCase("TextArea")){
			widget = new TextArea();
			((TextArea)widget).setTabIndex(tabIndex);
		}
		else if(s.equalsIgnoreCase("DatePicker")){
			widget = new DatePicker();
			((DatePicker)widget).setTabIndex(tabIndex);
			/*((DatePicker)widget).addFocusListener(new FocusListenerAdapter(){
				 public void onLostFocus(Widget sender){
					 //((DatePicker)sender).selectAll();
				 }
			 });*/
		}
		else if(s.equalsIgnoreCase("TextBox")){
			widget = new TextBox();
			if(questionDef != null && (questionDef.getDataType() == QuestionDef.QTN_TYPE_NUMERIC 
					|| questionDef.getDataType() == QuestionDef.QTN_TYPE_DECIMAL))
				FormUtil.allowNumericOnly((TextBox)widget,questionDef.getDataType() == QuestionDef.QTN_TYPE_DECIMAL);
			((TextBox)widget).setTabIndex(tabIndex);
		}
		else if(s.equalsIgnoreCase("Label"))
			widget = new Label(node.getAttribute("Text"));
		else if(s.equalsIgnoreCase("RepeatSection")){
			//Not dealing with nexted repeats
			//widget = new RunTimeGroupWidget();
			//((RunTimeGroupWidget)widget).setTabIndex(tabIndex);
		}
		else
			return tabIndex;

		RuntimeWidgetWrapper wrapper = new RuntimeWidgetWrapper(widget,images.error(),editListener);

		if(questionDef != null)
			wrapper.setQuestionDef(questionDef);
		if(binding != null)
			wrapper.setBinding(binding);

		if(parentWrapper != null)
			parentWrapper.addChildWidget(wrapper);

		String value = node.getAttribute("HelpText");
		if(value != null && value.trim().length() > 0)
			wrapper.setTitle(value);

		value = node.getAttribute("Width");
		if(value != null && value.trim().length() > 0)
			wrapper.setWidth(value);

		value = node.getAttribute("Height");
		if(value != null && value.trim().length() > 0)
			wrapper.setHeight(value);

		if(wrapper.getWrappedWidget() instanceof Label)
			WidgetEx.loadLabelProperties(node,wrapper);

		if(tabIndex > 0 && !(wrapper.getWrappedWidget() instanceof Button))
			widgets.put(new Integer(tabIndex), wrapper);
		else
			addWidget(wrapper);

		//FormDesignerUtil.setWidgetPosition(wrapper,left,top);

		if(widget instanceof Button && binding != null && (binding.equals("addnew")||binding.equals("remove"))){
			((Button)widget).addClickListener(new ClickListener(){
				public void onClick(Widget sender){
					execute(sender);
				}
			});
		}

		return tabIndex;
	}

	/**
	 * Just adds the first row. Other runtime rows are added using addNewRow
	 * @param wrapper
	 */
	private void addWidget(RuntimeWidgetWrapper wrapper){
		if(wrapper.getWrappedWidget() instanceof Button){
			buttons.add(wrapper);
			return;
		}

		//widgets.add(new RuntimeWidgetWrapper(wrapper));
		widgets.add(wrapper);

		int row = 0 , col = 0;
		if(table.getRowCount() > 0)
			col = table.getCellCount(row);

		table.setWidget(row, col, wrapper);
	}

	private void execute(Widget sender){
		if(repeatQtnsDef != null){
			String binding = ((RuntimeWidgetWrapper)sender.getParent().getParent()).getBinding();
			if(binding.equalsIgnoreCase("addnew"))
				addNewRow();
			else if(binding.equalsIgnoreCase("remove")){
				if(table.getRowCount() > 1){//There should be atleast one row{
					table.removeRow(table.getRowCount()-1);
					Element node = dataNodes.get(dataNodes.size() - 1);
					node.getParentNode().removeChild(node);
					dataNodes.remove(node);
				}
			}
		}
	}

	private void addNewRow(){
		Element parentDataNode = null;
		int row = table.getRowCount();
		for(int index = 0; index < widgets.size(); index++){
			RuntimeWidgetWrapper mainWidget = widgets.get(index);
			RuntimeWidgetWrapper copyWidget = getPreparedWidget(mainWidget);

			table.setWidget(row, index, copyWidget);

			if(index == 0){
				Element dataNode = mainWidget.getQuestionDef().getDataNode();
				if(dataNode != null){
					parentDataNode = (Element)dataNode.getParentNode().cloneNode(true);
					dataNode.getParentNode().getParentNode().appendChild(parentDataNode);
					dataNodes.add(parentDataNode);
				}
			}

			setDataNode(copyWidget,parentDataNode);
		}
	}

	private void setDataNode(RuntimeWidgetWrapper widget, Element parentNode){
		NodeList nodes = parentNode.getChildNodes();
		for(int index = 0; index < nodes.getLength(); index++){
			Node child = nodes.item(index);
			if(child.getNodeType() != Node.ELEMENT_NODE)
				continue;
			if(child.getNodeName().equals(widget.getBinding()) ||
			  (child.getParentNode().getNodeName() + "/"+ child.getNodeName()).equals(widget.getBinding())){
				widget.getQuestionDef().setDataNode((Element)child);
				return;
			}
		}
	}

	private RuntimeWidgetWrapper getPreparedWidget(RuntimeWidgetWrapper w){
		RuntimeWidgetWrapper widget = new RuntimeWidgetWrapper(w);
		widget.loadQuestion();
		
		QuestionDef questionDef = widget.getQuestionDef();
		if(questionDef != null && (questionDef.getDataType() == QuestionDef.QTN_TYPE_NUMERIC 
				|| questionDef.getDataType() == QuestionDef.QTN_TYPE_DECIMAL))
			FormUtil.allowNumericOnly((TextBox)widget.getWrappedWidget(),questionDef.getDataType() == QuestionDef.QTN_TYPE_DECIMAL);

		widget.refreshSize();
		return widget;
	}

	public void setEnabled(boolean enabled){
		HorizontalPanel panel = (HorizontalPanel)verticalPanel.getWidget(1);
		for(int index = 0; index < panel.getWidgetCount(); index++)
			((RuntimeWidgetWrapper)panel.getWidget(index)).setEnabled(enabled);

		for(int row = 0; row < table.getRowCount(); row++){
			for(int col = 0; col < table.getCellCount(row); col++)
				((RuntimeWidgetWrapper)table.getWidget(row, col)).setEnabled(enabled);
		}
	}
	
	public void setLocked(boolean locked){
		HorizontalPanel panel = (HorizontalPanel)verticalPanel.getWidget(1);
		for(int index = 0; index < panel.getWidgetCount(); index++)
			((RuntimeWidgetWrapper)panel.getWidget(index)).setLocked(locked);

		for(int row = 0; row < table.getRowCount(); row++){
			for(int col = 0; col < table.getCellCount(row); col++)
				((RuntimeWidgetWrapper)table.getWidget(row, col)).setLocked(locked);
		}	
	}

	public void saveValue(FormDef formDef){
		for(int row = 0; row < table.getRowCount(); row++){
			for(int col = 0; col < table.getCellCount(row); col++)
				((RuntimeWidgetWrapper)table.getWidget(row, col)).saveValue(formDef);
		}
	}
}
