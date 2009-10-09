package org.purc.purcforms.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.xforms.XformConverter;
import org.purc.purcforms.client.xforms.XformUtil;

import com.google.gwt.user.client.Window;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;


/** The definition of a page in a form or questionaire. 
 * 
 * @author Daniel Kayiwa
 *
 */
public class PageDef implements Serializable{

	/** A list of questions on a page. */
	private Vector questions;

	/** The page number. */
	private int pageNo = ModelConstants.NULL_ID;

	/** The name of the page. */
	private String name = ModelConstants.EMPTY_STRING;

	/** The xforms label node for this page. */
	private Element labelNode;
	
	/** The xforms group node for this page. */
	private Element groupNode;

	/** The form defintion to which this page belongs. */
	private FormDef parent;

	
	/**
	 * Constructs a new page.
	 * 
	 * @param parent the form to which the page belongs.
	 */
	public PageDef(FormDef parent) {
		this.parent = parent;
	}

	/**
	 * Creates a new copy of a page from an existing one.
	 * 
	 * @param pageDef the page to copy.
	 * @param parent the form to which the page belongs.
	 */
	public PageDef(PageDef pageDef,FormDef parent) {
		this(parent);
		setPageNo(pageDef.getPageNo());
		setName(pageDef.getName());
		copyQuestions(pageDef.getQuestions());
	}

	/**
	 * Constructs a page object with the following parameters.
	 * 
	 * @param name the name of the page.
	 * @param pageNo the number of the page.
	 * @param parent the form to which the page belongs.
	 */
	public PageDef(String name, int pageNo,FormDef parent) {
		this(parent);
		setName(name);
		setPageNo(pageNo);
		setQuestions(questions);
	}

	/**
	 * Constructs a page with the following parameters.
	 * 
	 * @param name the name of the page.
	 * @param pageNo the number of the page.
	 * @param questions a list of questions in the page.
	 * @param parent the form to which the page belongs.
	 */
	public PageDef(String name, int pageNo,Vector questions,FormDef parent) {
		this(parent);
		setName(name);
		setPageNo(pageNo);
		setQuestions(questions);
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Vector getQuestions() {
		return questions;
	}

	public FormDef getParent() {
		return parent;
	}

	public void setParent(FormDef parent) {
		this.parent = parent;
	}

	public int getQuestionCount(){
		if(questions == null)
			return 0;
		return questions.size();
	}

	public QuestionDef getQuestionAt(int index){
		if(questions == null)
			return null;
		return (QuestionDef)questions.elementAt(index);
	}

	/**
	 * @return the labelNode
	 */
	public Element getLabelNode() {
		return labelNode;
	}

	/**
	 * @param labelNode the labelNode to set
	 */
	public void setLabelNode(Element labelNode) {
		this.labelNode = labelNode;
	}

	/**
	 * @return the groupNode
	 */
	public Element getGroupNode() {
		return groupNode;
	}

	/**
	 * @param groupNode the groupNode to set
	 */
	public void setGroupNode(Element groupNode) {
		this.groupNode = groupNode;
	}

	public void addQuestion(QuestionDef qtn){
		if(questions == null)
			questions = new Vector();
		questions.addElement(qtn);
		qtn.setParent(this);
	}

	public void setQuestions(Vector questions) {
		this.questions = questions;
	}

	public QuestionDef getQuestion(String varName){
		if(questions == null)
			return null;

		for(int i=0; i<getQuestions().size(); i++){
			QuestionDef def = (QuestionDef)getQuestions().elementAt(i);
			if(def.getVariableName().equals(varName))
				return def;
			
			//Without this, then we have not validation and skip rules in repeat questions.
			if(def.getDataType() == QuestionDef.QTN_TYPE_REPEAT && def.getRepeatQtnsDef() != null){
				def = def.getRepeatQtnsDef().getQuestion(varName);
				if(def != null)
					return def;
			}
			/*else{
				String binding = def.getVariableName();
				if(varName.endsWith(binding) && parent != null){
					if(!binding.startsWith("/")) 
						binding = "/"+binding;
					binding  = parent.getVariableName() + binding;
					if(!binding.startsWith("/")) 
						binding = "/"+binding;
					if(binding.equals(varName))
						return def;
				}
				if(def.getDataType() == QuestionDef.QTN_TYPE_REPEAT){ //TODO Need to make sure this new addition does not introduce bugs
					def = def.getRepeatQtnsDef().getQuestion(varName);
					if(def != null)
						return def;
				}
			}*/
		}

		return null;
	}

	public QuestionDef getQuestion(int id){
		if(questions == null)
			return null;

		for(int i=0; i<getQuestions().size(); i++){
			QuestionDef def = (QuestionDef)getQuestions().elementAt(i);
			if(def.getId() == id)
				return def;
			
			//Without this, then we have not validation and skip rules in repeat questions.
			if(def.getDataType() == QuestionDef.QTN_TYPE_REPEAT && def.getRepeatQtnsDef() != null){
				def = def.getRepeatQtnsDef().getQuestion(id);
				if(def != null)
					return def;
			}
		}

		return null;
	}

	public String toString() {
		return getName();
	}

	private void copyQuestions(Vector questions){
		if(questions != null){
			this.questions = new Vector();
			for(int i=0; i<questions.size(); i++)
				this.questions.addElement(new QuestionDef((QuestionDef)questions.elementAt(i),this));
		}
	}

	public boolean removeQuestion(QuestionDef qtnDef, FormDef formDef){
		if(qtnDef.getControlNode() != null && qtnDef.getControlNode().getParentNode() != null){
			if(qtnDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
				qtnDef.getControlNode().getParentNode().getParentNode().removeChild(qtnDef.getControlNode().getParentNode());
			else
				qtnDef.getControlNode().getParentNode().removeChild(qtnDef.getControlNode());
		}

		//Either no / or just one occurrence. More than one nestings are avoided to make things simple
		if(qtnDef.getVariableName().indexOf('/') == qtnDef.getVariableName().lastIndexOf('/')){
			if(qtnDef.getDataNode() != null && qtnDef.getDataNode().getParentNode() != null)
				qtnDef.getDataNode().getParentNode().removeChild(qtnDef.getDataNode());
			if(qtnDef.getBindNode() != null && qtnDef.getBindNode().getParentNode() != null)
				qtnDef.getBindNode().getParentNode().removeChild(qtnDef.getBindNode());
		}

		if(formDef != null){
			formDef.removeQtnFromRules(qtnDef);
			formDef.removeQtnFromDynamicLists(qtnDef);
		}

		return questions.removeElement(qtnDef);
	}

	public boolean removeQuestionEx(QuestionDef qtnDef){
		return questions.removeElement(qtnDef);
	}

	public void removeAllQuestions(FormDef formDef){
		/*for(int i=0; i<questions.size(); i++)
			removeQuestion((QuestionDef)questions.elementAt(i));

		questions.removeAllElements();*/
		if(questions == null)
			return;

		while(questions.size() > 0)
			removeQuestion((QuestionDef)questions.elementAt(0),formDef);
	}

	public int size(){
		if(questions == null)
			return 0;
		return questions.size();
	}

	public void moveQuestionUp(QuestionDef questionDef){
		moveQuestionUp(questions,questionDef);
	}

	public static void moveQuestionUp(Vector questions, QuestionDef questionDef){
		int index = questions.indexOf(questionDef);

		//Not relying on group node because some forms have no groups
		Element controlNode = questionDef.getControlNode();
		Element parentNode = controlNode != null ? (Element)controlNode.getParentNode() : null;
		if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT && controlNode != null){
			controlNode = (Element)controlNode.getParentNode();
			parentNode = (Element)parentNode.getParentNode();
		}

		//if(parentNode == null && controlNode != null)
		//	return; //we have a problem here

		questions.remove(questionDef);

		//Store the question to replace
		QuestionDef currentQuestionDef = (QuestionDef)questions.elementAt(index-1);
		if(controlNode != null && parentNode != null && currentQuestionDef.getControlNode() != null)
			parentNode.removeChild(controlNode);

		if(!(questionDef.getVariableName().indexOf('/') > -1)){
			if(questionDef.getDataNode() != null && questionDef.getDataNode().getParentNode() != null && currentQuestionDef.getDataNode() != null)
				questionDef.getDataNode().getParentNode().removeChild(questionDef.getDataNode());
		}

		if(questionDef.getBindNode() != null && questionDef.getBindNode().getParentNode() != null && currentQuestionDef.getBindNode() != null)
			questionDef.getBindNode().getParentNode().removeChild(questionDef.getBindNode());

		List list = new ArrayList();
		while(questions.size() >= index){
			currentQuestionDef = (QuestionDef)questions.elementAt(index-1);
			list.add(currentQuestionDef);
			questions.remove(currentQuestionDef);
		}

		questions.add(questionDef);
		for(int i=0; i<list.size(); i++){
			if(i == 0 && controlNode != null){
				QuestionDef qtnDef = (QuestionDef)list.get(i);
				if(qtnDef.getControlNode() != null && parentNode != null){
					Node sibNode = qtnDef.getControlNode();
					if(qtnDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
						sibNode = sibNode.getParentNode();
					parentNode.insertBefore(controlNode, sibNode);
				}

				//move data node (We are not moving nested data nodes just to avoid complications
				if(!(questionDef.getVariableName().indexOf('/') > -1 || qtnDef.getVariableName().indexOf('/') > -1)){
					if(qtnDef.getDataNode() != null && qtnDef.getDataNode().getParentNode() != null && questionDef.getDataNode() != null)
						qtnDef.getDataNode().getParentNode().insertBefore(questionDef.getDataNode(), qtnDef.getDataNode());
				}

				//move binding node
				if(qtnDef.getBindNode() != null && qtnDef.getBindNode().getParentNode() != null && questionDef.getBindNode() != null)
					qtnDef.getBindNode().getParentNode().insertBefore(questionDef.getBindNode(), qtnDef.getBindNode());
			}
			questions.add(list.get(i));
		}
	}

	public void moveQuestionDown(QuestionDef questionDef){
		moveQuestionDown(questions,questionDef);
	}

	public static void moveQuestionDown(Vector questions, QuestionDef questionDef){
		int index = questions.indexOf(questionDef);	

		//Not relying on group node because some forms have no groups
		Element controlNode = questionDef.getControlNode();
		Element parentNode = controlNode != null ? (Element)controlNode.getParentNode() : null;
		if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT && controlNode != null){
			controlNode = (Element)controlNode.getParentNode();
			parentNode = (Element)parentNode.getParentNode();
		}

		//if(parentNode == null && controlNode != null)
		//	return; //we have a problem here

		questions.remove(questionDef);

		Node parentDataNode = questionDef.getDataNode() != null ? questionDef.getDataNode().getParentNode() : null;
		Node parentBindNode = questionDef.getBindNode() != null ? questionDef.getBindNode().getParentNode() : null;

		/*if(controlNode != null && parentNode != null)
			parentNode.removeChild(questionDef.getControlNode());

		if(questionDef.getDataNode() != null && questionDef.getDataNode().getParentNode() != null)
			questionDef.getDataNode().getParentNode().removeChild(questionDef.getDataNode());
		if(questionDef.getBindNode() != null && questionDef.getBindNode().getParentNode() != null)
			questionDef.getBindNode().getParentNode().removeChild(questionDef.getBindNode());*/

		QuestionDef currentItem; // = parent.getChild(index - 1);
		List list = new ArrayList();

		while(questions.size() > 0 && questions.size() > index){
			currentItem = (QuestionDef)questions.elementAt(index);
			list.add(currentItem);
			questions.remove(currentItem);
		}

		for(int i=0; i<list.size(); i++){
			if(i == 1){
				questions.add(questionDef); //Add after the first item.

				if(controlNode != null){
					if(controlNode != null && parentNode != null)
						parentNode.removeChild(controlNode);

					QuestionDef qtnDef = getNextSavedQuestion(list,i); //(QuestionDef)list.get(i);
					if(qtnDef.getControlNode() != null){
						Node sibNode = qtnDef.getControlNode();
						if(qtnDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
							sibNode = sibNode.getParentNode();
						parentNode.insertBefore(controlNode, sibNode);
					}
					else
						parentNode.appendChild(controlNode);


					//move data node (We are not moving nested data nodes just to avoid complications
					if(!(questionDef.getVariableName().indexOf('/') > -1 || qtnDef.getVariableName().indexOf('/') > -1))
						if(questionDef.getDataNode() != null && questionDef.getDataNode().getParentNode() != null){
							parentDataNode.removeChild(questionDef.getDataNode());

							if(qtnDef.getDataNode() != null){
								if(qtnDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT && qtnDef.getVariableName().contains("/"))
									parentDataNode.insertBefore(questionDef.getDataNode(), qtnDef.getDataNode().getParentNode());
								else
									parentDataNode.insertBefore(questionDef.getDataNode(), qtnDef.getDataNode());
							}
							else
								parentDataNode.appendChild(questionDef.getDataNode());
						}


					//move binding node
					if(parentBindNode != null){
						if(questionDef.getBindNode() != null && questionDef.getBindNode().getParentNode() != null)
							parentBindNode.removeChild(questionDef.getBindNode());

						if(qtnDef.getBindNode() != null)
							parentBindNode.insertBefore(questionDef.getBindNode(), qtnDef.getBindNode());
						else
							parentBindNode.appendChild(questionDef.getBindNode());
					}
				}
			}
			questions.add(list.get(i));
		}

		if(list.size() == 1){
			questions.add(questionDef);

			if(controlNode != null){
				if(questionDef.getControlNode() != null && parentNode != null){
					parentNode.removeChild(controlNode);
					parentNode.appendChild(controlNode);
				}

				if(!(questionDef.getVariableName().indexOf('/') > -1)){
					if(questionDef.getDataNode() != null && parentDataNode != null){
						parentDataNode.removeChild(questionDef.getDataNode());
						parentDataNode.appendChild(questionDef.getDataNode());
					}
				}

				//parentDataNode.insertBefore(questionDef.getDataNode(), questionDef.getDataNode());
				if(questionDef.getBindNode() != null && parentBindNode != null){
					parentBindNode.removeChild(questionDef.getBindNode());
					parentBindNode.appendChild(questionDef.getBindNode());
				}
				//parentBindNode.insertBefore(questionDef.getBindNode(), questionDef.getBindNode());
			}
		}
	}

	private static QuestionDef getNextSavedQuestion(List questions, int index){
		int size = questions.size();
		for(int i=index; i<size; i++){
			QuestionDef questionDef = (QuestionDef)questions.get(i);
			if(questionDef.getControlNode() != null)
				return questionDef;
		}
		return (QuestionDef)questions.get(index);
	}

	public boolean contains(QuestionDef qtn){
		return questions.contains(qtn);
	}

	public void updateDoc(Document doc, Element xformsNode, FormDef formDef, Element formNode, Element modelNode, boolean withData, String orgFormVarName){
		boolean allQuestionsNew = areAllQuestionsNew();
		if(labelNode == null && groupNode == null && allQuestionsNew) //Must be new page{
			XformConverter.fromPageDef2Xform(this,doc,xformsNode,formDef,formNode,modelNode);

		if(labelNode != null)
			XformUtil.setTextNodeValue(labelNode,name);

		if(groupNode != null)
			groupNode.setAttribute(XformConverter.ATTRIBUTE_NAME_ID, pageNo+"");

		Vector newQuestions = new Vector();
		if(questions != null){
			for(int i=0; i<questions.size(); i++){
				QuestionDef questionDef = (QuestionDef)questions.elementAt(i);
				if(!allQuestionsNew && questionDef.getDataNode() == null)
					newQuestions.add(questionDef);

				if(questionDef.updateDoc(doc,xformsNode,formDef,formNode,modelNode,(groupNode == null) ? xformsNode : groupNode,true,withData, orgFormVarName)){
					//for(int k=0; k<i; k++)
					//moveQuestionUp(questionDef);
				}
			}
		}

		for(int k = 0; k < newQuestions.size(); k++){
			QuestionDef questionDef = (QuestionDef)newQuestions.elementAt(k);
			
			//We do not update data nodes which deal with attributes.
			if(questionDef.getDataNode() == null && !questionDef.getVariableName().contains("@")){
				Window.alert(LocaleText.get("missingDataNode") + questionDef.getText());
				continue; //TODO This is a bug which should be resolved
			}

			int proposedIndex = questions.size() - (newQuestions.size() - k);
			int currentIndex = questions.indexOf(questionDef);
			if(currentIndex == proposedIndex)
				continue;

			moveQuestionNodesUp(questionDef,getRefQuestion(questions,newQuestions,currentIndex /*currentIndex+1*/));
		}
	}

	private QuestionDef getRefQuestion(Vector questions, Vector newQuestions, int index){
		QuestionDef questionDef;
		int i = index + 1;
		while(i < questions.size()){
			questionDef = (QuestionDef)questions.get(i);
			if(!newQuestions.contains(questionDef))
				return questionDef;
			i++;
		}

		return null;
	}

	private boolean areAllQuestionsNew(){
		if(questions == null)
			return false;

		for(int i=0; i<questions.size(); i++){
			QuestionDef questionDef = (QuestionDef)questions.elementAt(i);
			if(questionDef.getControlNode() != null)
				return false;
		}
		return true;
	}

	public QuestionDef getQuestionWithText(String text){
		if(questions == null)
			return null;

		for(int i=0; i<questions.size(); i++){
			QuestionDef questionDef = (QuestionDef)questions.elementAt(i);
			if(questionDef.getText().equals(text))
				return questionDef;
			else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT){ //TODO Need to make sure this new addition does not introduce bugs
				questionDef = questionDef.getRepeatQtnsDef().getQuestionWithText(text);
				if(questionDef != null)
					return questionDef;
			}
		}
		return null;
	}

	/**
	 * Updates this pageDef (as the main) with the parameter one
	 * 
	 * @param pageDef
	 */
	public void refresh(PageDef pageDef){
		if(pageNo == pageDef.getPageNo())
			name = pageDef.getName();

		int count = pageDef.getQuestionCount();
		for(int index = 0; index < count; index++){
			QuestionDef qtn = pageDef.getQuestionAt(index);
			QuestionDef questionDef = this.getQuestion(qtn.getVariableName());
			if(questionDef == null)
				continue; //Possibly this question was deleted on server
			questionDef.refresh(qtn);
		}
	}

	public void moveQuestionNodesUp(QuestionDef questionDef, QuestionDef refQuestionDef){

		//Not relying on group node because some forms have no groups
		Element controlNode = questionDef.getControlNode();
		Element parentNode = controlNode != null ? (Element)controlNode.getParentNode() : null;
		if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT && controlNode != null){
			controlNode = (Element)controlNode.getParentNode();
			parentNode = (Element)parentNode.getParentNode();
		}

		if(controlNode != null)
			parentNode.removeChild(controlNode);

		if(questionDef.getDataNode() != null)
			questionDef.getDataNode().getParentNode().removeChild(questionDef.getDataNode());

		if(questionDef.getBindNode() != null)
			questionDef.getBindNode().getParentNode().removeChild(questionDef.getBindNode());

		if(refQuestionDef.getControlNode() != null){
			Node sibNode = refQuestionDef.getControlNode();
			if(refQuestionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
				sibNode = sibNode.getParentNode();
			parentNode.insertBefore(controlNode, sibNode);
		}

		if(refQuestionDef.getDataNode() != null)
			refQuestionDef.getDataNode().getParentNode().insertBefore(questionDef.getDataNode(), refQuestionDef.getDataNode());

		if(refQuestionDef.getBindNode() != null)
			refQuestionDef.getBindNode().getParentNode().insertBefore(questionDef.getBindNode(), refQuestionDef.getBindNode());

	}

	public void updateDataNodes(Element parentDataNode){
		if(questions == null)
			return;

		for(int i=0; i<questions.size(); i++)
			((QuestionDef)questions.elementAt(i)).updateDataNodes(parentDataNode);
	}

	public void buildLanguageNodes(com.google.gwt.xml.client.Document doc, Element parentLangNode){
		if(labelNode == null || groupNode == null)
			return;

		String xpath = FormUtil.getNodePath(groupNode);
		String id = groupNode.getAttribute(XformConverter.ATTRIBUTE_NAME_ID);
		if(id != null && id.trim().length() > 0)
			xpath += "[@" + XformConverter.ATTRIBUTE_NAME_ID + "='" + id + "']";

		Element node = doc.createElement(XformConverter.NODE_NAME_TEXT);
		node.setAttribute(XformConverter.ATTRIBUTE_NAME_XPATH,  xpath + "/" + FormUtil.getNodeName(labelNode));
		node.setAttribute(XformConverter.ATTRIBUTE_NAME_VALUE, name);
		parentLangNode.appendChild(node);

		if(questions == null)
			return;

		for(int i=0; i<questions.size(); i++)
			((QuestionDef)questions.elementAt(i)).buildLanguageNodes(xpath+"/",doc,groupNode,parentLangNode);
	}

	public void clearChangeListeners(){
		if(questions == null)
			return;

		for(int i=0; i<questions.size(); i++)
			((QuestionDef)questions.elementAt(i)).clearChangeListeners();
	}
}
