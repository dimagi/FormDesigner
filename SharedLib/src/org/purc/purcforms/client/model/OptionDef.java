package org.purc.purcforms.client.model;

import java.io.Serializable;

import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.xforms.UiElementBuilder;
import org.purc.purcforms.client.xforms.XformConstants;
import org.purc.purcforms.client.xforms.XmlUtil;

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
	
	/**
	 * Generated serialization ID
	 */
	private static final long serialVersionUID = 6011207697283921703L;

	/** The numeric unique identifier of an answer option. */
	private int id = ModelConstants.NULL_ID;
	
	/** The display text of the answer option. */
	private String text = ModelConstants.EMPTY_STRING;
	
	//TODO May not need to serialize this property for smaller pay load. Then we would just rely on the id.
	/** The unique text identifier of an answer option. */
	private String variableName = ModelConstants.EMPTY_STRING;
	
	public static final char SEPARATOR_CHAR = ',';
	
	/** The xforms label node for this option. */
	private Element labelNode;
	
	/** The xforms value node for this option. */
	private Element valueNode;
	
	/** The xforms select or select1 node that this option belongs to. */
	private Element controlNode;
	
	/** The question to which this option belongs. */
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
	
	/**
	 * Updates the xforms xml document with the changes in the option.
	 * 
	 * @param doc the xml document.
	 * @param selectNode the select or select1 node that this option belongs to.
	 */
    public void updateDoc(Document doc, Element selectNode){
    	if(labelNode != null)
    		XmlUtil.setTextNodeValue(labelNode,text);
    	
    	if(valueNode != null)
    		XmlUtil.setTextNodeValue(valueNode,variableName);
    	
    	if(labelNode == null && valueNode == null) ////Must be new option.
    		UiElementBuilder.fromOptionDef2Xform(this,doc,selectNode);
    	
    	if(controlNode != null)
    		controlNode.setAttribute(XformConstants.ATTRIBUTE_NAME_ID, variableName);
	}
    
    /**
     * Builds the language xpath node for locatization of text for this option.
     * 
     * @param parentXpath the parent xpath expression.
     * @param doc the locale document.
     * @param parentNode
     */
    public void buildLanguageNodes(String parentXpath, com.google.gwt.xml.client.Document doc, Element parentNode){
    	if(labelNode != null && controlNode != null){
    		String xpath = parentXpath + "/" + FormUtil.getNodeName(controlNode);
    		
    		String id = controlNode.getAttribute(XformConstants.ATTRIBUTE_NAME_ID);
    		
    		if(id != null && id.trim().length() > 0)
    			xpath += "[@" + XformConstants.ATTRIBUTE_NAME_ID + "='" + id + "']";
    		
    		/*String parent = controlNode.getAttribute(XformConstants.ATTRIBUTE_NAME_PARENT);
    		if(parent != null && parent.trim().length() > 0)
    			xpath += "[@" + XformConstants.ATTRIBUTE_NAME_PARENT + "='" + parent + "']";*/
    		
    		xpath += "/"+ FormUtil.getNodeName(labelNode);
			
    		Element node = doc.createElement(XformConstants.NODE_NAME_TEXT);
			node.setAttribute(XformConstants.ATTRIBUTE_NAME_XPATH, xpath);
			node.setAttribute(XformConstants.ATTRIBUTE_NAME_VALUE, text);
			parentNode.appendChild(node);
		}
    }
}
