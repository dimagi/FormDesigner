package org.purc.purcforms.client.view;

import java.util.HashMap;
import java.util.Vector;

import org.purc.purcforms.client.controller.SubmitListener;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.model.SkipRule;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.widget.EditListener;
import org.purc.purcforms.client.widget.RuntimeGroupWidget;
import org.purc.purcforms.client.widget.RuntimeWidgetWrapper;
import org.purc.purcforms.client.widget.WidgetEx;
import org.purc.purcforms.client.xforms.XformConverter;
import org.zenika.widget.client.datePicker.DatePicker;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
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
public class FormRunnerView extends Composite implements WindowResizeListener,TabListener, EditListener{

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


	public FormRunnerView(Images images){
		this.images = images;
		
		FormUtil.maximizeWidget(tabs);

		initWidget(tabs);
		tabs.addTabListener(this);
		
		addNewTab("Page1");

		Window.addWindowResizeListener(this);
		
//		This is needed for IE
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				onWindowResized(Window.getClientWidth(), Window.getClientHeight());
			}
		});
	}

	public void loadForm(FormDef formDef,String layoutXml){
		this.formDef = formDef;

		tabs.clear();

		if(layoutXml != null && layoutXml.trim().length() > 0)
			loadLayout(layoutXml);

		moveToFirstWidget();

	}

	public void moveToFirstWidget(){
		moveToNextWidget(-1);
	}

	protected void moveToNextWidget(int index){
		boolean moved = false;

		while(++index < selectedPanel.getWidgetCount()){
			if(((RuntimeWidgetWrapper)selectedPanel.getWidget(index)).setFocus()){
				moved = true;
				break;
			}
		}

		if(!moved && this.selectedTabIndex < tabs.getWidgetCount()-1)
			tabs.selectTab(++selectedTabIndex);
	}

	public void loadLayout(String xml){
		tabs.clear();

		widgetMap = new HashMap<String,RuntimeWidgetWrapper>();

		com.google.gwt.xml.client.Document doc = XMLParser.parse(xml);
		Element root = doc.getDocumentElement();
		NodeList pages = root.getChildNodes();
		for(int i=0; i<pages.getLength(); i++){
			if(pages.item(i).getNodeType() != Node.ELEMENT_NODE)
				continue;
			Element node = (Element)pages.item(i);
			addNewTab(node.getAttribute("Text"));
			loadPage(node.getChildNodes());
		}

		if(formDef != null)
			fireRules();

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
				onWindowResized(Window.getClientWidth(), Window.getClientHeight());
			}
		});
	}

	private void addNewTab(String name){
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
				onWindowResized(Window.getClientWidth(), Window.getClientHeight());
			}
		});
	}

	protected void loadPage(NodeList nodes){
		HashMap<Integer,RuntimeWidgetWrapper> widgets = new HashMap<Integer,RuntimeWidgetWrapper>();
		int maxTabIndex = 0;

		//RuntimeWidgetWrapper wrapper = new RuntimeWidgetWrapper(null,null,null);
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
				selectedPanel.add(widget);
		}
	}

	protected int loadWidget(Element node,HashMap<Integer,RuntimeWidgetWrapper> widgets){
		RuntimeWidgetWrapper parentWrapper = null;

		String left = node.getAttribute("Left");
		String top = node.getAttribute("Top");
		String s = node.getAttribute("WidgetType");
		int tabIndex = (node.getAttribute("TabIndex") != null ? Integer.parseInt(node.getAttribute("TabIndex")) : 0);

		QuestionDef questionDef = null;
		String binding = node.getAttribute("Binding");
		if(binding != null && binding.trim().length() > 0){
			questionDef = formDef.getQuestion(binding);
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
			widget = new RuntimeGroupWidget(images,questionDef.getRepeatQtnsDef(),this);
			((RuntimeGroupWidget)widget).loadWidgets(node.getChildNodes());
			//((RuntimeGroupWidget)widget).setTabIndex(tabIndex);
		}
		else
			return tabIndex;

		RuntimeWidgetWrapper wrapper = new RuntimeWidgetWrapper(widget,images.error(),this);

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
		RuntimeWidgetWrapper parentWrapper = widgetMap.get(node.getAttribute("ParentBinding"));
		if(parentWrapper == null){
			QuestionDef qtn = formDef.getQuestion(node.getAttribute("ParentBinding"));
			if(qtn != null){
				parentWrapper = new RuntimeWidgetWrapper(widget,images.error(),this);
				parentWrapper.setQuestionDef(qtn);
				widgetMap.put(node.getAttribute("ParentBinding"), parentWrapper);
				selectedPanel.add(parentWrapper);
			}
		}	 
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

	protected boolean isValid(){
		boolean valid = true;
		int pageNo = -1;
		for(int index=0; index<tabs.getWidgetCount(); index++){
			if(!isValid((AbsolutePanel)tabs.getWidget(index))){
				valid = false;
				if(pageNo == -1)
					pageNo = index;
			}
		}
		
		if(!valid){
			Window.alert("Please first correct the errors on the form.");
			tabs.selectTab(pageNo);
		}
		return valid;
	}

	private boolean isValid(AbsolutePanel panel){
		boolean valid = true;
		for(int index=0; index<panel.getWidgetCount(); index++){
			if(!((RuntimeWidgetWrapper)panel.getWidget(index)).isValid())
				valid = false;
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

	public void onValueChanged(Widget sender, Object oldValue, Object newValue) {
		fireRules();
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
		int index = selectedPanel.getWidgetIndex(widget);
		moveToNextWidget(index);
	}

	public void onMoveToPrevWidget(Widget widget){
		int index = selectedPanel.getWidgetIndex(widget);
		boolean moved = false;

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
	
	public void onWindowResized(int width, int height) {
		height -= 110;
		sHeight = height+"px";
		super.setHeight(sHeight);
		
		if(selectedPanel != null)
			//selectedPanel.setHeight("100%");
			selectedPanel.setHeight(sHeight);
	} 
	
	public void setFormDef(FormDef formDef){
		if(this.formDef != formDef)
			tabs.clear();
		
		this.formDef = formDef;
	}
}
