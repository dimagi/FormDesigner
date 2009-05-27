package org.purc.purcforms.client.util;

import java.util.Vector;

import org.purc.purcforms.client.xforms.XformConverter;
import org.purc.purcforms.client.xpath.XPathExpression;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;


/**
 * 
 * @author daniel
 *
 */
public class LanguageUtil {

	private static final String NODE_NAME_XFORM = "xform";
	private static final String NODE_NAME_FORM = "Form";
	
	public static String translate(String srcXml, String languageXml, boolean xform){
		return translate(XMLParser.parse(srcXml),languageXml,xform);
	}
	
	public static String translate(Document doc, String languageXml, boolean xform){

		Document lngDoc = XMLParser.parse(languageXml);
		NodeList nodes = lngDoc.getDocumentElement().getChildNodes();
		if(nodes == null)
			return null;

		for(int index = 0; index < nodes.getLength(); index++){
			Node node = nodes.item(index);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if((xform && node.getNodeName().equalsIgnoreCase(NODE_NAME_XFORM)) ||
					(!xform && node.getNodeName().equalsIgnoreCase(NODE_NAME_FORM)))
				return translate(doc,node);
		}

		return null;
	}

	private static String translate(Document doc, Node parent){
		NodeList nodes = parent.getChildNodes();
		for(int index = 0; index < nodes.getLength(); index++){
			Node node = nodes.item(index);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;

			String xpath = ((Element)node).getAttribute(XformConverter.ATTRIBUTE_NAME_XPATH);
			String value = ((Element)node).getAttribute(XformConverter.ATTRIBUTE_NAME_VALUE);
			if(xpath == null || value == null)
				continue;

			Vector result = new XPathExpression(doc, xpath).getResult();
			if(result != null && result.size() > 0){
				Element targetNode = (Element)result.get(0);
				int pos = xpath.lastIndexOf('@');
				if(pos > 0 && xpath.indexOf('=',pos) < 0){
					String attributeName = xpath.substring(pos + 1, xpath.indexOf(']',pos));
					targetNode.setAttribute(attributeName, value);
				}
				else
					XformConverter.setTextNodeValue(targetNode, value);
			}
		}
		return doc.toString();
	}
	
	public static String getLocaleText(Document doc, String nodeName){
		NodeList nodes = doc.getDocumentElement().getChildNodes();
		for(int index = 0; index < nodes.getLength(); index++){
			Node node = nodes.item(index);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if(node.getNodeName().equalsIgnoreCase(nodeName))
				return node.toString();
		}

		return null;
	}
	
	public static String getXformsLocaleText(Document doc){
		return getLocaleText(doc, NODE_NAME_XFORM);
	}
	
	public static String getLayoutLocaleText(Document doc){
		return getLocaleText(doc, NODE_NAME_FORM);
	}
	
	public static Document createNewLanguageDoc(){
		com.google.gwt.xml.client.Document doc = XMLParser.createDocument();
		Element rootNode = doc.createElement("LanguageText");
		rootNode.setAttribute("lang", "en");
		doc.appendChild(rootNode);
		return doc;
	}
	
	public static String getLocaleText(String xform, String layout){
		Document doc = createNewLanguageDoc();
		
		if(xform != null && xform.trim().length() > 0)
			doc.getDocumentElement().appendChild(XMLParser.parse(xform).getDocumentElement());
		
		if(layout != null && layout.trim().length() > 0)
			doc.getDocumentElement().appendChild(XMLParser.parse(layout).getDocumentElement());
		
		return doc.toString();
	}
}
