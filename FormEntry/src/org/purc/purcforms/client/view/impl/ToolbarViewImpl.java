package org.purc.purcforms.client.view.impl;

import java.util.List;

import org.purc.purcforms.client.Context;
import org.purc.purcforms.client.FormEntryContext;
import org.purc.purcforms.client.controller.ILocaleListChangeListener;
import org.purc.purcforms.client.listener.FormDefListChangeListener;
import org.purc.purcforms.client.model.KeyValue;
import org.purc.purcforms.client.model.Locale;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;


/**
 * 
 * @author daniel
 *
 */
public class ToolbarViewImpl extends Composite implements FormDefListChangeListener, ILocaleListChangeListener {

	private ListBox lbForms = new ListBox(false);
	private Button btnSearch = new Button("Search");
	private Button btnAddNew = new Button("Add New");
	private Button btnDesign = new Button("Design");
	private Button btnDownloadForms = new Button("Download Forms");
	private Button btnUploadData = new Button("Upload Data");
	private Button btnSettings = new Button("Settings");
	private Button btnDelete = new Button("Delete");
	private ListBox lbLanguages = new ListBox(false);
	

	public ToolbarViewImpl(){

		lbForms.setWidth("300px");

		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setWidth("100%");
		
		Label label = new Label("Select Form");
		horizontalPanel.add(label);
		horizontalPanel.add(lbForms);

		horizontalPanel.add(btnSearch);
		horizontalPanel.add(btnAddNew);
		horizontalPanel.add(btnDesign);
		horizontalPanel.add(btnDelete);
		horizontalPanel.add(btnDownloadForms);
		horizontalPanel.add(btnUploadData);
		horizontalPanel.add(btnSettings);
		//horizontalPanel.add(new Button("Logout"));
		
		label.setWordWrap(false);
		
		btnAddNew.getElement().getStyle().setProperty("whiteSpace","nowrap");
		btnDownloadForms.getElement().getStyle().setProperty("whiteSpace","nowrap");
		btnUploadData.getElement().getStyle().setProperty("whiteSpace","nowrap");
		
		label = new Label(Window.getTitle());
		horizontalPanel.add(label);
		horizontalPanel.setCellWidth(label,"100%");
		horizontalPanel.setCellHorizontalAlignment(label,HasHorizontalAlignment.ALIGN_CENTER);
		
		label = new Label("Language");
		horizontalPanel.add(label);
		horizontalPanel.setCellHorizontalAlignment(label,HasHorizontalAlignment.ALIGN_RIGHT);
		
		horizontalPanel.add(lbLanguages);
		horizontalPanel.setCellHorizontalAlignment(lbLanguages,HasHorizontalAlignment.ALIGN_RIGHT);
		
		addEventHandlers();

		horizontalPanel.setSpacing(10);

		initWidget(horizontalPanel);

		Context.addLocaleListChangeListener(this);
		FormEntryContext.addFormDefListChangeListener(this);

		enableFormDefButtons(false);
	}

	private void enableFormDefButtons(boolean enable){
		btnSearch.setEnabled(enable);
		btnAddNew.setEnabled(enable);
		btnDesign.setEnabled(enable);
		btnUploadData.setEnabled(enable);
		btnDelete.setEnabled(enable);
	}

	private void addEventHandlers(){

		btnDownloadForms.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				FormEntryContext.getFormEntryController().downloadForms();
			}
		});

		btnUploadData.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				FormEntryContext.getFormEntryController().uploadData();
			}
		});

		btnDesign.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				FormEntryContext.getFormEntryController().designForm(lbForms.getValue(lbForms.getSelectedIndex()));
			}
		});

		btnAddNew.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				FormEntryContext.getFormEntryController().enterNewForm(lbForms.getValue(lbForms.getSelectedIndex()));
			}
		});

		btnSearch.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				FormEntryContext.getFormEntryController().listData(lbForms.getValue(lbForms.getSelectedIndex()));
			}
		});

		btnSettings.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				FormEntryContext.getFormEntryController().showSettings();
			}
		});
		
		btnDelete.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				FormEntryContext.getFormEntryController().deleteFormDef();
			}
		});

		lbForms.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				FormEntryContext.setFormDefId(lbForms.getValue(lbForms.getSelectedIndex()));
				FormEntryContext.setFormName(lbForms.getItemText(lbForms.getSelectedIndex()));
			}
		});
	}


	public void onFormDefListChanged(List<KeyValue> formList){
		lbForms.clear();

		if(formList != null){
			for(KeyValue keyValue : formList)
				lbForms.addItem(keyValue.getValue(), keyValue.getKey());

			if(lbForms.getSelectedIndex() >= 0){
				FormEntryContext.setFormDefId(lbForms.getValue(lbForms.getSelectedIndex()));
				FormEntryContext.setFormName(lbForms.getItemText(lbForms.getSelectedIndex()));
			}
		}
		
		enableFormDefButtons(formList != null && formList.size() > 0);
	}
	
	public void onLocaleListChanged(){
		lbLanguages.clear();
		
		List<Locale> locales = Context.getLocales();
		if(locales == null)
			return;
		
		for(Locale locale : locales)
			lbLanguages.addItem(locale.getName(), locale.getKey());
	}
}
