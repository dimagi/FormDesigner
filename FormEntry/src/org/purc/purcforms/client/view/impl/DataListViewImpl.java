package org.purc.purcforms.client.view.impl;

import java.util.List;

import org.purc.purcforms.client.listener.FormDataDeleteListener;
import org.purc.purcforms.client.listener.FormDataListener;
import org.purc.purcforms.client.model.FormDataHeader;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author daniel
 *
 */
public class DataListViewImpl extends Composite implements FormDataDeleteListener{

	private FlexTable table = new FlexTable();
	private List<FormDataHeader> dataList;
	
	private FormDataListener formDataListener;
	private int deleteIndex;
	private int editIndex;
	
	public DataListViewImpl(FormDataListener formDataListener){
		this.formDataListener = formDataListener;
		
		table.setWidget(0, 0, new Label("Description"));
		table.setWidget(0, 1, new Label("Date Created"));
		table.setWidget(0, 2, new Label("Date Last Changed"));
		table.setWidget(0, 3, new Label("Action"));
		
		table.getCellFormatter().setStyleName(0, 0, "getting-started-label");
		table.getCellFormatter().setStyleName(0, 1, "getting-started-label");
		table.getCellFormatter().setStyleName(0, 2, "getting-started-label");
		table.getCellFormatter().setStyleName(0, 3, "getting-started-label");
		
		table.setStyleName("cw-FlexTable");
		
		initWidget(table);
	}
	
	
	public void setDataList(List<FormDataHeader> dataList){
		this.dataList = dataList;
		
		while(table.getRowCount() > 1)
			table.removeRow(1);
		
		if(dataList == null)
			return;
		
		DateTimeFormat dateFormat = getDateTimeFormat();

		int index = 0;
		for(FormDataHeader data : dataList)
			addFormData(data, ++index, dateFormat);
	}
	
	private void addFormData(FormDataHeader data, int index, DateTimeFormat dateFormat){
		String description = data.getDescription();
		if(description == null || description.trim().length() == 0)
			description = "NO DESCRIPTION";

		table.setWidget(index, 0, new Label(index + ") " + description));
		table.setWidget(index, 1, new Label(dateFormat.format(data.getDateCreated())));
		table.setWidget(index, 2, new Label(dateFormat.format(data.getDateLastChanged())));
		
		
		HorizontalPanel panel = new HorizontalPanel();
		Button btn = new Button("Open");
		panel.add(btn);
		table.setWidget(index,3, panel);
		table.getCellFormatter().setHorizontalAlignment(index, 3, HasHorizontalAlignment.ALIGN_CENTER);

		btn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				openForm(((Button)event.getSource()).getParent());
			}
		});
		
		btn = new Button("Delete");
		panel.add(btn);

		btn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				deleteForm(((Button)event.getSource()).getParent());
			}
		});
	}
	
	public void addFormData(FormDataHeader data){
		dataList.add(data);
		addFormData(data, dataList.size(), getDateTimeFormat());
	}
	
	public void setFormData(FormDataHeader data){
		dataList.set(editIndex, data);
		
		String description = data.getDescription();
		if(description == null || description.trim().length() == 0)
			description = "NO DESCRIPTION";

		editIndex++;
		DateTimeFormat dateFormat = getDateTimeFormat();
		
		table.setWidget(editIndex, 0, new Label(editIndex + ") " + description));
		table.setWidget(editIndex, 1, new Label(dateFormat.format(data.getDateCreated())));
		table.setWidget(editIndex, 2, new Label(dateFormat.format(data.getDateLastChanged())));
	}
	
	private DateTimeFormat getDateTimeFormat(){
		return DateTimeFormat.getFormat("dd-MM-yyyy hh:mm:ss a");
	}
	
	private int getWidgetIndex(Widget widget){
		int rowCount = table.getRowCount();
		for(int row = 0; row < rowCount; row++){
			if(widget == table.getWidget(row, 0) || widget == table.getWidget(row, 3))
				return row;
		}
		return -1;
	}
	
	private void openForm(Widget widget){
		editIndex = getWidgetIndex(widget) - 1;
		formDataListener.onOpenFormData(dataList.get(editIndex).getId());
	}
	
	private void deleteForm(Widget widget){
		deleteIndex = getWidgetIndex(widget) - 1;
		FormDataHeader formDataHeader = dataList.get(deleteIndex);
		
		String desc = formDataHeader.getDescription();
		if(desc == null)
			desc = "";
		if(!Window.confirm("Do you really want to delete all data collected on this form " + desc + " ?"))
			return;
		
		formDataListener.onDeleteFormData(formDataHeader.getId(),this);
		
	}

	public void onFormDataDeleted(String id){
		table.removeRow(deleteIndex+1);
		dataList.remove(deleteIndex);
	}
}
