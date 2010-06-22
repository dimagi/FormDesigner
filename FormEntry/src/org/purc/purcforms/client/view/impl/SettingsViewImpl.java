package org.purc.purcforms.client.view.impl;

import org.purc.purcforms.client.FormEntryContext;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.util.FormUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * 
 * @author daniel
 *
 */
public class SettingsViewImpl extends DialogBox {

	/** Button to commit changes and close this dialog box. */
	private Button btnOk = new Button(LocaleText.get("ok"));
	
	/** Button to cancel changes, if any, and close this dialog box. */
	private Button btnCancel = new Button(LocaleText.get("cancel"));
	
	/** Main or root widget for this dialog box. */
	private VerticalPanel mainPanel = new VerticalPanel();
	
	private TextBox txtFormDownloadUrl = new TextBox();
	private TextBox txtDataUploadUrl = new TextBox();
	
	
	public SettingsViewImpl(){
		mainPanel.setSpacing(10);
		setWidget(mainPanel);
		
		setupTextBoxes();
		setupOkCancelButtons();
		
		setText("Settings");
	}
	
	/**
	 * Sets up the Ok and Cancel buttons.
	 */
	private void setupOkCancelButtons(){
		btnOk.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				save();
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
		
		mainPanel.add(horzPanel);
	}
	
	private void setupTextBoxes(){
		HorizontalPanel horzPanel = new HorizontalPanel();
		horzPanel.setSpacing(5);
		horzPanel.add(new Label("Form Download Url Suffix"));
		horzPanel.add(txtFormDownloadUrl);
		mainPanel.add(horzPanel);
		
		horzPanel = new HorizontalPanel();
		horzPanel.setSpacing(5);
		horzPanel.add(new Label("Data Upload Url Suffix"));
		horzPanel.add(txtDataUploadUrl);
		mainPanel.add(horzPanel);
		
		txtFormDownloadUrl.setText(FormEntryContext.getFormDownloadUrl());
		txtDataUploadUrl.setText(FormEntryContext.getDataUploadUrl());
		
		txtFormDownloadUrl.addKeyUpHandler(new KeyUpHandler(){
			public void onKeyUp(KeyUpEvent event) {
				if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
					txtDataUploadUrl.setFocus(true);
			}
		});
		
		txtDataUploadUrl.addKeyUpHandler(new KeyUpHandler(){
			public void onKeyUp(KeyUpEvent event) {
				if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
					save();
			}
		});
	}
	
	private void save(){
		hide();
		FormEntryContext.getFormEntryController().setFormDownloadUrl(txtFormDownloadUrl.getText());
		FormEntryContext.getFormEntryController().setDataUploadUrl(txtDataUploadUrl.getText());
	}
	
	
	/**
	 * Displays the dialog box at the center of the browser window.
	 */
	public void center(){
		
		//If there is any progress dialog box, close it.
		FormUtil.dlg.hide();
		
		//Let the base GWT implementation of centering take control.
		super.center();
		
		//Some how focus will not get to the user name unless when called within
		//a deffered command.
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				txtFormDownloadUrl.setFocus(true);
			}
		});
	}
}
