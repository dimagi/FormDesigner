package org.purc.purcforms.client.controller;

import org.purc.purcforms.client.model.Locale;

import com.google.gwt.user.client.Event;


/**
 * This interface is implemented by those who want to listen to global events 
 * of the form designer. An examples of such global events are: New Form, Open,
 * Save, Close, Print, Show About Info, and more.
 * 
 * @author daniel
 *
 */
public interface IFormDesignerListener extends IFormActionListener{

	/**
	 * Opens an existing XForm.
	 */
	public void openForm();
	
	/**
	 * Opens a new XForm.
	 */
	public void newForm();
	
	/**
	 * Saves the form as an xforms document.
	 */
	public void saveForm();
	
	/**
	 * Closes the form designer.
	 */
	public void closeForm();
	
	/** 
	 * Toggles the visibility of the toolbar.
	 */
	public void viewToolbar();
	
	/**
	 * Shows the form designer's help files.
	 */
	public void showHelpContents();
	
	/**
	 * Shows the about dialog box.
	 */
	public void showAboutInfo();
	
	/**
	 * Shows tbe list of languages or locales supported by the form designer.
	 */
	public void showLanguages();
	
	/**
	 * Shows a list of user options or settings for the form designer.
	 */
	public void showOptions();
	
	/**
	 * Aligns widgets on design surface to the left of the widget which was selected last.
	 */
	public void alignLeft();
	
	/**
	 * Aligns widgets on design surface to the right of the widget which was selected last.
	 */
	public void alignRight();
	
	/**
	 * Aligns widgets on design surface to the top of the widget which was selected last.
	 */
	public void alignTop();
	
	/**
	 * Aligns widgets on design surface to the bottom of the widget which was selected last.
	 */
	public void alignBottom();
	
	/**
	 * Makes widgets on design surface to be the same size as the widget which was selected last.
	 */
	public void makeSameSize();
	
	/**
	 * Makes widgets on design surface to be the same height as the widget which was selected last.
	 */
	public void makeSameHeight();
	
	/**
	 * Makes widgets on design surface to be the same width as the widget which was selected last.
	 */
	public void makeSameWidth();
	
	/**
	 * Formats xml with easily readable indenting. The formated is what is currently 
	 * selected (Xforms source, layout xml, labguage xml or model xml
	 */
	public void format();
	
	/**
	 * Saves the form as a new xforms document.
	 */
	public void saveFormAs();
	
	/**
	 * Loads a previously saved form widget layout. This is from saved layout xml.
	 */
	public void openFormLayout();
	
	/**
	 * Saves the form widget layout.
	 */
	public void saveFormLayout();
	
	/**
	 * Refreshed the currently selected object.
	 * 
	 * @param sender the object requesting for the refresh.
	 */
	public void refresh(Object sender);
	//public void saveFormLayoutAs();
	
	/**
	 * Loads the xform and form layout localizable text from a previously saved locale document.
	 */
	public void openLanguageText();
	
	/**
	 * Saves the xforms and form layout localizable text in a locale document.
	 */
	public void saveLanguageText();
	
	/**
	 * Changes the current locale of the form designer.
	 * 
	 * @param locale the locale to change to.
	 */
	public boolean changeLocale(Locale locale);
	
	/**
	 * Embeds the xform into xhtml.
	 */
	public void saveAsXhtml();
	
	public void saveAsPurcForm();
	
	/**
	 * Prints the currently selected page of the xform.
	 */
	public void printForm();
	
	/**
	 * Handles key board events that are not handled by the immediately selected widget.
	 * 
	 * @param event the event.
	 * @return true if you want the event to propagate to other event handlers, else false.
	 */
	public boolean handleKeyBoardEvent(Event event);
}
