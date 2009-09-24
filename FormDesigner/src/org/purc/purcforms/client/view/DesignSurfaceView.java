package org.purc.purcforms.client.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.purc.purcforms.client.Context;
import org.purc.purcforms.client.LeftPanel.Images;
import org.purc.purcforms.client.controller.DragDropListener;
import org.purc.purcforms.client.controller.FormDesignerDragController;
import org.purc.purcforms.client.controller.IWidgetPopupMenuListener;
import org.purc.purcforms.client.controller.LayoutChangeListener;
import org.purc.purcforms.client.controller.WidgetSelectionListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.OptionDef;
import org.purc.purcforms.client.model.PageDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.widget.DatePickerWidget;
import org.purc.purcforms.client.widget.DesignGroupWidget;
import org.purc.purcforms.client.widget.DesignWidgetWrapper;
import org.purc.purcforms.client.widget.WidgetEx;
import org.purc.purcforms.client.xforms.XformConverter;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventPreview;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SourcesMouseEvents;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;


/**
 * The surface onto which to drag and drop widgets.
 * 
 * @author daniel
 *
 */
public class DesignSurfaceView extends DesignGroupView implements /*WindowResizeListener,*/ TabListener,DragDropListener,SourcesMouseEvents,IWidgetPopupMenuListener{

	private String sHeight = "100%"; //"100%";
	private String sWidth = "100%";

	private LayoutChangeListener layoutChangeListener;

	//create a DropController for each drop target on which draggable widgets
	// can be dropped
	//DropController dropController;
	Vector<FormDesignerDragController> dragControllers = new Vector<FormDesignerDragController>();
	FormDef formDef;
	Document doc;

	private int embeddedHeightOffset = 0;


	public DesignSurfaceView(Images images){
		super(images);

		FormDesignerUtil.maximizeWidget(tabs);
		initPanel();
		tabs.selectTab(0);

		initWidget(tabs);
		tabs.addTabListener(this);

		DOM.sinkEvents(getElement(),DOM.getEventsSunk(getElement()) | Event.MOUSEEVENTS | Event.KEYEVENTS);

		widgetPopup = new PopupPanel(true,true);
		MenuBar menuBar = new MenuBar(true);
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.cut(),LocaleText.get("cut")),true,new Command(){
			public void execute() {widgetPopup.hide(); cutWidgets();}});

		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.copy(),LocaleText.get("copy")),true,new Command(){
			public void execute() {widgetPopup.hide(); copyWidgets(false);}});

		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.delete(),LocaleText.get("deleteItem")),true, new Command(){
			public void execute() {widgetPopup.hide(); deleteWidgets();}});

		menuBar.addSeparator(); //LocaleText.get("??????")?????????
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.add(),"Change Widget H"),true, new Command(){
			public void execute() {widgetPopup.hide(); changeWidget(false);}});

		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.add(),"Change Widget V"),true, new Command(){
			public void execute() {widgetPopup.hide(); changeWidget(true);}});

		widgetPopup.setWidget(menuBar);

		setupPopup();

		//Window.addWindowResizeListener(this);

		rubberBand.addStyleName("rubberBand");

		//DOM.sinkEvents(RootPanel.getBodyElement(), Event.ONKEYDOWN | DOM.getEventsSunk(RootPanel.getBodyElement()));

		//DOM.sinkEvents(getElement(), Event.ONKEYDOWN | DOM.getEventsSunk(getElement()));

		currentWidgetSelectionListener = this;
	}
	
	public boolean handleKeyBoardEvent(Event event){
		if(!childHandleKeyDownEvent(event))
			handleKeyDownEvent(event);
		
		return true;
	}

	private boolean childHandleKeyDownEvent(Event event){
		for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
			Widget widget = selectedPanel.getWidget(index);
			if(!(widget instanceof DesignWidgetWrapper))
				continue;
			if(!(((DesignWidgetWrapper)widget).getWrappedWidget() instanceof DesignGroupWidget))
				continue;

			if(((DesignGroupWidget)((DesignWidgetWrapper)widget).getWrappedWidget()).handleKeyDownEvent(event))
				return true;
		}

		return false;
	}

	protected void initPanel(){
		AbsolutePanel panel = new AbsolutePanel();
		FormDesignerUtil.maximizeWidget(panel);
		tabs.add(panel,"Page1");

		selectedPanel = panel;

		super.initPanel();

		dragControllers.add(tabs.getWidgetCount()-1,selectedDragController);
		panel.setHeight(sHeight);
		String s = getHeight();

		//This is needed for IE
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				//onWindowResized(Window.getClientWidth(), Window.getClientHeight());
				setHeight(getHeight());
			}
		});
	}

	private void setupPopup(){
		popup = new PopupPanel(true,true);

		MenuBar menuBar = new MenuBar(true);

		MenuBar addControlMenu = new MenuBar(true);

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("label")),true,new Command(){
			public void execute() {popup.hide(); addNewLabel(LocaleText.get("label"),true);}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("textBox")),true,new Command(){
			public void execute() {popup.hide(); addNewTextBox(true);}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("checkBox")),true,new Command(){
			public void execute() {popup.hide(); addNewCheckBox(true);}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("radioButton")),true,new Command(){
			public void execute() {popup.hide(); addNewRadioButton(true);}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("listBox")),true,new Command(){
			public void execute() {popup.hide(); addNewDropdownList(true);}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("textArea")),true,new Command(){
			public void execute() {popup.hide(); addNewTextArea(true);}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("button")),true,new Command(){
			public void execute() {popup.hide(); addNewButton(LocaleText.get("button"),null,true);}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("datePicker")),true,new Command(){
			public void execute() {popup.hide(); addNewDatePicker(true);}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("groupBox")),true,new Command(){
			public void execute() {popup.hide(); addNewGroupBox(true);}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("repeatSection")),true,new Command(){
			public void execute() {popup.hide(); addNewRepeatSection(true);}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("picture")),true,new Command(){
			public void execute() {popup.hide(); addNewPictureSection(null,null,true);}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("videoAudio")),true,new Command(){
			public void execute() {popup.hide(); addNewVideoAudioSection(null,null,true);}});

		/*addControlMenu.addSeparator();

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),"Group Box"),true,new Command(){
			public void execute() {popup.hide(); addNewButton();}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),"Auto Complete TextBox"),true,new Command(){
			public void execute() {popup.hide(); addNewTextBox();}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),"Time Picker"),true,new Command(){
			public void execute() {popup.hide(); ;}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),"Date & Time Picker"),true,new Command(){
			public void execute() {popup.hide(); ;}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),"Image"),true,new Command(){
			public void execute() {popup.hide(); ;}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),"Attachment"),true,new Command(){
			public void execute() {popup.hide(); ;}});*/

		menuBar.addItem("     "+LocaleText.get("addWidget"),addControlMenu);

		//if(selectedDragController.isAnyWidgetSelected()){
		deleteWidgetsSeparator = menuBar.addSeparator();
		deleteWidgetsMenu = menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.delete(),LocaleText.get("deleteSelected")),true,new Command(){
			public void execute() {popup.hide(); deleteWidgets();}});

		groupWidgetsSeparator = menuBar.addSeparator();
		groupWidgetsMenu = menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("groupWidgets")),true,new Command(){
			public void execute() {popup.hide(); groupWidgets();}});
		//}

		menuBar.addSeparator();	
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.add(),LocaleText.get("newTab")),true, new Command(){
			public void execute() {popup.hide(); addNewTab(null);}});

		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.delete(),LocaleText.get("deleteTab")),true, new Command(){
			public void execute() {popup.hide(); deleteTab();}});

		//if(selectedDragController.isAnyWidgetSelected()){
		cutCopySeparator = menuBar.addSeparator();
		cutMenu = menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.cut(),LocaleText.get("cut")),true,new Command(){
			public void execute() {popup.hide(); cutWidgets();}});

		copyMenu = menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.copy(),LocaleText.get("copy")),true,new Command(){
			public void execute() {popup.hide(); copyWidgets(false);}});
		//}
		//else if(clipBoardWidgets.size() > 0){
		pasteSeparator = menuBar.addSeparator();
		pasteMenu = menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.paste(),LocaleText.get("paste")),true,new Command(){
			public void execute() {popup.hide(); pasteWidgets(true);}});
		//}

		menuBar.addSeparator();	
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.add(),LocaleText.get("selectAll")),true, new Command(){
			public void execute() {popup.hide(); selectAll();}});

		menuBar.addSeparator();
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.loading(),LocaleText.get("refresh")),true,new Command(){
			public void execute() {popup.hide(); refresh();}});

		menuBar.addSeparator();
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.open(),LocaleText.get("load")),true,new Command(){
			public void execute() {popup.hide(); load();}});

		popup.setWidget(menuBar);
	}

	private void addNewTab(String name){
		initPanel();
		if(name == null)
			name = LocaleText.get("page")+(tabs.getWidgetCount());

		tabs.add(selectedPanel, name);
		selectedTabIndex = tabs.getWidgetCount() - 1;
		tabs.selectTab(selectedTabIndex);

		DesignWidgetWrapper widget = new DesignWidgetWrapper(tabs.getTabBar(),widgetPopup,this);
		widget.setBinding(name);
		pageWidgets.put(tabs.getTabBar().getTabCount()-1, widget);

		//widgetSelectionListener.onWidgetSelected(widget);

		DeferredCommand.addCommand(new Command() {
			public void execute() {
				//onWindowResized(Window.getClientWidth(), Window.getClientHeight());
				setHeight(getHeight());
			}
		});
	}



	/*public void onWindowResized(int width, int height){
		//height -= (160+embeddedHeightOffset); //(160 + 30);
		//sHeight = height+"px";
		super.setHeight(sHeight);


	}*/

	public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex){
		return true;
	}

	public void onTabSelected(SourcesTabEvents sender, int tabIndex){
		selectedTabIndex = tabIndex;

		selectedDragController = dragControllers.elementAt(selectedTabIndex);
		selectedPanel = selectedDragController.getBoundaryPanel();

		widgetSelectionListener.onWidgetSelected(getSelPageDesignWidget(),false);
	}

	public void setWidgetSelectionListener(WidgetSelectionListener  widgetSelectionListener){
		this.widgetSelectionListener = widgetSelectionListener;
	}

	public String getLayoutXml(){

		if(tabs.getWidgetCount() == 0)
			return null;

		com.google.gwt.xml.client.Document doc = XMLParser.createDocument();
		Element rootNode = doc.createElement("Form");
		if(formDef != null)
			rootNode.setAttribute(XformConverter.ATTRIBUTE_NAME_ID, formDef.getId()+"");
		doc.appendChild(rootNode);

		this.doc = doc;

		boolean hasWidgets = false;
		AbsolutePanel prevSelPanel = selectedPanel;
		for(int i=0; i<tabs.getWidgetCount(); i++){
			Element node = doc.createElement("Page");
			node.setAttribute(WidgetEx.WIDGET_PROPERTY_TEXT, DesignWidgetWrapper.getTabDisplayText(tabs.getTabBar().getTabHTML(i)));
			//node.setAttribute("BackgroundColor", tabs.getTabBar().getTabHTML(i));

			if(pageWidgets.get(i) != null)
				node.setAttribute(WidgetEx.WIDGET_PROPERTY_BINDING, pageWidgets.get(i).getBinding());

			rootNode.appendChild(node);
			AbsolutePanel panel = (AbsolutePanel)tabs.getWidget(i);

			selectedPanel = panel;
			node.setAttribute(WidgetEx.WIDGET_PROPERTY_WIDTH, getWidth());
			node.setAttribute(WidgetEx.WIDGET_PROPERTY_HEIGHT, getHeight());
			node.setAttribute(WidgetEx.WIDGET_PROPERTY_BACKGROUND_COLOR, getBackgroundColor());

			boolean b = buildLayoutXml(node,panel,doc);
			if(b)
				hasWidgets = true;
		}

		selectedPanel = prevSelPanel;

		if(hasWidgets)
			return FormDesignerUtil.formatXml(doc.toString());
		else if(formDef != null)
			//TODO This accidentally overwrites form layout after refresh when user has not refreshed the design surface.
			//When user clears widgets, the setting of formdef layout to null will be done immediately
			//after the delete.
			;//formDef.setLayoutXml(null);

		return null;
	}

	public Element getLanguageNode(){
		if(tabs.getWidgetCount() == 0)
			return null;

		com.google.gwt.xml.client.Document doc = XMLParser.createDocument();
		Element rootNode = doc.createElement("Form");
		if(formDef != null)
			rootNode.setAttribute(XformConverter.ATTRIBUTE_NAME_ID, formDef.getId()+"");
		doc.appendChild(rootNode);

		//String xpath = "Form/Page/Item[@Binding='";
		//String xpath = "Form/Page/Item[@";
		for(int i=0; i<tabs.getWidgetCount(); i++){
			if(pageWidgets.get(i) == null)
				continue; //TODO Need to deal with this case where all widgets are deleted but layout and locale text remains

			String xpath = "Form/Page[@Binding='"+pageWidgets.get(i).getBinding()+"']/Item[@";

			String text = DesignWidgetWrapper.getTabDisplayText(tabs.getTabBar().getTabHTML(i));
			Element node = doc.createElement(XformConverter.NODE_NAME_TEXT);
			node.setAttribute(XformConverter.ATTRIBUTE_NAME_XPATH, "Form/Page[@Binding='"+pageWidgets.get(i).getBinding()+"'][@Text]");
			node.setAttribute(XformConverter.ATTRIBUTE_NAME_VALUE, text);
			rootNode.appendChild(node);

			buildLanguageNode((AbsolutePanel)tabs.getWidget(i),doc, rootNode,xpath);
		}

		return rootNode;
	}

	private void buildLanguageNode(AbsolutePanel panel,com.google.gwt.xml.client.Document doc, Element parentNode, String xpath){
		for(int i=0; i<panel.getWidgetCount(); i++){
			Widget widget = panel.getWidget(i);
			if(!(widget instanceof DesignWidgetWrapper))
				continue;
			((DesignWidgetWrapper)widget).buildLanguageXml(doc,parentNode, xpath);
		}
	}

	private boolean buildLayoutXml(Element parent, AbsolutePanel panel, com.google.gwt.xml.client.Document doc){
		for(int i=0; i<panel.getWidgetCount(); i++){
			Widget widget = panel.getWidget(i);
			if(!(widget instanceof DesignWidgetWrapper))
				continue;
			((DesignWidgetWrapper)widget).buildLayoutXml(parent, doc);
		}

		return panel.getWidgetCount() > 0;
	}

	public boolean setLayoutXml(String xml, FormDef formDef){
		this.formDef = formDef;

		PaletteView.unRegisterAllDropControllers();
		tabs.clear();

		if(xml == null || xml.trim().length() == 0){
			addNewTab(null);
			return false;
		}

		com.google.gwt.xml.client.Document doc = XMLParser.parse(xml);
		Element root = doc.getDocumentElement();
		NodeList pages = root.getChildNodes();
		for(int i=0; i<pages.getLength(); i++){
			if(pages.item(i).getNodeType() != Node.ELEMENT_NODE)
				continue;
			Element node = (Element)pages.item(i);
			addNewTab(node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));

			((DesignGroupView)this).setWidth(node.getAttribute(WidgetEx.WIDGET_PROPERTY_WIDTH));
			((DesignGroupView)this).setHeight(node.getAttribute(WidgetEx.WIDGET_PROPERTY_HEIGHT));
			((DesignGroupView)this).setBackgroundColor(node.getAttribute(WidgetEx.WIDGET_PROPERTY_BACKGROUND_COLOR));

			loadPage(node.getChildNodes());
		}

		this.doc = doc;

		if(tabs.getWidgetCount() > 0){
			selectedTabIndex = 0;
			tabs.selectTab(selectedTabIndex);
		}

		return true;
	}

	private void loadPage(NodeList nodes){
		for(int i=0; i<nodes.getLength(); i++){
			if(nodes.item(i).getNodeType() != Node.ELEMENT_NODE)
				continue;

			try{
				Element node = (Element)nodes.item(i);
				DesignWidgetWrapper widget = loadWidget(node,selectedDragController,selectedPanel,images,widgetPopup,this,currentWidgetSelectionListener,formDef); //TODO CHECK ???????????????
				if(widget != null && (widget.getWrappedWidget() instanceof DesignGroupWidget)){
					((DesignGroupWidget)widget.getWrappedWidget()).loadWidgets(node,formDef);
					((DesignGroupWidget)widget.getWrappedWidget()).setWidgetSelectionListener(currentWidgetSelectionListener); //TODO CHECK
					if(!widget.isRepeated())
						selectedDragController.makeDraggable(widget, ((DesignGroupWidget)widget.getWrappedWidget()).getHeaderLabel());
				}
			}
			catch(Exception ex){
				//FormUtil.displayException(ex);
				ex.printStackTrace();
			}
		}
	}

	public static DesignWidgetWrapper loadWidget(Element node,FormDesignerDragController dragController, AbsolutePanel panel, Images images, PopupPanel widgetPopup, IWidgetPopupMenuListener widgetPopupMenuListener,WidgetSelectionListener widgetSelectionListener,FormDef formDef){
		String left = node.getAttribute(WidgetEx.WIDGET_PROPERTY_LEFT);
		String top = node.getAttribute(WidgetEx.WIDGET_PROPERTY_TOP);
		String s = node.getAttribute(WidgetEx.WIDGET_PROPERTY_WIDGETTYPE);

		Widget widget = null;
		if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_RADIOBUTTON))
			widget = new RadioButton(node.getAttribute(WidgetEx.WIDGET_PROPERTY_PARENTBINDING),node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_CHECKBOX))
			widget = new CheckBox(node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_BUTTON))
			widget = new Button(node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_LISTBOX))
			widget = new ListBox(false);
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_TEXTAREA))
			widget = new TextArea();
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_IMAGE))
			widget = images.picture().createImage();
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_VIDEO_AUDIO))
			widget = new Hyperlink(node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT),null);
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_DATEPICKER))
			widget = new DatePickerWidget();
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_TEXTBOX))
			widget = new TextBox();
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_LABEL))
			widget = new Label(node.getAttribute(WidgetEx.WIDGET_PROPERTY_TEXT));
		else if(s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_GROUPBOX) || s.equalsIgnoreCase(WidgetEx.WIDGET_TYPE_REPEATSECTION))
			widget = new DesignGroupWidget(images,widgetPopupMenuListener);
		else
			return null; 

		DesignWidgetWrapper wrapper = new DesignWidgetWrapper(widget,widgetPopup,widgetSelectionListener);
		wrapper.setLayoutNode(node);

		String value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_HELPTEXT);
		if(value != null && value.trim().length() > 0)
			wrapper.setTitle(value);

		value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_EXTERNALSOURCE);
		if(value != null && value.trim().length() > 0)
			wrapper.setExternalSource(value);

		value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_DISPLAYFIELD);
		if(value != null && value.trim().length() > 0)
			wrapper.setDisplayField(value);

		value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_VALUEFIELD);
		if(value != null && value.trim().length() > 0)
			wrapper.setValueField(value);

		value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_WIDTH);
		if(value != null && value.trim().length() > 0)
			wrapper.setWidth(value);

		value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_HEIGHT);
		if(value != null && value.trim().length() > 0)
			wrapper.setHeight(value);

		value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_REPEATED);
		if(value != null && value.trim().length() > 0)
			wrapper.setRepeated(value.equals(WidgetEx.REPEATED_TRUE_VALUE));

		String binding = node.getAttribute(WidgetEx.WIDGET_PROPERTY_BINDING);
		if(binding != null && binding.trim().length() > 0)
			wrapper.setBinding(binding);
		else
			binding = null;

		String parentBinding = node.getAttribute(WidgetEx.WIDGET_PROPERTY_PARENTBINDING);
		if(parentBinding != null && parentBinding.trim().length() > 0)
			wrapper.setParentBinding(parentBinding);
		else
			parentBinding = null;

		value = node.getAttribute(WidgetEx.WIDGET_PROPERTY_TABINDEX);
		if(value != null && value.trim().length() > 0)
			wrapper.setTabIndex(Integer.parseInt(value));

		//if(wrapper.getWrappedWidget() instanceof Label)
		WidgetEx.loadLabelProperties(node,wrapper);

		if(formDef != null && binding != null && parentBinding == null){
			QuestionDef questionDef = formDef.getQuestion(binding);
			if(questionDef != null)
				wrapper.setQuestionDef(questionDef);
		}

		/*if(formDef != null && (binding != null || parentBinding != null)){
			QuestionDef questionDef = formDef.getQuestion(parentBinding != null ? parentBinding : binding);
			if(questionDef != null){
				wrapper.setQuestionDef(questionDef);
			}
		}*/

		if(!"true".equals(node.getAttribute(WidgetEx.WIDGET_PROPERTY_HEADER_LABEL)))
			dragController.makeDraggable(wrapper);

		panel.add(wrapper);
		FormDesignerUtil.setWidgetPosition(wrapper, left, top);

		return wrapper;
	}

	public boolean deleteWidgets(){
		if(super.deleteWidgets() && doc != null){
			String layout = null;
			if(!(tabs.getTabBar().getTabCount() == 1 && (selectedPanel == null || (selectedPanel != null && selectedPanel.getWidgetCount() == 0))))
				layout = FormUtil.formatXml(doc.toString());
			layoutChangeListener.onLayoutChanged(layout);

			return true;
		}
		
		if(tabs.getTabBar().getTabCount() == 1 && selectedPanel != null && selectedPanel.getWidgetCount() == 0)
			layoutChangeListener.onLayoutChanged(null);

		return true;
	}

	private void deleteTab(){
		if(tabs.getWidgetCount() == 1){
			if(formDef != null)
				formDef.setLayoutXml(null); //TODO Check if this does not bring bugs
			Window.alert(LocaleText.get("cantDeleteAllTabs"));
			return;
		}

		if(selectedPanel.getWidgetCount() > 0){
			Window.alert(LocaleText.get("deleteAllTabWidgetsFirst"));
			return;
		}

		if(!Window.confirm(LocaleText.get("deleteTabPrompt")))
			return;

		FormDesignerDragController dragController = dragControllers.remove(selectedTabIndex);
		PaletteView.unRegisterDropController(dragController.getFormDesignerDropController());

		tabs.remove(selectedTabIndex);
		pageWidgets.remove(selectedTabIndex);
		if(selectedTabIndex > 0)
			selectedTabIndex -= 1;
		tabs.selectTab(selectedTabIndex);
	}

	public void setLayout(FormDef formDef){			
		this.formDef = formDef;

		PaletteView.unRegisterAllDropControllers();
		tabs.clear();

		Vector pages = formDef.getPages();
		if(pages != null){
			for(int i=0; i<pages.size(); i++){
				PageDef pageDef = (PageDef)pages.get(i);
				addNewTab(pageDef.getName());
				loadPage(pageDef);
			}

			//TODO May need to support multiple listeners.
			layoutChangeListener.onLayoutChanged(getLayoutXml());
		}

		if(tabs.getWidgetCount() > 0){
			selectedTabIndex = 0;
			tabs.selectTab(selectedTabIndex);
		}

	}

	private void loadPage(PageDef pageDef){
		loadQuestions(pageDef.getQuestions(),pageDef.getName());
	}


	/**
	 * Does automatic loading of question widgets onto the design surface.
	 * 
	 * @param questions
	 * @param pageName
	 */
	private void loadQuestions(List<QuestionDef> questions, String pageName){
		int max = 999999; //FormUtil.convertDimensionToInt(sHeight) - 0 + 150; //40; No longer adding submit button on every page
		int tabIndex = 0;
		x = y = 20;

		x += selectedPanel.getAbsoluteLeft();
		y += selectedPanel.getAbsoluteTop();

		DesignWidgetWrapper widgetWrapper = null;
		for(int i=0; i<questions.size(); i++){
			QuestionDef questionDef = (QuestionDef)questions.get(i);
			int type = questionDef.getDataType();

			if(!(type == QuestionDef.QTN_TYPE_VIDEO || type == QuestionDef.QTN_TYPE_AUDIO || type == QuestionDef.QTN_TYPE_IMAGE)){
				widgetWrapper = addNewLabel(questionDef.getText(),false);
				widgetWrapper.setBinding(questionDef.getVariableName());
				widgetWrapper.setTitle(questionDef.getText());
			}

			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT){
				widgetWrapper.setFontWeight("bold");
				widgetWrapper.setFontStyle("italic");
			}

			widgetWrapper = null;

			if(!(type == QuestionDef.QTN_TYPE_VIDEO || type == QuestionDef.QTN_TYPE_AUDIO || type == QuestionDef.QTN_TYPE_IMAGE))
				x += (questionDef.getText().length() * 10);

			if(type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
					type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC)
				widgetWrapper = addNewDropdownList(false);
			else if(type == QuestionDef.QTN_TYPE_DATE)
				widgetWrapper = addNewDatePicker(false);
			else if(type == QuestionDef.QTN_TYPE_LIST_MULTIPLE){
				widgetWrapper = addNewCheckBoxSet(questionDef,max,pageName,true,tabIndex);
				tabIndex += questionDef.getOptions().size();
			}
			else if(type == QuestionDef.QTN_TYPE_BOOLEAN)
				widgetWrapper = addNewDropdownList(false);
			else if(type == QuestionDef.QTN_TYPE_REPEAT)
				widgetWrapper = addNewRepeatSet(questionDef,max,pageName,false);
			else if(type == QuestionDef.QTN_TYPE_IMAGE)
				widgetWrapper = addNewPictureSection(questionDef.getVariableName(),questionDef.getText(),false);
			else if(type == QuestionDef.QTN_TYPE_VIDEO || type == QuestionDef.QTN_TYPE_AUDIO)
				widgetWrapper = addNewVideoAudioSection(questionDef.getVariableName(),questionDef.getText(),false);
			else
				widgetWrapper = addNewTextBox(false);

			if(widgetWrapper != null){
				if(!(type == QuestionDef.QTN_TYPE_IMAGE|| type == QuestionDef.QTN_TYPE_VIDEO|| type == QuestionDef.QTN_TYPE_AUDIO))
					widgetWrapper.setBinding(questionDef.getVariableName());

				widgetWrapper.setQuestionDef(questionDef);

				String helpText = questionDef.getHelpText();
				if(helpText != null && helpText.trim().length() > 0)
					helpText = questionDef.getHelpText();
				else
					helpText = questionDef.getText();

				widgetWrapper.setTitle(helpText);
				widgetWrapper.setTabIndex(++tabIndex);
			}

			x = 20 + selectedPanel.getAbsoluteLeft();
			y += 40;

			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_IMAGE)
				y += 195 + 30;
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_VIDEO || questionDef.getDataType() == QuestionDef.QTN_TYPE_AUDIO)
				y += 75 + 30;

			int rptIncr = 0;
			if(i < questions.size()-1){
				int dataType = ((QuestionDef)questions.get(i+1)).getDataType();
				if(dataType == QuestionDef.QTN_TYPE_REPEAT)
					rptIncr = 90 + 50;
				else if(dataType == QuestionDef.QTN_TYPE_IMAGE)
					rptIncr = 195 + 30;
				else if(dataType == QuestionDef.QTN_TYPE_VIDEO || dataType == QuestionDef.QTN_TYPE_AUDIO)
					rptIncr = 75 + 30;
			}

			if((y+40+rptIncr) > max){
				y += 10;
				//addNewButton(false);
				addNewTab(pageName);
				y = 20 + selectedPanel.getAbsoluteTop();
			}
		}

		y += 10;

		//The submit button is added only to the first tab such that we dont keep
		//adding multiple submit buttons everytime one refreshes the design surface
		if(tabs.getTabBar().getTabCount() == 1){
			addSubmitButton(false);
			
			x += 200;
			addCancelButton(false);
		}

		setHeight(y+40+"px");
	}

	/*public void stopHeaderLabelEdit(DesignWidgetWrapper headerLabel){
		if(selectedDragController.getSelectedWidgetCount() == 0)
			return;

		DesignWidgetWrapper group = (DesignWidgetWrapper)selectedDragController.getSelectedWidgetAt(0);
		assert(group.getWrappedWidget() instanceof DesignGroupWidget);
		selectedDragController.makeDraggable(group,headerLabel);
		editWidget = null;
	}*/

	protected DesignWidgetWrapper addNewCheckBoxSet(QuestionDef questionDef, int max, String pageName, boolean vertically, int tabIndex){
		List options = questionDef.getOptions();
		for(int i=0; i<options.size(); i++){
			/*if(i != 0){
				y += 40;

				if((y+40) > max){
					y += 10;
					//addNewButton(false);
					addNewTab(pageName);
					y = 20;
				}
			}*/
			
			OptionDef optionDef = (OptionDef)options.get(i);
			DesignWidgetWrapper wrapper = addNewWidget(new CheckBox(optionDef.getText()),false);
			wrapper.setBinding(optionDef.getVariableName());
			wrapper.setParentBinding(questionDef.getVariableName());
			wrapper.setText(optionDef.getText());
			wrapper.setTitle(optionDef.getText());
			wrapper.setTabIndex(++tabIndex);
			
			if(vertically)
				y += 40;
			else
				x += (optionDef.getText().length() * 12);
		}
		return null;
	}

	protected DesignWidgetWrapper addNewRepeatSet(QuestionDef questionDef, int max, String pageName, boolean select){
		x = 35 + selectedPanel.getAbsoluteLeft();
		y += 25;

		Vector questions = questionDef.getRepeatQtnsDef().getQuestions();
		if(questions == null)
			return addNewTextBox(select); //TODO Bug here
		for(int index = 0; index < questions.size(); index++){
			QuestionDef qtn = (QuestionDef)questions.get(index);
			if(index > 0)
				x += 210;
			DesignWidgetWrapper label = addNewLabel(qtn.getText(),select);
			label.setBinding(qtn.getVariableName());
			label.setTextDecoration("underline");
		}

		x = 20 + selectedPanel.getAbsoluteLeft();
		y += 25;
		DesignWidgetWrapper widget = addNewRepeatSection(select);

		FormDesignerDragController selDragController = selectedDragController;
		AbsolutePanel absPanel = selectedPanel;
		PopupPanel wgpopup = widgetPopup;
		WidgetSelectionListener wgSelectionListener = currentWidgetSelectionListener;
		currentWidgetSelectionListener = (DesignGroupWidget)widget.getWrappedWidget();

		int oldY = y;
		y = x = 10;

		selectedDragController = widget.getDragController();
		selectedPanel = widget.getPanel();
		widgetPopup = widget.getWidgetPopup();

		/*y = 30;
		Vector questions = questionDef.getRepeatQtnsDef().getQuestions();
		if(questions == null)
			return addNewTextBox(select); //TODO Bug here
		for(int index = 0; index < questions.size(); index++){
			QuestionDef qtn = (QuestionDef)questions.get(index);
			if(index > 0)
				x += 210;
			DesignWidgetWrapper label = addNewLabel(qtn.getText(),select);
			label.setBinding(qtn.getVariableName());
			label.setTextDecoration("underline");
		}*/

		//y = x = 10;
		x += selectedPanel.getAbsoluteLeft();
		y += selectedPanel.getAbsoluteTop() + 0; //50;

		DesignWidgetWrapper widgetWrapper = null;
		for(int index = 0; index < questions.size(); index++){
			QuestionDef qtn = (QuestionDef)questions.get(index);
			if(index > 0)
				x += 205;

			if(qtn.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || 
					qtn.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC)
				widgetWrapper = addNewDropdownList(false);
			else if(qtn.getDataType() == QuestionDef.QTN_TYPE_DATE)
				widgetWrapper = addNewDatePicker(false);
			else if(qtn.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE){
				widgetWrapper = addNewCheckBoxSet(qtn,max,pageName,false,index);
				index += qtn.getOptions().size();
			}
			else if(qtn.getDataType() == QuestionDef.QTN_TYPE_BOOLEAN)
				widgetWrapper = addNewDropdownList(false);
			else if(qtn.getDataType() == QuestionDef.QTN_TYPE_IMAGE)
				widgetWrapper = addNewPicture(select);
			else if(qtn.getDataType() == QuestionDef.QTN_TYPE_VIDEO ||
					qtn.getDataType() == QuestionDef.QTN_TYPE_AUDIO)
				widgetWrapper = addNewVideoAudioSection(null,qtn.getText(),select);
			else
				widgetWrapper = addNewTextBox(select);

			if(widgetWrapper != null){//addNewCheckBoxSet returns null
				widgetWrapper.setBinding(qtn.getVariableName());
				widgetWrapper.setQuestionDef(qtn);
				widgetWrapper.setTitle(qtn.getText());
				widgetWrapper.setTabIndex(index + 1);
			}
		}

		selectedDragController.clearSelection();

		selectedDragController = selDragController;
		selectedPanel = absPanel;
		widgetPopup = wgpopup;
		currentWidgetSelectionListener = wgSelectionListener;

		y = oldY;
		y += 90; //130; //25;

		if(questions.size() == 1)
			widget.setWidthInt(265);
		else
			widget.setWidthInt((questions.size() * 205)+15);
		return widget;
	}

	public void setFormDef(FormDef formDef){	
		if(this.formDef != formDef){
			PaletteView.unRegisterAllDropControllers();
			tabs.clear();
			addNewTab(null);
		}

		this.formDef = formDef;
	}

	public void refresh(){
		if(formDef == null)
			return;

		FormUtil.dlg.setText(LocaleText.get("refreshingDesignSurface"));
		FormUtil.dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {
				try{
					boolean loading = false;
					if(!(tabs.getTabBar().getTabCount() == 1 && (selectedPanel == null || (selectedPanel != null && selectedPanel.getWidgetCount() == 0))))
						loadNewWidgets();
					else{
						if(formDef.getLayoutXml() != null && formDef.getLayoutXml().trim().length() > 0 && selectedPanel != null && selectedPanel.getWidgetCount() == 0){
							loading = true;
							load();
						}
						else
							setLayout(formDef);
					}

					if(!loading)
						FormUtil.dlg.hide();
				}
				catch(Exception ex){
					FormUtil.dlg.hide();
					FormUtil.displayException(ex);
				}
			}
		});
	}

	public void load(){
		if(formDef == null)
			return;

		//AbsolutePanel panel (AbsolutePanel)tabs.getWidget(i);
		if((selectedPanel != null && selectedPanel.getWidgetCount() > 0) || tabs.getTabBar().getTabCount() > 1){
			Window.alert(LocaleText.get("deleteAllWidgetsFirst"));
			return;
		}

		FormUtil.dlg.setText(LocaleText.get("loadingDesignSurface"));
		FormUtil.dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {
				try{
					if(!setLayoutXml(formDef.getLayoutXml(), formDef))
						refresh();
					else
						FormUtil.dlg.hide();
				}
				catch(Exception ex){
					FormUtil.dlg.hide();
					FormUtil.displayException(ex);
				}
			}
		});
	}

	/**
	 * Loads widgets that have not yet been loaded on the design surface. If if they were once
	 * loaded, have been deleted.
	 */
	private void loadNewWidgets(){
		
		//Create list of bindings for widgets that are already loaded on the design surface.
		HashMap<String,String> bindings = new HashMap<String, String>();
		for(int i=0; i<dragControllers.size(); i++){
			AbsolutePanel panel = dragControllers.elementAt(i).getBoundaryPanel();
			fillBindings(panel,bindings);
		}

		//Create a list of questions that have not yet been loaded on the design surface.
		List<QuestionDef> newQuestions = new ArrayList<QuestionDef>();
		for(int index = 0; index < formDef.getPageCount(); index++)
			fillNewQuestions(formDef.getPageAt(index),newQuestions,bindings);

		//Load the new questions onto the design surface.
		if(newQuestions.size() > 0){
			String pageName = LocaleText.get("page")+(tabs.getTabBar().getTabCount()+1);
			addNewTab(pageName);
			loadQuestions(newQuestions,pageName);
		}
	}

	/**
	 * Fills bindings for loaded widgets in a given panel.
	 * 
	 * @param panel the panel.
	 * @param bindings the map of bindings. Made a map instead of list for only easy of search with key.
	 */
	private void fillBindings(AbsolutePanel panel,HashMap<String,String> bindings){
		if(panel.getWidgetIndex(rubberBand) > -1)
			panel.remove(rubberBand);

		for(int index = 0; index < panel.getWidgetCount(); index++){
			DesignWidgetWrapper widget = (DesignWidgetWrapper)panel.getWidget(index);
			
			//When a widget is deleted, it is reloaded on refresh even if its label still exists.
			if(widget.getWrappedWidget() instanceof Label)
				continue;
			
			String binding = widget.getParentBinding();
			if(binding == null)
				binding = widget.getBinding();
			bindings.put(binding, binding); //Could possibly put widget as value.
			
			if(widget.getWrappedWidget() instanceof DesignGroupWidget)
				fillBindings(((DesignGroupWidget)widget.getWrappedWidget()).getPanel(),bindings);
		}
	}

	/**
	 * Fills questions, in a page, that are not loaded on the design surface.
	 * 
	 * @param pageDef the page.
	 * @param newQuestions a list of the new questions.
	 * @param bindings a map of bindings for questions that are aleady loaded on the design surface.
	 */
	private void fillNewQuestions(PageDef pageDef, List<QuestionDef> newQuestions, HashMap<String,String> bindings){
		for(int index = 0; index < pageDef.getQuestionCount(); index ++){
			QuestionDef questionDef = pageDef.getQuestionAt(index);
			if(!bindings.containsKey(questionDef.getVariableName()))
				newQuestions.add(questionDef);
		}
	}

	public void setEmbeddedHeightOffset(int offset){
		embeddedHeightOffset = offset;
	}

	public void setLayoutChangeListener(LayoutChangeListener layoutChangeListener){
		this.layoutChangeListener = layoutChangeListener;
	}

	/*public DesignWidgetWrapper onDrop(Widget widget,int x, int y){
		if(!(widget instanceof PaletteWidget))
			return null;

		DesignWidgetWrapper retWidget = super.onDrop(widget, x, y);

		String text = ((PaletteWidget)widget).getText();

		if(text.equals(LocaleText.get("groupBox")))
			retWidget = addNewGroupBox(true);
		else if(text.equals(LocaleText.get("repeatSection")))
			retWidget = addNewRepeatSection(true);
		else if(text.equals(LocaleText.get("picture")))
			retWidget = addNewPictureSection(null,null,true);
		else if(text.equals(LocaleText.get("videoAudio")))
			retWidget = addNewVideoAudioSection(null,null,true);
		else if(text.equals(LocaleText.get("searchServer")))
			retWidget = addNewSearchServerWidget(null,null,true);

		if(retWidget != null){
			int height = FormUtil.convertDimensionToInt(getHeight());
			int h = retWidget.getTopInt() + retWidget.getHeightInt();
			if(height < h)
				setHeight(height + (h-height)+10+"px");
		}

		return retWidget;
	}*/

	public void onWidgetSelected(DesignWidgetWrapper widget, boolean multipleSel){

		boolean ctrlKey = FormDesignerUtil.getCtrlKey();
		if(!ctrlKey)
			stopLabelEdit(false);

		if(widget == null){
			selectedDragController.clearSelection(); //New and may cause bugs
			//widgetSelectionListener.onWidgetSelected(widget); //New and may cause bugs	
			return;
		}
		//if(selectedPanel.getWidgetIndex(widget) > -1)
		//	selectedDragController.toggleSelection(widget);

		if(!(widget.getWrappedWidget() instanceof TabBar)){
			//Event event = DOM.eventGetCurrentEvent(); //TODO verify that this does not introduce a bug
			//if(event != null && DOM.eventGetType(event) == Event.ONCONTEXTMENU){
			if(selectedPanel.getWidgetIndex(widget) > -1){
				if(!ctrlKey){
					if(selectedDragController.getSelectedWidgetCount() == 1)
						selectedDragController.clearSelection();
					selectedDragController.selectWidget(widget);
				}

				clearGroupBoxSelection();
			}
			else{

				if(!(widget.getWrappedWidget() instanceof Label && "100%".equals(widget.getWidth())))
					selectedDragController.clearSelection();

				for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
					Widget wid = selectedPanel.getWidget(index);
					if(!(wid instanceof DesignWidgetWrapper))
						continue;
					if(!(((DesignWidgetWrapper)wid).getWrappedWidget() instanceof DesignGroupWidget))
						continue;

					DesignGroupWidget designGroupWidget = (DesignGroupWidget)((DesignWidgetWrapper)wid).getWrappedWidget();
					if(!designGroupWidget.containsWidget(widget))
						designGroupWidget.clearGroupBoxSelection();
				}
			}
		}

		widgetSelectionListener.onWidgetSelected(widget, multipleSel);
	}

	protected void selectAll(){
		if(editWidget != null){
			txtEdit.selectAll();
			return; //let label editor do select all
		}
		
		List<Widget> widgets = selectedDragController.getSelectedWidgets();
		if(widgets != null){
			for(int index = 0; index < widgets.size(); index++){
				DesignWidgetWrapper widget = (DesignWidgetWrapper)widgets.get(index);
				if(widget.getWrappedWidget() instanceof DesignGroupWidget){
					selectedDragController.clearSelection();
					((DesignGroupWidget)widget.getWrappedWidget()).selectAll();
					//return;
				}
			}
		}

		super.selectAll();
	}

	public void setHeight(String height) {
		if(height != null && height.trim().length() > 0 && !height.equals("100%"))
			sHeight = height;
		super.setHeight(sHeight);

		//for(int i=0; i<dragControllers.size(); i++)
		//	dragControllers.elementAt(i).getBoundaryPanel().setHeight(sHeight);
	}

	public void setWidth(String width) {
		if(width != null && width.trim().length() > 0 && !width.equals("100%"))
			sWidth = width;
		super.setWidth(sWidth);

		//for(int i=0; i<dragControllers.size(); i++)
		//	dragControllers.elementAt(i).getBoundaryPanel().setWidth(sWidth);
	}
	
	public String getSelectedPageHtml(){
		if(selectedPanel == null)
			return "";
		return selectedPanel.getElement().getInnerHTML();
	}
}
