package org.purc.purcforms.client.database;

import org.purc.purcforms.client.listener.DataLoadListener;

import com.google.gwt.user.client.Window;



/**
 * Manages database operations.
 * 
 * NOTE: When calling more than one load method of this class, you should ensure that you only call
 * the next command after a call back (DataLoadListener::onDataReceived()) from the previous command.
 * 
 * @author daniel
 *
 */
public class DatabaseManager {

	private static final String KEY_FORM_DEF_LIST = "form_def_list";
	private static final String KEY_FORM_DATA_LIST = "_form_data_list";
	private static final String KEY_LAYOUT_SUFFIX = "_layout";
	private static final String KEY_JAVASCRIPT_SUFFIX = "_javascript";
	private static final String KEY_FORM_DOWNLOAD_URL = "form_downlod_url";
	private static final String KEY_DATA_UPLOAD_URL = "data_upload_url";
	private static final String KEY_XFORM_LOCALE_TEXT = "xform_locale_text";
	private static final String KEY_LAYOUT_LOCALE_TEXT = "layout_locale_text";

	private static DataLoadListener dataLoadListener;


	/**
	 * Initializes the database.
	 */
	public native void init() /*-{
		$wnd.Persist.remove('cookie');
		$wnd.dataStore = new $wnd.Persist.Store('PurcForms Data Store');
	}-*/;


	public void onDataReceived(boolean ok, String data){
		if(!ok)
			Window.alert(data);
		else
			dataLoadListener.onDataReceived(data);
	}


	private native void saveData(String key, String data) /*-{
		$wnd.dataStore.set(key, data);
	}-*/;


	private native void deleteData(String key) /*-{
		$wnd.dataStore.remove(key);
	}-*/;


	private native void loadData(String key, DatabaseManager x) /*-{
		$wnd.dataStore.get(key, function(ok, data) {
      			x.@org.purc.purcforms.client.database.DatabaseManager::onDataReceived(ZLjava/lang/String;)(ok,data);
    		});
	}-*/;


	public void saveFormDefList(String xml){
		saveData(KEY_FORM_DEF_LIST, xml);
	}


	public void saveFormDataList(String id, String xml){
		saveData(id + KEY_FORM_DATA_LIST, xml);
	}


	public void saveFormDef(String id, String xml){
		saveData(id, xml);
	}

	public void saveFormLayout(String id, String xml){
		saveData(id + KEY_LAYOUT_SUFFIX, xml);
	}

	public void saveFormJavaScript(String id, String xml){
		saveData(id + KEY_JAVASCRIPT_SUFFIX, xml);
	}
	
	public void saveXformLocaleText(String id, String xml){
		saveData(id + KEY_XFORM_LOCALE_TEXT, xml);
	}
	
	public void saveLayoutLocaleText(String id, String xml){
		saveData(id + KEY_LAYOUT_LOCALE_TEXT, xml);
	}

	public void saveFormData(String id, String xml){
		saveData(id, xml);
	}


	public void loadFormDefList(DataLoadListener dataLoadListener){
		DatabaseManager.dataLoadListener = dataLoadListener;
		loadData(KEY_FORM_DEF_LIST,this);
	}

	public void loadFormDataList(String id, DataLoadListener dataLoadListener){
		DatabaseManager.dataLoadListener = dataLoadListener;
		loadData(id + KEY_FORM_DATA_LIST,this);
	}

	public void loadFormDef(String id, DataLoadListener dataLoadListener){
		DatabaseManager.dataLoadListener = dataLoadListener;
		loadData(id,this); //TODO assuming these ids will never conflict with those of FormData
	}

	public void loadFormLayout(String id, DataLoadListener dataLoadListener){
		DatabaseManager.dataLoadListener = dataLoadListener;
		loadData(id + KEY_LAYOUT_SUFFIX,this);
	}

	public void loadFormJavaScript(String id, DataLoadListener dataLoadListener){
		DatabaseManager.dataLoadListener = dataLoadListener;
		loadData(id + KEY_JAVASCRIPT_SUFFIX,this);
	}
	
	public void loadXformLocaleText(String id, DataLoadListener dataLoadListener){
		DatabaseManager.dataLoadListener = dataLoadListener;
		loadData(id + KEY_XFORM_LOCALE_TEXT,this);
	}
	
	public void loadLayoutLocaleText(String id, DataLoadListener dataLoadListener){
		DatabaseManager.dataLoadListener = dataLoadListener;
		loadData(id + KEY_LAYOUT_LOCALE_TEXT,this);
	}

	public void loadFormData(String id, DataLoadListener dataLoadListener){
		DatabaseManager.dataLoadListener = dataLoadListener;
		loadData(id,this); //TODO assuming these ids will never conflict with those of FormDef
	}

	public void deleteFormData(String id){
		deleteData(id);
	}
	
	public void deleteFormDef(String id){
		deleteData(id);
		deleteData(id + KEY_FORM_DATA_LIST);
		deleteData(id + KEY_LAYOUT_SUFFIX);
		deleteData(id + KEY_JAVASCRIPT_SUFFIX);
		deleteData(id + KEY_XFORM_LOCALE_TEXT);
		deleteData(id + KEY_LAYOUT_LOCALE_TEXT);
	}
	
	public void deleteFormDataList(String id){
		deleteData(id + KEY_FORM_DATA_LIST);
	}
	
	public void saveFormDownloadUrl(String url){
		saveData(KEY_FORM_DOWNLOAD_URL, url);
	}
	
	public void saveDataUploadUrl(String url){
		saveData(KEY_DATA_UPLOAD_URL, url);
	}
	
	public void loadFormDownloadUrl(DataLoadListener dataLoadListener){
		DatabaseManager.dataLoadListener = dataLoadListener;
		loadData(KEY_FORM_DOWNLOAD_URL,this);
	}
	
	public void loadDataUploadUrl(DataLoadListener dataLoadListener){
		DatabaseManager.dataLoadListener = dataLoadListener;
		loadData(KEY_DATA_UPLOAD_URL,this);
	}
}