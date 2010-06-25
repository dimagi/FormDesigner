package org.purc.purcforms.client.xforms;

import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.util.ItextBuilder;

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
	 * Converts a form definition object to an XHTML document object.
	 * 
	 * @param formDef the form definition object.
	 * @return the xhtml document object.
	 */
	public static Document fromFormDef2XhtmlDoc(FormDef formDef){
		Document prevdoc = formDef.getDoc();

		Document doc = XMLParser.createDocument();
		doc.appendChild(doc.createProcessingInstruction("xml", "version=\"1.0\" encoding=\"UTF-8\""));

		Element htmlNode = doc.createElement("h:html");
		//formDef.setXformsNode(htmlNode);

		htmlNode.setAttribute("xmlns:h", "http://www.w3.org/1999/xhtml");
		htmlNode.setAttribute("xmlns:jr", "http://openrosa.org/javarosa");
		htmlNode.setAttribute(XformConstants.XML_NAMESPACE /*XformConstants.XML_NAMESPACE_PREFIX+XformConstants.PREFIX_XFORMS*/, XformConstants.NAMESPACE_XFORMS);
		htmlNode.setAttribute(XformConstants.XML_NAMESPACE_PREFIX+XformConstants.PREFIX_XML_SCHEMA, XformConstants.NAMESPACE_XML_SCHEMA);

		doc.appendChild(htmlNode);

		//add head
		Element headNode =  doc.createElement("h:head");
		htmlNode.appendChild(headNode);

		//add title
		Element titleNode =  doc.createElement("h:title");
		titleNode.appendChild(doc.createTextNode(formDef.getName()));
		headNode.appendChild(titleNode);

		//add body
		Element bodyNode =  doc.createElement("h:body");
		htmlNode.appendChild(bodyNode);

		//add model
		Element modelNode =  doc.createElement(XformConstants.NODE_NAME_MODEL);
		headNode.appendChild(modelNode);

		//we do not want to lose anything that the model could have had which we do not build when
		//creating an xform from scratch
		XformBuilder.buildXform(formDef,doc,bodyNode,modelNode);

		XformUtil.copyModel(prevdoc,doc);
		
		return doc;
	}
	
	/**
	 * Converts a form definition object to XHTML.
	 * 
	 * @param formDef the form definition object.
	 * @return the xhtml.
	 */
	public static String fromFormDef2Xhtml(FormDef formDef){
		Document doc = fromFormDef2XhtmlDoc(formDef);
		formDef.setDoc(doc);
		formDef.setXformsNode(doc.getDocumentElement());
		
		if(FormUtil.isJavaRosaSaveFormat())
			ItextBuilder.build(formDef);
		
		return XmlUtil.fromDoc2String(doc);
	}
}
