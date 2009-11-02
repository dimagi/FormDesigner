package org.purc.purcforms.client.view;

import org.purc.purcforms.client.locale.LocaleText;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * Widget for editing locales.
 * 
 * @author daniel
 *
 */
public class LocalesDialog  extends DialogBox {

	/**
	 * Creates a new instance of the locale dialog box.
	 */
	public LocalesDialog(){
		VerticalPanel panel = new VerticalPanel();
		setWidget(panel);

		Button btn = new Button(LocaleText.get("close"), new ClickHandler() {
			public void onClick(ClickEvent event) {
				hide();
			}
		});

		panel.add(btn);
		panel.setCellHorizontalAlignment(btn, HasAlignment.ALIGN_CENTER);

		setText(LocaleText.get("languages"));
	}

	@Override
	public boolean onKeyDownPreview(char key, int modifiers) {
		// Use the popup's key preview hooks to close the dialog when either
		// enter or escape is pressed.
		switch (key) {
		case KeyCodes.KEY_ESCAPE:
			hide();
			break;
		}

		return true;
	}
}
