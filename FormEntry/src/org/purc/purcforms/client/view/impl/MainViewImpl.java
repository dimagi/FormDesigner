package org.purc.purcforms.client.view.impl;


import java.util.ArrayList;
import java.util.List;

import org.purc.purcforms.client.FormDesignerWidget;
import org.purc.purcforms.client.FormEntryContext;
import org.purc.purcforms.client.FormRunnerEntryPoint;
import org.purc.purcforms.client.cmd.FormDesignerCmd;
import org.purc.purcforms.client.cmd.FormRunnerCmd;
import org.purc.purcforms.client.cmd.InitDataLoadCmd;
import org.purc.purcforms.client.controller.FormDataController;
import org.purc.purcforms.client.listener.FormDataListLoadListener;
import org.purc.purcforms.client.listener.FormDataLoadListener;
import org.purc.purcforms.client.listener.FormDefListChangeListener;
import org.purc.purcforms.client.listener.FormDefLoadListener;
import org.purc.purcforms.client.listener.FormSubmitCancelListener;
import org.purc.purcforms.client.model.FormDataHeader;
import org.purc.purcforms.client.model.KeyValue;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.view.FormRunnerView.Images;
import org.purc.purcforms.client.widget.FormRunnerWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * 
 * @author daniel
 *
 */
public class MainViewImpl extends Composite implements FormDefLoadListener, FormDataLoadListener, FormDataListLoadListener, ResizeHandler, FormSubmitCancelListener, FormDefListChangeListener{

	private static final Images images = (Images) GWT.create(Images.class);

	private VerticalPanel verticalPanel = new VerticalPanel();
	private FormDesignerWidget formDesigner;
	private FormRunnerWidget formRunner;
	private DataListViewImpl dataListView;
	

	public MainViewImpl(){

		verticalPanel.add(new ToolbarViewImpl());
		initWidget(verticalPanel);

		setWidth("100%");
		
		Window.addResizeHandler(this);
		
		FormEntryContext.addFormDefLoadListener(this);
		FormEntryContext.addFormDataListLoadListener(this);
		FormEntryContext.addFormDataLoadListener(this);
		FormEntryContext.addFormDefListChangeListener(this);
		
		new InitDataLoadCmd();
	}

	public void onFormDefLoaded(final boolean designForm, final String xformXml, final String layoutXml, final String javaScriptSrc){
		if(designForm){
			if(verticalPanel.getWidgetCount() > 1){
				if(verticalPanel.getWidget(1) != formDesigner)
					verticalPanel.remove(1);
			}
			
			loadFormDesigner(xformXml, layoutXml, javaScriptSrc);
		}
		else{
			if(layoutXml == null){
				FormUtil.dlg.hide();
				Window.alert("Please first design the form widget layout by clicking the 'Design' button, go to the 'Design Surface' tab, and then save");
				return;
			}
			
			if(verticalPanel.getWidgetCount() > 1){
				if(verticalPanel.getWidget(1) != formRunner)
					verticalPanel.remove(1);
			}
			
			loadFormRunner(xformXml, layoutXml, javaScriptSrc, null);
		}
	}


	private void loadFormDesigner(final String xformXml, final String layoutXml, final String javaScriptSrc){
		if(formDesigner == null){
			formDesigner = new FormDesignerWidget(true, true, true);
			verticalPanel.add(formDesigner);
			FormUtil.maximizeWidget(formDesigner);

			new FormDesignerCmd(formDesigner);
		}
		else{
			if(verticalPanel.getWidgetCount() == 1)
				verticalPanel.add(formDesigner);
			
			formDesigner.clear();
		}

		// Call the window resized handler to get the initial sizes setup. Doing
		// this in a deferred command causes it to occur after all widgets' sizes
		// have been computed by the browser.
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				formDesigner.onWindowResized(Window.getClientWidth(), Window.getClientHeight());				
				formDesigner.loadForm(1, xformXml, layoutXml, javaScriptSrc, false);
			}
		});
	}


	private void loadFormRunner(final String xformXml, final String layoutXml, final String javaScriptSrc, final String modelXml){
		if(formRunner == null){
			formRunner = new FormRunnerWidget(images);
			verticalPanel.add(formRunner);
			FormUtil.maximizeWidget(formRunner);

			new FormRunnerCmd(formRunner, this);
			
			//Replace the form designer's authentication call back with that of the form runner.
			FormRunnerEntryPoint.registerAuthenticationCallback();
		}
		else{
			if(verticalPanel.getWidgetCount() == 1)
				verticalPanel.add(formRunner);
			
			formRunner.clear();
		}

		// Call the window resized handler to get the initial sizes setup. Doing
		// this in a deferred command causes it to occur after all widgets' sizes
		// have been computed by the browser.
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				if(modelXml == null)				
					formRunner.loadForm(xformXml, layoutXml, javaScriptSrc);
				else
					formRunner.loadForm(1, xformXml, modelXml, layoutXml, javaScriptSrc);
				
				FormUtil.dlg.hide();
			}
		});		
	}
	
	public void onFormDataListLoaded(List<FormDataHeader> dataList){
		if(verticalPanel.getWidgetCount() > 1){
			if(verticalPanel.getWidget(1) != dataListView)
				verticalPanel.remove(1);
		}
		
		if(dataListView == null){
			dataListView = new DataListViewImpl(new FormDataController());
			verticalPanel.add(dataListView);
			FormUtil.maximizeWidget(dataListView);
		}
		else{
			if(verticalPanel.getWidgetCount() == 1)
				verticalPanel.add(dataListView);
		}

		dataListView.setDataList(dataList);
	}
	
	
	public void onFormDataLoaded(String xformXml, String layoutXml, String javaScriptSrc, String modelXml){
		if(verticalPanel.getWidgetCount() > 1){
			if(verticalPanel.getWidget(1) != formRunner)
				verticalPanel.remove(1);
		}
		
		loadFormRunner(xformXml, layoutXml, javaScriptSrc, modelXml);
	}
	

	public void onResize(ResizeEvent event) {
		if(formDesigner != null)
			formDesigner.onWindowResized(event.getWidth(), event.getHeight());

		//if(formRunner != null)
		//	formRunner.onWindowResized(event.getWidth(), event.getHeight());
	}

	private void clear(){
		while(verticalPanel.getWidgetCount() > 1)
			verticalPanel.remove(1);
	}
	
	public void onFormCancelled(){
		clear();
		
		if(dataListView != null)
			verticalPanel.add(dataListView);
	}
	
	public void onNewFormSubmitted(FormDataHeader formDataHeader){
		clear();
		
		if(dataListView == null){
			List<FormDataHeader> dataList = new ArrayList<FormDataHeader>();
			dataList.add(formDataHeader);
			onFormDataListLoaded(dataList);
		}
		else{
			verticalPanel.add(dataListView);
			dataListView.addFormData(formDataHeader);
		}
	}
	
	public void onExistingFormSubmitted(FormDataHeader formDataHeader){
		clear();
		
		assert(dataListView != null);

		verticalPanel.add(dataListView);
		dataListView.setFormData(formDataHeader);
	}
	
	public void onFormDefListChanged(List<KeyValue> formList){
		clear();
	}
}
