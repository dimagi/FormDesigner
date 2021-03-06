package org.openrosa.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openrosa.client.util.Itext;
import org.openrosa.client.util.ItextParser;
import org.openrosa.client.xforms.XformBuilder;
import org.openrosa.client.locale.LocaleText;
import org.openrosa.client.model.ModelConstants;
import org.openrosa.client.util.FormUtil;
import org.openrosa.client.xforms.UiElementBuilder;
import org.openrosa.client.xforms.XformConstants;
import org.openrosa.client.xforms.XformUtil;
import org.openrosa.client.xforms.XmlUtil;

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
	private String defaultLabel = ModelConstants.EMPTY_STRING;

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
	
	private boolean required,enabled;
	
	private boolean hasAdvancedCalculate, hasAdvancedConstraint, hasAdvancedRelevant;
	
	private String advancedCalculate, advancedConstraint, advancedRelevant;
	


	public GroupDef(){

	}

	/**
	 * Constructs a new page.
	 * 
	 * @param parent the parent element to which the page belongs.
	 */
	public GroupDef(IFormElement parent) {
		this.parent = parent;
		required = false;
		enabled = true;
	}

	/**
	 * Creates a new copy of a page from an existing one.
	 * 
	 * @param pageDef the page to copy.
	 * @param parent the form to which the page belongs.
	 */
	public GroupDef(GroupDef pageDef,IFormElement parent) {
		this(parent);
		setQuestionID(pageDef.getQuestionID());
		setChildren(pageDef.getChildren());
		setItextId(pageDef.getItextId());
		setItextId(getQuestionID());
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
		setQuestionID(name);
		setChildren(children);
		setItextId(name);
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
		setQuestionID(name);
		setChildren(children);
		setItextId(name);
	}
	
	/**
	 * Gets the form to which this question belongs.
	 * 
	 * @return the form.
	 */
	public FormDef getParentFormDef(){
		return (FormDef)getParentFormDef(this);
	}

	private IFormElement getParentFormDef(IFormElement questionDef){
		IFormElement parent = questionDef.getParent();
		if(parent instanceof FormDef)
			return parent;
		return getParentFormDef(parent);
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

//		if(itextId == null)
//			setItextId(XmlUtil.getItextId(labelNode));
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
			if(varName.equals(def.getQuestionID()))
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
		return getDisplayText();
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
		if(qtnDef.getQuestionID() != null && qtnDef.getQuestionID().indexOf('/') == qtnDef.getQuestionID().lastIndexOf('/')){
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

		if(!(questionDef.getQuestionID().indexOf('/') > -1)){
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
				if(!(questionDef.getQuestionID().indexOf('/') > -1 || qtnDef.getQuestionID().indexOf('/') > -1)){
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
					if(!(element.getQuestionID().indexOf('/') > -1 || qtnDef.getQuestionID().indexOf('/') > -1))
						if(element.getDataNode() != null && element.getDataNode().getParentNode() != null){
							parentDataNode.removeChild(element.getDataNode());

							if(qtnDef.getDataNode() != null){
								if(qtnDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT && qtnDef.getQuestionID().contains("/"))
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

				if(!(element.getQuestionID().indexOf('/') > -1)){
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
	 * @param rootDataNodeName the original form variable name before any updates were done.
	 */
	public void updateDoc(Document doc, Element xformsNode, FormDef formDef, Element formNode, Element modelNode, boolean withData, String rootDataNodeName){
		boolean allQuestionsNew = areAllQuestionsNew();
		boolean needsDOMNodes = false;
		Element groupNode = (this.getDataType() == QuestionDef.QTN_TYPE_GROUP ? getGroupNode() : getParent().getControlNode());
		if(groupNode == null){
			XformBuilder.fromGroupDef2Xform(this, doc, xformsNode, formDef, formNode, modelNode);
			groupNode = this.getGroupNode();
			if(groupNode == null){
				throw new RuntimeException("Problem with getting groupNode for question: "+this.getDisplayText());
			}
		}
		Element labelNode = getLabelNode();
		if(this.getDataType() == QuestionDef.QTN_TYPE_GROUP){
			needsDOMNodes = (getLabelNode() == null && groupNode == null);
		}else if(this.getDataType() == QuestionDef.QTN_TYPE_REPEAT){
			needsDOMNodes = (getParent().getControlNode() == null && getParent().getLabelNode() == null);
		}
		if(needsDOMNodes){ //Must be new page{
			XformBuilder.fromGroupDef2Xform(this,doc,xformsNode,formDef,formNode,modelNode);
		}
		if(getDataType() == QuestionDef.QTN_TYPE_GROUP){
			if(groupNode != null && !groupNode.getNodeName().contains(XformConstants.NODE_NAME_GROUP_MINUS_PREFIX)){
				String nodeName = groupNode.getNodeName();
				String xml = groupNode.toString();
				xml = xml.replace(nodeName, XformConstants.NODE_NAME_GROUP);
				Element child = XformUtil.getNode(xml);
				child = (Element)groupNode.getOwnerDocument().importNode(child, true);
				groupNode.getParentNode().replaceChild(child, groupNode);
				groupNode =  child;
			}
			
			groupNode.setAttribute("nodeset", getDataNodesetPath());
			
		}
		
		
		if(labelNode != null){
			XmlUtil.setTextNodeValue(getLabelNode(),getText());
		}else{
			Element label =  doc.createElement(XformConstants.NODE_NAME_LABEL);
			label.appendChild(doc.createTextNode(getText()));
			groupNode.appendChild(label);
			this.setLabelNode(label);
		}
		
		UiElementBuilder.addItextRefs(labelNode, this);

		List<IFormElement> newElements = new ArrayList<IFormElement>();
		if(children != null){
			for(int i=0; i<children.size(); i++){
				IFormElement questionDef = children.get(i);
				if(!allQuestionsNew && questionDef instanceof QuestionDef && questionDef.getDataNode() == null){
					newElements.add(questionDef);
				}

				if(questionDef instanceof QuestionDef){
					((QuestionDef)questionDef).updateDoc(doc,xformsNode,formDef,formNode,modelNode,(groupNode == null) ? xformsNode : groupNode, dataType != QuestionDef.QTN_TYPE_REPEAT, withData, rootDataNodeName);
				}else{
					((GroupDef)questionDef).updateDoc(doc,xformsNode,formDef,formNode,modelNode,withData,rootDataNodeName);
				}
			}
		}

		for(int k = 0; k < newElements.size(); k++){
			IFormElement element = newElements.get(k);

			int proposedIndex = children.size() - (newElements.size() - k);
			int currentIndex = children.indexOf(element);
			if(currentIndex == proposedIndex)
				continue;

			moveElementNodesUp(element,getRefElemement(children,newElements,currentIndex /*currentIndex+1*/));
		}
	}
	
	public List<String> getAllChildrenItextIDs(){
		ArrayList<String> list = new ArrayList<String>();
		List<IFormElement> children = this.getChildren();
		if(children == null){ return new ArrayList<String>(); }
		
		for(IFormElement child : children){
			list.addAll(Itext.getFullAvailableTextForms(child.getItextId())); //get the child's ItextID(s)
			list.addAll(child.getAllChildrenItextIDs()); //recurse down
		}
		return list;
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
			if(questionDef.getControlNode() != null){
				return false;
			}
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
			if(text.equals(Itext.getDisplayText(questionDef))){
				return questionDef;
			}

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
			IFormElement element = this.getElement(qtn.getQuestionID());
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
		Element eControlNode = element.getControlNode();
		Element eControlParentNode = eControlNode != null ? (Element)eControlNode.getParentNode() : null;
		Element eDataNode = element.getDataNode();
		Element eBindNode = element.getBindNode();
		
		Element rBindNode = refElement.getBindNode();
		Element rControlNode = refElement.getControlNode();
		Element rDataNode = refElement.getDataNode();
		
		if(element.getDataType() == QuestionDef.QTN_TYPE_REPEAT && eControlNode != null){
			eControlNode = (Element)eControlNode.getParentNode();
			eControlParentNode = (Element)eControlParentNode.getParentNode();
		}

		if(eControlNode != null){
			eControlParentNode.removeChild(eControlNode);
		}

		if(eDataNode != null){
			eDataNode.getParentNode().removeChild(eDataNode);
		}

		if(eBindNode != null){
			eBindNode.getParentNode().removeChild(eBindNode);
		}

		if(rControlNode != null){
			Node sibNode = rControlNode;
			if(refElement.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
				sibNode = sibNode.getParentNode();
			if(eControlParentNode != null){
				eControlParentNode.insertBefore(eControlNode, sibNode);
			}
		}

		if(rDataNode != null){
			rDataNode.getParentNode().insertBefore(eDataNode, rDataNode);
		}

		if(rBindNode != null){
			Node parentBindOfRefElement = rBindNode.getParentNode();
			if(parentBindOfRefElement != null){
				parentBindOfRefElement.insertBefore(eBindNode, rBindNode);
			}
		}

	}

	/**
	 * Returns the element specified by varName.
	 * if varName matches the parent node, return self,
	 * else go through children elements and return a match
	 * If no match is found, return null.
	 * @param varName
	 * @return the IFormElement that matches varName or null if no match.
	 */
	public IFormElement getElement(String varName){
		if(varName == null || children == null)
			return null;
		
		if(getQuestionID().equals(varName)){
			return this;
		}
		
		IFormElement retElement;
		for(int i=0;i<children.size();i++){
			IFormElement def = children.get(i);
			retElement = def.getElement(varName);
			if(retElement != null){
				return retElement; //there should only ever be one match.
			}
		}
		
		return null;
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
		node.setAttribute(XformConstants.ATTRIBUTE_NAME_VALUE, defaultLabel);
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
		return defaultLabel;
	}

	public void setText(String text){
		this.defaultLabel = text;
	}

	public int getDataType(){
		return dataType;
	}

	public void setDataType(int dataType){
		if(dataType == QuestionDef.QTN_TYPE_REPEAT) throw new RuntimeException(); //this should never happen.
		this.dataType = dataType;
	}

	public String getQuestionID(){
		return binding;
	}

	public void setQuestionID(String binding){
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
		if(getText() != null && !getText().isEmpty()){
			return getText();
		}else{
			return getQuestionID();
		}
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
	
	/**
	 * Get the Nodeset ref that points to the data node where the question's answer will be stored.
	 * @return
	 */
	public String getDataNodesetPath(){
		if(getParent() == null){
			return "/"+getQuestionID();
		}else{
			return getParent().getDataNodesetPath() + "/"+getQuestionID();
		}
		
	}

	public boolean isLocked(){
		return false;
	}

	public boolean isRequired(){
		return required;
	}

	public boolean isEnabled(){
		return enabled;
	}

	public String getDefaultValue(){
		return null;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		
	}

	@Override
	public void setLocked(boolean locked) {
		return;
	}

	@Override
	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean hasUINode() {
		return true;
	}

	public void setHasUINode(boolean hasUINode) {
		return; //groups always have some kind of UI node. If it doesn't, you've screwed up.
	}
	
	public void moveChildToIndex(IFormElement child, int index) throws Exception{
		if(!children.contains(child)){
			throw new Exception("Child not in Children list!");
		}
		
		children.remove(child);
		int i = (index > children.size()) ? children.size() : index;
		try{
			children.add(i, child);
		}catch(Exception e){
			FormUtil.displayException(e);
		}
	}
	
	public boolean hasAdvancedCalculate() {
		return hasAdvancedCalculate;
	}

	public boolean hasAdvancedConstraint() {
		return hasAdvancedConstraint;
	}

	public boolean hasAdvancedRelevant() {
		return hasAdvancedRelevant;
	}

	public void setHasAdvancedCalculate(boolean enabled) {
		hasAdvancedCalculate = enabled;
	}

	public void setHasAdvancedConstraint(boolean enabled) {
		hasAdvancedConstraint = enabled;
	}

	public void setHasAdvancedRelevant(boolean enabled) {
		hasAdvancedRelevant = enabled;
	}

	public String getAdvancedCalculate() {
		return advancedCalculate;
	}

	public String getAdvancedConstraint() {
		return advancedConstraint;
	}

	public String getAdvancedRelevant() {
		return advancedRelevant;
	}

	public void setAdvancedCalculate(String calcValue) {
		advancedCalculate = calcValue;
	}

	public void setAdvancedConstraint(String constValue) {
		advancedConstraint = constValue;
	}

	public void setAdvancedRelevant(String releValue) {
		advancedRelevant = releValue;
	}
	
	public boolean insertChildAfter(IFormElement child, IFormElement target) {
		boolean isChildQuestionOrGroupDef = (child instanceof GroupDef || child instanceof QuestionDef);
		if(!isChildQuestionOrGroupDef){ return false; } //we don't want to insert OptionDefs into this list.
		return FormDef.insertChildBeforeOrAfter(child, target, this.children, FormDef.INSERT_AFTER);
	}
	
	public boolean insertChildBefore(IFormElement child, IFormElement target) {
		boolean isChildQuestionOrGroupDef = (child instanceof GroupDef || child instanceof QuestionDef);
		if(!isChildQuestionOrGroupDef){ return false; } //we don't want to insert OptionDefs into this list.
		return FormDef.insertChildBeforeOrAfter(child, target, this.children, FormDef.INSERT_BEFORE);
	}
}
