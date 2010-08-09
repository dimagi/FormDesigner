package org.purc.purcforms.client.model;

import java.io.Serializable;

/**
 * EpihandyConstants shared throughout classes in the containing package.
 *
 * @version ,
 */
public class ModelConstants implements Serializable{
	
	/**
	 * Generated serialization ID
	 */
	private static final long serialVersionUID = -830180521446083067L;

	/** Empty strig representation */
	public static final String EMPTY_STRING = "";
	
	/** Index for no selection */
	public static final int NO_SELECTION = -1;
	
	/** ID not set numeric value */
	public static final int NULL_ID = -1;
	
	/** Conditions perator not set. */
	public static final int CONDITIONS_OPERATOR_NULL = 0;
	
	/** Conditions operator AND */
	public static final int CONDITIONS_OPERATOR_AND = 1;
	
	/** Conditions Operator OR */
	public static final int CONDITIONS_OPERATOR_OR = 2;
	
	/** Operator not set numeric value */
	public static final int OPERATOR_NULL = 0;
	
	/** Operator Equal */
	public static final int OPERATOR_EQUAL = 1;
	
	/** Operator Not Equal */
	public static final int OPERATOR_NOT_EQUAL = 2;
	
	/** Operator Greater */
	public static final int OPERATOR_GREATER = 3;
	
	/** Operator Greater of Equal */
	public static final int OPERATOR_GREATER_EQUAL = 4;
	
	/** Operator Less */
	public static final int OPERATOR_LESS = 5;
	
	/** Operator Less than or equal to */
	public static final int OPERATOR_LESS_EQUAL = 6;
	
	/** Operator In List. */
	public static final int OPERATOR_IN_LIST = 7;
	
	/**Operator Not In List. */
	public static final int OPERATOR_NOT_IN_LIST = 8;
	
	/** Operator Is Null. */
	public static final int OPERATOR_IS_NULL = 9;
	
	/** Operator Between. */
	public static final int OPERATOR_BETWEEN = 10;
	
	/** Operator Not Between. */
	public static final int OPERATOR_NOT_BETWEEN = 11;
	
	/** Operator Contains. */
	public static final int OPERATOR_CONTAINS = 12;
	
	/** Operator Not Contain. */
	public static final int OPERATOR_NOT_CONTAIN = 13;
	
	/** Operator Starts With. */
	public static final int OPERATOR_STARTS_WITH = 14;
	
	/** Operator Not Starts With. */
	public static final int OPERATOR_NOT_START_WITH = 15;
	
	/** Operator Is Not Null. */
	public static final int OPERATOR_IS_NOT_NULL = 16;
	
	/** Operator Ends With. */
	public static final int OPERATOR_ENDS_WITH = 17;
	
	/** Operator Not End With. */
	public static final int OPERATOR_NOT_END_WITH = 18;
	
	/** The Value Function. */
	public static final int FUNCTION_VALUE = 1;
	
	/** The Length function. */
	public static final int FUNCTION_LENGTH = 2;
	
	/** No rule action specified */
	public static final int ACTION_NONE = 0;
	
	/** Rule action to hide questions */
	public static final int ACTION_HIDE = 1 << 1;
	
	/** Rule action to show questions */
	public static final int ACTION_SHOW = 1 << 2;
	
	/** Rule action to disable questions */
	public static final int ACTION_DISABLE = 1 << 3;
	
	/** Rule action to enable questions */
	public static final int ACTION_ENABLE = 1 << 4;
	
	/** Rule action to make a question mandatory */
	public static final int ACTION_MAKE_MANDATORY = 1 << 5;
	
	/** Rule action to make a question optional */
	public static final int ACTION_MAKE_OPTIONAL = 1 << 6;
}
