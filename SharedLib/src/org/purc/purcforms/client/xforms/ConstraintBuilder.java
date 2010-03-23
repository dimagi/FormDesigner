package org.purc.purcforms.client.xforms;

import java.util.Vector;

import org.purc.purcforms.client.model.Condition;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.ModelConstants;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.model.ValidationRule;

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

		Vector conditions  = rule.getConditions();
		if(conditions == null || conditions.size() == 0){
			node.removeAttribute(XformConstants.ATTRIBUTE_NAME_CONSTRAINT);
			node.removeAttribute(XformConstants.ATTRIBUTE_NAME_CONSTRAINT_MESSAGE);
			return;
		}

		String constratint = "";
		for(int i=0; i<conditions.size(); i++){
			if(constratint.length() > 0)
				constratint += XformBuilderUtil.getConditionsOperatorText(rule.getConditionsOperator());
			constratint += fromValidationRuleCondition2Xform((Condition)conditions.elementAt(i),formDef,ModelConstants.ACTION_ENABLE,questionDef);
		}

		node.setAttribute(XformConstants.ATTRIBUTE_NAME_CONSTRAINT, constratint);
		node.setAttribute(XformConstants.ATTRIBUTE_NAME_CONSTRAINT_MESSAGE, rule.getErrorMessage());
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
	private static String fromValidationRuleCondition2Xform(Condition condition, FormDef formDef, int action, QuestionDef actionQtnDef){
		String constraint = null;

		QuestionDef questionDef = formDef.getQuestion(condition.getQuestionId());
		if(questionDef != null){			
			String value = " '" + condition.getValue() + "'";
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_BOOLEAN || questionDef.getDataType() == QuestionDef.QTN_TYPE_DECIMAL || questionDef.getDataType() == QuestionDef.QTN_TYPE_NUMERIC || 
					questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT || condition.getFunction() == ModelConstants.FUNCTION_LENGTH ||
					condition.getValue().endsWith("()"))
				value = " " + condition.getValue();

			constraint = ". ";
			//if(actionQtnDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
			//	constraint = "count(.) ";
			if(condition.getFunction() == ModelConstants.FUNCTION_LENGTH)
				constraint = "length(.) ";

			constraint += XformBuilderUtil.getXpathOperator(condition.getOperator(),action)+value;
		}
		return constraint;
	}
}
