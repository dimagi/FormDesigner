package org.purc.purcforms.client.controller;


/**
 * Interface for listening to item selection events.
 * 
 * @author daniel
 *
 */
public interface ItemSelectionListener {

	/**
	 * Called when an item has been selected.
	 * 
	 * @param sender the widget sending this event.
	 * @param item the item being selected.
	 */
	public void onItemSelected(Object sender, Object item);
	
	/**
	 * Called when an item is about to be selected.
	 * 
	 * @param sender the widget sending this event.
	 */
	public void onStartItemSelection(Object sender);
}
