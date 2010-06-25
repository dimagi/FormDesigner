package org.purc.purcforms.client.view;

import java.util.ArrayList;
import java.util.List;

import org.purc.purcforms.client.Context;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.Locale;
import org.purc.purcforms.client.util.FormUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;


/**
 * Widget for editing locales.
 * 
 * @author daniel
 *
 */
public class LocalesDialog  extends DialogBox {

	private FlexTable table = new FlexTable();
	
	/** Button to add a new language. */
	private Button btnAdd = new Button(LocaleText.get("addNew"));
	
	
	/**
	 * Creates a new instance of the locale dialog box.
	 */
	public LocalesDialog(){
		super(false,true);
		
		VerticalPanel panel = new VerticalPanel();
		panel.setSpacing(5);
		setWidget(panel);
		
		table.setWidget(0, 0, new Label("Name"));
		table.setWidget(0, 1, new Label("Key"));
		table.setWidget(0, 2, new Label("Delete"));
		
		table.getCellFormatter().setStyleName(0, 0, "getting-started-label");
		table.getCellFormatter().setStyleName(0, 1, "getting-started-label");
		table.getCellFormatter().setStyleName(0, 2, "getting-started-label");
		
		table.setStyleName("cw-FlexTable");		
		panel.add(table);
		
		
		HorizontalPanel horzPanel = new HorizontalPanel();
		FormUtil.maximizeWidget(horzPanel);
		panel.add(horzPanel);
		
		Button btn = new Button(LocaleText.get("save"), new ClickHandler() {
			public void onClick(ClickEvent event) {
				save();
				hide();
			}
		});

		horzPanel.add(btn);
		horzPanel.setCellHorizontalAlignment(btn, HasAlignment.ALIGN_LEFT);
		
		btn = new Button(LocaleText.get("cancel"), new ClickHandler() {
			public void onClick(ClickEvent event) {
				hide();
			}
		});

		horzPanel.add(btn);
		horzPanel.setCellHorizontalAlignment(btn, HasAlignment.ALIGN_RIGHT);

		setText(LocaleText.get("languages"));
		
		loadLocales();
		
		btnAdd.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addNewLocale();
			}
		});
	}
	
	private void addNewLocale(){
		table.removeRow(table.getRowCount() - 1);
		TextBox textBox = addNewLocale("","",table.getRowCount());
		textBox.setFocus(true);
		addAddButton();
	}

	
	private TextBox addNewLocale(String name, String key, int row){
		TextBox txtName = new TextBox();
		txtName.setText(name);
		table.setWidget(row, 0, txtName);
		
		TextBox txtKey = new TextBox();
		txtKey.setText(key);
		table.setWidget(row, 1, txtKey);			
		
		HorizontalPanel panel = new HorizontalPanel();
		Button btn = new Button("Delete");
		panel.add(btn);
		table.setWidget(row,2, panel);
		table.getCellFormatter().setHorizontalAlignment(row, 3, HasHorizontalAlignment.ALIGN_CENTER);

		btn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				deleteLocale(((Button)event.getSource()).getParent());
			}
		});
		
		txtName.addKeyDownHandler(new KeyDownHandler(){
			public void onKeyDown(KeyDownEvent event) {
				int keyCode = event.getNativeKeyCode();
				if(keyCode == KeyCodes.KEY_ENTER || keyCode == KeyCodes.KEY_DOWN)
					moveToNextWidget((Widget)event.getSource(),0,keyCode == KeyCodes.KEY_DOWN);
				else if(keyCode == KeyCodes.KEY_UP)
					moveToPrevWidget((Widget)event.getSource(),0);
			}
		});
		
		txtKey.addKeyDownHandler(new KeyDownHandler(){
			public void onKeyDown(KeyDownEvent event) {
				int keyCode = event.getNativeKeyCode();
				if(keyCode == KeyCodes.KEY_ENTER || keyCode == KeyCodes.KEY_DOWN)
					moveToNextWidget((Widget)event.getSource(),1,keyCode == KeyCodes.KEY_DOWN);
				else if(keyCode == KeyCodes.KEY_UP)
					moveToPrevWidget((Widget)event.getSource(),1);
			}
		});
		
		return txtName;
	}
	
	
	@Override
	public boolean onKeyDownPreview(char key, int modifiers) {
		// Use the popup's key preview hooks to close the dialog when either
		// enter or escape is pressed.
		switch (key) {
		case KeyCodes.KEY_ESCAPE:
			hide();
			break;
		}

		return true;
	}
	
	
	private void loadLocales(){
		int index = 1;
		for(Locale locale : Context.getLocales()){			
			addNewLocale(locale.getName(), locale.getKey(), index);
			index++;
		}
		
		addAddButton();
	}
	
	
	private void deleteLocale(Widget widget){		
		if(!Window.confirm("Do you really want to delete the " + getLanguageName(widget) + " " + LocaleText.get("language") + "?"))
			return;
		
		table.removeRow(getWidgetIndex(widget));
	}
	
	private String getLanguageName(Widget widget){
		int rowCount = table.getRowCount();
		for(int row = 0; row < rowCount; row++){
			if(widget == table.getWidget(row, 2))
				return ((TextBox)table.getWidget(row, 0)).getText();
		}
		return null;
	}
	
	private int getWidgetIndex(Widget widget){
		int rowCount = table.getRowCount();
		for(int row = 0; row < rowCount; row++){
			if(widget == table.getWidget(row, 2))
				return row;
		}
		return -1;
	}
	
	
	private void save(){
		List<Locale> locales = new ArrayList<Locale>();
		
		int rowCount = table.getRowCount() - 1;
		for(int row = 1; row < rowCount; row++){
			Locale locale = new Locale(((TextBox)table.getWidget(row, 1)).getText(), ((TextBox)table.getWidget(row, 0)).getText());
			
			if(locale.getName().trim().length() == 0)
				continue;
			
			if(locale.getKey().trim().length() == 0)
				locale.setKey(locale.getName());
			
			locales.add(locale);
		}
		
		Context.setLocales(locales);
	}
	
	
	/**
	 * Adds the add new button to the table widget.
	 */
	private void addAddButton(){
		FlexCellFormatter cellFormatter = table.getFlexCellFormatter();
		int row = table.getRowCount();
		cellFormatter.setColSpan(row, 0, 3);
		cellFormatter.setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER);
		table.setWidget(row, 0, btnAdd);
	}
	
	/**
	 * Moves input focus to the next widget.
	 * 
	 * @param sender the widget after which to move the input focus.
	 * @param col the index of the column which currently has input focus.
	 * @param sameCol set to true to move to the next widget in the same column.
	 */
	private void moveToNextWidget(Widget sender, int col, boolean sameCol){
		if(sameCol){
			int rowCount = table.getRowCount();
			for(int row = 1; row < rowCount; row++){
				if(sender == table.getWidget(row, col)){
					if(row == (rowCount - 2))
						return;

					TextBox textBox = ((TextBox)table.getWidget(row + 1, col));
					textBox.setFocus(true);
					textBox.selectAll();
					break;
				}
			}
		}
		else{
			int rowCount = table.getRowCount();
			for(int row = 1; row < rowCount; row++){
				if(sender == table.getWidget(row, col)){
					TextBox textBox = ((TextBox)table.getWidget(row, col));
					if(col == 1){
						if(row == (rowCount - 2)){
							if(textBox.getText() != null && textBox.getText().trim().length() > 0)
								addNewLocale();
							return;
						}
						row++;
						col = 1; //0;
					}
					else{
						if(textBox.getText() == null || textBox.getText().trim().length() == 0)
							return;
						else if(row == (rowCount - 2)){
							addNewLocale();
							return;
						}
						else
							row++;
						
						col = 0; //1;
					}

					textBox = ((TextBox)table.getWidget(row, col));
					textBox.setFocus(true);
					textBox.selectAll();
					break;
				}
			}
		}
	}
	
	
	/**
	 * Moves input focus to the widget before.
	 * 
	 * @param sender the widget before which to move the input focus.
	 * @param col the index of the column which currently has input focus.
	 */
	private void moveToPrevWidget(Widget sender, int col){
		int rowCount = table.getRowCount();

		//Starting from index 1 since 0 is the header row.
		for(int row = 1; row < rowCount; row++){
			if(sender == table.getWidget(row, col)){
				if(row == 1)
					return;

				TextBox textBox = ((TextBox)table.getWidget(row - 1, col));
				textBox.setFocus(true);
				textBox.selectAll();
				break;
			}
		}
	}
}
