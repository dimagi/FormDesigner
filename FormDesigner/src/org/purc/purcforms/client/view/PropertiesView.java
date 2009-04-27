package org.purc.purcforms.client.view;

import org.purc.purcforms.client.controller.IFormActionListener;
import org.purc.purcforms.client.controller.IFormChangeListener;
import org.purc.purcforms.client.controller.IFormSelectionListener;
import org.purc.purcforms.client.controller.ItemSelectionListener;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.OptionDef;
import org.purc.purcforms.client.model.PageDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.model.RepeatQtnsDef;
import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.widget.DescTemplateWidget;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;


/**
 * View responsible for displaying and hence allowing editing of form properties.
 * 
 * @author daniel
 *
 */
public class PropertiesView extends Composite implements IFormSelectionListener,ItemSelectionListener{

	private static final byte DT_NONE = -1;
	private static final byte DT_TEXT = 0;
	private static final byte DT_NUMBER = 1;
	private static final byte DT_DECIMAL = 2;
	private static final byte DT_DATE = 3;
	private static final byte DT_TIME = 4;
	private static final byte DT_DATE_TIME = 5;
	private static final byte DT_BOOLEAN = 6;
	private static final byte DT_SINGLE_SELECT = 7;
	private static final byte DT_MULTIPLE_SELECT = 8;
	private static final byte DT_REPEAT = 9;
	private static final byte DT_IMAGE = 10;
	private static final byte DT_VIDEO = 11;
	private static final byte DT_AUDIO = 12;
	private static final byte DT_SINGLE_SELECT_DYNAMIC = 13;

	private FlexTable table = new FlexTable();
	private ListBox cbDataType = new ListBox(false);
	private CheckBox chkVisible = new CheckBox();
	private CheckBox chkEnabled = new CheckBox();
	private CheckBox chkLocked = new CheckBox();
	private CheckBox chkRequired = new CheckBox();
	private TextBox txtText = new TextBox();
	private TextBox txtHelpText = new TextBox();
	private TextBox txtBinding = new TextBox();
	private TextBox txtDefaultValue = new TextBox();
	//private ListBox cbControlType = new ListBox(false);
	private TextBox txtDescTemplate = new TextBox();
	private DescTemplateWidget btnDescTemplate; // = new Button("Create/Edit");

	private Object propertiesObj;
	private IFormChangeListener formChangeListener;
	private SkipRulesView skipRulesView = new SkipRulesView();
	private ValidationRulesView validationRulesView = new ValidationRulesView();
	private DynamicListsView dynamicListsView = new DynamicListsView();
	private IFormActionListener formActionListener;

	public PropertiesView(){

		btnDescTemplate = new DescTemplateWidget(this);

		//chkVisible.setStyleName("gwt-CheckBox");

		table.setWidget(0, 0, new Label("Text"));
		table.setWidget(1, 0, new Label("Help Text"));
		table.setWidget(2, 0, new Label("Type"));
		table.setWidget(3, 0, new Label("Binding"));
		table.setWidget(4, 0, new Label("Visible"));
		table.setWidget(5, 0, new Label("Enabled"));
		table.setWidget(6, 0, new Label("Locked"));
		table.setWidget(7, 0, new Label("Required"));
		table.setWidget(8, 0, new Label("Default Value"));
		//table.setWidget(9, 0, new Label("Control Type"));
		table.setWidget(9, 0, new Label("Description Template"));

		table.setWidget(0, 1, txtText);
		table.setWidget(1, 1, txtHelpText);
		table.setWidget(2, 1, cbDataType);
		table.setWidget(3, 1, txtBinding);
		table.setWidget(4, 1, chkVisible);
		table.setWidget(5, 1, chkEnabled);
		table.setWidget(6, 1, chkLocked);
		table.setWidget(7, 1, chkRequired);
		table.setWidget(8, 1, txtDefaultValue);
		//table.setWidget(9, 1, cbControlType);

		HorizontalPanel panel = new HorizontalPanel();
		panel.add(txtDescTemplate);
		panel.add(btnDescTemplate);
		panel.setCellWidth(btnDescTemplate, "20%");
		FormDesignerUtil.maximizeWidget(txtDescTemplate);
		FormDesignerUtil.maximizeWidget(panel);
		table.setWidget(9, 1, panel);

		table.setStyleName("cw-FlexTable");

		cbDataType.addItem("Text");
		cbDataType.addItem("Number");
		cbDataType.addItem("Decimal");
		cbDataType.addItem("Date");
		cbDataType.addItem("Time");
		cbDataType.addItem("Date and Time");
		cbDataType.addItem("Boolean");
		cbDataType.addItem("Single Select");
		cbDataType.addItem("Multiple Select");
		cbDataType.addItem("Repeat");
		cbDataType.addItem("Picture");
		cbDataType.addItem("Video");
		cbDataType.addItem("Audio");
		cbDataType.addItem("Single Select Dynamic");

		FlexCellFormatter cellFormatter = table.getFlexCellFormatter();
		cellFormatter.setHorizontalAlignment(15, 1, HasHorizontalAlignment.ALIGN_CENTER);

		table.setWidth("100%");
		cellFormatter.setWidth(0, 0, "20%");
		//cellFormatter.setColSpan(0, 0, 2);

		txtText.setWidth("100%");
		txtHelpText.setWidth("100%");
		txtBinding.setWidth("100%");
		txtDefaultValue.setWidth("100%");
		cbDataType.setWidth("100%");
		//cbControlType.setWidth("100%");

		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.add(table);

		DecoratedTabPanel tabs = new DecoratedTabPanel();
		tabs.add(skipRulesView, "Skip Logic");
		tabs.add(validationRulesView, "Validation Logic");
		tabs.add(dynamicListsView, "Dynamic Lists");

		tabs.selectTab(0);
		verticalPanel.add(tabs);
		FormDesignerUtil.maximizeWidget(tabs);

		FormDesignerUtil.maximizeWidget(verticalPanel);
		initWidget(verticalPanel);

		setupEventListeners();

		cbDataType.setSelectedIndex(-1);

		enableQuestionOnlyProperties(false);
		txtText.setEnabled(false);
		txtDescTemplate.setEnabled(false);
		btnDescTemplate.setEnabled(false);

		txtText.setTitle("The question text.");
		txtHelpText.setTitle("The question description.");
		txtBinding.setTitle("The question internal identifier. For Questions, it should be a valid xml node name.");
		txtDefaultValue.setTitle("The default value or answer");
		cbDataType.setTitle("The type of question or type of expected answers.");
		//cbControlType.setTitle("The question text.");

		DOM.sinkEvents(getElement(), Event.ONKEYDOWN | DOM.getEventsSunk(getElement()));
	}

	private void setupEventListeners(){
		//Check boxes.
		chkVisible.addClickListener(new ClickListener(){
			public void onClick(Widget widget){
				((QuestionDef)propertiesObj).setVisible(chkVisible.isChecked());
				formChangeListener.onFormItemChanged(propertiesObj);
			}
		});

		chkEnabled.addClickListener(new ClickListener(){
			public void onClick(Widget widget){
				((QuestionDef)propertiesObj).setEnabled(chkEnabled.isChecked());
				formChangeListener.onFormItemChanged(propertiesObj);
			}
		});

		chkLocked.addClickListener(new ClickListener(){
			public void onClick(Widget widget){
				((QuestionDef)propertiesObj).setLocked(chkLocked.isChecked());
				formChangeListener.onFormItemChanged(propertiesObj);
			}
		});

		chkRequired.addClickListener(new ClickListener(){
			public void onClick(Widget widget){
				((QuestionDef)propertiesObj).setRequired(chkRequired.isChecked());
				formChangeListener.onFormItemChanged(propertiesObj);
			}
		});

		//Text boxes.
		txtDefaultValue.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				updateDefaultValue();
			}
		});
		txtDefaultValue.addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				updateDefaultValue();
			}
		});

		txtHelpText.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				updateHelpText();
			}
		});
		txtHelpText.addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				updateHelpText();
			}
			public void onKeyDown(Widget sender, char keyCode, int modifiers) {
				if(keyCode == KeyboardListener.KEY_ENTER || keyCode == KeyboardListener.KEY_DOWN)
					cbDataType.setFocus(true);
				else if(keyCode == KeyboardListener.KEY_UP){
					txtText.setFocus(true);
					txtText.selectAll();
				}
			}
		});

		txtBinding.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				updateBinding();
			}
		});
		txtBinding.addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				updateBinding();
			}
			public void onKeyDown(Widget sender, char keyCode, int modifiers) {
				if(keyCode == KeyboardListener.KEY_UP){
					if(cbDataType.isEnabled())
						cbDataType.setFocus(true);
					else{
						txtText.setFocus(true);
						txtText.selectAll();
					}
				}
			}
		});

		txtText.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				updateText();
			}
		});
		txtText.addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				updateText();
			}
			public void onKeyDown(Widget sender, char keyCode, int modifiers) {
				if(keyCode == KeyboardListener.KEY_ENTER || keyCode == KeyboardListener.KEY_DOWN){
					if(txtHelpText.isEnabled())
						txtHelpText.setFocus(true);
					else{
						txtBinding.setFocus(true);
						txtBinding.selectAll();
					}
				}
			}
		});

		txtDescTemplate.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				updateDescTemplate();
			}
		});
		txtDescTemplate.addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				updateDescTemplate();
			}
		});

		//Combo boxes
		cbDataType.addClickListener(new ClickListener(){
			public void onClick(Widget sender){
				updateDataType();
			}
		});
		cbDataType.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				updateDataType();
			}
		});
		cbDataType.addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyDown(Widget sender, char keyCode, int modifiers) {
				if(keyCode == KeyboardListener.KEY_ENTER || keyCode == KeyboardListener.KEY_DOWN){
					txtBinding.setFocus(true);
					txtBinding.selectAll();
				}
				else if(keyCode == KeyboardListener.KEY_UP){
					txtHelpText.setFocus(true);
					txtHelpText.selectAll();
				}
			}
		});
	}

	private void updateText(){
		if(propertiesObj == null)
			return;

		if(propertiesObj instanceof QuestionDef)
			((QuestionDef)propertiesObj).setText(txtText.getText());
		else if(propertiesObj instanceof OptionDef)
			((OptionDef)propertiesObj).setText(txtText.getText());
		else if(propertiesObj instanceof PageDef)
			((PageDef)propertiesObj).setName(txtText.getText());
		else if(propertiesObj instanceof FormDef)
			((FormDef)propertiesObj).setName(txtText.getText());

		formChangeListener.onFormItemChanged(propertiesObj);
	}

	private void updateDescTemplate(){
		if(propertiesObj == null)
			return;

		else if(propertiesObj instanceof FormDef){
			((FormDef)propertiesObj).setDescriptionTemplate(txtDescTemplate.getText());
			formChangeListener.onFormItemChanged(propertiesObj);
		}
	}

	private void updateBinding(){
		if(propertiesObj == null)
			return;

		if(propertiesObj instanceof QuestionDef)
			((QuestionDef)propertiesObj).setVariableName(txtBinding.getText());
		else if(propertiesObj instanceof OptionDef)
			((OptionDef)propertiesObj).setVariableName(txtBinding.getText());
		else if(propertiesObj instanceof FormDef)
			((FormDef)propertiesObj).setVariableName(txtBinding.getText());

		formChangeListener.onFormItemChanged(propertiesObj);
	}

	private void updateHelpText(){
		if(propertiesObj == null)
			return;

		((QuestionDef)propertiesObj).setHelpText(txtHelpText.getText());
		formChangeListener.onFormItemChanged(propertiesObj);
	}

	private void updateDefaultValue(){
		if(propertiesObj == null)
			return;

		((QuestionDef)propertiesObj).setDefaultValue(txtDefaultValue.getText());
		formChangeListener.onFormItemChanged(propertiesObj);
	}

	private void updateDataType(){
		if(propertiesObj == null)
			return;

		boolean deleteKids = false;
		int index = cbDataType.getSelectedIndex();
		QuestionDef questionDef = (QuestionDef)propertiesObj;
		if((questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
				questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE) &&
				!(index == DT_SINGLE_SELECT || index == DT_MULTIPLE_SELECT)){
			if(!Window.confirm("Do you really want to change to this type and lose all the options created, if any?")){
				index = (questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE) ? DT_SINGLE_SELECT : DT_MULTIPLE_SELECT;
				cbDataType.setSelectedIndex(index);
				return;
			}
			deleteKids = true;
		}

		//cbDataType.setSelectedIndex(index);
		setQuestionDataType((QuestionDef)propertiesObj);
		formChangeListener.onFormItemChanged(propertiesObj);
		if(deleteKids)
			formChangeListener.onDeleteChildren(propertiesObj);
	}

	private void setQuestionDataType(QuestionDef questionDef){
		int dataType = QuestionDef.QTN_TYPE_TEXT;

		switch(cbDataType.getSelectedIndex()){
		case DT_NUMBER:
			dataType = QuestionDef.QTN_TYPE_NUMERIC;
			break;
		case DT_DECIMAL:
			dataType = QuestionDef.QTN_TYPE_DECIMAL;
			break;
		case DT_DATE:
			dataType = QuestionDef.QTN_TYPE_DATE;
			break;
		case DT_TIME:
			dataType = QuestionDef.QTN_TYPE_TIME;
			break;
		case DT_DATE_TIME:
			dataType = QuestionDef.QTN_TYPE_DATE_TIME;
			break;
		case DT_BOOLEAN:
			dataType = QuestionDef.QTN_TYPE_BOOLEAN;
			break;
		case DT_SINGLE_SELECT:
			dataType = QuestionDef.QTN_TYPE_LIST_EXCLUSIVE;
			break;
		case DT_MULTIPLE_SELECT:
			dataType = QuestionDef.QTN_TYPE_LIST_MULTIPLE;
			break;
		case DT_REPEAT:
			dataType = QuestionDef.QTN_TYPE_REPEAT;
			break;
		case DT_IMAGE:
			dataType = QuestionDef.QTN_TYPE_IMAGE;
			break;
		case DT_VIDEO:
			dataType = QuestionDef.QTN_TYPE_VIDEO;
			break;
		case DT_AUDIO:
			dataType = QuestionDef.QTN_TYPE_AUDIO;
			break;
		case DT_SINGLE_SELECT_DYNAMIC:
			dataType = QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC;
			break;
		}

		if(dataType == QuestionDef.QTN_TYPE_REPEAT && 
				questionDef.getDataType() != QuestionDef.QTN_TYPE_REPEAT)
			questionDef.setRepeatQtnsDef(new RepeatQtnsDef(questionDef));

		questionDef.setDataType(dataType);
		
		if(questionDef.getDataType() != QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC)
			dynamicListsView.setEnabled(false);
		else if(!dynamicListsView.isEnabled())
			dynamicListsView.setQuestionDef(questionDef);
	}

	public void setFormChangeListener(IFormChangeListener formChangeListener){
		this.formChangeListener = formChangeListener;
	}

	private void setFormProperties(FormDef formDef){
		enableQuestionOnlyProperties(false);

		txtText.setEnabled(true);
		txtDescTemplate.setEnabled(true);
		btnDescTemplate.setEnabled(true);

		txtText.setText(formDef.getName());
		txtBinding.setText(formDef.getVariableName());
		//skipRulesView.setFormDef(formDef);

		txtDescTemplate.setText(formDef.getDescriptionTemplate());

		btnDescTemplate.setFormDef(formDef);
	}

	private void setPageProperties(PageDef pageDef){
		enableQuestionOnlyProperties(false);

		txtText.setEnabled(true);
		txtDescTemplate.setEnabled(false);
		btnDescTemplate.setEnabled(false);

		txtText.setText(pageDef.getName());
		txtBinding.setText(String.valueOf(pageDef.getPageNo()));
		//skipRulesView.updateSkipRule();
	}

	private void setQuestionProperties(QuestionDef questionDef){
		enableQuestionOnlyProperties(true);
		txtDescTemplate.setEnabled(false);
		btnDescTemplate.setEnabled(false);

		txtText.setText(questionDef.getText());
		txtBinding.setText(questionDef.getVariableName());
		txtHelpText.setText(questionDef.getHelpText());
		txtDefaultValue.setText(questionDef.getDefaultValue());

		chkVisible.setChecked(questionDef.isVisible());
		chkEnabled.setChecked(questionDef.isEnabled());
		chkLocked.setChecked(questionDef.isLocked());
		chkRequired.setChecked(questionDef.isRequired());

		setDataType(questionDef.getDataType());

		//Skip logic processing is a bit slow and hence we wanna update the 
		//UI with the rest of simple quick properties as we process skip logic
		DeferredCommand.addCommand(new Command(){
			public void execute() {
				skipRulesView.setQuestionDef((QuestionDef)propertiesObj);
				validationRulesView.setQuestionDef((QuestionDef)propertiesObj);
				dynamicListsView.setQuestionDef((QuestionDef)propertiesObj);
			}
		});
	}

	private void setQuestionOptionProperties(OptionDef optionDef){
		enableQuestionOnlyProperties(false);
		txtDescTemplate.setEnabled(false);
		btnDescTemplate.setEnabled(false);

		txtText.setText(optionDef.getText());
		txtBinding.setText(optionDef.getVariableName());
		//skipRulesView.updateSkipRule();
	}

	private void enableQuestionOnlyProperties(boolean enable){
		cbDataType.setEnabled(enable);
		//cbControlType.setEnabled(enable);
		chkVisible.setEnabled(enable);
		chkEnabled.setEnabled(enable);
		chkLocked.setEnabled(enable);
		chkRequired.setEnabled(enable);
		txtDefaultValue.setEnabled(enable);
		txtHelpText.setEnabled(enable);
		skipRulesView.setEnabled(enable);
		validationRulesView.setEnabled(enable);
		dynamicListsView.setEnabled(enable);

		clearProperties();
	}

	private void setDataType(int type){
		int index = DT_NONE;

		switch(type){
		case QuestionDef.QTN_TYPE_DATE:
			index = DT_DATE;
			break;
		case QuestionDef.QTN_TYPE_BOOLEAN:
			index = DT_BOOLEAN;
			break;
		case QuestionDef.QTN_TYPE_DATE_TIME:
			index = DT_DATE_TIME;
			break;
		case QuestionDef.QTN_TYPE_DECIMAL:
			index = DT_DECIMAL;
			break;
		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
			index = DT_SINGLE_SELECT;
			break;
		case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
			index = DT_MULTIPLE_SELECT;
			break;
		case QuestionDef.QTN_TYPE_NUMERIC:
			index = DT_NUMBER;
			break;
		case QuestionDef.QTN_TYPE_REPEAT:
			index = DT_REPEAT;
			break;
		case QuestionDef.QTN_TYPE_TEXT:
			index = DT_TEXT;
			break;
		case QuestionDef.QTN_TYPE_TIME:
			index = DT_TIME;
			break;
		case QuestionDef.QTN_TYPE_IMAGE:
			index = DT_IMAGE;
			break;
		case QuestionDef.QTN_TYPE_VIDEO:
			index = DT_VIDEO;
			break;
		case QuestionDef.QTN_TYPE_AUDIO:
			index = DT_AUDIO;
			break;
		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC:
			index = DT_SINGLE_SELECT_DYNAMIC;
			break;
		}

		cbDataType.setSelectedIndex(index);
	}

	public void clearProperties(){
		cbDataType.setSelectedIndex(DT_NONE);
		//cbControlType.setSelectedIndex(DT_NONE);
		chkVisible.setChecked(false);
		chkEnabled.setChecked(false);
		chkLocked.setChecked(false);
		chkRequired.setChecked(false);
		txtDefaultValue.setText(null);
		txtHelpText.setText(null);
		txtText.setText(null);
		txtBinding.setText(null);
		txtDescTemplate.setText(null);
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormSelectionListener#onFormItemSelected(java.lang.Object)
	 */
	public void onFormItemSelected(Object formItem) {
		propertiesObj = formItem;

		clearProperties();

		//For now these may be options for boolean question types (Yes & No)
		if(formItem == null){
			enableQuestionOnlyProperties(false);
			txtText.setEnabled(false);
			txtDescTemplate.setEnabled(false);
			btnDescTemplate.setEnabled(false);
			return;
		}

		if(formItem instanceof FormDef)
			setFormProperties((FormDef)formItem);
		else if(formItem instanceof PageDef)
			setPageProperties((PageDef)formItem);
		else if(formItem instanceof QuestionDef)
			setQuestionProperties((QuestionDef)formItem);
		else if(formItem instanceof OptionDef)
			setQuestionOptionProperties((OptionDef)formItem);
	}

	public void setFocus(){
		txtText.setFocus(true);
		txtText.selectAll();
	}

	public void onWindowResized(int width, int height){
		setWidth("100%");
		setHeight("100%");
		validationRulesView.onWindowResized(width, height);
	}

	public void commitChanges(){
		skipRulesView.updateSkipRule();
		validationRulesView.updateValidationRule();
		dynamicListsView.updateDynamicLists();
	}

	public void onItemSelected(Object sender, Object item) {
		if(sender instanceof DescTemplateWidget){
			txtDescTemplate.setText(txtDescTemplate.getText() + item);
			txtDescTemplate.setFocus(true);
		}
	}

	public void onStartItemSelection(Object sender) {

	}

	public void setFormActionListener(IFormActionListener formActionListener){
		this.formActionListener = formActionListener;
	}

	public void onBrowserEvent(Event event) {
		switch (DOM.eventGetType(event)) {
		case Event.ONKEYDOWN:
			if(!isVisible())
				return;

			int keyCode = event.getKeyCode();
			if(event.getCtrlKey()){
				if(keyCode == 'N' || keyCode == 'n'){
					formActionListener.addNewItem();
					DOM.eventPreventDefault(event);
				}
				else if(keyCode == KeyboardListener.KEY_RIGHT){
					formActionListener.moveToChild();
					DOM.eventPreventDefault(event);
				}
				else if(keyCode == KeyboardListener.KEY_LEFT){
					formActionListener.moveToParent();
					DOM.eventPreventDefault(event);
				}
				else if(keyCode == KeyboardListener.KEY_UP){
					formActionListener.moveUp();
					DOM.eventPreventDefault(event);
				}
				else if(keyCode == KeyboardListener.KEY_DOWN){
					formActionListener.moveDown();
					DOM.eventPreventDefault(event);
				}
			}
		}
	}
}
