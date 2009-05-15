package org.purc.purcforms.client.view;

import org.purc.purcforms.client.locale.LocaleText;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FormHandler;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormSubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormSubmitEvent;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author daniel
 *
 */
public class SaveFileDialog extends DialogBox{

	private FormPanel form = new FormPanel();
	private String actionUrl;
	private TextBox txtName;
	private TextArea txtArea;

	public SaveFileDialog(String url, String data, String fileName){
		initWidgets(url,data,fileName);
	}

	public void initWidgets(String url, String data, String fileName){
		actionUrl = url;
		form.setAction(actionUrl);
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setMethod(FormPanel.METHOD_POST);

		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.setSpacing(20);
		form.add(verticalPanel);

		txtArea = new TextArea();
		txtArea.setText(data);
		txtArea.setName("filecontents");
		txtArea.setVisible(false);

		txtName = new TextBox();
		txtName.setText(fileName);
		txtName.setName("filename");
		txtName.setWidth("200px");
		verticalPanel.add(txtName);
		verticalPanel.add(txtArea);

		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		horizontalPanel.add(new Button(LocaleText.get("save"), new ClickListener(){
			public void onClick(Widget sender){
				String fileName = txtName.getText();
				if(fileName != null && fileName.trim().length() > 0){
					String action = actionUrl;
					if(action.contains("?"))
						action += "&";
					else
						action += "?";
					action += "filename="+fileName;
					
					form.setAction(action);
					((VerticalPanel)txtName.getParent()).add(txtArea);
					form.submit();
				}
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
				hide();
				Window.Location.replace(form.getAction());
			}

			public void onSubmit(FormSubmitEvent event){
			}
		});

		setText(LocaleText.get("saveFileAs"));
	}
}
