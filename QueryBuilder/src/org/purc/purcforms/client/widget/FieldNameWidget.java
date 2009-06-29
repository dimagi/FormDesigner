package org.purc.purcforms.client.widget;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusListenerAdapter;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author daniel
 *
 */
public class FieldNameWidget extends Composite {

	private static final String EMPTY_VALUE = "_____";

	private HorizontalPanel horizontalPanel;
	private TextBox txtValue = new TextBox();
	private Hyperlink valueHyperlink;

	public FieldNameWidget(){
		setupWidgets();
	}

	private void setupWidgets(){
		horizontalPanel = new HorizontalPanel();;

		valueHyperlink = new Hyperlink(EMPTY_VALUE,null);
		horizontalPanel.add(valueHyperlink);

		valueHyperlink.addClickListener(new ClickListener(){
			public void onClick(Widget sender){
				startEdit();
			}
		});

		//Cuases wiered behaviour when editing with between operator
		/*txtValue1.addFocusListener(new FocusListenerAdapter(){
			public void onLostFocus(Widget sender){
				stopEdit();
			}
		});*/

		setupTextListeners();

		initWidget(horizontalPanel);
	}

	private void setupTextListeners(){

		txtValue.addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyPress(Widget sender, char keyCode, int modifiers) {
				if(keyCode == KeyboardListener.KEY_ENTER)
					stopEdit(true);
			}
		});

		txtValue.addFocusListener(new FocusListenerAdapter(){
			public void onLostFocus(Widget sender){
				//stopEdit(true);
			}
		});

		txtValue.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				stopEdit(true);
			}
		});
	}

	private void startEdit(){
		horizontalPanel.remove(valueHyperlink);
		horizontalPanel.add(txtValue);

		if(!valueHyperlink.getText().equals(EMPTY_VALUE))
			txtValue.setText(valueHyperlink.getText());

		txtValue.setFocus(true);
		txtValue.setFocus(true);
		txtValue.selectAll();
	}


	public void stopEdit(boolean updateValue){
		String val = txtValue.getText();		

		if(val.trim().length() == 0)
			val = EMPTY_VALUE;

		valueHyperlink.setText(val);
		horizontalPanel.remove(txtValue);
		horizontalPanel.add(valueHyperlink);
	}


	public String getValue(){
		String val = valueHyperlink.getText();
		if(val.equals(EMPTY_VALUE))
			return null;

		return val;
	}

	public void setValue(String value){
		valueHyperlink.setText(value);
		txtValue.setText(value);
	}
}
