package org.purc.purcforms.client.view;

import java.util.ArrayList;
import java.util.List;

import org.purc.purcforms.client.Context;
import org.purc.purcforms.client.Toolbar;
import org.purc.purcforms.client.controller.ICenterPanel;
import org.purc.purcforms.client.controller.SubmitListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.widget.RuntimeWidgetWrapper;
import org.purc.purcforms.client.xforms.XformBuilder;
import org.purc.purcforms.client.xforms.XformUtil;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;


/**
 * This widget is used to preview a form in the form designer.
 * 
 * @author daniel
 *
 */
public class PreviewView extends FormRunnerView {

	public interface Images extends FormRunnerView.Images,Toolbar.Images {
		ImageResource error();
	}

	/** Popup for displaying the context menu for the preview. */
	private PopupPanel popup;
	
	/** Reference to the design surface for getting layout xml during refresh. */
	private DesignSurfaceView designSurfaceView;
	
	/** Reference to the center panel for committing edit changes and getting the current form. */
	private ICenterPanel centerPanel;

	
	/**
	 * Creates a new instance of the preview widget.
	 * 
	 * @param images the images for the preview context menu.
	 */
	public PreviewView(Images images){
		//super(images);

		popup = new PopupPanel(true,true);
		MenuBar menuBar = new MenuBar(true);
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.refresh(),LocaleText.get("refresh")),true,new Command(){
			public void execute() {popup.hide(); refresh();}});

		menuBar.addSeparator();
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.save(),LocaleText.get("submit")),true,new Command(){
			public void execute() {popup.hide(); submit();}});

		popup.setWidget(menuBar);

		addNewTab(LocaleText.get("page")+"1");

		DOM.sinkEvents(getElement(),DOM.getEventsSunk(getElement()) | Event.ONMOUSEDOWN);

		//This is needed for IE
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				setHeight(getHeight());
			}
		});
	}

	//TODO These two should bind to interfaces.
	public void setDesignSurface(DesignSurfaceView designSurfaceView){
		this.designSurfaceView = designSurfaceView;
	}

	public void setCenterPanel(ICenterPanel centerPanel){
		this.centerPanel = centerPanel;
	}

	/**
	 * Sets up the preview widget.
	 */
	protected void initPanel(){
		AbsolutePanel panel = new AbsolutePanel();
		//FormDesignerUtil.maximizeWidget(panel);
		selectedPanel = panel;

		//This is needed for IE
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				setHeight(getHeight());
			}
		});
	}

	@Override
	protected void submit(){		
		if(formDef != null){
			if(formDef.getDoc() == null)
				XformBuilder.fromFormDef2Xform(formDef);

			saveValues();

			if(!isValid(false))
				return;

			String xml = XformUtil.getInstanceDataDoc(formDef.getDoc()).toString();
			xml = FormDesignerUtil.formatXml(xml); //"<?xml version='1.0' encoding='UTF-8' ?> " +
			submitListener.onSubmit(xml);
		}
	}
	

	/**
	 * Sets the listener for form submission events.
	 * 
	 * @param submitListener the listener.
	 */
	public void setSubmitListener(SubmitListener submitListener){
		this.submitListener = submitListener;
	}

	/**
	 * Checks if the preview surface has any widgets.
	 * 
	 * @return true if yes, else false.
	 */
	public boolean isPreviewing(){
		return tabs.getWidgetCount() > 0 && selectedPanel != null && selectedPanel.getWidgetCount() > 0;
	}

	@Override
	public void onBrowserEvent(Event event) {
		int type = DOM.eventGetType(event);

		switch (type) {
		case Event.ONMOUSEDOWN:
			if( (event.getButton() & Event.BUTTON_RIGHT) != 0){
				if(event.getTarget().getClassName().length() == 0){
					
					int ypos = event.getClientY();
					if(Window.getClientHeight() - ypos < 100)
						ypos = event.getClientY() - 100;
					
					int xpos = event.getClientX();
					if(Window.getClientWidth() - xpos < 110)
						xpos = event.getClientX() - 110;
					
					popup.setPopupPosition(xpos, ypos);
					popup.show();
					FormDesignerUtil.disableContextMenu(popup.getElement());
				}
			}
			break;
		}	
	}

	/**
	 * Reloads widgets on the preview surface.
	 */
	public void refresh(){
		FormUtil.dlg.setText(LocaleText.get("refreshingPreview"));
		FormUtil.dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {
				try{
					centerPanel.commitChanges();
					List<RuntimeWidgetWrapper> externalSourceWidgets = new ArrayList<RuntimeWidgetWrapper>();
					if(Context.isOfflineMode())
						;//externalSourceWidgets = null;
					loadForm(centerPanel.getFormDef(), designSurfaceView.getLayoutXml(),centerPanel.getJavaScriptSource(),externalSourceWidgets,true);
					FormUtil.dlg.hide();
				}
				catch(Exception ex){
					FormUtil.displayException(ex);
				}
			}
		});
	}

	/**
	 * Removes all widgets from the preview surface.
	 */
	public void clearPreview(){
		tabs.clear();
	}
}
