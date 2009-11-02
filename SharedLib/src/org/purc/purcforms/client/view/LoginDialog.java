package org.purc.purcforms.client.view;

import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.util.FormUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;


/**
 * This dialog box is used to let the user supply a user name and password when
 * attempting to submit data to the server. This dialog box is only displayed when
 * a user opens the data entry form for such a long time that the server session
 * expires and require them to relogin.
 * 
 * @author daniel
 *
 */
public class LoginDialog extends DialogBox {

	/** For capturing the user name. */
	private TextBox txtUserName;
	
	/** For capturing the user password. */
	private PasswordTextBox txtPassword;
	
	/** The widget for organising widgets in a table format. */
	private FlexTable table = new FlexTable();
	
	
	/**
	 * Creates a new instance of the login dialog box.
	 */
	public LoginDialog(){
		setup();
	}
	
	/**
	 * Sets up the login widget.
	 */
	private void setup(){
		
		setText(LocaleText.get("authenticationPrompt"));
				
		Label label = new Label(LocaleText.get("userName"));
		table.setWidget(1, 0, label);
		
		txtUserName = new TextBox();
		txtUserName.setWidth("50%");
		table.setWidget(1, 1, txtUserName);
		
		txtUserName.addKeyUpHandler(new KeyUpHandler(){
			public void onKeyUp(KeyUpEvent event) {
				if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
					txtPassword.setFocus(true);
			}
		});
		
		
		label = new Label(LocaleText.get("password"));
		table.setWidget(2, 0, label);
		
		txtPassword = new PasswordTextBox();
		txtPassword.setWidth("50%");
		table.setWidget(2, 1, txtPassword);
		
		txtPassword.addKeyUpHandler(new KeyUpHandler(){
			public void onKeyUp(KeyUpEvent event) {
				if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
					login();
			}
		});
		
		Button btnLogin = new Button(LocaleText.get("login"), new ClickHandler(){
			public void onClick(ClickEvent event){
				login();
			}
		});
		
		Button btnCancel = new Button(LocaleText.get("cancel"), new ClickHandler(){
			public void onClick(ClickEvent event){
				cancel();
			}
		});
		
		table.setWidget(3, 0, new Label(""));
		table.setWidget(5, 0, btnLogin);
		table.setWidget(5, 1, btnCancel);
		
		FlexCellFormatter formatter = table.getFlexCellFormatter();
		formatter.setColSpan(4, 0, 3);
		formatter.setColSpan(5, 0, 2);
		formatter.setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		formatter.setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		formatter.setHorizontalAlignment(4, 0, HasHorizontalAlignment.ALIGN_CENTER);
		
		formatter.setWidth(1, 1, "50%");
		formatter.setWidth(2, 1, "50%");
		
		FormUtil.maximizeWidget(txtUserName);
		FormUtil.maximizeWidget(txtPassword);
		
		VerticalPanel panel = new VerticalPanel();
		FormUtil.maximizeWidget(panel);
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		panel.add(table);
		
		setWidget(panel);
		
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				txtUserName.setFocus(true);
			}
		});
	}
	
	/**
	 * Called when one selects the OK button.
	 */
	private void login(){
		if(!FormUtil.authenticate(txtUserName.getText(), txtPassword.getText())){
			clearUserInfo();
			
			Label label = new Label(LocaleText.get("invalidUser"));
			table.setWidget(4, 0, label);
			DOM.setStyleAttribute(label.getElement(), "color", "red");
			
			txtUserName.setFocus(true);
		}
	}
	
	/**
	 * Called when the user selects the CANCEL button.
	 */
	private void cancel(){
		hide();
	}
	
	/**
	 * Clears previously entered user name and password.
	 */
	public void clearUserInfo(){
		txtUserName.setText(null);
		txtPassword.setText(null);
	}
	
	/**
	 * Displays the dialog box at the center of the browser window.
	 */
	public void center(){
		
		//If there is any progress dialog box, close it.
		FormUtil.dlg.hide();
		
		//Let the base GWT implementation of centering take control.
		super.center();
		
		//Some how focus will not get to the user name unless when called within
		//a deffered command.
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				txtUserName.setFocus(true);
			}
		});
	}
}
