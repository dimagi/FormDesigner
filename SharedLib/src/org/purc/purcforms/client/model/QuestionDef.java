package org.purc.purcforms.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.purc.purcforms.client.controller.QuestionChangeListener;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.xforms.XformConverter;
import org.purc.purcforms.client.xpath.XPathExpression;
import org.zenika.widget.client.util.DateUtil;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;


/** 
 * This is the question definition properties.
 * 
 * @author Daniel Kayiwa
 *
 */
public class QuestionDef implements Serializable{

	public static final String TRUE_VALUE = "true";
	public static final String FALSE_VALUE = "false";

	public static final String TRUE_DISPLAY_VALUE = "Yes";
	public static final String FALSE_DISPLAY_VALUE = "No";

	/** The prompt text. The text the user sees. */
	private String text = PurcConstants.EMPTY_STRING;

	/** The help text. */
	private String helpText = PurcConstants.EMPTY_STRING;

	/** The type of question. eg Numeric,Date,Text etc. */
	private int dataType = QTN_TYPE_TEXT;

	/** The value supplied as answer if the user has not supplied one. */
	private String defaultValue;

	private String answer;

	//TODO For a smaller payload, may need to combine (mandatory,visible,enabled,locked) 
	//into bit fields forming one byte. This would be a saving of 3 bytes per question.
	/** A flag to tell whether the question is to be answered or is optional. */
	private boolean required = false;

	/** A flag to tell whether the question should be shown or not. */
	private boolean visible = true;

	/** A flag to tell whether the question should be enabled or disabled. */
	private boolean enabled = true;

	/** A flag to tell whether a question is to be locked or not. A locked question 
	 * is one which is visible, enabled, but cannot be edited.
	 */
	private boolean locked = false;

	//TODO We have a bug here when more than one question, on a form, have the 
	//same variable names.
	//TODO May not need to serialize this property for smaller pay load. Then we would just rely on the id.
	/** The text indentifier of the question. This is used by the users of the questionaire 
	 * but in code we use the dynamically generated numeric id for speed. 
	 */
	private String variableName = PurcConstants.EMPTY_STRING;

	/** The allowed set of values (OptionDef) for an answer of the question. 
	 * This also holds repeat sets of questions (RepeatQtnsDef) for the QTN_TYPE_REPEAT.
	 * This is an optimization aspect to prevent storing these guys diffently as 
	 * they can't both happen at the same time. The internal storage implementation of these
	 * repeats is hidden from the user by means of getRepeatQtnsDef() and setRepeatQtnsDef().
	 */
	private Object options;

	/** The numeric identifier of a question. When a form definition is being built, each question is 
	 * given a unique (on a form) id starting from 1 up to 127. The assumption is that one will never need to have
	 * a form with more than 127 questions for a mobile device (It would be too big).
	 */
	private int id = PurcConstants.NULL_ID;

	/** Text question type. */
	public static final int QTN_TYPE_TEXT = 1;

	/** Numeric question type. These are numbers without decimal points*/
	public static final int QTN_TYPE_NUMERIC = 2;

	/** Decimal question type. These are numbers with decimals */
	public static final int QTN_TYPE_DECIMAL = 3;

	/** Date question type. This has only date component without time. */
	public static final int QTN_TYPE_DATE = 4;

	/** Time question type. This has only time element without date*/
	public static final int QTN_TYPE_TIME = 5;

	/** This is a question with alist of options where not more than one option can be selected at a time. */
	public static final int QTN_TYPE_LIST_EXCLUSIVE = 6;

	/** This is a question with alist of options where more than one option can be selected at a time. */
	public static final int QTN_TYPE_LIST_MULTIPLE = 7;

	/** Date and Time question type. This has both the date and time components*/
	public static final int QTN_TYPE_DATE_TIME = 8;

	/** Question with true and false answers. */
	public static final int QTN_TYPE_BOOLEAN = 9;

	/** Question with repeat sets of questions. */
	public static final int QTN_TYPE_REPEAT = 10;

	/** Question with image. */
	public static final int QTN_TYPE_IMAGE = 11;

	private Element dataNode;
	private Element labelNode;
	private Element hintNode;
	private Element bindNode;
	private Element controlNode;
	private Element firstOptionNode;

	private List<QuestionChangeListener> changeListeners = new ArrayList<QuestionChangeListener>();

	private Object parent;


	/** This constructor is used mainly during deserialization. */
	public QuestionDef(Object parent){
		this.parent = parent;
	}

	/** The copy constructor. */
	public QuestionDef(QuestionDef questionDef, Object parent){
		this(parent);
		setId(questionDef.getId());
		setText(questionDef.getText());
		setHelpText(questionDef.getHelpText());
		setDataType(questionDef.getDataType());
		setDefaultValue(questionDef.getDefaultValue());
		setVisible(questionDef.isVisible());
		setEnabled(questionDef.isEnabled());
		setLocked(questionDef.isLocked());
		setRequired(questionDef.isRequired());
		setVariableName(questionDef.getVariableName());

		if(getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE)
			copyQuestionOptions(questionDef.getOptions());
		else if(getDataType() == QuestionDef.QTN_TYPE_REPEAT)
			this.options = new RepeatQtnsDef(questionDef.getRepeatQtnsDef());
	}

	public QuestionDef(int id,String text,  int type, String variableName,Object parent) {
		this(parent);
		setId(id);
		setText(text);
		setDataType(type);
		setVariableName(variableName);
	}

	/**
	 * Constructs a new question definition object from the supplied parameters.
	 * For String type parameters, they should NOT be NULL. They should instead be empty,
	 * for the cases of missing values.
	 * 
	 * @param id
	 * @param text
	 * @param helpText - The hint or help text. Should NOT be NULL.
	 * @param mandatory
	 * @param type
	 * @param defaultValue
	 * @param visible
	 * @param enabled
	 * @param locked
	 * @param variableName
	 * @param options
	 */
	public QuestionDef(int id,String text, String helpText, boolean mandatory, int type, String defaultValue, boolean visible, boolean enabled, boolean locked, String variableName, Object options,Object parent) {
		this(parent);
		setId(id);
		setText(text);
		setHelpText(helpText);
		setDataType(type);
		setDefaultValue(defaultValue);
		setVisible(visible);
		setEnabled(enabled);
		setLocked(locked);
		setRequired(mandatory);		
		setVariableName(variableName);
		setOptions(options);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		//if(defaultValue != null && defaultValue.trim().length() > 0)
		this.defaultValue = defaultValue;
		this.answer =  defaultValue;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		//if(defaultValue != null && defaultValue.trim().length() > 0)
		this.answer = answer;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;

		for(int index = 0; index < changeListeners.size(); index++)
			changeListeners.get(index).onEnabledChanged(enabled);
	}

	public String getHelpText() {
		return helpText;
	}

	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;

		for(int index = 0; index < changeListeners.size(); index++)
			changeListeners.get(index).onLockedChanged(locked);
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;

		for(int index = 0; index < changeListeners.size(); index++)
			changeListeners.get(index).onRequiredChanged(required);
	}

	public Vector getOptions() {
		//if(!(type == QTN_TYPE_LIST_EXCLUSIVE || type == QTN_TYPE_LIST_MULTIPLE))
		//	throw new Exception("Invalid Operation");
		return (Vector)options;
	}

	public void setOptions(Object options) {
		this.options = options;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {	
		this.text = text;
	}

	public int getDataType() {
		return dataType;
	}

	public void setDataType(int dataType) {
		this.dataType = dataType;

		for(int index = 0; index < changeListeners.size(); index++)
			changeListeners.get(index).onDataTypeChanged(dataType);
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;

		for(int index = 0; index < changeListeners.size(); index++)
			changeListeners.get(index).onBindingChanged(variableName);
	}

	public Object getParent() {
		return parent;
	}

	public void setParent(Object parent) {
		this.parent = parent;
	}

	/**
	 * @return the bindNode
	 */
	public Element getBindNode() {
		return bindNode;
	}

	/**
	 * @param bindNode the bindNode to set
	 */
	public void setBindNode(Element bindNode) {
		this.bindNode = bindNode;
	}

	/**
	 * @return the dataNode
	 */
	public Element getDataNode() {
		return dataNode;
	}

	/**
	 * @param dataNode the dataNode to set
	 */
	public void setDataNode(Element dataNode) {
		this.dataNode = dataNode;
	}

	/**
	 * @return the hintNode
	 */
	public Element getHintNode() {
		return hintNode;
	}

	/**
	 * @param hintNode the hintNode to set
	 */
	public void setHintNode(Element hintNode) {
		this.hintNode = hintNode;
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
	 * @return the controlNode
	 */
	public Element getControlNode() {
		return controlNode;
	}

	/**
	 * @param controlNode the controlNode to set
	 */
	public void setControlNode(Element controlNode) {
		this.controlNode = controlNode;
	}

	/**
	 * @return the firstOptionNode
	 */
	public Element getFirstOptionNode() {
		return firstOptionNode;
	}

	/**
	 * @param firstOptionNode the firstOptionNode to set
	 */
	public void setFirstOptionNode(Element firstOptionNode) {
		this.firstOptionNode = firstOptionNode;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;

		for(int index = 0; index < changeListeners.size(); index++)
			changeListeners.get(index).onVisibleChanged(visible);
	}

	public void removeChangeListener(QuestionChangeListener changeListener) {
		changeListeners.remove(changeListener);
	}

	public void addChangeListener(QuestionChangeListener changeListener) {
		if(!changeListeners.contains(changeListener))
			changeListeners.add(changeListener);
	}

	public void addOption(OptionDef optionDef){
		if(options == null)
			options = new Vector();
		((Vector)options).addElement(optionDef);
		optionDef.setParent(this);
	}

	public RepeatQtnsDef getRepeatQtnsDef(){
		return (RepeatQtnsDef)options;
	}

	public void addRepeatQtnsDef(QuestionDef qtn){
		if(options == null)
			options = new RepeatQtnsDef(qtn);
		((RepeatQtnsDef)options).addQuestion(qtn);
	}

	public void setRepeatQtnsDef(RepeatQtnsDef repeatQtnsDef){
		options = repeatQtnsDef;
	}

	public String toString() {
		return getText();
	}

	private void copyQuestionOptions(Vector options){
		this.options = new Vector();
		for(int i=0; i<options.size(); i++)
			((Vector)this.options).addElement(new OptionDef((OptionDef)options.elementAt(i),this));
	}

	public void removeOption(OptionDef optionDef){
		if(options instanceof Vector){ //Could be a RepeatQtnsDef
			((Vector)options).removeElement(optionDef);

			if(((Vector)options).size() == 0)
				firstOptionNode = null;
		}

		if(controlNode != null && optionDef.getControlNode() != null)
			controlNode.removeChild(optionDef.getControlNode());
	}

	public void moveOptionUp(OptionDef optionDef){
		if(!(getDataType()==QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
				getDataType()==QuestionDef.QTN_TYPE_LIST_MULTIPLE))
			return;

		Vector optns = (Vector)options;
		int index = optns.indexOf(optionDef);

		optns.remove(optionDef);

		if(optionDef.getControlNode() != null)
			controlNode.removeChild(optionDef.getControlNode());

		OptionDef currentOptionDef;
		List list = new ArrayList();

		//Remove all from index before selected all the way downwards
		while(optns.size() >= index){
			currentOptionDef = (OptionDef)optns.elementAt(index-1);
			list.add(currentOptionDef);
			optns.remove(currentOptionDef);
		}

		optns.add(optionDef);
		for(int i=0; i<list.size(); i++){
			if(i == 0){
				OptionDef optnDef = (OptionDef)list.get(i);
				if(optnDef.getControlNode() != null)
					controlNode.insertBefore(optionDef.getControlNode(), optnDef.getControlNode());
			}
			optns.add(list.get(i));
		}
	}

	public void moveOptionDown(OptionDef optionDef){
		if(!(getDataType()==QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
				getDataType()==QuestionDef.QTN_TYPE_LIST_MULTIPLE))
			return;

		Vector optns = (Vector)options;
		int index = optns.indexOf(optionDef);	

		optns.remove(optionDef);

		if(optionDef.getControlNode() != null)
			controlNode.removeChild(optionDef.getControlNode());

		OptionDef currentItem; // = parent.getChild(index - 1);
		List list = new ArrayList();

		//Remove all otions below selected index
		while(optns.size() > 0 && optns.size() > index){
			currentItem = (OptionDef)optns.elementAt(index);
			list.add(currentItem);
			optns.remove(currentItem);
		}

		for(int i=0; i<list.size(); i++){
			if(i == 1){
				optns.add(optionDef); //Add after the first item but before the current (second).

				OptionDef optnDef = (OptionDef)list.get(i);
				if(optnDef.getControlNode() != null)
					controlNode.insertBefore(optionDef.getControlNode(), optnDef.getControlNode());
			}
			optns.add(list.get(i));
		}

		//If was second last and hence becoming last
		if(list.size() == 1){
			optns.add(optionDef);

			if(optionDef.getControlNode() != null)
				controlNode.appendChild(optionDef.getControlNode());
		}
	}

	public boolean updateDoc(Document doc, Element xformsNode, FormDef formDef, Element formNode, Element modelNode,Element groupNode,boolean appendParentBinding, boolean withData){
		boolean isNew = controlNode == null;
		if(controlNode == null) //Must be new question.
			XformConverter.fromQuestionDef2Xform(this,doc,xformsNode,formDef,formNode,modelNode,groupNode);
		else
			updateControlNodeName();

		if(labelNode != null) //How can this happen
			XformConverter.setTextNodeValue(labelNode,text);

		Element node = bindNode;
		if(node == null)
			node = controlNode;
		if(node != null){
			String binding = variableName;
			if(!binding.startsWith("/"+ formDef.getVariableName()+"/") && appendParentBinding){
				//if(!binding.contains("/"+ formDef.getVariableName()+"/"))
				if(!binding.startsWith(formDef.getVariableName()+"/"))
					binding = "/"+ formDef.getVariableName()+"/" + binding;
				else{
					variableName = "/" + variableName; //correct user binding syntax error
					binding = variableName;
				}
			}
			if(dataType != QuestionDef.QTN_TYPE_REPEAT)
				node.setAttribute(XformConverter.ATTRIBUTE_NAME_TYPE, XformConverter.getXmlType(dataType));
			if(node.getAttribute(XformConverter.ATTRIBUTE_NAME_NODESET) != null)
				node.setAttribute(XformConverter.ATTRIBUTE_NAME_NODESET,binding);
			if(node.getAttribute(XformConverter.ATTRIBUTE_NAME_REF) != null)
				node.setAttribute(XformConverter.ATTRIBUTE_NAME_REF,binding);

			if(required)
				node.setAttribute(XformConverter.ATTRIBUTE_NAME_REQUIRED,XformConverter.XPATH_VALUE_TRUE);
			else
				node.removeAttribute(XformConverter.ATTRIBUTE_NAME_REQUIRED);

			if(!enabled)
				node.setAttribute(XformConverter.ATTRIBUTE_NAME_READONLY,XformConverter.XPATH_VALUE_TRUE);
			else
				node.removeAttribute(XformConverter.ATTRIBUTE_NAME_READONLY);

			if(locked)
				node.setAttribute(XformConverter.ATTRIBUTE_NAME_LOCKED,XformConverter.XPATH_VALUE_TRUE);
			else
				node.removeAttribute(XformConverter.ATTRIBUTE_NAME_LOCKED);

			if(dataNode != null)
				updateDataNode(doc,formDef);
		}

		if((getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
				getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE) && options != null){
			Vector optns = (Vector)options;
			for(int i=0; i<optns.size(); i++){
				OptionDef optionDef = (OptionDef)optns.elementAt(i);
				optionDef.updateDoc(doc,controlNode);
				if(i == 0)
					firstOptionNode = optionDef.getControlNode();
			}
		}
		else if(getDataType() == QuestionDef.QTN_TYPE_REPEAT)
			getRepeatQtnsDef().updateDoc(doc,xformsNode,formDef,formNode,modelNode,groupNode,withData);

		//Put after options because it depends on the firstOptionNode
		if(hintNode != null){
			if(helpText.trim().length() > 0)
				XformConverter.setTextNodeValue(hintNode,helpText);
			else{
				controlNode.removeChild(hintNode);
				hintNode = null;
			}
		}
		else if(hintNode == null && helpText.trim().length() > 0)
			XformConverter.addHelpTextNode(this, doc, controlNode, firstOptionNode);

		if(withData)
			updateNodeValue(doc,formNode,(answer != null) ? answer : defaultValue);
		else
			updateNodeValue(doc,formNode,defaultValue);

		return isNew;
	}

	public void updateNodeValue(FormDef formDef){
		updateNodeValue(formDef.getDoc(),formDef.getDataNode(),answer);
	}

	public void updateNodeValue(Document doc, Element formNode,String value){
		if((dataType == QuestionDef.QTN_TYPE_DATE || dataType == QuestionDef.QTN_TYPE_DATE_TIME)
				&& value != null && value.trim().length() > 0){


			DateTimeFormat formatter = FormUtil.getDateTimeFormat(); //DateTimeFormat.getFormat(); //new DateTimeFormat("yyyy-MM-dd");
			if(formatter != null)
				value = formatter.format(DateUtil.getDateTimeFormat().parse(value));
		}

		if(value != null && value.trim().length() > 0){
			if(variableName.contains("@"))
				updateAttributeValue(formNode,value);
			else if(dataNode != null){
				if(dataNode.getChildNodes().getLength() > 0)
					dataNode.getChildNodes().item(0).setNodeValue(value);
				else
					dataNode.appendChild(doc.createTextNode(value));
			}
		}
		else{
			//TODO Check to see that this does not remove child model node of repeats
			if(dataNode != null && dataType != QuestionDef.QTN_TYPE_REPEAT){
				if(variableName.contains("@"))
					updateAttributeValue(formNode,"");
				else{
					NodeList childNodes = dataNode.getChildNodes();
					while(childNodes.getLength() > 0)
						dataNode.removeChild(childNodes.item(0));
				}
			}
		}
	}

	private void updateAttributeValue(Element formNode, String value){
		String xpath = variableName;		
		Element elem = formNode; //(Element)formNode.getParentNode();

		if(dataType != QuestionDef.QTN_TYPE_REPEAT){
			//xpath = new String(xpath.toCharArray(), 1, xpath.length()-1);
			int pos = xpath.lastIndexOf('@'); String attributeName = null;
			if(pos > 0){
				attributeName = xpath.substring(pos+1,xpath.length());
				xpath = xpath.substring(0,pos-1);
			}
			else if(pos == 0){
				attributeName = variableName.substring(1,variableName.length());
				if(value != null && value.trim().length() > 0) //we are not allowing empty strings for now.
					formNode.setAttribute(attributeName, value);
				return;
			}
			XPathExpression xpls = new XPathExpression(elem, xpath);
			Vector result = xpls.getResult();

			for (Enumeration e = result.elements(); e.hasMoreElements();) {
				Object obj = e.nextElement();
				if (obj instanceof Element){
					if(pos > 0) //Check if we are to set attribute value.
						((Element) obj).setAttribute(attributeName, value);
					else
						((Element) obj).setNodeValue(value);
				}
			}
		}
		else //TODO Need to work on repeats
			;//updateRepeatModel(elem,qtnData);
	}

	private void updateDataNode(Document doc, FormDef formDef){
		if(variableName.contains("@"))
			return;

		String name = dataNode.getNodeName();
		if(name.equalsIgnoreCase(variableName)){
			if(dataType != QuestionDef.QTN_TYPE_REPEAT)
				return;
			if(dataType == QuestionDef.QTN_TYPE_REPEAT && formDef.getVariableName().equals(dataNode.getParentNode().getNodeName()))
				return;
		}

		if(variableName.contains("/") && name.equals(variableName.substring(variableName.lastIndexOf("/")+1)) && dataNode.getParentNode().getNodeName().equals(variableName.substring(0,variableName.indexOf("/"))))
			return;


		String xml = dataNode.toString();
		if(!variableName.contains("/")){
			xml = xml.replace(name, variableName);
			Element node = XformConverter.getNode(xml);
			Element parent = (Element)dataNode.getParentNode();
			if(formDef.getVariableName().equals(parent.getNodeName()))
				parent.replaceChild(node, dataNode);
			else
				parent.replaceChild(node, dataNode);
			//formDef.getDataNode().replaceChild(node, parent);

			dataNode = node;
		}
		else{
			String newName = variableName.substring(variableName.lastIndexOf("/")+1);
			if(!name.equals(newName)){
				xml = xml.replace(name, newName);
				Element node = XformConverter.getNode(xml);
				Element parent = (Element)dataNode.getParentNode();
				parent.replaceChild(node, dataNode);
				dataNode = node;
			}

			String parentName = variableName.substring(0,variableName.indexOf("/"));
			String parentNodeName = dataNode.getParentNode().getNodeName();
			if(!parentName.equalsIgnoreCase(parentNodeName)){
				if(variableName.equals(parentName+"/"+parentNodeName+"/"+name))
					return;

				Element parentNode = doc.createElement(parentName);
				//parentNode = EpihandyXform.getNode(parentNode.toString());
				Element parent = (Element)dataNode.getParentNode();
				Element node = (Element)dataNode.cloneNode(true);
				parentNode.appendChild(node);
				if(formDef.getVariableName().equals(parent.getNodeName()))
					parent.replaceChild(parentNode, dataNode);
				else
					//if(dataNode.getParentNode().getParentNode() != null)
					formDef.getDataNode().replaceChild(parentNode, dataNode.getParentNode());

				dataNode = node;
			}
		}

		String id = variableName;
		if(id.contains("/"))
			id = id.substring(id.lastIndexOf('/')+1);

		//update binding node
		if(bindNode != null && bindNode.getAttribute(XformConverter.ATTRIBUTE_NAME_ID) != null)
			bindNode.setAttribute(XformConverter.ATTRIBUTE_NAME_ID,id);

		//update control node referencing the binding
		if(controlNode != null&& controlNode.getAttribute(XformConverter.ATTRIBUTE_NAME_BIND) != null)
			controlNode.setAttribute(XformConverter.ATTRIBUTE_NAME_BIND,id);
		else if(controlNode != null && controlNode.getAttribute(XformConverter.ATTRIBUTE_NAME_REF) != null){
			/*String ref = controlNode.getAttribute(EpihandyXform.ATTRIBUTE_NAME_REF);
			if(!ref.contains("/"))
				controlNode.setAttribute(EpihandyXform.ATTRIBUTE_NAME_REF,variableName);
			else
				ref = ref.substring(0,ref.indexOf('/')) + variableName;*/
			controlNode.setAttribute(XformConverter.ATTRIBUTE_NAME_REF,id);
		}
	}

	private void updateControlNodeName(){
		//TODO How about cases where the prefix is not xf?
		String name = controlNode.getNodeName();
		Element parent = (Element)controlNode.getParentNode();
		String xml = controlNode.toString();
		boolean modified = false;

		if(name.equals(XformConverter.NODE_NAME_INPUT) &&
				dataType == QuestionDef.QTN_TYPE_LIST_MULTIPLE){
			xml = xml.replace(name, XformConverter.NODE_NAME_SELECT);
			modified = true;
		}
		else if(name.equals(XformConverter.NODE_NAME_INPUT) &&
				dataType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE){
			xml = xml.replace(name, XformConverter.NODE_NAME_SELECT1);
			modified = true;
		}
		else if(name.equals(XformConverter.NODE_NAME_SELECT) &&
				dataType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE){
			xml = xml.replace(name, XformConverter.NODE_NAME_SELECT1);
			modified = true;
		}
		else if(name.equals(XformConverter.NODE_NAME_SELECT1) &&
				dataType == QuestionDef.QTN_TYPE_LIST_MULTIPLE){
			xml = xml.replace(name, XformConverter.NODE_NAME_SELECT);
			modified = true;
		}
		else if((name.equals(XformConverter.NODE_NAME_SELECT1) || 
				name.equals(XformConverter.NODE_NAME_SELECT)) &&
				!(dataType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
						dataType == QuestionDef.QTN_TYPE_LIST_MULTIPLE)){
			xml = xml.replace(name, XformConverter.NODE_NAME_INPUT);
			modified = true;
		}

		if(modified){
			Element child = XformConverter.getNode(xml);
			parent.replaceChild(child, controlNode);
			controlNode =  child;
			updateControlNodeChildren();
		}
	}

	private void updateControlNodeChildren(){
		NodeList list = controlNode.getElementsByTagName(XformConverter.NODE_NAME_LABEL_MINUS_PREFIX);
		if(list.getLength() > 0)
			labelNode = (Element)list.item(0);

		list = controlNode.getElementsByTagName(XformConverter.NODE_NAME_HINT_MINUS_PREFIX);
		if(list.getLength() > 0)
			hintNode = (Element)list.item(0);

		if(dataType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
				dataType == QuestionDef.QTN_TYPE_LIST_MULTIPLE){
			Vector optns = (Vector)options;
			for(int i=0; i<optns.size(); i++){
				OptionDef optionDef = (OptionDef)optns.elementAt(i);
				updateOptionNodeChildren(optionDef);
				if(i == 0)
					firstOptionNode = optionDef.getControlNode();
			}
		}
	}

	private void updateOptionNodeChildren(OptionDef optionDef){
		int count = controlNode.getChildNodes().getLength();
		for(int i=0; i<count; i++){
			Node node = controlNode.getChildNodes().item(i);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			if(node.getNodeName().equals(XformConverter.NODE_NAME_ITEM)){
				NodeList list = ((Element)node).getElementsByTagName(XformConverter.NODE_NAME_LABEL_MINUS_PREFIX);
				if(list.getLength() == 0)
					continue;

				if(optionDef.getText().equals(XformConverter.getTextValue((Element)list.item(0)))){
					optionDef.setLabelNode((Element)list.item(0));
					optionDef.setControlNode((Element)node);

					list = ((Element)node).getElementsByTagName(XformConverter.NODE_NAME_VALUE_MINUS_PREFIX);
					if(list.getLength() > 0)
						optionDef.setValueNode((Element)list.item(0));
					return;
				}
			}
		}
	}

	public OptionDef getOptionWithText(String text){
		Vector list = (Vector)options;
		for(int i=0; i<list.size(); i++){
			OptionDef optionDef = (OptionDef)list.elementAt(i);
			if(optionDef.getText().equals(text))
				return optionDef;
		}
		return null;
	}

	public OptionDef getOptionWithValue(String value){
		Vector list = (Vector)options;
		for(int i=0; i<list.size(); i++){
			OptionDef optionDef = (OptionDef)list.elementAt(i);
			if(optionDef.getVariableName().equals(value))
				return optionDef;
		}
		return null;
	}

	public void refresh(QuestionDef questionDef){
		setText(questionDef.getText());
		setHelpText(questionDef.getHelpText());
		setDefaultValue(questionDef.getDefaultValue());
		setDataType(questionDef.getDataType());
		setEnabled(questionDef.isEnabled());
		setRequired(questionDef.isRequired());
		setLocked(questionDef.isLocked());
		setVisible(questionDef.isVisible());

		if((dataType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || dataType == QuestionDef.QTN_TYPE_LIST_MULTIPLE) &&
		  (questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE) )
			refreshOptions(questionDef);
		else if(dataType == QuestionDef.QTN_TYPE_REPEAT && questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
			questionDef.getRepeatQtnsDef().refresh(questionDef.getRepeatQtnsDef()); //TODO Finish this
	}
	
	private void refreshOptions(QuestionDef questionDef){
		Vector options2 = questionDef.getOptions();
		if(options == null || options2 == null)
			return;
		
		for(int index = 0; index < options2.size(); index++){
			OptionDef optn = (OptionDef)options2.get(index);
			OptionDef optionDef = this.getOptionWithValue(optn.getVariableName());
			if(optionDef == null)
				continue;
			optionDef.setText(optn.getText());
		}
	}
}

