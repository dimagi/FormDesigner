package org.purc.purcforms.client.model;

import java.io.Serializable;


/**
 * 
 * @author daniel
 *
 */
public class DisplayField implements Serializable {

	/**
	 * Generated serialization ID
	 */
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String text;
	private String AggFunc;
	private int dataType;
	
	public DisplayField(){
		
	}

	public DisplayField(String name, String text, String AggFunc, int dataType) {
		super();
		this.name = name;
		this.text = text;
		this.AggFunc = AggFunc;
		this.dataType = dataType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getAggFunc() {
		return AggFunc;
	}

	public void setAggFunc(String aggFunc) {
		AggFunc = aggFunc;
	}

	public int getDataType() {
		return dataType;
	}

	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
}
