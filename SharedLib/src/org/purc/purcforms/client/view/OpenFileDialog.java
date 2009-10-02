package org.purc.purcforms.client.view;

import org.purc.purcforms.client.controller.OpenFileDialogEventListener;
import org.purc.purcforms.client.locale.LocaleText;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormHandler;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormSubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormSubmitEvent;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

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
		TextBox txt = new TextBox();
		txt.setText(GWT.getModuleBaseURL());
		
		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		horizontalPanel.add(new Button(LocaleText.get("open"), new ClickListener(){
			public void onClick(Widget sender){
				String action = actionUrl;
				if(action.contains("?"))
					action += "&";
				else
					action += "?";
				action += "pathname="+fileUpload.getFilename();
				//action += "&time="+ new java.util.Date().getTime();
				
				form.setAction(action);
				form.submit();
			}
		}));
		
		horizontalPanel.add(new Button(LocaleText.get("cancel"), new ClickListener(){
			public void onClick(Widget sender){
				hide();
			}
		}));
		
		verticalPanel.add(horizontalPanel);
		
		setWidget(form);
		
		form.addFormHandler(new FormHandler(){
			public void onSubmitComplete(FormSubmitCompleteEvent event){
				eventListener.onSetFileContents(event.getResults());
				hide();
			}
			
			public void onSubmit(FormSubmitEvent event){
				
			}
		});
		
		setText(LocaleText.get("openFile"));
	}
}
