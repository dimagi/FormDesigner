package org.purc.purcforms.client.view;

import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.util.FormUtil;

import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * Used to show a progress window to the user for slow processing operations.
 * 
 * @author daniel
 *
 */
public class ProgressDialog extends DialogBox{

	/** The label to show the progress or processing message. */
	private Label label = new Label(LocaleText.get("processingMsg"));

	/**
	 * Creates a new instance of the progress dialog.
	 */
	public ProgressDialog(){
		super(false,true);

		HorizontalPanel panel = new HorizontalPanel();
		panel.add(FormUtil.createImage(FormRunnerView.images.loading()));
		panel.add(label);
		
		setWidget(panel);
	}

	/**
	 * Displays the progress dialog box at the center of the browser window
	 * with the default progress message which is "Please wait while processing..."
	 */
	public void center(){

		//Reset the progress message to the default because it may have been
		//been changed with a custom one.
		label.setText(LocaleText.get("processingMsg"));
		
		super.center();
	}

	/**
	 * Displays the progress dialog box at the center of the browser window
	 * and with a custom progress message.
	 * 
	 * @param progressMsg the custom progress message.
	 */
	public void center(String progressMsg){		
		if(progressMsg == null)
			center();
		else
			label.setText(progressMsg);
	}
}
