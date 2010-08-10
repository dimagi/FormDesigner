package org.purc.purcforms.client.widget;

import org.purc.purcforms.client.controller.ItemSelectionListener;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;


/**
 * 
 * @author daniel
 *
 */
public class FieldNameWidget extends Composite {

	private static final String EMPTY_VALUE = "_____";

	private HorizontalPanel horizontalPanel;
	private TextBox txtValue = new TextBox();
	private Anchor valueAnchor;
	ItemSelectionListener itemSelectionListener;
	private String defaultValue;

	public FieldNameWidget(ItemSelectionListener itemSelectionListener){
		this.itemSelectionListener = itemSelectionListener;
		setupWidgets();
	}

	private void setupWidgets(){
		horizontalPanel = new HorizontalPanel();;

		valueAnchor = new Anchor(EMPTY_VALUE, "#");
		horizontalPanel.add(valueAnchor);

		valueAnchor.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event){
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

		txtValue.addKeyPressHandler(new KeyPressHandler(){
			public void onKeyPress(KeyPressEvent event) {
				if(event.getCharCode() == KeyCodes.KEY_ENTER)
					stopEdit(true);
			}
		});

		txtValue.addBlurHandler(new BlurHandler(){
			public void onBlur(BlurEvent event){
				//stopEdit(true);
			}
		});

		txtValue.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				stopEdit(true);
			}
		});
	}

	private void startEdit(){
		horizontalPanel.remove(valueAnchor);
		horizontalPanel.add(txtValue);

		if(!valueAnchor.getText().equals(EMPTY_VALUE))
			txtValue.setText(valueAnchor.getText());

		txtValue.setFocus(true);
		txtValue.setFocus(true);
		txtValue.selectAll();
	}


	public void stopEdit(boolean updateValue){
		String val = txtValue.getText();		

		if(val.trim().length() == 0)
			val = EMPTY_VALUE;

		valueAnchor.setText(val);
		horizontalPanel.remove(txtValue);
		horizontalPanel.add(valueAnchor);
		
		itemSelectionListener.onItemSelected(this,val);
	}


	public String getValue(){
		String val = valueAnchor.getText();
		if(val.equals(EMPTY_VALUE))
			return defaultValue;

		return val;
	}

	public void setValue(String value){
		defaultValue = value;
		valueAnchor.setText(value);
		txtValue.setText(value);
	}
}
