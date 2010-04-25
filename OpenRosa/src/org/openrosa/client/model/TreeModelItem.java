package org.openrosa.client.model;

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;

public class TreeModelItem extends BaseTreeModel implements Serializable {

	public TreeModelItem(String text, Object obj,TreeModel parent){
		
		if(parent != null){
			setParent(parent);
			parent.add(this);
		}
		
		setText(text);
		setUserObject(obj);
	}
	
	public String getText(){
		return get("text");
	}
	
	public void setText(String text){
		set("text",text);
	}
	
	public Object getUserObject(){
		return get("userobject");
	}
	
	public void setUserObject(Object obj){
		set("userobject",obj);
	}
}
