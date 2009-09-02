package org.purc.purcforms.client;

import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.view.FormRunnerView.Images;
import org.purc.purcforms.client.widget.FormRunnerWidget;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
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
		
		FormUtil.dlg.setText("loading");
		FormUtil.dlg.center();
		
		publishJS();

		DeferredCommand.addCommand(new Command() {
			public void execute() {
				onModuleLoadDeffered();
			}
		});		
	}

	public void onModuleLoadDeffered() {
		try{
			FormUtil.setupUncaughtExceptionHandler();	

			FormUtil.retrieveUserDivParameters();

			formRunner = new FormRunnerWidget(images);
			
			RootPanel.get("purcformrunner").add(formRunner);

			FormUtil.maximizeWidget(formRunner);

			String formId = FormUtil.getFormId();
			String entityId = FormUtil.getEntityId();
			if(formId != null && entityId != null)
				formRunner.loadForm(Integer.parseInt(formId),Integer.parseInt(entityId));
			else{
				FormUtil.dlg.hide();
				Window.alert(LocaleText.get("noFormId") + FormUtil.getEntityIdName() + LocaleText.get("divFound"));
			}

			DeferredCommand.addCommand(new Command() {
				public void execute() {
					String formId = FormUtil.getFormId();
					String entityId = FormUtil.getEntityId();
					if(formId == null || entityId == null)
						FormUtil.dlg.hide();
				}
			});
		}
		catch(Exception ex){
			FormUtil.dlg.hide();
			FormUtil.displayException(ex);
		}
	}
	
	/*public static native void okTest(String text) -{
		$wnd.heyMen(text);
	}-;

	private static boolean getGWT(String a, String b){
		return true;
	}*/

	// Set up the JS-callable signature as a global JS function.
	private native void publishJS() /*-{
   		$wnd.authenticationCallback = @org.purc.purcforms.client.view.FormRunnerView::authenticationCallback(Z);
	}-*/;
}
