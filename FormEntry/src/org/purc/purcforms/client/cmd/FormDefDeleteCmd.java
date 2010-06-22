package org.purc.purcforms.client.cmd;

import java.util.List;

import org.purc.purcforms.client.FormEntryConstants;
import org.purc.purcforms.client.FormEntryContext;
import org.purc.purcforms.client.listener.DataLoadListener;
import org.purc.purcforms.client.model.KeyValue;
import org.purc.purcforms.client.xforms.XmlUtil;

import com.google.gwt.user.client.Window;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;



/**
 * 
 * @author daniel
 *
 */
public class FormDefDeleteCmd implements DataLoadListener {

	private String id;
	private boolean loadingDefList;

	public FormDefDeleteCmd(String id){
		this.id = id;
		this.loadingDefList = false;
		FormEntryContext.getDatabaseManager().loadFormDataList(id, this);
	}

	public void onDataReceived(String data){
		if(loadingDefList)
			removeFormDefFromList(data);
		else{
			if(data != null){
				Window.alert("Please first delete all data collected for this form.");
				return;
			}

			FormEntryContext.getDatabaseManager().deleteFormDef(id);

			List<KeyValue> formList = FormEntryContext.getFormDefList();
			for(KeyValue keyValue : formList){
				if(id.equals(keyValue.getKey())){
					formList.remove(keyValue);
					break;
				}
			}
			FormEntryContext.setFormDefList(formList);

			loadingDefList = true;
			FormEntryContext.getDatabaseManager().loadFormDefList(this);
		}
	}
	
	private void removeFormDefFromList(String xml){
		Document doc = XmlUtil.getDocument(xml);
		NodeList nodes = doc.getElementsByTagName(FormEntryConstants.NODE_NAME_XFORM);
		for(int index = 0; index < nodes.getLength(); index++){
			Node node = nodes.item(index);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if(id.equals(((Element)node).getAttribute(FormEntryConstants.ATTRIBUTE_NAME_ID))){
				node.getParentNode().removeChild(node);
				FormEntryContext.getDatabaseManager().saveFormDefList(doc.toString());
				return;
			}
		}
	}
}
