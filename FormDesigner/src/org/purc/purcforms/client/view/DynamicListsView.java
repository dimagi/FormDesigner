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
import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.widget.skiprule.FieldWidget;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.xml.client.Node;


/**
 * This widget enables creation of dynamic selection lists.
 * 
 * @author daniel
 *
 */
public class DynamicListsView extends Composite implements ItemSelectionListener, ClickHandler{

	/** The main or root widget. */
	private VerticalPanel verticalPanel = new VerticalPanel();

	/** Widget to display the "Values for" text. */
	private Label lblValuesFor = new Label(LocaleText.get("valuesFor"));

	/** Widget to display the is equal to text. */
	private Label lblEqual = new Label(" "+LocaleText.get("isEqualTo"));

	/** The widget for selection of the parent questions.
	 * The parent question is the one on which the single select dynamic question depends.
	 */
	private FieldWidget fieldWidget;

	/** Widget to display the list of parent question options to select from. */
	private ListBox lbOption = new ListBox(false);

	/** Table to hold the list of dynamic options. */
	private FlexTable table = new FlexTable();

	/** Button to add a new dynamic selection list option. */
	private Button btnAdd = new Button(LocaleText.get("addNew"));

	/** The form definition object that this dynamic list belongs to. */
	private FormDef formDef;

	/** The question that we are building this dynamic selection list for.
	 * For the Continent and Country questions, this would be the Country question.
	 */
	private QuestionDef questionDef;

	/** Flag determining whether to enable this widget or not. */
	private boolean enabled;

	/** The dynamic option definition object that we are building. */
	private DynamicOptionDef dynamicOptionDef;

	/** Contains the list of child options for the parent question's selected option. */
	private List<OptionDef> optionList;

	/** The parent question whose selected option determines the list of child options.
	 *  For the Continent and Country questions, this would be the Continent question.
	 */
	private QuestionDef parentQuestionDef;


	/**
	 * Creates a new instance of the dynamic lists widget.
	 */
	public DynamicListsView(){
		setupWidgets();
	}

	
	/**
	 * Sets up widgets.
	 */
	private void setupWidgets(){

		fieldWidget = new FieldWidget(this);
		fieldWidget.setForDynamicOptions(true);

		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.add(lblValuesFor);
		horizontalPanel.add(fieldWidget);
		horizontalPanel.add(lblEqual);
		horizontalPanel.add(lbOption);
		horizontalPanel.setSpacing(5);

		verticalPanel.add(horizontalPanel);
		verticalPanel.add(table);

		lbOption.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				updateOptionList();
			}
		});

		btnAdd.addClickHandler(this);

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


	/**
	 * Sets the dynamic selection list question, whose list of options we set on this widget.
	 * 
	 * @param questionDef the question.
	 */
	public void setQuestionDef(QuestionDef questionDef){
		if(questionDef == null || questionDef.getDataType() != QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC){
			setEnabled(false);
			return;
		}

		setEnabled(true);
		clear();

		parentQuestionDef = null;
		optionList = null;
		dynamicOptionDef = null;

		if(questionDef.getParent() instanceof PageDef)
			formDef = ((PageDef)questionDef.getParent()).getParent();
		else
			formDef = ((PageDef)((QuestionDef)questionDef.getParent()).getParent()).getParent();

		if(questionDef != null)
			lblValuesFor.setText(LocaleText.get("valuesFor") + questionDef.getDisplayText() + "  "+LocaleText.get("whenAnswerFor"));
		else
			lblValuesFor.setText(LocaleText.get("valuesFor"));

		this.questionDef = questionDef;
		fieldWidget.setDynamicQuestionDef(questionDef);
		fieldWidget.setFormDef(formDef);

		QuestionDef parentQuestionDef = formDef.getDynamicOptionsParent(questionDef.getId());
		if(parentQuestionDef != null)
			fieldWidget.selectQuestion(parentQuestionDef);
	}
	

	/**
	 * Sets the form definition object that this dynamic selection list belongs to.
	 * 
	 * @param formDef the form definition object.
	 */
	public void setFormDef(FormDef formDef){
		updateDynamicLists();
		this.formDef = formDef;
		questionDef = null;
		parentQuestionDef = null;
		optionList = null;
		dynamicOptionDef = null;
		clear();
	}

	
	/**
	 * Removes all dynamic selection list values for any previous widget, if any.
	 */
	private void clear(){
		if(questionDef != null)
			updateDynamicLists();

		questionDef = null;
		lblValuesFor.setText(LocaleText.get("valuesFor"));
		lbOption.clear();

		while(verticalPanel.getWidgetCount() > 4)
			verticalPanel.remove(verticalPanel.getWidget(3));

		clearChildOptions();

		fieldWidget.setQuestion(null);
	}


	/**
	 * Removes all options from the options list table.
	 */
	private void clearChildOptions(){
		//Removes all options apart from the header which is at index 0.
		while(table.getRowCount() > 1)
			table.removeRow(1);
	}

	/**
	 * Sets whether to enable this widget or not.
	 * 
	 * @param enabled set to true to enable, else false.
	 */
	public void setEnabled(boolean enabled){
		this.enabled = enabled;

		lbOption.setEnabled(enabled);

		if(!enabled)
			clear();
	}
	

	/**
	 * Checks whether this widget is enabled or not.
	 * 
	 * @return true of enabled, else false.
	 */
	public boolean isEnabled(){
		return enabled;
	}


	/**
	 * @see org.purc.purcforms.client.controller.ItemSelectionListener#onItemSelected(Object, Object)
	 */
	public void onItemSelected(Object sender, Object item) {
		//This is only useful for us when a new parent question has been selected.
		if(sender != fieldWidget)
			return;

		//Clear all parent and child options.
		lbOption.clear();
		clearChildOptions();

		parentQuestionDef = (QuestionDef)item;

		//we only allow option lists for single select and single select dynamic types.
		int type = parentQuestionDef.getDataType();
		if(!(type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC))
			return;

		//Get the dynamic option definition object for which the selected
		//question acts as the parent of the relationship.
		dynamicOptionDef = formDef.getDynamicOptions(parentQuestionDef.getId());

		//As for now, we do not allow the a parent question to map to more
		//than once child question.
		if(dynamicOptionDef != null && dynamicOptionDef.getQuestionId() != questionDef.getId())
			return;

		//Populate the list of parent options from a single select question.
		if(type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE){
			if(!(parentQuestionDef.getOptionCount() > 0)){
				
				//we are creating new DynamicOptionDef() because we want to allow
				//one specify type to be single select dynamic without specifying any
				//options for cases where they will be got from the server using the
				//external source widget filter property.
				dynamicOptionDef = new DynamicOptionDef();
				dynamicOptionDef.setQuestionId(questionDef.getId());
				return;
			}

			List options = parentQuestionDef.getOptions();
			for(int i=0; i<options.size(); i++){
				OptionDef optionDef = (OptionDef)options.get(i);
				lbOption.addItem(optionDef.getText(),String.valueOf(optionDef.getId()));	
			}
		}

		//Populate the list of parent options from a dynamic selection list question.
		if(type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC){
			
			if(dynamicOptionDef == null){
				//we are creating new DynamicOptionDef() because we want to allow
				//one specify type to be single select dynamic without specifying any
				//options for cases where they will be got from the server using the
				//external source widget filter property.
				dynamicOptionDef = new DynamicOptionDef();
				dynamicOptionDef.setQuestionId(questionDef.getId());
			}
			
			DynamicOptionDef options = formDef.getChildDynamicOptions(parentQuestionDef.getId());
			if(options != null && options.getParentToChildOptions() != null){
				Iterator<Entry<Integer,List<OptionDef>>> iterator = options.getParentToChildOptions().entrySet().iterator();
				while(iterator.hasNext()){
					Entry<Integer,List<OptionDef>> entry = iterator.next();
					List<OptionDef> list = entry.getValue();
					for(int index = 0; index < list.size(); index++){
						OptionDef optionDef = list.get(index);
						lbOption.addItem(optionDef.getText(),String.valueOf(optionDef.getId()));
					}
				} 
			}
		}

		//If there is any selection, update the table of options.
		if(lbOption.getSelectedIndex() >= 0)
			updateOptionList();
	}

	/**
	 * @see org.purc.purcforms.client.controller.ItemSelectionListener#onStartItemSelection(Object)
	 */
	public void onStartItemSelection(Object sender){

	}


	/**
	 * Updates the form definition object with the dynamic option definition object
	 * that is being edited on this widget.
	 */
	public void updateDynamicLists(){
		//dynamicOptionDef.size() == 0 is commented out because we want to allow
		//one specify type to be single select dynamic without specifying any
		//options for cases where they will be got from the server using the
		//external source widget filter property.
		
		if(dynamicOptionDef == null /*|| dynamicOptionDef.size() == 0*/){
			if(parentQuestionDef != null)
				formDef.removeDynamicOptions(parentQuestionDef.getId());
			return;
		}

		formDef.setDynamicOptionDef(parentQuestionDef.getId(), dynamicOptionDef);
	}


	/**
	 * Populates the table of dynamic options with those options that are allowed
	 * for the currently selected option for the parent question.
	 */
	public void updateOptionList(){
		clearChildOptions();
		if(dynamicOptionDef == null){
			dynamicOptionDef = new DynamicOptionDef();
			dynamicOptionDef.setQuestionId(questionDef.getId());
		}

		int optionId = Integer.parseInt(lbOption.getValue(lbOption.getSelectedIndex()));
		optionList = dynamicOptionDef.getOptionList(optionId);
		if(optionList == null){
			optionList = new ArrayList<OptionDef>();
			dynamicOptionDef.setOptionList(optionId, optionList);
		}

		for(int index = 0; index < optionList.size(); index++){
			OptionDef optionDef = optionList.get(index);
			addOption(optionDef.getText(),optionDef.getVariableName(),table.getRowCount());
		}

		addAddButton();
	}


	/**
	 * Called when any of the add new, delete, move up or move down 
	 * button has been clicked.
	 * 
	 * @sender the button which was clicked.
	 */
	public void onClick(ClickEvent event){	
		Object sender = event.getSource();
		if(sender == btnAdd)
			addNewOption().setFocus(true);
		else{
			int rowCount = table.getRowCount();
			for(int row = 1; row < rowCount; row++){
				//Delete button
				if(sender == table.getWidget(row, 2)){
					OptionDef optionDef = optionList.get(row-1);
					if(!Window.confirm(LocaleText.get("removeRowPrompt") + " [" + optionDef.getText() + " - " + optionDef.getVariableName() + "]"))
						return;

					table.removeRow(row);
					optionList.remove(row-1);

					if(optionDef.getControlNode() != null && optionDef.getControlNode().getParentNode() != null)
						optionDef.getControlNode().getParentNode().removeChild(optionDef.getControlNode());
					break;
				}
				else if(sender == table.getWidget(row, 3)){
					//Move up button.
					if(row == 1)
						return;
					moveOptionUp(optionList.get(row-1));

					OptionDef optionDef = optionList.get(row-1);
					addOption(optionDef.getText(),optionDef.getVariableName(),row);

					optionDef = optionList.get(row-2);
					addOption(optionDef.getText(),optionDef.getVariableName(),row-1);
					break;
				}
				else if(sender == table.getWidget(row, 4)){
					//Move down button.
					if(row == (rowCount - 2))
						return;
					moveOptionDown(optionList.get(row-1));

					OptionDef optionDef = optionList.get(row-1);
					addOption(optionDef.getText(),optionDef.getVariableName(),row);

					optionDef = optionList.get(row);
					addOption(optionDef.getText(),optionDef.getVariableName(),row+1);
					break;
				}
			}
		}
	}

	
	/**
	 * Adds a new dynamic list option to the table.
	 */
	private TextBox addNewOption(){
		table.removeRow(table.getRowCount() - 1);
		TextBox textBox = addOption("","",table.getRowCount());
		textBox.setFocus(true);
		textBox.selectAll();
		addAddButton();
		addNewOptionDef();
		return textBox;
	}


	/**
	 * Adds a new option to the table of dynamic options list.
	 * 
	 * @param text the option text.
	 * @param binding the option binding.
	 * @param row the index of the row to add.
	 * @return the widget for editing text of the new option.
	 */
	private TextBox addOption(String text, String binding, int row){
		TextBox txtText = new TextBox();
		TextBox txtBinding = new TextBox();

		txtText.setText(text);
		txtBinding.setText(binding);

		table.setWidget(row, 0,txtText);
		table.setWidget(row, 1,txtBinding);

		PushButton button = new PushButton(FormUtil.createImage(FormDesignerWidget.images.delete()));
		button.setTitle(LocaleText.get("deleteItem"));
		button.addClickHandler(this);
		table.setWidget(row, 2,button);

		button = new PushButton(FormUtil.createImage(FormDesignerWidget.images.moveup()));
		button.setTitle(LocaleText.get("moveUp"));
		button.addClickHandler(this);
		table.setWidget(row, 3,button);

		button = new PushButton(FormUtil.createImage(FormDesignerWidget.images.movedown()));
		button.setTitle(LocaleText.get("moveDown"));
		button.addClickHandler(this);
		table.setWidget(row, 4,button);

		table.getFlexCellFormatter().setWidth(row, 0, "45%");
		table.getFlexCellFormatter().setWidth(row, 1, "45%");
		table.getFlexCellFormatter().setWidth(row, 2, "3.3%");
		table.getFlexCellFormatter().setWidth(row, 3, "3.3%");
		table.getFlexCellFormatter().setWidth(row, 4, "3.3%");
		table.getWidget(row, 0).setWidth("100%");
		table.getWidget(row, 1).setWidth("100%");

		txtText.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				updateText((TextBox)event.getSource());
			}
		});

		txtText.addKeyDownHandler(new KeyDownHandler(){
			public void onKeyDown(KeyDownEvent event) {
				int keyCode = event.getNativeKeyCode();
				if(keyCode == KeyCodes.KEY_ENTER || keyCode == KeyCodes.KEY_DOWN)
					moveToNextWidget((Widget)event.getSource(),0,keyCode == KeyCodes.KEY_DOWN);
				else if(keyCode == KeyCodes.KEY_UP)
					moveToPrevWidget((Widget)event.getSource(),0);
			}
		});
		
		txtText.addKeyUpHandler(new KeyUpHandler(){
			public void onKeyUp(KeyUpEvent event) {
				int keyCode = event.getNativeKeyCode();
				if(!(keyCode == KeyCodes.KEY_ENTER || keyCode == KeyCodes.KEY_DOWN ||
						keyCode == KeyCodes.KEY_DOWN || keyCode == KeyCodes.KEY_UP))
					updateText((TextBox)event.getSource());
			}
		});

		txtBinding.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				updateBinding((TextBox)event.getSource());
			}
		});

		txtBinding.addKeyDownHandler(new KeyDownHandler(){
			public void onKeyDown(KeyDownEvent event) {
				int keyCode = event.getNativeKeyCode();
				if(keyCode == KeyCodes.KEY_ENTER || keyCode == KeyCodes.KEY_DOWN)
					moveToNextWidget((Widget)event.getSource(),1,keyCode == KeyCodes.KEY_DOWN);
				else if(keyCode == KeyCodes.KEY_UP)
					moveToPrevWidget((Widget)event.getSource(),1);
			}
		});

		return txtText;
	}


	/**
	 * Updates the selected object with the new text as typed by the user.
	 */
	private void updateText(TextBox txtText){
		int rowCount = table.getRowCount();
		for(int row = 1; row < rowCount; row++){
			if(txtText == table.getWidget(row, 0)){
				OptionDef optionDef = null;
				if(optionList.size() > row-1)
					optionDef = optionList.get(row-1);

				if(optionDef == null)
					optionDef = addNewOptionDef();

				String orgTextDefBinding = FormDesignerUtil.getXmlTagName(optionDef.getText());
				
				optionDef.setText(txtText.getText());

				//automatically set the binding, if empty.
				TextBox txtBinding = (TextBox)table.getWidget(row, 1);
				String binding = txtBinding.getText();
				//if(binding == null || binding.trim().length() == 0){
				if(binding == null || binding.trim().length() == 0 || binding.equals(orgTextDefBinding)){
					txtBinding.setText(FormDesignerUtil.getXmlTagName(optionDef.getText()));
					optionDef.setVariableName(txtBinding.getText());
				}

				break;
			}
		}
	}


	/**
	 * Adds a new option definition object.
	 * 
	 * @return the new option definition object.
	 */
	private OptionDef addNewOptionDef(){
		OptionDef optionDef = new OptionDef(parentQuestionDef);
		optionDef.setId(dynamicOptionDef.getNextOptionId());
		dynamicOptionDef.setNextOptionId(optionDef.getId() + 1);
		optionList.add(optionDef);
		return optionDef;
	}


	/**
	 * Updates the selected object with the new binding as typed by the user.
	 */
	private void updateBinding(TextBox txtBinding){
		int rowCount = table.getRowCount();
		for(int row = 1; row < rowCount; row++){
			if(txtBinding == table.getWidget(row, 1)){
				OptionDef optionDef = null;
				if(optionList.size() > row-1)
					optionDef = optionList.get(row-1);

				if(optionDef == null)
					optionDef = addNewOptionDef();

				optionDef.setVariableName(txtBinding.getText());
				break;
			}
		}
	}

	/**
	 * Adds the add new button to the table widget.
	 */
	private void addAddButton(){
		FlexCellFormatter cellFormatter = table.getFlexCellFormatter();
		int row = table.getRowCount();
		cellFormatter.setColSpan(row, 0, 5);
		cellFormatter.setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER);
		table.setWidget(row, 0, btnAdd);
	}


	/**
	 * Moves input focus to the next widget.
	 * 
	 * @param sender the widget after which to move the input focus.
	 * @param col the index of the column which currently has input focus.
	 * @param sameCol set to true to move to the next widget in the same column.
	 */
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
						col = 1; //0;
					}
					else{
						if(textBox.getText() == null || textBox.getText().trim().length() == 0)
							return;
						else if(row == (rowCount - 2)){
							addNewOption();
							return;
						}
						else
							row++;
						
						col = 0; //1;
					}

					textBox = ((TextBox)table.getWidget(row, col));
					textBox.setFocus(true);
					textBox.selectAll();
					break;
				}
			}
		}
	}

	/**
	 * Moves input focus to the widget before.
	 * 
	 * @param sender the widget before which to move the input focus.
	 * @param col the index of the column which currently has input focus.
	 */
	private void moveToPrevWidget(Widget sender, int col){
		int rowCount = table.getRowCount();

		//Starting from index 1 since 0 is the header row.
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


	/**
	 * Moves an option one position upwards.
	 * 
	 * @param optionDef the option to move.
	 */
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
		List<OptionDef> list = new ArrayList<OptionDef>();

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


	/**
	 * Moves an option one position downwards.
	 * 
	 * @param optionDef the option to move.
	 */
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
		List<OptionDef> list = new ArrayList<OptionDef>();

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


	/**
	 * Gets the next option which has been converted to xforms and 
	 * hence attached to an xforms document node, starting at a given 
	 * index in a list of options.
	 * 
	 * @param options the list of options.
	 * @param index the index to start from in the option list.
	 * @return the option.
	 */
	private OptionDef getNextSavedOption(List options, int index){
		for(int i=index; i<options.size(); i++){
			OptionDef optionDef = (OptionDef)options.get(i);
			if(optionDef.getControlNode() != null)
				return optionDef;
		}
		return (OptionDef)options.get(index);
	}
}
