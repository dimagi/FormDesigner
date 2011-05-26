package org.openrosa.client.xforms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;

import org.openrosa.client.model.Calculation;
import org.openrosa.client.model.DynamicOptionDef;
import org.openrosa.client.model.FormDef;
import org.openrosa.client.model.GroupDef;
import org.openrosa.client.model.IFormElement;
import org.openrosa.client.model.QuestionDef;
import org.openrosa.client.model.SkipRule;
import org.openrosa.client.model.ValidationRule;
import org.openrosa.client.util.FormUtil;
import org.openrosa.client.util.Itext;
import org.openrosa.client.util.UUID;
import org.openrosa.client.xforms.XformConstants;
import org.openrosa.client.xforms.XformUtil;
import org.openrosa.client.xforms.XmlUtil;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;


/**
 * Builds an xforms document from a form definition object model components.
 * 
 * @author daniel
 *
 */
public class XformBuilder {

	/**
	 * All methods in this class are static and hence we expect no external
	 * Instantiation of this class.
	 */
	private XformBuilder(){

	}


	/**
	 * Converts a form definition object to its xforms xml.
	 * 
	 * @param formDef the form definition object.
	 * @return the xforms xml.
	 */
	public static String fromFormDef2Xform(FormDef formDef){

		//Create a new document.
		Document doc = XMLParser.createDocument();
		doc.appendChild(doc.createProcessingInstruction("xml", "version=\"1.0\" encoding=\"UTF-8\""));
		formDef.setDoc(doc);

		//Create the document root node.
		Element xformsNode = doc.createElement(XformConstants.NODE_NAME_XFORMS);
		formDef.setXformsNode(xformsNode);

		//Set the xf and xsd prefix values and then add the root node to the document. 
		xformsNode.setAttribute(XformConstants.XML_NAMESPACE /*XformConstants.XML_NAMESPACE_PREFIX+XformConstants.PREFIX_XFORMS*/, XformConstants.NAMESPACE_XFORMS);
		xformsNode.setAttribute(XformConstants.XML_NAMESPACE_PREFIX+XformConstants.PREFIX_XML_SCHEMA, XformConstants.NAMESPACE_XML_SCHEMA);
		doc.appendChild(xformsNode);

		//Create the xforms model node and add it to the root node.
		Element modelNode =  doc.createElement(XformConstants.NODE_NAME_MODEL);
		xformsNode.appendChild(modelNode);

		//Now build the rest of the xforms elements and add them to the document.
		buildXform(formDef,doc,xformsNode,modelNode);

		//Return the string representation of the document to the caller.
		return XmlUtil.fromDoc2String(doc);
	}


	/**
	 * Builds a form definition object's xforms document whose root and model 
	 * elements have already been created.
	 * 
	 * @param formDef the form definition object.
	 * @param doc the xforms document.
	 * @param parentNode the xforms node to which is the parent of the UI nodes.
	 * @param modelNode the xforms document model node.
	 */
	public static void buildXform(FormDef formDef, Document doc, Element parentNode, Element modelNode){

		//Create the instance node and add it to the model node.
		Element instanceNode =  doc.createElement(XformConstants.NODE_NAME_INSTANCE);
		modelNode.appendChild(instanceNode);
		formDef.setModelNode(modelNode);

		//Create the form data node and add it to the instance node.
		Element parentDataNode =  doc.createElement("data");
		parentDataNode.setAttribute("xmlnsCHANGEME", "http://openrosa.org/formdesigner/"+UUID.uuid());
		parentDataNode.setAttribute("xmlns:jrm", XformUtil.getDataXMLNSjrm());
		parentDataNode.setAttribute("uiVersion", "1");
		parentDataNode.setAttribute("version", "1");
		instanceNode.appendChild(parentDataNode);
		formDef.setDataNode(parentDataNode);


		//Check if we have any pages.
		if(formDef.getChildren() == null){
			return;
		}
		
		//Build the ui nodes for all questions in each page.
		for(int pageNo=0; pageNo<formDef.getChildCount(); pageNo++){
			IFormElement element = formDef.getChildAt(pageNo);
			if(element instanceof GroupDef){
				fromGroupDef2Xform((GroupDef)element,doc,parentNode,formDef,parentDataNode,modelNode);
			}else{
				UiElementBuilder.fromQuestionDef2Xform((QuestionDef)element,doc,formDef,parentDataNode,modelNode,parentNode);
			}
		}

		//Build relevant s for the skip rules.
		Vector rules = formDef.getSkipRules();
		if(rules != null){
			for(int i=0; i<rules.size(); i++)
				RelevantBuilder.fromSkipRule2Xform((SkipRule)rules.elementAt(i),formDef, doc);
		}

		//Build constraints for the validation rules.
		rules = formDef.getValidationRules();
		if(rules != null){
			for(int i=0; i<rules.size(); i++)
				ConstraintBuilder.fromValidationRule2Xform((ValidationRule)rules.elementAt(i),formDef);
		}

		//Build calculates for calculations
		for(int index = 0; index < formDef.getCalculationCount(); index++){
			Calculation calculation = formDef.getCalculationAt(index);
			IFormElement elementDef = formDef.getElement(calculation.getQuestionId());
			if(elementDef == null)
				continue;
			Element node = elementDef.getBindNode() != null ? elementDef.getBindNode() : elementDef.getControlNode();
			String expr = calculation.getCalculateExpression();
			expr = XmlUtil.escapeXMLAttribute(expr);
			if(node != null && expr != null && expr.length() != 0){
				node.setAttribute(XformConstants.ATTRIBUTE_NAME_CALCULATE, expr);
			}else{
				node.removeAttribute(XformConstants.ATTRIBUTE_NAME_CALCULATE);
			}
		}

		//Build itemsets for dynamic option definition objects.
		List<QuestionDef> orphanQuestions = new ArrayList<QuestionDef>();
		HashMap<Integer,DynamicOptionDef> dynamicOptions = formDef.getDynamicOptions();
		if(dynamicOptions != null){
			Iterator<Entry<Integer,DynamicOptionDef>> iterator = dynamicOptions.entrySet().iterator();
			while(iterator.hasNext()){
				Entry<Integer,DynamicOptionDef> entry = iterator.next();
				DynamicOptionDef dynamicOptionDef = entry.getValue();
				QuestionDef questionDef = formDef.getQuestion(entry.getKey());
				if(questionDef == null)
					continue;
				if(!ItemsetBuilder.fromDynamicOptionDef2Xform(doc,dynamicOptionDef,questionDef,formDef))
					orphanQuestions.add(questionDef);
			}
		}
		
		//Cleanse binds of bad attributs:
		cleanBindNodeRecurse(formDef);

		//If there are any itemsets which were not built completely due to their dependent
		//parent questions not having been parsed yet, build them now.
		if(orphanQuestions.size() > 0)
			ItemsetBuilder.updateDynamicOptions(dynamicOptions,orphanQuestions,formDef,doc);
	}
	
	/**
	 * A little helper function that you can throw all your removeAttribute statements in...
	 * @param bindNode
	 */
	private static void cleanBindNode(Element bindNode){
		bindNode.removeAttribute("action");
	}
	
	private static void cleanBindNodeRecurse(IFormElement parent){
		//first clean the parent
		Element bindNode = parent.getBindNode();
		if(bindNode != null){
			cleanBindNode(bindNode);
		}
		
		if(parent.getChildren() != null){
			//then recursively clean children
			for(int i=0;i<parent.getChildren().size();i++){
				IFormElement child = parent.getChildren().get(i);
				cleanBindNodeRecurse(child);
			}
		}
	}

	private static void addMetaData(Document doc, Element dataNode){
		Element metaNode =  doc.createElement("orx:meta");
		dataNode.appendChild(metaNode);
		
		Element node =  doc.createElement("orx:timeStart");
		metaNode.appendChild(node);
		
		node =  doc.createElement("orx:timeEnd");
		metaNode.appendChild(node);
		
		node =  doc.createElement("orx:instanceID");
		metaNode.appendChild(node);
		
//		node =  doc.createElement("orx:userID");
//		metaNode.appendChild(node);
//		
//		node =  doc.createElement("orx:deviceID");
//		metaNode.appendChild(node);
	}

	/**
	 * Converts a page definition object to xforms.
	 * 
	 * @param groupDef the page definition object.
	 * @param doc the xforms document.
	 * @param xformsNode the root node of the xforms document.
	 * @param formDef the form definition object to which the page belongs.
	 * @param formNode the xforms instance data node.
	 * @param modelNode the xforms model node.
	 */
	public static void fromGroupDef2Xform(GroupDef groupDef, Document doc, Element xformsNode, FormDef formDef, Element formNode, Element modelNode){

//		if(pageDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
//			UiElementBuilder.fromQuestionDef2Xform(pageDef,doc,xformsNode,formDef,formNode,modelNode,xformsNode);
//		else{
			//Create a group node
			Element groupNode =  doc.createElement(XformConstants.NODE_NAME_GROUP);
			Element labelNode =  doc.createElement(XformConstants.NODE_NAME_LABEL);
			Element hintNode = doc.createElement(XformConstants.NODE_NAME_HINT);
			Element bindNode = doc.createElement("bind");
			Element parentDataNode = groupDef.getParent().getDataNode();
			String nodesetPath = groupDef.getDataNodesetPath();
			String qtnID = FormUtil.getQtnIDFromNodeSetPath(nodesetPath);
			
			if(parentDataNode == null){
				parentDataNode = formDef.getDataNode();
			}
			Element dataNode = doc.createElement(qtnID);
			parentDataNode.appendChild(dataNode);
			
			groupDef.setDataNode(dataNode);
			
			boolean hasHintText = (groupDef.getHelpText()!= null && !groupDef.getHelpText().isEmpty() )||
									(Itext.getDefaultLocale().hasID(groupDef.getItextId()+";hint"));
			if(hasHintText){
				UiElementBuilder.addHelpItextRefs(hintNode, groupDef);
				groupDef.setHintNode(hintNode);
				groupNode.appendChild(hintNode);
			}
			
			labelNode.appendChild(doc.createTextNode(groupDef.getText()));
			UiElementBuilder.addItextRefs(labelNode, groupDef);
			groupNode.setAttribute("nodeset", groupDef.getDataNodesetPath());
			groupNode.appendChild(labelNode);
			xformsNode.appendChild(groupNode);
			
			if(groupDef.getDataType() == QuestionDef.QTN_TYPE_GROUP){
				groupDef.setLabelNode(labelNode);
				groupDef.setGroupNode(groupNode);
			}else{
				groupDef.getParent().setLabelNode(labelNode);
				groupDef.getParent().setControlNode(groupNode);
			}

			bindNode.setAttribute("id", qtnID);
			bindNode.setAttribute("nodeset", nodesetPath);
			modelNode.appendChild(bindNode);
			groupDef.setBindNode(bindNode);
			
			
			
			//Check if we have any questions in this page.
			List<IFormElement> questions = groupDef.getChildren();
			if(questions == null){
				return;
			}
			//Create ui nodes for each question.
			for(int i=0; i<questions.size(); i++){
				IFormElement qtn = questions.get(i);
				if(qtn instanceof QuestionDef){
					UiElementBuilder.fromQuestionDef2Xform((QuestionDef)qtn,doc,formDef,formNode,modelNode,groupNode);
				}else{
					fromGroupDef2Xform((GroupDef)qtn,doc,groupNode,formDef,formNode,modelNode);
				}
			}
//		}
	}
}
