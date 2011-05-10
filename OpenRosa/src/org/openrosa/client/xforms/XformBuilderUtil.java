package org.openrosa.client.xforms;

import java.util.List;
import java.util.Vector;

import org.openrosa.client.model.FormDef;
import org.openrosa.client.model.IFormElement;
import org.openrosa.client.model.QuestionDef;
import org.openrosa.client.model.ModelConstants;
import org.openrosa.client.xforms.XformConstants;


import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;


/**
 * Utility methods used during the building of xforms documents.
 * 
 * @author daniel
 *
 */
public class XformBuilderUtil {

	/**
	 * All methods in this class are static and hence we expect no external
	 * Instantiation of this class.
	 */
	private XformBuilderUtil(){

	}
	
	
	/**
	 * Converts from a question definition object type to its xsd type.
	 * 
	 * @param type the QuestionDef data type.
	 * @param node the node having the type attribute.
	 * @return the xsd type.
	 */
	public static String getXmlType(int type, Element node){
		if(node != null){
			if(type == QuestionDef.QTN_TYPE_VIDEO)
				node.setAttribute(XformConstants.ATTRIBUTE_NAME_FORMAT,XformConstants.ATTRIBUTE_VALUE_VIDEO);
			else if(type == QuestionDef.QTN_TYPE_AUDIO)
				node.setAttribute(XformConstants.ATTRIBUTE_NAME_FORMAT, XformConstants.ATTRIBUTE_VALUE_AUDIO);
			else if(type == QuestionDef.QTN_TYPE_IMAGE)
				node.setAttribute(XformConstants.ATTRIBUTE_NAME_FORMAT, XformConstants.ATTRIBUTE_VALUE_IMAGE);
			else if(type == QuestionDef.QTN_TYPE_GPS)
				node.setAttribute(XformConstants.ATTRIBUTE_NAME_FORMAT, XformConstants.ATTRIBUTE_VALUE_GPS);
		}

		switch(type){
		case QuestionDef.QTN_TYPE_BOOLEAN:
			return XformConstants.DATA_TYPE_BOOLEAN;
		case QuestionDef.QTN_TYPE_DATE:
			return XformConstants.DATA_TYPE_DATE;
		case QuestionDef.QTN_TYPE_DATE_TIME:
			return XformConstants.DATA_TYPE_DATE_TIME;
		case QuestionDef.QTN_TYPE_TIME:
			return XformConstants.DATA_TYPE_TIME;
		case QuestionDef.QTN_TYPE_DECIMAL:
			return XformConstants.DATA_TYPE_DECIMAL;
		case QuestionDef.QTN_TYPE_NUMERIC:
			return XformConstants.DATA_TYPE_INT;
		case QuestionDef.QTN_TYPE_TEXT:
			return XformConstants.DATA_TYPE_TEXT;
		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
			return "select1";
		case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
			return "select";
		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC:
			return "";
		case QuestionDef.QTN_TYPE_GPS:
			return "geopoint"; //XformConstants.DATA_TYPE_TEXT;
		case QuestionDef.QTN_TYPE_IMAGE:
		case QuestionDef.QTN_TYPE_VIDEO:
		case QuestionDef.QTN_TYPE_AUDIO:
			return XformConstants.DATA_TYPE_BINARY;
		case QuestionDef.QTN_TYPE_BARCODE:
			return XformConstants.DATA_TYPE_BARCODE;
		}

		return "";
	}

	
	/**
	 * Converts an operator, for combining more than one condition, to its xforms representation.
	 * 
	 * @param operator the operator numeric value.
	 * @return the operator xforms text.
	 */
	public static String getConditionsOperatorText(int operator){
		String operatorText = null;
		if(operator == ModelConstants.CONDITIONS_OPERATOR_AND)
			operatorText = XformConstants.CONDITIONS_OPERATOR_TEXT_AND;
		else if(operator == ModelConstants.CONDITIONS_OPERATOR_OR)
			operatorText = XformConstants.CONDITIONS_OPERATOR_TEXT_OR;

		return /*" " +*/ operatorText /*+ " "*/;
	}
	
	
	/**
	 * Creates an xforms instance data child node from a given question variable name.
	 * 
	 * @param doc the xforms document.
	 * @param variableName the question variable name.
	 * @param formDef the form definition object.
	 * @param parentDataNode the xforms instance data node.
	 * @return the instance data child node for the question.
	 */
	public static Element fromVariableName2Node(Document doc, String variableName,FormDef formDef,Element parentDataNode, IFormElement currentQuestion, IFormElement parentQuestion){
		String name = variableName;
		//TODO May need to be smarter than this. Avoid invalid node
		//names. eg those having slashes (form1/question1)
		if(name.startsWith(formDef.getQuestionID()))
			name = name.substring(formDef.getQuestionID().length()+1);

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
			String[] tokens = name.split("/");
			name = tokens[tokens.length-1];
			dataNode = doc.createElement(name);
			if(!hasChildElementWithName(parentDataNode,dataNode.getNodeName())){
				List<IFormElement> childrenDefs = parentQuestion.getChildren();
				int childIndex = childrenDefs.indexOf(currentQuestion);
				insertNodeAtIndex(parentDataNode, parentDataNode.getChildNodes(), childIndex, dataNode);
			}
		}
		else{
			//construct a tree of nodes as given by the path in variableName (e.g. foo/bar/bash/baz -> <foo><bar><bash><baz /></bash>....)
			Element parentNode = null;
			for(int i=0; i<nodes.size(); i++){
				if(i==0){
					dataNode = nodes.elementAt(i);
					if(!hasChildElementWithName(parentDataNode,dataNode.getNodeName())){
						parentDataNode.appendChild(dataNode);
					}
					parentNode = dataNode;
				}
				else{
					if(!hasChildElementWithName(parentNode,nodes.elementAt(i).getNodeName())){
						parentNode.appendChild(nodes.elementAt(i));
					}
					
					parentNode = nodes.elementAt(i);
				}
			}
			dataNode = nodes.elementAt(nodes.size()-1);
		}

		return dataNode;
	}
	
	/**
	 * Inserts the given childNode /before/ the item at Index
	 * applicable to Data and Control XML nodes only
	 * if index > childrenDOMNodes.length || index < 0 it appends the child to the list and returns.
	 */
	public static void insertNodeAtIndex(Element parentDOMNode, NodeList childrenDOMNodes, int index, Element child){
		if(index > childrenDOMNodes.getLength() || index < 0){
			parentDOMNode.appendChild(child);
			return;
		}
	
		if(index == -1 || childrenDOMNodes.getLength() == 0){
			parentDOMNode.appendChild(child);
		}else{
			Node nearestSibling = getNearestSibling(index, parentDOMNode.getChildNodes());
			if(nearestSibling != null){
				parentDOMNode.insertBefore(child, nearestSibling);
			}else{
				parentDOMNode.appendChild(child);
			}
		}
	}
	
	/**
	 * Goes through childrenDOMNodes and gets the Node specified by index. (where index is
	 * calculated by counting the number of NON-TEXT nodes).  E.g. childrenDOMNodes.getLength == 15
	 * but there are only 5 non-text nodes in the list. Specifiying getNearestSibling(3,...) would
	 * return the 3rd non-text node.  If index is out of bounds of the ChildrenDOMNodes list it will
	 * return the first or last, respectively, node regardless of its type
	 * @param index
	 * @param childrenDOMNodes
	 * @return
	 */
	private static Node getNearestSibling(int index, NodeList childrenDOMNodes){
		int count = 0;
		if(index < 0){
			return childrenDOMNodes.item(0);
		}else if(index > childrenDOMNodes.getLength()){
			return childrenDOMNodes.item(childrenDOMNodes.getLength()-1);
		}
		
		for(int i=0;i<childrenDOMNodes.getLength();i++){
			Node currentItem = childrenDOMNodes.item(i);
			if(count > index ){ 
				return currentItem; 
			}
			if(currentItem.getNodeType() == Element.TEXT_NODE){
				continue;
			}
			
			if(count == index){
				return currentItem;
			}else{
				count++;
			}
		}
		return childrenDOMNodes.item(childrenDOMNodes.getLength()-1); //since we got this far we should return the last node in the list.
	}
	
	/**
	 * If the node has a text node that is empty, or no text nodes, this will return true.
	 * If text nodes are present but they are non empty, returns false.
	 * @param element
	 * @return
	 */
	public static boolean nodeHasNoOrEmptyTextNodeChildren(Element element){
		for(int i=0;i<element.getChildNodes().getLength();i++){
			Node curNode = element.getChildNodes().item(i);
			if(curNode.getNodeType() == Node.TEXT_NODE){
				String s = curNode.getNodeValue();
				if(s.trim().isEmpty()){ return true;}   //there's an edge case here, where if you run in Firefox and element.getNodeValue() has more than 4096 whitespaces this might not be correct. Don't do that.
				else{ return false; }
			}
		}
		return true;
	}
	
	/**
	 * Checks to see if the Specified parent Element has a child (1st generation children only!)
	 * with the name specified (where name is the tag name of the element).
	 * @param parent
	 * @param name - CASE SENSITIVE!
	 * @return true if name found, false if not. False if no children are present.
	 */
	public static boolean hasChildElementWithName(Element parent, String name){
		if(!parent.hasChildNodes()){ return false; }
		for(int i=0;i<parent.getChildNodes().getLength();i++){
			if((parent.getChildNodes().item(i)).getNodeType() == Element.TEXT_NODE){ continue; }
			Element child = (Element)parent.getChildNodes().item(i);
			if(child.getNodeName().equals(name)){
				return true;
			}
		}
		return false;
		
		
		
	}
	
	
	/**
	 * Gets the bind id attribute value from a question variable name.
	 * 
	 * @param variableName the question variable name.
	 * @param isRepeatKid set to true if the question is a child of some other repeat question type.
	 * @return the binding id attribute value.
	 */
	public static String getBindIdFromVariableName(String variableName, boolean isRepeatKid){
		String id = variableName;

		if(!isRepeatKid && variableName.contains("/")){
			if(variableName.indexOf('/') == variableName.lastIndexOf('/'))
				id = variableName.substring(variableName.lastIndexOf('/')+1); //as one / eg encounter/encounter.encounter_datetime
			else
				id = variableName.substring(variableName.indexOf('/')+1,variableName.lastIndexOf('/')); //has two / eg obs/method_of_delivery/value
		}

		return id;
	}
	
	/**
	 * Gets the xpath expression operator (e.g =,!=,>) from an operator constant (e.g 1,2,3).
	 * 
	 * @param operator the operator constant.
	 * @param action the skip or validation rule target action.
	 * @return the xpath expression operator.
	 */
	public static String getXpathOperator(int operator, int action){
		if(operator == ModelConstants.OPERATOR_EQUAL)
			return "=";
		else if(operator == ModelConstants.OPERATOR_NOT_EQUAL)
			return "!=";
		else if(operator == ModelConstants.OPERATOR_LESS)
			return "<";
		else if(operator == ModelConstants.OPERATOR_GREATER)
			return ">";
		else if(operator == ModelConstants.OPERATOR_LESS_EQUAL)
			return "<=";
		else if(operator == ModelConstants.OPERATOR_GREATER_EQUAL)
			return ">=";
		else if(operator == ModelConstants.OPERATOR_IS_NOT_NULL)
			return "!=";
		else if(operator == ModelConstants.OPERATOR_IS_NULL)
			return "=";
		
		return "=";
	}


	/**
	 * Checks if a skip rule target action is in the positive or negative sense.
	 * 
	 * @param action the target action.
	 * @return true if positive, else false.
	 */
	public static boolean isPositiveAction(int action){
		return ((action & ModelConstants.ACTION_ENABLE) != 0) || ((action & ModelConstants.ACTION_SHOW) != 0);
	}
}
