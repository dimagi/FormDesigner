package org.purc.purcforms.client.controller;

import java.util.ArrayList;

import org.purc.purcforms.client.PurcConstants;
import org.purc.purcforms.client.widget.DesignWidgetWrapper;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.drop.AbstractPositioningDropController;
import com.allen_sauer.gwt.dnd.client.util.DOMUtil;
import com.allen_sauer.gwt.dnd.client.util.WidgetLocation;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class FormDesignerDropController extends AbstractPositioningDropController {

	static class Draggable {

		public int desiredX;

		public int desiredY;

		public int relativeX;

		public int relativeY;

		final int offsetHeight;

		final int offsetWidth;

		Widget positioner = null;

		final Widget widget;

		public Draggable(Widget widget) {
			this.widget = widget;
			offsetWidth = widget.getOffsetWidth();
			offsetHeight = widget.getOffsetHeight();
		}
	}

	private static final Label DUMMY_LABEL_IE_QUIRKS_MODE_OFFSET_HEIGHT = new Label("x");

	final ArrayList<Draggable> draggableList = new ArrayList<Draggable>();

	final AbsolutePanel dropTarget;

	int dropTargetClientHeight;

	int dropTargetClientWidth;

	int dropTargetOffsetX;

	int dropTargetOffsetY;
	
	private DragDropListener dragDropListener;
	

	public FormDesignerDropController(AbsolutePanel dropTarget,DragDropListener dragDropListener) {
		super(dropTarget);
		this.dropTarget = dropTarget;
		this.dragDropListener = dragDropListener;
	}

	/**
	 * Programmatically drop a widget on our drop target while obeying the
	 * constraints of this controller.
	 * 
	 * @param widget the widget to be dropped
	 * @param left the desired absolute horizontal location relative to our drop
	 *            target
	 * @param top the desired absolute vertical location relative to our drop
	 *            target
	 */
	public void drop(Widget widget, int left, int top) {
		left = Math.max(0, Math.min(left, dropTarget.getOffsetWidth() - widget.getOffsetWidth()));
		top = Math.max(0, Math.min(top, dropTarget.getOffsetHeight() - widget.getOffsetHeight()));
		dropTarget.add(widget, left, top);
	}

	@Override
	public void onDrop(DragContext context) {
		for (Draggable draggable : draggableList) {
			if(draggable.widget instanceof DesignWidgetWrapper){
				draggable.positioner.removeFromParent();
				dropTarget.add(draggable.widget, draggable.desiredX, draggable.desiredY);
				//dropTarget.add(draggable.widget, draggable.widget.getAbsoluteLeft(), draggable.widget.getAbsoluteTop());
				//dropTarget.add(draggable.widget, context.desiredDraggableX, context.desiredDraggableY);
			}
			else if(dragDropListener != null)
				dragDropListener.onDrop(draggable.widget,context.mouseX,context.mouseY);
				//dragDropListener.onDrop(draggable.widget,context.desiredDraggableX,context.desiredDraggableY);
		}
		super.onDrop(context);
	}

	@Override
	public void onEnter(DragContext context) {
		//super.onEnter(context);
		assert draggableList.size() == 0;

		dropTargetClientWidth = DOMUtil.getClientWidth(dropTarget.getElement());
		dropTargetClientHeight = DOMUtil.getClientHeight(dropTarget.getElement());
		WidgetLocation dropTargetLocation = new WidgetLocation(dropTarget, null);
		dropTargetOffsetX = dropTargetLocation.getLeft()
		+ DOMUtil.getBorderLeft(dropTarget.getElement());
		dropTargetOffsetY = dropTargetLocation.getTop() + DOMUtil.getBorderTop(dropTarget.getElement());

		int draggableAbsoluteLeft = context.draggable.getAbsoluteLeft();
		int draggableAbsoluteTop = context.draggable.getAbsoluteTop();
		for (Widget widget : context.selectedWidgets) {
			Draggable draggable = new Draggable(widget);
			draggable.positioner = makePositioner(widget);
			draggable.relativeX = widget.getAbsoluteLeft() - draggableAbsoluteLeft;
			draggable.relativeY = widget.getAbsoluteTop() - draggableAbsoluteTop;
			draggableList.add(draggable);
		}
	}

	@Override
	public void onLeave(DragContext context) {
		for (Draggable draggable : draggableList) {
			draggable.positioner.removeFromParent();
		}
		draggableList.clear();
		//super.onLeave(context);
	}

	@Override
	public void onMove(DragContext context) {
		super.onMove(context);

		int count = draggableList.size();

		for (Draggable draggable : draggableList) {

			if(count == 1){
				draggable.desiredX = draggable.widget.getAbsoluteLeft() - dropTargetOffsetX + draggable.relativeX;
				draggable.desiredY = draggable.widget.getAbsoluteTop() - dropTargetOffsetY + draggable.relativeY;
			}
			else{
				draggable.desiredX = context.desiredDraggableX - dropTargetOffsetX + draggable.relativeX;
				draggable.desiredY = context.desiredDraggableY - dropTargetOffsetY + draggable.relativeY;
			}

			draggable.desiredX = Math.max(0, Math.min(draggable.desiredX, dropTargetClientWidth
					- draggable.offsetWidth));
			draggable.desiredY = Math.max(0, Math.min(draggable.desiredY, dropTargetClientHeight
					- draggable.offsetHeight));

			dropTarget.add(draggable.positioner, draggable.desiredX, draggable.desiredY);
		}
	}

	Widget makePositioner(Widget reference) {
		// Use two widgets so that setPixelSize() consistently affects dimensions
		// excluding positioner border in quirks and strict modes
		SimplePanel outer = new SimplePanel();
		//outer.addStyleName(CSS_DRAGDROP_POSITIONER); //TODO ????????????????????????????
		outer.getElement().getStyle().setProperty("margin", "0"+PurcConstants.UNITS);

		// place off screen for border calculation
		RootPanel.get().add(outer, -500, -500);

		// Ensure IE quirks mode returns valid outer.offsetHeight, and thus valid
		// DOMUtil.getVerticalBorders(outer)
		outer.setWidget(DUMMY_LABEL_IE_QUIRKS_MODE_OFFSET_HEIGHT);

		SimplePanel inner = new SimplePanel();
		inner.getElement().getStyle().setProperty("margin", "0"+PurcConstants.UNITS);
		inner.getElement().getStyle().setProperty("border", "none");
		int offsetWidth = reference.getOffsetWidth() - DOMUtil.getHorizontalBorders(outer);
		int offsetHeight = reference.getOffsetHeight() - DOMUtil.getVerticalBorders(outer);
		inner.setPixelSize(offsetWidth, offsetHeight);

		outer.setWidget(inner);

		return outer;
	}
}