package org.purc.purcforms.client.widget;

import org.purc.purcforms.client.PurcConstants;
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
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;


/**
 * 
 * @author daniel
 *
 */
public class DesignGroupWidget extends DesignGroupView implements DragDropListener{

	private IWidgetPopupMenuListener widgetPopupMenuListener;

	private MenuItem parentCutMenu;
	private MenuItem parentCopyMenu;
	private MenuItem parentDeleteWidgetMenu;
	private MenuItemSeparator parentMenuSeparator;

	/** The tab index. */
	private int tabIndex = 0;
	
	/** The header label. */
	private DesignWidgetWrapper headerLabel;
	

	/**
	 * 
	 * @param images
	 * @param widgetPopupMenuListener
	 */
	public DesignGroupWidget(Images images,IWidgetPopupMenuListener widgetPopupMenuListener){
		super(images);

		this.currentWidgetSelectionListener = this;

		this.widgetPopupMenuListener = widgetPopupMenuListener;

		initPanel();
		initWidget(selectedPanel);

		addStyleName("getting-started-label2");

		DOM.sinkEvents(getElement(),DOM.getEventsSunk(getElement()) | Event.MOUSEEVENTS | Event.KEYEVENTS);

		setupPopup();
		
		widgetPopup = new PopupPanel(true,true);
		MenuBar menuBar = new MenuBar(true);
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.cut(),LocaleText.get("cut")),true,new Command(){
			public void execute() {widgetPopup.hide(); cutWidgets();}});

		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.copy(),LocaleText.get("copy")),true,new Command(){
			public void execute() {widgetPopup.hide(); copyWidgets(false);}});

		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.delete(),LocaleText.get("deleteItem")),true, new Command(){
			public void execute() {widgetPopup.hide(); deleteWidgets();}});

		menuBar.addSeparator(); //LocaleText.get("??????")?????????
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.add(),LocaleText.get("changeWidgetH")),true, new Command(){
			public void execute() {widgetPopup.hide(); changeWidget(false);}});

		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.add(),LocaleText.get("changeWidgetV")),true, new Command(){
			public void execute() {widgetPopup.hide(); changeWidget(true);}});

		widgetPopup.setWidget(menuBar);

		rubberBand.addStyleName("rubberBand");
	}

	public DesignGroupWidget(DesignGroupWidget designGroupWidget, Images images,IWidgetPopupMenuListener widgetPopupMenuListener){
		this(images,widgetPopupMenuListener);

		this.currentWidgetSelectionListener = this;

		
		int labelIndex = designGroupWidget.getWidgetIndex(designGroupWidget.getHeaderLabel());
		int count = designGroupWidget.getWidgetCount();
		for(int index = 0; index < count; index++){
			DesignWidgetWrapper widget = new DesignWidgetWrapper(designGroupWidget.getWidgetAt(index),images);

			//These two new items below fix the bug which is brought by copying group widgets
			//and deleting contained widgets which do not go away.
			//DesignWidgetWrapper widget = new DesignWidgetWrapper(designGroupWidget.getWidgetAt(index).getWrappedWidget(),widgetPopup,this);
			widget.setWidgetSelectionListener(this);
			widget.setPopupPanel(widgetPopup);

			selectedPanel.add(widget);
			
			if(index != labelIndex)
				selectedDragController.makeDraggable(widget);
			else{
				DOM.setStyleAttribute(widget.getElement(), "width", "100%");
				headerLabel = widget;
			}
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
			public void execute() {popup.hide(); addNewButton(LocaleText.get("button"),null,true);}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("datePicker")),true,new Command(){
			public void execute() {popup.hide(); addNewDatePicker(true);}});
		
		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("dateTimeWidget")),true,new Command(){
			public void execute() {popup.hide(); addNewDateTimeWidget(true);}});
		
		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("timeWidget")),true,new Command(){
			public void execute() {popup.hide(); addNewTimeWidget(true);}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("groupBox")),true,new Command(){
			public void execute() {popup.hide(); addNewGroupBox(true);}});

		addControlMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("repeatSection")),true,new Command(){
			public void execute() {popup.hide(); addNewRepeatSection(true);}});

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
		
		groupWidgetsSeparator = menuBar.addSeparator();
		groupWidgetsMenu = menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("groupWidgets")),true,new Command(){
			public void execute() {popup.hide(); groupWidgets();}});

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

		lockWidgetsMenu = menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.add(),LocaleText.get("lockWidgets")),true, new Command(){
			public void execute() {popup.hide(); lockWidgets();}});

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
		Hyperlink link = new Hyperlink(text,"");

		DesignWidgetWrapper wrapper = addNewWidget(link);
		wrapper.setFontFamily(FormUtil.getDefaultFontFamily());
		wrapper.setFontSize(FormUtil.getDefaultFontSize());
		return wrapper;
	}

	private DesignWidgetWrapper addNewPicture(){
		Image image = FormUtil.createImage(images.picture());
		DOM.setStyleAttribute(image.getElement(), "height","150"+PurcConstants.UNITS);
		DOM.setStyleAttribute(image.getElement(), "width","185"+PurcConstants.UNITS);
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
			if(selectedPanel.getWidget(i) instanceof DesignWidgetWrapper){
				DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedPanel.getWidget(i);
				Element node = ((DesignWidgetWrapper)selectedPanel.getWidget(i)).buildLayoutXml(parent, doc);
				if(widget == headerLabel)
					node.setAttribute(WidgetEx.WIDGET_PROPERTY_HEADER_LABEL, "true");
			}
		}
	}

	public void buildLanguageXml(com.google.gwt.xml.client.Document doc, Element parentNode, String xpath){
		for(int i=0; i<selectedPanel.getWidgetCount(); i++){
			Widget widget = selectedPanel.getWidget(i);
			if(!(widget instanceof DesignWidgetWrapper))
				continue;
			
			//if(getHeaderLabel() == widget)
			//	xpath += WidgetEx.WIDGET_PROPERTY_HEADER_LABEL + "='true' and @";
			
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
	
	protected int getWidgetIndex(Widget child){
		return selectedPanel.getWidgetIndex(child);
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
			Element element = (Element)nodes.item(i);
			DesignWidgetWrapper widget = DesignSurfaceView.loadWidget(element,selectedDragController,selectedPanel,images,widgetPopup,this.widgetPopupMenuListener,this,formDef);
			if("true".equals(element.getAttribute(WidgetEx.WIDGET_PROPERTY_HEADER_LABEL))){
				setHeaderLabel(widget);
				widget.setBinding(((DesignWidgetWrapper)getParent().getParent()).getBinding());
			}
			
			//Load all kids if this is a DesignGroupWidget
			if(widget != null && (widget.getWrappedWidget() instanceof DesignGroupWidget)){
				((DesignGroupWidget)widget.getWrappedWidget()).loadWidgets(element,formDef);
				((DesignGroupWidget)widget.getWrappedWidget()).setWidgetSelectionListener(currentWidgetSelectionListener); //TODO CHECK
				if(!widget.isRepeated())
					selectedDragController.makeDraggable(widget, ((DesignGroupWidget)widget.getWrappedWidget()).getHeaderLabel());
			}
		}
	}

	public IWidgetPopupMenuListener getWidgetPopupMenuListener(){
		return widgetPopupMenuListener;
	}

	/**
	 * @see org.purc.purcforms.client.controller.DragDropListener#onDrop(Widget, int, int)
	 */
	public DesignWidgetWrapper onDrop(Widget widget,int x, int y){
		if(!(widget instanceof PaletteWidget))
			return null;

		DesignWidgetWrapper retWidget = super.onDrop(widget, x, y);

		String text = ((PaletteWidget)widget).getName();

		if(text.equals(LocaleText.get("picture")))
			return addNewPicture();
		else if(text.equals(LocaleText.get("videoAudio")))
			return addNewVideoAudio(null);
		
		return retWidget;
	}

	public void onWidgetSelected(DesignWidgetWrapper widget, boolean multipleSel) {
		//if(DOM.eventGetCurrentEvent().getCtrlKey())
		//	return;

		stopLabelEdit(false);

		if(!(widget.getWrappedWidget() instanceof TabBar)){
			Event event = DOM.eventGetCurrentEvent();
			if(event != null && DOM.eventGetType(event) == Event.ONCONTEXTMENU){
				if(selectedDragController.getSelectedWidgetCount() == 1)
					selectedDragController.clearSelection();
				selectedDragController.selectWidget(widget);
			}
		}

		this.widgetSelectionListener.onWidgetSelected(widget,multipleSel);
	}

	public void clearGroupBoxSelection(){
		stopLabelEdit(false);
		selectedDragController.clearSelection();
	}

	public boolean containsWidget(Widget widget){
		return selectedPanel.getWidgetIndex(widget) > -1;
	}
	
	public boolean isAnyWidgetSelected(){
		return selectedDragController.isAnyWidgetSelected();
	}
	
	/**
	 * Gets the groupbox's header label.
	 * 
	 * @return the header label.
	 */
	public DesignWidgetWrapper getHeaderLabel(){
		return headerLabel;
	}
	
	/**
	 * Sets the groupbox's header label.
	 * 
	 * @param headerLabel the header label.
	 */
	public void setHeaderLabel(DesignWidgetWrapper headerLabel){
		this.headerLabel = headerLabel;
		this.headerLabel.setPopupPanel(null);
	}
}