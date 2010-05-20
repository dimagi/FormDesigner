package org.openrosa.client.view;

import java.util.ArrayList;
import java.util.List;

import org.openrosa.client.model.ItextModel;
import org.purc.purcforms.client.Context;
import org.purc.purcforms.client.model.Locale;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;


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
	

	public TextTabWidget(){
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
		
//		initComponent(contentPanel);
		window.add(contentPanel);
		window.setWidth(700);
		window.setMinHeight(400);
		window.setMinWidth(400);
		
		
		grid.addListener(Events.HeaderContextMenu, new Listener<GridEvent<ModelData>>()
				{
					public void handleEvent(final GridEvent<ModelData> ge)
					{
						// Add a Menu Item
						final MenuItem menuItem = new MenuItem("New Menu Item");
						menuItem.addListener(Events.Select, new Listener<BaseEvent>()
						{
							public void handleEvent(final BaseEvent be)
							{
								
							}
						});
						ge.getMenu().add(menuItem);
					}
				});
		
//		menuBut.addSelectionListener(new SelectionListener<ButtonEvent>() {
//			public void componentSelected(ButtonEvent ce) {
//				menuBut.showMenu();
//			}
//		});
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
				ColumnConfig columnConfig = new ColumnConfig(locale.getKey(), locale.getName(), 200);
				configs.add(columnConfig);
				columnConfig.setEditor(new CellEditor(new TextField<String>()));
				columnConfig.setStyle("font-size: 20px");
			}
		}

		return new ColumnModel(configs);
	}
	
	public void loadItext(ListStore<ItextModel> list){
		grid.reconfigure(list, getColumnModel());
		
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
}
