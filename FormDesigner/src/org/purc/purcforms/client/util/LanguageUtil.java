package org.purc.purcforms.client.util;

import java.util.HashMap;
import java.util.Vector;

import org.purc.purcforms.client.Context;
import org.purc.purcforms.client.xforms.XformConstants;
import org.purc.purcforms.client.xforms.XformParser;
import org.purc.purcforms.client.xforms.XmlUtil;
import org.purc.purcforms.client.xpath.XPathExpression;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;


/**
 * This class is responsible for the translation of xforms and layout xml 
 * files in various locales.
 * 
 * @author daniel
 *
 */
public class LanguageUtil {

	public static final String NODE_NAME_XFORM = "xform";
	public static final String NODE_NAME_FORM = "Form";
	
	public static final String NODE_NAME_LANGUAGE_TEXT = "LanguageText";
	

	/**
	 * Translates xforms or layout text into a locale or language as in the locale xml.
	 * 
	 * @param srcXml the text to be translated into another locale.
	 * @param languageXml the locale xml.
	 * @param xform set to true if translating an xform, else false.
	 * @return
	 */
	public static String translate(String srcXml, String languageXml, boolean xform){
		if(srcXml == null || srcXml.trim().length() == 0 || languageXml == null || languageXml.trim().length() == 0)
			return null;

		return translate(XMLParser.parse(srcXml),languageXml,xform);
	}

	public static String translate(Document doc, String languageXml, boolean xform){

		if(doc == null)
			return null;

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

	public static String translate(Document doc, Node parent){
		NodeList nodes = parent.getChildNodes();
		for(int index = 0; index < nodes.getLength(); index++){
			Node node = nodes.item(index);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;

			String xpath = ((Element)node).getAttribute(XformConstants.ATTRIBUTE_NAME_XPATH);
			String value = ((Element)node).getAttribute(XformConstants.ATTRIBUTE_NAME_VALUE);
			if(xpath == null || value == null)
				continue;
			
			Vector result = new XPathExpression(doc, xpath).getResult();
			if(result != null){
				
				//TODO We need to uniquely identify nodes and so each xpath should
				//point to no more than one node.
				if(result.size() > 1){
					;//Window.alert(result.size()+"..........."+xpath+"............"+value);
					continue;
				}
				else if(result.size() == 0)
					;//Window.alert(result.size()+"..........."+xpath+"............"+value);
				
				for(int item = 0; item < result.size(); item++){
					Element targetNode = (Element)result.get(item);
					int pos = xpath.lastIndexOf('@');
					if(pos > 0 && xpath.indexOf('=',pos) < 0){
						String attributeName = xpath.substring(pos + 1, xpath.indexOf(']',pos));
						targetNode.setAttribute(attributeName, value);
					}
					else
						XmlUtil.setTextNodeValue(targetNode, value);
				}
			}
		}
		return doc.toString();
	}

	/*public static String translate(String srcXml, String langXml){
		if(srcXml == null || srcXml.trim().length() == 0 || langXml == null || langXml.trim().length() == 0)
			return srcXml;

		Document doc = XMLParser.parse(srcXml);
		Element parentLangNode = XMLParser.parse(langXml).getDocumentElement();

		NodeList nodes = parentLangNode.getChildNodes();
		for(int index = 0; index < nodes.getLength(); index++){
			Node node = nodes.item(index);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;

			String xpath = ((Element)node).getAttribute(XformConstants.ATTRIBUTE_NAME_XPATH);
			String value = ((Element)node).getAttribute(XformConstants.ATTRIBUTE_NAME_VALUE);
			if(xpath == null || value == null)
				continue;

			Vector result = new XPathExpression(doc, xpath).getResult();
			if(result != null && result.size() > 0){
				
				//TODO We need to uniquely identify nodes and so each xpath should
				//point to no more than one node.
				if(result.size() > 1){
					Window.alert(result.size()+"..........."+xpath+"............"+value);
					continue;
				}
				else if(result.size() == 0)
					Window.alert(result.size()+"..........."+xpath+"............"+value);
				
				Element targetNode = (Element)result.get(0);
				int pos = xpath.lastIndexOf('@');
				if(pos > 0 && xpath.indexOf('=',pos) < 0){
					String attributeName = xpath.substring(pos + 1, xpath.indexOf(']',pos));
					targetNode.setAttribute(attributeName, value);
				}
				else
					XmlUtil.setTextNodeValue(targetNode, value);
			}
		}
		return doc.toString();
	}*/

	/**
	 * Extracts xforms locale text from a combined locale document.
	 * 
	 * @param doc the locale document.
	 * @return the xforms locale text.
	 */
	public static String getXformsLocaleText(Document doc){
		return getNodeText(doc, NODE_NAME_XFORM);
	}

	/**
	 * Extracts layout locale text from a combined locale document.
	 * 
	 * @param doc the locale document.
	 * @return the layout locale text.
	 */
	public static String getLayoutLocaleText(Document doc){
		return getNodeText(doc, NODE_NAME_FORM);
	}

	/**
	 * Creates a new language of locale document.
	 * 
	 * @return the new language document.
	 */
	public static Document createNewLanguageDoc(){
		com.google.gwt.xml.client.Document doc = XMLParser.createDocument();
		doc.appendChild(doc.createProcessingInstruction("xml", "version=\"1.0\" encoding=\"UTF-8\""));
		Element rootNode = doc.createElement("LanguageText");
		rootNode.setAttribute("lang", Context.getLocale().getKey());
		doc.appendChild(rootNode);
		return doc;
	}

	/**
	 * Gets an xml text which combines locale text for the xform and layout text.
	 * 
	 * @param xform the xform locale text.
	 * @param layout the layout locale text.
	 * @return the combined text.
	 */
	public static String getLocaleText(String xform, String layout){
		Document doc = createNewLanguageDoc();

		if(xform != null && xform.trim().length() > 0)
			doc.getDocumentElement().appendChild(doc.importNode(XMLParser.parse(xform).getDocumentElement(),true));

		if(layout != null && layout.trim().length() > 0)
			doc.getDocumentElement().appendChild(doc.importNode(XMLParser.parse(layout).getDocumentElement(),true));

		return doc.toString();
	}
	
	/**
	 * Extracts text for a given node name from a document.
	 * 
	 * @param doc the document.
	 * @param nodeName the node name.
	 * @return the node text.
	 */
	private static String getNodeText(Document doc, String nodeName){
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
	
	
	public static void loadLanguageText(Integer formId, String xml, HashMap<Integer,HashMap<String,String>> languageText){
		if(xml == null || xml.trim().length() == 0)
			return;
		
		NodeList nodes = XmlUtil.getDocument(xml).getDocumentElement().getElementsByTagName(NODE_NAME_LANGUAGE_TEXT);
		if(nodes != null)
			XformParser.loadLanguageText(formId, nodes, languageText);
	}
}
