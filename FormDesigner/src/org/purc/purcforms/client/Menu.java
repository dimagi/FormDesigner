package org.purc.purcforms.client;

import org.purc.purcforms.client.controller.IFormDesignerListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.util.FormDesignerUtil;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MenuBar;

/**
 * Creates the main menu widget for the form designer.
 * 
 * @author daniel
 *
 */
public class Menu extends Composite {

	/**
	 * The menu images.
	 */
	public interface Images extends Toolbar.Images {
		ImageResource info();
	}

	/** The images for menu icons. */
	private final Images images;

	/** The underlying GWT menu bar. */
	private MenuBar menuBar;

	/** Listener to menu item click events. */
	private IFormDesignerListener controller;


	/**
	 * Creates a new instance of the menu bar.
	 * 
	 * @param images the images for menu icons.
	 * @param controller the listener to menu item click events.
	 */
	public Menu(Images images,IFormDesignerListener controller){
		this.images = images;
		this.controller = controller;

		setupMenu();

		initWidget(menuBar);
	}

	/**
	 * Sets up the menu bar.
	 */
	private void setupMenu()
	{
		//Set up the file menu.
		MenuBar fileMenu = new MenuBar(true);

		if(Context.isOfflineMode()){
			fileMenu.addItem(FormDesignerUtil.createHeaderHTML(images.newform(),LocaleText.get("newForm")),true, new Command(){
				public void execute() {controller.newForm();}});
		}

		fileMenu.addItem(FormDesignerUtil.createHeaderHTML(images.open(),LocaleText.get("open")),true, new Command(){
			public void execute() {controller.openForm();}});

		fileMenu.addItem(FormDesignerUtil.createHeaderHTML(images.open(),LocaleText.get("print")),true, new Command(){
			public void execute() {controller.printForm();}});

		fileMenu.addSeparator();


		fileMenu.addItem(FormDesignerUtil.createHeaderHTML(images.save(),LocaleText.get("save")),true, new Command(){
			public void execute() {controller.saveForm();}});

		if(Context.isOfflineMode()){
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
			fileMenu.addItem(FormDesignerUtil.createHeaderHTML(images.save(),LocaleText.get("saveAsXhtml")),true, new Command(){
				public void execute() {controller.saveAsXhtml();}});
		}

		fileMenu.addItem(FormDesignerUtil.createHeaderHTML(images.save(),LocaleText.get("saveAsPurcForm")),true, new Command(){
			public void execute() {controller.saveAsPurcForm();}});


		fileMenu.addSeparator();
		fileMenu.addItem(LocaleText.get("close"), new Command(){
			public void execute() {controller.closeForm();}});



		//Set up the view menu.
		MenuBar viewMenu = new MenuBar(true);
		viewMenu.addItem(FormDesignerUtil.createHeaderHTML(images.refresh(),LocaleText.get("refresh")),true, new Command(){
			public void execute() {controller.refresh(this);}});


		//Set up the item menu.
		MenuBar itemMenu = new MenuBar(true);
		itemMenu.addItem(FormDesignerUtil.createHeaderHTML(images.add(),LocaleText.get("addNew")),true, new Command(){
			public void execute() {controller.addNewItem();}});

		itemMenu.addSeparator();
		itemMenu.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("addNewChild")),true, new Command(){
			public void execute() {controller.addNewChildItem();}});

		itemMenu.addSeparator();
		itemMenu.addItem(FormDesignerUtil.createHeaderHTML(images.delete(),LocaleText.get("deleteSelected")),true, new Command(){
			public void execute() {controller.deleteSelectedItem();}});

		itemMenu.addSeparator();
		itemMenu.addItem(FormDesignerUtil.createHeaderHTML(images.moveup(),LocaleText.get("moveUp")),true, new Command(){
			public void execute() {controller.moveItemUp();}});

		itemMenu.addItem(FormDesignerUtil.createHeaderHTML(images.movedown(),LocaleText.get("moveDown")),true, new Command(){
			public void execute() {controller.moveItemDown();}});

		itemMenu.addSeparator();
		itemMenu.addItem(FormDesignerUtil.createHeaderHTML(images.cut(),LocaleText.get("cut")),true, new Command(){
			public void execute() {controller.cutItem();}});

		itemMenu.addItem(FormDesignerUtil.createHeaderHTML(images.copy(),LocaleText.get("copy")),true, new Command(){
			public void execute() {controller.copyItem();}});

		itemMenu.addItem(FormDesignerUtil.createHeaderHTML(images.paste(),LocaleText.get("paste")),true, new Command(){
			public void execute() {controller.pasteItem();}});

		itemMenu.addSeparator();
		itemMenu.addItem(FormDesignerUtil.createHeaderHTML(images.refresh(),LocaleText.get("refresh")),true, new Command(){
			public void execute() {controller.refreshItem();}});


		// Set up the tools menu.
		MenuBar toolsMenu = new MenuBar(true);
		toolsMenu.addItem(LocaleText.get("format"), new Command(){
			public void execute() {controller.format();}});

		toolsMenu.addSeparator();
		toolsMenu.addItem(LocaleText.get("languages"), new Command(){
			public void execute() {controller.showLanguages();}});

		toolsMenu.addSeparator();
		toolsMenu.addItem(LocaleText.get("options"), new Command(){
			public void execute() {controller.showOptions();}});


		//Set up the help menu.
		MenuBar helpMenu = new MenuBar(true);
		helpMenu.addItem(FormDesignerUtil.createHeaderHTML(images.info(),LocaleText.get("helpContents")),true, new Command(){
			public void execute() {controller.showHelpContents();}});

		helpMenu.addSeparator();
		helpMenu.addItem(LocaleText.get("about") + " " + FormDesignerUtil.getTitle(), new Command(){
			public void execute() {controller.showAboutInfo();}});


		//Add all the top level menus to the GWT menu bar.
		menuBar = new MenuBar();
		menuBar.addItem(LocaleText.get("file"), fileMenu);
		menuBar.addItem(LocaleText.get("view"), viewMenu);
		menuBar.addItem(LocaleText.get("item"),itemMenu);
		menuBar.addItem(LocaleText.get("tools"), toolsMenu);
		menuBar.addItem(LocaleText.get("help"), helpMenu);

		menuBar.setAnimationEnabled(true);
	}
}
