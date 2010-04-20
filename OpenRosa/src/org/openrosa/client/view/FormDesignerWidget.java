package org.openrosa.client.view;

import org.purc.purcforms.client.FormDesignerImages;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.view.PreviewView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * The form designer widget.
 * 
 * It is composed of a GWT DockPanel as the main widget. This panel contains a 
 * GWT vertical panel which in turn contains a custom Menu widget, custom Tool bar widget, 
 * and a GWT HorizontalSplitPanel. The HorizontalSplitPanel contains a custom LeftPanel widget
 * on the left and a custom CenterPanel widget on the right.
 * When embedded in a GWT application, the embedding application can have custom tool bars and 
 * menu bars, instead of the default ones, which can have commands routed to this widget.
 * 
 * @author daniel
 *
 */
public class FormDesignerWidget extends Composite {

	/**
	 * Instantiate an application-level image bundle. This object will provide
	 * programatic access to all the images needed by widgets.
	 */
	public static final Images images = (Images) GWT.create(Images.class);

	/**
	 * An aggragate image bundle that pulls together all the images for this
	 * application into a single bundle.
	 */
	public interface Images extends LeftPanel.Images,Toolbar.Images,PreviewView.Images,FormDesignerImages {}

	private DockPanel dockPanel;
	
	private CenterWidget centerWidget = new CenterWidget();


	public FormDesignerWidget(boolean showToolbar,boolean showFormAsRoot){
		initDesigner();  
	}

	/**
	 * Builds the form designer and all its widgets.
	 * 
	 * @param showMenubar set to true to show the menu bar.
	 * @param showToolbar set to true to show the tool bar.
	 */
	private void initDesigner(){
		dockPanel = new DockPanel();

		VerticalPanel panel = new VerticalPanel();

		panel.add(new FileToolbar(centerWidget));
		panel.add(centerWidget);
		
		panel.setWidth("100%");

		dockPanel.add(panel, DockPanel.CENTER);

		FormUtil.maximizeWidget(dockPanel);

		initWidget(dockPanel);

		DOM.sinkEvents(getElement(),DOM.getEventsSunk(getElement()) | Event.MOUSEEVENTS);
	}

	/**
	 * @see com.google.gwt.user.client.WindowResizeListener#onWindowResized(int, int)
	 */
	public void onWindowResized(int width, int height){		
		centerWidget.onWindowResized(width, height);
	}

	/**
	 * Loads a form from the server into the form designer.
	 * 
	 * @param formId the form identifier.
	 *//*
	public void loadForm(int formId){
		if(formId != -1)
			controller.loadForm(formId);
	}

	*//**
	 * Sets the offset height of the form designer. This is useful for GWT
	 * applications using the form designer as an embedded widget and do not
	 * want it to take the whole browser space.
	 * 
	 * @param offset the offset height in pixels.
	 *//*
	public void setEmbeddedHeightOffset(int offset){
		centerPanel.setEmbeddedHeightOffset(offset);
	}

	*//**
	 * Loads a form in the form designer.
	 * 
	 * @param formId the form identifier.
	 * @param xform the form's xforms xml.
	 * @param layout the form's layout xml.
	 * @param readOnly set to true to prevent changing of form structure and allow 
	 * 				   changing only text and help text.
	 *//*
	public void loadForm(int formId,String xform, String layout, boolean readOnly){
		if(leftPanel.formExists(formId))
			return;

		centerPanel.setXformsSource(xform, false);
		centerPanel.setLayoutXml(layout, false);
		controller.openFormDeffered(formId,readOnly);
	}

	*//**
	 * Saves the currently selected form in the form designer.
	 *//*
	public void saveSelectedForm(){
		controller.saveForm();
	}

	*//**
	 * Creates a new form in the form designer.
	 * 
	 * @param name the name of the form.
	 * @param varName the form binding.
	 * @param formId the form identifier.
	 *//*
	public void addNewForm(String name, String varName, int formId){
		if(leftPanel.formExists(formId))
			return;

		leftPanel.addNewForm(name, varName, formId);
	}

	*//**
	 * Removes all forms in the form designer.
	 *//*
	public void clear(){
		leftPanel.clear();
	}

	*//**
	 * Sets the position of the splitter between the form designer's
	 * left and center panel.
	 * 
	 * @param pos the position in pixels.
	 *//*
	public void setSplitPos(String pos){
		hsplitClient.setSplitPosition(pos);
	}

	*//**
	 * Sets the listener to form save events.
	 * 
	 * @param formSaveListener the listener.
	 *//*
	public void setFormSaveListener(IFormSaveListener formSaveListener){
		controller.setFormSaveListener(formSaveListener);
	}

	*//**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#format()
	 *//*
	public void format(){
		controller.format();
	}

	*//**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignLeft()
	 *//*
	public void alignLeft(){
		controller.alignLeft();
	}

	*//**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignRight()
	 *//*
	public void alignRight(){
		controller.alignRight();
	}

	*//**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignTop()
	 *//*
	public void alignTop(){
		controller.alignTop();
	}

	*//**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#makeSameSize()
	 *//*
	public void makeSameSize(){
		controller.makeSameSize();
	}

	*//**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#makeSameHeight()
	 *//*
	public void makeSameHeight(){
		controller.makeSameHeight();
	}

	*//**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#makeSameWidth()
	 *//*
	public void makeSameWidth(){
		controller.makeSameWidth();
	}

	*//**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignBottom()
	 *//*
	public void alignBottom(){
		controller.alignBottom();
	}

	*//**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#showAboutInfo()
	 *//*
	public void openForm(){
		controller.openForm();
	}

	*//**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#moveItemUp()
	 *//*
	public void moveItemUp(){
		controller.moveItemUp();
	}

	*//**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#moveItemDown()
	 *//*
	public void moveItemDown(){
		controller.moveItemDown();
	}

	*//**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#cutItem()
	 *//*
	public void cutItem(){
		controller.cutItem();
	}

	*//**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#copyItem()
	 *//*
	public void copyItem(){
		controller.copyItem();
	}

	*//**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#pasteItem()
	 *//*
	public void pasteItem(){
		controller.pasteItem();
	}

	*//**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#addNewChildItem()
	 *//*
	public void addNewChildItem(){
		controller.addNewChildItem();
	}

	*//**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#addNewItem()
	 *//*
	public void addNewItem(){
		controller.addNewItem();
	}

	*//**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#deleteSelectedItems()
	 *//*
	public void deleteSelectedItem(){
		controller.deleteSelectedItem();
	}

	*//**
	 * Refreshes the currently selected item.
	 *//*
	public void refreshItem(){
		controller.refresh(this);
	}

	*//**
	 * @see org.purc.purcforms.client.LeftPanel#getSelectedForm()
	 *//*
	public FormDef getSelectedForm(){
		return leftPanel.getSelectedForm();
	}

	*//**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#setDefaultLocale(java.lang.String)
	 *//*
	public void setDefaultLocale(String locale){
		controller.setDefaultLocale(locale);
	}

	*//**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#changeLocale(java.lang.String)
	 *//*
	public void changeLocale(String locale){
		controller.changeLocale(locale);
	}

	*//**
	 * Sets the xforms and layout xml locale text for a given form.
	 * 
	 * @param formId the form identifier.
	 * @param locale the locale key.
	 * @param xform the form's xforms xml.
	 * @param layout the form's layout xml.
	 *//*
	public void setLocaleText(Integer formId, String locale, String xform, String layout){
		controller.setLocaleText(formId, locale, xform, layout);
	}

	*//**
	 * Removes the language tab.
	 *//*
	public void removeLanguageTab(){
		centerPanel.removeLanguageTab();
	}

	public void populateLocales(){
		toolbar.populateLocales();
	}

	public void removeXformSourceTab(){
		centerPanel.removeXformSourceTab();
	}


	public void removeLayoutXmlTab(){
		centerPanel.removeLayoutXmlTab();
	}


	public void removeModelXmlTab(){
		centerPanel.removeModelXmlTab();
	}

	public void removeJavaScriptTab(){
		centerPanel.removeJavaScriptSourceTab();
	}
*/
	
}