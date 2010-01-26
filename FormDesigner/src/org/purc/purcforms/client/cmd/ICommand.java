package org.purc.purcforms.client.cmd;


/**
 * Encapsulates an undoable or redoable command.
 * 
 * @author daniel
 *
 */
public interface ICommand {
	String getName();
	void execute();
}
