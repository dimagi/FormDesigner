package org.purc.purcforms.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;

import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.xforms.XformConverter;
import org.purc.purcforms.client.xpath.XPathExpression;

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
public class FormDef implements Serializable{

	/** A collection of page definitions (PageDef objects). */
	private Vector pages;

	//TODO May not need to serialize this property for smaller pay load. Then we may just rely on the id.
	//afterall it is not even guaranteed to be unique.
	/** The string unique identifier of the form definition. */
	private String variableName = ModelConstants.EMPTY_STRING;

	/** The display name of the form. */
	private String name = ModelConstants.EMPTY_STRING;

	/** The numeric unique identifier of the form definition. */
	private int id = ModelConstants.NULL_ID;

	/** The collection of rules (SkipRule objects) for this form. */
	private Vector skipRules;

	/** The collection of rules (ValidationRule objects) for this form. */
	private Vector validationRules;

	/** A string constistig for form fields that describe its data. eg description-template="${/data/question1}$ Market" */
	private String descriptionTemplate =  ModelConstants.EMPTY_STRING;

	/** A mapping of dynamic lists keyed by the id of the question whose values
	 *  determine possible values of another question as specified in the DynamicOptionDef object.
	 */
	private HashMap<Integer,DynamicOptionDef>  dynamicOptions;


	/** The xforms document.(for easy syncing between the object model and actual xforms document. */
	private Document doc;
	private Element dataNode;
	private Element xformsNode;
	private Element modelNode;

	private String layoutXml;
	private String xformXml;
	private String languageXml;
	
	private boolean readOnly = false;
	

	/** Constructs a form definition object. */
	public FormDef() {

	}

	public FormDef(FormDef formDef) {
		this(formDef,true);
	}
	
	public FormDef(FormDef formDef, boolean copyValidationRules) {
		setId(formDef.getId());
		setName(formDef.getName());

		//I just don't think we need this in addition to the id
		setVariableName(formDef.getVariableName());

		setDescriptionTemplate(formDef.getDescriptionTemplate());
		copyPages(formDef.getPages());
		copySkipRules(formDef.getSkipRules());
		
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
	public FormDef(int id, String name, String variableName,Vector pages, Vector skipRules, Vector validationRules, HashMap<Integer,DynamicOptionDef> dynamicOptions, String descTemplate) {
		setId(id);
		setName(name);

		//I just don't think we need this in addition to the id
		setVariableName(variableName);

		setPages(pages);
		setSkipRules(skipRules);
		setValidationRules(validationRules);
		setDynamicOptions(dynamicOptions);
		setDescriptionTemplate((descTemplate == null) ? ModelConstants.EMPTY_STRING : descTemplate);
	}

	public void addPage(PageDef pageDef){
		if(pages == null)
			pages = new Vector();

		pages.add(pageDef);
		pageDef.setParent(this);
	}

	public void addPage(){
		if(pages == null)
			pages = new Vector();

		pages.add(new PageDef("Page"+pages.size(),(int)pages.size(),this));
	}

	public PageDef setPageName(String name){
		PageDef pageDef = ((PageDef)pages.elementAt(pages.size()-1));
		pageDef.setName(name);
		return pageDef;
	}

	public void setPageLabelNode(Element labelNode){
		((PageDef)pages.elementAt(pages.size()-1)).setLabelNode(labelNode);
	}

	public void setPageGroupNode(Element groupNode){
		((PageDef)pages.elementAt(pages.size()-1)).setGroupNode(groupNode);
	}

	public Vector getPages() {
		return pages;
	}

	public PageDef getPageAt(int index) {
		if(pages == null)
			return null;
		return (PageDef)pages.elementAt(index);
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

	public void setPages(Vector pages) {
		this.pages = pages;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public SkipRule getSkipRule(QuestionDef questionDef){
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

	public void updateDoc(boolean withData){
		dataNode.setAttribute(XformConverter.ATTRIBUTE_NAME_NAME, name);

		//TODO Check that this comment out does not introduce bugs
		//We do not want a refreshed xform to overwrite existing formDef id
		//If ones want to change the id, he should load the xform as a new form with that id
		/*String val = dataNode.getAttribute(XformConverter.ATTRIBUTE_NAME_ID);
		if(val == null || val.trim().length() == 0)
			dataNode.setAttribute(XformConverter.ATTRIBUTE_NAME_ID, String.valueOf(id));
		else
			setId(Integer.parseInt(val));*/
		
		//TODO Check this with the above
		dataNode.setAttribute(XformConverter.ATTRIBUTE_NAME_ID,String.valueOf(id));
		
		String orgVarName = dataNode.getNodeName();
		if(!orgVarName.equalsIgnoreCase(variableName)){
			dataNode = XformConverter.renameNode(dataNode,variableName);
			updateDataNodes();
		}

		if(dataNode != null){
			if(descriptionTemplate == null || descriptionTemplate.trim().length() == 0)
				dataNode.removeAttribute(XformConverter.ATTRIBUTE_NAME_DESCRIPTION_TEMPLATE);
			else
				dataNode.setAttribute(XformConverter.ATTRIBUTE_NAME_DESCRIPTION_TEMPLATE, descriptionTemplate);
		}

		if(pages != null){
			for(int i=0; i<pages.size(); i++){
				PageDef pageDef = (PageDef)pages.elementAt(i);
				pageDef.updateDoc(doc,xformsNode,this,dataNode,modelNode,withData,orgVarName);
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
				QuestionDef questionDef = getQuestion(entry.getKey());
				if(questionDef == null)
					continue;

				dynamicOptionDef.updateDoc(this,questionDef);
			}
		}
	}

	private void updateDataNodes(){
		if(pages == null)
			return;

		for(int i=0; i<pages.size(); i++)
			((PageDef)pages.elementAt(i)).updateDataNodes(dataNode);
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
	public QuestionDef getQuestion(String varName){
		if(varName == null || pages == null)
			return null;

		for(int i=0; i<getPages().size(); i++){
			QuestionDef def = ((PageDef)getPages().elementAt(i)).getQuestion(varName);
			if(def != null)
				return def;
		}

		return null;
	}

	/**
	 * Gets a question identified by an id
	 * 
	 * @param id - the numeric identifier of the question. 
	 * @return the question reference.
	 */
	public QuestionDef getQuestion(int id){		
		if(pages == null)
			return null;

		for(int i=0; i<getPages().size(); i++){
			QuestionDef def = ((PageDef)getPages().elementAt(i)).getQuestion(id);
			if(def != null)
				return def;
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
		QuestionDef qtn = getQuestion(varName);
		if(qtn != null)
			return qtn.getId();

		return ModelConstants.NULL_ID;
	}

	public void addQuestion(QuestionDef qtn){
		if(pages == null){
			pages = new Vector();
			PageDef page = new PageDef(/*this.getVariableName()*/"Page1",Integer.parseInt("1"),null,this);
			pages.addElement(page);
		}

		((PageDef)pages.elementAt(pages.size()-1)).addQuestion(qtn);

		qtn.setParent(pages.elementAt(pages.size()-1));
	}

	private void copyPages(Vector pages){
		this.pages =  new Vector();
		for(int i=0; i<pages.size(); i++) //Should have atleast one page is why we are not checking for nulls.
			this.pages.addElement(new PageDef((PageDef)pages.elementAt(i),this));
	}

	private void copySkipRules(Vector rules){
		if(rules != null)
		{
			this.skipRules =  new Vector();
			for(int i=0; i<rules.size(); i++)
				this.skipRules.addElement(new SkipRule((SkipRule)rules.elementAt(i)));
		}
	}

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
				QuestionDef questionDef = getQuestion(dynamicOptionDef.getQuestionId());
				if(questionDef == null)
					return;
				dynamicOptions.put(new Integer(entry.getKey()), new DynamicOptionDef(dynamicOptionDef,questionDef));
			}
		}
	}

	/*private void copyDynamicOptions(HashMap<Integer,DynamicOptionDef>){

	}*/

	public void removePage(PageDef pageDef){
		/*for(int i=0; i<pages.size(); i++){
			((PageDef)pages.elementAt(i)).removeAllQuestions();
		}*/

		pageDef.removeAllQuestions(this);

		if(pageDef.getGroupNode() != null)
			pageDef.getGroupNode().getParentNode().removeChild(pageDef.getGroupNode());

		pages.removeElement(pageDef);
	}

	public void setDoc(Document doc){
		this.doc = doc;
	}

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
	}

	public void movePageUp(PageDef pageDef){
		int index = pages.indexOf(pageDef);

		pages.remove(pageDef);

		if(pageDef.getGroupNode() != null)
			xformsNode.removeChild(pageDef.getGroupNode());

		PageDef currentPageDef;
		List list = new ArrayList();

		while(pages.size() >= index){
			currentPageDef = (PageDef)pages.elementAt(index-1);
			list.add(currentPageDef);
			pages.remove(currentPageDef);
		}

		pages.add(pageDef);
		for(int i=0; i<list.size(); i++){
			if(i == 0){
				PageDef pgDef = (PageDef)list.get(i);
				if(pgDef.getGroupNode() != null)
					xformsNode.insertBefore(pageDef.getGroupNode(), pgDef.getGroupNode());
			}
			pages.add(list.get(i));
		}
	}

	public void movePageDown(PageDef pageDef){
		int index = pages.indexOf(pageDef);	

		pages.remove(pageDef);

		if(pageDef.getGroupNode() != null)
			xformsNode.removeChild(pageDef.getGroupNode());

		PageDef currentItem; // = parent.getChild(index - 1);
		List list = new ArrayList();

		while(pages.size() > 0 && pages.size() > index){
			currentItem = (PageDef)pages.elementAt(index);
			list.add(currentItem);
			pages.remove(currentItem);
		}

		for(int i=0; i<list.size(); i++){
			if(i == 1){
				pages.add(pageDef); //Add after the first item.

				PageDef pgDef = (PageDef)list.get(i);
				if(pgDef.getGroupNode() != null)
					xformsNode.insertBefore(pageDef.getGroupNode(), pgDef.getGroupNode());
			}
			pages.add(list.get(i));
		}

		if(list.size() == 1){
			pages.add(pageDef);

			if(pageDef.getGroupNode() != null)
				xformsNode.appendChild(pageDef.getGroupNode());
		}
	}

	public boolean removeQuestion(QuestionDef qtnDef){
		for(int i=0; i<pages.size(); i++){
			if(((PageDef)pages.elementAt(i)).removeQuestion(qtnDef,this))
				return true;
		}
		return false;
	}

	private void removeQtnFromValidationRules(QuestionDef questionDef){
		for(int index = 0; index < this.getValidationRuleCount(); index++){
			ValidationRule validationRule = getValidationRuleAt(index);
			validationRule.removeQuestion(questionDef);
			if(validationRule.getConditionCount() == 0){
				removeValidationRule(validationRule);
				index++;
			}
		}
	}

	private void removeQtnFromSkipRules(QuestionDef questionDef){
		for(int index = 0; index < getSkipRuleCount(); index++){
			SkipRule skipRule = getSkipRuleAt(index);
			skipRule.removeQuestion(questionDef);
			if(skipRule.getActionTargetCount() == 0 || skipRule.getConditionCount() == 0){
				removeSkipRule(skipRule);
				index++;
			}
		}
	}

	public void removeQtnFromRules(QuestionDef qtnDef){
		removeQtnFromValidationRules(qtnDef);
		removeQtnFromSkipRules(qtnDef);
	}

	public int getPageCount(){
		if(pages == null)
			return 0;
		return pages.size();
	}

	public int getSkipRuleCount(){
		if(skipRules == null)
			return 0;
		return skipRules.size();
	}

	public int getValidationRuleCount(){
		if(validationRules == null)
			return 0;
		return validationRules.size();
	}

	public void moveQuestion2Page(QuestionDef qtn, int pageNo){
		for(int i=0; i<pages.size(); i++){
			PageDef page = (PageDef)pages.elementAt(i);
			if(page.contains(qtn)){
				if(i == pageNo-1)
					return;
				page.removeQuestionEx(qtn);
				((PageDef)pages.elementAt(pageNo-1)).addQuestion(qtn);
				return;
			}
		}
	}

	public QuestionDef getQuestionWithText(String text){
		for(int i=0; i<pages.size(); i++){
			QuestionDef questionDef = ((PageDef)pages.elementAt(i)).getQuestionWithText(text);
			if(questionDef != null)
				return questionDef;
		}
		return null;
	}

	public String getQuestionBinding(QuestionDef questionDef){
		String binding = questionDef.getVariableName();
		if(!binding.startsWith("/"+ variableName+"/")){
			//if(!binding.contains("/"+ formDef.getVariableName()+"/"))
			if(binding.startsWith(variableName+"/"))
				binding = "/"+ binding;
			else
				binding = "/"+ variableName+"/" + binding;
			/*else{
					variableName = "/" + variableName; //correct user binding syntax error
					binding = variableName;
				}*/
		}
		return binding;
	}

	public boolean containsSkipRule(SkipRule skipRule){
		if(skipRules == null)
			return false;
		return skipRules.contains(skipRule);
	}

	public boolean containsValidationRule(ValidationRule validationRule){
		if(validationRules == null)
			return false;
		return validationRules.contains(validationRule);
	}

	public void addSkipRule(SkipRule skipRule){
		if(skipRules == null)
			skipRules = new Vector();
		skipRules.addElement(skipRule);
	}

	public void addValidationRule(ValidationRule validationRule){
		if(validationRules == null)
			validationRules = new Vector();
		validationRules.addElement(validationRule);
	}

	public boolean removeSkipRule(SkipRule skipRule){
		if(skipRules == null)
			return false;

		boolean ret = skipRules.remove(skipRule);
		if(dataNode != null){
			for(int index = 0; index < skipRule.getActionTargetCount(); index++){
				QuestionDef questionDef = getQuestion(skipRule.getActionTargetAt(index));
				if(questionDef != null && questionDef.getDataNode() != null){
					questionDef.getDataNode().removeAttribute(XformConverter.ATTRIBUTE_NAME_RELEVANT);
					questionDef.getDataNode().removeAttribute(XformConverter.ATTRIBUTE_NAME_ACTION);
				}
			}
		}
		return ret;
	}

	public boolean removeValidationRule(ValidationRule validationRule){
		if(validationRules == null)
			return false;

		boolean ret = validationRules.remove(validationRule);
		if(dataNode != null){
			QuestionDef questionDef = getQuestion(validationRule.getQuestionId());
			if(questionDef != null && questionDef.getBindNode() != null){
				questionDef.getBindNode().removeAttribute(XformConverter.ATTRIBUTE_NAME_CONSTRAINT);
				questionDef.getBindNode().removeAttribute(XformConverter.ATTRIBUTE_NAME_CONSTRAINT_MESSAGE);
			}
		}
		return ret;
	}

	public void setDynamicOptionDef(Integer questionId, DynamicOptionDef dynamicOptionDef){
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
				return getQuestion(entry.getKey());
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

	public void removeDynamicOptions(Integer questionId){
		if(dynamicOptions != null)
			dynamicOptions.remove(questionId);
	}

	/**
	 * Updates this formDef (as the main) with the parameter one
	 * 
	 * @param formDef
	 */
	public void refresh(FormDef formDef){
		this.id = formDef.getId();

		if(variableName.equals(formDef.getVariableName()))
			name = formDef.getName();

		for(int index = 0; index < formDef.getPageCount(); index++)
			refresh((PageDef)formDef.getPageAt(index));

		for(int index = 0; index < formDef.getSkipRuleCount(); index++)
			formDef.getSkipRuleAt(index).refresh(this, formDef);
	}

	private void refresh(PageDef pageDef){
		for(int index = 0; index < pages.size(); index++)
			((PageDef)pages.get(index)).refresh(pageDef);
	}

	public int getQuestionCount(){
		if(pages == null)
			return 0;

		int count = 0;
		for(int index = 0; index < pages.size(); index++)
			count += getPageAt(index).getQuestionCount();

		return count;
	}

	public void updateRuleConditionValue(String origValue, String newValue){
		for(int index = 0; index < getSkipRuleCount(); index++)
			getSkipRuleAt(index).updateConditionValue(origValue, newValue);

		for(int index = 0; index < getValidationRuleCount(); index++)
			getValidationRuleAt(index).updateConditionValue(origValue, newValue);
	}

	public Element getLanguageNode() {
		com.google.gwt.xml.client.Document doc = XMLParser.createDocument();
		Element rootNode = doc.createElement("xform");
		rootNode.setAttribute(XformConverter.ATTRIBUTE_NAME_ID, id+"");
		doc.appendChild(rootNode);

		if(dataNode != null){
			Element node = doc.createElement(XformConverter.NODE_NAME_TEXT);
			node.setAttribute(XformConverter.ATTRIBUTE_NAME_XPATH, FormUtil.getNodePath(dataNode)+"[@name]");
			node.setAttribute(XformConverter.ATTRIBUTE_NAME_VALUE, name);
			rootNode.appendChild(node);

			if(pages != null){
				for(int index = 0; index < pages.size(); index++)
					((PageDef)pages.elementAt(index)).buildLanguageNodes(doc, rootNode);
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
			
			/*XPathExpression xpls = new XPathExpression(this.doc, "xforms/model/instance/newform1"); //"/xforms/model/instance/newform1"
			Vector result = xpls.getResult();
			if(result.size() > 0)
				System.out.println(result.get(0));*/
		}

		return rootNode;
	}
	
	public static FormDef getFormDef(Object formItem){
		if(formItem instanceof FormDef)
			return (FormDef)formItem;
		else if(formItem instanceof PageDef)
			return ((PageDef)formItem).getParent();
		else if(formItem instanceof QuestionDef){
			Object item = ((QuestionDef)formItem).getParent();
			return getFormDef(item);
		}
		else if(formItem instanceof OptionDef){
			Object item = ((OptionDef)formItem).getParent();
			return getFormDef(item);
		}

		return null;
	}
	
	public void clearChangeListeners(){
		if(pages == null)
			return;

		for(int i=0; i<pages.size(); i++)
			((PageDef)pages.elementAt(i)).clearChangeListeners();
	}
}
