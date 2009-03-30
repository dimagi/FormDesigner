package org.purc.purcforms.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.purc.purcforms.client.xforms.XformConverter;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;

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
	
	/** The xforms document.(for easy syncing between the object model and actual xforms document. */
	private Document doc;
	private Element dataNode;
	private Element xformsNode;
	private Element modelNode;
	
	private String layout;
	private String xform;
	
	
	/** Constructs a form definition object. */
	public FormDef() {

	}
	
	public FormDef(FormDef formDef) {
		setId(formDef.getId());
		setName(formDef.getName());
		
		//I just don't think we need this in addition to the id
		setVariableName(formDef.getVariableName());
		
		setDescriptionTemplate(formDef.getDescriptionTemplate());
		copyPages(formDef.getPages());
		copySkipRules(formDef.getSkipRules());
		copyValidationRules(formDef.getValidationRules());
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
	public FormDef(int id, String name, String variableName,Vector pages, Vector skipRules, Vector validationRules, String descTemplate) {
		setId(id);
		setName(name);
		
		//I just don't think we need this in addition to the id
		setVariableName(variableName);
		
		setPages(pages);
		setSkipRules(skipRules);
		setValidationRules(validationRules);
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
	
	public void setPageName(String name){
		((PageDef)pages.elementAt(pages.size()-1)).setName(name);
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

	public String getDescriptionTemplate() {
		return descriptionTemplate;
	}

	public void setDescriptionTemplate(String descriptionTemplate) {
		this.descriptionTemplate = descriptionTemplate;
	}
	
	public String getLayout() {
		return layout;
	}

	public void setLayout(String layout) {
		this.layout = layout;
	}

	public String getXform() {
		return xform;
	}

	public void setXform(String xform) {
		this.xform = xform;
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
		if(!dataNode.getNodeName().equalsIgnoreCase(variableName))
			dataNode = XformConverter.renameNode(dataNode,variableName);
		
		if(dataNode != null){
			if(descriptionTemplate == null || descriptionTemplate.trim().length() == 0)
				dataNode.removeAttribute(XformConverter.ATTRIBUTE_NAME_DESCRIPTION_TEMPLATE);
			else
				dataNode.setAttribute(XformConverter.ATTRIBUTE_NAME_DESCRIPTION_TEMPLATE, descriptionTemplate);
		}
		
		for(int i=0; i<pages.size(); i++){
			PageDef pageDef = (PageDef)pages.elementAt(i);
			pageDef.updateDoc(doc,xformsNode,this,dataNode,modelNode,withData);
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
	
	public void removePage(PageDef pageDef){
		/*for(int i=0; i<pages.size(); i++){
			((PageDef)pages.elementAt(i)).removeAllQuestions();
		}*/
		
		pageDef.removeAllQuestions();
		
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
			if(((PageDef)pages.elementAt(i)).removeQuestion(qtnDef))
				return true;
		}
		return false;
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
		return skipRules.remove(skipRule);
	}
	
	public boolean removeValidationRule(ValidationRule validationRule){
		return validationRules.remove(validationRule);
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
}
