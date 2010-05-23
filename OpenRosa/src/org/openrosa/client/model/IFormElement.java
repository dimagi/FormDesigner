package org.openrosa.client.model;

import java.util.List;

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
	
	String getBinding();
	void setBinding(String binding);
	
	List<IFormElement> getChildren();
	void setChildren(List<IFormElement> children);
}
