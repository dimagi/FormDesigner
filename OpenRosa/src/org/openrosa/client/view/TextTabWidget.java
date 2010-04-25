package org.openrosa.client.view;

import java.util.ArrayList;
import java.util.List;

import org.openrosa.client.model.ItextModel;
import org.purc.purcforms.client.Context;
import org.purc.purcforms.client.model.Locale;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;


/**
 * The widget for the internationalization tab.
 * 
 * @author daniel
 *
 */
public class TextTabWidget extends com.extjs.gxt.ui.client.widget.Composite {

	private EditorGrid<ItextModel> grid;
	private ContentPanel contentPanel = new ContentPanel();
	

	public TextTabWidget(){
		grid = new EditorGrid<ItextModel>(new ListStore<ItextModel>(), getColumnModel());
		grid.setBorders(true);
		grid.setStripeRows(true);

		contentPanel.setHeaderVisible(false);
		contentPanel.setLayout(new FitLayout());
		contentPanel.add(grid);

		initComponent(contentPanel);
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
	}
	
	public List<ItextModel> getItext(){
		grid.getStore().commitChanges();
		return grid.getStore().getModels();
	}

	public void adjustHeight(String height){
		contentPanel.setHeight(height);
	}
}
