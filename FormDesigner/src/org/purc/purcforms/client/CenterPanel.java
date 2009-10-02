package org.purc.purcforms.client;

import org.purc.purcforms.client.LeftPanel.Images;
import org.purc.purcforms.client.controller.IFormActionListener;
import org.purc.purcforms.client.controller.IFormChangeListener;
import org.purc.purcforms.client.controller.IFormDesignerListener;
import org.purc.purcforms.client.controller.IFormSelectionListener;
import org.purc.purcforms.client.controller.LayoutChangeListener;
import org.purc.purcforms.client.controller.SubmitListener;
import org.purc.purcforms.client.controller.WidgetSelectionListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.util.LanguageUtil;
import org.purc.purcforms.client.view.DesignSurfaceView;
import org.purc.purcforms.client.view.PreviewView;
import org.purc.purcforms.client.view.PropertiesView;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventPreview;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;


/**
 * Panel containing the contents on the form being designed.
 * 
 * @author daniel
 *
 */
public class CenterPanel extends Composite implements TabListener, IFormSelectionListener, SubmitListener, LayoutChangeListener{

	/** Index for the properties tab. */
	private static final int SELECTED_INDEX_PROPERTIES = 0;
	
	/** Index for xforms source xml tab. */
	private static final int SELECTED_INDEX_XFORMS_SOURCE = 1;
	
	/** Index for the design surface tab. */
	private static final int SELECTED_INDEX_DESIGN_SURFACE = 2;
	
	/** Index for the layout xml tab. */
	private static final int SELECTED_INDEX_LAYOUT_XML = 3;
	
	/** Index for the locale or language xml tab. */
	private static int SELECTED_INDEX_LANGUAGE_XML = 4;
	
	/** Index for the preview tab. */
	private static int SELECTED_INDEX_PREVIEW = 5;
	
	/** Index for the model xml tab. */
	private static int SELECTED_INDEX_MODEL_XML = 6;

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
		initLayoutXml();
		initLanguageXml();
		initPreview();
		initModelXml();

		FormDesignerUtil.maximizeWidget(tabs);

		tabs.selectTab(0);
		initWidget(tabs);
		tabs.addTabListener(this);

		if(!FormUtil.getShowLanguageTab())
			this.removeLanguageTab();
		
		Context.setCurrentMode(Context.MODE_QUESTION_PROPERTIES);
		
		previewEvents();
	}
	
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


	public void setFormChangeListener(IFormChangeListener formChangeListener){
		propertiesView.setFormChangeListener(formChangeListener);
	}

	public void onTabSelected(SourcesTabEvents sender, int tabIndex){
		if(tabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			Context.setCurrentMode(Context.MODE_DESIGN);
		else if(tabIndex == SELECTED_INDEX_PREVIEW)
			Context.setCurrentMode(Context.MODE_PREVIEW);
		else if(tabIndex == SELECTED_INDEX_PROPERTIES)
			Context.setCurrentMode(Context.MODE_QUESTION_PROPERTIES);
		else if(tabIndex == SELECTED_INDEX_XFORMS_SOURCE)
			Context.setCurrentMode(Context.MODE_XFORMS_SOURCE);
		else
			Context.setCurrentMode(Context.MODE_NONE);
			
			
		selectedTabIndex = tabIndex;
		if(selectedTabIndex == SELECTED_INDEX_PREVIEW ){
			if(formDef != null){
				if(!previewView.isPreviewing())
					loadPreview();
				else
					previewView.moveToFirstWidget();
			}
		}
		else if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			designSurfaceView.refresh();
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
					previewView.loadForm(formDef,designSurfaceView.getLayoutXml(),null);
					FormUtil.dlg.hide();
				}
				catch(Exception ex){
					FormUtil.dlg.hide();
					FormUtil.displayException(ex);
				}
			}
		});
	}

	public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex){
		return true;
	}

	private void initDesignSurface(){
		tabs.add(scrollPanelDesign, "Design Surface");

		designSurfaceView.setWidth("100%"); //1015px
		designSurfaceView.setHeight("700px"); //707px
		designSurfaceView.setLayoutChangeListener(this);

		scrollPanelDesign.setWidget(designSurfaceView);

		//FormDesignerUtil.maximizeWidget(scrollPanel);

		/*tabs.add(designSurfaceView, "Design Surface");
		FormDesignerUtil.maximizeWidget(designSurfaceView);
		designSurfaceView.setLayoutChangeListener(this);*/
	}

	/**
	 * Sets up the xforms source tab.
	 */
	private void initXformsSource(){
		FormDesignerUtil.maximizeWidget(txtXformsSource);
		tabs.add(txtXformsSource, "XForms Source");
	}

	/**
	 * Sets up the layout xml tab.
	 */
	private void initLayoutXml(){
		tabs.add(txtLayoutXml, "Layout XML");
		FormDesignerUtil.maximizeWidget(txtLayoutXml);
	}
	
	/**
	 * Sets up the language xml tab.
	 */
	private void initLanguageXml(){
		tabs.add(txtLanguageXml, "Language XML");
		FormDesignerUtil.maximizeWidget(txtLanguageXml);
	}

	/**
	 * Sets up the preview surface tab.
	 */
	private void initPreview(){
		tabs.add(scrollPanelPreview, "Preview");
		//FormDesignerUtil.maximizeWidget(previewView);
		previewView.setWidth("100%"); //1015px
		previewView.setHeight("700px"); //707px
		previewView.setSubmitListener(this);
		previewView.setDesignSurface(designSurfaceView);
		previewView.setCenterPanel(this);

		scrollPanelPreview.setWidget(previewView);
	}

	/**
	 * Sets up the model xml tab.
	 */
	private void initModelXml(){
		tabs.add(txtModelXml, "Model XML");
		FormDesignerUtil.maximizeWidget(txtModelXml);
	}

	/**
	 * Sets up the properties tab.
	 */
	private void initProperties(){
		tabs.add(propertiesView, "Properties");
	}

	public void adjustHeight(String height){
		txtXformsSource.setHeight(height);
		//designSurfaceView.setHeight(height);
		txtLayoutXml.setHeight(height);
		//previewView.setHeight(height);
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

			//if(formItem instanceof FormDef){
			designSurfaceView.setFormDef(formDef);
			previewView.setFormDef(formDef);

			if(selectedTabIndex == SELECTED_INDEX_PREVIEW && formDef != null)
				previewView.loadForm(formDef,designSurfaceView.getLayoutXml(),null);
			//}
			
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
		//designSurfaceView.onWindowResized(width, height);
		//previewView.onWindowResized(width, height);

		updateScrollPos();

		//scrollPanel.setWidth(width-261+"px");
		//scrollPanel.setHeight(height-110+"px");
		//FormDesignerUtil.maximizeWidget(scrollPanel);
	}
	
	private void updateScrollPos(){
		onVerticalResize();
		
		int height = tabs.getOffsetHeight()-48;
		if(height > 0){
			scrollPanelDesign.setHeight(height +"px");
			scrollPanelPreview.setHeight(height +"px");
		}
	}

	public void onVerticalResize(){
		int d = Window.getClientWidth()-tabs.getAbsoluteLeft();
		if(d > 0){
			scrollPanelDesign.setWidth(d-16+"px");
			scrollPanelPreview.setWidth(d-16+"px");
		}
	}

	public void loadForm(FormDef formDef, String layoutXml){
		setFormDef(formDef);

		//previewView.loadForm(formDef,designSurfaceView.getLayoutXml());
		if(layoutXml == null || layoutXml.trim().length() == 0)
			designSurfaceView.setLayout(formDef);
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
		if(txtXformsSource.getText().length() == 0)
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
		if(selectXformsTab)
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
		if(selectTabs)
			tabs.selectTab(SELECTED_INDEX_LAYOUT_XML);
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
	 * @param xml the layout xml.
	 * @param selectTabs set to true to select the layout xml tab, else set to false.
	 */
	public void loadLayoutXml(String xml, boolean selectTabs){
		if(xml != null)
			txtLayoutXml.setText(xml);
		else
			xml = txtLayoutXml.getText();

		if(xml != null && xml.trim().length() > 0){
			designSurfaceView.setLayoutXml(xml,Context.inLocalizationMode() ? formDef : null); //TODO This passed null formdef in localization mode
			updateScrollPos();
			
			if(selectTabs)
				tabs.selectTab(SELECTED_INDEX_DESIGN_SURFACE);
		}
		else if(selectTabs)
			tabs.selectTab(SELECTED_INDEX_LAYOUT_XML);

		if(formDef != null)
			formDef.setLayoutXml(xml);
	}

	public void openFormLayout(boolean selectTabs){
		loadLayoutXml(null,selectTabs);
	}

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

	public void setWidgetSelectionListener(WidgetSelectionListener  widgetSelectionListener){
		designSurfaceView.setWidgetSelectionListener(widgetSelectionListener);
	}

	/* (non-Javadoc)
	 * @see org.purc.purcform.client.controller.IFormDesignerController#deleteSelectedItem()
	 */
	public void deleteSelectedItem() {
		if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			designSurfaceView.deleteSelectedItem();	
	}

	public void copyItem() {
		if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			designSurfaceView.copyItem();
	}

	/* (non-Javadoc)
	 * @see org.purc.purcform.client.controller.IFormActionListener#cutItem()
	 */
	public void cutItem() {
		if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			designSurfaceView.cutItem();
	}

	/* (non-Javadoc)
	 * @see org.purc.purcform.client.controller.IFormActionListener#pasteItem()
	 */
	public void pasteItem() {
		if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			designSurfaceView.pasteItem();
	}

	public void onSubmit(String xml) {
		this.txtModelXml.setText(xml);
		tabs.selectTab(SELECTED_INDEX_MODEL_XML);
	}
	
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
		if(selectedTabIndex == SELECTED_INDEX_PREVIEW )
			previewView.loadForm(formDef,designSurfaceView.getLayoutXml(),null);
		else if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			designSurfaceView.refresh();
	}

	public void setFormDef(FormDef formDef){
		if(this.formDef == null || this.formDef != formDef){
			if(formDef ==  null){
				txtLayoutXml.setText(null);
				txtXformsSource.setText(null);
				txtLanguageXml.setText(null);
			}
			else{
				txtLayoutXml.setText(formDef.getLayoutXml());
				txtXformsSource.setText(formDef.getXformXml());
				txtLanguageXml.setText(formDef.getLanguageXml());
			}
		}

		this.formDef = formDef;
	}

	public FormDef getFormDef(){
		return formDef;
	}

	public void setEmbeddedHeightOffset(int offset){
		designSurfaceView.setEmbeddedHeightOffset(offset);
		previewView.setEmbeddedHeightOffset(offset);
	}

	public void setFormActionListener(IFormActionListener formActionListener){
		this.propertiesView.setFormActionListener(formActionListener);
	}

	public boolean isInLayoutMode(){
		return tabs.getTabBar().getSelectedTab() == SELECTED_INDEX_LAYOUT_XML;
	}

	public void onLayoutChanged(String xml){
		txtLayoutXml.setText(xml);
		if(formDef != null)
			formDef.setLayoutXml(xml);
	}

	private void selectLanguageTab(){
		if(tabs.getTabBar().getTabCount() == 7)
			tabs.selectTab(SELECTED_INDEX_LANGUAGE_XML);
	}

	public void removeLanguageTab(){
		if(tabs.getTabBar().getTabCount() == 7){
			tabs.remove(SELECTED_INDEX_LANGUAGE_XML);
			--SELECTED_INDEX_PREVIEW;
			--SELECTED_INDEX_MODEL_XML;
		}
	}
	
	/**
	 * Sets listener to form designer global events.
	 * 
	 * @param formDesignerListener the listener.
	 */
	public void setFormDesignerListener(IFormDesignerListener formDesignerListener){
		this.formDesignerListener = formDesignerListener;
	}
}
