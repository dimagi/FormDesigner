package org.purc.purcforms.client.model;

import java.io.Serializable;


/**
 * 
 * @author daniel
 *
 */
public class DisplayField implements Serializable {

	private String name;
	private String text;
	private String AggFunc;
	
	public DisplayField(){
		
	}

	public DisplayField(String name, String text, String AggFunc) {
		super();
		this.name = name;
		this.text = text;
		this.AggFunc = AggFunc;
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
}
