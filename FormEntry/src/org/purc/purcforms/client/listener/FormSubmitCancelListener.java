package org.purc.purcforms.client.listener;

import org.purc.purcforms.client.model.FormDataHeader;

public interface FormSubmitCancelListener {
	
	void onFormCancelled();
	void onNewFormSubmitted(FormDataHeader formDataHeader);
	void onExistingFormSubmitted(FormDataHeader formDataHeader);
}
