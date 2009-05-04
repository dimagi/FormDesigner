package org.purc.purcforms.client;

import org.purc.purcforms.client.controller.IFormDesignerListener;
import org.purc.purcforms.client.util.FormDesignerUtil;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MenuBar;

/**
 * Creates the main menu for the form designer.
 * 
 * @author daniel
 *
 */
public class Menu extends Composite {

	public interface Images extends Toolbar.Images {
		AbstractImagePrototype info();
	}
	
	private final Images images;
	private MenuBar menuBar;
	private IFormDesignerListener controller;
	
	public Menu(Images images,IFormDesignerListener controller){
		this.images = images;
		this.controller = controller;
		
		setupMenu();
		initWidget(menuBar);
	}
	
	private void setupMenu()
	  {
		    MenuBar fileMenu = new MenuBar(true);
		    fileMenu.addItem(FormDesignerUtil.createHeaderHTML(images.newform(),"New"),true, new Command(){
		    	public void execute() {controller.newForm();}});
		    
		    fileMenu.addItem(FormDesignerUtil.createHeaderHTML(images.open(),"Open"),true, new Command(){
		    	public void execute() {controller.openForm();}});
		    
		    fileMenu.addSeparator();
		    fileMenu.addItem(FormDesignerUtil.createHeaderHTML(images.save(),"Save"),true, new Command(){
		    	public void execute() {controller.saveForm();}});
		    
		    fileMenu.addItem(FormDesignerUtil.createHeaderHTML(images.save(),"Save As"),true, new Command(){
		    	public void execute() {controller.saveFormAs();}});
		    
		    fileMenu.addSeparator();
		    fileMenu.addItem(FormDesignerUtil.createHeaderHTML(images.open(),"Open Layout"),true, new Command(){
		    	public void execute() {controller.openFormLayout();}});
		    
		    fileMenu.addItem(FormDesignerUtil.createHeaderHTML(images.save(),"Save Layout"),true, new Command(){
		    	public void execute() {controller.saveFormLayout();}});
		    
		    fileMenu.addSeparator();
		    fileMenu.addItem(FormDesignerUtil.createHeaderHTML(images.open(),"Open Language Text"),true, new Command(){
		    	public void execute() {controller.openLanguageText();}});
		    
		    fileMenu.addItem(FormDesignerUtil.createHeaderHTML(images.save(),"Save Language Text"),true, new Command(){
		    	public void execute() {controller.saveLanguageText();}});
		    
		    fileMenu.addSeparator();
		    fileMenu.addItem("Close", new Command(){
		    	public void execute() {controller.closeForm();}});

		    MenuBar viewMenu = new MenuBar(true);
		    viewMenu.addItem(FormDesignerUtil.createHeaderHTML(images.loading(),"Refresh"),true, new Command(){
		    	public void execute() {controller.refresh(this);}});

		    MenuBar qtnMenu = new MenuBar(true);
		    qtnMenu.addItem(FormDesignerUtil.createHeaderHTML(images.add(),"Add New"),true, new Command(){
		    	public void execute() {controller.addNewItem();}});
		    
		    qtnMenu.addSeparator();
		    qtnMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),"Add New Child"),true, new Command(){
		    	public void execute() {controller.addNewChildItem();}});
		    
		    qtnMenu.addSeparator();
		    qtnMenu.addItem(FormDesignerUtil.createHeaderHTML(images.delete(),"Delete Selected"),true, new Command(){
		    	public void execute() {controller.deleteSelectedItem();}});
		    
		    qtnMenu.addSeparator();
		    qtnMenu.addItem(FormDesignerUtil.createHeaderHTML(images.moveup(),"Move Up"),true, new Command(){
		    	public void execute() {controller.moveItemUp();}});
		    
		    qtnMenu.addItem(FormDesignerUtil.createHeaderHTML(images.movedown(),"Move Down"),true, new Command(){
		    	public void execute() {controller.moveItemDown();}});

		    qtnMenu.addSeparator();
		    qtnMenu.addItem(FormDesignerUtil.createHeaderHTML(images.cut(),"Cut"),true, new Command(){
		    	public void execute() {controller.cutItem();}});
		    
		    qtnMenu.addItem(FormDesignerUtil.createHeaderHTML(images.copy(),"Copy"),true, new Command(){
		    	public void execute() {controller.copyItem();}});
		    
		    qtnMenu.addItem(FormDesignerUtil.createHeaderHTML(images.paste(),"Paste"),true, new Command(){
		    	public void execute() {controller.pasteItem();}});
		    
		    qtnMenu.addSeparator();
		    qtnMenu.addItem(FormDesignerUtil.createHeaderHTML(images.loading(),"Refresh"),true, new Command(){
		    	public void execute() {controller.refreshItem();}});
		    
		    MenuBar toolsMenu = new MenuBar(true);
		    toolsMenu.addItem("Format", new Command(){
		    	public void execute() {controller.format();}});
		    
		    toolsMenu.addSeparator();
		    toolsMenu.addItem("Languages", new Command(){
		    	public void execute() {controller.showLanguages();}});
		    
		    toolsMenu.addSeparator();
		    toolsMenu.addItem("Options", new Command(){
		    	public void execute() {controller.showOptions();}});
		    
		    MenuBar helpMenu = new MenuBar(true);
		    helpMenu.addItem(FormDesignerUtil.createHeaderHTML(images.info(),"Help Contents"),true, new Command(){
		    	public void execute() {controller.showHelpContents();}});
		    
		    helpMenu.addSeparator();
		    helpMenu.addItem("About " + FormDesignerUtil.getTitle(), new Command(){
		    	public void execute() {controller.showAboutInfo();}});

		    menuBar = new MenuBar();
		    menuBar.addItem("File", fileMenu);
		    menuBar.addItem("View", viewMenu);
		    menuBar.addItem("Item",qtnMenu);
		    menuBar.addItem("Tools", toolsMenu);
		    menuBar.addItem("Help", helpMenu);
		    
		    menuBar.setAnimationEnabled(true);
		    //menuBar.setAutoOpen(true);
	  }
}
