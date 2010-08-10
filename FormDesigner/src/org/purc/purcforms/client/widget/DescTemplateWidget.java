package org.purc.purcforms.client.widget;

import org.purc.purcforms.client.controller.ItemSelectionListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.util.FormDesignerUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;


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
	private Anchor fieldAnchor = new Anchor(LocaleText.get("addField"), "#");
	private TextBox txtField = new TextBox();

	private MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
	private boolean enabled = false;
	private ItemSelectionListener itemSelectionListener;
	
	private int questionCount = 0;

	public DescTemplateWidget(ItemSelectionListener itemSelectionListener){
		this.itemSelectionListener = itemSelectionListener;

		horizontalPanel.add(fieldAnchor);

		fieldAnchor.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event){
				if(enabled){
					if(questionCount == 0)
						setFormDef(formDef);
					
					horizontalPanel.remove(fieldAnchor);
					horizontalPanel.add(sgstField);
					sgstField.setText(fieldAnchor.getText());
					sgstField.setFocus(true);
					txtField.selectAll();
				}
			}
		});

		initWidget(horizontalPanel);
	}

	public void setFormDef(FormDef formDef){
		this.formDef = formDef;
		
		questionCount = formDef.getQuestionCount();
		
		oracle.clear();

		for(int i=0; i<formDef.getPageCount(); i++)
			FormDesignerUtil.loadQuestions(false, formDef.getPageAt(i).getQuestions(),null,oracle,false);

		setupPopup();
	}

	private void setupPopup(){
		
		txtField = new TextBox();
		sgstField = new SuggestBox(oracle,txtField);
		fieldAnchor.setText(LocaleText.get("addField"));

		sgstField.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>(){
			public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event){
				stopSelection();
			}
		});
	}

	public void stopSelection(){
		if(horizontalPanel.getWidgetIndex(fieldAnchor) != -1)
			return;

		String val = sgstField.getText();
		if(val.trim().length() == 0)
			val = LocaleText.get("addField");
		fieldAnchor.setText(val);
		horizontalPanel.remove(sgstField);
		horizontalPanel.add(fieldAnchor);
		QuestionDef qtn = formDef.getQuestionWithText(sgstField.getText());
		if(qtn != null){
			fieldAnchor.setText(LocaleText.get("addField"));
			itemSelectionListener.onItemSelected(this,"/"+formDef.getBinding()+"/"+qtn.getBinding());
		}
	}

	public void setEnabled(boolean enabled){
		this.enabled = enabled;
		if(!enabled && horizontalPanel.getWidgetIndex(fieldAnchor) == -1)
			stopSelection();
	}
}
