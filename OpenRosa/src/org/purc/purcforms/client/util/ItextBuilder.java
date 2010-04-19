package org.purc.purcforms.client.util;

import java.util.List;
import java.util.Vector;

import org.purc.purcforms.client.Context;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.ItextModel;
import org.purc.purcforms.client.model.Locale;
import org.purc.purcforms.client.xforms.XmlUtil;
import org.purc.purcforms.client.xpath.XPathExpression;

import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;


/**
 * Builds an itext block in an xforms document.
 * 
 * @author daniel
 *
 */
public class ItextBuilder {

	public static void updateItextBlock(Document doc, FormDef formDef, ListStore<ItextModel> list){
		/*List<Locale> locales = Context.getLocales();
		if(locales == null)
			return;

		for(Locale locale : locales)
			updateItextBlock(doc,formDef,list,locale);*/

		updateItextBlock(doc,formDef,list,Context.getLocale());
	}


	public static void updateItextBlock(Document doc, FormDef formDef, ListStore<ItextModel> list, Locale locale){

		Element modelNode = XmlUtil.getNode(doc.getDocumentElement(),"model");
		assert(modelNode != null); //we must have a model in an xform.
		Element itextNode = XmlUtil.getNode(modelNode,"itext");
		if(itextNode == null){
			itextNode = doc.createElement("itext");
			modelNode.appendChild(itextNode);
		}
		else{
			NodeList translations = itextNode.getElementsByTagName("translation");

			for(int index = 0; index < translations.getLength(); index++){
				Node node = translations.item(index);
				if(node.getNodeType() != Node.ELEMENT_NODE)
					continue;

				if(((Element)node).getAttribute("lang").equalsIgnoreCase(locale.getName())){
					itextNode.removeChild(node);
					break;
				}
			}
		}

		Element translationNode = doc.createElement("translation");
		translationNode.setAttribute("lang", locale.getName());
		itextNode.appendChild(translationNode);

		Element rootNode = formDef.getLanguageNode();
		NodeList nodes = rootNode.getChildNodes();
		for(int index = 0; index < nodes.getLength(); index++){
			Node node = nodes.item(index);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;

			/*Element textNode = doc.createElement("text");
			translationNode.appendChild(textNode);*/

			String value = ((Element)node).getAttribute("value");
			String id = FormDesignerUtil.getXmlTagName(value);
			/*textNode.setAttribute("id", id);

			Element valueNode = doc.createElement("value");
			textNode.appendChild(valueNode);

			valueNode.appendChild(doc.createTextNode(value));*/

			String xpath = ((Element)node).getAttribute("xpath");
			Vector result = new XPathExpression(doc, xpath).getResult();
			if(result != null && result.size() > 0){
				Element targetNode = (Element)result.get(0);

				int pos = xpath.lastIndexOf('@');
				if(pos > 0 && xpath.indexOf('=',pos) < 0){
					//String attributeName = xpath.substring(pos + 1, xpath.indexOf(']',pos));
					//targetNode.setAttribute(attributeName, value);
					xpath = FormUtil.getNodePath(formDef.getPageAt(0).getGroupNode());
				}
				else{
					targetNode.setAttribute("ref", "jr:itext('" + id + "')");

					//remove text.
					removeAllChildNodes(targetNode);
				}

				assert(result.size() == 1); //each xpath expression should point to not more than one node.

				/*ItextModel itextModel = new ItextModel();
				itextModel.set("xpath", xpath);
				itextModel.set("id", id);
				itextModel.set(localeKey, value);
				list.add(itextModel);*/

				addTextNode(doc,translationNode, list,xpath,id,value,locale.getKey());
			}
			else if(index == 0){
				NodeList titles = doc.getElementsByTagName("title");
				if(titles != null && titles.getLength() > 0)
					addTextNode(doc,translationNode, list,FormUtil.getNodePath(titles.item(0)),id,value,locale.getKey());
			}
		}
	}

	private static void addTextNode(Document doc, Element translationNode, ListStore<ItextModel> list, String xpath, String id, String value, String localeKey){
		if(value.trim().length() == 0)
			return;
		
		Element textNode = doc.createElement("text");
		translationNode.appendChild(textNode);

		//String value = ((Element)node).getAttribute("value");
		//String id = FormDesignerUtil.getXmlTagName(value);
		textNode.setAttribute("id", id);

		Element valueNode = doc.createElement("value");
		textNode.appendChild(valueNode);

		valueNode.appendChild(doc.createTextNode(value));

		ItextModel itextModel = new ItextModel();
		itextModel.set("xpath", xpath);
		itextModel.set("id", id);
		itextModel.set(localeKey, value);
		list.add(itextModel);
	}

	private static void removeAllChildNodes(Element node){
		while(node.getChildNodes().getLength() > 0)
			node.removeChild(node.getChildNodes().item(0));
	}


	public static void updateItextBlock(Document doc, FormDef formDef, List<ItextModel> list){
		List<Locale> locales = Context.getLocales();
		if(locales == null)
			return;

		for(Locale locale : locales)
			updateItextBlock(doc,formDef,list,locale);
	}

	private static void updateItextBlock(Document doc, FormDef formDef, List<ItextModel> list, Locale locale){

		Element modelNode = XmlUtil.getNode(doc.getDocumentElement(),"model");
		assert(modelNode != null); //we must have a model in an xform.
		Element itextNode = XmlUtil.getNode(modelNode,"itext");
		if(itextNode == null){
			itextNode = doc.createElement("itext");
			modelNode.appendChild(itextNode);
		}
		else{
			NodeList translations = itextNode.getElementsByTagName("translation");

			for(int index = 0; index < translations.getLength(); index++){
				Node node = translations.item(index);
				if(node.getNodeType() != Node.ELEMENT_NODE)
					continue;

				if(((Element)node).getAttribute("lang").equalsIgnoreCase(locale.getName())){
					itextNode.removeChild(node);
					break;
				}
			}
		}

		Element translationNode = doc.createElement("translation");
		translationNode.setAttribute("lang", locale.getName());
		itextNode.appendChild(translationNode);

		for(ItextModel itext : list){
			Element textNode = doc.createElement("text");
			translationNode.appendChild(textNode);

			String id = itext.get("id");
			textNode.setAttribute("id", id);

			String value = itext.get(locale.getKey());
			Element valueNode = doc.createElement("value");
			textNode.appendChild(valueNode);
			valueNode.appendChild(doc.createTextNode(value));

			String xpath = (String)itext.get("xpath");
			Vector result = new XPathExpression(doc, xpath).getResult();
			if(result != null && result.size() > 0){
				Element targetNode = (Element)result.get(0);
				targetNode.setAttribute("ref", "jr:itext('" + id + "')");
				assert(result.size() == 1); //each xpath expression should point to not more than one node.

				if(locale.getKey().equalsIgnoreCase(Context.getDefaultLocale().getKey()))
					XmlUtil.setTextNodeValue(targetNode, value);
			}
		}
	}
}
