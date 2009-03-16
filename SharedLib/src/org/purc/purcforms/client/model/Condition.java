package org.purc.purcforms.client.model;

import java.io.Serializable;
import java.util.Date;

import org.zenika.widget.client.util.DateUtil;


/**
 * A condition which is part of a rule. For definition of a rule, go to the Rule class.
 * E.g. If sex is Male. If age is greater than than 4. etc
 *
 *@author Daniel Kayiwa
 */
public class Condition implements Serializable{
	
	/** The unique identifier of the question referenced by this condition. */
	private int questionId = ModelConstants.NULL_ID;
	
	/** The operator of the condition. Eg Equal to, Greater than, etc. */
	private int operator = ModelConstants.OPERATOR_NULL;
	
	/** The value checked to see if the condition is true or false.
	 * For the above example, the value would be 4 or the id of the Male option.
	 * For a list of options this value is the option id, not the value or text value.
	 */
	private String value = ModelConstants.EMPTY_STRING;
	
	private String secondValue = ModelConstants.EMPTY_STRING;
	
	/** The unique identifier of a condition. */
	private int id = ModelConstants.NULL_ID;
	
	/** Creates a new condition object. */
	public Condition(){

	}
	
	/** Copy constructor. */
	public Condition(Condition condition){
		this(condition.getId(),condition.getQuestionId(),condition.getOperator(),condition.getValue());
	}
	
	/**
	 * Creates a new condition object from its parameters. 
	 * 
	 * @param id - the numeric identifier of the condition.
	 * @param questionId - the numeric identifier of the question.
	 * @param operator - the condition operator.
	 * @param value - the value to be equated to.
	 */
	public Condition(int id,int questionId, int operator, String value) {
		this();
		setQuestionId(questionId);
		setOperator(operator);
		setValue(value);
		setId(id);
	}
	
	public int getOperator() {
		return operator;
	}
	public void setOperator(int operator) {
		this.operator = operator;
	}
	public int getQuestionId() {
		return questionId;
	}
	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getId() {
		return id;
	}
	public void setId(int conditionId) {
		this.id = conditionId;
	}
	
	public String getSecondValue() {
		return secondValue;
	}

	public void setSecondValue(String secondValue) {
		this.secondValue = secondValue;
	}

	/**
     * Test if a condition is true or false.
     */
	public boolean isTrue(FormDef formDef){
		QuestionDef qn = formDef.getQuestion(this.questionId);
		
		switch(qn.getDataType()){
			case QuestionDef.QTN_TYPE_TEXT:
				return isTextTrue(qn);
			case QuestionDef.QTN_TYPE_NUMERIC:
				return isNumericTrue(qn);
			case QuestionDef.QTN_TYPE_DATE:
				return isDateTrue(qn);
			case QuestionDef.QTN_TYPE_DATE_TIME:
				return isDateTimeTrue(qn);
			case QuestionDef.QTN_TYPE_DECIMAL:
				return isDecimalTrue(qn);
			case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
				return isListExclusiveTrue(qn);
			case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
				return isListMultipleTrue(qn);
			case QuestionDef.QTN_TYPE_TIME:
				return isTimeTrue(qn);
			case QuestionDef.QTN_TYPE_BOOLEAN:
				return isTextTrue(qn);
		}
		
		return true;
	}
	
	private boolean isNumericTrue(QuestionDef qtn){
		//return value.equals(qtn.getAnswer());
		try{
			if(qtn.getAnswer() == null || qtn.getAnswer().trim().length() == 0){
				if(operator == ModelConstants.OPERATOR_NOT_EQUAL ||
				   operator == ModelConstants.OPERATOR_NOT_BETWEEN)
						return true;
				return operator == ModelConstants.OPERATOR_IS_NULL;
			}
				
			int answer = Integer.parseInt(qtn.getAnswer());
			int intValue = Integer.parseInt(value);
				
			int secondIntValue = intValue;
			if(secondValue != null && secondValue.trim().length() > 0)
				secondIntValue = Integer.parseInt(secondValue);
					
			if(operator == ModelConstants.OPERATOR_EQUAL)
				return intValue == answer;
			else if(operator == ModelConstants.OPERATOR_NOT_EQUAL)
				return intValue != answer;
			else if(operator == ModelConstants.OPERATOR_LESS)
				return answer < intValue;
			else if(operator == ModelConstants.OPERATOR_LESS_EQUAL)
				return answer < intValue || intValue == answer;
			else if(operator == ModelConstants.OPERATOR_GREATER)
				return answer > intValue;
			else if(operator == ModelConstants.OPERATOR_GREATER_EQUAL)
				return answer > intValue || intValue == answer;
			else if(operator == ModelConstants.OPERATOR_BETWEEN)
				return answer > intValue && intValue < secondIntValue;
			else if(operator == ModelConstants.OPERATOR_NOT_BETWEEN)
				return !(answer > intValue && intValue < secondIntValue);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		
		return false;
	}
	
	//TODO Should this test be case sensitive?
	private boolean isTextTrue(QuestionDef qtn){
		String answer = qtn.getAnswer();
		if(answer == null || answer.trim().length() == 0){
			if(operator == ModelConstants.OPERATOR_NOT_EQUAL ||
			   operator == ModelConstants.OPERATOR_NOT_START_WITH ||
			   operator == ModelConstants.OPERATOR_NOT_CONTAIN)
				return true;
			
			return operator == ModelConstants.OPERATOR_IS_NULL;
		}
			
		if(operator == ModelConstants.OPERATOR_EQUAL)
			return value.equals(qtn.getAnswer());
		else if(operator == ModelConstants.OPERATOR_NOT_EQUAL)
			return !value.equals(qtn.getAnswer());
		else if(operator == ModelConstants.OPERATOR_STARTS_WITH)
			return answer.startsWith(value);
		else if(operator == ModelConstants.OPERATOR_NOT_START_WITH)
			return !answer.startsWith(value);
		else if(operator == ModelConstants.OPERATOR_CONTAINS)
			return answer.contains(value);
		else if(operator == ModelConstants.OPERATOR_NOT_CONTAIN)
			return !answer.contains(value);
		
		return false;
	}
	
	/**
	 * Tests if the passed parameter date value is equal to the value of the condition.
	 * 
	 * @param data - passed parameter date value.
	 * @return - true when the two values are the same, else false.
	 */
	private boolean isDateTrue(QuestionDef qtn){
		//return value.equals(qtn.getAnswer());
		try{
			if(qtn.getAnswer() == null || qtn.getAnswer().trim().length() == 0){
				if(operator == ModelConstants.OPERATOR_NOT_EQUAL ||
				   operator == ModelConstants.OPERATOR_NOT_BETWEEN)
						return true;
				return operator == ModelConstants.OPERATOR_IS_NULL;
			}
				
			Date answer = DateUtil.getDateTimeFormat().parse(qtn.getAnswer());
			Date dateValue = DateUtil.getDateTimeFormat().parse(value);
				
			Date secondDateValue = dateValue;
			if(secondValue != null && secondValue.trim().length() > 0)
				secondDateValue = DateUtil.getDateTimeFormat().parse(secondValue);
					
			if(operator == ModelConstants.OPERATOR_EQUAL)
				return dateValue.equals(answer);
			else if(operator == ModelConstants.OPERATOR_NOT_EQUAL)
				return !dateValue.equals(answer);
			else if(operator == ModelConstants.OPERATOR_LESS)
				return answer.before(dateValue);
			else if(operator == ModelConstants.OPERATOR_LESS_EQUAL)
				return answer.before(dateValue) || dateValue.equals(answer);
			else if(operator == ModelConstants.OPERATOR_GREATER)
				return answer.after(dateValue);
			else if(operator == ModelConstants.OPERATOR_GREATER_EQUAL)
				return answer.after(dateValue) || dateValue.equals(answer);
			else if(operator == ModelConstants.OPERATOR_BETWEEN)
				return answer.after(dateValue) && dateValue.before(secondDateValue);
			else if(operator == ModelConstants.OPERATOR_NOT_BETWEEN)
				return !(answer.after(dateValue) && dateValue.before(secondDateValue));
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		
		return false;
	}
	
	private boolean isDateTimeTrue(QuestionDef qtn){
		return isDateTrue(qtn);
	}
	
	private boolean isTimeTrue(QuestionDef qtn){
		return isDateTrue(qtn);
	}
	
	private boolean isListMultipleTrue(QuestionDef qtn){
		//if(qtn.answerContainsValue(value))
		//	return true;
		//return value.equals(qtn.getAnswer());
		try{
			if(qtn.getAnswer() == null || qtn.getAnswer().trim().length() == 0){
				if(operator == ModelConstants.OPERATOR_NOT_EQUAL || 
				   operator == ModelConstants.OPERATOR_NOT_IN_LIST)
					return true;
				return operator == ModelConstants.OPERATOR_IS_NULL;
			}
			//return qtn.getAnswer().contains(value);
			
			switch(operator){
				case ModelConstants.OPERATOR_EQUAL:
					return qtn.getAnswer().equals(value);
				case ModelConstants.OPERATOR_NOT_EQUAL:
					return !qtn.getAnswer().equals(value);
				case ModelConstants.OPERATOR_IN_LIST:
					return value.contains(qtn.getAnswer());
				case ModelConstants.OPERATOR_NOT_IN_LIST:
					return !value.contains(qtn.getAnswer());
				default:
				return false;
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		return false;
	}
	
	private boolean isListExclusiveTrue(QuestionDef qtn){
		
		try{
			if(qtn.getAnswer() == null || qtn.getAnswer().trim().length() == 0){
				//return operator != PurcConstants.OPERATOR_EQUAL;
				if(operator == ModelConstants.OPERATOR_NOT_EQUAL || 
				   operator == ModelConstants.OPERATOR_NOT_IN_LIST)
					return true;
				return operator == ModelConstants.OPERATOR_IS_NULL;
			}
	
			switch(operator){
				case ModelConstants.OPERATOR_EQUAL:
					return qtn.getAnswer().equals(value);
				case ModelConstants.OPERATOR_NOT_EQUAL:
					return !qtn.getAnswer().equals(value);
				case ModelConstants.OPERATOR_IN_LIST:
					return value.contains(qtn.getAnswer());
				case ModelConstants.OPERATOR_NOT_IN_LIST:
					return !value.contains(qtn.getAnswer());
				default:
					return false;
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		
		return false;
	}
	
	private boolean isDecimalTrue(QuestionDef qtn){
		//return value.equals(qtn.getAnswer());
		
		try{
			if(qtn.getAnswer() == null || qtn.getAnswer().trim().length() == 0){
				if(operator == ModelConstants.OPERATOR_NOT_EQUAL ||
				   operator == ModelConstants.OPERATOR_NOT_BETWEEN)
						return true;
				return operator == ModelConstants.OPERATOR_IS_NULL;
			}
				
			float answer = Float.parseFloat(qtn.getAnswer());
			float floatValue = Float.parseFloat(value);
				
			float secondFloatValue = floatValue;
			if(secondValue != null && secondValue.trim().length() > 0)
				secondFloatValue = Float.parseFloat(secondValue);
					
			if(operator == ModelConstants.OPERATOR_EQUAL)
				return floatValue == answer;
			else if(operator == ModelConstants.OPERATOR_NOT_EQUAL)
				return floatValue != answer;
			else if(operator == ModelConstants.OPERATOR_LESS)
				return answer < floatValue;
			else if(operator == ModelConstants.OPERATOR_LESS_EQUAL)
				return answer < floatValue || floatValue == answer;
			else if(operator == ModelConstants.OPERATOR_GREATER)
				return answer > floatValue;
			else if(operator == ModelConstants.OPERATOR_GREATER_EQUAL)
				return answer > floatValue || floatValue == answer;
			else if(operator == ModelConstants.OPERATOR_BETWEEN)
				return answer > floatValue && floatValue < secondFloatValue;
			else if(operator == ModelConstants.OPERATOR_NOT_BETWEEN)
				return !(answer > floatValue && floatValue < secondFloatValue);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		
		return false;
	}
}