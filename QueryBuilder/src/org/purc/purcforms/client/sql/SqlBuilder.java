package org.purc.purcforms.client.sql;

import java.util.List;

import org.purc.purcforms.client.model.Condition;
import org.purc.purcforms.client.model.FilterCondition;
import org.purc.purcforms.client.model.FilterConditionGroup;
import org.purc.purcforms.client.model.FilterConditionRow;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.ModelConstants;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.widget.GroupHyperlink;


/**
 * 
 * @author daniel
 *
 */
public class SqlBuilder {

	private static String DATE_SEPARATOR = "'";
	private static  String LIKE_SEPARATOR = "%";

	public static String buildSql(FormDef formDef, FilterConditionGroup filterConditionGroup){
		if(formDef == null || filterConditionGroup == null)
			return null;
		
		String sql = "SELECT * FROM " + formDef.getVariableName();
		
		String filter = null;
		if(filterConditionGroup.getConditionCount() > 0)
			filter = getFilter(filterConditionGroup);
		
		if(filter != null)
			sql = sql + " WHERE " + filter;
		
		return sql;
	}
	
	private static String getFilter(FilterConditionGroup filterGroup){
		String filter = "";
		
		FilterConditionRow prevRow = null;
		List<FilterConditionRow> rows = filterGroup.getConditions();
		for(FilterConditionRow row : rows){

			if(row instanceof FilterConditionGroup){	
				if(filter.length() > 0 && prevRow instanceof FilterCondition)
					filter = getSQLOuterCombiner(filterGroup.getConditionsOperator()) + "(" + filter + ")";
				
				if(filter.length() > 0)
					filter += getSQLInnerCombiner(filterGroup.getConditionsOperator());
				
				String curFilter = getFilter((FilterConditionGroup)row);
				filter += curFilter;
			}
			else if(row instanceof FilterCondition){
				if(filter.length() > 0)
					filter += getSQLInnerCombiner(filterGroup.getConditionsOperator());
				
				FilterCondition condition = (FilterCondition)row;
				filter += condition.getFieldName();
				filter += getDBOperator(condition.getOperator());
				filter += getQuotedValue(condition.getFirstValue(),condition.getDataType(),condition.getOperator());
			}
			
			prevRow = row;
		}
		
		if(filter.length() > 0 && prevRow instanceof FilterCondition)
			filter = getSQLOuterCombiner(filterGroup.getConditionsOperator()) + "(" + filter + ")";
			
		return filter;
	}

	private static String getDBOperator(int operator)
	{
		switch(operator)
		{
		case ModelConstants.OPERATOR_EQUAL:
			return " = ";
		case ModelConstants.OPERATOR_NOT_EQUAL:
			return " <> ";
		case ModelConstants.OPERATOR_LESS:
			return " < ";
		case ModelConstants.OPERATOR_LESS_EQUAL:
			return " <= ";
		case ModelConstants.OPERATOR_GREATER:
			return " > ";
		case ModelConstants.OPERATOR_GREATER_EQUAL:
			return " >= ";
		case ModelConstants.OPERATOR_IS_NULL:
			return " IS NULL ";
		case ModelConstants.OPERATOR_IS_NOT_NULL:
			return " IS NOT NULL ";
		case ModelConstants.OPERATOR_IN_LIST:
			return " IN (";
		case ModelConstants.OPERATOR_NOT_IN_LIST:
			return " NOT IN (";
		case ModelConstants.OPERATOR_STARTS_WITH:
			return " LIKE ";
		case ModelConstants.OPERATOR_NOT_START_WITH:
			return " NOT LIKE ";
		case ModelConstants.OPERATOR_CONTAINS:
			return " LIKE ";
		case ModelConstants.OPERATOR_NOT_CONTAIN:
			return " NOT LIKE ";
		case ModelConstants.OPERATOR_BETWEEN:
			return " BETWEEN ";
		case ModelConstants.OPERATOR_NOT_BETWEEN:
			return " NOT BETWEEN ";
		case ModelConstants.OPERATOR_ENDS_WITH:
			return " LIKE ";
		case ModelConstants.OPERATOR_NOT_END_WITH:
			return " NOT LIKE ";
		}

		return null;
	}

	private static String getQuotedValue(String fieldVal,int dataType, int operator)
	{
		switch(dataType)
		{
		case QuestionDef.QTN_TYPE_TEXT:
		{
			if(operator == ModelConstants.OPERATOR_STARTS_WITH || operator == ModelConstants.OPERATOR_NOT_START_WITH)
				return "'" + fieldVal + LIKE_SEPARATOR + "'";
			if(operator == ModelConstants.OPERATOR_NOT_END_WITH || operator == ModelConstants.OPERATOR_NOT_END_WITH)
				return "'" + LIKE_SEPARATOR + fieldVal + "'";
			if(operator == ModelConstants.OPERATOR_CONTAINS || operator == ModelConstants.OPERATOR_NOT_CONTAIN)
				return "'" + LIKE_SEPARATOR + fieldVal + LIKE_SEPARATOR  + "'";
			else
				return "'" + fieldVal + "'";
		}
		case QuestionDef.QTN_TYPE_DATE:
			return DATE_SEPARATOR + fieldVal + DATE_SEPARATOR;
		default:
			return " " + fieldVal + " ";
		}
	}

	private static String getSQLInnerCombiner(String val)
	{
		if(val.equalsIgnoreCase(GroupHyperlink.CONDITIONS_OPERATOR_TEXT_ALL))
			return " AND ";
		else if(val.equalsIgnoreCase(GroupHyperlink.CONDITIONS_OPERATOR_TEXT_ANY))
			return " OR ";
		else if(val.equalsIgnoreCase(GroupHyperlink.CONDITIONS_OPERATOR_TEXT_NONE))
			return " OR ";
		else if(val.equalsIgnoreCase(GroupHyperlink.CONDITIONS_OPERATOR_TEXT_NOT_ALL))
			return " AND ";

		return null;
	}

	private static String getSQLOuterCombiner(String val)
	{
		if(val.equalsIgnoreCase(GroupHyperlink.CONDITIONS_OPERATOR_TEXT_ALL))
			return " ";
		else if(val.equalsIgnoreCase(GroupHyperlink.CONDITIONS_OPERATOR_TEXT_ANY))
			return " ";
		else if(val.equalsIgnoreCase(GroupHyperlink.CONDITIONS_OPERATOR_TEXT_NONE))
			return " NOT ";
		else if(val.equalsIgnoreCase(GroupHyperlink.CONDITIONS_OPERATOR_TEXT_NOT_ALL))
			return " NOT ";

		return null;
	}
}
