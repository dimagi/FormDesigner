package org.purc.purcforms.client.xforms;

import java.util.HashMap;
import java.util.Iterator;

import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.ModelConstants;
import org.purc.purcforms.client.model.QuestionDef;

import com.google.gwt.xml.client.Element;


/**
 * Utility methods used during the parsing of xforms documents.
 * 
 * @author daniel
 *
 */
public class XformParserUtil {

	/**
	 * All methods in this class are static and hence we expect no external
	 * Instantiation of this class.
	 */
	private XformParserUtil(){

	}
	
	
	/**
	 * Converts an xpath operator in a relevant or constraint expression to its 
	 * corresponding constant in our object model.
	 * 
	 * @param expression the xpath expression.
	 * @param action the skip or validation rule action to the target questions.
	 * @return the operator constant.
	 */
	//TODO Add the other xpath operators
	public static int getOperator(String expression, int action){
		//We return the operator which is the opposite of the expression
		if(expression.indexOf(">=") > 0 || expression.indexOf("&gt;=") > 0){
			if(XformBuilderUtil.isPositiveAction(action))
				return ModelConstants.OPERATOR_GREATER_EQUAL;
			return ModelConstants.OPERATOR_LESS;
		}
		else if(expression.indexOf('>') > 0 || expression.indexOf("&gt;") > 0){
			if(XformBuilderUtil.isPositiveAction(action))
				return ModelConstants.OPERATOR_GREATER;
			return ModelConstants.OPERATOR_LESS_EQUAL;
		}
		else if(expression.indexOf("<=") > 0 || expression.indexOf("&lt;=") > 0){
			if(XformBuilderUtil.isPositiveAction(action))
				return ModelConstants.OPERATOR_LESS_EQUAL;
			return ModelConstants.OPERATOR_GREATER;
		}
		else if(expression.indexOf('<') > 0 || expression.indexOf("&lt;") > 0){
			if(XformBuilderUtil.isPositiveAction(action))
				return ModelConstants.OPERATOR_LESS;
			return ModelConstants.OPERATOR_GREATER_EQUAL;
		}
		else if(expression.indexOf("!=") > 0 || expression.indexOf("!=") > 0){
			if(XformBuilderUtil.isPositiveAction(action))
				return ModelConstants.OPERATOR_NOT_EQUAL;
			return ModelConstants.OPERATOR_EQUAL;
		}
		else if(expression.indexOf('=') > 0){
			if(XformBuilderUtil.isPositiveAction(action))
				return ModelConstants.OPERATOR_EQUAL;
			return ModelConstants.OPERATOR_NOT_EQUAL;
		}
		else if(expression.indexOf("not(starts-with") > 0){
			if(XformBuilderUtil.isPositiveAction(action))
				return ModelConstants.OPERATOR_NOT_START_WITH;
			return ModelConstants.OPERATOR_STARTS_WITH;
		}
		else if(expression.indexOf("starts-with") > 0){
			if(XformBuilderUtil.isPositiveAction(action))
				return ModelConstants.OPERATOR_STARTS_WITH;
			return ModelConstants.OPERATOR_NOT_START_WITH;
		}
		else if(expression.indexOf("not(contains") > 0){
			if(XformBuilderUtil.isPositiveAction(action))
				return ModelConstants.OPERATOR_NOT_CONTAIN;
			return ModelConstants.OPERATOR_CONTAINS;
		}
		else if(expression.indexOf("contains") > 0){
			if(XformBuilderUtil.isPositiveAction(action))
				return ModelConstants.OPERATOR_CONTAINS;
			return ModelConstants.OPERATOR_NOT_CONTAIN;
		}

		return ModelConstants.OPERATOR_NULL;
	}

	
	/**
	 * Gets the xpath operator size of an operator constant.
	 * 
	 * @param operator the operator constant.
	 * @param action the skip or validation rule target action.
	 * @return the xpath operator size.
	 */
	public static int getOperatorSize(int operator, int action){
		if(operator == ModelConstants.OPERATOR_GREATER_EQUAL || 
				operator == ModelConstants.OPERATOR_LESS_EQUAL ||
				operator == ModelConstants.OPERATOR_NOT_EQUAL)
			return XformBuilderUtil.isPositiveAction(action) ? 2 : 1;
		else if(operator == ModelConstants.OPERATOR_LESS ||
				operator == ModelConstants.OPERATOR_GREATER || 
				operator == ModelConstants.OPERATOR_EQUAL)
			return XformBuilderUtil.isPositiveAction(action) ? 1 : 2;

		return 0;
	}

	
	/**
	 * Gets the position of an operator in an xpath expression.
	 * 
	 * @param expression the xpath expression.
	 * @return the operator start position or index.
	 */
	public static int getOperatorPos(String expression){
		//Using lastindexof because of expressions like:
		//relevant="/ClinicalData/SubjectData/StudyEventData/FormData/ItemGroupData/ItemData[@ItemOID='I_REVI_IMPROVEMENT']/@Value = '1'"
		int pos = expression.lastIndexOf("!=");
		if(pos > 0)
			return pos;

		pos = expression.lastIndexOf(">=");
		if(pos > 0)
			return pos;

		pos = expression.lastIndexOf("<=");
		if(pos > 0)
			return pos;

		pos = expression.lastIndexOf('>');
		if(pos > 0)
			return pos;

		pos = expression.lastIndexOf('<');
		if(pos > 0)
			return pos;

		pos = expression.lastIndexOf('=');
		if(pos > 0)
			return pos;
		
		//the order of the code below should not be changed as for example 'starts with' can be taken
		//even when condition is 'not(starts-with'
		
		pos = expression.lastIndexOf("not(starts-with");
		if(pos > 0)
			return pos;
		
		pos = expression.lastIndexOf("starts-with");
		if(pos > 0)
			return pos;
		
		pos = expression.lastIndexOf("not(contains");
		if(pos > 0)
			return pos;
		
		pos = expression.lastIndexOf("contains");
		if(pos > 0)
			return pos;

		return pos;
	}

	
	/**
	 * Gets the question variable name without the form prefix (/newform1/)
	 * 
	 * @param bindNode the xforms bind node.
	 * @param formDef the form to which the question belongs.
	 * @return the question variable name.
	 */
	public static String getQuestionVariableName(Element bindNode, FormDef formDef){
		String name = bindNode.getAttribute(XformConstants.ATTRIBUTE_NAME_NODESET);

		if(name.startsWith("/"+formDef.getBinding()+"/"))
			name = name.replace("/"+formDef.getBinding()+"/", "");
		
		return name;
	}
	
	
	/**
	 * Sets the question definition object data type based on an xml xsd type.
	 * 
	 * @param def the question definition object.
	 * @param type the xml xsd type.
	 * @param node the xforms node having the type attribute.
	 */
	public static void setQuestionType(QuestionDef def, String type, Element node){
		if(type != null){
			if(type.equals(XformConstants.DATA_TYPE_TEXT) || type.indexOf("string") != -1 ){
				String format = node.getAttribute(XformConstants.ATTRIBUTE_NAME_FORMAT);
				if(XformConstants.ATTRIBUTE_VALUE_GPS.equals(format))
					def.setDataType(QuestionDef.QTN_TYPE_GPS);
				else
					def.setDataType(QuestionDef.QTN_TYPE_TEXT);
			}
			else if((type.equals("xsd:integer") || type.equals(XformConstants.DATA_TYPE_INT)) || (type.indexOf("integer") != -1 || (type.indexOf("int") != -1) && !type.equals("geopoint") ))
				def.setDataType(QuestionDef.QTN_TYPE_NUMERIC);
			else if(type.equals("xsd:decimal") || type.indexOf("decimal") != -1 )
				def.setDataType(QuestionDef.QTN_TYPE_DECIMAL);
			else if(type.equals("xsd:dateTime") || type.indexOf("dateTime") != -1 )
				def.setDataType(QuestionDef.QTN_TYPE_DATE_TIME);
			else if(type.equals("xsd:time") || type.indexOf("time") != -1 )
				def.setDataType(QuestionDef.QTN_TYPE_TIME);
			else if(type.equals(XformConstants.DATA_TYPE_DATE) || type.indexOf("date") != -1 )
				def.setDataType(QuestionDef.QTN_TYPE_DATE);
			else if(type.equals(XformConstants.DATA_TYPE_BOOLEAN) || type.indexOf("boolean") != -1 )
				def.setDataType(QuestionDef.QTN_TYPE_BOOLEAN);
			else if(type.equals(XformConstants.DATA_TYPE_BINARY) || type.indexOf("base64Binary") != -1 ){
				String format = node.getAttribute(XformConstants.ATTRIBUTE_NAME_FORMAT);
				if(XformConstants.ATTRIBUTE_VALUE_VIDEO.equals(format))
					def.setDataType(QuestionDef.QTN_TYPE_VIDEO);
				else if(XformConstants.ATTRIBUTE_VALUE_AUDIO.equals(format))
					def.setDataType(QuestionDef.QTN_TYPE_AUDIO);
				else
					def.setDataType(QuestionDef.QTN_TYPE_IMAGE);
			}
			//TODO These two are used by ODK
			else if(type.equalsIgnoreCase("binary")){
				def.setDataType(QuestionDef.QTN_TYPE_IMAGE);
			}
			else if(type.equalsIgnoreCase("geopoint"))
				def.setDataType(QuestionDef.QTN_TYPE_GPS);
			else if(type.equalsIgnoreCase("barcode"))
				def.setDataType(QuestionDef.QTN_TYPE_BARCODE);
		}
		else
			def.setDataType(QuestionDef.QTN_TYPE_TEXT); //QTN_TYPE_REPEAT
	}
	
	
	/**
	 * Goes through a given map of constraints attribute vaues and replaces the question 
	 * whose variable name matches with a given question.
	 * 
	 * @param constraints a map of contraints attribute values keyed by their question 
	 * 					  definition objects.
	 * @param questionDef the question definition object to replace that in the constraint map.
	 */
	public static void replaceConstraintQtn(HashMap constraints, QuestionDef questionDef){
		Iterator keys = constraints.keySet().iterator();
		while(keys.hasNext()){
			QuestionDef qtn = (QuestionDef)keys.next();
			if(qtn.getBinding().equals(questionDef.getBinding())){
				String constraint = (String)constraints.get(qtn);
				if(constraint != null){
					constraints.remove(qtn);
					constraints.put(questionDef, constraint);
				}
				return;
			}
		}
	}
	
	
	/**
	 * Gets the skip rule action for a question.
	 * 
	 * @param qtn the question definition object.
	 * @return the skip rule action which can be (ModelConstants.ACTION_DISABLE,
	 *         ModelConstants.ACTION_HIDE,ModelConstants.ACTION_SHOW or ModelConstants.ACTION_ENABLE)
	 */
	public static int getAction(QuestionDef qtn){
		Element node = qtn.getBindNode();
		if(node == null)
			return ModelConstants.ACTION_DISABLE;

		String value = node.getAttribute(XformConstants.ATTRIBUTE_NAME_ACTION);
		if(value == null)
			return ModelConstants.ACTION_DISABLE;

		int action = 0;
		if(value.equalsIgnoreCase(XformConstants.ATTRIBUTE_VALUE_ENABLE))
			action |= ModelConstants.ACTION_ENABLE;
		else if(value.equalsIgnoreCase(XformConstants.ATTRIBUTE_VALUE_DISABLE))
			action |= ModelConstants.ACTION_DISABLE;
		else if(value.equalsIgnoreCase(XformConstants.ATTRIBUTE_VALUE_SHOW))
			action |= ModelConstants.ACTION_SHOW;
		else if(value.equalsIgnoreCase(XformConstants.ATTRIBUTE_VALUE_HIDE))
			action |= ModelConstants.ACTION_HIDE;

		value = node.getAttribute(XformConstants.ATTRIBUTE_NAME_REQUIRED);
		if(XformConstants.XPATH_VALUE_TRUE.equalsIgnoreCase(value))
			action |= ModelConstants.ACTION_MAKE_MANDATORY;
		else 
			action |= ModelConstants.ACTION_MAKE_OPTIONAL;

		return action;
	}
	
	
	/**
	 * Gets the operator constant used to combine conditions in an expath expression.
	 * This will either be and AND or OR
	 * For now, we do not allow a mixture of these operators in the same expression.
	 * But we allow more than one as long as it is of the same type, either AND or OR.
	 * 
	 * @param expression the xpath expression.
	 * @return the operator constant.
	 */
	public static int getConditionsOperator(String expression){
		if(expression.toLowerCase().indexOf(XformConstants.CONDITIONS_OPERATOR_TEXT_AND) > 0)
			return ModelConstants.CONDITIONS_OPERATOR_AND;
		else if(expression.toLowerCase().indexOf(XformConstants.CONDITIONS_OPERATOR_TEXT_OR) > 0)
			return ModelConstants.CONDITIONS_OPERATOR_OR;
		return ModelConstants.CONDITIONS_OPERATOR_NULL;
	}
}
