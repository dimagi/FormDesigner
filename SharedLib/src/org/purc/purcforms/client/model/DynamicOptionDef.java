package org.purc.purcforms.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.purc.purcforms.client.xforms.XformConverter;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;


/**
 * 
 * @author daniel
 *
 */
public class DynamicOptionDef  implements Serializable{
	
	/** The question whose values are determined by or dependent on another question. **/
	private int questionId;
	
	/** A map between each parent option and a list of possible options for the dependant question. */
	private HashMap<Integer,List<OptionDef>> parentToChildOptions;
	
	/** This is not persisted but rather used only during design mode. **/
	private int nextOptionId = 1;
	
	private Element dataNode;
	
	
	public DynamicOptionDef(){
		
	}
	
	public DynamicOptionDef(DynamicOptionDef dynamicOptionDef, QuestionDef questionDef){
		setQuestionId(dynamicOptionDef.getQuestionId());
		parentToChildOptions = new HashMap<Integer,List<OptionDef>>();
		
		if(parentToChildOptions == null)
			return;
		
		Iterator<Entry<Integer,List<OptionDef>>> iterator = parentToChildOptions.entrySet().iterator();
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
	
	public void updateDoc(Document doc, FormDef formDef, QuestionDef questionDef){
		if(parentToChildOptions == null)
			return;
		
		if(dataNode == null)
			XformConverter.fromDynamicOptionDef2Xform(doc,this,questionDef,formDef);
		else
			XformConverter.updateDynamicOptionDef(doc, questionDef, this);
	}
	
	public Element getDataNode(){
		return dataNode;
	}
}
