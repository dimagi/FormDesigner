package org.purc.purcforms.client.cmd;

import org.purc.purcforms.client.FormEntryContext;
import org.purc.purcforms.client.listener.DataLoadListener;
import org.purc.purcforms.client.util.Utils;


/**
 * 
 * @author daniel
 *
 */
public class FormDataListLoadCmd implements DataLoadListener {

	public FormDataListLoadCmd(){
		
	}
	
	public void onDataReceived(String data){
		FormEntryContext.setFormDataList(Utils.getFormDataList(data));
	}
}
