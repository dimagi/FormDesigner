package org.purc.purcforms.client.view;

import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;

public class ProgressDialog extends DialogBox{

	private Label label = new Label("Please wait while processing...");
	
	public ProgressDialog(){
		super(false,true);
		
		setWidget(label);
	}
	
	public void setProgressMsg(String message){
		label.setText(message);
	}
}
