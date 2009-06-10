package org.purc.purcforms.client.widget;

import org.purc.purcforms.client.controller.ItemSelectionListener;

import com.google.gwt.user.client.Command;


/**
 * 
 * @author daniel
 *
 */
public class SelectItemCommand implements Command{

	private Object item;
	private ItemSelectionListener itemSelectionListener;
	
	public SelectItemCommand(Object item,ItemSelectionListener itemSelectionListener){
		this.item = item;
		this.itemSelectionListener = itemSelectionListener;
	}
	
	/**
	 * @see com.google.gwt.user.client.Command#execute()
	 */
	public void execute() {
		itemSelectionListener.onItemSelected(this, item);
	}

	
}
