package org.purc.purcforms.client.view;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * 
 * @author daniel
 *
 */
public class ValidationRulesView extends Composite{

	public ValidationRulesView(){
		setupWidgets();
	}
	
	private void setupWidgets(){
		VerticalPanel verticalPanel = new VerticalPanel();
		
		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.add(new Label("Choose records where all of the following apply"));
		verticalPanel.add(horizontalPanel);
		
		horizontalPanel = new HorizontalPanel();
		horizontalPanel.add(new CheckBox());
		horizontalPanel.add(new Label("Test Type is equal to"));
		verticalPanel.add(horizontalPanel);
		
		horizontalPanel = new HorizontalPanel();
		horizontalPanel.add(new CheckBox());
		horizontalPanel.add(new Label("Test Type is equal to"));
		verticalPanel.add(horizontalPanel);
		
		initWidget(verticalPanel);
	}
}
