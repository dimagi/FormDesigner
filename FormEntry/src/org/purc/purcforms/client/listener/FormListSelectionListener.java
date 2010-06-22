package org.purc.purcforms.client.listener;

import java.util.List;

import org.purc.purcforms.client.model.KeyValue;


/**
 * 
 * @author daniel
 *
 */
public interface FormListSelectionListener {
	void onFormListSelected(List<KeyValue> formList);
}
