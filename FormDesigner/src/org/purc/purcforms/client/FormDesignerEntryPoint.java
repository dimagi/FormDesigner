package org.purc.purcforms.client;

import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.view.LoginDialog;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class FormDesignerEntryPoint implements EntryPoint ,WindowResizeListener{

	private FormDesignerWidget designer;

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
			RootPanel rootPanel = RootPanel.get("purcformsdesigner");
			if(rootPanel == null)
				return;

			FormUtil.setupUncaughtExceptionHandler();

			FormDesignerUtil.setDesignerTitle();

			String s = FormUtil.getDivValue("allowBindEdit");
			if(s != null && s.equals("0"))
				Context.setAllowBindEdit(false);

			FormUtil.retrieveUserDivParameters();


			// Get rid of scrollbars, and clear out the window's built-in margin,
			// because we want to take advantage of the entire client area.
			Window.enableScrolling(false);
			Window.setMargin("0px");

			// Different themes use different background colors for the body
			// element, but IE only changes the background of the visible content
			// on the page instead of changing the background color of the entire
			// page. By changing the display style on the body element, we force
			// IE to redraw the background correctly.
			RootPanel.getBodyElement().getStyle().setProperty("display", "none");
			RootPanel.getBodyElement().getStyle().setProperty("display", "");

			designer = new FormDesignerWidget(true,true,true);
			
			// Finally, add the designer widget to the RootPanel, so that it will be
			// displayed.
			rootPanel.add(designer);
			
			s = FormUtil.getFormId();
			if(s != null)
				designer.loadForm(Integer.parseInt(s));

			// Call the window resized handler to get the initial sizes setup. Doing
			// this in a deferred command causes it to occur after all widgets' sizes
			// have been computed by the browser.
			DeferredCommand.addCommand(new Command() {
				public void execute() {
					onWindowResized(Window.getClientWidth(), Window.getClientHeight());
					
					String id = FormUtil.getFormId();
					if(id == null || id.equals("-1"))
						FormUtil.dlg.hide();
				}
			});

			
			//LoginDialog dlg = new LoginDialog(null);
			//dlg.center();
			
			//FormDesignerUtil.authenticate();
			
			//FormUtil.dlg.hide();

			//Element elem = DOM.getElementById("loading");
			//DOM.removeChild(elem.getParentElement(), elem);
			//DOM.removeChild(elem.getParentElement(), elem);
			//DOM.removeChild(RootPanel.getBodyElement(), DOM.getElementById("loading")); 

			//onWindowResized(Window.getClientWidth(), Window.getClientHeight());
			
			// Hook the window resize event, so that we can adjust the UI.
			Window.addWindowResizeListener(this);
		}
		catch(Exception ex){
			FormUtil.dlg.hide();
			FormUtil.displayException(ex);
		}
	}


	public void onWindowResized(int width, int height) {
		designer.onWindowResized(width, height);
	}
	
	// Set up the JS-callable signature as a global JS function.
	private native void publishJS() /*-{
   		$wnd.authenticationCallback = @org.purc.purcforms.client.controller.FormDesignerController::authenticationCallback(Z);
	}-*/;
}
