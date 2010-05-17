package org.openrosa.client.jr.core.services.storage;

public interface IStorageFactory {
	IStorageUtility newStorage (String name, Class type);
}
