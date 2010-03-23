package org.purc.purcforms.client;

import java.util.ArrayList;
import java.util.List;

import org.purc.purcforms.client.LeftPanel.Images;
import org.purc.purcforms.client.controller.ICenterPanel;
import org.purc.purcforms.client.controller.IFormActionListener;
import org.purc.purcforms.client.controller.IFormChangeListener;
import org.purc.purcforms.client.controller.IFormDesignerListener;
import org.purc.purcforms.client.controller.IFormSelectionListener;
import org.purc.purcforms.client.controller.LayoutChangeListener;
import org.purc.purcforms.client.controller.SubmitListener;
import org.purc.purcforms.client.controller.WidgetPropertyChangeListener;
import org.purc.purcforms.client.controller.WidgetSelectionListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.util.LanguageUtil;
import org.purc.purcforms.client.view.DesignSurfaceView;
import org.purc.purcforms.client.view.PreviewView;
import org.purc.purcforms.client.view.PropertiesView;
import org.purc.purcforms.client.widget.RuntimeWidgetWrapper;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventPreview;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;


/**
 * Panel containing the contents on the form being designed.
 * 
 * @author daniel
 *
 */
public class CenterPanel extends Composite implements SelectionHandler<Integer>, IFormSelectionListener, SubmitListener, LayoutChangeListener, ICenterPanel{

	/** Index for the properties tab. */
	private int SELECTED_INDEX_PROPERTIES = 0;

	/** Index for xforms source xml tab. */
	private int SELECTED_INDEX_XFORMS_SOURCE = 1;

	/** Index for the design surface tab. */
	private int SELECTED_INDEX_DESIGN_SURFACE = 2;
	
	/** Index for the javascript source tab. */
	private int SELECTED_INDEX_JAVASCRIPT_SOURCE = 3;

	/** Index for the layout xml tab. */
	private int SELECTED_INDEX_LAYOUT_XML = 4;

	/** Index for the locale or language xml tab. */
	private int SELECTED_INDEX_LANGUAGE_XML = 5;

	/** Index for the preview tab. */
	private int SELECTED_INDEX_PREVIEW = 6;

	/** Index for the model xml tab. */
	private int SELECTED_INDEX_MODEL_XML = 7;

	private boolean showXformsSource = true;
	private boolean showJavaScriptSource = true;
	private boolean showLayoutXml = true;
	private boolean showLanguageXml = true;
	private boolean showModelXml = true;


	/**
	 * Tab widget housing the contents.
	 */
	private DecoratedTabPanel tabs = new DecoratedTabPanel();

	/**
	 * TextArea displaying the XForms xml.
	 */
	private TextArea txtXformsSource = new TextArea();

	/**
	 * The view displaying form item properties.
	 */
	private PropertiesView propertiesView = new PropertiesView();

	/**
	 * View onto which user drags and drops ui controls in a WUSIWUG manner.
	 */
	private DesignSurfaceView designSurfaceView;
	
	/** The text area which contains javascript source. */
	private TextArea txtJavaScriptSource = new TextArea();

	/** The text area which contains layout xml. */
	private TextArea txtLayoutXml = new TextArea();

	/** The text area which contains model xml. */
	private TextArea txtModelXml = new TextArea();

	/** The text area which contains locale or language xml. */
	private TextArea txtLanguageXml = new TextArea();

	/**
	 * View used to display a form as it will look when the user is entering data in non-design mode.
	 */
	private PreviewView previewView;

	/** The form defintion object thats is currently being edited. */
	private FormDef formDef;

	/** The index of the selected tab. */
	private int selectedTabIndex = 0;	

	/** Scroll panel for the design surface. */
	private ScrollPanel scrollPanelDesign = new ScrollPanel();

	/** Scroll panel for the preview surface. */
	private ScrollPanel scrollPanelPreview = new ScrollPanel();

	/** Listener to form designer global events. */
	private IFormDesignerListener formDesignerListener;


	/**
	 * Constructs a new center panel widget.
	 * 
	 * @param images
	 */
	public CenterPanel(Images images) {		
		designSurfaceView = new DesignSurfaceView(images);
		previewView = new PreviewView((PreviewView.Images)images);

		initProperties();
		initXformsSource();
		initDesignSurface();
		initJavaScriptSource();
		initLayoutXml();
		initLanguageXml();
		initPreview();
		initModelXml();

		FormUtil.maximizeWidget(tabs);

		tabs.selectTab(0);
		initWidget(tabs);
		tabs.addSelectionHandler(this);

		Context.setCurrentMode(Context.MODE_QUESTION_PROPERTIES);

		previewEvents();
	}


	/**
	 * @see com.google.gwt.user.client.DOM#addEventPreview(EventPreview)
	 */
	private void previewEvents(){

		DOM.addEventPreview(new EventPreview() { 
			public boolean onEventPreview(Event event) 
			{ 				
				if (DOM.eventGetType(event) == Event.ONKEYDOWN) {
					byte mode = Context.getCurrentMode();

					if(mode == Context.MODE_DESIGN)
						return designSurfaceView.handleKeyBoardEvent(event);
					else if(mode == Context.MODE_PREVIEW)
						return previewView.handleKeyBoardEvent(event);
					else if(mode == Context.MODE_QUESTION_PROPERTIES || mode == Context.MODE_XFORMS_SOURCE)
						return formDesignerListener.handleKeyBoardEvent(event);
				}

				return true;
			}
		});
	}


	/**
	 * Sets the listener to form item property change events.
	 * 
	 * @param formChangeListener the listener.
	 */
	public void setFormChangeListener(IFormChangeListener formChangeListener){
		propertiesView.setFormChangeListener(formChangeListener);
	}

	/**
	 * @see com.google.gwt.event.logical.shared.SelectionHandler#onSelection(SelectionEvent)
	 */
	public void onSelection(SelectionEvent<Integer> event){
		selectedTabIndex = event.getSelectedItem();

		if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			Context.setCurrentMode(Context.MODE_DESIGN);
		else if(selectedTabIndex == SELECTED_INDEX_PREVIEW){
			if(formDef != null && formDef.getQuestionCount() > 0 && !designSurfaceView.hasWidgets()){
				tabs.selectTab(SELECTED_INDEX_DESIGN_SURFACE);
				
				DeferredCommand.addCommand(new Command(){
					public void execute() {
						tabs.selectTab(SELECTED_INDEX_PREVIEW);
					}
				});
				return;
			}
			
			Context.setCurrentMode(Context.MODE_PREVIEW);
		}
		else if(selectedTabIndex == SELECTED_INDEX_PROPERTIES)
			Context.setCurrentMode(Context.MODE_QUESTION_PROPERTIES);
		else if(selectedTabIndex == SELECTED_INDEX_XFORMS_SOURCE)
			Context.setCurrentMode(Context.MODE_XFORMS_SOURCE);
		else
			Context.setCurrentMode(Context.MODE_NONE);

		if(selectedTabIndex == SELECTED_INDEX_PREVIEW ){
			if(formDef != null){
				if(!previewView.isPreviewing())
					loadPreview();
				else
					previewView.moveToFirstWidget();
			}
		}
		else if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE){
			if(!designSurfaceView.hasWidgets())
				designSurfaceView.refresh();
		}


		//else if(selectedTabIndex == SELECTED_INDEX_LAYOUT_XML)
		//	txtLayoutXml.setText(designSurfaceView.getLayoutXml());
	}

	private void loadPreview(){
		FormUtil.dlg.setText(LocaleText.get("loadingPreview"));
		FormUtil.dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {
				try{
					commitChanges();
					
					List<RuntimeWidgetWrapper> externalSourceWidgets = new ArrayList<RuntimeWidgetWrapper>();
					if(Context.isOfflineMode())
						;//externalSourceWidgets = null;

					previewView.loadForm(formDef,designSurfaceView.getLayoutXml(),getJavaScriptSource(),externalSourceWidgets,true);
					FormUtil.dlg.hide();
				}
				catch(Exception ex){
					FormUtil.displayException(ex);
				}
			}
		});
	}

	/**
	 * Sets up the design surface.
	 */
	private void initDesignSurface(){
		tabs.add(scrollPanelDesign, LocaleText.get("designSurface"));

		int height = Window.getClientHeight();
		int width = Window.getClientWidth();
		designSurfaceView.setWidth(width+PurcConstants.UNITS); //100% //1015"+PurcConstants.UNITS
		designSurfaceView.setHeight(height+PurcConstants.UNITS); //707"+PurcConstants.UNITS
		designSurfaceView.setLayoutChangeListener(this);

		scrollPanelDesign.setWidget(designSurfaceView);

		tabs.selectTab(SELECTED_INDEX_DESIGN_SURFACE);

		updateScrollPos();
	}

	/**
	 * Sets up the xforms source tab.
	 */
	private void initXformsSource(){
		tabs.add(txtXformsSource, LocaleText.get("xformsSource"));
		FormUtil.maximizeWidget(txtXformsSource);
	}
	
	/**
	 * Sets up the layout xml tab.
	 */
	private void initJavaScriptSource(){
		tabs.add(txtJavaScriptSource, LocaleText.get("javaScriptSource"));
		FormUtil.maximizeWidget(txtJavaScriptSource);
	}

	/**
	 * Sets up the layout xml tab.
	 */
	private void initLayoutXml(){
		tabs.add(txtLayoutXml, LocaleText.get("layoutXml"));
		FormUtil.maximizeWidget(txtLayoutXml);
	}

	/**
	 * Sets up the language xml tab.
	 */
	private void initLanguageXml(){
		tabs.add(txtLanguageXml, LocaleText.get("languageXml"));
		FormUtil.maximizeWidget(txtLanguageXml);
	}

	/**
	 * Sets up the preview surface tab.
	 */
	private void initPreview(){
		tabs.add(scrollPanelPreview, LocaleText.get("preview"));
		previewView.setWidth("100%"); //1015"+PurcConstants.UNITS
		previewView.setHeight("700"+PurcConstants.UNITS); //707"+PurcConstants.UNITS
		previewView.setSubmitListener(this);
		previewView.setDesignSurface(designSurfaceView);
		previewView.setCenterPanel(this);

		scrollPanelPreview.setWidget(previewView);
	}

	/**
	 * Sets up the model xml tab.
	 */
	private void initModelXml(){
		tabs.add(txtModelXml, LocaleText.get("modelXml"));
		FormUtil.maximizeWidget(txtModelXml);
	}

	/**
	 * Sets up the properties tab.
	 */
	private void initProperties(){
		tabs.add(propertiesView, LocaleText.get("properties"));
	}

	/**
	 * Sets the height of the text area widgets.
	 * 
	 * @param height the height in pixels
	 */
	public void adjustHeight(String height){
		txtXformsSource.setHeight(height);
		txtJavaScriptSource.setHeight(height);
		txtLayoutXml.setHeight(height);
		txtModelXml.setHeight(height);
		txtLanguageXml.setHeight(height);
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormSelectionListener#onFormItemSelected(java.lang.Object)
	 */
	public void onFormItemSelected(Object formItem) {
		propertiesView.onFormItemSelected(formItem);

		if(selectedTabIndex == SELECTED_INDEX_PROPERTIES)
			propertiesView.setFocus();

		FormDef form = FormDef.getFormDef(formItem);

		if(this.formDef != form){
			setFormDef(form);

			designSurfaceView.setFormDef(formDef);
			previewView.setFormDef(formDef);

			if(selectedTabIndex == SELECTED_INDEX_PREVIEW && formDef != null)
				previewView.loadForm(formDef,designSurfaceView.getLayoutXml(),getJavaScriptSource(),null,true);

			//This is necessary for those running in a non GWT mode to update the 
			//scroll bars on loading the form.
			updateScrollPos();
		}
	}

	/**
	 * @see com.google.gwt.user.client.WindowResizeListener#onWindowResized(int, int)
	 */
	public void onWindowResized(int width, int height){
		propertiesView.onWindowResized(width, height);
		updateScrollPos();
	}

	/**
	 * Sets the current scroll height and width of the design and preview surface.
	 */
	private void updateScrollPos(){
		onVerticalResize();

		int height = tabs.getOffsetHeight()-48;
		if(height > 0){
			scrollPanelDesign.setHeight(height +PurcConstants.UNITS);
			scrollPanelPreview.setHeight(height +PurcConstants.UNITS);
		}
	}

	/**
	 * Called every time this widget is resized.
	 */
	public void onVerticalResize(){
		int d = Window.getClientWidth()-tabs.getAbsoluteLeft();
		if(d > 0){
			scrollPanelDesign.setWidth(d-16+PurcConstants.UNITS);
			scrollPanelPreview.setWidth(d-16+PurcConstants.UNITS);
		}
	}

	/**
	 * Loads a form with a given layout xml.
	 * 
	 * @param formDef the form definition object.
	 * @param layoutXml the layout xml. If this is null, the form is loaded
	 * 					with the default layout which is build automatically.
	 */
	public void loadForm(FormDef formDef, String layoutXml){
		setFormDef(formDef);

		//previewView.loadForm(formDef,designSurfaceView.getLayoutXml());
		if(layoutXml == null || layoutXml.trim().length() == 0){
			//This line is commented out because automatic widget formatting does not work well
			//when originating from this method call for opening an xforms document.
			;//designSurfaceView.setLayout(formDef);
		}
		else
			designSurfaceView.setLayoutXml(layoutXml,formDef);

		previewView.clearPreview();
		tabs.selectTab(SELECTED_INDEX_PROPERTIES);
	}

	/**
	 * Gets the xforms source xml.
	 * 
	 * @return the xforms xml.
	 */
	public String getXformsSource(){
		if(txtXformsSource.getText().length() == 0 && showXformsSource)
			tabs.selectTab(SELECTED_INDEX_XFORMS_SOURCE);
		return txtXformsSource.getText();
	}

	/**
	 * Sets the xforms source xml.
	 * 
	 * @param xml the xforms xml.
	 * @param selectXformsTab set to true to select the xforms source tab, else false.
	 */
	public void setXformsSource(String xml, boolean selectXformsTab){
		txtXformsSource.setText(xml);
		if(selectXformsTab && showXformsSource)
			tabs.selectTab(SELECTED_INDEX_XFORMS_SOURCE);
	}

	/**
	 * Gets the widget layout xml.
	 * 
	 * @return the layout xml.
	 */
	public String getLayoutXml(){
		return txtLayoutXml.getText();
	}
	
	/**
	 * Gets the javascript source.
	 * 
	 * @return the layout xml.
	 */
	public String getJavaScriptSource(){
		return txtJavaScriptSource.getText();
	}

	/**
	 * Gets the language xml.
	 * 
	 * @return the language xml.
	 */
	public String getLanguageXml(){
		return txtLanguageXml.getText();
	}

	/**
	 * Gets the inner html for the selected form page.
	 * 
	 * @return the html.
	 */
	public String getFormInnerHtml(){
		return designSurfaceView.getSelectedPageHtml();
	}

	/** 
	 * Sets the widget layout xml.
	 * 
	 * @param xml the layout xml.
	 * @param selectTabs set to true to select the layout xml tab, else set to false.
	 */
	public void setLayoutXml(String xml, boolean selectTabs){
		txtLayoutXml.setText(xml);
		if(selectTabs && showLayoutXml)
			tabs.selectTab(SELECTED_INDEX_LAYOUT_XML);
	}
	
	/** 
	 * Sets the javascript source.
	 * 
	 * @param src the javascript source.
	 */
	public void setJavaScriptSource(String src){
		txtJavaScriptSource.setText(src);
	}

	/**
	 * Sets the language xml.
	 * 
	 * @param xml the language xml.
	 * @param selectTab set to true to select the language xml tab, else set to false.
	 */
	public void setLanguageXml(String xml, boolean selectTab){
		txtLanguageXml.setText(xml);
		if(selectTab)
			selectLanguageTab();
	}

	/**
	 * Builds the widget layout xml and puts it in the layout xml tab.
	 */
	public void buildLayoutXml(){
		String layout = designSurfaceView.getLayoutXml();

		if(layout != null)
			this.formDef.setLayoutXml(layout);
		else
			layout = formDef.getLayoutXml(); //TODO Needs testing coz its new

		txtLayoutXml.setText(layout);
	}

	/**
	 * Builds the language xml and puts it in the language xml tab.
	 */
	public void buildLanguageXml(){
		Document doc = LanguageUtil.createNewLanguageDoc();
		Element rootNode = doc.getDocumentElement();

		Element node = null;
		if(formDef != null){
			node = formDef.getLanguageNode();
			if(node != null)
				rootNode.appendChild(node);
		}

		node = designSurfaceView.getLanguageNode();
		if(node != null)
			rootNode.appendChild(node);

		txtLanguageXml.setText(FormDesignerUtil.formatXml(doc.toString()));

		if(formDef != null)
			formDef.setLanguageXml(txtLanguageXml.getText());
	}

	/**
	 * Loads layout xml and builds the widgets represented on the design surface tab. 
	 *
	 * @param layoutXml the layout xml. If layoutXml is null, then it uses the one in
	 * 					the layout xml tab, if any is found there.
	 * @param selectTabs set to true to select the layout xml tab, else set to false.
	 */
	public void loadLayoutXml(String layoutXml, boolean selectTabs){
		if(layoutXml != null)
			txtLayoutXml.setText(layoutXml);
		else
			layoutXml = txtLayoutXml.getText();

		if(layoutXml != null && layoutXml.trim().length() > 0){
			//designSurfaceView.setLayoutXml(layoutXml,Context.inLocalizationMode() ? formDef : null); //TODO This passed null formdef in localization mode

			FormDef frmDef = null;
			if(Context.inLocalizationMode())
				frmDef = formDef;
			designSurfaceView.setLayoutXml(layoutXml,frmDef); //TODO This passed null formdef in localization mode

			updateScrollPos();

			if(selectTabs)
				tabs.selectTab(SELECTED_INDEX_DESIGN_SURFACE);
		}
		else if(selectTabs && showLayoutXml)
			tabs.selectTab(SELECTED_INDEX_LAYOUT_XML);

		if(formDef != null)
			formDef.setLayoutXml(layoutXml);
	}

	/**
	 * Loads the form widget layout from the xml in the layout xml tab.
	 * 
	 * @param selectTabs set to true to select the layout xml tab, else false.
	 */
	public void openFormLayout(boolean selectTabs){
		loadLayoutXml(null,selectTabs);
	}

	/**
	 * Loads the current form in the locale whose contents are in the language xml tab.
	 */
	public void openLanguageXml(){
		loadLanguageXml(null,false);
	}

	public void loadLanguageXml(String xml, boolean selectTabs){
		if(xml != null)
			txtLanguageXml.setText(xml);
		else
			xml = txtLanguageXml.getText();

		if(xml != null && xml.trim().length() > 0){
			if(formDef != null)
				txtXformsSource.setText(FormUtil.formatXml(LanguageUtil.translate(formDef.getDoc(), xml, true).toString()));

			String layoutXml = txtLayoutXml.getText();
			if(layoutXml != null && layoutXml.trim().length() > 0){
				txtLayoutXml.setText(FormUtil.formatXml(LanguageUtil.translate(layoutXml, xml, false).toString()));
				String s = txtLayoutXml.getText();
				s.trim();
			}

			if(selectTabs)
				selectLanguageTab();

			if(formDef != null)
				formDef.setLanguageXml(xml);
		}
		else if(selectTabs)
			selectLanguageTab();
	}

	public void saveFormLayout(){
		txtLayoutXml.setText(designSurfaceView.getLayoutXml());
		
		if(showLayoutXml)
			tabs.selectTab(SELECTED_INDEX_LAYOUT_XML);

		if(formDef != null)
			formDef.setLayoutXml(txtLayoutXml.getText());
	}

	public void saveLanguageText(boolean selectTab){
		buildLanguageXml();

		if(selectTab)
			selectLanguageTab();

		if(formDef != null)
			formDef.setLanguageXml(txtLanguageXml.getText());
	}
	
	public void saveJavaScriptSource(){
		if(formDef != null)
			formDef.setJavaScriptSource(txtJavaScriptSource.getText());
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerListener#format()()
	 */
	public void format(){
		if(selectedTabIndex == SELECTED_INDEX_XFORMS_SOURCE)
			txtXformsSource.setText(FormDesignerUtil.formatXml(txtXformsSource.getText()));
		else if(selectedTabIndex == SELECTED_INDEX_LAYOUT_XML)
			txtLayoutXml.setText(FormDesignerUtil.formatXml(txtLayoutXml.getText()));
		else if(selectedTabIndex == SELECTED_INDEX_MODEL_XML)
			txtModelXml.setText(FormDesignerUtil.formatXml(txtModelXml.getText()));
		else if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			designSurfaceView.format();
		else if(selectedTabIndex == SELECTED_INDEX_LANGUAGE_XML)
			txtLanguageXml.setText(FormDesignerUtil.formatXml(txtLanguageXml.getText()));
	}

	public void commitChanges(){
		propertiesView.commitChanges();
	}

	/**
	 * Sets the listener to widget selection changes.
	 * 
	 * @param widgetSelectionListener the listener.
	 */
	public void setWidgetSelectionListener(WidgetSelectionListener  widgetSelectionListener){
		designSurfaceView.setWidgetSelectionListener(widgetSelectionListener);
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#deleteSelectedItems()
	 */
	public void deleteSelectedItem() {
		if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			designSurfaceView.deleteSelectedItem();	
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#copyItem()
	 */
	public void copyItem() {
		if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			designSurfaceView.copyItem();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#cutItem()
	 */
	public void cutItem() {
		if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			designSurfaceView.cutItem();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#pasteItem()
	 */
	public void pasteItem() {
		if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			designSurfaceView.pasteItem();
	}

	/**
	 * @see org.purc.purcforms.client.controller.SubmitListener#onSubmit(String)()
	 */
	public void onSubmit(String xml) {
		this.txtModelXml.setText(xml);
		
		if(showModelXml)
			tabs.selectTab(SELECTED_INDEX_MODEL_XML);
		else
			Window.alert(LocaleText.get("formSubmitSuccess"));
	}

	/**
	 * @see org.purc.purcforms.client.controller.SubmitListener#onCancel()()
	 */
	public void onCancel(){

	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignLeft()
	 */
	public void alignLeft() {
		if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			designSurfaceView.alignLeft();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignRight()
	 */
	public void alignRight() {
		if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			designSurfaceView.alignRight();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignTop()
	 */
	public void alignTop() {
		if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			designSurfaceView.alignTop();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignBottom()
	 */
	public void alignBottom() {
		if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			designSurfaceView.alignBottom();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#makeSameHeight()
	 */
	public void makeSameHeight() {
		if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			designSurfaceView.makeSameHeight();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#makeSameSize()
	 */
	public void makeSameSize() {
		if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			designSurfaceView.makeSameSize();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#makeSameWidth()
	 */
	public void makeSameWidth() {
		if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			designSurfaceView.makeSameWidth();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#refresh()
	 */
	public void refresh(){
		if(selectedTabIndex == SELECTED_INDEX_PREVIEW)
			previewView.refresh(); //loadForm(formDef,designSurfaceView.getLayoutXml(),null);
		else if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			designSurfaceView.refresh();
	}

	/**
	 * Sets the current form that is being designed.
	 * 
	 * @param formDef the form definition object.
	 */
	public void setFormDef(FormDef formDef){
		if(this.formDef == null || this.formDef != formDef){
			if(formDef ==  null){
				txtLayoutXml.setText(null);
				txtXformsSource.setText(null);
				txtLanguageXml.setText(null);
				txtJavaScriptSource.setText(null);
			}
			else{
				txtLayoutXml.setText(formDef.getLayoutXml());
				txtXformsSource.setText(formDef.getXformXml());
				txtLanguageXml.setText(formDef.getLanguageXml());
				txtJavaScriptSource.setText(formDef.getJavaScriptSource());
			}
		}

		this.formDef = formDef;
	}

	/**
	 * Gets the current form that is being designed.
	 * 
	 * @return the form definition object.
	 */
	public FormDef getFormDef(){
		return formDef;
	}

	/**
	 * Sets the height offset used by this widget when embedded as a widget
	 * in a GWT application.
	 * 
	 * @param offset the offset pixels.
	 */
	public void setEmbeddedHeightOffset(int offset){
		designSurfaceView.setEmbeddedHeightOffset(offset);
		previewView.setEmbeddedHeightOffset(offset);
	}

	/**
	 * Sets the listener to form action events.
	 * 
	 * @param formActionListener the listener.
	 */
	public void setFormActionListener(IFormActionListener formActionListener){
		this.propertiesView.setFormActionListener(formActionListener);
	}

	/**
	 * Checks if the layout xml tab is selected.
	 * 
	 * @return true if yes, else false.
	 */
	public boolean isInLayoutMode(){
		return tabs.getTabBar().getSelectedTab() == SELECTED_INDEX_LAYOUT_XML;
	}

	/**
	 * @see org.purc.purcforms.client.controller.LayoutChangeListener#onLayoutChanged(String)
	 */
	public void onLayoutChanged(String xml){
		txtLayoutXml.setText(xml);
		if(formDef != null)
			formDef.setLayoutXml(xml);
	}

	/**
	 * Selects the language xml tab.
	 */
	private void selectLanguageTab(){
		if(showLanguageXml)
			tabs.selectTab(SELECTED_INDEX_LANGUAGE_XML);
	}

	/**
	 * Sets listener to form designer global events.
	 * 
	 * @param formDesignerListener the listener.
	 */
	public void setFormDesignerListener(IFormDesignerListener formDesignerListener){
		this.formDesignerListener = formDesignerListener;
	}

	/**
	 * Checks if the current selection mode allows refreshes.
	 * 
	 * @return true if it allows, else false.
	 */
	public boolean allowsRefresh(){
		return selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE || selectedTabIndex == SELECTED_INDEX_PREVIEW;
	}


	/**
	 * Removes the language xml tab.
	 */
	public void removeLanguageTab(){
		//if(tabs.getTabBar().getTabCount() == 7){
		if(showLanguageXml){
			tabs.remove(SELECTED_INDEX_LANGUAGE_XML);
			
			--SELECTED_INDEX_PREVIEW;
			--SELECTED_INDEX_MODEL_XML;
			
			showLanguageXml = false;
		}
		//}
	}


	public void removeXformSourceTab(){
		if(showXformsSource){
			tabs.remove(SELECTED_INDEX_XFORMS_SOURCE);

			--SELECTED_INDEX_DESIGN_SURFACE;
			--SELECTED_INDEX_JAVASCRIPT_SOURCE;
			--SELECTED_INDEX_LAYOUT_XML;
			--SELECTED_INDEX_LANGUAGE_XML;
			--SELECTED_INDEX_PREVIEW;
			--SELECTED_INDEX_MODEL_XML;
			
			showXformsSource = false;
		}
	}

	public void removeJavaScriptSourceTab(){
		if(showJavaScriptSource){
			tabs.remove(SELECTED_INDEX_JAVASCRIPT_SOURCE);

			--SELECTED_INDEX_LAYOUT_XML;
			--SELECTED_INDEX_LANGUAGE_XML;
			--SELECTED_INDEX_PREVIEW;
			--SELECTED_INDEX_MODEL_XML;
			
			showJavaScriptSource = false;
		}
	}


	public void removeLayoutXmlTab(){
		if(showLayoutXml){
			tabs.remove(SELECTED_INDEX_LAYOUT_XML);

			--SELECTED_INDEX_LANGUAGE_XML;
			--SELECTED_INDEX_PREVIEW;
			--SELECTED_INDEX_MODEL_XML;
			
			showLayoutXml = false;
		}
	}


	public void removeModelXmlTab(){
		if(showModelXml){
			tabs.remove(SELECTED_INDEX_MODEL_XML);
			showModelXml = false;
		}
	}
	
	
	public WidgetPropertyChangeListener getWidgetPropertyChangeListener(){
		return designSurfaceView;
	}
}
