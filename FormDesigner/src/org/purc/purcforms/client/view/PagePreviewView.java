package org.purc.purcforms.client.view;

import java.util.Vector;

import org.purc.purcforms.client.model.OptionDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.util.FormDesignerUtil;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;


/**
 * Previews a list of questions;
 * 
 * @author daniel
 *
 */
public class PagePreviewView extends Composite{

	private FlexTable table = new FlexTable();
	private Vector questions;
	private ScrollPanel scrollPanel = new ScrollPanel();
	
	public PagePreviewView(Vector questions){
		this.questions = questions;
		table.setCellSpacing(5);
		scrollPanel.setWidget(table);
		initWidget(scrollPanel);
		//FormsDesignerUtil.maximizeWidget(table);
		FormDesignerUtil.maximizeWidget(scrollPanel);
		loadQuestions();
	}
	
	private void loadQuestions(){
		
		int labelCol = 0, valueCol = 1, row = 0,half = questions.size()/2;
		if(questions.size() % 2 != 0)
			half ++;
		
		for(int currentQtnNo=0; currentQtnNo<questions.size(); currentQtnNo++){
	    	QuestionDef questionDef = (QuestionDef)questions.elementAt(currentQtnNo);
	    	
	    	Label label = new Label(questionDef.getText());
	    	label.setTitle(questionDef.getHelpText());
	    	
	    	/*if(currentQtnNo > half){
	    		if(row == (currentQtnNo-1)){
		    		row = 0;
		    		labelCol = 2;
		    		valueCol = 3;
	    		}
	    		else
	    			row++;
	    	}
	    	else*/
	    		row = currentQtnNo;
	    		
	    	table.setWidget(row, labelCol, label);
	    	
	    	if(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || 
	    			questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE){
	    		
	    		ListBox listBox = new ListBox(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE);
	    		table.setWidget(row, valueCol, listBox);
	    		
	    		Vector options = questionDef.getOptions();
		    	for(int currentOptionNo=0; currentOptionNo < options.size(); currentOptionNo++){
		    		OptionDef optionDef = (OptionDef)options.elementAt(currentOptionNo);
		    		listBox.addItem(optionDef.getText(), optionDef.getVariableName());
		    	}
	    	}
	    	else
	    		table.setWidget(row, valueCol, new TextBox());
	    	
	    	FlexCellFormatter cellFormatter = table.getFlexCellFormatter();
			cellFormatter.setHorizontalAlignment(row, labelCol, HasHorizontalAlignment.ALIGN_RIGHT);
	    }
	}
}
