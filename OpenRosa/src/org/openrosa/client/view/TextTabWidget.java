package org.openrosa.client.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openrosa.client.controller.ITextListener;
import org.openrosa.client.model.ItextModel;
import org.openrosa.client.util.Itext;
import org.openrosa.client.util.ItextLocale;
import org.openrosa.client.util.FormUtil;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonGroup;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.AbstractImagePrototype;


/**
 * The widget for the internationalization tab.
 * 
 *
 */
public class TextTabWidget extends com.extjs.gxt.ui.client.widget.Composite {

	private EditorGrid<ItextModel> grid;
	private ContentPanel contentPanel = new ContentPanel();
	private Window window = new Window();

	private ColumnModel cm;
	private int currentColumnIndex = 0;
	private int currentRowIndex = 0;

	private ITextListener listener;
	private static boolean showWindow = false;


	/** The images for the tool bar icons. */
	public final Images images;

	public interface Images extends ClientBundle{
		ImageResource smallAdd();
		ImageResource smallRemove();
	}

	public TextTabWidget(ITextListener listener){
		this.listener = listener;
		this.images = GWT.create(Images.class);
		window.setMaximizable(true);  
		window.setHeading("Localization");  
		grid = new EditorGrid<ItextModel>(new ListStore<ItextModel>(), createColumnModel());
		grid.setBorders(true);
		grid.setStripeRows(true);
		grid.setWidth(700);
		contentPanel.setHeaderVisible(false);
		contentPanel.setLayout(new FitLayout());
		contentPanel.add(grid);
		contentPanel.setWidth(700);

		makeToolbar();
		makeContextMenu();

		window.add(contentPanel);
		window.setWidth(700);
		window.setMinHeight(400);
		window.setMinWidth(400);

		FormUtil.maximizeWidget(grid);
		FormUtil.maximizeWidget(contentPanel);

		window.addListener(Events.Resize, new Listener<ComponentEvent>() {
			public void handleEvent(final ComponentEvent event) {
				contentPanel.setWidth(window.getWidth());
			}});

		setupContextMenu();

		FormUtil.maximizeWidget(this);
	}

	private void setupContextMenu() {
		grid.addListener(Events.HeaderContextMenu, new Listener<GridEvent<ModelData>>(){
			public void handleEvent(final GridEvent<ModelData> ge)
			{
				currentColumnIndex = ge.getColIndex();
				MenuItem menuItem = new MenuItem("Add Language");
				menuItem.addListener(Events.Select, new Listener<BaseEvent>(){
					public void handleEvent(BaseEvent be)
					{
						addNewLanguage("Language");
					}
				});

				ge.getMenu().add(menuItem);


				if(cm.getColumnCount() > 3){
					menuItem = new MenuItem("Remove Language");
					menuItem.addListener(Events.Select, new Listener<BaseEvent>(){
						public void handleEvent(BaseEvent be)
						{
							removeLanguage();
						}
					});

					ge.getMenu().add(menuItem);
				}


				if(currentColumnIndex > 1){
					menuItem = new MenuItem("Rename Language");
					menuItem.addListener(Events.Select, new Listener<BaseEvent>(){
						public void handleEvent(BaseEvent be)
						{
							renameLanguage(currentColumnIndex);
						}
					});

					ge.getMenu().add(menuItem);
				}
			}
		});


		grid.addListener(Events.CellClick, new Listener<GridEvent<ModelData>>(){
			public void handleEvent(final GridEvent<ModelData> ge)
			{
				currentColumnIndex = ge.getColIndex();
				currentRowIndex = ge.getRowIndex();
			}
		});
	}

	public void makeToolbar(){
		Button addLang,removeLang, btnSave, btnAddRow, btnRemoveRow;
		btnSave = new Button("Save");
		addLang = new Button("Add Language");
		removeLang = new Button("Remove Language");
		btnAddRow = new Button("Add Row");
		btnRemoveRow = new Button("Remove Row");
		ButtonGroup group = new ButtonGroup(5);
		ToolBar tb = new ToolBar();
		tb.add(btnSave);
		tb.add(new SeparatorToolItem());
		tb.add(addLang);
		tb.add(new SeparatorToolItem());
		tb.add(removeLang);
		tb.add(new SeparatorToolItem());
		tb.add(btnAddRow);
		tb.add(new SeparatorToolItem());
		tb.add(btnRemoveRow);

		contentPanel.setTopComponent(tb);

		btnSave.addListener(Events.Select, new Listener<ButtonEvent>(){
			public void handleEvent(ButtonEvent be)
			{
				save(true);
			}
		});

		addLang.addListener(Events.Select, new Listener<ButtonEvent>(){
			public void handleEvent(ButtonEvent be)
			{
				addNewLanguage("Language");
			}
		});

		removeLang.addListener(Events.Select, new Listener<ButtonEvent>(){
			public void handleEvent(ButtonEvent be)
			{
				removeLang();
			}
		});

		btnAddRow.addListener(Events.Select, new Listener<ButtonEvent>(){
			public void handleEvent(ButtonEvent be)
			{
				addNewRow();
			}
		});

		btnRemoveRow.addListener(Events.Select, new Listener<ButtonEvent>(){
			public void handleEvent(ButtonEvent be)
			{
				removeRow();
			}
		});
	}



	public void makeContextMenu(){
		Menu contextMenu = new Menu();  

		MenuItem addLang = new MenuItem();  
		addLang.setText("Add Language");  
		addLang.setIcon(AbstractImagePrototype.create(images.smallAdd()));  
		addLang.addSelectionListener(new SelectionListener<MenuEvent>() {  
			public void componentSelected(MenuEvent ce) {  
				addNewLanguage("Language");
			}
		});  
		contextMenu.add(addLang);  

		MenuItem removeLang = new MenuItem();  
		removeLang.setText("Remove Language");  
		removeLang.setIcon(AbstractImagePrototype.create(images.smallRemove()));  
		removeLang.addSelectionListener(new SelectionListener<MenuEvent>() {  
			public void componentSelected(MenuEvent ce) {  
				removeLang();
			}  
		});  
		contextMenu.add(removeLang);  

		MenuItem addRow = new MenuItem();  
		addRow.setText("Add Row");   
		addRow.addSelectionListener(new SelectionListener<MenuEvent>() {  
			public void componentSelected(MenuEvent ce) {  
				addNewRow();
			}  
		});  
		contextMenu.add(addRow); 

		MenuItem btnRemoveRow = new MenuItem();  
		btnRemoveRow.setText("Remove Row");   
		btnRemoveRow.addSelectionListener(new SelectionListener<MenuEvent>() {  
			public void componentSelected(MenuEvent ce) {  
				removeRow();
			}  
		});  
		contextMenu.add(btnRemoveRow); 


		grid.setContextMenu(contextMenu);  
	}

	private void removeLang(){		
		if(cm.getColumnCount() > 3)
			removeLanguage();
		else{
			com.google.gwt.user.client.Window.alert("Must have at least one language!");
		}
	}


	public void showWindow(){
		window.show();
	}

	public void hideWindow(){
		window.hide();
	}

	private ColumnModel createColumnModel(){
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		ColumnConfig xpath = new ColumnConfig("xpath", "Xpath", 5);
		xpath.setHidden(true);
		configs.add(xpath);

		ColumnConfig formId = new ColumnConfig("id", "Id", 200);
		configs.add(formId);
		TextField<String> text = new TextField<String>();
		CellEditor cellEditor = new CellEditor(text);
		formId.setEditor(cellEditor);
		//cellEditor.setStyle

		List<ItextLocale> locales = Itext.locales;
		if(locales != null){
			for(ItextLocale locale : locales){
				ColumnConfig columnConfig = new ColumnConfig(locale.getName(), locale.getName(), 200);
				configs.add(columnConfig);
				columnConfig.setEditor(new CellEditor(new TextField<String>()));
				columnConfig.setStyle("font-size: 20px");
			}
		}
		return new ColumnModel(configs);
	}

	/**
	 * Called by external methods when loading the Edit Itext window
	 */
	public void loadItext(){
		cm = createColumnModel();
		ListStore<ItextModel> rows = Itext.getItextRows();
		grid.reconfigure(rows, cm);
	}


	public void adjustHeight(String height){
		contentPanel.setHeight(height);
	}

	public void addNewLanguage(String defaultName) {
		String lang = com.google.gwt.user.client.Window.prompt("Please enter the language name", defaultName);
		if(lang != null && lang.trim().length() > 0){

			if(languageExists(lang.trim())){
				com.google.gwt.user.client.Window.alert("Please enter a language name different from those that exist.");
				addNewLanguage(lang);
				return;
			}

			ColumnConfig columnConfig = new ColumnConfig(lang, lang, 200);
			grid.getColumnModel().getColumns().add(columnConfig);
			columnConfig.setEditor(new CellEditor(new TextField<String>()));

			Itext.addLocale(lang);

			grid.reconfigure(Itext.getItextRows(), cm);

		}
	}  

	public void removeLanguage(){
		String language = cm.getColumnHeader(currentColumnIndex);
		if(!com.google.gwt.user.client.Window.confirm("Do you really want to remove the " + language + " language?"))
			return;
		
		Itext.removeLocale(language);
		
		cm.getColumns().remove(currentColumnIndex);
		grid.reconfigure(Itext.getItextRows(), cm);
	}

	public void renameLanguage(int curColIndex){
		String oldLanguage = cm.getColumnHeader(curColIndex);
		
		String newLanguage = com.google.gwt.user.client.Window.prompt("Please enter the new name", oldLanguage);
		
		if(newLanguage.equals(oldLanguage)) renameLanguage(curColIndex);
		
		if(newLanguage != null && newLanguage.trim().length() > 0){
			Itext.renameLocale(oldLanguage, newLanguage);
			cm.getColumns().get(currentColumnIndex).setHeader(newLanguage);
			grid.reconfigure(Itext.getItextRows(), cm);
		}
	}

	public void addNewRow(){
		Itext.getItextRows().add(new ItextModel());
		Itext.getItextRows().commitChanges();
		grid.reconfigure(Itext.getItextRows(), cm);
	}

	public void removeRow(){
		if(currentRowIndex < 0)
			return;

		String id = grid.getStore().getModels().get(currentRowIndex).get("id");
		
		if(!com.google.gwt.user.client.Window.confirm("Do you really want to remove the " + id + " row?"))
			return;
		

		Itext.removeRow(id);
		grid.reconfigure(Itext.getItextRows(), cm);
	}

	public void save(boolean showWindow){
		TextTabWidget.showWindow = showWindow;
		grid.getStore().commitChanges();
		hideWindow();
		GWT.log("TextTabWidget:388 Itext.getItextRows().len="+Itext.getItextRows().getCount());
		FormUtil.dlg.setText("Saving Itext...");
		FormUtil.dlg.show();

		DeferredCommand.addCommand(new Command() {
			public void execute() {
				try{
					listener.onSaveItext(Itext.getItextRows());
					grid.reconfigure(Itext.getItextRows(), createColumnModel()); //refresh everything
					FormUtil.dlg.hide();
					if(TextTabWidget.showWindow){
						showWindow();
					}
				}
				catch(Exception ex){
					FormUtil.displayException(ex);
				}
			}
		});
	}

	private boolean languageExists(String name){
		for(ItextLocale locale : Itext.locales){
			if(locale.getName().equals(name)){
				return true;
			}
		}
		return false;
	}

}
