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
		if(itext == null || itext.getLength() == 0)
			return doc;

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
					if(!tagNameEqualsValue || XmlUtil.getTextValue(valNode).isEmpty()){
						continue; //means invalid xform itext
					}
					String textform = ((Element)valNode).getAttribute("form");
					if(textform != null){
						fullID = id + ";" + textform;
						language.setTranslation(fullID, XmlUtil.getTextValue(valNode)); //some textform value
					}else{
						if(fullID.contains("_hint")){
							language.setTranslation(fullID.replace("_", ";"), XmlUtil.getTextValue(valNode));
						}else{
							language.setTranslation(fullID, XmlUtil.getTextValue(valNode));  //this is obviously the default value
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
		if(itextNode != null)
			itextNode.getParentNode().removeChild(itextNode);
		
		if(!Itext.hasItext()){
			return;
		}
		
		List<ItextLocale> locales = Itext.locales;
		if(locales == null)
			return; //Houston we have a problem. Itext hasn't been intialized properly (or at all).

		itextNode = formDef.getDoc().createElement("itext");
		modelNode.appendChild(itextNode);
		
		for(ItextLocale locale : locales)
			createTextValueNodes(formDef,locale,itextNode);
		
		
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
				valueNode.appendChild(doc.createTextNode(defaultText));
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
					valueNode.appendChild(doc.createTextNode(locale.getTranslation(ItextID,form)));
					textNode.appendChild(valueNode);
				}
				
			}
			//link nodes up
			translationNode.appendChild(textNode);

		}
		formDef.setDoc(doc);
		
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
		valNode.appendChild(doc.createTextNode(hintValue));
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
		
//		////////============================
//		
//		localeXformNodeMap.clear();
//	
//		Document doc = XmlUtil.getDocument(xml);
//
//		//Check if we have an itext block in this xform.
//		NodeList nodes = doc.getElementsByTagName("itext");
//		if(nodes == null || nodes.getLength() == 0)
//			return doc;
//
//		//Check if we have any translations in this itext block.
//		nodes = ((Element)nodes.item(0)).getElementsByTagName("translation");
//		if(nodes == null || nodes.getLength() == 0)
//			return doc;
//
//		List<Locale> locales = new ArrayList<Locale>(); //New list of locales as it comes form the parsed xform.
//		//HashMap<String,String> defaultItext = null; //Map of id and itext for the default language.
//		HashMap<String,String> defaultText = null; //Map of default id and itext (for multiple values of the itext node) for the default language.
//
//		//Map of each locale key and map of its id and itext translations.
//		HashMap<String, HashMap<String,String>> translations = new HashMap<String, HashMap<String,String>>();
//		for(int index = 0; index < nodes.getLength(); index++){
//			Element translationNode = (Element)nodes.item(index);
//			HashMap<String,String> itext = new HashMap<String,String>();
//			HashMap<String, String> defText = new HashMap<String,String>();
//			String lang = translationNode.getAttribute("lang");
//			translations.put(lang, itext);
//			fillItextMap(translationNode,itext,defText,lang,list,formAttrMap,itextMap);
//
//			if( ((Element)nodes.item(index)).getAttribute("default") != null || index == 0){
//				//defaultItext = itext; //first language acts as the default
//				defaultText = defText;
//				Context.setLocale(new Locale(lang,lang));
//				Context.setDefaultLocale(Context.getLocale());
//			}
//
//			//create a new locale object for the current translation.
//			locales.add(new Locale(lang,lang));
//		}
//
//		Context.setLocales(locales);
//
//
//		//create a hash table of locale xform nodes keyed by locale.
//		for(Locale locale : locales){
//			Document localeDoc = XMLParser.createDocument();
//			Element node = localeDoc.createElement(LanguageUtil.NODE_NAME_LANGUAGE_TEXT);
//			node.setAttribute("lang", locale.getName());
//			localeDoc.appendChild(node);
//
//			Element localeXformNode = localeDoc.createElement(LanguageUtil.NODE_NAME_XFORM);
//			node.appendChild(localeXformNode);
//
//			localeXformNodeMap.put(locale.getName(), localeXformNode);
//		}
//
//
//		tranlateNodes("label", doc, defaultText, list, Context.getLocale().getName(),itextMap); //getKey()??????
//		tranlateNodes("hint", doc, defaultText, list, Context.getLocale().getName(),itextMap); //getKey()??????
////		tranlateNodes("title", doc, defaultText, list, Context.getLocale().getName(),itextMap); //getKey()??????
//
//		Context.getLanguageText().clear();
//
//
//		HashMap<String,String> map = new HashMap<String,String>();
//		for(Locale locale : locales){
//			Element localeXformNode = localeXformNodeMap.get(locale.getName());
//			map.put(locale.getName(), localeXformNode.getOwnerDocument().toString());
//		}
//
//		//TODO Will the form id always be 1?
//		Context.getLanguageText().put(1, map);
//
//		return doc;
//	}


//	/**
//	 * Fills a map of id and itext for a given locale as represented by a given translation node.
//	 * 
//	 * @param translationNode the translation node.
//	 * @param itext the itext map.
//	 */
//	private static void fillItextMap(Element translationNode, HashMap<String,String> itext, HashMap<String,String> defaultText, String localeKey, ListStore<ItextModel> list, HashMap<String,String> formAttrMap, HashMap<String,ItextModel> itextMap){
//		NodeList nodes = translationNode.getChildNodes();
//		for(int index = 0; index < nodes.getLength(); index++){
//			Node textNode = nodes.item(index);
//			if(textNode.getNodeType() != Node.ELEMENT_NODE)
//				continue;
//
//			//itext.put(((Element)textNode).getAttribute("id"), getValueText(textNode));
//			setValueText(itext,((Element)textNode).getAttribute("id"), textNode, defaultText,localeKey,list,formAttrMap,itextMap);
//		}
//
//	}


//	/**
//	 * Gets the text value of a node.
//	 * 
//	 * @param textNode the node.
//	 * @return the text value.
//	 */
//	private static void setValueText(HashMap<String,String> itext, String id, Node textNode, HashMap<String,String> defaultText, String localeKey, ListStore<ItextModel> list, HashMap<String,String> formAttrMap, HashMap<String,ItextModel> itextMap){
//		String defaultValue = null, longValue = null, shortValue = null;
//
//		NodeList nodes = textNode.getChildNodes();
//		for(int index = 0; index < nodes.getLength(); index++){
//			Node valueNode = nodes.item(index);
//			if(valueNode.getNodeType() != Node.ELEMENT_NODE)
//				continue;
//
//			String form = ((Element)valueNode).getAttribute("form");
//			String text = XmlUtil.getTextValue(valueNode);
//			if(text != null){
//				itext.put(form == null ? id : id + ";" + form, text);
//
//				if(form == null)
//					defaultValue = text;
//				else if(form.equalsIgnoreCase("long"))
//					longValue = text;
//				else if(form != null && form.equalsIgnoreCase("short"))
//					shortValue = text;
//				else
//					defaultValue = text;
//
//				String fullId = form == null ? id : id + ";" + form;
//				ItextModel itextModel = itextMap.get(fullId);
//				if(itextModel == null){
//					itextModel = new ItextModel();
//					itextModel.set("id", fullId);
//					itextMap.put(fullId, itextModel);
//					list.add(itextModel);
//				}
//
//				//itextModel.set("id", fullId);
//				itextModel.set(localeKey, text);
//				//list.add(itextModel);
//			}
//
//			if(form != null){
//				List<String> attrList = itextFormAttrList.get(id);
//				if(attrList == null){
//					attrList = new ArrayList<String>();
//					itextFormAttrList.put(id, attrList);
//				}
//				attrList.add(form + "|" + text);
//			}
//		}
//
//		if(longValue != null){
//			defaultValue = longValue;
//			formAttrMap.put(id, "long");
//		}
//		else if(shortValue != null){
//			defaultValue = shortValue;
//			formAttrMap.put(id, "short");
//		}
//
//		defaultText.put(id, defaultValue);
//	}


//	/**
//	 * For a given xforms document, fills the text of all nodes having a given name with their 
//	 * corresponding text based on the itext id in the ref attribute.
//	 * 
//	 * @param name the name of the nodes to look for.
//	 * @param doc the xforms document.
//	 * @param itext the id to itext map.
//	 * @param list the itext model as required by gxt grids.
//	 */
//	private static void tranlateNodes(String name, Document doc, HashMap<String,String> itext, ListStore<ItextModel> list, String localeKey, HashMap<String,ItextModel> itextMap){
//		NodeList nodes = doc.getElementsByTagName(name);
//		if(nodes == null || nodes.getLength() == 0)
//			return;
//
//		//Map for detecting duplicates in itext. eg if id yes=Yes , we should not have information more than once.
//		HashMap<String,String> duplicatesMap = new HashMap<String, String>();
//
//		for(int index = 0; index < nodes.getLength(); index++){
//			Element node = (Element)nodes.item(index);
//
//			/*//Check if current node has a ref attribute.
//			String ref = node.getAttribute("ref");
//			if(ref == null)
//				continue;
//
//			//Check if node has jr:itext value in the ref attribute value.
//			int pos = ref.indexOf("jr:itext('");
//			if(pos < 0)
//				continue;
//
//			//Get the itext id which starts at the 11th character.*/
//
//			String id = getItextId(node);
//			if(id == null || id.trim().length() == 0)
//				continue;
//
//			String text = itext.get(id);
//
//			//If the text node does not already exist, add it, else just update itx text.
//			if(!XmlUtil.setTextNodeValue(node, text))
//				node.appendChild(doc.createTextNode(text));
//
//			//Skip the steps below if we have already processed this itext id.
//			if(duplicatesMap.containsKey(id))
//				continue;
//			else
//				duplicatesMap.put(id, id);
//
//			Element parentNode = (Element)node.getParentNode();
//			String idname = "bind";
//			String ref = parentNode.getAttribute("ref");
//			if(ref != null)
//				idname = "ref";
//			else
//				ref = parentNode.getAttribute("bind");
//			
//			if(ref == null){
//				ref = parentNode.getAttribute("id");
//				if(ref != null)
//					idname = "id";
//			}
//
//			//Create and add an itext model object as required by the gxt grid.
//
//			ItextModel itextModel = itextMap.get(id);
//			if(itextModel == null)
//				itextModel = itextMap.get(id+";long");
//			if(itextModel == null)
//				itextModel = itextMap.get(id+";short");
//
//			if(itextModel == null){
//				itextModel = new ItextModel();
//				itextMap.put(id, itextModel);
//				itextModel.set("id", id);
//				list.add(itextModel);
//			}
//
//			String xpath = itextModel.get("xpath");
//			if(xpath == null){//TODO Check to confirm that this null check does not cause bugs. Meaning do we have to rebuild the xpath everytime?
//				xpath = FormUtil.getNodePath(parentNode) + "[@" + idname + "='" + ref /*id*/ + "']" + "/" + name;
//				if(ref == null)
//					xpath = FormUtil.getNodePath(parentNode) + "/" + name;
//
//				itextModel.set("xpath", xpath);
//			}
//
//			//itextModel.set("id", id);
//			itextModel.set(localeKey, text);
//			//list.add(itextModel);
//
//			for(Locale locale : Context.getLocales()){
//				Element localeXformNode = localeXformNodeMap.get(locale.getName());
//				Element textNode = localeXformNode.getOwnerDocument().createElement("text");
//				textNode.setAttribute("xpath", xpath);
//				textNode.setAttribute("value", (String)itextModel.get(locale.getName()));
//				localeXformNode.appendChild(textNode);
//			}
//		}
//	}
//
//

}
