package org.purc.purcforms.client.view;

import java.util.ArrayList;
import java.util.List;

import org.purc.purcforms.client.PurcConstants;
import org.purc.purcforms.client.controller.QuestionSelectionListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.PageDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.model.SkipRule;
import org.purc.purcforms.client.util.FormUtil;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * Handles selection of multiple questions as targets for a given skip rule.
 * 
 * @author daniel
 *
 */
public class SkipQtnsDialog  extends DialogBox {

	/** Widget displaying list of all questions on the form. */
	private ListBox lbAllQtns = new ListBox(true);
	
	/** Widget displaying list of selected questions. */
	private ListBox lbSelQtns = new ListBox(true);
	
	/** Widget to select one or more questions. */
	private Button btnSelect = new Button(">>");
	
	/** Widget to remove one or more selected questions. */
	private Button btnDeselect = new Button("<<");
	
	/** Button to commit changes and close this dialog box. */
	private Button btnOk = new Button(LocaleText.get("ok"));
	
	/** Button to cancel changes, if any, and close this dialog box. */
	private Button btnCancel = new Button(LocaleText.get("cancel"));
	
	/** Main or root widget for this dialog box. */
	private VerticalPanel mainPanel = new VerticalPanel();
		
	/** Call back for communicating to interested parties
	 *  when user has finished selecting questions. */
	private QuestionSelectionListener qtnSelListener;
	
	
	/**
	 * Creates a new instance of skip questions dialog box.
	 */
	public SkipQtnsDialog(QuestionSelectionListener qtnSelListener){
		
		this.qtnSelListener = qtnSelListener;
		
		lbAllQtns.setWidth("250"+PurcConstants.UNITS);
		lbSelQtns.setWidth("250"+PurcConstants.UNITS);
		lbAllQtns.setHeight("200"+PurcConstants.UNITS);
		lbSelQtns.setHeight("200"+PurcConstants.UNITS);
		
		setWidget(mainPanel);

		setupHeaderLabels();
		
		HorizontalPanel horzPanel = new HorizontalPanel();
		horzPanel.add(lbAllQtns);
		
		lbAllQtns.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				btnSelect.setEnabled(true);
			}
		});
		
		lbSelQtns.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				btnDeselect.setEnabled(true);
			}
		});
		
		setupSelectionButtons(horzPanel);
		
		horzPanel.add(lbSelQtns);
		horzPanel.setSpacing(5);
		mainPanel.add(horzPanel);

		setupOkCancelButtons();
		
		mainPanel.setSpacing(5);
		
		setText(LocaleText.get("otherQuestions"));
	}
	
	
	/**
	 * Sets the form and currently selected question for the skip rule.
	 * 
	 * @param formDef the form definition object.
	 * @param questionDef the question definition object to which the skip rule is being set.
	 * @param skipRule the current skip rule.
	 */
	public void setData(FormDef formDef, QuestionDef questionDef, SkipRule skipRule){
		lbAllQtns.clear();
		lbSelQtns.clear();
		
		for(int index = 0; index < formDef.getPageCount(); index++)
			loadPageQnts(formDef.getPageAt(index),questionDef,skipRule);
		
		if(skipRule != null && skipRule.getActionTargets() != null)
			loadSelQuestions(formDef, questionDef, skipRule.getActionTargets());
	}
	
	
	/**
	 * Loads a list of selected questions in the selected questions list box.
	 * 
	 * @param formDef the form definition object.
	 * @param questionDef the question definition object to which the skip rule is being set.
	 * @param selQuestions the selected questions list.
	 */
	private void loadSelQuestions(FormDef formDef, QuestionDef questionDef, List<Integer> selQuestions){
		for(int index = 0; index < selQuestions.size(); index++){
			Integer qtnId = selQuestions.get(index);
			
			QuestionDef qtnDef = formDef.getQuestion(qtnId);
			if(qtnDef == null)
				continue;
			
			if(qtnDef == questionDef)
				continue;
			
			lbSelQtns.addItem(qtnDef.getDisplayText(), qtnDef.getVariableName());
		}
	}
	
	
	/**
	 * Loads the all questions list box with questions from a given page.
	 * 
	 * @param pageDef the page definition object.
	 * @param questionDef the question definition object.
	 * @param skipRule the current skip rule.
	 */
	private void loadPageQnts(PageDef pageDef,QuestionDef questionDef, SkipRule skipRule){
		for(int index = 0; index < pageDef.getQuestionCount(); index++){
			QuestionDef qtnDef = pageDef.getQuestionAt(index);
			if(qtnDef == questionDef)
				continue;
			
			if(skipRule != null && skipRule.containsActionTarget(qtnDef.getId()))
				continue;
			
			lbAllQtns.addItem(qtnDef.getDisplayText(), qtnDef.getVariableName());
		}
	}
	
	
	/**
	 * Sets up the header labels for the all and selected questions list boxes.
	 */
	private void setupHeaderLabels(){
		HorizontalPanel horzPanel = new HorizontalPanel();
		Label lblAllQtns = new Label(LocaleText.get("allQuestions"));
		Label lblSelQtns = new Label(LocaleText.get("selectedQuestions"));
		
		horzPanel.add(lblAllQtns);
		horzPanel.add(lblSelQtns);
		
		horzPanel.setCellHorizontalAlignment(lblAllQtns, HasAlignment.ALIGN_CENTER);
		horzPanel.setCellHorizontalAlignment(lblSelQtns, HasAlignment.ALIGN_CENTER);
		FormUtil.maximizeWidget(horzPanel);
		
		mainPanel.add(horzPanel);
	}
	
	
	/**
	 * Sets up the select and the remove selection buttons.
	 * 
	 * @param parentPanel the panel to contain these buttons.
	 */
	private void setupSelectionButtons(HorizontalPanel parentPanel){
		VerticalPanel vertPanel = new VerticalPanel();
		vertPanel.add(btnSelect);
		vertPanel.add(btnDeselect);
		vertPanel.setCellVerticalAlignment(btnSelect, HasAlignment.ALIGN_MIDDLE);
		vertPanel.setCellVerticalAlignment(btnDeselect, HasAlignment.ALIGN_MIDDLE);
		vertPanel.setHeight("200"+PurcConstants.UNITS);
		
		parentPanel.add(vertPanel);
		
		btnSelect.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				selectQuestions();
			}
		});
		
		btnDeselect.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				deselectQuestions();
			}
		});
		
		btnSelect.setEnabled(false);
		btnDeselect.setEnabled(false);
	}
	
	
	/**
	 * Sets up the Ok and Cancel buttons.
	 */
	private void setupOkCancelButtons(){
		btnOk.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				hide();
				qtnSelListener.onQuestionsSelected(getSelectedQtns());
			}
		});
		
		btnCancel.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				hide();
			}
		});

		HorizontalPanel horzPanel = new HorizontalPanel();
		
		horzPanel.add(btnOk);
		horzPanel.add(btnCancel);
		
		horzPanel.setCellHorizontalAlignment(btnOk, HasAlignment.ALIGN_CENTER);
		horzPanel.setCellHorizontalAlignment(btnCancel, HasAlignment.ALIGN_CENTER);
		FormUtil.maximizeWidget(horzPanel);
		
		btnOk.setEnabled(false);
		
		mainPanel.add(horzPanel);
	}
	
	
	/**
	 * Removes selected questions from the all questions list box and puts them in the
	 * selected questions list box.
	 */
	private void selectQuestions(){
		for(int index = 0; index < lbAllQtns.getItemCount(); index++){
			if(lbAllQtns.isItemSelected(index)){
				lbSelQtns.addItem(lbAllQtns.getItemText(index), lbAllQtns.getValue(index));
				lbAllQtns.removeItem(index);
				index--;
			}
		}
		
		btnSelect.setEnabled(false);
		btnOk.setEnabled(true/*lbSelQtns.getItemCount() > 0*/); //TODO need to be smarter than this
	}
	
	
	/**
	 * Removes selected questions from the selected questions list box and puts
	 * them in the all questions list box.
	 */
	private void deselectQuestions(){
		for(int index = 0; index < lbSelQtns.getItemCount(); index++){
			if(lbSelQtns.isItemSelected(index)){
				lbAllQtns.addItem(lbSelQtns.getItemText(index), lbSelQtns.getValue(index));
				lbSelQtns.removeItem(index);
				index--;
			}
		}
		
		btnDeselect.setEnabled(false);
		btnOk.setEnabled(true /*lbSelQtns.getItemCount() > 0*/); //TODO Need to be smarter than this.
	}
	
	
	/**
	 * Gets a list of questions which have been selected.
	 * 
	 * @return the questions list.
	 */
	private List<String> getSelectedQtns(){
		List<String> selectedQtns = new ArrayList<String>();
		
		for(int index = 0; index < lbSelQtns.getItemCount(); index++)
			selectedQtns.add(lbSelQtns.getValue(index));
		
		return selectedQtns;
	}
}
