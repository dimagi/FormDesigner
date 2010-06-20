package org.purc.purcforms.client.xforms;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NamedNodeMap;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;


/**
 * Utility methods used when manipulating xforms xml documents.
 * This class is different from XmlUtil by handling xforms 
 * specifics in the xml document. This class does not have any
 * xforms object model classes like FormDef, PageDef, QuestionDef, etc.
 * 
 * @author daniel
 *
 */
public class XformUtil {

	/**
	 * All methods in this class are static and hence we expect no external
	 * Instantiation of this class.
	 */
	private XformUtil(){

	}
	
	
	/**
	 * Creates a node from an xml fragment.
	 * 
	 * @param xml the xml fragment.
	 * @return the node.
	 */
	public static Element getNode(String xml){
		xml = "<xf:xforms xmlns:xf=\"http://www.w3.org/2002/xforms\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"  xmlns:h=\"http://www.w3.org/1999/xhtml\">" + xml;
		xml = xml + "</xf:xforms>";
		Document doc = XMLParser.parse(xml);
		Element node = (Element)doc.getDocumentElement().getChildNodes().item(0);
		if(node.getAttribute(XformConstants.ATTRIBUTE_NAME_XMLNS) != null)
			node.removeAttribute(XformConstants.ATTRIBUTE_NAME_XMLNS);
		return node;
	}
	
	
	/**
	 * Renames a node.
	 * 
	 * @param node the node to rename.
	 * @param newName the new node name.
	 * @return the new renamed node.
	 */
	public static Element renameNode(Element node, String newName){
		String xml = node.toString();
		xml = xml.replace(node.getNodeName(), newName);
		Element child = getNode(xml);
		Element parent = (Element)node.getParentNode();
		parent.replaceChild(child, node);
		return child;
	}
	
	
	/**
	 * Gets an xforms instance node with a given id.
	 * 
	 * @param modelNode the xforms model node.
	 * @param instanceId the instance id.
	 * @return the instance node.
	 */
	public static Element getInstanceNode(Element modelNode, String instanceId){
		NodeList nodes = modelNode.getElementsByTagName(XformConstants.NODE_NAME_INSTANCE); //TODO What if we have a different prefix from xf?
		if(nodes.getLength() == 0)
			nodes = modelNode.getElementsByTagName(XformConstants.NODE_NAME_INSTANCE_MINUS_PREFIX);
		
		if(nodes == null)
			return null;
		
		for(int index = 0; index < nodes.getLength(); index++){
			Element node = (Element)nodes.item(index);
			if(instanceId.equalsIgnoreCase(node.getAttribute(XformConstants.ATTRIBUTE_NAME_ID)))
				return node;
		}
		
		return null;
	}
	
	
	/**
	 * Gets the model node of an xforms document with a given root node.
	 * 
	 * @param element the xforms document root node.
	 * @return the mode node.
	 */
	public static Element getModelNode(Element element){
		int numOfEntries = element.getChildNodes().getLength();
		for (int i = 0; i < numOfEntries; i++) {
			if (element.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element child = (Element)element.getChildNodes().item(i);
				//String tagname = getNodeName(child);
				String tagname = child.getNodeName(); //NODE_NAME_INSTANCE has prefix
				//if (tagname.equals(NODE_NAME_MODEL)||tagname.equals(NODE_NAME_MODEL_MINUS_PREFIX))
				if(XmlUtil.nodeNameEquals(tagname,XformConstants.NODE_NAME_MODEL_MINUS_PREFIX))
					return child;
				else{
					child = getModelNode(child);
					if(child != null)
						return child;
				}
			}
		}
		return null;
	}
	
	
	/**
	 * Copies the xforms model instance from one document to another.
	 * 
	 * @param srcDoc the xforms document with the model instance to copy.
	 * @param destDoc the xforms document whose model instance we are to replace with the copied one.
	 */
	public static void copyModel(Document srcDoc, Document destDoc){
		if(srcDoc != null){			
			Element oldModel = getModelNode(srcDoc.getDocumentElement());
			Element newModel = getModelNode(destDoc.getDocumentElement());

			if(oldModel != null && newModel != null){
				oldModel = (Element)oldModel.cloneNode(true);
				destDoc.importNode(oldModel, true);
				newModel.getParentNode().appendChild(oldModel);
				newModel.getParentNode().removeChild(newModel);
			}
		}
	}
	
	
	/**
	 * Gets the node that has the text value which is the answer, in a given
	 * binding or variable name (e.g obs/weight/value)
	 * 
	 * @param dataNode the xforms instance data node.
	 * @param variableName te binding or variable name.
	 * @return the value node.
	 */
	public static Element getValueNode(Element dataNode,String variableName){
		Element node = dataNode;
		int startPos = 0;
		int endPos = variableName.indexOf('/');
		if(endPos < 0)
			return null;

		while(endPos >= 0){
			String name = variableName.substring(startPos, endPos);
			node = XmlUtil.getNode(node,name);
			if(node == null)
				return null;
			startPos = endPos + 1;
			endPos = variableName.indexOf('/', startPos);
		}

		String name = variableName.substring(startPos);
		node = XmlUtil.getNode(node,name);
		return node;
	}
	

	/**
	 * Changes our namespace prefix to the one used in the original document.
	 * 
	 * @param doc the document.
	 * @param xml the document xml.
	 * @return the new document xml with all namespace prefixes equal to the ones in the original document.
	 */
	public static String normalizeNameSpace(Document doc, String xml){
		//We use xf prefix. So if the loaded xform
		//did not use the same prefix, then replace our prefix with that
		//which came with the xform.
		String prefix = doc.getDocumentElement().getPrefix();
			
		if(!"xf".equals(prefix)){
			if(prefix == null || prefix.trim().length() == 0)
				prefix = "";
			else
				prefix += ":";
			
			xml = xml.replace("xf:", prefix);
		}
		
		return xml;
	}
	
	
	/**
	 * Gets the instance node of an xforms document.
	 * 
	 * @param doc the xforms document.
	 * @return the instance node.
	 */
	public static Element getInstanceNode(Document doc){
		return getInstanceNode(doc.getDocumentElement());
	}

	
	/**
	 * Gets the instance data node of an xforms document.
	 * 
	 * @param doc the xforms document.
	 * @return the instance data node.
	 */
	public static Element getInstanceDataNode(Document doc){
		return getInstanceDataNode(getInstanceNode(doc));
	}

	
	/**
	 * Gets the instance data node of an xforms document as a new document.
	 * 
	 * @param doc the xforms document.
	 * @return the new document having the instance data node as its root node.
	 */
	public static Document getInstanceDataDoc(Document doc){
		Element data = getInstanceDataNode(getInstanceNode(doc));
		Document dataDoc = XMLParser.createDocument();
		dataDoc.appendChild(dataDoc.createProcessingInstruction("xml", "version=\"1.0\" encoding=\"UTF-8\""));
		dataDoc.appendChild(data.cloneNode(true));

		Element root = dataDoc.getDocumentElement();
		NamedNodeMap attributes = doc.getDocumentElement().getAttributes();
		for(int index = 0; index < attributes.getLength(); index++){
			Node attribute = attributes.item(index);
			String name = attribute.getNodeName();
			if(name.startsWith("xmlns:")){
				try{
					root.setAttribute(name, attribute.getNodeValue());
				}
				catch(Exception ex){
					ex.printStackTrace();
				}
			}
		}

		return dataDoc;
	}

	
	/**
	 * Gets the instance node of an xforms document with a given root node.
	 * 
	 * @param element the root node of the xforms document.
	 * @return the instance node.
	 */
	public static Element getInstanceNode(Element element){
		int numOfEntries = element.getChildNodes().getLength();
		for (int i = 0; i < numOfEntries; i++) {
			if (element.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element child = (Element)element.getChildNodes().item(i);
				//String tagname = getNodeName(child);
				String tagname = child.getNodeName(); //NODE_NAME_INSTANCE has prefix
				//if(tagname.equals(NODE_NAME_INSTANCE)||tagname.equals(NODE_NAME_INSTANCE_MINUS_PREFIX))
				if(XmlUtil.nodeNameEquals(tagname,XformConstants.NODE_NAME_INSTANCE_MINUS_PREFIX))
					return child;
				else{
					child = getInstanceNode(child);
					if(child != null)
						return child;
				}
			}
		}
		return null;
	}

	
	/**
	 * Gets the instance data node of an xforms document with a given root node.
	 * 
	 * @param element the xforms document root node.
	 * @return the instance data node.
	 */
	public static Element getInstanceDataNode(Element element){
		int numOfEntries = element.getChildNodes().getLength();
		for (int i = 0; i < numOfEntries; i++) {
			if (element.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) 
				return (Element)element.getChildNodes().item(i);
		}

		return null;
	}
}
