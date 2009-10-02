package org.purc.purcforms.client.widget;

import org.purc.purcforms.client.controller.ItemSelectionListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.util.FormDesignerUtil;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestionEvent;
import com.google.gwt.user.client.ui.SuggestionHandler;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;


/**
 * This widget helps the user to build the form description template
 * and saves them from having to cram the syntax.
 * 
 * @author daniel
 *
 */
public class DescTemplateWidget extends Composite{

	private HorizontalPanel horizontalPanel = new HorizontalPanel();
	private FormDef formDef;

	private SuggestBox sgstField = new SuggestBox();
	private Hyperlink fieldHyperlink = new Hyperlink(LocaleText.get("addField"),null);
	private TextBox txtField = new TextBox();

	private MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
	private boolean enabled = false;
	private ItemSelectionListener itemSelectionListener;

	public DescTemplateWidget(ItemSelectionListener itemSelectionListener){
		this.itemSelectionListener = itemSelectionListener;

		horizontalPanel.add(fieldHyperlink);

		fieldHyperlink.addClickListener(new ClickListener(){
			public void onClick(Widget sender){
				if(enabled){
					horizontalPanel.remove(fieldHyperlink);
					horizontalPanel.add(sgstField);
					sgstField.setText(fieldHyperlink.getText());
					sgstField.setFocus(true);
					txtField.selectAll();
				}
			}
		});

		initWidget(horizontalPanel);
	}

	public void setFormDef(FormDef formDef){
		this.formDef = formDef;
		oracle.clear();

		for(int i=0; i<formDef.getPageCount(); i++)
			FormDesignerUtil.loadQuestions(formDef.getPageAt(i).getQuestions(),null,oracle,false);

		setupPopup();
	}

	private void setupPopup(){
		
		txtField = new TextBox();
		sgstField = new SuggestBox(oracle,txtField);
		fieldHyperlink.setText(LocaleText.get("addField"));

		sgstField.addEventHandler(new SuggestionHandler(){
			public void onSuggestionSelected(SuggestionEvent event){
				stopSelection();
			}
		});
	}

	public void stopSelection(){
		if(horizontalPanel.getWidgetIndex(fieldHyperlink) != -1)
			return;

		String val = sgstField.getText();
		if(val.trim().length() == 0)
			val = LocaleText.get("addField");
		fieldHyperlink.setText(val);
		horizontalPanel.remove(sgstField);
		horizontalPanel.add(fieldHyperlink);
		QuestionDef qtn = formDef.getQuestionWithText(sgstField.getText());
		if(qtn != null){
			fieldHyperlink.setText(LocaleText.get("addField"));
			itemSelectionListener.onItemSelected(this,"${/"+formDef.getVariableName()+"/"+qtn.getVariableName()+"}$");
		}
	}

	public void setEnabled(boolean enabled){
		this.enabled = enabled;
		if(!enabled && horizontalPanel.getWidgetIndex(fieldHyperlink) == -1)
			stopSelection();
	}
}
