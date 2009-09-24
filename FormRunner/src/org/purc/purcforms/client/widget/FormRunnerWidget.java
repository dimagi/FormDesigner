package org.purc.purcforms.client.widget;

import java.util.List;

import org.purc.purcforms.client.controller.FormRunnerController;
import org.purc.purcforms.client.controller.SubmitListener;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.view.FormRunnerView;
import org.purc.purcforms.client.view.FormRunnerView.Images;
import org.purc.purcforms.client.xforms.XformConverter;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventPreview;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;



/**
 * This is the widget for the form runtime engine.
 * 
 * @author daniel
 *
 */
public class FormRunnerWidget extends Composite{

	private DockPanel dockPanel = new DockPanel();
	private FormRunnerController controller;
	private FormRunnerView view;
	
	public FormRunnerWidget(Images images){
		
		view = new FormRunnerView(images);
		
		dockPanel.add(view, DockPanel.CENTER);
		
		FormUtil.maximizeWidget(dockPanel);

		initWidget(dockPanel);
		
		controller = new FormRunnerController(this);
		view.setSubmitListener(controller);
		
		//process key board events and pick Ctrl + S for saving.
		previewEvents();
	}
	
	public void loadForm(FormDef formDef,String layoutXml, List<RuntimeWidgetWrapper> externalSourceWidgets){
		view.loadForm(formDef, layoutXml,externalSourceWidgets);
	}
	
	public void loadForm(int formId, int entityId){
		controller.loadForm(formId,entityId);
	}
	
	public void loadForm(String xformXml, String layoutXml){
		view.loadForm(XformConverter.fromXform2FormDef(xformXml), layoutXml, null);
	}
	
	public void loadForm(int formId, String xformXml, String modelXml,String layoutXml){
		FormDef formDef = XformConverter.fromXform2FormDef(xformXml,modelXml);
		formDef.setId(formId);
		view.loadForm(formDef, layoutXml, null);
	}
	
	public void setEmbeddedHeightOffset(int offset){
		view.setEmbeddedHeightOffset(offset);
	}
	
	public void setSubmitListener(SubmitListener submitListener){
		view.setSubmitListener(submitListener);
	}
	
	private void previewEvents(){
		DOM.addEventPreview(new EventPreview() { 
			public boolean onEventPreview(Event event) 
			{ 				
				if (DOM.eventGetType(event) == Event.ONKEYDOWN)
					return view.handleKeyBoardEvent(event);
				
				return true;
			}
		});
	}
}
