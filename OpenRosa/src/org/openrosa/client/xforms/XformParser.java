package org.openrosa.client.xforms;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.openrosa.client.model.Calculation;
import org.openrosa.client.model.FormDef;
import org.openrosa.client.model.GroupDef;
import org.openrosa.client.model.IFormElement;
import org.openrosa.client.model.OptionDef;
import org.openrosa.client.model.QuestionDef;
import org.openrosa.client.model.RepeatQtnsDef;
import org.openrosa.client.util.ItextParser;
import org.openrosa.client.util.FormUtil;
import org.openrosa.client.xforms.XformConstants;
import org.openrosa.client.xforms.XformUtil;
import org.openrosa.client.xforms.XmlUtil;
import org.openrosa.client.xpath.XPathExpression;

import com.google.gwt.core.client.GWT;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;


/**
 * Parse xforms documents and builds the form definition object model.
 * 
 * @author daniel
 *
 */
public class XformParser {

	/** The current question id. */
	private static int currentQuestionId = 1;

	/** The current page number. */
	private static int currentPageNo = 1;


	/**
	 * All methods in this class are static and hence we expect no external
	 * Instantiation of this class.
	 */
	private XformParser(){

	}


	/**
	 * Gets a new question id.
	 * 
	 * @return the new question id
	 */
	private static int getNextQuestionId(){
		return currentQuestionId++;
	}


	/**
	 * Gets a new page number.
	 * 
	 * @return the new page number.
	 */
	private static int getNextPageNo(){
		return currentPageNo++;
	}


	/**
	 * Creates a copy of a formDef together with its xform xml.
	 * 
	 * @param formDef the form to copy.
	 * @return the new copy of the form.
	 */
	public static FormDef copyFormDef(FormDef formDef){
		if(formDef.getDoc() == null)
			return new FormDef(formDef);
		else //Value of false creates bugs where repeat widgets are not loaded properly on data preview
			formDef.updateDoc(true); //formDef.updateDoc(false);

		return fromXform2FormDef(XformUtil.normalizeNameSpace(formDef.getDoc(),XmlUtil.fromDoc2String(formDef.getDoc())));

		/*if(formDef.getDoc() == null)
			return new FormDef(formDef);
		else
			formDef.updateDoc(false);

		return fromXform2FormDef(XformUtil.normalizeNameSpace(formDef.getDoc(),XmlUtil.fromDoc2String(formDef.getDoc())));*/
	}

//	/**
//	 * Converts an xml document to a form definition object.
//	 * 
//	 * @param xml the document xml.
//	 * @return the form definition object.
//	 */
//	public static FormDef fromXform2FormDef(String xml, HashMap<Integer,HashMap<String,String>> languageText){
//		Document doc = XmlUtil.getDocument(xml);
//
//		String layoutXml = null, javaScriptSrc = null; NodeList nodes = null;
//		Element root = doc.getDocumentElement();
//		if(root.getNodeName().equals("PurcForm")){
//			nodes = root.getElementsByTagName("Xform");
//			assert(nodes.getLength() > 0);
//			xml = XmlUtil.getChildElement(nodes.item(0)).toString();
//			doc = XmlUtil.getDocument(xml);
//
//			nodes = root.getElementsByTagName("Layout");
//			if(nodes.getLength() > 0)
//				layoutXml = FormUtil.formatXml(XmlUtil.getChildElement(nodes.item(0)).toString());
//
//			nodes = root.getElementsByTagName("JavaScript");
//			if(nodes.getLength() > 0)
//				javaScriptSrc = XmlUtil.getChildCDATA(nodes.item(0)).getNodeValue();
//
//			nodes = root.getElementsByTagName("LanguageText"); 
//			assert(nodes.getLength() > 0);
//		}
//
//		FormDef formDef = getFormDef(doc);
//
//		if(layoutXml != null)
//			formDef.setLayoutXml(FormUtil.formatXml(layoutXml));
//
//		if(javaScriptSrc != null)
//			formDef.setJavaScriptSource(javaScriptSrc);
//
//		if(nodes != null){
//			loadLanguageText(formDef.getId(),nodes,languageText);
//			formDef.setXformXml(FormUtil.formatXml(xml));
//		}
//
//		return formDef;
//	}


	public static void loadLanguageText(Integer formId, NodeList nodes, HashMap<Integer,HashMap<String,String>> languageText){
		for(int index = 0; index < nodes.getLength(); index++){
			Element node = (Element)nodes.item(index);

			HashMap<String,String> map = languageText.get(formId);
			if(map == null){
				map = new HashMap<String,String>();
				languageText.put(formId, map);
			}

			map.put(node.getAttribute("lang"), FormUtil.formatXml(node.toString()));
		}
	}


	/**
	 * Converts an xml document to a form definition object.
	 * 
	 * @param xml the document xml.
	 * @return the form definition object.
	 */
	public static FormDef fromXform2FormDef(String xml){
		Document doc = XmlUtil.getDocument(xml);
		FormDef formDef = getFormDef(doc);
		formDef.setId(1);
		return formDef;
	}

	/**
	 * Converts an xforms document into a form definition object and also
	 * replaces its model with the given one.
	 * 
	 * @param xformXml the xforms document xml.
	 * @param modelXml the new xforms model xml.
	 * @return the form definition object.
	 */
	public static FormDef fromXform2FormDef(String xformXml, String modelXml){
		Document doc = XmlUtil.getDocument(xformXml);

		//If model xml has been supplied, use it to replace the existing one.
		if(modelXml != null){
			Element node = XmlUtil.getDocument(modelXml).getDocumentElement();//XformConverter.getNode(XformConverter.getDocument(modelXml).getDocumentElement().toString());
			Element dataNode = XformUtil.getInstanceDataNode(doc);
			Node parent = dataNode.getParentNode();
			parent.appendChild(node);
			parent.replaceChild(node,dataNode);
		}

		return getFormDef(doc);
	}


	/**
	 * Converts an xml document object to a form definition object.
	 * 
	 * @param doc the xml document object.
	 * @return the form definition object.
	 */
	public static FormDef getFormDef(Document doc){
		Element rootNode = doc.getDocumentElement();
		FormDef formDef = new FormDef();
		formDef.setDoc(doc);
		formDef.setId(1);
		HashMap id2VarNameMap = new HashMap();
		HashMap relevants = new HashMap();
		HashMap constraints = new HashMap();
		Vector repeats = new Vector();
		HashMap rptKidMap = new HashMap();
		List<QuestionDef> orphanDynOptionQns = new ArrayList<QuestionDef>();

		currentQuestionId = 1;
		currentPageNo = 1;

		parseElement(formDef,rootNode,id2VarNameMap,null,relevants,repeats,rptKidMap,(int)0,null,constraints,orphanDynOptionQns);

		if(formDef.getName() == null || formDef.getName().length() == 0)
			formDef.setName(formDef.getQuestionID());

		DefaultValueUtil.setDefaultValues(XformUtil.getInstanceDataNode(doc),formDef,id2VarNameMap); //TODO Very slow needs optimisation for very big forms
		RelevantParser.addSkipRules(formDef,relevants);
		ConstraintParser.addValidationRules(formDef,constraints);

		ItemsetParser.parseOrphanDynOptionQns(formDef,orphanDynOptionQns);

		//Remove all that we had created as questions when parsing bindings but will not require
		//user input (eg JR's DeviceId, EndTime), since questions are only for cases where we want user input.
		//TODO Needs to be fixed when having multiple groups
//		removeElementsWithoutControlNode(formDef.getChildren());

		return formDef;
	}

	private static void removeElementsWithoutControlNode(List<IFormElement> elements){
		if(elements == null)
			return;
		
		for(int index = 0; index < elements.size(); index++){
			IFormElement element = elements.get(index);
			if(element.getControlNode() == null && !(element instanceof GroupDef)){
				//element.getParent().removeChild(element); //We do not want to lose the bindings if any.
				GWT.log("removing node:"+element.getQuestionID());
				element.getParent().getChildren().remove(element);
				index--;
			}
			else if(element instanceof GroupDef){
//				removeElementsWithoutText(element.getChildren());
			}
		}
	}


	/**
	 * Parses an xforms document and builds a form definition object.
	 * 
	 * @param formDef the form definition object that we are building.
	 * @param element the element that we are currently parsing.
	 * @param id2VarNameMap a map of question bind ids to the variable names.
	 * @param questionDef the question definition object that is currently being parsed.
	 * @param relevants the map of constraint attribute values keyed by their 
	 * 					  question definition objects.
	 * @param repeatQtns a list of repeat question types.
	 * @param rptKidMap a map of question definition objects which are children of a repeat 
	 * 					question type, keyed by their variable names.
	 * @param currentPageNo the number of the current page we are parsing.
	 * @param parentQtn the parent of the question we are currently processing.
	 * @param constraints the map of constraint attribute values keyed by their 
	 * 					  question definition objects.
	 * @param orphanDynOptionQns a list of dynamic option definition questions who parent
	 *                           questions have not yet been parsed.
	 * @return the question we are currently parsing.
	 */
	private static IFormElement parseElement(FormDef formDef, Element element, HashMap id2VarNameMap,IFormElement questionDef,HashMap relevants,Vector repeatQtns, HashMap rptKidMap, int currentPageNo, IFormElement parentQtn, HashMap constraints, List<QuestionDef> orphanDynOptionQns){
		String label = "";
		String hint = "";
		String value = "";
		Element labelNode = null;
		Element hintNode = null;
		Element valueNode = null;

		//TODO wiered bug here for some forms, nodes.getLength() returns a value less
		//than numOfEntries during the loop. So something could be changing the node list
		NodeList nodes = element.getChildNodes();
		int numOfEntries = nodes.getLength();
		for (int i = 0; i < numOfEntries; i++) {
			if(nodes.item(i) == null || nodes.item(i).getNodeType() != Node.ELEMENT_NODE)
				continue;

			Element child = (Element)nodes.item(i);
			String tagname = child.getNodeName(); //getNodeName(child);

			//if(tagname.equals(NODE_NAME_SUBMIT) || tagname.equals(NODE_NAME_SUBMIT_MINUS_PREFIX))
			if(XmlUtil.nodeNameEquals(tagname,XformConstants.NODE_NAME_SUBMIT_MINUS_PREFIX))
				continue;
			else if (XmlUtil.nodeNameEquals(tagname,"head"))
				parseElement(formDef,child,id2VarNameMap,questionDef,relevants,repeatQtns,rptKidMap,currentPageNo,parentQtn,constraints,orphanDynOptionQns);
			else if (XmlUtil.nodeNameEquals(tagname,"body")){
				formDef.setBodyNode(child);
				parseElement(formDef, child,id2VarNameMap,questionDef,relevants,repeatQtns,rptKidMap,currentPageNo,parentQtn,constraints,orphanDynOptionQns);
			}
			else if (XmlUtil.nodeNameEquals(tagname,"title")){
				formDef.setName(getText(child));
				formDef.setItextId(XmlUtil.getItextId(child));
			}
			//else if (tagname.equals(NODE_NAME_MODEL) || tagname.equals(NODE_NAME_MODEL_MINUS_PREFIX)){
			else if(XmlUtil.nodeNameEquals(tagname,XformConstants.NODE_NAME_MODEL_MINUS_PREFIX)){
				formDef.setModelNode((Element)child);
				formDef.setXformsNode(child.getOwnerDocument().getDocumentElement() /*child.getParentNode()*/);
				parseElement(formDef, child,id2VarNameMap,questionDef,relevants,repeatQtns,rptKidMap,currentPageNo,parentQtn,constraints,orphanDynOptionQns);
			}
			//else if (tagname.equals(NODE_NAME_GROUP) || tagname.equals(NODE_NAME_GROUP_MINUS_PREFIX)){
			else if(XmlUtil.nodeNameEquals(tagname,XformConstants.NODE_NAME_GROUP_MINUS_PREFIX)){
				/*questionDef = */parseGroupElement(formDef, child,id2VarNameMap,questionDef,relevants,repeatQtns,rptKidMap,currentPageNo,parentQtn,constraints,orphanDynOptionQns);
			}
			//else if(tagname.equals(NODE_NAME_INSTANCE)||tagname.equals(NODE_NAME_INSTANCE_MINUS_PREFIX)) {
			else if(XmlUtil.nodeNameEquals(tagname,XformConstants.NODE_NAME_INSTANCE_MINUS_PREFIX)){
				parseInstanceElement(formDef, child);
			} 
			//else if (tagname.equals(NODE_NAME_BIND)||tagname.equals(NODE_NAME_BIND_MINUS_PREFIX) /*|| tagname.equals(ATTRIBUTE_NAME_REF)*/) {
			else if(XmlUtil.nodeNameEquals(tagname,XformConstants.NODE_NAME_BIND_MINUS_PREFIX)){
				IFormElement qtn = parseBindElement(formDef, child,id2VarNameMap,questionDef,relevants,repeatQtns,rptKidMap,currentPageNo,parentQtn,constraints,orphanDynOptionQns);
				qtn.setHasUINode(false);
			} 
			//else if (tagname.equals(NODE_NAME_INPUT) || tagname.equals(NODE_NAME_SELECT1) || tagname.equals(NODE_NAME_SELECT) || tagname.equals(NODE_NAME_REPEAT)
			//		|| tagname.equals(NODE_NAME_INPUT_MINUS_PREFIX) || tagname.equals(NODE_NAME_SELECT1_MINUS_PREFIX) || tagname.equals(NODE_NAME_SELECT_MINUS_PREFIX) || tagname.equals(NODE_NAME_REPEAT_MINUS_PREFIX)) {
			else if(XmlUtil.nodeNameEquals(tagname,XformConstants.NODE_NAME_INPUT_MINUS_PREFIX) || XmlUtil.nodeNameEquals(tagname,XformConstants.NODE_NAME_SELECT1_MINUS_PREFIX) || 
					XmlUtil.nodeNameEquals(tagname,XformConstants.NODE_NAME_SELECT_MINUS_PREFIX) ||
					XmlUtil.nodeNameEquals(tagname,XformConstants.NODE_NAME_UPLOAD_MINUS_PREFIX) ||
					XmlUtil.nodeNameEquals(tagname,XformConstants.NODE_NAME_TRIGGER_MINUS_PREFIX)){

				NodeContext nodeContext = new NodeContext(label, hint, value, labelNode, hintNode, valueNode);
				questionDef = parseUiElement(formDef, child,id2VarNameMap,questionDef,relevants,repeatQtns,rptKidMap,currentPageNo,parentQtn,constraints,orphanDynOptionQns,nodeContext);
				questionDef.setHasUINode(true);
				label = nodeContext.getLabel();
				hint = nodeContext.getHint();
				value = nodeContext.getValue();
				labelNode = nodeContext.getLabelNode();
				hintNode = nodeContext.getHintNode();
				valueNode = nodeContext.getValueNode();
			} 
			else if(XmlUtil.nodeNameEquals(tagname,XformConstants.NODE_NAME_REPEAT_MINUS_PREFIX)){
				// do nothing. Repeats are dealt with in parseGroupElement()
			}
			//else if(tagname.equals(NODE_NAME_ITEMSET)||tagname.equals(NODE_NAME_ITEMSET_MINUS_PREFIX)){
			else if(XmlUtil.nodeNameEquals(tagname,XformConstants.NODE_NAME_ITEMSET_MINUS_PREFIX)){
				questionDef.setDataType(QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC);
				((QuestionDef)questionDef).setFirstOptionNode(child);
				ItemsetParser.parseDynamicOptionsList((QuestionDef)questionDef,child.getAttribute(XformConstants.ATTRIBUTE_NAME_NODESET),formDef,orphanDynOptionQns);
			}
			//else if(tagname.equals(NODE_NAME_LABEL)||tagname.equals(NODE_NAME_LABEL_MINUS_PREFIX)){
			else if(XmlUtil.nodeNameEquals(tagname,XformConstants.NODE_NAME_LABEL_MINUS_PREFIX)){

				NodeContext nodeContext = new NodeContext(label, hint, value, labelNode, hintNode, valueNode);;
				parseLabelElement(formDef, child, questionDef, nodeContext);

				if(questionDef instanceof GroupDef)
					setLabelValueNode(formDef, element, questionDef, parentQtn, nodeContext);

				label = nodeContext.getLabel();
				hint = nodeContext.getHint();
				value = nodeContext.getValue();
				labelNode = nodeContext.getLabelNode();
				hintNode = nodeContext.getHintNode();
				valueNode = nodeContext.getValueNode();
			}
			//else if (tagname.equals(NODE_NAME_HINT)||tagname.equals(NODE_NAME_HINT_MINUS_PREFIX)){
			else if(XmlUtil.nodeNameEquals(tagname,XformConstants.NODE_NAME_HINT_MINUS_PREFIX)){

				NodeContext nodeContext = new NodeContext(label, hint, value, labelNode, hintNode, valueNode);;
				parseHintElement(formDef, child, questionDef, nodeContext);

				label = nodeContext.getLabel();
				hint = nodeContext.getHint();
				value = nodeContext.getValue();
				labelNode = nodeContext.getLabelNode();
				hintNode = nodeContext.getHintNode();
				valueNode = nodeContext.getValueNode();
			}
			//else if (tagname.equals(NODE_NAME_ITEM)||tagname.equals(NODE_NAME_ITEM_MINUS_PREFIX))
			else if(XmlUtil.nodeNameEquals(tagname,XformConstants.NODE_NAME_ITEM_MINUS_PREFIX)){
				OptionDef optionDef = new OptionDef((QuestionDef)questionDef);
				parseElement(formDef, child,id2VarNameMap,optionDef,relevants,repeatQtns,rptKidMap,currentPageNo,questionDef,constraints,orphanDynOptionQns);
			//else if (tagname.equals(NODE_NAME_VALUE)||tagname.equals(NODE_NAME_VALUE_MINUS_PREFIX)){
			}else if(XmlUtil.nodeNameEquals(tagname,XformConstants.NODE_NAME_VALUE_MINUS_PREFIX)){
				if(true /*child.getChildNodes().getLength() != 0*/){
					value = getText(child);
					if(value == null){ value = ""; }
					valueNode = child;
				}
			}
			else
				parseElement(formDef, child,id2VarNameMap,questionDef,relevants,repeatQtns,rptKidMap,currentPageNo,parentQtn,constraints,orphanDynOptionQns);
			// TODO - how are other elements like html:p or br handled?
		}

		NodeContext nodeContext = new NodeContext(label, hint, value, labelNode, hintNode, valueNode);;
		setLabelValueNode(formDef, element, questionDef, parentQtn, nodeContext);

		label = nodeContext.getLabel();
		hint = nodeContext.getHint();
		value = nodeContext.getValue();
		labelNode = nodeContext.getLabelNode();
		hintNode = nodeContext.getHintNode();
		valueNode = nodeContext.getValueNode();

		return questionDef;
	}


	/**
	 * Sets the label and value nodes of the current object being parsed.
	 * 
	 * @param formDef the form definition object.
	 * @param element the element we are currently parsing.
	 * @param questionDef the question we are currently parsing.
	 * @param parentQtn the parent of the question we are currently processing.
	 * @param nodeContext the node context.
	 */
	private static void setLabelValueNode(FormDef formDef, Element element, IFormElement questionDef, IFormElement parentQtn, NodeContext nodeContext){
		boolean hasLabel = !nodeContext.getLabel().isEmpty() ||
									(	(nodeContext.getLabelNode() != null) &&
										(nodeContext.getLabelNode().getAttribute("ref") != null) &&
										(!nodeContext.getLabelNode().getAttribute("ref").isEmpty())	);
		
		boolean hasValue = !nodeContext.getValue().isEmpty();
		
		if (hasLabel && hasValue) {
			if (parentQtn instanceof QuestionDef && parentQtn != null && parentQtn.getChildren() != null){
				if(((QuestionDef)parentQtn).getOptions().size() == 0){
					((QuestionDef)parentQtn).setFirstOptionNode(element);
				}
				OptionDef option = ((OptionDef)questionDef);
				option.setId(Integer.parseInt(String.valueOf(parentQtn.getChildren().size())));
				option.setText(nodeContext.getLabel());
				option.setQuestionID(nodeContext.getValue());
				option.setParent((QuestionDef)parentQtn);

				option.setControlNode(element);
				element.removeAttribute("id");
				option.setLabelNode(nodeContext.getLabelNode());
				option.setValueNode(nodeContext.getValueNode());
				option.setDefaultValue(nodeContext.getValue());
				((QuestionDef)parentQtn).addOption(option);

			}
		}
		else if (hasLabel && questionDef != null){
			if(questionDef.getText() == null || questionDef.getText().trim().isEmpty()){

				if(questionDef != parentQtn && parentQtn instanceof GroupDef && questionDef.getParent() != parentQtn){
					questionDef.getParent().getChildren().remove(questionDef);
					parentQtn.addChild(questionDef);
				}

				questionDef.setText(nodeContext.getLabel());				
				questionDef.setControlNode(element);
				questionDef.setLabelNode(nodeContext.getLabelNode());

				if(questionDef instanceof QuestionDef)
					setQuestionDataNode((QuestionDef)questionDef,formDef,parentQtn);
			}
			if(nodeContext.getLabelNode() != null){
				String ref = nodeContext.getLabelNode().getAttribute("ref");
				if(ref != null && !ref.isEmpty()){
					if(ref.contains("itext('")){
						String[] refTokens = ref.split("'");
						questionDef.setItextId(refTokens[1]); //refTokens should be in the form of [ "jr:itext(", "SOME_ID", ")" ] since we split on the ' char.
					}
				}
			}
		}
	}


	/**
	 * Sets the xforms instance data node child of a given question definition object.
	 * 
	 * @param qtn the question definition object.
	 * @param formDef the form to which the question belongs.
	 * @param parentQtn the parent question to which qtn belongs as a child.
	 * 					This is only non null for kids of repeat question types.
	 */
	private static void setQuestionDataNode(IFormElement qtn, FormDef formDef, IFormElement parentQtn){
//		String xpath = qtn.getQuestionID();
		String xpath = qtn.getDataNodesetPath();
		//xpath = new String(xpath.toCharArray(), 1, xpath.length()-1);
		int pos = xpath.lastIndexOf('@'); String attributeName = null;
		if(pos > 0){
			attributeName = xpath.substring(pos+1,xpath.length());
			xpath = xpath.substring(0,pos-1);
		}

		Element node = formDef.getDataNode();
		if(qtn.getControlNode().getParentNode().getNodeName().equals(XformConstants.NODE_NAME_REPEAT)){
			if(parentQtn != null)
				node = parentQtn.getDataNode();
		}

		if(node == null)
			return; //data node may not be present in the xforms document.
		
		String nodeName = "/" + node.getNodeName() + "/";
		if(xpath.contains(nodeName)){
			xpath = xpath.substring(xpath.indexOf(nodeName) + nodeName.length());
		}
		

		XPathExpression xpls = new XPathExpression(node, xpath);
		Vector result = xpls.getResult();

		for (Enumeration e = result.elements(); e.hasMoreElements();) {
			Object obj = e.nextElement();
			if (obj instanceof Element){
				if(pos > 0) //Check if we are to set attribute value.
					qtn.setDataNode(((Element) obj)); //((Element) obj).setAttribute(attributeName, value);
				else
					qtn.setDataNode(((Element) obj));//((Element) obj).addChild(Node.TEXT_NODE, value);

				break;
			}
		}
	}


//	/**
//	 * Checks if this is a repeat child question and adds it.
//	 * @param qtn the questions to check
//	 * @param repeats the list of repeat questions
//	 * @return true if so, else false.
//	 */
//	private static boolean addRepeatChildQtn(IFormElement qtn, Vector repeats,Element child,HashMap map,HashMap rptKidmap){
//		for(int i=0; i<repeats.size(); i++){
//			QuestionDef rptQtn = (QuestionDef)repeats.get(i);
//			if(qtn.getBinding().contains(rptQtn.getBinding())){
//				RepeatQtnsDef rptQtnsDef = rptQtn.getRepeatQtnsDef();
//				//rptQtnsDef.addQuestion(qtn); //TODO This is temporarily removed to solve the wiered problem list bug
//				String varname = qtn.getBinding().substring(rptQtn.getBinding().length()+1);
//				//varname = varname.substring(0, varname.indexOf('/'));
//				//map.put(child.getAttribute(ATTRIBUTE_NAME_ID), varname);
//				map.put(varname, varname);
//				rptKidmap.put(varname, qtn);
//				return true;		
//			}
//		}
//		return false;
//	}


	/**
	 * Adds a new question that uses a ref attribute instead of bind.
	 * 
	 * @param formDef the form definition object to which the question belongs.
	 * @param child the node being currently processed.
	 * @param relevants the map of constraint attribute values keyed by their 
	 * 					  question definition objects.
	 * @param ref the ref attribute value.
	 * @param bind the bind attribute value.
	 * @param constraints the map of constraint attribute values keyed by their 
	 * 					  question definition objects.
	 * @return the variable name of the new question.
	 */
	private static String addNonBindControl(FormDef formDef,Element child,HashMap relevants, String ref, String bind,HashMap constraints, IFormElement parentQtn){
		if(parentQtn == null){
			parentQtn = formDef;
		}
		QuestionDef qtn = new QuestionDef(null);
		qtn.setId(getNextQuestionId());

		if(child.getAttribute(XformConstants.ATTRIBUTE_NAME_TYPE) == null){
			if(XmlUtil.nodeNameEquals(child.getNodeName(),XformConstants.NODE_NAME_TRIGGER_MINUS_PREFIX)){
				qtn.setDataType(QuestionDef.QTN_TYPE_LABEL);
			}else{
				qtn.setDataType(QuestionDef.QTN_TYPE_TEXT);
			}
		}
		else{
			XformParserUtil.setQuestionType(qtn,child.getAttribute(XformConstants.ATTRIBUTE_NAME_TYPE),child);
		}

		if(child.getAttribute(XformConstants.ATTRIBUTE_NAME_REQUIRED) != null && child.getAttribute(XformConstants.ATTRIBUTE_NAME_REQUIRED).equals(XformConstants.XPATH_VALUE_TRUE)){
			if(child.getAttribute(XformConstants.ATTRIBUTE_NAME_ACTION) == null){
				qtn.setRequired(true);
			}
		}
		if(child.getAttribute(XformConstants.ATTRIBUTE_NAME_READONLY) != null && child.getAttribute(XformConstants.ATTRIBUTE_NAME_READONLY).equals(XformConstants.XPATH_VALUE_TRUE)){
			qtn.setEnabled(false);
		}
		if(child.getAttribute(XformConstants.ATTRIBUTE_NAME_LOCKED) != null && child.getAttribute(XformConstants.ATTRIBUTE_NAME_LOCKED).equals(XformConstants.XPATH_VALUE_TRUE)){
			qtn.setLocked(true);
		}

		String qtnID = ((ref != null) ? ref : bind);
		String[] tokens = qtnID.split("/");
		qtnID = tokens[tokens.length-1];
		qtn.setQuestionID(qtnID);

		if(parentQtn instanceof GroupDef){
			parentQtn.addChild(qtn);
		}else if(parentQtn.getDataType() == QuestionDef.QTN_TYPE_REPEAT){ 
			((QuestionDef)parentQtn).getRepeatQtnsDef().addChild(qtn);
		}else{
			formDef.addChild(qtn);
		}

		if(child.getAttribute(XformConstants.ATTRIBUTE_NAME_RELEVANT) != null){
			relevants.put(qtn,child.getAttribute(XformConstants.ATTRIBUTE_NAME_RELEVANT));
		}

		if(child.getAttribute(XformConstants.ATTRIBUTE_NAME_CONSTRAINT) != null){
			constraints.put(qtn,child.getAttribute(XformConstants.ATTRIBUTE_NAME_CONSTRAINT));
		}

		if(child.getAttribute(XformConstants.ATTRIBUTE_NAME_CALCULATE) != null){
			formDef.addCalculation(new Calculation(qtn.getId(),child.getAttribute(XformConstants.ATTRIBUTE_NAME_CALCULATE)));
		}

		return qtn.getQuestionID();
	}


	/**
	 * Parses the instance node of an xforms document.
	 * 
	 * @param formDef the form definition object that we are building.
	 * @param child the element that we are currently parsing.
	 */
	private static void parseInstanceElement(FormDef formDef, Element child){
		if(formDef.getDataNode() != null)
			return; //we only take the first instance node for formdef ref

		Element dataNode = null;
		for(int k=0; k<child.getChildNodes().getLength(); k++){
			if(child.getChildNodes().item(k).getNodeType() == Node.ELEMENT_NODE){
				dataNode = (Element)child.getChildNodes().item(k);
				formDef.setDataNode(dataNode);
			}
		}

		formDef.setQuestionID(XmlUtil.getNodeName(dataNode));
		if(dataNode.getAttribute(XformConstants.ATTRIBUTE_NAME_DESCRIPTION_TEMPLATE) != null)
			formDef.setDescriptionTemplate(dataNode.getAttribute(XformConstants.ATTRIBUTE_NAME_DESCRIPTION_TEMPLATE));

		if(dataNode.getAttribute(XformConstants.ATTRIBUTE_NAME_ID) != null){
			try{
				formDef.setId(Integer.parseInt(dataNode.getAttribute(XformConstants.ATTRIBUTE_NAME_ID)));
			}
			catch(Exception ex){/*We may have non numeric ids like for odk. We just ignore them.*/}
		}

		if(dataNode.getAttribute(XformConstants.ATTRIBUTE_NAME_NAME) != null)
			formDef.setName(dataNode.getAttribute(XformConstants.ATTRIBUTE_NAME_NAME));

		if(dataNode.getAttribute(XformConstants.ATTRIBUTE_NAME_FORM_KEY) != null)
			formDef.setFormKey(dataNode.getAttribute(XformConstants.ATTRIBUTE_NAME_FORM_KEY));
	}


	/**
	 * Parses a group element of an xforms document.
	 * 
	 * @param formDef the form definition object that we are building.
	 * @param element the element that we are currently parsing.
	 * @param id2VarNameMap a map of question bind ids to the variable names.
	 * @param questionDef the question definition object that is currently being parsed.
	 * @param relevants the map of constraint attribute values keyed by their 
	 * 					  question definition objects.
	 * @param repeatQtns a list of repeat question types.
	 * @param rptKidMap a map of question definition objects which are children of a repeat 
	 * 					question type, keyed by their variable names.
	 * @param currentPageNo the number of the current page we are parsing.
	 * @param parentQtn the parent of the question we are currently processing.
	 * @param constraints the map of constraint attribute values keyed by their 
	 * 					  question definition objects.
	 * @param orphanDynOptionQns a list of dynamic option definition questions who parent
	 *                           questions have not yet been parsed.
	 */
	private static void parseGroupElement(FormDef formDef, Element child, HashMap id2VarNameMap,IFormElement questionDef,HashMap relevants,Vector repeatQtns, HashMap rptKidMap, int currentPageNo, IFormElement parentQtn, HashMap constraints, List<QuestionDef> orphanDynOptionQns){
		//Check to see if we're dealing with a REPEAT instead of a regular group.
		NodeList childNodes = child.getChildNodes();
		for(int i=0;i<childNodes.getLength();i++){
			if(childNodes.item(i).getNodeType() != Node.ELEMENT_NODE){ continue; }
			Element grpChild = (Element)childNodes.item(i);
			if(grpChild.getNodeType() == Element.TEXT_NODE){ continue; }
			if(XmlUtil.nodeNameEquals(grpChild.getNodeName(), "repeat")){
				if(parentQtn == null){ parentQtn = formDef; }
				parseRepeatElement(formDef, child, id2VarNameMap, questionDef, relevants, repeatQtns, rptKidMap, currentPageNo, parentQtn, constraints, orphanDynOptionQns,i);
//				parseRepeatElement(formDef,parentQtn,questionDef,child,i);

				return; //short circuit regular group parsing.
			}
		}
		
		String variableName = XformParserUtil.getQuestionIDFromRefOrNodeset(child, formDef);
		GroupDef groupDef = new GroupDef();
		QuestionDef groupBind = (QuestionDef)formDef.getElement(variableName);
		if(groupBind != null){
			groupDef.setQuestionID(groupBind.getQuestionID());
			groupDef.setBindNode(groupBind.getBindNode());
			groupDef.setParent(groupBind.getParent());
			groupDef.setId(groupBind.getId());
			groupDef.setRequired(groupBind.isRequired());
//			groupDef.setVisible(groupBind.isVisible());
			groupDef.setEnabled(groupBind.isEnabled());
			formDef.removeChild(groupBind);
			
		}
		
		groupDef.setControlNode(child);
		groupDef.setQuestionID(variableName);

		if(parentQtn == null){
			formDef.addChild(groupDef);
		}else{
			parentQtn.addChild(groupDef);
		}

		questionDef = groupDef;
		parentQtn = questionDef;
		
		parseElement(formDef, child,id2VarNameMap,questionDef,relevants,repeatQtns,rptKidMap,currentPageNo,parentQtn,constraints,orphanDynOptionQns);
	}
	
	private static void parseRepeatElement(FormDef formDef, Element child, HashMap id2VarNameMap,IFormElement questionDef,HashMap relevants,Vector repeatQtns, HashMap rptKidMap, int currentPageNo, IFormElement parentQtn, HashMap constraints, List<QuestionDef> orphanDynOptionQns, int repeatElementIndex){
		String nodeset, jrCount;
		QuestionDef repeat = null;
		Element repeatNode = ((Element)child.getChildNodes().item(repeatElementIndex));
		nodeset = repeatNode.getAttribute("nodeset");
		jrCount = getJRCountAttributeValue(repeatNode);
		boolean hasLabelNode = ((Element)child).getElementsByTagName("label").getLength() > 0;
		Element labelNode = (Element)child.getElementsByTagName("label").item(0);
		
		boolean hasHintNode = ((Element)child).getElementsByTagName("hint").getLength() > 0;
		Element hintNode;
		
		String[] idTokens = nodeset.split("/");
		String id = idTokens[idTokens.length-1];
		IFormElement elementDef = formDef.getElement(id);
		if(id != null && elementDef != null){
			repeat = (QuestionDef)elementDef;
			if(parentQtn != null){
				parentQtn.getChildren().remove(repeat);
			}else{
				formDef.getChildren().remove(repeat);
			}
//			formDef.removeChild(elementDef);
		}
		
		if(repeat == null){
			repeat = new QuestionDef(parentQtn);
		}
		repeat.setQuestionID(id);
		repeat.setParent(parentQtn);
		repeat.setDataType(QuestionDef.QTN_TYPE_REPEAT);
		repeat.setHasUINode(true);

		if(hasLabelNode){
			repeat.setText(XmlUtil.getTextValue(labelNode));
			repeat.setLabelNode(labelNode);
			repeat.setItextId(XmlUtil.getItextId(labelNode));
		}
		
		if(hasHintNode){
			hintNode = (Element)child.getElementsByTagName("hint").item(0);
			repeat.setHintNode(hintNode);
			repeat.setHelpText(XmlUtil.getTextValue(hintNode));
		}
		
		
		if(jrCount != null && !jrCount.isEmpty()){
			repeat.setRepeatCountNodePath(jrCount);
		}
		repeat.setControlNode(((Element)child.getChildNodes().item(repeatElementIndex)));
		setQuestionDataNode((QuestionDef)repeat,formDef,parentQtn);
		RepeatQtnsDef repeatQtnsDef = new RepeatQtnsDef(repeat); 
		repeat.setRepeatQtnsDef(repeatQtnsDef);
		parentQtn.addChild(repeat);
		questionDef = repeat;
		parentQtn = repeatQtnsDef;

		parseElement(formDef, repeatNode,id2VarNameMap,questionDef,relevants,repeatQtns,rptKidMap,currentPageNo,parentQtn,constraints,orphanDynOptionQns);
	}
	
	/**
	 * Convenience function, gets the jr:repeatCount attribute value from the node and returns it
	 * @param repeatNode
	 * @return
	 */
	private static String getJRCountAttributeValue(Element repeatNode){
		return (repeatNode.getAttribute("jr:count") != null ? repeatNode.getAttribute("jr:count") : repeatNode.getAttribute("count"));
	}


	/**
	 * Parses a bind element of an xforms document.
	 * 
	 * @param formDef the form definition object that we are building.
	 * @param element the element that we are currently parsing.
	 * @param id2VarNameMap a map of question bind ids to the variable names.
	 * @param questionDef the question definition object that is currently being parsed.
	 * @param relevants the map of constraint attribute values keyed by their 
	 * 					  question definition objects.
	 * @param repeatQtns a list of repeat question types.
	 * @param rptKidMap a map of question definition objects which are children of a repeat 
	 * 					question type, keyed by their variable names.
	 * @param currentPageNo the number of the current page we are parsing.
	 * @param parentQtn the parent of the question we are currently processing.
	 * @param constraints the map of constraint attribute values keyed by their 
	 * 					  question definition objects.
	 * @param orphanDynOptionQns a list of dynamic option definition questions who parent
	 *                           questions have not yet been parsed.
	 * @return the question we are currently parsing.
	 */
	private static IFormElement parseBindElement(FormDef formDef, Element child, HashMap id2VarNameMap,IFormElement questionDef,HashMap relevants,Vector repeatQtns, HashMap rptKidMap, int currentPageNo, IFormElement parentQtn, HashMap constraints, List<QuestionDef> orphanDynOptionQns){
		IFormElement def = new QuestionDef(null);
		def.setBindNode(child);
		def.setId(getNextQuestionId());
		def.setQuestionID(FormUtil.getQtnIDFromElement(child));

		if(child.getAttribute(XformConstants.ATTRIBUTE_NAME_TYPE)!= null){
			XformParserUtil.setQuestionType((QuestionDef)def,child.getAttribute(XformConstants.ATTRIBUTE_NAME_TYPE),child);
		}
		
		if(child.getAttribute(XformConstants.ATTRIBUTE_NAME_REQUIRED) != null && child.getAttribute(XformConstants.ATTRIBUTE_NAME_REQUIRED).equals(XformConstants.XPATH_VALUE_TRUE)){
			def.setRequired(true);
		}
		
		if(child.getAttribute(XformConstants.ATTRIBUTE_NAME_READONLY) != null && child.getAttribute(XformConstants.ATTRIBUTE_NAME_READONLY).equals(XformConstants.XPATH_VALUE_TRUE)){
			def.setEnabled(false);
		}
		
		if(child.getAttribute(XformConstants.ATTRIBUTE_NAME_LOCKED) != null && child.getAttribute(XformConstants.ATTRIBUTE_NAME_LOCKED).equals(XformConstants.XPATH_VALUE_TRUE)){
			def.setLocked(true);
		}
		

		if(child.getAttribute(XformConstants.ATTRIBUTE_NAME_RELEVANT) != null){
			relevants.put(def,child.getAttribute(XformConstants.ATTRIBUTE_NAME_RELEVANT));
		}

		if(child.getAttribute(XformConstants.ATTRIBUTE_NAME_CONSTRAINT) != null){
			constraints.put(def,child.getAttribute(XformConstants.ATTRIBUTE_NAME_CONSTRAINT));
		}

		if(child.getAttribute(XformConstants.ATTRIBUTE_NAME_CALCULATE) != null){
			formDef.addCalculation(new Calculation(def.getId(),child.getAttribute(XformConstants.ATTRIBUTE_NAME_CALCULATE)));
		}
		
		child.removeAttribute("id");
		
		if(child.getAttribute("type") != null && child.getAttribute("type").isEmpty()){
			child.removeAttribute("type");
		}
		
		formDef.addChild(def);
		return def;
	}


	/**
	 * Parses a UI element of an xforms document.
	 * 
	 * @param formDef the form definition object that we are building.
	 * @param element the element that we are currently parsing.
	 * @param id2VarNameMap a map of question bind ids to the variable names.
	 * @param questionDef the question definition object that is currently being parsed.
	 * @param relevants the map of constraint attribute values keyed by their 
	 * 					  question definition objects.
	 * @param repeatQtns a list of repeat question types.
	 * @param rptKidMap a map of question definition objects which are children of a repeat 
	 * 					question type, keyed by their variable names.
	 * @param currentPageNo the number of the current page we are parsing.
	 * @param parentQtn the parent of the question we are currently processing.
	 * @param constraints the map of constraint attribute values keyed by their 
	 * 					  question definition objects.
	 * @param orphanDynOptionQns a list of dynamic option definition questions who parent
	 *                           questions have not yet been parsed.
	 * @param nodeContext the current node context.
	 * @return the question we are currently parsing.
	 */
	private static IFormElement parseUiElement(FormDef formDef, Element child, HashMap id2VarNameMap,IFormElement questionDef,
												HashMap relevants,Vector repeatQtns, HashMap rptKidMap, int currentPageNo, 
												IFormElement parentQtn, HashMap constraints, List<QuestionDef> orphanDynOptionQns, 
												NodeContext nodeContext){
		
		String ref = child.getAttribute(XformConstants.ATTRIBUTE_NAME_REF);
		if(ref != null && ref.contains("jr:itext")){
			ref = ref.replace("jr:itext('",""); //
			ref = ref.replace("')", "");        //remove incorrect jr:itext('ID') string
			ref = ref.replace(" ", "");//remove spaces (are not allowed)
		}
		String bind = child.getAttribute(XformConstants.ATTRIBUTE_NAME_BIND);
		if(ref == null && bind == null){
			ref = child.getAttribute(XformConstants.ATTRIBUTE_NAME_NODESET);
		}
		
		//this appears to assume always relative nodeset paths (in Control nodes)...
//		String varName = (String)id2VarNameMap.get(((ref != null) ? ref : bind));
		String varName = FormUtil.getQtnIDFromNodeSetPath(ref != null ? ref : bind);

		String tagname = child.getNodeName();

		//new addition may cause bugs
		if(varName == null){

			if(ref != null && ref.startsWith("/"+formDef.getQuestionID()+"/")){
				String[] tokens = ref.split("/");
				varName = tokens[tokens.length-1];
			}

			if(formDef.getElement(varName) == null){
				varName = addNonBindControl(formDef,child,relevants,ref,bind,constraints,parentQtn);
			}
			if(ref != null){
				id2VarNameMap.put(ref, ref);
			}
		}

		if(varName != null){
			IFormElement qtn = formDef.getElement(varName);
			if(qtn == null){  //what about questions without binds?
				qtn = (QuestionDef)rptKidMap.get(varName);
			}
			if(qtn == null){
				varName = addNonBindControl(formDef, child, relevants, varName, bind, constraints, parentQtn);
				qtn = formDef.getElement(varName);
			}
			if(XmlUtil.nodeNameEquals(tagname,XformConstants.NODE_NAME_SELECT1_MINUS_PREFIX) || XmlUtil.nodeNameEquals(tagname,XformConstants.NODE_NAME_SELECT_MINUS_PREFIX)){
				qtn.setDataType((XmlUtil.nodeNameEquals(tagname,XformConstants.NODE_NAME_SELECT1_MINUS_PREFIX)) ? QuestionDef.QTN_TYPE_LIST_EXCLUSIVE : QuestionDef.QTN_TYPE_LIST_MULTIPLE);
				((QuestionDef)qtn).setOptions(new Vector());
			}else if(XmlUtil.nodeNameEquals(tagname,XformConstants.NODE_NAME_REPEAT_MINUS_PREFIX) && !nodeContext.getLabel().equals("")){
				questionDef.setDataType(QuestionDef.QTN_TYPE_REPEAT);
				questionDef.setText(nodeContext.getLabel());
				questionDef.setHelpText(nodeContext.getHint());

				questionDef.setQuestionID(qtn.getQuestionID());
				questionDef.setBindNode(qtn.getBindNode());

				nodeContext.setLabel("");
				nodeContext.setHint("");

				questionDef.setLabelNode(nodeContext.getLabelNode());
				questionDef.setHintNode(nodeContext.getHintNode());

				questionDef.setControlNode(child);
				setQuestionDataNode(questionDef,formDef,parentQtn);
				parentQtn = questionDef;

				formDef.getChildren().remove(qtn);
				formDef.getChildren().remove(qtn);
			}
			else if(XmlUtil.nodeNameEquals(tagname,XformConstants.NODE_NAME_UPLOAD_MINUS_PREFIX)){
				if("image/*".equalsIgnoreCase(child.getAttribute(XformConstants.ATTRIBUTE_NAME_MEDIATYPE))){
					qtn.setDataType(QuestionDef.QTN_TYPE_IMAGE);
				}else if("audio/*".equalsIgnoreCase(child.getAttribute(XformConstants.ATTRIBUTE_NAME_MEDIATYPE))){
					qtn.setDataType(QuestionDef.QTN_TYPE_AUDIO);
				}else if("video/*".equalsIgnoreCase(child.getAttribute(XformConstants.ATTRIBUTE_NAME_MEDIATYPE))){
					qtn.setDataType(QuestionDef.QTN_TYPE_VIDEO);
				}
			}

			Element parent = (Element)child.getParentNode(); 
			if(XmlUtil.nodeNameEquals(parent.getNodeName(),XformConstants.NODE_NAME_REPEAT_MINUS_PREFIX)){
				varName = (String)id2VarNameMap.get(parent.getAttribute(XformConstants.ATTRIBUTE_NAME_BIND) != null ? parent.getAttribute(XformConstants.ATTRIBUTE_NAME_BIND) : parent.getAttribute(XformConstants.ATTRIBUTE_NAME_NODESET));
				IFormElement rptQtnDef = formDef.getElement(varName);
				qtn.setId(getNextQuestionId());


//				qtn.setBindNode(child);
				qtn.setControlNode(child);

				//Remove repeat question constraint if any
				XformParserUtil.replaceConstraintQtn(constraints,(QuestionDef)qtn);
			}

			if(XmlUtil.nodeNameEquals(child.getNodeName(),XformConstants.NODE_NAME_TRIGGER_MINUS_PREFIX)){
				qtn.setDataType(QuestionDef.QTN_TYPE_LABEL);
			}
			questionDef = qtn;
			parseElement(formDef, child, id2VarNameMap,questionDef,relevants,repeatQtns,rptKidMap,currentPageNo,parentQtn,constraints,orphanDynOptionQns);
		}
		
		//For the children being oredered in the correct way (by appearance of the Control node in the xml doc).
		
		IFormElement parent = questionDef.getParent();
		if(parent instanceof FormDef || parent instanceof GroupDef){
			int numChildren = parent.getChildCount();
			try{
				parent.moveChildToIndex(questionDef, numChildren-1); //moves item to end of list.
			}catch(Exception e){
				FormUtil.displayException(e);
			}
		}
		
		return questionDef;
	}


	/**
	 * Parses a label element of an xforms document.
	 * 
	 * @param formDef the form definition object that we are building.
	 * @param child the element that we are currently parsing.
	 * @param questionDef the question definition object that is currently being parsed.
	 * @param nodeContext the current node context.
	 */
	private static void parseLabelElement(FormDef formDef, Element child, IFormElement questionDef, NodeContext nodeContext){
		String parentName = ((Element)child.getParentNode()).getNodeName();
		if(XmlUtil.nodeNameEquals(parentName,XformConstants.NODE_NAME_INPUT_MINUS_PREFIX) || XmlUtil.nodeNameEquals(parentName,XformConstants.NODE_NAME_SELECT_MINUS_PREFIX) ||
				XmlUtil.nodeNameEquals(parentName,XformConstants.NODE_NAME_SELECT1_MINUS_PREFIX) || XmlUtil.nodeNameEquals(parentName,XformConstants.NODE_NAME_ITEM_MINUS_PREFIX) ||
				XmlUtil.nodeNameEquals(parentName,XformConstants.NODE_NAME_UPLOAD_MINUS_PREFIX) || XmlUtil.nodeNameEquals(parentName,XformConstants.NODE_NAME_TRIGGER_MINUS_PREFIX)){
				String label = XmlUtil.getTextValue(child);
				if(label == null) label = "";
				nodeContext.setLabel(label);
				nodeContext.setLabelNode(child);
			
		}
		else if(XmlUtil.nodeNameEquals(parentName,XformConstants.NODE_NAME_REPEAT_MINUS_PREFIX)){
			if(questionDef != null){
				questionDef.setText(XmlUtil.getTextValue(child));
				if(getText(child)!=null){
					questionDef.setItextId(getText(child));
				}
			}
		}
		else if(XmlUtil.nodeNameEquals(parentName,XformConstants.NODE_NAME_GROUP_MINUS_PREFIX)){
				String label = XmlUtil.getTextValue(child);
				if(label == null) label = "";
				nodeContext.setLabel(label);
				nodeContext.setLabelNode(child);
		}
		
		String ref = child.getAttribute("ref");
		if(ref != null && !ref.isEmpty()){
			if(ref.contains("itext('")){
				String[] refTokens = ref.split("'");
				questionDef.setItextId(refTokens[1]); //refTokens should be in the form of [ "jr:itext(", "SOME_ID", ")" ] since we split on the ' char.
			}
		}
	}


	/**
	 * Parses a hint element of an xforms document.
	 * 
	 * @param formDef the form definition object that we are building.
	 * @param child the element that we are currently parsing.
	 * @param questionDef the question definition object that is currently being parsed.
	 * @param nodeContext the current node context.
	 */
	private static void parseHintElement(FormDef formDef, Element child, IFormElement questionDef, NodeContext nodeContext){
		String parentName = ((Element)child.getParentNode()).getNodeName();
		//if(parentName.equalsIgnoreCase(NODE_NAME_GROUP)||parentName.equalsIgnoreCase(NODE_NAME_GROUP_MINUS_PREFIX)){
		if(XmlUtil.nodeNameEquals(parentName,XformConstants.NODE_NAME_GROUP_MINUS_PREFIX)){
			if(true /*child.getChildNodes().getLength() != 0*/){
				nodeContext.setHint(getText(child));
				nodeContext.setHintNode(child);
			}
		}
		else if(questionDef != null){
			questionDef.setHelpText(getText(child));
			questionDef.setHintNode(child /*element*/);
		}
	}

	private static String getText(Element node){
		if(node.getChildNodes().getLength() != 0){
			String text = node.getChildNodes().item(0).getNodeValue().trim();
			if(text.length() > 0)
				return text;
		}
		return null;
	}
}
