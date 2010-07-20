package org.openrosa.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;

import org.openrosa.client.OpenRosaConstants;
import org.purc.purcforms.client.model.ModelConstants;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.xforms.XformConstants;
import org.purc.purcforms.client.xforms.XformUtil;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;

/**
 * Definition of a form. This has some meta data about the form definition and  
 * a collection of pages together with question branching or skipping rules.
 * A form is sent as defined in one language. For instance, those using
 * Swahili would get forms in that language, etc. We don't support runtime
 * changing of a form language in order to have a more efficient implementation
 * as a trade off for more flexibility which may not be used most of the times.
 * 
 * @author Daniel Kayiwa
 *
 */
public class FormDef implements IFormElement, Serializable{

	//TODO May not need to serialize this property for smaller pay load. Then we may just rely on the id.
	//afterall it is not even guaranteed to be unique.
	/** The string unique identifier of the form definition. */
	private String variableName = ModelConstants.EMPTY_STRING;

	/** The display name of the form. */
	private String name = ModelConstants.EMPTY_STRING;

	private String formKey = ModelConstants.EMPTY_STRING;

	/** The numeric unique identifier of the form definition. */
	private int id = ModelConstants.NULL_ID;

	/** The collection of rules (SkipRule objects) for this form. */
	private Vector skipRules;

	/** The collection of rules (ValidationRule objects) for this form. */
	private Vector validationRules;

	/** The collection of calculations (Calculation objects) for this form. */
	private Vector calculations;

	/** A string consisting for form fields that describe its data. eg description-template="${/data/question1}$ Market" */
	private String descriptionTemplate =  ModelConstants.EMPTY_STRING;

	/** A mapping of dynamic lists keyed by the id of the question whose values
	 *  determine possible values of another question as specified in the DynamicOptionDef object.
	 */
	private HashMap<Integer,DynamicOptionDef>  dynamicOptions;


	/** The xforms document.(for easy syncing between the object model and actual xforms document. */
	private Document doc;

	/** 
	 * The data node of the xform that this form represents.
	 * This is the node immediately under the instace node.
	 */
	private Element dataNode;

	/** The top level node of the xform that this form represents. */
	private Element xformsNode;

	/** The model node of the xform that this form represents. */
	private Element modelNode;
	
	/** The body node. */
	private Element bodyNode;

	/** The layout xml for this form. */
	private String layoutXml;

	/** The javascript source for this form. */
	private String javaScriptSource;

	/** The xforms xml for this form. */
	private String xformXml;

	/** The language xml for this form. */
	private String languageXml;

	/** 
	 * Flag to determine if we can change the form structure.
	 * For a read only form, we can only change the Text and Help Text.
	 * 
	 */
	private boolean readOnly = false;

	private String itextId;


	List<IFormElement> children;


	/** Constructs a form definition object. */
	public FormDef() {

	}

	/**
	 * Creates a new copy of the form from an existing one.
	 * 
	 * @param formDef the form to copy from.
	 */
	public FormDef(FormDef formDef) {
		this(formDef,true);
	}

	/**
	 * Creates a new copy of the form from an existing one, with a flag which
	 * tells whether we should copy the validation rules too.
	 * 
	 * @param formDef the form to copy from.
	 * @param copyValidationRules set to true if you also want to copy the validation rules, else false.
	 */
	public FormDef(FormDef formDef, boolean copyValidationRules) {
		setId(formDef.getId());
		setName(formDef.getName());
		setFormKey(formDef.getFormKey());

		//I just don't think we need this in addition to the id
		setVariableName(formDef.getVariableName());

		setDescriptionTemplate(formDef.getDescriptionTemplate());
		copyChildren(formDef.getChildren());
		copySkipRules(formDef.getSkipRules());
		copyCalculations(formDef.getCalculations());

		//This is a temporary fix for an infinite recursion that happens when validation
		//rule copy constructor tries to set a formdef using the FormDef copy constructor.
		if(copyValidationRules)
			copyValidationRules(formDef.getValidationRules());

		copyDynamicOptions(formDef.getDynamicOptions());
	}

	/**
	 * Constructs a form definition object from these parameters.
	 * 
	 * @param name - the numeric unique identifier of the form definition.
	 * @param name - the display name of the form.
	 * @param variableName - the string unique identifier of the form definition.
	 * @param pages - collection of page definitions.
	 * @param rules - collection of branching rules.
	 */
	public FormDef(int id, String name, String formKey, String variableName,List<IFormElement> children, Vector skipRules, Vector validationRules, HashMap<Integer,DynamicOptionDef> dynamicOptions, String descTemplate, Vector calculations) {
		setId(id);
		setName(name);
		setFormKey(formKey);

		//I just don't think we need this in addition to the id
		setVariableName(variableName);

		setChildren(children);
		setSkipRules(skipRules);
		setValidationRules(validationRules);
		setDynamicOptions(dynamicOptions);
		setDescriptionTemplate((descTemplate == null) ? ModelConstants.EMPTY_STRING : descTemplate);
		setCalculations(calculations);
	}

	public SkipRule getSkipRuleAt(int index) {
		if(skipRules == null)
			return null;
		return (SkipRule)skipRules.elementAt(index);
	}

	public ValidationRule getValidationRuleAt(int index) {
		if(validationRules == null)
			return null;
		return (ValidationRule)validationRules.elementAt(index);
	}

	public Calculation getCalculationAt(int index) {
		if(calculations == null)
			return null;
		return (Calculation)calculations.elementAt(index);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFormKey() {
		return formKey;
	}

	public void setFormKey(String formKey) {
		this.formKey = formKey;
	}

	//I just don't think we need this in addition to the id
	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getItextId() {
		return itextId;
	}

	public void setItextId(String itextId) {
		this.itextId = itextId;
	}

	public Vector getSkipRules() {
		return skipRules;
	}

	public void setSkipRules(Vector skipRules) {
		this.skipRules = skipRules;
	}

	public Vector getValidationRules() {
		return validationRules;
	}

	public void setValidationRules(Vector validationRules) {
		this.validationRules = validationRules;
	}

	public Vector getCalculations() {
		return calculations;
	}

	public void setCalculations(Vector calculations) {
		this.calculations = calculations;
	}

	public HashMap<Integer,DynamicOptionDef> getDynamicOptions() {
		return dynamicOptions;
	}

	public void setDynamicOptions(HashMap<Integer,DynamicOptionDef> dynamicOptions) {
		this.dynamicOptions = dynamicOptions;
	}

	public String getDescriptionTemplate() {
		return descriptionTemplate;
	}

	public void setDescriptionTemplate(String descriptionTemplate) {
		this.descriptionTemplate = descriptionTemplate;
	}

	public String getJavaScriptSource() {
		return javaScriptSource;
	}

	public void setJavaScriptSource(String javaScriptSource) {
		this.javaScriptSource = javaScriptSource;
	}

	public String getLayoutXml() {
		return layoutXml;
	}

	public void setLayoutXml(String layout) {
		this.layoutXml = layout;
	}

	public String getLanguageXml() {
		return languageXml;
	}

	public void setLanguageXml(String languageXml) {
		this.languageXml = languageXml;
	}

	public String getXformXml() {
		return xformXml;
	}

	public void setXformXml(String xform) {
		this.xformXml = xform;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/**
	 * Gets the first skip rule which has a given question as one of its targets.
	 * 
	 * @param questionDef the question.
	 * @return the skip rule.
	 */
	public SkipRule getSkipRule(IFormElement questionDef){
		if(skipRules == null)
			return null;

		for(int i=0; i<skipRules.size(); i++){
			SkipRule rule = (SkipRule)skipRules.elementAt(i);
			Vector targets = rule.getActionTargets();
			for(int j=0; j<targets.size(); j++){
				if(((Integer)targets.elementAt(j)).intValue() == questionDef.getId())
					return rule;
			}
		}

		return null;
	}

	public Calculation getCalculation(QuestionDef questionDef){
		if(calculations == null)
			return null;

		for(int i=0; i<calculations.size(); i++){
			Calculation calculation = (Calculation)calculations.elementAt(i);
			if(calculation.getQuestionId() == questionDef.getId())
				return calculation;
		}

		return null;
	}

	/**
	 * Gets the validation rule for a given question.
	 * 
	 * @param questionDef the question.
	 * @return the validation rule.
	 */
	public ValidationRule getValidationRule(QuestionDef questionDef){
		if(validationRules == null)
			return null;

		for(int i=0; i<validationRules.size(); i++){
			ValidationRule rule = (ValidationRule)validationRules.elementAt(i);
			if(questionDef.getId() == rule.getQuestionId())
				return rule;
		}

		return null;
	}

	/**
	 * Updates the xforms document with the current changes in the form.
	 * 
	 * @param withData set to true if you want question answers to also be saved as part of the xform.
	 */
	public void updateDoc(boolean withData){
		dataNode.setAttribute(XformConstants.ATTRIBUTE_NAME_NAME, name);
		dataNode.setAttribute(XformConstants.ATTRIBUTE_NAME_FORM_KEY, formKey);

		//TODO Check that this comment out does not introduce bugs
		//We do not want a refreshed xform to overwrite existing formDef id
		//If ones want to change the id, he should load the xform as a new form with that id
		/*String val = dataNode.getAttribute(XformConstants.ATTRIBUTE_NAME_ID);
		if(val == null || val.trim().length() == 0)
			dataNode.setAttribute(XformConstants.ATTRIBUTE_NAME_ID, String.valueOf(id));
		else
			setId(Integer.parseInt(val));*/

		//TODO Check this with the above
		//Some use non numeric ids like in ODK. And so in such cases, we do not want to overwrite
		//the existing ids.
		String sid = dataNode.getAttribute(XformConstants.ATTRIBUTE_NAME_ID);
		if(sid == null || sid.trim().length() == 0 || FormUtil.isNumeric(sid))
			dataNode.setAttribute(XformConstants.ATTRIBUTE_NAME_ID,String.valueOf(id));

		String orgVarName = dataNode.getNodeName();
		if(!orgVarName.equalsIgnoreCase(variableName)){
			dataNode = XformUtil.renameNode(dataNode,variableName);
			updateDataNodes();
			((Element)dataNode.getParentNode()).setAttribute(XformConstants.ATTRIBUTE_NAME_ID, variableName);
		}

		if(dataNode != null){
			if(descriptionTemplate == null || descriptionTemplate.trim().length() == 0)
				dataNode.removeAttribute(XformConstants.ATTRIBUTE_NAME_DESCRIPTION_TEMPLATE);
			else
				dataNode.setAttribute(XformConstants.ATTRIBUTE_NAME_DESCRIPTION_TEMPLATE, descriptionTemplate);
		}
		
		if(children != null){
			if(bodyNode == null && children.size() > 0)
				bodyNode = (Element)children.get(0).getControlNode().getParentNode();
			
			for(int i=0; i<children.size(); i++){
				IFormElement element = children.get(i);
				if(element instanceof GroupDef)
					((GroupDef)element).updateDoc(doc,bodyNode,this,dataNode,modelNode,withData,orgVarName);
				else
					((QuestionDef)element).updateDoc(doc,xformsNode,this,dataNode,modelNode,bodyNode,true,withData, orgVarName);;
			}
		}

		if(skipRules != null){
			for(int i=0; i<skipRules.size(); i++){
				SkipRule skipRule = (SkipRule)skipRules.elementAt(i);
				skipRule.updateDoc(this);
			}
		}

		if(validationRules != null){
			for(int i=0; i<validationRules.size(); i++){
				ValidationRule validationRule = (ValidationRule)validationRules.elementAt(i);
				validationRule.updateDoc(this);
			}
		}

		if(dynamicOptions != null){
			Iterator<Entry<Integer,DynamicOptionDef>> iterator = dynamicOptions.entrySet().iterator();
			while(iterator.hasNext()){
				Entry<Integer,DynamicOptionDef> entry = iterator.next();
				DynamicOptionDef dynamicOptionDef = entry.getValue();
				QuestionDef questionDef = (QuestionDef)getElement(entry.getKey());
				if(questionDef == null)
					continue;

				dynamicOptionDef.updateDoc(this,questionDef);
			}
		}

		if(calculations != null){
			for(int i=0; i<calculations.size(); i++){
				Calculation calculation = (Calculation)calculations.elementAt(i);
				if(getElement(calculation.getQuestionId()) == null) //possibly question deleted
					calculations.remove(i);
				else
					calculation.updateDoc(this);
			}
		}
	}

	private void updateDataNodes(){
		if(children == null)
			return;

		for(int i=0; i<children.size(); i++)
			((IFormElement)children.get(i)).updateDataNodes(dataNode);
	}

	public String toString() {
		return getName();
	}

	/**
	 * Gets a question identified by a variable name.
	 * 
	 * @param varName - the string identifier of the question. 
	 * @return the question reference.
	 */
	public IFormElement getElement(String varName){
		if(varName == null || children == null)
			return null;

		for(int i=0; i<children.size(); i++){
			IFormElement def = children.get(i);
			if(varName.equals(def.getBinding()))
				return def;
			
			if(def instanceof GroupDef){
				def = ((GroupDef)def).getElement(varName);
				if(def != null)
					return def;
			}
		}

		return null;
	}
	
	public QuestionDef getQuestion(String varName){
		return (QuestionDef)getElement(varName);
	}
	
	public QuestionDef getQuestion(int id){
		return (QuestionDef)getElement(id);
	}

	/**
	 * Gets a question identified by an id
	 * 
	 * @param id - the numeric identifier of the question. 
	 * @return the question reference.
	 */
	public IFormElement getElement(int id){
		if(children == null)
			return null;

		for(int i=0; i<children.size(); i++){
			IFormElement def = children.get(i);
			if(id == def.getId())
				return def;
			
			if(def instanceof GroupDef){
				def = ((GroupDef)def).getElement(id);
				if(def != null)
					return def;
			}
		}

		return null;
	}

	/**
	 * Gets a numeric question identifier for a given question variable name.
	 * 
	 * @param varName - the string identifier of the question. 
	 * @return the numeric question identifier.
	 */
	public int getQuestionId(String varName){
		IFormElement qtn = getElement(varName);
		if(qtn != null)
			return qtn.getId();

		return ModelConstants.NULL_ID;
	}

	/**
	 * Adds a new question to the form.
	 * 
	 * @param qtn the new question to add.
	 */
	public void addElement(IFormElement qtn){
		if(children == null)
			children = new ArrayList<IFormElement>();

		children.add(qtn);
		qtn.setParent(this);
	}

	/**
	 * Copies a given list of pages into this form.
	 * 
	 * @param pages the pages to copy.
	 */
	private void copyChildren(List<IFormElement> children){
		if(children != null){
			this.children =  new ArrayList<IFormElement>();
			for(int i=0; i<children.size(); i++) //Should have atleast one page is why we are not checking for nulls.
				this.children.add(children.get(i).copy(this));
		}
	}

	/**
	 * Copies a given list of skip rules into this form.
	 * 
	 * @param rules the skip rules.
	 */
	private void copySkipRules(Vector rules){
		if(rules != null)
		{
			this.skipRules =  new Vector();
			for(int i=0; i<rules.size(); i++)
				this.skipRules.addElement(new SkipRule((SkipRule)rules.elementAt(i)));
		}
	}

	/**
	 * Copies a given list of validation rules into this form.
	 * 
	 * @param rules the validation rules.
	 */
	private void copyValidationRules(Vector rules){
		if(rules != null)
		{
			this.validationRules =  new Vector();
			for(int i=0; i<rules.size(); i++)
				this.validationRules.addElement(new ValidationRule((ValidationRule)rules.elementAt(i)));
		}
	}

	private void copyDynamicOptions(HashMap<Integer,DynamicOptionDef> options){
		if(options != null)
		{
			dynamicOptions =  new HashMap<Integer,DynamicOptionDef>();

			Iterator<Entry<Integer,DynamicOptionDef>> iterator = options.entrySet().iterator();
			while(iterator.hasNext()){
				Entry<Integer,DynamicOptionDef> entry = iterator.next();
				DynamicOptionDef dynamicOptionDef = entry.getValue();
				QuestionDef questionDef = (QuestionDef)getElement(dynamicOptionDef.getQuestionId());
				if(questionDef == null)
					return;
				dynamicOptions.put(new Integer(entry.getKey()), new DynamicOptionDef(dynamicOptionDef,questionDef));
			}
		}
	}

	private void copyCalculations(Vector calculations){
		if(calculations != null)
		{
			this.calculations =  new Vector();
			for(int i=0; i<calculations.size(); i++)
				this.calculations.addElement(new Calculation((Calculation)calculations.elementAt(i)));
		}
	}

	/*private void copyDynamicOptions(HashMap<Integer,DynamicOptionDef>){

	}*/

	/**
	 * Removes a page from the form.
	 * 
	 * @param pageFef the page to remove.
	 */
	public boolean removeChild(IFormElement element){
		if(element instanceof GroupDef){
			((GroupDef)element).removeAllElements(this);

			if(((GroupDef)element).getGroupNode() != null)
				((GroupDef)element).getGroupNode().getParentNode().removeChild(((GroupDef)element).getGroupNode());
		}
		else
			GroupDef.removeElement2((QuestionDef)element, this, true);

		return children.remove(element);
	}

	/**
	 * Sets the xforms document represented by this form.
	 * @param doc
	 */
	public void setDoc(Document doc){
		this.doc = doc;
	}

	/**
	 * Gets the xforms document represented by this form.
	 * @return
	 */
	public Document getDoc(){
		return doc;
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
	 * @return the xformsNode
	 */
	public Element getXformsNode() {
		return xformsNode;
	}

	/**
	 * @param xformsNode the xformsNode to set
	 */
	public void setXformsNode(Element xformsNode) {
		this.xformsNode = xformsNode;
	}

	/**
	 * @return the modelNode
	 */
	public Element getModelNode() {
		return modelNode;
	}

	/**
	 * @param modelNode the modelNode to set
	 */
	public void setModelNode(Element modelNode) {
		this.modelNode = modelNode;
		
		if(modelNode != null){
			String prefix = modelNode.getPrefix();
			if(prefix != null && prefix.trim().length() > 0)
				XformConstants.updatePrefixConstants(prefix);
		}
	}

	public Element getBodyNode() {
		return bodyNode;
	}

	public void setBodyNode(Element bodyNode) {
		this.bodyNode = bodyNode;
	}

	/**
	 * Moves a page one position up in the form.
	 * 
	 * @param pageDef the page to move.
	 */
	/*public void movePageUp(IFormElement element){
		int index = children.indexOf(element);

		children.remove(element);

		Node parentNode = element.getControlNode().getParentNode();
		if(element.getControlNode() != null)
			parentNode.removeChild(element.getControlNode()); //xformsNode.removeChild(element.getControlNode());

		IFormElement currentElement;
		List<IFormElement> list = new ArrayList<IFormElement>();

		while(children.size() >= index){
			currentElement = children.get(index-1);
			list.add(currentElement);
			children.remove(currentElement);
		}

		children.add(element);
		for(int i=0; i<list.size(); i++){
			if(i == 0){
				IFormElement elem = list.get(i);
				if(elem.getControlNode() != null)
					parentNode.insertBefore(element.getControlNode(), elem.getControlNode()); //xformsNode.insertBefore(element.getControlNode(), elem.getControlNode());
			}
			children.add(list.get(i));
		}
	}*/

	/**
	 * Moves a page one position down in the form.
	 * 
	 * @param pageDef the page to move.
	 */
	/*public void movePageDown(IFormElement element){
		int index = children.indexOf(element);	

		children.remove(element);

		Node parentNode = element.getControlNode().getParentNode();
		if(element.getControlNode() != null)
			parentNode.removeChild(element.getControlNode()); //xformsNode.removeChild(element.getControlNode());

		IFormElement currentItem; // = parent.getChild(index - 1);
		List<IFormElement> list = new ArrayList<IFormElement>();

		while(children.size() > 0 && children.size() > index){
			currentItem = children.get(index);
			list.add(currentItem);
			children.remove(currentItem);
		}

		for(int i=0; i<list.size(); i++){
			if(i == 1){
				children.add(element); //Add after the first item.

				IFormElement pgDef = list.get(i);
				if(pgDef.getControlNode() != null)
					parentNode.insertBefore(element.getControlNode(), pgDef.getControlNode()); //xformsNode.insertBefore(element.getControlNode(), pgDef.getGroupNode());
			}
			children.add(list.get(i));
		}

		if(list.size() == 1){
			children.add(element);

			if(element.getControlNode() != null)
				parentNode.appendChild(element.getControlNode()); //xformsNode.appendChild(element.getControlNode());
		}
	}*/

	/**
	 * Removes a question from the form.
	 * 
	 * @param qtnDef the question to remove.
	 * @return true if the question has been found and removed, else false.
	 */
	public boolean removeQuestion(IFormElement qtnDef, boolean delete){
		for(int i=0; i<children.size(); i++){
			IFormElement element = children.get(i);
			if(element == qtnDef){
				children.remove(qtnDef);
				return true;
			}
			else if(element instanceof GroupDef){
				if(((GroupDef)element).removeElement(element, this, delete))
					return true;
			}
		}
		return false;
	}

	/**
	 * Removes a question for the validation rules list.
	 * 
	 * @param questionDef the question to remove.
	 */
	private void removeQtnFromValidationRules(IFormElement questionDef){
		for(int index = 0; index < this.getValidationRuleCount(); index++){
			ValidationRule validationRule = getValidationRuleAt(index);
			validationRule.removeQuestion(questionDef);
			if(validationRule.getConditionCount() == 0){
				removeValidationRule(validationRule);
				index++;
			}
		}
	}

	/**
	 * Removes a question from skip rules list.
	 * 
	 * @param questionDef the question to remove.
	 */
	private void removeQtnFromSkipRules(IFormElement questionDef){
		for(int index = 0; index < getSkipRuleCount(); index++){
			SkipRule skipRule = getSkipRuleAt(index);
			skipRule.removeQuestion(questionDef);
			if(skipRule.getActionTargetCount() == 0 || skipRule.getConditionCount() == 0){
				removeSkipRule(skipRule);
				index++;
			}
		}
	}

	/**
	 * Removes a question from the validation rules which are referencing it.
	 * 
	 * @param qtnDef the question to remove.
	 */
	public void removeQtnFromRules(IFormElement qtnDef){
		removeQtnFromValidationRules(qtnDef);
		removeQtnFromSkipRules(qtnDef);
	}

	/**
	 * Check if a question is referenced by any dynamic selection list relationship
	 * and if so, removes the relationship.
	 * 
	 * @param questionDef the question to check.
	 */
	public void removeQtnFromDynamicLists(IFormElement questionDef){
		if(!(questionDef instanceof QuestionDef))
			return; //only QuestionDefs can be referenced by DynamicOptionDef s
		
		if(dynamicOptions != null){

			Object[] keys = dynamicOptions.keySet().toArray();
			for(int index = 0; index < keys.length; index++){
				Integer questionId = (Integer)keys[index];
				DynamicOptionDef dynamicOptionDef = dynamicOptions.get(questionId);

				//Check if the deleted question is the parent of a dynamic selection
				//list relationship. And if so, delete the relationship.
				if(questionId.intValue() == questionDef.getId()){
					dynamicOptions.remove(questionId);
					removeDynamicInstanceNode(dynamicOptionDef);
					continue;
				}

				//Check if the deleted question is the child of a dynamic selection
				//list relationship. And if so, delete the relationship.
				if(dynamicOptionDef.getQuestionId() == questionDef.getId()){
					dynamicOptions.remove(questionId);
					removeDynamicInstanceNode(dynamicOptionDef);
					continue;
				}

				dynamicOptionDef.updateDoc(this,(QuestionDef)questionDef);
			}
		}
	}


	public void removeQtnFromCalculations(QuestionDef questionDef){
		for(int index = 0; index < getCalculationCount(); index++){
			Calculation calculation = getCalculationAt(index);
			if(calculation.getQuestionId() == questionDef.getId()){
				calculations.remove(index);

				Element node = questionDef.getBindNode() != null ? questionDef.getBindNode() : questionDef.getControlNode();
				if(questionDef.getBindNode() != null)
					node.removeAttribute(XformConstants.ATTRIBUTE_NAME_CALCULATE);

				return;
			}
		}
	}


	public void updateCalculation(QuestionDef questionDef, String calculateExpression){
		if(calculateExpression == null || calculateExpression.trim().length() == 0)
			removeQtnFromCalculations(questionDef);
		else{
			Calculation calculation = getCalculation(questionDef);
			if(calculation == null)
				addCalculation(new Calculation(questionDef.getId(),calculateExpression));
			else
				calculation.setCalculateExpression(calculateExpression);
		}
	}


	/**
	 * Removes the instance node referenced by a dynamic selection list object.
	 * 
	 * @param dynamicOptionDef the dynamic selection list object.
	 */
	private static void removeDynamicInstanceNode(DynamicOptionDef dynamicOptionDef){
		//dataNode points to <dynamiclist>
		//dataNode.getParentNode() points to <xf:instance id="theid">
		//dataNode.getParentNode().getParentNode() points to <xf:model>

		Element dataNode = dynamicOptionDef.getDataNode();
		if(dataNode != null && dataNode.getParentNode() != null
				&& dataNode.getParentNode().getParentNode() != null){

			dataNode.getParentNode().getParentNode().removeChild(dataNode.getParentNode());	
		}
	}

	/**
	 * Gets the number of skip rules in the form.
	 * 
	 * @return the number of skip rules.
	 */
	public int getSkipRuleCount(){
		if(skipRules == null)
			return 0;
		return skipRules.size();
	}

	public int getCalculationCount(){
		if(calculations == null)
			return 0;
		return calculations.size();
	}

	/**
	 * Gets the number of validation rules in the form.
	 * 
	 * @return the number of validation rules.
	 */
	public int getValidationRuleCount(){
		if(validationRules == null)
			return 0;
		return validationRules.size();
	}

	/**
	 * Gets questions with given display text.
	 * 
	 * @param text the display text to look for.
	 * @return the question of found, else null.
	 */
	public IFormElement getQuestionWithText(String text){
		for(int i=0; i<children.size(); i++){
			IFormElement element = children.get(i);
			if(text.equals(element.getText()))
				return element;
			
			if(element instanceof GroupDef){
				element = ((GroupDef)element).getQuestionWithText(text);
				if(element != null)
					return element;
			}
		}
		return null;
	}


	/**
	 * Checks if the form has a particular skip rule.
	 * 
	 * @param skipRule the skip rule to check.
	 * @return true if the skip rule has been found, else false.
	 */
	public boolean containsSkipRule(SkipRule skipRule){
		if(skipRules == null)
			return false;
		return skipRules.contains(skipRule);
	}


	/**
	 * Checks if a form has a particular validation rule.
	 * 
	 * @param validationRule the validation rule to check.
	 * @return true if the validation rule has been found, else false.
	 */
	public boolean containsValidationRule(ValidationRule validationRule){
		if(validationRules == null)
			return false;
		return validationRules.contains(validationRule);
	}

	/**
	 * Adds a new skip rule to the form.
	 * 
	 * @param skipRule the new skip rule to add.
	 */
	public void addSkipRule(SkipRule skipRule){
		if(skipRules == null)
			skipRules = new Vector();
		skipRules.addElement(skipRule);
	}

	/**
	 * Adds a new validation rule to the form.
	 * 
	 * @param validationRule the new validation rule to add.
	 */
	public void addValidationRule(ValidationRule validationRule){
		if(validationRules == null)
			validationRules = new Vector();
		validationRules.addElement(validationRule);
	}

	public void addCalculation(Calculation calculation){
		if(calculations == null)
			calculations = new Vector();
		calculations.addElement(calculation);
	}


	/**
	 * Removes a skip rule from the form.
	 * 
	 * @param skipRule the skip rule to remove.
	 * @return true if the skip rule has been found and removed, else false.
	 */
	public boolean removeSkipRule(SkipRule skipRule){
		if(skipRules == null)
			return false;

		boolean ret = skipRules.remove(skipRule);
		if(dataNode != null){
			for(int index = 0; index < skipRule.getActionTargetCount(); index++){
				QuestionDef questionDef = (QuestionDef)getElement(skipRule.getActionTargetAt(index));
				if(questionDef != null){
					Element node = questionDef.getBindNode() != null ? questionDef.getBindNode() : questionDef.getControlNode();
					if(node != null){
						node.removeAttribute(XformConstants.ATTRIBUTE_NAME_RELEVANT);
						node.removeAttribute(XformConstants.ATTRIBUTE_NAME_ACTION);
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Removes a validation rule from the form.
	 * 
	 * @param validationRule the validation rule to remove.
	 * @return true if the validation rule has been found and removed.
	 */
	public boolean removeValidationRule(ValidationRule validationRule){
		if(validationRules == null)
			return false;

		boolean ret = validationRules.remove(validationRule);
		if(dataNode != null){
			IFormElement questionDef = getElement(validationRule.getQuestionId());
			if(questionDef != null){
				Element node = questionDef.getBindNode() != null ? questionDef.getBindNode() : questionDef.getControlNode();
				if(node != null){
					node.removeAttribute(XformConstants.ATTRIBUTE_NAME_CONSTRAINT);
					node.removeAttribute(XformConstants.ATTRIBUTE_NAME_CONSTRAINT_MESSAGE);
				}
			}
		}
		return ret;
	}

	public void setDynamicOptionDef(Integer questionId, DynamicOptionDef dynamicOptionDef){

		//The parent or child question may have been deleted.
		if(getElement(questionId) == null || getElement(dynamicOptionDef.getQuestionId()) == null)
			return;

		if(dynamicOptions == null)
			dynamicOptions = new HashMap<Integer,DynamicOptionDef>();

		dynamicOptions.put(questionId, dynamicOptionDef);
	}

	public DynamicOptionDef getDynamicOptions(Integer questionId){
		if(dynamicOptions == null)
			return null;
		return dynamicOptions.get(questionId);
	}

	public DynamicOptionDef getChildDynamicOptions(Integer questionId){
		if(dynamicOptions == null)
			return null;

		Iterator<Entry<Integer,DynamicOptionDef>> iterator = dynamicOptions.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<Integer,DynamicOptionDef> entry = iterator.next();
			DynamicOptionDef dynamicOptionDef = entry.getValue();
			if(dynamicOptionDef.getQuestionId() == questionId)
				return dynamicOptionDef;
		}
		return null;
	}

	public QuestionDef getDynamicOptionsParent(Integer questionId){
		if(dynamicOptions == null)
			return null;

		Iterator<Entry<Integer,DynamicOptionDef>> iterator = dynamicOptions.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<Integer,DynamicOptionDef> entry = iterator.next();
			DynamicOptionDef dynamicOptionDef = entry.getValue();
			if(dynamicOptionDef.getQuestionId() == questionId)
				return (QuestionDef)getElement(entry.getKey());
		}
		return null;
	}

	public OptionDef getDynamicOptionDef(Integer questionId, int id){
		if(dynamicOptions == null)
			return null;

		Iterator<Entry<Integer,DynamicOptionDef>> iterator = dynamicOptions.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<Integer,DynamicOptionDef> entry = iterator.next();
			DynamicOptionDef dynamicOptionDef = entry.getValue();
			if(dynamicOptionDef.getQuestionId() == questionId)
				return dynamicOptionDef.getOptionWithId(id);
		}
		return null;
	}

	/**
	 * Removes a dynamic selection list relationship referenced by a given question.
	 * 
	 * @param questionId the question to check from dynamic selection lists.
	 */
	public void removeDynamicOptions(Integer questionId){
		if(dynamicOptions != null){
			DynamicOptionDef dynamciOptionDef = dynamicOptions.get(questionId);
			if(dynamciOptionDef == null)
				return;

			removeDynamicInstanceNode(dynamciOptionDef);

			dynamicOptions.remove(questionId);
		}
	}

	/**
	 * Updates this formDef (as the main from the refresh source) with the parameter one
	 * 
	 * @param formDef the old formDef to copy from.
	 */
	public void refresh(FormDef formDef){
		this.id = formDef.getId();

		if(variableName.equals(formDef.getVariableName()))
			name = formDef.getName();

		for(int index = 0; index < formDef.getChildren().size(); index++){
			IFormElement element = formDef.getChildren().get(index);
			if(element instanceof GroupDef)
				;//((GroupDef)element).refresh(groupDef);

			//refresh((PageDef)formDef.getPageAt(index));
		}

		//Clear existing skip rules if any. Already existing skip rules will always
		//overwrite those from the refresh source.
		skipRules = new Vector();
		for(int index = 0; index < formDef.getSkipRuleCount(); index++)
			formDef.getSkipRuleAt(index).refresh(this, formDef);

		//Clear existing validation rules if any. Already existing validation rules 
		//will always overwrite those from the refresh source.
		validationRules = new Vector();
		for(int index = 0; index < formDef.getValidationRuleCount(); index++)
			formDef.getValidationRuleAt(index).refresh(this, formDef);

		//If we already had dynamic options, they will always overwrite all 
		//from the refresh source.
		//TODO May need to do a smarter refresh by only overwriting those that have
		//come from the server and then leave the rest.
		if(formDef.getDynamicOptions() != null){
			dynamicOptions =  new HashMap<Integer,DynamicOptionDef>();

			Iterator<Entry<Integer,DynamicOptionDef>> iterator = formDef.getDynamicOptions().entrySet().iterator();
			while(iterator.hasNext()){
				Entry<Integer,DynamicOptionDef> entry = iterator.next();

				QuestionDef oldParentQtnDef = (QuestionDef)formDef.getElement(entry.getKey());
				if(oldParentQtnDef == null)
					continue; //How can this be missing in the original formdef???

				QuestionDef newParentQtnDef = (QuestionDef)getElement(oldParentQtnDef.getBinding());
				if(newParentQtnDef == null)
					continue; //My be deleted by refresh source.

				DynamicOptionDef oldDynOptionDef = entry.getValue();
				QuestionDef oldChildQtnDef = (QuestionDef)formDef.getElement(oldDynOptionDef.getQuestionId());
				if(oldChildQtnDef == null)
					return; //can this be lost in the old formdef????

				QuestionDef newChildQtnDef = (QuestionDef)getElement(oldChildQtnDef.getBinding());
				if(newChildQtnDef == null)
					continue; //possibly deleted by refresh sourced (eg server).

				DynamicOptionDef newDynOptionDef = new DynamicOptionDef();
				newDynOptionDef.setQuestionId(newChildQtnDef.getId());
				newDynOptionDef.refresh(this, formDef, newDynOptionDef, oldDynOptionDef,newParentQtnDef,oldParentQtnDef,newChildQtnDef,oldChildQtnDef);
				dynamicOptions.put(new Integer(newParentQtnDef.getId()),newDynOptionDef);
			}
		}

		//add calculations for questions that still exist.
		calculations = new Vector();
		for(int index = 0; index < formDef.getCalculationCount(); index++){
			Calculation calculation = formDef.getCalculationAt(index);
			QuestionDef questionDef = (QuestionDef)getElement(formDef.getElement(calculation.getQuestionId()).getBinding());
			if(questionDef != null)
				addCalculation(new Calculation(questionDef.getId(),calculation.getCalculateExpression()));
		}
	}

	public void refresh(IFormElement element){
		//for(int index = 0; index < children.size(); index++){
		//	((PageDef)children.get(index)).refresh(pageDef);
		//}
	}

	public int getChildCount(){
		if(children == null)
			return 0;

		return children.size();
	}

	public IFormElement getChildAt(int index){
		return children.get(index);
	}

	/**
	 * Gets the total number of questions contained in the form.
	 * 
	 * @return the number of questions.
	 */
	public int getQuestionCount(){
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
	 * Gets the element at a given position on the first level.
	 * 
	 * @param index the element position.
	 * @return the element object.
	 */
	public IFormElement getElementAt(int index){
		if(children == null)
			return null;

		return  children.get(index);
	}

	public void updateRuleConditionValue(String origValue, String newValue){
		for(int index = 0; index < getSkipRuleCount(); index++)
			getSkipRuleAt(index).updateConditionValue(origValue, newValue);

		for(int index = 0; index < getValidationRuleCount(); index++)
			getValidationRuleAt(index).updateConditionValue(origValue, newValue);
	}

	public Element getLanguageNode() {
		com.google.gwt.xml.client.Document doc = XMLParser.createDocument();
		doc.appendChild(doc.createProcessingInstruction("xml", "version=\"1.0\" encoding=\"UTF-8\""));
		Element rootNode = doc.createElement("xform");
		rootNode.setAttribute(XformConstants.ATTRIBUTE_NAME_ID, id+"");
		doc.appendChild(rootNode);

		if(dataNode != null){
			Element node = doc.createElement(XformConstants.NODE_NAME_TEXT);
			node.setAttribute(XformConstants.ATTRIBUTE_NAME_XPATH, FormUtil.getNodePath(dataNode)+"[@name]");
			node.setAttribute(XformConstants.ATTRIBUTE_NAME_VALUE, name);
			node.setAttribute(XformConstants.ATTRIBUTE_NAME_ID, itextId);
			node.setAttribute(OpenRosaConstants.ATTRIBUTE_NAME_UNIQUE_ID, "FormDef"+id);
			rootNode.appendChild(node);

			if(children != null){
				for(int index = 0; index < children.size(); index++){
					IFormElement element = children.get(index);
					if(element instanceof GroupDef)
						((GroupDef)element).buildLanguageNodes(null, doc, rootNode);
					else{
						assert(element instanceof QuestionDef);
						((QuestionDef)element).buildLanguageNodes(FormUtil.getNodePath(xformsNode), doc, xformsNode, rootNode);
					}
				}
			}

			if(validationRules != null){
				for(int index = 0; index < validationRules.size(); index++)
					((ValidationRule)validationRules.elementAt(index)).buildLanguageNodes(this, rootNode);
			}

			if(dynamicOptions != null){
				Iterator<Entry<Integer,DynamicOptionDef>> iterator = dynamicOptions.entrySet().iterator();
				while(iterator.hasNext())
					iterator.next().getValue().buildLanguageNodes(this, rootNode);
			}
		}

		return rootNode;
	}

	/**
	 * Gets the form to which a particular item (PageDef,QuestionDef,OptionDef) belongs.
	 * 
	 * @param formItem the item.
	 * @return the form.
	 */
	public static FormDef getFormDef(IFormElement formItem){
		if(formItem == null)
			return null;
		
		if(formItem instanceof FormDef)
			return (FormDef)formItem;
		else
			return getFormDef(formItem.getParent());
	}

	/**
	 * Removes all question change event listeners.
	 */
	public void clearChangeListeners(){
		if(children == null)
			return;

		for(int i=0; i<children.size(); i++)
			children.get(i).clearChangeListeners();
	}


	public String getText(){
		return name;
	}

	public void setText(String text){
		setName(text);
	}

	public String getBinding(){
		return variableName;
	}

	public void setBinding(String binding){
		setVariableName(binding);
	}

	public List<IFormElement> getChildren(){
		return children;
	}

	public void setChildren(List<IFormElement> children){
		this.children = children;
	}
	
	public void addChild(IFormElement element){
		if(children == null)
			children = new ArrayList<IFormElement>();
		children.add(element);
		element.setParent(this);
	}

	public IFormElement getParent(){
		return null;
	}

	public void setParent(IFormElement parent){

	}

	public Element getControlNode(){
		return null;
	}

	public void setControlNode(Element controlNode){

	}

	public Element getBindNode(){
		return null;
	}

	public void setBindNode(Element bindNode){

	}

	public int getDataType(){
		return QuestionDef.QTN_TYPE_NULL;
	}

	public void setDataType(int dataType){

	}

	public void updateDataNodes(Element parentDataNode){

	}

	public IFormElement copy(IFormElement parent){
		return null;
	}
	
	public String getDisplayText(){
		return name;
	}
	
	public String getHelpText(){
		return null;
	}
	
	public void setHelpText(String helpText){
		
	}
	
	public Element getLabelNode(){
		return null;
	}
	
	public void setLabelNode(Element labelNode){
		
	}
	
	public Element getHintNode(){
		return null;
	}
	
	public void setHintNode(Element hintNode){
		
	}
	
	public FormDef getFormDef(){
		return this;
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
