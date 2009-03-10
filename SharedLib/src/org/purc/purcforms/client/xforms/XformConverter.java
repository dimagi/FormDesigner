package org.purc.purcforms.client.xforms;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.purc.purcforms.client.model.Condition;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.OptionDef;
import org.purc.purcforms.client.model.PageDef;
import org.purc.purcforms.client.model.PurcConstants;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.model.RepeatQtnsDef;
import org.purc.purcforms.client.model.SkipRule;
import org.purc.purcforms.client.xpath.XPathExpression;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NamedNodeMap;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.XMLParser;


/**
 * Provides conversion from xforms to the object model and vice vasa.
 * 
 * @author Daniel Kayiwa
 *
 */
public class XformConverter implements Serializable{

	private static final String XML_NAMESPACE_PREFIX = "xmlns:";

	/** Namespace prefix for XForms. */
	private static final String PREFIX_XFORMS = "xf";

	private static final String PREFIX_XFORMS_AND_COLON = "xf:";

	/** Namespace prefix for XML schema. */
	private static final String PREFIX_XML_SCHEMA = "xsd";

	/** The second Namespace prefix for XML schema. */
	private static final String PREFIX_XML_SCHEMA2 = "xs";

	/** Namespace prefix for XML schema instance. */
	private static final String PREFIX_XML_INSTANCES = "xsi";

	/** Namespace for XForms. */
	private static final String NAMESPACE_XFORMS = "http://www.w3.org/2002/xforms";

	/** Namespace for XML schema. */
	private static final String NAMESPACE_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

	/** Namespace for XML schema instance. */
	private static final String NAMESPACE_XML_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";


	public static final String NODE_NAME_XFORMS = PREFIX_XFORMS_AND_COLON+"xforms";
	public static final String NODE_NAME_INSTANCE = PREFIX_XFORMS_AND_COLON+"instance";
	public static final String NODE_NAME_MODEL = PREFIX_XFORMS_AND_COLON+"model";
	public static final String NODE_NAME_BIND = PREFIX_XFORMS_AND_COLON+"bind";
	public static final String NODE_NAME_LABEL = PREFIX_XFORMS_AND_COLON+"label";
	public static final String NODE_NAME_HINT = PREFIX_XFORMS_AND_COLON+"hint";
	public static final String NODE_NAME_ITEM = PREFIX_XFORMS_AND_COLON+"item";
	public static final String NODE_NAME_INPUT = PREFIX_XFORMS_AND_COLON+"input";
	public static final String NODE_NAME_SELECT = PREFIX_XFORMS_AND_COLON+"select";
	public static final String NODE_NAME_SELECT1 = PREFIX_XFORMS_AND_COLON+"select1";
	public static final String NODE_NAME_REPEAT = PREFIX_XFORMS_AND_COLON+"repeat";
	public static final String NODE_NAME_TRIGGER = PREFIX_XFORMS_AND_COLON+"trigger";
	public static final String NODE_NAME_SUBMIT = PREFIX_XFORMS_AND_COLON+"submit";
	public static final String NODE_NAME_VALUE = PREFIX_XFORMS_AND_COLON+"value";
	public static final String NODE_NAME_GROUP = PREFIX_XFORMS_AND_COLON+"group";

	public static final String NODE_NAME_XFORMS_MINUS_PREFIX = "xforms";
	public static final String NODE_NAME_INSTANCE_MINUS_PREFIX = "instance";
	public static final String NODE_NAME_MODEL_MINUS_PREFIX = "model";
	public static final String NODE_NAME_BIND_MINUS_PREFIX = "bind";
	public static final String NODE_NAME_LABEL_MINUS_PREFIX = "label";
	public static final String NODE_NAME_HINT_MINUS_PREFIX = "hint";
	public static final String NODE_NAME_ITEM_MINUS_PREFIX = "item";
	public static final String NODE_NAME_INPUT_MINUS_PREFIX = "input";
	public static final String NODE_NAME_SELECT_MINUS_PREFIX = "select";
	public static final String NODE_NAME_SELECT1_MINUS_PREFIX = "select1";
	public static final String NODE_NAME_REPEAT_MINUS_PREFIX = "repeat";
	public static final String NODE_NAME_TRIGGER_MINUS_PREFIX = "trigger";
	public static final String NODE_NAME_SUBMIT_MINUS_PREFIX = "submit";
	public static final String NODE_NAME_VALUE_MINUS_PREFIX = "value";
	public static final String NODE_NAME_GROUP_MINUS_PREFIX = "group";

	public static final String ATTRIBUTE_NAME_ID = "id";
	public static final String ATTRIBUTE_NAME_BIND = "bind";
	public static final String ATTRIBUTE_NAME_REF = "ref";
	public static final String ATTRIBUTE_NAME_NODESET = "nodeset";
	public static final String ATTRIBUTE_NAME_LOCKED = "locked";
	public static final String ATTRIBUTE_NAME_READONLY = "readonly";
	public static final String ATTRIBUTE_NAME_RELEVANT = "relevant";
	public static final String ATTRIBUTE_NAME_REQUIRED = "required";
	public static final String ATTRIBUTE_NAME_TYPE = "type";
	public static final String ATTRIBUTE_NAME_NAME = "name";
	public static final String ATTRIBUTE_NAME_XMLNS = "xmlns:"+PREFIX_XFORMS;
	public static final String ATTRIBUTE_NAME_DESCRIPTION_TEMPLATE = "description-template"; //eg ${/patient/family_name}$
	public static final String ATTRIBUTE_NAME_ACTION = "action";

	public static final String ATTRIBUTE_VALUE_ENABLE = "enable";
	public static final String ATTRIBUTE_VALUE_DISABLE = "disable";
	public static final String ATTRIBUTE_VALUE_SHOW = "show";
	public static final String ATTRIBUTE_VALUE_HIDE = "hide";

	private static final String DATA_TYPE_DATE = "xsd:date";
	private static final String DATA_TYPE_INT = "xsd:int";
	private static final String DATA_TYPE_TEXT = "xsd:string";
	private static final String DATA_TYPE_BOOLEAN = "xsd:boolean";
	private static final String DATA_TYPE_BINARY = "xsd:base64Binary";

	public static final String XPATH_VALUE_TRUE = "true()";
	public static final String XPATH_VALUE_FALSE = "false()";

	private static final String CONDITIONS_OPERATOR_TEXT_AND = " AND ";
	private static final String CONDITIONS_OPERATOR_TEXT_OR = " OR ";


	public XformConverter(){

	}

	public static String getXmlType(int type, Element node){
		if(type == QuestionDef.QTN_TYPE_VIDEO)
			node.setAttribute("format", "video");
		else if(type == QuestionDef.QTN_TYPE_AUDIO)
			node.setAttribute("format", "audio");
		else if(type == QuestionDef.QTN_TYPE_IMAGE)
			node.setAttribute("format", "image");

		switch(type){
		case QuestionDef.QTN_TYPE_BOOLEAN:
			return DATA_TYPE_BOOLEAN;
		case QuestionDef.QTN_TYPE_DATE:
			return DATA_TYPE_DATE;
		case QuestionDef.QTN_TYPE_DATE_TIME:
			return "xsd:dateTime";
		case QuestionDef.QTN_TYPE_TIME:
			return "xsd:time";
		case QuestionDef.QTN_TYPE_DECIMAL:
			return "xsd:decimal";
		case QuestionDef.QTN_TYPE_NUMERIC:
			return DATA_TYPE_INT;
		case QuestionDef.QTN_TYPE_TEXT:
		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
		case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
			return DATA_TYPE_TEXT;
		case QuestionDef.QTN_TYPE_IMAGE:
		case QuestionDef.QTN_TYPE_VIDEO:
		case QuestionDef.QTN_TYPE_AUDIO:
			return DATA_TYPE_BINARY;
		}

		return "";
	}

	public static String fromFormDef2Xform(FormDef formDef){
		Document doc = XMLParser.createDocument();
		formDef.setDoc(doc);

		Element xformsNode = doc.createElement(NODE_NAME_XFORMS);
		formDef.setXformsNode(xformsNode);

		//xformsNode.setAttribute("xmlns", "http://www.w3.org/2002/xforms");
		xformsNode.setAttribute(XML_NAMESPACE_PREFIX+PREFIX_XFORMS, NAMESPACE_XFORMS);
		xformsNode.setAttribute(XML_NAMESPACE_PREFIX+PREFIX_XML_SCHEMA, NAMESPACE_XML_SCHEMA);
		doc.appendChild(xformsNode);

		Element modelNode =  doc.createElement(NODE_NAME_MODEL);
		xformsNode.appendChild(modelNode);

		Element instanceNode =  doc.createElement(NODE_NAME_INSTANCE);
		modelNode.appendChild(instanceNode);
		formDef.setModelNode(modelNode);

		Element formNode =  doc.createElement(formDef.getVariableName());
		formNode.setAttribute(ATTRIBUTE_NAME_NAME, formDef.getName());
		instanceNode.appendChild(formNode);
		formDef.setDataNode(formNode);

		for(int pageNo=0; pageNo<formDef.getPages().size(); pageNo++){
			PageDef pageDef = (PageDef)formDef.getPages().elementAt(pageNo);
			fromPageDef2Xform(pageDef,doc,xformsNode,formDef,formNode,modelNode);
		}

		Vector rules = formDef.getSkipRules();
		if(rules != null){
			for(int i=0; i<rules.size(); i++)
				fromSkipRule2Xform((SkipRule)rules.elementAt(i),formDef);
		}

		return fromDoc2String(doc);
	}

	public static void fromSkipRule2Xform(SkipRule rule, FormDef formDef){
		String relevant = "";
		Vector conditions  = rule.getConditions();
		for(int i=0; i<conditions.size(); i++){
			if(relevant.length() > 0)
				relevant += getConditionsOperatorText(rule.getConditionsOperator());
			relevant += fromSkipCondition2Xform((Condition)conditions.elementAt(i),formDef,rule.getAction());
		}

		Vector actionTargets =  rule.getActionTargets();
		for(int i=0; i<actionTargets.size(); i++){
			int id = ((Integer)actionTargets.elementAt(i)).intValue();
			QuestionDef questionDef = formDef.getQuestion(id);
			if(questionDef == null)
				continue;

			Element node = questionDef.getBindNode();
			if(node == null)
				node = questionDef.getControlNode();
			node.setAttribute(XformConverter.ATTRIBUTE_NAME_RELEVANT, relevant);

			String value = ATTRIBUTE_VALUE_ENABLE;
			if((rule.getAction() & PurcConstants.ACTION_ENABLE) != 0)
				value = ATTRIBUTE_VALUE_ENABLE;
			else if((rule.getAction() & PurcConstants.ACTION_DISABLE) != 0)
				value = ATTRIBUTE_VALUE_DISABLE;
			else if((rule.getAction() & PurcConstants.ACTION_SHOW) != 0)
				value = ATTRIBUTE_VALUE_SHOW;
			else if((rule.getAction() & PurcConstants.ACTION_HIDE) != 0)
				value = ATTRIBUTE_VALUE_HIDE;
			node.setAttribute(XformConverter.ATTRIBUTE_NAME_ACTION, value);

			if((rule.getAction() & PurcConstants.ACTION_MAKE_MANDATORY) != 0)
				value = XPATH_VALUE_TRUE;
			else if((rule.getAction() & PurcConstants.ACTION_MAKE_OPTIONAL) != 0)
				value = XPATH_VALUE_FALSE;
			node.setAttribute(XformConverter.ATTRIBUTE_NAME_REQUIRED, value);
		}
	}

	private static String getConditionsOperatorText(int operator){
		String operatorText = null;
		if(operator == PurcConstants.CONDITIONS_OPERATOR_AND)
			operatorText = CONDITIONS_OPERATOR_TEXT_AND;
		else if(operator == PurcConstants.CONDITIONS_OPERATOR_OR)
			operatorText = CONDITIONS_OPERATOR_TEXT_OR;

		return /*" " +*/ operatorText /*+ " "*/;
	}

	private static String fromSkipCondition2Xform(Condition condition, FormDef formDef, int action){
		String relevant = null;

		QuestionDef questionDef = formDef.getQuestion(condition.getQuestionId());
		if(questionDef != null){
			relevant = questionDef.getVariableName();
			if(!relevant.contains(formDef.getVariableName()))
				relevant = "/" + formDef.getVariableName() + "/" + questionDef.getVariableName();
			relevant += " " + getOperator(condition.getOperator(),action)+" '" + condition.getValue() + "'";
		}
		return relevant;
	}

	public static void fromPageDef2Xform(PageDef pageDef, Document doc, Element xformsNode, FormDef formDef, Element formNode, Element modelNode){
		Element groupNode =  doc.createElement(NODE_NAME_GROUP);
		Element labelNode =  doc.createElement(NODE_NAME_LABEL);
		labelNode.appendChild(doc.createTextNode(pageDef.getName()));
		groupNode.appendChild(labelNode);
		xformsNode.appendChild(groupNode);
		//formDef.setPageLabelNode(labelNode);
		//formDef.setPageGroupNode(groupNode);
		pageDef.setLabelNode(labelNode);
		pageDef.setGroupNode(groupNode);

		Vector questions = pageDef.getQuestions();
		for(int i=0; i<questions.size(); i++){
			QuestionDef qtn = (QuestionDef)questions.elementAt(i);
			fromQuestionDef2Xform(qtn,doc,xformsNode,formDef,formNode,modelNode,groupNode);
		}
	}

	private static Element fromVariableName2NodeName(Document doc, String variableName,FormDef formDef,Element formNode){
		String name = variableName;
		//TODO May need to be smarter than this. Avoid invalid node
		//names. eg those having slashes (form1/question1)
		if(name.startsWith(formDef.getVariableName()))
			name = name.substring(formDef.getVariableName().length()+1);

		//TODO Should do this for all invalid characters in node names.
		/*name = name.replace("/", "");
		name = name.replace("\\", "");*/
		name = name.replace(" ", "");

		Vector<Element> nodes = new Vector<Element>();
		int prevPos = 0;
		int pos = name.indexOf('/');
		String s;
		while(pos > 0){
			s = name.substring(prevPos, pos);
			nodes.add(doc.createElement(s));

			prevPos = ++pos;
			pos = name.indexOf('/', pos);
		}

		if(nodes.size() > 0 && prevPos < name.length()){
			s = name.substring(prevPos);
			nodes.add(doc.createElement(s));
		}

		Element dataNode = null;
		if(nodes.size() == 0){
			dataNode = doc.createElement(name);
			formNode.appendChild(dataNode);
		}
		else{
			for(int i=0; i<nodes.size(); i++){
				if(i==0){
					dataNode = nodes.elementAt(i);
					formNode.appendChild(dataNode);
				}
				else
					dataNode.appendChild(nodes.elementAt(i));
			}
			dataNode = nodes.elementAt(nodes.size()-1);
		}

		return dataNode;
	}

	public static void fromQuestionDef2Xform(QuestionDef qtn, Document doc, Element xformsNode, FormDef formDef, Element formNode, Element modelNode,Element groupNode){
		Element dataNode =  fromVariableName2NodeName(doc,qtn.getVariableName(),formDef,formNode);
		if(qtn.getDefaultValue() != null && qtn.getDefaultValue().trim().length() > 0)
			dataNode.appendChild(doc.createTextNode(qtn.getDefaultValue()));
		qtn.setDataNode(dataNode);

		Element bindNode =  doc.createElement(NODE_NAME_BIND);
		String id = qtn.getVariableName();
		if(id.contains("/"))
			id = id.substring(id.lastIndexOf('/')+1);
		bindNode.setAttribute(ATTRIBUTE_NAME_ID, id);

		String nodeset = qtn.getVariableName();
		if(!nodeset.startsWith("/"))
			nodeset = "/" + nodeset;
		if(!nodeset.startsWith("/" + formDef.getVariableName() + "/"))
			nodeset = "/" + formDef.getVariableName() + "/" + qtn.getVariableName();
		bindNode.setAttribute(ATTRIBUTE_NAME_NODESET, nodeset);

		if(qtn.getDataType() != QuestionDef.QTN_TYPE_REPEAT)
			bindNode.setAttribute(ATTRIBUTE_NAME_TYPE, getXmlType(qtn.getDataType(),bindNode));	
		if(qtn.isRequired())
			bindNode.setAttribute(ATTRIBUTE_NAME_REQUIRED, XPATH_VALUE_TRUE);
		if(!qtn.isEnabled())
			bindNode.setAttribute(ATTRIBUTE_NAME_READONLY, XPATH_VALUE_TRUE);
		if(qtn.isLocked())
			bindNode.setAttribute(ATTRIBUTE_NAME_LOCKED, XPATH_VALUE_TRUE);

		String bindAttributeName = ATTRIBUTE_NAME_REF;
		if(!groupNode.getNodeName().equals(NODE_NAME_REPEAT)){
			modelNode.appendChild(bindNode);
			qtn.setBindNode(bindNode);
			bindAttributeName = ATTRIBUTE_NAME_BIND;
		}	

		Element inputNode =  getXformInputElementName(doc,qtn,bindAttributeName);
		if(groupNode != null) //Some forms may not be in groups
			groupNode.appendChild(inputNode);
		else
			xformsNode.appendChild(inputNode);

		qtn.setControlNode(inputNode);

		Element labelNode =  doc.createElement(NODE_NAME_LABEL);
		labelNode.appendChild(doc.createTextNode(qtn.getText()));
		inputNode.appendChild(labelNode);
		qtn.setLabelNode(labelNode);

		addHelpTextNode(qtn,doc,inputNode,null);

		if(qtn.getDataType() != QuestionDef.QTN_TYPE_REPEAT){
			Vector options = qtn.getOptions();
			if(options != null && options.size() > 0){
				for(int j=0; j<options.size(); j++){
					OptionDef optionDef = (OptionDef)options.elementAt(j);
					Element itemNode = fromOptionDef2Xform(optionDef,doc,inputNode);	
					if(j == 0)
						qtn.setFirstOptionNode(itemNode);
				}
			}
		}
		else{
			Element repeatNode =  doc.createElement(NODE_NAME_REPEAT);
			repeatNode.setAttribute(ATTRIBUTE_NAME_BIND, id);
			inputNode.appendChild(repeatNode);
			qtn.setControlNode(repeatNode);

			RepeatQtnsDef rptQtns = qtn.getRepeatQtnsDef();
			for(int j=0; j<rptQtns.size(); j++)
				createQuestion(rptQtns.getQuestionAt(j),repeatNode,dataNode,doc);
		}
	}

	public static void addHelpTextNode(QuestionDef qtn, Document doc, Element inputNode, Element firstOptionNode){
		String helpText = qtn.getHelpText();
		if(helpText != null && helpText.length() > 0){
			Element hintNode =  doc.createElement(NODE_NAME_HINT);
			hintNode.appendChild(doc.createTextNode(helpText));
			if(firstOptionNode == null)
				inputNode.appendChild(hintNode);
			else
				inputNode.insertBefore(hintNode, firstOptionNode);
			qtn.setHintNode(hintNode);
		}
	}

	public static Element fromOptionDef2Xform(OptionDef optionDef, Document doc, Element inputNode){
		Element itemNode =  doc.createElement(NODE_NAME_ITEM);

		Element node =  doc.createElement(NODE_NAME_LABEL);
		node.appendChild(doc.createTextNode(optionDef.getText()));
		itemNode.appendChild(node);
		optionDef.setLabelNode(node);

		node =  doc.createElement(NODE_NAME_VALUE);
		node.appendChild(doc.createTextNode(optionDef.getVariableName()));
		itemNode.appendChild(node);
		optionDef.setValueNode(node);

		inputNode.appendChild(itemNode);
		optionDef.setControlNode(itemNode);
		return itemNode;
	}

	private static void createQuestion(QuestionDef qtnDef, Element parentControlNode, Element parentDataNode, Document doc){
		String name = qtnDef.getVariableName();

		//TODO Should do this for all invalid characters in node names.
		name = name.replace("/", "");
		name = name.replace("\\", "");
		name = name.replace(" ", "");

		Element dataNode =  doc.createElement(name);
		if(qtnDef.getDefaultValue() != null && qtnDef.getDefaultValue().trim().length() > 0)
			dataNode.appendChild(doc.createTextNode(qtnDef.getDefaultValue()));
		parentDataNode.appendChild(dataNode);
		qtnDef.setDataNode(dataNode);

		Element inputNode =  getXformInputElementName(doc,qtnDef,ATTRIBUTE_NAME_REF);
		inputNode.setAttribute(ATTRIBUTE_NAME_TYPE, getXmlType(qtnDef.getDataType(),inputNode));
		if(qtnDef.isRequired())
			inputNode.setAttribute(ATTRIBUTE_NAME_REQUIRED, XPATH_VALUE_TRUE);
		if(!qtnDef.isEnabled())
			inputNode.setAttribute(ATTRIBUTE_NAME_READONLY, XPATH_VALUE_TRUE);
		if(qtnDef.isLocked())
			inputNode.setAttribute(ATTRIBUTE_NAME_LOCKED, XPATH_VALUE_TRUE);

		parentControlNode.appendChild(inputNode);
		qtnDef.setControlNode(inputNode);

		Element labelNode =  doc.createElement(NODE_NAME_LABEL);
		labelNode.appendChild(doc.createTextNode(qtnDef.getText()));
		inputNode.appendChild(labelNode);
		qtnDef.setLabelNode(labelNode);

		addHelpTextNode(qtnDef,doc,inputNode,null);

		if(qtnDef.getDataType() != QuestionDef.QTN_TYPE_REPEAT){
			Vector options = qtnDef.getOptions();
			if(options != null && options.size() > 0){
				for(int j=0; j<options.size(); j++){
					OptionDef optionDef = (OptionDef)options.elementAt(j);
					Element itemNode = fromOptionDef2Xform(optionDef,doc,inputNode);	
					if(j == 0)
						qtnDef.setFirstOptionNode(itemNode);
				}
			}
		}
	}

	private static Element getXformInputElementName(Document doc, QuestionDef qtnDef, String bindAttributeName){

		String name = NODE_NAME_INPUT;

		if(qtnDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE)
			name = NODE_NAME_SELECT1;
		else if(qtnDef.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE)
			name = NODE_NAME_SELECT;
		else if(qtnDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
			name = NODE_NAME_GROUP;

		String id = qtnDef.getVariableName();
		if(id.contains("/"))
			id = id.substring(id.lastIndexOf('/')+1);

		Element node = doc.createElement(name);
		if(qtnDef.getDataType() != QuestionDef.QTN_TYPE_REPEAT)
			node.setAttribute(bindAttributeName, id);

		if(qtnDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || qtnDef.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE)
			node.setAttribute("selection", "closed");

		return node;
	}

	private static boolean hasTextNode(Element node){
		for(int i=0; i<node.getChildNodes().getLength(); i++){
			if(node.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE)
				return true;
		}
		return false;
	}

	private static void removeTextNode(Element node){
		for(int i=0; i<node.getChildNodes().getLength(); i++){
			if(node.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE){
				String text = node.getChildNodes().item(i).getNodeValue().trim();
				if(text.length() > 0 && !text.equalsIgnoreCase("\n")){
					node.removeChild(node.getChildNodes().item(i));
					return;
				}
			}
		}
	}

	private static Element getInstanceNode(Document doc){
		return getInstanceNode(doc.getDocumentElement());

	}

	public static Element getInstanceDataNode(Document doc){
		return getInstanceDataNode(getInstanceNode(doc));
	}

	public static Document getInstanceDataDoc(Document doc){
		Element data = getInstanceDataNode(getInstanceNode(doc));
		Document dataDoc = XMLParser.createDocument();
		dataDoc.appendChild(data.cloneNode(true));

		Element root = dataDoc.getDocumentElement();
		NamedNodeMap attributes = doc.getDocumentElement().getAttributes();
		for(int index = 0; index < attributes.getLength(); index++){
			Node attribute = attributes.item(index);
			String name = attribute.getNodeName();
			if(name.startsWith("xmlns:"))
				root.setAttribute(name, attribute.getNodeValue());
		}

		return dataDoc;
	}

	private static Element getInstanceNode(Element element){
		int numOfEntries = element.getChildNodes().getLength();
		for (int i = 0; i < numOfEntries; i++) {
			if (element.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element child = (Element)element.getChildNodes().item(i);
				//String tagname = getNodeName(child);
				String tagname = child.getNodeName(); //NODE_NAME_INSTANCE has prefix
				if (tagname.equals(NODE_NAME_INSTANCE)||tagname.equals(NODE_NAME_INSTANCE_MINUS_PREFIX))
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

	private static Element getInstanceDataNode(Element element){
		int numOfEntries = element.getChildNodes().getLength();
		for (int i = 0; i < numOfEntries; i++) {
			if (element.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) 
				return (Element)element.getChildNodes().item(i);
		}

		return null;
	}

	//TODO This and the one below need to be refactored.
	public static String fromNode2String(Node node){
		return node.toString();
	}


	public static String fromDoc2String(Document doc){
		return doc.toString();
	}

	public static FormDef fromXform2FormDef(String xml){
		Document doc = getDocument(xml);
		return getFormDef(doc);
	}

	public static Document getDocument(String xml){
		return XMLParser.parse(xml);
	}

	public static FormDef getFormDef(Document doc){
		Element rootNode = doc.getDocumentElement();
		FormDef formDef = new FormDef();
		formDef.setDoc(doc);
		HashMap id2VarNameMap = new HashMap();
		HashMap relevants = new HashMap();
		Vector repeats = new Vector();
		HashMap rptKidMap = new HashMap();
		parseElement(formDef,rootNode,id2VarNameMap,null,relevants,repeats,rptKidMap,(int)0,null);
		if(formDef.getName() == null || formDef.getName().length() == 0)
			formDef.setName(formDef.getVariableName());
		setDefaultValues(getInstanceDataNode(doc),formDef,id2VarNameMap); //TODO Very slow needs optimisation for very big forms
		addSkipRules(formDef,id2VarNameMap,relevants);
		return formDef;
	}

	private static String getNodeTextValue(Element dataNode,String name){
		Element node = getNode(dataNode,name);
		if(node != null)
			return getTextValue(node);
		return null;
	}

	private static void setDefaultValues(Element dataNode,FormDef formDef,HashMap id2VarNameMap){
		String id, val;
		Iterator keys = id2VarNameMap.keySet().iterator();
		while(keys.hasNext()){
			id = (String)keys.next();
			String variableName = (String)id2VarNameMap.get(id);

			QuestionDef def = formDef.getQuestion(variableName);
			if(def == null)
				continue;

			if(variableName.contains("@"))
				setAttributeDefaultValue(def,variableName,dataNode);
			else{
				val = getNodeTextValue(dataNode,id);
				if(val == null || val.trim().length() == 0) //we are not allowing empty strings for now.
					continue;
				def.setDefaultValue(val);
			}
		}

		//Now do set default values for repeats since they are not part of the id2VarNameMap
		for(int pageNo=0; pageNo<formDef.getPageCount(); pageNo++){
			PageDef pageDef = formDef.getPageAt(pageNo);
			for(int qtnNo=0; qtnNo<pageDef.getQuestionCount(); qtnNo++){
				QuestionDef questionDef = pageDef.getQuestionAt(qtnNo);
				if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
					setQtnsDefaultValues(questionDef.getDataNode(),formDef,questionDef.getRepeatQtnsDef());
			}
		}
	}

	private static void setAttributeDefaultValue(QuestionDef qtn, String variableName,Element dataNode){
		String xpath = variableName;
		int pos = xpath.lastIndexOf('@'); String attributeName = null;
		if(pos == 0){
			attributeName = variableName.substring(1,variableName.length());
			String value = dataNode.getAttribute(attributeName);
			if(value != null && value.trim().length() > 0) //we are not allowing empty strings for now.
				qtn.setDefaultValue(value);
			return;
		}

		attributeName = xpath.substring(pos+1,xpath.length());
		xpath = xpath.substring(0,pos-1);

		XPathExpression xpls = new XPathExpression(dataNode, xpath);
		Vector result = xpls.getResult();

		for (Enumeration e = result.elements(); e.hasMoreElements();) {
			Object obj = e.nextElement();
			if (obj instanceof Element){
				String value = ((Element) obj).getAttribute(attributeName);
				if(value != null && value.trim().length() > 0) //we are not allowing empty strings for now.
					qtn.setDefaultValue(value);
			}
		}
	}

	private static void setQtnsDefaultValues(Element dataNode, FormDef formDef, RepeatQtnsDef repeatQtnsDef){
		for(int i=0; i<repeatQtnsDef.getQuestionsCount(); i++){
			QuestionDef questionDef = repeatQtnsDef.getQuestionAt(i);
			String id = questionDef.getVariableName();
			String val = getNodeTextValue(dataNode,id);
			if(val == null || val.trim().length() == 0) //we are not allowing empty strings for now.
				continue;
			questionDef.setDefaultValue(val);
		}
	}

	public static String getTextValue(Element node){
		int numOfEntries = node.getChildNodes().getLength();
		for (int i = 0; i < numOfEntries; i++) {
			if (node.getChildNodes().item(i).getNodeType() == Node.TEXT_NODE)
				return node.getChildNodes().item(i).getNodeValue();

			if(node.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE){
				String val = getTextValue((Element)node.getChildNodes().item(i));
				if(val != null)
					return val;
			}
		}

		return null;
	}

	public static boolean setTextNodeValue(Element node, String value){
		int numOfEntries = node.getChildNodes().getLength();
		for (int i = 0; i < numOfEntries; i++) {
			if (node.getChildNodes().item(i).getNodeType() == Node.TEXT_NODE){
				node.getChildNodes().item(i).setNodeValue(value);
				return true;
			}

			if(node.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE){
				if(setTextNodeValue((Element)node.getChildNodes().item(i),value))
					return true;
			}
		}
		return false;
	}

	/**
	 * Gets a child element of a parent node with a given name.
	 * 
	 * @param parent - the parent element
	 * @param name - the name of the child.
	 * @return - the child element.
	 */
	private static Element getNode(Element parent, String name){
		if(parent == null)
			return null;

		for(int i=0; i<parent.getChildNodes().getLength(); i++){
			if(parent.getChildNodes().item(i).getNodeType() != Node.ELEMENT_NODE)
				continue;

			Element child = (Element)parent.getChildNodes().item(i);
			if(getNodeName(child).equals(name))
				return child;

			child = getNode(child,name);
			if(child != null)
				return child;
		}

		return null;
	}

	private static QuestionDef parseElement(FormDef formDef, Element element, HashMap map,QuestionDef questionDef,HashMap relevants,Vector repeats, HashMap rptKidMap, int currentPageNo,QuestionDef parentQtn){
		String label = "";
		String hint = "";
		String value = "";
		Element labelNode = null;
		Element hintNode = null;
		Element valueNode = null;

		int numOfEntries = element.getChildNodes().getLength();
		for (int i = 0; i < numOfEntries; i++) {
			if (element.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element child = (Element)element.getChildNodes().item(i);
				String tagname = child.getNodeName(); //getNodeName(child);

				if(tagname.equals(NODE_NAME_REPEAT) || tagname.equals(NODE_NAME_REPEAT_MINUS_PREFIX))
					tagname.toString();

				if(tagname.equals(NODE_NAME_SUBMIT) || tagname.equals(NODE_NAME_SUBMIT_MINUS_PREFIX))
					continue;
				else if (tagname.equals("head"))
					parseElement(formDef,child,map,questionDef,relevants,repeats,rptKidMap,currentPageNo,parentQtn);
				else if (tagname.equals("body"))
					parseElement(formDef, child,map,questionDef,relevants,repeats,rptKidMap,currentPageNo,parentQtn);
				else if (tagname.equals("title")){
					if(child.getChildNodes().getLength() != 0)
						formDef.setName(child.getChildNodes().item(0).getNodeValue().trim());
				}
				else if (tagname.equals(NODE_NAME_MODEL) || tagname.equals(NODE_NAME_MODEL_MINUS_PREFIX)){
					formDef.setModelNode((Element)child);
					formDef.setXformsNode((Element)child.getParentNode());
					parseElement(formDef, child,map,questionDef,relevants,repeats,rptKidMap,currentPageNo,parentQtn);
				}
				else if (tagname.equals(NODE_NAME_GROUP) || tagname.equals(NODE_NAME_GROUP_MINUS_PREFIX)){
					String parentName = ((Element)child.getParentNode()).getNodeName();
					if(!(parentName.equalsIgnoreCase(NODE_NAME_GROUP)||parentName.equalsIgnoreCase(NODE_NAME_GROUP_MINUS_PREFIX))){
						if(formDef.getPageCount() < ++currentPageNo)
							formDef.addPage();
						formDef.setPageGroupNode((Element)child);
					}
					parseElement(formDef, child,map,questionDef,relevants,repeats,rptKidMap,currentPageNo,parentQtn);
				}
				else if(tagname.equals(NODE_NAME_INSTANCE)||tagname.equals(NODE_NAME_INSTANCE_MINUS_PREFIX)) {
					Element dataNode = null;
					for(int k=0; k<child.getChildNodes().getLength(); k++){
						if(child.getChildNodes().item(k).getNodeType() == Node.ELEMENT_NODE){
							dataNode = (Element)child.getChildNodes().item(k);
							formDef.setDataNode(dataNode);
						}
					}
					formDef.setVariableName(getNodeName(dataNode));
					if(dataNode.getAttribute(ATTRIBUTE_NAME_DESCRIPTION_TEMPLATE) != null)
						formDef.setDescriptionTemplate(dataNode.getAttribute(ATTRIBUTE_NAME_DESCRIPTION_TEMPLATE));
					if(dataNode.getAttribute(ATTRIBUTE_NAME_ID) != null)
						formDef.setId(Integer.parseInt(dataNode.getAttribute(ATTRIBUTE_NAME_ID)));
					if(dataNode.getAttribute(ATTRIBUTE_NAME_NAME) != null)
						formDef.setName(dataNode.getAttribute(ATTRIBUTE_NAME_NAME));
				} else if (tagname.equals(NODE_NAME_BIND)||tagname.equals(NODE_NAME_BIND_MINUS_PREFIX) /*|| tagname.equals(ATTRIBUTE_NAME_REF)*/) {
					QuestionDef qtn = new QuestionDef(null);
					qtn.setBindNode(child);
					//qtn.setId(Integer.parseInt(String.valueOf(i)));
					if(formDef.getPages() == null)
						qtn.setId(Integer.parseInt("1"));
					else{
						//This limitation was only for mobile devices and hence does not make sense for browser xform clients.
						/*if(((PageDef)formDef.getPages().elementAt(0)).getQuestions().size() > 126){
							System.out.println(((PageDef)formDef.getPages().elementAt(0)).getQuestions().size());
							//break;
						}*/
						//System.out.println(((PageDef)formDef.getPages().elementAt(0)).getQuestions().size());
						qtn.setId(Integer.parseInt(String.valueOf(((PageDef)formDef.getPages().elementAt(0)).getQuestions().size()+1))); //TODO Could some questions be on pages other than the first
					}
					qtn.setVariableName(getQuestionVariableName(child,formDef));
					setQuestionType(qtn,child.getAttribute(ATTRIBUTE_NAME_TYPE),child);
					if(child.getAttribute(ATTRIBUTE_NAME_REQUIRED) != null && child.getAttribute(ATTRIBUTE_NAME_REQUIRED).equals(XPATH_VALUE_TRUE))
						qtn.setRequired(true);
					if(child.getAttribute(ATTRIBUTE_NAME_READONLY) != null && child.getAttribute(ATTRIBUTE_NAME_READONLY).equals(XPATH_VALUE_TRUE))
						qtn.setEnabled(false);
					if(child.getAttribute(ATTRIBUTE_NAME_LOCKED) != null && child.getAttribute(ATTRIBUTE_NAME_LOCKED).equals(XPATH_VALUE_TRUE))
						qtn.setLocked(true);

					if(!addRepeatChildQtn(qtn,repeats,child,map,rptKidMap)){
						map.put(child.getAttribute(ATTRIBUTE_NAME_ID), qtn.getVariableName());
						formDef.addQuestion(qtn);
					}

					if(child.getAttribute(ATTRIBUTE_NAME_RELEVANT) != null)
						relevants.put(qtn,child.getAttribute(ATTRIBUTE_NAME_RELEVANT));

					if(qtn.getDataType() == QuestionDef.QTN_TYPE_REPEAT){
						RepeatQtnsDef repeatQtnsDef = new RepeatQtnsDef(qtn);
						qtn.setRepeatQtnsDef(repeatQtnsDef);
						repeats.addElement(qtn);

						questionDef = qtn;
					}

				} else if (tagname.equals(NODE_NAME_INPUT) || tagname.equals(NODE_NAME_SELECT1) || tagname.equals(NODE_NAME_SELECT) || tagname.equals(NODE_NAME_REPEAT)
						|| tagname.equals(NODE_NAME_INPUT_MINUS_PREFIX) || tagname.equals(NODE_NAME_SELECT1_MINUS_PREFIX) || tagname.equals(NODE_NAME_SELECT_MINUS_PREFIX) || tagname.equals(NODE_NAME_REPEAT_MINUS_PREFIX)) {
					String ref = child.getAttribute(ATTRIBUTE_NAME_REF);
					String bind = child.getAttribute(ATTRIBUTE_NAME_BIND);
					String varName = (String)map.get(((ref != null) ? ref : bind));

					//if(tagname.equals(NODE_NAME_REPEAT) || tagname.equals(NODE_NAME_REPEAT_MINUS_PREFIX))
					//	map.put(bind, bind); //TODO Not very sure about this

					//new addition may cause bugs
					if(varName == null){
						varName = addNonBindControl(formDef,child,relevants,ref,bind);
						if(ref != null)
							map.put(ref, ref);
					}

					if(isNumQuestionsBiggerThanMax(formDef))
						break;

					if(varName != null){
						QuestionDef qtn = formDef.getQuestion(varName);
						if(qtn == null)
							qtn = (QuestionDef)rptKidMap.get(varName);

						if(tagname.equals(NODE_NAME_SELECT1) || tagname.equals(NODE_NAME_SELECT)
								||tagname.equals(NODE_NAME_SELECT1_MINUS_PREFIX) || tagname.equals(NODE_NAME_SELECT_MINUS_PREFIX)){
							qtn.setDataType((tagname.equals(NODE_NAME_SELECT1)||tagname.equals(NODE_NAME_SELECT1_MINUS_PREFIX)) ? QuestionDef.QTN_TYPE_LIST_EXCLUSIVE : QuestionDef.QTN_TYPE_LIST_MULTIPLE);
							qtn.setOptions(new Vector());
						}//TODO first addition for repeats
						else if((tagname.equals(NODE_NAME_REPEAT)||tagname.equals(NODE_NAME_REPEAT_MINUS_PREFIX)) && !label.equals("")){
							qtn.setDataType(QuestionDef.QTN_TYPE_REPEAT);
							qtn.setText(label);
							qtn.setHelpText(hint);
							qtn.setRepeatQtnsDef(new RepeatQtnsDef(qtn));
							formDef.moveQuestion2Page(qtn, currentPageNo);
							label = "";
							hint = "";
							qtn.setLabelNode(labelNode);
							qtn.setHintNode(hintNode);
							qtn.setControlNode(child);
							int pageNo = currentPageNo;
							if(pageNo == 0) pageNo = 1; //Xform may not have groups for pages.
							setQuestionDataNode(qtn,formDef,formDef.getPageAt(pageNo-1),parentQtn);
							parentQtn = qtn;
						}

						//TODO second addition for repeats
						Element parent = (Element)child.getParentNode(); 
						if(parent.getNodeName().equals(NODE_NAME_REPEAT)||parent.getNodeName().equals(NODE_NAME_REPEAT_MINUS_PREFIX)){
							varName = (String)map.get(parent.getAttribute(ATTRIBUTE_NAME_BIND) != null ? parent.getAttribute(ATTRIBUTE_NAME_BIND) : parent.getAttribute(ATTRIBUTE_NAME_NODESET));
							QuestionDef rptQtnDef = formDef.getQuestion(varName);
							rptQtnDef.addRepeatQtnsDef(qtn);
							formDef.removeQuestion(qtn);
						}

						questionDef = qtn;
						parseElement(formDef, child, map,questionDef,relevants,repeats,rptKidMap,currentPageNo,parentQtn);
					}
				} else if(tagname.equals(NODE_NAME_LABEL)||tagname.equals(NODE_NAME_LABEL_MINUS_PREFIX)){
					String parentName = ((Element)child.getParentNode()).getNodeName();
					if(parentName.equalsIgnoreCase(NODE_NAME_INPUT) || parentName.equalsIgnoreCase(NODE_NAME_SELECT) || parentName.equalsIgnoreCase(NODE_NAME_SELECT1) || parentName.equalsIgnoreCase(NODE_NAME_ITEM)
							||parentName.equalsIgnoreCase(NODE_NAME_INPUT_MINUS_PREFIX) || parentName.equalsIgnoreCase(NODE_NAME_SELECT_MINUS_PREFIX) || parentName.equalsIgnoreCase(NODE_NAME_SELECT1_MINUS_PREFIX) || parentName.equalsIgnoreCase(NODE_NAME_ITEM_MINUS_PREFIX)){
						if(child.getChildNodes().getLength() != 0){
							label = child.getChildNodes().item(0).getNodeValue().trim(); //questionDef.setText(child.getChildNodes().item(0).getNodeValue().trim());
							labelNode = child;
						}
					}
					else if(parentName.equalsIgnoreCase(NODE_NAME_REPEAT)||parentName.equalsIgnoreCase(NODE_NAME_REPEAT_MINUS_PREFIX)){
						if(questionDef != null && child.getChildNodes().getLength() != 0)
							questionDef.setText(child.getChildNodes().item(0).getNodeValue().trim());
					}
					else if(parentName.equalsIgnoreCase(NODE_NAME_GROUP)||parentName.equalsIgnoreCase(NODE_NAME_GROUP_MINUS_PREFIX)){
						if(child.getChildNodes().getLength() != 0){
							label = child.getChildNodes().item(0).getNodeValue().trim();
							labelNode = child;
						}
					}
				}
				else if (tagname.equals(NODE_NAME_HINT)||tagname.equals(NODE_NAME_HINT_MINUS_PREFIX)){
					String parentName = ((Element)child.getParentNode()).getNodeName();
					if(parentName.equalsIgnoreCase(NODE_NAME_GROUP)||parentName.equalsIgnoreCase(NODE_NAME_GROUP_MINUS_PREFIX)){
						if(child.getChildNodes().getLength() != 0){
							hint = child.getChildNodes().item(0).getNodeValue().trim();
							hintNode = child;
						}
					}
					else if(questionDef != null){
						if(child.getChildNodes().getLength() != 0){
							questionDef.setHelpText(child.getChildNodes().item(0).getNodeValue().trim());
							questionDef.setHintNode(element);
						}
					}
				}
				else if (tagname.equals(NODE_NAME_ITEM)||tagname.equals(NODE_NAME_ITEM_MINUS_PREFIX))
					parseElement(formDef, child,map,questionDef,relevants,repeats,rptKidMap,currentPageNo,parentQtn);
				else if (tagname.equals(NODE_NAME_VALUE)||tagname.equals(NODE_NAME_VALUE_MINUS_PREFIX)){
					if(child.getChildNodes().getLength() != 0){
						value = child.getChildNodes().item(0).getNodeValue().trim();
						valueNode = child;
					}
				}
				else
					parseElement(formDef, child,map,questionDef,relevants,repeats,rptKidMap,currentPageNo,parentQtn);
				// TODO - how are other elements like html:p or br handled?
			}
		}

		if (!label.equals("") && !value.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
			if (questionDef != null && questionDef.getOptions() != null){
				if(questionDef.getOptions().size() == 0)
					questionDef.setFirstOptionNode(element);
				OptionDef optionDef = new OptionDef(Integer.parseInt(String.valueOf(questionDef.getOptions().size())),label, value,questionDef);
				optionDef.setControlNode(element);
				optionDef.setLabelNode(labelNode);
				optionDef.setValueNode(valueNode);
				questionDef.addOption(optionDef);
			}
		} 
		else if (!label.equals("") && questionDef != null){
			if(questionDef.getText() == null || questionDef.getText().trim().length()==0){
				questionDef.setText(label);
				int pageNo = currentPageNo;
				if(pageNo == 0) pageNo = 1; //Xform may not have groups for pages.
				formDef.moveQuestion2Page(questionDef, pageNo);
				questionDef.setControlNode(element);
				questionDef.setLabelNode(labelNode);
				setQuestionDataNode(questionDef,formDef,formDef.getPageAt(pageNo-1),parentQtn);
			}
			else{
				formDef.setPageName(label);
				formDef.setPageLabelNode(labelNode);
			}
		}

		return questionDef;
	}

	private static void setQuestionDataNode(QuestionDef qtn, FormDef formDef, PageDef pageDef,QuestionDef parentQtn){
		String xpath = qtn.getVariableName();

		//xpath = new String(xpath.toCharArray(), 1, xpath.length()-1);
		int pos = xpath.lastIndexOf('@'); String attributeName = null;
		if(pos > 0){
			attributeName = xpath.substring(pos+1,xpath.length());
			xpath = xpath.substring(0,pos-1);
		}

		Element node = formDef.getDataNode();
		if(qtn.getControlNode().getParentNode().getNodeName().equals(XformConverter.NODE_NAME_REPEAT)){
			if(parentQtn != null)
				node = parentQtn.getDataNode();
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
			}
		}
	}

	private static String getQuestionVariableName(Element child, FormDef formDef){
		String name = child.getAttribute(ATTRIBUTE_NAME_NODESET);

		if(name.startsWith("/"+formDef.getVariableName()+"/"))
			name = name.replace("/"+formDef.getVariableName()+"/", "");
		return name;
	}

	/**
	 * Checks if this is a repeat child question and adds it.
	 * @param qtn the questions to check
	 * @param repeats the list of repeat questions
	 * @return true if so, else false.
	 */
	private static boolean addRepeatChildQtn(QuestionDef qtn, Vector repeats,Element child,HashMap map,HashMap rptKidmap){
		for(int i=0; i<repeats.size(); i++){
			QuestionDef rptQtn = (QuestionDef)repeats.get(i);
			if(qtn.getVariableName().contains(rptQtn.getVariableName())){
				RepeatQtnsDef rptQtnsDef = rptQtn.getRepeatQtnsDef();
				//rptQtnsDef.addQuestion(qtn); //TODO This is temporarily removed to solve the wiered problem list bug
				String varname = qtn.getVariableName().substring(rptQtn.getVariableName().length()+1);
				//varname = varname.substring(0, varname.indexOf('/'));
				//map.put(child.getAttribute(ATTRIBUTE_NAME_ID), varname);
				map.put(varname, varname);
				rptKidmap.put(varname, qtn);
				return true;		
			}
		}
		return false;
	}

	/*private static boolean isAllowedNodeName(String name){
		return name.equalsIgnoreCase(NODE_NAME_BIND) || name.equalsIgnoreCase(NODE_NAME_INPUT) ||
			   name.equalsIgnoreCase(NODE_NAME_LABEL) || name.equalsIgnoreCase(NODE_NAME_SELECT1) ||
			   name.equalsIgnoreCase(NODE_NAME_ITEM) || name.equalsIgnoreCase(NODE_NAME_HINT) ||
			   name.equalsIgnoreCase(NODE_NAME_REPEAT) || name.equalsIgnoreCase(NODE_NAME_SELECT) ||
			   name.equalsIgnoreCase(NODE_NAME_MODEL);
	}*/

	//secret,textarea, submit, trigger, upload, range
	/*<range ref="length" start="0" end="100" step="5">
	  <label>Length:</label>
	</range>*/
	private static void setQuestionType(QuestionDef def, String type, Element node){
		if(type != null){
			if(type.equals(DATA_TYPE_TEXT) || type.indexOf("string") != -1 )
				def.setDataType(QuestionDef.QTN_TYPE_TEXT);
			else if((type.equals("xsd:integer") || type.equals(DATA_TYPE_INT)) || (type.indexOf("integer") != -1 || type.indexOf("int") != -1))
				def.setDataType(QuestionDef.QTN_TYPE_NUMERIC);
			else if(type.equals("xsd:decimal") || type.indexOf("decimal") != -1 )
				def.setDataType(QuestionDef.QTN_TYPE_DECIMAL);
			else if(type.equals("xsd:dateTime") || type.indexOf("dateTime") != -1 )
				def.setDataType(QuestionDef.QTN_TYPE_DATE_TIME);
			else if(type.equals("xsd:time") || type.indexOf("time") != -1 )
				def.setDataType(QuestionDef.QTN_TYPE_TIME);
			else if(type.equals(DATA_TYPE_DATE) || type.indexOf("date") != -1 )
				def.setDataType(QuestionDef.QTN_TYPE_DATE);
			else if(type.equals(DATA_TYPE_BOOLEAN) || type.indexOf("boolean") != -1 )
				def.setDataType(QuestionDef.QTN_TYPE_BOOLEAN);
			else if(type.equals(DATA_TYPE_BINARY) || type.indexOf("base64Binary") != -1 ){
				String format = node.getAttribute("format");
				if("video".equals(format))
					def.setDataType(QuestionDef.QTN_TYPE_VIDEO);
				else if("audio".equals(format))
					def.setDataType(QuestionDef.QTN_TYPE_AUDIO);
				else
					def.setDataType(QuestionDef.QTN_TYPE_IMAGE);
			}
		}
		else
			def.setDataType(QuestionDef.QTN_TYPE_TEXT); //QTN_TYPE_REPEAT
	}
	/*public StudyDef fromXform2StudyDef(String xform){
		return null;
	}

	public String fromStudyDef2Xform(StudyDef def){
		return null;
	}*/

	private static void addSkipRules(FormDef formDef, HashMap map, HashMap relevants){
		Vector rules = new Vector();

		Iterator keys = relevants.keySet().iterator();
		int id = 0;
		while(keys.hasNext()){
			QuestionDef qtn = (QuestionDef)keys.next();
			SkipRule skipRule = buildSkipRule(formDef, qtn.getId(),(String)relevants.get(qtn),++id,getAction(qtn));
			if(skipRule != null)
				rules.add(skipRule);
		}

		formDef.setSkipRules(rules);
	}

	private static int getAction(QuestionDef qtn){
		Element node = qtn.getBindNode();
		if(node == null)
			return PurcConstants.ACTION_DISABLE;

		String value = node.getAttribute(ATTRIBUTE_NAME_ACTION);
		if(value == null)
			return PurcConstants.ACTION_DISABLE;

		int action = 0;
		if(value.equalsIgnoreCase(ATTRIBUTE_VALUE_ENABLE))
			action |= PurcConstants.ACTION_ENABLE;
		else if(value.equalsIgnoreCase(ATTRIBUTE_VALUE_DISABLE))
			action |= PurcConstants.ACTION_DISABLE;
		else if(value.equalsIgnoreCase(ATTRIBUTE_VALUE_SHOW))
			action |= PurcConstants.ACTION_SHOW;
		else if(value.equalsIgnoreCase(ATTRIBUTE_VALUE_HIDE))
			action |= PurcConstants.ACTION_HIDE;

		value = node.getAttribute(ATTRIBUTE_NAME_REQUIRED);
		if(value.equalsIgnoreCase(XPATH_VALUE_TRUE))
			action |= PurcConstants.ACTION_MAKE_MANDATORY;
		else 
			action |= PurcConstants.ACTION_MAKE_OPTIONAL;

		return action;
	}

	private static SkipRule buildSkipRule(FormDef formDef, int questionId, String relevant, int id, int action){

		SkipRule skipRule = new SkipRule();
		skipRule.setId(id);
		//TODO For now we are only dealing with enabling and disabling.
		skipRule.setAction(action);
		skipRule.setConditions(getSkipRuleConditions(formDef,relevant,action));
		skipRule.setConditionsOperator(getConditionsOperator(relevant));

		Vector actionTargets = new Vector();
		actionTargets.add(new Integer(questionId));
		skipRule.setActionTargets(actionTargets);
		//skipRule.setName(name);

		if(skipRule.getConditions() == null || skipRule.getConditions().size() == 0)
			return null;
		return skipRule;
	}

	private static Vector getSkipRuleConditions(FormDef formDef, String relevant, int action){
		Vector conditions = new Vector();

		Vector list = getConditionsOperatorTokens(relevant);

		Condition condition  = new Condition();
		for(int i=0; i<list.size(); i++){
			condition = getSkipRuleCondition(formDef,(String)list.elementAt(i),(int)(i+1),action);
			if(condition != null)
				conditions.add(condition);
		}

		return conditions;
	}

	private static Condition getSkipRuleCondition(FormDef formDef, String relevant, int id, int action){		
		Condition condition  = new Condition();
		condition.setId(id);
		condition.setOperator(getOperator(relevant,action));

		//eg relevant="/data/question10='7'"
		int pos = getOperatorPos(relevant);
		if(pos < 0)
			return null;

		String varName = relevant.substring(0, pos);
		QuestionDef questionDef = formDef.getQuestion(varName.trim());
		if(questionDef == null){
			String prefix = "/" + formDef.getVariableName() + "/";
			if(varName.startsWith(prefix))
				questionDef = formDef.getQuestion(varName.trim().substring(prefix.length(), varName.trim().length()));
			if(questionDef == null)
				return null;
		}
		condition.setQuestionId(questionDef.getId());

		String value;
		//first try a value delimited by '
		int pos2 = relevant.lastIndexOf('\'');
		if(pos2 > 0){
			//pos1++;
			int pos1 = relevant.substring(0, pos2).lastIndexOf('\'',pos2);
			if(pos1 < 0){
				System.out.println("Relevant value not closed with ' characher");
				return null;
			}
			pos1++;
			value = relevant.substring(pos1,pos2);
		}
		else //else we take whole value after operator	
			value = relevant.substring(pos+1,relevant.length());

		if(!(value.equals("null") || value.equals(""))){
			condition.setValue(value.trim());

			if(condition.getOperator() == PurcConstants.OPERATOR_NULL)
				return null; //no operator set hence making the condition invalid
		}
		else
			condition.setOperator(PurcConstants.OPERATOR_IS_NULL);

		return condition;
	}

	//TODO Add the other xpath operators
	private static int getOperator(String relevant, int action){
		/*if(relevant.indexOf('=') > 0)
			return PurcConstants.OPERATOR_EQUAL;
		else if(relevant.indexOf('>') > 0)
			return PurcConstants.OPERATOR_GREATER;
		else if(relevant.indexOf(">=") > 0)
			return PurcConstants.OPERATOR_GREATER_EQUAL;
		else if(relevant.indexOf('<') > 0)
			return PurcConstants.OPERATOR_LESS;
		else if(relevant.indexOf("<=") > 0)
			return PurcConstants.OPERATOR_LESS_EQUAL;
		else if(relevant.indexOf("!=") > 0 || relevant.indexOf("!=") > 0)
			return PurcConstants.OPERATOR_NOT_EQUAL;*/

		//We return the operator which is the opposite of the relevant

		if(relevant.indexOf(">=") > 0 || relevant.indexOf("&gt;=") > 0){
			if(isPositiveAction(action))
				return PurcConstants.OPERATOR_GREATER_EQUAL;
			return PurcConstants.OPERATOR_LESS;
		}
		else if(relevant.indexOf('>') > 0 || relevant.indexOf("&gt;") > 0){
			if(isPositiveAction(action))
				return PurcConstants.OPERATOR_GREATER;
			return PurcConstants.OPERATOR_LESS_EQUAL;
		}
		else if(relevant.indexOf("<=") > 0 || relevant.indexOf("&lt;=") > 0){
			if(isPositiveAction(action))
				return PurcConstants.OPERATOR_LESS_EQUAL;
			return PurcConstants.OPERATOR_GREATER;
		}
		else if(relevant.indexOf('<') > 0 || relevant.indexOf("&lt;") > 0){
			if(isPositiveAction(action))
				return PurcConstants.OPERATOR_LESS;
			return PurcConstants.OPERATOR_GREATER_EQUAL;
		}
		else if(relevant.indexOf("!=") > 0 || relevant.indexOf("!=") > 0){
			if(isPositiveAction(action))
				return PurcConstants.OPERATOR_NOT_EQUAL;
			return PurcConstants.OPERATOR_EQUAL;
		}
		else if(relevant.indexOf('=') > 0){
			if(isPositiveAction(action))
				return PurcConstants.OPERATOR_EQUAL;
			return PurcConstants.OPERATOR_NOT_EQUAL;
		}

		return PurcConstants.OPERATOR_NULL;
	}

	private static boolean isPositiveAction(int action){
		return ((action & PurcConstants.ACTION_ENABLE) != 0) || ((action & PurcConstants.ACTION_SHOW) != 0);
	}

	private static String getOperator(int operator, int action){
		if(operator == PurcConstants.OPERATOR_EQUAL)
			return isPositiveAction(action) ? "=" : "!=";
		else if(operator == PurcConstants.OPERATOR_NOT_EQUAL)
			return isPositiveAction(action) ? "!=" : "=";
		else if(operator == PurcConstants.OPERATOR_LESS)
			return isPositiveAction(action) ? "<" : ">=";
		else if(operator == PurcConstants.OPERATOR_GREATER)
			return isPositiveAction(action) ? ">" : "<=";
		else if(operator == PurcConstants.OPERATOR_LESS_EQUAL)
			return isPositiveAction(action) ? "<=" : ">";
		else if(operator == PurcConstants.OPERATOR_GREATER_EQUAL)
			return isPositiveAction(action) ? ">=" : "<";
		return "=";
	}

	private static int getOperatorPos(String relevant){
		//Using lastindexof because of expressions like:
		//relevant="/ClinicalData/SubjectData/StudyEventData/FormData/ItemGroupData/ItemData[@ItemOID='I_REVI_IMPROVEMENT']/@Value = '1'"
		int pos = relevant.lastIndexOf("!=");
		if(pos > 0)
			return pos;

		pos = relevant.lastIndexOf(">=");
		if(pos > 0)
			return pos;

		pos = relevant.lastIndexOf("<=");
		if(pos > 0)
			return pos;

		pos = relevant.lastIndexOf('>');
		if(pos > 0)
			return pos;

		pos = relevant.lastIndexOf('<');
		if(pos > 0)
			return pos;

		pos = relevant.lastIndexOf('=');
		if(pos > 0)
			return pos;

		return pos;
	}

	private static Vector getConditionsOperatorTokens(String relevant){
		//TODO For now we are only dealing with one AND or OR, for simplicity
		//If one mixes both in the same relevant statement, then we take the first.
		Vector list = new Vector();

		int pos = 0;
		do{
			pos = extractConditionsOperatorTokens(relevant,pos,list);
		}while(pos > 0);

		return list;
	}

	private static int extractConditionsOperatorTokens(String relevant,int startPos, Vector list){
		int pos,pos2,opSize = 3;

		pos = relevant.toUpperCase().indexOf(CONDITIONS_OPERATOR_TEXT_AND,startPos);
		if(pos <0){
			pos = relevant.toUpperCase().indexOf(CONDITIONS_OPERATOR_TEXT_OR,startPos);
			opSize = 2;
		}

		//AND may be the last token when we have starting ORs hence skipping them. eg (relevant="/data/question10=7 OR /data/question6=4    OR  /data/question8=1 AND /data/question1='daniel'")
		pos2 = relevant.toUpperCase().indexOf(CONDITIONS_OPERATOR_TEXT_OR,startPos);
		if(pos2 > 0 && pos2 < pos){
			pos = pos2;
			opSize = 2;
		}


		if(pos < 0){
			list.add(relevant.substring(startPos).trim());
			opSize = 0;
		}
		else
			list.add(relevant.substring(startPos,pos).trim());

		return pos+opSize;
	}

	private static int getConditionsOperator(String relevant){
		if(relevant.toUpperCase().indexOf(CONDITIONS_OPERATOR_TEXT_AND) > 0)
			return PurcConstants.CONDITIONS_OPERATOR_AND;
		else if(relevant.toUpperCase().indexOf(CONDITIONS_OPERATOR_TEXT_OR) > 0)
			return PurcConstants.CONDITIONS_OPERATOR_OR;
		return PurcConstants.CONDITIONS_OPERATOR_NULL;
	}


	private static boolean isNumQuestionsBiggerThanMax(FormDef formDef){
		return false; //((PageDef)formDef.getPages().elementAt(0)).getQuestions().size() > 126;
	}

	private static String addNonBindControl(FormDef formDef,Element child,HashMap relevants, String ref, String bind){
		QuestionDef qtn = new QuestionDef(null);
		if(formDef.getPages() == null)
			qtn.setId(Integer.parseInt("1"));
		else{
			if(isNumQuestionsBiggerThanMax(formDef))
				return null;
			qtn.setId(Integer.parseInt(String.valueOf(((PageDef)formDef.getPages().elementAt(0)).getQuestions().size()+1)));
		}

		if(child.getAttribute(ATTRIBUTE_NAME_TYPE) == null)
			qtn.setDataType(QuestionDef.QTN_TYPE_TEXT);
		else
			setQuestionType(qtn,child.getAttribute(ATTRIBUTE_NAME_TYPE),child);

		if(child.getAttribute(ATTRIBUTE_NAME_REQUIRED) != null && child.getAttribute(ATTRIBUTE_NAME_REQUIRED).equals(XPATH_VALUE_TRUE))
			qtn.setRequired(true);
		if(child.getAttribute(ATTRIBUTE_NAME_READONLY) != null && child.getAttribute(ATTRIBUTE_NAME_READONLY).equals(XPATH_VALUE_TRUE))
			qtn.setEnabled(false);
		if(child.getAttribute(ATTRIBUTE_NAME_LOCKED) != null && child.getAttribute(ATTRIBUTE_NAME_LOCKED).equals(XPATH_VALUE_TRUE))
			qtn.setLocked(true);

		qtn.setVariableName(((ref != null) ? ref : bind));
		formDef.addQuestion(qtn);

		if(child.getAttribute(ATTRIBUTE_NAME_RELEVANT) != null)
			relevants.put(qtn,child.getAttribute(ATTRIBUTE_NAME_RELEVANT));

		return qtn.getVariableName();
	}

	private static String getNodeName(Element element){
		String name = element.getNodeName();
		String prefix = element.getPrefix();
		if(prefix != null){
			if(name.startsWith(prefix))
				name = name.replace(prefix+":", "");
		}
		return name;
	}

	public static Element getNode(String xml){
		xml = "<xf:xforms xmlns:xf=\"http://www.w3.org/2002/xforms\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" + xml;
		xml = xml + "</xf:xforms>";
		Document doc = XMLParser.parse(xml);
		Element node = (Element)doc.getDocumentElement().getChildNodes().item(0);
		if(node.getAttribute(XformConverter.ATTRIBUTE_NAME_XMLNS) != null)
			node.removeAttribute(XformConverter.ATTRIBUTE_NAME_XMLNS);
		return node;
	}

	public static Element renameNode(Element node, String newName){
		String xml = node.toString();
		xml = xml.replace(node.getNodeName(), newName);
		Element child = XformConverter.getNode(xml);
		Element parent = (Element)node.getParentNode();
		parent.replaceChild(child, node);
		return child;
	}
}
