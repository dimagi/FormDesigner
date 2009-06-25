package org.purc.purcforms.client.controller;

public interface IFormDesignerListener extends IFormActionListener{

	/**
	 * Opens an existing Form.
	 */
	public void openForm();
	
	/**
	 * Opens a new Form.
	 */
	public void newForm();
	
	public void saveForm();
	public void closeForm();
	public void viewToolbar();
	public void showHelpContents();
	public void showAboutInfo();
	public void showLanguages();
	public void showOptions();
	public void alignLeft();
	public void alignRight();
	public void alignTop();
	public void makeSameSize();
	public void makeSameHeight();
	public void makeSameWidth();
	public void alignBottom();
	public void format();
	public void saveFormAs();
	
	//public void newFormLayout();
	public void openFormLayout();
	public void saveFormLayout();
	public void refresh(Object sender);
	//public void saveFormLayoutAs();
	public void openLanguageText();
	public void saveLanguageText();
	
	public void changeLocale(String locale);
	
	public void saveAsXhtml();
}
