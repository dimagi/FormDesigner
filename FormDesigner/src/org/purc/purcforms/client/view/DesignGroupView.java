package org.purc.purcforms.client.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.purc.purcforms.client.Context;
import org.purc.purcforms.client.LeftPanel.Images;
import org.purc.purcforms.client.controller.DragDropListener;
import org.purc.purcforms.client.controller.FormDesignerDragController;
import org.purc.purcforms.client.controller.FormDesignerDropController;
import org.purc.purcforms.client.controller.IWidgetPopupMenuListener;
import org.purc.purcforms.client.controller.WidgetSelectionListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.widget.DatePickerWidget;
import org.purc.purcforms.client.widget.DesignGroupWidget;
import org.purc.purcforms.client.widget.DesignWidgetWrapper;
import org.purc.purcforms.client.widget.PaletteWidget;
import org.zenika.widget.client.datePicker.DatePicker;

import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
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
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.MouseListenerCollection;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author daniel
 *
 */
public class DesignGroupView extends Composite implements WidgetSelectionListener,IWidgetPopupMenuListener,DragDropListener{

	private MouseListenerCollection mouseListeners;
	protected static final int MOVE_LEFT = 1;
	protected static final int MOVE_RIGHT = 2;
	protected static final int MOVE_UP = 3;
	protected static final int MOVE_DOWN = 4;

	protected PopupPanel popup;
	protected PopupPanel widgetPopup;

	protected int x;
	protected int y;

	protected int clipboardLeftMostPos;
	protected int clipboardTopMostPos;
	protected MenuItem copyMenu;
	protected MenuItem cutMenu;
	protected MenuItem pasteMenu;
	protected MenuItem deleteWidgetsMenu;
	protected MenuItem groupWidgetsMenu;
	protected MenuItemSeparator cutCopySeparator;
	protected MenuItemSeparator pasteSeparator;
	protected MenuItemSeparator deleteWidgetsSeparator;
	protected MenuItemSeparator groupWidgetsSeparator;

	protected int selectionXPos;
	protected int selectionYPos;
	protected boolean mouseMoved = false;

	protected AbsolutePanel selectedPanel = new AbsolutePanel();
	protected FormDesignerDragController selectedDragController;
	protected WidgetSelectionListener widgetSelectionListener;

	protected Label rubberBand = new Label(""); //HTML("<DIV ID='rubberBand'></DIV>");

	protected final Images images;

	//These three do not belong here (should be only for DesignSurfaceView)
	protected DecoratedTabPanel tabs = new DecoratedTabPanel();
	protected HashMap<Integer,DesignWidgetWrapper> pageWidgets = new HashMap<Integer,DesignWidgetWrapper>();
	protected int selectedTabIndex = 0;

	protected WidgetSelectionListener currentWidgetSelectionListener;

	private TextBox txtEdit = new TextBox();
	protected DesignWidgetWrapper editWidget;
	protected String rubberBandHeight;
	protected String rubberBandWidth;

	public DesignGroupView(Images images){
		this.images = images;
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

	public void onDragEnd(Widget widget) {
		onWidgetSelected(getSelectedWidget((DesignWidgetWrapper)widget));

		((DesignWidgetWrapper)widget).refreshSize();
		if(((DesignWidgetWrapper)widget).getWrappedWidget() instanceof DesignGroupWidget)
			DOM.setStyleAttribute(((DesignWidgetWrapper)widget).getWrappedWidget().getElement(), "cursor", "default");

		//if(((DesignWidgetWrapper)widget).getWrappedWidget() instanceof DesignGroupWidget)
		//	((DesignGroupWidget)((DesignWidgetWrapper)widget).getWrappedWidget()).getHeaderLabel().refreshSize();
	}

	public void onDragStart(Widget widget) {
		onWidgetSelected(getSelectedWidget((DesignWidgetWrapper)widget));
	}

	private DesignWidgetWrapper getSelectedWidget(DesignWidgetWrapper widget){
		if(widget.getWrappedWidget() instanceof DesignGroupWidget && !widget.isRepeated()){
			String cursor = DOM.getStyleAttribute(widget.getWrappedWidget().getElement(), "cursor");
			if("move".equals(cursor) || "default".equals(cursor))
				return ((DesignGroupWidget)widget.getWrappedWidget()).getHeaderLabel();
		}
		return widget;
	}

	public void onWidgetSelected(Widget widget){
		this.widgetSelectionListener.onWidgetSelected(widget);
	}

	protected void cutWidgets(){
		copyWidgets(true);
	}

	protected void groupWidgets(){
		for(int i=0; i<selectedDragController.getSelectedWidgetCount(); i++){
			if(((DesignWidgetWrapper)selectedDragController.getSelectedWidgetAt(i)).getWrappedWidget() instanceof DesignGroupWidget)
				return; //We do not allow nested group boxes
		}

		cutWidgets();

		x = clipboardLeftMostPos + selectedPanel.getAbsoluteLeft() ;
		y = clipboardTopMostPos + selectedPanel.getAbsoluteTop();

		DesignWidgetWrapper widget = ((DesignSurfaceView)this).addNewGroupBox(false);
		DesignGroupView designGroupView = (DesignGroupView)widget.getWrappedWidget();
		designGroupView.updateCursorPos(x+20, y+45);
		designGroupView.pasteWidgets(true);
		selectedDragController.clearSelection();
		widget.setHeightInt(FormUtil.convertDimensionToInt(rubberBandHeight)+35);
		widget.setWidth(rubberBandWidth);
	}

	protected void copyWidgets(boolean remove){
		Context.clipBoardWidgets.clear();

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

			if(remove){ //cut{
				tryUnregisterDropController(widget);
				selectedPanel.remove(widget);
			}
			else{ //copy
				widget = new DesignWidgetWrapper(widget,images);
				tryUnregisterDropController(widget);
			}

			Context.clipBoardWidgets.add(widget);
		}
	}

	private void tryUnregisterDropController(DesignWidgetWrapper widget){
		if(widget.getWrappedWidget() instanceof DesignGroupWidget)
			PaletteView.unRegisterDropController(((DesignGroupWidget)widget.getWrappedWidget()).getDragController().getFormDesignerDropController());
	}

	private void updateClipboardLeftMostPos(){
		for(int i=0; i<Context.clipBoardWidgets.size(); i++){
			DesignWidgetWrapper widget = Context.clipBoardWidgets.get(i);
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
		}
	}

	protected void updateCursorPos(int x, int y){
		this.x = x;
		this.y = y;
	}

	protected void pasteWidgets(boolean afterContextMenu){
		int xOffset = x - clipboardLeftMostPos;
		int yOffset = y - clipboardTopMostPos;

		selectedDragController.clearSelection();

		for(int i=0; i<Context.clipBoardWidgets.size(); i++){
			DesignWidgetWrapper widget = new DesignWidgetWrapper(Context.clipBoardWidgets.get(i),images);
			widget.setWidgetSelectionListener(this);

			if(i == 0){
				if(widget.getPopupPanel() != widgetPopup)
					updateClipboardLeftMostPos();
			}

			selectedDragController.makeDraggable(widget);
			selectedPanel.add(widget);

			if(widget.getWrappedWidget() instanceof DesignGroupWidget && !widget.isRepeated())
				selectedDragController.makeDraggable(widget,((DesignGroupWidget)widget.getWrappedWidget()).getHeaderLabel());

			if(widget.getPopupPanel() != widgetPopup){
				if(afterContextMenu)
					selectedPanel.setWidgetPosition(widget,(x-getAbsoluteLeft())+(widget.getLeftInt()-clipboardLeftMostPos-10),(y-getAbsoluteTop())+(widget.getTopInt()-clipboardTopMostPos-10));
				else
					selectedPanel.setWidgetPosition(widget,x-10,y-10);
			}
			else{
				String s = widget.getLeft();
				int xPos = Integer.parseInt(s.substring(0,s.length()-2)) + xOffset;
				s = widget.getTop();
				int yPos = Integer.parseInt(s.substring(0,s.length()-2)) + yOffset;

				if(yPos-widget.getAbsoluteTop() >= 0 && xPos-widget.getAbsoluteLeft() >= 0){
					xPos = xPos-widget.getAbsoluteLeft();
					yPos = yPos-widget.getAbsoluteTop();
				}

				selectedPanel.setWidgetPosition(widget,xPos,yPos);
			}

			widget.setWidth(widget.getWidth());
			widget.setHeight(widget.getHeight());
			widget.setPopupPanel(widgetPopup);
			selectedDragController.toggleSelection(widget);
			if(widget.getWrappedWidget() instanceof DesignGroupWidget)
				((DesignGroupWidget)widget.getWrappedWidget()).setWidgetPosition();

			if(i == 0 && Context.clipBoardWidgets.size() == 1)
				widgetSelectionListener.onWidgetSelected(widget);
		}

		if(Context.clipBoardWidgets.size() > 1)
			widgetSelectionListener.onWidgetSelected(null);
	}

	protected boolean deleteWidgets(){
		if(!Window.confirm(LocaleText.get("deleteWidgetPrompt")))
			return true;

		for(int i=0; i<selectedDragController.getSelectedWidgetCount(); i++){
			DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedDragController.getSelectedWidgetAt(i);
			if(widget.getLayoutNode() != null)
				widget.getLayoutNode().getParentNode().removeChild(widget.getLayoutNode());
			tryUnregisterDropController(widget);
			selectedPanel.remove(widget);
		}

		selectedDragController.clearSelection();

		return false;
	}

	public void copyItem() {
		if(selectedDragController.isAnyWidgetSelected())
			copyWidgets(false);
		else
			copyChildWidgets(false);
	}

	public void cutItem() {
		if(selectedDragController.isAnyWidgetSelected())
			cutWidgets();
		else
			copyChildWidgets(true);
	}

	private void copyChildWidgets(boolean remove){
		if(selectedPanel.getWidgetIndex(rubberBand) > -1)
			selectedPanel.remove(rubberBand);
		for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
			DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedPanel.getWidget(index);
			if(widget.getWrappedWidget() instanceof DesignGroupWidget){
				DesignGroupWidget designGroupWidget = (DesignGroupWidget)widget.getWrappedWidget();
				if(designGroupWidget.isAnyWidgetSelected()){
					designGroupWidget.copyWidgets(remove);
					return;
				}
			}
		}
	}

	public void pasteItem(){
		pasteItem(false);
	}

	public void pasteItem(boolean increment) {
		if(Context.clipBoardWidgets.size() > 0){
			if(!selectedDragController.isAnyWidgetSelected()){
				if(this instanceof DesignGroupWidget){
					//x = selectedPanel.getAbsoluteLeft() + 10;
					//y = selectedPanel.getAbsoluteTop() + 10;
				}
				else if(increment){
					x += selectedPanel.getAbsoluteLeft();
					y += selectedPanel.getAbsoluteTop();
				}
				else{
					x += 10;
					y += 10;
				}

				pasteWidgets(false);
			}
			else if(selectedDragController.getSelectedWidgetCount() == 1){
				DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedDragController.getSelectedWidgetAt(0);
				if(widget.getWrappedWidget() instanceof DesignGroupWidget)
					((DesignGroupWidget)widget.getWrappedWidget()).pasteItem();
			}
		}
	}

	public void deleteSelectedItem() {
		if(selectedDragController.isAnyWidgetSelected())
			deleteWidgets();
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


	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignLeft()
	 */
	public boolean alignLeft() {
		List<Widget> widgets = selectedDragController.getSelectedWidgets();
		if(widgets == null || widgets.size() < 2){
			for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
				DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedPanel.getWidget(index);
				if(widget.getWrappedWidget() instanceof DesignGroupWidget){
					if(((DesignGroupWidget)widget.getWrappedWidget()).alignLeft())
						return true;
				}
			}
			return false;
		}

		//align according to the last selected item.
		String left = ((DesignWidgetWrapper)widgets.get(widgets.size() - 1)).getLeft();
		for(int index = 0; index < widgets.size(); index++)
			((DesignWidgetWrapper)widgets.get(index)).setLeft(left);

		return true;
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignRight()
	 */
	public boolean alignRight() {
		List<Widget> widgets = selectedDragController.getSelectedWidgets();
		if(widgets == null || widgets.size() < 2){
			for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
				DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedPanel.getWidget(index);
				if(widget.getWrappedWidget() instanceof DesignGroupWidget){
					if(((DesignGroupWidget)widget.getWrappedWidget()).alignRight())
						return true;
				}
			}
			return false;
		}

		//align according to the last selected item.
		DesignWidgetWrapper widget = (DesignWidgetWrapper)widgets.get(widgets.size() - 1);
		int total = widget.getElement().getScrollWidth() + FormUtil.convertDimensionToInt(widget.getLeft());
		for(int index = 0; index < widgets.size(); index++){
			widget = (DesignWidgetWrapper)widgets.get(index);
			widget.setLeft((total - widget.getElement().getScrollWidth()+"px"));
		}

		return true;
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignLeft()
	 */
	public boolean alignTop() {
		List<Widget> widgets = selectedDragController.getSelectedWidgets();
		if(widgets == null || widgets.size() < 2){
			for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
				DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedPanel.getWidget(index);
				if(widget.getWrappedWidget() instanceof DesignGroupWidget){
					if(((DesignGroupWidget)widget.getWrappedWidget()).alignTop())
						return true;
				}
			}
			return false;
		}

		//align according to the last selected item.
		String top = ((DesignWidgetWrapper)widgets.get(widgets.size() - 1)).getTop();
		for(int index = 0; index < widgets.size(); index++)
			((DesignWidgetWrapper)widgets.get(index)).setTop(top);

		return true;
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignRight()
	 */
	public boolean alignBottom() {
		List<Widget> widgets = selectedDragController.getSelectedWidgets();
		if(widgets == null || widgets.size() < 2){
			for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
				DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedPanel.getWidget(index);
				if(widget.getWrappedWidget() instanceof DesignGroupWidget){
					if(((DesignGroupWidget)widget.getWrappedWidget()).alignBottom())
						return true;
				}
			}
			return false;
		}

		//align according to the last selected item.
		DesignWidgetWrapper widget = (DesignWidgetWrapper)widgets.get(widgets.size() - 1);
		int total = widget.getElement().getScrollHeight() + FormUtil.convertDimensionToInt(widget.getTop());
		for(int index = 0; index < widgets.size(); index++){
			widget = (DesignWidgetWrapper)widgets.get(index);
			widget.setTop((total - widget.getElement().getScrollHeight()+"px"));
		}

		return true;
	}

	public boolean makeSameHeight() {
		List<Widget> widgets = selectedDragController.getSelectedWidgets();
		if(widgets == null || widgets.size() < 2){
			for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
				DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedPanel.getWidget(index);
				if(widget.getWrappedWidget() instanceof DesignGroupWidget){
					if(((DesignGroupWidget)widget.getWrappedWidget()).makeSameHeight())
						return true;
				}
			}
			return false;
		}

		//align according to the last selected item.
		String height = ((DesignWidgetWrapper)widgets.get(widgets.size() - 1)).getHeight();
		for(int index = 0; index < widgets.size(); index++)
			((DesignWidgetWrapper)widgets.get(index)).setHeight(height);

		return true;
	}

	public boolean makeSameWidth() {
		List<Widget> widgets = selectedDragController.getSelectedWidgets();
		if(widgets == null || widgets.size() < 2){
			for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
				DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedPanel.getWidget(index);
				if(widget.getWrappedWidget() instanceof DesignGroupWidget){
					if(((DesignGroupWidget)widget.getWrappedWidget()).makeSameWidth())
						return true;
				}
			}
			return false;
		}

		//align according to the last selected item.
		String width = ((DesignWidgetWrapper)widgets.get(widgets.size() - 1)).getWidth();
		for(int index = 0; index < widgets.size(); index++)
			((DesignWidgetWrapper)widgets.get(index)).setWidth(width);

		return true;
	}

	public boolean makeSameSize() {
		List<Widget> widgets = selectedDragController.getSelectedWidgets();
		if(widgets == null || widgets.size() < 2){
			for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
				DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedPanel.getWidget(index);
				if(widget.getWrappedWidget() instanceof DesignGroupWidget){
					if(((DesignGroupWidget)widget.getWrappedWidget()).makeSameSize())
						return true;
				}
			}
			return false;
		}

		//align according to the last selected item.
		String width = ((DesignWidgetWrapper)widgets.get(widgets.size() - 1)).getWidth();
		String height = ((DesignWidgetWrapper)widgets.get(widgets.size() - 1)).getHeight();
		for(int index = 0; index < widgets.size(); index++){
			((DesignWidgetWrapper)widgets.get(index)).setWidth(width);
			((DesignWidgetWrapper)widgets.get(index)).setHeight(height);
		}

		return true;
	}

	public boolean format(){
		if(selectedDragController.getSelectedWidgetCount() < 2){
			for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
				DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedPanel.getWidget(index);
				if(widget.getWrappedWidget() instanceof DesignGroupWidget){
					if(((DesignGroupWidget)widget.getWrappedWidget()).format())
						return true;
				}
			}
			return false;
		}

		rightAlignLabels(selectedPanel);
		return true;
	}

	protected void selectAll(){
		selectedDragController.clearSelection();
		for(int i=0; i<selectedPanel.getWidgetCount(); i++){
			if(selectedPanel.getWidget(i) instanceof DesignWidgetWrapper){
				DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedPanel.getWidget(i);
				if("100%".equalsIgnoreCase(widget.getWidth()))
					continue; //This could be a group header label and hence we are not selecting it via all
				selectedDragController.selectWidget(widget);
			}
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

	protected void handleStartLabelEditing(Event event){
		String s = event.getTarget().getClassName();
		s.toString();
		if(!event.getCtrlKey() && !isTextBoxFocus(event)){
			if(selectedDragController.getSelectedWidgetCount() == 1 /*||
					(selectedDragController.getSelectedWidgetCount() == 0 && this instanceof DesignGroupWidget)*/){
				stopLabelEdit();

				if(selectedDragController.getSelectedWidgetCount() == 0 && this instanceof DesignGroupWidget)
					editWidget = ((DesignGroupWidget)this).getHeaderLabel();
				else
					editWidget = (DesignWidgetWrapper)selectedDragController.getSelectedWidgetAt(0);

				if(editWidget != null){
					if(editWidget.getWidgetSelectionListener() instanceof DesignGroupWidget & !(this instanceof DesignGroupWidget)){
						((DesignGroupWidget)editWidget.getWidgetSelectionListener()).handleStartLabelEditing(event);
						editWidget = null;
						return;
					}
					else if(editWidget.hasLabelEdidting()){
						selectedDragController.makeNotDraggable(editWidget);
						selectedDragController.clearSelection();
						editWidget.startEditMode(txtEdit);
					}
					else
						editWidget = null;
				}
			}
		}
	}

	protected void handleStopLabelEditing(){
		if(editWidget == null && selectedDragController.isAnyWidgetSelected()){
			editWidget = (DesignWidgetWrapper)selectedDragController.getSelectedWidgetAt(0);
			if(editWidget.getWidgetSelectionListener() instanceof DesignGroupWidget & !(this instanceof DesignGroupWidget)){
				((DesignGroupWidget)editWidget.getWidgetSelectionListener()).handleStopLabelEditing();
				editWidget = null;
				return;
			}
			else if(editWidget.hasLabelEdidting())
				stopLabelEdit();
		}
		else
			stopLabelEdit();
	}

	protected boolean isTextBoxFocus(Event event){
		return event.getTarget().getClassName().equalsIgnoreCase("gwt-TextBox") || event.getTarget().getClassName().equalsIgnoreCase("gwt-SuggestBox");
	}
	
	protected boolean isTextAreaFocus(Event event){
		return event.getTarget().getClassName().equalsIgnoreCase("gwt-TextArea");
	}

	public int getRubberLeft(){
		return FormUtil.convertDimensionToInt(DOM.getStyleAttribute(rubberBand.getElement(), "left"));
	}

	public int getRubberTop(){
		return FormUtil.convertDimensionToInt(DOM.getStyleAttribute(rubberBand.getElement(), "top"));
	}

	public void startRubberBand(Event event){
		//if(this instanceof DesignGroupWidget)
		//	return;

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
			//This exception is intentionally ignored as a rubber band is no big deal
		}
	}

	protected boolean moveWidgets(int dirrection){
		List<Widget> widgets = selectedDragController.getSelectedWidgets();
		if(widgets == null)
			return false;

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

		return widgets.size() > 0;
	}

	protected void selectWidgets(Event event){
		int endX = event.getClientX() - selectedPanel.getAbsoluteLeft();
		int endY = event.getClientY() - selectedPanel.getAbsoluteTop();

		//Store this for Group Widgets
		rubberBandHeight = DOM.getStyleAttribute(rubberBand.getElement(), "height");
		rubberBandWidth = DOM.getStyleAttribute(rubberBand.getElement(), "width");

		for(int i=0; i<selectedPanel.getWidgetCount(); i++){
			if(selectedPanel.getWidget(i) instanceof  DesignWidgetWrapper){
				DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedPanel.getWidget(i);
				if(widget.isWidgetInRect(selectionXPos, selectionYPos, endX, endY))
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

	protected void updatePopup(){
		boolean visible = false;
		if(selectedDragController.isAnyWidgetSelected())
			visible = true;
		deleteWidgetsSeparator.setVisible(visible);
		deleteWidgetsMenu.setVisible(visible);

		//For now this is only used by the DesignSurfaceView
		if(groupWidgetsSeparator != null){
			groupWidgetsSeparator.setVisible(visible);
			groupWidgetsMenu.setVisible(visible);
		}
		
		cutCopySeparator.setVisible(visible);
		cutMenu.setVisible(visible);
		copyMenu.setVisible(visible); 

		visible = false;
		if(Context.clipBoardWidgets.size() > 0)
			visible = true;
		pasteSeparator.setVisible(visible);
		pasteMenu.setVisible(visible); 
	}

	protected DesignWidgetWrapper getSelPageDesignWidget(){
		return pageWidgets.get(selectedTabIndex);
	}

	protected DesignWidgetWrapper addNewWidget(Widget widget, boolean select, WidgetSelectionListener widgetSelectionListener){
		currentWidgetSelectionListener = widgetSelectionListener;
		return addNewWidget(widget,select);
	}

	protected DesignWidgetWrapper addNewWidget(Widget widget, boolean select){
		stopLabelEdit();

		DesignWidgetWrapper wrapper = new DesignWidgetWrapper(widget,widgetPopup,currentWidgetSelectionListener);

		/*if(widget instanceof ListBox)
			selectedDragController.makeDraggable(wrapper,wrapper);
		else*/
		selectedDragController.makeDraggable(wrapper);

		selectedPanel.add(wrapper);
		//selectedPanel.setWidgetPosition(wrapper, x-wrapper.getAbsoluteLeft(), y-wrapper.getAbsoluteTop());
		selectedPanel.setWidgetPosition(wrapper, x-wrapper.getParent().getAbsoluteLeft(), y-wrapper.getParent().getAbsoluteTop());
		if(select){
			selectedDragController.clearSelection();
			selectedDragController.toggleSelection(wrapper);
			//widgetSelectionListener.onWidgetSelected(wrapper);
			onWidgetSelected(wrapper);
		}
		return wrapper;
	}

	protected DesignWidgetWrapper addNewLabel(String text, boolean select){
		if(text == null)
			text = LocaleText.get("label");
		Label label = new Label(text);

		DesignWidgetWrapper wrapper = addNewWidget(label,select);		
		wrapper.setFontFamily(FormUtil.getDefaultFontFamily());
		return wrapper;
	}

	protected DesignWidgetWrapper addNewVideoAudio(String text, boolean select){
		if(text == null)
			text = LocaleText.get("clickToPlay");
		Hyperlink link = new Hyperlink(text,null);

		DesignWidgetWrapper wrapper = addNewWidget(link,select);
		wrapper.setFontFamily(FormUtil.getDefaultFontFamily());
		return wrapper;
	}

	protected DesignWidgetWrapper addNewPicture(boolean select){
		Image image = images.picture().createImage();
		DOM.setStyleAttribute(image.getElement(), "height","155px");
		DOM.setStyleAttribute(image.getElement(), "width","185px");
		return addNewWidget(image,select);
	}

	protected DesignWidgetWrapper addNewTextBox(boolean select){
		TextBox tb = new TextBox();
		DOM.setStyleAttribute(tb.getElement(), "height","25px");
		DOM.setStyleAttribute(tb.getElement(), "width","200px");
		return addNewWidget(tb,select);
	}

	protected DesignWidgetWrapper addNewDatePicker(boolean select){
		DatePicker tb = new DatePickerWidget();
		DOM.setStyleAttribute(tb.getElement(), "height","25px");
		DOM.setStyleAttribute(tb.getElement(), "width","200px");
		return addNewWidget(tb,select);
	}

	protected DesignWidgetWrapper addNewCheckBox(boolean select){
		return addNewWidget(new CheckBox(LocaleText.get("checkBox")),select);
	}

	protected DesignWidgetWrapper addNewRadioButton(boolean select){
		return addNewWidget(new RadioButton("RadioButton",LocaleText.get("radioButton")),select);
	}

	protected DesignWidgetWrapper addNewDropdownList(boolean select){
		ListBox lb = new ListBox(false);
		DOM.setStyleAttribute(lb.getElement(), "height","25px");
		DOM.setStyleAttribute(lb.getElement(), "width","200px");
		DesignWidgetWrapper wrapper = addNewWidget(lb,select);
		return wrapper;
	}

	protected DesignWidgetWrapper addNewTextArea(boolean select){
		TextArea ta = new TextArea();
		DOM.setStyleAttribute(ta.getElement(), "height","60px");
		DOM.setStyleAttribute(ta.getElement(), "width","200px");
		return addNewWidget(ta,select);
	}

	protected DesignWidgetWrapper addNewButton(String label, String binding, boolean select){
		DesignWidgetWrapper wrapper = addNewWidget(new Button(label),select);
		wrapper.setWidthInt(70);
		wrapper.setHeightInt(30);
		wrapper.setBinding(binding);
		return wrapper;
	}

	protected DesignWidgetWrapper addNewButton(boolean select){
		return addNewButton(LocaleText.get("submit"),"submit",select);
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

	protected DesignWidgetWrapper addNewCheckBoxSet(QuestionDef questionDef, int max, String pageName){
		return null;
	}

	public DesignWidgetWrapper onDrop(Widget widget,int x, int y){
		if(!(widget instanceof PaletteWidget))
			return null;

		this.x = x;
		this.y = y;

		String text = ((PaletteWidget)widget).getText();

		if(text.equals(LocaleText.get("label")))
			return addNewLabel(LocaleText.get("label"),true);
		else if(text.equals(LocaleText.get("textBox")))
			return addNewTextBox(true);
		else if(text.equals(LocaleText.get("checkBox")))
			return addNewCheckBox(true);
		else if(text.equals(LocaleText.get("radioButton")))
			return addNewRadioButton(true);
		else if(text.equals(LocaleText.get("listBox")))
			return addNewDropdownList(true);
		else if(text.equals(LocaleText.get("textArea")))
			return addNewTextArea(true);
		else if(text.equals(LocaleText.get("button")))
			return addNewButton(true);
		else if(text.equals(LocaleText.get("datePicker")))
			return addNewDatePicker(true);
		
		return null;
	}

	protected void initPanel(){

		//Create a DragController for each logical area where a set of draggable
		// widgets and drop targets will be allowed to interact with one another.
		selectedDragController = new FormDesignerDragController(selectedPanel, false,this);

		// Positioner is always constrained to the boundary panel
		// Use 'true' to also constrain the draggable or drag proxy to the boundary panel
		//dragController.setBehaviorConstrainedToBoundaryPanel(false);

		// Allow multiple widgets to be selected at once using CTRL-click
		selectedDragController.setBehaviorMultipleSelection(true);
		selectedDragController.setBehaviorDragStartSensitivity(1);
		//selectedDragController.setBehaviorCancelDocumentSelections(true);

		// create a DropController for each drop target on which draggable widgets
		// can be dropped
		DropController dropController =  new FormDesignerDropController(selectedPanel,this);

		// Don't forget to register each DropController with a DragController
		selectedDragController.registerDropController(dropController);
		PaletteView.registerDropController(dropController);

		initEditWidget();
	}

	private void initEditWidget(){
		DOM.setStyleAttribute(txtEdit.getElement(), "borderStyle", "none");
		DOM.setStyleAttribute(txtEdit.getElement(), "fontFamily", FormUtil.getDefaultFontFamily());
		//DOM.setStyleAttribute(txtEdit.getElement(), "opacity", "1");
		txtEdit.setWidth("400px");
		//txtEdit.addStyleName("purcforms-label-editor");
	}

	protected void stopLabelEdit(){
		if(editWidget != null){
			if(selectedPanel.getWidgetIndex(editWidget) < 0){
				editWidget = null;
				return;
			}

			editWidget.stopEditMode();

			String text = txtEdit.getText();
			if(text.trim().length() > 0)
				editWidget.setText(text);

			//selectedPanel.remove(editWidget);
			//selectedPanel.add(editWidget);
			//selectedPanel.setWidgetPosition(editWidget, editWidget.getLeftInt(), editWidget.getTopInt());

			//if(this instanceof DesignSurfaceView){
			selectedPanel.setWidgetPosition(editWidget, editWidget.getLeftInt(), editWidget.getTopInt());
			selectedDragController.makeDraggable(editWidget);
			selectedDragController.selectWidget(editWidget);
			widgetSelectionListener.onWidgetSelected(editWidget);
			/*}
			else{
				DesignSurfaceView surface = (DesignSurfaceView)getParent().getParent().getParent().getParent().getParent().getParent().getParent();
				surface.stopHeaderLabelEdit(editWidget);
			}*/
			editWidget = null;
		}
	}

	public void onBrowserEvent(Event event) {
		switch (DOM.eventGetType(event)) {
		case Event.ONMOUSEDOWN:  
			mouseMoved = false;
			x = event.getClientX();
			y = event.getClientY();

			if(editWidget != null){
				if(editWidget.getWrappedWidgetEx().getElement() == event.getTarget())
					return;
				handleStopLabelEditing();
			}

			if( (event.getButton() & Event.BUTTON_RIGHT) != 0){
				updatePopup();
				popup.setPopupPosition(event.getClientX(), event.getClientY());
				FormDesignerUtil.disableContextMenu(popup.getElement());
				popup.show();
			}
			else{
				selectionXPos = selectionYPos = -1;
				selectionXPos = x - selectedPanel.getAbsoluteLeft();
				selectionYPos = y - selectedPanel.getAbsoluteTop();

				if(!(event.getShiftKey() || event.getCtrlKey())){
					selectedDragController.clearSelection();
					if(event.getTarget() != this.selectedPanel.getElement()){
						try{
							if(event.getTarget().getInnerText().equals(DesignWidgetWrapper.getTabDisplayText(tabs.getTabBar().getTabHTML(tabs.getTabBar().getSelectedTab())))){
								widgetSelectionListener.onWidgetSelected(getSelPageDesignWidget());
								return;
							}
						}catch(Exception ex){}
					}
				}

				if(this instanceof DesignGroupWidget)
					widgetSelectionListener.onWidgetSelected((DesignWidgetWrapper)this.getParent().getParent());

				//if(!(this instanceof DesignGroupWidget) || (this instanceof DesignGroupWidget && !((DesignWidgetWrapper)this.getParent().getParent()).isRepeated()))
				//	widgetSelectionListener.onWidgetSelected(null);

				widgetSelectionListener.onWidgetSelected(this);

				clearGroupBoxSelection();

				if(!(this instanceof DesignGroupWidget && !"default".equals(DOM.getStyleAttribute(getElement(), "cursor"))))
					startRubberBand(event);
			}

			break;
		case Event.ONMOUSEMOVE:
			mouseMoved = true;
			if(event.getButton() == Event.BUTTON_LEFT)
				moveRubberBand(event);
			break;
		case Event.ONMOUSEUP:

			//if(selectedPanel.getWidgetCount() > 0)
			stopRubberBand(event);
			if(selectionXPos != -1 && mouseMoved)
				selectWidgets(event);
			mouseMoved = false;
			break;
		case Event.ONKEYDOWN:
			handleKeyDownEvent(event);
			break;
		}
	}

	protected void clearGroupBoxSelection(){
		for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
			Widget wid = selectedPanel.getWidget(index);
			if(!(wid instanceof DesignWidgetWrapper))
				continue;
			if(!(((DesignWidgetWrapper)wid).getWrappedWidget() instanceof DesignGroupWidget))
				continue;
			((DesignGroupWidget)((DesignWidgetWrapper)wid).getWrappedWidget()).clearGroupBoxSelection();
		}
	}

	protected boolean handleKeyDownEvent(Event event){
		/*if(isTextBoxFocus(event)){
			if("none".equalsIgnoreCase(event.getTarget().getStyle().getProperty("borderStyle")));
				return true;
		}*/
		
		boolean ret = false;

		if(this.isVisible()){
			int keyCode = event.getKeyCode();
			if(keyCode == KeyboardListener.KEY_LEFT)
				ret = moveWidgets(MOVE_LEFT);
			else if(keyCode == KeyboardListener.KEY_RIGHT)
				ret = moveWidgets(MOVE_RIGHT);
			else if(keyCode == KeyboardListener.KEY_UP)
				ret = moveWidgets(MOVE_UP);
			else if(keyCode == KeyboardListener.KEY_DOWN)
				ret = moveWidgets(MOVE_DOWN);  
			else if(event.getCtrlKey() && (keyCode == 'A' || keyCode == 'a')){
				if(!isTextAreaFocus(event)){ //TODO This works only when the textarea is clicked to get focus. Need to make it work even before clicking the text area (as long as it is visible)
					selectAll();
					DOM.eventPreventDefault(event);
				}
				ret = true;
			}
			else if(event.getCtrlKey() && (keyCode == 'C' || keyCode == 'c')){
				if(selectedDragController.isAnyWidgetSelected()){
					copyWidgets(false);
					ret = true;
				}
			}
			else if(event.getCtrlKey() && (keyCode == 'X' || keyCode == 'x')){
				if(selectedDragController.isAnyWidgetSelected()){
					cutWidgets();
					ret = true;
				}
			}
			else if(event.getCtrlKey() && (keyCode == 'V' || keyCode == 'v')){
				if(Context.clipBoardWidgets.size() > 0 && x >= 0){
					if(event.getTarget() == selectedPanel.getElement()){
						//x += selectedPanel.getAbsoluteLeft();
						//y += selectedPanel.getAbsoluteTop();
						//pasteWidgets(true);
						pasteItem(false);
						x = -1; //TODO preven;t pasting twice as this is fired twice. Needs smarter solution
					}
					ret = true;
				}
			}
			else if(keyCode == KeyboardListener.KEY_DELETE && !isTextBoxFocus(event)){
				if(selectedDragController.isAnyWidgetSelected()){
					deleteWidgets();
					ret = true;
				}
			}
			else if(event.getCtrlKey() && (keyCode == 'F' || keyCode == 'f')){
				format();
				DOM.eventPreventDefault(event);
				ret = false; //For now this is reserved for only designsurfaceview
			}

			boolean textBoxFocus = isTextBoxFocus(event);
			if(!textBoxFocus || (editWidget != null /*&& event.getCurrentTarget() == editWidget.getElement()*/)){
				if(keyCode != KeyboardListener.KEY_DELETE)
					handleStartLabelEditing(event);

				if(keyCode == KeyboardListener.KEY_ENTER)
					handleStopLabelEditing();
			}
		}

		return ret;
	}
	
	public String getBackgroundColor(){
		return DOM.getStyleAttribute(selectedPanel.getElement(), "backgroundColor");
	}
	
	public String getWidth(){
		return DOM.getStyleAttribute(selectedPanel.getElement(), "width");
	}
	
	public String getHeight(){
		return DOM.getStyleAttribute(selectedPanel.getElement(), "height");
	}
	
	public void setBackgroundColor(String backgroundColor){
		try{
			DOM.setStyleAttribute(selectedPanel.getElement(), "backgroundColor", backgroundColor);
		}catch(Exception ex){}
	}
	
	public void setWidth(String width){
		try{
			DOM.setStyleAttribute(selectedPanel.getElement(), "width", width);
		}catch(Exception ex){}
	}
	
	public void setHeight(String height){
		try{
			DOM.setStyleAttribute(selectedPanel.getElement(), "height", height);
		}catch(Exception ex){}
	}
}
