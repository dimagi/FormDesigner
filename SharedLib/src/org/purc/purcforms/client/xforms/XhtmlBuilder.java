package org.purc.purcforms.client.xforms;

import org.purc.purcforms.client.model.FormDef;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;


/**
 * Builds an xhtml document from a form definition object model.
 * 
 * @author daniel
 *
 */
public class XhtmlBuilder {

	/**
	 * All methods in this class are static and hence we expect no external
	 * Instantiation of this class.
	 */
	private XhtmlBuilder(){

	}
	
	
	/**
	 * Converts a form definition object to XHTML.
	 * 
	 * @param formDef the form definition object.
	 * @return the xhtml.
	 */
	public static String fromFormDef2Xhtml(FormDef formDef){
		Document prevdoc = formDef.getDoc();

		Document doc = XMLParser.createDocument();
		doc.appendChild(doc.createProcessingInstruction("xml", "version=\"1.0\" encoding=\"UTF-8\""));
		formDef.setDoc(doc);

		Element htmlNode = doc.createElement("html");
		formDef.setXformsNode(htmlNode);

		htmlNode.setAttribute("xmlns", "http://www.w3.org/1999/xhtml");
		htmlNode.setAttribute(XformConstants.XML_NAMESPACE_PREFIX+XformConstants.PREFIX_XFORMS, XformConstants.NAMESPACE_XFORMS);
		htmlNode.setAttribute(XformConstants.XML_NAMESPACE_PREFIX+XformConstants.PREFIX_XML_SCHEMA, XformConstants.NAMESPACE_XML_SCHEMA);

		doc.appendChild(htmlNode);

		//add head
		Element headNode =  doc.createElement("head");
		htmlNode.appendChild(headNode);

		//add title
		Element titleNode =  doc.createElement("title");
		titleNode.appendChild(doc.createTextNode(formDef.getName()));
		headNode.appendChild(titleNode);

		//add body
		Element bodyNode =  doc.createElement("body");
		htmlNode.appendChild(bodyNode);

		//add model
		Element modelNode =  doc.createElement(XformConstants.NODE_NAME_MODEL);
		headNode.appendChild(modelNode);

		XformBuilder.buildXform(formDef,doc,bodyNode,modelNode);

		XformUtil.copyModel(prevdoc,doc);

		return XmlUtil.fromDoc2String(doc);
	}
}
