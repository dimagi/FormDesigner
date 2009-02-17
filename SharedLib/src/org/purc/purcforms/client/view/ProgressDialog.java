package org.purc.purcforms.client.view;

import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;

public class ProgressDialog extends DialogBox{

	public ProgressDialog(){
		super(false,true);
		
		setWidget(new Label("Please wait while processing..."));
	}
}
