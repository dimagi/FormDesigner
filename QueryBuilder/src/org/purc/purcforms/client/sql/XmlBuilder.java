package org.purc.purcforms.client.sql;

import java.util.List;

import org.purc.purcforms.client.model.DisplayField;
import org.purc.purcforms.client.model.FilterCondition;
import org.purc.purcforms.client.model.FilterConditionGroup;
import org.purc.purcforms.client.model.FilterConditionRow;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.SortField;
import org.purc.purcforms.client.xforms.XformBuilderUtil;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;


/**
 * 
 * @author daniel
 *
 */
public class XmlBuilder {

	public static final String NODE_NAME_QUERYDEF = "QueryDef";

	public static final String NODE_NAME_FILTER_CONDITIONS = "FilterConditions";
	public static final String NODE_NAME_DISPLAY_FIELDS = "DisplayFields";
	public static final String NODE_NAME_SORT_FIELDS = "SortFields";
	public static final String NODE_NAME_FIELD = "Field";

	public static final String NODE_NAME_GROUP = "Group";
	public static final String NODE_NAME_CONDITION = "Condition";

	public static final String ATTRIBUTE_NAME_OPERATOR = "operator";
	public static final String ATTRIBUTE_NAME_FIELD = "field";
	public static final String ATTRIBUTE_NAME_VALUE = "value";

	public static final String ATTRIBUTE_NAME_NAME = "name";
	public static final String ATTRIBUTE_NAME_AGG_FUNC = "AggFunc";
	public static final String ATTRIBUTE_NAME_TEXT = "text";
	public static final String ATTRIBUTE_NAME_SORT_ORDER = "sortOrder";
	public static final String ATTRIBUTE_NAME_TYPE = "type";
	//public static final String ATTRIBUTE_NAME_TABLE = "table";

	private static Document doc;

	public static String buildXml(FormDef formDef, FilterConditionGroup filterConditionGroup, List<DisplayField> displayFields, List<SortField> sortFields){
		if(formDef == null || filterConditionGroup == null)
			return null;

		doc = XMLParser.createDocument();
		doc.appendChild(doc.createProcessingInstruction("xml", "version=\"1.0\" encoding=\"UTF-8\""));
		Element rootNode = doc.createElement(NODE_NAME_QUERYDEF);
		doc.appendChild(rootNode);

		Element filterNode = doc.createElement(NODE_NAME_FILTER_CONDITIONS);
		rootNode.appendChild(filterNode);

		Element groupNode = doc.createElement(NODE_NAME_GROUP);
		groupNode.setAttribute(ATTRIBUTE_NAME_OPERATOR, String.valueOf(filterConditionGroup.getConditionsOperator()));
		filterNode.appendChild(groupNode);

		buildFilter(groupNode,filterConditionGroup);

		if(!(displayFields == null || displayFields.size() == 0)){
			Element displayFieldsNode = doc.createElement(NODE_NAME_DISPLAY_FIELDS);
			//displayFieldsNode.setAttribute(ATTRIBUTE_NAME_TABLE, formDef.getVariableName());
			rootNode.appendChild(displayFieldsNode);
			buildDisplayFields(displayFieldsNode,displayFields);
		}

		if(!(sortFields == null || sortFields.size() == 0)){
			Element sortFieldsNode = doc.createElement(NODE_NAME_SORT_FIELDS);
			rootNode.appendChild(sortFieldsNode);
			buildSortFields(sortFieldsNode,sortFields);
		}

		return doc.toString();
	}

	public static void buildFilter(Element parentNode, FilterConditionGroup filterConditionGroup){
		List<FilterConditionRow> rows = filterConditionGroup.getConditions();
		for(FilterConditionRow row : rows){
			if(row instanceof FilterConditionGroup){
				Element node = doc.createElement(NODE_NAME_GROUP);
				node.setAttribute(ATTRIBUTE_NAME_OPERATOR, String.valueOf(((FilterConditionGroup)row).getConditionsOperator()));
				parentNode.appendChild(node);
				buildFilter(node,(FilterConditionGroup)row);
			}
			else
				buildFilter(parentNode,(FilterCondition)row);
		}
	}

	private static void buildFilter(Element parentNode, FilterCondition condition){	
		Element node = doc.createElement(NODE_NAME_CONDITION);
		parentNode.appendChild(node);

		node.setAttribute(ATTRIBUTE_NAME_FIELD, condition.getFieldName());
		node.setAttribute(ATTRIBUTE_NAME_OPERATOR, String.valueOf(condition.getOperator()));
		node.setAttribute(ATTRIBUTE_NAME_VALUE, condition.getFirstValue());
	}

	private static void buildDisplayFields(Element displayFieldsNode, List<DisplayField> displayFields){
		for(DisplayField field : displayFields){
			Element node = doc.createElement(NODE_NAME_FIELD);
			node.setAttribute(ATTRIBUTE_NAME_NAME, field.getName());
			node.setAttribute(ATTRIBUTE_NAME_TEXT, field.getText());
			node.setAttribute(ATTRIBUTE_NAME_TYPE, XformBuilderUtil.getXmlType(field.getDataType(),null));
			if(field.getAggFunc() != null)
				node.setAttribute(ATTRIBUTE_NAME_AGG_FUNC, field.getAggFunc());
			displayFieldsNode.appendChild(node);
		}
	}

	private static void buildSortFields(Element sortFieldsNode, List<SortField> sortFields){
		for(SortField field : sortFields){
			Element node = doc.createElement(NODE_NAME_FIELD);
			node.setAttribute(ATTRIBUTE_NAME_NAME, field.getName());
			node.setAttribute(ATTRIBUTE_NAME_SORT_ORDER, field.getSortOrder()+"");
			sortFieldsNode.appendChild(node);
		}
	}
}
