package org.openrosa.client.xforms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.openrosa.client.model.DynamicOptionDef;
import org.openrosa.client.model.FormDef;
import org.openrosa.client.model.OptionDef;
import org.openrosa.client.model.QuestionDef;
import org.purc.purcforms.client.xforms.XformConstants;
import org.purc.purcforms.client.xforms.XmlUtil;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;


/**
 * builds itemset portions of xforms documents, together with their instance data,
 * from dynamic option definition objects.
 * 
 * @author daniel
 *
 */
public class ItemsetBuilder {

	/**
	 * All methods in this class are static and hence we expect no external
	 * Instantiation of this class.
	 */
	private ItemsetBuilder(){

	}


	/**
	 * Converts a dynamic option definition object to an xform.
	 * 
	 * @param doc the xforms document.
	 * @param dynamicOptionDef the dynamic option definition object.
	 * @param parentQuestionDef the question whose selected option determines the allowed options for the dynamic option definition object.
	 * @param formDef the form definition object.
	 * @return true if the conversion was completed successfully.
	 */
	public static boolean fromDynamicOptionDef2Xform(Document doc, DynamicOptionDef dynamicOptionDef, QuestionDef parentQuestionDef, FormDef formDef){
		QuestionDef questionDef = formDef.getQuestion(dynamicOptionDef.getQuestionId());
		if(questionDef == null)
			return true;

		Element modelNode = formDef.getModelNode();
		Element instanceNode =  doc.createElement(XformConstants.NODE_NAME_INSTANCE);
		instanceNode.setAttribute(XformConstants.ATTRIBUTE_NAME_ID, questionDef.getBinding());
		NodeList nodes = modelNode.getElementsByTagName(XformConstants.NODE_NAME_INSTANCE);
		if(nodes.getLength() == 0)
			nodes = modelNode.getElementsByTagName(XformConstants.NODE_NAME_INSTANCE_MINUS_PREFIX); //TODO What happens when we pass a name with a prefix?
		modelNode.insertBefore(instanceNode, XmlUtil.getNextElementSibling((Element)nodes.item(nodes.getLength() - 1)));

		Element dataNode =  doc.createElement("dynamiclist"/*questionDef.getVariableName()*/);
		instanceNode.appendChild(dataNode);
		dynamicOptionDef.setDataNode(dataNode);

		//Some times the FirstOptionNode can be null. eg when a form is opened with a type
		//other than single select dynamic and then changed to it.
		if(questionDef.getFirstOptionNode() == null)
			questionDef.setFirstOptionNode(createDynamicOptionDefNode(doc,questionDef.getControlNode()));
		Element itemSetNode = questionDef.getFirstOptionNode();
		itemSetNode.setAttribute(XformConstants.ATTRIBUTE_NAME_NODESET, "instance('"+ questionDef.getBinding()+"')/item[@parent=instance('"+formDef.getVariableName()+"')/"+parentQuestionDef.getBinding()+"]");

		
		HashMap<Integer,List<OptionDef>> parentToChildOptions = dynamicOptionDef.getParentToChildOptions();
		if(parentToChildOptions != null){
			Iterator<Entry<Integer,List<OptionDef>>> iterator = parentToChildOptions.entrySet().iterator();
			while(iterator.hasNext()){
				Entry<Integer,List<OptionDef>> entry = iterator.next();
				List<OptionDef> list = entry.getValue();

				OptionDef parentOptionDef = null; //parentQuestionDef.getOption(entry.getKey());
				
				if(parentQuestionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE)
					parentOptionDef = parentQuestionDef.getOption(entry.getKey());
				else if(parentQuestionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC)
					parentOptionDef = formDef.getDynamicOptionDef(parentQuestionDef.getId(), entry.getKey());
				
				if(parentOptionDef == null){
					//Parent question options are not yet loaded.
					//modelNode.removeChild(instanceNode);
					return false;//continue;
				}

				for(int index = 0; index < list.size(); index++){
					OptionDef optionDef = list.get(index);
					addNewDynamicOption(doc, optionDef, parentOptionDef, dataNode);
					
					//TODO The line below makes the application hang when one clicks
					//the submit button on a not yet saved form.
					//questionDef.addOption(optionDef,false); //Bug increasing list we are iterating.
				}
			}
		}

		//Some times the FirstOptionNode can be null. eg when a form is opened with a type
		//other than single select dynamic and then changed to it.
		/*if(questionDef.getFirstOptionNode() == null)
			questionDef.setFirstOptionNode(createDynamicOptionDefNode(doc,questionDef.getControlNode()));
		Element itemSetNode = questionDef.getFirstOptionNode();
		itemSetNode.setAttribute(XformConstants.ATTRIBUTE_NAME_NODESET, "instance('"+ questionDef.getVariableName()+"')/item[@parent=instance('"+formDef.getVariableName()+"')/"+parentQuestionDef.getVariableName()+"]");*/

		return true;
	}


	/**
	 * Adds a new xforms node for an option definition object of a dynamic option definition object.
	 * 
	 * @param doc the xforms document.
	 * @param optionDef the new option definition object.
	 * @param parentOptionDef the parent option definition object whose being selected results into
	 *                        the child question list where the new option we are adding belongs.
	 * @param dataNode the dynamic option definition xforms data node.
	 */
	private static void addNewDynamicOption(Document doc, OptionDef optionDef, OptionDef parentOptionDef, Element dataNode){
		Element itemNode =  doc.createElement("item");
		optionDef.setControlNode(itemNode);

		Element node =  doc.createElement("label");
		node.appendChild(doc.createTextNode(optionDef.getText()));
		itemNode.appendChild(node);
		optionDef.setLabelNode(node);

		node =  doc.createElement("value");
		node.appendChild(doc.createTextNode(optionDef.getBinding()));
		itemNode.appendChild(node);
		optionDef.setValueNode(node);

		itemNode.setAttribute(XformConstants.ATTRIBUTE_NAME_ID, optionDef.getBinding());
		itemNode.setAttribute(XformConstants.ATTRIBUTE_NAME_PARENT, parentOptionDef.getBinding());

		dataNode.appendChild(itemNode);
	}


	/**
	 * Creates an itemset node for a single select dynamic type of question.
	 * 
	 * @param doc the xforms document.
	 * @param inputNode the xforms select1 node which is the parent of this itemset node. 
	 * @return the itemset node.
	 */
	public static Element createDynamicOptionDefNode(Document doc, Element inputNode){
		Element itemSetNode =  doc.createElement(XformConstants.NODE_NAME_ITEMSET);
		itemSetNode.setAttribute(XformConstants.ATTRIBUTE_NAME_NODESET, "");

		Element node =  doc.createElement(XformConstants.NODE_NAME_LABEL);
		node.setAttribute(XformConstants.ATTRIBUTE_NAME_REF, "label");
		itemSetNode.appendChild(node);

		node =  doc.createElement(XformConstants.NODE_NAME_VALUE);
		node.setAttribute(XformConstants.ATTRIBUTE_NAME_REF, "value");
		itemSetNode.appendChild(node);

		inputNode.appendChild(itemSetNode);
		//optionDef.setControlNode(itemSetNode);
		return itemSetNode;
	}


	/**
	 * For creating dynamic options instance node for those that were not created in the
	 * first pass because of dependent dynamic options not have also been created yet.
	 * To see this, first create a new dynamic options question which depends on another
	 * dynamic options questions and then try saving. The second question's instance
	 * values will only be created during second save, if this method is not called.
	 * 
	 * @param dynamicOptions a map of dynamic option definition objects keyed by their parent question identifier.
	 * @param questions list of parent questions whose selected option determines the allowed
	 * 					options for the child question as defined in the dynamic option definition object.
	 * @param formDef the form to which this dynamic options belong.
	 * @param doc the xforms document.
	 */
	public static void updateDynamicOptions(HashMap<Integer,DynamicOptionDef> dynamicOptions, List<QuestionDef> questions, FormDef formDef,Document doc){
		List<QuestionDef> newDynQtns = new ArrayList<QuestionDef>();

		for(QuestionDef qtn : questions){
			if(!fromDynamicOptionDef2Xform(doc,dynamicOptions.get(qtn.getId()),qtn,formDef))
				newDynQtns.add(qtn);
		}

		//The size must have decrease, else we shall get infinite recursion.
		if(newDynQtns.size() > 0 && newDynQtns.size() < questions.size())
			updateDynamicOptions(dynamicOptions,newDynQtns,formDef,doc);
	}


	/**
	 * Updates the xforms document, that a dynamic option definition object references, with
	 * the current changes in the dynamic option definition object.
	 * 
	 * @param formDef the form definition object to which the dynamic option definition object belongs.
	 * @param parentQuestionDef the parent question definition object for the dynamic option definition.
	 * @param dynamicOptionDef the dynamic option definition object.
	 */
	public static void updateDynamicOptionDef(FormDef formDef, QuestionDef parentQuestionDef, DynamicOptionDef dynamicOptionDef){
		HashMap<Integer,List<OptionDef>> parentToChildOptions = dynamicOptionDef.getParentToChildOptions();
		if(parentToChildOptions == null)
			return;
		
		Iterator<Entry<Integer,List<OptionDef>>> iterator = parentToChildOptions.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<Integer,List<OptionDef>> entry = iterator.next();
			List<OptionDef> list = entry.getValue();

			OptionDef parentOptionDef = null;
			if(parentQuestionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE)
				parentOptionDef = parentQuestionDef.getOption(entry.getKey());
			else if(parentQuestionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC)
				parentOptionDef = formDef.getDynamicOptionDef(parentQuestionDef.getId(), entry.getKey());
			
			if(parentOptionDef == null)
				continue;

			for(int index = 0; index < list.size(); index++){
				OptionDef optionDef = list.get(index);

				if(optionDef.getControlNode() == null)
					addNewDynamicOption(formDef.getDoc(), list.get(index), parentOptionDef, dynamicOptionDef.getDataNode());
				else{
					XmlUtil.setTextNodeValue(optionDef.getLabelNode(),optionDef.getText());
					XmlUtil.setTextNodeValue(optionDef.getValueNode(),optionDef.getBinding());
					optionDef.getControlNode().setAttribute(XformConstants.ATTRIBUTE_NAME_ID, optionDef.getBinding());
					optionDef.getControlNode().setAttribute(XformConstants.ATTRIBUTE_NAME_PARENT, parentOptionDef.getBinding());
				}
			}
		}
	}
}
