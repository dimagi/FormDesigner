package org.purc.purcforms.client;

import java.util.ArrayList;
import java.util.List;

import org.purc.purcforms.client.controller.FormEntryController;
import org.purc.purcforms.client.database.DatabaseManager;
import org.purc.purcforms.client.listener.FormDataListLoadListener;
import org.purc.purcforms.client.listener.FormDataLoadListener;
import org.purc.purcforms.client.listener.FormDefListChangeListener;
import org.purc.purcforms.client.listener.FormDefLoadListener;
import org.purc.purcforms.client.model.FormDataHeader;
import org.purc.purcforms.client.model.KeyValue;


/**
 * 
 * @author daniel
 *
 */
public class FormEntryContext {

	private static DatabaseManager databaseMgr = new DatabaseManager();
	
	private static FormEntryController formEntryController = new FormEntryController();
	
	private static List<FormDefListChangeListener> formDefListChangeListeners = new ArrayList<FormDefListChangeListener>();
	private static List<FormDefLoadListener> formDefLoadListeners = new ArrayList<FormDefLoadListener>();
	
	private static List<FormDataListLoadListener> formDataListLoadListeners = new ArrayList<FormDataListLoadListener>();
	private static List<FormDataLoadListener> formDataLoadListeners = new ArrayList<FormDataLoadListener>();

	private static String formDownloadUrl; //http://open-data-kit.appspot.com/formList
	private static String dataUploadUrl;
	
	/** The current form definition id. */
	private static String formDefId;
	
	/** The current form name. */
	private static String formName;
	
	/** The current form data id. */
	private static String formDataId;
	
	/** The current form's xforms xml. */
	private static String xformXml;
	
	private static List<KeyValue> formDefList;
	
	private static String userName;
	
	private static String password;
	
	
	public static void init(){
		databaseMgr.init();
	}
	
	public static DatabaseManager getDatabaseManager(){
		return databaseMgr;
	}
	
	public static FormEntryController getFormEntryController(){
		return formEntryController;
	}
	
	public static void addFormDefListChangeListener(FormDefListChangeListener formDefListChangeListener){
		formDefListChangeListeners.add(formDefListChangeListener);
	}
	
	public static void addFormDataListLoadListener(FormDataListLoadListener formDataListLoadListener){
		formDataListLoadListeners.add(formDataListLoadListener);
	}
	
	public static void addFormDataLoadListener(FormDataLoadListener formDataLoadListener){
		formDataLoadListeners.add(formDataLoadListener);
	}
	
	public static void setFormDefList(List<KeyValue> formDefList){
		FormEntryContext.formDefList = formDefList;
		
		for(FormDefListChangeListener formDefListChangeListener : formDefListChangeListeners)
			formDefListChangeListener.onFormDefListChanged(formDefList);
	}
	
	public static void setFormDataList(List<FormDataHeader> formDataList){
		for(FormDataListLoadListener formDataListLoadListener : formDataListLoadListeners)
			formDataListLoadListener.onFormDataListLoaded(formDataList);
	}
	
	public static void addFormDefLoadListener(FormDefLoadListener formDefLoadListener){
		formDefLoadListeners.add(formDefLoadListener);
	}
	
	public static void setCurrentFormDef(boolean designForm, String xformXml, String layoutXml, String javaScriptSrc){
		FormEntryContext.xformXml = xformXml;
		
		for(FormDefLoadListener formDefLoadListener : formDefLoadListeners)
			formDefLoadListener.onFormDefLoaded(designForm, xformXml, layoutXml, javaScriptSrc);
	}
	
	public static void setCurrentFormData(String xformXml, String layoutXml, String javaScriptSrc, String modelXml){
		FormEntryContext.xformXml = xformXml;
		
		for(FormDataLoadListener formDataLoadListener : formDataLoadListeners)
			formDataLoadListener.onFormDataLoaded(xformXml, layoutXml, javaScriptSrc, modelXml);
	}
	
	public static void setFormDefId(String id){
		formDefId = id;
	}
	
	public static String getFormDefId(){
		return formDefId;
	}
	
	public static void setFormDataId(String id){
		formDataId = id;
	}
	
	public static String getFormDataId(){
		return formDataId;
	}
	
	public static String getXformXml(){
		return xformXml;
	}
	
	public static List<KeyValue> getFormDefList(){
		return formDefList;
	}
	
	public static String getFormDownloadUrl(){
		return formDownloadUrl;
	}
	
	public static void setFormDownloadUrl(String url){
		formDownloadUrl = url;
	}
	
	public static String getDataUploadUrl(){
		return dataUploadUrl;
	}
	
	public static void setDataUploadUrl(String url){
		dataUploadUrl = url;
	}
	
	public static void setFormName(String name){
		formName = name;
	}
	
	public static String getFormName(){
		return formName;
	}
	
	public static String getUserName(){
		return userName;
	}
	
	public static String getPassword(){
		return password;
	}
	
	public static void setUserName(String userName){
		FormEntryContext.userName = userName;
	}
	
	public static void setPassword(String password){
		FormEntryContext.password = password;
	}
}
