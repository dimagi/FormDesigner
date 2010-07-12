package org.purc.purcforms.client.cmd;

import org.purc.purcforms.client.FormDesignerWidget;
import org.purc.purcforms.client.FormEntryContext;
import org.purc.purcforms.client.controller.IFormSaveListener;

import com.google.gwt.user.client.Window;

public class FormDesignerCmd implements IFormSaveListener{
	
	public FormDesignerCmd(FormDesignerWidget formDesigner){
		formDesigner.setFormSaveListener(this);
	}
	
	
	public boolean onSaveForm(int formId, String xformsXml, String layoutXml, String javaScriptSrc){
		
		FormEntryContext.getDatabaseManager().saveFormDef(FormEntryContext.getFormDefId(), xformsXml);
		FormEntryContext.getDatabaseManager().saveFormLayout(FormEntryContext.getFormDefId(), layoutXml);
		FormEntryContext.getDatabaseManager().saveFormJavaScript(FormEntryContext.getFormDefId(), javaScriptSrc);
		
		return true;
	}
	
	
	public void onSaveLocaleText(int formId, String xformsLocaleText, String layoutLocaleText){
		FormEntryContext.getDatabaseManager().saveXformLocaleText(FormEntryContext.getFormDefId(), xformsLocaleText);
		FormEntryContext.getDatabaseManager().saveLayoutLocaleText(FormEntryContext.getFormDefId(), layoutLocaleText);
		
		Window.alert("Form Saved Successfully.");
	}
}
