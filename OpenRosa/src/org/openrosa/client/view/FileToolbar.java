package org.openrosa.client.view;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;


/**
 * 
 * @author daniel
 *
 */
public class FileToolbar extends Composite {

	/** Main widget for this tool bar. */
	private HorizontalPanel panel = new HorizontalPanel();
	
	/** The tool bar buttons. */
	private PushButton btnNew;
	private PushButton btnOpen;
	private PushButton btnSave;
	
	

	public FileToolbar(){
		setupToolbar();
		initWidget(panel);
	}
	
	
	/**
	 * Sets up the tool bar.
	 */
	private void setupToolbar(){
		btnNew = new PushButton("New");
		btnOpen = new PushButton("Open");
		btnSave = new PushButton("Save");
		
		panel.add(btnNew);
		panel.add(btnOpen);
		panel.add(btnSave);
		
		panel.setSpacing(3);
	}
}
