package org.purc.purcforms.client.listener;

import java.util.HashMap;
import java.util.List;


/**
 * 
 * @author daniel
 *
 */
public interface DataListCollectorListener {
	void onDataListCollected(List<String> dataList, HashMap<String, String> dataDefMap);
}
