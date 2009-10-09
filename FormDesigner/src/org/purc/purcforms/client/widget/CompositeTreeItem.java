package org.purc.purcforms.client.widget;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

/**
 * Custom widget to wrap around tree items and enable proper selection.
 * 
 * @author daniel
 *
 */
public class CompositeTreeItem extends TreeItem {
	
	/**
	* Creates an empty tree item.
	*/
	public CompositeTreeItem() {
		super();
	}

	/**
	* Constructs a tree item with the given text.
	* 
	* @param text the item's text
	*/
	public CompositeTreeItem(String text) {
		super(new Label(text));
	}

	/**
	 * Constructs a tree item with the given <code>Widget</code>.
	 * 
	 * @param widget the item's widget
	 */
	public CompositeTreeItem(Widget widget) {
		super(widget);
	}

	@Override
	public void setWidget(Widget newWidget) {
		super.setWidget(newWidget);
		getWidget().setStyleName("gwt-CompositeTreeItem");
	}

	@Override
	public TreeItem addItem(String itemText) {
		return super.addItem(new Label(itemText));
	}

	@Override
	public void setSelected(boolean selected) {
		if (isSelected() == selected)
			return;

		super.setSelected(selected);

		if (selected)
			getWidget().addStyleName("gwt-CompositeTreeItem-selected");
		else
			getWidget().removeStyleName("gwt-CompositeTreeItem-selected");
	}
}