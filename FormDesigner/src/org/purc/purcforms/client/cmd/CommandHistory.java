package org.purc.purcforms.client.cmd;

import java.util.Stack;


/**
 * 
 * @author daniel
 *
 */
public class CommandHistory {

	private Stack<ICommand> undoCmds = new Stack<ICommand>();
	private Stack<ICommand> redoCmds = new Stack<ICommand>();
	
	public void undo(){
		
	}
	
	public void redo(){
		
	}
	
	public boolean canUndo(){
		return undoCmds.size() > 0;
	}
	
	public boolean canRedo(){
		return redoCmds.size() > 0;
	}
	
	public String getUndoCommandName(){
		if(undoCmds.size() == 0)
			return null;
		else
			return undoCmds.lastElement().getName();
	}
	
	public String getRedoCommandName(){
		if(redoCmds.size() == 0)
			return null;
		else
			return redoCmds.lastElement().getName();
	}
}
