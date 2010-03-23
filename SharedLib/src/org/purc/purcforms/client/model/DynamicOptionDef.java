package org.purc.purcforms.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.xforms.ItemsetBuilder;
import org.purc.purcforms.client.xforms.ItemsetUtil;
import org.purc.purcforms.client.xforms.XformBuilder;
import org.purc.purcforms.client.xforms.XformConstants;

import com.google.gwt.xml.client.Element;


/**
 * This is the definition of options lists for a question whose list of options
 * depends on the selected option for another question. The question whose options
 * lists are defined by this object should be of type Single Select Dynamic.
 * 
 * @author daniel
 *
 */
public class DynamicOptionDef  implements Serializable{

	/** The question whose values are determined by or dependent on the answer of another 
	 * (parent) question. In other wards this question must be of type Single Select Dynamic.
	 *  This is the question we refer to as the child in this relationship.
	 */
	private int questionId;

	/** A map between each parent option and a list of possible options for the dependent question above. */
	private HashMap<Integer,List<OptionDef>> parentToChildOptions;

	/** 
	 * This is not persisted but rather used only during design mode
	 * to ensure that we have unique option ids.
	 */
	private static int nextOptionId = 1;

	/** An instance child node for this dynamic selection list definition object.
	 *  This node is the "<dynamiclist>" which is a child of <xf:instance id="question2">
	 *  where the questionId member variable is for a question whose binding is "question2".
	 */
	private Element dataNode;


	/**
	 * Creates a new instance of the dynamic option definition.
	 */
	public DynamicOptionDef(){

	}

	/**
	 * Creates a copy of a dynamic option definition object.
	 * 
	 * @param dynamicOptionDef the dynamic option definition object to copy.
	 * @param questionDef the question definition object referenced by this dynamic 
	 * 					  option definition object.
	 */
	public DynamicOptionDef(DynamicOptionDef dynamicOptionDef, QuestionDef questionDef){
		setQuestionId(dynamicOptionDef.getQuestionId()); //same as questionDef.getId()
		parentToChildOptions = new HashMap<Integer,List<OptionDef>>();

		Iterator<Entry<Integer,List<OptionDef>>> iterator = dynamicOptionDef.getParentToChildOptions().entrySet().iterator();
		while(iterator.hasNext()){
			Entry<Integer,List<OptionDef>> entry = iterator.next();
			List<OptionDef> list = entry.getValue();

			List<OptionDef> newList = new ArrayList<OptionDef>();
			for(int index = 0; index < list.size(); index++)
				newList.add(new OptionDef(list.get(index),questionDef));

			parentToChildOptions.put(new Integer(entry.getKey()), newList);
		}
	}

	public int getQuestionId() {
		return questionId;
	}

	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}

	public HashMap<Integer, List<OptionDef>> getParentToChildOptions() {
		return parentToChildOptions;
	}

	public void setParentToChildOptions(HashMap<Integer, List<OptionDef>> parentToChildOptions) {
		this.parentToChildOptions = parentToChildOptions;
	}

	/**
	 * Gets the list of options for a particular parent question selected option.
	 * 
	 * @param optionId the identifier of the parent question selected option.
	 * @return the option list.
	 */
	public List<OptionDef> getOptionList(Integer optionId){
		if(parentToChildOptions == null)
			return null;

		return parentToChildOptions.get(optionId);
	}
	
	/**
	 * Gets the entire list of options referenced by this dynamic option definition object.
	 * 
	 * @return the options list.
	 */
	public List<OptionDef> getOptions(){
		if(parentToChildOptions == null)
			return null;

		List<OptionDef> options = new ArrayList<OptionDef>();
		
		Iterator<Entry<Integer,List<OptionDef>>> iterator = parentToChildOptions.entrySet().iterator();
		while(iterator.hasNext()){
			List<OptionDef> list = iterator.next().getValue();
			for(int index = 0; index < list.size(); index++)
				options.add(list.get(index));
		}
		
		return options;
	}

	/**
	 * Sets the list of options for a given option in a parent question.
	 * 
	 * @param optionId the identifier of the parent question option.
	 * @param list the option list.
	 */
	public void setOptionList(Integer optionId, List<OptionDef> list){
		if(parentToChildOptions == null)
			parentToChildOptions = new HashMap<Integer, List<OptionDef>>();
		parentToChildOptions.put(optionId, list);
	}

	/**
	 * Removes the list of options for a given parent question option.
	 * 
	 * @param optionId the identifier of the parent question option.
	 */
	public void removeOptionList(Integer optionId){
		parentToChildOptions.remove(optionId);
	}

	/**
	 * Gets the next available option id.
	 * 
	 * @return the option id.
	 */
	public int getNextOptionId() {
		return nextOptionId;
	}

	/**
	 * Get the next available option id with a flag which tells whether to increment the counter.
	 * 
	 * @param increment set to true to increment the counter by one.
	 * @return the option id.
	 */
	public int getNextOptionId(boolean increment) {
		return nextOptionId++;
	}

	/**
	 * Sets the value of the next option id.
	 * 
	 * @param nextOptionId the option id value.
	 */
	public void setNextOptionId(int nextOptionId) {
		this.nextOptionId = nextOptionId;
	}

	/**
	 * Gets the size of the parent to child option mappings.
	 * 
	 * @return the numeric size.
	 */
	public int size(){
		if(parentToChildOptions == null)
			return 0;
		return parentToChildOptions.size();
	}

	/**
	 * Sets the xforms document node that has data for this object.
	 * 
	 * @param node the xforms node.
	 */
	public void setDataNode(Element node){
		this.dataNode = node;
	}

	/**
	 * Updates the xforms document with the current changes in the 
	 * dynamic option definition object.
	 * 
	 * @param formDef the form definition object.
	 * @param parentQuestionDef the parent question whose options determine the list
	 *                          of options that this object contains.
	 */
	public void updateDoc(FormDef formDef, QuestionDef parentQuestionDef){
		//if(parentToChildOptions == null)
		//	return;

		if(dataNode == null)
			ItemsetBuilder.fromDynamicOptionDef2Xform(formDef.getDoc(),this,parentQuestionDef,formDef);
		else{
			//Update the nodeset child instance id
			QuestionDef  questionDef = formDef.getQuestion(questionId);
			if(questionDef == null || questionDef.getFirstOptionNode() == null)
				return;

			String nodeset = questionDef.getFirstOptionNode().getAttribute(XformConstants.ATTRIBUTE_NAME_NODESET);
			if(nodeset == null)
				return;
			
			if(nodeset.trim().length() == 0 && questionDef.getFirstOptionNode() != null)
				questionDef.getFirstOptionNode().setAttribute(XformConstants.ATTRIBUTE_NAME_NODESET, "instance('"+ questionDef.getVariableName()+"')/item[@parent=instance('"+formDef.getVariableName()+"')/"+parentQuestionDef.getVariableName()+"]");

			
			String instanceId = ItemsetUtil.getChildInstanceId(nodeset);
			if(!(instanceId == null || instanceId.equals(questionDef.getVariableName()))){
				nodeset = nodeset.replace("'"+instanceId+"'", "'"+questionDef.getVariableName()+"'");
				questionDef.getFirstOptionNode().setAttribute(XformConstants.ATTRIBUTE_NAME_NODESET, nodeset);
			}

			//Update the nodeset parent instance id
			instanceId = ItemsetUtil.getParentQuestionBindId(nodeset);
			if(!(instanceId == null || instanceId.equals(parentQuestionDef.getVariableName()))){
				nodeset = nodeset.replace("')/"+instanceId+"]", "')/"+parentQuestionDef.getVariableName()+"]");
				questionDef.getFirstOptionNode().setAttribute(XformConstants.ATTRIBUTE_NAME_NODESET, nodeset);
			}
			
			//update the nodeset form instance id
			instanceId = ItemsetUtil.getFormInstanceId(nodeset);
			if(!(instanceId == null || instanceId.equals(formDef.getVariableName()))){
				nodeset = nodeset.replace("'"+instanceId+"'", "'"+formDef.getVariableName()+"'");
				questionDef.getFirstOptionNode().setAttribute(XformConstants.ATTRIBUTE_NAME_NODESET, nodeset);
			}
			
			//Update the instance id
			if(dataNode.getParentNode() != null)
				((Element)dataNode.getParentNode()).setAttribute(XformConstants.ATTRIBUTE_NAME_ID, questionDef.getVariableName());
		
			ItemsetBuilder.updateDynamicOptionDef(formDef, parentQuestionDef, this);
		}
	}

	/**
	 * Gets the xforms document node that has data for this object.
	 * 
	 * @return the xforms node.
	 */
	public Element getDataNode(){
		return dataNode;
	}

	/**
	 * Gets an option with a particular value or binding.
	 * 
	 * @param value the option value or binding.
	 * @return the option.
	 */
	public OptionDef getOptionWithValue(String value){
		if(parentToChildOptions == null || value == null)
			return null;

		Iterator<Entry<Integer,List<OptionDef>>> iterator = parentToChildOptions.entrySet().iterator();
		while(iterator.hasNext()){
			OptionDef optionDef = getOptionWithValue(iterator.next().getValue(),value);
			if(optionDef != null)
				return optionDef;
		}
		return null;
	}
	
	/**
	 * Gets an option with a particular text.
	 * 
	 * @param text the option text.
	 * @return the option.
	 */
	public OptionDef getOptionWithText(String text){
		if(parentToChildOptions == null || text == null)
			return null;

		Iterator<Entry<Integer,List<OptionDef>>> iterator = parentToChildOptions.entrySet().iterator();
		while(iterator.hasNext()){
			OptionDef optionDef = getOptionWithText(iterator.next().getValue(),text);
			if(optionDef != null)
				return optionDef;
		}
		return null;
	}
	
	/**
	 * Gets an option with a particular text from an options list.
	 * 
	 * @param options the options list.
	 * @param text the text of the option to get.
	 * @return the option.
	 */
	private OptionDef getOptionWithText(List<OptionDef> options, String text){
		List list = (List)options;
		for(int i=0; i<list.size(); i++){
			OptionDef optionDef = (OptionDef)list.get(i);
			if(optionDef.getText().equals(text))
				return optionDef;
		}
		return null;
	}

	/**
	 * Gets an option with a particular value or binding from an options list.
	 * 
	 * @param options the options list.
	 * @param value the value or binding of the option to get.
	 * @return the option.
	 */
	private OptionDef getOptionWithValue(List<OptionDef> options, String value){
		List list = (List)options;
		for(int i=0; i<list.size(); i++){
			OptionDef optionDef = (OptionDef)list.get(i);
			if(optionDef.getVariableName().equals(value))
				return optionDef;
		}
		return null;
	}
	
	/**
	 * Gets an option with a particular id.
	 * 
	 * @param id the option id.
	 * @return the option.
	 */
	public OptionDef getOptionWithId(int id){
		if(parentToChildOptions == null)
			return null;

		Iterator<Entry<Integer,List<OptionDef>>> iterator = parentToChildOptions.entrySet().iterator();
		while(iterator.hasNext()){
			OptionDef optionDef = getOptionWithId(iterator.next().getValue(),id);
			if(optionDef != null)
				return optionDef;
		}
		return null;
	}
	
	/**
	 * Gets an option with a particular id from an options list.
	 * 
	 * @param options the options list.
	 * @param id the id of the option to get.
	 * @return the option.
	 */
	private OptionDef getOptionWithId(List<OptionDef> options, int id){
		List list = (List)options;
		for(int i=0; i<list.size(); i++){
			OptionDef optionDef = (OptionDef)list.get(i);
			if(optionDef.getId() == id)
				return optionDef;
		}
		return null;
	}
	
	/**
	 * Builds the locale xpath xpressions and their text values for this question.
	 * 
	 * @param formDef the form definition object that this object belongs to.
	 * @param parentLangNode the parent language node we are building onto.
	 */
	public void buildLanguageNodes(FormDef formDef, Element parentLangNode){
		if(parentToChildOptions == null)
			return;
		
		if(dataNode == null)
			return;
		
		QuestionDef questionDef = formDef.getQuestion(questionId);
		if(questionDef == null)
			return;
		
		if(dataNode.getParentNode() == null)
			return;
		
		String xpath = FormUtil.getNodePath(dataNode.getParentNode());
		String id = ((Element)dataNode.getParentNode()).getAttribute(XformConstants.ATTRIBUTE_NAME_ID);
		if(id != null && id.trim().length() > 0)
			xpath += "[@" + XformConstants.ATTRIBUTE_NAME_ID + "='" + questionDef.getVariableName() + "']";
		
		xpath += "/" + FormUtil.getNodeName(dataNode);
		
		Iterator<Entry<Integer,List<OptionDef>>> iterator = parentToChildOptions.entrySet().iterator();
		while(iterator.hasNext()){
			List<OptionDef> list = iterator.next().getValue();
			for(int index = 0; index < list.size(); index++)
				list.get(index).buildLanguageNodes(xpath,formDef.getDoc(), parentLangNode);
		}
	}	
	
	/**
	 * Updates this dynamicOptionDef (as the main from the refresh source) with the parameter one
	 * 
	 * @param dstFormDef the destination form definition object.
	 * @param srcFormDef the old form definition object to copy from.
	 * @param newDynOptionDef the new dynamic option definition object that we are building.
	 * @param srcDynOptionDef the old dynamic option definition object that we are copying from.
	 * @param newParentQtnDef the new parent question definition object.
	 * @param oldParentQtnDef the old parent definition object.
	 * @param oldChildQtnDef the old child definition object.
	 * @param newChildQtnDef the new child definition object.
	 * 
	 */
	public void refresh(FormDef dstFormDef, FormDef srcFormDef,DynamicOptionDef newDynOptionDef, DynamicOptionDef srcDynOptionDef, QuestionDef newParentQtnDef, QuestionDef oldParentQtnDef, QuestionDef oldChildQtnDef, QuestionDef newChildQtnDef){
		parentToChildOptions = new HashMap<Integer,List<OptionDef>>();

		Iterator<Entry<Integer,List<OptionDef>>> iterator = srcDynOptionDef.getParentToChildOptions().entrySet().iterator();
		while(iterator.hasNext()){
			Entry<Integer,List<OptionDef>> entry = iterator.next();
			
			OptionDef optionDef = oldParentQtnDef.getOption(entry.getKey());
			if(optionDef == null)
				continue; //how can this be????.
			
			optionDef = newParentQtnDef.getOptionWithValue(optionDef.getVariableName());
			if(optionDef == null)
				continue; //possibly option deleted.
			
			List<OptionDef> newList = refreshList(newDynOptionDef,entry.getValue(),newParentQtnDef, oldParentQtnDef, oldChildQtnDef, newChildQtnDef);
			if(newList.size() > 0)
				parentToChildOptions.put(new Integer(optionDef.getId()), newList);
		}
	}
	
	private List<OptionDef> refreshList(DynamicOptionDef dynamicOptionDef, List<OptionDef> list, QuestionDef newParentQtnDef, QuestionDef oldParentQtnDef, QuestionDef oldChildQtnDef, QuestionDef newChildQtnDef){
		List<OptionDef> newList = new ArrayList<OptionDef>();
		
		for(int index = 0; index < list.size(); index++){
			OptionDef oldOptionDef = list.get(index);
			
			OptionDef newOptionDef = newParentQtnDef.getOptionWithValue(oldOptionDef.getVariableName());
			if(newOptionDef == null){
				//We do not want to lose options we had created before refresh.
				//The user should manually delete them after a refresh, if they don't want them.
				
				//TODO The new optiondef may need a new id 
				newOptionDef = new OptionDef(oldOptionDef,newChildQtnDef);
				newOptionDef.setId(dynamicOptionDef.getNextOptionId());
			}
			else
				newOptionDef.setText(oldOptionDef.getText());
			
			newList.add(newOptionDef);
		}
		
		return newList;
	}
}
