package org.purc.purcforms.client;

import org.purc.purcforms.client.controller.IFormChangeListener;
import org.purc.purcforms.client.controller.IFormDesignerListener;
import org.purc.purcforms.client.controller.IFormSelectionListener;
import org.purc.purcforms.client.controller.WidgetSelectionListener;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.view.FormsTreeView;
import org.purc.purcforms.client.view.PaletteView;
import org.purc.purcforms.client.view.WidgetPropertiesView;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedStackPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author daniel
 *
 */
public class LeftPanel extends Composite {

	  /**
	   * An image bundle specifying the images for this Widget and aggragating
	   * images needed in child widgets.
	   */
	  public interface Images extends FormsTreeView.Images{
		  
		/**
		 * ImageBundle.@resource view.images.contactsgroup.gif
		 */
	    AbstractImagePrototype contactsgroup();

	    AbstractImagePrototype leftCorner();

	    AbstractImagePrototype mailgroup();

	    AbstractImagePrototype rightCorner();

	    AbstractImagePrototype tasksgroup();
	    
	    AbstractImagePrototype home();

	    AbstractImagePrototype inbox();

	    AbstractImagePrototype sent();

	    AbstractImagePrototype trash();
	    
	    AbstractImagePrototype filtersgroup();
	  }

	  
	  private DecoratedStackPanel stackPanel = new DecoratedStackPanel();
	  private IFormSelectionListener formSelectionListener;
	  private FormsTreeView formsTreeView;
	  private WidgetPropertiesView widgetPropertiesView;
	  private PaletteView paletteView;

	  /**
	   * Constructs a new shortcuts widget using the specified images.
	   * 
	   * @param images a bundle that provides the images for this widget
	   */
	  public LeftPanel(Images images, IFormSelectionListener formSelectionListener) {
	    this.formSelectionListener = formSelectionListener;
	    
	    formsTreeView = new FormsTreeView(images,formSelectionListener);
	    widgetPropertiesView = new WidgetPropertiesView();
	    paletteView =  new PaletteView(images);
	    
	    add(images,formsTreeView , images.tasksgroup(), "Forms");
	    add(images,widgetPropertiesView , images.filtersgroup(), "Properties");
	    //add(images,paletteView, images.contactsgroup(), "Palette");
	    
	    formsTreeView.addFormSelectionListener(widgetPropertiesView);
	    
	    //stackPanel.setTitle("Form Questions");
	    FormDesignerUtil.maximizeWidget(stackPanel);
	    
	    initWidget(stackPanel);
	  }
	  
	  public void setFormDesignerListener(IFormDesignerListener formDesignerListener){
		  formsTreeView.setFormDesignerListener(formDesignerListener);
	  }
	  
	  public void showFormAsRoot(boolean showFormAsRoot){
		  formsTreeView.showFormAsRoot(showFormAsRoot);
	  }
	  
	  public IFormChangeListener getFormChangeListener(){
		  return formsTreeView;
	  }
	  
	  protected void onLoad() {
	    // Show the mailboxes group by default.
	    stackPanel.showStack(0);
	  }

	  private void add(Images images, Widget widget, AbstractImagePrototype imageProto,String caption) {
	    stackPanel.add(widget, FormDesignerUtil.createHeaderHTML(imageProto, caption), true);
	  }
	  
	  public void loadForm(FormDef formDef){
		  formsTreeView.loadForm(formDef,true);
	  }
	  
	  public void refresh(FormDef formDef){
		  formsTreeView.refreshForm(formDef);
	  }
	  
	  public void deleteSelectedItem(){
		  formsTreeView.deleteSelectedItem();
	  }
	  
	  public void addNewForm(){
		  formsTreeView.addNewForm();
	  }
	  
	  public void addNewForm(String name, String varName, int formId){
		  formsTreeView.addNewForm(name, varName, formId);
	  }
	  
	  public void addNewItem(){
		  formsTreeView.addNewItem();
	  }
	  
	  public void addNewChildItem(){
		  formsTreeView.addNewChildItem();
	  }
	  
	  public void moveItemUp() {
		  formsTreeView.moveItemUp();
	  }
	  
	  public void moveItemDown(){
		  formsTreeView.moveItemDown();
	  }
	  
	  public void cutItem(){
		  formsTreeView.cutItem();
	  }
	  
	  public void copyItem() {
		  formsTreeView.copyItem();
	  }
	  
	  public void pasteItem(){
		  formsTreeView.pasteItem();
	  }
	  
	  public void refreshItem(){
		  formsTreeView.refreshItem();
	  }
	  
	  public Object getSelectedForm(){
		  return formsTreeView.getSelectedForm();
	  }
	  
	  public WidgetSelectionListener getWidgetSelectionListener(){
		  return widgetPropertiesView;
	  }
	  
	  public void refresh(){
		  widgetPropertiesView.refresh();
	  }
	  
	  public void clear(){
		  formsTreeView.clear();
	  }
	  
	  public boolean formExists(int formId){
		  return formsTreeView.formExists(formId);
	  }
}
