package org.purc.purcforms.client.view;

import org.purc.purcforms.client.controller.IOpenFileDialogEventListener;
import org.purc.purcforms.client.locale.LocaleText;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
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
 * 
 * @author daniel
 *
 */
public class OpenFileDialog extends DialogBox{

	private FormPanel form = new FormPanel();
	private FileUpload fileUpload;
	private IOpenFileDialogEventListener eventListener;
	private String actionUrl;
	
	public OpenFileDialog(IOpenFileDialogEventListener eventListener, String url){
		this.eventListener = eventListener;
		initWidgets(url);
	}
	
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
