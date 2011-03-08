package org.purc.purcforms.client.model;

import java.io.Serializable;

import org.purc.purcforms.client.xforms.XformConstants;

import com.google.gwt.xml.client.Element;


/**
 * 
 * @author daniel
 *
 */
public class Calculation implements Serializable{

	/**
	 * Generated serialization ID
	 */
	private static final long serialVersionUID = 1L;

	/** The unique identifier of the question whose value to calculate. */
	private int questionId = ModelConstants.NULL_ID;
	
	/** The calculate xpath expression. */
	private String calculateExpression = ModelConstants.EMPTY_STRING;


	public Calculation(Calculation calculation) {
		this(calculation.getQuestionId(),calculation.getCalculateExpression());
	}

	public Calculation(int questionId, String calculateExpression) {
		super();
		this.questionId = questionId;
		this.calculateExpression = calculateExpression;
	}

	public int getQuestionId() {
		return questionId;
	}

	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}

	public String getCalculateExpression() {
		return calculateExpression;
	}

	public void setCalculateExpression(String calculateExpression) {
		this.calculateExpression = calculateExpression;
	}
	
	
	public void updateDoc(FormDef formDef){
		QuestionDef questionDef = formDef.getQuestion(questionId);
		assert(questionDef != null);
		Element node = questionDef.getBindNode();
		if(node == null)
			node = questionDef.getControlNode();
		
		node.setAttribute(XformConstants.ATTRIBUTE_NAME_CALCULATE,calculateExpression);
	}
}
