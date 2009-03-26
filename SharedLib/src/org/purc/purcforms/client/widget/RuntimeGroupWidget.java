package org.purc.purcforms.client.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.purc.purcforms.client.controller.IOpenFileDialogEventListener;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.model.RepeatQtnsDef;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.view.OpenFileDialog;
import org.purc.purcforms.client.view.FormRunnerView.Images;
import org.zenika.widget.client.datePicker.DatePicker;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
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
public class RuntimeGroupWidget extends Composite implements IOpenFileDialogEventListener{

	private final Images images;
	private RepeatQtnsDef repeatQtnsDef;
	private HashMap<String,RuntimeWidgetWrapper> widgetMap = new HashMap<String,RuntimeWidgetWrapper>();
	private EditListener editListener;
	private FlexTable table;
	private List<RuntimeWidgetWrapper> buttons = new ArrayList<RuntimeWidgetWrapper>();
	private List<RuntimeWidgetWrapper> widgets = new ArrayList<RuntimeWidgetWrapper>();
	private VerticalPanel verticalPanel = new VerticalPanel();
	private List<Element> dataNodes = new ArrayList<Element>();
	private AbsolutePanel selectedPanel = new AbsolutePanel();
	private boolean isRepeated = false;
	private Image image;
	private FormDef formDef;


	public RuntimeGroupWidget(Images images,FormDef formDef,RepeatQtnsDef repeatQtnsDef,EditListener editListener, boolean isRepeated){
		this.images = images;
		this.formDef = formDef;
		this.repeatQtnsDef = repeatQtnsDef;
		this.editListener = editListener;
		this.isRepeated = isRepeated;

		if(isRepeated){
			table = new FlexTable();
			FormUtil.maximizeWidget(table);		
			verticalPanel.add(table);
			initWidget(verticalPanel);
		}
		else
			initWidget(selectedPanel);
		//setupEventListeners();

		//table.setStyleName("cw-FlexTable");
		this.addStyleName("purcforms-repeat-border");
	}

	//TODO The code below needs great refactoring together with PreviewView
	private RuntimeWidgetWrapper getParentWrapper(Widget widget, Element node){
		RuntimeWidgetWrapper parentWrapper = widgetMap.get(node.getAttribute("ParentBinding"));
		if(parentWrapper == null && repeatQtnsDef != null){
			QuestionDef qtn = repeatQtnsDef.getQuestion(node.getAttribute("ParentBinding"));
			if(qtn != null){
				parentWrapper = new RuntimeWidgetWrapper(widget,images.error(),editListener);
				parentWrapper.setQuestionDef(qtn,true);
				widgetMap.put(node.getAttribute("ParentBinding"), parentWrapper);
				addWidget(parentWrapper);
			}
		}	 
		return parentWrapper;
	}

	public void loadWidgets(FormDef formDef,NodeList nodes, List<RuntimeWidgetWrapper> externalSourceWidgets){
		HashMap<Integer,RuntimeWidgetWrapper> widgets = new HashMap<Integer,RuntimeWidgetWrapper>();
		int maxTabIndex = 0;

		for(int i=0; i<nodes.getLength(); i++){
			if(nodes.item(i).getNodeType() != Node.ELEMENT_NODE)
				continue;
			try{
				Element node = (Element)nodes.item(i);
				int index = loadWidget(formDef,node,widgets,externalSourceWidgets);
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

	private int loadWidget(FormDef formDef, Element node,HashMap<Integer,RuntimeWidgetWrapper> widgets, List<RuntimeWidgetWrapper> externalSourceWidgets){
		RuntimeWidgetWrapper parentWrapper = null;

		String s = node.getAttribute(WidgetEx.WIDGET_PROPERTY_WIDGETTYPE);
		int tabIndex = (node.getAttribute(WidgetEx.WIDGET_PROPERTY_TABINDEX) != null ? Integer.parseInt(node.getAttribute(WidgetEx.WIDGET_PROPERTY_TABINDEX)) : 0);

		QuestionDef questionDef = null;
		String binding = node.getAttribute(WidgetEx.WIDGET_PROPERTY_BINDING);
		if(binding != null && binding.trim().length() > 0 && repeatQtnsDef != null){
			questionDef = repeatQtnsDef.getQuestion(binding);
			if(questionDef != null)
				questionDef.setAnswer(questionDef.getDefaultValue()); //Just incase we are refreshing and had already set the answer
		}

		Widget widget = null;
		if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_RADIOBUTTON)){
			widget = new RadioButton(node.getAttribute(WidgetEx.WIDGET_PROPERTY_PARENTBINDING),node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));
			parentWrapper = getParentWrapper(widget,node);
			((RadioButton)widget).setTabIndex(tabIndex);
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_CHECKBOX)){
			widget = new CheckBox(node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));
			parentWrapper = getParentWrapper(widget,node);
			((CheckBox)widget).setTabIndex(tabIndex);
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_BUTTON)){
			widget = new Button(node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));
			((Button)widget).setTabIndex(tabIndex);
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_LISTBOX)){
			widget = new ListBox(false);
			((ListBox)widget).setTabIndex(tabIndex);
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_TEXTAREA)){
			widget = new TextArea();
			((TextArea)widget).setTabIndex(tabIndex);
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_DATEPICKER)){
			widget = new DatePickerWidget();
			((DatePicker)widget).setTabIndex(tabIndex);
			/*((DatePicker)widget).addFocusListener(new FocusListenerAdapter(){
				 public void onLostFocus(Widget sender){
					 //((DatePicker)sender).selectAll();
				 }
			 });*/
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_TEXTBOX)){
			widget = new TextBox();
			if(questionDef != null && (questionDef.getDataType() == QuestionDef.QTN_TYPE_NUMERIC 
					|| questionDef.getDataType() == QuestionDef.QTN_TYPE_DECIMAL))
				FormUtil.allowNumericOnly((TextBox)widget,questionDef.getDataType() == QuestionDef.QTN_TYPE_DECIMAL);
			((TextBox)widget).setTabIndex(tabIndex);
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_LABEL))
			widget = new Label(node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_IMAGE)){
			widget = new Image();
			String xpath = binding;
			if(!xpath.startsWith(formDef.getVariableName()))
				xpath = "/" + formDef.getVariableName() + "/" + binding;
			((Image)widget).setUrl(URL.encode("multimedia?formId="+formDef.getId()+"&xpath="+xpath+"&time="+ new java.util.Date().getTime()));
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_VIDEO_AUDIO)){
			widget = new HTML();
			String xpath = binding;
			if(!xpath.startsWith(formDef.getVariableName()))
				xpath = "/" + formDef.getVariableName() + "/" + binding;

			String contentType = "&contentType=video/mpeg";
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_AUDIO)
				contentType = "&contentType=audio/x-wav";

			((HTML)widget).setHTML("<a href=" + URL.encode("multimedia?formId="+formDef.getId()+"&xpath="+xpath+contentType+"&time="+ new java.util.Date().getTime()) + ">"+node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT)+"</a>");
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_REPEATSECTION)){
			//Not dealing with nexted repeats
			//widget = new RunTimeGroupWidget();
			//((RunTimeGroupWidget)widget).setTabIndex(tabIndex);
		}
		else
			return tabIndex;

		RuntimeWidgetWrapper wrapper = new RuntimeWidgetWrapper(widget,images.error(),editListener);
		boolean loadWidget = true;

		if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_VIDEO_AUDIO) || s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_IMAGE)){
			if(binding != null && binding.trim().length() > 0){
				questionDef = formDef.getQuestion(binding);
				if(questionDef != null)
					questionDef.setAnswer(questionDef.getDefaultValue()); //Just incase we are refreshing and had already set the answer
			}
		}

		if(questionDef != null)
			wrapper.setQuestionDef(questionDef,false);

		if(binding != null)
			wrapper.setBinding(binding);

		if(parentWrapper != null)
			parentWrapper.addChildWidget(wrapper);

		String value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_HELPTEXT);
		if(value != null && value.trim().length() > 0)
			wrapper.setTitle(value);

		value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_WIDTH);
		if(value != null && value.trim().length() > 0)
			wrapper.setWidth(value);

		value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_HEIGHT);
		if(value != null && value.trim().length() > 0)
			wrapper.setHeight(value);

		value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_EXTERNALSOURCE);
		if(value != null && value.trim().length() > 0)
			wrapper.setExternalSource(value);

		value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_DISPLAYFIELD);
		if(value != null && value.trim().length() > 0)
			wrapper.setDisplayField(value);

		value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_VALUEFIELD);
		if(value != null && value.trim().length() > 0){
			wrapper.setValueField(value);

			if(externalSourceWidgets != null && wrapper.getExternalSource() != null && wrapper.getDisplayField() != null
					&& (wrapper.getWrappedWidget() instanceof TextBox || wrapper.getWrappedWidget() instanceof ListBox)
					&& questionDef != null
					&& (wrapper.getQuestionDef().getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE
							||wrapper.getQuestionDef().getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE)){
				externalSourceWidgets.add(wrapper);
				loadWidget = false;
			}
		}

		if(loadWidget)
			wrapper.loadQuestion();

		value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_HEIGHT);
		if(value != null && value.trim().length() > 0)
			wrapper.setHeight(value);

		value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_LEFT);
		if(value != null && value.trim().length() > 0)
			wrapper.setLeft(value);

		value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_TOP);
		if(value != null && value.trim().length() > 0)
			wrapper.setTop(value);

		if(wrapper.getWrappedWidget() instanceof Label)
			WidgetEx.loadLabelProperties(node,wrapper);

		if(tabIndex > 0 && !(wrapper.getWrappedWidget() instanceof Button))
			widgets.put(new Integer(tabIndex), wrapper);
		else
			addWidget(wrapper);

		//FormDesignerUtil.setWidgetPosition(wrapper,left,top);

		if(widget instanceof Button && binding != null){
			wrapper.setParentBinding(node.getAttribute(WidgetEx.WIDGET_PROPERTY_PARENTBINDING));

			if(binding.equals("addnew")||binding.equals("remove") ||
					binding.equals("browse")||binding.equals("clear")){
				((Button)widget).addClickListener(new ClickListener(){
					public void onClick(Widget sender){
						execute(sender);
					}
				});
			}
		}

		return tabIndex;
	}

	/**
	 * Just adds the first row. Other runtime rows are added using addNewRow
	 * @param wrapper
	 */
	private void addWidget(RuntimeWidgetWrapper wrapper){
		String binding = wrapper.getBinding();
		if(wrapper.getWrappedWidget() instanceof Button && 
				!("browse".equals(binding) || "clear".equals(binding))){
			buttons.add(wrapper);
			return;
		}

		if(isRepeated){
			//widgets.add(new RuntimeWidgetWrapper(wrapper));
			widgets.add(wrapper);

			int row = 0 , col = 0;
			if(table.getRowCount() > 0)
				col = table.getCellCount(row);

			table.setWidget(row, col, wrapper);
		}
		else{
			selectedPanel.add(wrapper);
			FormUtil.setWidgetPosition(wrapper,wrapper.getLeft(),wrapper.getTop());
		}
	}

	private void execute(Widget sender){
		String binding = ((RuntimeWidgetWrapper)sender.getParent().getParent()).getBinding();

		if(repeatQtnsDef != null){
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
		else{
			if(binding.equalsIgnoreCase("clear")){
				if(!Window.confirm("Do you really want to delete this picture?"))
					return;

				image = getCurrentImage(sender);
				image.setUrl(null);
				return;
			}
			else if(binding.equalsIgnoreCase("browse")){
				image = getCurrentImage(sender);
				OpenFileDialog dlg = new OpenFileDialog(this,"multimedia");
				dlg.center();
			}
		}
	}

	private Image getCurrentImage(Widget sender){
		RuntimeWidgetWrapper button = (RuntimeWidgetWrapper)sender.getParent().getParent();
		for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
			RuntimeWidgetWrapper widget = (RuntimeWidgetWrapper)selectedPanel.getWidget(index);
			if(widget.getWrappedWidget() instanceof Image){
				if(widget.getBinding().equalsIgnoreCase(button.getParentBinding()))
					return (Image)widget.getWrappedWidget();
			}
		}
		return null;
	}

	private void addNewRow(){
		Element newRepeatDataNode = null;
		int row = table.getRowCount();
		for(int index = 0; index < widgets.size(); index++){
			RuntimeWidgetWrapper mainWidget = widgets.get(index);
			RuntimeWidgetWrapper copyWidget = getPreparedWidget(mainWidget);

			table.setWidget(row, index, copyWidget);

			if(index == 0){
				Element dataNode = mainWidget.getQuestionDef().getDataNode();
				if(dataNode != null){
					Element repeatDataNode = getParentNode(dataNode,mainWidget.getBinding());
					newRepeatDataNode = (Element)repeatDataNode.cloneNode(true);
					repeatDataNode.getParentNode().appendChild(newRepeatDataNode);
					dataNodes.add(newRepeatDataNode);
				}
			}

			setDataNode(copyWidget,newRepeatDataNode,copyWidget.getBinding());
		}
	}

	private Element getParentNode(Node node, String binding){	
		String name = binding;
		int pos = binding.indexOf('/');
		if(pos > 0)
			name = binding.substring(0, pos);

		return getParentNodeWithName(node,name);
	}

	private Element getParentNodeWithName(Node node, String name){
		Element parentNode = (Element)node.getParentNode();
		if(node.getNodeName().equalsIgnoreCase(name))
			return parentNode;
		return getParentNodeWithName(parentNode,name);
	}

	private void setDataNode(RuntimeWidgetWrapper widget, Element parentNode, String binding){
		String name = binding;
		int pos = name.indexOf('/');
		if(pos > 0)
			name = name.substring(0, pos);

		NodeList nodes = parentNode.getChildNodes();
		for(int index = 0; index < nodes.getLength(); index++){
			Node child = nodes.item(index);
			if(child.getNodeType() != Node.ELEMENT_NODE)
				continue;
			if(child.getNodeName().equals(name) /*||
					(child.getParentNode().getNodeName() + "/"+ child.getNodeName()).equals(widget.getBinding())*/){
				if(pos > 0)
					setDataNode(widget,(Element)child,binding.substring(pos+1));
				else
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
		if(isRepeated){
			HorizontalPanel panel = (HorizontalPanel)verticalPanel.getWidget(1);
			for(int index = 0; index < panel.getWidgetCount(); index++)
				((RuntimeWidgetWrapper)panel.getWidget(index)).setEnabled(enabled);

			for(int row = 0; row < table.getRowCount(); row++){
				for(int col = 0; col < table.getCellCount(row); col++)
					((RuntimeWidgetWrapper)table.getWidget(row, col)).setEnabled(enabled);
			}
		}
		else{
			for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
				((RuntimeWidgetWrapper)selectedPanel.getWidget(index)).setEnabled(enabled);
			}
		}
	}

	public void setLocked(boolean locked){
		if(isRepeated){
			HorizontalPanel panel = (HorizontalPanel)verticalPanel.getWidget(1);
			for(int index = 0; index < panel.getWidgetCount(); index++)
				((RuntimeWidgetWrapper)panel.getWidget(index)).setLocked(locked);

			for(int row = 0; row < table.getRowCount(); row++){
				for(int col = 0; col < table.getCellCount(row); col++)
					((RuntimeWidgetWrapper)table.getWidget(row, col)).setLocked(locked);
			}	
		}
		else{
			for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
				((RuntimeWidgetWrapper)selectedPanel.getWidget(index)).setLocked(locked);
			}
		}
	}

	public void saveValue(FormDef formDef){
		if(isRepeated){
			for(int row = 0; row < table.getRowCount(); row++){
				for(int col = 0; col < table.getCellCount(row); col++)
					((RuntimeWidgetWrapper)table.getWidget(row, col)).saveValue(formDef);
			}
		}
		else{
			for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
				((RuntimeWidgetWrapper)selectedPanel.getWidget(index)).saveValue(formDef);
			}
		}
	}

	public void onSetFileContents(String contents) {
		//System.out.println(contents);
		
		if(contents != null && contents.trim().length() > 0){
			image.setUrl("multimedia?action=recentbinary"+"&time="+ new java.util.Date().getTime());
			RuntimeWidgetWrapper widgetWrapper = (RuntimeWidgetWrapper)image.getParent().getParent();
			widgetWrapper.getQuestionDef().setAnswer(contents);
		}
	}
}
