package org.purc.purcforms.client.widget.skiprule;

import java.util.Vector;

import org.purc.purcforms.client.controller.ItemSelectionListener;
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
 * 
 * @author daniel
 *
 */
public class FieldWidget extends Composite{

	private static final int HORIZONTAL_SPACING = 5;
	private static final String EMPTY_VALUE = "_____";
	
	private FormDef formDef;
	private HorizontalPanel horizontalPanel;
	private SuggestBox sgstField = new SuggestBox();
	private TextBox txtField = new TextBox();
	private Hyperlink fieldHyperlink;
	private ItemSelectionListener itemSelectionListener;
	
	public FieldWidget(ItemSelectionListener itemSelectionListener){
		this.itemSelectionListener = itemSelectionListener;
		setupWidgets();
	}
	
	public void setFormDef(FormDef formDef){
		this.formDef = formDef;
		setupPopup();
	}
	
	private void setupWidgets(){
		fieldHyperlink = new Hyperlink("Field 1",null);
		
		horizontalPanel = new HorizontalPanel();
		horizontalPanel.add(fieldHyperlink);

		fieldHyperlink.addClickListener(new ClickListener(){
			public void onClick(Widget sender){
				itemSelectionListener.onStartItemSelection(this);
				horizontalPanel.remove(fieldHyperlink);
				horizontalPanel.add(sgstField);
				sgstField.setText(fieldHyperlink.getText());
				sgstField.setFocus(true);
				txtField.selectAll();
			}
		});
		
		/*txtField.addFocusListener(new FocusListenerAdapter(){
			public void onLostFocus(Widget sender){
				stopSelection();
			}
		});
		
		/*sgstField.addFocusListener(new FocusListenerAdapter(){
			public void onLostFocus(Widget sender){
				stopSelection();
			}
		});*/
		
		sgstField.addEventHandler(new SuggestionHandler(){
			public void onSuggestionSelected(SuggestionEvent event){
				stopSelection();
			}
		});
		
		initWidget(horizontalPanel);
	}
	
	public void stopSelection(){
		if(horizontalPanel.getWidgetIndex(fieldHyperlink) != -1)
			return;
		
		String val = sgstField.getText();
		if(val.trim().length() == 0)
			val = EMPTY_VALUE;
		fieldHyperlink.setText(val);
		horizontalPanel.remove(sgstField);
		horizontalPanel.add(fieldHyperlink);
		QuestionDef qtn = formDef.getQuestionWithText(txtField.getText());
		if(qtn != null)
			itemSelectionListener.onItemSelected(this,qtn);
	}
	
	private void setupPopup(){
		MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();

		for(int i=0; i<formDef.getPageCount(); i++)
			FormDesignerUtil.loadQuestions(formDef.getPageAt(i).getQuestions(),null,oracle);
		
		sgstField = new SuggestBox(oracle,txtField);
		selectFirstQuestion();
		
		sgstField.addEventHandler(new SuggestionHandler(){
			public void onSuggestionSelected(SuggestionEvent event){
					stopSelection();
			}
		});
		
		/*sgstField.addFocusListener(new FocusListenerAdapter(){
			public void onLostFocus(Widget sender){
				stopSelection();
			}
		});*/
	}
	
	private void selectQuestion(QuestionDef questionDef){
		fieldHyperlink.setText(questionDef.getText());
		itemSelectionListener.onItemSelected(this, questionDef);
	}
	
	private void selectFirstQuestion(){
		for(int i=0; i<formDef.getPageCount(); i++){
			if(selectFirstQuestion(formDef.getPageAt(i).getQuestions()))
				return;
		}
	}
	
	private boolean selectFirstQuestion(Vector questions){
		for(int i=0; i<questions.size(); i++){
			QuestionDef questionDef = (QuestionDef)questions.elementAt(i);
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
				selectFirstQuestion(questionDef.getRepeatQtnsDef().getQuestions());
			else{
				selectQuestion(questionDef);
				return true;
			}
		}
		return false;
	}
	
	public void setQuestion(QuestionDef questionDef){
		fieldHyperlink.setText(questionDef.getText());
	}
}
