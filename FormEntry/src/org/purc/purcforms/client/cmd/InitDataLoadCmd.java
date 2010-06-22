package org.purc.purcforms.client.cmd;

import org.purc.purcforms.client.FormEntryContext;
import org.purc.purcforms.client.listener.DataLoadListener;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.util.Utils;


/**
 * 
 * @author daniel
 *
 */
public class InitDataLoadCmd implements DataLoadListener{

	private int loadCount = 0;
	
	public InitDataLoadCmd(){
		FormEntryContext.getDatabaseManager().loadFormDefList(this);
	}
	
	public void onDataReceived(String data){
		loadCount++;
		
		if(loadCount == 1){
			FormEntryContext.setFormDefList(Utils.getFormDefList(data));
			FormEntryContext.getDatabaseManager().loadFormDownloadUrl(this);
		}
		else if(loadCount == 2){
			String url = data;
			if(url == null)
				url = FormUtil.getFormDefDownloadUrlSuffix();
			FormEntryContext.setFormDownloadUrl(url);
			FormEntryContext.getDatabaseManager().loadDataUploadUrl(this);
		}
		else if(loadCount == 3){
			String url = data;
			if(url == null)
				url = FormUtil.getFormDataUploadUrlSuffix();
			FormEntryContext.setDataUploadUrl(url);
		}
	}
}
