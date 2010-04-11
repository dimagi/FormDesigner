package org.openrosa.client.view;

import org.purc.purcforms.client.util.FormUtil;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * 
 * @author daniel
 *
 */
public class XformsTabWidget extends Composite {

	private TextArea txtXforms = new TextArea();
	
	
	public XformsTabWidget(){
		
		VerticalPanel verticalPanel = new VerticalPanel();
		
		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.add(new Button("Validate Xform"));
		horizontalPanel.add(new Button("Update to URL"));
		horizontalPanel.add(new Button("Save As"));
		horizontalPanel.setSpacing(10);
		verticalPanel.add(horizontalPanel);
		
		verticalPanel.add(new Label("Xform XML Source"));
		verticalPanel.add(txtXforms);
		FormUtil.maximizeWidget(txtXforms);
		
		verticalPanel.setSpacing(5);
		
		initWidget(verticalPanel);
		
		FormUtil.maximizeWidget(this);
	}
	
	
	public void adjustHeight(String height){
		txtXforms.setHeight(height);
	}
	
	public void setXform(String xml){
		txtXforms.setText(xml);
	}
	
	public String getXform(){
		return txtXforms.getText();
	}
}
