package org.purc.purcforms.client.model;

import java.io.Serializable;
import java.util.Vector;

import org.purc.purcforms.client.xforms.XformConverter;


/**
 * 
 * @author daniel
 *
 */
public class ValidationRule implements Serializable{
	
	/** The unique identifier of the question referenced by this condition. */
	private int questionId = PurcConstants.NULL_ID;
	
	/** A list of conditions (Condition object) to be tested for a rule. 
	 * E.g. age is greater than 4. etc
	 */
	private Vector conditions;
	
	
	/** The validation rule name. */
	private String errorMessage;
	
	/** Operator for combining more than one condition. (And, Or) only these two for now. */
	private int conditionsOperator = PurcConstants.CONDITIONS_OPERATOR_NULL;
	
	
	private FormDef formDef;
	
		
	/** Constructs a rule object ready to be initialized. */
	public ValidationRule(FormDef formDef){
		this.formDef = formDef;
	}
	
	public ValidationRule(int questionId, FormDef formDef){
		this.questionId = questionId;
		this.formDef = formDef;
	}
	
	/** Copy constructor. */
	public ValidationRule(ValidationRule validationRule){
		setQuestionId(validationRule.getQuestionId());
		setErrorMessage(validationRule.getErrorMessage());
		setConditionsOperator(validationRule.getConditionsOperator());
		copyConditions(validationRule.getConditions());
		setFormDef(new FormDef(validationRule.getFormDef()));
	}
	
	/** Construct a Rule object from parameters. 
	 * 
	 * @param ruleId 
	 * @param conditions 
	 * @param action
	 * @param actionTargets
	 */
	public ValidationRule(int questionId, Vector conditions , String errorMessage) {
		setQuestionId(questionId);
		setConditions(conditions);
		setErrorMessage(errorMessage);
	}

	public Vector getConditions() {
		return conditions;
	}

	public void setConditions(Vector conditions) {
		this.conditions = conditions;
	}

	public int getQuestionId() {
		return questionId;
	}

	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}
	
	public int getConditionsOperator() {
		return conditionsOperator;
	}

	public void setConditionsOperator(int conditionsOperator) {
		this.conditionsOperator = conditionsOperator;
	}
	
	public Condition getConditionAt(int index) {
		if(conditions == null)
			return null;
		return (Condition)conditions.elementAt(index);
	}
	
	public int getConditionCount() {
		if(conditions == null)
			return 0;
		return conditions.size();
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public FormDef getFormDef() {
		return formDef;
	}

	public void setFormDef(FormDef formDef) {
		this.formDef = formDef;
	}

	public void addCondition(Condition condition){
		if(conditions == null)
			conditions = new Vector();
		conditions.add(condition);
	}
	
	public boolean containsCondition(Condition condition){
		if(conditions == null)
			return false;
		return conditions.contains(condition);
	}
	
	public void updateCondition(Condition condition){
		for(int i=0; i<conditions.size(); i++){
			Condition cond = (Condition)conditions.elementAt(i);
			if(cond.getId() == condition.getId()){
				conditions.remove(i);
				conditions.add(condition);
				break;
			}
		}
	}
	
	public void removeCondition(Condition condition){
		conditions.remove(condition);
	}
	
	public boolean isValid(){
		return isValid(formDef);
	}

	/** 
	 * Checks conditions of a rule and executes the corresponding actions
	 * 
	 * @param data
	 */
	public boolean isValid(FormDef formDef){
		boolean trueFound = false, falseFound = false;
		
		for(int i=0; i<getConditions().size(); i++){
			Condition condition = (Condition)this.getConditions().elementAt(i);
			if(condition.isTrue(formDef))
				trueFound = true;
			else
				falseFound = true;
		}
		
		if(getConditions().size() == 1 || getConditionsOperator() == PurcConstants.CONDITIONS_OPERATOR_AND)
			return !falseFound;
		else if(getConditionsOperator() == PurcConstants.CONDITIONS_OPERATOR_OR)
			return trueFound;
		
		return false;
	}
	
	private void copyConditions(Vector conditions){
		this.conditions = new Vector();
		for(int i=0; i<conditions.size(); i++)
			this.conditions.addElement(new Condition((Condition)conditions.elementAt(i)));
	}
	
	public void updateDoc(FormDef formDef){
		XformConverter.fromValidationRule2Xform(this,formDef);
	}
	
	public void refresh(FormDef dstFormDef, FormDef srcFormDef){
		ValidationRule validationRule = null;
		
		for(int index = 0; index < this.getConditionCount(); index++){
			Condition condition = getConditionAt(index);
			QuestionDef qtn = srcFormDef.getQuestion(condition.getQuestionId());
			if(qtn == null)
				continue;
			QuestionDef questionDef = dstFormDef.getQuestion(qtn.getVariableName());
			if(questionDef == null)
				continue;
			if(validationRule == null)
				validationRule = new ValidationRule(dstFormDef);
			condition.setQuestionId(questionDef.getId());
			validationRule.addCondition(new Condition(condition));
		}
		
		if(validationRule == null)
			return; //No matching condition found.
		
		if(validationRule.getConditionCount() > 0)
			dstFormDef.addValidationRule(validationRule);
	}

}
