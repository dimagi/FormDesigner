package org.purc.purcforms.client.view;

import org.purc.purcforms.client.controller.DisplayColumnActionListener;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.QueryBuilderConstants;
import org.purc.purcforms.client.widget.ConditionWidget;
import org.purc.purcforms.client.widget.DisplayColumnWidget;
import org.purc.purcforms.client.widget.SortColumnWidget;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author daniel
 *
 */
public class DisplayFieldsView  extends Composite implements DisplayColumnActionListener{
	
	private static final int HORIZONTAL_SPACING = 5;
	private static final int VERTICAL_SPACING = 5;
	
	private HorizontalPanel horizontalPanel = new HorizontalPanel();
	private VerticalPanel columnPanel = new VerticalPanel();
	private VerticalPanel sortPanel = new VerticalPanel();
	private Hyperlink addColumnLink = new Hyperlink("Click to add new column",null); //LocaleText.get("????")
	
	private FormDef formDef;
	private boolean enabled = true;
	
	
	public DisplayFieldsView(){
		setupWidgets();
	}

	private void setupWidgets(){
		horizontalPanel.setSpacing(HORIZONTAL_SPACING);
		columnPanel.setSpacing(VERTICAL_SPACING);
		sortPanel.setSpacing(VERTICAL_SPACING);
		
		horizontalPanel.add(columnPanel);
		horizontalPanel.add(sortPanel);
		
		columnPanel.add(new Label("Query Columns")); //LocaleText.get("????")
		columnPanel.add(addColumnLink);
		
		sortPanel.add(new Label("Column Sorting Order")); //LocaleText.get("????")
		
		initWidget(horizontalPanel);
		
		addColumnLink.addClickListener(new ClickListener(){
			public void onClick(Widget sender){
				addColumn(sender);
			}
		});
	}
	
	public ConditionWidget addColumn(Widget sender){
		ConditionWidget conditionWidget = null;
		
		if(formDef != null && enabled){
			Widget widget = new DisplayColumnWidget(formDef,null,this);

			columnPanel.remove(addColumnLink);
			columnPanel.add(widget);
			columnPanel.add(addColumnLink);
		}
		
		return conditionWidget;
	}
	
	public void setFormDef(FormDef formDef){
		this.formDef = formDef;
		clearConditions();
		addAddColumnLink();
	}
	
	public void addAddColumnLink(){
		columnPanel.add(addColumnLink);
	}

	private void clearConditions(){
		while(columnPanel.getWidgetCount() > 1)
			columnPanel.remove(columnPanel.getWidget(1));
	}
	
	public void moveColumnUp(Widget sender){
		Widget widget = sender.getParent().getParent();
		int index = columnPanel.getWidgetIndex(widget);
		if(index == 1)
			return;
		columnPanel.remove(widget);
		columnPanel.insert(widget,index-1);
	}
	
	public void moveColumnDown(Widget sender){
		Widget widget = sender.getParent().getParent();
		int index = columnPanel.getWidgetIndex(widget);
		if(index == columnPanel.getWidgetCount() - 2)
			return;
		columnPanel.remove(widget);
		columnPanel.insert(widget,index+1);
	}
	
	public void showSimpleColum(Widget sender){
		
	}
	
	public void showAggregateColumn(Widget sender){
		
	}
	
	public void deleteColumn(Widget sender){
		columnPanel.remove(sender.getParent().getParent());
	}
	
	public void changeSortOrder(Widget sender, int sortOrder){
		if(sortOrder != QueryBuilderConstants.SORT_NULL)
			sortPanel.add(new SortColumnWidget((DisplayColumnWidget)sender));
	}
}
