package org.purc.purcforms.client.view;

import org.purc.purcforms.client.PurcConstants;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.sql.SqlBuilder;
import org.purc.purcforms.client.sql.XmlBuilder;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.xforms.XformParser;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.TextArea;


/**
 * 
 * @author daniel
 *
 */
public class QueryBuilderView  extends Composite implements SelectionHandler<Integer>,ResizeHandler{

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
		
		tabs.addSelectionHandler(this);
		initWidget(tabs);
		
		tabs.selectTab(1);
		
		Window.addResizeHandler(this);

		//		This is needed for IE
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				onWindowResized(Window.getClientWidth(), Window.getClientHeight());
			}
		});
		
		txtXform.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				parseXform();
			}
		});
		
		txtDefXml.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				parseQueryDef();
			}
		});
		
		//txtXform.setText(FormUtil.formatXml(getTestXform()));
		//parseXform();
		
		//txtDefXml.setText(getTestQueryDef());
		//parseQueryDef();
	}

	/**
	 * @see com.google.gwt.event.logical.shared.SelectionHandler#onSelection(SelectionEvent)
	 */
	public void onSelection(SelectionEvent<Integer> event){
		selectedTabIndex = event.getSelectedItem();
		
		FormUtil.dlg.setText("Building " + (selectedTabIndex == 3 ? "Query Definition" : "SQL")); //LocaleText.get("???????")
		FormUtil.dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {
				try{
					if(selectedTabIndex == 3)
						buildQueryDef();
					else if(selectedTabIndex == 4)
						buildSql();

					FormUtil.dlg.hide();
				}
				catch(Exception ex){
					FormUtil.displayException(ex);
				}	
			}
		});
	}
	
	public void onWindowResized(int width, int height) {
		txtXform.setHeight(height-50+PurcConstants.UNITS);
		txtDefXml.setHeight(height-50+PurcConstants.UNITS);
		txtSql.setHeight(height-50+PurcConstants.UNITS);
	} 
	
	private void parseXform(){
		FormUtil.dlg.setText("Parsing Xform"); //LocaleText.get("???????")
		FormUtil.dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {
				try{
					FormDef formDef = null;
					String xml = txtXform.getText().trim();
					if(xml.length() > 0)
						formDef = XformParser.fromXform2FormDef(xml);

					filterConditionsView.setFormDef(formDef);
					displayFieldsView.setFormDef(formDef);
					
					FormUtil.dlg.hide();
				}
				catch(Exception ex){
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
					if(xml.length() > 0){
						filterConditionsView.loadQueryDef(xml);
						displayFieldsView.loadQueryDef(xml);
					}
					
					FormUtil.dlg.hide();
				}
				catch(Exception ex){
					FormUtil.displayException(ex);
				}	
			}
		});
	}
	
	/*private String getTestXform(){
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
			"   <xf:bind id=\"question3\" nodeset=\"/newform1/question3\" type=\"xsd:int\"/> " +
			"   <xf:bind id=\"question4\" nodeset=\"/newform1/question4\" type=\"xsd:date\"/> " +
			" </xf:model> " +
			" <xf:group id=\"1\"> " +
			"   <xf:label>Page1</xf:label> " +
			"  <xf:input bind=\"question1\"> " +
			"    <xf:label>Question1</xf:label> " +
			"  </xf:input> " +
			"  <xf:input bind=\"question2\"> " +
			"    <xf:label>Question2</xf:label> " +
			"  </xf:input> " +
			"  <xf:input bind=\"question3\"> " +
			"    <xf:label>Question3</xf:label> " +
			"  </xf:input> " +
			"  <xf:input bind=\"question4\"> " +
			"    <xf:label>Question4</xf:label> " +
			"  </xf:input> " +
			" </xf:group> " +
			" </xf:xforms>";
	}
	
	private String getTestQueryDef(){
		return "<querydef> "+
			  " <FilterConditions> "+
			  " <group operator=\"all\"> "+
			  " <group operator=\"all\"> "+
			  "     <condition field=\"question1\" operator=\"1\" value=\"aaa\"/> "+
			  "   </group> "+
			  "   <group operator=\"all\"> "+
			  "     <condition field=\"question1\" operator=\"1\" value=\"bbbb\"/> "+
			  "   </group> "+
			  " </group> "+
			  " </FilterConditions> " +
			  " <DisplayFields> " +
			  " 	<Field name=\"question1\" text=\"Last Name\"/> " +
			  " 	<Field name=\"question2\" text=\"First Name\"/> " +
			  " 	<Field name=\"question3\" text=\"Weight\" AggFunc=\"COUNT\" /> " +
			  " 	<Field name=\"question4\" text=\"Date of Birth\"/> " +
			  " </DisplayFields> " +
			  " <SortFields> " +
			  " 	<Field name=\"question1\" sortOrder=\"1\"/> " +
			  " 	<Field name=\"question2\" sortOrder=\"2\"/> " +
			  " </SortFields> " +
			  " </querydef>";
	}*/
	
	private void buildSql(){
		txtSql.setText(SqlBuilder.buildSql(filterConditionsView.getFormDef(),displayFieldsView.getDisplayFields(),filterConditionsView.getFilterConditionRows(),displayFieldsView.getSortFields()));
	}
	
	private void buildQueryDef(){
		txtDefXml.setText(FormUtil.formatXml(FormUtil.formatXml(XmlBuilder.buildXml(filterConditionsView.getFormDef(),filterConditionsView.getFilterConditionRows(),displayFieldsView.getDisplayFields(),displayFieldsView.getSortFields()))));
	}
	
	public String getQueryDef(){
		buildQueryDef();
		return txtDefXml.getText();
	}
	
	public String getSql(){
		buildSql();
		return txtSql.getText();
	}
	
	public void setXform(String xml){
		txtXform.setText(xml);
		//parseXform();
	}
	
	public void setQueryDef(String xml){
		txtDefXml.setText(xml);
		//parseQueryDef();
	}
	
	public void setSql(String sql){
		txtSql.setText(sql);
	}
	
	public void load(){
		parseXform();
		parseQueryDef();
	}
	
	public void onResize(ResizeEvent event){
		onWindowResized(Window.getClientWidth(), Window.getClientHeight());
	}
	
	public void hideDebugTabs(){
		tabs.remove(0);
		tabs.remove(2);
		tabs.remove(2);
	}
}
