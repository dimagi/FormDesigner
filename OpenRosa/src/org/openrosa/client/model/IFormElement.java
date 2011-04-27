package org.openrosa.client.model;

import java.util.List;

import com.google.gwt.xml.client.Element;

/**
 * 
 * @author daniel
 *
 */
public interface IFormElement {

	//NOTE!! Advanced relevant/constriant/calculate implies respective attribute values
	//that the simple parsers can not deal with. These advanced modes are stored as strings
	//and just dumped out on XHTML generation.
	
	public boolean hasAdvancedConstraint();
	public void setHasAdvancedConstraint(boolean enabled);
	public String getAdvancedConstraint();
	public void setAdvancedConstraint(String constValue);
	
	public boolean hasAdvancedRelevant();
	public void setHasAdvancedRelevant(boolean enabled);
	public String getAdvancedRelevant();
	public void setAdvancedRelevant(String releValue);
	
	public boolean hasAdvancedCalculate();
	public void setHasAdvancedCalculate(boolean enabled);
	public String getAdvancedCalculate();
	public void setAdvancedCalculate(String calcValue);
	
	int getId();
	void setId(int id);
	
	String getText();
	void setText(String text);
	
	String getHelpText();
	void setHelpText(String helpText);
	
	int getDataType();
	void setDataType(int dataType);
	
	String getBinding();
	void setBinding(String binding);
	
	List<IFormElement> getChildren();
	void setChildren(List<IFormElement> children);
	void addChild(IFormElement element);
	
	Element getControlNode();
	void setControlNode(Element controlNode);
	
	Element getBindNode();
	void setBindNode(Element bindNode);
	
	Element getDataNode();
	void setDataNode(Element dataNode);
	
	Element getLabelNode();
	void setLabelNode(Element labelNode);
	
	Element getHintNode();
	void setHintNode(Element hintNode);
	
	IFormElement getParent();
	void setParent(IFormElement parent);
	
	void refresh(IFormElement element);
	
	void updateDataNodes(Element parentDataNode);
	
	IFormElement copy(IFormElement parent);
	
	void clearChangeListeners();
	
	String getDisplayText();
	
	/**
	 * Returns the element specified by varName.
	 * if varName matches the parent node, return self,
	 * else go through children elements and return a match
	 * If no match is found, return null.
	 * @param varName
	 * @return
	 */
	IFormElement getElement(String varName);
	
	/**
	 * The Itext ID is always == the Item ID (which should always be unique throughout the form)
	 * @return
	 */
	String getItextId();
	void setItextId(String ID);
	
	boolean removeChild(IFormElement element);
	
	int getChildCount();
	
	public FormDef getFormDef();
	
	boolean isLocked();
	void setLocked(boolean locked);
	boolean isRequired();
	void setRequired(boolean required);
	boolean isEnabled();
	void setEnabled(boolean enabled);
	boolean isVisible();
	void setVisible(boolean visible);
	String getDefaultValue();
	
	FormDef getParentFormDef();
	
	/**
	 * Get the Nodeset ref that points to the data node where the question's answer will be stored.
	 * @return
	 */
	public String getDataNodesetPath();
	
	public void setHasUINode(boolean hasUINode);
	
	public boolean hasUINode();
	
	/**
	 * Moves the given child to a new position in the children list.
	 * Operation does not affect/manipulate the DOM links
	 * of stored XML nodes in the IFormElement
	 * @param child
	 * @param index
	 * @throws Exception
	 */
	public void moveChildToIndex(IFormElement child, int index) throws Exception;
	
	/**
	 * Insert the child IFormElement before the target element (which should
	 * already be a child of the object calling this function)
	 * 
	 * WILL MOVE child TO NEW LOCATION IF ALREADY PRESENT IN PARENT'S CHILDREN LIST 
	 * @param child
	 * @param target
	 * @return
	 */
	public boolean insertChildBefore(IFormElement child, IFormElement target);
	
	/**
	 * Insert the child IFormElement after the target element (which should
	 * already be a child of the object calling this function)
	 * 
	 * will MOVE child TO NEW LOCATION if child is already present in the target's children list! 
	 * @param child
	 * @param target
	 * @return
	 */
	public boolean insertChildAfter(IFormElement child, IFormElement target);

}
