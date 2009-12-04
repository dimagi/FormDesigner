package org.purc.purcforms.client.xforms;

import java.util.Vector;

import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.ModelConstants;
import org.purc.purcforms.client.model.QuestionDef;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;


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
			return "xsd:dateTime";
		case QuestionDef.QTN_TYPE_TIME:
			return "xsd:time";
		case QuestionDef.QTN_TYPE_DECIMAL:
			return "xsd:decimal";
		case QuestionDef.QTN_TYPE_NUMERIC:
			return XformConstants.DATA_TYPE_INT;
		case QuestionDef.QTN_TYPE_TEXT:
		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
		case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC:
		case QuestionDef.QTN_TYPE_GPS:
			return XformConstants.DATA_TYPE_TEXT;
		case QuestionDef.QTN_TYPE_IMAGE:
		case QuestionDef.QTN_TYPE_VIDEO:
		case QuestionDef.QTN_TYPE_AUDIO:
			return XformConstants.DATA_TYPE_BINARY;
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
	 * @param formNode the xforms instance data node.
	 * @return the instance data child node for the question.
	 */
	public static Element fromVariableName2Node(Document doc, String variableName,FormDef formDef,Element formNode){
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
			return isPositiveAction(action) ? "=" : "!=";
		else if(operator == ModelConstants.OPERATOR_NOT_EQUAL)
			return isPositiveAction(action) ? "!=" : "=";
		else if(operator == ModelConstants.OPERATOR_LESS)
			return isPositiveAction(action) ? "<" : ">=";
		else if(operator == ModelConstants.OPERATOR_GREATER)
			return isPositiveAction(action) ? ">" : "<=";
		else if(operator == ModelConstants.OPERATOR_LESS_EQUAL)
			return isPositiveAction(action) ? "<=" : ">";
		else if(operator == ModelConstants.OPERATOR_GREATER_EQUAL)
			return isPositiveAction(action) ? ">=" : "<";
		else if(operator == ModelConstants.OPERATOR_IS_NOT_NULL)
			return isPositiveAction(action) ? "!=" : "=";
		else if(operator == ModelConstants.OPERATOR_IS_NULL)
			return isPositiveAction(action) ? "=" : "!=";
		
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
