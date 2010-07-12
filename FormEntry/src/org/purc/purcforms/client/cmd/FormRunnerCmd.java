package org.purc.purcforms.client.cmd;

import java.util.Date;

import org.purc.purcforms.client.FormEntryConstants;
import org.purc.purcforms.client.FormEntryContext;
import org.purc.purcforms.client.controller.SubmitListener;
import org.purc.purcforms.client.listener.DataLoadListener;
import org.purc.purcforms.client.listener.FormSubmitCancelListener;
import org.purc.purcforms.client.model.FormDataHeader;
import org.purc.purcforms.client.util.Utils;
import org.purc.purcforms.client.widget.FormRunnerWidget;
import org.purc.purcforms.client.xforms.XmlUtil;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;


public class FormRunnerCmd implements SubmitListener, DataLoadListener{

	//private FormRunnerWidget formRunner;
	private String dataXml;
	private FormSubmitCancelListener listener;
	
	public FormRunnerCmd(FormRunnerWidget formRunner, FormSubmitCancelListener contentListener){
		//this.formRunner = formRunner;
		this.listener = contentListener;
		formRunner.setSubmitListener(this);
	}
	
	public void onSubmit(String xml){
		String id = FormEntryContext.getFormDataId();
		if(id == null){
			this.dataXml = xml;
			FormEntryContext.getDatabaseManager().loadFormDataList(FormEntryContext.getFormDefId(), this);
		}
		else
			new EditedFormDataSaveCmd(id, xml, listener);
	}
	
	public void onCancel(){
		listener.onFormCancelled();
	}
	
	public void onDataReceived(String data){
		String id = String.valueOf(new java.util.Date().getTime());
		
		FormDataHeader formDataHeader = new FormDataHeader();
		FormEntryContext.getDatabaseManager().saveFormDataList(FormEntryContext.getFormDefId(), getFormDataListXml(id,data,formDataHeader));
		FormEntryContext.getDatabaseManager().saveFormData(id, dataXml);
		
		listener.onNewFormSubmitted(formDataHeader);
	}
	
	private String getFormDataListXml(String id, String xml, FormDataHeader formDataHeader){
		Document doc = null;
		
		if(xml == null){
			doc = XMLParser.createDocument();
			Element element = doc.createElement(FormEntryConstants.NODE_NAME_DATA_LIST);
			doc.appendChild(element);
		}
		else
			doc = XmlUtil.getDocument(xml);
		
		Document dataDoc = XmlUtil.getDocument(dataXml);
		String descTemplate = dataDoc.getDocumentElement().getAttribute("description-template");
		String description = Utils.getDescriptionTemplate(dataDoc.getDocumentElement(), descTemplate);
		
		formDataHeader.setId(id);
		formDataHeader.setDescription(description == null ? "" : description);
		formDataHeader.setDateCreated(new Date());
		formDataHeader.setDateLastChanged(new Date());
		
		Element element = doc.createElement(FormEntryConstants.NODE_NAME_DATA);
		element.setAttribute(FormEntryConstants.ATTRIBUTE_NAME_ID, id);
		element.setAttribute(FormEntryConstants.ATTRIBUTE_NAME_DESCRIPTION, formDataHeader.getDescription());
		element.setAttribute(FormEntryConstants.ATTRIBUTE_NAME_DATE_CREATED, String.valueOf(formDataHeader.getDateCreated().getTime()));
		element.setAttribute(FormEntryConstants.ATTRIBUTE_NAME_DATE_LAST_CHANGED, String.valueOf(formDataHeader.getDateLastChanged().getTime()));
		doc.getDocumentElement().appendChild(element);
		
		return doc.toString();
	}
}
