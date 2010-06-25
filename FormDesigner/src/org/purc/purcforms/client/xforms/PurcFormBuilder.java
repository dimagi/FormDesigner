package org.purc.purcforms.client.xforms;

import java.util.HashMap;
import java.util.Set;

import org.purc.purcforms.client.model.FormDef;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;


/**
 * 
 * @author daniel
 *
 */
public class PurcFormBuilder {

	public static String build(FormDef formDef, HashMap<String,String> localeText){

		Document doc = XMLParser.createDocument();
		doc.appendChild(doc.createProcessingInstruction("xml", "version=\"1.0\" encoding=\"UTF-8\""));

		Element purcFormNode = doc.createElement("PurcForm");
		doc.appendChild(purcFormNode);

		Element xformNode = doc.createElement("Xform");
		Element node = (Element)formDef.getDoc().getDocumentElement().cloneNode(true);
		doc.importNode(node, true);
		xformNode.appendChild(node);
		purcFormNode.appendChild(xformNode);


		String xml = formDef.getLayoutXml();
		if(xml != null && xml.trim().length() > 0){
			node = (Element)XmlUtil.getDocument(xml).getDocumentElement().cloneNode(true);
			doc.importNode(node, true);
			Element layoutNode = doc.createElement("Layout");
			layoutNode.appendChild(node);
			purcFormNode.appendChild(layoutNode);
		}
		
		String src = formDef.getJavaScriptSource();
		if(src != null && src.trim().length() > 0){
			Element javaScriptNode = doc.createElement("JavaScript");
			javaScriptNode.appendChild(doc.createCDATASection(src));
			purcFormNode.appendChild(javaScriptNode);
		}
		
		purcFormNode.appendChild(getLanguageNode(doc,localeText));

		return XmlUtil.fromDoc2String(doc);
	}
	
	
	private static Element getLanguageNode(Document doc, HashMap<String,String> localeText){
		Element languageNode = doc.createElement("Language");
		
		Set<String> locales = localeText.keySet();
		for(String locale : locales){
			Element node = (Element)XmlUtil.getDocument(localeText.get(locale)).getDocumentElement().cloneNode(true);
			doc.importNode(node, true);
			languageNode.appendChild(node);
		}
		
		return languageNode;
	}
	
	
	public static String getCombinedLanguageText(HashMap<String,String> localeText){
		Document doc = XMLParser.createDocument();
		doc.appendChild(doc.createProcessingInstruction("xml", "version=\"1.0\" encoding=\"UTF-8\""));
		doc.appendChild(getLanguageNode(doc,localeText));
		
		return XmlUtil.fromDoc2String(doc);
	}
}
