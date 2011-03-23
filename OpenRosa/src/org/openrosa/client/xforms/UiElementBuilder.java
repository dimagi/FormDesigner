package org.openrosa.client.xforms;

import java.util.List;

import org.openrosa.client.model.FormDef;
import org.openrosa.client.model.IFormElement;
import org.openrosa.client.model.OptionDef;
import org.openrosa.client.model.QuestionDef;
import org.openrosa.client.util.FormUtil;
import org.openrosa.client.util.Itext;
import org.openrosa.client.xforms.XformConstants;

import com.google.gwt.user.client.Window;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;


/**
 * Builds xforms UI elements like input, select, select1, etc. 
 * from question definition objects of the form model.
 * 
 * @author daniel
 *
 */
public class UiElementBuilder {

	/**
	 * All methods in this class are static and hence we expect no external
	 * Instantiation of this class.
	 */
	private UiElementBuilder(){

	}
	
	
	/**
	 * Converts a question definition object to xforms.
	 * 
	 * @param qtn the question definition object.
	 * @param doc the xforms document.
	 * @param xformsNode the root node of the xforms document.
	 * @param formDef the form definition object to which the question belongs.
	 * @param formNode the xforms instance data node.
	 * @param modelNode the xforms model node.
	 * @param groupNode the xforms group node to which the question belongs.
	 */
	public static void fromQuestionDef2Xform(IFormElement qtn, Document doc, Element xformsNode, FormDef formDef, Element formNode, Element modelNode,Element groupNode){
		if(qtn.getParent() != null && qtn.getParent().getDataType() == QuestionDef.QTN_TYPE_REPEAT){
			formNode = qtn.getParent().getDataNode();
		}
		Element dataNode =  XformBuilderUtil.fromVariableName2Node(doc,qtn.getBinding(),formDef,formNode);
		if(qtn.getDefaultValue() != null && qtn.getDefaultValue().trim().length() > 0){
			dataNode.appendChild(doc.createTextNode(qtn.getDefaultValue()));
		}
		qtn.setDataNode(dataNode);

		Element bindNode =  doc.createElement(XformConstants.NODE_NAME_BIND);
		String id = XformBuilderUtil.getBindIdFromVariableName(qtn.getBinding(),false);
		bindNode.setAttribute(XformConstants.ATTRIBUTE_NAME_ID, id);

		String nodeset = qtn.getBinding();
		if(!nodeset.startsWith("/"))
			nodeset = "/" + nodeset;
		if(!nodeset.startsWith("/" + formDef.getVariableName() + "/"))
			nodeset = "/" + formDef.getVariableName() + "/" + qtn.getBinding();
		bindNode.setAttribute(XformConstants.ATTRIBUTE_NAME_NODESET, nodeset);

		if(qtn.getDataType() != QuestionDef.QTN_TYPE_REPEAT)
			bindNode.setAttribute(XformConstants.ATTRIBUTE_NAME_TYPE, XformBuilderUtil.getXmlType(qtn.getDataType(),bindNode));	
		if(qtn.isRequired())
			bindNode.setAttribute(XformConstants.ATTRIBUTE_NAME_REQUIRED, XformConstants.XPATH_VALUE_TRUE);
		if(!qtn.isEnabled())
			bindNode.setAttribute(XformConstants.ATTRIBUTE_NAME_READONLY, XformConstants.XPATH_VALUE_TRUE);
		if(qtn.isLocked())
			bindNode.setAttribute(XformConstants.ATTRIBUTE_NAME_LOCKED, XformConstants.XPATH_VALUE_TRUE);
		if(!qtn.isVisible())
			bindNode.setAttribute(XformConstants.ATTRIBUTE_NAME_VISIBLE, XformConstants.XPATH_VALUE_FALSE);

		String bindAttributeName = XformConstants.ATTRIBUTE_NAME_REF;
		if(!groupNode.getNodeName().equals(XformConstants.NODE_NAME_REPEAT)){
			modelNode.appendChild(bindNode);
			qtn.setBindNode(bindNode);
			bindAttributeName = XformConstants.ATTRIBUTE_NAME_BIND;
		}	

		Element uiNode =  buildXformUIElement(doc,qtn,bindAttributeName,false);
		if(groupNode != null){ //Some forms may not be in groups
			groupNode.appendChild(uiNode);
		}else{
			xformsNode.appendChild(uiNode);
		}
		qtn.setControlNode(uiNode);

		Element labelNode =  doc.createElement(XformConstants.NODE_NAME_LABEL);
		labelNode.appendChild(doc.createTextNode(qtn.getText()));
		addItextRefs(labelNode, qtn);
		uiNode.appendChild(labelNode);
		qtn.setLabelNode(labelNode);

		addHelpTextNode(qtn,doc,uiNode,null);

		if(qtn.getDataType() != QuestionDef.QTN_TYPE_REPEAT){
			if(qtn.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC)
				((QuestionDef)qtn).setFirstOptionNode(ItemsetBuilder.createDynamicOptionDefNode(doc,uiNode));
			else{
				List options = qtn.getChildren();
				if(options != null && options.size() > 0){
					for(int j=0; j<options.size(); j++){
						OptionDef optionDef = (OptionDef)options.get(j);
						Element itemNode = fromOptionDef2Xform(optionDef,doc,uiNode);	
						if(j == 0)
							((QuestionDef)qtn).setFirstOptionNode(itemNode);
					}
				}
			}
		}
		else{
			Element repeatNode =  doc.createElement(XformConstants.NODE_NAME_REPEAT);
			repeatNode.setAttribute(XformConstants.ATTRIBUTE_NAME_BIND, id);
			uiNode.appendChild(repeatNode);
			qtn.setControlNode(repeatNode);

			List<IFormElement> rptQtns = ((QuestionDef)qtn).getRepeatQtnsDef().getChildren();
			if(rptQtns == null){
				return;
			}
			for(int j=0; j<rptQtns.size(); j++)
				createQuestion(rptQtns.get(j),repeatNode,dataNode,doc);
		}
	}
	
	/**
	 * Adds a ref="jr:itext('itext_id')" attribute
	 * to the given node, if the itextID specified by
	 * def exists in the Itext object.
	 * 
	 * Will not alter the DOM node if no itext for that itextID is present.
	 * 
	 * @param element - Element DOM that requires the itext reference
	 * @param def - IFormElement object containing the ItextID
	 * @return the (possibly altered) element passed in as an argument.
	 */
	public static Element addItextRefs(Element element, IFormElement def){
		if(Itext.getDefaultLocale().hasID(def.getItextId())){
			element.setAttribute("ref", "jr:itext('"+def.getItextId()+"')");
		}else{
			element.removeAttribute("ref");
		}
		
		return element;
	}
	
	public static Element addHelpItextRefs(Element element, IFormElement def){
		if(Itext.getDefaultLocale().hasID(def.getItextId()+";hint")){
			element.setAttribute("ref", "jr:itext('"+def.getItextId()+"_hint')");
		}
		
		if(def.getHelpText()!= null && !def.getHelpText().isEmpty()){
			element.setNodeValue(def.getHelpText());
		}
		
		return element;
	}


	/**
	 * Creates an xforms ui node for a child question of a parent repeat question type.
	 * 
	 * @param qtnDef the child question definition object.
	 * @param parentControlNode the ui node of the parent repeat question.
	 * @param parentDataNode the data node of the parent repeat question.
	 * @param doc the xforms document.
	 */
	private static void createQuestion(IFormElement qtnDef, Element parentControlNode, Element parentDataNode, Document doc){
		String name = qtnDef.getBinding();

		//TODO Should do this for all invalid characters in node names.
		name = name.replace("/", "");
		name = name.replace("\\", "");
		name = name.replace(" ", "");

		Element dataNode =  doc.createElement(name);
		if(qtnDef.getDefaultValue() != null && qtnDef.getDefaultValue().trim().length() > 0)
			dataNode.appendChild(doc.createTextNode(qtnDef.getDefaultValue()));
		parentDataNode.appendChild(dataNode);
		qtnDef.setDataNode(dataNode);

		Element inputNode =  buildXformUIElement(doc,qtnDef,XformConstants.ATTRIBUTE_NAME_REF,true);
		inputNode.setAttribute(XformConstants.ATTRIBUTE_NAME_TYPE, XformBuilderUtil.getXmlType(qtnDef.getDataType(),inputNode));
		if(qtnDef.isRequired())
			inputNode.setAttribute(XformConstants.ATTRIBUTE_NAME_REQUIRED, XformConstants.XPATH_VALUE_TRUE);
		if(!qtnDef.isEnabled())
			inputNode.setAttribute(XformConstants.ATTRIBUTE_NAME_READONLY, XformConstants.XPATH_VALUE_TRUE);
		if(qtnDef.isLocked())
			inputNode.setAttribute(XformConstants.ATTRIBUTE_NAME_LOCKED, XformConstants.XPATH_VALUE_TRUE);
		if(!qtnDef.isVisible())
			inputNode.setAttribute(XformConstants.ATTRIBUTE_NAME_VISIBLE, XformConstants.XPATH_VALUE_FALSE);

		parentControlNode.appendChild(inputNode);
		qtnDef.setControlNode(inputNode);
		qtnDef.setBindNode(inputNode);

		Element labelNode =  doc.createElement(XformConstants.NODE_NAME_LABEL);
		labelNode.appendChild(doc.createTextNode(qtnDef.getText()));
		inputNode.appendChild(labelNode);
		qtnDef.setLabelNode(labelNode);

		addHelpTextNode(qtnDef,doc,inputNode,null);

		if(qtnDef.getDataType() != QuestionDef.QTN_TYPE_REPEAT){
			List options = qtnDef.getChildren();
			if(options != null && options.size() > 0){
				for(int index=0; index<options.size(); index++){
					OptionDef optionDef = (OptionDef)options.get(index);
					Element itemNode = fromOptionDef2Xform(optionDef,doc,inputNode);	
					if(index == 0)
						((QuestionDef)qtnDef).setFirstOptionNode(itemNode);
				}
			}
		}
	}


	/**
	 * Gets the xforms ui node for a given question definition object.
	 * 
	 * @param doc the xforms document.
	 * @param qtnDef the question definition object.
	 * @param bindAttributeName the attribute name for binding. Could be "bind" or "ref".
	 * @param isRepeatKid set to true if this question is a child of another repeat question type.
	 * @return the xforms ui node.
	 */
	private static Element buildXformUIElement(Document doc, IFormElement qtnDef, String bindAttributeName, boolean isRepeatKid){

		String name = XformConstants.NODE_NAME_INPUT;

		int type = qtnDef.getDataType();
		if(type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC)
			name = XformConstants.NODE_NAME_SELECT1;
		else if(type == QuestionDef.QTN_TYPE_LIST_MULTIPLE)
			name = XformConstants.NODE_NAME_SELECT;
		else if(type == QuestionDef.QTN_TYPE_REPEAT)
			name = XformConstants.NODE_NAME_GROUP;
		else if(type == QuestionDef.QTN_TYPE_IMAGE || type == QuestionDef.QTN_TYPE_AUDIO || type == QuestionDef.QTN_TYPE_VIDEO)
			name = XformConstants.NODE_NAME_UPLOAD;
		else if(type == QuestionDef.QTN_TYPE_LABEL)
			name = XformConstants.NODE_NAME_TRIGGER;

		String id = XformBuilderUtil.getBindIdFromVariableName(qtnDef.getBinding(), isRepeatKid);
		Element node = createElementNS(name,null,null);
		if(qtnDef.getDataType() != QuestionDef.QTN_TYPE_REPEAT)
			node.setAttribute(bindAttributeName, id);
		else
			node.setAttribute(XformConstants.ATTRIBUTE_NAME_ID, qtnDef.getBinding());

		setMediaType(node, type);
		
		//if(qtnDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || qtnDef.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE)
		//	node.setAttribute("selection", "closed");

		return node;
	}
	
//	  public static native Element createElementNS(Document doc, String tag, String NS) /*-{
//	    return doc.createElementNS(NS, tag);
//	  }-*/;
	
	/**
	 * @param localName the local name of the element to create
	 * @param prefix the namespace prefix to use
	 * @param namespaceUri the namespace URI to use
	 * @return
	 */
	public static Element createElementNS(String localName, String prefix, String namespaceUri) {
		
		String tagName = (prefix != null) ? prefix + ":" +localName : localName;
		
		String xmlnsAttribute = null;
		
		if (namespaceUri != null) {
			xmlnsAttribute = "xmlns";
			if (prefix != null) {
				xmlnsAttribute += ":" +prefix;
			}
			xmlnsAttribute += "=\"" + namespaceUri + "\"";
		}

		String template = "<" + tagName + " ";
		
		if (xmlnsAttribute != null) {
			template += xmlnsAttribute + " ";
		}
		
		template += "/>";
		
		Element e = XMLParser.parse(template).getDocumentElement();
		
		// return clone to work around chrome wrong document error
		// see http://code.google.com/p/google-web-toolkit/issues/detail?id=4074
		Element clone = (Element) e.cloneNode(true);
//		Window.alert ("using 	cool createElementNS");
		return clone;
//		return e;
	}

	public static void setMediaType(Element node, int type){
		String mediatype = null;
		if(type == QuestionDef.QTN_TYPE_IMAGE)
			mediatype = XformConstants.ATTRIBUTE_VALUE_IMAGE;
		else if(type == QuestionDef.QTN_TYPE_AUDIO)
			mediatype = XformConstants.ATTRIBUTE_VALUE_AUDIO;
		else if(type == QuestionDef.QTN_TYPE_VIDEO)
			mediatype = XformConstants.ATTRIBUTE_VALUE_VIDEO;
		
		if(mediatype != null)
			node.setAttribute(XformConstants.ATTRIBUTE_NAME_MEDIATYPE, mediatype + "/*");
	}
	

	/**
	 * Converts an option definition object to xforms.
	 * 
	 * @param optionDef the option definition object.
	 * @param doc the xforms document.
	 * @param uiNode the xforms ui node of the question to which this option belongs.
	 * @return the item node of the option definition object.
	 */
	public static Element fromOptionDef2Xform(OptionDef optionDef, Document doc, Element uiNode){
		Element itemNode =  doc.createElement(XformConstants.NODE_NAME_ITEM);
		itemNode.setAttribute(XformConstants.ATTRIBUTE_NAME_ID, optionDef.getBinding());

		Element node =  doc.createElement(XformConstants.NODE_NAME_LABEL);
		node.appendChild(doc.createTextNode(optionDef.getText()));
		addItextRefs(node, optionDef);
		itemNode.appendChild(node);
		optionDef.setLabelNode(node);

		node =  doc.createElement(XformConstants.NODE_NAME_VALUE);
		node.appendChild(doc.createTextNode(optionDef.getBinding()));
		itemNode.appendChild(node);
		optionDef.setValueNode(node);

		uiNode.appendChild(itemNode);
		optionDef.setControlNode(itemNode);
		return itemNode;
	}


	/**
	 * Sets the xforms help text or hint node for a question.
	 * 
	 * @param qtn the question definition object.
	 * @param doc the xforms document.
	 * @param inputNode the xforms ui node.
	 * @param firstOptionNode the first option node if a single or multiple select question type.
	 */
	public static void addHelpTextNode(IFormElement qtn, Document doc, Element inputNode, Element firstOptionNode){
		String helpText = qtn.getHelpText();
		if(FormUtil.shouldHaveHintDOMNode(qtn)){
			Element hintNode =  doc.createElement(XformConstants.NODE_NAME_HINT);
			hintNode.appendChild(doc.createTextNode(helpText));
			addHelpItextRefs(hintNode, qtn);
			qtn.setHintNode(hintNode);
			if(firstOptionNode == null)
				inputNode.appendChild(hintNode);
			else
				inputNode.insertBefore(hintNode, firstOptionNode);
			qtn.setHintNode(hintNode);
		}
	}
}
