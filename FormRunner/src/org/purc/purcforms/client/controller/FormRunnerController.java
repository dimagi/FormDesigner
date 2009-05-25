package org.purc.purcforms.client.controller;

import java.util.ArrayList;
import java.util.List;

import org.purc.purcforms.client.PurcConstants;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.OptionDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.view.ErrorDialog;
import org.purc.purcforms.client.view.ProgressDialog;
import org.purc.purcforms.client.widget.FormRunnerWidget;
import org.purc.purcforms.client.widget.RuntimeWidgetWrapper;
import org.purc.purcforms.client.widget.WidgetEx;
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

	private final char FIELD_SEPARATOR = '|'; //TODO These may need to be changed.
	private final char RECORD_SEPARATOR = '$';

	private FormRunnerWidget formRunner;
	private ProgressDialog dlg = new ProgressDialog();
	private String xformXml;
	private String layoutXml;
	private int formId;
	private int entityId;
	private List<RuntimeWidgetWrapper> externalSourceWidgets;
	private int externalSourceWidgetIndex = 0;

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
					if(xml == null || xml.length() == 0){
						Window.alert(LocaleText.get("noDataFound"));
						return;
					}

					xformXml = null; layoutXml = null;

					int pos = xml.indexOf(PurcConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR);
					if(pos > 0){
						xformXml = xml.substring(0,pos);
						layoutXml = xml.substring(pos+PurcConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR.length(), xml.length());
						openForm();
					}
					else
						Window.alert(LocaleText.get("noFormLayout"));
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
		dlg.setText(LocaleText.get("openingForm"));
		dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {
				try{
					externalSourceWidgets = new ArrayList<RuntimeWidgetWrapper>();
					FormDef formDef = XformConverter.fromXform2FormDef(xformXml);
					formRunner.loadForm(formDef, layoutXml,externalSourceWidgets);
					
					if(externalSourceWidgets.size() > 0)
						fillExternalSourceWidget(externalSourceWidgets.get(externalSourceWidgetIndex++));
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
							text += LocaleText.get("causedBy");
						}
					}

					ErrorDialog dialogBox = new ErrorDialog();
					dialogBox.setText(LocaleText.get("unexpectedFailure"));
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
					Window.alert(LocaleText.get("formSubmitSuccess"));

					String url = FormUtil.getHostPageBaseURL();
					url += FormUtil.getAfterSubmitUrlSuffix();

					if(FormUtil.appendEntityIdAfterSubmit()){
						url += FormUtil.getEntityIdName();
						url += "=" + entityId;
					}

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
	
	private void fillExternalSourceWidget(RuntimeWidgetWrapper widget){
		String url = FormUtil.getHostPageBaseURL();
		url += FormUtil.getExternalSourceUrlSuffix();
		url += WidgetEx.WIDGET_PROPERTY_EXTERNALSOURCE + "="+widget.getExternalSource();
		url += "&" + WidgetEx.WIDGET_PROPERTY_DISPLAYFIELD + "="+widget.getDisplayField();
		url += "&" + WidgetEx.WIDGET_PROPERTY_VALUEFIELD + "="+widget.getValueField();
		
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,URL.encode(url));

		try{
			builder.sendRequest(null, new RequestCallback(){
				public void onResponseReceived(Request request, Response response){
					fillWidgetValues(response.getText());
					fillNextExternalSourceWidget();
				}

				public void onError(Request request, Throwable exception){
					exception.printStackTrace();
					Window.alert(exception.getMessage());
					fillNextExternalSourceWidget();
				}
			});
		}
		catch(RequestException ex){
			ex.printStackTrace();
			Window.alert(ex.getMessage());
			fillNextExternalSourceWidget();
		}
	}
	
	private void fillNextExternalSourceWidget(){
		if(externalSourceWidgetIndex < externalSourceWidgets.size())
			fillExternalSourceWidget(externalSourceWidgets.get(externalSourceWidgetIndex++));
		else{
			externalSourceWidgets.clear();
			externalSourceWidgetIndex = 0;
		}
	}
	
	private void fillWidgetValues(String text){
		if(text == null)
			return;
			
		RuntimeWidgetWrapper widget = externalSourceWidgets.get(externalSourceWidgetIndex-1);
		QuestionDef questionDef = widget.getQuestionDef();
		questionDef.clearOptions();
		
		String displayField = null, valueField = null; int beginIndex = 0;
		int pos = text.indexOf(FIELD_SEPARATOR,beginIndex);
		while(pos > 0){
			displayField = text.substring(beginIndex, pos);
			
			beginIndex = pos+1;
			pos = text.indexOf(RECORD_SEPARATOR, beginIndex);
			if(pos > 0){
				valueField = text.substring(beginIndex, pos);
				questionDef.addOption(new OptionDef(questionDef.getOptionCount()+1,displayField,valueField,questionDef));
				beginIndex = pos+1;
				pos = text.indexOf(FIELD_SEPARATOR,beginIndex);
			}
			else{
				valueField = text.substring(beginIndex);
				questionDef.addOption(new OptionDef(questionDef.getOptionCount()+1,displayField,valueField,questionDef));
			}
		}
		
		widget.loadQuestion();
	}
	
	//Recursion fails here for very big lists and we are therefore using iteration
	/*private void fillWidgetValues(String text, int beginIndex, QuestionDef questionDef){
		String displayField = null, valueField = null;
		int pos = text.indexOf(FIELD_SEPARATOR,beginIndex);
		if(pos > 0){
			displayField = text.substring(beginIndex, pos);
			
			beginIndex = pos+1;
			pos = text.indexOf(RECORD_SEPARATOR, beginIndex);
			if(pos > 0){
				valueField = text.substring(beginIndex, pos);
				questionDef.addOption(new OptionDef(questionDef.getOptionCount()+1,displayField,valueField,questionDef));
				fillWidgetValues(text,pos+1,questionDef);
			}
			else{
				valueField = text.substring(beginIndex);
				questionDef.addOption(new OptionDef(questionDef.getOptionCount()+1,displayField,valueField,questionDef));
			}
		}
	}*/
}
