package org.openrosa.client.view;

import java.util.HashMap;
import java.util.List;

import org.openrosa.client.Context;
import org.openrosa.client.FormDesigner;
import org.openrosa.client.controller.IFileListener;
import org.openrosa.client.controller.ITextListener;
import org.openrosa.client.model.FormDef;
import org.openrosa.client.model.ItextModel;
import org.openrosa.client.util.ContinueEditDialog;
import org.openrosa.client.util.Itext;
import org.openrosa.client.util.ItextBuilder;
import org.openrosa.client.util.ItextParser;
import org.openrosa.client.util.XEPResponse;
import org.openrosa.client.xforms.XformParser;
import org.openrosa.client.xforms.XhtmlBuilder;
import org.purc.purcforms.client.PurcConstants;
import org.purc.purcforms.client.controller.IFormSelectionListener;
import org.purc.purcforms.client.controller.OpenFileDialogEventListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.view.LoginDialog;
import org.purc.purcforms.client.view.OpenFileDialog;
import org.purc.purcforms.client.view.SaveFileDialog;
import org.purc.purcforms.client.xforms.XmlUtil;

import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
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

	private static final int TAB_INDEX_DESIGN = 0;
	//private static final int TAB_INDEX_XFORMS = 1;
	private static final int TAB_INDEX_ITEXT = 2;

	/**
	 * Tab widget housing the contents.
	 */
	private DecoratedTabPanel tabs = new DecoratedTabPanel();

	private XformsTabWidget xformsWidget = new XformsTabWidget(this);
	DesignTabWidget designWidget = new DesignTabWidget(this);
	private TextTabWidget itextWidget = new TextTabWidget(this);

	/** The current form definition object. */
	private FormDef formDef;

	/** Mapping of itext id's to their form attribute values. eg id=name, form=short */
	private static HashMap<String,String> formAttrMap = new HashMap<String,String>();

	/** List of ItextModel objects as they are shown in the grid. */
	ListStore<ItextModel> itextList = new ListStore<ItextModel>();

	/**
	 * this is a flag the onSave() method checks to see if it should show the xml window when it saves.
	 */
	private boolean showXMLWindowFlag = true; 

	/**
	 * The dialog box used to log on the server when the user's session expires on the server.
	 */
	private static LoginDialog loginDlg = new LoginDialog();
	
	/** Static self reference such that the static login call back can have
	 *  a reference to proceed with the current action.
	 */
	private static CenterWidget centerWidget;
	
	
	private String externalXML;
	private Boolean loadExternalXML;
	
	private ContinueEditDialog submitDialogue;


	public CenterWidget() {	
		centerWidget = this;
		submitDialogue = new ContinueEditDialog();
		initDesignTab();
		initXformsTab();
		initItextTab();

		FormUtil.maximizeWidget(tabs);

		tabs.selectTab(TAB_INDEX_DESIGN);
		this.loadExternalXML = false;

		//////////////////////////////!!!!!!!!!!!!!!!!!
		initWidget(designWidget);   /// <<<<<<---------------------- This is a gruesome shortcut
		///////////////////////////////////////////////   The whole 'CenterWidget' should be removed
		// and designWidget should be directly called from FormDesignWidget.java

		//tabs.addSelectionHandler(this);

		FormUtil.maximizeWidget(tabs);
		FormUtil.maximizeWidget(designWidget);
		FormUtil.maximizeWidget(this);

		designWidget.addFormSelectionListener(this);
	}

	private void initDesignTab(){
		tabs.add(designWidget, "Design");
	}

	private void initXformsTab(){
		tabs.add(xformsWidget, "Xforms");
	}

	private void initItextTab(){
		//		tabs.add(itextWidget, "Internationalization");
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
	public void showOpen(){
		xformsWidget.showWindow();
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
			showOpen();
			return;
		}

		//		tabs.selectTab(TAB_INDEX_DESIGN);

		//org.openrosa.client.jr.core.model.FormDef formDef1 = org.openrosa.client.jr.xforms.parse.XFormParser.getFormDef(xml);

		Itext.clearLocales();
		Document doc = ItextParser.parse(xml,itextList,formAttrMap,Context.getItextMap());
		FormDef formDef = XformParser.getFormDef(doc);

		//Because we are still reusing the default purcforms xforms parsing, we need to set the page node.
		/*if(formDef.getQuestionCount() > 0){
			PageDef pageDef = formDef.getPageAt(0);
			QuestionDef questionDef = pageDef.getElementAt(0);
			if(questionDef.getControlNode() != null)
				pageDef.setGroupNode((Element)questionDef.getControlNode().getParentNode());
		}*/

		formDef.setXformXml(xml);
		designWidget.loadForm(formDef);
		FormUtil.dlg.hide();
		//itextWidget.loadItext(list); on loading, form item is selected and this is eventually called.
	}

	public void onSave(boolean showWindow){
		xformsWidget.hideWindow();
		FormUtil.dlg.setText("Saving...");
		FormUtil.dlg.show();
		showXMLWindowFlag = showWindow;
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				try{
					saveFile(showXMLWindowFlag);
					xformsWidget.showWindow();
					FormUtil.dlg.hide();
				}
				catch(Exception ex){
					FormUtil.displayException(ex);
				}
			}
		});
	}

	public void showItext(){

		FormUtil.dlg.setText("Opening...");
		FormUtil.dlg.show();

		DeferredCommand.addCommand(new Command() {
			public void execute() {

				try{
					saveFile(false);

					//String xml = null;

					if(Context.getFormDef() == null)
						formDef = null;

					if(formDef == null || formDef.getDoc() == null)
						return;
					
					if(formDef.getText() == null || formDef.getText().isEmpty()){
						return;
					}

					//this line is necessary for gxt to load the form with text on the design tab.
					tabs.selectTab(TAB_INDEX_DESIGN);

					//This line is called in the this.saveFile(false) above.
					/*textBuilder.updateItextBlock(formDef.getDoc(), formDef, itextWidget.getItext(),formAttrMap,itextMap);
					xml = FormUtil.formatXml(XmlUtil.fromDoc2String(formDef.getDoc()));

					//update form outline with the itext changes
					itextList = new ListStore<ItextModel>();
					formDef = XformParser.getFormDef(ItextParser.parse(xml,itextList,formAttrMap,itextMap));
			//		designWidget.refreshForm(formDef);*/

					itextWidget.loadItext(itextList);

					itextWidget.showWindow();

					FormUtil.dlg.hide();
				}
				catch(Exception ex){
					FormUtil.displayException(ex);
				}
			}
		});
	}


	private void saveFile(boolean showWindow){
		//FormDef formDef = Context.getFormDef();

		if(Context.getFormDef() == null)
			formDef = null;

		String xml = null;
		if(formDef != null){
			designWidget.commitChanges();

			//TODO need to solve bug when opened forms do not reflect changes in the instance data node names.
			//This is caused by copying a new model during the xhtml conversion
			Document doc = formDef.getDoc();

			if(doc != null){
//				Window.alert("Lol, doc is not null");
				formDef.updateDoc(false);
			}else{
				doc = XhtmlBuilder.fromFormDef2XhtmlDoc(formDef);
				formDef.setDoc(doc);
				formDef.setXformsNode(doc.getDocumentElement());
			}

			ItextBuilder.updateItextBlock(doc,formDef,itextList,formAttrMap,Context.getItextMap());
			//itextWidget.loadItext(itextList);

			doc.getDocumentElement().setAttribute("xmlns:jr", "http://openrosa.org/javarosa");
			doc.getDocumentElement().setAttribute("xmlns", "http://www.w3.org/2002/xforms");

			//These purcforms attributes are not needed by javarosa
			if(formDef.getDataNode() != null){
				formDef.getDataNode().removeAttribute("name");
				formDef.getDataNode().removeAttribute("formKey");
			}
			xml = FormUtil.formatXml(XmlUtil.fromDoc2String(doc));
		}

		if(formDef != null)
			formDef.setXformXml(xml);

		xformsWidget.setXform(xml);
		
		//hack
		cleanupBadXML(formDef.getDoc());
		
		
		
		if(showWindow){
			xformsWidget.showWindow();
		}
		//		tabs.selectTab(TAB_INDEX_XFORMS);

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
		
//		if(formItem == null)
//			clearItextData();
		
		if(!(formItem instanceof FormDef))
			return;

		FormDef formDef = (FormDef)formItem;

		/*if(this.formDef == null || this.formDef != formItem){
			xformsWidget.setXform(formDef == null ? null : formDef.getXformXml());

			ListStore<ItextModel> list = new ListStore<ItextModel>();

			if(formDef != null && formDef.getDoc() != null)
				ItextBuilder.updateItextBlock(formDef.getDoc(),formDef,list,formAttrMap,itextMap);

			itextWidget.loadItext(list);
		}*/

		this.formDef = formDef;
	}

	public void onSaveItext(){
		//TODO: Take Itext.ListStore<ItextModel>
		//inject changed/new values into the XML Doc (formDef.getDoc())
		//trigger a new parsing of the form.
		//Then do:
		// formDef.setXformXml(xml);
		// xformsWidget.setXform(xml);
		//
		
		
		
		ListStore<ItextModel> itext = Itext.getItextRows();
		String xml = null;
		if(!(formDef == null || formDef.getDoc() == null)){
			//this line is necessary for gxt to load the form with text on the design tab.
			tabs.selectTab(TAB_INDEX_DESIGN);
	
			ItextBuilder.updateItextBlock(formDef.getDoc(), formDef, itext, formAttrMap, Context.getItextMap());
			xml = FormUtil.formatXml(XmlUtil.fromDoc2String(formDef.getDoc()));
	
			//update form outline with the itext changes
			formDef = XformParser.getFormDef(ItextParser.parse(xml,itextList,formAttrMap,Context.getItextMap()));
			designWidget.refreshForm(formDef);
		}
		formDef.setXformXml(xml);
		xformsWidget.setXform(xml);
	}


	public void onSaveFile(){

		FormUtil.dlg.setText("Saving...");
		FormUtil.dlg.show();

		DeferredCommand.addCommand(new Command() {
			public void execute() {

				try{
					if(formDef != null){
						saveFile(false);
						FormUtil.dlg.hide();

						String fileName = "filename";
						fileName = formDef.getName();
						SaveFileDialog dlg = new SaveFileDialog(FormUtil.getFileSaveUrl(), formDef.getXformXml(), fileName);
						dlg.center();	
					}
					else{
						FormUtil.dlg.hide();
						Window.alert("No form to save");
					}
				}
				catch(Exception ex){
					FormUtil.displayException(ex);
				}
			}
		});
	}


	public void onOpenFile(){
		OpenFileDialog dlg = new OpenFileDialog(this,FormUtil.getFileOpenUrl());
		dlg.center();
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
		CenterWidget.continueEditing = continueEdit;
		if(formDef == null)
			Window.alert("No form to submit");
		else
			FormUtil.dlg.setText("Submitting Form...");
			boolean canSubmit = submitData();
			
			
			
	}


	/**
	 * This is called from the server after an attempt to authenticate the current
	 * user before they can submit form data.
	 * 
	 * @param authenticated has a value of true if the server has successfully authenticated the user, else false.
	 */
	private static void authenticationCallback(boolean authenticated) {	

		//If user has passed authentication, just go on with whatever they wanted to do
		//else just redisplay the login dialog and let them enter correct
		//user name and password.
		if(authenticated){
			loginDlg.hide();
			centerWidget.submitData();
		}
		else
			loginDlg.center();
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
			data += URL.encode("xform="+xml);
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
							FormDesigner.closeHandler.removeHandler();
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
}
