package org.purc.purcforms.client;

import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.view.FormRunnerView.Images;
import org.purc.purcforms.client.widget.FormRunnerWidget;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class FormRunnerEntryPoint implements EntryPoint{

	private FormRunnerWidget formRunner;
	
	/**
	 * Instantiate an application-level image bundle. This object will provide
	 * programmatic access to all the images needed by widgets.
	 */
	private static final Images images = (Images) GWT.create(Images.class);
	
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		FormUtil.setupUncaughtExceptionHandler();	

		FormUtil.retrieveUserDivParameters();
		
		formRunner = new FormRunnerWidget(images);

		String formId = FormUtil.getFormId();
		String entityId = FormUtil.getEntityId();
		if(formId != null && entityId != null)
			formRunner.loadForm(Integer.parseInt(formId),Integer.parseInt(entityId));
		else
			Window.alert(LocaleText.get("noFormId") + FormUtil.getEntityIdName() + LocaleText.get("divFound"));
		
		RootPanel.get("purcformrunner").add(formRunner);
		
		FormUtil.maximizeWidget(formRunner);

	}
}
