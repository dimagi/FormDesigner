package org.purc.purcforms.client.view;

import java.util.ArrayList;
import java.util.List;

import org.purc.purcforms.client.controller.QuestionSelectionListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.PageDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.util.FormUtil;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
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
	private Button btnDeselect = new Button("<<");
	
	private Button btnOk = new Button(LocaleText.get("ok"));
	private Button btnCancel = new Button(LocaleText.get("cancel"));
	
	private VerticalPanel mainPanel = new VerticalPanel();
	
	private List<String> selectedQtns = new ArrayList<String>();
	
	private QuestionSelectionListener qtnSelListener;
	
	
	/**
	 * Creates a new instance of skip questions dialog box.
	 */
	public SkipQtnsDialog(QuestionSelectionListener qtnSelListener){
		
		this.qtnSelListener = qtnSelListener;
		
		lbAllQtns.setWidth("200px");
		lbSelQtns.setWidth("200px");
		lbAllQtns.setHeight("200px");
		lbSelQtns.setHeight("200px");
		
		setWidget(mainPanel);

		setupHeaderLabels();
		
		HorizontalPanel horzPanel = new HorizontalPanel();
		horzPanel.add(lbAllQtns);
		
		lbAllQtns.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				btnSelect.setEnabled(true);
			}
		});
		
		lbSelQtns.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				btnDeselect.setEnabled(true);
			}
		});
		
		setupSelectionButtons(horzPanel);
		
		horzPanel.add(lbSelQtns);
		horzPanel.setSpacing(5);
		mainPanel.add(horzPanel);

		setupOkCancelButtons();
		
		mainPanel.setSpacing(5);
		
		setText("Other Questions");
	}
	
	
	public void setData(FormDef formDef,QuestionDef questionDef){
		lbAllQtns.clear();
		lbSelQtns.clear();
		
		for(int index = 0; index < formDef.getPageCount(); index++)
			loadPageQnts(formDef.getPageAt(index),questionDef);
	}
	
	
	private void loadPageQnts(PageDef pageDef,QuestionDef questionDef){
		for(int index = 0; index < pageDef.getQuestionCount(); index++){
			QuestionDef qtnDef = pageDef.getQuestionAt(index);
			if(qtnDef == questionDef)
				continue;
			
			lbAllQtns.addItem(qtnDef.getText(), qtnDef.getVariableName());
		}
	}
	
	private void setupHeaderLabels(){
		HorizontalPanel horzPanel = new HorizontalPanel();
		Label lblAllQtns = new Label("All Questions");
		Label lblSelQtns = new Label("Selected Questions");
		
		horzPanel.add(lblAllQtns);
		horzPanel.add(lblSelQtns);
		
		horzPanel.setCellHorizontalAlignment(lblAllQtns, HasAlignment.ALIGN_CENTER);
		horzPanel.setCellHorizontalAlignment(lblSelQtns, HasAlignment.ALIGN_CENTER);
		FormUtil.maximizeWidget(horzPanel);
		
		mainPanel.add(horzPanel);
	}
	
	private void setupSelectionButtons(HorizontalPanel parentPanel){
		VerticalPanel vertPanel = new VerticalPanel();
		vertPanel.add(btnSelect);
		vertPanel.add(btnDeselect);
		vertPanel.setCellVerticalAlignment(btnSelect, HasAlignment.ALIGN_MIDDLE);
		vertPanel.setCellVerticalAlignment(btnDeselect, HasAlignment.ALIGN_MIDDLE);
		vertPanel.setHeight("200px");
		
		parentPanel.add(vertPanel);
		
		btnSelect.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				selectQuestions();
			}
		});
		
		btnDeselect.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				deselectQuestions();
			}
		});
		
		btnSelect.setEnabled(false);
		btnDeselect.setEnabled(false);
	}
	
	private void setupOkCancelButtons(){
		btnOk.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				hide();
				qtnSelListener.onQuestionsSelected(getSelectedQtns());
			}
		});
		
		btnCancel.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
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
	
	private void selectQuestions(){
		for(int index = 0; index < lbAllQtns.getItemCount(); index++){
			if(lbAllQtns.isItemSelected(index)){
				lbSelQtns.addItem(lbAllQtns.getItemText(index), lbAllQtns.getValue(index));
				lbAllQtns.removeItem(index);
				index--;
			}
		}
		
		btnSelect.setEnabled(false);
		btnOk.setEnabled(lbSelQtns.getItemCount() > 0);
	}
	
	
	private void deselectQuestions(){
		for(int index = 0; index < lbSelQtns.getItemCount(); index++){
			if(lbSelQtns.isItemSelected(index)){
				lbAllQtns.addItem(lbSelQtns.getItemText(index), lbSelQtns.getValue(index));
				lbSelQtns.removeItem(index);
				index--;
			}
		}
		
		btnDeselect.setEnabled(false);
		btnOk.setEnabled(lbSelQtns.getItemCount() > 0);
	}
	
	
	private List<String> getSelectedQtns(){
		selectedQtns.clear();
		
		for(int index = 0; index < lbSelQtns.getItemCount(); index++)
			selectedQtns.add(lbSelQtns.getValue(index));
		
		return selectedQtns;
	}
}
