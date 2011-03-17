package org.openrosa.client.view;

import org.openrosa.client.PurcConstants;
import org.openrosa.client.locale.LocaleText;
import org.openrosa.client.util.FormUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * This modal dialog box is used to display exceptions to the user.
 * 
 * @author daniel
 *
 */
public class ValidationDialogue extends DialogBox implements ClickHandler {
	
	/** This displays the error message of the exception. */
	private TextArea txtReturnMsg = new TextArea();
	
	
	/**
	 * Creates a new instance of the error dialog box.
	 */
	public ValidationDialogue() {
		
		Button closeButton = new Button(LocaleText.get("close"), this);
		VerticalPanel panel = new VerticalPanel();
		panel.setSpacing(4);
		panel.add(txtReturnMsg);
		panel.add(closeButton);
		panel.setCellHorizontalAlignment(closeButton, VerticalPanel.ALIGN_CENTER);

		setWidget(panel);
		
		txtReturnMsg.setWidth("500"+PurcConstants.UNITS);
		txtReturnMsg.setHeight("200"+PurcConstants.UNITS);
	}

	/**
	 * Called when one clicks the close button.
	 */
	public void onClick(ClickEvent event) {
		hide();
	}

	/**
	 * Sets the exception error message.
	 * 
	 * @param returnMsg the error message.
	 */
	public void setReturnMessage(String returnMsg) {
		txtReturnMsg.setText(returnMsg);
	}
	
}