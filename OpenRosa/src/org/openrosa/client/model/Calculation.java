package org.openrosa.client.model;

import java.io.Serializable;

import org.openrosa.client.xforms.XformConstants;
import org.openrosa.client.xforms.XmlUtil;

import com.google.gwt.xml.client.Element;


/**
 * 
 * @author daniel
 *
 */
public class Calculation implements Serializable{

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
		this.calculateExpression = calculateExpression.replace("&gt;",">").replace("&lt;","<");
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
		IFormElement questionDef = formDef.getElement(questionId);
		assert(questionDef != null);
		Element node = questionDef.getBindNode();
		if(node == null){
			return;
		}
		String expr = getCalculateExpression();
//		expr = expr.replace(">", "&gt;").replace("<", "&lt;");
		String oldexpr = node.getAttribute(XformConstants.ATTRIBUTE_NAME_CALCULATE);
		if(expr == null || expr.isEmpty() || (oldexpr != null && oldexpr.isEmpty())){
			node.removeAttribute(XformConstants.ATTRIBUTE_NAME_CALCULATE);
		}else{
			expr = XmlUtil.escapeXMLAttribute(expr);
			node.setAttribute(XformConstants.ATTRIBUTE_NAME_CALCULATE,expr);
		}
	}
}
