package org.purc.purcforms.client.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.purc.purcforms.client.Context;
import org.purc.purcforms.client.model.ItextModel;
import org.purc.purcforms.client.model.Locale;
import org.purc.purcforms.client.xforms.XmlUtil;

import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;


public class ItextParser {

	public static Document parse(String xml, ListStore<ItextModel> list){
		Document doc = XmlUtil.getDocument(xml);
		
		NodeList nodes = doc.getElementsByTagName("itext");
		if(nodes == null || nodes.getLength() == 0)
			return doc;
		
		nodes = ((Element)nodes.item(0)).getElementsByTagName("translation");
		if(nodes == null || nodes.getLength() == 0)
			return doc;
		
		List<Locale> locales = new ArrayList<Locale>();
		HashMap<String,String> defaultItext = null;
		HashMap<String, HashMap<String,String>> translations = new HashMap<String, HashMap<String,String>>();
		for(int index = 0; index < nodes.getLength(); index++){
			Element translationNode = (Element)nodes.item(index);
			HashMap<String,String> itext = new HashMap<String,String>();
			String lang = translationNode.getAttribute("lang");
			translations.put(lang, itext);
			fillItext(translationNode,itext);
			
			if(index == 0){
				defaultItext = itext;
				Context.setLocale(new Locale(lang,lang));
			}
			
			locales.add(new Locale(lang,lang));
		}
		
		tranlateNodes("label", doc, defaultItext, list);
		tranlateNodes("hint", doc, defaultItext, list);
		tranlateNodes("title", doc, defaultItext, list);
		
		Context.setLocales(locales);
		
		return doc;
	}
	
	private static void fillItext(Element translationNode, HashMap<String,String> itext){
		NodeList nodes = translationNode.getChildNodes();
		for(int index = 0; index < nodes.getLength(); index++){
			Node textNode = nodes.item(index);
			if(textNode.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			itext.put(((Element)textNode).getAttribute("id"), getValueText(textNode));
		}
	}
	
	private static String getValueText(Node textNode){
		NodeList nodes = textNode.getChildNodes();
		for(int index = 0; index < nodes.getLength(); index++){
			Node valueNode = nodes.item(index);
			if(valueNode.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			return XmlUtil.getTextValue(valueNode);
		}
		
		return "";
	}
	
	private static void tranlateNodes(String name, Document doc, HashMap<String,String> itext, ListStore<ItextModel> list){
		NodeList nodes = doc.getElementsByTagName(name);
		if(nodes == null || nodes.getLength() == 0)
			return;
		
		for(int index = 0; index < nodes.getLength(); index++){
			Element node = (Element)nodes.item(index);
			String ref = node.getAttribute("ref");
			if(ref == null)
				continue;
			
			int pos = ref.indexOf("jr:itext('");
			if(pos < 0)
				continue;
			
			String id = ref.substring(10,ref.lastIndexOf("'"));
			String text = itext.get(id);
			if(!XmlUtil.setTextNodeValue(node, text))
				node.appendChild(doc.createTextNode(text));
			
			Element parentNode = (Element)node.getParentNode();
			String idname = "bind";
			ref = parentNode.getAttribute("ref");
			if(ref != null)
				idname = "ref";
			else
				ref = parentNode.getAttribute("bind");
			
			ItextModel itextModel = new ItextModel();
			itextModel.set("xpath", FormUtil.getNodePath(parentNode) + "[@" + idname + "='" + id + "']" + "/" + name);
			itextModel.set("id", id);
			itextModel.set("en", text);
			list.add(itextModel);
		}
	}
}
