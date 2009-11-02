
package org.purc.purcforms.client;

import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.util.FormDesignerUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * This is purcforms about dialog box.
 */
public class AboutDialog extends DialogBox {

	public AboutDialog() {
		// Use this opportunity to set the dialog's caption.
		setText(LocaleText.get("about")+" " + FormDesignerUtil.getTitle());

		// Create a VerticalPanel to contain the 'about' label and the 'OK' button.
		VerticalPanel outer = new VerticalPanel();

		// Create the 'about' text and set a style name so we can style it with CSS.

		HTML text = new HTML(LocaleText.get("aboutMessage"));
		text.setStyleName("formDesigner-AboutText");
		outer.add(text);

		// Create the 'OK' button, along with a listener that hides the dialog
		// when the button is clicked.
		Button btn = new Button(LocaleText.get("close"), new ClickHandler() {
			public void onClick(ClickEvent event) {
				hide();
			}
		});

		outer.add(btn);
		outer.setCellHorizontalAlignment(btn, HasAlignment.ALIGN_CENTER);

		setWidget(outer);
	}

	@Override
	public boolean onKeyDownPreview(char key, int modifiers) {
		// Use the popup's key preview hooks to close the dialog when either
		// enter or escape is pressed.
		switch (key) {
		case KeyCodes.KEY_ENTER:
		case KeyCodes.KEY_ESCAPE:
			hide();
			break;
		}

		return true;
	}
}
