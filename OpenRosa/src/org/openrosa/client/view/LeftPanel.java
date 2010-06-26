package org.openrosa.client.view;

import java.util.List;

import org.openrosa.client.model.FormDef;
import org.purc.purcforms.client.Context;
import org.purc.purcforms.client.controller.IFormActionListener;
import org.purc.purcforms.client.controller.IFormChangeListener;
import org.purc.purcforms.client.controller.IFormDesignerListener;
import org.purc.purcforms.client.controller.IFormSelectionListener;
import org.purc.purcforms.client.model.Locale;
import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.util.FormUtil;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedStackPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * This widget is on the left hand side of the form designer and contains
 * the Forms, Palette, and Widget Properties panels.
 * 
 * @author daniel
 *
 */
public class LeftPanel extends Composite {

	/**
	 * An image bundle specifying the images for this Widget and aggregating
	 * images needed in child widgets.
	 */
	public interface Images extends FormsTreeView.Images{
		ImageResource tasksgroup();
		ImageResource filtersgroup();
	}

	/** The GWT stack panel which serves as the main or root widget. */
	private FlowPanel stackPanel = new FlowPanel();
	
	/** Widgets which displays the list of forms in a tree view. */
	private FormsTreeView formsTreeView;
	

	/**
	 * Constructs a new left panel object.
	 * 
	 * @param images a bundle that provides the images for this widget
	 */
	public LeftPanel(Images images, IFormSelectionListener formSelectionListener) {
		formsTreeView = new FormsTreeView(images,formSelectionListener);
		ContentPanel cp = new ContentPanel();
		cp.setHeading("Form Tree View");
		//cp.setScrollMode(Scroll.AUTOY);
		cp.add(formsTreeView);
		stackPanel.add(cp);
//		add(images,formsTreeView , images.tasksgroup(), "Form Outline");
		FormUtil.maximizeWidget(stackPanel);

		initWidget(stackPanel);
		stackPanel.addStyleName("myFormTreeView");
	}

	/**
	 * Sets the listener to form designer global events.
	 * 
	 * @param formDesignerListener the listener.
	 */
	public void setFormDesignerListener(IFormDesignerListener formDesignerListener){
		formsTreeView.setFormDesignerListener(formDesignerListener);
	}

	public void showFormAsRoot(){
		formsTreeView.showFormAsRoot(true);
	}

	/**
	 * Gets the listener for form item property changes.
	 * 
	 * @return the listener.
	 */
	public IFormChangeListener getFormChangeListener(){
		return formsTreeView;
	}

	private void add(Images images, Widget widget, ImageResource imageProto,String caption) {
//		stackPanel.add(widget, FormDesignerUtil.createHeaderHTML(imageProto, caption), true);
	}

	/**
	 * Loads a given form.
	 * 
	 * @param formDef the form definition object.
	 */
	public void loadForm(FormDef formDef){
		formsTreeView.loadForm(formDef,true,false);
	}

	public void refresh(FormDef formDef){
		formsTreeView.refreshForm(formDef);
	}
	
	/**
	 * Gets the list of forms which are loaded.
	 * 
	 * @return the forms list.
	 */
	public List<FormDef> getForms(){
		return formsTreeView.getForms();
	}
	
	/**
	 * Loads a list of forms and selects one of them.
	 * 
	 * @param forms the form list to load.
	 * @param selFormId the id of the form to select.
	 */
	public void loadForms(List<FormDef> forms, int selFormId){
		formsTreeView.loadForms(forms,selFormId);
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#deleteSelectedItems()
	 */
	public void deleteSelectedItem(){
		formsTreeView.deleteSelectedItem();
	}

	/**
	 * Adds a new form.
	 */
	public void addNewForm(){
		formsTreeView.addNewForm();
	}

	/**
	 * Adds a new form with a given name, binding, and id
	 * 
	 * @param name the form name.
	 * @param varName the form binding.
	 * @param formId the form id.
	 */
	public void addNewForm(String name, String varName, int formId){
		formsTreeView.addNewForm(name, varName, formId);
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#addNewItem()
	 */
	public void addNewItem(){
		formsTreeView.addNewItem();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#addNewChildItem()
	 */
	public void addNewChildItem(){
		formsTreeView.addNewChildItem();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#moveItemUp()
	 */
	public void moveItemUp() {
		formsTreeView.moveItemUp();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#moveItemDown()
	 */
	public void moveItemDown(){
		formsTreeView.moveItemDown();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#cutItem()
	 */
	public void cutItem(){
		formsTreeView.cutItem();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#copyItem()
	 */
	public void copyItem() {
		formsTreeView.copyItem();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#pasteItem()
	 */
	public void pasteItem(){
		formsTreeView.pasteItem();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#refreshItem()
	 */
	public void refreshItem(){
		formsTreeView.refreshItem();
	}
	
	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#addNewQuestion()
	 */
	public void addNewQuestion(int dataType){
		formsTreeView.addNewQuestion(dataType);
	}

	/**
	 * Gets the selected form.
	 * 
	 * @return the form definition object.
	 */
	public FormDef getSelectedForm(){
		return formsTreeView.getSelectedForm();
	}

	/**
	 * Removes all forms.
	 */
	public void clear(){
		formsTreeView.clear();
	}

	/**
	 * Check if a with a given id is loaded.
	 * 
	 * @param formId the form id.
	 * @return true if it exists, else false.
	 */
	public boolean formExists(int formId){
		return formsTreeView.formExists(formId);
	}

	/**
	 * Checks if the selected form is valid for saving.
	 * 
	 * @return true if valid, else false.
	 */
	public boolean isValidForm(){
		return formsTreeView.isValidForm();
	}
	
	/**
	 * Gets the listener to form action events.
	 * 
	 * @return the listener.
	 */
	public IFormActionListener getFormActionListener(){
		return formsTreeView;
	}
	
	/**
	 * Sets the default locale.
	 * 
	 * @param locale the localey key.
	 */
	public void setDefaultLocale(Locale locale){
		Context.setDefaultLocale(locale);
	}
	
	/**
	 * Adds a listener to form item selection events.
	 * 
	 * @param formSelectionListener the listener to add.
	 */
	public void addFormSelectionListener(IFormSelectionListener formSelectionListener){
		formsTreeView.addFormSelectionListener(formSelectionListener);
	}
}
