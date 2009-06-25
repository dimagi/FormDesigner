package org.purc.purcforms.client;

import org.purc.purcforms.client.LeftPanel.Images;
import org.purc.purcforms.client.controller.IFormActionListener;
import org.purc.purcforms.client.controller.IFormChangeListener;
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
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
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

	private static final int SELECTED_INDEX_PROPERTIES = 0;
	private static final int SELECTED_INDEX_XFORMS_SOURCE = 1;
	private static final int SELECTED_INDEX_DESIGN_SURFACE = 2;
	private static final int SELECTED_INDEX_LAYOUT_XML = 3;
	private static int SELECTED_INDEX_LANGUAGE_XML = 4;
	private static int SELECTED_INDEX_PREVIEW = 5;
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

	private TextArea txtLayoutXml = new TextArea();
	private TextArea txtModelXml = new TextArea();
	private TextArea txtLanguageXml = new TextArea();

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
		initLanguageXml();
		initPreview();
		initModelXml();

		FormDesignerUtil.maximizeWidget(tabs);

		tabs.selectTab(0);
		initWidget(tabs);
		tabs.addTabListener(this);
		
		if(!FormUtil.getShowLanguageTab())
			this.removeLanguageTab();
	}

	public void setFormChangeListener(IFormChangeListener formChangeListener){
		propertiesView.setFormChangeListener(formChangeListener);
	}

	public void onTabSelected(SourcesTabEvents sender, int tabIndex){
		selectedTabIndex = tabIndex;
		if(selectedTabIndex == SELECTED_INDEX_PREVIEW ){
			if(formDef != null){
				if(!previewView.isPreviewing())
					loadPreview();
				else
					previewView.moveToFirstWidget();
			}
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
		tabs.add(designSurfaceView, "Design Surface");
		FormDesignerUtil.maximizeWidget(designSurfaceView);
		designSurfaceView.setLayoutChangeListener(this);
	}

	private void initXformsSource(){
		FormDesignerUtil.maximizeWidget(txtXformsSource);
		tabs.add(txtXformsSource, "XForms Source");
	}

	private void initLayoutXml(){
		tabs.add(txtLayoutXml, "Layout XML");
		FormDesignerUtil.maximizeWidget(txtLayoutXml);
	}

	private void initLanguageXml(){
		tabs.add(txtLanguageXml, "Language XML");
		FormDesignerUtil.maximizeWidget(txtLanguageXml);
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
		}
	}

	public void onWindowResized(int width, int height){
		propertiesView.onWindowResized(width, height);
		designSurfaceView.onWindowResized(width, height);
		previewView.onWindowResized(width, height);
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

	public String getLanguageXml(){
		return txtLanguageXml.getText();
	}

	public void setLayoutXml(String xml, boolean selectTabs){
		txtLayoutXml.setText(xml);
		if(selectTabs)
			tabs.selectTab(SELECTED_INDEX_LAYOUT_XML);
	}

	public void setLanguageXml(String xml, boolean selectTab){
		txtLanguageXml.setText(xml);
		if(selectTab)
			selectLanguageTab();
	}

	public void buildLayoutXml(){
		String layout = designSurfaceView.getLayoutXml();

		if(layout != null)
			this.formDef.setLayoutXml(layout);
		else
			layout = formDef.getLayoutXml(); //TODO Needs testing coz its new

		txtLayoutXml.setText(layout);
	}

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
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignLeft()
	 */
	public void alignTop() {
		if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			designSurfaceView.alignTop();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerController#alignRight()
	 */
	public void alignBottom() {
		if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			designSurfaceView.alignBottom();
	}

	public void makeSameHeight() {
		if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			designSurfaceView.makeSameHeight();
	}

	public void makeSameSize() {
		if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			designSurfaceView.makeSameSize();
	}

	public void makeSameWidth() {
		if(selectedTabIndex == SELECTED_INDEX_DESIGN_SURFACE)
			designSurfaceView.makeSameWidth();
	}

	public void refresh(){
		if(selectedTabIndex == SELECTED_INDEX_PREVIEW )
			previewView.loadForm(formDef,designSurfaceView.getLayoutXml(),null);
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
}
