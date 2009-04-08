package org.purc.purcforms.client.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.purc.purcforms.client.LeftPanel.Images;
import org.purc.purcforms.client.controller.DragDropListener;
import org.purc.purcforms.client.controller.FormDesignerDragController;
import org.purc.purcforms.client.controller.FormDesignerDropController;
import org.purc.purcforms.client.controller.IWidgetPopupMenuListener;
import org.purc.purcforms.client.controller.WidgetSelectionListener;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.OptionDef;
import org.purc.purcforms.client.model.PageDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.view.DesignSurfaceView;
import org.zenika.widget.client.datePicker.DatePicker;

import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
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
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;

public class DesignGroupWidget extends Composite implements WidgetSelectionListener,DragDropListener,SourcesMouseEvents{

	private MouseListenerCollection mouseListeners;
	private static final int MOVE_LEFT = 1;
	private static final int MOVE_RIGHT = 2;
	private static final int MOVE_UP = 3;
	private static final int MOVE_DOWN = 4;

	//private DecoratedTabPanel tabs = new DecoratedTabPanel();
	private PopupPanel popup;
	private PopupPanel widgetPopup;
	private IWidgetPopupMenuListener widgetPopupMenuListener;
	private final Images images;
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

	private MenuItem parentCutMenu;
	private MenuItem parentCopyMenu;
	private MenuItem parentDeleteWidgetMenu;
	private MenuItemSeparator parentMenuSeparator;

	private int selectionXPos;
	private int selectionYPos;
	private boolean mouseMoved = false;

	private int tabIndex = 0;

	public DesignGroupWidget(Images images,IWidgetPopupMenuListener widgetPopupMenuListener){
		this.images = images;
		this.widgetPopupMenuListener = widgetPopupMenuListener;

		//FormsDesignerUtil.maximizeWidget(tabs);
		initPanel();
		initWidget(selectedPanel);

		addStyleName("getting-started-label2");

		DOM.sinkEvents(getElement(),DOM.getEventsSunk(getElement()) | Event.MOUSEEVENTS | Event.KEYEVENTS);

		widgetPopup = new PopupPanel(true,true);
		MenuBar menuBar = new MenuBar(true);
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.cut(),"Cut"),true,new Command(){
			public void execute() {widgetPopup.hide(); cutWidgets();}});

		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.copy(),"Copy"),true,new Command(){
			public void execute() {widgetPopup.hide(); copyWidgets(false);}});

		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.delete(),"Delete"),true, new Command(){
			public void execute() {widgetPopup.hide(); deleteWidgets();}});

		widgetPopup.setWidget(menuBar);

		setupPopup();
	}

	public DesignGroupWidget(DesignGroupWidget designGroupWidget, Images images,IWidgetPopupMenuListener widgetPopupMenuListener){
		this(images,widgetPopupMenuListener);

		int count = designGroupWidget.getWidgetCount();
		for(int index = 0; index < count; index++){
			DesignWidgetWrapper widget = new DesignWidgetWrapper(designGroupWidget.getWidgetAt(index),images);
			selectedDragController.makeDraggable(widget);
			selectedPanel.add(widget);
		}

		widgetSelectionListener = designGroupWidget.widgetSelectionListener;
	}

	public Images getImages(){
		return images;
	}

	public void storePosition(){
		int count = getWidgetCount();
		for(int index = 0; index < count; index++)
			getWidgetAt(index).storePosition();
	}

	private void initPanel(){	    
		//Create a DragController for each logical area where a set of draggable
		// widgets and drop targets will be allowed to interact with one another.
		selectedDragController = new FormDesignerDragController(selectedPanel, false,this);

		// Positioner is always constrained to the boundary panel
		// Use 'true' to also constrain the draggable or drag proxy to the boundary panel
		//dragController.setBehaviorConstrainedToBoundaryPanel(false);

		// Allow multiple widgets to be selected at once using CTRL-click
		selectedDragController.setBehaviorMultipleSelection(true);

		selectedDragController.setBehaviorCancelDocumentSelections(true);

		// create a DropController for each drop target on which draggable widgets
		// can be dropped
		DropController dropController =  new FormDesignerDropController(selectedPanel);

		// Don't forget to register each DropController with a DragController
		selectedDragController.registerDropController(dropController);
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
						/*if(event.getTarget().getInnerText().equals(tabs.getTabBar().getTabHTML(tabs.getTabBar().getSelectedTab()))){
		    					  widgetSelectionListener.onWidgetSelected(new DesignWidgetWrapper(tabs.getTabBar(),widgetPopup,this));
		    					  return;
		    				  }*/
					}
				}
				widgetSelectionListener.onWidgetSelected(null);
			}
			break;
		case Event.ONMOUSEMOVE:
			mouseMoved = true;
			break;
		case Event.ONMOUSEUP:

			if(selectionXPos != -1 && mouseMoved)
				selectWidgets(event.getClientX() - selectedPanel.getAbsoluteLeft(),
						event.getClientY() - selectedPanel.getAbsoluteTop());
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

	private void selectWidgets(int endX, int endY){
		for(int i=0; i<selectedPanel.getWidgetCount(); i++){
			DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedPanel.getWidget(i);
			if(widget.isWidgetInRect(this.selectionXPos, this.selectionYPos, endX, endY))
				this.selectedDragController.selectWidget(widget);
		}
	}

	private void setupPopup(){
		popup = new PopupPanel(true,true);

		MenuBar menuBar = new MenuBar(true);

		MenuBar addControlMenu = new MenuBar(true);

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),"Label"),true,new Command(){
			public void execute() {popup.hide(); addNewLabel("Label");}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),"TextBox"),true,new Command(){
			public void execute() {popup.hide(); addNewTextBox();}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),"CheckBox"),true,new Command(){
			public void execute() {popup.hide(); addNewCheckBox();}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),"RadioButton"),true,new Command(){
			public void execute() {popup.hide(); addNewRadioButton();}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),"DropdownList"),true,new Command(){
			public void execute() {popup.hide(); addNewDropdownList();}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),"TextArea"),true,new Command(){
			public void execute() {popup.hide(); addTextArea();}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),"Button"),true,new Command(){
			public void execute() {popup.hide(); addNewButton();}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),"Date Picker"),true,new Command(){
			public void execute() {popup.hide(); addNewDatePicker();}});
		
		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),"Picture"),true,new Command(){
			public void execute() {popup.hide(); addNewPicture();}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),"Video/Audio"),true,new Command(){
			public void execute() {popup.hide(); addNewVideoAudio(null);}});

		/*addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),"Group Box"),true,new Command(){
		    	public void execute() {popup.hide(); addNewButton();}});

		  addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),"Auto Complete TextBox"),true,new Command(){
		    	public void execute() {popup.hide(); addNewTextBox();}});

		  /*addControlMenu.addItem(FormsDesignerUtil.createHeaderHTML(images.addchild(),"Repeat Section"),true,new Command(){
		    	public void execute() {popup.hide(); addNewRepeatSection();}});

		  addControlMenu.addSeparator();
		  addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),"Time Picker"),true,new Command(){
		    	public void execute() {popup.hide(); ;}});

		  addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),"Date & Time Picker"),true,new Command(){
		    	public void execute() {popup.hide(); ;}});

		  addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),"Image"),true,new Command(){
		    	public void execute() {popup.hide(); ;}});

		  addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),"Attachment"),true,new Command(){
		    	public void execute() {popup.hide(); ;}});*/

		menuBar.addItem("   AddControl",addControlMenu);

		//if(selectedDragController.isAnyWidgetSelected()){
		deleteWidgetsSeparator = menuBar.addSeparator();
		deleteWidgetsMenu = menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),"Delete Selected Widgets"),true,new Command(){
			public void execute() {popup.hide(); deleteWidgets();}});
		//}

		//if(selectedDragController.isAnyWidgetSelected()){
		cutCopySeparator = menuBar.addSeparator();
		cutMenu = menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.cut(),"Cut"),true,new Command(){
			public void execute() {popup.hide(); cutWidgets();}});

		copyMenu = menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.copy(),"Copy"),true,new Command(){
			public void execute() {popup.hide(); copyWidgets(false);}});
		//}
		//else if(clipBoardWidgets.size() > 0){
		pasteSeparator = menuBar.addSeparator();
		pasteMenu = menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.paste(),"Paste"),true,new Command(){
			public void execute() {popup.hide(); pasteWidgets();}});
		//}

		parentMenuSeparator = menuBar.addSeparator();

		final Widget widget = this;
		parentCutMenu = menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.cut(),"Cut"),true,new Command(){
			public void execute() {popup.hide(); widgetPopupMenuListener.onCut(widget);}});

		parentCopyMenu = menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.copy(),"Copy"),true,new Command(){
			public void execute() {popup.hide(); widgetPopupMenuListener.onCopy(widget);}});

		parentDeleteWidgetMenu = menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.delete(),"Delete"),true, new Command(){
			public void execute() {popup.hide(); widgetPopupMenuListener.onDelete(widget);}});

		menuBar.addSeparator();


		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.add(),"Select All"),true, new Command(){
			public void execute() {popup.hide(); selectAll();}});

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

		parentCutMenu.setVisible(!visible);
		parentCopyMenu.setVisible(!visible);
		parentDeleteWidgetMenu.setVisible(!visible);
		parentMenuSeparator.setVisible(!visible);

		visible = false;
		if(clipBoardWidgets.size() > 0)
			visible = true;
		pasteSeparator.setVisible(visible);
		pasteMenu.setVisible(visible); 
	}

	private DesignWidgetWrapper addNewWidget(Widget widget){
		DesignWidgetWrapper wrapper = new DesignWidgetWrapper(widget,widgetPopup,this);
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
		return addNewWidget(new Label(text));
	}

	private DesignWidgetWrapper addNewVideoAudio(String text){
		if(text == null)
			text = "Click to play";
		Hyperlink link = new Hyperlink(text,null);
		
		DesignWidgetWrapper wrapper = addNewWidget(link);
		wrapper.setFontFamily(FormUtil.getDefaultFontFamily());
		return wrapper;
	}
	
	private DesignWidgetWrapper addNewPicture(){
		Image image = images.picture().createImage();
		DOM.setStyleAttribute(image.getElement(), "height","150px");
		DOM.setStyleAttribute(image.getElement(), "width","185px");
		return addNewWidget(image);
	}

	//TODO ?????????????//
	/*private DesignWidgetWrapper addNewRepeatSection(){
		 DesignSurfaceView repeat = new DesignSurfaceView(images);
		 repeat.addStyleName("getting-started-label");
		 DOM.setStyleAttribute(repeat.getElement(), "height","50px");
		 DOM.setStyleAttribute(repeat.getElement(), "width","500px");
		 return addNewWidget(repeat);
	 }*/

	private DesignWidgetWrapper addNewTextBox(){
		TextBox tb = new TextBox();
		DOM.setStyleAttribute(tb.getElement(), "height","25px");
		DOM.setStyleAttribute(tb.getElement(), "width","200px");
		return addNewWidget(tb);
	}

	private DesignWidgetWrapper addNewDatePicker(){
		DatePicker tb = new DatePickerWidget();
		DOM.setStyleAttribute(tb.getElement(), "height","25px");
		DOM.setStyleAttribute(tb.getElement(), "width","200px");
		return addNewWidget(tb);
	}

	private DesignWidgetWrapper addNewCheckBox(){
		return addNewWidget(new CheckBox("CheckBox"));
	}

	private DesignWidgetWrapper addNewRadioButton(){
		return addNewWidget(new RadioButton("RadioButton","RadioButton"));
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

	private DesignWidgetWrapper addNewButton(){
		DesignWidgetWrapper wrapper = addNewWidget(new Button("Submit"));
		wrapper.setBinding("submit");
		return wrapper;
	}

	private void selectAll(){
		selectedDragController.clearSelection();
		for(int i=0; i<selectedPanel.getWidgetCount(); i++)
			selectedDragController.selectWidget(selectedPanel.getWidget(i));
	}

	public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex){
		return true;
	}

	public void setWidgetSelectionListener(WidgetSelectionListener  widgetSelectionListener){
		this.widgetSelectionListener = widgetSelectionListener;
	}

	public String getLayoutXml(){

		/*if(tabs.getWidgetCount() == 0)
			 return null;

		 com.google.gwt.xml.client.Document doc = XMLParser.createDocument();
		 Element rootNode = doc.createElement("Form");
		 doc.appendChild(rootNode);

		 boolean hasWidgets = false;
		 for(int i=0; i<tabs.getWidgetCount(); i++){
			 Element node = doc.createElement("Page");
			 node.setAttribute("Text", tabs.getTabBar().getTabHTML(i));
			 //node.setAttribute("BackgroundColor", tabs.getTabBar().getTabHTML(i));
			 rootNode.appendChild(node);
			 AbsolutePanel panel = (AbsolutePanel)tabs.getWidget(i);
			 boolean b = buildLayoutXml(node,panel,doc);
			 if(b)
				 hasWidgets = true;
		 }

		 if(hasWidgets)
			 return FormsDesignerUtil.formatXml(doc.toString());*/
		return null;
	}

	/*private boolean buildLayoutXml(Element parent, AbsolutePanel panel, com.google.gwt.xml.client.Document doc){
		 for(int i=0; i<panel.getWidgetCount(); i++){
			 DesignWidgetWrapper wrapper = (DesignWidgetWrapper)panel.getWidget(i);
			 Element node = doc.createElement("Item");
			 parent.appendChild(node);			 
			 node.setAttribute("WidgetType", wrapper.getWidgetName());

			 String value = wrapper.getText();
			 if(value != null && value.trim().length() > 0)
				 node.setAttribute("Text", value);
			 else
				 node.removeAttribute("Text");

			 value = wrapper.getTitle();
			 if(value != null && value.trim().length() > 0)
				 node.setAttribute("HelpText", value);
			 else
				 node.removeAttribute("HelpText");

			 value = wrapper.getBinding();
			 if(value != null && value.trim().length() > 0)
				 node.setAttribute("Binding", value);
			 else
				 node.removeAttribute("Binding");

			 value = wrapper.getParentBinding();
			 if(value != null && value.trim().length() > 0)
				 node.setAttribute("ParentBinding", value);
			 else
				 node.removeAttribute("ParentBinding");

			 node.setAttribute("Left", wrapper.getLeft());
			 node.setAttribute("Top", wrapper.getTop());

			 value = wrapper.getWidth();
			 if(value != null && value.trim().length() > 0)
				 node.setAttribute("Width", value);
			 else
				 node.removeAttribute("Width");

			 value = wrapper.getHeight();
			 if(value != null && value.trim().length() > 0)
				 node.setAttribute("Height", value);
			 else
				 node.removeAttribute("Height");

			 node.setAttribute("TabIndex", String.valueOf(wrapper.getTabIndex()));
		 }

		 return panel.getWidgetCount() > 0;
	 }*/

	public void setLayoutXml(String xml){
		/*tabs.clear();

		 com.google.gwt.xml.client.Document doc = XMLParser.parse(xml);
		 Element root = doc.getDocumentElement();
		 NodeList pages = root.getChildNodes();
		 for(int i=0; i<pages.getLength(); i++){
			 if(pages.item(i).getNodeType() != Node.ELEMENT_NODE)
				 continue;
			 Element node = (Element)pages.item(i);
			 addNewTab(node.getAttribute("Text"));
			 loadPage(node.getChildNodes());
		 }*/
	}

	/*private void loadPage(NodeList nodes){
		 for(int i=0; i<nodes.getLength(); i++){
			 if(nodes.item(i).getNodeType() != Node.ELEMENT_NODE)
				 continue;

			 Element node = (Element)nodes.item(i);
			 String left = node.getAttribute("Left");
			 String top = node.getAttribute("Top");
			 String s = node.getAttribute("WidgetType");

			 Widget widget = null;
			 if(s.equalsIgnoreCase("RadioButton"))
				 widget = new RadioButton(node.getAttribute("ParentBinding"),node.getAttribute("Text"));
			 else if(s.equalsIgnoreCase("CheckBox"))
				 widget = new CheckBox(node.getAttribute("Text"));
			 else if(s.equalsIgnoreCase("Button"))
			     widget = new Button(node.getAttribute("Text"));
			 else if(s.equalsIgnoreCase("ListBox"))
				 widget = new ListBox(false);
			 else if(s.equalsIgnoreCase("TextArea"))
				 widget = new TextArea();
			 else if(s.equalsIgnoreCase("DatePicker"))
				 widget = new DatePickerWidget();
			 else if(s.equalsIgnoreCase("TextBox"))
				 widget = new TextBox();
			 else if(s.equalsIgnoreCase("Label"))
				 widget = new Label(node.getAttribute("Text"));
			 else
				 continue; 

			 DesignWidgetWrapper wrapper = new DesignWidgetWrapper(widget,widgetPopup,this);

			 String value = node.getAttribute("HelpText");
			 if(value != null && value.trim().length() > 0)
				 wrapper.setTitle(value);

			 value = node.getAttribute("Width");
			 if(value != null && value.trim().length() > 0)
				 wrapper.setWidth(value);

			 value = node.getAttribute("Height");
			 if(value != null && value.trim().length() > 0)
				 wrapper.setHeight(value);

			 value = node.getAttribute("Binding");
			 if(value != null && value.trim().length() > 0)
				 wrapper.setBinding(value);

			 value = node.getAttribute("ParentBinding");
			 if(value != null && value.trim().length() > 0)
				 wrapper.setParentBinding(value);

			 value = node.getAttribute("TabIndex");
			 if(value != null && value.trim().length() > 0)
				 wrapper.setTabIndex(Integer.parseInt(value));

			 selectedDragController.makeDraggable(wrapper);
			 selectedPanel.add(wrapper);
			 FormDesignerUtil.setWidgetPosition(wrapper, left, top);

		 }
	 }*/

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
			selectedDragController.toggleSelection(widget);
		}
	}

	private void deleteWidgets(){
		if(!Window.confirm("Do you really want to delete the selected widget(s)?"))
			return;

		for(int i=0; i<selectedDragController.getSelectedWidgetCount(); i++)
			selectedPanel.remove(selectedDragController.getSelectedWidgetAt(i));

		selectedDragController.clearSelection();
	}

	public void onWidgetSelected(DesignWidgetWrapper widget) {
		if(!(widget.getWrappedWidget() instanceof TabBar)){
			//Event event = DOM.eventGetCurrentEvent();
			//if(DOM.eventGetType(event) == Event.ONCONTEXTMENU){ //TODO verify that this does not introduce a bug
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

	private void loadPage(PageDef pageDef){

		int max = FormUtil.convertDimensionToInt(sHeight) - 40;
		int pageParts = 0, tabIndex = 0;
		Vector questions  = pageDef.getQuestions();
		x = y = 20;
		DesignWidgetWrapper widgetWrapper = null;
		for(int i=0; i<questions.size(); i++){
			QuestionDef questionDef = (QuestionDef)questions.get(i);
			widgetWrapper = addNewLabel(questionDef.getText());

			widgetWrapper = null;

			x += (questionDef.getText().length() * 10);
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE)
				widgetWrapper = addNewDropdownList();
			else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_DATE)
				widgetWrapper = addNewDatePicker();
			else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE)
				widgetWrapper = addNewCheckBoxSet(questionDef);
			else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_BOOLEAN)
				widgetWrapper = addNewDropdownList();
			else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_IMAGE)
				widgetWrapper = addNewPicture();
			else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_VIDEO ||
					questionDef.getDataType() == QuestionDef.QTN_TYPE_AUDIO)
				widgetWrapper = addNewVideoAudio(null);
			else
				widgetWrapper = addNewTextBox();

			if(widgetWrapper != null){
				widgetWrapper.setBinding(questionDef.getVariableName());
				widgetWrapper.setTabIndex(++tabIndex);
			}

			x = 20;
			y += 40;

			if((y+40) > max){
				y += 10;
				addNewButton();
				//addNewTab(pageDef.getName()+(++pageParts));
				y = 20;
			}
		}

		y += 10;
		addNewButton();
	}

	private void rightAlignLabels(AbsolutePanel panel){
		List<DesignWidgetWrapper> labels = new ArrayList<DesignWidgetWrapper>();
		List<DesignWidgetWrapper> inputs = new ArrayList<DesignWidgetWrapper>();
		int longestLabelWidth = 0, longestLabelLeft = 20;

		for(int index =0; index < panel.getWidgetCount(); index++){
			DesignWidgetWrapper widget = (DesignWidgetWrapper)panel.getWidget(index);
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

		DesignWidgetWrapper widget = null;
		for(int index = 0; index < labels.size(); index++){
			widget = labels.get(index);
			widget.setLeft((relativeWidth - widget.getElement().getScrollWidth()+"px"));
		}
	}

	private DesignWidgetWrapper addNewCheckBoxSet(QuestionDef questionDef){
		Vector options = questionDef.getOptions();
		for(int i=0; i<options.size(); i++){
			if(i != 0)
				y += 40;
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

	public void format(){
		rightAlignLabels(selectedPanel);
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

	public int getTabIndex(){
		return tabIndex;
	}

	public void setTabIndex(int index){
		this.tabIndex = index;
	}

	public void buildLayoutXml(Element parent, com.google.gwt.xml.client.Document doc){
		for(int i=0; i<selectedPanel.getWidgetCount(); i++)
			((DesignWidgetWrapper)selectedPanel.getWidget(i)).buildLayoutXml(parent, doc);
	}

	public int getWidgetCount(){
		return selectedPanel.getWidgetCount();
	}

	public DesignWidgetWrapper getWidgetAt(int index){
		return (DesignWidgetWrapper)selectedPanel.getWidget(index);
	}

	public void setWidgetPosition(){
		for(int i=0; i<selectedPanel.getWidgetCount(); i++){
			DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedPanel.getWidget(i);
			selectedPanel.setWidgetPosition(widget,widget.getLeftInt(),widget.getTopInt());
			widget.setWidth(widget.getWidth());
			widget.setHeight(widget.getHeight());
		}
	}

	public void loadWidgets(Element node,FormDef formDef){
		NodeList nodes = node.getChildNodes();
		for(int i=0; i<nodes.getLength(); i++){
			if(nodes.item(i).getNodeType() != Node.ELEMENT_NODE)
				continue;
			DesignSurfaceView.loadWidget((Element)nodes.item(i),selectedDragController,selectedPanel,images,widgetPopup,this.widgetPopupMenuListener,this,formDef);
		}
	}

	public FormDesignerDragController getDragController(){
		return this.selectedDragController;
	}

	public AbsolutePanel getPanel(){
		return this.selectedPanel;
	}

	public PopupPanel getWidgetPopup(){
		return widgetPopup;
	}

	public IWidgetPopupMenuListener getWidgetPopupMenuListener(){
		return widgetPopupMenuListener;
	}
}
