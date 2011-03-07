package org.openrosa.client.model;

import java.util.List;

import com.google.gwt.xml.client.Element;

/**
 * 
 * @author daniel
 *
 */
public interface IFormElement {

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
	
	/**
	 * Get the Nodeset ref that points to the data node where the question's answer will be stored.
	 * @return
	 */
	public String getDataNodesetPath();
}
