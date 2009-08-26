package org.purc.purcforms.client.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;

import org.purc.purcforms.client.controller.QuestionChangeListener;
import org.purc.purcforms.client.controller.SubmitListener;
import org.purc.purcforms.client.model.DynamicOptionDef;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.OptionDef;
import org.purc.purcforms.client.model.PageDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.model.RepeatQtnsDef;
import org.purc.purcforms.client.model.SkipRule;
import org.purc.purcforms.client.model.ValidationRule;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.widget.DatePickerWidget;
import org.purc.purcforms.client.widget.EditListener;
import org.purc.purcforms.client.widget.RuntimeGroupWidget;
import org.purc.purcforms.client.widget.RuntimeWidgetWrapper;
import org.purc.purcforms.client.widget.WidgetEx;
import org.purc.purcforms.client.xforms.XformConverter;
import org.zenika.widget.client.datePicker.DatePicker;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;


/**
 * 
 * @author daniel
 *
 */
public class FormRunnerView extends Composite implements /*WindowResizeListener,*/TabListener, EditListener,QuestionChangeListener{

	public interface Images extends ImageBundle {
		AbstractImagePrototype error();
	}

	protected final Images images;

	protected DecoratedTabPanel tabs = new DecoratedTabPanel();
	protected int selectedTabIndex;
	protected AbsolutePanel selectedPanel;
	protected String sHeight = "100%";
	protected FormDef formDef;
	protected SubmitListener submitListener;
	protected HashMap<String,RuntimeWidgetWrapper> widgetMap;
	protected RuntimeWidgetWrapper firstInvalidWidget;

	protected int embeddedHeightOffset = 0;

	protected HashMap<QuestionDef,List<Widget>> labelMap;
	protected HashMap<Widget,String> labelText;
	protected HashMap<Widget,String> labelReplaceText;
	
	protected HashMap<QuestionDef,List<CheckBox>> checkBoxGroupMap;

	public FormRunnerView(Images images){
		this.images = images;

		FormUtil.maximizeWidget(tabs);

		initWidget(tabs);
		tabs.addTabListener(this);

		//Window.addWindowResizeListener(this);

		//		This is needed for IE
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				//onWindowResized(Window.getClientWidth(), Window.getClientHeight());
				setHeight(getHeight());
			}
		});
	}

	/**
	 * This function is called when on selected the refresh button on the runner view
	 * 
	 * @param formDef the form definition to load
	 * @param layoutXml the form widget layout xml
	 * @param externalSourceWidgets a list of widgets which get their data from sources external to the xform
	 */
	public void loadForm(FormDef formDef,String layoutXml, List<RuntimeWidgetWrapper> externalSourceWidgets){
		if(formDef == null)
			this.formDef = null;
		else{
			this.formDef = new FormDef(formDef); //TODO make sure using a copy of the passed object does not introduce bugs.
			
			//set the document xml which we shall need for updating the model with question answers
			XformConverter.fromFormDef2Xform(this.formDef);
		}
		
		tabs.clear();
		if(formDef == null || layoutXml == null || layoutXml.trim().length() == 0){
			addNewTab("Page1");
			return;
		}

		loadLayout(layoutXml,externalSourceWidgets);
		moveToFirstWidget();
	}

	public void moveToFirstWidget(){
		moveToNextWidget(-1);
	}

	protected void moveToNextWidget(int index){
		while(++index < selectedPanel.getWidgetCount()){
			if(((RuntimeWidgetWrapper)selectedPanel.getWidget(index)).setFocus())
				break;
		}
	}

	public void loadLayout(String xml, List<RuntimeWidgetWrapper> externalSourceWidgets){
		tabs.clear();

		if(formDef != null)
			formDef.clearChangeListeners();

		widgetMap = new HashMap<String,RuntimeWidgetWrapper>();
		labelMap = new HashMap<QuestionDef,List<Widget>>();
		labelText = new HashMap<Widget,String>();
		labelReplaceText = new HashMap<Widget,String>();
		checkBoxGroupMap = new HashMap<QuestionDef,List<CheckBox>>();

		com.google.gwt.xml.client.Document doc = XMLParser.parse(xml);
		Element root = doc.getDocumentElement();
		NodeList pages = root.getChildNodes();
		for(int i=0; i<pages.getLength(); i++){
			if(pages.item(i).getNodeType() != Node.ELEMENT_NODE)
				continue;
			Element node = (Element)pages.item(i);
			addNewTab(node.getAttribute("Text"));
			
			setWidth(node.getAttribute(WidgetEx.WIDGET_PROPERTY_WIDTH));
			setHeight(node.getAttribute(WidgetEx.WIDGET_PROPERTY_HEIGHT));
			setBackgroundColor(node.getAttribute(WidgetEx.WIDGET_PROPERTY_BACKGROUND_COLOR));
			
			/*selectedPanel.setWidth(node.getAttribute(WidgetEx.WIDGET_PROPERTY_WIDTH));
			selectedPanel.setHeight(node.getAttribute(WidgetEx.WIDGET_PROPERTY_HEIGHT));
			try{
				DOM.setStyleAttribute(selectedPanel.getElement(), "backgroundColor", node.getAttribute(WidgetEx.WIDGET_PROPERTY_BACKGROUND_COLOR));
			}catch(Exception ex){}*/

			loadPage(node.getChildNodes(),externalSourceWidgets);
		}

		if(formDef != null)
			fireRules();

		updateDynamicOptions();

		if(tabs.getWidgetCount() > 0)
			tabs.selectTab(0);
	}

	protected void initPanel(){
		AbsolutePanel panel = new AbsolutePanel();
		FormUtil.maximizeWidget(panel);
		selectedPanel = panel;

		//This is needed for IE
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				//onWindowResized(Window.getClientWidth(), Window.getClientHeight());
				setHeight(getHeight());
			}
		});
	}

	protected void addNewTab(String name){
		initPanel();
		if(name == null)
			name = "Page"+tabs.getWidgetCount();
		if(name.indexOf("</") > 0)
			name = name.substring(name.indexOf(">")+1,name.indexOf("</"));
		tabs.add(selectedPanel, name);
		selectedPanel.setHeight(sHeight);
		selectedTabIndex = tabs.getWidgetCount() - 1;
		tabs.selectTab(selectedTabIndex);

		DeferredCommand.addCommand(new Command() {
			public void execute() {
				//onWindowResized(Window.getClientWidth(), Window.getClientHeight());
				setHeight(getHeight());
			}
		});
	}

	protected void loadPage(NodeList nodes, List<RuntimeWidgetWrapper> externalSourceWidgets){
		HashMap<Integer,RuntimeWidgetWrapper> widgets = new HashMap<Integer,RuntimeWidgetWrapper>();
		int maxTabIndex = 0;

		//RuntimeWidgetWrapper wrapper = new RuntimeWidgetWrapper(null,null,null);
		for(int i=0; i<nodes.getLength(); i++){
			if(nodes.item(i).getNodeType() != Node.ELEMENT_NODE)
				continue;
			try{
				Element node = (Element)nodes.item(i);
				int index = loadWidget(node,widgets,externalSourceWidgets);
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
				selectedPanel.add(widget);
		}
	}

	protected int loadWidget(Element node,HashMap<Integer,RuntimeWidgetWrapper> widgets, List<RuntimeWidgetWrapper> externalSourceWidgets){
		RuntimeWidgetWrapper parentWrapper = null;

		String left = node.getAttribute(WidgetEx.WIDGET_PROPERTY_LEFT);
		String top = node.getAttribute(WidgetEx.WIDGET_PROPERTY_TOP);
		String s = node.getAttribute(WidgetEx.WIDGET_PROPERTY_WIDGETTYPE);
		int tabIndex = (node.getAttribute(WidgetEx.WIDGET_PROPERTY_TABINDEX) != null ? Integer.parseInt(node.getAttribute(WidgetEx.WIDGET_PROPERTY_TABINDEX)) : 0);

		QuestionDef questionDef = null;
		String binding = node.getAttribute(WidgetEx.WIDGET_PROPERTY_BINDING);
		if(binding != null && binding.trim().length() > 0){
			questionDef = formDef.getQuestion(binding);
			if(questionDef != null)
				questionDef.setAnswer(questionDef.getDefaultValue()); //Just incase we are refreshing and had already set the answer
		}

		RuntimeWidgetWrapper wrapper = null;
		boolean wrapperSet = false;
		Widget widget = null;
		if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_RADIOBUTTON)){
			widget = new RadioButton(node.getAttribute(WidgetEx.WIDGET_PROPERTY_PARENTBINDING),node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));

			if(widgetMap.get(node.getAttribute(WidgetEx.WIDGET_PROPERTY_PARENTBINDING)) == null)
				wrapperSet = true;

			parentWrapper = getParentWrapper(widget,node);
			((RadioButton)widget).setTabIndex(tabIndex);

			if(wrapperSet)
				wrapper = parentWrapper;
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_CHECKBOX)){
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
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_GROUPBOX)||s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_REPEATSECTION)){
			RepeatQtnsDef repeatQtnsDef = null;
			if(questionDef != null)
				repeatQtnsDef = questionDef.getRepeatQtnsDef();

			boolean repeated = false;
			String value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_REPEATED);
			if(value != null && value.trim().length() > 0)
				repeated = (value.equals(WidgetEx.REPEATED_TRUE_VALUE));

			widget = new RuntimeGroupWidget(images,formDef,repeatQtnsDef,this,repeated);
			((RuntimeGroupWidget)widget).loadWidgets(formDef,node.getChildNodes(),externalSourceWidgets);
			//((RuntimeGroupWidget)widget).setTabIndex(tabIndex);
			getLabelMap(((RuntimeGroupWidget)widget).getLabelMap());
			getLabelText(((RuntimeGroupWidget)widget).getLabelText());
			getLabelReplaceText(((RuntimeGroupWidget)widget).getLabelReplaceText());
			getCheckBoxGroupMap(((RuntimeGroupWidget)widget).getCheckBoxGroupMap());
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_IMAGE)){
			widget = new Image();
			String xpath = binding;
			if(!xpath.startsWith(formDef.getVariableName()))
				xpath = "/" + formDef.getVariableName() + "/" + binding;
			((Image)widget).setUrl(URL.encode(FormUtil.getMultimediaUrlSuffix()+"?formId="+formDef.getId()+"&xpath="+xpath));
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_VIDEO_AUDIO) && questionDef != null){
			String answer = questionDef.getAnswer();
			if(answer != null && answer.trim().length() !=0 ){
				widget = new HTML();
				if(binding != null && binding.trim().length() > 0){
					String xpath = binding;
					if(!xpath.startsWith(formDef.getVariableName()))
						xpath = "/" + formDef.getVariableName() + "/" + binding;

					String extension = "";//.3gp ".mpeg";
					String contentType = "&contentType=video/3gpp";
					if(questionDef.getDataType() == QuestionDef.QTN_TYPE_AUDIO)
						contentType = "&contentType=audio/x-wav";

					((HTML)widget).setHTML("<a href=" + URL.encode(FormUtil.getMultimediaUrlSuffix()+extension+"?formId="+formDef.getId()+"&xpath="+xpath+contentType) + ">"+node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT)+"</a>");
				}
			}
		}
		else
			return tabIndex;

		if(!wrapperSet)
			wrapper = new RuntimeWidgetWrapper(widget,images.error(),this);

		boolean loadWidget = true;

		if(questionDef != null){
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC)
				questionDef.setOptions(null); //may have been set by the preview
			wrapper.setQuestionDef(questionDef,false);
			ValidationRule validationRule = formDef.getValidationRule(questionDef);
			wrapper.setValidationRule(validationRule);
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT && validationRule != null)
				questionDef.setAnswer("0");
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

		//if(wrapper.getWrappedWidget() instanceof Label)
		WidgetEx.loadLabelProperties(node,wrapper);

		if(tabIndex > 0)
			widgets.put(new Integer(tabIndex), wrapper);
		else
			selectedPanel.add(wrapper);
		FormUtil.setWidgetPosition(wrapper,left,top);

		if(widget instanceof Button && binding != null && binding.equals("submit")){
			((Button)widget).addClickListener(new ClickListener(){
				public void onClick(Widget sender){
					submit();
				}
			});
		}

		return tabIndex;
	}

	protected RuntimeWidgetWrapper getParentWrapper(Widget widget, Element node){
		RuntimeWidgetWrapper parentWrapper = widgetMap.get(node.getAttribute(WidgetEx.WIDGET_PROPERTY_PARENTBINDING));
		if(parentWrapper == null){
			QuestionDef qtn = formDef.getQuestion(node.getAttribute(WidgetEx.WIDGET_PROPERTY_PARENTBINDING));
			if(qtn != null){
				parentWrapper = new RuntimeWidgetWrapper(widget,images.error(),this);
				parentWrapper.setQuestionDef(qtn,true);
				widgetMap.put(node.getAttribute(WidgetEx.WIDGET_PROPERTY_PARENTBINDING), parentWrapper);
				selectedPanel.add(parentWrapper);
				
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

	protected void submit(){
		if(formDef != null){
			if(formDef.getDoc() == null)
				XformConverter.fromFormDef2Xform(formDef);

			saveValues();

			if(!isValid())
				return;

			String xml = XformConverter.getInstanceDataDoc(formDef.getDoc()).toString();
			xml = FormUtil.formatXml("<?xml version='1.0' encoding='UTF-8' ?> " + xml);
			submitListener.onSubmit(xml);
		}
	}

	public void onSubmit(){
		submit();
	}

	protected boolean isValid(){
		boolean valid = true;
		int pageNo = -1;
		firstInvalidWidget = null;
		for(int index=0; index<tabs.getWidgetCount(); index++){
			if(!isValid((AbsolutePanel)tabs.getWidget(index))){
				valid = false;
				if(pageNo == -1)
					pageNo = index;
			}
		}

		if(!valid){
			//Window.alert("Please first correct the errors on the form.");
			tabs.selectTab(pageNo);
			assert(firstInvalidWidget != null);
			firstInvalidWidget.setFocus();
		}
		return valid;
	}

	private boolean isValid(AbsolutePanel panel){
		boolean valid = true;
		for(int index=0; index<panel.getWidgetCount(); index++){
			RuntimeWidgetWrapper widget = (RuntimeWidgetWrapper)panel.getWidget(index);
			if(!widget.isValid()){
				valid = false;
				if(firstInvalidWidget == null && widget.isFocusable())
					firstInvalidWidget = widget.getInvalidWidget();
			}
		}
		return valid;
	}

	protected void saveValues(){
		for(int index=0; index<tabs.getWidgetCount(); index++)
			savePageValues((AbsolutePanel)tabs.getWidget(index));
	}

	protected void savePageValues(AbsolutePanel panel){
		for(int index=0; index<panel.getWidgetCount(); index++)
			((RuntimeWidgetWrapper)panel.getWidget(index)).saveValue(formDef);
	}

	public void onValueChanged(QuestionDef questionDef) {
		fireRules();
		updateDynamicOptions(questionDef);

		List<Widget> labels = labelMap.get(questionDef);
		if(labels != null){
			for(Widget widget : labels)
				((Label)widget).setText(labelText.get(widget).replace(labelReplaceText.get(widget), questionDef.getAnswer()));
		}
		
		List<CheckBox> list = checkBoxGroupMap.get(questionDef);
		if(list != null && questionDef.isRequired()){
			for(CheckBox checkBox : list)
				((RuntimeWidgetWrapper)checkBox.getParent().getParent()).isValid();
		}
	}

	public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex) {
		return true;
	}

	public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
		selectedTabIndex = tabIndex;
		selectedPanel = (AbsolutePanel)tabs.getWidget(tabIndex);
		moveToFirstWidget();
	}

	public void setSubmitListener(SubmitListener submitListener){
		this.submitListener = submitListener;
	}

	public boolean isPreviewing(){
		return tabs.getWidgetCount() > 0;
	}

	public void onMoveToNextWidget(Widget widget) {
		if(widget.getParent().getParent() instanceof RuntimeGroupWidget){
			if(((RuntimeGroupWidget)widget.getParent().getParent()).onMoveToNextWidget(widget))
				return;
			else
				widget = widget.getParent().getParent().getParent().getParent();
		}

		int index = selectedPanel.getWidgetIndex(widget);
		moveToNextWidget(index);
	}

	public void onMoveToPrevWidget(Widget widget){
		boolean moved = false;

		if(widget.getParent().getParent() instanceof RuntimeGroupWidget){
			if(((RuntimeGroupWidget)widget.getParent().getParent()).onMoveToPrevWidget(widget))
				return;
			else
				widget = widget.getParent().getParent().getParent().getParent();
		}


		int index = selectedPanel.getWidgetIndex(widget);
		while(--index > 0){
			if(((RuntimeWidgetWrapper)selectedPanel.getWidget(index)).setFocus()){
				moved = true;
				break;
			}
		}

		if(!moved && this.selectedTabIndex > 0)
			tabs.selectTab(--selectedTabIndex);
	}

	protected void fireRules(){		
		Vector rules = formDef.getSkipRules();
		if(rules != null && rules.size() > 0){
			for(int i=0; i<rules.size(); i++){
				SkipRule rule = (SkipRule)rules.elementAt(i);
				rule.fire(formDef);
			}
		}
	}

	public void clearPreview(){
		tabs.clear();
	}

	/*public void onWindowResized(int width, int height) {
		height -= (110+embeddedHeightOffset);
		sHeight = height+"px";
		super.setHeight(sHeight);

		if(selectedPanel != null)
			//selectedPanel.setHeight("100%");
			selectedPanel.setHeight(sHeight);
	}*/

	
	/**
	 * This function is called when one switches between forms in the tree view
	 */
	public void setFormDef(FormDef formDef){
		if(this.formDef != formDef){
			tabs.clear();
			addNewTab("Page1");
		}

		if(formDef == null)
			this.formDef = null;
		else
			this.formDef = new FormDef(formDef); //TODO make sure using a copy of the passed object does not introduce bugs.
	}

	public void setEmbeddedHeightOffset(int offset){
		embeddedHeightOffset = offset;
	}

	private void updateDynamicOptions(QuestionDef questionDef){
		int type = questionDef.getDataType();
		if(!(type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC))
			return;

		DynamicOptionDef dynamicOptionDef = formDef.getDynamicOptions(questionDef.getId());
		if(dynamicOptionDef == null)
			return;

		QuestionDef childQuestionDef = formDef.getQuestion(dynamicOptionDef.getQuestionId());
		if(childQuestionDef == null)
			return;

		OptionDef optionDef = questionDef.getOptionWithValue(questionDef.getAnswer());
		List<OptionDef> optionList = null;
		if(optionDef != null)
			optionList = dynamicOptionDef.getOptionList(optionDef.getId());

		childQuestionDef.setOptionList(optionList);

		onValueChanged(childQuestionDef); //do it recursively untill when no more dependent questions.
	}

	private void updateDynamicOptions(){
		for(byte i=0; i<formDef.getPages().size(); i++){
			PageDef pageDef = (PageDef)formDef.getPages().elementAt(i);
			for(byte j=0; j<pageDef.getQuestions().size(); j++)
				updateDynamicOptions((QuestionDef)pageDef.getQuestions().elementAt(j));
		}
	}

	private void getLabelMap(HashMap<QuestionDef,List<Widget>> labelMap){
		Iterator<Entry<QuestionDef,List<Widget>>> iterator = labelMap.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<QuestionDef,List<Widget>> entry = iterator.next();
			this.labelMap.put(entry.getKey(), entry.getValue());
		}
	}

	private void getLabelText(HashMap<Widget,String> labelText){
		Iterator<Entry<Widget,String>> iterator = labelText.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<Widget,String> entry = iterator.next();
			this.labelText.put(entry.getKey(), entry.getValue());
		}
	}

	private void getLabelReplaceText(HashMap<Widget,String> labelReplaceText){
		Iterator<Entry<Widget,String>> iterator = labelReplaceText.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<Widget,String> entry = iterator.next();
			this.labelReplaceText.put(entry.getKey(), entry.getValue());
		}
	}
	
	private void getCheckBoxGroupMap(HashMap<QuestionDef,List<CheckBox>> labelMap){
		Iterator<Entry<QuestionDef,List<CheckBox>>> iterator = labelMap.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<QuestionDef,List<CheckBox>> entry = iterator.next();
			this.checkBoxGroupMap.put(entry.getKey(), entry.getValue());
		}
	}
	
	public String getBackgroundColor(){
		if(selectedPanel == null)
			return "";
		return DOM.getStyleAttribute(selectedPanel.getElement(), "backgroundColor");
	}
	
	public String getWidth(){
		if(selectedPanel == null)
			return "";
		return DOM.getStyleAttribute(selectedPanel.getElement(), "width");
	}
	
	public String getHeight(){
		if(selectedPanel == null)
			return "";
		return DOM.getStyleAttribute(selectedPanel.getElement(), "height");
	}
	
	public void setBackgroundColor(String backgroundColor){
		try{
			if(selectedPanel != null)
				DOM.setStyleAttribute(selectedPanel.getElement(), "backgroundColor", backgroundColor);
		}catch(Exception ex){}
	}
	
	public void setWidth(String width){
		try{
			if(selectedPanel != null)
				DOM.setStyleAttribute(selectedPanel.getElement(), "width", width);
		}catch(Exception ex){}
	}
	
	public void setHeight(String height){
		try{
			if(height != null && height.trim().length() > 0 && !height.equals("100%"))
				sHeight = height;
			if(selectedPanel != null)
				DOM.setStyleAttribute(selectedPanel.getElement(), "height", sHeight);
		}catch(Exception ex){}
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
