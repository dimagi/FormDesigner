package org.openrosa.client.view;

import java.util.ArrayList;
import java.util.List;

import org.purc.purcforms.client.model.ItextModel;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.HeaderGroupConfig;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * 
 * @author daniel
 *
 */
public class TextTabWidget extends Composite {

	private Grid<ItextModel> grid;
	private VerticalPanel panel = new VerticalPanel();
	
	
	public TextTabWidget(){
		// create column config - defines the column in the grid
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        // create audit fields
        ColumnConfig formId = new ColumnConfig("id", "aaaaa", 50);
        configs.add(formId);

        ColumnConfig date = new ColumnConfig("default", "bbbb", 125);
        date.setSortable(false); // FIXME: has to be like this because the column doesn't exist yet in the table (we must add it)
        configs.add(date);

        configs.add(new ColumnConfig("english", "ccc", 60));

        
        ColumnModel columnModel = new ColumnModel(configs);
        columnModel.addHeaderGroup(0, 0, new HeaderGroupConfig("aaaa", 1, 3));
        columnModel.addHeaderGroup(0, 3, new HeaderGroupConfig("bbb", 1, 1));

		grid = new Grid<ItextModel>(new ListStore<ItextModel>(), columnModel);
		initWidget(grid);
	}
}
