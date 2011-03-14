package org.openrosa.client.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openrosa.client.Context;
import org.openrosa.client.model.FormDef;
import org.openrosa.client.model.IFormElement;
import org.openrosa.client.util.ItextLocale;
import org.openrosa.client.view.CenterPanel;
import org.openrosa.client.view.SaveFileDialog;
import org.openrosa.client.view.Toolbar;
import org.openrosa.client.xforms.XformBuilder;
import org.openrosa.client.xforms.XformParser;
import org.openrosa.client.xforms.XhtmlBuilder;
import org.openrosa.client.PurcConstants;
import org.openrosa.client.controller.IFormDesignerListener;
import org.openrosa.client.controller.IFormSaveListener;
import org.openrosa.client.controller.OpenFileDialogEventListener;
import org.openrosa.client.locale.LocaleText;
import org.openrosa.client.model.ModelConstants;
import org.openrosa.client.util.FormDesignerUtil;
import org.openrosa.client.util.FormUtil;
import org.openrosa.client.util.Itext;
import org.openrosa.client.util.LanguageUtil;
import org.openrosa.client.view.FormsTreeView;
import org.openrosa.client.xforms.XformUtil;
import org.openrosa.client.xforms.XmlUtil;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;


/**
* Controls the interactions between the menu, tool bar and 
* various views (eg Left and Center panels) for the form designer.
* 
*
*/
public class FormDesignerController implements IFormDesignerListener, OpenFileDialogEventListener{

	/** The panel on the right hand side of the form designer. */
	private static CenterPanel centerPanel;

	/** The panel on the left hand side of the form designer. */
	private FormsTreeView formsTreeView;
	
	private Toolbar toolbar;

	/**
	 * The identifier of the loaded or opened form.
	 */
	private Integer formId;	

	/**
	 * The listener to form save events.
	 */
	private IFormSaveListener formSaveListener;

	/** Action for saving form. */
	private static final byte CA_SAVE_FORM = 2;

	/** Action for refreshing a form. */
	private static final byte CA_REFRESH_FORM = 3;

	public static byte currentAction = 0;
	/** The object that is being refreshed. */
	private Object refreshObject;
	
	private static FormDesignerController fdc;

	
	/**
	 * Constructs a new instance of the form designer controller.
	 * 
	 * @param centerPanel the right hand side panel.
	 * @param leftPanel the left hand side panel.
	 */
	public FormDesignerController(CenterPanel centerPanel, FormsTreeView treeView){
		this.formsTreeView = treeView;
		this.centerPanel = centerPanel;

		this.centerPanel.setFormDesignerListener(this);
		fdc = this;

	}
	
	public void setToolbar(Toolbar toolbar){
		this.toolbar = toolbar;
	}
	
	public void alertToolbarQuestionAdded(IFormElement question){
		toolbar.checkEnableAddSelect(question);
	}

	public void changePropertiesViewSelection(IFormElement objectDef){
		centerPanel.getPropertiesView().changeSelectedObject(objectDef);
	}
	
	public static FormDesignerController getFormDesignerController(){
		return fdc;
	}
	
	
	/**
	 * @see org.openrosa.client.controller.IFormActionListener#addNewItem()
	 */
	public void addNewItem() {
		formsTreeView.addNewItem();
	}

	public void refreshTreeView(){
		formsTreeView.refreshForm(Context.getFormDef());
	}
	
	/**
	 * @see org.openrosa.client.controller.IFormActionListener#addNewChildItem()
	 */
	public void addNewChildItem() {
		formsTreeView.addNewChildItem();
	}

	/**
	 * @see org.openrosa.client.controller.IFormActionListener#addNewQuestion()
	 */
	public void addNewQuestion(int dataType){
		//HERE IS WHERE A NEW QUESTION SHOULD BE CREATED
		// - Create Question
		// - Add to FormDef
		// - Add to FormsTreeView
		
		
		
		formsTreeView.addNewQuestion(dataType);
	}
	
	
	/**
	 * @see org.openrosa.client.controller.IFormDesignerController#printForm()
	 */
	public void printForm(){
		FormDef formDef = centerPanel.getFormDef();
		if(formDef != null)
			openForm(formDef.getName(), centerPanel.getFormInnerHtml());
	}
	

	/**
	 * Opens a new browser window with a given title and html contents.
	 * 
	 * @param title the window title.
	 * @param html the window html contents.
	 */
	public static native void openForm(String title,String html) /*-{
		 var win =window.open('','purcforms','width=350,height=250,menubar=1,toolbar=1,status=1,scrollbars=1,resizable=1');
		 win.document.open("text/html","replace");
		 win.document.writeln('<html><head><title>' + title + '</title></head><body bgcolor=white onLoad="self.focus()">'+html+'</body></html>');
		 win.document.close();
	}-*/;

	/**
	 * @see org.openrosa.client.controller.IFormDesignerController#closeForm()
	 */
	public void closeForm() {
		String url = FormUtil.getCloseUrl();
		if(url != null && url.trim().length() > 0)
			Window.Location.replace(url);
	} 

	/**
	 * @see org.openrosa.client.controller.IFormActionListener#deleteSelectedItems()
	 */
	public void deleteSelectedItem() {
		if(Context.getCurrentMode() == Context.MODE_QUESTION_PROPERTIES)
			formsTreeView.deleteSelectedItem();	
		else
			centerPanel.deleteSelectedItem();
	}

	/**
	 * @see org.openrosa.client.controller.IFormActionListener#moveItemDown()
	 */
	public void moveItemDown() {
		formsTreeView.moveItemDown();
	}

	/**
	 * @see org.openrosa.client.controller.IFormActionListener#moveItemUp()
	 */
	public void moveItemUp() {
		formsTreeView.moveItemUp();
	}

//	/**
//	 * @see org.openrosa.client.controller.IFormActionListener#newForm()
//	 */
//	public void newForm() {
//		if(isOfflineMode())
//			formsTreeView.addNewForm();
//	}


//	/**
//	 * Loads a form from the Xforms Source tab, in a deferred command.
//	 * 
//	 * @param id the form identifier.
//	 * @param readonly set to true to load the form in read only mode.
//	 */
//	public void openFormDeffered(int id, boolean readonly) {
//		final int tempFormId = id;
//		final boolean tempReadonly = readonly;
//
//		FormUtil.dlg.setText(LocaleText.get("openingForm"));
//		FormUtil.dlg.center();
//
//		DeferredCommand.addCommand(new Command(){
//			public void execute() {
//				try{
//					String xml = centerPanel.getXformsSource().trim();
//					if(xml.length() > 0){
//						FormDef formDef = XformParser.fromXform2FormDef(xml,Context.getLanguageText());
//						formDef.setReadOnly(tempReadonly);
//
//						if(tempFormId != ModelConstants.NULL_ID)
//							formDef.setId(tempFormId);
//
//						if(formDef.getLayoutXml() != null)
//							centerPanel.setLayoutXml(formDef.getLayoutXml(), false);
//						else{
//							//Could be from form runner which puts these contents in center panel
//							//because it does not yet have a formdef by the time it has this data.
//							formDef.setXformXml(centerPanel.getXformsSource());
//							formDef.setLayoutXml(centerPanel.getLayoutXml());
//						}
//						
//						if(formDef.getJavaScriptSource() != null)
//							centerPanel.setJavaScriptSource(formDef.getJavaScriptSource());
//						else
//							formDef.setJavaScriptSource(centerPanel.getJavaScriptSource());
//
//						//TODO May also need to refresh UI if form was not stored in default lang.
//						HashMap<String,String> locales = Context.getLanguageText().get(formDef.getId());
//						if(locales != null){
//							formDef.setLanguageXml(FormUtil.formatXml(locales.get(Context.getLocale())));
//							centerPanel.setLanguageXml(formDef.getLanguageXml(), false);
//						}
//
//						leftPanel.loadForm(formDef);
//						centerPanel.loadForm(formDef,formDef.getLayoutXml());
//						centerPanel.format();
//					}
//					FormUtil.dlg.hide();
//				}
//				catch(Exception ex){
//					FormUtil.dlg.hide();
//					FormUtil.displayException(ex);
//				}	
//			}
//		});
//	}


	/**
	 * Loads the form widget layout in the Layout Xml tab.
	 * 
	 * @param selectTabs set to true to select the layout xml tab.
	 */
	public void openFormLayout(boolean selectTabs) {
		openFormLayoutDeffered(selectTabs);
	}

	/**
	 * Loads the form widget layout in the Layout Xml tab, in a deferred command.
	 * 
	 * @param selectTabs set to true to select the layout xml tab.
	 */
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

//	/**
//	 * @see org.openrosa.client.controller.IFormDesignerController#saveForm()
//	 */
//	public void saveForm(){
//			saveTheForm();
//	}

//	private void saveTheForm() {
//		final FormDef obj = formsTreeView.getSelectedForm();
//
//		if(obj == null){
//			Window.alert(LocaleText.get("selectSaveItem"));
//			return;
//		}
//
//		if(Context.inLocalizationMode()){
//			saveLanguageText();
//			return;
//		}
//
//		FormUtil.dlg.setText(LocaleText.get("savingForm"));
//		FormUtil.dlg.center();
//
//		DeferredCommand.addCommand(new Command(){
//			public void execute() {
//				try{
//					centerPanel.commitChanges();
//
//					//TODO Need to preserve user's model and any others.
//					String xml = null;
//					FormDef formDef = obj;
//					if(formDef.getDoc() == null)
//						xml = XformBuilder.fromFormDef2Xform(formDef);
//					else{
//						formDef.updateDoc(false);
//						xml = XmlUtil.fromDoc2String(formDef.getDoc());
//					}
//
//					xml = XformUtil.normalizeNameSpace(formDef.getDoc(), xml);
//
//					xml = FormDesignerUtil.formatXml(xml);
//
//					formDef.setXformXml(xml);
//					/*centerPanel.setXformsSource(xml,formSaveListener == null && isOfflineMode());
//					centerPanel.buildLayoutXml();
//					formDef.setLayout(centerPanel.getLayoutXml());
//
//					centerPanel.saveLanguageText(false);
//					setLocaleText(formDef.getId(),Context.getLocale(), centerPanel.getLanguageXml());
//
//					centerPanel.saveJavaScriptSource();
//					
//					if(!isOfflineMode() && formSaveListener == null)
//						saveForm(xml,centerPanel.getLayoutXml(),PurcFormBuilder.getCombinedLanguageText(languageText.get(formDef.getId())),centerPanel.getJavaScriptSource());
//
//					boolean saveLocaleText = false;
//					if(formSaveListener != null)
//						saveLocaleText = formSaveListener.onSaveForm(formDef.getId(), xml, centerPanel.getLayoutXml());*/
//
//					if(isOfflineMode() || formSaveListener != null)
//						FormUtil.dlg.hide();
//
//					//Save text for the current language
//					//if(saveLocaleText)
//					//	saveTheLanguageText(false,false);
//				}
//				catch(Exception ex){
//					FormUtil.dlg.hide();
//					FormUtil.displayException(ex);
//					return;
//				}	
//			}
//		});
//	}


//	public void saveFormAs() {
//		if(isOfflineMode()){
//			final Object obj = formsTreeView.getSelectedForm();
//			if(obj == null){
//				Window.alert(LocaleText.get("selectSaveItem"));
//				return;
//			}
//
//			FormUtil.dlg.setText(LocaleText.get("savingForm"));
//			FormUtil.dlg.center();
//
//			DeferredCommand.addCommand(new Command(){
//				public void execute() {
//					try{
//						String xml = null;
//						xml = XformBuilder.fromFormDef2Xform((FormDef)obj);
//						xml = FormDesignerUtil.formatXml(xml);
//						centerPanel.setXformsSource(xml,formSaveListener == null && isOfflineMode());
//						FormUtil.dlg.hide();
//					}
//					catch(Exception ex){
//						FormUtil.dlg.hide();
//						FormUtil.displayException(ex);
//					}	
//				}
//			});
//		}
//		else
//			saveAs();
//	}



	public void showAboutInfo() {
//		AboutDialog dlg = new AboutDialog();
//		dlg.setAnimationEnabled(true);
//		dlg.center();
	}

	/**
	 * @see org.openrosa.client.controller.IFormDesignerController#alignLeft()
	 */
	public void showHelpContents() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.openrosa.client.controller.IFormDesignerController#showLanguages()
	 */
	public void showLanguages() {
//		LocalesDialog dlg = new LocalesDialog();
//		dlg.center();
		
		
		//Appears unused
	}

	/**
	 * @see org.openrosa.client.controller.IFormDesignerController#showOptions()
	 */
	public void showOptions() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.openrosa.client.controller.IFormDesignerController#viewToolbar()
	 */
	public void viewToolbar() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.openrosa.client.controller.IFormDesignerController#alignLeft()
	 */
	public void alignLeft() {
		centerPanel.alignLeft();
	}

	/**
	 * @see org.openrosa.client.controller.IFormDesignerController#alignRight()
	 */
	public void alignRight() {
		centerPanel.alignRight();
	}

	/**
	 * @see org.openrosa.client.controller.IFormDesignerController#alignTop()
	 */
	public void alignTop() {
		centerPanel.alignTop();
	}

	/**
	 * @see org.openrosa.client.controller.IFormDesignerController#alignBottom()
	 */
	public void alignBottom() {
		centerPanel.alignBottom();
	}

	/**
	 * @see org.openrosa.client.controller.IFormDesignerController#makeSameHeight()
	 */
	public void makeSameHeight() {
		centerPanel.makeSameHeight();
	}

	/**
	 * @see org.openrosa.client.controller.IFormDesignerController#makeSameSize()
	 */
	public void makeSameSize() {
		centerPanel.makeSameSize();
	}

	/**
	 * @see org.openrosa.client.controller.IFormDesignerController#makeSameWidth()
	 */
	public void makeSameWidth() {
		centerPanel.makeSameWidth();
	}

	/**
	 * @see org.openrosa.client.controller.IFormActionListener#copyItem()
	 */
	public void copyItem() {
		if(!Context.isStructureReadOnly()){
			if(Context.getCurrentMode() == Context.MODE_QUESTION_PROPERTIES)
				formsTreeView.copyItem();
			else
				centerPanel.copyItem();
		}
	}

	/**
	 * @see org.openrosa.client.controller.IFormActionListener#cutItem()
	 */
	public void cutItem() {
		if(!Context.isStructureReadOnly()){
			if(Context.getCurrentMode() == Context.MODE_QUESTION_PROPERTIES)
				formsTreeView.cutItem();
			else
				centerPanel.cutItem();
		}
	}

	/**
	 * @see org.openrosa.client.controller.IFormActionListener#pasteItem()
	 */
	public void pasteItem() {
		if(!Context.isStructureReadOnly()){
			if(Context.getCurrentMode() == Context.MODE_QUESTION_PROPERTIES)
				formsTreeView.pasteItem();
			else
				centerPanel.pasteItem();
		}
	}

//	/**
//	 * @see org.openrosa.client.controller.IFormActionListener#refreshItem()
//	 */
//	public void refreshItem(){
//		if(!Context.isStructureReadOnly())
//			formsTreeView.refreshItem();
//	}

	/**
	 * @see org.openrosa.client.controller.IFormDesignerController#format()
	 */
	public void format() {
		centerPanel.format();
	}

//	private void refreshObject() {
//
//		//If the center panel's current mode does not allow refreshes 
//		//or the forms tree view is the one which has requested a refresh.
//		if(!centerPanel.allowsRefresh() || refreshObject instanceof FormsTreeView ||
//				Context.getCurrentMode() == Context.MODE_XFORMS_SOURCE){ //TODO This controller should not know about LeftPanel implementation details.
//
//			if(formId != null){
//				FormUtil.dlg.setText(LocaleText.get("refreshingForm"));
//				FormUtil.dlg.center();
//
//				DeferredCommand.addCommand(new Command(){
//					public void execute() {
//						refreshForm();
//						FormUtil.dlg.hide();	
//					}
//				});
//			}
//			else
//				refreshFormDeffered();
//		}
//		else
//			centerPanel.refresh();
//	}

//	public void refresh(Object sender) {
//		refreshObject = sender;
//
//		if(isOfflineMode())
//			refreshObject();
//		else{
//			currentAction = CA_REFRESH_FORM;
//			FormUtil.isAuthenticated();
//		}
//	}

	public void saveForm(String xformXml, String layoutXml, String languageXml, String javaScriptSrc){
		String url = FormUtil.getHostPageBaseURL();
		url += FormUtil.getFormDefUploadUrlSuffix();
		url += FormUtil.getFormIdName()+"="+this.formId;

		RequestBuilder builder = new RequestBuilder(RequestBuilder.POST,URL.encode(url));

		try{
			String xml = xformXml;
			if(layoutXml != null && layoutXml.trim().length() > 0)
				xml += PurcConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR + layoutXml;

			if(languageXml != null && languageXml.trim().length() > 0)
				xml += PurcConstants.PURCFORMS_FORMDEF_LOCALE_XML_SEPARATOR + languageXml;
			
			if(javaScriptSrc != null && javaScriptSrc.trim().length() > 0)
				xml += PurcConstants.PURCFORMS_FORMDEF_JAVASCRIPT_SRC_SEPARATOR + javaScriptSrc;

			builder.sendRequest(xml, new RequestCallback(){
				public void onResponseReceived(Request request, Response response){
					FormUtil.dlg.hide();
					Window.alert(LocaleText.get("formSaveSuccess"));
				}

				public void onError(Request request, Throwable exception){
					FormUtil.dlg.hide();
					FormUtil.displayException(exception);
				}
			});
		}
		catch(RequestException ex){
			FormUtil.dlg.hide();
			FormUtil.displayException(ex);
		}
	}

	public void saveLocaleText(String languageXml){
		String url = FormUtil.getHostPageBaseURL();
		url += FormUtil.getFormDefUploadUrlSuffix();
		url += FormUtil.getFormIdName()+"="+this.formId;
		url += "&localeXml=true";

		RequestBuilder builder = new RequestBuilder(RequestBuilder.POST,URL.encode(url));

		try{
			builder.sendRequest(languageXml, new RequestCallback(){
				public void onResponseReceived(Request request, Response response){
					FormUtil.dlg.hide();
					Window.alert(LocaleText.get("formSaveSuccess"));
				}

				public void onError(Request request, Throwable exception){
					FormUtil.dlg.hide();
					FormUtil.displayException(exception);
				}
			});
		}
		catch(RequestException ex){
			FormUtil.dlg.hide();
			FormUtil.displayException(ex);
		}
	}

//	/**
//	 * Checks if the form designer is in offline mode.
//	 * 
//	 * @return true if in offline mode, else false.
//	 */
//	public boolean isOfflineMode(){
//		return formId == null;
//	}

//	private void refreshForm(){
//		String url = FormUtil.getHostPageBaseURL();
//		url += FormUtil.getFormDefRefreshUrlSuffix();
//		url += FormUtil.getFormIdName()+"="+this.formId;
//
//		//url += "&uname=Guyzb&pw=daniel123";
//
//		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,URL.encode(url));
//
//		try{
//			builder.sendRequest(null, new RequestCallback(){
//				public void onResponseReceived(Request request, Response response){
//					String xml = response.getText();
//					if(xml == null || xml.length() == 0){
//						Window.alert(LocaleText.get("noDataFound"));
//						return;
//					}
//
//					centerPanel.setXformsSource(xml,false);
//					refreshFormDeffered();
//				}
//
//				public void onError(Request request, Throwable exception){
//					FormUtil.displayException(exception);
//				}
//			});
//		}
//		catch(RequestException ex){
//			FormUtil.displayException(ex);
//		}
//	}

//	/**
//	 * Refreshes the selected from in a deferred command.
//	 */
//	private void refreshFormDeffered(){
//		FormUtil.dlg.setText(LocaleText.get("refreshingForm"));
//		FormUtil.dlg.center();
//
//		DeferredCommand.addCommand(new Command(){
//			public void execute() {
//				try{
//					String xml = centerPanel.getXformsSource();
//					if(xml != null && xml.trim().length() > 0){
//						FormDef formDef = XformParser.fromXform2FormDef(xml);
//
//						FormDef oldFormDef = centerPanel.getFormDef();
//
//						//If we are in offline mode, we completely overwrite the form 
//						//with the contents of the xforms source tab.
//						if(!isOfflineMode())
//							formDef.refresh(oldFormDef);
//
//						formDef.updateDoc(false);
//						xml = formDef.getDoc().toString();
//
//						formDef.setXformXml(FormUtil.formatXml(xml));
//
//						formDef.setLayoutXml(oldFormDef.getLayoutXml());
//						formDef.setLanguageXml(oldFormDef.getLanguageXml());
//
//						formsTreeView.refreshForm(formDef);
//						centerPanel.refresh();
//					}
//					FormUtil.dlg.hide();
//				}
//				catch(Exception ex){
//					FormUtil.dlg.hide();
//					FormUtil.displayException(ex);
//				}
//			}
//		});
//	}

	/**
	 * Sets the listener to form save events.
	 * 
	 * @param formSaveListener the listener.
	 */
	public void setFormSaveListener(IFormSaveListener formSaveListener){
		this.formSaveListener = formSaveListener;
	}

	/**
	 * @see org.openrosa.client.controller.IFormActionListener#moveUp()
	 */
	public void moveUp(){
		formsTreeView.moveUp();
	}

	/**
	 * @see org.openrosa.client.controller.IFormActionListener#moveDown()
	 */
	public void moveDown(){
		formsTreeView.moveUp();
	}

	/**
	 * @see org.openrosa.client.controller.IFormActionListener#moveToParent()
	 */
	public void moveToParent(){
		formsTreeView.moveToParent();
	}

	/**
	 * @see org.openrosa.client.controller.IFormActionListener#moveToChild()
	 */
	public void moveToChild(){
		formsTreeView.moveToChild();
	}


	public void saveAs(){
		try{
			String data = (centerPanel.isInLayoutMode() ? centerPanel.getLayoutXml() : centerPanel.getXformsSource());
			if(data == null || data.trim().length() == 0)
				return;

			FormDef formDef = formsTreeView.getSelectedForm();
			String fileName = "filename";
			if(formDef != null)
				fileName = formDef.getName();

			if(centerPanel.isInLayoutMode())
				fileName += "-" + LocaleText.get("layout");

			SaveFileDialog dlg = new SaveFileDialog(FormUtil.getFileSaveUrl(),data,fileName);
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
					FormDef formDef = formsTreeView.getSelectedForm();
					if(formDef != null)
						selFormId = formDef.getId();

					List<FormDef> forms = formsTreeView.getForms();
					if(forms != null && forms.size() > 0){
						List<FormDef> newForms = new ArrayList<FormDef>();
						for(FormDef form : forms){
							xml = null;//getFormLocaleText(form.getId(),Itext.currentLocale.getName());
							if(xml != null){
								String xform = FormUtil.formatXml(LanguageUtil.translate(form.getDoc(), xml, true));
								FormDef newFormDef = XformParser.fromXform2FormDef(xform);
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

						formsTreeView.loadForms(newForms, formDef.getId());
					}

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

	/**
	 * Saves locale text for the selected form, in a deferred command.
	 * 
	 * @param selectTab set to true to select the language tab.
	 */
	public void saveLanguageText(){
		FormUtil.dlg.setText(LocaleText.get("savingLanguageText"));
		FormUtil.dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {
				saveTheLanguageText();
			}
		});
	}


	/**
	 * Saves locale text for the selected form, in a non deferred command.
	 * 
	 * @param selectTab set to true to select the language tab.
	 * @param rebuild set to true to rebuild the language xml.
	 */
	public void saveTheLanguageText(){
//			throw new Exception("Implement me!!");
//			FormUtil.displayException(ex);
			
	}


//	/**
//	 * Reloads forms in a given locale.
//	 * 
//	 * @param locale the locale.
//	 */
//	public boolean changeLocale(final ItextLocale locale){
//
//		final FormDef formDef = centerPanel.getFormDef();
//		if(formDef == null)
//			return false;
//
//		//We need to have saved a form in order to translate it.
//		if(formDef.getDoc() == null)
//			saveForm();
//		else if(!Window.confirm(LocaleText.get("localeChangePrompt")))
//			return false;
//
//		//We need to do the translation in a differed command such that it happens after form saving,
//		//just in case form hadn't yet been saved.
//		DeferredCommand.addCommand(new Command(){
//			public void execute() {
//
//				//Store the new locale.
//				Itext.setCurrentLocale(locale);
//
//				HashMap<String,String> map = Context.getLanguageText().get(formDef.getId());
//
//				String xml = null;
//				//Get text for this locale, if we have it. 
//				if(map != null)
//					xml = map.get(locale.getName());
//
//				//If we don't, then get text for the default locale.
//				if(xml == null && map != null)
//					xml = map.get(Itext.getDefaultLocale().getName());
//
//				//Now reload the forms in this selected locale.
//				centerPanel.setLanguageXml(xml, false);
//				openLanguageText();
//			}
//		});
//
//		return true;
//	}

//	/**
//	 * Sets locale text for a given form.
//	 * 
//	 * @param formId the form identifier.
//	 * @param locale the locale key.
//	 * @param text the form locale text.
//	 */
//	private void setLocaleText(Integer formId, String locale, String text){
//		HashMap<String,String> map = Context.getLanguageText().get(formId);
//		if(map == null){
//			map = new HashMap<String,String>();
//			Context.getLanguageText().put(formId, map);
//		}
//
//		map.put(locale, text);
//	}

//	/**
//	 * Gets locale text for a given form.
//	 * 
//	 * @param formId the form identifier.
//	 * @param locale  the locale key.
//	 * @return the form locale text.
//	 */
//	private String getFormLocaleText(int formId, String locale){
//		HashMap<String,String> map = Context.getLanguageText().get(formId);
//		if(map != null)
//			return map.get(locale);
//		return null;
//	}
//
//	/**
//	 * Sets xforms and layout locale text for a given form.
//	 * 
//	 * @param formId the form identifier.
//	 * @param locale the locale key.
//	 * @param xform the xforms locale text.
//	 * @param layout the layout locale text.
//	 */
//	public void setLocaleText(Integer formId, String locale, String xform, String layout){
//		setLocaleText(formId,locale, LanguageUtil.getLocaleText(xform, layout));
//	}

//	/**
//	 * Embeds the selected xform into xhtml.
//	 */
//	public void saveAsXhtml(){
//		if(!isOfflineMode())
//			return;
//
//		final Object obj = formsTreeView.getSelectedForm();
//		if(obj == null){
//			Window.alert(LocaleText.get("selectSaveItem"));
//			return;
//		}
//
//		FormUtil.dlg.setText(LocaleText.get("savingForm"));
//		FormUtil.dlg.center();
//
//		DeferredCommand.addCommand(new Command(){
//			public void execute() {
//				try{
//					FormDef formDef = new FormDef((FormDef)obj);
//					formDef.setDoc(((FormDef)obj).getDoc()); //We want to copy the model xml
//					String xml = XhtmlBuilder.fromFormDef2Xhtml(formDef);
//					xml = FormDesignerUtil.formatXml(xml);
//					centerPanel.setXformsSource(xml,formSaveListener == null && isOfflineMode());
//					FormUtil.dlg.hide();
//				}
//				catch(Exception ex){
//					FormUtil.dlg.hide();
//					FormUtil.displayException(ex);
//				}	
//			}
//		});
//	}


//	public void saveAsPurcForm(){
//		//if(!isOfflineMode())
//		//	return;
//
//		/*if(isOfflineMode())
//			saveTheForm();
//
//		DeferredCommand.addCommand(new Command(){
//			public void execute() {
//
//				FormUtil.dlg.setText(LocaleText.get("savingForm"));
//				FormUtil.dlg.center();
//
//				DeferredCommand.addCommand(new Command(){
//					public void execute() {
//						try{
//							FormDef formDef = leftPanel.getSelectedForm();
//							String xml = PurcFormBuilder.build(formDef, languageText.get(formDef.getId()));
//							xml = FormDesignerUtil.formatXml(xml);
//
//							FormUtil.dlg.hide();
//
//							if(isOfflineMode())
//								centerPanel.setXformsSource(xml,formSaveListener == null && isOfflineMode());
//							else{
//								SaveFileDialog dlg = new SaveFileDialog(FormUtil.getFileSaveUrl(),xml,formDef.getName());
//								dlg.center();
//							}
//						}
//						catch(Exception ex){
//							FormUtil.dlg.hide();
//							FormUtil.displayException(ex);
//						}	
//					}
//				});
//
//			}
//		});*/
//	}


	@Override
	public boolean handleKeyBoardEvent(Event event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void openFormLayout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSetFileContents(String contents) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void openForm() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveFormLayout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean changeLocale(ItextLocale locale) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void refresh(Object sender) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveAsPurcForm() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveAsXhtml() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveForm() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveFormAs() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refreshItem() {
		// TODO Auto-generated method stub
		
	}
}
