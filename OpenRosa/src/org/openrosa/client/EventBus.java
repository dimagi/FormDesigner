package org.openrosa.client;

import java.util.ArrayList;
import java.util.List;

import org.openrosa.client.controller.IDataTypeChangeListener;
import org.openrosa.client.model.IFormElement;


/**
 * Bus for managing global events.
 * 
 * @author daniel
 *
 */
public class EventBus {

	private List<IDataTypeChangeListener> dataTypeChangeListeners = new ArrayList<IDataTypeChangeListener>();
	
	
	public void addDataTypeChangeListener(IDataTypeChangeListener dataTypeChangeListener){
		dataTypeChangeListeners.add(dataTypeChangeListener);
	}
	
	public void removeDataTypeChangeListener(IDataTypeChangeListener dataTypeChangeListener){
		dataTypeChangeListeners.remove(dataTypeChangeListener);
	}
	
	public void fireDataTypeChangeEvent(IFormElement element, int prevDataType){
		for(IDataTypeChangeListener listener : dataTypeChangeListeners)
			listener.onDataTypeChanged(element, prevDataType);
	}
}
