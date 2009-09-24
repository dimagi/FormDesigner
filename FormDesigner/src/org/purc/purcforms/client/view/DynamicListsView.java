package org.purc.purcforms.client.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.purc.purcforms.client.FormDesignerWidget;
import org.purc.purcforms.client.controller.ItemSelectionListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.DynamicOptionDef;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.OptionDef;
import org.purc.purcforms.client.model.PageDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.widget.skiprule.FieldWidget;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.xml.client.Node;


/**
 * 
 * @author daniel
 *
 */
public class DynamicListsView extends Composite implements ItemSelectionListener, ClickListener{

	private VerticalPanel verticalPanel = new VerticalPanel();
	private Label lblAction = new Label(LocaleText.get("valuesFor"));
	private Label lblEqual = new Label(" "+LocaleText.get("isEqualTo"));
	private FieldWidget fieldWidget;
	private ListBox lbValue = new ListBox(false);
	private FlexTable table = new FlexTable();
	private Button btnAdd = new Button(LocaleText.get("addNew"));
	private FormDef formDef;
	private QuestionDef questionDef;
	private boolean enabled;

	private DynamicOptionDef dynamicOptionDef;
	private List<OptionDef> optionList;
	private QuestionDef parentQuestionDef;


	public DynamicListsView(){
		setupWidgets();
	}

	private void setupWidgets(){

		fieldWidget = new FieldWidget(this);
		fieldWidget.setForDynamicOptions(true);

		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.add(lblAction);
		horizontalPanel.add(fieldWidget);
		horizontalPanel.add(lblEqual);
		horizontalPanel.add(lbValue);
		horizontalPanel.setSpacing(5);

		verticalPanel.add(horizontalPanel);
		verticalPanel.add(table);

		lbValue.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				updateValueList();
			}
		});

		btnAdd.addClickListener(this);
		
		table.setStyleName("cw-FlexTable");
		table.setWidget(0, 0,new Label(LocaleText.get("text")));
		table.setWidget(0, 1,new Label(LocaleText.get("binding")));
		table.setWidget(0, 2,new Label(LocaleText.get("action")));
		table.getFlexCellFormatter().setColSpan(0, 2, 3);
		
		table.setWidth("100%");
		table.setHeight("100%");
		
		table.getCellFormatter().setStyleName(0, 0, "getting-started-label");
		table.getCellFormatter().setStyleName(0, 1, "getting-started-label");
		table.getCellFormatter().setStyleName(0, 2, "getting-started-label");
		
		initWidget(verticalPanel);
	}

	public void setQuestionDef(QuestionDef questionDef){
		if(questionDef == null || questionDef.getDataType() != QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC){
			setEnabled(false);
			return;
		}

		setEnabled(true);
		clearConditions();

		parentQuestionDef = null;
		optionList = null;
		dynamicOptionDef = null;

		if(questionDef.getParent() instanceof PageDef)
			formDef = ((PageDef)questionDef.getParent()).getParent();
		else
			formDef = ((PageDef)((QuestionDef)questionDef.getParent()).getParent()).getParent();

		if(questionDef != null)
			lblAction.setText(LocaleText.get("valuesFor") + questionDef.getDisplayText() + "  "+LocaleText.get("whenAnswerFor"));
		else
			lblAction.setText(LocaleText.get("valuesFor"));

		this.questionDef = questionDef;
		fieldWidget.setDynamicQuestionDef(questionDef);
		fieldWidget.setFormDef(formDef);

		QuestionDef parentQuestionDef = formDef.getDynamicOptionsParent(questionDef.getId());
		if(parentQuestionDef != null)
			fieldWidget.selectQuestion(parentQuestionDef);
	}

	public void setFormDef(FormDef formDef){
		updateDynamicLists();
		this.formDef = formDef;
		questionDef = null;
		parentQuestionDef = null;
		optionList = null;
		dynamicOptionDef = null;
		clearConditions();
	}

	private void clearConditions(){
		if(questionDef != null)
			updateDynamicLists();

		questionDef = null;
		lblAction.setText(LocaleText.get("valuesFor"));
		lbValue.clear();

		while(verticalPanel.getWidgetCount() > 4)
			verticalPanel.remove(verticalPanel.getWidget(3));

		clearValues();

		fieldWidget.setQuestion(null);
	}

	private void clearValues(){
		while(table.getRowCount() > 1)
			table.removeRow(1);
	}

	public void setEnabled(boolean enabled){
		this.enabled = enabled;

		lbValue.setEnabled(enabled);

		if(!enabled)
			clearConditions();
	}

	public boolean isEnabled(){
		return enabled;
	}

	public void onItemSelected(Object sender, Object item) {
		if(sender == fieldWidget){
			lbValue.clear();
			clearValues();

			parentQuestionDef = (QuestionDef)item;

			int type = parentQuestionDef.getDataType();
			if(!(type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC))
				return;

			dynamicOptionDef = formDef.getDynamicOptions(parentQuestionDef.getId());
			
			//As for now, we do not allow the a parent question to map to more
			//than once child question.
			if(dynamicOptionDef != null && dynamicOptionDef.getQuestionId() != questionDef.getId())
				return;

			if(type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE){
				if(!(parentQuestionDef.getOptionCount() > 0))
					return;

				List options = parentQuestionDef.getOptions();
				for(int i=0; i<options.size(); i++){
					OptionDef optionDef = (OptionDef)options.get(i);
					lbValue.addItem(optionDef.getText(),String.valueOf(optionDef.getId()));	
				}
			}

			//dynamicOptionDef = formDef.getDynamicOptions(parentQuestionDef.getId());

			if(type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC){
				DynamicOptionDef options = formDef.getChildDynamicOptions(parentQuestionDef.getId());
				if(options != null){
					Iterator<Entry<Integer,List<OptionDef>>> iterator = options.getParentToChildOptions().entrySet().iterator();
					while(iterator.hasNext()){
						Entry<Integer,List<OptionDef>> entry = iterator.next();
						List<OptionDef> list = entry.getValue();
						for(int index = 0; index < list.size(); index++){
							OptionDef optionDef = list.get(index);
							lbValue.addItem(optionDef.getText(),String.valueOf(optionDef.getId()));
						}
					} //This is commented out because of reversing the order
					/*HashMap<Integer, List<OptionDef>> parentToChildOptions = options.getParentToChildOptions();
					int count = parentToChildOptions.size();
					Object[] keys = parentToChildOptions.keySet().toArray();
					for(int index = count -1; index >= 0; index--){
						List<OptionDef> list = parentToChildOptions.get(keys[index]);
						for(int i = 0; i < list.size(); i++){
							OptionDef optionDef = list.get(i);
							lbValue.addItem(optionDef.getText(),String.valueOf(optionDef.getId()));
						}
					}*/
					
				}
			}

			if(lbValue.getSelectedIndex() >= 0)
				updateValueList();
		}
	}

	public void onStartItemSelection(Object sender){
	}

	public void updateDynamicLists(){
		if(dynamicOptionDef == null || dynamicOptionDef.size() == 0){
			if(parentQuestionDef != null)
				formDef.removeDynamicOptions(parentQuestionDef.getId());
			return;
		}

		formDef.setDynamicOptionDef(parentQuestionDef.getId(), dynamicOptionDef);
	}

	public void updateValueList(){
		clearValues();
		if(dynamicOptionDef == null){
			dynamicOptionDef = new DynamicOptionDef();
			dynamicOptionDef.setQuestionId(questionDef.getId());
		}

		int optionId = Integer.parseInt(lbValue.getValue(lbValue.getSelectedIndex()));
		optionList = dynamicOptionDef.getOptionList(optionId);
		if(optionList == null){
			optionList = new ArrayList<OptionDef>();
			dynamicOptionDef.setOptionList(optionId, optionList);
		}

		for(int index = 0; index < optionList.size(); index++){
			OptionDef optionDef = optionList.get(index);
			addValue(optionDef.getText(),optionDef.getVariableName(),table.getRowCount());
		}

		addAddButton();
	}

	public void onClick(Widget sender){		
		if(sender == btnAdd)
			addNewOption();
		else{
			int rowCount = table.getRowCount();
			for(int row = 1; row < rowCount; row++){
				if(sender == table.getWidget(row, 2)){
					OptionDef optionDef = optionList.get(row-1);
					if(!Window.confirm(LocaleText.get("removeRowPrompt") + " [" + optionDef.getText() + " - " + optionDef.getVariableName() + "]"))
						return;
					
					table.removeRow(row);
					optionList.remove(row-1);
	
					//if(optionList.size() == 0)
					//		firstOptionNode = null;

					if(optionDef.getControlNode() != null && optionDef.getControlNode().getParentNode() != null)
						optionDef.getControlNode().getParentNode().removeChild(optionDef.getControlNode());
					break;
				}
				else if(sender == table.getWidget(row, 3)){
					if(row == 1)
						return;
					moveOptionUp(optionList.get(row-1));
					
					OptionDef optionDef = optionList.get(row-1);
					addValue(optionDef.getText(),optionDef.getVariableName(),row);
					
					optionDef = optionList.get(row-2);
					addValue(optionDef.getText(),optionDef.getVariableName(),row-1);
					break;
				}
				else if(sender == table.getWidget(row, 4)){
					if(row == (rowCount - 2))
						return;
					moveOptionDown(optionList.get(row-1));
					
					OptionDef optionDef = optionList.get(row-1);
					addValue(optionDef.getText(),optionDef.getVariableName(),row);
					
					optionDef = optionList.get(row);
					addValue(optionDef.getText(),optionDef.getVariableName(),row+1);
					break;
				}
			}
		}
	}

	private void addNewOption(){
		table.removeRow(table.getRowCount() - 1);
		TextBox textBox = addValue("","",table.getRowCount());
		textBox.setFocus(true);
		textBox.selectAll();
		addAddButton();
		addNewOptionDef();
	}

	private TextBox addValue(String text, String value, int row){
		TextBox txtName = new TextBox();
		TextBox txtValue = new TextBox();

		txtName.setText(text);
		txtValue.setText(value);

		table.setWidget(row, 0,txtName);
		table.setWidget(row, 1,txtValue);

		PushButton button = new PushButton(FormDesignerWidget.images.delete().createImage());
		button.addClickListener(this);
		table.setWidget(row, 2,button);

		button = new PushButton(FormDesignerWidget.images.moveup().createImage());
		button.addClickListener(this);
		table.setWidget(row, 3,button);

		button = new PushButton(FormDesignerWidget.images.movedown().createImage());
		button.addClickListener(this);
		table.setWidget(row, 4,button);

		table.getFlexCellFormatter().setWidth(row, 0, "45%");
		table.getFlexCellFormatter().setWidth(row, 1, "45%");
		table.getFlexCellFormatter().setWidth(row, 2, "3.3%");
		table.getFlexCellFormatter().setWidth(row, 3, "3.3%");
		table.getFlexCellFormatter().setWidth(row, 4, "3.3%");
		table.getWidget(row, 0).setWidth("100%");
		table.getWidget(row, 1).setWidth("100%");

		txtName.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				updateName((TextBox)sender);
			}
		});

		txtName.addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyDown(Widget sender, char keyCode, int modifiers) {
				if(keyCode == KeyboardListener.KEY_ENTER || keyCode == KeyboardListener.KEY_DOWN)
					moveToNextWidget(sender,0,keyCode == KeyboardListener.KEY_DOWN);
				else if(keyCode == KeyboardListener.KEY_UP)
					moveToPrevWidget(sender,0);
			}
		});


		txtValue.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				updateValue((TextBox)sender);
			}
		});

		txtValue.addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyDown(Widget sender, char keyCode, int modifiers) {
				if(keyCode == KeyboardListener.KEY_ENTER || keyCode == KeyboardListener.KEY_DOWN)
					moveToNextWidget(sender,1,keyCode == KeyboardListener.KEY_DOWN);
				else if(keyCode == KeyboardListener.KEY_UP)
					moveToPrevWidget(sender,1);
			}
		});

		return txtName;
	}

	private void updateName(TextBox txtName){
		int rowCount = table.getRowCount();
		for(int row = 1; row < rowCount; row++){
			if(txtName == table.getWidget(row, 0)){
				OptionDef optionDef = null;
				if(optionList.size() > row-1)
					optionDef = optionList.get(row-1);

				if(optionDef == null)
					optionDef = addNewOptionDef();
				
				optionDef.setText(txtName.getText());
				break;
			}
		}
	}
	
	private OptionDef addNewOptionDef(){
		OptionDef optionDef = new OptionDef(parentQuestionDef);
		optionDef.setId(dynamicOptionDef.getNextOptionId());
		dynamicOptionDef.setNextOptionId(optionDef.getId() + 1);
		optionList.add(optionDef);
		return optionDef;
	}

	private void updateValue(TextBox txtValue){
		int rowCount = table.getRowCount();
		for(int row = 1; row < rowCount; row++){
			if(txtValue == table.getWidget(row, 1)){
				OptionDef optionDef = null;
				if(optionList.size() > row-1)
					optionDef = optionList.get(row-1);

				if(optionDef == null)
					optionDef = addNewOptionDef();
				
				optionDef.setVariableName(txtValue.getText());
				break;
			}
		}
	}

	private void addAddButton(){
		FlexCellFormatter cellFormatter = table.getFlexCellFormatter();
		int row = table.getRowCount();
		cellFormatter.setColSpan(row, 0, 5);
		cellFormatter.setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER);
		table.setWidget(row, 0, btnAdd);
	}

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
								addNewOption();
							return;
						}
						row++;
						col = 0;
					}
					else{
						if(textBox.getText() == null || textBox.getText().trim().length() == 0)
							return;
						col = 1;
					}
					
					textBox = ((TextBox)table.getWidget(row, col));
					textBox.setFocus(true);
					textBox.selectAll();
					break;
				}
			}
		}
	}

	private void moveToPrevWidget(Widget sender, int col){
		int rowCount = table.getRowCount();
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
	
	public void moveOptionUp(OptionDef optionDef){
		List optns = optionList;
		int index = optns.indexOf(optionDef);

		optns.remove(optionDef);

		Node parentNode = null;
		if(optionDef.getControlNode() != null){
			parentNode = optionDef.getControlNode().getParentNode();
			parentNode.removeChild(optionDef.getControlNode());
		}

		OptionDef currentOptionDef;
		List list = new ArrayList();

		//Remove all from index before selected all the way downwards
		while(optns.size() >= index){
			currentOptionDef = (OptionDef)optns.get(index-1);
			list.add(currentOptionDef);
			optns.remove(currentOptionDef);
		}

		optns.add(optionDef);
		for(int i=0; i<list.size(); i++){
			if(i == 0){
				OptionDef optnDef = (OptionDef)list.get(i);
				if(parentNode != null && optnDef.getControlNode() != null && optionDef.getControlNode() != null)
					parentNode.insertBefore(optionDef.getControlNode(), optnDef.getControlNode());
			}
			optns.add(list.get(i));
		}
	}
	
	public void moveOptionDown(OptionDef optionDef){
		List optns = optionList;
		int index = optns.indexOf(optionDef);	

		optns.remove(optionDef);

		Node parentNode = null;
		if(optionDef.getControlNode() != null){
			parentNode = optionDef.getControlNode().getParentNode();
			parentNode.removeChild(optionDef.getControlNode());
		}

		OptionDef currentItem; // = parent.getChild(index - 1);
		List list = new ArrayList();

		//Remove all otions below selected index
		while(optns.size() > 0 && optns.size() > index){
			currentItem = (OptionDef)optns.get(index);
			list.add(currentItem);
			optns.remove(currentItem);
		}

		for(int i=0; i<list.size(); i++){
			if(i == 1){
				optns.add(optionDef); //Add after the first item but before the current (second).

				OptionDef optnDef = getNextSavedOption(list,i); //(OptionDef)list.get(i);
				if(optnDef.getControlNode() != null && optionDef.getControlNode() != null)
					parentNode.insertBefore(optionDef.getControlNode(), optnDef.getControlNode());
				else if(parentNode != null)
					parentNode.appendChild(optionDef.getControlNode());
			}
			optns.add(list.get(i));
		}

		//If was second last and hence becoming last
		if(list.size() == 1){
			optns.add(optionDef);

			if(optionDef.getControlNode() != null)
				parentNode.appendChild(optionDef.getControlNode());
		}
	}
	
	private OptionDef getNextSavedOption(List options, int index){
		for(int i=index; i<options.size(); i++){
			OptionDef optionDef = (OptionDef)options.get(i);
			if(optionDef.getControlNode() != null)
				return optionDef;
		}
		return (OptionDef)options.get(index);
	}
}
