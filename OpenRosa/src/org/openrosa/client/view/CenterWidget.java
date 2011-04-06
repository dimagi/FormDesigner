package org.openrosa.client.view;


import org.openrosa.client.Context;
import org.openrosa.client.FormDesigner;
import org.openrosa.client.controller.IFileListener;
import org.openrosa.client.controller.ITextListener;
import org.openrosa.client.model.FormDef;
import org.openrosa.client.model.ItextModel;
import org.openrosa.client.util.Itext;
import org.openrosa.client.util.ItextParser;
import org.openrosa.client.util.XEPResponse;
import org.openrosa.client.xforms.XformParser;
import org.openrosa.client.xforms.XhtmlBuilder;
import org.openrosa.client.PurcConstants;
import org.openrosa.client.controller.IFormSelectionListener;
import org.openrosa.client.controller.OpenFileDialogEventListener;
import org.openrosa.client.util.FormUtil;
import org.openrosa.client.xforms.XmlUtil;

import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.xml.client.Document;


/**
 * This is the widget that houses the xforms, design and internationalization tab.
 * 
 * @author daniel
 *
 */
public class CenterWidget extends Composite implements IFileListener,IFormSelectionListener,ITextListener, OpenFileDialogEventListener {

	/**
	 * Tab widget housing the contents.
	 */
	private DecoratedTabPanel tabs = new DecoratedTabPanel();

	private XformsTabWidget xformsWidget = new XformsTabWidget(this);
	DesignTabWidget designWidget = new DesignTabWidget(this);
	private TextTabWidget itextWidget = new TextTabWidget(this);

	/**
	 * this is a flag the onSave() method checks to see if it should show the xml window when it saves.
	 */
	private boolean showXMLWindowFlag = true; 
	
	private String externalXML;
	private Boolean loadExternalXML;
	


	public CenterWidget() {	
		this.loadExternalXML = false;
		initWidget(designWidget);  

		FormUtil.maximizeWidget(tabs);
		FormUtil.maximizeWidget(designWidget);
		FormUtil.maximizeWidget(this);

		designWidget.addFormSelectionListener(this);
	}



	public void onWindowResized(int width, int height){
		int shortcutHeight = height - getAbsoluteTop();
		if(shortcutHeight > 50){
			xformsWidget.adjustHeight(shortcutHeight-130 + PurcConstants.UNITS);
			itextWidget.adjustHeight(shortcutHeight-50 + PurcConstants.UNITS);
		}

		designWidget.onWindowResized(width, height);
	}

	public void onNew(){
		designWidget.addNewForm();
	}

	public void onOpen(){
		
		if(!xformsWidget.isVisible()){
			xformsWidget.setXform(null);
			xformsWidget.showWindow();
			return;
		}
		
		xformsWidget.hideWindow();
		FormUtil.dlg.setText("Opening...");
		FormUtil.dlg.show();

		DeferredCommand.addCommand(new Command() {
			public void execute() {
				try{
					openFile();
					xformsWidget.showWindow();
					FormUtil.dlg.hide();
				}
				catch(Exception ex){
					FormUtil.displayException(ex);
				}
			}
		});
	}
	
	public void openExternalXML(String xml){

		this.loadExternalXML = true;
		this.externalXML = xml;
		openFile();
	}
	
	private void openFile(){
		String xml;
		if(loadExternalXML && externalXML != null){
			xml = externalXML;
			loadExternalXML = false;  //to keep standard operation going
		}else{
			xml = xformsWidget.getXform();
		}
		if(xml == null || xml.trim().length() == 0){
			xformsWidget.showWindow();
			return;
		}


		Itext.clearLocales();
		Document doc = ItextParser.parse(xml);
		FormDef formDef = XformParser.getFormDef(doc);

		formDef.setXformXml(xml);
		designWidget.loadForm(formDef);
		FormUtil.dlg.hide();
	}

	
	/**
	 * Updates the FormDef and the XML it points to so everything is nice and lined up.
	 * Then shows the XML form
	 */
	public void onPreview(boolean showWindow){
		xformsWidget.hideWindow();
		FormUtil.dlg.setText("Saving...");
		FormUtil.dlg.show();
		showXMLWindowFlag = showWindow;
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				try{
					boolean saved = saveFile(showXMLWindowFlag);
					xformsWidget.showWindow();
					FormUtil.dlg.hide();
				}
				catch(Exception ex){
					FormUtil.displayException(ex);
				}
			}
		});
	}
	
	public boolean saveFile(boolean showWindow){  //used by onPreview(), showItext() and submitData()
		FormDef formDef = Context.getFormDef();
		String xml = null;
		
		if(formDef == null){
			xformsWidget.setXform("");
			Window.alert("Form is empty! Nothing to save.");
			return false;
		}

		designWidget.commitChanges();

		Document doc = formDef.getDoc();

		if(doc != null){
			formDef.updateDoc(false);
		}else{
			doc = XhtmlBuilder.fromFormDef2XhtmlDoc(formDef);
			formDef.setDoc(doc);
			formDef.setXformsNode(doc.getDocumentElement());
		}
		ItextParser.updateItextBlock(formDef);
		itextWidget.save(false); //calls onSaveItext() below
		doc.getDocumentElement().setAttribute("xmlns:jr", "http://openrosa.org/javarosa");
		doc.getDocumentElement().setAttribute("xmlns", "http://www.w3.org/2002/xforms");

		xml = FormUtil.formatXml(XmlUtil.fromDoc2String(doc));

		if(formDef != null)
			formDef.setXformXml(xml);

		xformsWidget.setXform(xml);
		
		//hack
		cleanupBadXML(formDef.getDoc());
		
		
		
		if(showWindow){
			xformsWidget.showWindow();
		}
		
		return true;

	}

	public void onSaveItext(ListStore<ItextModel> itextrows){
		
		Itext.updateModel(itextrows);
		FormDef formDef = Context.getFormDef();
		if(formDef == null || formDef.getDoc() == null){
			Window.alert("FormDef is null. Can't save Itext");
			return;
		}
		String xml = null;

		ItextParser.updateItextBlock(formDef);
		xml = FormUtil.formatXml(XmlUtil.fromDoc2String(formDef.getDoc()));
		//update form outline with the itext changes
//		formDef = XformParser.getFormDef(ItextParser.parse(xml));
		designWidget.refreshForm(formDef);
		formDef.setXformXml(xml);
		xformsWidget.setXform(xml);
	}


	public void refreshForm(){
		FormDef formDef = Context.getFormDef();
		designWidget.refreshForm(formDef);
	}

	
	public void showItext(){

		FormUtil.dlg.setText("Opening...");
		FormUtil.dlg.show();

		DeferredCommand.addCommand(new Command() {
			public void execute() {

				try{
//					saveFile(false);
//					FormDef formDef = Context.getFormDef();
//					//String xml = null;
//
//					if(formDef == null || formDef.getDoc() == null || (formDef.getText() == null) || formDef.getText().isEmpty())
//						return;
					

					//This line is called in the this.saveFile(false) above.
					/*textBuilder.updateItextBlock(formDef.getDoc(), formDef, itextWidget.getItext(),formAttrMap,itextMap);
					xml = FormUtil.formatXml(XmlUtil.fromDoc2String(formDef.getDoc()));

					//update form outline with the itext changes
					itextList = new ListStore<ItextModel>();
					formDef = XformParser.getFormDef(ItextParser.parse(xml,itextList,formAttrMap,itextMap));
			//		designWidget.refreshForm(formDef);*/

					itextWidget.loadItext();

					itextWidget.showWindow();

					FormUtil.dlg.hide();
				}
				catch(Exception ex){
					FormUtil.displayException(ex);
				}
			}
		});
	}

	public void cleanupBadXML(Document doc){
////		Window.alert("Cleaning?");
//		((Element)doc.getElementsByTagName("model").item(0)).removeAttribute("xmlns");
//		NodeList nl = doc.getElementsByTagName("input");
////		Window.alert("number of input tags found:"+nl.getLength());
//		for (int i=0;i<nl.getLength();i++){
//			((Element)nl.item(i)).removeAttribute("xmlns");
//		}
//		return;
	}

	public void onFormItemSelected(Object formItem){
	}

	public void onSetFileContents(String contents){
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,FormUtil.getFileOpenUrl());

		try{
			builder.sendRequest(null, new RequestCallback(){
				public void onResponseReceived(Request request, Response response){

					if(response.getStatusCode() != Response.SC_OK){
						FormUtil.displayReponseError(response);
						return;
					}

					String contents = response.getText();
					if(contents != null && contents.trim().length() > 0){
						xformsWidget.setXform(contents);
						onOpen();
					}
				}

				public void onError(Request request, Throwable exception){
					FormUtil.displayException(exception);
				}
			});
		}
		catch(RequestException ex){
			FormUtil.displayException(ex);
		}
	}


	public void onSubmit(boolean continueEdit){
		boolean saved = saveFile(false);
		CenterWidget.continueEditing = continueEdit;
		if(Context.getFormDef() == null || !saved)
			Window.alert("No form to submit");
		else
			FormUtil.dlg.setText("Submitting Form...");
			boolean canSubmit = submitData();
			if(!canSubmit){
				Window.alert("Client side submission error. Data not sent!");
			}
	}

	public static boolean continueEditing = false;
	private boolean submitData(){
		
		if(FormDesigner.token == null || FormDesigner.token.length() == 0){
			return false;
		}
		
//		submitDialogue.show();
		String url = FormDesigner.XEP_POST_FORM_URL;
		
//		String url = FormUtil.getHostPageBaseURL();
//		url += FormUtil.getFormDefUploadUrlSuffix();
		//url += FormUtil.getFormIdName()+"="+this.formId;

		RequestBuilder builder = new RequestBuilder(RequestBuilder.POST,URL.encode(url));

		try{
			builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
			String data = "token="+FormDesigner.token + "&";
			if(continueEditing){
				data += "continue=true&";
			}else{
				data += "continue=false&";
			}
			saveFile(false);
			String xml = xformsWidget.getXform();
			if(xml == null || xml.isEmpty()){
//				Window.alert("Form being sent is blank.");
			}else{
//				Window.alert("Sending Form...");
				FormUtil.dlg.center("Sending Form...");
				FormUtil.dlg.show();
			}
			data += "xform="+URL.encodeComponent(xml);
			builder.sendRequest(data, new RequestCallback(){
				public void onResponseReceived(Request request, Response response){
					int code = response.getStatusCode();
					FormUtil.dlg.hide();
//					Window.alert("Received Status Code is: "+code+"\n"+
//							"Headers:"+response.getHeadersAsString());
					if(response.getStatusCode() == Response.SC_OK){
						FormUtil.dlg.center("Succesfully Sent Form!");
						FormUtil.dlg.show();
						FormUtil.dlg.hide();
						XEPResponse xepResponse = ParseXEPResponse(response.getText());
						
						
						if(xepResponse.getContinue()){
							Window.alert("Successfully Saved! Please continue editing\n Status: "+xepResponse.getStatus());
						}else{
//							Window.alert("Successfully submitted.  Status: "+xepResponse.getStatus()+"\nRedirecting you back to HQ...");
							FormDesigner.enableCloseHandler(false);
							String redirectURL = xepResponse.getCallback();
							Window.Location.assign(redirectURL);
						}
						
						
						
						
						return;
					}else{
						FormUtil.displayReponseError(response);
					}
					
					
//					if(!continueEditing){
//						Window.Loation.assign(response.getHeader("Location"));
//					}
				}

				public void onError(Request request, Throwable exception){
					Window.alert("sendRequest onError exception....");
					FormUtil.displayException(exception);
				}
			});
			
			return true;
		}
		catch(RequestException ex){
			FormUtil.displayException(ex);
			return false;
		}
	}
	
	  private final native XEPResponse ParseXEPResponse(String json) /*-{
	    return eval("(" + json + ")");
	  }-*/;



	@Override
	public void onOpenFile() {
		// TODO Auto-generated method stub
		
	}
}
