package org.purc.purcforms.client.cmd;

import java.util.Date;

import org.purc.purcforms.client.FormEntryConstants;
import org.purc.purcforms.client.FormEntryContext;
import org.purc.purcforms.client.listener.DataLoadListener;
import org.purc.purcforms.client.listener.FormSubmitCancelListener;
import org.purc.purcforms.client.model.FormDataHeader;
import org.purc.purcforms.client.util.Utils;
import org.purc.purcforms.client.xforms.XmlUtil;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;



/**
 * 
 * @author daniel
 *
 */
public class EditedFormDataSaveCmd implements DataLoadListener {

	private String id;
	private FormSubmitCancelListener contentListener;
	private String dataXml;
	
	
	public EditedFormDataSaveCmd(String id, String xml, FormSubmitCancelListener contentListener){
		this.id = id;
		this.contentListener = contentListener;
		this.dataXml = xml;
		
		FormEntryContext.getDatabaseManager().loadFormDataList(FormEntryContext.getFormDefId(), this);
	}
	
	
	public void onDataReceived(String data){
		assert(data != null);
		
		FormDataHeader formDataHeader = new FormDataHeader();
		FormEntryContext.getDatabaseManager().saveFormDataList(FormEntryContext.getFormDefId(), getFormDataListXml(data, formDataHeader));
		FormEntryContext.getDatabaseManager().saveFormData(id, dataXml);
		
		contentListener.onExistingFormSubmitted(formDataHeader);
	}
	
	
	private String getFormDataListXml(String xml, FormDataHeader formDataHeader){
		Document doc = XmlUtil.getDocument(xml);
		NodeList nodes = doc.getElementsByTagName(FormEntryConstants.NODE_NAME_DATA);
		assert(nodes != null);
			
		for(int index = 0; index < nodes.getLength(); index++){
			Node node = nodes.item(index);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			if(((Element)node).getAttribute(FormEntryConstants.ATTRIBUTE_NAME_ID).equals(id)){
				formDataHeader.setId(((Element)node).getAttribute(FormEntryConstants.ATTRIBUTE_NAME_ID));
				formDataHeader.setDateLastChanged(new Date());
				formDataHeader.setDateCreated(new Date(Long.parseLong(((Element)node).getAttribute(FormEntryConstants.ATTRIBUTE_NAME_DATE_CREATED))));
				
				Document dataDoc = XmlUtil.getDocument(dataXml);
				String descTemplate = dataDoc.getDocumentElement().getAttribute("description-template");
				String description = Utils.getDescriptionTemplate(dataDoc.getDocumentElement(), descTemplate);
				formDataHeader.setDescription(description == null ? "" : description);
				
				((Element)node).setAttribute(FormEntryConstants.ATTRIBUTE_NAME_DESCRIPTION, formDataHeader.getDescription());
				((Element)node).setAttribute(FormEntryConstants.ATTRIBUTE_NAME_DATE_LAST_CHANGED, String.valueOf(formDataHeader.getDateLastChanged().getTime()));
				break;
			}
		}
		
		return doc.toString();
	}
}
