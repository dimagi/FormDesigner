package org.purc.purcforms.client.view;

import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.sql.SqlBuilder;
import org.purc.purcforms.client.sql.XmlBuilder;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.xforms.XformConverter;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author daniel
 *
 */
public class QueryBuilderView  extends Composite implements WindowResizeListener,TabListener{

	private int selectedTabIndex;
	private DecoratedTabPanel tabs = new DecoratedTabPanel();
	private TextArea txtXform = new TextArea();
	private TextArea txtDefXml= new TextArea();
	private TextArea txtSql = new TextArea();
	
	private FilterConditionsView filterConditionsView = new FilterConditionsView();
	private DisplayFieldsView displayFieldsView = new DisplayFieldsView();
	
	public QueryBuilderView(){
		
		txtXform.setWidth("100%");
		txtXform.setHeight("100%");
		tabs.setWidth("100%");
		tabs.setHeight("100%");
		
		tabs.add(txtXform,"XForms Source");
		tabs.add(filterConditionsView,"Filter Conditions");
		tabs.add(displayFieldsView,"Display Fields");
		tabs.add(txtDefXml,"Definition XML");
		tabs.add(txtSql,"SQL");
		
		tabs.addTabListener(this);
		initWidget(tabs);
		
		tabs.selectTab(2);
		
		Window.addWindowResizeListener(this);

		//		This is needed for IE
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				onWindowResized(Window.getClientWidth(), Window.getClientHeight());
			}
		});
		
		txtXform.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				parseXform();
			}
		});
		
		txtDefXml.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				parseQueryDef();
			}
		});
		
		txtXform.setText(FormUtil.formatXml(getTestXform()));
		parseXform();
		
		txtDefXml.setText(getTestQueryDef());
		parseQueryDef();
	}
	
	public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex) {
		return true;
	}

	public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
		selectedTabIndex = tabIndex;
		
		FormUtil.dlg.setText("Building " + (selectedTabIndex == 3 ? "Query Definition" : "SQL")); //LocaleText.get("???????")
		FormUtil.dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {
				try{
					if(selectedTabIndex == 3)
						txtDefXml.setText(FormUtil.formatXml(FormUtil.formatXml(XmlBuilder.buildXml(filterConditionsView.getFormDef(),filterConditionsView.getFilterConditionRows()))));
					else if(selectedTabIndex == 4)
						txtSql.setText(SqlBuilder.buildSql(filterConditionsView.getFormDef(),filterConditionsView.getFilterConditionRows()));

					FormUtil.dlg.hide();
				}
				catch(Exception ex){
					FormUtil.dlg.hide();
					FormUtil.displayException(ex);
				}	
			}
		});
	}
	
	public void onWindowResized(int width, int height) {
		txtXform.setHeight(height-50+"px");
		txtDefXml.setHeight(height-50+"px");
		txtSql.setHeight(height-50+"px");
		
		/*height -= (110+embeddedHeightOffset);
		sHeight = height+"px";
		super.setHeight(sHeight);

		if(selectedPanel != null)
			//selectedPanel.setHeight("100%");
			selectedPanel.setHeight(sHeight);*/
	} 
	
	private void parseXform(){
		FormUtil.dlg.setText("Parsing Xform"); //LocaleText.get("???????")
		FormUtil.dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {
				try{
					String xml = txtXform.getText().trim();
					if(xml.length() > 0){
						FormDef formDef = XformConverter.fromXform2FormDef(xml);
						filterConditionsView.setFormDef(formDef);
						displayFieldsView.setFormDef(formDef);
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
	
	private void parseQueryDef(){
		FormUtil.dlg.setText("Parsing Query Definition"); //LocaleText.get("???????")
		FormUtil.dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {
				try{
					String xml = txtDefXml.getText().trim();
					if(xml.length() > 0)
						filterConditionsView.loadQueryDef(xml);
					
					FormUtil.dlg.hide();
				}
				catch(Exception ex){
					FormUtil.dlg.hide();
					FormUtil.displayException(ex);
				}	
			}
		});
	}
	
	private String getTestXform(){
		return "<xf:xforms xmlns:xf=\"http://www.w3.org/2002/xforms\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"> " +
			" <xf:model> " +
			"   <xf:instance id=\"newform1\"> " +
			"     <newform1 name=\"New Form1\" id=\"1\"> " +
			"       <question1/> " +
			"       <question2/> " +
			"     </newform1> " +
			"   </xf:instance> " +
			"   <xf:bind id=\"question1\" nodeset=\"/newform1/question1\" type=\"xsd:string\"/> " +
			"   <xf:bind id=\"question2\" nodeset=\"/newform1/question2\" type=\"xsd:string\"/> " +
			" </xf:model> " +
			" <xf:group id=\"1\"> " +
			"   <xf:label>Page1</xf:label> " +
			"  <xf:input bind=\"question1\"> " +
			"    <xf:label>Question1</xf:label> " +
			"  </xf:input> " +
			"  <xf:input bind=\"question2\"> " +
			"    <xf:label>Question2</xf:label> " +
			"  </xf:input> " +
			" </xf:group> " +
			" </xf:xforms>";
	}
	
	private String getTestQueryDef(){
		return "<querydef> "+
			  " <group operator=\"all\"> "+
			  " <group operator=\"all\"> "+
			  "     <condition field=\"question1\" operator=\"1\" value=\"aaa\"/> "+
			  "   </group> "+
			  "   <group operator=\"all\"> "+
			  "     <condition field=\"question1\" operator=\"1\" value=\"bbbb\"/> "+
			  "   </group> "+
			  " </group> "+
			  " </querydef>";
	}
}
