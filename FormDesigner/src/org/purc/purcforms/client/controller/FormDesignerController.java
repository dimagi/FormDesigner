package org.purc.purcforms.client.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.purc.purcforms.client.AboutDialog;
import org.purc.purcforms.client.CenterPanel;
import org.purc.purcforms.client.Context;
import org.purc.purcforms.client.LeftPanel;
import org.purc.purcforms.client.PurcConstants;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.ModelConstants;
import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.util.LanguageUtil;
import org.purc.purcforms.client.view.FormsTreeView;
import org.purc.purcforms.client.view.OpenFileDialog;
import org.purc.purcforms.client.view.SaveFileDialog;
import org.purc.purcforms.client.xforms.XformConverter;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;


/**
 * 
 * @author daniel
 *
 */
public class FormDesignerController implements IFormDesignerListener, IOpenFileDialogEventListener{

	private CenterPanel centerPanel;
	private LeftPanel leftPanel;
	private Integer formId;	
	private IFormSaveListener formSaveListener;
	private HashMap<Integer,HashMap<String,String>> languageText = new HashMap<Integer,HashMap<String,String>>();


	public FormDesignerController(CenterPanel centerPanel, LeftPanel leftPanel){
		this.leftPanel = leftPanel;
		this.centerPanel = centerPanel;
	}

	public void addNewItem() {
		leftPanel.addNewItem();
	}

	public void addNewChildItem() {
		leftPanel.addNewChildItem();
	}

	public static native void back() /*-{
		window.history.go(-1);
	}-*/;

	public void closeForm() {
		back();
	} 

	public void deleteSelectedItem() {
		leftPanel.deleteSelectedItem();	
		centerPanel.deleteSelectedItem();
	}

	public void moveItemDown() {
		leftPanel.moveItemDown();
	}

	public void moveItemUp() {
		leftPanel.moveItemUp();
	}

	public void newForm() {
		if(isOfflineMode())
			leftPanel.addNewForm();
	}

	public void openForm() {
		if(isOfflineMode()){
			String xml = null;
			if(centerPanel.isInLayoutMode()){
				xml = centerPanel.getLayoutXml();
				if(xml == null || xml.trim().length() == 0){
					OpenFileDialog dlg = new OpenFileDialog(this,"formopen");
					dlg.center();
				}
			}
			else{
				xml = centerPanel.getXformsSource();
				if(xml != null && xml.trim().length() > 0){
					FormDef formDef = leftPanel.getSelectedForm();
					if(formDef != null)
						refreshFormDeffered();
					else
						openFormDeffered(ModelConstants.NULL_ID);
				}
				else{
					OpenFileDialog dlg = new OpenFileDialog(this,"formopen");
					dlg.center();
				}
			}
		}
	}

	public void openFormDeffered(int id) {
		final int tempFormId = id;

		FormUtil.dlg.setText(LocaleText.get("openingForm"));
		FormUtil.dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {
				try{
					String xml = centerPanel.getXformsSource().trim();
					if(xml.length() > 0){
						FormDef formDef = XformConverter.fromXform2FormDef(xml);
						if(tempFormId != ModelConstants.NULL_ID)
							formDef.setId(tempFormId);

						formDef.setXformXml(centerPanel.getXformsSource());
						formDef.setLayoutXml(centerPanel.getLayoutXml());
						leftPanel.loadForm(formDef);
						centerPanel.loadForm(formDef,formDef.getLayoutXml());
						centerPanel.format();
					}
					FormUtil.dlg.hide();
				}
				catch(Exception ex){
					FormUtil.dlg.hide();
					FormUtil.displayException(ex);
				}	
			}
		});
	}

	public void openFormLayout() {
		openFormLayout(true);
	}

	public void openFormLayout(boolean selectTabs) {
		if(isOfflineMode())
			openFormLayoutDeffered(selectTabs);
	}

	public void openFormLayoutDeffered(boolean selectTabs) {
		final boolean selectTbs = selectTabs;

		FormUtil.dlg.setText(LocaleText.get("openingFormLayout"));
		FormUtil.dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {
				try{
					centerPanel.openFormLayout(selectTbs);
					FormUtil.dlg.hide();
				}
				catch(Exception ex){
					FormUtil.dlg.hide();
					FormUtil.displayException(ex);
				}	
			}
		});
	}

	public void saveForm() {
		if(!leftPanel.isValidForm())
			return;

		final FormDef obj = leftPanel.getSelectedForm();
		if(obj == null){
			Window.alert(LocaleText.get("selectSaveItem"));
			return;
		}

		if(Context.inLocalizationMode()){
			saveLanguageText(true);
			return;
		}

		FormUtil.dlg.setText(LocaleText.get("savingForm"));
		FormUtil.dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {
				try{
					centerPanel.commitChanges();

					//TODO Need to preserve user's model and any others.
					String xml = null;
					FormDef formDef = obj;
					if(formDef.getDoc() == null){
						xml = XformConverter.fromFormDef2Xform(formDef);
						xml = FormDesignerUtil.formatXml(xml);
					}
					else{
						formDef.updateDoc(false);
						xml = XformConverter.fromDoc2String(formDef.getDoc());
						xml = FormDesignerUtil.formatXml(xml);
					}

					formDef.setXformXml(xml);
					centerPanel.setXformsSource(xml,formId == null);
					centerPanel.buildLayoutXml();
					//formDef.setLayout(centerPanel.getLayoutXml());

					if(!isOfflineMode())
						saveForm(xml,centerPanel.getLayoutXml());

					if(formSaveListener != null)
						formSaveListener.onSaveForm(formDef.getId(), xml, centerPanel.getLayoutXml());

					FormUtil.dlg.hide();
				}
				catch(Exception ex){
					FormUtil.dlg.hide();
					FormUtil.displayException(ex);
					return;
				}	
			}
		});

		saveLanguageText(false); //Save text for the default language
	}

	public void saveFormAs() {
		if(isOfflineMode()){
			final Object obj = leftPanel.getSelectedForm();
			if(obj == null){
				Window.alert(LocaleText.get("selectSaveItem"));
				return;
			}

			FormUtil.dlg.setText(LocaleText.get("savingFormLayout"));
			FormUtil.dlg.center();

			DeferredCommand.addCommand(new Command(){
				public void execute() {
					try{
						String xml = null;
						xml = XformConverter.fromFormDef2Xform((FormDef)obj);
						xml = FormDesignerUtil.formatXml(xml);
						centerPanel.setXformsSource(xml,formId == null);
						FormUtil.dlg.hide();
					}
					catch(Exception ex){
						FormUtil.dlg.hide();
						FormUtil.displayException(ex);
					}	
				}
			});
		}
	}

	public void saveFormLayout() {
		FormUtil.dlg.setText(LocaleText.get("savingFormLayout"));
		FormUtil.dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {
				try{
					centerPanel.saveFormLayout();
					FormUtil.dlg.hide();
				}
				catch(Exception ex){
					FormUtil.dlg.hide();
					FormUtil.displayException(ex);
				}	
			}
		});
	}

	public void showAboutInfo() {
		AboutDialog dlg = new AboutDialog();
		dlg.setAnimationEnabled(true);
		dlg.show();
		dlg.center();
	}

	public void showHelpContents() {
		// TODO Auto-generated method stub

	}

	public void showLanguages() {
		// TODO Auto-generated method stub

	}

	public void showOptions() {
		// TODO Auto-generated method stub

	}

	public void viewToolbar() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignLeft()
	 */
	public void alignLeft() {
		centerPanel.alignLeft();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignRight()
	 */
	public void alignRight() {
		centerPanel.alignRight();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignLeft()
	 */
	public void alignTop() {
		centerPanel.alignTop();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignRight()
	 */
	public void alignBottom() {
		centerPanel.alignBottom();
	}

	public void makeSameHeight() {
		centerPanel.makeSameHeight();
	}

	public void makeSameSize() {
		centerPanel.makeSameSize();
	}

	public void makeSameWidth() {
		centerPanel.makeSameWidth();
	}

	public void copyItem() {
		if(!Context.inLocalizationMode()){
			leftPanel.copyItem();
			centerPanel.copyItem();
		}
	}

	public void cutItem() {
		if(!Context.inLocalizationMode()){
			leftPanel.cutItem();
			centerPanel.cutItem();
		}
	}

	public void pasteItem() {
		if(!Context.inLocalizationMode()){
			leftPanel.pasteItem();
			centerPanel.pasteItem();
		}
	}

	public void refreshItem(){
		if(!Context.inLocalizationMode())
			leftPanel.refreshItem();
	}

	public void format() {
		centerPanel.format();
	}

	public void refresh(Object sender) {
		if(sender instanceof FormsTreeView){ //TODO This controller should not know about LeftPanel implementation details.
			if(formId != null){
				FormUtil.dlg.setText(LocaleText.get("refresingForm"));
				FormUtil.dlg.center();

				DeferredCommand.addCommand(new Command(){
					public void execute() {
						refreshForm();
						FormUtil.dlg.hide();	
					}
				});
			}
			else
				refreshFormDeffered();
		}
		else{
			centerPanel.refresh();
			leftPanel.refresh();
		}
	}

	public void loadForm(int frmId){
		this.formId = frmId;

		String url = FormUtil.getHostPageBaseURL();
		url += FormUtil.getFormDefDownloadUrlSuffix();
		url += FormUtil.getFormIdName()+"="+this.formId;

		//url += "&uname=Guyzb&pw=daniel123";

		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,URL.encode(url));

		try{
			builder.sendRequest(null, new RequestCallback(){
				public void onResponseReceived(Request request, Response response){
					String xml = response.getText();
					if(xml == null || xml.length() == 0){
						Window.alert(LocaleText.get("noDataFound"));
						return;
					}

					String xformXml, layoutXml = null;

					int pos = xml.indexOf(PurcConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR);
					if(pos > 0){
						xformXml = xml.substring(0,pos);
						layoutXml = FormUtil.formatXml(xml.substring(pos+PurcConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR.length(), xml.length()));
					}
					else
						xformXml = xml;

					centerPanel.setXformsSource(FormUtil.formatXml(xformXml),false);
					centerPanel.setLayoutXml(layoutXml,false);
					openFormDeffered(formId);
				}

				public void onError(Request request, Throwable exception){
					exception.printStackTrace();
					Window.alert(exception.getMessage());
				}
			});
		}
		catch(RequestException ex){
			ex.printStackTrace();
			Window.alert(ex.getMessage());
		}
	}

	public void saveForm(String xformXml, String layoutXml){
		String url = FormUtil.getHostPageBaseURL();
		url += FormUtil.getFormDefUploadUrlSuffix();
		url += FormUtil.getFormIdName()+"="+this.formId;

		RequestBuilder builder = new RequestBuilder(RequestBuilder.POST,URL.encode(url));
		//builder.setHeader("Content_type", "application/x-www-form-urlencoded");

		try{
			String xml = xformXml;
			if(layoutXml != null && layoutXml.trim().length() > 0)
				xml += PurcConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR + layoutXml;

			builder.sendRequest(xml, new RequestCallback(){
				public void onResponseReceived(Request request, Response response){
					Window.alert(LocaleText.get("formSaveSuccess"));
				}

				public void onError(Request request, Throwable exception){
					exception.printStackTrace();
					Window.alert(exception.getMessage());
				}
			});
		}
		catch(RequestException ex){
			ex.printStackTrace();
			Window.alert(ex.getMessage());
		}
	}

	public boolean isOfflineMode(){
		return formId == null;
	}

	private void refreshForm(){
		String url = FormUtil.getHostPageBaseURL();
		url += FormUtil.getFormDefRefreshUrlSuffix();
		url += FormUtil.getFormIdName()+"="+this.formId;

		//url += "&uname=Guyzb&pw=daniel123";

		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,URL.encode(url));

		try{
			builder.sendRequest(null, new RequestCallback(){
				public void onResponseReceived(Request request, Response response){
					String xml = response.getText();
					if(xml == null || xml.length() == 0){
						Window.alert(LocaleText.get("noDataFound"));
						return;
					}

					centerPanel.setXformsSource(xml,false);
					refreshFormDeffered();

				}

				public void onError(Request request, Throwable exception){
					exception.printStackTrace();
					Window.alert(exception.getMessage());
				}
			});
		}
		catch(RequestException ex){
			ex.printStackTrace();
			Window.alert(ex.getMessage());
		}
	}

	private void refreshFormDeffered(){
		FormUtil.dlg.setText(LocaleText.get("refreshingForm"));
		FormUtil.dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {
				try{
					String xml = centerPanel.getXformsSource();
					if(xml != null && xml.trim().length() > 0){
						FormDef formDef = XformConverter.fromXform2FormDef(xml);
						formDef.refresh(centerPanel.getFormDef());
						formDef.updateDoc(false);
						xml = formDef.getDoc().toString();

						formDef.setXformXml(FormUtil.formatXml(xml));
						formDef.setLayoutXml(centerPanel.getLayoutXml());

						leftPanel.refresh(formDef);
					}
					FormUtil.dlg.hide();
				}
				catch(Exception ex){
					FormUtil.dlg.hide();
					FormUtil.displayException(ex);
				}
			}
		});
	}

	public void setFormSaveListener(IFormSaveListener formSaveListener){
		this.formSaveListener = formSaveListener;
	}

	public void moveUp(){
		leftPanel.getFormActionListener().moveUp();
	}

	public void moveDown(){
		leftPanel.getFormActionListener().moveUp();
	}

	public void moveToParent(){
		leftPanel.getFormActionListener().moveToParent();
	}

	public void moveToChild(){
		leftPanel.getFormActionListener().moveToChild();
	}

	public void onSetFileContents(String contents) {

		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,"formopen");

		try{
			builder.sendRequest(null, new RequestCallback(){
				public void onResponseReceived(Request request, Response response){
					String contents = response.getText();
					if(contents != null && contents.trim().length() > 0){
						if(centerPanel.isInLayoutMode())
							centerPanel.setLayoutXml(contents, false);
						else{
							centerPanel.setXformsSource(contents, true);
							openForm();
						}
					}
				}

				public void onError(Request request, Throwable exception){
					exception.printStackTrace();
					Window.alert(exception.getMessage());
				}
			});
		}
		catch(RequestException ex){
			ex.printStackTrace();
			Window.alert(ex.getMessage());
		}
	}

	public void saveAs(){
		try{
			String data = (centerPanel.isInLayoutMode() ? centerPanel.getLayoutXml() : centerPanel.getXformsSource());
			if(data == null || data.trim().length() == 0)
				return;

			FormDef formDef = leftPanel.getSelectedForm();
			String fileName = "filename";
			if(formDef != null)
				fileName = formDef.getName();

			if(centerPanel.isInLayoutMode())
				fileName += "-" + LocaleText.get("layout");

			SaveFileDialog dlg = new SaveFileDialog("formsave",data,fileName);
			dlg.center();
		}
		catch(Exception ex){
			FormUtil.displayException(ex);
		}
	}

	public void openLanguageText(){

		FormUtil.dlg.setText(LocaleText.get("translatingFormLanguage"));
		FormUtil.dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {
				try{
					int selFormId = -1; String xml = null; 
					FormDef formDef = leftPanel.getSelectedForm();
					if(formDef != null)
						selFormId = formDef.getId();

					List<FormDef> forms = leftPanel.getForms();
					if(forms != null && forms.size() > 0){
						List<FormDef> newForms = new ArrayList<FormDef>();
						for(FormDef form : forms){
							xml = getFormLocaleText(form.getId(),Context.getLocale());
							if(xml != null){
								String xform = FormUtil.formatXml(LanguageUtil.translate(form.getDoc(), xml, true));
								FormDef newFormDef = XformConverter.fromXform2FormDef(xform);
								newFormDef.setXformXml(xform);
								newFormDef.setLayoutXml(FormUtil.formatXml(LanguageUtil.translate(form.getLayoutXml(), xml, false)));
								newFormDef.setLanguageXml(xml);
								newForms.add(newFormDef);
								
								if(newFormDef.getId() == selFormId)
									formDef = newFormDef;
							}
							else{
								newForms.add(form);
								if(form.getId() == selFormId)
									formDef = form;
							}
						}

						leftPanel.loadForms(newForms, formDef.getId());
					}

					//centerPanel.setLanguageXml(formDef.getLanguageXml(), false);
					//centerPanel.setLayoutXml(formDef.getLayoutXml(), false);

					/*centerPanel.openLanguageXml();

					xml = centerPanel.getXformsSource();
					if(xml != null && xml.trim().length() > 0){
						formDef = XformConverter.fromXform2FormDef(xml);
						formDef.setXformXml(xml);
						//formDef.setLayoutXml(orgFormDef.getLayoutXml()); 
						//formDef.setLanguageXml(orgFormDef.getLanguageXml());
						formDef.setLayoutXml(centerPanel.getLayoutXml()); 
						formDef.setLanguageXml(centerPanel.getLanguageXml());

						leftPanel.refresh(formDef);

						//languageText.put(Context.getLocale(), centerPanel.getLanguageXml());
						setLocaleText(formDef.getId(),Context.getLocale(), centerPanel.getLanguageXml());
					}*/

					FormUtil.dlg.hide();

					String layoutXml = centerPanel.getLayoutXml();
					if(layoutXml != null && layoutXml.trim().length() > 0)
						openFormLayout(false);
				}
				catch(Exception ex){
					FormUtil.dlg.hide();
					FormUtil.displayException(ex);
				}
			}
		});
	}

	public void saveLanguageText(boolean selectTab){
		saveLanguageTextDeffered(selectTab);
	}

	public void saveLanguageText(){
		saveLanguageTextDeffered(true);
	}

	public void saveLanguageTextDeffered(boolean selectTab){
		final boolean selTab = selectTab;

		FormUtil.dlg.setText(LocaleText.get("savingLanguageText"));
		FormUtil.dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {
				try{
					centerPanel.saveLanguageText(selTab);
					setLocaleText(centerPanel.getFormDef().getId(),Context.getLocale(), centerPanel.getLanguageXml());
					//languageText.put(Context.getLocale(), centerPanel.getLanguageXml());

					if(formSaveListener != null){
						FormDef formDef = centerPanel.getFormDef();
						String langXml = formDef.getLanguageXml();
						if(langXml != null && langXml.trim().length() > 0){
							Document doc = XMLParser.parse(langXml);
							formSaveListener.onSaveLocaleText(formDef.getId(), LanguageUtil.getXformsLocaleText(doc), LanguageUtil.getLayoutLocaleText(doc));
						}
					}

					FormUtil.dlg.hide();
				}
				catch(Exception ex){
					FormUtil.dlg.hide();
					FormUtil.displayException(ex);
				}	
			}
		});
	}

	public void changeLocale(String locale){
		Context.setLocale(locale);

		String xml = null;
		FormDef formDef = centerPanel.getFormDef();
		if(formDef != null){
			HashMap<String,String> map = languageText.get(formDef.getId());
			if(map != null)
				xml = map.get(locale);

			if(xml == null && map != null)
				xml = map.get(Context.getDefaultLocale());
		}

		centerPanel.setLanguageXml(xml, false);
		openLanguageText();
	}

	private void setLocaleText(Integer formId, String locale, String text){
		HashMap<String,String> map = languageText.get(formId);
		if(map == null){
			map = new HashMap<String,String>();
			languageText.put(formId, map);
		}

		map.put(locale, text);
	}

	private String getFormLocaleText(int formId, String locale){
		HashMap<String,String> map = languageText.get(formId);
		if(map != null)
			return map.get(locale);
		return null;
	}

	public void setLocaleText(Integer formId, String locale, String xform, String layout){
		setLocaleText(formId,locale, LanguageUtil.getLocaleText(xform, layout));
		//languageText.put(locale, LanguageUtil.getLocaleText(xform, layout));
	}

	public void setDefaultLocale(String locale){
		Context.setDefaultLocale(locale);
	}
}
