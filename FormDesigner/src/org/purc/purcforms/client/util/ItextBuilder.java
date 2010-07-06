package org.purc.purcforms.client.util;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.purc.purcforms.client.Context;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.Locale;
import org.purc.purcforms.client.xforms.XmlUtil;
import org.purc.purcforms.client.xpath.XPathExpression;

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
public class ItextBuilder {

	public static final String ATTRIBUTE_NAME_UNIQUE_ID = "uniqueid";


	/**
	 * 
	 * @param formDef
	 */
	public static void build(FormDef formDef){

		Element modelNode = XmlUtil.getNode(formDef.getDoc().getDocumentElement(),"model");
		assert(modelNode != null); //we must have a model in an xform.

		Element itextNode = XmlUtil.getNode(modelNode,"itext");
		if(itextNode != null)
			itextNode.getParentNode().removeChild(itextNode);

		List<Locale> locales = Context.getLocales();
		if(locales == null)
			return;

		itextNode = formDef.getDoc().createElement("itext");
		modelNode.appendChild(itextNode);
		
		for(Locale locale : locales)
			build(formDef, locale.getKey(), itextNode);
	}


	private static void build(FormDef formDef, String localeKey, Element itextNode){
		Element translationNode = formDef.getDoc().createElement("translation");
		translationNode.setAttribute("lang", localeKey);
		itextNode.appendChild(translationNode);

		//Map for detecting duplicates in itext. eg if id yes=Yes , we should not have information more than once.
		HashMap<String,String> duplicatesMap = new HashMap<String, String>();

		Element rootNode = formDef.getLanguageNode();
		NodeList nodes = rootNode.getChildNodes();
		for(int index = 0; index < nodes.getLength(); index++){
			Node node = nodes.item(index);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;

			String value = ((Element)node).getAttribute("value");
			String id = /*((Element)node).getAttribute("id");*/ FormDesignerUtil.getXmlTagName(value);

			if(id == null || id.trim().length() == 0)
				continue;

			/*textNode.setAttribute("id", id);

			Element valueNode = doc.createElement("value");
			textNode.appendChild(valueNode);

			valueNode.appendChild(doc.createTextNode(value));*/

			String xpath = ((Element)node).getAttribute("xpath");
			Vector result = new XPathExpression(formDef.getDoc(), xpath).getResult();
			if(result != null && result.size() > 0){
				Element targetNode = (Element)result.get(0);

				int pos = xpath.lastIndexOf('@');
				if(pos > 0 && xpath.indexOf('=',pos) < 0){
					//String attributeName = xpath.substring(pos + 1, xpath.indexOf(']',pos));
					//targetNode.setAttribute(attributeName, value);

					//xpath = FormUtil.getNodePath(formDef.getPageAt(0).getGroupNode());

					NodeList titles = formDef.getDoc().getElementsByTagName("h:title");
					if(titles == null || titles.getLength() == 0)
						titles = formDef.getDoc().getElementsByTagName("title");
					if(titles != null && titles.getLength() > 0){
						xpath = FormUtil.getNodePath(titles.item(0));
						((Element)titles.item(0)).setAttribute("ref", "jr:itext('" + id + "')");
					}
					else
						continue;
				}
				else{
					targetNode.setAttribute("ref", "jr:itext('" + id + "')");

					//remove text.
					removeAllChildNodes(targetNode);
				}

				if(result.size() > 1){
					Window.alert(id + " is not uniquely indentified by xpath: " + xpath + " result count: " + result.size());
					return;
				}
				
				assert(result.size() == 1); //each xpath expression should point to not more than one node.

				/*ItextModel itextModel = new ItextModel();
				itextModel.set("xpath", xpath);
				itextModel.set("id", id);
				itextModel.set(localeKey, value);
				list.add(itextModel);*/

				if(id == null || id.trim().length() == 0)
					continue;

				//Skip the steps below if we have already processed this itext id.
				if(duplicatesMap.containsKey(id))
					continue;
				else
					duplicatesMap.put(id, id);

				addTextNode(formDef.getDoc(), translationNode, xpath,id, value, localeKey, ((Element)node).getAttribute(ATTRIBUTE_NAME_UNIQUE_ID)); //getKey()??????
			}
			else if(index == 0){
				//NodeList titles = doc.getElementsByTagName("title");
				//if(titles != null && titles.getLength() > 0)
				//	addTextNode(doc,translationNode, list,FormUtil.getNodePath(titles.item(0)),id,value,locale.getKey());
			}
		}
	}


	/**
	 * Creates a new itext node with its text value for a given 
	 * xforms documet's translation node.
	 * 
	 * @param doc the xfrorms document.
	 * @param translationNode the translation node.
	 * @param list the gxt grid model list.
	 * @param xpath the xpath expression pointing to the node text that this itext represents.
	 * @param id the itext id.
	 * @param value the itext value of the given id.
	 * @param localeKey the locale key
	 */
	private static void addTextNode(Document doc, Element translationNode, String xpath, String id, String value, String localeKey, String uniqueId){
		if(value.trim().length() == 0)
			return;

		Element textNode = doc.createElement("text");
		translationNode.appendChild(textNode);

		textNode.setAttribute("id", id);

		Element valueNode = doc.createElement("value");
		textNode.appendChild(valueNode);

		valueNode.appendChild(doc.createTextNode(value));
	}


	/**
	 * Removes all child nodes for a give  node.
	 * 
	 * @param node the node whose child nodes to remove.
	 */
	private static void removeAllChildNodes(Element node){
		while(node.getChildNodes().getLength() > 0)
			node.removeChild(node.getChildNodes().item(0));
	}
}
