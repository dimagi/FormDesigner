package org.openrosa.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openrosa.client.OpenRosaConstants;
import org.openrosa.client.util.Itext;
import org.openrosa.client.util.ItextParser;
import org.openrosa.client.xforms.UiElementBuilder;
import org.openrosa.client.util.FormUtil;
import org.openrosa.client.xforms.XformConstants;
import org.openrosa.client.xforms.XmlUtil;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;


/** 
 * Definition of an answer option or one of the possible answers of a question
 * with a given set of allowed answers..
 * 
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
	
	/** The internal value of this OptionDef element */
	private String defaultValue;
	
	
	/** Constructs the answer option definition object where
	 * initialization parameters are not supplied. */
	public OptionDef(QuestionDef parent) {  
		this.parent = parent;
	}
	
	public List<String> getAllChildrenItextIDs(){
		return new ArrayList<String>(); //This method does not apply to OptionDefs (they can never have children)
	}
	
	
	/** The copy constructor  */
	public OptionDef(OptionDef optionDef,QuestionDef parent) { 
		 this(parent);
		 setId(optionDef.getId());
		 setText(optionDef.getText());
		 setQuestionID(optionDef.getQuestionID());
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
		setQuestionID(variableName);
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
	
	public String getQuestionID() {
		return binding;
	}
	
	public void setQuestionID(String variableName) {
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
	 * Gets the form to which this question belongs.
	 * 
	 * @return the form.
	 */
	public FormDef getParentFormDef(){
		return (FormDef)getParentFormDef(this);
	}

	private IFormElement getParentFormDef(IFormElement questionDef){
		IFormElement parent = questionDef.getParent();
		if(parent instanceof FormDef)
			return parent;
		return getParentFormDef(parent);
	}

	/**
	 * @param labelNode the labelNode to set
	 */
	public void setLabelNode(Element labelNode) {
		this.labelNode = labelNode;
		
		if(itextId == null)
			setItextId(XmlUtil.getItextId(labelNode));
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
    	if(labelNode != null){
    		XmlUtil.setTextNodeValue(labelNode,getText());
    		UiElementBuilder.addItextRefs(labelNode, this);
    	}
    	
    	if(valueNode != null){
    		XmlUtil.setTextNodeValue(valueNode,getDefaultValue());
    	}
    	
    	if(labelNode == null && valueNode == null){ ////Must be new option.
    		UiElementBuilder.fromOptionDef2Xform(this,doc,selectNode);
    	}
    	if(controlNode != null){
//    		controlNode.setAttribute(XformConstants.ATTRIBUTE_NAME_ID, binding);
    	}
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
    	return QuestionDef.QTN_TYPE_OPTION_ITEM;
    }
    
    public IFormElement getElement(String varName){
    	//Method does not apply to Options.
    	return null;
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
		throw new RuntimeException("Error in OptionDef in updateDoc, unrecognized method!");
	}
	
	public void updateDataNodes(Element parentDataNode){
		
	}
	
	public IFormElement copy(IFormElement parent){
		return new OptionDef(this, (QuestionDef)parent);
	}
	
	public void clearChangeListeners(){
		
	}
	
	public String getDisplayText(){
		return getText();
	}
	
	public void addChild(IFormElement element){
		throw new RuntimeException("Can't add a child to an OptionDef!");
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
	
	public String getDefaultValue(){
		return defaultValue;
	}
	
	public void setDefaultValue(String defaultValue){
		this.defaultValue = defaultValue;
	}

	@Override
	public void setEnabled(boolean enabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLocked(boolean locked) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRequired(boolean required) {
		// TODO Auto-generated method stub
		
	}

	
	/**
	 * Get the Nodeset ref that points to the data node where the question's answer will be stored.
	 * @return
	 */
	public String getDataNodesetPath(){
		//Not Valid for an OptionDef
		return "No Nodeset Path for Option elements!";
		
	}

	@Override
	public boolean hasUINode() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void setHasUINode(boolean hasUINode) {
		return; //OptionDef should always have a UINode.
		
	}

	@Override
	public void moveChildToIndex(IFormElement child, int index)
			throws Exception {
		throw new Exception("OptionDefs cannot have children!");
		
	}

	/** Not Applicable **/
	public boolean hasAdvancedCalculate() {
		return false;
	}

	/** Not Applicable **/
	public boolean hasAdvancedConstraint() {
		return false;
	}

	/** Not Applicable **/
	public boolean hasAdvancedRelevant() {
		return false;
	}

	/** Not Applicable **/
	public void setHasAdvancedCalculate(boolean enabled) {
		return;
	}
	
	/** Not Applicable **/
	public void setHasAdvancedConstraint(boolean enabled) {
		return;
	}

	/** Not Applicable **/
	public void setHasAdvancedRelevant(boolean enabled) {
		return;
	}

	/** Not Applicable **/
	public String getAdvancedCalculate() {
		return null;
	}

	/** Not Applicable **/
	public String getAdvancedConstraint() {
		return null;
	}

	/** Not Applicable **/
	public String getAdvancedRelevant() {
		return null;
	}

	/** Not Applicable **/
	public void setAdvancedCalculate(String calcValue) {
		return; 
	}

	/** Not Applicable **/
	public void setAdvancedConstraint(String constValue) {
		return;
	}

	/** Not Applicable **/
	public void setAdvancedRelevant(String releValue) {
		return;
	}

	//this method does not make sense for OptionDefs
	public boolean insertChildAfter(IFormElement child, IFormElement target) {
		return false;
	}

	//this method does not make sense for OptionDefs
	public boolean insertChildBefore(IFormElement child, IFormElement target) {
		return false;
	}
}
