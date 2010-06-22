package org.purc.purcforms.client;

import java.util.ArrayList;
import java.util.List;

import org.purc.purcforms.client.model.Locale;
import org.purc.purcforms.client.util.FormUtil;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;


public class FormEntry implements EntryPoint{
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		
		FormUtil.setupUncaughtExceptionHandler();
		
		FormUtil.retrieveUserDivParameters();
		
		FormEntryContext.init();
		
		RootPanel panel = RootPanel.get("purcformsentry");
		if(panel != null)
			panel.add(new FormEntryWidget());
		
		loadLocales();
	}
	
	private void loadLocales(){
		String localesList = FormUtil.getDivValue("localeList");
		
		if(localesList == null || localesList.trim().length() == 0)
			return;
		
		String[] tokens = localesList.split(",");
		if(tokens == null || tokens.length == 0)
			return;
		
		List<Locale> locales = new ArrayList<Locale>();
		
		for(String token: tokens){
			int index = token.indexOf(':');
			
			//Should at least have one character for key or name
			if(index < 1 || index == token.length() - 1)
				continue;
			
			locales.add(new Locale(token.substring(0,index).trim(),token.substring(index+1).trim()));
		}
		
		Context.setLocales(locales);
	}
}
