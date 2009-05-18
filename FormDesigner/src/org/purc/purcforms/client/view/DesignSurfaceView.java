package org.purc.purcforms.client.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.purc.purcforms.client.LeftPanel.Images;
import org.purc.purcforms.client.controller.DragDropListener;
import org.purc.purcforms.client.controller.FormDesignerDragController;
import org.purc.purcforms.client.controller.FormDesignerDropController;
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
import org.zenika.widget.client.datePicker.DatePicker;

import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventPreview;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.MouseListenerCollection;
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
public class DesignSurfaceView extends Composite implements /*WindowResizeListener,*/ TabListener,WidgetSelectionListener,DragDropListener,SourcesMouseEvents,IWidgetPopupMenuListener{

	private MouseListenerCollection mouseListeners;
	private static final int MOVE_LEFT = 1;
	private static final int MOVE_RIGHT = 2;
	private static final int MOVE_UP = 3;
	private static final int MOVE_DOWN = 4;

	private DecoratedTabPanel tabs = new DecoratedTabPanel();
	private PopupPanel popup;
	private PopupPanel widgetPopup;
	private final Images images;
	private int selectedTabIndex = 0;
	private String sHeight = "100%";
	private int x;
	private int y;
	private List<DesignWidgetWrapper> clipBoardWidgets = new Vector<DesignWidgetWrapper>();
	private int clipboardLeftMostPos;
	private int clipboardTopMostPos;
	private MenuItem copyMenu;
	private MenuItem cutMenu;
	private MenuItem pasteMenu;
	private MenuItem deleteWidgetsMenu;
	private MenuItemSeparator cutCopySeparator;
	private MenuItemSeparator pasteSeparator;
	private MenuItemSeparator deleteWidgetsSeparator;

	private AbsolutePanel selectedPanel = new AbsolutePanel();
	private FormDesignerDragController selectedDragController;
	private WidgetSelectionListener widgetSelectionListener;
	private WidgetSelectionListener currentWidgetSelectionListener;
	private DesignWidgetWrapper selectedWidget;
	private LayoutChangeListener layoutChangeListener;

	private int selectionXPos;
	private int selectionYPos;
	private boolean mouseMoved = false;

	private Label rubberBand = new Label(""); //HTML("<DIV ID='rubberBand'></DIV>");

	//create a DropController for each drop target on which draggable widgets
	// can be dropped
	//DropController dropController;
	Vector<FormDesignerDragController> dragControllers = new Vector<FormDesignerDragController>();
	FormDef formDef;
	Document doc;

	private int embeddedHeightOffset = 0;

	public DesignSurfaceView(Images images){
		this.images = images;

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

		widgetPopup.setWidget(menuBar);

		setupPopup();

		//Window.addWindowResizeListener(this);

		rubberBand.addStyleName("rubberBand");

		//DOM.sinkEvents(RootPanel.getBodyElement(), Event.ONKEYDOWN | DOM.getEventsSunk(RootPanel.getBodyElement()));

		DOM.sinkEvents(getElement(), Event.ONKEYDOWN | DOM.getEventsSunk(getElement()));

		DOM.addEventPreview(new EventPreview() { 
			public boolean onEventPreview(Event event) 
			{ 
				if (DOM.eventGetType(event) == Event.ONKEYDOWN) {
					//DOM.eventPreventDefault(pEvent);
					onBrowserEvent(event);
					return true;
				}
				return true;
			}
		});

		currentWidgetSelectionListener = this;
	}

	private void initPanel(){
		AbsolutePanel panel = new AbsolutePanel();
		FormDesignerUtil.maximizeWidget(panel);
		tabs.add(panel,"Page1");

		//Create a DragController for each logical area where a set of draggable
		// widgets and drop targets will be allowed to interact with one another.
		FormDesignerDragController dragController = new FormDesignerDragController(panel, false,this);

		// Positioner is always constrained to the boundary panel
		// Use 'true' to also constrain the draggable or drag proxy to the boundary panel
		//dragController.setBehaviorConstrainedToBoundaryPanel(false);

		// Allow multiple widgets to be selected at once using CTRL-click
		dragController.setBehaviorMultipleSelection(true);

		dragController.setBehaviorCancelDocumentSelections(true);

		// create a DropController for each drop target on which draggable widgets
		// can be dropped
		DropController dropController =  new FormDesignerDropController(panel);

		// Don't forget to register each DropController with a DragController
		dragController.registerDropController(dropController);

		dragControllers.add(tabs.getWidgetCount()-1,dragController);
		panel.setHeight(sHeight);
		selectedDragController = dragController;
		selectedPanel = panel;

		//selectedPanel.add(rubberBand);

		//This is needed for IE
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				onWindowResized(Window.getClientWidth(), Window.getClientHeight());
			}
		});
	}

	public void onBrowserEvent(Event event) {
		switch (DOM.eventGetType(event)) {
		case Event.ONMOUSEDOWN:  
			mouseMoved = false;
			x = event.getClientX();
			y = event.getClientY();

			if( (event.getButton() & Event.BUTTON_RIGHT) != 0){

				updatePopup();

				//Account for the difference between absolute position and the
				// body's positioning context.
				//x = event.getClientX() ;//- Document.get().getBodyOffsetLeft();
				//y = event.getClientY() ;//- Document.get().getBodyOffsetTop();

				popup.setPopupPosition(event.getClientX(), event.getClientY());

				FormDesignerUtil.disableContextMenu(popup.getElement());
				popup.show();
			}
			else{
				selectionXPos = selectionYPos = -1;
				//if(!selectedDragController.isAnyWidgetSelected()){
				selectionXPos = event.getClientX() - selectedPanel.getAbsoluteLeft();
				selectionYPos = event.getClientY() - selectedPanel.getAbsoluteTop();
				//}

				if(!(event.getShiftKey() || event.getCtrlKey())){
					selectedDragController.clearSelection();
					if(event.getTarget() != this.selectedPanel.getElement()){
						if(event.getTarget().getInnerText().equals(DesignWidgetWrapper.getTabDisplayText(tabs.getTabBar().getTabHTML(tabs.getTabBar().getSelectedTab())))){
							widgetSelectionListener.onWidgetSelected(new DesignWidgetWrapper(tabs.getTabBar(),widgetPopup,this));
							return;
						}
					}
				}
				widgetSelectionListener.onWidgetSelected(null);

				startRubberBand(event);
			}
			break;
		case Event.ONMOUSEMOVE:
			mouseMoved = true;
			//FormsDesignerUtil.disableContextMenu(getElement());
			if(event.getButton() == Event.BUTTON_LEFT)
				moveRubberBand(event);
			break;
		case Event.ONMOUSEUP:

			if(selectedPanel.getWidgetCount() > 0)
				stopRubberBand(event);
			if(selectionXPos != -1 && mouseMoved)
				selectWidgets(event);
			mouseMoved = false;
			break;
		case Event.ONKEYDOWN:
			if(this.isVisible()){
				int keyCode = event.getKeyCode();
				if(keyCode == KeyboardListener.KEY_LEFT)
					moveWidgets(MOVE_LEFT);
				else if(keyCode == KeyboardListener.KEY_RIGHT)
					moveWidgets(MOVE_RIGHT);
				else if(keyCode == KeyboardListener.KEY_UP)
					moveWidgets(MOVE_UP);
				else if(keyCode == KeyboardListener.KEY_DOWN)
					moveWidgets(MOVE_DOWN);  
				else if(event.getCtrlKey() && (keyCode == 'A' || keyCode == 'a')){
					selectAll();
					DOM.eventPreventDefault(event);
				}
				else if(event.getCtrlKey() && (keyCode == 'C' || keyCode == 'c')){
					if(selectedDragController.isAnyWidgetSelected())
						copyWidgets(false);
				}
				else if(event.getCtrlKey() && (keyCode == 'X' || keyCode == 'x')){
					if(selectedDragController.isAnyWidgetSelected())
						cutWidgets();
				}
				else if(event.getCtrlKey() && (keyCode == 'V' || keyCode == 'v')){
					if(clipBoardWidgets.size() > 0 && x >= 0){
						x += selectedPanel.getAbsoluteLeft();
						y += selectedPanel.getAbsoluteTop();
						pasteWidgets();
						x = -1; //TODO prevent pasting twice as this is fired twice. Needs smarter solution
					}
				}
				else if(keyCode == KeyboardListener.KEY_DELETE){
					if(selectedDragController.isAnyWidgetSelected())
						deleteWidgets();
				}
				else if(event.getCtrlKey() && (keyCode == 'F' || keyCode == 'f')){
					format();
					DOM.eventPreventDefault(event);
				}
			}
			break;
		}
	}

	private void moveWidgets(int dirrection){
		List<Widget> widgets = selectedDragController.getSelectedWidgets();
		if(widgets == null)
			return;

		int pos;
		for(int index = 0; index < widgets.size(); index++){
			DesignWidgetWrapper widget = (DesignWidgetWrapper)widgets.get(index);

			if(dirrection == MOVE_LEFT){
				pos = FormUtil.convertDimensionToInt(widget.getLeft());
				widget.setLeft(pos-1+"px");
			}
			else if(dirrection == MOVE_RIGHT){
				pos = FormUtil.convertDimensionToInt(widget.getLeft());
				widget.setLeft(pos+1+"px");
			}
			else if(dirrection == MOVE_UP){
				pos = FormUtil.convertDimensionToInt(widget.getTop());
				widget.setTop(pos-1+"px");		
			}
			else if(dirrection == MOVE_DOWN){
				pos = FormUtil.convertDimensionToInt(widget.getTop());
				widget.setTop(pos+1+"px");
			}
		}
	}

	private void selectWidgets(Event event){
		int endX = event.getClientX() - selectedPanel.getAbsoluteLeft();
		int endY = event.getClientY() - selectedPanel.getAbsoluteTop();
		for(int i=0; i<selectedPanel.getWidgetCount(); i++){
			if(selectedPanel.getWidget(i) instanceof  DesignWidgetWrapper){
				DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedPanel.getWidget(i);
				if(widget.isWidgetInRect(this.selectionXPos, this.selectionYPos, endX, endY))
					this.selectedDragController.selectWidget(widget);
			}
		}

		if((event.getCtrlKey() || event.getShiftKey() || event.getAltKey()) && selectedDragController.getSelectedWidgetCount() == 1){
			DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedDragController.getSelectedWidgetAt(0);
			widget.setWidthInt(endX - widget.getLeftInt());
			widget.setHeightInt(endY - widget.getTopInt());
		}

		if(event.getKeyCode() == KeyboardListener.KEY_UP || event.getKeyCode() == KeyboardListener.KEY_DOWN){
			for(int index = 0; index < selectedDragController.getSelectedWidgetCount(); index++){
				DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedDragController.getSelectedWidgetAt(index);
				widget.setHeightInt(endY - widget.getTopInt());
			}
		}

		if(event.getKeyCode() == KeyboardListener.KEY_RIGHT || event.getKeyCode() == KeyboardListener.KEY_LEFT){
			for(int index = 0; index < selectedDragController.getSelectedWidgetCount(); index++){
				DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedDragController.getSelectedWidgetAt(index);
				widget.setWidthInt(endX - widget.getLeftInt());
			}
		}
	}

	private void setupPopup(){
		popup = new PopupPanel(true,true);

		MenuBar menuBar = new MenuBar(true);

		MenuBar addControlMenu = new MenuBar(true);

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("label")),true,new Command(){
			public void execute() {popup.hide(); addNewLabel(LocaleText.get("label"));}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("textBox")),true,new Command(){
			public void execute() {popup.hide(); addNewTextBox();}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("checkBox")),true,new Command(){
			public void execute() {popup.hide(); addNewCheckBox();}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("radioButton")),true,new Command(){
			public void execute() {popup.hide(); addNewRadioButton();}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("listBox")),true,new Command(){
			public void execute() {popup.hide(); addNewDropdownList();}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("textArea")),true,new Command(){
			public void execute() {popup.hide(); addTextArea();}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("button")),true,new Command(){
			public void execute() {popup.hide(); addNewButton();}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("datePicker")),true,new Command(){
			public void execute() {popup.hide(); addNewDatePicker();}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("groupBox")),true,new Command(){
			public void execute() {popup.hide(); addNewGroupBox();}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("repeatSection")),true,new Command(){
			public void execute() {popup.hide(); addNewRepeatSection();}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("picture")),true,new Command(){
			public void execute() {popup.hide(); addNewPictureSection(null);}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("videoAudio")),true,new Command(){
			public void execute() {popup.hide(); addNewVideoAudioSection(null);}});

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

		menuBar.addItem("     Add Widget",addControlMenu);

		//if(selectedDragController.isAnyWidgetSelected()){
		deleteWidgetsSeparator = menuBar.addSeparator();
		deleteWidgetsMenu = menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("deleteSelected")),true,new Command(){
			public void execute() {popup.hide(); deleteWidgets();}});
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
			public void execute() {popup.hide(); pasteWidgets();}});
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

	private void updatePopup(){
		boolean visible = false;
		if(selectedDragController.isAnyWidgetSelected())
			visible = true;
		deleteWidgetsSeparator.setVisible(visible);
		deleteWidgetsMenu.setVisible(visible);
		cutCopySeparator.setVisible(visible);
		cutMenu.setVisible(visible);
		copyMenu.setVisible(visible); 

		visible = false;
		if(clipBoardWidgets.size() > 0)
			visible = true;
		pasteSeparator.setVisible(visible);
		pasteMenu.setVisible(visible); 
	}

	private DesignWidgetWrapper addNewWidget(Widget widget){
		DesignWidgetWrapper wrapper = new DesignWidgetWrapper(widget,widgetPopup,currentWidgetSelectionListener);
		selectedDragController.makeDraggable(wrapper);
		selectedPanel.add(wrapper);
		selectedPanel.setWidgetPosition(wrapper, x-wrapper.getAbsoluteLeft(), y-wrapper.getAbsoluteTop());
		selectedDragController.clearSelection();
		selectedDragController.toggleSelection(wrapper);
		widgetSelectionListener.onWidgetSelected(wrapper);
		return wrapper;
	}

	private DesignWidgetWrapper addNewLabel(String text){
		if(text == null)
			text = "Label";
		Label label = new Label(text);

		DesignWidgetWrapper wrapper = addNewWidget(label);
		wrapper.setFontFamily(FormUtil.getDefaultFontFamily());
		return wrapper;
	}

	private DesignWidgetWrapper addNewVideoAudio(String text){
		if(text == null)
			text = "Click to play";
		Hyperlink link = new Hyperlink(text,null);

		DesignWidgetWrapper wrapper = addNewWidget(link);
		wrapper.setFontFamily(FormUtil.getDefaultFontFamily());
		return wrapper;
	}

	private DesignWidgetWrapper addNewRepeatSection(){
		DesignGroupWidget repeat = new DesignGroupWidget(images,this);
		repeat.addStyleName("getting-started-label2");
		DOM.setStyleAttribute(repeat.getElement(), "height","100px");
		DOM.setStyleAttribute(repeat.getElement(), "width","500px");
		repeat.setWidgetSelectionListener(currentWidgetSelectionListener); //TODO CHECK ????????????????

		DesignWidgetWrapper widget = addNewWidget(repeat);
		widget.setRepeated(true);

		FormDesignerDragController selDragController = selectedDragController;
		AbsolutePanel absPanel = selectedPanel;
		PopupPanel wdpopup = widgetPopup;
		WidgetSelectionListener wgSelectionListener = currentWidgetSelectionListener;

		selectedDragController = widget.getDragController();
		selectedPanel = widget.getPanel();
		widgetPopup = repeat.getWidgetPopup();
		currentWidgetSelectionListener = repeat;

		int oldY = y;
		y = 55;
		x = 10;
		if(selectedPanel.getAbsoluteLeft() > 0)
			x += selectedPanel.getAbsoluteLeft();
		if(selectedPanel.getAbsoluteTop() > 0)
			y += selectedPanel.getAbsoluteTop();

		addNewButton(LocaleText.get("addNew"),"addnew");
		x = 150;
		if(selectedPanel.getAbsoluteLeft() > 0)
			x += selectedPanel.getAbsoluteLeft();
		addNewButton(LocaleText.get("remove"),"remove");

		selectedDragController.clearSelection();

		selectedDragController = selDragController;
		selectedPanel = absPanel;
		widgetPopup = wdpopup;
		currentWidgetSelectionListener = wgSelectionListener;

		y = oldY;

		return widget;
	}

	private DesignWidgetWrapper addNewPictureSection(String parentBinding){
		DesignGroupWidget repeat = new DesignGroupWidget(images,this);
		repeat.addStyleName("getting-started-label2");
		DOM.setStyleAttribute(repeat.getElement(), "height","220px");
		DOM.setStyleAttribute(repeat.getElement(), "width","200px");
		repeat.setWidgetSelectionListener(currentWidgetSelectionListener); //TODO CHECK ????????????????

		DesignWidgetWrapper widget = addNewWidget(repeat);
		widget.setRepeated(false);

		FormDesignerDragController selDragController = selectedDragController;
		AbsolutePanel absPanel = selectedPanel;
		PopupPanel wdpopup = widgetPopup;
		WidgetSelectionListener wgSelectionListener = currentWidgetSelectionListener;

		selectedDragController = widget.getDragController();
		selectedPanel = widget.getPanel();
		widgetPopup = repeat.getWidgetPopup();
		currentWidgetSelectionListener = repeat;

		int oldY = y;

		y = 10;
		x = 10;
		if(selectedPanel.getAbsoluteLeft() > 0)
			x += selectedPanel.getAbsoluteLeft();
		if(selectedPanel.getAbsoluteTop() > 0)
			y += selectedPanel.getAbsoluteTop();
		addNewPicture().setBinding(parentBinding);

		y = 55 + 120;
		x = 10;
		if(selectedPanel.getAbsoluteLeft() > 0)
			x += selectedPanel.getAbsoluteLeft();
		if(selectedPanel.getAbsoluteTop() > 0)
			y += selectedPanel.getAbsoluteTop();

		addNewButton(LocaleText.get("browse"),"browse").setParentBinding(parentBinding);
		x = 120;
		if(selectedPanel.getAbsoluteLeft() > 0)
			x += selectedPanel.getAbsoluteLeft();
		addNewButton(LocaleText.get("clear"),"clear").setParentBinding(parentBinding);

		selectedDragController.clearSelection();

		selectedDragController = selDragController;
		selectedPanel = absPanel;
		widgetPopup = wdpopup;
		currentWidgetSelectionListener = wgSelectionListener;

		y = oldY;

		return widget;
	}

	private DesignWidgetWrapper addNewVideoAudioSection(String parentBinding){
		DesignGroupWidget repeat = new DesignGroupWidget(images,this);
		repeat.addStyleName("getting-started-label2");
		DOM.setStyleAttribute(repeat.getElement(), "height","100px");
		DOM.setStyleAttribute(repeat.getElement(), "width","200px");
		repeat.setWidgetSelectionListener(currentWidgetSelectionListener); //TODO CHECK ????????????????

		DesignWidgetWrapper widget = addNewWidget(repeat);
		widget.setRepeated(false);

		FormDesignerDragController selDragController = selectedDragController;
		AbsolutePanel absPanel = selectedPanel;
		PopupPanel wdpopup = widgetPopup;
		WidgetSelectionListener wgSelectionListener = currentWidgetSelectionListener;

		selectedDragController = widget.getDragController();
		selectedPanel = widget.getPanel();
		widgetPopup = repeat.getWidgetPopup();
		currentWidgetSelectionListener = repeat;

		int oldY = y;

		y = 20;
		x = 45;
		if(selectedPanel.getAbsoluteLeft() > 0)
			x += selectedPanel.getAbsoluteLeft();
		if(selectedPanel.getAbsoluteTop() > 0)
			y += selectedPanel.getAbsoluteTop();
		addNewVideoAudio(null).setBinding(parentBinding);

		y = 60;
		x = 10;
		if(selectedPanel.getAbsoluteLeft() > 0)
			x += selectedPanel.getAbsoluteLeft();
		if(selectedPanel.getAbsoluteTop() > 0)
			y += selectedPanel.getAbsoluteTop();

		addNewButton(LocaleText.get("browse"),"browse").setParentBinding(parentBinding);
		x = 120;
		if(selectedPanel.getAbsoluteLeft() > 0)
			x += selectedPanel.getAbsoluteLeft();
		addNewButton(LocaleText.get("clear"),"clear").setParentBinding(parentBinding);

		selectedDragController.clearSelection();

		selectedDragController = selDragController;
		selectedPanel = absPanel;
		widgetPopup = wdpopup;
		currentWidgetSelectionListener = wgSelectionListener;

		y = oldY;

		return widget;
	}



	private DesignWidgetWrapper addNewGroupBox(){
		DesignGroupWidget group = new DesignGroupWidget(images,this);
		group.addStyleName("getting-started-label2");
		DOM.setStyleAttribute(group.getElement(), "height","200px");
		DOM.setStyleAttribute(group.getElement(), "width","500px");
		group.setWidgetSelectionListener(currentWidgetSelectionListener); //TODO CHECK ??????????????

		DesignWidgetWrapper widget = addNewWidget(group);

		return widget;
	}

	private DesignWidgetWrapper addNewPicture(){
		Image image = images.picture().createImage();
		DOM.setStyleAttribute(image.getElement(), "height","155px");
		DOM.setStyleAttribute(image.getElement(), "width","185px");
		return addNewWidget(image);
	}

	private DesignWidgetWrapper addNewTextBox(){
		TextBox tb = new TextBox();
		DOM.setStyleAttribute(tb.getElement(), "height","25px");
		DOM.setStyleAttribute(tb.getElement(), "width","200px");
		return addNewWidget(tb);
	}

	private DesignWidgetWrapper addNewRepeatSet(QuestionDef questionDef, int max, String pageName){
		x = 35 + selectedPanel.getAbsoluteLeft();
		y += 25;

		Vector questions = questionDef.getRepeatQtnsDef().getQuestions();
		if(questions == null)
			return addNewTextBox(); //TODO Bug here
		for(int index = 0; index < questions.size(); index++){
			QuestionDef qtn = (QuestionDef)questions.get(index);
			if(index > 0)
				x += 210;
			DesignWidgetWrapper label = addNewLabel(qtn.getText());
			label.setBinding(qtn.getVariableName());
			label.setTextDecoration("underline");
		}

		x = 20 + selectedPanel.getAbsoluteLeft();
		y += 25;
		DesignWidgetWrapper widget = addNewRepeatSection();

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

		x += selectedPanel.getAbsoluteLeft();
		y += selectedPanel.getAbsoluteTop();

		DesignWidgetWrapper widgetWrapper = null;
		for(int index = 0; index < questions.size(); index++){
			QuestionDef qtn = (QuestionDef)questions.get(index);
			if(index > 0)
				x += 205;

			if(qtn.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || 
					qtn.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC)
				widgetWrapper = addNewDropdownList();
			else if(qtn.getDataType() == QuestionDef.QTN_TYPE_DATE)
				widgetWrapper = addNewDatePicker();
			else if(qtn.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE)
				widgetWrapper = addNewCheckBoxSet(questionDef,max,pageName);
			else if(qtn.getDataType() == QuestionDef.QTN_TYPE_BOOLEAN)
				widgetWrapper = addNewDropdownList();
			else if(qtn.getDataType() == QuestionDef.QTN_TYPE_IMAGE)
				widgetWrapper = addNewPicture();
			else if(qtn.getDataType() == QuestionDef.QTN_TYPE_VIDEO ||
					qtn.getDataType() == QuestionDef.QTN_TYPE_AUDIO)
				widgetWrapper = addNewVideoAudioSection(null);
			else
				widgetWrapper = addNewTextBox();

			widgetWrapper.setBinding(qtn.getVariableName());
			widgetWrapper.setQuestionDef(qtn);
			widgetWrapper.setTitle(qtn.getText());
			widgetWrapper.setTabIndex(index + 1);
		}

		selectedDragController.clearSelection();

		selectedDragController = selDragController;
		selectedPanel = absPanel;
		widgetPopup = wgpopup;
		currentWidgetSelectionListener = wgSelectionListener;

		y = oldY;
		y += 130; //25;

		if(questions.size() == 1)
			widget.setWidthInt(265);
		else
			widget.setWidthInt((questions.size() * 205)+15);
		return widget;
	}

	private DesignWidgetWrapper addNewDatePicker(){
		DatePicker tb = new DatePickerWidget();
		DOM.setStyleAttribute(tb.getElement(), "height","25px");
		DOM.setStyleAttribute(tb.getElement(), "width","200px");
		return addNewWidget(tb);
	}

	private DesignWidgetWrapper addNewCheckBox(){
		return addNewWidget(new CheckBox(LocaleText.get("checkBox")));
	}

	private DesignWidgetWrapper addNewRadioButton(){
		return addNewWidget(new RadioButton("RadioButton",LocaleText.get("radioButton")));
	}

	private DesignWidgetWrapper addNewDropdownList(){
		ListBox lb = new ListBox(false);
		DOM.setStyleAttribute(lb.getElement(), "height","25px");
		DOM.setStyleAttribute(lb.getElement(), "width","200px");
		DesignWidgetWrapper wrapper = addNewWidget(lb);
		return wrapper;
	}

	private DesignWidgetWrapper addTextArea(){
		TextArea ta = new TextArea();
		DOM.setStyleAttribute(ta.getElement(), "height","60px");
		DOM.setStyleAttribute(ta.getElement(), "width","200px");
		return addNewWidget(ta);
	}

	private DesignWidgetWrapper addNewButton(String label, String binding){
		DesignWidgetWrapper wrapper = addNewWidget(new Button(label));
		wrapper.setBinding(binding);
		return wrapper;
	}

	private DesignWidgetWrapper addNewButton(){
		return addNewButton(LocaleText.get("submit"),"submit");
	}

	private void addNewTab(String name){
		initPanel();
		if(name == null)
			name = LocaleText.get("page")+(tabs.getWidgetCount());

		tabs.add(selectedPanel, name);
		selectedTabIndex = tabs.getWidgetCount() - 1;
		tabs.selectTab(selectedTabIndex);

		widgetSelectionListener.onWidgetSelected(new DesignWidgetWrapper(tabs.getTabBar(),widgetPopup,this));

		DeferredCommand.addCommand(new Command() {
			public void execute() {
				onWindowResized(Window.getClientWidth(), Window.getClientHeight());
			}
		});
	}

	private void selectAll(){
		selectedDragController.clearSelection();
		for(int i=0; i<selectedPanel.getWidgetCount(); i++)
			selectedDragController.selectWidget(selectedPanel.getWidget(i));
	}

	public void onWindowResized(int width, int height){
		height -= (160+embeddedHeightOffset); //(160 + 30);
		//height = DOM.getIntStyleAttribute(getElement(), "height") - 45;
		sHeight = height+"px";
		super.setHeight(sHeight);

		for(int i=0; i<dragControllers.size(); i++)
			dragControllers.elementAt(i).getBoundaryPanel().setHeight(sHeight);
	}

	public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex){
		return true;
	}

	public void onTabSelected(SourcesTabEvents sender, int tabIndex){
		selectedTabIndex = tabIndex;

		selectedDragController = dragControllers.elementAt(selectedTabIndex);
		selectedPanel = selectedDragController.getBoundaryPanel();

		widgetSelectionListener.onWidgetSelected(new DesignWidgetWrapper(tabs.getTabBar(),widgetPopup,this));
	}

	public void setWidgetSelectionListener(WidgetSelectionListener  widgetSelectionListener){
		this.widgetSelectionListener = widgetSelectionListener;
	}

	public String getLayoutXml(){

		if(tabs.getWidgetCount() == 0)
			return null;

		com.google.gwt.xml.client.Document doc = XMLParser.createDocument();
		Element rootNode = doc.createElement("Form");
		doc.appendChild(rootNode);

		this.doc = doc;

		boolean hasWidgets = false;
		for(int i=0; i<tabs.getWidgetCount(); i++){
			Element node = doc.createElement("Page");
			node.setAttribute(WidgetEx.WIDGET_PROPERTY_TEXT, DesignWidgetWrapper.getTabDisplayText(tabs.getTabBar().getTabHTML(i)));
			//node.setAttribute("BackgroundColor", tabs.getTabBar().getTabHTML(i));
			rootNode.appendChild(node);
			AbsolutePanel panel = (AbsolutePanel)tabs.getWidget(i);
			boolean b = buildLayoutXml(node,panel,doc);
			if(b)
				hasWidgets = true;
		}

		if(hasWidgets)
			return FormDesignerUtil.formatXml(doc.toString());
		return null;
	}

	public Element getLanguageNode(){
		if(tabs.getWidgetCount() == 0)
			return null;

		com.google.gwt.xml.client.Document doc = XMLParser.createDocument();
		Element rootNode = doc.createElement("Form");
		doc.appendChild(rootNode);

		String xpath = "Form/Page/Item[@Binding='";
		for(int i=0; i<tabs.getWidgetCount(); i++){
			String text = DesignWidgetWrapper.getTabDisplayText(tabs.getTabBar().getTabHTML(i));
			Element node = doc.createElement(XformConverter.NODE_NAME_TEXT);
			node.setAttribute(XformConverter.ATTRIBUTE_NAME_XPATH, "Form/Page[@Text='"+text+"'");
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
			Element node = (Element)nodes.item(i);
			DesignWidgetWrapper widget = loadWidget(node,selectedDragController,selectedPanel,images,widgetPopup,this,currentWidgetSelectionListener,formDef); //TODO CHECK ???????????????
			if(widget != null && (widget.getWrappedWidget() instanceof DesignGroupWidget)){
				((DesignGroupWidget)widget.getWrappedWidget()).loadWidgets(node,formDef);
				((DesignGroupWidget)widget.getWrappedWidget()).setWidgetSelectionListener(currentWidgetSelectionListener); //TODO CHECK
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

		dragController.makeDraggable(wrapper);
		panel.add(wrapper);
		FormDesignerUtil.setWidgetPosition(wrapper, left, top);

		return wrapper;
	}

	/*private void setWidgetPosition(Widget w, String left, String top) {
		 com.google.gwt.user.client.Element h = w.getElement();
		 DOM.setStyleAttribute(h, "position", "absolute");
		 DOM.setStyleAttribute(h, "left", left);
		 DOM.setStyleAttribute(h, "top", top);
	 }*/

	private void cutWidgets(){
		copyWidgets(true);
	}

	private void copyWidgets(boolean remove){
		clipBoardWidgets.clear();

		for(int i=0; i<selectedDragController.getSelectedWidgetCount(); i++){
			DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedDragController.getSelectedWidgetAt(i);
			widget.storePosition();
			if(i == 0){
				clipboardLeftMostPos = FormUtil.convertDimensionToInt(widget.getLeft());;
				clipboardTopMostPos = FormUtil.convertDimensionToInt(widget.getTop());;
			}
			else{
				int dimension = FormUtil.convertDimensionToInt(widget.getLeft());
				if(clipboardLeftMostPos > dimension)
					clipboardLeftMostPos = dimension;
				dimension = FormUtil.convertDimensionToInt(widget.getTop());
				if(clipboardTopMostPos > dimension)
					clipboardTopMostPos = dimension;
			}

			if(remove) //cut
				selectedPanel.remove(widget);
			else //copy
				widget = new DesignWidgetWrapper(widget,images);
			clipBoardWidgets.add(widget);
		}
	}

	private void pasteWidgets(){
		int xOffset = x - clipboardLeftMostPos;
		int yOffset = y - clipboardTopMostPos;

		selectedDragController.clearSelection();

		for(int i=0; i<clipBoardWidgets.size(); i++){
			DesignWidgetWrapper widget = new DesignWidgetWrapper(clipBoardWidgets.get(i),images);
			String s = widget.getLeft();
			int xPos = Integer.parseInt(s.substring(0,s.length()-2)) + xOffset;
			s = widget.getTop();
			int yPos = Integer.parseInt(s.substring(0,s.length()-2)) + yOffset;
			this.selectedDragController.makeDraggable(widget);
			selectedPanel.add(widget);
			selectedPanel.setWidgetPosition(widget,xPos-widget.getAbsoluteLeft(),yPos-widget.getAbsoluteTop());
			widget.setWidth(widget.getWidth());
			widget.setHeight(widget.getHeight());
			selectedDragController.toggleSelection(widget);
			if(widget.getWrappedWidget() instanceof DesignGroupWidget)
				((DesignGroupWidget)widget.getWrappedWidget()).setWidgetPosition();
		}
	}

	private void deleteWidgets(){
		if(!Window.confirm(LocaleText.get("deleteWidgetPrompt")))
			return;

		for(int i=0; i<selectedDragController.getSelectedWidgetCount(); i++){
			DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedDragController.getSelectedWidgetAt(i);
			if(widget.getLayoutNode() != null)
				widget.getLayoutNode().getParentNode().removeChild(widget.getLayoutNode());
			selectedPanel.remove(widget);
		}

		if(doc != null){
			String layout = null;
			if(!(tabs.getTabBar().getTabCount() == 1 && (selectedPanel == null || (selectedPanel != null && selectedPanel.getWidgetCount() == 0))))
				layout = FormUtil.formatXml(doc.toString());
			layoutChangeListener.onLayoutChanged(layout);
		}

		selectedDragController.clearSelection();
	}

	private void deleteTab(){
		if(tabs.getWidgetCount() == 1){
			Window.alert(LocaleText.get("cantDeleteAllTabs"));
			return;
		}

		if(selectedPanel.getWidgetCount() > 0){
			Window.alert(LocaleText.get("deleteAllTabWidgetsFirst"));
			return;
		}

		if(!Window.confirm(LocaleText.get("deleteTabPrompt")))
			return;

		dragControllers.remove(selectedTabIndex);
		tabs.remove(selectedTabIndex);
		if(selectedTabIndex > 0)
			selectedTabIndex -= 1;
		tabs.selectTab(selectedTabIndex);
	}

	public void onWidgetSelected(DesignWidgetWrapper widget) {
		if(widget == null)
			return;

		if(!(widget.getWrappedWidget() instanceof TabBar)){
			//Event event = DOM.eventGetCurrentEvent(); //TODO verify that this does not introduce a bug
			//if(event != null && DOM.eventGetType(event) == Event.ONCONTEXTMENU){
			if(selectedDragController.getSelectedWidgetCount() == 1)
				selectedDragController.clearSelection();
			selectedDragController.selectWidget(widget);
			//}
		}

		this.widgetSelectionListener.onWidgetSelected(widget);
	}

	public void copyItem() {
		if(selectedDragController.isAnyWidgetSelected())
			copyWidgets(false);
	}

	/* (non-Javadoc)
	 * @see org.purc.purcform.client.controller.IFormActionListener#cutItem()
	 */
	public void cutItem() {
		if(selectedDragController.isAnyWidgetSelected())
			cutWidgets();
	}

	/* (non-Javadoc)
	 * @see org.purc.purcform.client.controller.IFormActionListener#pasteItem()
	 */
	public void pasteItem() {
		if(clipBoardWidgets.size() > 0){
			x = selectedPanel.getAbsoluteLeft() + 10;
			y = selectedPanel.getAbsoluteTop() + 10;
			pasteWidgets();
		}
	}

	public void deleteSelectedItem() {
		if(selectedDragController.isAnyWidgetSelected())
			this.deleteWidgets();
	}

	public void onDragEnd(Widget widget) {
		onWidgetSelected((DesignWidgetWrapper)widget);
	}

	public void onDragStart(Widget widget) {
		onWidgetSelected((DesignWidgetWrapper)widget);
	}

	public void setLayout(FormDef formDef){			
		this.formDef = formDef;
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

	private void loadQuestions(List<QuestionDef> questions, String pageName){
		int max = FormUtil.convertDimensionToInt(sHeight) - 40;
		int tabIndex = 0;
		x = y = 20;

		x += selectedPanel.getAbsoluteLeft();
		y += selectedPanel.getAbsoluteTop();

		DesignWidgetWrapper widgetWrapper = null;
		for(int i=0; i<questions.size(); i++){
			QuestionDef questionDef = (QuestionDef)questions.get(i);
			widgetWrapper = addNewLabel(questionDef.getText());
			widgetWrapper.setBinding(questionDef.getVariableName());

			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT){
				widgetWrapper.setFontWeight("bold");
				widgetWrapper.setFontStyle("italic");
			}

			widgetWrapper = null;

			x += (questionDef.getText().length() * 10);
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
					questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC)
				widgetWrapper = addNewDropdownList();
			else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_DATE)
				widgetWrapper = addNewDatePicker();
			else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE)
				widgetWrapper = addNewCheckBoxSet(questionDef,max,pageName);
			else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_BOOLEAN)
				widgetWrapper = addNewDropdownList();
			else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
				widgetWrapper = addNewRepeatSet(questionDef,max,pageName);
			else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_IMAGE)
				widgetWrapper = addNewPictureSection(questionDef.getVariableName());
			else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_VIDEO ||
					questionDef.getDataType() == QuestionDef.QTN_TYPE_AUDIO)
				widgetWrapper = addNewVideoAudioSection(questionDef.getVariableName());
			else
				widgetWrapper = addNewTextBox();

			if(widgetWrapper != null){
				if(!(questionDef.getDataType() == QuestionDef.QTN_TYPE_IMAGE||
						questionDef.getDataType() == QuestionDef.QTN_TYPE_VIDEO||
						questionDef.getDataType() == QuestionDef.QTN_TYPE_AUDIO))
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
				y += 195;
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_VIDEO || questionDef.getDataType() == QuestionDef.QTN_TYPE_AUDIO)
				y += 75;

			int rptIncr = 0;
			if(i < questions.size()-1){
				int dataType = ((QuestionDef)questions.get(i+1)).getDataType();
				if(dataType == QuestionDef.QTN_TYPE_REPEAT)
					rptIncr = 90;
				else if(dataType == QuestionDef.QTN_TYPE_IMAGE)
					rptIncr = 195;
				else if(dataType == QuestionDef.QTN_TYPE_VIDEO || dataType == QuestionDef.QTN_TYPE_AUDIO)
					rptIncr = 75;
			}

			if((y+40+rptIncr) > max){
				y += 10;
				addNewButton();
				addNewTab(pageName);
				y = 20 + selectedPanel.getAbsoluteTop();
			}
		}

		y += 10;
		addNewButton();
	}

	private void rightAlignLabels(AbsolutePanel panel){
		List<DesignWidgetWrapper> labels = new ArrayList<DesignWidgetWrapper>();
		List<DesignWidgetWrapper> inputs = new ArrayList<DesignWidgetWrapper>();
		int longestLabelWidth = 0, longestLabelLeft = 20;

		boolean usingSelection = false;
		int count = selectedDragController.getSelectedWidgetCount();
		if(count < 2)
			count = panel.getWidgetCount();
		else
			usingSelection = true;

		DesignWidgetWrapper widget = null;
		for(int index =0; index < count; index++){
			if(usingSelection)
				widget = (DesignWidgetWrapper)selectedDragController.getSelectedWidgetAt(index);
			else
				widget = (DesignWidgetWrapper)panel.getWidget(index);

			if(widget.getWrappedWidget() instanceof Button)
				continue;

			if(widget.getWrappedWidget() instanceof Label){
				if(widget.getElement().getScrollWidth() > longestLabelWidth){
					longestLabelWidth = widget.getElement().getScrollWidth();
					longestLabelLeft = FormUtil.convertDimensionToInt(widget.getLeft());
				}
				labels.add(widget);
			}
			else
				inputs.add(widget);
		}

		int relativeWidth = longestLabelWidth+longestLabelLeft;
		String left = (relativeWidth+5)+"px";
		for(int index = 0; index < inputs.size(); index++)
			inputs.get(index).setLeft(left);

		for(int index = 0; index < labels.size(); index++){
			widget = labels.get(index);
			widget.setLeft((relativeWidth - widget.getElement().getScrollWidth()+"px"));
		}
	}

	private DesignWidgetWrapper addNewCheckBoxSet(QuestionDef questionDef, int max, String pageName){
		List options = questionDef.getOptions();
		for(int i=0; i<options.size(); i++){
			if(i != 0){
				y += 40;

				if((y+40) > max){
					y += 10;
					addNewButton();
					addNewTab(pageName);
					y = 20;
				}
			}
			OptionDef optionDef = (OptionDef)options.get(i);
			DesignWidgetWrapper wrapper = addNewWidget(new CheckBox(optionDef.getText()));
			wrapper.setBinding(optionDef.getVariableName());
			wrapper.setParentBinding(questionDef.getVariableName());
		}
		return null;
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignLeft()
	 */
	public void alignLeft() {
		List<Widget> widgets = selectedDragController.getSelectedWidgets();
		if(widgets == null || widgets.size() < 2)
			return;

		//align according to the last selected item.
		String left = ((DesignWidgetWrapper)widgets.get(widgets.size() - 1)).getLeft();
		for(int index = 0; index < widgets.size(); index++)
			((DesignWidgetWrapper)widgets.get(index)).setLeft(left);
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignRight()
	 */
	public void alignRight() {
		List<Widget> widgets = selectedDragController.getSelectedWidgets();
		if(widgets == null || widgets.size() < 2)
			return;

		//align according to the last selected item.
		DesignWidgetWrapper widget = (DesignWidgetWrapper)widgets.get(widgets.size() - 1);
		int total = widget.getElement().getScrollWidth() + FormUtil.convertDimensionToInt(widget.getLeft());
		for(int index = 0; index < widgets.size(); index++){
			widget = (DesignWidgetWrapper)widgets.get(index);
			widget.setLeft((total - widget.getElement().getScrollWidth()+"px"));
		}
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignLeft()
	 */
	public void alignTop() {
		List<Widget> widgets = selectedDragController.getSelectedWidgets();
		if(widgets == null || widgets.size() < 2)
			return;

		//align according to the last selected item.
		String top = ((DesignWidgetWrapper)widgets.get(widgets.size() - 1)).getTop();
		for(int index = 0; index < widgets.size(); index++)
			((DesignWidgetWrapper)widgets.get(index)).setTop(top);
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignRight()
	 */
	public void alignBottom() {
		List<Widget> widgets = selectedDragController.getSelectedWidgets();
		if(widgets == null || widgets.size() < 2)
			return;

		//align according to the last selected item.
		DesignWidgetWrapper widget = (DesignWidgetWrapper)widgets.get(widgets.size() - 1);
		int total = widget.getElement().getScrollHeight() + FormUtil.convertDimensionToInt(widget.getTop());
		for(int index = 0; index < widgets.size(); index++){
			widget = (DesignWidgetWrapper)widgets.get(index);
			widget.setTop((total - widget.getElement().getScrollHeight()+"px"));
		}
	}

	public void makeSameHeight() {
		List<Widget> widgets = selectedDragController.getSelectedWidgets();
		if(widgets == null || widgets.size() < 2)
			return;

		//align according to the last selected item.
		String height = ((DesignWidgetWrapper)widgets.get(widgets.size() - 1)).getHeight();
		for(int index = 0; index < widgets.size(); index++)
			((DesignWidgetWrapper)widgets.get(index)).setHeight(height);
	}

	public void makeSameWidth() {
		List<Widget> widgets = selectedDragController.getSelectedWidgets();
		if(widgets == null || widgets.size() < 2)
			return;

		//align according to the last selected item.
		String width = ((DesignWidgetWrapper)widgets.get(widgets.size() - 1)).getWidth();
		for(int index = 0; index < widgets.size(); index++)
			((DesignWidgetWrapper)widgets.get(index)).setWidth(width);
	}

	public void makeSameSize() {
		List<Widget> widgets = selectedDragController.getSelectedWidgets();
		if(widgets == null || widgets.size() < 2)
			return;

		//align according to the last selected item.
		String width = ((DesignWidgetWrapper)widgets.get(widgets.size() - 1)).getWidth();
		String height = ((DesignWidgetWrapper)widgets.get(widgets.size() - 1)).getHeight();
		for(int index = 0; index < widgets.size(); index++){
			((DesignWidgetWrapper)widgets.get(index)).setWidth(width);
			((DesignWidgetWrapper)widgets.get(index)).setHeight(height);
		}
	}

	public void format(){
		if(selectedDragController.getSelectedWidgetCount() > 2)
			rightAlignLabels(selectedPanel);
	}

	public int getRubberLeft(){
		return FormUtil.convertDimensionToInt(DOM.getStyleAttribute(rubberBand.getElement(), "left"));
	}

	public int getRubberTop(){
		return FormUtil.convertDimensionToInt(DOM.getStyleAttribute(rubberBand.getElement(), "top"));
	}

	public void startRubberBand(Event event){
		selectedPanel.add(rubberBand);

		x = event.getClientX()-selectedPanel.getAbsoluteLeft();
		y = event.getClientY()-selectedPanel.getAbsoluteTop();

		DOM.setStyleAttribute(rubberBand.getElement(), "width", 0+"px");
		DOM.setStyleAttribute(rubberBand.getElement(), "height", 0+"px");
		DOM.setStyleAttribute(rubberBand.getElement(), "left", x+"px");
		DOM.setStyleAttribute(rubberBand.getElement(), "top", y+"px");
		DOM.setStyleAttribute(rubberBand.getElement(), "visibility", "visible");
	}


	public void stopRubberBand(Event event){
		selectedPanel.remove(rubberBand);
	}

	public void moveRubberBand(Event event){
		try
		{
			int width = (event.getClientX()-selectedPanel.getAbsoluteLeft())-x;
			int height = (event.getClientY()-selectedPanel.getAbsoluteTop())-y;

			if(width < 0){
				DOM.setStyleAttribute(rubberBand.getElement(), "left", event.getClientX()-selectedPanel.getAbsoluteLeft()+"px");
				DOM.setStyleAttribute(rubberBand.getElement(), "width", width * -1 + "px");
			}
			else
				DOM.setStyleAttribute(rubberBand.getElement(), "width", (event.getClientX()-selectedPanel.getAbsoluteLeft())-getRubberLeft()+"px");

			if(height < 0){
				DOM.setStyleAttribute(rubberBand.getElement(), "top", event.getClientY()-selectedPanel.getAbsoluteTop()+"px");
				DOM.setStyleAttribute(rubberBand.getElement(), "height", height * -1 + "px");
			}
			else
				DOM.setStyleAttribute(rubberBand.getElement(), "height", (event.getClientY()-selectedPanel.getAbsoluteTop())-getRubberTop()+"px");
		}
		catch(Exception ex){

		}
	}

	public void addMouseListener(MouseListener listener) {
		if (mouseListeners == null) {
			mouseListeners = new MouseListenerCollection();
		}
		mouseListeners.add(listener);
	}

	public void removeMouseListener(MouseListener listener) {
		if (mouseListeners != null) {
			mouseListeners.remove(listener);
		}
	}

	public void setFormDef(FormDef formDef){	
		if(this.formDef != formDef){
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

	public void onCopy(Widget sender) {
		selectedDragController.clearSelection();
		selectedDragController.selectWidget(sender.getParent().getParent());
		copyWidgets(false);
	}

	public void onCut(Widget sender) {
		selectedDragController.clearSelection();
		selectedDragController.selectWidget(sender.getParent().getParent());
		cutWidgets();
	}

	public void onDelete(Widget sender) {
		selectedDragController.clearSelection();
		selectedDragController.selectWidget(sender.getParent().getParent());
		deleteWidgets();
	}

	private void loadNewWidgets(){
		HashMap<String,String> bindings = new HashMap<String, String>();
		for(int i=0; i<dragControllers.size(); i++){
			AbsolutePanel panel = dragControllers.elementAt(i).getBoundaryPanel();
			fillBindings(panel,bindings);
		}

		List<QuestionDef> newQuestions = new ArrayList<QuestionDef>();
		for(int index = 0; index < formDef.getPageCount(); index++)
			fillNewQuestions(formDef.getPageAt(index),newQuestions,bindings);

		if(newQuestions.size() > 0){
			String pageName = LocaleText.get("page")+(tabs.getTabBar().getTabCount()+1);
			addNewTab(pageName);
			loadQuestions(newQuestions,pageName);
		}
	}

	private void fillBindings(AbsolutePanel panel,HashMap<String,String> bindings){
		for(int index = 0; index < panel.getWidgetCount(); index++){
			DesignWidgetWrapper widget = (DesignWidgetWrapper)panel.getWidget(index);
			String binding = widget.getParentBinding();
			if(binding == null)
				binding = widget.getBinding();
			bindings.put(binding, binding); //Could possibly put widget as value.
			if(widget.getWrappedWidget() instanceof DesignGroupWidget)
				fillBindings(((DesignGroupWidget)widget.getWrappedWidget()).getPanel(),bindings);
		}
	}

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
}
