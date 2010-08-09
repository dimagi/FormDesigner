package org.purc.purcforms.client.model;



/**
 * 
 * @author daniel
 *
 */
public class FilterCondition extends FilterConditionRow {

	/**
	 * Generated serialization ID
	 */
	private static final long serialVersionUID = -2688667012773543641L;
	
	private String fieldName;
	private String firstValue;
	private String secondValue;
	private int operator;
	private int dataType;
	
	
	public FilterCondition(){
		
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFirstValue() {
		return firstValue;
	}

	public void setFirstValue(String firstValue) {
		this.firstValue = firstValue;
	}

	public String getSecondValue() {
		return secondValue;
	}

	public void setSecondValue(String secondValue) {
		this.secondValue = secondValue;
	}

	public int getOperator() {
		return operator;
	}

	public void setOperator(int operator) {
		this.operator = operator;
	}

	public int getDataType() {
		return dataType;
	}

	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
}
