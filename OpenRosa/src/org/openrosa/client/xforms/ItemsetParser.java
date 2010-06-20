package org.openrosa.client.xforms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openrosa.client.model.DynamicOptionDef;
import org.openrosa.client.model.FormDef;
import org.openrosa.client.model.OptionDef;
import org.openrosa.client.model.QuestionDef;
import org.purc.purcforms.client.xforms.XformConstants;
import org.purc.purcforms.client.xforms.XformUtil;
import org.purc.purcforms.client.xforms.XmlUtil;

import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;


/**
 * Parse itemset portions of xforms documents, together with their instance data,
 * and builds the dynamic option definition objects of the model.
 * 
 * @author daniel
 *
 */
public class ItemsetParser {

	/**
	 * All methods in this class are static and hence we expect no external
	 * Instantiation of this class.
	 */
	private ItemsetParser(){

	}
	
	
	/**
	 * Parses dynamic option lists for questions whose parent questions had not been yet 
	 * processed during the earlier passes because of question ordering in the xform.
	 * 
	 * @param formDef the form definition.
	 * @param orphanDynOptionQns the list of unprocessed dynamic option child questions.
	 */
	public static void parseOrphanDynOptionQns(FormDef formDef, List<QuestionDef> orphanDynOptionQns){
		int orgSize = orphanDynOptionQns.size();
		if(orgSize == 0)
			return;

		//We are using an array copy because we will modify the orphanDynOptionQns list
		//as we loop through it and hence do not want concurrency modification exceptions.
		Object[] qtns = orphanDynOptionQns.toArray();
		for(int index = 0; index < qtns.length; index++){
			QuestionDef questionDef = (QuestionDef)qtns[index];
			if(questionDef.getFirstOptionNode() != null)
				parseDynamicOptionsList(questionDef, questionDef.getFirstOptionNode().getAttribute(XformConstants.ATTRIBUTE_NAME_NODESET), formDef,orphanDynOptionQns);
		}

		//If we have more to process and the number has reduced, just continue
		//Else if number is the same then we 
		int newSize = orphanDynOptionQns.size();
		if(newSize > 0 && newSize < orgSize)
			parseOrphanDynOptionQns(formDef,orphanDynOptionQns);
	}
	
	
	/**
	 * Parses an xforms dynamic options instance node child, whose name is dynamiclist, and updates the given dynamic
	 * option definition object.
	 * 
	 * @param dynamicOptionDef the dynamic option definition object.
	 * @param questionDef the child question that this dynamic option definition references
	 * @param parentQuestionDef the parent question to this dyanamic option question.
	 * @param node the instance node child, which called dynamiclist.
	 * @param optionDef the child option is is currently being parsed.
	 * @param parentOptionIdMap the map of child options keyed by their parent option ids.
	 * @param formDef the form to which the dynamic option definition object belongs.
	 * @param orphanDynOptionQns a list of dynamic option definition questions who parent
	 *                           questions have not yet been parsed.
	 */
	public static void parseDynamicOptions(DynamicOptionDef dynamicOptionDef, QuestionDef questionDef, QuestionDef parentQuestionDef, Node node, OptionDef optionDef, HashMap<String,Integer> parentOptionIdMap, FormDef formDef, List<QuestionDef> orphanDynOptionQns){
		String label = "";
		String value = "";
		Element labelNode = null;
		Element valueNode = null;

		NodeList nodes = node.getChildNodes();		
		for(int index = 0; index < nodes.getLength(); index++){
			Node child = nodes.item(index);
			if(child.getNodeType() != Node.ELEMENT_NODE)
				continue;

			String name = child.getNodeName();
			//if(name.equals(NODE_NAME_ITEM_MINUS_PREFIX)){
			if(XmlUtil.nodeNameEquals(name,XformConstants.NODE_NAME_ITEM_MINUS_PREFIX)){
				String parent = ((Element)child).getAttribute(XformConstants.ATTRIBUTE_NAME_PARENT);
				if(parent == null || parent.trim().length() == 0)
					continue;

				optionDef = new OptionDef(dynamicOptionDef.getNextOptionId(true),label, value,questionDef);
				optionDef.setControlNode((Element)child);
				Integer optionId = parentOptionIdMap.get(parent);
				if(optionId == null){
					OptionDef optnDef = parentQuestionDef.getOptionWithValue(parent);
					if(parentQuestionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC){
						DynamicOptionDef dynOptionsDef = formDef.getChildDynamicOptions(parentQuestionDef.getId());
						if(dynOptionsDef == null)
							return;
						optnDef = dynOptionsDef.getOptionWithValue(parent);
					}
					if(optnDef == null){
						if(!orphanDynOptionQns.contains(questionDef))
							orphanDynOptionQns.add(questionDef);

						continue;
					}

					optionId = optnDef.getId();
					parentOptionIdMap.put(parent, optionId);
				}
				List<OptionDef> optionList = dynamicOptionDef.getOptionList(optionId);
				if(optionList == null){
					optionList = new ArrayList<OptionDef>();
					dynamicOptionDef.setOptionList(optionId, optionList);
				}
				optionList.add(optionDef);

				parseDynamicOptions(dynamicOptionDef,questionDef,parentQuestionDef,child,optionDef,parentOptionIdMap,formDef,orphanDynOptionQns);
			}
			//else if(name.equals(NODE_NAME_LABEL_MINUS_PREFIX)){
			else if(XmlUtil.nodeNameEquals(name,XformConstants.NODE_NAME_LABEL_MINUS_PREFIX)){
				if(child.getChildNodes().getLength() != 0){
					label = child.getChildNodes().item(0).getNodeValue().trim(); //questionDef.setText(child.getChildNodes().item(0).getNodeValue().trim());
					labelNode = (Element)child;
				}
			}
			//else if(name.equals(NODE_NAME_VALUE_MINUS_PREFIX)){
			else if(XmlUtil.nodeNameEquals(name,XformConstants.NODE_NAME_VALUE_MINUS_PREFIX)){
				if(child.getChildNodes().getLength() != 0){
					value = child.getChildNodes().item(0).getNodeValue().trim();
					valueNode = (Element)child;
				}
			}
		}

		if (!label.equals("") && !value.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
			if (optionDef != null){
				optionDef.setText(label);
				optionDef.setBinding(value);
				optionDef.setLabelNode(labelNode);
				optionDef.setValueNode(valueNode);
			}
		} 
	}

	
	/**
	 * Creates a new instance of a dynamic option definition object and builds its
	 * child to parent options.
	 * 
	 * @param questionDef the child question to be referenced by the dynamic option definition object. 
	 * @param nodeset the nodeset attribute value for the itemset of this dyanamic option definition object.
	 * @param formDef the form definition object to which this dynamic option list belongs.
	 * @param orphanDynOptionQns the list of unprocessed dynamic option child questions.
	 */
	public static void parseDynamicOptionsList(QuestionDef questionDef, String nodeset, FormDef formDef, List<QuestionDef> orphanDynOptionQns){
		if(nodeset == null || nodeset.trim().length() == 0)
			return;

		//Get the parent question bind id.
		String binding = ItemsetUtil.getParentQuestionBindId(nodeset);
		assert(binding != null);
		if(binding == null)
			return; //This can only be a bug

		//Return if parent question has not yet been parsed.
		QuestionDef parentQuestionDef = (QuestionDef)formDef.getElement(binding);
		if(parentQuestionDef == null)
			return;

		//Get the dynamic option definition instance id.
		String instanceId = ItemsetUtil.getChildInstanceId(nodeset);
		assert(instanceId != null);
		if(instanceId == null)
			return; //This can only be a bug

		//Get the dynamic option definition instance node.
		Element instanceNode = XformUtil.getInstanceNode(formDef.getModelNode(), instanceId);
		//assert(instanceNode != null);
		if(instanceNode == null)
			return; //This can only be a bug
		
		//Create a new dynamic option definition object.
		DynamicOptionDef dynamicOptionDef = new DynamicOptionDef();
		dynamicOptionDef.setQuestionId(questionDef.getId());
		
		//Go through its instance node child and get the first and only element node
		//whose name should be dynamiclist, then parse its children.
		NodeList nodes = instanceNode.getChildNodes();
		for(int index = 0; index < nodes.getLength(); index++){
			Node child = nodes.item(index);
			if(child.getNodeType() == Node.ELEMENT_NODE){
				assert(child.getNodeName().equals("dynamiclist"));
				dynamicOptionDef.setDataNode((Element)child);
				
				HashMap<String,Integer> parentOptionIdMap = new HashMap<String,Integer>();
				parseDynamicOptions(dynamicOptionDef,questionDef,parentQuestionDef,child,null,parentOptionIdMap,formDef,orphanDynOptionQns);
				break;
			}
		}

		//If processed completely (as in parent question found), remove from orphan list.
		if(dynamicOptionDef.getParentToChildOptions() != null && orphanDynOptionQns.contains(questionDef))
			orphanDynOptionQns.remove(questionDef);

		//Add this new dynamic option definition object to the form.
		formDef.setDynamicOptionDef(parentQuestionDef.getId(), dynamicOptionDef);
	}

}
