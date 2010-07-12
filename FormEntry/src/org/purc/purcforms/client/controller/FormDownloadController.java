package org.purc.purcforms.client.controller;

import org.purc.purcforms.client.FormEntryConstants;
import org.purc.purcforms.client.FormEntryContext;
import org.purc.purcforms.client.listener.FormDefDownloadListener;
import org.purc.purcforms.client.listener.LoginInfoListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.util.Utils;
import org.purc.purcforms.client.view.impl.LoginInfoDlg;

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
public class FormDownloadController implements LoginInfoListener{

	private FormDefDownloadListener formDownloadListener;

	public FormDownloadController(){

	}

	public void downloadForms(FormDefDownloadListener formDownloadListener){
		this.formDownloadListener = formDownloadListener;
		new LoginInfoDlg(this).center();
	}

	private void downloadForms(){
		FormUtil.dlg.setText("Downloading form list");
		FormUtil.dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {

				String url = FormUtil.getHostPageBaseURL();
				if(!Utils.urlContainsHttp(FormEntryContext.getFormDownloadUrl()))
					url += FormEntryContext.getFormDownloadUrl();
				else{
					url += FormEntryConstants.FORM_DOWNLOAD_URL_SUFFIX;
					url = Utils.addParameter(url, FormEntryConstants.PARAM_NAME_REDIRECT_URL, FormEntryContext.getFormDownloadUrl());
				}

				String userName = FormEntryContext.getUserName();
				if(userName != null && userName.trim().length() > 0)
					url = Utils.urlAppendNamePassword(url, FormEntryContext.getUserName(), FormEntryContext.getPassword());

				RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,URL.encode(url));
				//builder.setHeader("Access-Control-Allow-Origin", "http://open-data-kit.appspot.com/formList");
				try{
					builder.sendRequest(null, new RequestCallback(){
						public void onResponseReceived(Request request, Response response){
							if(response.getStatusCode() != Response.SC_OK){
								FormUtil.displayReponseError(response);
								return;
							}

							String xml = response.getText();
							if(xml == null || xml.length() == 0){
								FormUtil.dlg.hide();
								Window.alert(LocaleText.get("noDataFound"));
								return;
							}

							formDownloadListener.onFormDefListDownloaded(Utils.getFormDefList(xml));

							FormUtil.dlg.hide();
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
		});
	}


	public void downloadForm(final String id, String name, final FormDefDownloadListener formDownloadListener){
		FormUtil.dlg.setText("Downloading form: " + name);
		FormUtil.dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {

				String url = FormUtil.getHostPageBaseURL();
				if(!Utils.urlContainsHttp(id)){
					url += FormEntryContext.getFormDownloadUrl();
					url = Utils.urlAppendNamePassword(url, FormEntryContext.getUserName(), FormEntryContext.getPassword());
					url = Utils.addParameter(url, "formId", id);
				}
				else{
					url += FormEntryConstants.FORM_DOWNLOAD_URL_SUFFIX;
					url = Utils.addParameter(url, FormEntryConstants.PARAM_NAME_REDIRECT_URL, id);
				}

				RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,URL.encode(url));

				try{
					builder.sendRequest(null, new RequestCallback(){
						public void onResponseReceived(Request request, Response response){

							if(response.getStatusCode() != Response.SC_OK){
								FormUtil.displayReponseError(response);
								return;
							}

							String xml = response.getText();
							if(xml == null || xml.length() == 0){
								FormUtil.dlg.hide();
								Window.alert(LocaleText.get("noDataFound"));
								return;
							}

							FormEntryContext.getDatabaseManager().saveFormDef(id,xml);
							formDownloadListener.onFormDefDownloaded(id);

							//With this open, progress message is only displayed for the first form but not others.
							//FormUtil.dlg.hide();
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
		});
	}


	public void onLoginInfo(String name, String password){
		FormEntryContext.setUserName(name);
		FormEntryContext.setPassword(password);

		downloadForms();
	}
}
