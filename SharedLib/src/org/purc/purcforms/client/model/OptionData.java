package org.purc.purcforms.client.model;

import java.io.Serializable;


/**
 * 
 * @author Daniel
 *
 */
public class OptionData  implements Serializable {
	
	private int id = ModelConstants.NULL_ID;
	private OptionDef def;
	
	public OptionData(){
		
	}

	/** Copy constructor. */
	public OptionData(OptionData data){
		setId(data.getId());
		setDef(new OptionDef(data.getDef(),data.getDef().getParent()));
	}
	
	public OptionData( OptionDef def) {
		setDef(def);
		setId(def.getId());
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public OptionDef getDef() {
		return def;
	}

	public void setDef(OptionDef def) {
		this.def = def;
	}

	public String toString() {
		return getDef().getText();
	}
	
	public String getValue(){
		return getDef().getVariableName();
	}
}
