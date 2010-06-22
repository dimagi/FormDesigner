package org.purc.purcforms.client.listener;

import java.util.List;

import org.purc.purcforms.client.model.KeyValue;

public interface FormDefListChangeListener {
	void onFormDefListChanged(List<KeyValue> formList);
}
