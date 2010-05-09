package org.openrosa.client.xforms;

import com.google.gwt.xml.client.Element;


/**
 * 
 * @author daniel
 *
 */
public class NodeContext {

	String label = "";
	String hint = "";
	String value = "";
	Element labelNode = null;
	Element hintNode = null;
	Element valueNode = null;
	
	
	public NodeContext(String label, String hint, String value,
			Element labelNode, Element hintNode, Element valueNode) {
		this.label = label;
		this.hint = hint;
		this.value = value;
		this.labelNode = labelNode;
		this.hintNode = hintNode;
		this.valueNode = valueNode;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getHint() {
		return hint;
	}

	public void setHint(String hint) {
		this.hint = hint;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Element getLabelNode() {
		return labelNode;
	}

	public void setLabelNode(Element labelNode) {
		this.labelNode = labelNode;
	}

	public Element getHintNode() {
		return hintNode;
	}

	public void setHintNode(Element hintNode) {
		this.hintNode = hintNode;
	}

	public Element getValueNode() {
		return valueNode;
	}

	public void setValueNode(Element valueNode) {
		this.valueNode = valueNode;
	}
}
