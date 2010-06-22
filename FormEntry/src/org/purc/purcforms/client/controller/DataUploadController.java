package org.purc.purcforms.client.controller;

import java.util.HashMap;
import java.util.List;

import org.purc.purcforms.client.FormEntryContext;
import org.purc.purcforms.client.cmd.DataListCollectorCmd;
import org.purc.purcforms.client.cmd.DataUploadCmd;
import org.purc.purcforms.client.cmd.FormDataDeleteCmd;
import org.purc.purcforms.client.listener.DataListCollectorListener;
import org.purc.purcforms.client.listener.DataUploadListener;
import org.purc.purcforms.client.listener.FormDataDeleteListener;
import org.purc.purcforms.client.listener.LoginInfoListener;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.view.impl.LoginInfoDlg;

import com.google.gwt.user.client.Window;


/**
 * 
 * @author daniel
 *
 */
public class DataUploadController implements DataListCollectorListener, DataUploadListener, FormDataDeleteListener, LoginInfoListener {

	private List<String> dataList;
	private int pos = 0;
	private int count = 0;
	private int failureCount = 0;
	private String id;
	private HashMap<String, String> dataDefMap;
	
	
	public DataUploadController(){
		
	}
	
	public void uploaData(){
		new LoginInfoDlg(this).center();
	}
	
	private void uploadData(String id){
		this.id = id;
		new DataUploadCmd(id,pos+1, dataList.size(), this); //add 1 for counting
	}
	
	public void onDataListCollected(List<String> dataList, HashMap<String, String> dataDefMap){
		this.dataList = dataList;
		this.dataDefMap = dataDefMap;
		
		count = dataList.size();
		pos = 0;
		failureCount = 0;
		
		if(count == 0){
			Window.alert("No data to upload.");
			return;
		}
		
		uploadData(dataList.get(pos));
	}
	
	public void onDataUploaded(){
		new FormDataDeleteCmd(id, dataDefMap.get(id), this);
	}
	
	public void onDataUploadFailed(){
		failureCount++;
		tryUploadNextData();
	}
	
	private void tryUploadNextData(){
		if(++pos < count)
			uploadData(dataList.get(pos));
		else
			displayUploadCompleteMsg();
	}
	
	private void displayUploadCompleteMsg(){
		
		FormEntryContext.setUserName(null);
		FormEntryContext.setPassword(null);
		
		String message = count + " forms of data uploaded successfully.";
		if(failureCount > 0)
			message = (count - failureCount) + " forms of data uploaded successfully and " + failureCount + " failed";
	
		FormUtil.dlg.hide();
		
		Window.alert(message);
		
		//TODO We need to clear the main view just incase it has a list of data loaded.
	}
	
	public void onFormDataDeleted(String id){
		tryUploadNextData();
	}
	
	public void onLoginInfo(String name, String password){
		FormEntryContext.setUserName(name);
		FormEntryContext.setPassword(password);
		
		new DataListCollectorCmd(this);
	}
}
