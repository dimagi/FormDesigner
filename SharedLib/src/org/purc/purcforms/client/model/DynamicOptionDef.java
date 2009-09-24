package org.purc.purcforms.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.xforms.XformConverter;

import com.google.gwt.xml.client.Element;


/**
 * 
 * @author daniel
 *
 */
public class DynamicOptionDef  implements Serializable{

	/** The question whose values are determined by or dependent on the answer of another question. **/
	private int questionId;

	/** A map between each parent option and a list of possible options for the dependant question. */
	private HashMap<Integer,List<OptionDef>> parentToChildOptions;

	/** This is not persisted but rather used only during design mode. **/
	private static int nextOptionId = 1;

	private Element dataNode;


	public DynamicOptionDef(){

	}

	public DynamicOptionDef(DynamicOptionDef dynamicOptionDef, QuestionDef questionDef){
		setQuestionId(dynamicOptionDef.getQuestionId());
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

	public void setParentToChildOptions(
			HashMap<Integer, List<OptionDef>> parentToChildOptions) {
		this.parentToChildOptions = parentToChildOptions;
	}

	public List<OptionDef> getOptionList(Integer optionId){
		if(parentToChildOptions == null)
			return null;

		return parentToChildOptions.get(optionId);
	}
	
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

	public void setOptionList(Integer optionId, List<OptionDef> list){
		if(parentToChildOptions == null)
			parentToChildOptions = new HashMap<Integer, List<OptionDef>>();
		parentToChildOptions.put(optionId, list);
	}

	public void removeOptionList(Integer optionId){
		parentToChildOptions.remove(optionId);
	}

	public int getNextOptionId() {
		return nextOptionId;
	}

	public int getNextOptionId(boolean increment) {
		return nextOptionId++;
	}

	public void setNextOptionId(int nextOptionId) {
		this.nextOptionId = nextOptionId;
	}

	public int size(){
		if(parentToChildOptions == null)
			return 0;
		return parentToChildOptions.size();
	}

	public void setDataNode(Element node){
		this.dataNode = node;
	}

	public void updateDoc(FormDef formDef, QuestionDef parentQuestionDef){
		if(parentToChildOptions == null)
			return;

		if(dataNode == null)
			XformConverter.fromDynamicOptionDef2Xform(formDef.getDoc(),this,parentQuestionDef,formDef);
		else{
			//Update the nodeset child instance id
			QuestionDef  questionDef = formDef.getQuestion(questionId);
			if(questionDef == null || questionDef.getFirstOptionNode() == null)
				return;

			String nodeset = questionDef.getFirstOptionNode().getAttribute(XformConverter.ATTRIBUTE_NAME_NODESET);
			if(nodeset == null)
				return;
			
			if(nodeset.trim().length() == 0 && questionDef.getFirstOptionNode() != null)
				questionDef.getFirstOptionNode().setAttribute(XformConverter.ATTRIBUTE_NAME_NODESET, "instance('"+ questionDef.getVariableName()+"')/item[@parent=instance('"+formDef.getVariableName()+"')/"+parentQuestionDef.getVariableName()+"]");

			
			String instanceId = XformConverter.getDynamicOptionChildInstanceId(nodeset);
			if(!(instanceId == null || instanceId.equals(questionDef.getVariableName()))){
				nodeset = nodeset.replace("'"+instanceId+"'", "'"+questionDef.getVariableName()+"'");
				questionDef.getFirstOptionNode().setAttribute(XformConverter.ATTRIBUTE_NAME_NODESET, nodeset);
			}

			//Update the nodeset parent instance id
			instanceId = XformConverter.getDynamicOptionParentInstanceId(nodeset);
			if(!(instanceId == null || instanceId.equals(parentQuestionDef.getVariableName()))){
				nodeset = nodeset.replace("')/"+instanceId+"]", "')/"+parentQuestionDef.getVariableName()+"]");
				questionDef.getFirstOptionNode().setAttribute(XformConverter.ATTRIBUTE_NAME_NODESET, nodeset);
			}
			
			//update the nodeset form instance id
			instanceId = XformConverter.getDynamicOptionFormInstanceId(nodeset);
			if(!(instanceId == null || instanceId.equals(formDef.getVariableName()))){
				nodeset = nodeset.replace("'"+instanceId+"'", "'"+formDef.getVariableName()+"'");
				questionDef.getFirstOptionNode().setAttribute(XformConverter.ATTRIBUTE_NAME_NODESET, nodeset);
			}
			
			//Update the instance id
			if(dataNode.getParentNode() != null)
				((Element)dataNode.getParentNode()).setAttribute(XformConverter.ATTRIBUTE_NAME_ID, questionDef.getVariableName());
		
			XformConverter.updateDynamicOptionDef(formDef, parentQuestionDef, this);
		}
	}

	public Element getDataNode(){
		return dataNode;
	}

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
	
	private OptionDef getOptionWithText(List<OptionDef> options, String text){
		List list = (List)options;
		for(int i=0; i<list.size(); i++){
			OptionDef optionDef = (OptionDef)list.get(i);
			if(optionDef.getText().equals(text))
				return optionDef;
		}
		return null;
	}

	private OptionDef getOptionWithValue(List<OptionDef> options, String value){
		List list = (List)options;
		for(int i=0; i<list.size(); i++){
			OptionDef optionDef = (OptionDef)list.get(i);
			if(optionDef.getVariableName().equals(value))
				return optionDef;
		}
		return null;
	}
	
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
	
	private OptionDef getOptionWithId(List<OptionDef> options, int id){
		List list = (List)options;
		for(int i=0; i<list.size(); i++){
			OptionDef optionDef = (OptionDef)list.get(i);
			if(optionDef.getId() == id)
				return optionDef;
		}
		return null;
	}
	
	public void buildLanguageNodes(FormDef formDef, Element parentNode){
		if(parentToChildOptions == null)
			return;
		
		if(dataNode == null)
			return;
		
		QuestionDef questionDef = formDef.getQuestion(questionId);
		if(questionDef == null)
			return;
		
		String xpath = FormUtil.getNodePath(dataNode.getParentNode());
		String id = ((Element)dataNode.getParentNode()).getAttribute(XformConverter.ATTRIBUTE_NAME_ID);
		if(id != null && id.trim().length() > 0)
			xpath += "[@" + XformConverter.ATTRIBUTE_NAME_ID + "='" + questionDef.getVariableName() + "']";
		
		xpath += "/" + FormUtil.getNodeName(dataNode);
		
		Iterator<Entry<Integer,List<OptionDef>>> iterator = parentToChildOptions.entrySet().iterator();
		while(iterator.hasNext()){
			List<OptionDef> list = iterator.next().getValue();
			for(int index = 0; index < list.size(); index++)
				list.get(index).buildLanguageNodes(xpath,formDef.getDoc(), parentNode);
		}
	}	
	
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
