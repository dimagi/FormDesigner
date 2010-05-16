package org.openrosa.client.jr.core.services.storage;

import org.openrosa.client.jr.core.util.externalizable.Externalizable;

/**
 * A modest extension to Externalizable which identifies objects that have the concept of an internal 'record ID'
 */
public interface Persistable extends Externalizable {
	void setID (int ID);
	int getID ();
}
