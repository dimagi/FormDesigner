package org.openrosa.client.view;

import org.openrosa.client.controller.IFileListener;
import org.openrosa.client.view.FormDesignerWidget.Images;
import org.purc.purcforms.client.util.FormUtil;

import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
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
	private final IFileListener fileListener;
	
	Images images;
	
	public XformsTabWidget(IFileListener fileListenerr){
		this.images = FormDesignerWidget.images;
		this.fileListener = fileListenerr;
		window.setMaximizable(true);  
		window.setHeading("Xform Source");  
				
		ContentPanel cp = new ContentPanel();
		
		ToolBar tb = new ToolBar();
			
		VerticalPanel verticalPanel = new VerticalPanel();
		Button validate,update,saveas,openBut;
		validate = new Button("Validate Xform");
		validate.setIcon(AbstractImagePrototype.create(images.validate()));
		validate.setIconAlign(IconAlign.LEFT);
		validate.disable(); //feature not ready yet
		tb.add(validate);
		
		tb.add(new SeparatorToolItem());
		
		update = new Button("Update to URL");
		update.disable(); //feature not ready yet
		tb.add(update);
		
		tb.add(new SeparatorToolItem());
		
		saveas = new Button("Save As");
		saveas.disable(); //feature not ready yet
		tb.add(saveas);
		
		tb.add(new SeparatorToolItem());
		
		openBut = new Button("Open Pasted Text");
		openBut.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				fileListener.onOpen();
			}
		});
		
		tb.add(openBut);
		
		window.setTopComponent(tb);
		window.setWidth(800);
		window.setMinWidth(500);
		cp.setHeading("XForm Editor Output");
		cp.add(txtXforms);
//		txtXforms.
		FormUtil.maximizeWidget(txtXforms);
		
		/*window.addListener(Events.Resize, new Listener<ComponentEvent>() {
	        public void handleEvent(final ComponentEvent event) {
	                txtXforms.setWidth(/*window.getWidth() +*//* "100%");
	        }});*/
				
		window.add(cp);
		
		initWidget(verticalPanel);
		
		FormUtil.maximizeWidget(this);
	}
	
	
	public void adjustHeight(String height){
		txtXforms.setHeight(height);
	}
	
	public void setXform(String xml){
		txtXforms.setText(xml);
	    txtXforms.setCharacterWidth(800);
	    txtXforms.setVisibleLines(150);
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
	
	public boolean isVisible(){
		return window.isVisible();
	}
}
