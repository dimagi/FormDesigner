package org.openrosa.client.util;

import java.util.HashMap;
import java.util.List;

import org.openrosa.client.model.FormDef;
import org.openrosa.client.model.ItextModel;
import org.openrosa.client.xforms.XmlUtil;

import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Text;
import com.google.gwt.xml.client.XMLParser;


/**
 * Parsing of a new XML (only the Itext section).
 * 
 * Also provides tools for updating the FormDef using the Itext data stored
 * in Itext
 *
 */
public class ItextParser {	


	/**
	 * Parses an xform and sets the text of various nodes based on the current a locale
	 * as represented by their itext ids. The translation element with the "default" attribute (default="") OR the first locale in the itext block
	 * (if no default attribute is found) is the one taken as the default.
	 * 
	 * @param xml the xforms xml.
	 * @return the document where all itext refs are filled with text for a given locale.
	 */
	public static Document parse(String xml){
//		System.out.println(xml);
		Itext.clearLocales();
		Document doc = XmlUtil.getDocument(xml);
		
		//Check if we have an itext block in this xform.
		NodeList itext = doc.getElementsByTagName("itext");
		if(itext == null || itext.getLength() == 0){
			return doc;
		}

		//Check if we have any translations in this itext block.
		NodeList translations = ((Element)itext.item(0)).getElementsByTagName("translation");
		if(translations == null || translations.getLength() == 0)
			return doc;

		for(int i=0; i<translations.getLength();i++){
			Element translation = (Element)translations.item(i);
			String languageName = translation.getAttribute("lang");
			ItextLocale language = Itext.getLocale(languageName); //creates a new locale (if it doesn't exist already) and adds it to the list in Itext
			if(translation.getAttribute("default")!=null){ Itext.setDefaultLocale(languageName); }

			
			NodeList textNodes = translation.getChildNodes();
			
			for(int j=0; j<textNodes.getLength(); j++){
				if(textNodes.item(j).getNodeType() != Node.ELEMENT_NODE) continue;
				
				Element text = (Element)textNodes.item(j);
				String id= text.getAttribute("id");
				if(id == null) continue; //invalid javarosa xform xml at this point
				NodeList forms = text.getElementsByTagName("value");
				for(int k=0; k<forms.getLength(); k++){
					String fullID = id;
					Node valNode = forms.item(k);
					boolean tagNameEqualsValue = ((Element)valNode).getTagName().toLowerCase().equals("value");
					if(!tagNameEqualsValue || XmlUtil.getItextTextValue(valNode) == null || XmlUtil.getItextTextValue(valNode).isEmpty()){
						continue; //means invalid xform itext
					}
					String textform = ((Element)valNode).getAttribute("form");
					if(textform != null){
						fullID = id + ";" + textform;
						language.setTranslation(fullID, XmlUtil.getItextTextValue(valNode)); //some textform value
					}else{
						if(fullID.contains("_hint")){
							language.setTranslation(fullID.replace("_", ";"), XmlUtil.getItextTextValue(valNode));
						}else{
							language.setTranslation(fullID, XmlUtil.getItextTextValue(valNode));  //this is obviously the default value
						}
					}
				}
				
			}
			
		}
		
		return doc;
	}  
	
	/**
	 * Updates an xforms document (XML) itext block based on data stored in the Itext object.
	 * 
	 * @param doc the xforms document.
	 * @param formDef the form definition object.
	 * @param list the gxt grid itext model.
	 */
	public static void updateItextBlock(FormDef formDef){
		Document doc = formDef.getDoc();
		Element modelNode = XmlUtil.getNode(doc.getDocumentElement(),"model");
		assert(modelNode != null); //we must have a model in an xform.

		Element itextNode = XmlUtil.getNode(modelNode,"itext");
		if(itextNode != null){
			itextNode.getParentNode().removeChild(itextNode);
		}
		
		if(!Itext.hasItext()){
			return;
		}
		
		List<ItextLocale> locales = Itext.locales;
		if(locales == null)
			return; //Houston we have a problem. Itext hasn't been intialized properly (or at all).

		itextNode = formDef.getDoc().createElement("itext");
		modelNode.appendChild(itextNode);
		
		for(ItextLocale locale : locales){
			createTextValueNodes(formDef,locale,itextNode);
		}
		
		
	}

	
	/**
	 * Creates the text (and value) nodes that fill up each translation block
	 * @param formDef
	 * @param list
	 * @param locale
	 */
	private static void createTextValueNodes(FormDef formDef, ItextLocale locale, Element itextNode){
		Document doc = formDef.getDoc();
		Element translationNode = doc.createElement("translation");
		translationNode.setAttribute("lang", locale.getName());
		
		//Check for default
		if(locale.isDefault()){
			translationNode.setAttribute("default", "");
		}
		itextNode.appendChild(translationNode);
		for(String ItextID : locale.getAvailableItextIDs()){
			String defaultText = locale.getDefaultTranslation(ItextID);
			
	
			
			//create nodes
			Element textNode = doc.createElement("text");
			Element valueNode;
			
			//set default value itext value if it exists
			textNode.setAttribute("id", ItextID);
			if(defaultText != null){
				valueNode = doc.createElement("value");
//				valueNode.appendChild(doc.createTextNode(defaultText));
				attachItextText(valueNode, defaultText);
				textNode.appendChild(valueNode);
			}
			
			//set/add special form values if they exist
			for(String form : locale.getAvailableForms(ItextID)){
				if(locale.getTranslation(ItextID,form)!= null){
					
					//interrupt for hint special case.
					if(form.toLowerCase().equals("hint")){
						makeHintTextNode(translationNode, ItextID, locale.getTranslation(ItextID,form));
						continue;
					}
					
					valueNode = doc.createElement("value");
					valueNode.setAttribute("form", form);
//					valueNode.appendChild(doc.createTextNode(locale.getTranslation(ItextID,form)));
					attachItextText(valueNode, locale.getTranslation(ItextID,form));
					textNode.appendChild(valueNode);
				}
				
			}
			//link nodes up
			translationNode.appendChild(textNode);

		}
		formDef.setDoc(doc);
		
	}
	
	/**
	 * Creates the correct TEXT and (if necessary output nodes) combo and attaches
	 * it to the specified parentNode.  The parentNode is returned after attachment.
	 * @param parentNode - Element you want to attach the Itext Text to.
	 * @param rawText - Raw text (including any '&lt;output&gt;' string tags
	 * @return
	 */
	public static Element attachItextText(Element parentNode, String rawText){
		if(rawText == null || rawText.isEmpty()){
			return null;
		}else{
			rawText = "<foo xmlns=\"http://www.w3.org/2002/xforms\">" + rawText + "</foo>";
		}
		
		Document tempDoc = XMLParser.parse(rawText);
		Node imp = parentNode.getOwnerDocument().importNode(tempDoc.getChildNodes().item(0), true);
		NodeList childNodes = imp.getChildNodes();
		int numChildren = childNodes.getLength();
		for(int i = 0; i<numChildren;i++){
			Node child = imp.getFirstChild(); //for some reason appendChild below actively removes the child from the childNodes list...
			int nodeType = child.getNodeType();
			parentNode.appendChild(child);
		}
		
		return parentNode;
	}
	
	/**
	 * automatically creates a hint text node (for question hints)
	 * The hint itext id becomes "itextID_hint" where itextID is specified as an
	 * argument.
	 * 
	 * Hint value is the actual text of the hint.
	 * @param translation
	 * @param itextID
	 * @param hintValue
	 */
	private static void makeHintTextNode(Element translation,String itextID, String hintValue){
		Document doc = translation.getOwnerDocument();
		Element textNode = doc.createElement("text");
		textNode.setAttribute("id", itextID+"_hint");
		Element valNode = doc.createElement("value");
//		valNode.appendChild(doc.createTextNode(hintValue));
		attachItextText(valNode, hintValue);
		textNode.appendChild(valNode);
		translation.appendChild(textNode);
	}
	
	
	public static String getRowKeys(ItextModel row){
		String k = "";
		for(String key: row.getPropertyNames()){
			k += key+",";
		}
		
		return k;
	}

}
