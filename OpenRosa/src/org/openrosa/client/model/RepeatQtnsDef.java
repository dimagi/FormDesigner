package org.openrosa.client.model;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import org.openrosa.client.xforms.UiElementBuilder;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;


/**
 * Definition for repeat sets of questions. Basically this is just a specialized collection
 * of a set of repeating questions, together with reference to their parent question.
 * 
 * @author daniel
 *
 */
public class RepeatQtnsDef extends GroupDef implements Serializable {
	
	
	/** Reference to the parent question. */
	private QuestionDef parentQtnDef;
	
	
	/**
	 * Creates a new repeat questions definition object.
	 */
	public RepeatQtnsDef() {
		super();
		parentQtnDef = null;
	}
	
	/** Copy Constructor. */
	public RepeatQtnsDef(RepeatQtnsDef repeatQtnsDef, QuestionDef parent) {
		super((GroupDef)repeatQtnsDef, parent);
		setParentQtnDef(repeatQtnsDef.getParentQtnDef());

	}
	
	public RepeatQtnsDef(QuestionDef qtnDef) {
		super(qtnDef);
		setParentQtnDef(qtnDef);
	}
	
	public QuestionDef getParentQtnDef() {
		return parentQtnDef;
	}

	public void setParentQtnDef(QuestionDef qtnDef) {
		this.parentQtnDef = qtnDef;
	}
	
	public Element getDataNode(){
		return parentQtnDef.getDataNode();
	}
	
	public void setDataNode(Element element){
		parentQtnDef.setDataNode(element);
	}

//	public Vector getQuestions() {
//		return questions;
//	}
	
//	public int size(){
//		if(questions == null)
//			return 0;
//		return questions.size();
//	}

//	public void addQuestion(QuestionDef qtn){
//		if(questions == null)
//			questions = new Vector();
//		
//		//qtn.setId((byte)(questions.size()+1)); id should be set somewhere else
//		questions.addElement(qtn);
//	}
//	
//	public void removeQuestion(QuestionDef qtnDef, FormDef formDef){
//		if(qtnDef.getControlNode() != null && qtnDef.getControlNode().getParentNode() != null)
//			qtnDef.getControlNode().getParentNode().removeChild(qtnDef.getControlNode());
//		if(qtnDef.getDataNode() != null && qtnDef.getDataNode().getParentNode() != null)
//			qtnDef.getDataNode().getParentNode().removeChild(qtnDef.getDataNode());
//		if(qtnDef.getBindNode() != null && qtnDef.getBindNode().getParentNode() != null)
//			qtnDef.getBindNode().getParentNode().removeChild(qtnDef.getBindNode());
//		
//		if(formDef != null)
//			formDef.removeQtnFromRules(qtnDef);
//		
//		questions.removeElement(qtnDef);
//	}
//	
//	public void setQuestions(Vector questions) {
//		this.questions = questions;
//	}
	
	public String getText(){
		if(parentQtnDef != null)
			return parentQtnDef.getText();
		return null;
	}
	
//	public QuestionDef getQuestion(int id){
//		if(questions == null)
//			return null;
//		
//		for(int i=0; i<getChildren().size(); i++){
//			QuestionDef def = (QuestionDef)(getChildren().get(i));
//			if(def.getId() == id)
//				return def;
//		}
//		
//		return null;
//	}
//	
//	public QuestionDef getQuestionAt(int index){
//		return (QuestionDef)questions.elementAt(index);
//	}
	
//	public int getQuestionsCount(){
//		if(questions == null)
//			return 0;
//		return questions.size();
//	}
	
//	private void copyQuestions(Vector questions){
//		if(questions == null)
//			return;
//		
//		this.questions = new Vector();
//		for(int i=0; i<questions.size(); i++)
//			this.questions.addElement(new QuestionDef((QuestionDef)questions.elementAt(i),parentQtnDef));
//	}
	
	/*public void moveQuestionUp(QuestionDef questionDef){		
		PageDef.moveQuestionUp(questions, questionDef);
	}
	
	public void moveQuestionDown(QuestionDef questionDef){		
		PageDef.moveQuestionDown(questions, questionDef);
	}*/
	
//	public void updateDoc(Document doc, Element xformsNode, FormDef formDef, Element formNode, Element modelNode,Element groupNode, boolean withData, String orgFormVarName){
//		if(questions == null)
//			return;
//		
//		for(int i=0; i<questions.size(); i++){
//			QuestionDef questionDef = (QuestionDef)questions.elementAt(i);
//			questionDef.updateDoc(doc,xformsNode,formDef,parentQtnDef.getDataNode(),modelNode,parentQtnDef.getControlNode(),false,withData,orgFormVarName);
//		}
//		
//	}
	

	
	public void setId(int id){
		parentQtnDef.setId(id);
	}
	
	public int getId(){
		return parentQtnDef.getId();
	}
	
	public void setText(String text){
		parentQtnDef.setText(text);
	}
	
	public String getBinding(){
		return parentQtnDef.getBinding();
	}
	
	public void setBinding(String binding){
		parentQtnDef.setVariableName(binding);
	}
	
	
	public int getDataType(){
		return QuestionDef.QTN_TYPE_REPEAT;
	}

	/**
	 * Does Nothing.
	 */
	public void setDataType(int dataType){
		return; //this Def's type should *only* ever be Repeat.
	}
}
