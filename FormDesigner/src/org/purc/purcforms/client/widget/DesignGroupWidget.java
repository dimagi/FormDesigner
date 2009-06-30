package org.purc.purcforms.client.widget;

import org.purc.purcforms.client.LeftPanel.Images;
import org.purc.purcforms.client.controller.DragDropListener;
import org.purc.purcforms.client.controller.IWidgetPopupMenuListener;
import org.purc.purcforms.client.controller.WidgetSelectionListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.view.DesignGroupView;
import org.purc.purcforms.client.view.DesignSurfaceView;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SourcesMouseEvents;
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;

public class DesignGroupWidget extends DesignGroupView implements DragDropListener,SourcesMouseEvents{

	private IWidgetPopupMenuListener widgetPopupMenuListener;

	private MenuItem parentCutMenu;
	private MenuItem parentCopyMenu;
	private MenuItem parentDeleteWidgetMenu;
	private MenuItemSeparator parentMenuSeparator;

	private int tabIndex = 0;

	public DesignGroupWidget(Images images,IWidgetPopupMenuListener widgetPopupMenuListener){
		super(images);

		this.currentWidgetSelectionListener = this;

		this.widgetPopupMenuListener = widgetPopupMenuListener;

		initPanel();
		initWidget(selectedPanel);

		addStyleName("getting-started-label2");

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
	}

	public DesignGroupWidget(DesignGroupWidget designGroupWidget, Images images,IWidgetPopupMenuListener widgetPopupMenuListener){
		this(images,widgetPopupMenuListener);

		this.currentWidgetSelectionListener = this;

		int count = designGroupWidget.getWidgetCount();
		for(int index = 0; index < count; index++){
			DesignWidgetWrapper widget = new DesignWidgetWrapper(designGroupWidget.getWidgetAt(index),images);

			//These two new items below fix the bug which is brought by copying group widgets
			//and deleting contained widgets which do not go away.
			//DesignWidgetWrapper widget = new DesignWidgetWrapper(designGroupWidget.getWidgetAt(index).getWrappedWidget(),widgetPopup,this);
			widget.setWidgetSelectionListener(this);
			widget.setPopupPanel(widgetPopup);

			selectedDragController.makeDraggable(widget);
			selectedPanel.add(widget);
		}

		widgetSelectionListener = designGroupWidget.widgetSelectionListener;
	}

	public Images getImages(){
		return images;
	}

	public void storePosition(){
		if(selectedPanel.getWidgetIndex(rubberBand) > -1)
			selectedPanel.remove(rubberBand);
		
		int count = getWidgetCount();
		for(int index = 0; index < count; index++)
			getWidgetAt(index).storePosition();
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

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("dropdownList")),true,new Command(){
			public void execute() {popup.hide(); addNewDropdownList(true);}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("textArea")),true,new Command(){
			public void execute() {popup.hide(); addNewTextArea(true);}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("button")),true,new Command(){
			public void execute() {popup.hide(); addNewButton(true);}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("datePicker")),true,new Command(){
			public void execute() {popup.hide(); addNewDatePicker(true);}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("picture")),true,new Command(){
			public void execute() {popup.hide(); addNewPicture();}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("videoAudio")),true,new Command(){
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

		menuBar.addItem("   "+LocaleText.get("addWidget"),addControlMenu);

		//if(selectedDragController.isAnyWidgetSelected()){
		deleteWidgetsSeparator = menuBar.addSeparator();
		deleteWidgetsMenu = menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("deleteSelected")),true,new Command(){
			public void execute() {popup.hide(); deleteWidgets();}});
		//}

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

		parentMenuSeparator = menuBar.addSeparator();

		final Widget widget = this;
		parentCutMenu = menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.cut(),LocaleText.get("cut")),true,new Command(){
			public void execute() {popup.hide(); widgetPopupMenuListener.onCut(widget);}});

		parentCopyMenu = menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.copy(),LocaleText.get("copy")),true,new Command(){
			public void execute() {popup.hide(); widgetPopupMenuListener.onCopy(widget);}});

		parentDeleteWidgetMenu = menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.delete(),LocaleText.get("deleteItem")),true, new Command(){
			public void execute() {popup.hide(); widgetPopupMenuListener.onDelete(widget);}});

		menuBar.addSeparator();


		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.add(),LocaleText.get("selectAll")),true, new Command(){
			public void execute() {popup.hide(); selectAll();}});

		popup.setWidget(menuBar);
	}

	protected void updatePopup(){
		super.updatePopup();

		boolean visible = false;
		if(selectedDragController.isAnyWidgetSelected())
			visible = true;

		parentCutMenu.setVisible(!visible);
		parentCopyMenu.setVisible(!visible);
		parentDeleteWidgetMenu.setVisible(!visible);
		parentMenuSeparator.setVisible(!visible);
	}

	private DesignWidgetWrapper addNewWidget(Widget widget){
		return super.addNewWidget(widget,true,this);
	}

	private DesignWidgetWrapper addNewVideoAudio(String text){
		if(text == null)
			text = LocaleText.get("clickToPlay");
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

	public void setWidgetSelectionListener(WidgetSelectionListener  widgetSelectionListener){
		this.widgetSelectionListener = widgetSelectionListener;
	}

	public int getTabIndex(){
		return tabIndex;
	}

	public void setTabIndex(int index){
		this.tabIndex = index;
	}

	public void buildLayoutXml(Element parent, com.google.gwt.xml.client.Document doc){
		for(int i=0; i<selectedPanel.getWidgetCount(); i++){
			if(selectedPanel.getWidget(i) instanceof DesignWidgetWrapper)
				((DesignWidgetWrapper)selectedPanel.getWidget(i)).buildLayoutXml(parent, doc);
		}
	}

	public void buildLanguageXml(com.google.gwt.xml.client.Document doc, Element parentNode, String xpath){
		for(int i=0; i<selectedPanel.getWidgetCount(); i++){
			Widget widget = selectedPanel.getWidget(i);
			if(!(widget instanceof DesignWidgetWrapper))
				continue;
			((DesignWidgetWrapper)widget).buildLanguageXml(doc,parentNode, xpath);
		}
	}

	public int getWidgetCount(){
		if(selectedPanel.getWidgetIndex(rubberBand) > -1)
			selectedPanel.remove(rubberBand);
		
		return selectedPanel.getWidgetCount();
	}

	public DesignWidgetWrapper getWidgetAt(int index){
		if(selectedPanel.getWidgetIndex(rubberBand) > -1)
			selectedPanel.remove(rubberBand);
		
		return (DesignWidgetWrapper)selectedPanel.getWidget(index); //TODO Could contain rubber band
	}

	public void setWidgetPosition(){
		for(int i=0; i<selectedPanel.getWidgetCount(); i++){
			if(selectedPanel.getWidget(i) instanceof DesignWidgetWrapper){
				DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedPanel.getWidget(i);
				selectedPanel.setWidgetPosition(widget,widget.getLeftInt(),widget.getTopInt());
				widget.setWidth(widget.getWidth());
				widget.setHeight(widget.getHeight());
			}
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

	public IWidgetPopupMenuListener getWidgetPopupMenuListener(){
		return widgetPopupMenuListener;
	}

	public void onDrop(Widget widget,int x, int y){
		if(!(widget instanceof PaletteWidget))
			return;

		super.onDrop(widget, x, y);

		String text = ((PaletteWidget)widget).getText();

		if(text.equals(LocaleText.get("picture")))
			addNewPicture();
		else if(text.equals(LocaleText.get("videoAudio")))
			addNewVideoAudio(null);
	}

	public void onWidgetSelected(DesignWidgetWrapper widget) {
		//if(DOM.eventGetCurrentEvent().getCtrlKey())
		//	return;

		stopLabelEdit();

		if(!(widget.getWrappedWidget() instanceof TabBar)){
			Event event = DOM.eventGetCurrentEvent();
			if(event != null && DOM.eventGetType(event) == Event.ONCONTEXTMENU){
				if(selectedDragController.getSelectedWidgetCount() == 1)
					selectedDragController.clearSelection();
				selectedDragController.selectWidget(widget);
			}
		}

		this.widgetSelectionListener.onWidgetSelected(widget);
	}

	public void clearSelection(){
		stopLabelEdit();
		selectedDragController.clearSelection();
	}

	public boolean containsWidget(Widget widget){
		return selectedPanel.getWidgetIndex(widget) > -1;
	}
	
	public boolean isAnyWidgetSelected(){
		return selectedDragController.isAnyWidgetSelected();
	}
}

//public void onBrowserEvent(Event event) {
//switch (DOM.eventGetType(event)) {
//case Event.ONMOUSEDOWN:  
//	mouseMoved = false;
//	x = event.getClientX();
//	y = event.getClientY();
//
//	if( (event.getButton() & Event.BUTTON_RIGHT) != 0){
//
//		updatePopup();
//
//		//Account for the difference between absolute position and the
//		// body's positioning context.
//		//x = event.getClientX() ;//- Document.get().getBodyOffsetLeft();
//		//y = event.getClientY() ;//- Document.get().getBodyOffsetTop();
//
//		popup.setPopupPosition(event.getClientX(), event.getClientY());
//
//		FormDesignerUtil.disableContextMenu(popup.getElement());
//		popup.show();
//	}
//	else{
//		selectionXPos = selectionYPos = -1;
//		//if(!selectedDragController.isAnyWidgetSelected()){
//		selectionXPos = event.getClientX() - selectedPanel.getAbsoluteLeft();
//		selectionYPos = event.getClientY() - selectedPanel.getAbsoluteTop();
//		//}
//
//		if(!(event.getShiftKey() || event.getCtrlKey())){
//			selectedDragController.clearSelection();
//			if(event.getTarget() != this.selectedPanel.getElement()){
//				/*if(event.getTarget().getInnerText().equals(tabs.getTabBar().getTabHTML(tabs.getTabBar().getSelectedTab()))){
//    					  widgetSelectionListener.onWidgetSelected(new DesignWidgetWrapper(tabs.getTabBar(),widgetPopup,this));
//    					  return;
//    				  }*/
//			}
//		}
//		widgetSelectionListener.onWidgetSelected(null);
//	}
//	break;
//case Event.ONMOUSEMOVE:
//	mouseMoved = true;
//	break;
//case Event.ONMOUSEUP:
//
//	if(selectionXPos != -1 && mouseMoved)
//		selectWidgets(event.getClientX() - selectedPanel.getAbsoluteLeft(),
//				event.getClientY() - selectedPanel.getAbsoluteTop());
//	mouseMoved = false;
//	break;
//case Event.ONKEYDOWN:
//	if(this.isVisible()){
//		int keyCode = event.getKeyCode();
//		if(keyCode == KeyboardListener.KEY_LEFT)
//			moveWidgets(MOVE_LEFT);
//		else if(keyCode == KeyboardListener.KEY_RIGHT)
//			moveWidgets(MOVE_RIGHT);
//		else if(keyCode == KeyboardListener.KEY_UP)
//			moveWidgets(MOVE_UP);
//		else if(keyCode == KeyboardListener.KEY_DOWN)
//			moveWidgets(MOVE_DOWN);  
//		else if(event.getCtrlKey() && (keyCode == 'A' || keyCode == 'a')){
//			selectAll();
//			DOM.eventPreventDefault(event);
//		}
//		else if(event.getCtrlKey() && (keyCode == 'C' || keyCode == 'c')){
//			if(selectedDragController.isAnyWidgetSelected())
//				copyWidgets(false);
//		}
//		else if(event.getCtrlKey() && (keyCode == 'X' || keyCode == 'x')){
//			if(selectedDragController.isAnyWidgetSelected())
//				cutWidgets();
//		}
//		else if(event.getCtrlKey() && (keyCode == 'V' || keyCode == 'v')){
//			if(Context.clipBoardWidgets.size() > 0 && x >= 0){
//				x += selectedPanel.getAbsoluteLeft();
//				y += selectedPanel.getAbsoluteTop();
//				pasteWidgets();
//				x = -1; //TODO prevent pasting twice as this is fired twice. Needs smarter solution
//			}
//		}
//		else if(keyCode == KeyboardListener.KEY_DELETE){
//			if(selectedDragController.isAnyWidgetSelected())
//				deleteWidgets();
//		}
//		else if(event.getCtrlKey() && (keyCode == 'F' || keyCode == 'f')){
//			format();
//			DOM.eventPreventDefault(event);
//		}
//	}
//	break;
//}
//}
