package org.purc.purcforms.client.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.purc.purcforms.client.Context;
import org.purc.purcforms.client.PurcConstants;
import org.purc.purcforms.client.LeftPanel.Images;
import org.purc.purcforms.client.controller.DragDropListener;
import org.purc.purcforms.client.controller.FormDesignerDragController;
import org.purc.purcforms.client.controller.FormDesignerDropController;
import org.purc.purcforms.client.controller.IWidgetPopupMenuListener;
import org.purc.purcforms.client.controller.WidgetPropertyChangeListener;
import org.purc.purcforms.client.controller.WidgetPropertySetter;
import org.purc.purcforms.client.controller.WidgetSelectionListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.OptionDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.util.StyleUtil;
import org.purc.purcforms.client.widget.DatePickerEx;
import org.purc.purcforms.client.widget.DatePickerWidget;
import org.purc.purcforms.client.widget.DateTimeWidget;
import org.purc.purcforms.client.widget.DesignGroupWidget;
import org.purc.purcforms.client.widget.DesignWidgetWrapper;
import org.purc.purcforms.client.widget.PaletteWidget;
import org.purc.purcforms.client.widget.RadioButtonWidget;
import org.purc.purcforms.client.widget.TextBoxWidget;
import org.purc.purcforms.client.widget.TimeWidget;

import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.google.gwt.event.dom.client.KeyCodes;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
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
public class DesignGroupView extends Composite implements WidgetSelectionListener,IWidgetPopupMenuListener,DragDropListener,WidgetPropertyChangeListener{

	protected static final int MOVE_LEFT = 1;
	protected static final int MOVE_RIGHT = 2;
	protected static final int MOVE_UP = 3;
	protected static final int MOVE_DOWN = 4;

	/** The popup panel for the design surface context menu. */
	protected PopupPanel popup;

	/** The popup panel for the widget context menu. */
	protected PopupPanel widgetPopup;

	/** The cursor position x cordinate. */
	protected int x;

	/** The cursor position y cordinate. */
	protected int y;

	protected int clipboardLeftMostPos;
	protected int clipboardTopMostPos;

	/** The copy menu item for the design surface context menu. */
	protected MenuItem copyMenu;

	/** The cut menu item. */
	protected MenuItem cutMenu;
	protected MenuItem pasteMenu;
	protected MenuItem deleteWidgetsMenu;
	protected MenuItem groupWidgetsMenu;
	protected MenuItem lockWidgetsMenu;
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

	/** The rubber band widget for multiple widget selection. */
	protected Label rubberBand = new Label(""); //HTML("<DIV ID='rubberBand'></DIV>");

	protected final Images images;

	//These three do not belong here (should be only for DesignSurfaceView)
	/** Tabs for displaying pages. */
	protected DecoratedTabPanel tabs = new DecoratedTabPanel();
	protected HashMap<Integer,DesignWidgetWrapper> pageWidgets = new HashMap<Integer,DesignWidgetWrapper>();

	/** The index of the selected page tab. */
	protected int selectedTabIndex = 0;

	protected WidgetSelectionListener currentWidgetSelectionListener;

	/** The text box widget for inline label editing. */
	protected TextBox txtEdit = new TextBox();
	protected DesignWidgetWrapper editWidget;

	/** The selection rubber band height in pixels. */
	protected String rubberBandHeight;

	/** The selection rubber band width in pixels. */
	protected String rubberBandWidth;


	/**
	 * Creates a new instance of the design surface.
	 * 
	 * @param images
	 */
	public DesignGroupView(Images images){
		this.images = images;
	}

	public void onDragEnd(Widget widget) {
		onWidgetSelected(getSelectedWidget((DesignWidgetWrapper)widget),true);

		((DesignWidgetWrapper)widget).refreshSize();
		if(((DesignWidgetWrapper)widget).getWrappedWidget() instanceof DesignGroupWidget)
			DOM.setStyleAttribute(((DesignWidgetWrapper)widget).getWrappedWidget().getElement(), "cursor", "default");

		//if(((DesignWidgetWrapper)widget).getWrappedWidget() instanceof DesignGroupWidget)
		//	((DesignGroupWidget)((DesignWidgetWrapper)widget).getWrappedWidget()).getHeaderLabel().refreshSize();
	}

	public void onDragStart(Widget widget) {
		onWidgetSelected(getSelectedWidget((DesignWidgetWrapper)widget),true);
	}

	private DesignWidgetWrapper getSelectedWidget(DesignWidgetWrapper widget){
		if(widget.getWrappedWidget() instanceof DesignGroupWidget && !widget.isRepeated()){
			String cursor = DOM.getStyleAttribute(widget.getWrappedWidget().getElement(), "cursor");
			if("move".equals(cursor) || "default".equals(cursor))
				return ((DesignGroupWidget)widget.getWrappedWidget()).getHeaderLabel();
		}
		return widget;
	}

	public void onWidgetSelected(Widget widget, boolean multipleSel){

		//Some widgets like check boxes and buttons may not have sizes set yet
		//and so when in edit mode, they fire onmousedown events.
		if(widget != null && widget == editWidget)
			return;

		if(widget == null)
			selectedDragController.clearSelection();
		else if(widget instanceof DesignWidgetWrapper && !(((DesignWidgetWrapper)widget).getWrappedWidget() instanceof DesignGroupWidget)){
			String s = ((DesignWidgetWrapper)widget).getWidth();
			if(!"100%".equals(s)){
				if(widgetSelectionListener instanceof DesignSurfaceView){
					((DesignSurfaceView)widgetSelectionListener).clearSelection();
					if(selectedDragController.getSelectedWidgetCount() == 1)
						((DesignSurfaceView)widgetSelectionListener).clearGroupBoxSelection();

					//if(!multipleSel && selectedDragController.getSelectedWidgetCount() == 1)
					//	selectedDragController.clearSelection();
				}

				if(!multipleSel && !selectedDragController.isWidgetSelected(widget)/*selectedDragController.getSelectedWidgetCount() == 1*/)
					selectedDragController.clearSelection();

				//Deselect and stop editing of any widget in group boxes
				//TODO Doesnt this slow us a bit?
				if(widget instanceof DesignWidgetWrapper &&  ((DesignWidgetWrapper)widget).getWidgetSelectionListener() instanceof DesignSurfaceView)
					clearGroupBoxSelection();

				//Deselect any previously selected widgets in groupbox
				selectedDragController.selectWidget(widget); //TODO Test this and make sure it does not introduce bugs
			}

			stopLabelEdit(false);
		}

		widgetSelectionListener.onWidgetSelected(widget, multipleSel);
	}

	protected void cutWidgets(){
		copyWidgets(true);
	}

	/**
	 * Adds the selected widgets to a group box.
	 */
	protected void groupWidgets(){
		
		//We now allow nested group boxes
		/*for(int i=0; i<selectedDragController.getSelectedWidgetCount(); i++){
			if(((DesignWidgetWrapper)selectedDragController.getSelectedWidgetAt(i)).getWrappedWidget() instanceof DesignGroupWidget)
				return; //We do not allow nested group boxes
		}*/

		cutWidgets();

		x = clipboardLeftMostPos + selectedPanel.getAbsoluteLeft() ;
		y = clipboardTopMostPos + selectedPanel.getAbsoluteTop();

		DesignWidgetWrapper widget = addNewGroupBox(false);
		DesignGroupView designGroupView = (DesignGroupView)widget.getWrappedWidget();
		designGroupView.updateCursorPos(x+20, y+45);
		designGroupView.pasteWidgets(true);
		selectedDragController.clearSelection();
		designGroupView.clearSelection();
		widget.setHeightInt(FormUtil.convertDimensionToInt(rubberBandHeight)+35);
		widget.setWidth(rubberBandWidth);

		selectedDragController.selectWidget(widget);
		widgetSelectionListener.onWidgetSelected(((DesignGroupWidget)designGroupView).getHeaderLabel(),false);
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
				widgetSelectionListener.onWidgetSelected(widget,true);
		}

		if(Context.clipBoardWidgets.size() > 1)
			widgetSelectionListener.onWidgetSelected(null,true);
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

	/**
	 * Deletes the selected widgets.
	 */
	public void deleteSelectedItem() {
		if(selectedDragController.isAnyWidgetSelected())
			deleteWidgets();
	}

	/**
	 * Aligns all labels to the right and all non labels to their left.
	 * This is excecuted after pressing Ctrl + F
	 * 
	 * @param panel the panel holding the widgets.
	 */
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

		List<String> tops = getInputWidgetTops(panel,usingSelection,count);

		DesignWidgetWrapper widget = null;
		for(int index =0; index < count; index++){
			if(usingSelection)
				widget = (DesignWidgetWrapper)selectedDragController.getSelectedWidgetAt(index);
			else
				widget = (DesignWidgetWrapper)panel.getWidget(index);

			//We do not format buttons and group boxes
			if(widget.getWrappedWidget() instanceof Button || widget.getWrappedWidget() instanceof DesignGroupWidget)
				continue;

			if(widget.getWrappedWidget() instanceof Label){
				//We do not align labels which are not on the same y pos as at least one input widget.
				if(!tops.contains(widget.getTop()))
					continue;

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
		String left = (relativeWidth+5)+PurcConstants.UNITS;
		for(int index = 0; index < inputs.size(); index++)
			inputs.get(index).setLeft(left);

		for(int index = 0; index < labels.size(); index++){
			widget = labels.get(index);
			widget.setLeft((relativeWidth - widget.getElement().getScrollWidth()+PurcConstants.UNITS));
		}
	}


	/**
	 * Gets a list of widget top values, in the selected page, which capture user data. 
	 * These are neither labels, buttons, nor group boxes.
	 * 
	 * @param panel the absolute panel for the current page.
	 * @param usingSelection set to true if you want only selected widgets.
	 * @param count the number of widgets to traverse.
	 * @return the widget top value list.
	 */
	private List<String> getInputWidgetTops(AbsolutePanel panel, boolean usingSelection, int count){
		List<String> inputWidgetTops = new ArrayList<String>();

		DesignWidgetWrapper widget = null;
		for(int index =0; index < count; index++){
			if(usingSelection)
				widget = (DesignWidgetWrapper)selectedDragController.getSelectedWidgetAt(index);
			else
				widget = (DesignWidgetWrapper)panel.getWidget(index);

			if(widget.getWrappedWidget() instanceof Button || 
					widget.getWrappedWidget() instanceof DesignGroupWidget ||
					widget.getWrappedWidget() instanceof Label) 
				continue;

			inputWidgetTops.add(widget.getTop());
		}

		return inputWidgetTops;
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
			widget.setLeft((total - widget.getElement().getScrollWidth()+PurcConstants.UNITS));
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
			widget.setTop((total - widget.getElement().getScrollHeight()+PurcConstants.UNITS));
		}

		return true;
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#makeSameHeight()
	 */
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

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#makeSameWidth()
	 */
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

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#makeSameSize()
	 */
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

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#format()
	 */
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

	/**
	 * Selects all widgets on the selected page.
	 */
	protected void selectAll(){
		if(editWidget != null){
			txtEdit.selectAll();
			return; //let label editor do select all
		}

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

	protected boolean handleStartLabelEditing(Event event){
		String s = event.getTarget().getClassName();
		s.toString();
		if(!event.getCtrlKey() && !isTextBoxFocus(event)){
			if(selectedDragController.getSelectedWidgetCount() == 1 /*||
					(selectedDragController.getSelectedWidgetCount() == 0 && this instanceof DesignGroupWidget)*/){
				stopLabelEdit(false);

				if(selectedDragController.getSelectedWidgetCount() == 0 && this instanceof DesignGroupWidget)
					editWidget = ((DesignGroupWidget)this).getHeaderLabel();
				else
					editWidget = (DesignWidgetWrapper)selectedDragController.getSelectedWidgetAt(0);

				if(editWidget != null){
					if(editWidget.getWidgetSelectionListener() instanceof DesignGroupWidget & !(this instanceof DesignGroupWidget)){
						boolean ret = ((DesignGroupWidget)editWidget.getWidgetSelectionListener()).handleStartLabelEditing(event);
						editWidget = null;
						return ret;
					}
					else if(editWidget.hasLabelEdidting()){
						selectedDragController.makeNotDraggable(editWidget);

						//editWidget.removeStyleName("dragdrop-handle");
						//editWidget.removeStyleName("dragdrop-draggable");

						if(this instanceof DesignGroupWidget){
							//this.removeStyleName("dragdrop-handle");
							//this.removeStyleName("dragdrop-draggable");
							((DesignGroupWidget)this).clearSelection();
						}

						selectedDragController.clearSelection();
						editWidget.startEditMode(txtEdit);
						return true;
					}
					else if(editWidget.getWrappedWidget() instanceof DesignGroupWidget){
						//Handle label editing
						DesignWidgetWrapper headerLabel = ((DesignGroupWidget)editWidget.getWrappedWidget()).getHeaderLabel();
						if(headerLabel == null)
							return false;

						//Without these two lines, the edit text is not selected, not even with Ctrl + A
						selectedDragController.makeNotDraggable(editWidget);
						editWidget.removeStyleName("dragdrop-handle");

						((DesignGroupWidget)editWidget.getWrappedWidget()).clearSelection();
						selectedDragController.clearSelection();
						headerLabel.startEditMode(txtEdit);
						return true;
					}
					else
						editWidget = null;
				}
			}
			/*else{
				editWidget = getSelPageDesignWidget();
				editWidget.startEditMode(txtEdit);
				return true;
			}*/
		}
		return false;
	}

	protected void handleStopLabelEditing(boolean select){
		if(editWidget == null && selectedDragController.isAnyWidgetSelected()){
			editWidget = (DesignWidgetWrapper)selectedDragController.getSelectedWidgetAt(0);
			if(editWidget.getWidgetSelectionListener() instanceof DesignGroupWidget & !(this instanceof DesignGroupWidget)){
				((DesignGroupWidget)editWidget.getWidgetSelectionListener()).handleStopLabelEditing(select);
				editWidget = null;
				return;
			}
			else if(editWidget.hasLabelEdidting())
				stopLabelEdit(select);
		}
		else
			stopLabelEdit(select);
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

		//Prevent the browser from selecting text.
		DOM.eventPreventDefault(event);

		selectedPanel.add(rubberBand);

		x = event.getClientX()-selectedPanel.getAbsoluteLeft();
		y = event.getClientY()-selectedPanel.getAbsoluteTop();

		DOM.setStyleAttribute(rubberBand.getElement(), "width", 0+PurcConstants.UNITS);
		DOM.setStyleAttribute(rubberBand.getElement(), "height", 0+PurcConstants.UNITS);
		DOM.setStyleAttribute(rubberBand.getElement(), "left", x+PurcConstants.UNITS);
		DOM.setStyleAttribute(rubberBand.getElement(), "top", y+PurcConstants.UNITS);
		DOM.setStyleAttribute(rubberBand.getElement(), "visibility", "visible");

		DOM.setCapture(getElement());
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
				DOM.setStyleAttribute(rubberBand.getElement(), "left", event.getClientX()-selectedPanel.getAbsoluteLeft()+PurcConstants.UNITS);
				DOM.setStyleAttribute(rubberBand.getElement(), "width", width * -1 + PurcConstants.UNITS);
			}
			else
				DOM.setStyleAttribute(rubberBand.getElement(), "width", (event.getClientX()-selectedPanel.getAbsoluteLeft())-getRubberLeft()+PurcConstants.UNITS);

			if(height < 0){
				DOM.setStyleAttribute(rubberBand.getElement(), "top", event.getClientY()-selectedPanel.getAbsoluteTop()+PurcConstants.UNITS);
				DOM.setStyleAttribute(rubberBand.getElement(), "height", height * -1 + PurcConstants.UNITS);
			}
			else
				DOM.setStyleAttribute(rubberBand.getElement(), "height", (event.getClientY()-selectedPanel.getAbsoluteTop())-getRubberTop()+PurcConstants.UNITS);
		}
		catch(Exception ex){
			//This exception is intentionally ignored as a rubber band is no big deal
		}
	}

	/**
	 * Moves widgets in a given direction due to movement of the keyboard arrow keys.
	 * 
	 * @param dirrection the move dirrection.
	 * @return true if any widget was moved, else false.
	 */
	protected boolean moveWidgets(int dirrection){
		List<Widget> widgets = selectedDragController.getSelectedWidgets();
		if(widgets == null)
			return false;

		int pos;
		for(int index = 0; index < widgets.size(); index++){
			DesignWidgetWrapper widget = (DesignWidgetWrapper)widgets.get(index);

			if(dirrection == MOVE_LEFT){
				pos = FormUtil.convertDimensionToInt(widget.getLeft());
				widget.setLeft(pos-1+PurcConstants.UNITS);
			}
			else if(dirrection == MOVE_RIGHT){
				pos = FormUtil.convertDimensionToInt(widget.getLeft());
				widget.setLeft(pos+1+PurcConstants.UNITS);
			}
			else if(dirrection == MOVE_UP){
				pos = FormUtil.convertDimensionToInt(widget.getTop());
				widget.setTop(pos-1+PurcConstants.UNITS);		
			}
			else if(dirrection == MOVE_DOWN){
				pos = FormUtil.convertDimensionToInt(widget.getTop());
				widget.setTop(pos+1+PurcConstants.UNITS);
			}
		}

		return widgets.size() > 0;
	}

	/**
	 * Resizes widgets in a given direction due to movement of the keyboard arrow keys.
	 * 
	 * @param Event the current event object.
	 * @return true if any widget was resized, else false.
	 */
	protected boolean resizeWidgets(Event event){
		List<Widget> widgets = selectedDragController.getSelectedWidgets();
		if(widgets == null)
			return false;

		int resizedCount = 0;

		int keycode = event.getKeyCode();
		for(int index = 0; index < widgets.size(); index++){
			DesignWidgetWrapper widget = (DesignWidgetWrapper)widgets.get(index);
			if(!widget.isResizable())
				continue;

			resizedCount++;

			if(keycode == KeyCodes.KEY_LEFT)
				widget.setWidthInt(widget.getWidthInt()-1);
			else if(keycode == KeyCodes.KEY_RIGHT)
				widget.setWidthInt(widget.getWidthInt()+1);
			else if(keycode == KeyCodes.KEY_UP)
				widget.setHeightInt(widget.getHeightInt()-1);
			else if(keycode == KeyCodes.KEY_DOWN)
				widget.setHeightInt(widget.getHeightInt()+1);
			else 
				return false; //Shift press when not in combination with arrow keys is ignored.
		}

		return resizedCount > 0;
	}

	/**
	 * Selects widgets wrapped by the rubber band.
	 * 
	 * @param event the event object.
	 */
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
					selectedDragController.selectWidget(widget);
			}
		}

		if((event.getCtrlKey() || event.getShiftKey() || event.getAltKey()) && selectedDragController.getSelectedWidgetCount() == 1){
			DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedDragController.getSelectedWidgetAt(0);
			widget.setWidthInt(endX - widget.getLeftInt());
			widget.setHeightInt(endY - widget.getTopInt());
		}

		if(event.getKeyCode() == KeyCodes.KEY_UP || event.getKeyCode() == KeyCodes.KEY_DOWN){
			for(int index = 0; index < selectedDragController.getSelectedWidgetCount(); index++){
				DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedDragController.getSelectedWidgetAt(index);
				widget.setHeightInt(endY - widget.getTopInt());
			}
		}

		if(event.getKeyCode() == KeyCodes.KEY_RIGHT || event.getKeyCode() == KeyCodes.KEY_LEFT){
			for(int index = 0; index < selectedDragController.getSelectedWidgetCount(); index++){
				DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedDragController.getSelectedWidgetAt(index);
				widget.setWidthInt(endX - widget.getLeftInt());
			}
		}

		if(selectedDragController.getSelectedWidgetCount() > 0)
			widgetSelectionListener.onWidgetSelected(null, false);
	}

	/**
	 * Updates the design surface context menu basing on the selected widgets.
	 */
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

		lockWidgetsMenu.setHTML(FormDesignerUtil.createHeaderHTML(images.add(),Context.getLockWidgets() ? LocaleText.get("unLockWidgets") : LocaleText.get("lockWidgets")));
	}

	/**
	 * Gets the design widget wrapper representing the selected page.
	 * 
	 * @return the design widget wrapper for the selected page.
	 */
	protected DesignWidgetWrapper getSelPageDesignWidget(){
		return pageWidgets.get(selectedTabIndex);
	}

	/**
	 * Adds a new widget with a widget selection listener to the selected page.
	 * 
	 * @param widget the widget.
	 * @param select set to true to automatically select the new widget.
	 * @param widgetSelectionListener the widget selection listener.
	 * @return the newly added widget.
	 */
	protected DesignWidgetWrapper addNewWidget(Widget widget, boolean select, WidgetSelectionListener widgetSelectionListener){
		currentWidgetSelectionListener = widgetSelectionListener;
		return addNewWidget(widget,select);
	}

	/**
	 * Adds a new widget to the currently selected page.
	 * 
	 * @param widget the widget to add.
	 * @param select set to true to automatically select the widget.
	 * @return the new widget.
	 */
	protected DesignWidgetWrapper addNewWidget(Widget widget, boolean select){
		stopLabelEdit(false);

		DesignWidgetWrapper wrapper = new DesignWidgetWrapper(widget,widgetPopup,currentWidgetSelectionListener);

		if(widget instanceof Label || widget instanceof TextBox || widget instanceof ListBox ||
				widget instanceof TextArea || widget instanceof Hyperlink || 
				widget instanceof CheckBox || widget instanceof RadioButton ||
				widget instanceof DateTimeWidget || widget instanceof Button){
			
			wrapper.setFontFamily(FormUtil.getDefaultFontFamily());
			wrapper.setFontSize(FormUtil.getDefaultFontSize());
			
			if(widget instanceof DateTimeWidget){
				((DateTimeWidget)widget).setFontFamily(FormUtil.getDefaultFontFamily());
				((DateTimeWidget)widget).setFontSize(FormUtil.getDefaultFontSize());
			}
		}

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
			onWidgetSelected(wrapper,false);
		}
		return wrapper;
	}

	/**
	 * Adds a new label widget to the selected page.
	 * 
	 * @param text the label text.
	 * @param select set to true if you want to automatically select the widget.
	 * @return the new label widget.
	 */
	protected DesignWidgetWrapper addNewLabel(String text, boolean select){
		if(text == null)
			text = LocaleText.get("label");
		Label label = new Label(text);

		DesignWidgetWrapper wrapper = addNewWidget(label,select);		
		wrapper.setFontFamily(FormUtil.getDefaultFontFamily());
		wrapper.setFontSize(FormUtil.getDefaultFontSize());
		return wrapper;
	}

	/**
	 * Adds a new audio or video widget.
	 * 
	 * @param text the display text for the widget.
	 * @param select set to true to automatically select the new widget.
	 * @return the newly added widget.
	 */
	protected DesignWidgetWrapper addNewVideoAudio(String text, boolean select){
		if(text == null)
			text = LocaleText.get("clickToPlay");
		Hyperlink link = new Hyperlink(text,"");

		DesignWidgetWrapper wrapper = addNewWidget(link,select);
		wrapper.setFontFamily(FormUtil.getDefaultFontFamily());
		wrapper.setFontSize(FormUtil.getDefaultFontSize());
		return wrapper;
	}

	/**
	 * Adds a new server search widget to the selected page.
	 * 
	 * @param text the display text for the widget.
	 * @param select set to true to automatically select the new widget.
	 * @return the newly added widget.
	 */
	protected DesignWidgetWrapper addNewServerSearch(String text, boolean select){
		if(text == null)
			text = LocaleText.get("noSelection");
		Label label = new Label(text);

		DesignWidgetWrapper wrapper = addNewWidget(label,select);
		wrapper.setFontFamily(FormUtil.getDefaultFontFamily());
		wrapper.setFontSize(FormUtil.getDefaultFontSize());
		return wrapper;
	}

	/**
	 * Adds a new picture widget to the selected page.
	 * 
	 * @param select set to true to automatically select the new widget.s
	 * @return the newly added widget.
	 */
	protected DesignWidgetWrapper addNewPicture(boolean select){
		Image image = FormUtil.createImage(images.picture());
		DOM.setStyleAttribute(image.getElement(), "height","155"+PurcConstants.UNITS);
		DOM.setStyleAttribute(image.getElement(), "width","185"+PurcConstants.UNITS);
		return addNewWidget(image,select);
	}

	/**
	 * Adds a new TextBox to the selected page.
	 * 
	 * @param select set to true to automatically select the new widget.
	 * @return the newly added widget.s
	 */
	protected DesignWidgetWrapper addNewTextBox(boolean select){
		TextBoxWidget tb = new TextBoxWidget();
		DOM.setStyleAttribute(tb.getElement(), "height","25"+PurcConstants.UNITS);
		DOM.setStyleAttribute(tb.getElement(), "width","200"+PurcConstants.UNITS);
		return addNewWidget(tb,select);
	}

	/**
	 * Adds a new date picker to the selected page.
	 * 
	 * @param select set to true to automatically select the new widget.
	 * @return the newly added widget.
	 */
	protected DesignWidgetWrapper addNewDatePicker(boolean select){
		DatePickerEx tb = new DatePickerWidget();
		DOM.setStyleAttribute(tb.getElement(), "height","25"+PurcConstants.UNITS);
		DOM.setStyleAttribute(tb.getElement(), "width","200"+PurcConstants.UNITS);
		return addNewWidget(tb,select);
	}

	/**
	 * Adds a new date time widget to the selected page.
	 * 
	 * @param select set to true to automatically select the new widget.
	 * @return the newly added widget.
	 */
	protected DesignWidgetWrapper addNewDateTimeWidget(boolean select){
		DateTimeWidget tb = new DateTimeWidget();
		DOM.setStyleAttribute(tb.getElement(), "height","25"+PurcConstants.UNITS);
		DOM.setStyleAttribute(tb.getElement(), "width","200"+PurcConstants.UNITS);
		return addNewWidget(tb,select);
	}

	/**
	 * Adds a new time widget to the selected page.
	 * 
	 * @param select set to true to automatically select the new widget.
	 * @return the newly added widget.
	 */
	protected DesignWidgetWrapper addNewTimeWidget(boolean select){
		TimeWidget tb = new TimeWidget();
		DOM.setStyleAttribute(tb.getElement(), "height","25"+PurcConstants.UNITS);
		DOM.setStyleAttribute(tb.getElement(), "width","200"+PurcConstants.UNITS);
		return addNewWidget(tb,select);
	}

	/**
	 * Adds a new CheckBox to the selected page.
	 * 
	 * @param select set to true to automatically select the new widget.
	 * @return the newly added widget.
	 */
	protected DesignWidgetWrapper addNewCheckBox(boolean select){
		DesignWidgetWrapper wrapper = addNewWidget(new CheckBox(LocaleText.get("checkBox")),select);		
		wrapper.setFontFamily(FormUtil.getDefaultFontFamily());
		wrapper.setFontSize(FormUtil.getDefaultFontSize());
		return wrapper;
	}

	/**
	 * Adds a new radio button to the selected page.
	 * 
	 * @param select set to true to automatically select the new widget.
	 * @return the newly added widget.
	 */
	protected DesignWidgetWrapper addNewRadioButton(boolean select){
		DesignWidgetWrapper wrapper = addNewWidget(new RadioButtonWidget("RadioButton",LocaleText.get("radioButton")),select);
		wrapper.setFontFamily(FormUtil.getDefaultFontFamily());
		wrapper.setFontSize(FormUtil.getDefaultFontSize());
		return wrapper;
	}

	/**
	 * Adds a new drop downlist box to the selected page.
	 * 
	 * @param select set to true to automatically select the new widget.
	 * @return the newly added widget.
	 */
	protected DesignWidgetWrapper addNewDropdownList(boolean select){
		ListBox lb = new ListBox(false);
		DOM.setStyleAttribute(lb.getElement(), "height","25"+PurcConstants.UNITS);
		DOM.setStyleAttribute(lb.getElement(), "width","200"+PurcConstants.UNITS);
		DesignWidgetWrapper wrapper = addNewWidget(lb,select);
		return wrapper;
	}

	/**
	 * Adds a new text area to the selected page.
	 * 
	 * @param select set to true to automatically select the new widget.
	 * @return the newly added widget.
	 */
	protected DesignWidgetWrapper addNewTextArea(boolean select){
		TextArea ta = new TextArea();
		DOM.setStyleAttribute(ta.getElement(), "height","60"+PurcConstants.UNITS);
		DOM.setStyleAttribute(ta.getElement(), "width","200"+PurcConstants.UNITS);
		return addNewWidget(ta,select);
	}

	/**
	 * Adds a new button.
	 * 
	 * @param label the button label or text.
	 * @param binding the widget binding.
	 * @param select set to true to automatically select the new widget.
	 * @return the newly added widget.
	 */
	protected DesignWidgetWrapper addNewButton(String label, String binding, boolean select){
		DesignWidgetWrapper wrapper = addNewWidget(new Button(label),select);
		wrapper.setFontFamily(FormUtil.getDefaultFontFamily());
		wrapper.setFontSize(FormUtil.getDefaultFontSize());
		wrapper.setWidthInt(70);
		wrapper.setHeightInt(30);
		wrapper.setBinding(binding);
		wrapper.setTitle(binding);
		return wrapper;
	}

	/**
	 * Adds a new submit button.
	 * 
	 * @param select set to true to automatically select the new button.s
	 * @return the new button widget.
	 */
	protected DesignWidgetWrapper addSubmitButton(boolean select){
		return addNewButton(LocaleText.get("submit"),"submit",select);
	}

	/**
	 * Adds a new cancel button.
	 * 
	 * @param select set to true to automatically select the new widget.
	 * @return the new button.
	 */
	protected DesignWidgetWrapper addCancelButton(boolean select){
		return addNewButton(LocaleText.get("cancel"),"cancel",select);
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

	public DesignWidgetWrapper onDrop(Widget widget,int x, int y){
		if(!(widget instanceof PaletteWidget))
			return null;

		this.x = x;
		this.y = y;

		String text = ((PaletteWidget)widget).getName();

		DesignWidgetWrapper retWidget = null;

		if(text.equals(LocaleText.get("label")))
			retWidget = addNewLabel(LocaleText.get("label"),true);
		else if(text.equals(LocaleText.get("textBox")))
			retWidget = addNewTextBox(true);
		else if(text.equals(LocaleText.get("checkBox")))
			retWidget = addNewCheckBox(true);
		else if(text.equals(LocaleText.get("radioButton")))
			retWidget = addNewRadioButton(true);
		else if(text.equals(LocaleText.get("listBox")))
			retWidget = addNewDropdownList(true);
		else if(text.equals(LocaleText.get("textArea")))
			retWidget = addNewTextArea(true);
		else if(text.equals(LocaleText.get("button")))
			retWidget = addNewButton(LocaleText.get("button"),null,true);
		else if(text.equals(LocaleText.get("datePicker")))
			retWidget = addNewDatePicker(true);
		else if(text.equals(LocaleText.get("dateTimeWidget")))
			retWidget = addNewDateTimeWidget(true);
		else if(text.equals(LocaleText.get("timeWidget")))
			retWidget = addNewTimeWidget(true);
		else if(text.equals(LocaleText.get("groupBox")))
			retWidget = addNewGroupBox(true);
		else if(text.equals(LocaleText.get("repeatSection")))
			retWidget = addNewRepeatSection(true);
		else if(text.equals(LocaleText.get("picture")))
			retWidget = addNewPictureSection(null,null,true);
		else if(text.equals(LocaleText.get("videoAudio")))
			retWidget = addNewVideoAudioSection(null,null,true);
		/*else if(text.equals(LocaleText.get("searchServer")))
			retWidget = addNewSearchServerWidget(null,null,true);*/

		if(retWidget != null){
			int height = FormUtil.convertDimensionToInt(getHeight());
			int h = retWidget.getTopInt() + retWidget.getHeightInt();
			if(height < h)
				setHeight(height + (h-height)+10+PurcConstants.UNITS);

			int width = FormUtil.convertDimensionToInt(getWidth());
			int w = retWidget.getLeftInt() + retWidget.getWidthInt();
			if(width < w)
				setWidth(width + (w-width)+10+PurcConstants.UNITS);
		}

		return retWidget;
	}

	/**
	 * Sets up the current page.
	 */
	protected void initPanel(){

		//Create a DragController for each logical area where a set of draggable
		// widgets and drop targets will be allowed to interact with one another.
		selectedDragController = new FormDesignerDragController(selectedPanel, false,this);

		// Positioner is always constrained to the boundary panel
		// Use 'true' to also constrain the draggable or drag proxy to the boundary panel
		//dragController.setBehaviorConstrainedToBoundaryPanel(false);

		// Allow multiple widgets to be selected at once using CTRL-click
		selectedDragController.setBehaviorMultipleSelection(true);

		//Un commenting the line below causes flickering during drag and drop
		//selectedDragController.setBehaviorDragStartSensitivity(1);

		//selectedDragController.setBehaviorCancelDocumentSelections(true);

		// create a DropController for each drop target on which draggable widgets
		// can be dropped
		DropController dropController =  new FormDesignerDropController(selectedPanel,this);

		// Don't forget to register each DropController with a DragController
		selectedDragController.registerDropController(dropController);
		PaletteView.registerDropController(dropController);

		initEditWidget();
	}

	/**
	 * Sets up the inline label editing widget.
	 */
	private void initEditWidget(){
		DOM.setStyleAttribute(txtEdit.getElement(), "borderStyle", "none");
		DOM.setStyleAttribute(txtEdit.getElement(), "fontFamily", FormUtil.getDefaultFontFamily());
		DOM.setStyleAttribute(txtEdit.getElement(), "fontSize", FormUtil.getDefaultFontSize());
		//DOM.setStyleAttribute(txtEdit.getElement(), "opacity", "1");
		txtEdit.setWidth("400"+PurcConstants.UNITS);
		//txtEdit.addStyleName("purcforms-label-editor");
	}

	/**
	 * Stops inplace editing of widget text.
	 * 
	 * @param select a flag to determine whether to select the stoped edit widget.
	 */
	protected void stopLabelEdit(boolean select){
		if(editWidget != null){
			if(selectedPanel.getWidgetIndex(editWidget) < 0){
				editWidget = null;
				return;
			}

			DesignWidgetWrapper designGroupWidgetWrapper = null;
			if(editWidget.getWrappedWidget() instanceof DesignGroupWidget){
				//Stop header label editing
				DesignWidgetWrapper headerLabel = ((DesignGroupWidget)editWidget.getWrappedWidget()).getHeaderLabel();
				if(headerLabel != null){
					designGroupWidgetWrapper = editWidget;
					editWidget = headerLabel;
				}
			}

			editWidget.stopEditMode();

			String text = txtEdit.getText();
			if(text.trim().length() > 0)
				editWidget.setText(text);

			//selectedPanel.remove(editWidget);
			//selectedPanel.add(editWidget);
			//selectedPanel.setWidgetPosition(editWidget, editWidget.getLeftInt(), editWidget.getTopInt());

			//if(this instanceof DesignSurfaceView){

			if(designGroupWidgetWrapper == null){
				selectedPanel.setWidgetPosition(editWidget, editWidget.getLeftInt(), editWidget.getTopInt());
				selectedDragController.makeDraggable(editWidget);
			}
			else
				selectedDragController.makeDraggable(designGroupWidgetWrapper,editWidget);

			if(designGroupWidgetWrapper != null)
				editWidget = designGroupWidgetWrapper;

			if(select){
				selectedDragController.selectWidget(editWidget);
				widgetSelectionListener.onWidgetSelected(editWidget,false);
			}

			/*}
			else{
				DesignSurfaceView surface = (DesignSurfaceView)getParent().getParent().getParent().getParent().getParent().getParent().getParent();
				surface.stopHeaderLabelEdit(editWidget);
			}*/
			editWidget = null;
		}
	}

	@Override
	public void onBrowserEvent(Event event) {

		switch (DOM.eventGetType(event)) {
		case Event.ONMOUSEDOWN:  

			mouseMoved = false;
			x = event.getClientX();
			y = event.getClientY();

			if(editWidget != null){
				if(editWidget.getWrappedWidgetEx().getElement() == event.getTarget())
					return;
				handleStopLabelEditing(false);
			}

			if( (event.getButton() & Event.BUTTON_RIGHT) != 0){
				updatePopup();

				int ypos = event.getClientY();
				if(Window.getClientHeight() - ypos < 220)
					ypos = event.getClientY() - 220;

				int xpos = event.getClientX();
				if(Window.getClientWidth() - xpos < 170)
					xpos = event.getClientX() - 170;

				popup.setPopupPosition(xpos, ypos);
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
								widgetSelectionListener.onWidgetSelected(getSelPageDesignWidget(),event.getCtrlKey());
								return;
							}
						}catch(Exception ex){}
					}
				}

				if(widgetSelectionListener != null){
					if(this instanceof DesignGroupWidget)
						widgetSelectionListener.onWidgetSelected((DesignWidgetWrapper)this.getParent().getParent(),event.getCtrlKey());

					//if(!(this instanceof DesignGroupWidget) || (this instanceof DesignGroupWidget && !((DesignWidgetWrapper)this.getParent().getParent()).isRepeated()))
					//	widgetSelectionListener.onWidgetSelected(null);

					widgetSelectionListener.onWidgetSelected(this,event.getCtrlKey());
				}

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
			if(selectionXPos > 0 && mouseMoved)
				selectWidgets(event);
			mouseMoved = false;
			DOM.releaseCapture(getElement()); //Mouse could have been captured in startRubberBand and so release it.
			break;
			/*case Event.ONKEYDOWN: This when not commented out makes me have to press enter twice to edit checkbox labels
			handleKeyDownEvent(event);
			break;*/
		}
	}

	/**
	 * Un selects all selected widgets, if any, in all group boxes on the current page.
	 */
	protected void clearGroupBoxSelection(){
		for(int index = 0; index < selectedPanel.getWidgetCount(); index++){
			Widget wid = selectedPanel.getWidget(index);
			if(!(wid instanceof DesignWidgetWrapper))
				continue;
			if(!(((DesignWidgetWrapper)wid).getWrappedWidget() instanceof DesignGroupWidget))
				continue;
			((DesignGroupWidget)((DesignWidgetWrapper)wid).getWrappedWidget()).clearGroupBoxSelection();

			if(selectedDragController.isWidgetSelected(wid))
				selectedDragController.toggleSelection(wid);
		}
	}

	/**
	 * Un selects all selected widgets, if any, on the current page.
	 */
	protected void clearSelection(){
		selectedDragController.clearSelection();
	}

	/**
	 * Processes key down events for the selected page.
	 * 
	 * @param event the event object.
	 * @return true if the event has been handled, else false.
	 */
	protected boolean handleKeyDownEvent(Event event){
		/*if(isTextBoxFocus(event)){
			if("none".equalsIgnoreCase(event.getTarget().getStyle().getProperty("borderStyle")));
				return true;
		}*/

		boolean ret = false;

		if(isTextBoxFocus(event) && editWidget == null)
			return false;  //could be on widget properties pane.

		if(this.isVisible()){
			int keyCode = event.getKeyCode();
			if(event.getShiftKey() || event.getCtrlKey())
				ret = resizeWidgets(event);
			else if(keyCode == KeyCodes.KEY_LEFT)
				ret = moveWidgets(MOVE_LEFT);
			else if(keyCode == KeyCodes.KEY_RIGHT)
				ret = moveWidgets(MOVE_RIGHT);
			else if(keyCode == KeyCodes.KEY_UP)
				ret = moveWidgets(MOVE_UP);
			else if(keyCode == KeyCodes.KEY_DOWN)
				ret = moveWidgets(MOVE_DOWN);  

			if(event.getCtrlKey() && (keyCode == 'A' || keyCode == 'a')){
				if(!isTextAreaFocus(event)){ //TODO This works only when the textarea is clicked to get focus. Need to make it work even before clicking the text area (as long as it is visible)
					//As for now, Ctrl+A selects all widgets on the design surface's current tab
					//If one wants to select all widgets within a DesignGroupWidget, they should
					//right click and select all
					if(isTextBoxFocus(event)){
						if(this instanceof DesignGroupWidget && ((DesignGroupWidget)this).editWidget != null)
							((DesignGroupWidget)this).txtEdit.selectAll();

						DOM.eventPreventDefault(event);
					}
					else{
						if(this instanceof DesignSurfaceView)
							((DesignSurfaceView)this).selectAll();
						else if(widgetSelectionListener instanceof DesignSurfaceView)
							((DesignSurfaceView)widgetSelectionListener).selectAll();

						DOM.eventPreventDefault(event);
					}
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
			else if(keyCode == KeyCodes.KEY_DELETE && !isTextBoxFocus(event)){
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

			if(!ret){
				if(!isTextBoxFocus(event) || (editWidget != null /*&& event.getCurrentTarget() == editWidget.getElement()*/)){
					boolean ret1 = false;
					if(keyCode != KeyCodes.KEY_DELETE && editWidget == null)
						ret1 = handleStartLabelEditing(event);
					else if(keyCode == KeyCodes.KEY_ENTER && editWidget != null)
						handleStopLabelEditing(true);
					else if(keyCode == KeyCodes.KEY_ESCAPE && editWidget != null){
						txtEdit.setText(editWidget.getText());
						handleStopLabelEditing(true);
					}

					if(ret1) //If handle start label edit is handled, need to signal such that others are not called for the same.
						ret = true;
				}
			}
		}

		return ret;
	}

	/**
	 * Gets the background color for the selected page.
	 * 
	 * @return the html color value.
	 */
	public String getBackgroundColor(){
		return DOM.getStyleAttribute(selectedPanel.getElement(), "backgroundColor");
	}

	/**
	 * Gets the widgth for the selected page.
	 * 
	 * @return the widget in pixels.
	 */
	public String getWidth(){
		return DOM.getStyleAttribute(selectedPanel.getElement(), "width");
	}

	/**
	 * Gets the height for the selected page.
	 * 
	 * @return the height in pixels.
	 */
	public String getHeight(){
		return DOM.getStyleAttribute(selectedPanel.getElement(), "height");
	}

	/**
	 * Sets the background color of the selected page.
	 * 
	 * @param backgroundColor the background color. This can be any valid html color value.
	 */
	public void setBackgroundColor(String backgroundColor){
		try{
			DOM.setStyleAttribute(selectedPanel.getElement(), "backgroundColor", backgroundColor);
		}catch(Exception ex){}
	}

	/**
	 * Sets the width in pixels of the selected page.
	 */
	public void setWidth(String width){
		try{
			DOM.setStyleAttribute(selectedPanel.getElement(), "width", width);
		}catch(Exception ex){}
	}

	/**
	 * Sets the height in pixels of the selected page.
	 */
	public void setHeight(String height){
		try{
			DOM.setStyleAttribute(selectedPanel.getElement(), "height", height);
		}catch(Exception ex){}
	}

	/**
	 * Adds a new set of radio buttons.
	 * 
	 * @param questionDef the question that we are to add the radio buttons for.
	 * @param vertically set to true to add the radio buttons slopping vertically downwards instead of horizontally.
	 * @return this will always be null.
	 */
	protected DesignWidgetWrapper addNewRadioButtonSet(QuestionDef questionDef, boolean vertically){
		List<OptionDef> options = questionDef.getOptions();

		if(questionDef.getDataType() == QuestionDef.QTN_TYPE_BOOLEAN){
			options = new ArrayList<OptionDef>();
			options.add(new OptionDef(1,QuestionDef.TRUE_DISPLAY_VALUE,QuestionDef.TRUE_VALUE,questionDef));
			options.add(new OptionDef(1,QuestionDef.FALSE_DISPLAY_VALUE,QuestionDef.FALSE_VALUE,questionDef));
		}

		for(int i=0; i<options.size(); i++){
			/*if(i != 0){
				if(vertically)
					y += 40;
				else
					x += 40;
			}*/

			OptionDef optionDef = (OptionDef)options.get(i);
			DesignWidgetWrapper wrapper = addNewWidget(new RadioButtonWidget(optionDef.getText()),false);
			wrapper.setFontFamily(FormUtil.getDefaultFontFamily());
			wrapper.setFontSize(FormUtil.getDefaultFontSize());
			wrapper.setBinding(optionDef.getVariableName());
			wrapper.setParentBinding(questionDef.getVariableName());
			wrapper.setText(optionDef.getText());
			wrapper.setTitle(optionDef.getText());

			if(vertically)
				y += 40;
			else
				x += (optionDef.getText().length() * 14);
		}

		/*OptionDef optionDef = new OptionDef(0,LocaleText.get("noSelection"),null,questionDef);
		DesignWidgetWrapper wrapper = addNewWidget(new RadioButtonWidget(optionDef.getText()),false);
		wrapper.setFontFamily(FormUtil.getDefaultFontFamily());
		wrapper.setFontSize(FormUtil.getDefaultFontSize());
		wrapper.setParentBinding(questionDef.getVariableName());
		wrapper.setText(optionDef.getText());
		wrapper.setTitle(optionDef.getText());*/

		return null;
	}

	/**
	 * Changes a widget to a different type. For instance single select question types
	 * can have their drop down widget changed to radio buttons.
	 * 
	 * @param vertically set to true to have the widgets vertically, else false to have them horizontally.
	 */
	protected void changeWidget(boolean vertically){
		if(selectedDragController.getSelectedWidgetCount() != 1)
			return;

		DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedDragController.getSelectedWidgetAt(0);
		if(!(widget.getWrappedWidget() instanceof ListBox /*|| widget.getWrappedWidget() instanceof TextBox*/))
			return;

		QuestionDef questionDef = widget.getQuestionDef();
		if(questionDef == null)
			return;

		x = widget.getLeftInt() + selectedPanel.getAbsoluteLeft();
		y = widget.getTopInt() + selectedPanel.getAbsoluteTop();

		if(widget.getLayoutNode() != null)
			widget.getLayoutNode().getParentNode().removeChild(widget.getLayoutNode());
		selectedPanel.remove(widget);
		selectedDragController.clearSelection();

		if(widget.getWrappedWidget() instanceof ListBox)
			addNewRadioButtonSet(questionDef,vertically);
		else
			;//addNewSearchServerWidget(questionDef.getVariableName(),questionDef.getText(), true);

		//increase height if the last widget is beyond our current y coordinate.
		int height = FormUtil.convertDimensionToInt(getHeight());
		if((height + getAbsoluteTop()) < y)
			setHeight(y+PurcConstants.UNITS);
	}

	/**
	 * Adds a new serach server widget.
	 * 
	 * @param parentBinding the binding of the question for this widget.
	 * @param text the widget display text.
	 * @param select set to true to automatically select the added widget.,
	 * @return the added widget.
	 */
	/*protected DesignWidgetWrapper addNewSearchServerWidget(String parentBinding, String text, boolean select){
		DesignGroupWidget repeat = new DesignGroupWidget(images,this);
		repeat.addStyleName("getting-started-label2");
		DOM.setStyleAttribute(repeat.getElement(), "height","70"+PurcConstants.UNITS);
		DOM.setStyleAttribute(repeat.getElement(), "width","285"+PurcConstants.UNITS);
		repeat.setWidgetSelectionListener(currentWidgetSelectionListener); //TODO CHECK ????????????????

		DesignWidgetWrapper widget = addNewWidget(repeat,select);
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

		y = 28; //20 + 25;
		x = 5; //45;
		if(selectedPanel.getAbsoluteLeft() > 0)
			x += selectedPanel.getAbsoluteLeft();
		if(selectedPanel.getAbsoluteTop() > 0)
			y += selectedPanel.getAbsoluteTop();
		addNewServerSearch(null,false).setBinding(parentBinding);

		y = 28; //60 + 25;
		x = 10;

		//new
		x = LocaleText.get("noSelection").length() * 10;

		if(selectedPanel.getAbsoluteLeft() > 0)
			x += selectedPanel.getAbsoluteLeft();
		if(selectedPanel.getAbsoluteTop() > 0)
			y += selectedPanel.getAbsoluteTop();

		addNewButton(LocaleText.get("search"),"search",false).setParentBinding(parentBinding);
		//x = 120;

		x = 10;
		x = (LocaleText.get("noSelection").length() * 12) + (LocaleText.get("search").length() * 10);

		if(selectedPanel.getAbsoluteLeft() > 0)
			x += selectedPanel.getAbsoluteLeft();
		addNewButton(LocaleText.get("clear"),"clear",false).setParentBinding(parentBinding);

		selectedDragController.clearSelection();

		selectedDragController = selDragController;
		selectedPanel = absPanel;
		widgetPopup = wdpopup;
		currentWidgetSelectionListener = wgSelectionListener;

		y = oldY;

		//Header label stuff
		widget.setBorderStyle("dashed");
		AbsolutePanel panel = selectedPanel;
		FormDesignerDragController dragController = selectedDragController;

		selectedDragController = widget.getDragController();
		selectedPanel = widget.getPanel();

		y = selectedPanel.getAbsoluteTop();
		x = selectedPanel.getAbsoluteLeft();
		DesignWidgetWrapper headerLabel = addNewLabel(text != null ? text : LocaleText.get("searchServer"), false);
		headerLabel.setBackgroundColor(StyleUtil.COLOR_GROUP_HEADER);
		DOM.setStyleAttribute(headerLabel.getElement(), "width","100%");
		headerLabel.setTextAlign("center");
		selectedDragController.makeNotDraggable(headerLabel);
		headerLabel.setWidth("100%");
		headerLabel.setHeightInt(20);
		headerLabel.setForeColor("white");
		headerLabel.setFontWeight("bold");

		selectedPanel = panel;
		selectedDragController = dragController;

		selectedDragController.makeDraggable(widget,headerLabel);
		repeat.setHeaderLabel(headerLabel);
		//End header label stuff

		y = oldY;

		//Without this, widgets in this box cant use Ctrl + A in edit mode and also
		//edited text is not automatically selected.
		widget.removeStyleName("dragdrop-handle");

		return widget;
	}*/

	/**
	 * Adds a new group box widget.
	 * 
	 * @param select set to true to automatically selecte the newly added widget.
	 * @return the new widget.
	 */
	protected DesignWidgetWrapper addNewGroupBox(boolean select){
		DesignGroupWidget group = new DesignGroupWidget(images,this);
		group.addStyleName("getting-started-label2");
		DOM.setStyleAttribute(group.getElement(), "height","200"+PurcConstants.UNITS);
		DOM.setStyleAttribute(group.getElement(), "width","500"+PurcConstants.UNITS);
		group.setWidgetSelectionListener(currentWidgetSelectionListener); //TODO CHECK ??????????????

		DesignWidgetWrapper widget = addNewWidget(group,select);
		//selectedDragController.makeNotDraggable(widget);


		//Header label stuff
		widget.setBorderStyle("dashed");
		AbsolutePanel panel = selectedPanel;
		FormDesignerDragController dragController = selectedDragController;

		selectedDragController = widget.getDragController();
		selectedPanel = widget.getPanel();

		DesignWidgetWrapper headerLabel = addNewLabel("Header Label", false);
		headerLabel.setBackgroundColor(StyleUtil.COLOR_GROUP_HEADER);
		DOM.setStyleAttribute(headerLabel.getElement(), "width","100%");
		headerLabel.setTextAlign("center");
		selectedDragController.makeNotDraggable(headerLabel);
		headerLabel.setWidth("100%");
		headerLabel.setHeightInt(20);
		headerLabel.setForeColor("white");
		headerLabel.setFontWeight("bold");

		selectedPanel = panel;
		selectedDragController = dragController;

		selectedDragController.makeDraggable(widget,headerLabel);
		group.setHeaderLabel(headerLabel);
		//End header label stuff

		//Without this, widgets in this box cant use Ctrl + A in edit mode and also
		//edited text is not automatically selected.
		widget.removeStyleName("dragdrop-handle");

		return widget;
	}

	/**
	 * Adds a new repeat section widget.
	 * 
	 * @param select set to true to automatically select the newly added widget.
	 * @return the new widget.
	 */
	protected DesignWidgetWrapper addNewRepeatSection(boolean select){
		DesignGroupWidget repeat = new DesignGroupWidget(images,this);
		repeat.addStyleName("getting-started-label2");
		DOM.setStyleAttribute(repeat.getElement(), "height","100"+PurcConstants.UNITS);
		DOM.setStyleAttribute(repeat.getElement(), "width","500"+PurcConstants.UNITS);
		repeat.setWidgetSelectionListener(currentWidgetSelectionListener); //TODO CHECK ????????????????

		DesignWidgetWrapper widget = addNewWidget(repeat,select);
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
		y = 55 + 0; //50;
		x = 10;
		if(selectedPanel.getAbsoluteLeft() > 0)
			x += selectedPanel.getAbsoluteLeft();
		if(selectedPanel.getAbsoluteTop() > 0)
			y += selectedPanel.getAbsoluteTop();

		addNewButton(LocaleText.get("addNew"),"addnew",false);
		/*x = 150;
		if(selectedPanel.getAbsoluteLeft() > 0)
			x += selectedPanel.getAbsoluteLeft();
		addNewButton(LocaleText.get("remove"),"remove",false);*/

		selectedDragController.clearSelection();

		selectedDragController = selDragController;
		selectedPanel = absPanel;
		widgetPopup = wdpopup;
		currentWidgetSelectionListener = wgSelectionListener;

		y = oldY;


		//Group label headers are turned off from repeats because we use tables
		//instead of absolute panels.
		//Header label stuff
		/*widget.setBorderStyle("dashed");
		AbsolutePanel panel = selectedPanel;
		FormDesignerDragController dragController = selectedDragController;

		selectedDragController = widget.getDragController();
		selectedPanel = widget.getPanel();

		oldY = y;
		x = selectedPanel.getAbsoluteLeft();
		y = selectedPanel.getAbsoluteTop();
		DesignWidgetWrapper w = addNewLabel("Header Label", false);
		w.setBackgroundColor(StyleUtil.COLOR_GROUP_HEADER);
		DOM.setStyleAttribute(w.getElement(), "width","100%");
		w.setTextAlign("center");
		//selectedDragController.makeNotDraggable(w);
		w.setWidth("100%");
		w.setForeColor("white");
		w.setFontWeight("bold");

		selectedPanel = panel;
		selectedDragController = dragController;
		y = oldY;*/
		//End header label stuff

		//Without this, widgets in this box cant use Ctrl + A in edit mode and also
		//edited text is not automatically selected.
		widget.removeStyleName("dragdrop-handle");

		return widget;
	}

	/**
	 * Adds a new picture section widget.
	 * 
	 * @param parentBinding the binding of the question for this widget.
	 * @param text the display text for the widget.
	 * @param select set to true if you want to automatically select the new widget.
	 * @return the newly added widget.
	 */
	protected DesignWidgetWrapper addNewPictureSection(String parentBinding, String text, boolean select){
		DesignGroupWidget repeat = new DesignGroupWidget(images,this);
		repeat.addStyleName("getting-started-label2");
		DOM.setStyleAttribute(repeat.getElement(), "height","245"+PurcConstants.UNITS);
		DOM.setStyleAttribute(repeat.getElement(), "width","200"+PurcConstants.UNITS);
		repeat.setWidgetSelectionListener(currentWidgetSelectionListener); //TODO CHECK ????????????????

		DesignWidgetWrapper widget = addNewWidget(repeat,select);
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

		y = 35;
		x = 10;
		if(selectedPanel.getAbsoluteLeft() > 0)
			x += selectedPanel.getAbsoluteLeft();
		if(selectedPanel.getAbsoluteTop() > 0)
			y += selectedPanel.getAbsoluteTop();
		addNewPicture(false).setBinding(parentBinding);

		y = 55 + 120 + 25;
		x = 10;
		if(selectedPanel.getAbsoluteLeft() > 0)
			x += selectedPanel.getAbsoluteLeft();
		if(selectedPanel.getAbsoluteTop() > 0)
			y += selectedPanel.getAbsoluteTop();

		addNewButton(LocaleText.get("browse"),"browse",false).setParentBinding(parentBinding);
		x = 120;
		if(selectedPanel.getAbsoluteLeft() > 0)
			x += selectedPanel.getAbsoluteLeft();
		addNewButton(LocaleText.get("clear"),"clear",false).setParentBinding(parentBinding);

		selectedDragController.clearSelection();

		selectedDragController = selDragController;
		selectedPanel = absPanel;
		widgetPopup = wdpopup;
		currentWidgetSelectionListener = wgSelectionListener;

		y = oldY;

		//Header label stuff
		widget.setBorderStyle("dashed");
		AbsolutePanel panel = selectedPanel;
		FormDesignerDragController dragController = selectedDragController;

		selectedDragController = widget.getDragController();
		selectedPanel = widget.getPanel();

		y = selectedPanel.getAbsoluteTop();
		x = selectedPanel.getAbsoluteLeft();
		DesignWidgetWrapper headerLabel = addNewLabel(text != null ? text : "Picture", false);
		headerLabel.setBackgroundColor(StyleUtil.COLOR_GROUP_HEADER);
		DOM.setStyleAttribute(headerLabel.getElement(), "width","100%");
		headerLabel.setTextAlign("center");
		selectedDragController.makeNotDraggable(headerLabel);
		headerLabel.setWidth("100%");
		headerLabel.setHeightInt(20);
		headerLabel.setForeColor("white");
		headerLabel.setFontWeight("bold");

		selectedPanel = panel;
		selectedDragController = dragController;

		selectedDragController.makeDraggable(widget,headerLabel);
		repeat.setHeaderLabel(headerLabel);
		//End header label stuff

		y = oldY;

		//Without this, widgets in this box cant use Ctrl + A in edit mode and also
		//edited text is not automatically selected.
		widget.removeStyleName("dragdrop-handle");

		return widget;
	}

	/**
	 * Adds a new Audio or Video section widget.
	 * 
	 * @param parentBinding the binding of the question for this widget.
	 * @param text the widget text.
	 * @param select set to true if you want the widget to be automatically selected.
	 * @return the newly added widget.
	 */
	protected DesignWidgetWrapper addNewVideoAudioSection(String parentBinding, String text, boolean select){
		DesignGroupWidget repeat = new DesignGroupWidget(images,this);
		repeat.addStyleName("getting-started-label2");
		DOM.setStyleAttribute(repeat.getElement(), "height","125"+PurcConstants.UNITS);
		DOM.setStyleAttribute(repeat.getElement(), "width","200"+PurcConstants.UNITS);
		repeat.setWidgetSelectionListener(currentWidgetSelectionListener); //TODO CHECK ????????????????

		DesignWidgetWrapper widget = addNewWidget(repeat,select);
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

		y = 20 + 25;
		x = 45;
		if(selectedPanel.getAbsoluteLeft() > 0)
			x += selectedPanel.getAbsoluteLeft();
		if(selectedPanel.getAbsoluteTop() > 0)
			y += selectedPanel.getAbsoluteTop();
		addNewVideoAudio(null,false).setBinding(parentBinding);

		y = 60 + 25;
		x = 10;
		if(selectedPanel.getAbsoluteLeft() > 0)
			x += selectedPanel.getAbsoluteLeft();
		if(selectedPanel.getAbsoluteTop() > 0)
			y += selectedPanel.getAbsoluteTop();

		addNewButton(LocaleText.get("browse"),"browse",false).setParentBinding(parentBinding);
		x = 120;
		if(selectedPanel.getAbsoluteLeft() > 0)
			x += selectedPanel.getAbsoluteLeft();
		addNewButton(LocaleText.get("clear"),"clear",false).setParentBinding(parentBinding);

		selectedDragController.clearSelection();

		selectedDragController = selDragController;
		selectedPanel = absPanel;
		widgetPopup = wdpopup;
		currentWidgetSelectionListener = wgSelectionListener;

		y = oldY;

		//Header label stuff
		widget.setBorderStyle("dashed");
		AbsolutePanel panel = selectedPanel;
		FormDesignerDragController dragController = selectedDragController;

		selectedDragController = widget.getDragController();
		selectedPanel = widget.getPanel();

		y = selectedPanel.getAbsoluteTop();
		x = selectedPanel.getAbsoluteLeft();
		DesignWidgetWrapper headerLabel = addNewLabel(text != null ? text : LocaleText.get("recording"), false);
		headerLabel.setBackgroundColor(StyleUtil.COLOR_GROUP_HEADER);
		DOM.setStyleAttribute(headerLabel.getElement(), "width","100%");
		headerLabel.setTextAlign("center");
		selectedDragController.makeNotDraggable(headerLabel);
		headerLabel.setWidth("100%");
		headerLabel.setHeightInt(20);
		headerLabel.setForeColor("white");
		headerLabel.setFontWeight("bold");

		selectedPanel = panel;
		selectedDragController = dragController;

		selectedDragController.makeDraggable(widget,headerLabel);
		repeat.setHeaderLabel(headerLabel);
		//End header label stuff

		y = oldY;

		//Without this, widgets in this box cant use Ctrl + A in edit mode and also
		//edited text is not automatically selected.
		widget.removeStyleName("dragdrop-handle");

		return widget;
	}


	protected void lockWidgets(){
		Context.setLockWidgets(!Context.getLockWidgets());
	}


	public boolean onWidgetPropertyChanged(byte property, String value){
		if(WidgetPropertySetter.setProperty(property, selectedDragController, value))
			return true;

		int count  = selectedPanel.getWidgetCount();
		for(int index = 0; index < count; index++){
			Widget widget = selectedPanel.getWidget(index);
			if(!(widget instanceof DesignWidgetWrapper))
				continue;

			DesignWidgetWrapper wrapper = (DesignWidgetWrapper)widget;
			if(!(wrapper.getWrappedWidget() instanceof DesignGroupWidget))
				continue;

			if(((DesignGroupWidget)wrapper.getWrappedWidget()).onWidgetPropertyChanged(property, value))
				return true;
		}

		return false;
	}
}
