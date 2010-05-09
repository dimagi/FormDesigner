package org.openrosa.client.model;

import java.io.Serializable;
import java.util.Date;

import org.purc.purcforms.client.util.FormUtil;

import com.google.gwt.i18n.client.DateTimeFormat;


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

	/** The aggregate function. Eg Length, Value. */
	private int function = ModelConstants.FUNCTION_VALUE;

	/** The value checked to see if the condition is true or false.
	 * For the above example, the value would be 4 or the id of the Male option.
	 * For a list of options this value is the option id, not the value or text value.
	 */
	private String value = ModelConstants.EMPTY_STRING;

	/** The the question whose value is dynamically put in the value property. 
	 * This is useful for only design mode when the question variablename changes
	 * and hence we need to change the value or the value property.
	 * */
	private QuestionDef valueQtnDef;

	/**
	 * The second value checked to see if the condition is true. This has values or
	 * for contitions that have the OR operator or AND operator. Eg weight > 0 and weight < 100
	 */
	private String secondValue = ModelConstants.EMPTY_STRING;

	/** The unique identifier of a condition. */
	private int id = ModelConstants.NULL_ID;

	/** Creates a new condition object. */
	public Condition(){

	}

	/** Copy constructor. */
	public Condition(Condition condition){
		this(condition.getId(),condition.getQuestionId(),condition.getOperator(),condition.getFunction(),condition.getValue());
	}

	/**
	 * Creates a new condition object from its parameters. 
	 * 
	 * @param id - the numeric identifier of the condition.
	 * @param questionId - the numeric identifier of the question.
	 * @param operator - the condition operator.
	 * @param value - the value to be equated to.
	 */
	public Condition(int id,int questionId, int operator, int function, String value) {
		this();
		setQuestionId(questionId);
		setOperator(operator);
		setFunction(function);
		setValue(value);
		setId(id);
	}

	public int getOperator() {
		return operator;
	}
	public void setOperator(int operator) {
		this.operator = operator;
	}
	public int getFunction() {
		return function;
	}

	public void setFunction(int function) {
		this.function = function;
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
	 * Checks if this condition is true or false.
	 * 
	 * @param formDef the form definition object.
	 * @param validation set to true if this is a validation rule condition, else false if skip rule condition.
	 * @return true if the condition is true, else false.
	 */
	public boolean isTrue(FormDef formDef, boolean validation){
		String tempValue = value;
		boolean ret = true;

		try{
			QuestionDef qn = formDef.getQuestion(this.questionId);
			if(qn == null)
				return false; //possibly question deleted

			if(value.startsWith(formDef.getVariableName()+"/")){
				QuestionDef qn2 = formDef.getQuestion(value.substring(value.indexOf('/')+1));
				if(qn2 != null){
					value = qn2.getAnswer();
					if(value == null || value.trim().length() == 0){
						value = tempValue;
						if(qn.getAnswer() == null || qn.getAnswer().trim().length() == 0)
							return true; //Both questions not answered yet
						return false;
					}
					else if(qn.getAnswer() == null || qn.getAnswer().trim().length() == 0){
						value = tempValue;
						return false;
						//return validation; //TODO Do we really need validations to return true when qtn is not answered?
					}
				}
			}

			switch(qn.getDataType()){
			case QuestionDef.QTN_TYPE_TEXT:
				ret = isTextTrue(qn,validation);
				break;
			case QuestionDef.QTN_TYPE_REPEAT:
			case QuestionDef.QTN_TYPE_NUMERIC:
				ret = isNumericTrue(qn,validation);
				break;
			case QuestionDef.QTN_TYPE_DATE:
				ret = isDateTrue(qn,validation);
				break;
			case QuestionDef.QTN_TYPE_DATE_TIME:
				ret = isDateTimeTrue(qn,validation);
				break;
			case QuestionDef.QTN_TYPE_DECIMAL:
				ret = isDecimalTrue(qn,validation);
				break;
			case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
			case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC:
				ret = isListExclusiveTrue(qn,validation);
				break;
			case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
				ret = isListMultipleTrue(qn,validation);
				break;
			case QuestionDef.QTN_TYPE_TIME:
				ret = isTimeTrue(qn,validation);
				break;
			case QuestionDef.QTN_TYPE_BOOLEAN:
				ret = isTextTrue(qn,validation);
				break;
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}

		value = tempValue;

		return ret;
	}

	/**
	 * Check to see if a condition, attached to a numeric question, is true.
	 * 
	 * @param qtn the question whose answer we are using to test the condition
	 * @param validation has value of true if this is a validation logic condition, else false if skip logic one.
	 * @return true if the condition is true, else false.
	 */
	private boolean isNumericTrue(QuestionDef qtn, boolean validation){
		//return value.equals(qtn.getAnswer());
		try{
			if(qtn.getAnswer() == null || qtn.getAnswer().trim().length() == 0){
				if(validation && operator == ModelConstants.OPERATOR_IS_NOT_NULL)
					return false;
				else if(validation || operator == ModelConstants.OPERATOR_NOT_EQUAL ||
						operator == ModelConstants.OPERATOR_NOT_BETWEEN)
					return true;
				return operator == ModelConstants.OPERATOR_IS_NULL;
			}
			else if(operator == ModelConstants.OPERATOR_IS_NOT_NULL)
				return true;

			long answer = Long.parseLong(qtn.getAnswer());
			long longValue = Long.parseLong(value);

			long secondLongValue = longValue;
			if(secondValue != null && secondValue.trim().length() > 0)
				secondLongValue = Long.parseLong(secondValue);

			if(operator == ModelConstants.OPERATOR_EQUAL)
				return longValue == answer;
			else if(operator == ModelConstants.OPERATOR_NOT_EQUAL)
				return longValue != answer;
			else if(operator == ModelConstants.OPERATOR_LESS)
				return answer < longValue;
			else if(operator == ModelConstants.OPERATOR_LESS_EQUAL)
				return answer < longValue || longValue == answer;
			else if(operator == ModelConstants.OPERATOR_GREATER)
				return answer > longValue;
			else if(operator == ModelConstants.OPERATOR_GREATER_EQUAL)
				return answer > longValue || longValue == answer;
			else if(operator == ModelConstants.OPERATOR_BETWEEN)
				return answer > longValue && longValue < secondLongValue;
			else if(operator == ModelConstants.OPERATOR_NOT_BETWEEN)
				return !(answer > longValue && longValue < secondLongValue);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}

		return false;
	}

	//TODO Should this test be case sensitive?
	/**
	 * Check to see if a condition, attached to a text question, is true.
	 * 
	 * @param qtn the question whose answer we are using to test the condition
	 * @param validation has value of true if this is a validation logic condition, else false if skip logic one.
	 * @return true if the condition is true, else false.
	 */
	private boolean isTextTrue(QuestionDef qtn, boolean validation){
		String answer = qtn.getAnswer();

		if(function == ModelConstants.FUNCTION_VALUE){
			if(answer == null || answer.trim().length() == 0){
				if(validation && operator == ModelConstants.OPERATOR_IS_NOT_NULL)
					return false;
				else if(validation || operator == ModelConstants.OPERATOR_NOT_EQUAL ||
						operator == ModelConstants.OPERATOR_NOT_START_WITH ||
						operator == ModelConstants.OPERATOR_NOT_CONTAIN)
					return true;

				return operator == ModelConstants.OPERATOR_IS_NULL;
			}
			else if(operator == ModelConstants.OPERATOR_IS_NOT_NULL)
				return true;

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
		}
		else{
			if(answer == null || answer.trim().length() == 0)
				return true;

			long len1 = 0, len2 = 0, len = 0;
			if(value != null && value.trim().length() > 0)
				len1 = Long.parseLong(value);
			if(secondValue != null && secondValue.trim().length() > 0)
				len2 = Long.parseLong(secondValue);

			len = answer.trim().length();

			if(operator == ModelConstants.OPERATOR_EQUAL)
				return len == len1;
			else if(operator == ModelConstants.OPERATOR_NOT_EQUAL)
				return len != len1;
			else if(operator == ModelConstants.OPERATOR_LESS)
				return len < len1;
			else if(operator == ModelConstants.OPERATOR_LESS_EQUAL)
				return len <= len1;
			else if(operator == ModelConstants.OPERATOR_GREATER)
				return len > len1;
				else if(operator == ModelConstants.OPERATOR_GREATER_EQUAL)
					return len >= len1;
					else if(operator == ModelConstants.OPERATOR_BETWEEN)
						return len > len1 && len < len2;
						else if(operator == ModelConstants.OPERATOR_NOT_BETWEEN)
							return !(len > len1 && len < len2);
		}

		return false;
	}

	/**
	 * Check to see if a condition, attached to a date question, is true.
	 * 
	 * @param qtn the question whose answer we are using to test the condition
	 * @param validation has value of true if this is a validation logic condition, else false if skip logic one.
	 * @return true if the condition is true, else false.
	 */
	private boolean isDateTrue(QuestionDef qtn, boolean validation){
		//return value.equals(qtn.getAnswer());
		try{
			if(qtn.getAnswer() == null || qtn.getAnswer().trim().length() == 0){
				if(validation && operator == ModelConstants.OPERATOR_IS_NOT_NULL)
					return false;
				else if(validation || operator == ModelConstants.OPERATOR_NOT_EQUAL ||
						operator == ModelConstants.OPERATOR_NOT_BETWEEN)
					return true;
				return operator == ModelConstants.OPERATOR_IS_NULL;
			}
			else if(operator == ModelConstants.OPERATOR_IS_NOT_NULL)
				return true;

			Date answer = getDateTimeSubmitFormat(qtn).parse(qtn.getAnswer());
			Date dateValue = null;
			if(QuestionDef.isDateFunction(value))
				dateValue = QuestionDef.getDateFunctionValue(value);	
			else
				dateValue = getDateTimeSubmitFormat(qtn).parse(value);

			Date secondDateValue = dateValue;
			if(secondValue != null && secondValue.trim().length() > 0){
				if(QuestionDef.isDateFunction(secondValue))
					secondDateValue = QuestionDef.getDateFunctionValue(secondValue);	
				else
					secondDateValue = getDateTimeSubmitFormat(qtn).parse(secondValue);
			}

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

	private DateTimeFormat getDateTimeSubmitFormat(QuestionDef qtn){
		if(qtn.getDataType() == QuestionDef.QTN_TYPE_DATE_TIME)
			return FormUtil.getDateTimeSubmitFormat();
		else
			return FormUtil.getDateSubmitFormat();
	}


	/**
	 * Check to see if a condition, attached to a date and time question, is true.
	 * 
	 * @param qtn the question whose answer we are using to test the condition
	 * @param validation has value of true if this is a validation logic condition, else false if skip logic one.
	 * @return true if the condition is true, else false.
	 */
	private boolean isDateTimeTrue(QuestionDef qtn, boolean validation){
		return isDateTrue(qtn,validation);
	}


	/**
	 * Check to see if a condition, attached to a time question, is true.
	 * 
	 * @param qtn the question whose answer we are using to test the condition
	 * @param validation has value of true if this is a validation logic condition, else false if skip logic one.
	 * @return true if the condition is true, else false.
	 */
	private boolean isTimeTrue(QuestionDef qtn, boolean validation){
		return isDateTrue(qtn,validation);
	}


	/**
	 * Check to see if a condition, attached to a multiple select question, is true.
	 * 
	 * @param qtn the question whose answer we are using to test the condition
	 * @param validation has value of true if this is a validation logic condition, else false if skip logic one.
	 * @return true if the condition is true, else false.
	 */
	private boolean isListMultipleTrue(QuestionDef qtn, boolean validation){
		//if(qtn.answerContainsValue(value))
		//	return true;
		//return value.equals(qtn.getAnswer());
		try{
			if(qtn.getAnswer() == null || qtn.getAnswer().trim().length() == 0){
				if(validation && operator == ModelConstants.OPERATOR_IS_NOT_NULL)
					return false;
				else if(validation || operator == ModelConstants.OPERATOR_NOT_EQUAL || 
						operator == ModelConstants.OPERATOR_NOT_IN_LIST)
					return true;
				return operator == ModelConstants.OPERATOR_IS_NULL;
			}
			else if(operator == ModelConstants.OPERATOR_IS_NOT_NULL)
				return true;
			//return qtn.getAnswer().contains(value);

			switch(operator){
			case ModelConstants.OPERATOR_EQUAL:
				return qtn.getAnswer().contains(value); //qtn.getAnswer().equals(value);
			case ModelConstants.OPERATOR_NOT_EQUAL:
				return !qtn.getAnswer().contains(value); //!qtn.getAnswer().equals(value);
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


	/**
	 * Check to see if a condition, attached to a single select question, is true.
	 * 
	 * @param qtn the question whose answer we are using to test the condition
	 * @param validation has value of true if this is a validation logic condition, else false if skip logic one.
	 * @return true if the condition is true, else false.
	 */
	private boolean isListExclusiveTrue(QuestionDef qtn, boolean validation){

		try{
			if(qtn.getAnswer() == null || qtn.getAnswer().trim().length() == 0){
				//return operator != PurcConstants.OPERATOR_EQUAL;
				if(validation && operator == ModelConstants.OPERATOR_IS_NOT_NULL)
					return false;
				else if(validation || operator == ModelConstants.OPERATOR_NOT_EQUAL || 
						operator == ModelConstants.OPERATOR_NOT_IN_LIST)
					return true;
				return operator == ModelConstants.OPERATOR_IS_NULL;
			}
			else if(operator == ModelConstants.OPERATOR_IS_NOT_NULL)
				return true;

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


	/**
	 * Check to see if a condition, attached to a decimal question, is true.
	 * 
	 * @param qtn the question whose answer we are using to test the condition
	 * @param validation has value of true if this is a validation logic condition, else false if skip logic one.
	 * @return true if the condition is true, else false.
	 */
	private boolean isDecimalTrue(QuestionDef qtn, boolean validation){
		//return value.equals(qtn.getAnswer());

		try{
			if(qtn.getAnswer() == null || qtn.getAnswer().trim().length() == 0){
				if(validation && operator == ModelConstants.OPERATOR_IS_NOT_NULL)
					return false;
				else if(validation || operator == ModelConstants.OPERATOR_NOT_EQUAL ||
						operator == ModelConstants.OPERATOR_NOT_BETWEEN)
					return true;
				return operator == ModelConstants.OPERATOR_IS_NULL;
			}
			else if(operator == ModelConstants.OPERATOR_IS_NOT_NULL)
				return true;

			double answer = Double.parseDouble(qtn.getAnswer());
			double doubleValue = Double.parseDouble(value);

			double secondDoubleValue = doubleValue;
			if(secondValue != null && secondValue.trim().length() > 0)
				secondDoubleValue = Double.parseDouble(secondValue);

			if(operator == ModelConstants.OPERATOR_EQUAL)
				return doubleValue == answer;
			else if(operator == ModelConstants.OPERATOR_NOT_EQUAL)
				return doubleValue != answer;
			else if(operator == ModelConstants.OPERATOR_LESS)
				return answer < doubleValue;
			else if(operator == ModelConstants.OPERATOR_LESS_EQUAL)
				return answer < doubleValue || doubleValue == answer;
			else if(operator == ModelConstants.OPERATOR_GREATER)
				return answer > doubleValue;
			else if(operator == ModelConstants.OPERATOR_GREATER_EQUAL)
				return answer > doubleValue || doubleValue == answer;
			else if(operator == ModelConstants.OPERATOR_BETWEEN)
				return answer > doubleValue && doubleValue < secondDoubleValue;
			else if(operator == ModelConstants.OPERATOR_NOT_BETWEEN)
				return !(answer > doubleValue && doubleValue < secondDoubleValue);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}

		return false;
	}


	/**
	 * Gets the value for this condition. If the value references another question,
	 * it returns the answer of that question.
	 * 
	 * @param formDef the form definition object that this condition belongs to.
	 * @return the text value.
	 */
	public String getValue(FormDef formDef){	
		if(value.startsWith(formDef.getVariableName()+"/")){
			QuestionDef qn = formDef.getQuestion(value.substring(value.indexOf('/')+1));
			if(qn != null)
				return qn.getAnswer();
		}
		return value;
	}

	/**
	 * Sets the new value of the condition.
	 * 
	 * @param origValue the original value.
	 * @param newValue the new value.
	 */
	public void updateValue(String origValue, String newValue){
		if(origValue.equals(value))
			value = newValue;
	}

	public void setValueQtnDef(QuestionDef valueQtnDef){
		this.valueQtnDef = valueQtnDef;
	}

	public QuestionDef getValueQtnDef(){
		return valueQtnDef;
	}

	/**
	 * Checks if this condition references an answer of a particular question.
	 * 
	 * @param questionDef the question whose answer is referenced.
	 * @param formDef the form being filled.
	 * @return true if it does, else false.
	 */
	public boolean hasQuestion(QuestionDef questionDef, FormDef formDef){
		if(value.startsWith(formDef.getVariableName()+"/")){
			QuestionDef qtn = formDef.getQuestion(value.substring(value.indexOf('/')+1));
			if(qtn != null && qtn == questionDef)
				return true;
		}

		return false;
	}

	/**
	 * Gets the question, if any, referenced by a condition value.
	 * Such values exit for cross field validations where a condition's
	 * value is not static but dynamic in the sense that it comes from 
	 * the answer of some other question (The question we are returning)
	 * An example of such a condition is "Male number of kids should be
	 * less than or equal to the Total number of kids."
	 * 
	 * @param formDef the form that this condition belongs to.
	 * @return the question if any is found.
	 */
	public QuestionDef getQuestion(FormDef formDef){
		if(value.startsWith(formDef.getVariableName()+"/"))
			return formDef.getQuestion(value.substring(value.indexOf('/')+1));

		return null;
	}
}