package org.purc.purcforms.client.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.purc.purcforms.client.FormEntryConstants;
import org.purc.purcforms.client.model.FormDataHeader;
import org.purc.purcforms.client.model.KeyValue;
import org.purc.purcforms.client.xforms.XmlUtil;
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
public class Utils {

	public static List<KeyValue> getFormDefList(NodeList nodes, List<KeyValue> list){
		
		for(int index = 0; index < nodes.getLength(); index++){
			Node node = nodes.item(index);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;

			list.add(new KeyValue(((Element)node).getAttribute(FormEntryConstants.ATTRIBUTE_NAME_URL), XmlUtil.getTextValue(node)));
		}
		
		return list;
	}
	
	
	public static List<KeyValue> getFormDefList(String xml){
		List<KeyValue> list = new ArrayList<KeyValue>();
		
		if(xml == null)
			return list;
		
		Document doc = XmlUtil.getDocument(xml);
		NodeList nodes = doc.getElementsByTagName(FormEntryConstants.NODE_NAME_XFORM);
		if(nodes == null || nodes.getLength() == 0){
			nodes = doc.getElementsByTagName(FormEntryConstants.NODE_NAME_FORM);
			if(nodes == null)
				return list;
			else
				return getFormDefList(nodes, list);
		}

		for(int index = 0; index < nodes.getLength(); index++){
			Node node = nodes.item(index);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;

			list.add(new KeyValue(((Element)node).getAttribute(FormEntryConstants.ATTRIBUTE_NAME_ID), ((Element)node).getAttribute(FormEntryConstants.ATTRIBUTE_NAME_NAME)));
		}
		
		return list;
	}
	
	public static String getFormDefListXml(List<KeyValue> formList){
		Document doc = XMLParser.createDocument();
		Element rootNode = doc.createElement(FormEntryConstants.NODE_NAME_XFORMS);
		doc.appendChild(rootNode);
		
		for(KeyValue keyValue : formList){
			Element node = doc.createElement(FormEntryConstants.NODE_NAME_XFORM);
			node.setAttribute(FormEntryConstants.ATTRIBUTE_NAME_ID, keyValue.getKey());
			node.setAttribute(FormEntryConstants.ATTRIBUTE_NAME_NAME, keyValue.getValue());
			rootNode.appendChild(node);
		}
		
		return doc.toString();
	}
	
	
	public static List<FormDataHeader> getFormDataList(String xml){
		List<FormDataHeader> list = new ArrayList<FormDataHeader>();
		
		if(xml == null)
			return list;
		
		Document doc = XmlUtil.getDocument(xml);
		NodeList nodes = doc.getElementsByTagName(FormEntryConstants.NODE_NAME_DATA);
		if(nodes == null)
			return list;

		for(int index = 0; index < nodes.getLength(); index++){
			Node node = nodes.item(index);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;

			FormDataHeader data = new FormDataHeader();
			
			data.setId(((Element)node).getAttribute(FormEntryConstants.ATTRIBUTE_NAME_ID));
			data.setDescription(((Element)node).getAttribute(FormEntryConstants.ATTRIBUTE_NAME_DESCRIPTION));
			data.setDateCreated(new Date(Long.parseLong(((Element)node).getAttribute(FormEntryConstants.ATTRIBUTE_NAME_DATE_CREATED))));
			data.setDateLastChanged(new Date(Long.parseLong(((Element)node).getAttribute(FormEntryConstants.ATTRIBUTE_NAME_DATE_LAST_CHANGED))));
			
			list.add(data);
		}
		
		return list;
	}
	
	
	public static void fillFormDataIdList(String xml, List<String> list, String defId, HashMap<String, String> dataDefMap){		
		Document doc = XmlUtil.getDocument(xml);
		NodeList nodes = doc.getElementsByTagName(FormEntryConstants.NODE_NAME_DATA);
		if(nodes == null)
			return;

		for(int index = 0; index < nodes.getLength(); index++){
			Node node = nodes.item(index);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			String dataId = ((Element)node).getAttribute(FormEntryConstants.ATTRIBUTE_NAME_ID);
			list.add(dataId);
			
			dataDefMap.put(dataId, defId);
		}
	}
	
	
	/**
	 * Gets the value of the description template form an xforms model document.
	 * 
	 * @param node the root node of the xml document.
	 * @param template the description template
	 * @return
	 */
	public static String getDescriptionTemplate(Element node, String template){
		if(template == null || template.trim().length() == 0)
			return null;
		
//		String s = "Where does ${name}$ come from?";
		String f,v,text = template;

		int startIndex,j,i = 0;
		do{
			startIndex = i; //mark the point where we found the first $ character.

			i = text.indexOf("${",startIndex); //check the opening $ character
			if(i == -1)
				break; //token not found.

			j = text.indexOf("}$",i+1); //check the closing $ character
			if(j == -1)
				break; //closing token not found. possibly wrong syntax.

			f = text.substring(0,i); //get the text before token
			v = getValue(node,text.substring(i+2, j)); //append value of token.

			f += (v == null) ? "" : v;
			f += text.substring(j+2, text.length()); //append value after token.

			text = f;

		}while (true); //will break out when dollar symbols are out.

		return text;
	}
	
	
	/**
	 * Gets the text value of a node as pointed to by an xpath expression in an xml document
	 * whose root node is given.
	 * 
	 * @param node the root node.
	 * @param xpath the xpath expression.
	 * @return the text value.
	 */
	private static String getValue(Element node, String xpath){
		int pos = xpath.lastIndexOf('@'); String attributeName = null;
		if(pos > 0){
			attributeName = xpath.substring(pos+1,xpath.length());
			xpath = xpath.substring(0,pos-1);
		}
		
		XPathExpression xpls = new XPathExpression(node, xpath);
		Vector<?> result = xpls.getResult();

		for (Enumeration<?> e = result.elements(); e.hasMoreElements();) {
			Object obj = e.nextElement();
			if (obj instanceof Element){
				if(pos > 0) //Check if we are to set attribute value.
					return ((Element) obj).getAttribute(attributeName);
				else
					return XmlUtil.getTextValue((Element) obj);
			}
		}
		
		return null;
	}
	
	public static String urlAppendNamePassword(String url, String name, String password){
		url = addParameter(url, "username", name);
		return addParameter(url, "password", password);
	}
	
	public static String addParameter(String url, String name, String value){
		if(value != null && value.trim().length() > 0){
			if(url.indexOf('?') < 0)
				url += "?";
			else
				url += "&";
			
			url += name + "=" + value;
		}
		return url;
	}
	
	public static boolean urlContainsHttp(String url){
		return url.contains("http://") || url.contains("https://");
	}
}
