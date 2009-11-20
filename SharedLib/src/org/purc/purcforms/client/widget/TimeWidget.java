package org.purc.purcforms.client.widget;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.TextBox;


/**
 * Widget for entering time.
 * 
 * @author daniel
 *
 */
public class TimeWidget extends TextBox{

	/** The input mask. */
	private String MASK = "--:-- --";

	/** Flag to tell whether we are setting selection range or not. */
	private boolean settingSelRange = false;


	/**
	 * Creates a new instance of the time widget.
	 */
	public TimeWidget(){
		setText(MASK);
		addKeyPressHandler();
		setMaxLength(8);
	}


	/**
	 * Adds handler for key board events.
	 */
	private void addKeyPressHandler(){
		addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent event) {
				handleKeyEvent((TextBox)event.getSource(), event.getCharCode());
			}
		});

		addKeyDownHandler(new KeyDownHandler() {
			public void onKeyDown(KeyDownEvent event) {
				//if((event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE))
				//	handleKeyEvent((TextBox)event.getSource(), (char)event.getNativeKeyCode());
			}
		});
	}

	
	/**
	 * Handles a key board event.
	 * 
	 * @param source the sender of the event.
	 * @param keyCode the key code.
	 */
	private void handleKeyEvent(TextBox source, char keyCode){
		String text = source.getText().trim();
		int pos = source.getCursorPos();
		
		if ((!Character.isDigit(keyCode)) && (keyCode != KeyCodes.KEY_TAB)
				&& (keyCode != KeyCodes.KEY_BACKSPACE) && (keyCode != KeyCodes.KEY_LEFT)
				&& (keyCode != KeyCodes.KEY_UP) && (keyCode != KeyCodes.KEY_RIGHT)
				&& (keyCode != KeyCodes.KEY_DOWN)&& (keyCode != KeyCodes.KEY_DELETE)) {

			if(pos == 5 && (keyCode == 'a' || keyCode == 'p' || keyCode == 'A' || keyCode == 'P'))
				pos = 6;
			
			if(pos == 6 && (keyCode == 'a' || keyCode == 'p' || keyCode == 'A' || keyCode == 'P')){
				char[] chars = text.toCharArray();
				chars[pos] = Character.toUpperCase(keyCode);
				chars[pos+1] = 'M';
				setText(new String(chars));
				source.setCursorPos(pos);
			}
			else if(pos == 7 && (keyCode == 'm' || keyCode == 'M')){
				char[] chars = text.toCharArray();
				chars[pos] = Character.toUpperCase(keyCode);
				setText(new String(chars));
				source.setCursorPos(pos+1);
			}
				
			source.cancelKey(); 
		}
		else{
			if(keyCode == KeyCodes.KEY_TAB || keyCode == KeyCodes.KEY_LEFT
					|| keyCode == KeyCodes.KEY_UP || keyCode == KeyCodes.KEY_RIGHT)
				return;
			else if((keyCode == KeyCodes.KEY_BACKSPACE)){
				if(source.getSelectionLength() == 8){
					source.cancelKey();
					setText(MASK);
					source.setCursorPos(0);
					return;
				}
				else if(source.getSelectionLength() > 0){
					source.setCursorPos(pos);
					source.cancelKey();
				}
				else if(pos == 0)
					return;
				
				else if(pos == 3)
					pos = 2;
				else if(pos == 6)
					pos = 5;

				pos -= 1;

				char[] chars = text.toCharArray();
				chars[pos] = '-';
				setText(new String(chars));
				source.setCursorPos(pos);

				source.cancelKey(); 
			}
			else if(pos == 5 || pos == 6 || pos == 7)
				source.cancelKey();
			else{
				if(pos == 2)
					pos = 3;
				else if(pos == 5)
					pos = 6;
				else if(pos == 8)
					pos = 7;
			
				setSelectionRange(pos, 1);
			}
		}
	}


	/**
	 * Sets the widget text.
	 * 
	 * @param text the text to set.
	 */
	public void setText(String text){
		if(text == null || text.trim().length() == 0)
			text = MASK;

		super.setText(text);
	}

	/**
	 * Gets the widget text without the inpiut mask.
	 * 
	 * @return the text.
	 */
	public String getText(){
		String text = super.getText();

		if(!settingSelRange && text.equals(MASK))
			text = "";

		return text;
	}
	
	/**
	 * Gets the widget text including the input mask.
	 * 
	 * @return the widget text.
	 */
	public String getTextWithMask(){
		return super.getText();
	}

	
	/**
	 * @see com.google.gwt.user.client.ui.FocusWidget#setFocus(boolean)
	 */
	public void setFocus(boolean focused){
		super.setFocus(focused);

		if(super.getText().length() == 0)
			setText(MASK);

		setSelectionRange(0, 1);

	}

	
	/**
	 * @see com.google.gwt.user.client.ui.TextBoxBase#setSelectionRange(int, int)
	 */
	public void setSelectionRange(int pos, int length) {
		settingSelRange = true;
		super.setSelectionRange(pos, length);
		settingSelRange = false;
	}
}
