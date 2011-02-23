package org.openrosa.client.util;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.openrosa.client.Context;
import org.openrosa.client.OpenRosaConstants;
import org.openrosa.client.model.FormDef;
import org.openrosa.client.model.ItextModel;
import org.purc.purcforms.client.model.Locale;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.xforms.XmlUtil;
import org.purc.purcforms.client.xpath.XPathExpression;

import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.user.client.Window;
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
	

	
	/**
	 * Removes all child nodes for a give  node.
	 * 
	 * @param node the node whose child nodes to remove.
	 */
	private static void removeAllChildNodes(Element node){
		while(node.getChildNodes().getLength() > 0)
			node.removeChild(node.getChildNodes().item(0));
	}


	/**
	 * Updates an xforms document itext block with values as edited by the user from a grid.
	 * 
	 * @param doc the xforms document.
	 * @param formDef the form definition object.
	 * @param list the gxt grid itext model.
	 */
	public static void updateItextBlock(FormDef formDef, ListStore<ItextModel> list){
		
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
			createTextValueNodes(formDef, list, locale);
	}

	
	/**
	 * Creates the text (and value) nodes that fill up each translation block
	 * @param formDef
	 * @param list
	 * @param locale
	 */
	private static void createTextValueNodes(FormDef formDef, ListStore<ItextModel> list, Locale locale){
		Document doc = formDef.getDoc();
		Element itextNode = (Element)doc.getElementsByTagName("itext").item(0);
		Element translationNode = formDef.getDoc().createElement("translation");
		translationNode.setAttribute("lang", locale.getName());
		itextNode.appendChild(translationNode);

		//we shold also have alist of those that were duplicates
		for(ItextModel itext : list.getModels()){
			
			String id = itext.get("id");
			String language_name = itext.get(locale.getName());
			
			//create nodes
			Element textNode = doc.createElement("text");
			Element valueNode = doc.createElement("value");
			
			//link nodes up
			translationNode.appendChild(textNode);
			textNode.appendChild(valueNode);
			
			//set values
			textNode.setAttribute("id", id);
			valueNode.appendChild(doc.createTextNode(language_name));


			String xpath = (String)itext.get("xpath");
			if(xpath == null)
				continue;
			
			Vector result = new XPathExpression(doc, xpath).getResult();
			if(result != null && result.size() > 0){
				Element targetNode = (Element)result.get(0);
				targetNode.setAttribute("ref", "jr:itext('" + id + "')");
				assert(result.size() == 1); //each xpath expression should point to not more than one node.

				if(locale.getName().equalsIgnoreCase(Context.getDefaultLocale().getName()))
					XmlUtil.setTextNodeValue(targetNode, language_name);
			}
		}
	}
}
