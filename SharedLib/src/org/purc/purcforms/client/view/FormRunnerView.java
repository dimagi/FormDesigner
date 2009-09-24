package org.purc.purcforms.client.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;

import org.purc.purcforms.client.controller.QuestionChangeListener;
import org.purc.purcforms.client.controller.SubmitListener;
import org.purc.purcforms.client.locale.LocaleText;
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
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
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
 * This widgets is responsible for displaying a form at run time and lets user fill data 
 * as it controls the running of skip and validation rules.
 * 
 * @author daniel
 *
 */
public class FormRunnerView extends Composite implements /*WindowResizeListener,*/TabListener, EditListener,QuestionChangeListener{

	public interface Images extends ImageBundle {
		AbstractImagePrototype error();
	}

	/** Images reference where we get the error icon for widgets with errors. */
	protected final Images images;

	/** The tabs where we lay various page panels. */
	protected DecoratedTabPanel tabs = new DecoratedTabPanel();
	
	/** The currently selected tab index. */
	protected int selectedTabIndex;
	
	/** The currently selected tab panel. */
	protected AbsolutePanel selectedPanel;
	
	/** The height of the currently selected tab panel. */
	protected String sHeight = "100%";
	
	/** Reference to the form definition. */
	protected FormDef formDef;
	
	/** Listener to form submit events. */
	protected SubmitListener submitListener;
	
	protected HashMap<String,RuntimeWidgetWrapper> widgetMap;
	
	/** 
	 * The first invalid widget. This is used when we validate more than one widget in a group
	 * and at the end of the list we want to set focus to the first widget that we found invalid.
	 */
	protected RuntimeWidgetWrapper firstInvalidWidget;

	/** 
	 * Used when we are used as an emebeded widgets in another GWT application
	 * and the user wants to control the space in which to embbed us.
	 */
	protected int embeddedHeightOffset = 0;

	protected HashMap<QuestionDef,List<Widget>> labelMap;
	protected HashMap<Widget,String> labelText;
	protected HashMap<Widget,String> labelReplaceText;

	protected HashMap<QuestionDef,List<CheckBox>> checkBoxGroupMap;
	
	/** 
	 * A map where the key widget's value change requires a list of other widgets to
	 * run their validation rules. Eg key qtn: Total No of kids born and dependant qtn: how many are male?
	 * Then the validation rule could be like no of male kids should be less than total
	 * no of kids born. So whenever the tatal no changes, no of males should be revalidated.
	 */
	protected HashMap<RuntimeWidgetWrapper,List<RuntimeWidgetWrapper>> validationWidgetsMap;

	/** 
	 * A reference to the login dialog to be used when user leaves the form open for long and the
	 * server session times out. Because we do not want them lose their changes, when they try to
	 * submit data, we use this dialog to logon the server.
	 */
	private static LoginDialog loginDlg = new LoginDialog();
	
	/**
	 * Reference to this very view. We need this static reference because the javascript login
	 * callback method is static and will need this view to submit the data on successful login.
	 */
	private static FormRunnerView formRunnerView;
	
	
	/**
	 * Constructs an instance of the form runner.
	 *
	 * @param images reference to images used in the application.
	 */
	public FormRunnerView(Images images){
		this.images = images;

		FormUtil.maximizeWidget(tabs);

		initWidget(tabs);
		tabs.addTabListener(this);

		//This is needed for IE which does not seem to set the height properly.
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				//onWindowResized(Window.getClientWidth(), Window.getClientHeight());
				setHeight(getHeight());
			}
		});

		formRunnerView = this;
	}

	/**
	 * Reloads the form runner view
	 * 
	 * @param formDef the form definition to load.
	 * @param layoutXml the form widget layout xml.
	 * @param externalSourceWidgets a list of widgets which get their data from sources 
	 * 		  external to the xform.
	 */
	public void loadForm(FormDef formDef,String layoutXml, List<RuntimeWidgetWrapper> externalSourceWidgets){
		FormUtil.initialize();
		
		if(externalSourceWidgets == null){
			//Here we must be in preview mode where we need to create a new copy of the formdef
			//such that we dont set preview values as default formdef values.
			if(formDef == null)
				this.formDef = null;
			else //set the document xml which we shall need for updating the model with question answers
				this.formDef = XformConverter.copyFormDef(formDef);
		}
		else
			this.formDef = formDef;

		tabs.clear();
		if(formDef == null || layoutXml == null || layoutXml.trim().length() == 0){
			addNewTab("Page1");
			return;
		}
		
		loadLayout(layoutXml,externalSourceWidgets);
		moveToFirstWidget();
	}

	/**
	 * Sets focus to the widget with the smallest tab index.
	 */
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
		validationWidgetsMap = new HashMap<RuntimeWidgetWrapper,List<RuntimeWidgetWrapper>>();
		
		//A list of widgets with validation rules.
		List<RuntimeWidgetWrapper> validationRuleWidgets = new ArrayList<RuntimeWidgetWrapper>();
		
		//A map of parent validation widgets keyed by their QuestionDef.
		//A parent validation widget is one whose QuestionDef is contained in any condition
		//of any validation rule.
		HashMap<QuestionDef,RuntimeWidgetWrapper> qtnParentValidationWidgetMap = new HashMap<QuestionDef,RuntimeWidgetWrapper>();
		
		//A list of questions for parent validation widgets.
		List<QuestionDef> parentValidationWidgetQtns = new ArrayList<QuestionDef>();
		
		initValidationWidgetsMap(parentValidationWidgetQtns);
		
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

			loadPage(node.getChildNodes(),externalSourceWidgets,parentValidationWidgetQtns,validationRuleWidgets,qtnParentValidationWidgetMap);
		}
		
		setValidationWidgetsMap(validationRuleWidgets,qtnParentValidationWidgetMap);

		if(formDef != null)
			fireSkipRules();

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

	/**
	 * Adds a new tab or page.
	 * 
	 * @param name the name of the page to add.
	 */
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

	protected void loadPage(NodeList nodes, List<RuntimeWidgetWrapper> externalSourceWidgets,List<QuestionDef> validationQtns,List<RuntimeWidgetWrapper> validationWidgets,HashMap<QuestionDef,RuntimeWidgetWrapper> qtnWidgetMap){
		HashMap<Integer,RuntimeWidgetWrapper> widgets = new HashMap<Integer,RuntimeWidgetWrapper>();
		int maxTabIndex = 0;

		//RuntimeWidgetWrapper wrapper = new RuntimeWidgetWrapper(null,null,null);
		for(int i=0; i<nodes.getLength(); i++){
			if(nodes.item(i).getNodeType() != Node.ELEMENT_NODE)
				continue;
			try{
				Element node = (Element)nodes.item(i);
				int index = loadWidget(node,widgets,externalSourceWidgets,validationQtns,validationWidgets,qtnWidgetMap);
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
			if(widget != null){
				selectedPanel.add(widget);
				FormUtil.setWidgetPosition(widget,widget.getLeft(),widget.getTop());
				//FormUtil.setWidgetPosition(selectedPanel,widget,widget.getLeft(),widget.getTop());
			}
		}
	}

	protected int loadWidget(Element node,HashMap<Integer,RuntimeWidgetWrapper> widgets, List<RuntimeWidgetWrapper> externalSourceWidgets,List<QuestionDef> validationQtns,List<RuntimeWidgetWrapper> validationWidgets,HashMap<QuestionDef,RuntimeWidgetWrapper> qtnWidgetMap){
		RuntimeWidgetWrapper parentWrapper = null;

		String left = node.getAttribute(WidgetEx.WIDGET_PROPERTY_LEFT);
		String top = node.getAttribute(WidgetEx.WIDGET_PROPERTY_TOP);
		String s = node.getAttribute(WidgetEx.WIDGET_PROPERTY_WIDGETTYPE);
		int tabIndex = (node.getAttribute(WidgetEx.WIDGET_PROPERTY_TABINDEX) != null ? Integer.parseInt(node.getAttribute(WidgetEx.WIDGET_PROPERTY_TABINDEX)) : 0);

		QuestionDef questionDef = null;
		String binding = node.getAttribute(WidgetEx.WIDGET_PROPERTY_BINDING);
		String parentBinding = node.getAttribute(WidgetEx.WIDGET_PROPERTY_PARENTBINDING);
		if(binding != null && binding.trim().length() > 0){
			questionDef = formDef.getQuestion(binding);
			if(questionDef != null)
				questionDef.setAnswer(questionDef.getDefaultValue()); //Just incase we are refreshing and had already set the answer
		}

		RuntimeWidgetWrapper wrapper = null;
		boolean wrapperSet = false;
		Widget widget = null;
		if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_RADIOBUTTON)){
			widget = new RadioButton(parentBinding,node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));

			if(widgetMap.get(parentBinding) == null)
				wrapperSet = true;

			parentWrapper = getParentWrapper(widget,node);
			((RadioButton)widget).setTabIndex(tabIndex);

			if(wrapperSet)
				wrapper = parentWrapper;
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_CHECKBOX)){
			widget = new CheckBox(node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));
			if(widgetMap.get(parentBinding) == null)
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
						contentType = "&contentType=audio/3gpp"; //"&contentType=audio/x-wav";

					contentType += "&name="+questionDef.getVariableName()+".3gp";

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
			if(validationRule != null && questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
				questionDef.setAnswer("0");
			
			if(validationQtns.contains(questionDef) && isValidationWidget(wrapper)){
				validationWidgetsMap.put(wrapper, new ArrayList<RuntimeWidgetWrapper>());
				qtnWidgetMap.put(questionDef, wrapper);
			}
			
			if(validationRule != null && isValidationWidget(wrapper))
				validationWidgets.add(wrapper);
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

		wrapper.setTabIndex(tabIndex);

		if(tabIndex > 0)
			widgets.put(new Integer(tabIndex), wrapper);
		else
			selectedPanel.add(wrapper);

		FormUtil.setWidgetPosition(wrapper,left,top);

		if(widget instanceof Button && binding != null){
			if(binding.equals("submit")){
				((Button)widget).addClickListener(new ClickListener(){
					public void onClick(Widget sender){
						submit();
					}
				});
			}
			else if(binding.equals("cancel")){
				((Button)widget).addClickListener(new ClickListener(){
					public void onClick(Widget sender){
						onCancel();
					}
				});
			}
			else if(binding.equals("search") && parentBinding != null){
				((Button)widget).addClickListener(new ClickListener(){
					public void onClick(Widget sender){
						onSearch(null,sender);
					}
				});
			}
		}

		return tabIndex;
	}
	
	private boolean isValidationWidget(RuntimeWidgetWrapper wrapper){
		return !((wrapper.getWrappedWidget() instanceof Label)||
				(wrapper.getWrappedWidget() instanceof HTML) || (wrapper.getWrappedWidget() instanceof Hyperlink) ||
				(wrapper.getWrappedWidget() instanceof Button));
	}

	protected RuntimeWidgetWrapper getParentWrapper(Widget widget, Element node){
		RuntimeWidgetWrapper parentWrapper = widgetMap.get(node.getAttribute(WidgetEx.WIDGET_PROPERTY_PARENTBINDING));
		if(parentWrapper == null){
			QuestionDef qtn = formDef.getQuestion(node.getAttribute(WidgetEx.WIDGET_PROPERTY_PARENTBINDING));
			if(qtn != null){
				parentWrapper = new RuntimeWidgetWrapper(widget,images.error(),this);
				parentWrapper.setQuestionDef(qtn,true);
				widgetMap.put(node.getAttribute(WidgetEx.WIDGET_PROPERTY_PARENTBINDING), parentWrapper);
				//selectedPanel.add(parentWrapper);		//will be added by the caller		
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

	/**
	 * Submits form data to the server.
	 */
	protected void submit(){
		if(formDef != null)
			FormUtil.isAuthenticated();
	}

	public void onSubmit(){
		submit();
	}

	public void onCancel(){
		if(Window.confirm(LocaleText.get("cancelFormPrompt")))
			submitListener.onCancel();
	}
	
	public void onSearch(String key,Widget widget){
		//FormUtil.searchExternal(key,widget.getElement(),widget.getElement(),null);
	}

	private void submitData(){
		if(formDef.getDoc() == null)
			XformConverter.fromFormDef2Xform(formDef);

		saveValues();

		if(!isValid())
			return;

		String xml = XformConverter.getInstanceDataDoc(formDef.getDoc()).toString();
		xml = FormUtil.formatXml("<?xml version='1.0' encoding='UTF-8' ?> " + xml);
		submitListener.onSubmit(xml);
	}

	/**
	 * Checks if form data has validation errors.
	 * 
	 * @return true if form has no validation errors, else false.
	 */
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

	/**
	 * Checks if widgets on a panel or page have validation errors.
	 * 
	 * @param panel the panel whose widgets to check.
	 * @return true if the panel widgets have no errors, else false.
	 */
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

	/**
	 * Saves form answers from widgets, on all pages,
	 *  to their corresponding model QuestionDef objects.
	 */
	protected void saveValues(){
		for(int index=0; index<tabs.getWidgetCount(); index++)
			savePageValues((AbsolutePanel)tabs.getWidget(index));
	}

	/**
	 * Saves page answers from widgets to their corresponding model QuestionDef objects.
	 * 
	 * @param panel the panel whose widgets to save.
	 */
	protected void savePageValues(AbsolutePanel panel){
		for(int index=0; index<panel.getWidgetCount(); index++)
			((RuntimeWidgetWrapper)panel.getWidget(index)).saveValue(formDef);
	}

	/**
     * @see org.purc.purcforms.client.widget.EditListener#onValueChanged(org.purc.purcforms.client.widget.RuntimeWidgetWrapper)
     */
	public void onValueChanged(RuntimeWidgetWrapper widget) {
		onValueChanged(widget.getQuestionDef());
		fireParentQtnValidationRules(widget);
	}
	
	private void onValueChanged(QuestionDef questionDef){
		fireSkipRules();
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

	/**
	 * Sets the listener to form submission events.
	 * 
	 * @param submitListener reference to the listener.
	 */
	public void setSubmitListener(SubmitListener submitListener){
		this.submitListener = submitListener;
	}

	/**
	 * Checks whether the form is being previewed.
	 * 
	 * @return true if yes, else false.
	 */
	public boolean isPreviewing(){
		return tabs.getWidgetCount() > 0;
	}

	/**
     * @see org.purc.purcforms.client.widget.EditListener#onMoveToNextWidget(com.google.gwt.user.client.ui.Widget)
     */
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

	/**
     * @see org.purc.purcforms.client.widget.EditListener#onMoveToPrevWidget(com.google.gwt.user.client.ui.Widget)
     */
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

	protected void fireSkipRules(){		
		Vector rules = formDef.getSkipRules();
		if(rules != null && rules.size() > 0){
			for(int i=0; i<rules.size(); i++){
				SkipRule rule = (SkipRule)rules.elementAt(i);
				rule.fire(formDef);
			}
		}
	}

	/**
	 * Clears the preview window.
	 */
	public void clearPreview(){
		tabs.clear();
	}

	/**
	 * This function is called when one switches between forms in the tree view
	 */
	public void setFormDef(FormDef formDef){
		if(this.formDef != formDef){
			tabs.clear();
			addNewTab("Page1");
		}
	}

	/**
	 * Sets the height offset to be used for the form when embedded in a GWT application.
	 * 
	 * @param offset
	 */
	public void setEmbeddedHeightOffset(int offset){
		embeddedHeightOffset = offset;
	}

	/**
	 * Updates the list of options for a question whose list of options depends
	 * on the select option for a given question.
	 * 
	 * @param questionDef the question whose selected value determines the list of
	 * 					  allowed options for another question.
	 */
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
		if(formDef.getPages() == null)
			return;

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

	/**
	 * Gets the background color of the selected page.
	 * 
	 * @return the background color.
	 */
	public String getBackgroundColor(){
		if(selectedPanel == null)
			return "";
		return DOM.getStyleAttribute(selectedPanel.getElement(), "backgroundColor");
	}

	/**
	 * Gets the widget of the selected page.
	 * 
	 * @return the width in pixels.
	 */
	public String getWidth(){
		if(selectedPanel == null)
			return "";
		return DOM.getStyleAttribute(selectedPanel.getElement(), "width");
	}

	/**
	 * Gets the height of the selected page.
	 * 
	 * @return the height in pixes.
	 */
	public String getHeight(){
		if(selectedPanel == null)
			return "";
		return DOM.getStyleAttribute(selectedPanel.getElement(), "height");
	}

	/**
	 * Sets the background color of the selected page.
	 * 
	 * @param backgroundColor the background color.
	 */
	public void setBackgroundColor(String backgroundColor){
		try{
			if(selectedPanel != null)
				DOM.setStyleAttribute(selectedPanel.getElement(), "backgroundColor", backgroundColor);
		}catch(Exception ex){}
	}

	/**
	 * Sets the width of the currently selected tab panel.
	 * 
	 * @param widget the widget to set in pixels.
	 */
	public void setWidth(String width){
		try{
			if(selectedPanel != null)
				DOM.setStyleAttribute(selectedPanel.getElement(), "width", width);
		}catch(Exception ex){}
	}

	/**
	 * Sets the height of the selected widget panel.
	 * 
	 * @param height the height to set in pixels.
	 */
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

	/**
	 * This method is called from javascript after a user has made an attempt to log on the server.
	 * 
	 * @param authenticated true o
	 */
	private static void authenticationCallback(boolean authenticated) {		
		if(authenticated){
			loginDlg.hide();
			formRunnerView.submitData();
		}
		else
			loginDlg.center();
	}
	
	/**
     * @see org.purc.purcforms.client.widget.EditListener#onRowAdded(org.purc.purcforms.client.widget.RuntimeWidgetWrapper)
     */
	public void onRowAdded(RuntimeWidgetWrapper rptWidget, int increment){
		
		//Get the current bottom y position of the repeat widget.
		int bottomYpos = rptWidget.getTopInt() + rptWidget.getHeightInt();
		
		for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
			RuntimeWidgetWrapper currentWidget = (RuntimeWidgetWrapper)selectedPanel.getWidget(index);
			if(currentWidget == rptWidget)
				continue;
			
			int top = currentWidget.getTopInt();
			if(top >= bottomYpos)
				currentWidget.setTopInt(top + increment);
		}
		
		DOM.setStyleAttribute(selectedPanel.getElement(), "height", getHeightInt()+increment+"px");	
	}
	
	/**
     * @see org.purc.purcforms.client.widget.EditListener#onRowRemoved(org.purc.purcforms.client.widget.RuntimeWidgetWrapper)
     */
	public void onRowRemoved(RuntimeWidgetWrapper rptWidget, int decrement){
		
		//Get the current bottom y position of the repeat widget.
		int bottomYpos = rptWidget.getTopInt() + rptWidget.getHeightInt();
		
		//Move widgets which are below the bottom of the repeat widget.
		for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
			RuntimeWidgetWrapper currentWidget = (RuntimeWidgetWrapper)selectedPanel.getWidget(index);
			if(currentWidget == rptWidget)
				continue;
			
			int top = currentWidget.getTopInt();
			if(top >= bottomYpos)
				currentWidget.setTopInt(top - decrement);
		}

		DOM.setStyleAttribute(selectedPanel.getElement(), "height", getHeightInt()-decrement+"px");
	}
	
	/**
	 * Gets the height of the currently selected page.
	 * 
	 * @return the height.
	 */
	private int getHeightInt(){
		return FormUtil.convertDimensionToInt(DOM.getStyleAttribute(selectedPanel.getElement(), "height"));
	}
	
	/**
	 * Fires all validation rules that are dependant on the value of a certain widget.
	 * In other wards the value in this widget is referenced in one or more conditions
	 * or one or more validation rules. These are cross field validation rules. eg Total no
	 * of kids should be less than those that are born as male.
	 * 
	 * @param widget the widget whose value change requires refiring of other rules.
	 */
	private void fireParentQtnValidationRules(RuntimeWidgetWrapper widget){
		List<RuntimeWidgetWrapper> widgets  = validationWidgetsMap.get(widget);
		if(widgets == null)
			return;
		
		for(RuntimeWidgetWrapper wgt : widgets)
			wgt.isValid();
	}
	
	/**
	 * 
	 * @param validationQtns
	 */
	private void initValidationWidgetsMap(List<QuestionDef> parentValidationWidgetQtns){
		int count = formDef.getValidationRuleCount();
		for(int index = 0; index < count; index++){
			ValidationRule rule = formDef.getValidationRuleAt(index);
			List<QuestionDef> qtns = rule.getQuestions(formDef);
			for(QuestionDef questionDef : qtns){
				if(!parentValidationWidgetQtns.contains(questionDef))
					parentValidationWidgetQtns.add(questionDef);
			}
		}
	}
	
	/**
	 * 
	 * @param validationWidgets a list of widgets with validation rules
	 * @param qtnWidgetMap
	 */
	private void setValidationWidgetsMap(List<RuntimeWidgetWrapper> validationRuleWidgets,HashMap<QuestionDef,RuntimeWidgetWrapper> qtnParentValidationWidgetMap){
		for(RuntimeWidgetWrapper widget : validationRuleWidgets){
			ValidationRule rule = widget.getValidationRule();
			List<QuestionDef> qtns = rule.getQuestions(formDef);
			for(QuestionDef questionDef : qtns){
				RuntimeWidgetWrapper wgt = qtnParentValidationWidgetMap.get(questionDef);
				if(wgt == null)
					continue;
				
				List<RuntimeWidgetWrapper> widgets = validationWidgetsMap.get(wgt);
				if(widgets == null)
					continue;
				
				widgets.add(widget);
			}
		}
	}
	
	public boolean handleKeyBoardEvent(Event event){
		if(event.getCtrlKey() && event.getKeyCode() == 'S'){
			onSubmit();
			
			//Returning false such that firefox does not try to save the page.
			return false;
		}
		
		return true;
	}
}
