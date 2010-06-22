package org.purc.purcforms.client.listener;

import java.util.List;

import org.purc.purcforms.client.model.KeyValue;

public interface FormDefDownloadListener {
	
	void onFormDefDownloaded(String id);
	void onFormDefListDownloaded(List<KeyValue> formList);
}
