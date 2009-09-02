package org.purc.purcforms.client.view;

import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.util.FormUtil;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;


/**
 * 
 * @author daniel
 *
 */
public class LoginDialog extends DialogBox implements ClickListener {

	private TextBox txtUserName;
	private PasswordTextBox txtPassword;
	private FlexTable table = new FlexTable();
	
	public LoginDialog(){
		setup();
	}
	
	public void onClick(Widget sender) {
		hide();
	}
	
	private void setup(){
		
		setText(LocaleText.get("authenticationPrompt"));
				
		Label label = new Label(LocaleText.get("userName"));
		table.setWidget(1, 0, label);
		
		txtUserName = new TextBox();
		txtUserName.setWidth("50%");
		table.setWidget(1, 1, txtUserName);
		
		txtUserName.addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				if(keyCode == KeyboardListener.KEY_ENTER)
					txtPassword.setFocus(true);
			}
		});
		
		
		label = new Label(LocaleText.get("password"));
		table.setWidget(2, 0, label);
		
		txtPassword = new PasswordTextBox();
		txtPassword.setWidth("50%");
		table.setWidget(2, 1, txtPassword);
		
		txtPassword.addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				if(keyCode == KeyboardListener.KEY_ENTER)
					login();
			}
		});
		
		Button btnLogin = new Button(LocaleText.get("login"), new ClickListener(){
			public void onClick(Widget sender){
				login();
			}
		});
		
		Button btnCancel = new Button(LocaleText.get("cancel"), new ClickListener(){
			public void onClick(Widget sender){
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
	
	private void login(){
		if(!FormUtil.authenticate(txtUserName.getText(), txtPassword.getText())){
			clearUserInfo();
			
			Label label = new Label(LocaleText.get("invalidUser"));
			table.setWidget(4, 0, label);
			DOM.setStyleAttribute(label.getElement(), "color", "red");
			
			txtUserName.setFocus(true);
		}
	}
	
	private void cancel(){
		hide();
	}
	
	public void clearUserInfo(){
		txtUserName.setText(null);
		txtPassword.setText(null);
	}
	
	public void center(){
		FormUtil.dlg.hide();
		
		super.center();
		
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				txtUserName.setFocus(true);
			}
		});
	}
}
