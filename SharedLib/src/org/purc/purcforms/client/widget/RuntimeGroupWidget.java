package org.purc.purcforms.client.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.purc.purcforms.client.controller.OpenFileDialogEventListener;
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
import org.purc.purcforms.client.xforms.XformConstants;
import org.purc.purcforms.client.xforms.XmlUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
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
public class RuntimeGroupWidget extends Composite implements OpenFileDialogEventListener,QuestionChangeListener{

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

	protected HashMap<QuestionDef,List<Label>> labelMap = new HashMap<QuestionDef,List<Label>>();;
	protected HashMap<Label,String> labelText = new HashMap<Label,String>();
	protected HashMap<Label,String> labelReplaceText = new HashMap<Label,String>();

	protected HashMap<QuestionDef,List<CheckBox>> checkBoxGroupMap = new HashMap<QuestionDef,List<CheckBox>>();
	
	protected HashMap<QuestionDef,List<RuntimeWidgetWrapper>> calcWidgetMap = new HashMap<QuestionDef,List<RuntimeWidgetWrapper>>();
	
	/**
	 * A map of filtered single select dynamic questions and their corresponding 
	 * non label widgets. Only questions of single select dynamic which have the
	 * widget filter property set are put in this list
	 */
	protected HashMap<QuestionDef,RuntimeWidgetWrapper> filtDynOptWidgetMap = new HashMap<QuestionDef,RuntimeWidgetWrapper>();
	
	
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
		else{
			//FormUtil.maximizeWidget(selectedPanel);	
			initWidget(selectedPanel);
		}
		//setupEventListeners();

		//table.setStyleName("cw-FlexTable");
		this.addStyleName("purcforms-repeat-border");
	}

	//TODO The code below needs great refactoring together with PreviewView
	private RuntimeWidgetWrapper getParentWrapper(Widget widget, Element node, String parentBinding){
		RuntimeWidgetWrapper parentWrapper = widgetMap.get(parentBinding);
		if(parentWrapper == null){
			QuestionDef qtn = null;
			if(repeatQtnsDef != null)
				qtn = repeatQtnsDef.getQuestion(parentBinding);
			else
				qtn = formDef.getQuestion(parentBinding);

			if(qtn != null){
				parentWrapper = new RuntimeWidgetWrapper(widget,images.error(),editListener);
				parentWrapper.setQuestionDef(qtn,true);
				widgetMap.put(parentBinding, parentWrapper);
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

	public void loadWidgets(FormDef formDef,NodeList nodes, List<RuntimeWidgetWrapper> externalSourceWidgets,
			HashMap<QuestionDef,List<QuestionDef>> calcQtnMappings, HashMap<QuestionDef,List<RuntimeWidgetWrapper>> calcWidgetMap,
			HashMap<QuestionDef,RuntimeWidgetWrapper> filtDynOptWidgetMap){
		
		HashMap<Integer,RuntimeWidgetWrapper> widgetMap = new HashMap<Integer,RuntimeWidgetWrapper>();
		int maxTabIndex = 0;

		for(int i=0; i<nodes.getLength(); i++){
			if(nodes.item(i).getNodeType() != Node.ELEMENT_NODE)
				continue;
			try{
				Element node = (Element)nodes.item(i);
				int index = loadWidget(formDef,node,widgetMap,externalSourceWidgets,calcQtnMappings, calcWidgetMap, filtDynOptWidgetMap);
				if(index > maxTabIndex)
					maxTabIndex = index;
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}

		//We are adding widgets to the panel according to the tab index.
		for(int index = 0; index <= maxTabIndex; index++){
			RuntimeWidgetWrapper widget = widgetMap.get(new Integer(index));
			if(widget != null)
				addWidget(widget);
		}

		if(isRepeated){
			
			DeferredCommand.addCommand(new Command() {
				public void execute() {
					RuntimeWidgetWrapper widget = widgets.get(0);
					if(!(widget.getQuestionDef() == null || widget.getQuestionDef().getDataNode() == null)){
						/*Element dataNode = (Element)widget.getQuestionDef().getDataNode().getParentNode();
						Element parent = (Element)dataNode.getParentNode();
						NodeList nodeList = parent.getElementsByTagName(dataNode.getNodeName());*/

						Element repeatDataNode = getParentNode(widget.getQuestionDef().getDataNode(),(widget.getWrappedWidget() instanceof CheckBox) ? widget.getParentBinding() : widget.getBinding());
						Element parent = (Element)repeatDataNode.getParentNode();
						NodeList nodeList = parent.getElementsByTagName(repeatDataNode.getNodeName());


						RuntimeWidgetWrapper wrapper = (RuntimeWidgetWrapper)getParent().getParent();
						int y = getHeightInt();

						for(int index = 1; index < nodeList.getLength(); index++)
							addNewRow((Element)nodeList.item(index));

						editListener.onRowAdded(wrapper,getHeightInt()-y);

					}
				}
			});	
		}

		//Now add the button widgets, if any.
		if(isRepeated){
			HorizontalPanel panel = new HorizontalPanel();
			panel.setSpacing(5);
			for(int index = 0; index < buttons.size(); index++)
				panel.add(buttons.get(index));
			verticalPanel.add(panel);

			addDeleteButton(0);

			FormUtil.maximizeWidget(panel);
		}
		else{
			for(int index = 0; index < buttons.size(); index++){
				RuntimeWidgetWrapper widget = buttons.get(index);
				selectedPanel.add(widget);
				FormUtil.setWidgetPosition(widget,widget.getLeft(),widget.getTop());
				//FormUtil.setWidgetPosition(selectedPanel,widget,widget.getLeft(),widget.getTop());
			}
		}
	}

	private void addDeleteButton(int row){
		PushButton btn = new PushButton(LocaleText.get("deleteItem"));
		btn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event){
				removeRow((Widget)event.getSource());
			}
		});
		table.setWidget(row, widgets.size(), btn);
	}


	private void removeRow(Widget sender){
		if(table.getRowCount() == 1){//There should be atleast one row{
			clearValue();
			return;
		}

		for(int row = 1; row < table.getRowCount(); row++){
			if(sender == table.getWidget(row, widgets.size())){

				RuntimeWidgetWrapper wrapper = (RuntimeWidgetWrapper)getParent().getParent();
				int y = getHeightInt();

				table.removeRow(row);
				Element node = dataNodes.get(row-1);
				node.getParentNode().removeChild(node);
				dataNodes.remove(node);
				if(btnAdd != null)
					btnAdd.setEnabled(true);

				editListener.onRowRemoved(wrapper,y-getHeightInt());

				RuntimeWidgetWrapper parent = (RuntimeWidgetWrapper)getParent().getParent();
				ValidationRule validationRule = parent.getValidationRule();
				if(validationRule != null)
					parent.getQuestionDef().setAnswer(table.getRowCount()+"");
			}
		}
	}

	private int loadWidget(FormDef formDef, Element node,HashMap<Integer,RuntimeWidgetWrapper> widgets, List<RuntimeWidgetWrapper> externalSourceWidgets,
			HashMap<QuestionDef,List<QuestionDef>> calcQtnMappings,HashMap<QuestionDef,List<RuntimeWidgetWrapper>> calcWidgetMap,
			HashMap<QuestionDef,RuntimeWidgetWrapper> filtDynOptWidgetMap){
		
		RuntimeWidgetWrapper parentWrapper = null;

		String s = node.getAttribute(WidgetEx.WIDGET_PROPERTY_WIDGETTYPE);
		int tabIndex = (node.getAttribute(WidgetEx.WIDGET_PROPERTY_TABINDEX) != null ? Integer.parseInt(node.getAttribute(WidgetEx.WIDGET_PROPERTY_TABINDEX)) : 0);

		QuestionDef questionDef = null;
		String binding = node.getAttribute(WidgetEx.WIDGET_PROPERTY_BINDING);
		String parentBinding = node.getAttribute(WidgetEx.WIDGET_PROPERTY_PARENTBINDING);

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
			/*widget = new RadioButton(parentBinding,node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));
			parentWrapper = getParentWrapper(widget,node);
			((RadioButton)widget).setTabIndex(tabIndex);*/

			widget = new RadioButtonWidget(parentBinding,node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));

			if(widgetMap.get(parentBinding) == null)
				wrapperSet = true;

			parentWrapper = getParentWrapper(widget,node,parentBinding);
			((RadioButton)widget).setTabIndex(tabIndex);

			if(wrapperSet){
				wrapper = parentWrapper;
				questionDef = formDef.getQuestion(parentBinding);
			}
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_CHECKBOX)){
			/*widget = new CheckBox(node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));
			parentWrapper = getParentWrapper(widget,node);
			((CheckBox)widget).setTabIndex(tabIndex);*/

			widget = new CheckBoxWidget(node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));
			if(widgetMap.get(parentBinding) == null)
				wrapperSet = true;

			parentWrapper = getParentWrapper(widget,node,parentBinding);
			((CheckBox)widget).setTabIndex(tabIndex);

			String defaultValue = parentWrapper.getQuestionDef().getDefaultValue();
			if(defaultValue != null && defaultValue.contains(binding))
				((CheckBox)widget).setValue(true);

			if(wrapperSet){
				wrapper = parentWrapper;
				questionDef = formDef.getQuestion(parentBinding);
			}
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_BUTTON)){
			widget = new Button(node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));
			((Button)widget).setTabIndex(tabIndex);
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_LISTBOX)){
			widget = new ListBoxWidget(false);
			((ListBox)widget).setTabIndex(tabIndex);
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_TEXTAREA)){
			widget = new TextArea();
			((TextArea)widget).setTabIndex(tabIndex);
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_DATEPICKER)){
			widget = new DatePickerWidget();
			((DatePickerEx)widget).setTabIndex(tabIndex);
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_DATETIME)){
			widget = new DateTimeWidget();
			((DateTimeWidget)widget).setTabIndex(tabIndex);
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_TIME)){
			widget = new TimeWidget();
			((TimeWidget)widget).setTabIndex(tabIndex);
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
				labelText.put((Label)widget, text);
				labelReplaceText.put((Label)widget, "${"+varname+"}$");

				((Label)widget).setText(text.replace("${"+varname+"}$", ""));
				if(varname.startsWith("/"+ formDef.getVariableName()+"/"))
					varname = varname.substring(("/"+ formDef.getVariableName()+"/").length(),varname.length());

				QuestionDef qtnDef = formDef.getQuestion(varname);
				List<Label> labels = labelMap.get(qtnDef);
				if(labels == null){
					labels = new ArrayList<Label>();
					labelMap.put(qtnDef, labels);
				}
				labels.add((Label)widget);
			}
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_IMAGE)){
			widget = new Image();
			String xpath = binding;
			if(!xpath.startsWith(formDef.getVariableName()))
				xpath = "/" + formDef.getVariableName() + "/" + binding;
			((Image)widget).setUrl(URL.encode(FormUtil.getMultimediaUrl()+"?formId="+formDef.getId()+"&xpath="+xpath+"&time="+ new java.util.Date().getTime()));
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

			((HTML)widget).setHTML("<a href=" + URL.encode(FormUtil.getMultimediaUrl()+extension + "?formId="+formDef.getId()+"&xpath="+xpath+contentType+"&time="+ new java.util.Date().getTime()) + ">"+node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT)+"</a>");

			String answer = questionDef.getAnswer();
			if(answer == null || answer.trim().length() == 0 )
				((HTML)widget).setVisible(false);
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_GROUPBOX)||s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_REPEATSECTION)){
			RepeatQtnsDef repeatQtnsDef = null;
			if(questionDef != null)
				repeatQtnsDef = questionDef.getRepeatQtnsDef();

			boolean repeated = false;
			String value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_REPEATED);
			if(value != null && value.trim().length() > 0)
				repeated = (value.equals(WidgetEx.REPEATED_TRUE_VALUE));

			widget = new RuntimeGroupWidget(images,formDef,repeatQtnsDef,editListener,repeated);
			((RuntimeGroupWidget)widget).loadWidgets(formDef,node.getChildNodes(),externalSourceWidgets,calcQtnMappings,calcWidgetMap,filtDynOptWidgetMap);
			/*getLabelMap(((RuntimeGroupWidget)widget).getLabelMap());
			getLabelText(((RuntimeGroupWidget)widget).getLabelText());
			getLabelReplaceText(((RuntimeGroupWidget)widget).getLabelReplaceText());
			getCheckBoxGroupMap(((RuntimeGroupWidget)widget).getCheckBoxGroupMap());*/
		}

		/*else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_REPEATSECTION)){
			//Not dealing with nested repeats
			//widget = new RunTimeGroupWidget();
			//((RunTimeGroupWidget)widget).setTabIndex(tabIndex);
		}*/
		else
			return tabIndex;

		if(!wrapperSet){
			wrapper = new RuntimeWidgetWrapper(widget,images.error(),editListener);

			if(parentWrapper != null){ //Check box or radio button
				if(!parentWrapper.getQuestionDef().isVisible())
					wrapper.setVisible(false);
				if(!parentWrapper.getQuestionDef().isEnabled())
					wrapper.setEnabled(false);
				if(parentWrapper.getQuestionDef().isLocked())
					wrapper.setLocked(true);
			}
		}

		//RuntimeWidgetWrapper wrapper = new RuntimeWidgetWrapper(widget,images.error(),editListener);
		boolean loadWidget = true;

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
		
		value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_FILTERFIELD);
		if(value != null && value.trim().length() > 0)
			wrapper.setFilterField(value);
		
		value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_ID);
		if(value != null && value.trim().length() > 0)
			wrapper.setId(value);
		
		if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_VIDEO_AUDIO) || s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_IMAGE)){
			if(binding != null && binding.trim().length() > 0){
				questionDef = formDef.getQuestion(binding);
				if(questionDef != null)
					questionDef.setAnswer(questionDef.getDefaultValue()); //Just incase we are refreshing and had already set the answer
			}
		}

		if(questionDef != null){
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC){
				questionDef.setOptions(null); //may have been set by the preview
				//if(wrapper.getWrappedWidget() instanceof ListBox || wrapper.getWrappedWidget() instanceof TextBox)
				if(wrapper.getFilterField() != null && wrapper.getFilterField().trim().length() > 0)
					filtDynOptWidgetMap.put(questionDef, wrapper);
			}
			
			wrapper.setQuestionDef(questionDef,false);
			ValidationRule validationRule = formDef.getValidationRule(questionDef);
			wrapper.setValidationRule(validationRule);
		}
		
		if(parentBinding != null)
			wrapper.setParentBinding(parentBinding);
		
		if(binding != null)
			wrapper.setBinding(binding);

		if(parentWrapper != null)
			parentWrapper.addChildWidget(wrapper);
		

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

		//if(wrapper.getWrappedWidget() instanceof Label)
		WidgetEx.loadLabelProperties(node,wrapper);

		wrapper.setTabIndex(tabIndex);
		//wrapper.setParentBinding(parentBinding);

		if(tabIndex > 0 && !(wrapper.getWrappedWidget() instanceof Button))
			widgets.put(new Integer(tabIndex), wrapper);
		else
			addWidget(wrapper);

		if(wrapperSet)
			;//FormUtil.setWidgetPosition(wrapper,left,top);

		if(widget instanceof Button && binding != null){
			//wrapper.setParentBinding(parentBinding);

			if(binding.equals("addnew")||binding.equals("remove") || binding.equals("submit") ||
					binding.equals("browse")||binding.equals("clear")||binding.equals("cancel")||binding.equals("search")){
				((Button)widget).addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event){
						execute((Widget)event.getSource());
					}
				});
			}
		}
		
		if(wrapper.isEditable() && questionDef != null)
			FormRunnerView.updateCalcWidgetMapping(wrapper, calcQtnMappings, calcWidgetMap);

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

			//Ensure that the Add New and Remove buttons are displayed according to tab index
			if(buttons.size() == 0 || (buttons.get(0).getTabIndex() <= wrapper.getTabIndex()))
				buttons.add(wrapper);
			else{
				RuntimeWidgetWrapper w = buttons.remove(0);
				buttons.add(wrapper);
				buttons.add(w);
			}
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
			//FormUtil.setWidgetPosition(selectedPanel,wrapper,wrapper.getLeft(),wrapper.getTop());
		}
	}

	private void execute(Widget sender){
		String binding = ((RuntimeWidgetWrapper)sender.getParent().getParent()).getBinding();

		if(binding.equalsIgnoreCase("search")){
			RuntimeWidgetWrapper wrapper = getCurrentMultimediWrapper(sender);
			if(wrapper != null && wrapper.getExternalSource() != null)
				;//FormUtil.searchExternal(wrapper.getExternalSource(),sender.getElement(),wrapper.getWrappedWidget().getElement(),null);
		}
		else if(binding.equalsIgnoreCase("submit"))
			((FormRunnerView)getParent().getParent().getParent().getParent().getParent().getParent().getParent()).onSubmit();
		else if(binding.equalsIgnoreCase("cancel"))
			((FormRunnerView)getParent().getParent().getParent().getParent().getParent().getParent().getParent()).onCancel();
		else if(repeatQtnsDef != null){
			if(binding.equalsIgnoreCase("addnew")){
				RuntimeWidgetWrapper wrapper = (RuntimeWidgetWrapper)getParent().getParent();
				int y = getHeightInt();

				addNewRow(sender);

				editListener.onRowAdded(wrapper,getHeightInt()-y);
			}
			else if(binding.equalsIgnoreCase("remove")){
				if(table.getRowCount() > 1){//There should be atleast one row{
					RuntimeWidgetWrapper wrapper = (RuntimeWidgetWrapper)getParent().getParent();
					int y = getHeightInt();

					table.removeRow(table.getRowCount()-1);
					Element node = dataNodes.get(dataNodes.size() - 1);
					node.getParentNode().removeChild(node);
					dataNodes.remove(node);
					if(btnAdd != null)
						btnAdd.setEnabled(true);

					editListener.onRowRemoved(wrapper,y-getHeightInt());
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
				else if(wrapper.getWrappedWidget() instanceof Label)
					((Label)wrapper.getWrappedWidget()).setText(LocaleText.get("noSelection"));
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
				if(wrapper == null)
					return;

				if(wrapper.getWrappedWidget() instanceof Image)
					image = (Image)wrapper.getWrappedWidget();
				else
					html = (HTML)wrapper.getWrappedWidget();

				String xpath = wrapper.getBinding();
				if(!xpath.startsWith(formDef.getVariableName()))
					xpath = "/" + formDef.getVariableName() + "/" + wrapper.getBinding();

				String contentType = "&contentType=video/3gpp";
				contentType += "&name="+wrapper.getQuestionDef().getVariableName()+".3gp";

				//TODO What if the multimedia url suffix already has a ?
				String url = FormUtil.getMultimediaUrl()+"?formId="+formDef.getId()+"&xpath="+xpath+contentType+"&time="+ new java.util.Date().getTime();
				OpenFileDialog dlg = new OpenFileDialog(this,url);
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
			Widget wrappedWidget = widget.getWrappedWidget();
			if(wrappedWidget instanceof Image || wrappedWidget instanceof HTML || wrappedWidget instanceof Label){
				String binding  = widget.getBinding();
				if(binding != null && binding.equalsIgnoreCase(button.getParentBinding()))
					return widget;
			}
		}
		return null;
	}

	private void addNewRow(Widget sender){
		HashMap<String,RuntimeWidgetWrapper> widgetMap = new HashMap<String,RuntimeWidgetWrapper>();

		Element newRepeatDataNode = null;
		int row = table.getRowCount();
		for(int index = 0; index < widgets.size(); index++){
			RuntimeWidgetWrapper mainWidget = widgets.get(index);
			RuntimeWidgetWrapper copyWidget = getPreparedWidget(mainWidget,false);

			//table.setWidget(row, index, copyWidget);

			if(index == 0){
				Element dataNode = mainWidget.getQuestionDef().getDataNode();
				if(dataNode == null){
					Window.alert("Please first save the form"); //LocaleText.get("?????");
					return; //possibly form not yet saved
				}

				Element repeatDataNode = getParentNode(dataNode,(mainWidget.getWrappedWidget() instanceof CheckBox) ? mainWidget.getParentBinding() : mainWidget.getBinding());
				newRepeatDataNode = (Element)repeatDataNode.cloneNode(true);
				repeatDataNode.getParentNode().appendChild(newRepeatDataNode);
				//workonDefaults(newRepeatDataNode);
				dataNodes.add(newRepeatDataNode);
			}

			table.setWidget(row, index, copyWidget);

			setDataNode(copyWidget,newRepeatDataNode,copyWidget.getBinding(),false);
			
			//Loading widget from here instead of in getPreparedWidget because setDataNode may clear default values
			copyWidget.loadQuestion();

			if(copyWidget.getWrappedWidget() instanceof RadioButton)
				((RadioButton)copyWidget.getWrappedWidget()).setName(((RadioButton)copyWidget.getWrappedWidget()).getName()+row);

			if(copyWidget.getWrappedWidget() instanceof CheckBox){
				RuntimeWidgetWrapper widget = widgetMap.get(copyWidget.getParentBinding());
				if(widget == null){
					widget = copyWidget;
					widgetMap.put(copyWidget.getParentBinding(), widget);
				}
				widget.addChildWidget(copyWidget);
			}
		}

		addDeleteButton(row);

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

		addDeleteButton(row);
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
		if(widget.getQuestionDef() == null)
			return; //for checkboxes, only the first may have reference to the parent questiondef

		String name = (widget.getWrappedWidget() instanceof CheckBox) ? widget.getParentBinding() : binding;
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
						widget.getQuestionDef().setDefaultValue(XmlUtil.getTextValue((Element)child));
						widget.loadQuestion();
					}
					else{
						((Element)child).setAttribute("new", XformConstants.XPATH_VALUE_TRUE);
						if(XformConstants.XPATH_VALUE_FALSE.equals(((Element)child).getAttribute("default")))
							widget.getQuestionDef().setDefaultValue(null);
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
				for(int col = 0; col < table.getCellCount(row)-1; col++)
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
				for(int col = 0; col < table.getCellCount(row)-1; col++)
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
				for(int col = 0; col < table.getCellCount(row)-1; col++)
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
			for(int col = 0; col < table.getCellCount(row)-1; col++){
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
				image.setUrl(FormUtil.getMultimediaUrl()+"?action=recentbinary&time="+ new java.util.Date().getTime()+"&formId="+formDef.getId()+"&xpath="+xpath);
			else{
				String extension = "";//.3gp ".mpeg";
				String contentType = "&contentType=video/3gpp";
				if(widgetWrapper.getQuestionDef().getDataType() == QuestionDef.QTN_TYPE_AUDIO)
					contentType = "&contentType=audio/3gpp"; //"&contentType=audio/x-wav";
				//extension = ".wav";

				contentType += "&name="+widgetWrapper.getQuestionDef().getVariableName()+".3gp";

				html.setVisible(true);
				html.setHTML("<a href=" + URL.encode(FormUtil.getMultimediaUrl()+extension + "?formId="+formDef.getId()+"&xpath="+xpath+contentType+"&time="+ new java.util.Date().getTime()) + ">"+html.getText()+"</a>");				
			}

			widgetWrapper.getQuestionDef().setAnswer(contents);
		}
	}

	public void clearValue(){
		if(isRepeated){
			while(table.getRowCount() > 1)
				table.removeRow(1);

			for(int col = 0; col < table.getCellCount(0)-1; col++)
				((RuntimeWidgetWrapper)table.getWidget(0, col)).clearValue();
		}
		else{
			for(int index = 0; index < selectedPanel.getWidgetCount(); index++)
				((RuntimeWidgetWrapper)selectedPanel.getWidget(index)).clearValue();
		}
	}

	public boolean isValid(){
		firstInvalidWidget = null;

		if(isRepeated){
			for(int row = 0; row < table.getRowCount(); row++){
				for(int col = 0; col < table.getCellCount(row)-1; col++){
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
				for(int col = 0; col < table.getCellCount(row)-1; col++){
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
		
		if(index == -1){
			//Handle tabbing for repeats within the flex table
			if(isRepeated){
				boolean found = false;
				for(int row = 0; row < table.getRowCount(); row++){
					for(int col = 0; col < table.getCellCount(row); col++){
						if(found){
							Widget curWidget = table.getWidget(row, col);
							if(curWidget instanceof RuntimeWidgetWrapper && ((RuntimeWidgetWrapper)curWidget).setFocus())
								return true;
						}
						
						if(table.getWidget(row, col) == widget)
							found = true;
					}
				}
			}
			
			return false;
		}
		
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

	public HashMap<QuestionDef,List<Label>> getLabelMap(){
		return labelMap;
	}
	
	public HashMap<QuestionDef,List<RuntimeWidgetWrapper>> getCalcWidgetMap(){
		return calcWidgetMap;
	}
	
	public HashMap<QuestionDef,RuntimeWidgetWrapper> getFiltDynOptWidgetMap(){
		return filtDynOptWidgetMap;
	}

	public HashMap<Label,String> getLabelText(){
		return labelText;
	}

	public HashMap<Label,String> getLabelReplaceText(){
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
				checkBox.setValue(false);
		}
	}

	public void onVisibleChanged(QuestionDef sender,boolean visible){
		List<CheckBox> list = checkBoxGroupMap.get(sender);
		if(list == null)
			return;

		for(CheckBox checkBox : list){
			checkBox.setVisible(visible);
			if(!visible)
				checkBox.setValue(false);
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

	public int getHeightInt(){
		return getElement().getOffsetHeight();
	}
	
	/*private void workonDefaults(Node repeatDataNode){
		NodeList nodes = repeatDataNode.getChildNodes();
		for(int index = 0; index < nodes.getLength(); index++){
			Node child = nodes.item(index);
			if(child.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			if(XformConstants.XPATH_VALUE_FALSE.equals(((Element)child).getAttribute("default")))
				XmlUtil.setTextValue((Element)child, "");
			
			workonDefaults(child);
		}
	}*/
}
