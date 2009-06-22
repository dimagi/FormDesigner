package org.purc.purcforms.client.widget;

import com.google.gwt.user.client.ui.Hyperlink;


/**
 * 
 * @author daniel
 *
 */
public class AddConditionHyperlink extends Hyperlink{
	
	private int depth = 1;
	
	public AddConditionHyperlink(String text, String targetHistoryToken, int depth){
		super(text,targetHistoryToken);
		this.depth = depth;
	}
	
	public int getDepth(){
		return depth;
	}
}
