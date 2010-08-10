package org.purc.purcforms.client.widget;

import java.util.Vector;

import org.purc.purcforms.client.controller.ItemSelectionListener;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.util.QueryBuilderUtil;

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
 * 
 * @author daniel
 *
 */
public class FieldWidget extends Composite{

	//private static final int HORIZONTAL_SPACING = 5;
	private static final String EMPTY_VALUE = "_____";
	
	private FormDef formDef;
	private HorizontalPanel horizontalPanel;
	private SuggestBox sgstField = new SuggestBox();
	private TextBox txtField = new TextBox();
	private Anchor fieldAnchor;
	private ItemSelectionListener itemSelectionListener;
	private boolean forDynamicOptions = false;
	private QuestionDef dynamicQuestionDef;

	
	public FieldWidget(ItemSelectionListener itemSelectionListener){
		this.itemSelectionListener = itemSelectionListener;
		setupWidgets();
	}
	
	public void setFormDef(FormDef formDef){
		this.formDef = formDef;
		setupPopup();
	}
	
	private void setupWidgets(){
		fieldAnchor = new Anchor("", "#"); //Field 1
		
		horizontalPanel = new HorizontalPanel();
		horizontalPanel.add(fieldAnchor);

		fieldAnchor.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event){
				itemSelectionListener.onStartItemSelection(this);
				horizontalPanel.remove(fieldAnchor);
				horizontalPanel.add(sgstField);
				sgstField.setText(fieldAnchor.getText());
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
		
		sgstField.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>(){
			public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event){
				stopSelection();
			}
		});
		
		initWidget(horizontalPanel);
	}
	
	public void stopSelection(){
		if(horizontalPanel.getWidgetIndex(fieldAnchor) != -1)
			return;
		
		String val = sgstField.getText();
		if(val.trim().length() == 0)
			val = EMPTY_VALUE;
		fieldAnchor.setText(val);
		horizontalPanel.remove(sgstField);
		horizontalPanel.add(fieldAnchor);
		QuestionDef qtn = formDef.getQuestionWithText(txtField.getText());
		if(qtn != null)
			itemSelectionListener.onItemSelected(this,qtn);
	}
	
	private void setupPopup(){
		MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();

		for(int i=0; i<formDef.getPageCount(); i++)
			QueryBuilderUtil.loadQuestions(formDef.getPageAt(i).getQuestions(),dynamicQuestionDef,oracle,forDynamicOptions);
		
		txtField = new TextBox(); //TODO New and hence could be buggy
		sgstField = new SuggestBox(oracle,txtField);
		selectFirstQuestion();
		
		sgstField.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>(){
			public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event){
					stopSelection();
			}
		});
		
		/*sgstField.addFocusListener(new FocusListenerAdapter(){
			public void onLostFocus(Widget sender){
				stopSelection();
			}
		});*/
	}
	
	public void selectQuestion(QuestionDef questionDef){
		fieldAnchor.setText(questionDef.getText());
		itemSelectionListener.onItemSelected(this, questionDef);
	}
	
	private void selectFirstQuestion(){
		for(int i=0; i<formDef.getPageCount(); i++){
			if(selectFirstQuestion(formDef.getPageAt(i).getQuestions()))
				return;
		}
	}
	
	private boolean selectFirstQuestion(Vector<QuestionDef> questions){
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
		if(questionDef != null)
			fieldAnchor.setText(questionDef.getText());
		else
			fieldAnchor.setText("");
	}
	
	public void setForDynamicOptions(boolean forDynamicOptions){
		this.forDynamicOptions = forDynamicOptions;
	}
	
	public void setDynamicQuestionDef(QuestionDef dynamicQuestionDef){
		this.dynamicQuestionDef = dynamicQuestionDef;
	}
}
