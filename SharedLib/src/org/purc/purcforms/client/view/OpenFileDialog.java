package org.purc.purcforms.client.view;

import org.purc.purcforms.client.controller.OpenFileDialogEventListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.util.FormUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;

/**
 * Used for letting the user browse for a file to open on the local file system.
 * 
 * @author daniel
 *
 */
public class OpenFileDialog extends DialogBox{

	/** The form used for posting the selected file to. */
	private FormPanel form = new FormPanel();
	
	/** The file upload widget. */
	private FileUpload fileUpload;
	
	/** Listener to the file dialog events. */
	private OpenFileDialogEventListener eventListener;
	
	/** The url to post the file selection to. */
	private String actionUrl;
	
	
	/**
	 * Creates a new instance of the open file dialog box.
	 * 
	 * @param eventListener the listener to the file dialog events.
	 * @param url the url to post the file to.
	 */
	public OpenFileDialog(OpenFileDialogEventListener eventListener, String url){
		this.eventListener = eventListener;
		initWidgets(url);
	}
	
	/**
	 * Sets up the widgets.
	 * 
	 * @param url the url to post to.
	 */
	public void initWidgets(String url){
		actionUrl = url;
		form.setAction(actionUrl);
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setMethod(FormPanel.METHOD_POST);
		
		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.setSpacing(20);
		form.add(verticalPanel);
		
		fileUpload = new FileUpload();
		fileUpload.setName("filecontents");
		verticalPanel.add(fileUpload);
		
		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setWidth("100%");
		horizontalPanel.setHeight("100%");
		horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
		
		Button button = new Button(LocaleText.get("open"), new ClickHandler(){
			public void onClick(ClickEvent event){
				String action = actionUrl;
				if(action.contains("?"))
					action += "&";
				else
					action += "?";
				action += "pathname="+fileUpload.getFilename();
				//action += "&time="+ new java.util.Date().getTime();
				
				form.setAction(action);
				form.submit();
				//hide();
				
				FormUtil.dlg.setText(LocaleText.get("processingMsg"));
				FormUtil.dlg.center();
			}
		});
		
		horizontalPanel.add(button);
		horizontalPanel.setCellHorizontalAlignment(button, HasHorizontalAlignment.ALIGN_LEFT);
		
		button = new Button(LocaleText.get("cancel"), new ClickHandler(){
			public void onClick(ClickEvent event){
				hide();
				FormUtil.dlg.hide();
			}
		});
		
		horizontalPanel.add(button);
		horizontalPanel.setCellHorizontalAlignment(button, HasHorizontalAlignment.ALIGN_RIGHT);
		
		verticalPanel.add(horizontalPanel);
		
		setWidget(form);
		
		form.addSubmitCompleteHandler(new SubmitCompleteHandler(){
			public void onSubmitComplete(FormPanel.SubmitCompleteEvent event){
				eventListener.onSetFileContents(event.getResults());
				hide();
				FormUtil.dlg.hide();
			}
		});
		
		setText(LocaleText.get("openFile"));
	}
}
