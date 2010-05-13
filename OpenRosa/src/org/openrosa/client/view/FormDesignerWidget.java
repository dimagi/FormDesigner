package org.openrosa.client.view;

import org.purc.purcforms.client.util.FormUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * The form designer widget.
 * 
 * It is composed of a GWT DockPanel as the main widget. This panel contains a 
 * GWT vertical panel which in turn contains a custom Menu widget, custom Tool bar widget, 
 * and a GWT HorizontalSplitPanel. The HorizontalSplitPanel contains a custom LeftPanel widget
 * on the left and a custom CenterPanel widget on the right.
 * When embedded in a GWT application, the embedding application can have custom tool bars and 
 * menu bars, instead of the default ones, which can have commands routed to this widget.
 * 
 * @author daniel
 *
 */
public class FormDesignerWidget extends Composite {

	/**
	 * Instantiate an application-level image bundle. This object will provide
	 * programatic access to all the images needed by widgets.
	 */
	public static final Images images = (Images) GWT.create(Images.class);

	/**
	 * An aggragate image bundle that pulls together all the images for this
	 * application into a single bundle.
	 */
	public interface Images extends LeftPanel.Images {}

	private DockPanel dockPanel;
	
	private CenterWidget centerWidget = new CenterWidget();


	public FormDesignerWidget(boolean showToolbar,boolean showFormAsRoot){
		initDesigner();  
	}

	/**
	 * Builds the form designer and all its widgets.
	 * 
	 * @param showMenubar set to true to show the menu bar.
	 * @param showToolbar set to true to show the tool bar.
	 */
	private void initDesigner(){
		dockPanel = new DockPanel();

		VerticalPanel panel = new VerticalPanel();

//		panel.add(new FileToolbar(centerWidget));
		panel.add(centerWidget);
		
		panel.setWidth("100%");

		dockPanel.add(panel, DockPanel.CENTER);

		FormUtil.maximizeWidget(dockPanel);

		initWidget(dockPanel);

		DOM.sinkEvents(getElement(),DOM.getEventsSunk(getElement()) | Event.MOUSEEVENTS);
	}

	/**
	 * @see com.google.gwt.user.client.WindowResizeListener#onWindowResized(int, int)
	 */
	public void onWindowResized(int width, int height){		
		centerWidget.onWindowResized(width, height);
	}
	
}