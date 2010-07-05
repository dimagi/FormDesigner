package org.openrosa.client.model;

import java.io.Serializable;
import java.util.List;

import org.openrosa.client.OpenRosaConstants;
import org.openrosa.client.util.ItextParser;
import org.openrosa.client.xforms.UiElementBuilder;
import org.purc.purcforms.client.util.FormUtil;
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
public class OptionDef implements IFormElement, Serializable {
	
	/** The numeric unique identifier of an answer option. */
	private int id = ModelConstants.NULL_ID;
	
	/** The display text of the answer option. */
	private String text = ModelConstants.EMPTY_STRING;
	
	//TODO May not need to serialize this property for smaller pay load. Then we would just rely on the id.
	/** The unique text identifier of an answer option. */
	private String binding = ModelConstants.EMPTY_STRING;
	
	public static final char SEPARATOR_CHAR = ',';
	
	/** The xforms label node for this option. */
	private Element labelNode;
	
	/** The xforms value node for this option. */
	private Element valueNode;
	
	/** The xforms select or select1 node that this option belongs to. */
	private Element controlNode;
	
	/** The question to which this option belongs. */
	private IFormElement parent;

	private String itextId;
	
	
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
		 setBinding(optionDef.getBinding());
		 setItextId(optionDef.getItextId());
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
		setBinding(variableName);
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
	
	public String getBinding() {
		return binding;
	}
	
	public void setBinding(String variableName) {
		this.binding = variableName;
	}

	public IFormElement getParent() {
		return parent;
	}

	public void setParent(IFormElement parent) {
		this.parent = parent;
	}

	public String getItextId() {
		return itextId;
	}

	public void setItextId(String itextId) {
		this.itextId = itextId;
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
		
		if(itextId == null)
			setItextId(ItextParser.getItextId(labelNode));
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
    		XmlUtil.setTextNodeValue(valueNode,binding);
    	
    	if(labelNode == null && valueNode == null) ////Must be new option.
    		UiElementBuilder.fromOptionDef2Xform(this,doc,selectNode);
    	
    	if(controlNode != null)
    		controlNode.setAttribute(XformConstants.ATTRIBUTE_NAME_ID, binding);
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
			node.setAttribute(XformConstants.ATTRIBUTE_NAME_ID, itextId);
			node.setAttribute(OpenRosaConstants.ATTRIBUTE_NAME_UNIQUE_ID, "OptionDef"+id);
			parentNode.appendChild(node);
		}
    }
    
    public int getDataType(){
    	return QuestionDef.QTN_TYPE_NULL;
    }
    
    public void setDataType(int dataType){
    	
    }
	
    public List<IFormElement> getChildren(){
    	return null;
    }
    
    public void setChildren(List<IFormElement> children){
    	
    }
	
	public Element getBindNode(){
		return null;
	}
	
	public void setBindNode(Element bindNode){
		
	}
	
	public Element getDataNode(){
		return null;
	}
	
	public void setDataNode(Element dataNode){
		
	}
	
	public void refresh(IFormElement element){
		
	}
	
	public void updateDoc(Document doc, Element xformsNode, IFormElement formDef, Element formNode, Element modelNode, boolean withData, String orgFormVarName){
		
	}
	
	public void updateDataNodes(Element parentDataNode){
		
	}
	
	public IFormElement copy(IFormElement parent){
		return new OptionDef(this, (QuestionDef)parent);
	}
	
	public void clearChangeListeners(){
		
	}
	
	public String getDisplayText(){
		return text;
	}
	
	public void addChild(IFormElement element){
		
	}
	
	public String getHelpText(){
		return null;
	}
	
	public void setHelpText(String helpText){
		
	}
	
	public Element getHintNode(){
		return null;
	}
	
	public void setHintNode(Element hintNode){
		
	}
	
	public boolean removeChild(IFormElement element){
		return false;
	}
	
	public int getChildCount(){
		return 0;
	}
	

	public FormDef getFormDef(){
		IFormElement element = getParent();
		if(parent instanceof FormDef)
			return (FormDef)element;
		
		return element.getFormDef();
	}
	
	
	public boolean isLocked(){
		return false;
	}
	
	public boolean isRequired(){
		return false;
	}
	
	public boolean isEnabled(){
		return true;
	}
	
	public boolean isVisible(){
		return true;
	}
	
	public String getDefaultValue(){
		return null;
	}
}
