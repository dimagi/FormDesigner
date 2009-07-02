package org.purc.purcforms.client;

import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.util.FormUtil;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
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
		
		RootPanel rootPanel = RootPanel.get("purcformsdesigner");
		if(rootPanel == null)
			return;

		FormUtil.setupUncaughtExceptionHandler();

		FormDesignerUtil.setDesignerTitle();
		
		FormUtil.retrieveUserDivParameters();
		
		designer = new FormDesignerWidget(true,true,true);

		String s = FormUtil.getFormId();
		if(s != null)
			designer.loadForm(Integer.parseInt(s));

		// Hook the window resize event, so that we can adjust the UI.
		Window.addWindowResizeListener(this);

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

		// Finally, add the designer widget to the RootPanel, so that it will be
		// displayed.
		rootPanel.add(designer);

		// Call the window resized handler to get the initial sizes setup. Doing
		// this in a deferred command causes it to occur after all widgets' sizes
		// have been computed by the browser.
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				onWindowResized(Window.getClientWidth(), Window.getClientHeight());
			}
		});
		
		//Element elem = DOM.getElementById("loading");
		//DOM.removeChild(elem.getParentElement(), elem);
		//DOM.removeChild(elem.getParentElement(), elem);
		//DOM.removeChild(RootPanel.getBodyElement(), DOM.getElementById("loading")); 

		//onWindowResized(Window.getClientWidth(), Window.getClientHeight());
	}

	public void onWindowResized(int width, int height) {
		designer.onWindowResized(width, height);
	}
}
