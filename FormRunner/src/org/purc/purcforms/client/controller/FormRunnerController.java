package org.purc.purcforms.client.controller;

import org.purc.purcforms.client.PurcConstants;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.view.ErrorDialog;
import org.purc.purcforms.client.view.ProgressDialog;
import org.purc.purcforms.client.widget.FormRunnerWidget;
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
public class FormRunnerController implements SubmitListener{

	private FormRunnerWidget formRunner;
	private ProgressDialog dlg = new ProgressDialog();
	private String xformXml;
	private String layoutXml;
	private int formId;
	private int entityId;

	public FormRunnerController(FormRunnerWidget formRunner){
		this.formRunner = formRunner;
	}

	public void loadForm(int formId, int entityId){
		this.formId = formId;
		this.entityId = entityId;
		
		//"http://127.0.0.1:8080/openmrs/moduleServlet/xforms/xformDownload?target=xformentry&formId="+formId+"&patientId="+patientId+"&contentType=xml&uname=Guyzb&pw=daniel123"
		String url = FormUtil.getHostPageBaseURL();
		url += FormUtil.getEntityFormDefDownloadUrlSuffix();
		url += FormUtil.getFormIdName()+"="+this.formId;
		url += "&" + FormUtil.getEntityIdName() + "="+this.entityId;
		
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,URL.encode(url));

		try{
			builder.sendRequest(null, new RequestCallback(){
				public void onResponseReceived(Request request, Response response){
					String xml = response.getText();
					xformXml = null; layoutXml = null;

					int pos = xml.indexOf(PurcConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR);
					if(pos > 0){
						xformXml = xml.substring(0,pos);
						layoutXml = xml.substring(pos+PurcConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR.length(), xml.length());
						openForm();
					}
					else
						Window.alert("No form layout found. Please first design and save the form.");
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

	public void openForm() {
		dlg.setText("Opening Form");
		dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {
				try{
					FormDef formDef = XformConverter.fromXform2FormDef(xformXml);
					formRunner.loadForm(formDef, layoutXml);
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
					dialogBox.setBody(s);//("<pre>" + text + "</pre>");
					dialogBox.setCallStack(text);
					dialogBox.center();
				}
				dlg.hide();	
			}
		});
	}
	
	public void onSubmit(String xml){
		
		//"http://127.0.0.1:8080/openmrs/module/xforms/xformDataUpload.form"
		String url = FormUtil.getHostPageBaseURL();
		url += FormUtil.getFormDataUploadUrlSuffix();
		
		RequestBuilder builder = new RequestBuilder(RequestBuilder.POST,URL.encode(url));

		try{
			builder.sendRequest(xml, new RequestCallback(){
				public void onResponseReceived(Request request, Response response){
					Window.alert("Form Data Submitted Successfully");
					
					String url = FormUtil.getHostPageBaseURL();
					url += FormUtil.getAfterSubmitUrlSuffix();
					url += FormUtil.getEntityIdName();
					url += "=" + entityId;
					
					Window.Location.replace(url); //"http://127.0.0.1:8080/openmrs/patientDashboard.form?patientId=13"
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
}
