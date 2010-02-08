package org.purc.purcforms.client.xforms;


/**
 * Constants used when manipulating xforms documents.
 * 
 * @author daniel
 *
 */
public class XformConstants {

	/** The xmlns prefix. */
	public static final String XML_NAMESPACE_PREFIX = "xmlns:";

	/** Namespace prefix for XForms. */
	public static final String PREFIX_XFORMS = "xf";

	public static final String PREFIX_XFORMS_AND_COLON = "xf:";

	/** Namespace prefix for XML schema. */
	public static final String PREFIX_XML_SCHEMA = "xsd";

	/** The second Namespace prefix for XML schema. */
	public static final String PREFIX_XML_SCHEMA2 = "xs";

	/** Namespace prefix for XML schema instance. */
	public static final String PREFIX_XML_INSTANCES = "xsi";

	/** Namespace for XForms. */
	public static final String NAMESPACE_XFORMS = "http://www.w3.org/2002/xforms";

	/** Namespace for XML schema. */
	public static final String NAMESPACE_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

	/** Namespace for XML schema instance. */
	public static final String NAMESPACE_XML_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";


	public static final String NODE_NAME_XFORMS = PREFIX_XFORMS_AND_COLON+"xforms";
	public static final String NODE_NAME_INSTANCE = PREFIX_XFORMS_AND_COLON+"instance";
	public static final String NODE_NAME_MODEL = PREFIX_XFORMS_AND_COLON+"model";
	public static final String NODE_NAME_BIND = PREFIX_XFORMS_AND_COLON+"bind";
	public static final String NODE_NAME_LABEL = PREFIX_XFORMS_AND_COLON+"label";
	public static final String NODE_NAME_HINT = PREFIX_XFORMS_AND_COLON+"hint";
	public static final String NODE_NAME_ITEM = PREFIX_XFORMS_AND_COLON+"item";
	public static final String NODE_NAME_ITEMSET = PREFIX_XFORMS_AND_COLON+"itemset";
	public static final String NODE_NAME_INPUT = PREFIX_XFORMS_AND_COLON+"input";
	public static final String NODE_NAME_SELECT = PREFIX_XFORMS_AND_COLON+"select";
	public static final String NODE_NAME_SELECT1 = PREFIX_XFORMS_AND_COLON+"select1";
	public static final String NODE_NAME_REPEAT = PREFIX_XFORMS_AND_COLON+"repeat";
	public static final String NODE_NAME_TRIGGER = PREFIX_XFORMS_AND_COLON+"trigger";
	public static final String NODE_NAME_SUBMIT = PREFIX_XFORMS_AND_COLON+"submit";
	public static final String NODE_NAME_VALUE = PREFIX_XFORMS_AND_COLON+"value";
	public static final String NODE_NAME_GROUP = PREFIX_XFORMS_AND_COLON+"group";

	public static final String NODE_NAME_XFORMS_MINUS_PREFIX = "xforms";
	public static final String NODE_NAME_INSTANCE_MINUS_PREFIX = "instance";
	public static final String NODE_NAME_MODEL_MINUS_PREFIX = "model";
	public static final String NODE_NAME_BIND_MINUS_PREFIX = "bind";
	public static final String NODE_NAME_LABEL_MINUS_PREFIX = "label";
	public static final String NODE_NAME_HINT_MINUS_PREFIX = "hint";
	public static final String NODE_NAME_ITEM_MINUS_PREFIX = "item";
	public static final String NODE_NAME_ITEMSET_MINUS_PREFIX = "itemset";
	public static final String NODE_NAME_INPUT_MINUS_PREFIX = "input";
	public static final String NODE_NAME_SELECT_MINUS_PREFIX = "select";
	public static final String NODE_NAME_SELECT1_MINUS_PREFIX = "select1";
	public static final String NODE_NAME_REPEAT_MINUS_PREFIX = "repeat";
	public static final String NODE_NAME_TRIGGER_MINUS_PREFIX = "trigger";
	public static final String NODE_NAME_SUBMIT_MINUS_PREFIX = "submit";
	public static final String NODE_NAME_VALUE_MINUS_PREFIX = "value";
	public static final String NODE_NAME_GROUP_MINUS_PREFIX = "group";

	public static final String NODE_NAME_TEXT = "text";
	public static final String ATTRIBUTE_NAME_XPATH = "xpath";
	public static final String ATTRIBUTE_NAME_VALUE = "value";

	public static final String ATTRIBUTE_NAME_ID = "id";
	public static final String ATTRIBUTE_NAME_BIND = "bind";
	public static final String ATTRIBUTE_NAME_REF = "ref";
	public static final String ATTRIBUTE_NAME_NODESET = "nodeset";
	public static final String ATTRIBUTE_NAME_LOCKED = "locked";
	public static final String ATTRIBUTE_NAME_VISIBLE = "visible";
	public static final String ATTRIBUTE_NAME_READONLY = "readonly";
	public static final String ATTRIBUTE_NAME_RELEVANT = "relevant";
	public static final String ATTRIBUTE_NAME_CONSTRAINT = "constraint";
	public static final String ATTRIBUTE_NAME_CONSTRAINT_MESSAGE = "message";
	public static final String ATTRIBUTE_NAME_REQUIRED = "required";
	public static final String ATTRIBUTE_NAME_CALCULATE = "calculate";
	public static final String ATTRIBUTE_NAME_TYPE = "type";
	public static final String ATTRIBUTE_NAME_NAME = "name";
	public static final String ATTRIBUTE_NAME_FORM_KEY = "formKey";
	public static final String ATTRIBUTE_NAME_XMLNS = "xmlns:"+PREFIX_XFORMS;
	public static final String ATTRIBUTE_NAME_DESCRIPTION_TEMPLATE = "description-template"; //eg ${/patient/family_name}$
	public static final String ATTRIBUTE_NAME_ACTION = "action";
	public static final String ATTRIBUTE_NAME_PARENT = "parent";
	public static final String ATTRIBUTE_NAME_FORMAT = "format";

	public static final String ATTRIBUTE_VALUE_ENABLE = "enable";
	public static final String ATTRIBUTE_VALUE_DISABLE = "disable";
	public static final String ATTRIBUTE_VALUE_SHOW = "show";
	public static final String ATTRIBUTE_VALUE_HIDE = "hide";
	public static final String ATTRIBUTE_VALUE_IMAGE = "image";
	public static final String ATTRIBUTE_VALUE_VIDEO = "video";
	public static final String ATTRIBUTE_VALUE_AUDIO = "audio";
	public static final String ATTRIBUTE_VALUE_GPS = "gps";

	public static final String DATA_TYPE_DATE = "xsd:date";
	public static final String DATA_TYPE_INT = "xsd:int";
	public static final String DATA_TYPE_TEXT = "xsd:string";
	public static final String DATA_TYPE_BOOLEAN = "xsd:boolean";
	public static final String DATA_TYPE_BINARY = "xsd:base64Binary";

	public static final String XPATH_VALUE_TRUE = "true()";
	public static final String XPATH_VALUE_FALSE = "false()";

	public static final String CONDITIONS_OPERATOR_TEXT_AND = " and ";
	public static final String CONDITIONS_OPERATOR_TEXT_OR = " or ";
}
