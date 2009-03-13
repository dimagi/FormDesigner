package org.purc.purcforms.client;

import org.purc.purcforms.client.LeftPanel.Images;
import org.purc.purcforms.client.controller.IFormChangeListener;
import org.purc.purcforms.client.controller.IFormSelectionListener;
import org.purc.purcforms.client.controller.SubmitListener;
import org.purc.purcforms.client.controller.WidgetSelectionListener;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.view.DesignSurfaceView;
import org.purc.purcforms.client.view.PreviewView;
import org.purc.purcforms.client.view.PropertiesView;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TextArea;


/**
 * Panel containing the contents on the form being designed.
 * 
 * @author daniel
 *
 */
public class CenterPanel extends Composite implements TabListener, IFormSelectionListener, SubmitListener{

	private static final int SELECTED_INDEX_PROPERTIES = 0;
	private static final int SELECTED_INDEX_XFORMS_SOURCE = 1;
	private static final int SELECTED_INDEX_DESIGN_SURFACE = 2;
	private static final int SELECTED_INDEX_LAYOUT_XML = 3;
	private static final int SELECTED_INDEX_PREVIEW = 4;
	private static final int SELECTED_INDEX_MODEL_XML = 5;

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

	private TextArea txtLayoutXml = new TextArea();
	private TextArea txtModelXml = new TextArea();

	/**
	 * View used to display a form as it will look when the user is entering data in non-design mode.
	 */
	private PreviewView previewView;
	private FormDef formDef;
	private int selectedTabIndex = 0;	

	public CenterPanel(Images images) {		
		designSurfaceView = new DesignSurfaceView(images);
		previewView = new PreviewView((PreviewView.Images)images);

		initProperties();
		initXformsSource();
		initDesignSurface();
		initLayoutXml();
		initPreview();
		initModelXml();

		FormDesignerUtil.maximizeWidget(tabs);

		tabs.selectTab(0);
		initWidget(tabs);
		tabs.addTabListener(this);
	}

	public void setFormChangeListener(IFormChangeListener formChangeListener){
		propertiesView.setFormChangeListener(formChangeListener);
	}

	public void onTabSelected(SourcesTabEvents sender, int tabIndex){
		selectedTabIndex = tabIndex;
		if(selectedTabIndex == SELECTED_INDEX_PREVIEW ){
			if(formDef != null){
				if(!previewView.isPreviewing()){
					commitChanges();
					previewView.loadForm(formDef,designSurfaceView.getLayoutXml(),null);
				}
				else
					previewView.moveToFirstWidget();
			}
		}
		//else if(selectedTabIndex == SELECTED_INDEX_LAYOUT_XML)
		//	txtLayoutXml.setText(designSurfaceView.getLayoutXml());
	}

	public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex){
		return true;
	}

	private void initDesignSurface(){
		tabs.add(designSurfaceView, "Design Surface");
		FormDesignerUtil.maximizeWidget(designSurfaceView);
	}

	private void initXformsSource(){
		FormDesignerUtil.maximizeWidget(txtXformsSource);
		tabs.add(txtXformsSource, "XForms Source");
	}

	private void initLayoutXml(){
		tabs.add(txtLayoutXml, "Layout XML");
		FormDesignerUtil.maximizeWidget(txtLayoutXml);
	}

	private void initPreview(){
		tabs.add(previewView, "Preview");
		FormDesignerUtil.maximizeWidget(previewView);
		previewView.setSubmitListener(this);
		previewView.setDesignSurface(designSurfaceView);
		previewView.setCenterPanel(this);
	}

	private void initModelXml(){
		tabs.add(txtModelXml, "Model XML");
		FormDesignerUtil.maximizeWidget(txtModelXml);
	}

	private void initProperties(){
		tabs.add(propertiesView, "Properties");
	}

	public void adjustHeight(String height){
		txtXformsSource.setHeight(height);
		//designSurfaceView.setHeight(height);
		txtLayoutXml.setHeight(height);
		//previewView.setHeight(height);
		txtModelXml.setHeight(height);
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormSelectionListener#onFormItemSelected(java.lang.Object)
	 */
	public void onFormItemSelected(Object formItem) {
		propertiesView.onFormItemSelected(formItem);
		if(formItem instanceof FormDef){
			formDef = (FormDef)formItem;
			designSurfaceView.setFormDef(formDef);
			previewView.setFormDef(formDef);

			if(selectedTabIndex == SELECTED_INDEX_PREVIEW)
				previewView.loadForm(formDef,designSurfaceView.getLayoutXml(),null);
		}
	}

	public void onWindowResized(int width, int height){
		propertiesView.onWindowResized(width, height);
		designSurfaceView.onWindowResized(width, height);
		previewView.onWindowResized(width, height);
	}

	public void loadForm(FormDef formDef, String layoutXml){
		this.formDef = formDef;
		//previewView.loadForm(formDef,designSurfaceView.getLayoutXml());
		if(layoutXml == null || layoutXml.trim().length() == 0)
			designSurfaceView.setLayout(formDef);
		else
			designSurfaceView.setLayoutXml(layoutXml,formDef);
		
		previewView.clearPreview();
		tabs.selectTab(SELECTED_INDEX_PROPERTIES);
	}

	public String getXformsSource(){
		if(txtXformsSource.getText().length() == 0)
			tabs.selectTab(SELECTED_INDEX_XFORMS_SOURCE);
		return txtXformsSource.getText();
	}

	public void setXformsSource(String xml, boolean selectXformsTab){
		txtXformsSource.setText(xml);
		if(selectXformsTab)
			tabs.selectTab(SELECTED_INDEX_XFORMS_SOURCE);
	}

	public String getLayoutXml(){
		return txtLayoutXml.getText();
	}
	
	public void setLayoutXml(String xml, boolean selectTabs){
		txtLayoutXml.setText(xml);
		if(selectTabs)
			tabs.selectTab(SELECTED_INDEX_LAYOUT_XML);
	}
	
	public void buildLayoutXml(){
		txtLayoutXml.setText(designSurfaceView.getLayoutXml());
	}

	public void loadLayoutXml(String xml, boolean selectTabs){
		if(xml != null)
			txtLayoutXml.setText(xml);
		else
			xml = txtLayoutXml.getText();

		if(xml != null && xml.trim().length() > 0){
			designSurfaceView.setLayoutXml(xml,null);
			if(selectTabs)
				tabs.selectTab(SELECTED_INDEX_DESIGN_SURFACE);
		}
		else if(selectTabs)
			tabs.selectTab(SELECTED_INDEX_LAYOUT_XML);
	}

	public void openFormLayout(){
		loadLayoutXml(null,true);
	}

	public void saveFormLayout(){
		txtLayoutXml.setText(designSurfaceView.getLayoutXml());
		tabs.selectTab(SELECTED_INDEX_LAYOUT_XML);
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
		designSurfaceView.deleteSelectedItem();	
	}

	public void copyItem() {
		designSurfaceView.copyItem();
	}

	/* (non-Javadoc)
	 * @see org.purc.purcform.client.controller.IFormActionListener#cutItem()
	 */
	public void cutItem() {
		designSurfaceView.cutItem();
	}

	/* (non-Javadoc)
	 * @see org.purc.purcform.client.controller.IFormActionListener#pasteItem()
	 */
	public void pasteItem() {
		designSurfaceView.pasteItem();
	}

	public void onSubmit(String xml) {
		this.txtModelXml.setText(xml);
		tabs.selectTab(SELECTED_INDEX_MODEL_XML);
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignLeft()
	 */
	public void alignLeft() {
		designSurfaceView.alignLeft();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignRight()
	 */
	public void alignRight() {
		designSurfaceView.alignRight();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignLeft()
	 */
	public void alignTop() {
		designSurfaceView.alignTop();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignRight()
	 */
	public void alignBottom() {
		designSurfaceView.alignBottom();
	}

	public void makeSameHeight() {
		designSurfaceView.makeSameHeight();
	}

	public void makeSameSize() {
		designSurfaceView.makeSameSize();
	}

	public void makeSameWidth() {
		designSurfaceView.makeSameWidth();
	}

	public void refresh(){
		if(selectedTabIndex == SELECTED_INDEX_PREVIEW )
			previewView.loadForm(formDef,designSurfaceView.getLayoutXml(),null);
	}
	
	public FormDef getFormDef(){
		return formDef;
	}
	
	public void setEmbeddedHeightOffset(int offset){
		designSurfaceView.setEmbeddedHeightOffset(offset);
	}
}
