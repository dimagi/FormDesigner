package org.purc.purcforms.client.view;

import org.purc.purcforms.client.util.FormDesignerUtil;

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
	
	public OpenFileDialog(){
		initWidgets();
	}
	
	public void initWidgets(){
		FormDesignerUtil.maximizeWidget(form);
		form.setAction("/formsaveservlet");
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setMethod(FormPanel.METHOD_POST);
		
		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.setSpacing(20);
		form.add(verticalPanel);
		
		FileUpload fileUpload = new FileUpload();
		fileUpload.setName("filecontents");
		verticalPanel.add(fileUpload);
		TextBox txt = new TextBox();
		txt.setText(GWT.getModuleBaseURL());
		//verticalPanel.add(txt);
		
		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		FormDesignerUtil.maximizeWidget(horizontalPanel);
		horizontalPanel.add(new Button("Open", new ClickListener(){
			public void onClick(Widget sender){
				form.submit();
				//hide();
				/*ErrorDialog dlg = new ErrorDialog();
				dlg.setText("testing");
				dlg.setBody(getElement().getInnerHTML());
				//dlg.center();
				Window.alert(getElement().getInnerHTML());*/
			}
		}));
		
		horizontalPanel.add(new Button("Cancel", new ClickListener(){
			public void onClick(Widget sender){
				hide();
			}
		}));
		
		verticalPanel.add(horizontalPanel);
		
		setWidget(form);
		
		form.addFormHandler(new FormHandler(){
			public void onSubmitComplete(FormSubmitCompleteEvent event){
				Window.alert(event.getResults());
			}
			
			public void onSubmit(FormSubmitEvent event){
				
			}
		});
		
		//txt.setText(form.getElement().getInnerHTML());
		//DOM.setStyleAttribute(form.getElement(), "backgroundColor", "#ABCDEF");
		//DOM.setStyleAttribute(verticalPanel.getElement(), "backgroundColor", "#ABCDEF");
	}
}
