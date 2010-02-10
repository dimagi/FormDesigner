package org.openrosa.client.view;

import org.purc.purcforms.client.util.FormUtil;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.Label;


/**
 * 
 * @author daniel
 *
 */
public class CenterWidget extends Composite {

	/**
	 * Tab widget housing the contents.
	 */
	private DecoratedTabPanel tabs = new DecoratedTabPanel();
	
	
	public CenterWidget() {
		initDesignTab();
		initXformsTab();
		initItextTab();

		FormUtil.maximizeWidget(tabs);

		tabs.selectTab(0);
		initWidget(tabs);
		//tabs.addSelectionHandler(this);
		
		FormUtil.maximizeWidget(this);
	}
	
	private void initDesignTab(){
		tabs.add(new Label(), "Design");
		//FormUtil.maximizeWidget(txtLayoutXml);
	}
	
	private void initXformsTab(){
		tabs.add(new XformsWidget(), "Xforms");
		//FormUtil.maximizeWidget(txtLayoutXml);
	}
	
	private void initItextTab(){
		tabs.add(new Label(), "Internationalization");
		//FormUtil.maximizeWidget(txtLayoutXml);
	}
}
