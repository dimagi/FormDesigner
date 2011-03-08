package org.openrosa.client.view;

import java.util.ArrayList;
import java.util.List;

import org.openrosa.client.controller.FormDesignerController;
import org.openrosa.client.controller.IFileListener;
import org.openrosa.client.model.FormDef;
import org.openrosa.client.controller.IFormSelectionListener;
import org.openrosa.client.util.FormUtil;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;


/**
 * This is the widget which we display in the design tab.
 * 
 * @author daniel
 *
 */
public class DesignTabWidget extends Composite implements IFormSelectionListener{
	
	/** The main widget for the form designer. */
	private ContentPanel panel;


	/**
	 * Widget used on the right hand side of the form designer. This widget has the
	 * Properties, Xforms Source, Design Surface, LayoutXml, Language Xml
	 * and Model Xml tabs.
	 */
	private CenterPanel centerPanel = new CenterPanel(FormDesignerWidget.images);

	/** 
	 * Widget used on the left hand side of the form designer to display a list
	 * of forms and their pages, questions, etc.
	 */
	private FormsTreeView formsTreeView = new FormsTreeView(centerPanel);

	/** The coordinator for execution of commands between the menu and tool bar, left and center panel. */
	private FormDesignerController controller = new FormDesignerController(centerPanel,formsTreeView);

	/** The splitter between the left and center panel. */


	/** Flag to tell whether we in in the resize mode of the splitter. */
	private boolean isResizing = false;
	
	/** List of form item selection listeners. */
	private List<IFormSelectionListener> formSelectionListeners = new ArrayList<IFormSelectionListener>();
	
	public DesignTabWidget(IFileListener fileListener){
		formsTreeView.showFormAsRoot(true);

		formsTreeView.setFormDesignerListener(controller);
		formsTreeView.addFormSelectionListener(this);
		formsTreeView.addFormSelectionListener(centerPanel.getFormSelectionListener());
		
		centerPanel.setFormActionListener(formsTreeView);
		
		initDesigner(fileListener);  
		centerPanel.setFormChangeListener(formsTreeView);
	}


	private void initDesigner(IFileListener fileListener){
		
		
	    panel = new ContentPanel();  
	    panel.setCollapsible(false);  
	    panel.setFrame(true);  
	    panel.setHeading("OpenRosa Form Designer");  
	    BorderLayout layout = new BorderLayout();
	    panel.setLayout(layout); 
	    panel.setBorders(false);
	    
		Toolbar toolbar = new Toolbar(controller,fileListener,this);
		controller.setToolbar(toolbar);
		//Context.addLocaleListChangeListener(toolbar);
		panel.setTopComponent(toolbar.getToolBar());
	    
	    BorderLayoutData leftData = new BorderLayoutData(LayoutRegion.WEST,300);  
	    leftData.setSplit(true);  
	    leftData.setCollapsible(true);  
	    leftData.setMargins(new Margins(0,5,10,0));
	    //panel.setScrollMode(Scroll.AUTOY);
	    ContentPanel cp = new ContentPanel();
	    cp.setHeading("Form Tree View");
		cp.add(formsTreeView);
		FlowPanel stackPanel = new FlowPanel();
		stackPanel.add(cp);
		stackPanel.addStyleName("myFormTreeView");
		FormUtil.maximizeWidget(stackPanel);
	    ScrollPanel scrollPanel = new ScrollPanel();
	    scrollPanel.setWidget(stackPanel);
	    panel.add(scrollPanel,leftData);
	    
	    
	    
	    BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER,300);  
	    centerData.setMargins(new Margins(0,0,10,0));  
	    centerData.setSplit(true); 
	    cp = new ContentPanel();
	    
	    cp.expand();
	    cp.setHeading("Properties View");
	    centerPanel.setWidth("100%");
	    centerPanel.setHeight("100%");
	    cp.add(centerPanel);
	    panel.add(cp,centerData);
	    
//	    panel.setSize(1500,768);
		panel.expand();
		layout.expand(LayoutRegion.CENTER);
		layout.expand(LayoutRegion.WEST);
		initWidget(panel);
		
		DOM.sinkEvents(getElement(),DOM.getEventsSunk(getElement()) | Event.MOUSEEVENTS);
/*		
 		panel = new VerticalPanel();
 
		hsplitClient = new HorizontalSplitPanel();
		hsplitClient.setLeftWidget(leftPanel);
		hsplitClient.setRightWidget(centerPanel);
		hsplitClient.setSplitPosition("25%");

		//VerticalPanel panel = new VerticalPanel();

		Toolbar toolbar = new Toolbar(FormDesignerWidget.images,controller);
		Context.addLocaleListChangeListener(toolbar);
		panel.add(toolbar);

		panel.add(hsplitClient);

		//dockPanel.add(panel, DockPanel.CENTER);

		//FormUtil.maximizeWidget(panel);
		//FormUtil.maximizeWidget(hsplitClient);

		initWidget(panel);
		
		DOM.sinkEvents(getElement(),DOM.getEventsSunk(getElement()) | Event.MOUSEEVENTS);
		*/
	}
	
	
	/**
	 * @see com.google.gwt.user.client.WindowResizeListener#onWindowResized(int, int)
	 */
	public void onWindowResized(int width, int height){		
		int shortcutHeight = height - formsTreeView.getAbsoluteTop() - 5;//8;
		if (shortcutHeight < 1) 
			shortcutHeight = 1;

		//leftPanel.setHeight(shortcutHeight - 5 + PurcConstants.UNITS);

		shortcutHeight = height - centerPanel.getAbsoluteTop() - 70;
		if(shortcutHeight > 100){
			//centerPanel.adjustHeight(shortcutHeight + PurcConstants.UNITS);
			//centerPanel.onWindowResized(width, height);
			//panel.setHeight(shortcutHeight+60+PurcConstants.UNITS);
			panel.setHeight(height);
		}
	}
	
	
//	@Override
//	public void onBrowserEvent(Event event) {
//		//TODO Firefox doesn't seem to give us mouse events when resizing.
//		if(isResizing)
//			centerPanel.onVerticalResize();
//
//		isResizing = false;
//		if(hsplitClient.isResizing()){
//			isResizing = true;
//			centerPanel.onVerticalResize();
//		}
//	}
	
	public void loadForm(FormDef formDef){
		formsTreeView.loadForm(formDef, true, false);
	}
	
	public void refreshForm(FormDef formDef){
		formsTreeView.refreshForm(formDef);
	}
	
	public void addNewForm(){
		formsTreeView.addNewForm();
	}
	
	public void commitChanges(){
		
		centerPanel.commitChanges();
	}
	
	/**
	 * Adds a listener to form item selection events.
	 * 
	 * @param formSelectionListener the listener to add.
	 */
	public void addFormSelectionListener(IFormSelectionListener formSelectionListener){
		this.formSelectionListeners.add(formSelectionListener);
	}
	
	public void onFormItemSelected(Object formItem){
//		for(int i=0; i<formSelectionListeners.size(); i++)
//			formSelectionListeners.get(i).onFormItemSelected(formItem);
	}
}
