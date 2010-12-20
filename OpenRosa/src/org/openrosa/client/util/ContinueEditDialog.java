package org.openrosa.client.util;

import org.openrosa.client.view.CenterWidget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;

public class ContinueEditDialog extends DialogBox {

	public ContinueEditDialog() {
		// Set the dialog box's caption.
		setText("Continue editing the form?");

		// Enable animation.
		setAnimationEnabled(true);

		// Enable glass background.
		setGlassEnabled(true);

		// DialogBox is a SimplePanel, so you have to set its widget property to
		// whatever you want its contents to be.
		Button yes = new Button("Yes");
		Button no = new Button("No");
		yes.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				CenterWidget.continueEditing = true;
				ContinueEditDialog.this.hide();
			}
		});
		setWidget(yes);
		
		no.addClickHandler(new ClickHandler() { 
			public void onClick(ClickEvent event) {
				CenterWidget.continueEditing = false;
				ContinueEditDialog.this.hide();
			}
		});
		setWidget(no);
	}

}