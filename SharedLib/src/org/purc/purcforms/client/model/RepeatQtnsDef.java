package org.purc.purcforms.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.xforms.XformConverter;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;


/**
 * Definition for repeat sets of questions. Basically this is just a specialized collection
 * of a set of repeating questions, together with with reference to their parent question.
 * 
 * @author daniel
 *
 */
public class RepeatQtnsDef implements Serializable {
	
	/** A list of questions (QuestionDef objects) on a repeat questions row. */
	private Vector questions;
	
	/** Reference to the parent question. */
	private QuestionDef qtnDef;
	
	private byte maxRows = -1;
	
	public RepeatQtnsDef() {
		 
	}
	
	/** Copy Constructor. */
	public RepeatQtnsDef(RepeatQtnsDef repeatQtnsDef) {
		//setQtnDef(new QuestionDef(repeatQtnsDef.getQtnDef()));
		setQtnDef(repeatQtnsDef.getQtnDef());
		copyQuestions(repeatQtnsDef.getQuestions());
	}
	
	public RepeatQtnsDef(QuestionDef qtnDef) {
		setQtnDef(qtnDef);
	}
	
	public RepeatQtnsDef(QuestionDef qtnDef,Vector questions) {
		this(qtnDef);
		setQuestions(questions);
	}
	
	public QuestionDef getQtnDef() {
		return qtnDef;
	}

	public void setQtnDef(QuestionDef qtnDef) {
		this.qtnDef = qtnDef;
	}

	public Vector getQuestions() {
		return questions;
	}
	
	public int size(){
		if(questions == null)
			return 0;
		return questions.size();
	}

	public void addQuestion(QuestionDef qtn){
		if(questions == null)
			questions = new Vector();
		
		//qtn.setId((byte)(questions.size()+1)); id should be set somewhere else
		questions.addElement(qtn);
	}
	
	public void removeQuestion(QuestionDef qtnDef, FormDef formDef){
		if(qtnDef.getControlNode() != null && qtnDef.getControlNode().getParentNode() != null)
			qtnDef.getControlNode().getParentNode().removeChild(qtnDef.getControlNode());
		if(qtnDef.getDataNode() != null && qtnDef.getDataNode().getParentNode() != null)
			qtnDef.getDataNode().getParentNode().removeChild(qtnDef.getDataNode());
		if(qtnDef.getBindNode() != null && qtnDef.getBindNode().getParentNode() != null)
			qtnDef.getBindNode().getParentNode().removeChild(qtnDef.getBindNode());
		
		if(formDef != null)
			formDef.removeQtnFromRules(qtnDef);
		
		questions.removeElement(qtnDef);
	}
	
	public void setQuestions(Vector questions) {
		this.questions = questions;
	}
	
	public String getText(){
		if(qtnDef != null)
			return qtnDef.getText();
		return null;
	}
	
	public QuestionDef getQuestion(int id){
		if(questions == null)
			return null;
		
		for(int i=0; i<getQuestions().size(); i++){
			QuestionDef def = (QuestionDef)getQuestions().elementAt(i);
			if(def.getId() == id)
				return def;
		}
		
		return null;
	}
	
	public QuestionDef getQuestionAt(int index){
		return (QuestionDef)questions.elementAt(index);
	}
	
	public int getQuestionsCount(){
		if(questions == null)
			return 0;
		return questions.size();
	}
	
	private void copyQuestions(Vector questions){
		if(questions == null)
			return;
		
		this.questions = new Vector();
		for(int i=0; i<questions.size(); i++)
			this.questions.addElement(new QuestionDef((QuestionDef)questions.elementAt(i),qtnDef));
	}
	
	public void moveQuestionUp(QuestionDef questionDef){
		/*int index = questions.indexOf(questionDef);
		
		questions.remove(questionDef);
		
		QuestionDef currentQuestionDef;
		List list = new ArrayList();
		
		while(questions.size() >= index){
			currentQuestionDef = (QuestionDef)questions.elementAt(index-1);
			list.add(currentQuestionDef);
			questions.remove(currentQuestionDef);
		}
		
		questions.add(questionDef);
		for(int i=0; i<list.size(); i++)
			questions.add(list.get(i));*/
		
		PageDef.moveQuestionUp(questions, questionDef);
	}
	
	public void moveQuestionDown(QuestionDef questionDef){
		/*int index = questions.indexOf(questionDef);	
		
		questions.remove(questionDef);
		
		QuestionDef currentItem; // = parent.getChild(index - 1);
		List list = new ArrayList();
		
		while(questions.size() > 0 && questions.size() > index){
			currentItem = (QuestionDef)questions.elementAt(index);
			list.add(currentItem);
			questions.remove(currentItem);
		}
		
		for(int i=0; i<list.size(); i++){
			if(i == 1)
				questions.add(questionDef); //Add after the first item.
			questions.add(list.get(i));
		}
		
		if(list.size() == 1)
			questions.add(questionDef);*/
		
		PageDef.moveQuestionDown(questions, questionDef);
	}
	
	public void updateDoc(Document doc, Element xformsNode, FormDef formDef, Element formNode, Element modelNode,Element groupNode, boolean withData, String orgFormVarName){
		if(questions == null)
			return;
		
		for(int i=0; i<questions.size(); i++){
			QuestionDef questionDef = (QuestionDef)questions.elementAt(i);
			questionDef.updateDoc(doc,xformsNode,formDef,qtnDef.getDataNode(),modelNode,qtnDef.getControlNode(),false,withData,orgFormVarName);
		}
	}
	
	/**
	 * Gets a question identified by a variable name.
	 * 
	 * @param varName - the string identifier of the question. 
	 * @return the question reference.
	 */
	public QuestionDef getQuestion(String varName){
		if(varName == null || questions == null)
			return null;
		
		for(int i=0; i<questions.size(); i++){
			QuestionDef def = (QuestionDef)questions.elementAt(i);
			if(def.getVariableName().equals(varName))
				return def;
		}
		
		return null;
	}
	
	public QuestionDef getQuestionWithText(String text){
		if(text == null || questions == null)
			return null;
		
		for(int i=0; i<questions.size(); i++){
			QuestionDef questionDef = (QuestionDef)questions.elementAt(i);
			if(questionDef.getText().equals(text))
				return questionDef;
		}
		return null;
	}
	
	public void refresh(RepeatQtnsDef pepeatQtnsDef){
		Vector questions2 = pepeatQtnsDef.getQuestions();
		if(questions == null || questions2 == null)
			return;
		
		for(int index = 0; index < questions2.size(); index++){
			QuestionDef qtn = (QuestionDef)questions2.get(index);
			QuestionDef questionDef = getQuestion(qtn.getVariableName());
			if(questionDef != null)
				questionDef.refresh(qtn);
		}
	}
	
	public void updateDataNodes(Element parentDataNode){
		if(questions == null)
			return;

		for(int i=0; i<questions.size(); i++)
			((QuestionDef)questions.elementAt(i)).updateDataNodes(parentDataNode);
	}
	
	public void setMaxRows(byte maxRows){
		this.maxRows = maxRows;
	}
	
	public byte getMaxRows(){
		return maxRows;
	}
	
	public void buildLanguageNodes(com.google.gwt.xml.client.Document doc, Element parentNode){
		if(questions == null)
			return;

		for(int i=0; i<questions.size(); i++)
			((QuestionDef)questions.elementAt(i)).buildLanguageNodes(doc,parentNode);
	}
}
