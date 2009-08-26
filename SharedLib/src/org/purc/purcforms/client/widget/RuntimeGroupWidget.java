package org.purc.purcforms.client.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.purc.purcforms.client.controller.IOpenFileDialogEventListener;
import org.purc.purcforms.client.controller.QuestionChangeListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.OptionDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.model.RepeatQtnsDef;
import org.purc.purcforms.client.model.ValidationRule;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.view.FormRunnerView;
import org.purc.purcforms.client.view.OpenFileDialog;
import org.purc.purcforms.client.view.FormRunnerView.Images;
import org.purc.purcforms.client.xforms.XformConverter;
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
public class RuntimeGroupWidget extends Composite implements IOpenFileDialogEventListener,QuestionChangeListener{

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
	private HTML html;
	private FormDef formDef;
	private Button btnAdd;
	private RuntimeWidgetWrapper firstInvalidWidget;
	
	protected HashMap<QuestionDef,List<Widget>> labelMap = new HashMap<QuestionDef,List<Widget>>();;
	protected HashMap<Widget,String> labelText = new HashMap<Widget,String>();
	protected HashMap<Widget,String> labelReplaceText = new HashMap<Widget,String>();

	protected HashMap<QuestionDef,List<CheckBox>> checkBoxGroupMap = new HashMap<QuestionDef,List<CheckBox>>();
	
	
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
		RuntimeWidgetWrapper parentWrapper = widgetMap.get(node.getAttribute(WidgetEx.WIDGET_PROPERTY_PARENTBINDING));
		if(parentWrapper == null){
			QuestionDef qtn = null;
			if(repeatQtnsDef != null)
				qtn = repeatQtnsDef.getQuestion(node.getAttribute(WidgetEx.WIDGET_PROPERTY_PARENTBINDING));
			else
				qtn = formDef.getQuestion(node.getAttribute(WidgetEx.WIDGET_PROPERTY_PARENTBINDING));

			if(qtn != null){
				parentWrapper = new RuntimeWidgetWrapper(widget,images.error(),editListener);
				parentWrapper.setQuestionDef(qtn,true);
				widgetMap.put(node.getAttribute(WidgetEx.WIDGET_PROPERTY_PARENTBINDING), parentWrapper);
				//addWidget(parentWrapper); //Misplaces first widget (with tabindex > 0) of a group (CheckBox and RadioButtons)
			
				qtn.addChangeListener(this);
				List<CheckBox> list = new ArrayList<CheckBox>();
				list.add((CheckBox)widget);
				checkBoxGroupMap.put(qtn, list);
			}
		}	 
		else
			checkBoxGroupMap.get(parentWrapper.getQuestionDef()).add((CheckBox)widget);
		
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

		if(isRepeated){
			RuntimeWidgetWrapper widget = this.widgets.get(0);
			if(!(widget.getQuestionDef() == null || widget.getQuestionDef().getDataNode() == null)){
				Element dataNode = (Element)widget.getQuestionDef().getDataNode().getParentNode();
				Element parent = (Element)dataNode.getParentNode();
				NodeList nodeList = parent.getElementsByTagName(dataNode.getNodeName());
				for(int index = 1; index < nodeList.getLength(); index++)
					addNewRow((Element)nodeList.item(index));
			}
		}

		if(isRepeated){
			HorizontalPanel panel = new HorizontalPanel();
			panel.setSpacing(5);
			for(int index = 0; index < buttons.size(); index++)
				panel.add(buttons.get(index));
			verticalPanel.add(panel);
			FormUtil.maximizeWidget(panel);
		}
		else{
			for(int index = 0; index < buttons.size(); index++){
				RuntimeWidgetWrapper widget = buttons.get(index);
				selectedPanel.add(widget);
				FormUtil.setWidgetPosition(widget,widget.getLeft(),widget.getTop());
			}
		}
	}

	private int loadWidget(FormDef formDef, Element node,HashMap<Integer,RuntimeWidgetWrapper> widgets, List<RuntimeWidgetWrapper> externalSourceWidgets){
		RuntimeWidgetWrapper parentWrapper = null;

		String s = node.getAttribute(WidgetEx.WIDGET_PROPERTY_WIDGETTYPE);
		int tabIndex = (node.getAttribute(WidgetEx.WIDGET_PROPERTY_TABINDEX) != null ? Integer.parseInt(node.getAttribute(WidgetEx.WIDGET_PROPERTY_TABINDEX)) : 0);

		QuestionDef questionDef = null;
		String binding = node.getAttribute(WidgetEx.WIDGET_PROPERTY_BINDING);

		if(isRepeated){
			if(binding != null && binding.trim().length() > 0 && repeatQtnsDef != null){
				questionDef = repeatQtnsDef.getQuestion(binding);
				if(questionDef != null)
					questionDef.setAnswer(questionDef.getDefaultValue()); //Just incase we are refreshing and had already set the answer
			}
		}
		else{
			if(binding != null && binding.trim().length() > 0){
				questionDef = formDef.getQuestion(binding);
				if(questionDef != null)
					questionDef.setAnswer(questionDef.getDefaultValue()); //Just incase we are refreshing and had already set the answer
			}
		}

		RuntimeWidgetWrapper wrapper = null;
		boolean wrapperSet = false;
		Widget widget = null;
		if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_RADIOBUTTON)){
			/*widget = new RadioButton(node.getAttribute(WidgetEx.WIDGET_PROPERTY_PARENTBINDING),node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));
			parentWrapper = getParentWrapper(widget,node);
			((RadioButton)widget).setTabIndex(tabIndex);*/

			widget = new RadioButton(node.getAttribute(WidgetEx.WIDGET_PROPERTY_PARENTBINDING),node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));

			if(widgetMap.get(node.getAttribute(WidgetEx.WIDGET_PROPERTY_PARENTBINDING)) == null)
				wrapperSet = true;

			parentWrapper = getParentWrapper(widget,node);
			((RadioButton)widget).setTabIndex(tabIndex);

			if(wrapperSet)
				wrapper = parentWrapper;
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_CHECKBOX)){
			/*widget = new CheckBox(node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));
			parentWrapper = getParentWrapper(widget,node);
			((CheckBox)widget).setTabIndex(tabIndex);*/

			widget = new CheckBox(node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));
			if(widgetMap.get(node.getAttribute(WidgetEx.WIDGET_PROPERTY_PARENTBINDING)) == null)
				wrapperSet = true;

			parentWrapper = getParentWrapper(widget,node);
			((CheckBox)widget).setTabIndex(tabIndex);

			String defaultValue = parentWrapper.getQuestionDef().getDefaultValue();
			if(defaultValue != null && defaultValue.contains(binding))
				((CheckBox)widget).setChecked(true);

			if(wrapperSet)
				wrapper = parentWrapper;
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
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_LABEL)){
			String text = node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT);
			widget = new Label(text);
			
			int pos1 = text.indexOf("${");
			int pos2 = text.indexOf("}$");
			if(pos1 > -1 && pos2 > -1 && (pos2 > pos1)){
				String varname = text.substring(pos1+2,pos2);
				labelText.put(widget, text);
				labelReplaceText.put(widget, "${"+varname+"}$");
				
				((Label)widget).setText(text.replace("${"+varname+"}$", ""));
				if(varname.startsWith("/"+ formDef.getVariableName()+"/"))
					varname = varname.substring(("/"+ formDef.getVariableName()+"/").length(),varname.length());
				
				QuestionDef qtnDef = formDef.getQuestion(varname);
				List<Widget> labels = labelMap.get(qtnDef);
				if(labels == null){
					labels = new ArrayList<Widget>();
					labelMap.put(qtnDef, labels);
				}
				labels.add(widget);
			}
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_IMAGE)){
			widget = new Image();
			String xpath = binding;
			if(!xpath.startsWith(formDef.getVariableName()))
				xpath = "/" + formDef.getVariableName() + "/" + binding;
			((Image)widget).setUrl(URL.encode(FormUtil.getMultimediaUrlSuffix()+"?formId="+formDef.getId()+"&xpath="+xpath+"&time="+ new java.util.Date().getTime()));
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_VIDEO_AUDIO) && questionDef != null){
			widget = new HTML();
			String xpath = binding;
			if(!xpath.startsWith(formDef.getVariableName()))
				xpath = "/" + formDef.getVariableName() + "/" + binding;

			String extension = "";//.3gp ".mpeg";
			String contentType = "&contentType=video/3gpp";
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_AUDIO)
				contentType = "&contentType=audio/3gpp"; //"&contentType=audio/x-wav";
				//extension = ".wav";
			
			contentType += "&name="+questionDef.getVariableName()+".3gp";

			((HTML)widget).setHTML("<a href=" + URL.encode(FormUtil.getMultimediaUrlSuffix()+extension + "?formId="+formDef.getId()+"&xpath="+xpath+contentType+"&time="+ new java.util.Date().getTime()) + ">"+node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT)+"</a>");

			String answer = questionDef.getAnswer();
			if(answer == null || answer.trim().length() == 0 )
				((HTML)widget).setVisible(false);
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_REPEATSECTION)){
			//Not dealing with nexted repeats
			//widget = new RunTimeGroupWidget();
			//((RunTimeGroupWidget)widget).setTabIndex(tabIndex);
		}
		else
			return tabIndex;

		if(!wrapperSet)
			wrapper = new RuntimeWidgetWrapper(widget,images.error(),editListener);

		//RuntimeWidgetWrapper wrapper = new RuntimeWidgetWrapper(widget,images.error(),editListener);
		boolean loadWidget = true;

		if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_VIDEO_AUDIO) || s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_IMAGE)){
			if(binding != null && binding.trim().length() > 0){
				questionDef = formDef.getQuestion(binding);
				if(questionDef != null)
					questionDef.setAnswer(questionDef.getDefaultValue()); //Just incase we are refreshing and had already set the answer
			}
		}

		if(questionDef != null){
			wrapper.setQuestionDef(questionDef,false);
			ValidationRule validationRule = formDef.getValidationRule(questionDef);
			wrapper.setValidationRule(validationRule);
		}

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

		String left = node.getAttribute(WidgetEx.WIDGET_PROPERTY_LEFT);
		if(left != null && left.trim().length() > 0)
			wrapper.setLeft(left);

		String top = node.getAttribute(WidgetEx.WIDGET_PROPERTY_TOP);
		if(top != null && top.trim().length() > 0)
			wrapper.setTop(top);

		if(wrapper.getWrappedWidget() instanceof Label)
			WidgetEx.loadLabelProperties(node,wrapper);

		if(tabIndex > 0 && !(wrapper.getWrappedWidget() instanceof Button))
			widgets.put(new Integer(tabIndex), wrapper);
		else
			addWidget(wrapper);

		if(wrapperSet)
			FormUtil.setWidgetPosition(wrapper,left,top);

		if(widget instanceof Button && binding != null){
			wrapper.setParentBinding(node.getAttribute(WidgetEx.WIDGET_PROPERTY_PARENTBINDING));

			if(binding.equals("addnew")||binding.equals("remove") || binding.equals("submit") ||
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

		if(binding.equalsIgnoreCase("submit"))
			((FormRunnerView)getParent().getParent().getParent().getParent().getParent().getParent().getParent()).onSubmit();
		else if(repeatQtnsDef != null){
			if(binding.equalsIgnoreCase("addnew"))
				addNewRow(sender);
			else if(binding.equalsIgnoreCase("remove")){
				if(table.getRowCount() > 1){//There should be atleast one row{
					table.removeRow(table.getRowCount()-1);
					Element node = dataNodes.get(dataNodes.size() - 1);
					node.getParentNode().removeChild(node);
					dataNodes.remove(node);
					if(btnAdd != null)
						btnAdd.setEnabled(true);
				}

				RuntimeWidgetWrapper parent = (RuntimeWidgetWrapper)getParent().getParent();
				ValidationRule validationRule = parent.getValidationRule();
				if(validationRule != null)
					parent.getQuestionDef().setAnswer(table.getRowCount()+"");
			}
		}
		else{
			if(binding.equalsIgnoreCase("clear")){
				RuntimeWidgetWrapper wrapper = getCurrentMultimediWrapper(sender);
				if(wrapper == null)
					return;

				if(wrapper.getWrappedWidget() instanceof Image && (((Image)wrapper.getWrappedWidget()).getUrl() == null ||
						((Image)wrapper.getWrappedWidget()).getUrl().trim().length() == 0))
					return;
				if(wrapper.getWrappedWidget() instanceof HTML && !wrapper.getWrappedWidget().isVisible())
					return;

				if(!Window.confirm(LocaleText.get("deleteItemPrompt")))
					return;

				QuestionDef questionDef = wrapper.getQuestionDef();
				if(questionDef != null)
					questionDef.setAnswer(null);

				if(wrapper.getWrappedWidget() instanceof Image){
					image = (Image)wrapper.getWrappedWidget();
					image.setUrl(null);
					html = null;
				}
				else{
					html = (HTML)wrapper.getWrappedWidget();
					html.setHTML(LocaleText.get("clickToPlay"));
					html.setVisible(false);
					image = null;
				}
				return;
			}
			else if(binding.equalsIgnoreCase("browse")){
				RuntimeWidgetWrapper wrapper = getCurrentMultimediWrapper(sender);

				if(wrapper.getWrappedWidget() instanceof Image)
					image = (Image)wrapper.getWrappedWidget();
				else
					html = (HTML)wrapper.getWrappedWidget();

				OpenFileDialog dlg = new OpenFileDialog(this,FormUtil.getMultimediaUrlSuffix());
				dlg.center();
			}
		}
	}

	/*private Image getCurrentImage(Widget sender){			
		RuntimeWidgetWrapper wrapper = getCurrentMultimediWrapper(sender);
		if(wrapper != null)
			return (Image)wrapper.getWrappedWidget();

		return null;
	}*/

	private RuntimeWidgetWrapper getCurrentMultimediWrapper(Widget sender){
		RuntimeWidgetWrapper button = (RuntimeWidgetWrapper)sender.getParent().getParent();
		for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
			RuntimeWidgetWrapper widget = (RuntimeWidgetWrapper)selectedPanel.getWidget(index);
			if(widget.getWrappedWidget() instanceof Image || widget.getWrappedWidget() instanceof HTML){
				if(widget.getBinding().equalsIgnoreCase(button.getParentBinding()))
					return widget;
			}
		}
		return null;
	}

	private void addNewRow(Widget sender){
		Element newRepeatDataNode = null;
		int row = table.getRowCount();
		for(int index = 0; index < widgets.size(); index++){
			RuntimeWidgetWrapper mainWidget = widgets.get(index);
			RuntimeWidgetWrapper copyWidget = getPreparedWidget(mainWidget,true);

			//table.setWidget(row, index, copyWidget);

			if(index == 0){
				Element dataNode = mainWidget.getQuestionDef().getDataNode();
				if(dataNode == null){
					Window.alert("Please first save the form"); //LocaleText.get("?????");
					return; //possibly form not yet saved
				}

				Element repeatDataNode = getParentNode(dataNode,mainWidget.getBinding());
				newRepeatDataNode = (Element)repeatDataNode.cloneNode(true);
				repeatDataNode.getParentNode().appendChild(newRepeatDataNode);
				dataNodes.add(newRepeatDataNode);
			}

			table.setWidget(row, index, copyWidget);

			setDataNode(copyWidget,newRepeatDataNode,copyWidget.getBinding(),false);
		}

		btnAdd = (Button)sender;
		RuntimeWidgetWrapper parent = (RuntimeWidgetWrapper)getParent().getParent();
		ValidationRule validationRule = parent.getValidationRule();
		if(validationRule != null){
			row++;
			parent.getQuestionDef().setAnswer(row+"");
			if(validationRule.getMaxValue(formDef) == row)
				((Button)sender).setEnabled(false);
		}
		//byte maxRows = repeatQtnsDef.getMaxRows();
		//if(maxRows > 0 && row == maxRows)
		//	((Button)sender).setEnabled(false);
	}

	private void addNewRow(Element dataNode){
		dataNodes.add(dataNode);

		int row = table.getRowCount();
		for(int index = 0; index < widgets.size(); index++){
			RuntimeWidgetWrapper mainWidget = widgets.get(index);
			RuntimeWidgetWrapper copyWidget = getPreparedWidget(mainWidget,false);

			table.setWidget(row, index, copyWidget);

			setDataNode(copyWidget,dataNode,copyWidget.getBinding(),true);
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

	private void setDataNode(RuntimeWidgetWrapper widget, Element parentNode, String binding, boolean loadQtn){
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
					setDataNode(widget,(Element)child,binding.substring(pos+1),loadQtn);
				else{
					widget.getQuestionDef().setDataNode((Element)child);
					if(loadQtn){
						widget.getQuestionDef().setDefaultValue(XformConverter.getTextValue((Element)child));
						widget.loadQuestion();
					}
				}
				return;
			}
		}
	}

	private RuntimeWidgetWrapper getPreparedWidget(RuntimeWidgetWrapper w, boolean loadQtn){
		RuntimeWidgetWrapper widget = new RuntimeWidgetWrapper(w);

		if(loadQtn)
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

		if(repeatQtnsDef != null)
			repeatQtnsDef.getQtnDef().setAnswer(getRowCount()+"");
	}

	public int getRowCount(){
		int rows = 0;

		for(int row = 0; row < table.getRowCount(); row++){
			boolean answerFound = false;
			for(int col = 0; col < table.getCellCount(row); col++){
				if(((RuntimeWidgetWrapper)table.getWidget(row, col)).isAnswered()){
					answerFound = true;
					break;
				}
			}

			if(answerFound)
				rows++;
		}

		return rows;
	}

	public void onSetFileContents(String contents) {
		if(contents != null && contents.trim().length() > 0){
			contents = contents.replace("<pre>", "");
			contents = contents.replace("</pre>", "");
			RuntimeWidgetWrapper widgetWrapper = null;

			if(image != null)
				widgetWrapper = (RuntimeWidgetWrapper)image.getParent().getParent();
			else
				widgetWrapper = (RuntimeWidgetWrapper)html.getParent().getParent();
			
			String xpath = widgetWrapper.getBinding();
			if(!xpath.startsWith(formDef.getVariableName()))
				xpath = "/" + formDef.getVariableName() + "/" + widgetWrapper.getBinding();
			
			if(image != null)
				image.setUrl(FormUtil.getMultimediaUrlSuffix()+"?action=recentbinary&time="+ new java.util.Date().getTime()+"&formId="+formDef.getId()+"&xpath="+xpath);
			else{
				String extension = "";//.3gp ".mpeg";
				String contentType = "&contentType=video/3gpp";
				if(widgetWrapper.getQuestionDef().getDataType() == QuestionDef.QTN_TYPE_AUDIO)
					contentType = "&contentType=audio/3gpp"; //"&contentType=audio/x-wav";
					//extension = ".wav";
				
				contentType += "&name="+widgetWrapper.getQuestionDef().getVariableName()+".3gp";

				html.setVisible(true);
				html.setHTML("<a href=" + URL.encode(FormUtil.getMultimediaUrlSuffix()+extension + "?formId="+formDef.getId()+"&xpath="+xpath+contentType+"&time="+ new java.util.Date().getTime()) + ">"+html.getText()+"</a>");				
			}

			widgetWrapper.getQuestionDef().setAnswer(contents);
		}
	}

	public void clearValue(){
		if(isRepeated){
			while(table.getRowCount() > 1)
				table.removeRow(1);

			for(int col = 0; col < table.getCellCount(0); col++)
				((RuntimeWidgetWrapper)table.getWidget(0, col)).clearValue();
		}
		else{
			for(int index = 0; index < selectedPanel.getWidgetCount(); index++)
				((RuntimeWidgetWrapper)selectedPanel.getWidget(index)).clearValue();
		}
	}

	public boolean isValid(){
		if(isRepeated){
			for(int row = 0; row < table.getRowCount(); row++){
				for(int col = 0; col < table.getCellCount(row); col++){
					boolean valid = ((RuntimeWidgetWrapper)table.getWidget(row, col)).isValid();
					if(!valid){
						firstInvalidWidget = (RuntimeWidgetWrapper)table.getWidget(row, col);
						return false;
					}
				}
			}
			return true;
		}
		else{
			boolean valid = true;
			for(int index=0; index<selectedPanel.getWidgetCount(); index++){
				RuntimeWidgetWrapper widget = (RuntimeWidgetWrapper)selectedPanel.getWidget(index);
				if(!widget.isValid()){
					valid = false;
					if(firstInvalidWidget == null && widget.isFocusable())
						firstInvalidWidget = widget.getInvalidWidget();
				}
			}
			return valid;
		}
	}

	public RuntimeWidgetWrapper getInvalidWidget(){
		if(firstInvalidWidget == null)
			return (RuntimeWidgetWrapper)getParent().getParent();
		return firstInvalidWidget;
	}

	public boolean setFocus(){
		if(isRepeated){
			for(int row = 0; row < table.getRowCount(); row++){
				for(int col = 0; col < table.getCellCount(row); col++){
					RuntimeWidgetWrapper widget = (RuntimeWidgetWrapper)table.getWidget(row, col);
					if(widget.isFocusable()){
						widget.setFocus();
						return true;
					}
				}
			}
		}
		else{
			for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
				RuntimeWidgetWrapper widget = (RuntimeWidgetWrapper)selectedPanel.getWidget(index);
				if(widget.isFocusable()){
					widget.setFocus();
					return true;
				}
			}
		}

		return false;
	}

	public boolean onMoveToNextWidget(Widget widget) {
		int index = selectedPanel.getWidgetIndex(widget);
		return moveToNextWidget(index);
	}

	public boolean onMoveToPrevWidget(Widget widget){
		int index = selectedPanel.getWidgetIndex(widget);
		while(--index > 0){
			if(((RuntimeWidgetWrapper)selectedPanel.getWidget(index)).setFocus())
				return true;
		}

		return false;
	}

	protected boolean moveToNextWidget(int index){
		while(++index < selectedPanel.getWidgetCount())
			if(((RuntimeWidgetWrapper)selectedPanel.getWidget(index)).setFocus()){
				return true;
			}

		return false;
	}
	
	public HashMap<QuestionDef,List<Widget>> getLabelMap(){
		return labelMap;
	}
	
	public HashMap<Widget,String> getLabelText(){
		return labelText;
	}
	
	public HashMap<Widget,String> getLabelReplaceText(){
		return labelReplaceText;
	}
	
	public HashMap<QuestionDef,List<CheckBox>> getCheckBoxGroupMap(){
		return checkBoxGroupMap;
	}
	
	public void onEnabledChanged(QuestionDef sender,boolean enabled){
		List<CheckBox> list = checkBoxGroupMap.get(sender);
		if(list == null)
			return;
		
		for(CheckBox checkBox : list){
			checkBox.setEnabled(enabled);
			if(!enabled)
				checkBox.setChecked(false);
		}
	}
	
	public void onVisibleChanged(QuestionDef sender,boolean visible){
		List<CheckBox> list = checkBoxGroupMap.get(sender);
		if(list == null)
			return;
		
		for(CheckBox checkBox : list){
			checkBox.setVisible(visible);
			if(!visible)
				checkBox.setChecked(false);
		}
	}
	
	public void onRequiredChanged(QuestionDef sender,boolean required){
		
	}
	
	public void onLockedChanged(QuestionDef sender,boolean locked){
		
	}
	
	public void onBindingChanged(QuestionDef sender,String newValue){
		
	}
	
	public void onDataTypeChanged(QuestionDef sender,int dataType){
		
	}
	
	public void onOptionsChanged(QuestionDef sender,List<OptionDef> optionList){
		
	}
}
