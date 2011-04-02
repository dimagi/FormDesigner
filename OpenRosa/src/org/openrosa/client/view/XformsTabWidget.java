package org.openrosa.client.view;

import java.util.HashMap;

import org.openrosa.client.FormDesigner;
import org.openrosa.client.controller.IFileListener;
import org.openrosa.client.view.Toolbar.Images;
import org.openrosa.client.util.FormUtil;
import org.openrosa.client.util.QueryAndFormData;
import org.openrosa.client.util.XEPResponse;

import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.RequestTimeoutException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
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
	private final String VALIDATE_URL = "https://www.commcarehq.org/formtranslate/validate/";
	Images images;
	private static ValidationDialogue valDialog = new ValidationDialogue();
	
//	protected static class JSONXForm extends JavaScriptObject {
//	    public final native String getXForm() /*-{ 
//	        return this.xform;
//	    }-*/;
//	    public final native void setXForm(String value) /*-{
//	        this.xform = value;
//	    }-*/;
//
//	}
	
	private boolean validateXForm(String xml){
		
		RequestBuilder builder = new RequestBuilder(RequestBuilder.POST,VALIDATE_URL);
		try{
//			builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
			HashMap<String, String> requestParams = new HashMap<String, String>();
			requestParams.put("xform", xml);
			String data = QueryAndFormData.buildQueryString(requestParams);	
			if(xml == null || xml.isEmpty()){
				return false;
			}else{
				FormUtil.dlg.center("Validating Form...");
				FormUtil.dlg.show();
			}
			GWT.log("sending form for validation...data = "+data);
			GWT.log("sending form for validation...url = "+VALIDATE_URL);
			builder.setTimeoutMillis(5000);
			Request reponse = builder.sendRequest(data, new RequestCallback(){
				public void onResponseReceived(Request request, Response response){
					GWT.log("Validation response received!");
					int code = response.getStatusCode();
					FormUtil.dlg.hide();
					if(code == Response.SC_OK){
						valDialog.center();
						String output = response.getText();
						valDialog.setReturnMessage(output);
						valDialog.show();
						return;
					}else{
						GWT.log("Reponse code (for validation)="+code);
						GWT.log("Validation Service response headers="+response.getHeadersAsString());
						GWT.log(response.getStatusText()+"|||||"+response.getText()+"|||||"+response);
						com.google.gwt.user.client.Window.alert("Failed to validate form. Response code received: "+code);
					}
					
				}

				public void onError(Request request, Throwable exception){
					com.google.gwt.user.client.Window.alert("sendRequest onError exception....");
					FormUtil.displayException(exception);
					if (exception instanceof RequestTimeoutException) {
					       GWT.log("Request timed out!");
					} 
				
				}
			});
			
			return true;
		}
		catch(RequestException ex){
			FormUtil.displayException(ex);
			return false;
		}
	}
	
	public XformsTabWidget(IFileListener fileListenerr){
		this.images = Toolbar.images;
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
		validate.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce){
				validateXForm(txtXforms.getText());
			}
		});
		validate.disable();
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
	
//	public static final native JSONXForm buildJSONXform(String xform) /*-{
//    	return eval("({'xform'," + xform + "})");
//	}-*/;
	
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
