package org.purc.purcforms.client.controller;

public interface IFormActionListener {
	
	public void deleteSelectedItem();
	public void addNewItem();
	public void addNewChildItem();
	public void moveItemUp();
	public void moveItemDown();
	public void cutItem();
	public void copyItem();
	public void pasteItem();
	public void refreshItem();
}
