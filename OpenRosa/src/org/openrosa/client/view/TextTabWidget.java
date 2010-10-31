package org.openrosa.client.view;

import java.util.ArrayList;
import java.util.List;

import org.openrosa.client.Context;
import org.openrosa.client.controller.ITextListener;
import org.openrosa.client.model.ItextModel;
import org.purc.purcforms.client.model.Locale;
import org.purc.purcforms.client.util.FormUtil;

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
 * @author daniel
 *
 */
public class TextTabWidget extends com.extjs.gxt.ui.client.widget.Composite {

	private EditorGrid<ItextModel> grid;
	private ContentPanel contentPanel = new ContentPanel();
	private Window window = new Window();

	private ListStore<ItextModel> store;
	private ColumnModel cm;
	private int currentColumnIndex = 0;
	private int currentRowIndex = 0;

	private ITextListener listener;


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
		window.setHeading("Xform Source");  
		grid = new EditorGrid<ItextModel>(new ListStore<ItextModel>(), getColumnModel());
		grid.setBorders(true);
		grid.setStripeRows(true);
		grid.setWidth(700);
		contentPanel.setHeaderVisible(false);
		contentPanel.setLayout(new FitLayout());
		contentPanel.add(grid);
		contentPanel.setWidth(700);

		makeToolbar();
		makeContextMenu();

		//		initComponent(contentPanel);
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

		//		menuBut.addSelectionListener(new SelectionListener<ButtonEvent>() {
		//			public void componentSelected(ButtonEvent ce) {
		//				menuBut.showMenu();
		//			}
		//		});

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
							renameLanguage();
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
		//		group.addButton(addLang);
		//		group.add(new SeparatorToolItem());
		//		group.addButton(removeLang);
		//		group.setHeading("Language Actions");
		//		tb.add(group);
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
				save();
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
				removeActiveColumnLang();
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

				//		        ModelData folder = grid.getSelectionModel().getSelectedItem();  
				//		        if (folder != null) {  
				//		          Folde child = new Folder("Add Child " + count++);  
				//		          store.add(folder, child, false);  
				//		          tree.setExpanded(folder, true);  
				//		        }

				//TODO
				//NEED TO PUT HOOK TO addLanguage() here!

				addNewLanguage("Language");
			}
		});  
		contextMenu.add(addLang);  

		MenuItem removeLang = new MenuItem();  
		removeLang.setText("Remove Language");  
		removeLang.setIcon(AbstractImagePrototype.create(images.smallRemove()));  
		removeLang.addSelectionListener(new SelectionListener<MenuEvent>() {  
			public void componentSelected(MenuEvent ce) {  

				//		        List<ModelData> selected = tree.getSelectionModel().getSelectedItems();  
				//		        for (ModelData sel : selected) {  
				//		          store.remove(sel);  
				//		        }  

				//TODO
				//NEED TO PUT HOOK to removeLanguage() here

				//removeLanguage();

				removeActiveColumnLang();
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

	private void removeActiveColumnLang(){		
		if(cm.getColumnCount() > 3)
			removeLanguage();
	}


	public void showWindow(){
		window.show();
	}

	public void hideWindow(){
		window.hide();
	}

	private ColumnModel getColumnModel(){
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

		List<Locale> locales = Context.getLocales();
		if(locales != null){
			for(Locale locale : locales){
				ColumnConfig columnConfig = new ColumnConfig(locale.getName(), locale.getName(), 200); //getKey()??????
				configs.add(columnConfig);
				columnConfig.setEditor(new CellEditor(new TextField<String>()));
				columnConfig.setStyle("font-size: 20px");
			}
		}

		return new ColumnModel(configs);
	}

	public void loadItext(ListStore<ItextModel> list){
		store = list;
		cm = getColumnModel();
		grid.reconfigure(store, cm);

		//setupContextMenu();

		//		MenuItem closeMenuItem = new MenuItem();
		//		closeMenuItem.setText("Close");
		//		closeMenuItem.addSelectionListener(new SelectionListener<MenuEvent>(){
		//			public void componentSelected(MenuEvent ce){
		//		                //this.close();
		//			}
		//		});
		//		
		//		final Menu contextMenu = new Menu();
		//		contextMenu.add(closeMenuItem);
		//		
		//		grid.getView().getHeader().addListener(Events.OnMouseUp, new Listener<ComponentEvent>(){
		//		    public void handleEvent(ComponentEvent event){
		//		        if(event.isRightClick()){
		//		            event.stopEvent();
		//		            contextMenu.showAt(event.getClientX(), event.getClientY());
		//		        }
		//		    }
		//		});
	}

	public List<ItextModel> getItext(){
		grid.getStore().commitChanges();
		return grid.getStore().getModels();
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

			String id = cm.getColumnId(2);
			for(ItextModel model : store.getModels())
				model.set(lang, model.get(id));

			grid.reconfigure(store, cm);

			Context.getLocales().add(new Locale(lang,lang));
			Context.setLocales(Context.getLocales()); //for locale change notification
		}
	}  

	public void removeLanguage(){
		String language = cm.getColumnHeader(currentColumnIndex);
		if(!com.google.gwt.user.client.Window.confirm("Do you really want to remove the " + language + " language?"))
			return;

		for(ItextModel model : store.getModels())
			model.remove(language);

		cm.getColumns().remove(currentColumnIndex);
		grid.reconfigure(store, cm);

		List<Locale> locales = Context.getLocales();
		for(Locale locale : locales){
			if(locale.getName().equals(language)){
				locales.remove(locale);
				break;
			}
		}

		Context.setLocales(locales);
	}

	public void renameLanguage(){
		String oldLanguage = cm.getColumnHeader(currentColumnIndex);

		String newLanguage = com.google.gwt.user.client.Window.prompt("Please enter the new name", oldLanguage);
		if(newLanguage != null && newLanguage.trim().length() > 0){

			List<ItextModel> models = store.getModels();
			for(ItextModel model : models){
				model.set(newLanguage, model.get(oldLanguage));
				//model.remove(oldLanguage);
			}

			cm.getColumns().get(currentColumnIndex).setHeader(newLanguage);
			grid.reconfigure(store, cm);

			List<Locale> locales = Context.getLocales();
			for(Locale locale : locales){
				if(locale.getName().equals(oldLanguage)){
					locale.setName(newLanguage);
					locale.setKey(newLanguage);
					break;
				}
			}

			Context.setLocales(Context.getLocales()); //for locale change notification
		}
	}

	public void addNewRow(){
		grid.getStore().add(new ItextModel());
		grid.reconfigure(store, cm);
	}

	public void removeRow(){
		if(currentRowIndex < 0)
			return;

		String id = store.getModels().get(currentRowIndex).get("id");
		if(!com.google.gwt.user.client.Window.confirm("Do you really want to remove the " + id + " row?"))
			return;

		ItextModel model = store.getModels().get(currentRowIndex);
		grid.getStore().remove(model);
		grid.reconfigure(store, cm);
	}

	public void save(){
		
		hideWindow();
		
		FormUtil.dlg.setText("Saving Itext...");
		FormUtil.dlg.show();

		DeferredCommand.addCommand(new Command() {
			public void execute() {
				try{
					listener.onSaveItext(getItext());
					FormUtil.dlg.hide();
					showWindow();
					//com.google.gwt.user.client.Window.alert("Saved Successfully");
				}
				catch(Exception ex){
					FormUtil.displayException(ex);
				}
			}
		});
	}

	private boolean languageExists(String name){
		List<Locale> locales = Context.getLocales();
		for(Locale locale : locales){
			if(locale.getName().equals(name)){
				return true;
			}
		}

		return false;
	}
}
