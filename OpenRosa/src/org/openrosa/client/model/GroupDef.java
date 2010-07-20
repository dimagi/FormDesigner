package org.openrosa.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openrosa.client.util.ItextParser;
import org.openrosa.client.xforms.XformBuilder;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.ModelConstants;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.xforms.XformConstants;
import org.purc.purcforms.client.xforms.XformUtil;
import org.purc.purcforms.client.xforms.XmlUtil;

import com.google.gwt.user.client.Window;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;


/** The definition of a group of questions on a questionaire. 
 * 
 * @author Daniel Kayiwa
 *
 */
public class GroupDef implements IFormElement, Serializable{

	/** List of children for this group. */
	private List<IFormElement> children;

	/** The name of the group. */
	private String name = ModelConstants.EMPTY_STRING;

	/** The help text of the group. */
	private String helpText = ModelConstants.EMPTY_STRING;

	/** The xforms label node for this group. */
	private Element labelNode;

	/** The xforms hint node for this group. */
	private Element hintNode;

	/** The xforms group node for this page. */
	private Element groupNode;

	private Element bindNode;

	private Element dataNode;

	/** The parent definition to which this group belongs. */
	private IFormElement parent;

	private String binding;

	private int id;

	private String itextId;

	private int dataType = QuestionDef.QTN_TYPE_GROUP;


	public GroupDef(){

	}

	/**
	 * Constructs a new page.
	 * 
	 * @param parent the parent element to which the page belongs.
	 */
	public GroupDef(IFormElement parent) {
		this.parent = parent;
	}

	/**
	 * Creates a new copy of a page from an existing one.
	 * 
	 * @param pageDef the page to copy.
	 * @param parent the form to which the page belongs.
	 */
	public GroupDef(GroupDef pageDef,IFormElement parent) {
		this(parent);
		setName(pageDef.getName());
		setChildren(pageDef.getChildren());
		setItextId(pageDef.getItextId());
	}

	/**
	 * Constructs a page object with the following parameters.
	 * 
	 * @param name the name of the page.
	 * @param pageNo the number of the page.
	 * @param parent the form to which the page belongs.
	 */
	public GroupDef(String name,IFormElement parent) {
		this(parent);
		setName(name);
		setChildren(children);
	}

	/**
	 * Constructs a page with the following parameters.
	 * 
	 * @param name the name of the page.
	 * @param pageNo the number of the page.
	 * @param questions a list of questions in the page.
	 * @param parent the form to which the page belongs.
	 */
	public GroupDef(String name,List<IFormElement> children, IFormElement parent) {
		this(parent);
		setName(name);
		setChildren(children);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public IFormElement getParent() {
		return parent;
	}

	public void setParent(IFormElement parent) {
		this.parent = parent;
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

		if(itextId == null)
			setItextId(ItextParser.getItextId(labelNode));
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

	/**
	 * Gets the number of questions on this page.
	 * 
	 * @return the number of questions.
	 */
	public int getChildCount(){
		if(children == null)
			return 0;

		int count = 0;
		for(int index = 0; index < children.size(); index++){
			IFormElement element = children.get(index);
			if(element instanceof GroupDef)
				count += ((GroupDef)element).getChildCount();
			else{
				assert(element instanceof QuestionDef);
				count += 1;
			}
		}

		return count;
	}


	/**
	 * Gets the question at a given position on this page.
	 * 
	 * @param index the position.
	 * @return the question.
	 */
	public IFormElement getChildAt(int index){
		if(children == null)
			return null;
		return (IFormElement)children.get(index);
	}


	/**
	 * Adds a question to the page.
	 * 
	 * @param qtn the question to add.
	 */
	public void addChild(IFormElement child){
		if(children == null)
			children = new ArrayList<IFormElement>();
		children.add(child);
		child.setParent(this);
	}


	/**
	 * Gets a question with a given variable name.
	 * 
	 * @param varName the question variable name.
	 * @return the question.
	 */
	public IFormElement getElement(String varName){
		if(children == null)
			return null;

		for(int i=0; i<children.size(); i++){
			IFormElement def = children.get(i);
			if(varName.equals(def.getBinding()))
				return def;

			//Without this, then we have not validation and skip rules in repeat questions.
			if(def instanceof GroupDef){
				IFormElement elem = ((GroupDef)def).getElement(varName);
				if(elem != null)
					return elem;
			}
			/*if(def.getDataType() == QuestionDef.QTN_TYPE_REPEAT && def.getRepeatQtnsDef() != null){
				def = def.getRepeatQtnsDef().getElement(varName);
				if(def != null)
					return def;
			}*/
		}

		return null;
	}

	public QuestionDef getQuestion(String varName){
		return (QuestionDef)getElement(varName);
	}

	public QuestionDef getQuestion(int id){
		return (QuestionDef)getElement(id);
	}


	public int getElementIndex(String varName){
		if(children == null)
			return -1;

		for(int i=0; i<children.size(); i++){
			IFormElement def = children.get(i);
			if(varName.equals(def.getBinding()))
				return i;
		}

		return -1;
	}


	/**
	 * Gets a question with a given identifier.
	 * 
	 * @param id the question identifier.
	 * @return the question.
	 */
	public IFormElement getElement(int id){
		if(children == null)
			return null;

		for(int i=0; i<children.size(); i++){
			IFormElement def = children.get(i);
			if(def.getId() == id)
				return def;

			//Without this, then we have not validation and skip rules in repeat questions.
			if(def instanceof GroupDef){
				IFormElement elem = ((GroupDef)def).getElement(id);
				if(elem != null)
					return elem;
			}

			/*if(def.getDataType() == QuestionDef.QTN_TYPE_REPEAT && def.getRepeatQtnsDef() != null){
				def = def.getRepeatQtnsDef().getElement(id);
				if(def != null)
					return def;
			}*/
		}

		return null;
	}


	@Override
	public String toString() {
		return getName();
	}


	/**
	 * Copies a list of questions into this page.
	 * 
	 * @param questions the list of questions to copy.
	 */
	private void copyChildren(List<IFormElement> children){
		if(children != null){
			this.children = new ArrayList<IFormElement>();
			for(int i=0; i<children.size(); i++){
				IFormElement child = children.get(i);
				if(child instanceof QuestionDef)
					this.children.add((IFormElement)new QuestionDef((QuestionDef)children.get(i),this));
				else if(child instanceof GroupDef)
					this.children.add((IFormElement)new GroupDef((GroupDef)children.get(i),this));
			}
		}
	}


	/**
	 * Removes a question from this page.
	 * 
	 * @param qtnDef the question to remove.
	 * @param formDef the form to which this page belongs.
	 * @return true if the question was found and removed successfully, else false.
	 */
	public boolean removeElement(IFormElement qtnDef, FormDef formDef, boolean delete){
		removeElement2(qtnDef,formDef, delete);
		//TODO Need to do recursive checks for group defs before remove.
		return children.remove(qtnDef);
	}

	public static void removeElement2(IFormElement qtnDef, FormDef formDef, boolean delete){
		if(qtnDef.getControlNode() != null && qtnDef.getControlNode().getParentNode() != null){
			if(qtnDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
				qtnDef.getControlNode().getParentNode().getParentNode().removeChild(qtnDef.getControlNode().getParentNode());
			else
				qtnDef.getControlNode().getParentNode().removeChild(qtnDef.getControlNode());
		}

		//Either no / or just one occurrence. More than one nestings are avoided to make things simple
		if(qtnDef.getBinding() != null && qtnDef.getBinding().indexOf('/') == qtnDef.getBinding().lastIndexOf('/')){
			if(qtnDef.getDataNode() != null && qtnDef.getDataNode().getParentNode() != null)
				qtnDef.getDataNode().getParentNode().removeChild(qtnDef.getDataNode());
			if(qtnDef.getBindNode() != null && qtnDef.getBindNode().getParentNode() != null)
				qtnDef.getBindNode().getParentNode().removeChild(qtnDef.getBindNode());
		}

		if(formDef != null){
			formDef.removeQtnFromRules(qtnDef);
			formDef.removeQtnFromDynamicLists(qtnDef);
		}
	}


	/**
	 * Removes all questions from this page.
	 * 
	 * @param formDef the form to which the page belongs.
	 */
	public void removeAllElements(FormDef formDef){
		if(children == null)
			return;

		while(children.size() > 0)
			removeElement((QuestionDef)children.get(0),formDef, true);
	}


	/**
	 * Gets the number of questions on this page.
	 * 
	 * @return the number of questions.
	 */
	public int size(){
		if(children == null)
			return 0;
		return children.size();
	}


	/**
	 * Moves a question up by one position in the page.
	 * 
	 * @param questionDef the question to move.
	 */
	public void moveElementUp(IFormElement questionDef){
		moveElementUp(children,questionDef);
	}


	/**
	 * Moves a question up by one position in a list of questions.
	 * 
	 * @param questions the list of questions.
	 * @param questionDef the question to move.
	 */
	public static void moveElementUp(List<IFormElement> children, IFormElement questionDef){
		int index = children.indexOf(questionDef);

		//Not relying on group node because some forms have no groups
		Element controlNode = questionDef.getControlNode();
		Element parentNode = controlNode != null ? (Element)controlNode.getParentNode() : null;
		if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT && controlNode != null){
			controlNode = (Element)controlNode.getParentNode();
			parentNode = (Element)parentNode.getParentNode();
		}

		children.remove(questionDef);

		//Store the question to replace
		IFormElement currentElement = children.get(index-1);
		if(controlNode != null && parentNode != null && currentElement.getControlNode() != null)
			parentNode.removeChild(controlNode);

		if(!(questionDef.getBinding().indexOf('/') > -1)){
			if(questionDef.getDataNode() != null && questionDef.getDataNode().getParentNode() != null && currentElement.getDataNode() != null)
				questionDef.getDataNode().getParentNode().removeChild(questionDef.getDataNode());
		}

		if(questionDef.getBindNode() != null && questionDef.getBindNode().getParentNode() != null && currentElement.getBindNode() != null)
			questionDef.getBindNode().getParentNode().removeChild(questionDef.getBindNode());

		List<IFormElement> list = new ArrayList<IFormElement>();
		while(children.size() >= index){
			currentElement = (IFormElement)children.get(index-1);
			list.add(currentElement);
			children.remove(currentElement);
		}

		children.add(questionDef);
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
				if(!(questionDef.getBinding().indexOf('/') > -1 || qtnDef.getBinding().indexOf('/') > -1)){
					if(qtnDef.getDataNode() != null && qtnDef.getDataNode().getParentNode() != null && questionDef.getDataNode() != null)
						qtnDef.getDataNode().getParentNode().insertBefore(questionDef.getDataNode(), qtnDef.getDataNode());
				}

				//move binding node
				if(qtnDef.getBindNode() != null && qtnDef.getBindNode().getParentNode() != null && questionDef.getBindNode() != null)
					qtnDef.getBindNode().getParentNode().insertBefore(questionDef.getBindNode(), qtnDef.getBindNode());
			}
			children.add(list.get(i));
		}
	}


	/**
	 * Moves a question down by one position in the page.
	 * 
	 * @param questionDef the question to move.
	 */
	public void moveElementDown(IFormElement questionDef){
		moveElementDown(children,questionDef);
	}


	/**
	 * Moves a question down by one position in a list of questions.
	 * 
	 * @param elements the list of questions.
	 * @param element the question to move.
	 */
	public static void moveElementDown(List<IFormElement> elements, IFormElement element){
		int index = elements.indexOf(element);	

		//Not relying on group node because some forms have no groups
		Element controlNode = element.getControlNode();
		Element parentNode = controlNode != null ? (Element)controlNode.getParentNode() : null;
		if(element.getDataType() == QuestionDef.QTN_TYPE_REPEAT && controlNode != null){
			controlNode = (Element)controlNode.getParentNode();
			parentNode = (Element)parentNode.getParentNode();
		}

		elements.remove(element);

		Node parentDataNode = element.getDataNode() != null ? element.getDataNode().getParentNode() : null;
		Node parentBindNode = element.getBindNode() != null ? element.getBindNode().getParentNode() : null;

		/*if(controlNode != null && parentNode != null)
			parentNode.removeChild(questionDef.getControlNode());

		if(questionDef.getDataNode() != null && questionDef.getDataNode().getParentNode() != null)
			questionDef.getDataNode().getParentNode().removeChild(questionDef.getDataNode());
		if(questionDef.getBindNode() != null && questionDef.getBindNode().getParentNode() != null)
			questionDef.getBindNode().getParentNode().removeChild(questionDef.getBindNode());*/

		IFormElement currentItem; // = parent.getChild(index - 1);
		List<IFormElement> list = new ArrayList<IFormElement>();

		while(elements.size() > 0 && elements.size() > index){
			currentItem = elements.get(index);
			list.add(currentItem);
			elements.remove(currentItem);
		}

		for(int i=0; i<list.size(); i++){
			if(i == 1){
				elements.add(element); //Add after the first item.

				if(controlNode != null){
					if(controlNode != null && parentNode != null)
						parentNode.removeChild(controlNode);

					IFormElement qtnDef = getNextSavedElement(list,i); //(QuestionDef)list.get(i);
					if(qtnDef.getControlNode() != null){
						Node sibNode = qtnDef.getControlNode();
						if(qtnDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
							sibNode = sibNode.getParentNode();
						parentNode.insertBefore(controlNode, sibNode);
					}
					else
						parentNode.appendChild(controlNode);


					//move data node (We are not moving nested data nodes just to avoid complications
					if(!(element.getBinding().indexOf('/') > -1 || qtnDef.getBinding().indexOf('/') > -1))
						if(element.getDataNode() != null && element.getDataNode().getParentNode() != null){
							parentDataNode.removeChild(element.getDataNode());

							if(qtnDef.getDataNode() != null){
								if(qtnDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT && qtnDef.getBinding().contains("/"))
									parentDataNode.insertBefore(element.getDataNode(), qtnDef.getDataNode().getParentNode());
								else
									parentDataNode.insertBefore(element.getDataNode(), qtnDef.getDataNode());
							}
							else
								parentDataNode.appendChild(element.getDataNode());
						}


					//move binding node
					if(parentBindNode != null){
						if(element.getBindNode() != null && element.getBindNode().getParentNode() != null)
							parentBindNode.removeChild(element.getBindNode());

						if(qtnDef.getBindNode() != null)
							parentBindNode.insertBefore(element.getBindNode(), qtnDef.getBindNode());
						else
							parentBindNode.appendChild(element.getBindNode());
					}
				}
			}
			elements.add(list.get(i));
		}

		if(list.size() == 1){
			elements.add(element);

			if(controlNode != null){
				if(element.getControlNode() != null && parentNode != null){
					parentNode.removeChild(controlNode);
					parentNode.appendChild(controlNode);
				}

				if(!(element.getBinding().indexOf('/') > -1)){
					if(element.getDataNode() != null && parentDataNode != null){
						parentDataNode.removeChild(element.getDataNode());
						parentDataNode.appendChild(element.getDataNode());
					}
				}

				//parentDataNode.insertBefore(questionDef.getDataNode(), questionDef.getDataNode());
				if(element.getBindNode() != null && parentBindNode != null){
					parentBindNode.removeChild(element.getBindNode());
					parentBindNode.appendChild(element.getBindNode());
				}
				//parentBindNode.insertBefore(questionDef.getBindNode(), questionDef.getBindNode());
			}
		}
	}


	/**
	 * Gets the next question which has been converted to xforms and 
	 * hence attached to an xforms document node, starting at a given 
	 * index in a list of questions.
	 * 
	 * @param questions the list of questions.
	 * @param index the index to start from in the questions list.
	 * @return the question.
	 */
	private static IFormElement getNextSavedElement(List<IFormElement> questions, int index){
		int size = questions.size();
		for(int i=index; i<size; i++){
			IFormElement questionDef = questions.get(i);
			if(questionDef.getControlNode() != null)
				return questionDef;
		}
		return questions.get(index);
	}


	/**
	 * Checks if this page contains a particular question.
	 * 
	 * @param qtn the question to check.
	 * @return true if it contains, else false.
	 */
	public boolean contains(IFormElement qtn){
		return children.contains(qtn);
	}


	/**
	 * Updates the xforms document nodes referenced by this page and all its children.
	 * 
	 * @param doc the xforms document.
	 * @param xformsNode the xforms document root node.
	 * @param formDef the form to which this page belongs.
	 * @param formNode the xforms instance data node.
	 * @param modelNode the xforms model node.
	 * @param withData set to true to also update the xforms instance data values from question answers.
	 * @param orgFormVarName the original form variable name before any updates were done.
	 */
	public void updateDoc(Document doc, Element xformsNode, FormDef formDef, Element formNode, Element modelNode, boolean withData, String orgFormVarName){
		boolean allQuestionsNew = areAllQuestionsNew();
		if(labelNode == null && groupNode == null /*&& allQuestionsNew*/) //Must be new page{
			XformBuilder.fromPageDef2Xform(this,doc,xformsNode,formDef,formNode,modelNode);

		if(dataType != QuestionDef.QTN_TYPE_REPEAT){ //For repeats, the group node is named repeat
			if(groupNode != null && !groupNode.getNodeName().contains(XformConstants.NODE_NAME_GROUP_MINUS_PREFIX)){
				String nodeName = groupNode.getNodeName();
				String xml = groupNode.toString();
				xml = xml.replace(nodeName, XformConstants.NODE_NAME_GROUP);
				Element child = XformUtil.getNode(xml);
				child = (Element)groupNode.getOwnerDocument().importNode(child, true);
				groupNode.getParentNode().replaceChild(child, groupNode);
				groupNode =  child;
			}
		}

		if(labelNode != null)
			XmlUtil.setTextNodeValue(labelNode,name);

		//if(groupNode != null)
		//	groupNode.setAttribute(XformConstants.ATTRIBUTE_NAME_ID, pageNo+"");

		List<IFormElement> newElements = new ArrayList<IFormElement>();
		if(children != null){
			for(int i=0; i<children.size(); i++){
				IFormElement questionDef = children.get(i);
				if(!allQuestionsNew && questionDef instanceof QuestionDef && questionDef.getDataNode() == null)
					newElements.add(questionDef);

				if(questionDef instanceof QuestionDef)
					((QuestionDef)questionDef).updateDoc(doc,xformsNode,formDef,formNode,modelNode,(groupNode == null) ? xformsNode : groupNode, dataType != QuestionDef.QTN_TYPE_REPEAT, withData, orgFormVarName);
				else
					((GroupDef)questionDef).updateDoc(doc,xformsNode,formDef,formNode,modelNode,withData,orgFormVarName);
			}
		}

		for(int k = 0; k < newElements.size(); k++){
			IFormElement element = newElements.get(k);

			//We do not update data nodes which deal with attributes.
			if(element.getDataNode() == null && !element.getBinding().contains("@")){
				Window.alert(LocaleText.get("missingDataNode") + element.getText());
				continue; //TODO This is a bug which should be resolved
			}

			int proposedIndex = children.size() - (newElements.size() - k);
			int currentIndex = children.indexOf(element);
			if(currentIndex == proposedIndex)
				continue;

			moveElementNodesUp(element,getRefElemement(children,newElements,currentIndex /*currentIndex+1*/));
		}
	}


	/**
	 * Gets the first question which is not new, in a given list of questions,
	 * starting at a given position.
	 *  
	 * @param questions the list of questions to traverse.
	 * @param newQuestions the list of new questions.
	 * @param index the position to start from.
	 * @return the question.
	 */
	private IFormElement getRefElemement(List<IFormElement> questions, List<IFormElement> newQuestions, int index){
		IFormElement questionDef;
		int i = index + 1;
		while(i < questions.size()){
			questionDef = questions.get(i);
			if(!newQuestions.contains(questionDef))
				return questionDef;
			i++;
		}

		return null;
	}


	/**
	 * Checks if all question on this page are new.
	 * 
	 * @return true if all are new, else false.
	 */
	private boolean areAllQuestionsNew(){
		if(children == null)
			return false;

		for(int i=0; i<children.size(); i++){
			IFormElement questionDef = children.get(i);
			if(questionDef.getControlNode() != null)
				return false;
		}
		return true;
	}


	/**
	 * Gets a question with a given text.
	 * 
	 * @param text the text.
	 * @return the question.
	 */
	public IFormElement getQuestionWithText(String text){
		if(children == null)
			return null;

		for(int i=0; i<children.size(); i++){
			IFormElement questionDef = children.get(i);
			if(text.equals(questionDef.getText()))
				return questionDef;

			if(questionDef instanceof GroupDef){
				IFormElement elem = ((GroupDef)questionDef).getQuestionWithText(text);
				if(elem != null)
					return elem;
			}

			/*else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT){ //TODO Need to make sure this new addition does not introduce bugs
				questionDef = questionDef.getRepeatQtnsDef().getQuestionWithText(text);
				if(questionDef != null)
					return questionDef;
			}*/
		}
		return null;
	}


	/**
	 * Updates this pageDef (as the main or new from a refresh xml) with the parameter one (existing or the one being refreshed)
	 * 
	 * @param groupDef
	 */
	public void refresh(GroupDef groupDef){
		//if(pageNo == pageDef.getPageNo())
		//	name = pageDef.getName();

		int count = groupDef.getChildCount();
		for(int index = 0; index < count; index++){
			IFormElement qtn = groupDef.getChildAt(index);
			IFormElement element = this.getElement(qtn.getBinding());
			if(element == null)
				continue; //Possibly this question was deleted on server
			element.refresh(qtn);

			/*int index1 = this.getQuestionIndex(qtn.getVariableName());
			if(index != index1 && index1 != -1 && index < this.getQuestionCount() - 1){
				this.getQuestions().removeElement(questionDef);
				this.getQuestions().insertElementAt(questionDef, index);
			}*/
		}
	}


	/**
	 * Moves the question xforms document nodes by one position upwards.
	 * 
	 * @param element the question whose xforms nodes to move. 
	 * @param refElement the question immediately above which we are to move.
	 */
	public void moveElementNodesUp(IFormElement element, IFormElement refElement){

		//Not relying on group node because some forms have no groups
		Element controlNode = element.getControlNode();
		Element parentNode = controlNode != null ? (Element)controlNode.getParentNode() : null;
		if(element.getDataType() == QuestionDef.QTN_TYPE_REPEAT && controlNode != null){
			controlNode = (Element)controlNode.getParentNode();
			parentNode = (Element)parentNode.getParentNode();
		}

		if(controlNode != null)
			parentNode.removeChild(controlNode);

		if(element.getDataNode() != null)
			element.getDataNode().getParentNode().removeChild(element.getDataNode());

		if(element.getBindNode() != null)
			element.getBindNode().getParentNode().removeChild(element.getBindNode());

		if(refElement.getControlNode() != null){
			Node sibNode = refElement.getControlNode();
			if(refElement.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
				sibNode = sibNode.getParentNode();
			parentNode.insertBefore(controlNode, sibNode);
		}

		if(refElement.getDataNode() != null)
			refElement.getDataNode().getParentNode().insertBefore(element.getDataNode(), refElement.getDataNode());

		if(refElement.getBindNode() != null)
			refElement.getBindNode().getParentNode().insertBefore(element.getBindNode(), refElement.getBindNode());

	}


	/**
	 * Updates the xforms instance data nodes referenced by this page's questions.
	 * 
	 * @param parentDataNode the parent data node for this page.
	 */
	public void updateDataNodes(Element parentDataNode){
		if(children == null)
			return;

		for(int i=0; i<children.size(); i++)
			((QuestionDef)children.get(i)).updateDataNodes(parentDataNode);
	}


	/**
	 * Builds the language translation nodes for text in this page and all its children.
	 * 
	 * @param doc the language translation document.
	 * @param parentLangNode the language parent node for the page language nodes.
	 */
	public void buildLanguageNodes(String xpath, com.google.gwt.xml.client.Document doc, Element parentLangNode){
		if(labelNode == null || groupNode == null)
			return;

		if(xpath == null)
			xpath = FormUtil.getNodePath(groupNode);
		else
			xpath += "/" + FormUtil.getNodeName(groupNode);

		String id = groupNode.getAttribute(XformConstants.ATTRIBUTE_NAME_ID);
		if(id != null && id.trim().length() > 0)
			xpath += "[@" + XformConstants.ATTRIBUTE_NAME_ID + "='" + id + "']";

		Element node = doc.createElement(XformConstants.NODE_NAME_TEXT);
		node.setAttribute(XformConstants.ATTRIBUTE_NAME_XPATH,  xpath + "/" + FormUtil.getNodeName(labelNode));
		node.setAttribute(XformConstants.ATTRIBUTE_NAME_VALUE, name);
		node.setAttribute(XformConstants.ATTRIBUTE_NAME_ID, itextId);
		parentLangNode.appendChild(node);

		if(children == null)
			return;

		for(int i=0; i<children.size(); i++){
			IFormElement element = children.get(i);
			if(element instanceof QuestionDef)
				((QuestionDef)element).buildLanguageNodes(xpath,doc,groupNode,parentLangNode);
			else
				((GroupDef)element).buildLanguageNodes(xpath, doc, parentLangNode);
		}
	}


	/**
	 * Removes all question change event listeners.
	 */
	public void clearChangeListeners(){
		if(children == null)
			return;

		for(int i=0; i<children.size(); i++)
			((QuestionDef)children.get(i)).clearChangeListeners();
	}

	public int getId(){
		return id;
	}

	public void setId(int id){
		this.id = id;
	}

	public String getText(){
		return name;
	}

	public void setText(String text){
		this.name = text;
	}

	public int getDataType(){
		return dataType;
	}

	public void setDataType(int dataType){
		this.dataType = dataType;
	}

	public String getBinding(){
		return binding;
	}

	public void setBinding(String binding){
		this.binding = binding;
	}

	public List<IFormElement> getChildren(){
		return children;
	}

	public void setChildren(List<IFormElement> children){
		this.children = children;
	}

	public Element getBindNode(){
		return bindNode;
	}

	public void setBindNode(Element bindNode){
		this.bindNode = bindNode;
	}

	public Element getDataNode(){
		return dataNode;
	}

	public void setDataNode(Element dataNode){
		this.dataNode = dataNode;
	}

	public void refresh(IFormElement element){

	}

	public Element getControlNode(){
		return groupNode;
	}

	public void setControlNode(Element controlNode){
		groupNode = controlNode;
	}

	public IFormElement copy(IFormElement parent){
		return new GroupDef(this, parent);
	}

	public String getDisplayText(){
		return name;
	}

	public String getItextId() {
		return itextId;
	}

	public void setItextId(String itextId) {
		this.itextId = itextId;
	}

	public String getHelpText(){
		return helpText;
	}

	public void setHelpText(String helpText){
		this.helpText = helpText;
	}

	public Element getHintNode(){
		return hintNode;
	}

	public void setHintNode(Element hintNode){
		this.hintNode = hintNode;
	}

	public boolean removeChild(IFormElement element){
		if(children == null)
			return false;

		if(children.remove(element))
			return true;

		for(IFormElement child : children){
			if(child.removeChild(element))
				return true;
		}

		return false;
		//return this.removeElement(qtnDef, formDef);
	}

	public FormDef getFormDef(){
		IFormElement element = getParent();
		if(parent instanceof FormDef)
			return (FormDef)element;

		return element.getFormDef();
	}

	public boolean isLocked(){
		return false;
	}

	public boolean isRequired(){
		return false;
	}

	public boolean isEnabled(){
		return true;
	}

	public boolean isVisible(){
		return true;
	}

	public String getDefaultValue(){
		return null;
	}
}
