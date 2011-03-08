package org.purc.purcforms.client.view;

import org.purc.purcforms.client.PurcConstants;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.util.FormUtil;

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
public class ErrorDialog extends DialogBox implements ClickHandler {
	
	/** This displays the error message of the exception. */
	private TextArea txtErrorMsg = new TextArea();
	
	/** This displays the call stack at the time of the exception. */
	private TextArea callStack = new TextArea();
	
	
	/**
	 * Creates a new instance of the error dialog box.
	 */
	public ErrorDialog() {
		
		Button closeButton = new Button(LocaleText.get("close"), this);
		VerticalPanel panel = new VerticalPanel();
		panel.setSpacing(4);
		panel.add(txtErrorMsg);
		panel.add(closeButton);
		panel.setCellHorizontalAlignment(closeButton, VerticalPanel.ALIGN_CENTER);

		//Setup the disclosure panel to display the call stack.
		DisclosurePanel advanced = new DisclosurePanel(LocaleText.get("more"));
		advanced.setAnimationEnabled(true);
		advanced.setContent(callStack);
		panel.add(advanced);

		setWidget(panel);
		
		txtErrorMsg.setWidth("500"+PurcConstants.UNITS);
		txtErrorMsg.setHeight("200"+PurcConstants.UNITS);
		callStack.setWidth("500"+PurcConstants.UNITS);
		callStack.setHeight("200"+PurcConstants.UNITS);
	}

	/**
	 * Called when one clicks the close button.
	 */
	public void onClick(ClickEvent event) {
		hide();
		
		//TODO Some how when an exception is thrown, the progress dialog may stay on. So needs a fix.
		FormUtil.dlg.hide(); 
	}

	/**
	 * Sets the exception error message.
	 * 
	 * @param errorMsg the error message.
	 */
	public void setErrorMessage(String errorMsg) {
		txtErrorMsg.setText(errorMsg);
	}
	
	/**
	 * Sets the call stack at the time of the exception.
	 * 
	 * @param stack the call stack
	 */
	public void setCallStack(String stack){
		callStack.setText(stack);
	}
}