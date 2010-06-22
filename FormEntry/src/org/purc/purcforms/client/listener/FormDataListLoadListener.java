package org.purc.purcforms.client.listener;

import java.util.List;

import org.purc.purcforms.client.model.FormDataHeader;

public interface FormDataListLoadListener {
	void onFormDataListLoaded(List<FormDataHeader> dataList);
}
