package org.purc.purcforms.client.view;

import org.purc.purcforms.client.PurcConstants;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.util.FormUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;


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
		verticalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		verticalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		verticalPanel.setSpacing(10);
		form.add(verticalPanel);

		txtArea = new TextArea();
		txtArea.setText(data);
		txtArea.setName("filecontents");
		txtArea.setVisible(false);

		txtName = new TextBox();
		txtName.setText(fileName);
		txtName.setName("filename");
		txtName.setWidth("250"+PurcConstants.UNITS);
		
		verticalPanel.add(txtName);
		verticalPanel.add(txtArea);

		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setWidth("100%");
		horizontalPanel.setHeight("100%");
		horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
		
		Button button = new Button(LocaleText.get("save"), new ClickHandler(){
			public void onClick(ClickEvent event){
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
					//hide();
					
					FormUtil.dlg.setText(LocaleText.get("processingMsg"));
					FormUtil.dlg.center();
				}
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
				hide();
				FormUtil.dlg.hide();
				Window.Location.replace(form.getAction());
			}
		});

		setText(LocaleText.get("saveFileAs"));
	}
	
	
	/**
	 * Displays the dialog box at the center of the browser window.
	 */
	public void center(){
				
		//Let the base GWT implementation of centering take control.
		super.center();
		
		//Some how focus will not get to the name unless when called within
		//a deffered command.
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				txtName.setFocus(true);
			}
		});
	}
}
