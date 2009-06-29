package org.purc.purcforms.client.sql;

import java.util.List;

import org.purc.purcforms.client.model.FilterCondition;
import org.purc.purcforms.client.model.FilterConditionGroup;
import org.purc.purcforms.client.model.FilterConditionRow;
import org.purc.purcforms.client.model.FormDef;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;


/**
 * 
 * @author daniel
 *
 */
public class XmlBuilder {

	public static final String NODE_NAME_QUERYDEF = "querydef";
	public static final String NODE_NAME_GROUP = "group";
	public static final String NODE_NAME_CONDITION = "condition";
	
	public static final String ATTRIBUTE_NAME_OPERATOR = "operator";
	public static final String ATTRIBUTE_NAME_FIELD = "field";
	public static final String ATTRIBUTE_NAME_VALUE = "value";
	
	private static Document doc;
	
	public static String buildXml(FormDef formDef, FilterConditionGroup filterConditionGroup){
		if(formDef == null || filterConditionGroup == null)
			return null;
		
		doc = XMLParser.createDocument();
		Element rootNode = doc.createElement(NODE_NAME_QUERYDEF);
		doc.appendChild(rootNode);
		
		Element groupNode = doc.createElement(NODE_NAME_GROUP);
		groupNode.setAttribute(ATTRIBUTE_NAME_OPERATOR, String.valueOf(filterConditionGroup.getConditionsOperator()));
		rootNode.appendChild(groupNode);
		
		buildXml(groupNode,filterConditionGroup);
		
		return doc.toString();
	}
	
	public static void buildXml(Element parentNode, FilterConditionGroup filterConditionGroup){
		List<FilterConditionRow> rows = filterConditionGroup.getConditions();
		for(FilterConditionRow row : rows){
			if(row instanceof FilterConditionGroup){
				Element node = doc.createElement(NODE_NAME_GROUP);
				node.setAttribute(ATTRIBUTE_NAME_OPERATOR, String.valueOf(((FilterConditionGroup)row).getConditionsOperator()));
				parentNode.appendChild(node);
				buildXml(node,(FilterConditionGroup)row);
			}
			else
				buildXml(parentNode,(FilterCondition)row);
		}
	}
	
	private static void buildXml(Element parentNode, FilterCondition condition){	
		Element node = doc.createElement(NODE_NAME_CONDITION);
		parentNode.appendChild(node);
		
		node.setAttribute(ATTRIBUTE_NAME_FIELD, condition.getFieldName());
		node.setAttribute(ATTRIBUTE_NAME_OPERATOR, String.valueOf(condition.getOperator()));
		node.setAttribute(ATTRIBUTE_NAME_VALUE, condition.getFirstValue());
	}
}
