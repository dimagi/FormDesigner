package org.openrosa.client.view;

import java.util.HashMap;
import java.util.List;

import org.openrosa.client.Context;
import org.openrosa.client.controller.IFileListener;
import org.openrosa.client.controller.ITextListener;
import org.openrosa.client.model.FormDef;
import org.openrosa.client.model.ItextModel;
import org.openrosa.client.model.PageDef;
import org.openrosa.client.model.QuestionDef;
import org.openrosa.client.util.ItextBuilder;
import org.openrosa.client.util.ItextParser;
import org.openrosa.client.xforms.XformParser;
import org.openrosa.client.xforms.XhtmlBuilder;
import org.purc.purcforms.client.PurcConstants;
import org.purc.purcforms.client.controller.IFormSelectionListener;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.xforms.XmlUtil;

import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;


/**
 * This is the widget that houses the xforms, design and internationalization tab.
 * 
 * @author daniel
 *
 */
public class CenterWidget extends Composite implements IFileListener,IFormSelectionListener,ITextListener {

	private static final int TAB_INDEX_DESIGN = 0;
	private static final int TAB_INDEX_XFORMS = 1;
	private static final int TAB_INDEX_ITEXT = 2;

	/**
	 * Tab widget housing the contents.
	 */
	private DecoratedTabPanel tabs = new DecoratedTabPanel();

	private XformsTabWidget xformsWidget = new XformsTabWidget(this);
	DesignTabWidget designWidget = new DesignTabWidget(this);
	private TextTabWidget itextWidget = new TextTabWidget(this);

	private FormDef formDef;
	private static HashMap<String,String> formAttrMap = new HashMap<String,String>();
	private static HashMap<String,ItextModel> itextMap = new HashMap<String,ItextModel>();
	ListStore<ItextModel> itextList = new ListStore<ItextModel>();

	/**
	 * this is a flag the onSave() method checks to see if it should show the xml window when it saves.
	 */
	private boolean showXMLWindowFlag = true; 

	public CenterWidget() {		
		initDesignTab();
		initXformsTab();
		initItextTab();

		FormUtil.maximizeWidget(tabs);

		tabs.selectTab(TAB_INDEX_DESIGN);
		
		//////////////////////////////!!!!!!!!!!!!!!!!!
		initWidget(designWidget);   /// <<<<<<---------------------- This is a gruesome shortcut
		///////////////////////////////////////////////   The whole 'CenterWidget' should be removed
		// and designWidget should be directly called from FormDesignWidget.java
		
		//tabs.addSelectionHandler(this);

		FormUtil.maximizeWidget(tabs);
		FormUtil.maximizeWidget(this);

		designWidget.addFormSelectionListener(this);
	}

	private void initDesignTab(){
		tabs.add(designWidget, "Design");
	}

	private void initXformsTab(){
		tabs.add(xformsWidget, "Xforms");
	}

	private void initItextTab(){
//		tabs.add(itextWidget, "Internationalization");
	}

	public void onWindowResized(int width, int height){
		int shortcutHeight = height - getAbsoluteTop();
		if(shortcutHeight > 50){
			xformsWidget.adjustHeight(shortcutHeight-130 + PurcConstants.UNITS);
			itextWidget.adjustHeight(shortcutHeight-50 + PurcConstants.UNITS);
		}

		designWidget.onWindowResized(width, height);
	}

	public void onNew(){
		designWidget.addNewForm();
	}
	public void showOpen(){
		xformsWidget.showWindow();
	}
	public void onOpen(){
		FormUtil.dlg.setText("Opening...");
		FormUtil.dlg.show();

		DeferredCommand.addCommand(new Command() {
			public void execute() {
				try{
					openFile();
					FormUtil.dlg.hide();
				}
				catch(Exception ex){
					FormUtil.displayException(ex);
				}
			}
		});
	}

	private void openFile(){
		String xml = xformsWidget.getXform();
		if(xml == null || xml.trim().length() == 0){
			showOpen();
			return;
		}

//		tabs.selectTab(TAB_INDEX_DESIGN);
		
		//org.openrosa.client.jr.core.model.FormDef formDef1 = org.openrosa.client.jr.xforms.parse.XFormParser.getFormDef(xml);
		
		formAttrMap.clear();
	    itextMap.clear();
	    ItextParser.itextFormAttrList.clear();
	    itextList = new ListStore<ItextModel>();
	    ItextBuilder.itextIds.clear();
	    
		FormDef formDef = XformParser.getFormDef(ItextParser.parse(xml,itextList,formAttrMap,itextMap));

		//Because we are still reusing the default purcforms xforms parsing, we need to set the page node.
		if(formDef.getQuestionCount() > 0){
			PageDef pageDef = formDef.getPageAt(0);
			QuestionDef questionDef = pageDef.getQuestionAt(0);
			if(questionDef.getControlNode() != null)
				pageDef.setGroupNode((Element)questionDef.getControlNode().getParentNode());
		}

		formDef.setXformXml(xml);
		designWidget.loadForm(formDef);
		//itextWidget.loadItext(list); on loading, form item is selected and this is eventually called.
	}

	public void onSave(boolean showWindow){
		FormUtil.dlg.setText("Saving...");
		FormUtil.dlg.show();
		showXMLWindowFlag = showWindow;
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				try{
					saveFile(showXMLWindowFlag);
					FormUtil.dlg.hide();
				}
				catch(Exception ex){
					FormUtil.displayException(ex);
				}
			}
		});
	}
	
	public void showItext(){
		
		this.saveFile(false);
		
		//String xml = null;
		
		if(Context.getFormDef() == null)
			formDef = null;
		
		if(formDef == null || formDef.getDoc() == null)
			return;

		//this line is necessary for gxt to load the form with text on the design tab.
		tabs.selectTab(TAB_INDEX_DESIGN);
		
		//This line is called in the this.saveFile(false) above.
		/*textBuilder.updateItextBlock(formDef.getDoc(), formDef, itextWidget.getItext(),formAttrMap,itextMap);
		xml = FormUtil.formatXml(XmlUtil.fromDoc2String(formDef.getDoc()));
		
		//update form outline with the itext changes
		itextList = new ListStore<ItextModel>();
		formDef = XformParser.getFormDef(ItextParser.parse(xml,itextList,formAttrMap,itextMap));
//		designWidget.refreshForm(formDef);*/
		itextWidget.loadItext(itextList);
		
		itextWidget.showWindow();
		
	}

	private void saveFile(boolean showWindow){
		//FormDef formDef = Context.getFormDef();
		
		if(Context.getFormDef() == null)
			formDef = null;

		String xml = null;
		
		if(tabs.getTabBar().getSelectedTab() == TAB_INDEX_ITEXT)
			xml = saveItext();
		else if(formDef != null){
			designWidget.commitChanges();

			//TODO need to solve bug when opened forms do not reflect changes in the instance data node names.
			//This is caused by copying a new model during the xhtml conversion
			Document doc = formDef.getDoc();

			if(doc != null)
				formDef.updateDoc(false);
			else{
				doc = XhtmlBuilder.fromFormDef2XhtmlDoc(formDef);
				formDef.setDoc(doc);
				formDef.setXformsNode(doc.getDocumentElement());
			}

			ItextBuilder.updateItextBlock(doc,formDef,itextList,formAttrMap,itextMap);
			//itextWidget.loadItext(itextList);

			doc.getDocumentElement().setAttribute("xmlns:jr", "http://openrosa.org/javarosa");
			doc.getDocumentElement().setAttribute("xmlns", "http://www.w3.org/1999/xhtml");

			//These purcforms attributes are not needed by javarosa
			if(formDef.getDataNode() != null){
				formDef.getDataNode().removeAttribute("name");
				formDef.getDataNode().removeAttribute("formKey");
			}
			xml = FormUtil.formatXml(XmlUtil.fromDoc2String(doc));
		}

		if(formDef != null)
			formDef.setXformXml(xml);
		else
			itextWidget.loadItext(itextList);

		xformsWidget.setXform(xml);
		if(showWindow){
			xformsWidget.showWindow();
		}
//		tabs.selectTab(TAB_INDEX_XFORMS);
		
	}

	public String saveItext() {
		String xml;
		if(formDef == null || formDef.getDoc() == null)
			return null;

		//this line is necessary for gxt to load the form with text on the design tab.
		tabs.selectTab(TAB_INDEX_DESIGN);
		
		ItextBuilder.updateItextBlock(formDef.getDoc(), formDef, itextWidget.getItext(),formAttrMap,itextMap);
		xml = FormUtil.formatXml(XmlUtil.fromDoc2String(formDef.getDoc()));

		//update form outline with the itext changes
		formDef = XformParser.getFormDef(ItextParser.parse(xml,itextList,formAttrMap,itextMap));
		designWidget.refreshForm(formDef);
		return xml;
	}

	public void onFormItemSelected(Object formItem){
		if(!(formItem instanceof FormDef))
			return;

		FormDef formDef = (FormDef)formItem;

		/*if(this.formDef == null || this.formDef != formItem){
			xformsWidget.setXform(formDef == null ? null : formDef.getXformXml());

			ListStore<ItextModel> list = new ListStore<ItextModel>();

			if(formDef != null && formDef.getDoc() != null)
				ItextBuilder.updateItextBlock(formDef.getDoc(),formDef,list,formAttrMap,itextMap);

			itextWidget.loadItext(list);
		}*/

		this.formDef = formDef;
	}
	
	public void onSaveItext(List<ItextModel> itext){
		String xml = saveItext();
		formDef.setXformXml(xml);
		xformsWidget.setXform(xml);
	}
}
