package org.openrosa.client.view;

import org.openrosa.client.view.FormDesignerWidget.Images;
import org.purc.purcforms.client.util.FormUtil;

import com.extjs.gxt.ui.client.Style.ButtonScale;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;

import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * 
 * @author daniel
 *
 */
public class XformsTabWidget extends Composite {

	private TextArea txtXforms = new TextArea();
	private Window window = new Window();

	
	Images images;
	
	public XformsTabWidget(){
		this.images = FormDesignerWidget.images;
		window.setMaximizable(true);  
		window.setHeading("Xform Source");  
				
		ContentPanel cp = new ContentPanel();
		
		ToolBar tb = new ToolBar();
			
		VerticalPanel verticalPanel = new VerticalPanel();
		Button validate,update,saveas;
		validate = new Button("Validate Xform");
		validate.setIcon(AbstractImagePrototype.create(images.validate()));
		validate.setIconAlign(IconAlign.LEFT);
		tb.add(validate);
		
		tb.add(new SeparatorToolItem());
		
		update = new Button("Update to URL");
		tb.add(update);
		
		tb.add(new SeparatorToolItem());
		
		saveas = new Button("Save As");
		tb.add(saveas);
		
		window.setTopComponent(tb);
		
		cp.setHeading("XForm Editor Output");
		cp.add(txtXforms);
//		txtXforms.
//		FormUtil.maximizeWidget(txtXforms);
				
		window.add(cp);
		
		initWidget(verticalPanel);
		
		FormUtil.maximizeWidget(this);
	}
	
	
	public void adjustHeight(String height){
		txtXforms.setHeight(height);
	}
	
	public void setXform(String xml){
		txtXforms.setText(xml);
	    txtXforms.setCharacterWidth(80);
	    txtXforms.setVisibleLines(15);
	}
	
	public String getXform(){
		return txtXforms.getText();
	}
	
	public void showWindow(){
		window.show();
	}
	
	public void hideWindow(){
		window.hide();
	}
	
	
}
