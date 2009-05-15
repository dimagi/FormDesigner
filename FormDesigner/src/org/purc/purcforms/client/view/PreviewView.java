package org.purc.purcforms.client.view;

import org.purc.purcforms.client.CenterPanel;
import org.purc.purcforms.client.Toolbar;
import org.purc.purcforms.client.controller.SubmitListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.xforms.XformConverter;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;


/**
 * Preview a form.
 * 
 * @author daniel
 *
 */
public class PreviewView extends FormRunnerView {

	public interface Images extends FormRunnerView.Images,Toolbar.Images {
		AbstractImagePrototype error();
	}

	private PopupPanel popup;
	private DesignSurfaceView designSurfaceView;
	private CenterPanel centerPanel;

	public PreviewView(Images images){
		super(images);

		popup = new PopupPanel(true,true);
		MenuBar menuBar = new MenuBar(true);
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.loading(),LocaleText.get("refresh")),true,new Command(){
			public void execute() {popup.hide(); refresh();}});

		menuBar.addSeparator();
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.save(),LocaleText.get("submit")),true,new Command(){
			public void execute() {popup.hide(); submit();}});

		popup.setWidget(menuBar);

		addNewTab("Page1");
		
		DOM.sinkEvents(getElement(),DOM.getEventsSunk(getElement()) | Event.ONMOUSEDOWN);

		Window.addWindowResizeListener(this);
		
//		This is needed for IE
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				onWindowResized(Window.getClientWidth(), Window.getClientHeight());
			}
		});
	}
	
	//TODO These two should bind to interfaces.
	public void setDesignSurface(DesignSurfaceView designSurfaceView){
		this.designSurfaceView = designSurfaceView;
	}

	public void setCenterPanel(CenterPanel centerPanel){
		this.centerPanel = centerPanel;
	}

	protected void initPanel(){
		AbsolutePanel panel = new AbsolutePanel();
		FormDesignerUtil.maximizeWidget(panel);
		selectedPanel = panel;

		//This is needed for IE
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				onWindowResized(Window.getClientWidth(), Window.getClientHeight());
			}
		});
	}
	
	protected void submit(){
		if(formDef != null){
			if(formDef.getDoc() == null)
				XformConverter.fromFormDef2Xform(formDef);
			
			saveValues();

			if(!isValid())
				return;

			String xml = XformConverter.getInstanceDataDoc(formDef.getDoc()).toString();
			xml = FormDesignerUtil.formatXml("<?xml version='1.0' encoding='UTF-8' ?> " + xml);
			submitListener.onSubmit(xml);
		}
	}

	public void onWindowResized(int width, int height) {
		//height -= 160;
		height -= (160+embeddedHeightOffset);
		sHeight = height+"px";
		super.setHeight(sHeight);
		
		for(int index=0; index<tabs.getWidgetCount(); index++)
			tabs.getWidget(index).setHeight(sHeight);
	} 

	public void setSubmitListener(SubmitListener submitListener){
		this.submitListener = submitListener;
	}

	public boolean isPreviewing(){
		return tabs.getWidgetCount() > 0;
	}

	public void onBrowserEvent(Event event) {
		int type = DOM.eventGetType(event);

		switch (type) {
		case Event.ONMOUSEDOWN:
			if( (event.getButton() & Event.BUTTON_RIGHT) != 0){
				popup.setPopupPosition(event.getClientX(), event.getClientY());
				popup.show();
				FormDesignerUtil.disableContextMenu(popup.getElement());
			}
			break;
		}	
	}

	public void refresh(){
		FormUtil.dlg.setText(LocaleText.get("refreshingPreview"));
		FormUtil.dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {
				try{
					centerPanel.commitChanges();
					loadForm(formDef, designSurfaceView.getLayoutXml(),null);
					FormUtil.dlg.hide();
				}
				catch(Exception ex){
					FormUtil.dlg.hide();
					FormUtil.displayException(ex);
				}
			}
		});
	}
	
	public void clearPreview(){
		tabs.clear();
	}
}
