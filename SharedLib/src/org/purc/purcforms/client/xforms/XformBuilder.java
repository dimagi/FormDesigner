package org.purc.purcforms.client.xforms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;

import org.purc.purcforms.client.model.Calculation;
import org.purc.purcforms.client.model.DynamicOptionDef;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.PageDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.model.SkipRule;
import org.purc.purcforms.client.model.ValidationRule;

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
		xformsNode.setAttribute(XformConstants.XML_NAMESPACE_PREFIX+XformConstants.PREFIX_XFORMS, XformConstants.NAMESPACE_XFORMS);
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
		instanceNode.setAttribute(XformConstants.ATTRIBUTE_NAME_ID, formDef.getVariableName());
		modelNode.appendChild(instanceNode);
		formDef.setModelNode(modelNode);

		//Create the form data node and add it to the instance node.
		Element formNode =  doc.createElement(formDef.getVariableName());
		formNode.setAttribute(XformConstants.ATTRIBUTE_NAME_NAME, formDef.getName());
		formNode.setAttribute(XformConstants.ATTRIBUTE_NAME_ID, String.valueOf(formDef.getId()));
		formNode.setAttribute(XformConstants.ATTRIBUTE_NAME_FORM_KEY, String.valueOf(formDef.getFormKey()));
		instanceNode.appendChild(formNode);
		formDef.setDataNode(formNode);

		if(formDef.getDescriptionTemplate() != null && formDef.getDescriptionTemplate().trim().length() > 0)
			formNode.setAttribute(XformConstants.ATTRIBUTE_NAME_DESCRIPTION_TEMPLATE, formDef.getDescriptionTemplate());

		//Check if we have any pages.
		if(formDef.getPages() == null)
			return;

		//Build the ui nodes for all questions in each page.
		for(int pageNo=0; pageNo<formDef.getPages().size(); pageNo++){
			PageDef pageDef = (PageDef)formDef.getPages().elementAt(pageNo);
			fromPageDef2Xform(pageDef,doc,parentNode,formDef,formNode,modelNode);
		}

		//Build relevant s for the skip rules.
		Vector rules = formDef.getSkipRules();
		if(rules != null){
			for(int i=0; i<rules.size(); i++)
				RelevantBuilder.fromSkipRule2Xform((SkipRule)rules.elementAt(i),formDef);
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
			QuestionDef questionDef = formDef.getQuestion(calculation.getQuestionId());
			if(questionDef == null)
				continue;
			Element node = questionDef.getBindNode() != null ? questionDef.getBindNode() : questionDef.getControlNode();
			if(node != null)
				node.setAttribute(XformConstants.ATTRIBUTE_NAME_CALCULATE, calculation.getCalculateExpression());
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

		//If there are any itemsets which were not built completely due to their dependent
		//parent questions not having been parsed yet, build them now.
		if(orphanQuestions.size() > 0)
			ItemsetBuilder.updateDynamicOptions(dynamicOptions,orphanQuestions,formDef,doc);
	}


	/**
	 * Converts a page definition object to xforms.
	 * 
	 * @param pageDef the page definition object.
	 * @param doc the xforms document.
	 * @param xformsNode the root node of the xforms document.
	 * @param formDef the form definition object to which the page belongs.
	 * @param formNode the xforms instance data node.
	 * @param modelNode the xforms model node.
	 */
	public static void fromPageDef2Xform(PageDef pageDef, Document doc, Element xformsNode, FormDef formDef, Element formNode, Element modelNode){

		//Create a group node
		Element groupNode =  doc.createElement(XformConstants.NODE_NAME_GROUP);
		Element labelNode =  doc.createElement(XformConstants.NODE_NAME_LABEL);
		labelNode.appendChild(doc.createTextNode(pageDef.getName()));
		groupNode.appendChild(labelNode);
		xformsNode.appendChild(groupNode);
		pageDef.setLabelNode(labelNode);
		pageDef.setGroupNode(groupNode);

		//Set the identifier of the group node to be used for localisation.
		groupNode.setAttribute(XformConstants.ATTRIBUTE_NAME_ID, pageDef.getPageNo()+"");
		
		//Check if we have any questions in this page.
		Vector questions = pageDef.getQuestions();
		if(questions == null)
			return;

		//Create ui nodes for each question.
		for(int i=0; i<questions.size(); i++){
			QuestionDef qtn = (QuestionDef)questions.elementAt(i);
			UiElementBuilder.fromQuestionDef2Xform(qtn,doc,xformsNode,formDef,formNode,modelNode,groupNode);
		}
	}
}
