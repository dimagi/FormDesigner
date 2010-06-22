package org.purc.purcforms.client.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.purc.purcforms.client.FormEntryContext;
import org.purc.purcforms.client.listener.DataListCollectorListener;
import org.purc.purcforms.client.listener.DataLoadListener;
import org.purc.purcforms.client.model.KeyValue;
import org.purc.purcforms.client.util.Utils;

//85,000
//15,000

/**
 * 
 * @author daniel
 *
 */
public class DataListCollectorCmd implements DataLoadListener{

	private DataListCollectorListener listener;
	private List<String> dataList;
	private int loadCount = 0;
	private List<KeyValue> formDefList;
	private String id;
	private HashMap<String, String> dataDefMap = new HashMap<String, String>();
	
	
	public DataListCollectorCmd(DataListCollectorListener listener){
		this.listener = listener;
		fillDataList();
	}
	
	private void fillDataList(){
		dataList = new ArrayList<String>();
		
		formDefList = FormEntryContext.getFormDefList();
		if(formDefList == null || formDefList.size() == 0)
			return;
		
		loadNextFormDataList();
	}
	
	public void onDataReceived(String data){
		if(data != null)
			Utils.fillFormDataIdList(data, dataList, id, dataDefMap);
		
		if(++loadCount == FormEntryContext.getFormDefList().size())
			listener.onDataListCollected(dataList, dataDefMap);
		else
			loadNextFormDataList();
	}
	
	private void loadNextFormDataList(){
		KeyValue keyValue = formDefList.get(loadCount);
		id = keyValue.getKey();
		FormEntryContext.getDatabaseManager().loadFormDataList(id, this);
	}
}
