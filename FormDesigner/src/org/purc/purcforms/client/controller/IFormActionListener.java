package org.purc.purcforms.client.controller;


/**
 * This interface is implemented by those classes that want to listen to events which
 * happens on a form in the forms panel. Examples of such events are Add New, Add Child,
 * Delete Selected, Move Up, Move Down, Refresh, Cut, Copy, Paste, and more.
 * 
 * @author daniel
 *
 */
public interface IFormActionListener {
	
	/**
	 * Called to delete the selected item. 
	 * This could be a form, page, question, or question option.
	 */
	public void deleteSelectedItem();
	
	/**
	 * Called to add a new item at the same level as the selected item.
	 * For instance if a form is selected, this should add a new form, if a question
	 * is selected, this should add a new question, if a page is selected, this should
	 * add a new page.
	 * If there is no form in the forms panel, this should add a new form, page, and
	 * one question.
	 */
	public void addNewItem();
	
	/**
	 * Called to add a new item which is a child of the selected item. If there is not form
	 * in the forms panel, or if the selected item should have no kids (eg text question) 
	 * then this does the same as addNewItem(). A form's child is a page, a page's child
	 * is a question, a single select, multiple select question has kids which are options.
	 * A repeat question has kids which are questions.
	 */
	public void addNewChildItem();
	
	/**
	 * Moves the selected item one position above.
	 */
	public void moveItemUp();
	
	/**
	 * Moves the selected item one position below.
	 */
	public void moveItemDown();
	
	/**
	 * Removes the selected item and copies it to the clipboard.
	 */
	public void cutItem();
	
	/**
	 * Copies the selected item to the clipboard.
	 */
	public void copyItem();
	
	/**
	 * Pastes the selected item from the clipboard as a child of the selected item.
	 * If the clipboard item is not a child of the selected item, then the paste
	 * command is ignored.
	 */
	public void pasteItem();
	
	/**
	 * Reloads the selected form with the contents in the xforms source tab.
	 * When not in offline mode, these are changes that have come from the server.
	 */
	public void refreshItem();
	
	/**
	 * Called to select the item above the selected item. 
	 * This is done with the UP arrow key.
	 */
	public void moveUp();
	
	/**
	 * Called to selected the item below the selected item.
	 * This is done with the DOWN arrow key.
	 */
	public void moveDown();
	
	/**
	 * Called to select the parent of the selected item.
	 * This is done with the RIGHT arrow key.
	 */
	public void moveToParent();
	
	/**
	 * Called to move to the child of the selected item.
	 * This is done with the LEFT arrow key.
	 */
	public void moveToChild();
}
