package org.purc.purcforms.client.sql;

import java.util.List;

import org.purc.purcforms.client.model.Condition;
import org.purc.purcforms.client.model.FilterConditionRow;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.ModelConstants;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.model.SkipRule;
import org.purc.purcforms.client.widget.GroupHyperlink;


/**
 * 
 * @author daniel
 *
 */
public class SqlBuilder {

	private static String DATE_SEPARATOR = "'";
	private static  String LIKE_SEPARATOR = "%";

	private static FormDef formDef;

	
	public static String buildSql(FormDef formDef, List<FilterConditionRow> rows){
		if(formDef == null || rows == null)
			return null;
		
		String sql = "SELECT * FROM " + formDef.getVariableName();
		
		return sql;
	}

	public static String buildSql2(FormDef formDef, List<FilterConditionRow> rows){
		

		SqlBuilder.formDef = formDef;

		String sql = "SELECT * FROM " + formDef.getVariableName();
		String filter = null,fieldName;

		String operator = getConditionsOperator();
		/*for(int index = 0; index < skipRule.getConditionCount(); index++){
			Condition condition = skipRule.getConditionAt(index);

			QuestionDef questionDef = formDef.getQuestion(condition.getQuestionId());
			if(questionDef == null)
				continue;

			if(filter == null)
				filter = " WHERE ";
			else
				filter += " " + operator + " ";

			fieldName  = getFieldName(questionDef);
			//operator  = GetRowOperator(row);
		}*/

		if(filter != null)
			sql = sql + filter;

		return sql;
	}

	private static String getConditionsOperator(){
		/*if(skipRule.getConditionsOperator() == ModelConstants.CONDITIONS_OPERATOR_AND)
			return "AND";
		else*/
			return "OR";
	}

	private static String getFilter(Condition condition){
		condition.getOperator();
		return null;
	}

	private static String GetDBOperator(int operator)
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

	private static String getFieldName(QuestionDef questionDef){
		int index = questionDef.getVariableName().lastIndexOf('/');
		if(index > -1)
			return questionDef.getVariableName().substring(index+1);
		return questionDef.getVariableName();
	}

	private static String GetQuotedValue(String fieldVal,int dataType, int operator)
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

	private static String GetSQLInnerCombiner(String val)
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

	private static String GetSQLOuterCombiner(String val)
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
