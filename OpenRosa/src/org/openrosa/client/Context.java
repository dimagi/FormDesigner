package org.openrosa.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.openrosa.client.controller.ILocaleSelectionListener;
import org.openrosa.client.model.FormDef;
import org.openrosa.client.controller.ILocaleListChangeListener;
import org.openrosa.client.util.FormUtil;


/**
 * Contains shared information that has the notion of being current (eg currently
 * selected form, current locale, curent mode (design or preview), and more. 
 * It represents the runtime context of the form designer.
 * Contexts are associated with the current thread.
 * 
 * @author daniel
 *
 */
public class Context {
	
	/** State of the form designer being in neither preview or design mode. */
	public static final byte MODE_NONE = 0;
	
	/** State when setting questions properties in the properties tab. */
	public static final byte MODE_QUESTION_PROPERTIES = 1;
	
	/** State when the form designer is in design mode. 
	 * As in used dragging around widgets on the design surface.
	 */
	public static final byte MODE_DESIGN = 2;
	
	/** State when the user is previewing their form designs. */
	public static final byte MODE_PREVIEW = 3;
	
	/** State when displaying the xforms source. */
	public static final byte MODE_XFORMS_SOURCE = 4;
	

	
	/**Determines if we should allow changing of question bindings.
	 * This is useful for cases where users are not allowed to change the question binding
	 * which affected the names of the xml model.
	 */
	private static boolean allowBindEdit = true;
	
	/** The current mode of the form designer. */
	private static byte currentMode = MODE_NONE;
	
	/** The form having focus. */
	private static FormDef formDef;
	
	/** Flag telling whether widgets and locked and hence allow no movement. */
	private static boolean lockWidgets = false;
	
//	/** A list of widgets that have been cut or copied to the clipboard and ready for pasting. */
//	public static List<DesignWidgetWrapper> clipBoardWidgets = new Vector<DesignWidgetWrapper>();
	
	private static boolean offlineMode = true;
	
	/** A mapping for form locale text. The key is the formId while the value is a map of locale 
	 * key and text, where locale key is the value map key and text is the value map value.
	 */
	private static HashMap<Integer,HashMap<String,String>> languageText = new HashMap<Integer,HashMap<String,String>>();

	private static EventBus eventBus = new EventBus();
	
	
	/**
	 * Gets the form that has focus.
	 * 
	 * @return the form definition object.
	 */
	public static FormDef getFormDef() {
		return formDef;
	}

	/**
	 * Sets the form that has focus.
	 * 
	 * @param formDef the form definition object.
	 */
	public static void setFormDef(FormDef formDef) {
		Context.formDef = formDef;
	}


	
	
	/**
	 * Check if the current form allows changes for both structure and text.
	 * 
	 * @return true if readonly else false.
	 */
	public static boolean isReadOnly(){
		return (formDef != null && formDef.isReadOnly());
	}
	
	/**
	 * Checks whether the current form structure allows changes.
	 * 
	 * @return true if readonly else false.
	 */
	public static boolean isStructureReadOnly(){
		if((formDef != null && formDef.isReadOnly()))
			return true;
		return false;
	}
	
	/**
	 * Checks if we should allow changing of question bindings.
	 * 
	 * @return true if yes, else false.
	 */
	public static boolean allowBindEdit(){
		return allowBindEdit;
	}
	
	/**
	 * Turns off or on question binding.
	 * 
	 * @param allowBindEdit set to true if we should allow editing bindings, else false.
	 */
	public static void setAllowBindEdit(boolean allowBindEdit){
		Context.allowBindEdit = allowBindEdit;
	}
	
	/**
	 * Gets the current form designer mode.
	 * 
	 * @return can be (MODE_DESIGN,MODE_PREVIEW,MODE_NONE)
	 */
	public static byte getCurrentMode(){
		return currentMode;
	}
	
	/**
	 * Sets the current form designer mode.
	 * 
	 * @param currentMode should be (MODE_DESIGN or MODE_PREVIEW,MODE_NONE)
	 */
	public static void setCurrentMode(byte currentMode){
		Context.currentMode = currentMode;
	}
	
	public static boolean getLockWidgets(){
		return lockWidgets;
	}
	
	public static void setLockWidgets(boolean lockWidgets){
		Context.lockWidgets = lockWidgets;
	}
	
	public static boolean isOfflineMode(){
		return offlineMode;
	}
	
	public static void setOfflineModeStatus(){
		try{
			String formId = FormUtil.getFormId();
			if(formId != null && Integer.parseInt(formId) >= 0)
				offlineMode = false;
		}
		catch(Exception ex){}
	}
	
	public static EventBus getEventBus(){
		return eventBus;
	}
	
//	public static HashMap<Integer,HashMap<String,String>> getLanguageText(){
//		return languageText;
//	}
//
//	public static boolean inLocalizationMode() {
//		// TODO Auto-generated method stub
//		return true;
//	}
	
}
