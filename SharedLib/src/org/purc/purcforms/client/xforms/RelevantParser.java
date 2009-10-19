package org.purc.purcforms.client.xforms;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.purc.purcforms.client.model.Condition;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.ModelConstants;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.model.SkipRule;


/**
 * Parses relevant attributes of xforms documents and builds the skip rule objects of the model.
 * 
 * @author daniel
 *
 */
public class RelevantParser {

	/**
	 * All methods in this class are static and hence we expect no external
	 * Instantiation of this class.
	 */
	private RelevantParser(){

	}


	/**
	 * Builds skip rule object from a list of relevant attribute values.
	 * 
	 * @param formDef the form defintion object to which the skip rules belong.
	 * @param relevants the map of relevant attribute values keyed by their 
	 * 					  question definition objects.
	 */
	public static void addSkipRules(FormDef formDef, HashMap relevants){
		Vector rules = new Vector();

		HashMap<String,SkipRule> skipRulesMap = new HashMap<String,SkipRule>();

		Iterator keys = relevants.keySet().iterator();
		int id = 0;
		while(keys.hasNext()){
			QuestionDef qtn = (QuestionDef)keys.next();
			String relevant = (String)relevants.get(qtn);

			//If there is a skip rule with the same relevant as the current
			//then just add this question as another action target to the skip
			//rule instead of creating a new skip rule.
			SkipRule skipRule = skipRulesMap.get(relevant);
			if(skipRule != null)
				skipRule.addActionTarget(qtn.getId());
			else{
				skipRule = buildSkipRule(formDef, qtn.getId(),relevant,++id,XformParserUtil.getAction(qtn));
				if(skipRule != null){
					rules.add(skipRule);
					skipRulesMap.put(relevant, skipRule);
				}
			}
		}

		formDef.setSkipRules(rules);
	}


	/**
	 * Creates a skip rule object from a relevent attribute value.
	 * 
	 * @param formDef the form definition object to build the skip rule for.
	 * @param questionId the identifier of the question which is the target of the skip rule.
	 * @param relevant the relevant attribute value.
	 * @param id the identifier for the skip rule.
	 * @param action the skip rule action to apply to the above target question.
	 * @return the skip rule object.
	 */
	private static SkipRule buildSkipRule(FormDef formDef, int questionId, String relevant, int id, int action){

		SkipRule skipRule = new SkipRule();
		skipRule.setId(id);
		//TODO For now we are only dealing with enabling and disabling.
		skipRule.setAction(action);
		skipRule.setConditions(getSkipRuleConditions(formDef,relevant,action));
		skipRule.setConditionsOperator(XformParserUtil.getConditionsOperator(relevant));

		//For now we only have one action target, much as the object model is
		//flexible enough to support any number of them.
		Vector actionTargets = new Vector();
		actionTargets.add(new Integer(questionId));
		skipRule.setActionTargets(actionTargets);

		// If skip rule has no conditions, then its as good as no skip rule at all.
		if(skipRule.getConditions() == null || skipRule.getConditions().size() == 0)
			return null;
		return skipRule;
	}


	/**
	 * Gets a list of conditions for a skip rule as per the relevant attribute value.
	 * 
	 * @param formDef the form definition object to which the skip rule belongs.
	 * @param relevant the relevant attribute value.
	 * @param action the skip rule target action.
	 * @return the conditions list.
	 */
	private static Vector getSkipRuleConditions(FormDef formDef, String relevant, int action){
		Vector conditions = new Vector();

		Vector list = XpathParser.getConditionsOperatorTokens(relevant);

		Condition condition  = new Condition();
		for(int i=0; i<list.size(); i++){
			condition = getSkipRuleCondition(formDef,(String)list.elementAt(i),(int)(i+1),action);
			if(condition != null)
				conditions.add(condition);
		}

		return conditions;
	}


	/**
	 * Creates a skip rule condition object from a portion of the relevant attribute value.
	 * 
	 * @param formDef the form definition object to which the skip rule belongs.
	 * @param relevant the token or portion from the relevant attribute value.
	 * @param id the new condition identifier.
	 * @param action the skip rule target action.
	 * @return the new condition object.
	 */
	private static Condition getSkipRuleCondition(FormDef formDef, String relevant, int id, int action){		
		Condition condition  = new Condition();
		condition.setId(id);
		condition.setOperator(XformParserUtil.getOperator(relevant,action));

		//eg relevant="/data/question10='7'"
		int pos = XformParserUtil.getOperatorPos(relevant);
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
			value = relevant.substring(pos+XformParserUtil.getOperatorSize(condition.getOperator(),action),relevant.length());

		value = value.trim();
		if(!(value.equals("null") || value.equals(""))){
			condition.setValue(value);

			//This is just for the designer
			if(value.startsWith(formDef.getVariableName() + "/"))
				condition.setValueQtnDef(formDef.getQuestion(value.substring(value.indexOf('/')+1)));

			if(condition.getOperator() == ModelConstants.OPERATOR_NULL)
				return null; //no operator set hence making the condition invalid
		}
		else
			condition.setOperator(ModelConstants.OPERATOR_IS_NULL);

		return condition;
	}
}
