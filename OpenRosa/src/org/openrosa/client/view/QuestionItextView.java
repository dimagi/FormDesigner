package org.openrosa.client.view;

import org.openrosa.client.Context;
import org.openrosa.client.model.ItextModel;
import org.openrosa.client.model.QuestionDef;
import org.openrosa.client.util.Itext;
import org.openrosa.client.util.ItextLocale;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;


public class QuestionItextView extends Composite {

	private FlexTable table = new FlexTable();


	public QuestionItextView(){
		initWidget(table);
	}
	
	/**
	 * Sets whether to enable this widget or not.
	 * 
	 * @param enabled set to true to enable, else false.
	 */
	public void setEnabled(boolean enabled){

	}
	
	public void update(){
		
	}
	
	public void setQuestionDef(QuestionDef questionDef){
		table.clear();
		
		if(questionDef == null)
			return;
		
		final ItextModel itextRow = Itext.getItextRows().findModel("id", questionDef.getItextId());
		int row  = 0;
		for(ItextLocale locale : Itext.locales){
			
			final String localeName = locale.getName();
			
			table.setWidget(row, 0, new Label(localeName));
			
			final TextBox txt = new TextBox();
			txt.setText((itextRow != null) ? (String)itextRow.get(localeName) : questionDef.getText());
			
			txt.addChangeHandler(new ChangeHandler(){
				public void onChange(ChangeEvent event){ 
					itextRow.set(localeName, txt.getText());
				}
			});
			
			table.setWidget(row, 1, txt);
			
			row++;
		}
	}
}
