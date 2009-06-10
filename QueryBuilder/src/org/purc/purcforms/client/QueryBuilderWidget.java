package org.purc.purcforms.client;

import org.purc.purcforms.client.controller.QueryBuilderController;
import org.purc.purcforms.client.view.QueryBuilderView;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;


/**
 * 
 * @author daniel
 *
 */
public class QueryBuilderWidget extends Composite{

	private DockPanel dockPanel = new DockPanel();
	private QueryBuilderController controller;
	private QueryBuilderView view = new QueryBuilderView();
	
	public QueryBuilderWidget(){
		
		//view = new FormRunnerView(images);
		dockPanel.setWidth("100%");
		dockPanel.setHeight("100%");
		
		dockPanel.add(view, DockPanel.CENTER);
		
		//FormUtil.maximizeWidget(dockPanel);

		initWidget(dockPanel);
		
		controller = new QueryBuilderController();
	}
	
	public void setEmbeddedHeightOffset(int offset){
		//view.setEmbeddedHeightOffset(offset);
	}
}
