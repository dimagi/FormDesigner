package org.purc.purcforms.client.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.purc.purcforms.client.Context;
import org.purc.purcforms.client.PurcConstants;
import org.purc.purcforms.client.widget.DesignGroupWidget;
import org.purc.purcforms.client.widget.DesignWidgetWrapper;
import org.purc.purcforms.client.widget.PaletteWidget;

import com.allen_sauer.gwt.dnd.client.AbstractDragController;
import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.BoundaryDropController;
import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.allen_sauer.gwt.dnd.client.util.CoordinateLocation;
import com.allen_sauer.gwt.dnd.client.util.DOMUtil;
import com.allen_sauer.gwt.dnd.client.util.Location;
import com.allen_sauer.gwt.dnd.client.util.WidgetArea;
import com.allen_sauer.gwt.dnd.client.util.WidgetLocation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class FormDesignerDragController extends AbstractDragController{

	/**
	 * Private implementation class to store widget information while dragging.
	 */
	private static class SavedWidgetInfo {

		/**
		 * The initial draggable index for indexed panel parents.
		 */
		int initialDraggableIndex;

		/**
		 * Initial draggable CSS margin.
		 */
		String initialDraggableMargin;

		/**
		 * Initial draggable parent widget.
		 */
		Widget initialDraggableParent;

		/**
		 * Initial location for absolute panel parents.
		 */
		Location initialDraggableParentLocation;
	}

	/**
	 * CSS style name applied to movable panels.
	 */
	private static final String PRIVATE_CSS_MOVABLE_PANEL = "dragdrop-movable-panel";

	/**
	 * CSS style name applied to drag proxies.
	 */
	private static final String PRIVATE_CSS_PROXY = "dragdrop-proxy";

	/**
	 * The implicit boundary drop controller.
	 */
	private final BoundaryDropController boundaryDropController;

	private int boundaryOffsetX;

	private int boundaryOffsetY;

	private boolean dragProxyEnabled = false;

	private final DropControllerCollection dropControllerCollection;

	private final ArrayList<DropController> dropControllerList = new ArrayList<DropController>();

	private int dropTargetClientHeight;

	private int dropTargetClientWidth;

	private Widget movablePanel;

	private HashMap<Widget, SavedWidgetInfo> savedWidgetInfoMap;

	private DragDropListener dragDropListener;

	/**
	 * Create a new pickup-and-move style drag controller. Allows widgets or a
	 * suitable proxy to be temporarily picked up and moved around the specified
	 * boundary panel.
	 * 
	 * <p>
	 * Note: An implicit {@link BoundaryDropController} is created and registered
	 * automatically.
	 * </p>
	 * 
	 * @param boundaryPanel the desired boundary panel or <code>RootPanel.get()</code>
	 *                      if entire document body is to be the boundary
	 * @param allowDroppingOnBoundaryPanel whether or not boundary panel should
	 *            allow dropping
	 */
	public FormDesignerDragController(AbsolutePanel boundaryPanel, boolean allowDroppingOnBoundaryPanel, DragDropListener dragDropListener) {
		super(boundaryPanel);
		assert boundaryPanel != null : "Use 'RootPanel.get()' instead of 'null'.";
		boundaryDropController = newBoundaryDropController(boundaryPanel, allowDroppingOnBoundaryPanel);
		registerDropController(boundaryDropController);
		dropControllerCollection = new DropControllerCollection(dropControllerList);
		this.dragDropListener = dragDropListener;
	}

	private void checkGWTIssue1813(Widget child, AbsolutePanel parent) {
		if (!GWT.isScript()) {
			if (child.getElement().getOffsetParent() != parent.getElement()) {
				DOMUtil.reportFatalAndThrowRuntimeException("The boundary panel for this drag controller does not appear to have"
						+ " 'position: relative' CSS applied to it."
						+ " This may be due to custom CSS in your application, although this"
						+ " is often caused by using the result of RootPanel.get(\"some-unique-id\") as your boundary"
						+ " panel, as described in GWT issue 1813"
						+ " (http://code.google.com/p/google-web-toolkit/issues/detail?id=1813)."
						+ " Please star / vote for this issue if it has just affected your application."
						+ " You can often remedy this problem by adding one line of code to your application:"
						+ " boundaryPanel.getElement().getStyle().setProperty(\"position\", \"relative\");");
			}
		}
	}

	/*private boolean isResizing(){
		String s = DOM.getStyleAttribute(((DesignWidgetWrapper)context.draggable).getWrappedWidget().getElement(), "cursor");

		if(s.equalsIgnoreCase("w-resize") || s.equalsIgnoreCase("se-resize") ||
				s.equalsIgnoreCase("e-resize") || s.equalsIgnoreCase("sw-resize") ||
				s.equalsIgnoreCase("n-resize") || s.equalsIgnoreCase("ne-resize") ||
				s.equalsIgnoreCase("s-resize") || s.equalsIgnoreCase("nw-resize")){
			return true;
		}

		return false;
	}*/

	@Override
	public void dragEnd() {

		//if(Context.getLockWidgets())
		//	return;

		assert context.finalDropController == null == (context.vetoException != null);
		if (context.vetoException != null) {
			if (!getBehaviorDragProxy())
				restoreSelectedWidgetsLocation();
		} else
			context.dropController.onDrop(context);

		context.dropController.onLeave(context);
		context.dropController = null;

		if (!getBehaviorDragProxy()) {
			restoreSelectedWidgetsStyle();
		}

		movablePanel.removeFromParent();
		movablePanel = null;
		super.dragEnd();

		if(dragDropListener != null)
			dragDropListener.onDragEnd(context.draggable);
	}

	public void dragMove() {

		//if(Context.getLockWidgets())
		//	return;

		int desiredLeft = context.desiredDraggableX - boundaryOffsetX;
		int desiredTop = context.desiredDraggableY - boundaryOffsetY;
		if (getBehaviorConstrainedToBoundaryPanel()) {
			desiredLeft = Math.max(0, Math.min(desiredLeft, dropTargetClientWidth
					- context.draggable.getOffsetWidth()));
			desiredTop = Math.max(0, Math.min(desiredTop, dropTargetClientHeight
					- context.draggable.getOffsetHeight()));
		}


		if(!Context.getLockWidgets()){
			if(context.draggable instanceof DesignWidgetWrapper){
				/*DesignWidgetWrapper wrapper = (DesignWidgetWrapper)context.draggable;

			String s = "";
			if(wrapper.getWrappedWidget() instanceof DesignGroupWidget){
				s = DOM.getStyleAttribute(((DesignGroupWidget)wrapper.getWrappedWidget()).getHeaderLabel().getWrappedWidget().getElement(), "cursor");
				wrapper = ((DesignGroupWidget)wrapper.getWrappedWidget()).getHeaderLabel();
			}*/

				String cursor = DOM.getStyleAttribute(((DesignWidgetWrapper)context.draggable).getWrappedWidget().getElement(), "cursor");

				if("default".equals(cursor) && ((DesignWidgetWrapper)context.draggable).getWrappedWidget() instanceof DesignGroupWidget){
					//cursor = DOM.getStyleAttribute(((DesignGroupWidget)((DesignWidgetWrapper)context.draggable).getWrappedWidget()).getHeaderLabel().getElement(), "cursor");
					Event event = DOM.eventGetCurrentEvent();
					//cursor = ((DesignGroupWidget)((DesignWidgetWrapper)context.draggable).getWrappedWidget()).getHeaderLabel().getDesignCursor(event.getClientX(),event.getClientY(),3);
				}

				//Event event = DOM.eventGetCurrentEvent();
				//String cursor = ((DesignWidgetWrapper)context.draggable).getDesignCursor(event.getClientX(),event.getClientY(),3);

				if(cursor.equalsIgnoreCase("w-resize"))
					incrementWidth(false);
				else if(cursor.equalsIgnoreCase("e-resize"))
					incrementWidth(true);
				else if(cursor.equalsIgnoreCase("n-resize"))
					incrementHeight(false);
				else if(cursor.equalsIgnoreCase("s-resize"))
					incrementHeight(true);
				else if(cursor.equalsIgnoreCase("se-resize")){
					incrementHeight(true);
					incrementWidth(true);
				}
				else if(cursor.equalsIgnoreCase("sw-resize")){
					incrementHeight(true);
					incrementWidth(false);
				}
				else if(cursor.equalsIgnoreCase("ne-resize")){
					incrementHeight(false);
					incrementWidth(true);
				}
				else if(cursor.equalsIgnoreCase("nw-resize")){
					incrementHeight(false);
					incrementWidth(false);
				}
				else /*if(cursor.equalsIgnoreCase("move"))*/{
					//if(!"100%".equals(((DesignWidgetWrapper)context.draggable).getWidth()))
					DOMUtil.fastSetElementPosition(movablePanel.getElement(), desiredLeft, desiredTop);
				}
			}
			else{
				//DOM.setStyleAttribute(movablePanel.getElement(),"cursor","crosshair");
				DOM.setStyleAttribute(movablePanel.getElement(), "cursor", "pointer");
				DOMUtil.fastSetElementPosition(movablePanel.getElement(), desiredLeft, desiredTop);
			}
		}


		DropController newDropController = getIntersectDropController(context.mouseX, context.mouseY);
		if (context.dropController != newDropController) {
			if (context.dropController != null) {
				context.dropController.onLeave(context);
			}
			context.dropController = newDropController;
			if (context.dropController != null) {
				context.dropController.onEnter(context);
			}
		}

		if (context.dropController != null) {
			context.dropController.onMove(context);
		}
	}

	private void incrementWidth(boolean right){
		DesignWidgetWrapper widget = (DesignWidgetWrapper)context.draggable;

		if(right){
			int len = context.mouseX-widget.getLeftInt()-widget.getParent().getAbsoluteLeft();
			widget.setWidthInt(len+3);
		}
		else{
			int oldLeft = widget.getLeftInt();
			int newLeft = context.mouseX-widget.getParent().getAbsoluteLeft();
			int len = oldLeft - newLeft;

			widget.setLeftInt(newLeft-3);
			widget.setWidthInt(Math.abs(widget.getWidthInt()+len+3));
		}
	}

	private void incrementHeight(boolean bottom){
		DesignWidgetWrapper widget = (DesignWidgetWrapper)context.draggable;

		if(bottom){
			int len = (context.mouseY-widget.getTopInt()-widget.getParent().getAbsoluteTop());
			widget.setHeightInt(len+3);
		}
		else{
			int oldLeft = widget.getTopInt();
			int newLeft = context.mouseY-widget.getParent().getAbsoluteTop();
			int len = oldLeft - newLeft;

			widget.setTopInt(newLeft-3);
			widget.setHeightInt(Math.abs(widget.getHeightInt()+len+3));
		}		
	}

	@Override
	public void dragStart() {

		if(context.draggable instanceof DesignWidgetWrapper && "100%".equals(((DesignWidgetWrapper)context.draggable).getWidth())){
			context.draggable = context.draggable;//.getParent().getParent().getParent().getParent();
		}

		//if(Context.getLockWidgets())
		//	return;

		super.dragStart();

		if(dragDropListener != null)
			dragDropListener.onDragStart(context.draggable);

		WidgetLocation currentDraggableLocation = new WidgetLocation(context.draggable,context.boundaryPanel);
		//currentDraggableLocation.
		if (getBehaviorDragProxy()) {
			movablePanel = newDragProxy(context);

			context.boundaryPanel.add(movablePanel, currentDraggableLocation.getLeft(),
					currentDraggableLocation.getTop());
			checkGWTIssue1813(movablePanel, context.boundaryPanel);
		} else {
			saveSelectedWidgetsLocationAndStyle();
			AbsolutePanel container = new AbsolutePanel();
			container.getElement().getStyle().setProperty("overflow", "visible");

			container.setPixelSize(context.draggable.getOffsetWidth(),
					context.draggable.getOffsetHeight());
			context.boundaryPanel.add(container, currentDraggableLocation.getLeft(),
					currentDraggableLocation.getTop());
			checkGWTIssue1813(container, context.boundaryPanel);

			int draggableAbsoluteLeft = context.draggable.getAbsoluteLeft();
			int draggableAbsoluteTop = context.draggable.getAbsoluteTop();
			HashMap<Widget, CoordinateLocation> widgetLocation = new HashMap<Widget, CoordinateLocation>();
			for (Widget widget : context.selectedWidgets) {
				widgetLocation.put(widget, new CoordinateLocation(widget.getAbsoluteLeft(),
						widget.getAbsoluteTop()));
			}

			context.dropController = getIntersectDropController(context.mouseX, context.mouseY);
			if (context.dropController != null) {
				context.dropController.onEnter(context);
			}

			for (Widget widget : context.selectedWidgets) {
				Location location = widgetLocation.get(widget);

				int relativeX = (widget instanceof PaletteWidget) ? (context.mouseX - widget.getAbsoluteLeft()) : location.getLeft()- draggableAbsoluteLeft;
				int relativeY = (widget instanceof PaletteWidget) ? (context.mouseY - widget.getAbsoluteTop()) : location.getTop()- draggableAbsoluteTop;

				if(widget instanceof DesignWidgetWrapper)
					container.add(widget, relativeX, relativeY);
				else
					container.add(new Label("+"), relativeX, relativeY);
			}
			movablePanel = container;
		}
		movablePanel.addStyleName(PRIVATE_CSS_MOVABLE_PANEL);

		// one time calculation of boundary panel location for efficiency during
		// dragging
		Location widgetLocation = new WidgetLocation(context.boundaryPanel, null);
		boundaryOffsetX = widgetLocation.getLeft()
		+ DOMUtil.getBorderLeft(context.boundaryPanel.getElement());
		boundaryOffsetY = widgetLocation.getTop()
		+ DOMUtil.getBorderTop(context.boundaryPanel.getElement());

		dropTargetClientWidth = DOMUtil.getClientWidth(context.boundaryPanel.getElement()); //TODO ?????????????????????????
		dropTargetClientHeight = DOMUtil.getClientHeight(context.boundaryPanel.getElement()); //TODO ?????????????????????????
	}

	/**
	 * Whether or not dropping on the boundary panel is permitted.
	 * 
	 * @return <code>true</code> if dropping on the boundary panel is allowed
	 */
	public boolean getBehaviorBoundaryPanelDrop() {
		return boundaryDropController.getBehaviorBoundaryPanelDrop();
	}

	/**
	 * Determine whether or not this controller automatically creates a drag proxy
	 * for each drag operation.
	 * 
	 * @return <code>true</code> if drag proxy behavior is enabled
	 */
	public boolean getBehaviorDragProxy() {
		return dragProxyEnabled;
	}

	private DropController getIntersectDropController(int x, int y) {
		DropController dropController = dropControllerCollection.getIntersectDropController(x, y);
		return dropController != null ? dropController : boundaryDropController;
	}

	/**
	 * Create a new BoundaryDropController to manage our boundary panel as a drop
	 * target. To ensure that draggable widgets can only be dropped on registered
	 * drop targets, set <code>allowDroppingOnBoundaryPanel</code> to <code>false</code>.
	 *
	 * @param boundaryPanel the panel to which our drag-and-drop operations are constrained
	 * @param allowDroppingOnBoundaryPanel whether or not dropping is allowed on the boundary panel
	 * @return the new BoundaryDropController
	 */
	protected BoundaryDropController newBoundaryDropController(AbsolutePanel boundaryPanel,
			boolean allowDroppingOnBoundaryPanel) {
		return new BoundaryDropController(boundaryPanel, allowDroppingOnBoundaryPanel);
	}

	/**
	 * Called by {@link PickupDragController#dragStart()} to allow subclasses to
	 * provide their own drag proxies.
	 * 
	 * @param context the current drag context
	 * @return a new drag proxy
	 */
	protected Widget newDragProxy(DragContext context) {
		AbsolutePanel container = new AbsolutePanel();
		container.getElement().getStyle().setProperty("overflow", "visible");

		WidgetArea draggableArea = new WidgetArea(context.draggable, null);
		for (Widget widget : context.selectedWidgets) {
			WidgetArea widgetArea = new WidgetArea(widget, null);
			Widget proxy = new SimplePanel();
			proxy.setPixelSize(widget.getOffsetWidth(), widget.getOffsetHeight());
			proxy.addStyleName(PRIVATE_CSS_PROXY);
			container.add(proxy, widgetArea.getLeft() - draggableArea.getLeft(), widgetArea.getTop()
					- draggableArea.getTop());
		}

		return container;
	}

	@Override
	public void previewDragEnd() throws VetoDragException {
		assert context.finalDropController == null;
		assert context.vetoException == null;

		//if(Context.getLockWidgets())
		//	return;

		try {
			try {
				// may throw VetoDragException
				context.dropController.onPreviewDrop(context);
				context.finalDropController = context.dropController;
			} finally {
				// may throw VetoDragException
				super.previewDragEnd();
			}
		} catch (VetoDragException ex) {
			context.finalDropController = null;
			throw ex;
		}
	}

	/**
	 * Register a new DropController, representing a new drop target, with this
	 * drag controller.
	 * 
	 * @see #unregisterDropController(DropController)
	 * 
	 * @param dropController the controller to register
	 */
	public void registerDropController(DropController dropController) {
		dropControllerList.add(dropController);
	}

	public DropController getFormDesignerDropController() {
		if(dropControllerList == null)
			return null;

		for(int index = 0; index < dropControllerList.size(); index++){
			DropController dropController = dropControllerList.get(index);
			if(dropController instanceof FormDesignerDropController)
				return dropController;
		}

		return null;
	}

	@Override
	public void resetCache() {
		super.resetCache();
		dropControllerCollection.resetCache(context.boundaryPanel, context); //TODO ???????????????????????????????????
	}

	/**
	 * Restore the selected widgets to their original location.
	 * @see #saveSelectedWidgetsLocationAndStyle()
	 * @see #restoreSelectedWidgetsStyle()
	 */
	protected void restoreSelectedWidgetsLocation() {
		for (Widget widget : context.selectedWidgets) {
			SavedWidgetInfo info = savedWidgetInfoMap.get(widget);

			// TODO simplify after enhancement for issue 1112 provides InsertPanel
			// interface
			// http://code.google.com/p/google-web-toolkit/issues/detail?id=1112
			if (info.initialDraggableParent instanceof AbsolutePanel) {
				((AbsolutePanel) info.initialDraggableParent).add(widget,
						info.initialDraggableParentLocation.getLeft(),
						info.initialDraggableParentLocation.getTop());
			} else if (info.initialDraggableParent instanceof HorizontalPanel) {
				((HorizontalPanel) info.initialDraggableParent).insert(widget, info.initialDraggableIndex);
			} else if (info.initialDraggableParent instanceof VerticalPanel) {
				((VerticalPanel) info.initialDraggableParent).insert(widget, info.initialDraggableIndex);
			} else if (info.initialDraggableParent instanceof FlowPanel) {
				((FlowPanel) info.initialDraggableParent).insert(widget, info.initialDraggableIndex);
			} else if (info.initialDraggableParent instanceof SimplePanel) {
				((SimplePanel) info.initialDraggableParent).setWidget(widget);
			} else {
				throw new RuntimeException("Unable to handle initialDraggableParent "
						+ info.initialDraggableParent.getClass().getName());
			}
		}
	}

	/**
	 * Restore the selected widgets with their original style.
	 * @see #saveSelectedWidgetsLocationAndStyle()
	 * @see #restoreSelectedWidgetsLocation()
	 */
	protected void restoreSelectedWidgetsStyle() {
		for (Widget widget : context.selectedWidgets) {
			SavedWidgetInfo info = savedWidgetInfoMap.get(widget);
			if(info != null)
				widget.getElement().getStyle().setProperty("margin", info.initialDraggableMargin);
		}
	}

	/**
	 * Save the selected widgets' current location in case they much
	 * be restored due to a cancelled drop.
	 * @see #restoreSelectedWidgetsLocation()
	 */
	protected void saveSelectedWidgetsLocationAndStyle() {
		savedWidgetInfoMap = new HashMap<Widget, SavedWidgetInfo>();
		for (Widget widget : context.selectedWidgets) {
			SavedWidgetInfo info = new SavedWidgetInfo();
			info.initialDraggableParent = widget.getParent();

			// TODO simplify after enhancement for issue 1112 provides InsertPanel
			// interface
			// http://code.google.com/p/google-web-toolkit/issues/detail?id=1112
			if (info.initialDraggableParent instanceof AbsolutePanel) {
				info.initialDraggableParentLocation = new WidgetLocation(widget,
						info.initialDraggableParent);
			} else if (info.initialDraggableParent instanceof HorizontalPanel) {
				info.initialDraggableIndex = ((HorizontalPanel) info.initialDraggableParent).getWidgetIndex(widget);
			} else if (info.initialDraggableParent instanceof VerticalPanel) {
				info.initialDraggableIndex = ((VerticalPanel) info.initialDraggableParent).getWidgetIndex(widget);
			} else if (info.initialDraggableParent instanceof FlowPanel) {
				info.initialDraggableIndex = ((FlowPanel) info.initialDraggableParent).getWidgetIndex(widget);
			} else if (info.initialDraggableParent instanceof SimplePanel) {
				// save nothing
			} else {
				throw new RuntimeException(
						"Unable to handle 'initialDraggableParent instanceof "
						+ info.initialDraggableParent.getClass().getName()
						+ "'; Please create your own "
						+ PickupDragController.class.getName()
						+ " and override saveSelectedWidgetsLocationAndStyle(), restoreSelectedWidgetsLocation() and restoreSelectedWidgetsStyle()");
			}

			info.initialDraggableMargin = DOM.getStyleAttribute(widget.getElement(), "margin");
			widget.getElement().getStyle().setProperty("margin", "0"+PurcConstants.UNITS);
			savedWidgetInfoMap.put(widget, info);
		}
	}

	/**
	 * Set whether or not widgets may be dropped anywhere on the boundary panel.
	 * Set to <code>false</code> when you only want explicitly registered drop
	 * controllers to accept drops. Defaults to <code>true</code>.
	 * 
	 * @param allowDroppingOnBoundaryPanel <code>true</code> to allow dropping
	 */
	public void setBehaviorBoundaryPanelDrop(boolean allowDroppingOnBoundaryPanel) {
		boundaryDropController.setBehaviorBoundaryPanelDrop(allowDroppingOnBoundaryPanel);
	}

	/**
	 * Set whether or not this controller should automatically create a drag proxy
	 * for each drag operation.
	 * 
	 * @param dragProxyEnabled <code>true</code> to enable drag proxy behavior
	 */
	public void setBehaviorDragProxy(boolean dragProxyEnabled) {
		this.dragProxyEnabled = dragProxyEnabled;
	}

	/**
	 * Unregister a DropController from this drag controller.
	 * 
	 * @see #registerDropController(DropController)
	 * 
	 * @param dropController the controller to register
	 */
	public void unregisterDropController(DropController dropController) {
		dropControllerList.remove(dropController);
	}

	public void unregisterAllDropControllers() {
		dropControllerList.clear();
	}

	public int getSelectedWidgetCount(){
		return context.selectedWidgets.size();
	}

	public boolean isAnyWidgetSelected(){
		return context.selectedWidgets.size() > 0;
	}

	public Widget getSelectedWidgetAt(int index){
		return context.selectedWidgets.get(index);
	}

	public void selectWidget(Widget draggable) {
		assert draggable != null;
		if (!context.selectedWidgets.contains(draggable)) {
			context.selectedWidgets.add(draggable);
			draggable.addStyleName("dragdrop-selected");
		}
	}

	public List<Widget> getSelectedWidgets(){
		return context.selectedWidgets;
	}

	public boolean isWidgetSelected(Widget widget){
		return context.selectedWidgets.contains(widget);

	}
}
