package org.purc.purcforms.client.cmd;

import org.purc.purcforms.client.widget.DesignWidgetWrapper;


public class DeleteWidgetCmd implements ICommand {

	private DesignWidgetWrapper widget;
	
	
	public String getName(){
		return "Delete Widget";
	}
	
	public void execute(){
		
	}
}
