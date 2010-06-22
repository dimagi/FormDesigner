package org.purc.purcforms.client.view.impl;

import java.util.ArrayList;
import java.util.List;

import org.purc.purcforms.client.listener.FormListSelectionListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.KeyValue;
import org.purc.purcforms.client.util.FormUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * 
 * @author daniel
 *
 */
public class FormSelectionViewImpl extends DialogBox {

	/** Button to commit changes and close this dialog box. */
	private Button btnOk = new Button(LocaleText.get("ok"));
	
	/** Button to cancel changes, if any, and close this dialog box. */
	private Button btnCancel = new Button(LocaleText.get("cancel"));
	
	/** Main or root widget for this dialog box. */
	private VerticalPanel mainPanel = new VerticalPanel();
	
	private FormListSelectionListener listener;
	private int selectedCount = 0;
	private List<KeyValue> selectedFormList = new ArrayList<KeyValue>();
	private List<KeyValue> formList;
	
	
	public FormSelectionViewImpl(List<KeyValue> formList, FormListSelectionListener listener){
		this.listener = listener;
		
		mainPanel.setSpacing(10);
		setWidget(mainPanel);
		
		setupFormList(formList);
		
		setupOkCancelButtons();
		
		setText("Select forms to download");
	}
	
	/**
	 * Sets up the Ok and Cancel buttons.
	 */
	private void setupOkCancelButtons(){
		btnOk.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				hide();
				listener.onFormListSelected(getSelectedForms());
			}
		});
		
		btnCancel.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				hide();
			}
		});

		HorizontalPanel horzPanel = new HorizontalPanel();
		
		horzPanel.add(btnOk);
		horzPanel.add(btnCancel);
		
		horzPanel.setCellHorizontalAlignment(btnOk, HasAlignment.ALIGN_CENTER);
		horzPanel.setCellHorizontalAlignment(btnCancel, HasAlignment.ALIGN_CENTER);
		FormUtil.maximizeWidget(horzPanel);
		
		btnOk.setEnabled(false);
		
		mainPanel.add(horzPanel);
	}
	
	private List<KeyValue> getSelectedForms(){
		return selectedFormList;
	}
	
	private void setupFormList(List<KeyValue> frmList){
		this.formList = frmList;
		
		ScrollPanel scrollPanel = new ScrollPanel();
		scrollPanel.setHeight("200px");
		scrollPanel.setWidth("400px");
		mainPanel.add(scrollPanel);
		
		VerticalPanel panel = new VerticalPanel();
		scrollPanel.setWidget(panel);
		
		int index = 0;
		for(KeyValue keyValue : formList){
			CheckBox checkbox = new CheckBox();
			checkbox.setText(keyValue.getValue());
			panel.add(checkbox);
			
			checkbox.setTabIndex(index++);
			
			checkbox.addValueChangeHandler(new ValueChangeHandler<Boolean>(){
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					if(event.getValue()){
						selectedFormList.add(formList.get(((CheckBox)event.getSource()).getTabIndex()));
						selectedCount++;
					}
					else{
						selectedFormList.remove(formList.get(((CheckBox)event.getSource()).getTabIndex()));
						selectedCount--;
					}
					
					btnOk.setEnabled(selectedCount > 0);
				}
			});
		}
	}
}
