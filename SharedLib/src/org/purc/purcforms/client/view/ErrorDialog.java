package org.purc.purcforms.client.view;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ErrorDialog extends DialogBox implements ClickListener {
	private HTML body = new HTML("");
	private HTML callStack = new HTML();

	public ErrorDialog() {
		//setStylePrimaryName("FormDesigner-ErrorDialog");
		Button closeButton = new Button("Close", this);
		VerticalPanel panel = new VerticalPanel();
		panel.setSpacing(4);
		panel.add(body);
		panel.add(closeButton);
		panel.setCellHorizontalAlignment(closeButton, VerticalPanel.ALIGN_CENTER);

		DisclosurePanel advanced = new DisclosurePanel("More");
		advanced.setAnimationEnabled(true);
		advanced.setContent(callStack);
		panel.add(advanced);

		setWidget(panel);
	}

	public String getBody() {
		return body.getHTML();
	}

	public void onClick(Widget sender) {
		hide();
	}

	public void setBody(String html) {
		body.setHTML(html);
	}
	
	public void setCallStack(String stack){
		callStack.setHTML(stack);
	}
}