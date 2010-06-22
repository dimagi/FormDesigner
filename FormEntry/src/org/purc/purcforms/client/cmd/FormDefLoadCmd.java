package org.purc.purcforms.client.cmd;

import org.purc.purcforms.client.FormEntryContext;
import org.purc.purcforms.client.listener.DataLoadListener;


/**
 * 
 * @author daniel
 *
 */
public class FormDefLoadCmd implements DataLoadListener {
	
	private boolean designForm;
	
	private int loadCount = 0;
	private String xformXml;
	private String layoutXml;
	private String javaScriptSrc;
	
	public FormDefLoadCmd(boolean designForm){
		this.designForm = designForm;
	}
	
	public void onDataReceived(String data){
		loadCount++;
		
		if(loadCount == 1){
			xformXml = data;
			FormEntryContext.getDatabaseManager().loadFormLayout(FormEntryContext.getFormDefId(), this);
		}
		else if(loadCount == 2){
			layoutXml = data;
			FormEntryContext.getDatabaseManager().loadFormJavaScript(FormEntryContext.getFormDefId(), this);
		}
		else{
			assert(loadCount == 3);
			
			javaScriptSrc = data;
			FormEntryContext.setCurrentFormDef(designForm, xformXml, layoutXml, javaScriptSrc);
		}
	}
}
