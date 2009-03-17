package org.purc.purcforms.client.controller;

import org.purc.purcforms.client.AboutDialog;
import org.purc.purcforms.client.CenterPanel;
import org.purc.purcforms.client.LeftPanel;
import org.purc.purcforms.client.PurcConstants;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.ModelConstants;
import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.view.ErrorDialog;
import org.purc.purcforms.client.view.FormsTreeView;
import org.purc.purcforms.client.view.ProgressDialog;
import org.purc.purcforms.client.xforms.XformConverter;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;


/**
 * 
 * @author daniel
 *
 */
public class FormDesignerController implements IFormDesignerListener{

	private CenterPanel centerPanel;
	private LeftPanel leftPanel;
	private Integer formId;	

	private static ProgressDialog dlg = new ProgressDialog();


	public FormDesignerController(CenterPanel centerPanel, LeftPanel leftPanel){
		this.leftPanel = leftPanel;
		this.centerPanel = centerPanel;
	}

	/* (non-Javadoc)
	 * @see org.purc.purcform.client.controller.IFormDesignerController#addNewItem()
	 */
	public void addNewItem() {
		leftPanel.addNewItem();
	}

	/* (non-Javadoc)
	 * @see org.purc.purcform.client.controller.IFormDesignerController#addNewChildItem()
	 */
	public void addNewChildItem() {
		leftPanel.addNewChildItem();
	}

	public static native void back() /*-{
		window.history.go(-1);
	}-*/;
	
	public void closeForm() {
		back();
	} 

	/* (non-Javadoc)
	 * @see org.purc.purcform.client.controller.IFormDesignerController#deleteSelectedItem()
	 */
	public void deleteSelectedItem() {
		leftPanel.deleteSelectedItem();	
		centerPanel.deleteSelectedItem();
	}

	/* (non-Javadoc)
	 * @see org.purc.purcform.client.controller.IFormDesignerController#moveItemDown()
	 */
	public void moveItemDown() {
		leftPanel.moveItemDown();
	}

	/* (non-Javadoc)
	 * @see org.purc.purcform.client.controller.IFormDesignerController#moveItemUp()
	 */
	public void moveItemUp() {
		leftPanel.moveItemUp();
	}

	/* (non-Javadoc)
	 * @see org.purc.purcform.client.controller.IFormDesignerController#newForm()
	 */
	public void newForm() {
		if(isOfflineMode())
			leftPanel.addNewForm();
	}

	/* (non-Javadoc)
	 * @see org.purc.purcform.client.controller.IFormDesignerController#openForm()
	 */
	public void openForm() {
		if(isOfflineMode())
			openFormDeffered(ModelConstants.NULL_ID);
	}

	public void openFormDeffered(int id) {
		final int tempFormId = id;
		
		dlg.setText("Opening Form");
		dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {
				try{
					String xml = centerPanel.getXformsSource().trim();
					if(xml.length() > 0){
						FormDef formDef = XformConverter.fromXform2FormDef(xml);
						if(tempFormId != ModelConstants.NULL_ID)
							formDef.setId(tempFormId);
						
						leftPanel.loadForm(formDef);
						centerPanel.loadForm(formDef,centerPanel.getLayoutXml());
						centerPanel.format();
					}
				}
				catch(Exception ex){
					ex.printStackTrace();

					String text = "Uncaught exception: ";
					String s = text;
					while (ex != null) {
						s = ex.getMessage();
						StackTraceElement[] stackTraceElements = ex.getStackTrace();
						text += ex.toString() + "\n";
						for (int i = 0; i < stackTraceElements.length; i++) {
							text += "    at " + stackTraceElements[i] + "\n";
						}
						ex = (Exception)ex.getCause();
						if (ex != null) {
							text += "Caused by: ";
						}
					}

					ErrorDialog dialogBox = new ErrorDialog();
					dialogBox.setText("Unxpected Failure");
					dialogBox.setBody(s);
					dialogBox.setCallStack(text);
					dialogBox.center();
				}
				dlg.hide();	
			}
		});
	}


	public void openFormLayout() {
		if(isOfflineMode())
			openFormLayoutDeffered();
	}

	public void openFormLayoutDeffered() {
		dlg.setText("Opening Form Layout");
		dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {
				try{
					centerPanel.openFormLayout();
				}
				catch(Exception ex){
					ErrorDialog dialogBox = new ErrorDialog();
					dialogBox.setText("Unxpected Failure while opening form layout.");
					dialogBox.setBody(ex.getMessage());
					dialogBox.center();
					ex.printStackTrace();
				}
				dlg.hide();	
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.purc.purcform.client.controller.IFormDesignerController#saveForm()
	 */
	public void saveForm() {
		Object obj = leftPanel.getSelectedForm();
		if(obj == null){
			Window.alert("Please select the item to save.");
			return;
		}

		centerPanel.commitChanges();

		//TODO Need to preserve user's model and any others.
		String xml = null;
		FormDef formDef = (FormDef)obj;
		if(formDef.getDoc() == null){
			xml = XformConverter.fromFormDef2Xform(formDef);
			xml = FormDesignerUtil.formatXml(xml);
		}
		else{
			formDef.updateDoc(false);
			xml = XformConverter.fromDoc2String(formDef.getDoc());
			xml = FormDesignerUtil.formatXml(xml);
		}

		centerPanel.setXformsSource(xml,formId == null);
		centerPanel.buildLayoutXml();
		formDef.setLayout(centerPanel.getLayoutXml());

		if(!isOfflineMode())
			saveForm(xml,centerPanel.getLayoutXml());
	}

	public void saveFormAs() {
		if(isOfflineMode()){
			Object obj = leftPanel.getSelectedForm();
			if(obj == null){
				Window.alert("Please select the item to save.");
				return;
			}

			String xml = null;
			xml = XformConverter.fromFormDef2Xform((FormDef)obj);
			xml = FormDesignerUtil.formatXml(xml);
			centerPanel.setXformsSource(xml,formId == null);
		}
	}

	public void saveFormLayout() {
		dlg.setText("Saving Form Layout");
		dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {
				try{
					centerPanel.saveFormLayout();
				}
				catch(Exception ex){
					ErrorDialog dialogBox = new ErrorDialog();
					dialogBox.setText("Unxpected Failure while opening form layout.");
					dialogBox.setBody(ex.getMessage());
					dialogBox.center();
					ex.printStackTrace();
				}
				dlg.hide();	
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.purc.purcform.client.controller.IFormDesignerController#showAboutInfo()
	 */
	public void showAboutInfo() {
		AboutDialog dlg = new AboutDialog();
		dlg.setAnimationEnabled(true);
		dlg.show();
		dlg.center();
	}

	/* (non-Javadoc)
	 * @see org.purc.purcform.client.controller.IFormDesignerController#showHelpContents()
	 */
	public void showHelpContents() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.purc.purcform.client.controller.IFormDesignerController#showLanguages()
	 */
	public void showLanguages() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.purc.purcform.client.controller.IFormDesignerController#showOptions()
	 */
	public void showOptions() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.purc.purcform.client.controller.IFormDesignerController#viewToolbar()
	 */
	public void viewToolbar() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignLeft()
	 */
	public void alignLeft() {
		centerPanel.alignLeft();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignRight()
	 */
	public void alignRight() {
		centerPanel.alignRight();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignLeft()
	 */
	public void alignTop() {
		centerPanel.alignTop();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignRight()
	 */
	public void alignBottom() {
		centerPanel.alignBottom();
	}

	public void makeSameHeight() {
		centerPanel.makeSameHeight();
	}

	public void makeSameSize() {
		centerPanel.makeSameSize();
	}

	public void makeSameWidth() {
		centerPanel.makeSameWidth();
	}

	/* (non-Javadoc)
	 * @see org.purc.purcform.client.controller.IFormActionListener#copyItem()
	 */
	public void copyItem() {
		leftPanel.copyItem();
		centerPanel.copyItem();
	}

	/* (non-Javadoc)
	 * @see org.purc.purcform.client.controller.IFormActionListener#cutItem()
	 */
	public void cutItem() {
		leftPanel.cutItem();
		centerPanel.cutItem();
	}

	/* (non-Javadoc)
	 * @see org.purc.purcform.client.controller.IFormActionListener#pasteItem()
	 */
	public void pasteItem() {
		leftPanel.pasteItem();
		centerPanel.pasteItem();
	}

	public void refreshItem(){
		leftPanel.refreshItem();
	}

	/* (non-Javadoc)
	 * @see org.purc.purcform.client.controller.IFormDesignerListener#format()
	 */
	public void format() {
		centerPanel.format();
	}

	public void refresh(Object sender) {
		if(sender instanceof FormsTreeView){ //TODO This controller should not know about LeftPanel implementation details.
			if(formId != null){
				dlg.setText("Refresing Form");
				dlg.center();

				DeferredCommand.addCommand(new Command(){
					public void execute() {
						refreshForm();
						dlg.hide();	
					}
				});
			}
		}
		else{
			centerPanel.refresh();
			leftPanel.refresh();
		}
	}

	public void loadForm(int frmId){
		this.formId = frmId;

		String url = FormUtil.getHostPageBaseURL();
		url += FormUtil.getFormDefDownloadUrlSuffix();
		url += FormUtil.getFormIdName()+"="+this.formId;

		//url += "&uname=Guyzb&pw=daniel123";

		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,URL.encode(url));

		try{
			builder.sendRequest(null, new RequestCallback(){
				public void onResponseReceived(Request request, Response response){
					String xml = response.getText();
					if(xml == null || xml.length() == 0){
						Window.alert("No data found.");
						return;
					}
					
					String xformXml, layoutXml = null;

					int pos = xml.indexOf(PurcConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR);
					if(pos > 0){
						xformXml = xml.substring(0,pos);
						layoutXml = FormUtil.formatXml(xml.substring(pos+PurcConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR.length(), xml.length()));
					}
					else
						xformXml = xml;

					centerPanel.setXformsSource(FormUtil.formatXml(xformXml),false);
					centerPanel.setLayoutXml(layoutXml,false);
					openFormDeffered(formId);
				}

				public void onError(Request request, Throwable exception){
					exception.printStackTrace();
					Window.alert(exception.getMessage());
				}
			});
		}
		catch(RequestException ex){
			ex.printStackTrace();
			Window.alert(ex.getMessage());
		}
	}

	public void saveForm(String xformXml, String layoutXml){
		String url = FormUtil.getHostPageBaseURL();
		url += FormUtil.getFormDefUploadUrlSuffix();
		url += FormUtil.getFormIdName()+"="+this.formId;

		RequestBuilder builder = new RequestBuilder(RequestBuilder.POST,URL.encode(url));
		//builder.setHeader("Content_type", "application/x-www-form-urlencoded");

		try{
			String xml = xformXml;
			if(layoutXml != null && layoutXml.trim().length() > 0)
				xml += PurcConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR + layoutXml;

			builder.sendRequest(xml, new RequestCallback(){
				public void onResponseReceived(Request request, Response response){
					Window.alert("Form saved successfully");
				}

				public void onError(Request request, Throwable exception){
					exception.printStackTrace();
					Window.alert(exception.getMessage());
				}
			});
		}
		catch(RequestException ex){
			ex.printStackTrace();
			Window.alert(ex.getMessage());
		}
	}

	public boolean isOfflineMode(){
		return formId == null;
	}

	private void refreshForm(){
		String url = FormUtil.getHostPageBaseURL();
		url += FormUtil.getFormDefRefreshUrlSuffix();
		url += FormUtil.getFormIdName()+"="+this.formId;

		//url += "&uname=Guyzb&pw=daniel123";

		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,URL.encode(url));

		try{
			builder.sendRequest(null, new RequestCallback(){
				public void onResponseReceived(Request request, Response response){
					String xml = response.getText();
					if(xml == null || xml.length() == 0){
						Window.alert("No data found.");
						return;
					}

					centerPanel.setXformsSource(xml,false);
					refreshFormDeffered();

				}

				public void onError(Request request, Throwable exception){
					exception.printStackTrace();
					Window.alert(exception.getMessage());
				}
			});
		}
		catch(RequestException ex){
			ex.printStackTrace();
			Window.alert(ex.getMessage());
		}
	}

	private void refreshFormDeffered(){
		dlg.setText("Refreshing Form");
		dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {
				try{
					String xml = centerPanel.getXformsSource();
					FormDef formDef = XformConverter.fromXform2FormDef(xml);
					formDef.refresh(centerPanel.getFormDef());
					formDef.updateDoc(false);
					xml = formDef.getDoc().toString();

					leftPanel.refresh(formDef);
					centerPanel.setXformsSource(FormUtil.formatXml(xml), false);
				}
				catch(Exception ex){
					ex.printStackTrace();

					String text = "Uncaught exception: ";
					String s = text;
					while (ex != null) {
						s = ex.getMessage();
						StackTraceElement[] stackTraceElements = ex.getStackTrace();
						text += ex.toString() + "\n";
						for (int i = 0; i < stackTraceElements.length; i++) {
							text += "    at " + stackTraceElements[i] + "\n";
						}
						ex = (Exception)ex.getCause();
						if (ex != null) {
							text += "Caused by: ";
						}
					}

					ErrorDialog dialogBox = new ErrorDialog();
					dialogBox.setText("Unxpected Failure");
					dialogBox.setBody(s);
					dialogBox.setCallStack(text);
					dialogBox.center();
				}
				dlg.hide();	
			}
		});
	}
}
