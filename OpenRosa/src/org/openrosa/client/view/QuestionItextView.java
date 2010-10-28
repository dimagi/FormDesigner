package org.openrosa.client.view;

import org.openrosa.client.Context;
import org.openrosa.client.model.ItextModel;
import org.openrosa.client.model.QuestionDef;
import org.purc.purcforms.client.model.Locale;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;


/**
 * 
 * @author danielkayiwa
 *
 */
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
		/*this.enabled = enabled;

		lbOption.setEnabled(enabled);

		if(!enabled)
			clear();*/
	}
	
	public void update(){
		
	}
	
	public void setQuestionDef(QuestionDef questionDef){
		table.clear();
		
		if(questionDef == null)
			return;
		
		final ItextModel itextModel = Context.getItextMap().get(questionDef.getItextId());
		
		int row  = 0;
		for(Locale locale : Context.getLocales()){
			
			//Text for the current locale is not shown in this itext tab.
			if(locale.getKey().equals(Context.getLocale().getKey()))
				continue;
			
			final String localeName = locale.getName();
			
			table.setWidget(row, 0, new Label(localeName));
			
			final TextBox txt = new TextBox();
			txt.setText((itextModel != null) ? (String)itextModel.get(localeName) : questionDef.getText());
			
			txt.addChangeHandler(new ChangeHandler(){
				public void onChange(ChangeEvent event){
					itextModel.set(localeName, txt.getText());
				}
			});
			
			table.setWidget(row, 1, txt);
			
			row++;
		}
	}
}
