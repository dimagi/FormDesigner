package org.purc.purcforms.client.widget;

import org.purc.purcforms.client.controller.ItemSelectionListener;
import org.purc.purcforms.client.controller.SortColumnActionListener;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author daniel
 *
 */
public class SortColumnWidget extends Composite implements ItemSelectionListener{

	private static final int HORIZONTAL_SPACING = 5;
	
	private HorizontalPanel horizontalPanel = new HorizontalPanel();
	private SortColumnActionHyperlink actionHyperlink;
	private Label fieldText;
	private SortHyperlink sortHyperlink;
	SortColumnActionListener listener;
	
	public SortColumnWidget(int sortOrder,SortColumnActionListener listener){
		setupWidgets(sortOrder,listener);
	}
	
	private void setupWidgets(int sortOrder,SortColumnActionListener listener){
		this.listener = listener;
		
		actionHyperlink = new SortColumnActionHyperlink("<>",null,listener);
		fieldText = new Label();
		sortHyperlink = new SortHyperlink(SortHyperlink.SORT_TEXT_ASCENDING,null,this,false);
		
		horizontalPanel.setSpacing(HORIZONTAL_SPACING);
		horizontalPanel.add(actionHyperlink);
		horizontalPanel.add(fieldText);
		horizontalPanel.add(sortHyperlink);
		
		sortHyperlink.setSortOrder(sortOrder);
		
		initWidget(horizontalPanel);
	}
	
	public void changeSortOrder(Widget sender, int sortOrder){
		setSortOrder(sortOrder);
	}
	
	public void onItemSelected(Object sender, Object item){
		listener.changeSortOrder(this, ((Integer)item).intValue());
	}
	
	public void onStartItemSelection(Object sender){
		
	}
	
	public void setText(String text){
		fieldText.setText(text);
	}
	
	public void setSortOrder(int sortOrder){
		sortHyperlink.setSortOrder(sortOrder);
	}
}
