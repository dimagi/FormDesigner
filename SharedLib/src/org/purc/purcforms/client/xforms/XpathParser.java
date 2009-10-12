package org.purc.purcforms.client.xforms;

import java.util.Vector;


/**
 * Parses xpath expression in relevant and constraint attribute values
 * and get out the list of condition tokens.
 * 
 * @author daniel
 *
 */
public class XpathParser {

	/**
	 * All methods in this class are static and hence we expect no external
	 * Instantiation of this class.
	 */
	private XpathParser(){

	}
	
	
	/**
	 * Gets a list of xpath conditions as separated by AND or OR, in a given xpath expression.
	 * E.g if we have: constraint=". > 0 and . < 10", then the list will have
	 * ". > 0", ". < 10"
	 * 
	 * @param expression the xpath expression.
	 * @return the xpath condition list.
	 */
	public static Vector getConditionsOperatorTokens(String expression){
		//TODO For now we are only dealing with one AND or OR, for simplicity
		//If one mixes both in the same relevant statement, then we take the first.
		Vector list = new Vector();

		int pos = 0;
		do{
			pos = extractConditionsOperatorTokens(expression,pos,list);
		}while(pos > 0);

		return list;
	}
	
	
	/**
	 * Gets an xpath condition, starting at a given position in an xpath expression 
	 * and puts it in a list.
	 * 
	 * @param expression the xpath expression.
	 * @param startPos the position, in the expression, from which to start the condition search.
	 * @param list the list of xpath conditions.
	 * @return the position or index from which the next condition search should begin.
	 */
	private static int extractConditionsOperatorTokens(String expression,int startPos, Vector list){
		int pos,pos2,opSize = XformConstants.CONDITIONS_OPERATOR_TEXT_AND.length();

		pos = expression.toLowerCase().indexOf(XformConstants.CONDITIONS_OPERATOR_TEXT_AND,startPos);
		if(pos <0){
			pos = expression.toLowerCase().indexOf(XformConstants.CONDITIONS_OPERATOR_TEXT_OR,startPos);
			opSize = XformConstants.CONDITIONS_OPERATOR_TEXT_OR.length();
		}

		//AND may be the last token when we have starting ORs hence skipping them. 
		//eg (relevant="/data/question10=7 OR /data/question6=4    OR  /data/question8=1 AND /data/question1='daniel'")
		pos2 = expression.toLowerCase().indexOf(XformConstants.CONDITIONS_OPERATOR_TEXT_OR,startPos);
		if(pos2 > 0 && pos2 < pos){
			pos = pos2;
			opSize = XformConstants.CONDITIONS_OPERATOR_TEXT_OR.length();
		}

		if(pos < 0){
			//If we did not find any operator, then take the whole of the remaining
			//part of the expression as one condition.
			list.add(expression.substring(startPos).trim());
			opSize = 0;
		}
		else{
			//If we found an operator, then extract the condition before it.
			list.add(expression.substring(startPos,pos).trim());
		}

		return pos+opSize;
	}

}
