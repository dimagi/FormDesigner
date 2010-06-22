package org.purc.purcforms.client.listener;

public interface FormDataListener {

	void onOpenFormData(String id);
	void onDeleteFormData(String id, FormDataDeleteListener listener);
}
