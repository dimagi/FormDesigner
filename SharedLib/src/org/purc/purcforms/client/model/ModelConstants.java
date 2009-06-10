package org.purc.purcforms.client.model;

import java.io.Serializable;

/**
 * EpihandyConstants shared throught classes in the containing package.
 *
 * @version ,
 */
public class ModelConstants implements Serializable{
	
	//Bit flags for some optimizations (e.g data compression.)
	public static final int BIT_FLAG1 = 1 << 0;  //  1 
	public static final int BIT_FLAG2 = 1 << 1;  //  2 
	public static final int BIT_FLAG3 = 1 << 2;  //  4 
	public static final int BIT_FLAG4 = 1 << 3;  //  8 
	public static final int BIT_FLAG5 = 1 << 4;  // 16 
	public static final int BIT_FLAG6 = 1 << 5;  // 32
	public static final int BIT_FLAG7 = 1 << 6;  // 64 
	
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
	
	/** Operator Less than */
	public static final int OPERATOR_LESS_EQUAL = 6;
	
	public static final int OPERATOR_IN_LIST = 7;
	
	public static final int OPERATOR_NOT_IN_LIST = 8;
	
	public static final int OPERATOR_IS_NULL = 9;
	
	public static final int OPERATOR_BETWEEN = 10;
	
	public static final int OPERATOR_NOT_BETWEEN = 11;
	
	public static final int OPERATOR_CONTAINS = 12;
	
	public static final int OPERATOR_NOT_CONTAIN = 13;
	
	public static final int OPERATOR_STARTS_WITH = 14;
	
	public static final int OPERATOR_NOT_START_WITH = 15;
	
	public static final int OPERATOR_IS_NOT_NULL = 16;
	
	public static final int OPERATOR_ENDS_WITH = 17;
	
	public static final int OPERATOR_NOT_END_WITH = 18;
	
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
	
	public static final String NULLS_NOT_ALLOWED = "Nulls not allowed. Use empty string";
	
	/** The maximum number of characters for text input. */
	public static final int MAX_NUM_CHARS = 500;
	
	/** The default study id for those that dont deal with studies, they just have forms. */
	public static final int DEFAULT_STUDY_ID = 1;
}
