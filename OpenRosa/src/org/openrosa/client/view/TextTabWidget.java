package org.openrosa.client.view;

import java.util.ArrayList;
import java.util.List;

import org.purc.purcforms.client.Context;
import org.purc.purcforms.client.model.ItextModel;
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
	ContentPanel contentPanel = new ContentPanel();


	public TextTabWidget(){
		// create column config - defines the column in the grid
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		// create audit fields
		ColumnConfig formId = new ColumnConfig("id", "Id", 50);
		configs.add(formId);
		TextField<String> text = new TextField<String>();
		formId.setEditor(new CellEditor(text));

		ColumnConfig def = new ColumnConfig("default", "Default", 125);
        configs.add(def);
        def.setEditor(new CellEditor(new TextField<String>()));

		List<Locale> locales = Context.getLocales();
		if(locales != null){
			for(Locale locale : locales){
				ColumnConfig columnConfig = new ColumnConfig(locale.getKey(), locale.getName(), 200);
				configs.add(columnConfig);
				columnConfig.setEditor(new CellEditor(new TextField<String>()));
			}
		}

		ColumnModel columnModel = new ColumnModel(configs);

		ListStore<ItextModel> list = new ListStore<ItextModel>();
		list.add(new ItextModel("1","Test1","Test6"));
		list.add(new ItextModel("1","Test2","Test7"));
		list.add(new ItextModel("1","Test3","Test8"));
		list.add(new ItextModel("1","Test4","Test9"));
		list.add(new ItextModel("1","Test5","Test10"));

		grid = new EditorGrid<ItextModel>(list, columnModel);
		grid.setBorders(true);
		grid.setStripeRows(true);
		//grid.getView().setForceFit(true);
		//grid.getView().setAutoFill(true);       

		contentPanel.setHeaderVisible(false);
		contentPanel.setLayout(new FitLayout());
		contentPanel.add(grid);

		initComponent(contentPanel);
	}

	public void adjustHeight(String height){
		contentPanel.setHeight(height);
	}
}
