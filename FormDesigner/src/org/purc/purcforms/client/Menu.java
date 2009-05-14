package org.purc.purcforms.client;

import org.purc.purcforms.client.controller.IFormDesignerListener;
import org.purc.purcforms.client.locale.LocaleText;
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
		    fileMenu.addItem(FormDesignerUtil.createHeaderHTML(images.newform(),LocaleText.get("newForm")),true, new Command(){
		    	public void execute() {controller.newForm();}});
		    
		    fileMenu.addItem(FormDesignerUtil.createHeaderHTML(images.open(),LocaleText.get("open")),true, new Command(){
		    	public void execute() {controller.openForm();}});
		    
		    fileMenu.addSeparator();
		    fileMenu.addItem(FormDesignerUtil.createHeaderHTML(images.save(),LocaleText.get("save")),true, new Command(){
		    	public void execute() {controller.saveForm();}});
		    
		    fileMenu.addItem(FormDesignerUtil.createHeaderHTML(images.save(),LocaleText.get("saveAs")),true, new Command(){
		    	public void execute() {controller.saveFormAs();}});
		    
		    fileMenu.addSeparator();
		    fileMenu.addItem(FormDesignerUtil.createHeaderHTML(images.open(),LocaleText.get("openLayout")),true, new Command(){
		    	public void execute() {controller.openFormLayout();}});
		    
		    fileMenu.addItem(FormDesignerUtil.createHeaderHTML(images.save(),LocaleText.get("saveLayout")),true, new Command(){
		    	public void execute() {controller.saveFormLayout();}});
		    
		    fileMenu.addSeparator();
		    fileMenu.addItem(FormDesignerUtil.createHeaderHTML(images.open(),LocaleText.get("openLanguageText")),true, new Command(){
		    	public void execute() {controller.openLanguageText();}});
		    
		    fileMenu.addItem(FormDesignerUtil.createHeaderHTML(images.save(),LocaleText.get("saveLanguageText")),true, new Command(){
		    	public void execute() {controller.saveLanguageText();}});
		    
		    fileMenu.addSeparator();
		    fileMenu.addItem(LocaleText.get("close"), new Command(){
		    	public void execute() {controller.closeForm();}});

		    MenuBar viewMenu = new MenuBar(true);
		    viewMenu.addItem(FormDesignerUtil.createHeaderHTML(images.loading(),LocaleText.get("refresh")),true, new Command(){
		    	public void execute() {controller.refresh(this);}});

		    MenuBar qtnMenu = new MenuBar(true);
		    qtnMenu.addItem(FormDesignerUtil.createHeaderHTML(images.add(),LocaleText.get("addNew")),true, new Command(){
		    	public void execute() {controller.addNewItem();}});
		    
		    qtnMenu.addSeparator();
		    qtnMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("addNewChild")),true, new Command(){
		    	public void execute() {controller.addNewChildItem();}});
		    
		    qtnMenu.addSeparator();
		    qtnMenu.addItem(FormDesignerUtil.createHeaderHTML(images.delete(),LocaleText.get("deleteSelected")),true, new Command(){
		    	public void execute() {controller.deleteSelectedItem();}});
		    
		    qtnMenu.addSeparator();
		    qtnMenu.addItem(FormDesignerUtil.createHeaderHTML(images.moveup(),LocaleText.get("moveUp")),true, new Command(){
		    	public void execute() {controller.moveItemUp();}});
		    
		    qtnMenu.addItem(FormDesignerUtil.createHeaderHTML(images.movedown(),LocaleText.get("moveDown")),true, new Command(){
		    	public void execute() {controller.moveItemDown();}});

		    qtnMenu.addSeparator();
		    qtnMenu.addItem(FormDesignerUtil.createHeaderHTML(images.cut(),LocaleText.get("cut")),true, new Command(){
		    	public void execute() {controller.cutItem();}});
		    
		    qtnMenu.addItem(FormDesignerUtil.createHeaderHTML(images.copy(),LocaleText.get("copy")),true, new Command(){
		    	public void execute() {controller.copyItem();}});
		    
		    qtnMenu.addItem(FormDesignerUtil.createHeaderHTML(images.paste(),LocaleText.get("paste")),true, new Command(){
		    	public void execute() {controller.pasteItem();}});
		    
		    qtnMenu.addSeparator();
		    qtnMenu.addItem(FormDesignerUtil.createHeaderHTML(images.loading(),LocaleText.get("refresh")),true, new Command(){
		    	public void execute() {controller.refreshItem();}});
		    
		    MenuBar toolsMenu = new MenuBar(true);
		    toolsMenu.addItem(LocaleText.get("format"), new Command(){
		    	public void execute() {controller.format();}});
		    
		    toolsMenu.addSeparator();
		    toolsMenu.addItem(LocaleText.get("languages"), new Command(){
		    	public void execute() {controller.showLanguages();}});
		    
		    toolsMenu.addSeparator();
		    toolsMenu.addItem(LocaleText.get("options"), new Command(){
		    	public void execute() {controller.showOptions();}});
		    
		    MenuBar helpMenu = new MenuBar(true);
		    helpMenu.addItem(FormDesignerUtil.createHeaderHTML(images.info(),LocaleText.get("helpContents")),true, new Command(){
		    	public void execute() {controller.showHelpContents();}});
		    
		    helpMenu.addSeparator();
		    helpMenu.addItem(LocaleText.get("about") + FormDesignerUtil.getTitle(), new Command(){
		    	public void execute() {controller.showAboutInfo();}});

		    menuBar = new MenuBar();
		    menuBar.addItem(LocaleText.get("file"), fileMenu);
		    menuBar.addItem(LocaleText.get("view"), viewMenu);
		    menuBar.addItem(LocaleText.get("item"),qtnMenu);
		    menuBar.addItem(LocaleText.get("tools"), toolsMenu);
		    menuBar.addItem(LocaleText.get("help"), helpMenu);
		    
		    menuBar.setAnimationEnabled(true);
		    //menuBar.setAutoOpen(true);
	  }
}
