package org.openrosa.client.view;

import java.util.Iterator;

import org.openrosa.client.FormDesigner;
import org.openrosa.client.util.FormUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * The form designer widget.
 * 
 * It is composed of a GWT DockPanel as the main widget. This panel contains a 
 * GWT vertical panel which in turn contains a custom Menu widget, custom Tool bar widget, 
 * and a GWT HorizontalSplitPanel. The HorizontalSplitPanel contains a custom LeftPanel widget
 * on the left and a custom CenterPanel widget on the right.
 * When embedded in a GWT application, the embedding application can have custom tool bars and 
 * menu bars, instead of the default ones, which can have commands routed to this widget.
 * 
 * @author daniel
 *
 */
public class FormDesignerWidget extends Composite{

	/**
	 * Instantiate an application-level image bundle. This object will provide
	 * programatic access to all the images needed by widgets.
	 */
	public static Images images;

	/**
	 * An aggragate image bundle that pulls together all the images for this
	 * application into a single bundle.
	 */
	public interface Images extends ClientBundle{
		ImageResource tasksgroup();
		ImageResource filtersgroup();
	}

	private DockPanel dockPanel;
	
	private CenterWidget centerWidget;
	
	private String returnXml;    
	private int returnErrorCode;


	public FormDesignerWidget(boolean showToolbar,boolean showFormAsRoot){
		images = (Images) GWT.create(Images.class);
		initDesigner();  
	}

	/**
	 * Builds the form designer and all its widgets.
	 * 
	 * @param showMenubar set to true to show the menu bar.
	 * @param showToolbar set to true to show the tool bar.
	 */
	private void initDesigner(){
//		dockPanel = new DockPanel();
//
//		VerticalPanel panel = new VerticalPanel();
//
////		panel.add(new FileToolbar(centerWidget));
//		panel.add(centerWidget);
//		
//		panel.setWidth("100%");
//
//		dockPanel.add(panel, DockPanel.CENTER);
//
//		FormUtil.maximizeWidget(dockPanel);
//
//		initWidget(dockPanel);
		centerWidget = new CenterWidget();
		centerWidget.setWidth("100%");
		FormUtil.maximizeWidget(centerWidget);
		initWidget(centerWidget);
		

		DOM.sinkEvents(getElement(),DOM.getEventsSunk(getElement()) | Event.MOUSEEVENTS);
		
		
		//Check to see if a token was included in the URL and load the form
		//if so.

//		String xml=getFirstName();
	
//	Window.alert(xml);
		
		
		
	}
	
	
	public native String getFirstName()/*-{

	return $wnd.gottenXML;

	}-*/;

	/**
	 * @see com.google.gwt.user.client.WindowResizeListener#onWindowResized(int, int)
	 */
	public void onWindowResized(int width, int height){		
		centerWidget.onWindowResized(width, height);
	}
	
	public String getExternalForm(){
		
		String url = FormDesigner.XEP_GET_FORM_URL+FormDesigner.token+"/";
		doGet(url);

		return this.returnXml;
	}


	private void setRetrievedXML(String xml){
		this.returnXml = xml;
		if (xml.trim().isEmpty()){
			FormUtil.dlg.hide();
//			FormDesigner.alert("Blank Xform received! Please create a form and submit to server...");
			return;
		}

		if (xml != null){

			centerWidget.openExternalXML(xml);  //<----
		}else{
			FormDesigner.alert("Problem Retreiving external form! Return Error Code: "+returnErrorCode);
		}
		
	}
	
	private void setReturnErrorCode(int code){
		this.returnErrorCode = code;
	}
	
	public void doGet(String url) {
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
		String s = "";
		Iterator<String> iter = Cookies.getCookieNames().iterator();
		for(int i=0;i<Cookies.getCookieNames().size();i++){
			s +=iter.next()+", ";
		}
		
		//Window.alert(s);
		try {
			Request response = builder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					FormDesigner.alert("Error on while doing GET Request. In FormDesignerWidget.MyRequestCallback, exception: "+exception.getStackTrace());
				}
				
				public void onResponseReceived(Request request, Response response) {
//					parseCookies(response);
					setReturnErrorCode(response.getStatusCode());
					setRetrievedXML(response.getText());
				}
			});
		} catch (RequestException e) {
			FormDesigner.alert("Problem with Getting External Form: FormDesignerWidget.doGet, RequestException:"+e.getStackTrace());
		}
		
		
		
		
		
	}
		    
	
    public static void parseCookies(final Response pResponse) { 
        final Header[] headers = pResponse.getHeaders(); 
        if (headers == null || headers.length == 0) { 
          return; 
        } 
        for (int i = 0; i < headers.length; i++) { 
                if (headers[i] != null && "Set-Cookie".equalsIgnoreCase(headers[i].getName())) { 
                        final String cookieRaw = headers[i].getValue(); 
                        jsAddCookie( cookieRaw ); 
                } 
        } 
    } 
    
	private static native void jsAddCookie( String pRaw ) /*-{
		$doc.cookie = pRaw;
	}-*/; 
		    
	
}