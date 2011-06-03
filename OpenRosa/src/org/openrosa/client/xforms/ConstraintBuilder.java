package org.openrosa.client.xforms;

import java.util.Vector;

import org.openrosa.client.model.Condition;
import org.openrosa.client.model.FormDef;
import org.openrosa.client.model.IFormElement;
import org.openrosa.client.model.QuestionDef;
import org.openrosa.client.model.SkipRule;
import org.openrosa.client.model.ValidationRule;
import org.openrosa.client.model.ModelConstants;
import org.openrosa.client.xforms.XformConstants;

import com.google.gwt.xml.client.Element;


/**
 * Builds constraint attributes of xforms documents from validation rule definition objects.
 * 
 * @author daniel
 *
 */
public class ConstraintBuilder {

	/**
	 * All methods in this class are static and hence we expect no external
	 * Instantiation of this class.
	 */
	private ConstraintBuilder(){

	}
	
	/**
	 * A slightly higher level function that figures out
	 * if the question associated with this rule has
	 * an 'advanced' relevant or if it should be extracted
	 * out of the SkipRule in the traditional way (for less
	 * complex relevants)
	 * @param rule The SkipRule associated with the specified skip rule.
	 * @param qID The internal ID for the question for which the relevant will be generated
	 * @param formDef
	 * @return
	 */
	public static String getConstraintFromRule(ValidationRule rule, int qID, FormDef formDef){
		IFormElement elementDef = formDef.getElement(qID);
		String constraint = "";
		if(elementDef.hasAdvancedConstraint()){
			constraint = elementDef.getAdvancedConstraint();
			if(constraint != null){
				return constraint;
			}else{
				return "";
			}
		}
		
		
		Vector conditions  = rule.getConditions();
		if(conditions == null || conditions.size() == 0){
			return null;
		}
		for(int i=0; i<conditions.size(); i++){
			if(constraint.length() > 0){
				constraint += XformBuilderUtil.getConditionsOperatorText(rule.getConditionsOperator());
			}
			constraint += fromValidationRuleCondition2Xform((Condition)conditions.elementAt(i),formDef,rule.getConditionsOperator());
		}
		
		
		return constraint;
	}
	
	/**
	 * Converts a validation rule to its xforms representation.
	 * The question definition object referenced by the validation rule
	 * is expected to already have reference to the xforms document nodes
	 * that we need to manipulate for the validation rule's xforms contents.
	 * 
	 * @param rule the validation rule.
	 * @param formDef the form definition object.
	 */
	public static void fromValidationRule2Xform(ValidationRule rule, FormDef formDef){
		QuestionDef questionDef = formDef.getQuestion(rule.getQuestionId());

		if(questionDef == null){
			formDef.removeValidationRule(rule);
			return; //possibly question deleted.
		}

		Element node = questionDef.getBindNode();
		if(node == null)
			node = questionDef.getControlNode();


		String constraint = getConstraintFromRule(rule,questionDef.getId(),formDef);
//		constraint = constraint.replace(">", "&gt;").replace("<", "&lt;");
		if(constraint == null){
			node.removeAttribute(XformConstants.ATTRIBUTE_NAME_CONSTRAINT);
			node.removeAttribute(XformConstants.ATTRIBUTE_NAME_CONSTRAINT_MESSAGE);
			return;
		}
//		constraint = XmlUtil.escapeXMLAttribute(constraint);
		node.setAttribute(XformConstants.ATTRIBUTE_NAME_CONSTRAINT, constraint);
		node.setAttribute(XformConstants.ATTRIBUTE_NAME_CONSTRAINT_MESSAGE, rule.getErrorMessage());
	}
	
	/**
	 * Takes in a SkipRule and returns the completed Relevant
	 * attribute value
	 * @param rule
	 * @param formDef - the underlying formdef for this mess.
	 * @return - The reconstructed relevant attribute.
	 */
	public static String fromValidationRule2String(ValidationRule rule, FormDef formDef){
		String constraint = "";
		if(rule == null){
			return constraint;
		}
		Vector conditions  = rule.getConditions();
		if(conditions == null){
			return null;
		}
		for(int i=0; i<conditions.size(); i++){
			if(constraint.length() > 0){
				constraint += XformBuilderUtil.getConditionsOperatorText(rule.getConditionsOperator());
			}
			constraint += fromValidationRuleCondition2Xform((Condition)conditions.elementAt(i),
															formDef,
															rule.getConditionsOperator());
		}
		return constraint;
	}
	
	/**
	 * Creates an xforms representation of a validation rule condition.
	 * 
	 * @param condition the condition object.
	 * @param formDef the form definition object to which the skip rule belongs.
	 * @param action the validation rule action.
	 * @param actionQtnDef the question referenced by the validation rule.
	 * @return the condition xforms representation.
	 */
	private static String fromValidationRuleCondition2Xform(Condition condition, FormDef formDef, int action){
		String constraint = null;

		QuestionDef questionDef = formDef.getQuestion(condition.getQuestionId());
		if(questionDef != null){			
			String value = " '" + condition.getValue() + "'";
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_BOOLEAN || questionDef.getDataType() == QuestionDef.QTN_TYPE_DECIMAL || 
					questionDef.getDataType() == QuestionDef.QTN_TYPE_NUMERIC || questionDef.getDataType() == QuestionDef.QTN_TYPE_LONG ||
					questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT || condition.getFunction() == ModelConstants.FUNCTION_LENGTH ||
					condition.getValue().endsWith("()"))
				value = " " + condition.getValue();

			constraint = ". ";
			//if(actionQtnDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
			//	constraint = "count(.) ";
			if(condition.getFunction() == ModelConstants.FUNCTION_LENGTH)
				constraint = "length(.) ";
			
			if(condition.getOperator() == ModelConstants.OPERATOR_BETWEEN){
				constraint += XformBuilderUtil.getXpathOperator(ModelConstants.OPERATOR_GREATER,action)+value + " and "+ "." + XformBuilderUtil.getXpathOperator( ModelConstants.OPERATOR_LESS,action)+ condition.getSecondValue();
			}else if(condition.getOperator() == ModelConstants.OPERATOR_NOT_BETWEEN){
				constraint +=XformBuilderUtil.getXpathOperator(ModelConstants.OPERATOR_GREATER,action)+condition.getSecondValue() + " or "+ "." + XformBuilderUtil.getXpathOperator( ModelConstants.OPERATOR_LESS,action)+value ;
			}else if (condition.getOperator() == ModelConstants.OPERATOR_STARTS_WITH){
				constraint += "starts-with(.,"+ value+")"; 
			}else if (condition.getOperator() == ModelConstants.OPERATOR_NOT_START_WITH){
				constraint += "not(starts-with(.,"+ value+"))";
			}else if (condition.getOperator() == ModelConstants.OPERATOR_CONTAINS){
				constraint += "contains(.,"+ value+")";
			}else if (condition.getOperator() == ModelConstants.OPERATOR_NOT_CONTAIN){
				constraint += "not(contains(.,"+ value+"))";
			}else{
				constraint += XformBuilderUtil.getXpathOperator(condition.getOperator(),action)+value;
			}
		}
		return constraint;
	}
}
