package org.openrosa.client.xforms;

import java.util.Vector;

import org.openrosa.client.model.Condition;
import org.openrosa.client.model.FormDef;
import org.openrosa.client.model.QuestionDef;
import org.openrosa.client.model.SkipRule;
import org.purc.purcforms.client.model.ModelConstants;
import org.purc.purcforms.client.xforms.XformBuilderUtil;
import org.purc.purcforms.client.xforms.XformConstants;

import com.google.gwt.xml.client.Element;


/**
 * Builds relevant attributes of xforms documents from skip rule definition objects.
 * 
 * @author daniel
 *
 */
public class RelevantBuilder {

	/**
	 * All methods in this class are static and hence we expect no external
	 * Instantiation of this class.
	 */
	private RelevantBuilder(){

	}
	
	
	/**
	 * Converts a skip rule definition object to xforms.
	 * 
	 * @param rule the skip rule definition object
	 * @param formDef the form definition.
	 */
	public static void fromSkipRule2Xform(SkipRule rule, FormDef formDef){
		String relevant = "";
		Vector conditions  = rule.getConditions();
		for(int i=0; i<conditions.size(); i++){
			if(relevant.length() > 0)
				relevant += XformBuilderUtil.getConditionsOperatorText(rule.getConditionsOperator());
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

			if(relevant.trim().length() == 0){
				node.removeAttribute(XformConstants.ATTRIBUTE_NAME_RELEVANT);
				node.removeAttribute(XformConstants.ATTRIBUTE_NAME_ACTION);
				node.removeAttribute(XformConstants.ATTRIBUTE_NAME_REQUIRED);
			}
			else{
				node.setAttribute(XformConstants.ATTRIBUTE_NAME_RELEVANT, relevant);

				String value = XformConstants.ATTRIBUTE_VALUE_ENABLE;
				if((rule.getAction() & ModelConstants.ACTION_ENABLE) != 0)
					value = XformConstants.ATTRIBUTE_VALUE_ENABLE;
				else if((rule.getAction() & ModelConstants.ACTION_DISABLE) != 0)
					value = XformConstants.ATTRIBUTE_VALUE_DISABLE;
				else if((rule.getAction() & ModelConstants.ACTION_SHOW) != 0)
					value = XformConstants.ATTRIBUTE_VALUE_SHOW;
				else if((rule.getAction() & ModelConstants.ACTION_HIDE) != 0)
					value = XformConstants.ATTRIBUTE_VALUE_HIDE;
				node.setAttribute(XformConstants.ATTRIBUTE_NAME_ACTION, value);

				if((rule.getAction() & ModelConstants.ACTION_MAKE_MANDATORY) != 0)
					value = XformConstants.XPATH_VALUE_TRUE;
				else if((rule.getAction() & ModelConstants.ACTION_MAKE_OPTIONAL) != 0)
					value = XformConstants.XPATH_VALUE_FALSE;
				//node.setAttribute(XformConstants.ATTRIBUTE_NAME_REQUIRED, value);
			}
		}
	}
	
	
	/**
	 * Creates an xforms representation of a skip rule condition.
	 * 
	 * @param condition the condition object.
	 * @param formDef the form definition object to which the skip rule belongs.
	 * @param action the skip rule action to its target questions.
	 * @return the condition xforms representation.
	 */
	private static String fromSkipCondition2Xform(Condition condition, FormDef formDef, int action){
		String relevant = null;

		QuestionDef questionDef = formDef.getQuestion(condition.getQuestionId());
		if(questionDef != null){
			relevant = questionDef.getBinding();
			if(!relevant.contains(formDef.getVariableName()))
				relevant = "/" + formDef.getVariableName() + "/" + questionDef.getBinding();

			String value = " '" + condition.getValue() + "'";
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_BOOLEAN || questionDef.getDataType() == QuestionDef.QTN_TYPE_DECIMAL || questionDef.getDataType() == QuestionDef.QTN_TYPE_NUMERIC)
				value = " " + condition.getValue();

			relevant += " " + XformBuilderUtil.getXpathOperator(condition.getOperator(),action)+value;
		}
		return relevant;
	}
}
