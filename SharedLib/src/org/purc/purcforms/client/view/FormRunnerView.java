package org.purc.purcforms.client.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;

import org.purc.purcforms.client.PurcConstants;
import org.purc.purcforms.client.controller.QuestionChangeListener;
import org.purc.purcforms.client.controller.SubmitListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.Calculation;
import org.purc.purcforms.client.model.DynamicOptionDef;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.OptionDef;
import org.purc.purcforms.client.model.PageDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.model.RepeatQtnsDef;
import org.purc.purcforms.client.model.SkipRule;
import org.purc.purcforms.client.model.ValidationRule;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.widget.CheckBoxWidget;
import org.purc.purcforms.client.widget.DatePickerEx;
import org.purc.purcforms.client.widget.DatePickerWidget;
import org.purc.purcforms.client.widget.DateTimeWidget;
import org.purc.purcforms.client.widget.EditListener;
import org.purc.purcforms.client.widget.ListBoxWidget;
import org.purc.purcforms.client.widget.RadioButtonWidget;
import org.purc.purcforms.client.widget.RuntimeGroupWidget;
import org.purc.purcforms.client.widget.RuntimeWidgetWrapper;
import org.purc.purcforms.client.widget.TimeWidget;
import org.purc.purcforms.client.widget.WidgetEx;
import org.purc.purcforms.client.xforms.XformBuilder;
import org.purc.purcforms.client.xforms.XformParser;
import org.purc.purcforms.client.xforms.XformUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
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
public class FormRunnerView extends Composite implements SelectionHandler<Integer>, EditListener,QuestionChangeListener{

	private final char FIELD_SEPARATOR = '|'; //TODO These may need to be changed.
	private final char RECORD_SEPARATOR = '$';

	public interface Images extends ClientBundle {
		ImageResource error();
		ImageResource loading();
	}

	/** Images reference where we get the error icon for widgets with errors. */
	public static final Images images = (Images) GWT.create(Images.class);

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

	/** A map of parent binding text and one of the widgets that reference it. */
	protected HashMap<String,RuntimeWidgetWrapper> parentBindingWidgetMap;

	/** 
	 * The first invalid widget. This is used when we validate more than one widget in a group
	 * and at the end of the list we want to set focus to the first widget that we found invalid.
	 */
	protected RuntimeWidgetWrapper firstInvalidWidget;

	/** 
	 * Used when we are used as an embedded widget in another GWT application
	 * and the user wants to control the space in which to embed us.
	 */
	protected int embeddedHeightOffset = 0;

	/** A map of a questions and its list of Label widgets.
	 *  Labels in this list are only those which have portions of its text
	 *  that are to be replaced with answers from some questions
	 *  which are the keys in this map.
	 */
	protected HashMap<QuestionDef,List<Label>> labelMap;

	/** A map of a label widget and its text.
	 *  Labels in this map are only those which have portions of its text
	 *  that are to be replaced with answers from some questions.
	 */
	protected HashMap<Label,String> labelText;

	/** A map of a label widget and its template text (eg ${/newform1/name}$) which is to be 
	 * replaced by answers to some questions. Labels in this map are only those which have 
	 * portions of its text that are to be replaced with answers from some questions.
	 */
	protected HashMap<Label,String> labelReplaceText;

	/** A map of a question and its list of CheckBox widgets. */
	protected HashMap<QuestionDef,List<CheckBox>> checkBoxGroupMap;

	/** 
	 * A map where the key widget's value change requires a list of other widgets to
	 * run their validation rules. E.g key qtn: Total No of kids born and dependent qtn: how many are male?
	 * Then the validation rule could be like no of male kids should be less than total
	 * no of kids born. So whenever the total no changes, no of males should be revalidated.
	 */
	protected HashMap<RuntimeWidgetWrapper,List<RuntimeWidgetWrapper>> validationWidgetsMap;

	/** A map of a questions and its list of RuntimeWidgetWrapper widgets.
	 *  Widgets in this list are only those which should have values
	 *  that are to be computed using answers from some questions
	 *  which are the keys in this map.
	 */
	protected HashMap<QuestionDef,List<RuntimeWidgetWrapper>> calcWidgetMap;

	/**
	 * A map of filtered single select dynamic questions and their corresponding 
	 * non label widgets. Only questions of single select dynamic which have the
	 * widget filter property set are put in this list
	 */
	protected HashMap<QuestionDef,RuntimeWidgetWrapper> filtDynOptWidgetMap;

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


	private List<RuntimeWidgetWrapper> externalSourceWidgets;
	private int externalSourceWidgetIndex = 0;



	/**
	 * Constructs an instance of the form runner.
	 *
	 * @param images reference to images used in the application.
	 */
	public FormRunnerView(/*Images images*/){
		//this.images = images;

		FormUtil.maximizeWidget(tabs);

		initWidget(tabs);
		tabs.addSelectionHandler(this);

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
	public void loadForm(FormDef formDef,String layoutXml, String javaScriptSrc, List<RuntimeWidgetWrapper> externalSourceWidgets, boolean previewMode){
		FormUtil.initialize();

		if(previewMode /*externalSourceWidgets == null*/){
			//Here we must be in preview mode where we need to create a new copy of the formdef
			//such that we don't set preview values as default formdef values.
			if(formDef == null)
				this.formDef = null;
			else //set the document xml which we shall need for updating the model with question answers
				this.formDef = XformParser.copyFormDef(formDef);
		}
		else
			this.formDef = formDef;

		tabs.clear();
		if(formDef == null || layoutXml == null || layoutXml.trim().length() == 0){
			addNewTab(LocaleText.get("page") + "1");
			return;
		}

		loadLayout(layoutXml,externalSourceWidgets,getCalcQtnMappings(this.formDef));
		isValid(true);
		moveToFirstWidget();

		com.google.gwt.dom.client.Element script = DOM.getElementById("purcforms_javascript");
		if(script != null)
			script.removeFromParent();

		/*String s = " function calculateAge(){ " +
		" 	var element2 = document.getElementById('question5'); " +
		"   var element1 = document.getElementById('question1'); " +
		" 	element2.value = element1.value;	 " +
		" } " +
		"  " +
		"  " +
		" var element = document.getElementById('question1'); " +
		" element.addEventListener('change',calculateAge ,false) ";*/

		if(javaScriptSrc != null){
			Document document = Document.get();
			script = document.createElement("script");
			script.setAttribute("type", "text/javascript");
			script.setAttribute("id", "purcforms_javascript");
			script.appendChild(document.createTextNode(javaScriptSrc));
			document.getElementsByTagName("head").getItem(0).appendChild(script);
		}

		this.externalSourceWidgets = externalSourceWidgets;
		externalSourceWidgetIndex = 0;
		if(externalSourceWidgets != null && externalSourceWidgets.size() > 0 && FormUtil.getExternalSourceUrlSuffix() != null)
			fillExternalSourceWidget(externalSourceWidgets.get(externalSourceWidgetIndex++),null);
	}

	/**
	 * Sets focus to the widget with the smallest tab index.
	 */
	public void moveToFirstWidget(){
		moveToNextWidget(-1);
	}


	/**
	 * Sets focus to the focusable widget whose tab index is next to a given index.
	 * 
	 * @param index the given tab index.
	 */
	protected void moveToNextWidget(int index){

		//If we have reached end of tab order, just wrap around to the first widget.
		if(index > selectedPanel.getWidgetCount() - 2)
			index = 0;

		while(++index < selectedPanel.getWidgetCount()){
			if(((RuntimeWidgetWrapper)selectedPanel.getWidget(index)).setFocus())
				break;
		}
	}


	/**
	 * Loads widgets in a given layout xml and populates a list of widgets whose source of
	 * allowed option is external to the xform.
	 * 
	 * @param xml the layout xml.
	 * @param externalSourceWidgets the list of external source widgets.
	 */
	public void loadLayout(String xml, List<RuntimeWidgetWrapper> externalSourceWidgets, HashMap<QuestionDef,List<QuestionDef>> calcQtnMappings){
		tabs.clear();

		if(formDef != null)
			formDef.clearChangeListeners();

		parentBindingWidgetMap = new HashMap<String,RuntimeWidgetWrapper>();
		labelMap = new HashMap<QuestionDef,List<Label>>();
		labelText = new HashMap<Label,String>();
		labelReplaceText = new HashMap<Label,String>();
		checkBoxGroupMap = new HashMap<QuestionDef,List<CheckBox>>();
		validationWidgetsMap = new HashMap<RuntimeWidgetWrapper,List<RuntimeWidgetWrapper>>();
		calcWidgetMap = new HashMap<QuestionDef,List<RuntimeWidgetWrapper>>();
		filtDynOptWidgetMap = new HashMap<QuestionDef,RuntimeWidgetWrapper>();

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
			WidgetEx.loadLabelProperties(node, new RuntimeWidgetWrapper(tabs.getTabBar(),images.error(),this));

			setWidth(node.getAttribute(WidgetEx.WIDGET_PROPERTY_WIDTH));
			setHeight(node.getAttribute(WidgetEx.WIDGET_PROPERTY_HEIGHT));
			setBackgroundColor(node.getAttribute(WidgetEx.WIDGET_PROPERTY_BACKGROUND_COLOR));

			loadPage(node.getChildNodes(),externalSourceWidgets,parentValidationWidgetQtns,validationRuleWidgets,qtnParentValidationWidgetMap, calcQtnMappings);
		}

		setValidationWidgetsMap(validationRuleWidgets,qtnParentValidationWidgetMap);

		if(formDef != null){
			fireSkipRules();
			doCalculations();
		}

		//For those widgets whose answers are now set, lets load their option lists.
		updateDynamicOptions();

		if(tabs.getWidgetCount() > 0)
			tabs.selectTab(0);
	}


	/**
	 * Sets up the main panel widget.
	 */
	protected void initPanel(){
		AbsolutePanel panel = new AbsolutePanel();
		FormUtil.maximizeWidget(panel);
		selectedPanel = panel;

		//This is needed for IE
		DeferredCommand.addCommand(new Command() {
			public void execute() {
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
			name = LocaleText.get("page")+tabs.getWidgetCount();
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


	/**
	 * Loads widgets contained in a list of nodes for a page.
	 * 
	 * @param nodes the node list
	 * @param externalSourceWidgets
	 * @param validationQtns
	 * @param validationWidgets
	 * @param qtnWidgetMap a map keyed by the QuestionDef object for each loaded widget.
	 */
	protected void loadPage(NodeList nodes, List<RuntimeWidgetWrapper> externalSourceWidgets,List<QuestionDef> validationQtns,List<RuntimeWidgetWrapper> validationWidgets,HashMap<QuestionDef,RuntimeWidgetWrapper> qtnWidgetMap,HashMap<QuestionDef,List<QuestionDef>> calcQtnMappings){
		HashMap<Integer,RuntimeWidgetWrapper> widgets = new HashMap<Integer,RuntimeWidgetWrapper>();
		int maxTabIndex = 0;

		//RuntimeWidgetWrapper wrapper = new RuntimeWidgetWrapper(null,null,null);
		for(int i=0; i<nodes.getLength(); i++){
			if(nodes.item(i).getNodeType() != Node.ELEMENT_NODE)
				continue;
			try{
				Element node = (Element)nodes.item(i);
				int index = loadWidget(node,widgets,externalSourceWidgets,validationQtns,validationWidgets,qtnWidgetMap,calcQtnMappings);
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

	protected int loadWidget(Element node,HashMap<Integer,RuntimeWidgetWrapper> widgets, List<RuntimeWidgetWrapper> externalSourceWidgets,List<QuestionDef> validationQtns,List<RuntimeWidgetWrapper> validationWidgets,HashMap<QuestionDef,RuntimeWidgetWrapper> qtnWidgetMap,HashMap<QuestionDef,List<QuestionDef>> calcQtnMappings){
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
			widget = new RadioButtonWidget(parentBinding,node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));

			if(parentBindingWidgetMap.get(parentBinding) == null)
				wrapperSet = true;

			parentWrapper = getParentBindingWrapper(widget,parentBinding);
			((RadioButton)widget).setTabIndex(tabIndex);

			if(wrapperSet){
				wrapper = parentWrapper;
				questionDef = formDef.getQuestion(parentBinding);
			}
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_CHECKBOX)){
			widget = new CheckBoxWidget(node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));
			if(parentBindingWidgetMap.get(parentBinding) == null)
				wrapperSet = true;

			parentWrapper = getParentBindingWrapper(widget,parentBinding);
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
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_GROUPBOX)||s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_REPEATSECTION)){
			RepeatQtnsDef repeatQtnsDef = null;
			if(questionDef != null)
				repeatQtnsDef = questionDef.getRepeatQtnsDef();

			boolean repeated = false;
			String value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_REPEATED);
			if(value != null && value.trim().length() > 0)
				repeated = (value.equals(WidgetEx.REPEATED_TRUE_VALUE));

			widget = new RuntimeGroupWidget(images,formDef,repeatQtnsDef,this,repeated);
			((RuntimeGroupWidget)widget).loadWidgets(formDef,node.getChildNodes(),externalSourceWidgets,calcQtnMappings,calcWidgetMap,filtDynOptWidgetMap);
			//((RuntimeGroupWidget)widget).setTabIndex(tabIndex);
			copyLabelMap(((RuntimeGroupWidget)widget).getLabelMap());
			copyLabelText(((RuntimeGroupWidget)widget).getLabelText());
			copyLabelReplaceText(((RuntimeGroupWidget)widget).getLabelReplaceText());
			copyCheckBoxGroupMap(((RuntimeGroupWidget)widget).getCheckBoxGroupMap());
			copyCalcWidgetMap(((RuntimeGroupWidget)widget).getCalcWidgetMap());
			copyFiltDynOptWidgetMap(((RuntimeGroupWidget)widget).getFiltDynOptWidgetMap());
		}
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_IMAGE)){
			widget = new Image();
			String xpath = binding;
			if(!xpath.startsWith(formDef.getVariableName()))
				xpath = "/" + formDef.getVariableName() + "/" + binding;
			((Image)widget).setUrl(URL.encode(FormUtil.getMultimediaUrl()+"?formId="+formDef.getId()+"&xpath="+xpath));
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

					((HTML)widget).setHTML("<a href=" + URL.encode(FormUtil.getMultimediaUrl()+extension+"?formId="+formDef.getId()+"&xpath="+xpath+contentType) + ">"+node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT)+"</a>");
				}
			}
		}
		else
			return tabIndex;

		if(!wrapperSet){
			wrapper = new RuntimeWidgetWrapper(widget,images.error(),this);

			if(parentWrapper != null){ //Check box or radio button
				if(!parentWrapper.getQuestionDef().isVisible())
					wrapper.setVisible(false);
				if(!parentWrapper.getQuestionDef().isEnabled())
					wrapper.setEnabled(false);
				if(parentWrapper.getQuestionDef().isLocked())
					wrapper.setLocked(true);
			}
		}

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
			if(validationRule != null && questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
				questionDef.setAnswer("0");

			if(validationQtns.contains(questionDef) && isValidationWidget(wrapper)){
				validationWidgetsMap.put(wrapper, new ArrayList<RuntimeWidgetWrapper>());
				qtnWidgetMap.put(questionDef, wrapper);
			}

			if(validationRule != null && isValidationWidget(wrapper))
				validationWidgets.add(wrapper);
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
					&& questionDef != null){

				if(!(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE
						||questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE)){
					questionDef.setDataType(QuestionDef.QTN_TYPE_LIST_EXCLUSIVE);
				}

				externalSourceWidgets.add(wrapper);
				loadWidget = false;

				wrapper.addSuggestBoxChangeEvent();
			}
		}

		if(loadWidget)
			wrapper.loadQuestion();

		WidgetEx.loadLabelProperties(node,wrapper);

		wrapper.setTabIndex(tabIndex);

		if(tabIndex > 0)
			widgets.put(new Integer(tabIndex), wrapper);
		else
			selectedPanel.add(wrapper);

		FormUtil.setWidgetPosition(wrapper,left,top);

		if(widget instanceof Button && binding != null){
			if(binding.equals("submit")){
				((Button)widget).addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event){
						submit();
					}
				});
			}
			else if(binding.equals("cancel")){
				((Button)widget).addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event){
						onCancel();
					}
				});
			}
			else if(binding.equals("search") && parentBinding != null){
				((Button)widget).addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event){
						onSearch(null,(Widget)event.getSource());
					}
				});
			}
		}

		if(wrapper.isEditable() && questionDef != null)
			updateCalcWidgetMapping(wrapper, calcQtnMappings, calcWidgetMap);

		return tabIndex;
	}


	/**
	 * Check if a widget can have a validation rule.
	 * 
	 * @param wrapper the widget
	 * @return true if it can have, else false.
	 */
	private boolean isValidationWidget(RuntimeWidgetWrapper wrapper){
		return !((wrapper.getWrappedWidget() instanceof Label)||
				(wrapper.getWrappedWidget() instanceof HTML) || (wrapper.getWrappedWidget() instanceof Hyperlink) ||
				(wrapper.getWrappedWidget() instanceof Button));
	}


	/**
	 * Gets a widget that has a given parent binding value as that of a given widget.
	 * 
	 * @param widget the given widget.
	 * @param parentBinding the parent binding value.
	 * @return the widget that has the same parent binding.
	 */
	protected RuntimeWidgetWrapper getParentBindingWrapper(Widget widget, String parentBinding){
		RuntimeWidgetWrapper parentWrapper = parentBindingWidgetMap.get(parentBinding);
		if(parentWrapper == null){
			QuestionDef qtn = formDef.getQuestion(parentBinding);
			if(qtn != null){
				parentWrapper = new RuntimeWidgetWrapper(widget,images.error(),this);
				parentWrapper.setQuestionDef(qtn,true);
				parentBindingWidgetMap.put(parentBinding, parentWrapper);
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

		//Before calling the submit listener, we first check if the user is authenticated
		//The authentication will call us back and tell us whether to proceed with the
		//data submission or display the login dialog box.
		if(formDef != null)
			FormUtil.isAuthenticated();
	}


	/**
	 * Called when one clicks the submit button on the form to submit form data.
	 */
	public void onSubmit(){
		submit();
	}


	/**
	 * Called when one clicks the cancel button on the form, meaning that they have
	 * changed their mind about submitting the form.
	 */
	public void onCancel(){
		if(Window.confirm(LocaleText.get("cancelFormPrompt")))
			submitListener.onCancel();
	}

	public void onSearch(String key,Widget widget){
		//FormUtil.searchExternal(key,widget.getElement(),widget.getElement(),null);
	}


	/**
	 * Does the actual submission of form data to the submit listener.
	 */
	private void submitData(){
		if(formDef.getDoc() == null)
			XformBuilder.fromFormDef2Xform(formDef);

		saveValues();

		if(!isValid(false))
			return;

		String xml = XformUtil.getInstanceDataDoc(formDef.getDoc()).toString();
		xml = FormUtil.formatXml(xml); //"<?xml version='1.0' encoding='UTF-8' ?> " + 
		submitListener.onSubmit(xml);
	}


	/**
	 * Checks if form data has validation errors.
	 * 
	 * @param fireValueChanged set to true to fire the value changed event, else false.
	 * @return true if form has no validation errors, else false.
	 */
	protected boolean isValid(boolean fireValueChanged){
		boolean valid = true;
		int pageNo = -1;
		firstInvalidWidget = null;
		for(int index=0; index<tabs.getWidgetCount(); index++){
			if(!isValid((AbsolutePanel)tabs.getWidget(index),fireValueChanged)){
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
	 * @param fireValueChanged set to true to fire the value changed event, else false.
	 * @return true if the panel widgets have no errors, else false.
	 */
	private boolean isValid(AbsolutePanel panel, boolean fireValueChanged){
		boolean valid = true;
		for(int index=0; index<panel.getWidgetCount(); index++){
			RuntimeWidgetWrapper widget = (RuntimeWidgetWrapper)panel.getWidget(index);
			if(!widget.isValid()){
				valid = false;
				if(firstInvalidWidget == null && widget.isFocusable())
					firstInvalidWidget = widget.getInvalidWidget();
			}

			if(fireValueChanged && widget.getQuestionDef() != null)
				onValueChanged(widget);
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
		if(!widget.isEditable())
			return;

		onValueChanged(widget.getQuestionDef());
		fireParentQtnValidationRules(widget);
	}


	/**
	 * Called when the value or answer of a question changes.
	 * 
	 * @param questionDef the question definition object.
	 */
	private void onValueChanged(QuestionDef questionDef){
		fireSkipRules();
		//doCalculations();
		updateDynamicOptions(questionDef);

		List<Label> labels = labelMap.get(questionDef);
		if(labels != null){
			for(Widget widget : labels){
				String replaceText = questionDef.getAnswer();
				if(replaceText == null)
					replaceText = "";
				((Label)widget).setText(labelText.get(widget).replace(labelReplaceText.get(widget), replaceText));
			}
		}


		List<RuntimeWidgetWrapper> widgets = calcWidgetMap.get(questionDef);
		if(widgets != null){

			for(RuntimeWidgetWrapper widget : widgets){
				Calculation calculation = formDef.getCalculation(widget.getQuestionDef());
				//String calcExpression = calculation.getCalculateExpression();
				String calcExpression = replaceCalcExpression(calculation.getCalculateExpression(),widget.getQuestionDef()); //calcExpression.replace(binding, answer);

				int type = widget.getQuestionDef().getDataType();
				String answer = calcExpression;

				if(calculation.getCalculateExpression().trim().indexOf(' ') > 0){
					if(type == QuestionDef.QTN_TYPE_NUMERIC){
						try{
							answer = ""+FormUtil.evaluateIntExpression(calcExpression);
						}
						catch(Exception ex){
							answer = FormUtil.evaluateStringExpression(calcExpression);
						}
					}
					else if(type == QuestionDef.QTN_TYPE_DECIMAL){
						try{
							answer = ""+FormUtil.evaluateDoubleExpression(calcExpression);
						}
						catch(Exception ex){
							answer = FormUtil.evaluateStringExpression(calcExpression);
						}
					}
					else{
						try{
							answer = FormUtil.evaluateStringExpression(calcExpression);
						}
						catch(Exception ex){
							answer = ""+FormUtil.evaluateDoubleExpression(calcExpression);
						}
					}
				}

				widget.setAnswer(answer);
				widget.isValid(); //TODO May need to fire change event instead
				onValueChanged(widget);
			}
		}


		List<CheckBox> list = checkBoxGroupMap.get(questionDef);
		if(list != null /*&& questionDef.isRequired()*/){
			for(CheckBox checkBox : list)
				((RuntimeWidgetWrapper)checkBox.getParent().getParent()).isValid();
		}
	}

	/**
	 * @see com.google.gwt.event.logical.shared.SelectionHandler#onSelection(SelectionEvent)
	 */
	public void onSelection(SelectionEvent<Integer> event){
		selectedTabIndex = event.getSelectedItem();
		selectedPanel = (AbsolutePanel)tabs.getWidget(selectedTabIndex);
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
		if(widget.getParent().getParent() != null){
			if(widget.getParent().getParent() instanceof RuntimeGroupWidget){
				//Non repeating widgets in a group box
				if(((RuntimeGroupWidget)widget.getParent().getParent()).onMoveToNextWidget(widget))
					return;
				else
					widget = widget.getParent().getParent().getParent().getParent();
			}
			else if(widget.getParent().getParent().getParent() instanceof RuntimeGroupWidget){
				//Repeating widgets.
				if(((RuntimeGroupWidget)widget.getParent().getParent().getParent()).onMoveToNextWidget(widget))
					return;
				else
					widget = widget.getParent().getParent().getParent().getParent().getParent();
			}
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

	/**
	 * Checks all skip logic and does the appropriate action for the affected widgets.
	 */
	protected void fireSkipRules(){		
		Vector rules = formDef.getSkipRules();
		if(rules != null){
			for(int i=0; i<rules.size(); i++){
				SkipRule rule = (SkipRule)rules.elementAt(i);
				rule.fire(formDef);
			}
		}
	}

	protected void doCalculations(){		
		Vector calculations = formDef.getCalculations();
		if(calculations != null){
			for(int i=0; i<calculations.size(); i++){
				Calculation calculation = (Calculation)calculations.elementAt(i);
				//rule.fire(formDef);
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
			addNewTab(LocaleText.get("page") + "1");
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

		//Get the dynamic option definition where this question is the parent.
		DynamicOptionDef dynamicOptionDef = formDef.getDynamicOptions(questionDef.getId());
		if(dynamicOptionDef == null)
			return;

		//Get the question definition for the child/dependent question.
		final QuestionDef childQuestionDef = formDef.getQuestion(dynamicOptionDef.getQuestionId());
		if(childQuestionDef == null)
			return;

		childQuestionDef.setOptionList(null);

		//Get the selected option/answer in the parent question
		OptionDef optionDef = questionDef.getOptionWithValue(questionDef.getAnswer());
		if(optionDef != null){
			RuntimeWidgetWrapper widget = filtDynOptWidgetMap.get(childQuestionDef);
			if(widget == null){
				//Set the now possible answers for the child question as per the parent's selected value.
				List<OptionDef> optionList = dynamicOptionDef.getOptionList(optionDef.getId());
				childQuestionDef.setOptionList(optionList);
			}
			else{
				//TODO Need to show progress window.
				//widget values come from an external filtered source.
				fillExternalSourceWidget(widget,questionDef.getAnswer());
				return;
			}
		}

		//do it recursively until when no more dependent questions.
		onValueChanged(childQuestionDef);
	}


	/**
	 * Updates dynamic selection lists in all pages of the form to their values as determined
	 * by the selected option of their parent questions.
	 */
	private void updateDynamicOptions(){
		if(formDef.getPages() == null)
			return;

		for(byte i=0; i<formDef.getPages().size(); i++){
			PageDef pageDef = (PageDef)formDef.getPages().elementAt(i);
			for(byte j=0; j<pageDef.getQuestions().size(); j++)
				updateDynamicOptions((QuestionDef)pageDef.getQuestions().elementAt(j));
		}
	}


	/**
	 * Copies from a given label map to our class level one.
	 * 
	 * @param labelMap the label map to copy from.
	 */
	private void copyLabelMap(HashMap<QuestionDef,List<Label>> labelMap){
		Iterator<Entry<QuestionDef,List<Label>>> iterator = labelMap.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<QuestionDef,List<Label>> entry = iterator.next();
			this.labelMap.put(entry.getKey(), entry.getValue());
		}
	}

	private void copyCalcWidgetMap(HashMap<QuestionDef,List<RuntimeWidgetWrapper>> calcWidgetMap){
		Iterator<Entry<QuestionDef,List<RuntimeWidgetWrapper>>> iterator = calcWidgetMap.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<QuestionDef,List<RuntimeWidgetWrapper>> entry = iterator.next();
			this.calcWidgetMap.put(entry.getKey(), entry.getValue());
		}
	}

	private void copyFiltDynOptWidgetMap(HashMap<QuestionDef,RuntimeWidgetWrapper> filtDynOptWidgetMap){
		Iterator<Entry<QuestionDef,RuntimeWidgetWrapper>> iterator = filtDynOptWidgetMap.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<QuestionDef,RuntimeWidgetWrapper> entry = iterator.next();
			this.filtDynOptWidgetMap.put(entry.getKey(), entry.getValue());
		}
	}


	/**
	 * Copies from a given label text map to our class level one.
	 * 
	 * @param labelText the label text map to copy from.
	 */
	private void copyLabelText(HashMap<Label,String> labelText){
		Iterator<Entry<Label,String>> iterator = labelText.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<Label,String> entry = iterator.next();
			this.labelText.put(entry.getKey(), entry.getValue());
		}
	}


	/**
	 * Copies from a given label replace text map to our class level one.
	 * 
	 * @param labelReplaceText the label replace text map to copy from.
	 */
	private void copyLabelReplaceText(HashMap<Label,String> labelReplaceText){
		Iterator<Entry<Label,String>> iterator = labelReplaceText.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<Label,String> entry = iterator.next();
			this.labelReplaceText.put(entry.getKey(), entry.getValue());
		}
	}


	/**
	 * Copies from a given check box group map to our class level one.
	 * 
	 * @param labelMap the check box group map to copy from.
	 */
	private void copyCheckBoxGroupMap(HashMap<QuestionDef,List<CheckBox>> labelMap){
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

	/**
	 * @see org.purc.purcforms.client.controller.QuestionChangeListener#onEnabledChanged(QuestionDef, boolean)
	 */
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

	/**
	 * @see org.purc.purcforms.client.controller.QuestionChangeListener#onVisibleChanged(QuestionDef, boolean)
	 */
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

	/**
	 * @see org.purc.purcforms.client.controller.QuestionChangeListener#onRequiredChanged(QuestionDef, boolean)
	 */
	public void onRequiredChanged(QuestionDef sender,boolean required){

	}

	/**
	 * @see org.purc.purcforms.client.controller.QuestionChangeListener#onLockedChanged(QuestionDef, boolean)
	 */
	public void onLockedChanged(QuestionDef sender,boolean locked){

	}

	/**
	 * @see org.purc.purcforms.client.controller.QuestionChangeListener#onBindingChanged(QuestionDef, String)
	 */
	public void onBindingChanged(QuestionDef sender,String newValue){

	}

	/**
	 * @see org.purc.purcforms.client.controller.QuestionChangeListener#onDataTypeChanged(QuestionDef, int)
	 */
	public void onDataTypeChanged(QuestionDef sender,int dataType){

	}

	/**
	 * @see org.purc.purcforms.client.controller.QuestionChangeListener#onOptionsChanged(QuestionDef, List)
	 */
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
	 * This method is called from javascript to submit form data.
	 */
	public static void submitForm(){
		formRunnerView.submit();
	}

	/**
	 * @see org.purc.purcforms.client.widget.EditListener#onRowAdded(org.purc.purcforms.client.widget.RuntimeWidgetWrapper)
	 */
	public void onRowAdded(RuntimeWidgetWrapper rptWidget, int increment){

		//Get the current bottom y position of the repeat widget.
		int bottomYpos = getBottomYPos(rptWidget);

		for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
			RuntimeWidgetWrapper currentWidget = (RuntimeWidgetWrapper)selectedPanel.getWidget(index);
			if(currentWidget == rptWidget)
				continue;

			int top = currentWidget.getTopInt();
			if(top >= bottomYpos)
				currentWidget.setTopInt(top + increment);
		}

		DOM.setStyleAttribute(selectedPanel.getElement(), "height", getHeightInt()+increment+PurcConstants.UNITS);	

		setParentHeight(true,rptWidget,increment);
	}

	private void setParentHeight(boolean increase, RuntimeWidgetWrapper rptWidget, int change){
		Widget parent = rptWidget.getParent();
		while(parent != null){
			if(parent instanceof RuntimeGroupWidget){
				RuntimeWidgetWrapper wrapper = (RuntimeWidgetWrapper)((RuntimeGroupWidget)parent).getParent().getParent();
				int height = wrapper.getHeightInt();
				wrapper.setHeight((increase ? height+change : height-change)+PurcConstants.UNITS);
			}
			else if(parent instanceof FormRunnerView)
				return;

			parent = parent.getParent();
		}
	}

	private int getBottomYPos(RuntimeWidgetWrapper rptWidget){
		int bottomYpos = rptWidget.getTopInt() + rptWidget.getHeightInt();

		Widget parent = rptWidget.getParent();
		while(parent != null){
			if(parent instanceof RuntimeGroupWidget){
				RuntimeWidgetWrapper wrapper = (RuntimeWidgetWrapper)((RuntimeGroupWidget)parent).getParent().getParent();
				if(selectedPanel.getWidgetIndex(wrapper) != -1){
					bottomYpos = wrapper.getTopInt() + wrapper.getHeightInt();
					break;
				}
			}
			else if(parent instanceof FormRunnerView)
				break;

			parent = parent.getParent();
		}

		return bottomYpos;
	}


	/**
	 * @see org.purc.purcforms.client.widget.EditListener#onRowRemoved(org.purc.purcforms.client.widget.RuntimeWidgetWrapper)
	 */
	public void onRowRemoved(RuntimeWidgetWrapper rptWidget, int decrement){

		//Get the current bottom y position of the repeat widget.
		int bottomYpos = getBottomYPos(rptWidget);

		//Move widgets which are below the bottom of the repeat widget.
		for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
			RuntimeWidgetWrapper currentWidget = (RuntimeWidgetWrapper)selectedPanel.getWidget(index);
			if(currentWidget == rptWidget)
				continue;

			int top = currentWidget.getTopInt();
			if(top >= bottomYpos)
				currentWidget.setTopInt(top - decrement);
		}

		DOM.setStyleAttribute(selectedPanel.getElement(), "height", getHeightInt()-decrement+PurcConstants.UNITS);

		setParentHeight(false,rptWidget,decrement);
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
	 * Fires all validation rules that are dependent on the value of a certain widget.
	 * In other wards the value in this widget is referenced in one or more conditions
	 * or one or more validation rules. These are cross field validation rules. eg Total no
	 * of kids should be less than those that are born as male.
	 * 
	 * @param widget the widget whose value change requires re firing of other rules.
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
	 * @param parentValidationWidgetQtns
	 */
	private void initValidationWidgetsMap(List<QuestionDef> parentValidationWidgetQtns){
		int count = formDef.getValidationRuleCount();
		for(int index = 0; index < count; index++){
			ValidationRule rule = formDef.getValidationRuleAt(index);
			List<QuestionDef> qtns = rule.getValueQuestions(formDef);
			for(QuestionDef questionDef : qtns){
				if(!parentValidationWidgetQtns.contains(questionDef))
					parentValidationWidgetQtns.add(questionDef);
			}
		}
	}

	/**
	 * 
	 * @param validationRuleWidgets a list of widgets with validation rules
	 * @param qtnParentValidationWidgetMap
	 */
	private void setValidationWidgetsMap(List<RuntimeWidgetWrapper> validationRuleWidgets,HashMap<QuestionDef,RuntimeWidgetWrapper> qtnParentValidationWidgetMap){
		for(RuntimeWidgetWrapper widget : validationRuleWidgets){
			ValidationRule rule = widget.getValidationRule();
			List<QuestionDef> qtns = rule.getValueQuestions(formDef);
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


	/**
	 * Processes global keyboard events.
	 * 
	 * @param event the event.
	 * @return false if processed, else true.
	 */
	public boolean handleKeyBoardEvent(Event event){
		if(event.getCtrlKey() && event.getKeyCode() == 'S'){
			onSubmit();

			//Returning false such that firefox does not try to save the page.
			return false;
		}

		return true;
	}


	public static HashMap<QuestionDef,List<QuestionDef>> getCalcQtnMappings(FormDef formDef){
		HashMap<QuestionDef,List<QuestionDef>> calcQtnMappings = new HashMap<QuestionDef,List<QuestionDef>>();

		String qtnBinding, formBinding = "/" + formDef.getVariableName() + "/";
		for(int index = 0; index < formDef.getCalculationCount(); index++){
			Calculation calculation = formDef.getCalculationAt(index);
			String expression = calculation.getCalculateExpression();

			int pos = expression.indexOf(formBinding);
			while(pos > -1){
				int pos2 = expression.indexOf(' ', pos);
				if(pos2 > -1)
					qtnBinding = expression.substring(pos,pos2);
				else
					qtnBinding = expression.substring(pos);

				qtnBinding = qtnBinding.substring(formBinding.length());

				QuestionDef questionDef = formDef.getQuestion(qtnBinding);
				if(questionDef != null){
					List<QuestionDef> qtns = calcQtnMappings.get(questionDef);
					if(qtns == null){
						qtns = new ArrayList<QuestionDef>();
						calcQtnMappings.put(questionDef, qtns);
					}

					QuestionDef qtnDef = formDef.getQuestion(calculation.getQuestionId());
					if(!qtns.contains(qtnDef))
						qtns.add(qtnDef);
				}

				if(pos2 > -1)
					pos = expression.indexOf(formBinding,pos2+1);
				else
					break;
			}
		}

		return calcQtnMappings;
	}


	public static void updateCalcWidgetMapping(RuntimeWidgetWrapper widget, HashMap<QuestionDef,List<QuestionDef>> calcQtnMappings,
			HashMap<QuestionDef,List<RuntimeWidgetWrapper>> calcWidgetMap){

		Iterator<Entry<QuestionDef,List<QuestionDef>>> iterator = calcQtnMappings.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<QuestionDef,List<QuestionDef>> entry = iterator.next();
			if(!entry.getValue().contains(widget.getQuestionDef()))
				continue;

			List<RuntimeWidgetWrapper> widgets = calcWidgetMap.get(entry.getKey());
			if(widgets == null){
				widgets = new ArrayList<RuntimeWidgetWrapper>();
				calcWidgetMap.put(entry.getKey(), widgets);
			}

			widgets.add(widget);
		}
	}

	private String replaceCalcExpression(String calcExpression, QuestionDef questionDef){

		String expression = calcExpression;

		String qtnBinding, formBinding = "/" + formDef.getVariableName() + "/";

		int pos = expression.indexOf(formBinding);
		while(pos > -1){
			int pos2 = expression.indexOf(' ', pos);
			if(pos2 > -1)
				qtnBinding = expression.substring(pos,pos2);
			else
				qtnBinding = expression.substring(pos);

			qtnBinding = qtnBinding.substring(formBinding.length());

			QuestionDef qtnDef = formDef.getQuestion(qtnBinding);
			if(qtnDef == null)
				return "";

			expression = expression.replace(formBinding+qtnBinding, getCalcExpressionAnswer(questionDef.getDataType(),qtnDef,calcExpression));

			if(pos2 > -1)
				pos = expression.indexOf(formBinding);
			else
				break;
		}

		return expression;
	}

	private String getCalcExpressionAnswer(int type, QuestionDef questionDef, String calcExpression){
		String answer = questionDef.getAnswer();
		if(answer == null || answer.trim().length() == 0){
			if(type == QuestionDef.QTN_TYPE_NUMERIC || type == QuestionDef.QTN_TYPE_DECIMAL)
				answer = "0";
			else if(type == QuestionDef.QTN_TYPE_DATE || type == QuestionDef.QTN_TYPE_DATE_TIME ||
					type == QuestionDef.QTN_TYPE_DATE_TIME)
				answer = "";
			else
				answer = calcExpression.trim().indexOf(' ') > 0 ? "''" : "";
		}

		type = questionDef.getDataType();
		if(type ==  QuestionDef.QTN_TYPE_NUMERIC || type ==  QuestionDef.QTN_TYPE_DECIMAL)
			return answer;
		else if(answer != null && answer.trim().length() > 0 && calcExpression.trim().indexOf(' ') > 0 && !answer.equals("''"))
			return "'" + answer + "'";

		return answer;
	}


	/**
	 * Fills a widget with a list of possible values/answers from a source 
	 * external to the xform. e.g database,web services, etc.
	 * 
	 * @param widget the widget whose possible answers to fill.
	 * @param filterValue the filter value for those that are filtered.
	 */
	private void fillExternalSourceWidget(final RuntimeWidgetWrapper widget, final String filterValue){

		String url = FormUtil.getHostPageBaseURL();
		url += FormUtil.getExternalSourceUrlSuffix();
		url += WidgetEx.WIDGET_PROPERTY_EXTERNALSOURCE + "="+widget.getExternalSource();
		url += "&" + WidgetEx.WIDGET_PROPERTY_DISPLAYFIELD + "="+widget.getDisplayField();
		url += "&" + WidgetEx.WIDGET_PROPERTY_VALUEFIELD + "="+widget.getValueField();

		String filterField = widget.getFilterField();
		if(filterField != null && filterField.trim().length() > 0){
			//All QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC widgets are already changed to QTN_TYPE_LIST_EXCLUSIVE, by the time we reach here.
			//if(widget.getQuestionDef().getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC && (filterValue == null || filterValue.trim().length() == 0))
			//	return;

			if(filtDynOptWidgetMap.get(widget.getQuestionDef()) != null && (filterValue == null || filterValue.trim().length() == 0)){
				fillNextExternalSourceWidget();
				return;
			}

			url += "&FilterField=" + filterField + "&FilterValue=";

			if(filterValue == null)
				url += "IS NULL";
			else
				url += filterValue;
		}

		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,URL.encode(url));

		try{
			builder.sendRequest(null, new RequestCallback(){
				public void onResponseReceived(Request request, Response response){
					String text = response.getText();
					if(filterValue == null){
						//loading for the first time
						fillWidgetValues(text);
						fillNextExternalSourceWidget();
					}
					else{ 
						//loading later after filter.
						fillWidgetValues(text,widget);
						onValueChanged(widget.getQuestionDef());
					}
				}

				public void onError(Request request, Throwable exception){
					FormUtil.displayException(exception);
					
					if(filterValue == null)
						fillNextExternalSourceWidget();
					else
						onValueChanged(widget.getQuestionDef());
				}
			});
		}
		catch(RequestException ex){
			FormUtil.displayException(ex);
			
			if(filterValue == null)
				fillNextExternalSourceWidget();
			else
				onValueChanged(widget.getQuestionDef());
		}
	}


	private void fillNextExternalSourceWidget(){
		if(externalSourceWidgetIndex < externalSourceWidgets.size())
			fillExternalSourceWidget(externalSourceWidgets.get(externalSourceWidgetIndex++),null);
		else{
			externalSourceWidgets.clear();
			externalSourceWidgetIndex = 0;
		}
	}

	private void fillWidgetValues(String text){
		RuntimeWidgetWrapper widget = externalSourceWidgets.get(externalSourceWidgetIndex-1);
		fillWidgetValues(text,widget);
		updateDynamicOptions(widget.getQuestionDef());
	}

	private void fillWidgetValues(String text,RuntimeWidgetWrapper widget){
		if(text == null)
			return;

		QuestionDef questionDef = widget.getQuestionDef();
		questionDef.clearOptions();

		String displayField = null, valueField = null; int beginIndex = 0;
		int pos = text.indexOf(FIELD_SEPARATOR,beginIndex);
		while(pos > 0){
			displayField = text.substring(beginIndex, pos);

			beginIndex = pos+1;
			pos = text.indexOf(RECORD_SEPARATOR, beginIndex);
			if(pos > 0){
				valueField = text.substring(beginIndex, pos);
				questionDef.addOption(new OptionDef(questionDef.getOptionCount()+1,displayField,valueField,questionDef));
				beginIndex = pos+1;
				pos = text.indexOf(FIELD_SEPARATOR,beginIndex);
			}
			else{
				valueField = text.substring(beginIndex);
				questionDef.addOption(new OptionDef(questionDef.getOptionCount()+1,displayField,valueField,questionDef));
			}
		}

		widget.loadQuestion();
	}

	//Recursion fails here for very big lists and we are therefore using iteration
	/*private void fillWidgetValues(String text, int beginIndex, QuestionDef questionDef){
		String displayField = null, valueField = null;
		int pos = text.indexOf(FIELD_SEPARATOR,beginIndex);
		if(pos > 0){
			displayField = text.substring(beginIndex, pos);

			beginIndex = pos+1;
			pos = text.indexOf(RECORD_SEPARATOR, beginIndex);
			if(pos > 0){
				valueField = text.substring(beginIndex, pos);
				questionDef.addOption(new OptionDef(questionDef.getOptionCount()+1,displayField,valueField,questionDef));
				fillWidgetValues(text,pos+1,questionDef);
			}
			else{
				valueField = text.substring(beginIndex);
				questionDef.addOption(new OptionDef(questionDef.getOptionCount()+1,displayField,valueField,questionDef));
			}
		}
	}*/
}
