package org.purc.purcforms.client.util;

import java.util.Vector;

import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.xforms.XmlUtil;
import org.purc.purcforms.client.xpath.XPathExpression;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;


/**
 * 
 * @author daniel
 *
 */
public class ITextUtil {

	public static void updateItextBlock(Document doc, FormDef formDef){
		
		Element modelNode = XmlUtil.getNode(doc.getDocumentElement(),"model");
		assert(modelNode != null); //we must have a model in an xform.
		Element itextNode = XmlUtil.getNode(modelNode,"itext");
		if(itextNode == null){
			itextNode = doc.createElement("itext");
			modelNode.appendChild(itextNode);
		}
		else{
			NodeList kids = itextNode.getChildNodes();
			if(kids != null){
				while(kids.getLength() > 0)
					itextNode.removeChild(kids.item(0));
			}	
		}
		
		Element translationNode = doc.createElement("translation");
		itextNode.appendChild(translationNode);
		
		Element rootNode = formDef.getLanguageNode();
		NodeList nodes = rootNode.getChildNodes();
		for(int index = 0; index < nodes.getLength(); index++){
			Node node = nodes.item(index);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			Element textNode = doc.createElement("text");
			translationNode.appendChild(textNode);
			
			String value = ((Element)node).getAttribute("value");
			String id = FormDesignerUtil.getXmlTagName(value);
			textNode.setAttribute("id", id);
			
			Element valueNode = doc.createElement("value");
			textNode.appendChild(valueNode);
			
			valueNode.appendChild(doc.createTextNode(value));
			
			String xpath = ((Element)node).getAttribute("xpath");
			Vector result = new XPathExpression(doc, xpath).getResult();
			if(result != null && result.size() > 0){
				Element targetNode = (Element)result.get(0);
				targetNode.setAttribute("ref", "jr:itext('" + id + "')");
				assert(result.size() == 1); //each xpath expression should point to not more than one node.
			}
		}
	}
}
