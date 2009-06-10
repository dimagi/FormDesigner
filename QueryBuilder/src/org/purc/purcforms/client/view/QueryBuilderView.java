package org.purc.purcforms.client.view;

import org.purc.purcforms.client.sql.SqlBuilder;
import org.purc.purcforms.client.xforms.XformConverter;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.Label;
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
	
	public QueryBuilderView(){
		
		txtXform.setWidth("100%");
		txtXform.setHeight("100%");
		tabs.setWidth("100%");
		tabs.setHeight("100%");
		
		tabs.add(txtXform,"XForms Source");
		tabs.add(filterConditionsView,"Filter Conditions");
		tabs.add(new Label(),"Display Fields");
		tabs.add(txtDefXml,"Definition XML");
		tabs.add(txtSql,"SQL");
		
		tabs.addTabListener(this);
		initWidget(tabs);
		
		tabs.selectTab(1);
		
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
	}
	
	public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex) {
		return true;
	}

	public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
		selectedTabIndex = tabIndex;
		
		if(selectedTabIndex == 4){
			filterConditionsView.updateSkipRule();
			txtSql.setText(SqlBuilder.buildSql(filterConditionsView.getFormDef(),filterConditionsView.getSkipRule()));
		}
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
		String xml = txtXform.getText().trim();
		if(xml.length() > 0)
			filterConditionsView.setFormDef(XformConverter.fromXform2FormDef(xml));
	}
}
