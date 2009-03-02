package org.purc.purcforms.client.widget;

import java.util.List;

import org.purc.purcforms.client.controller.FormRunnerController;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.view.FormRunnerView;
import org.purc.purcforms.client.view.FormRunnerView.Images;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;



/**
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
	}
	
	public void loadForm(FormDef formDef,String layoutXml, List<RuntimeWidgetWrapper> externalSourceWidgets){
		view.loadForm(formDef, layoutXml,externalSourceWidgets);
	}
	
	public void loadForm(int formId, int patientId){
		controller.loadForm(formId,patientId);
	}
}
