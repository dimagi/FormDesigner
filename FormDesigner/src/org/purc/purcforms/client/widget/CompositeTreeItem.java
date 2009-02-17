package org.purc.purcforms.client.widget;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

public class CompositeTreeItem extends TreeItem {
	   public CompositeTreeItem() {
		      super();
		   }

		   public CompositeTreeItem(String html) {
		      super(new Label(html));
		   }

		   public CompositeTreeItem(Widget widget) {
		      super(widget);
		   }

		   public void setWidget(Widget newWidget) {
		      super.setWidget(newWidget);
		      getWidget().setStyleName("gwt-CompositeTreeItem");
		   }

		   public TreeItem addItem(String itemText) {
		      return super.addItem(new Label(itemText));
		   }

		   public void setSelected(boolean selected) {
		      if (isSelected() == selected) {
		         return;
		      }

		      super.setSelected(selected);

		      if (selected)
		         getWidget().addStyleName("gwt-CompositeTreeItem-selected");
		      else
		         getWidget().removeStyleName("gwt-CompositeTreeItem-selected");
		   }
}
