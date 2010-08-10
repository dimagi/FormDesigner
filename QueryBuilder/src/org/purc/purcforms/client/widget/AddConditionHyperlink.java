package org.purc.purcforms.client.widget;

import com.google.gwt.user.client.ui.Anchor;


/**
 * 
 * @author daniel
 *
 */
public class AddConditionHyperlink extends Anchor {
	
	private int depth = 1;
	
	public AddConditionHyperlink(String text, String target, int depth){
		super(text, target);
		this.depth = depth;
	}
	
	public int getDepth(){
		return depth;
	}
}
