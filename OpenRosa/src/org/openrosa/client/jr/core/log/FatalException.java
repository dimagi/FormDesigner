package org.openrosa.client.jr.core.log;

public class FatalException extends WrappedException {

	public FatalException () {
		this("");
	}
	
	public FatalException (String message) {
		super(message);
	}
	
	public FatalException (Exception child) {
		super(child);
	}
	
	public FatalException (String message, Exception child) {
		super(message, child);
	}
	
}
