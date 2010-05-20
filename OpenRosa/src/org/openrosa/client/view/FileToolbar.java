package org.openrosa.client.view;

import org.openrosa.client.controller.IFileListener;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;


/**
 * This is the top most tool bar which has the New, Open and Save buttons.
 * These are basically file operations where the file contents could be pasted in the textarea widgets
 * or browsed from the file system when not working in offline mode.
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
	
	private IFileListener fileListener;
	
	
	public FileToolbar(IFileListener fileListener){
		this.fileListener = fileListener;
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
		
		btnNew.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event){
				fileListener.onNew();}}
		);
		
		btnOpen.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event){
				fileListener.onOpen();}}
		);
		
		btnSave.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event){
				fileListener.onSave(true);}}
		);
	}
}
