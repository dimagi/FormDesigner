package org.purc.purcforms.client.cmd;

import java.util.ArrayList;
import java.util.List;

import org.purc.purcforms.client.FormEntryContext;
import org.purc.purcforms.client.listener.FormDefDownloadListener;
import org.purc.purcforms.client.listener.FormListSelectionListener;
import org.purc.purcforms.client.model.KeyValue;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.util.Utils;
import org.purc.purcforms.client.view.impl.FormSelectionViewImpl;

import com.google.gwt.user.client.Window;


/**
 * 
 * @author daniel
 *
 */
public class FormDownloadCmd implements FormDefDownloadListener, FormListSelectionListener{
	
	/** List of only the new forms to be downloaded. */
	private List<KeyValue> formList;
	private int formIndex = 0;
	private KeyValue keyValue;
	

	public void onFormDefDownloaded(String id){
		formIndex++;
		
		List<KeyValue> formDefList = FormEntryContext.getFormDefList();
		if(formDefList == null)
			formDefList = new ArrayList<KeyValue>();
		formDefList.add(keyValue);
		
		FormEntryContext.setFormDefList(formDefList);
		FormEntryContext.getDatabaseManager().saveFormDefList(Utils.getFormDefListXml(formDefList));
		
		downloadNextForm();
	}
	
	public void onFormDefListDownloaded(List<KeyValue> formList){
		List<KeyValue> existingFormList = FormEntryContext.getFormDefList();
		if(existingFormList != null){
			for(KeyValue keyValue : existingFormList)
				removeFormDef(keyValue.getKey(), formList);
		}
		
		if(formList.size() == 0){
			FormUtil.dlg.hide();
			Window.alert("No new forms to download.");
		}
		else
			new FormSelectionViewImpl(formList,this).center();
	}
	
	private void downloadNextForm(){
		if(formIndex == formList.size()){
			FormEntryContext.setUserName(null);
			FormEntryContext.setPassword(null);
			FormUtil.dlg.hide(); //This does not belong here but stays for the meantime as we fix the bug of progress message not being displayed for downloading of forms other than the first one.
			return;
		}
		
		keyValue = formList.get(formIndex);
		FormEntryContext.getFormEntryController().downloadForm(keyValue.getKey(), keyValue.getValue(), this);
	}
	
	public void onFormListSelected(List<KeyValue> formList){
		this.formList = formList;
		formIndex = 0;
		
		//Some forms may fail to download and so we then do not want to maintain them in our list.
		/*List<KeyValue> list = mergeFormListWithExisting(formList);
		FormEntryContext.setFormDefList(list);
		FormEntryContext.getDatabaseManager().saveFormDefList(Utils.getFormDefListXml(list));*/
		
		downloadNextForm();
	}
	
	private void removeFormDef(String id, List<KeyValue> formList){
		for(KeyValue keyValue : formList){
			if(id.equalsIgnoreCase(keyValue.getKey())){
				formList.remove(keyValue);
				return;
			}
		}
	}
	
	/*private List<KeyValue> mergeFormListWithExisting(List<KeyValue> newFormList){
		List<KeyValue> formList = FormEntryContext.getFormDefList();
		formList.addAll(newFormList);
		return formList;
	}*/
}
