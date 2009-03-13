package org.purc.purcforms.client;

import org.purc.purcforms.client.controller.FormDesignerController;
import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.view.PreviewView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * The XForms designer widget.
 * 
 * @author daniel
 *
 */
public class FormDesignerWidget extends Composite{

	private DockPanel dockPanel;

	/**
	 * Instantiate an application-level image bundle. This object will provide
	 * programmatic access to all the images needed by widgets.
	 */
	private static final Images images = (Images) GWT.create(Images.class);

	/**
	 * An aggragate image bundle that pulls together all the images for this
	 * application into a single bundle.
	 */
	public interface Images extends LeftPanel.Images,Toolbar.Images,Menu.Images,PreviewView.Images {}


	private CenterPanel centerPanel = new CenterPanel(images);
	private LeftPanel leftPanel = new LeftPanel(images,centerPanel);
	private FormDesignerController controller = new FormDesignerController(centerPanel,leftPanel);

	private Menu menu = new Menu(images,controller);
	private Toolbar toolbar = new Toolbar(images,controller);
	private HorizontalSplitPanel hsplitClient;
	private boolean showMenubar;
	private boolean showToolbar;
	//private boolean showFormAsRoot;


	public FormDesignerWidget(boolean showMenubar, boolean showToolbar,boolean showFormAsRoot){
		this.showMenubar = showMenubar;
		this.showToolbar =  showToolbar;
		leftPanel.showFormAsRoot(showFormAsRoot);
		centerPanel.setWidgetSelectionListener(leftPanel.getWidgetSelectionListener());
		leftPanel.setFormDesignerListener(controller);
		
		initDesigner();  
		centerPanel.setFormChangeListener(leftPanel.getFormChangeListener());
	}

	private void initDesigner(){
		dockPanel = new DockPanel();

		hsplitClient = new HorizontalSplitPanel();
		hsplitClient.setLeftWidget(leftPanel);
		hsplitClient.setRightWidget(centerPanel);
		hsplitClient.setSplitPosition("25%");
		
		VerticalPanel panel = new VerticalPanel();
		if(showMenubar)
			panel.add(menu);
		if(showToolbar)
			panel.add(toolbar);
		panel.add(hsplitClient);
		panel.setWidth("100%");

		dockPanel.add(panel, DockPanel.CENTER);
		FormDesignerUtil.maximizeWidget(dockPanel);
		FormDesignerUtil.maximizeWidget(hsplitClient);

		initWidget(dockPanel);
	}

	public void onWindowResized(int width, int height){		
		int shortcutHeight = height - leftPanel.getAbsoluteTop() - 0;//8;
		if (shortcutHeight < 1) 
			shortcutHeight = 1;

		leftPanel.setHeight(shortcutHeight + "px");

		shortcutHeight = height - centerPanel.getAbsoluteTop();
		centerPanel.adjustHeight(shortcutHeight-50 + "px");
		centerPanel.onWindowResized(width, height);
		hsplitClient.setHeight(shortcutHeight+"px");
		//hsplitClient.setSize(width+"px", shortcutHeight+"px");
	}
	
	public void loadForm(int formId){
		if(formId != -1)
			controller.loadForm(formId);
	}
	
	//These are for external users of this widget
	public void setEmbeddedHeightOffset(int offset){
		centerPanel.setEmbeddedHeightOffset(offset);
	}
	
	public void loadForm(String xform, String layout){
		centerPanel.setXformsSource(xform, false);
		centerPanel.setLayoutXml(layout, false);
		controller.openFormDeffered();
	}
	
	public void clear(){
		leftPanel.clear();
	}
}
