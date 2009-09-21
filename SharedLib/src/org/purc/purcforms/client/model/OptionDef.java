package org.purc.purcforms.client.model;

import java.io.Serializable;

import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.xforms.XformConverter;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;


/** 
 * Definition of an answer option or one of the possible answers of a question
 * with a given set of allowed answers..
 * 
 * @author Daniel Kayiwa
 *
 */
public class OptionDef implements Serializable {
	/** The numeric unique identifier of an answer option. */
	private int id = ModelConstants.NULL_ID;
	
	/** The display text of the answer option. */
	private String text = ModelConstants.EMPTY_STRING;
	
	//TODO May not need to serialize this property for smaller pay load. Then we would just rely on the id.
	/** The unique text ientifier of an answer option. */
	private String variableName = ModelConstants.EMPTY_STRING;
	
	public static final char SEPARATOR_CHAR = ',';
	
	private Element labelNode;
	private Element valueNode;
	private Element controlNode;
	
	private QuestionDef parent;

	/** Constructs the answer option definition object where
	 * initialization parameters are not supplied. */
	public OptionDef(QuestionDef parent) {  
		this.parent = parent;
	}
	
	/** The copy constructor  */
	public OptionDef(OptionDef optionDef,QuestionDef parent) { 
		 this(parent);
		 setId(optionDef.getId());
		 setText(optionDef.getText());
		 setVariableName(optionDef.getVariableName());
		 //setParent(parent /*optionDef.getParent()*/);
	}
	
	/** Constructs a new option answer definition object from the following parameters.
	 * 
	 * @param id
	 * @param text
	 * @param variableName
	 */
	public OptionDef(int id,String text, String variableName,QuestionDef parent) {
		this(parent);
		setId(id);
		setText(text);
		setVariableName(variableName);
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public String getVariableName() {
		return variableName;
	}
	
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public QuestionDef getParent() {
		return parent;
	}

	public void setParent(QuestionDef parent) {
		this.parent = parent;
	}

	/**
	 * @return the labelNode
	 */
	public Element getLabelNode() {
		return labelNode;
	}

	/**
	 * @param labelNode the labelNode to set
	 */
	public void setLabelNode(Element labelNode) {
		this.labelNode = labelNode;
	}

	/**
	 * @return the valueNode
	 */
	public Element getValueNode() {
		return valueNode;
	}

	/**
	 * @param valueNode the valueNode to set
	 */
	public void setValueNode(Element valueNode) {
		this.valueNode = valueNode;
	}

	/**
	 * @return the controlNode
	 */
	public Element getControlNode() {
		return controlNode;
	}

	/**
	 * @param controlNode the controlNode to set
	 */
	public void setControlNode(Element controlNode) {
		this.controlNode = controlNode;
	}

	public String toString() {
		return getText();
	}
	
    public void updateDoc(Document doc, Element selectNode){
    	if(labelNode != null)
    		XformConverter.setTextNodeValue(labelNode,text);
    	
    	if(valueNode != null)
    		XformConverter.setTextNodeValue(valueNode,variableName);
    	
    	if(labelNode == null && valueNode == null) ////Must be new option.
    		XformConverter.fromOptionDef2Xform(this,doc,selectNode);
    	
    	if(controlNode != null)
    		controlNode.setAttribute(XformConverter.ATTRIBUTE_NAME_ID, variableName);
	}
    
    public void buildLanguageNodes(String parentXpath, com.google.gwt.xml.client.Document doc, Element parentNode){
    	if(labelNode != null && controlNode != null){
    		String xpath = parentXpath + "/" + FormUtil.getNodeName(controlNode);
    		
    		String id = controlNode.getAttribute(XformConverter.ATTRIBUTE_NAME_ID);
    		if(id != null && id.trim().length() > 0)
    			xpath += "[@" + XformConverter.ATTRIBUTE_NAME_ID + "='" + id + "']";
    		
    		/*String parent = controlNode.getAttribute(XformConverter.ATTRIBUTE_NAME_PARENT);
    		if(parent != null && parent.trim().length() > 0)
    			xpath += "[@" + XformConverter.ATTRIBUTE_NAME_PARENT + "='" + parent + "']";*/
    		
    		xpath += "/"+ FormUtil.getNodeName(labelNode);
			
    		Element node = doc.createElement(XformConverter.NODE_NAME_TEXT);
			node.setAttribute(XformConverter.ATTRIBUTE_NAME_XPATH, xpath);
			node.setAttribute(XformConverter.ATTRIBUTE_NAME_VALUE, text);
			parentNode.appendChild(node);
		}
    }
}
