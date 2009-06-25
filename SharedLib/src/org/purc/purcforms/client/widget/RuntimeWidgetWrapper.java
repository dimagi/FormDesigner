package org.purc.purcforms.client.widget;

import java.util.ArrayList;
import java.util.List;

import org.purc.purcforms.client.controller.QuestionChangeListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.OptionDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.model.ValidationRule;
import org.purc.purcforms.client.util.FormUtil;
import org.zenika.widget.client.datePicker.DatePicker;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FocusListenerAdapter;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestionEvent;
import com.google.gwt.user.client.ui.SuggestionHandler;
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author daniel
 *
 */
public class RuntimeWidgetWrapper extends WidgetEx implements QuestionChangeListener{

	protected Image errorImage;
	protected List<RuntimeWidgetWrapper> childWidgets;
	protected EditListener editListener;
	private AbstractImagePrototype errorImageProto;
	private boolean locked = false;

	private ValidationRule validationRule;

	public RuntimeWidgetWrapper(){

	}

	public RuntimeWidgetWrapper(RuntimeWidgetWrapper widget){
		super(widget);

		this.editListener = widget.getEditListener();
		this.errorImage = widget.getErrorImage().createImage();
		this.errorImageProto = widget.getErrorImage();
		errorImage.setTitle(LocaleText.get("requiredErrorMsg"));

		panel.add(this.widget);
		initWidget(panel);
		setupEventListeners();

		if(widget.questionDef != null) //TODO For long list of options may need to share list
			questionDef = new QuestionDef(widget.questionDef,widget.questionDef.getParent());
	}

	public RuntimeWidgetWrapper(Widget widget,AbstractImagePrototype errorImageProto,EditListener editListener){
		this.widget = widget;
		this.errorImageProto = errorImageProto;
		this.errorImage = errorImageProto.createImage();
		this.editListener = editListener;

		panel.add(widget);
		initWidget(panel);
		setupEventListeners();
		errorImage.setTitle(LocaleText.get("requiredErrorMsg"));
	}

	public AbstractImagePrototype getErrorImage(){
		return errorImageProto;
	}

	public EditListener getEditListener(){
		return editListener;
	}

	public void onBrowserEvent(Event event) {
		if(locked)
			event.preventDefault();
	}

	private void setupEventListeners(){
		if(widget instanceof DatePicker){
			((DatePicker)widget).addFocusListener(new FocusListenerAdapter(){
				public void onLostFocus(Widget sender){
					((DatePicker)widget).selectAll();
				}
			});
		}

		if(widget instanceof TextBox)
			setupTextBoxEventListeners();
		else if(widget instanceof CheckBox){
			/*if(childWidgets != null){
				 for(int index=0; index < childWidgets.size(); index++){
					  RuntimeWidgetWrapper childWidget = childWidgets.get(index);
					  ((CheckBox)childWidget.getWrappedWidget()).addClickListener(new ClickListener(){
							public void onClick(Widget sender){
								questionDef.setAnswer(((RuntimeWidgetWrapper)sender.getParent()).getBinding());
								isValid();
								editListener.onValueChanged(null, null, questionDef.getAnswer());
							}
					});
				 }
			}*/

			((CheckBox)widget).addKeyboardListener(new KeyboardListenerAdapter(){
				public void onKeyDown(Widget sender, char keyCode, int modifiers) {
					if(keyCode == KeyboardListener.KEY_ENTER || keyCode == KeyboardListener.KEY_DOWN
							|| keyCode == KeyboardListener.KEY_RIGHT)
						editListener.onMoveToNextWidget((RuntimeWidgetWrapper)panel.getParent());
					else if(keyCode == KeyboardListener.KEY_UP || keyCode == KeyboardListener.KEY_LEFT)
						editListener.onMoveToPrevWidget((RuntimeWidgetWrapper)panel.getParent());
				}
			});
		}
		else if(widget instanceof ListBox){
			((ListBox)widget).addChangeListener(new ChangeListener(){
				public void onChange(Widget sender){
					questionDef.setAnswer(((ListBox)widget).getValue(((ListBox)widget).getSelectedIndex()));
					isValid();
					editListener.onValueChanged(questionDef);
				}
			});

			((ListBox)widget).addKeyboardListener(new KeyboardListenerAdapter(){
				public void onKeyDown(Widget sender, char keyCode, int modifiers) {
					if(keyCode == KeyboardListener.KEY_ENTER || keyCode == KeyboardListener.KEY_RIGHT)
						editListener.onMoveToNextWidget((RuntimeWidgetWrapper)panel.getParent());
					else if(keyCode == KeyboardListener.KEY_LEFT)
						editListener.onMoveToPrevWidget((RuntimeWidgetWrapper)panel.getParent());
				}//TODO Do we really wanna alter the behaviour of the arrow keys for list boxes?
			});
		}

		DOM.sinkEvents(getElement(),DOM.getEventsSunk(getElement()) | Event.MOUSEEVENTS | Event.ONCONTEXTMENU | Event.KEYEVENTS);
	}

	private void setupTextBoxEventListeners(){
		if(widget.getParent() instanceof SuggestBox){
			((SuggestBox)widget.getParent()).addEventHandler(new SuggestionHandler(){
				public void onSuggestionSelected(SuggestionEvent event){
					if(questionDef != null){
						OptionDef optionDef = questionDef.getOptionWithText(getTextBoxAnswer());
						if(optionDef != null)
							questionDef.setAnswer(optionDef.getVariableName());
						else
							questionDef.setAnswer(null);
						isValid();
						editListener.onValueChanged(questionDef);
					}
				}
			});
		}
		else{
			((TextBox)widget).addChangeListener(new ChangeListener(){
				public void onChange(Widget sender){
					//questionDef.setAnswer(((TextBox)widget).getText());
					if(questionDef != null){
						questionDef.setAnswer(getTextBoxAnswer());
						isValid();
						editListener.onValueChanged(questionDef);
					}
					
					if(widget instanceof DatePickerWidget)
						editListener.onMoveToNextWidget((RuntimeWidgetWrapper)panel.getParent());
				}
			});
		}

		((TextBox)widget).addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				if(questionDef != null && !(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC)){
					questionDef.setAnswer(getTextBoxAnswer());
					isValid();
					editListener.onValueChanged(questionDef);
				}
			}

			public void onKeyDown(Widget sender, char keyCode, int modifiers) {
				if(keyCode == KeyboardListener.KEY_ENTER || keyCode == KeyboardListener.KEY_DOWN)
					editListener.onMoveToNextWidget((RuntimeWidgetWrapper)panel.getParent());
				else if(keyCode == KeyboardListener.KEY_UP)
					editListener.onMoveToPrevWidget((RuntimeWidgetWrapper)panel.getParent());
			}
		});
	}

	public void setWidth(String width){
		DOM.setStyleAttribute(widget.getElement(), "width",width);
	}

	public void setHeight(String height){
		DOM.setStyleAttribute(widget.getElement(), "height",height);
	}

	public void setText(String text){
		if(widget instanceof RadioButton)
			((RadioButton)widget).setText(text);
		else if(widget instanceof CheckBox)
			((CheckBox)widget).setText(text);
		else if(widget instanceof Button)
			((Button)widget).setText(text);
		else if(widget instanceof Label)
			((Label)widget).setText(text);
		else if(widget instanceof HTML)
			((HTML)widget).setText(text);
		else if(widget instanceof Hyperlink)
			((Label)widget).setText(text);
		else if(widget instanceof TabBar && text != null && text.trim().length() > 0)
			((TabBar)widget).setTabHTML(((TabBar)widget).getSelectedTab(), "<span style='white-space:nowrap'>" + text + "</span>");
	}

	public void setTitle(String title){
		if(widget instanceof RadioButton)
			((RadioButton)widget).setText(title);
		else if(widget instanceof CheckBox)
			((CheckBox)widget).setTitle(title);
		else if(widget instanceof Button)
			((Button)widget).setTitle(title);
		else if(widget instanceof ListBox)
			((ListBox)widget).setTitle(title);
		else if(widget instanceof TextArea)
			((TextArea)widget).setTitle(title);
		else if(widget instanceof TextBox)
			((TextBox)widget).setTitle(title);
		else if(widget instanceof Label)
			((Label)widget).setTitle(title);
		else if(widget instanceof Hyperlink)
			((Hyperlink)widget).setTitle(title);
		else if(widget instanceof HTML)
			((HTML)widget).setTitle(title);
	}

	public void setQuestionDef(QuestionDef questionDef ,boolean loadWidget){
		this.questionDef = questionDef;

		if(loadWidget)
			loadQuestion();
	}

	public void loadQuestion(){
		if(questionDef == null)
			return;

		//questionDef.clearChangeListeners(); Removed from here because we want to allow more that one widget listen on the same question.
		questionDef.addChangeListener(this);
		questionDef.setAnswer(questionDef.getDefaultValueSubmit());

		String defaultValue = questionDef.getDefaultValue();

		int type = questionDef.getDataType();
		if((type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC
				|| type == QuestionDef.QTN_TYPE_LIST_MULTIPLE)
				&& widget instanceof ListBox){
			List options  = questionDef.getOptions();
			int defaultValueIndex = 0;
			ListBox listBox = (ListBox)widget;
			listBox.clear(); //Could be called more than once.

			listBox.addItem("","");
			if(options != null){
				for(int index = 0; index < options.size(); index++){
					OptionDef optionDef = (OptionDef)options.get(index);
					listBox.addItem(optionDef.getText(), optionDef.getVariableName());
					if(optionDef.getVariableName().equalsIgnoreCase(defaultValue))
						defaultValueIndex = index+1;
				}
			}
			listBox.setSelectedIndex(defaultValueIndex);
		}
		else if((type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC) && widget instanceof TextBox){ 
			MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
			FormUtil.loadOptions(questionDef.getOptions(),oracle);
			if(widget.getParent() != null){
				RuntimeWidgetWrapper copyWidget = new RuntimeWidgetWrapper(this);
				panel.remove(widget.getParent());
				panel.remove(widget);

				copyWidgetProperties(copyWidget);
				setWidth(getWidth());
				setHeight(getHeight());
			}

			SuggestBox sgstBox = new SuggestBox(oracle,(TextBox)widget);
			panel.add(sgstBox);
			sgstBox.setTabIndex(((TextBox)widget).getTabIndex());
			setupTextBoxEventListeners();
		}
		else if(type == QuestionDef.QTN_TYPE_BOOLEAN && widget instanceof ListBox){
			ListBox listBox = (ListBox)widget;
			listBox.addItem("","");
			listBox.addItem(QuestionDef.TRUE_DISPLAY_VALUE, QuestionDef.TRUE_VALUE);
			listBox.addItem(QuestionDef.FALSE_DISPLAY_VALUE ,QuestionDef.FALSE_VALUE);
			listBox.setSelectedIndex(0);

			if(defaultValue != null){
				if(defaultValue.equalsIgnoreCase(QuestionDef.TRUE_VALUE))
					listBox.setSelectedIndex(1);
				else if(defaultValue.equalsIgnoreCase(QuestionDef.FALSE_VALUE))
					listBox.setSelectedIndex(2);
			}
		}

		if(widget instanceof TextBox && defaultValue != null){
			if(type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE){
				OptionDef optionDef = questionDef.getOptionWithValue(defaultValue);
				if(optionDef != null)
					((TextBox)widget).setText(optionDef.getText());
			}
			else{
				if(defaultValue.trim().length() > 0 && questionDef.isDate() && questionDef.isDateFunction(defaultValue))
					defaultValue = questionDef.getDefaultValueDisplay();
				else if(defaultValue.trim().length() > 0 && questionDef.isDate())
					defaultValue = fromSubmit2DisplayDate(defaultValue);

				((TextBox)widget).setText(defaultValue);
			}
		}

		if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
			questionDef.setAnswer("0");
		
		isValid();

		if(!questionDef.isEnabled())
			setEnabled(false);
		if(!questionDef.isVisible())
			setVisible(false);
		if(questionDef.isLocked())
			setLocked(true);
	}

	private String fromSubmit2DisplayDate(String value){
		try{
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_TIME)
				return FormUtil.getTimeDisplayFormat().format(FormUtil.getTimeSubmitFormat().parse(value));
			else
				return FormUtil.getDateTimeDisplayFormat().format(FormUtil.getDateTimeSubmitFormat().parse(value));
		}catch(Exception ex){}
		return null;
	}

	public void setEnabled(boolean enabled){
		if(widget instanceof RadioButton){
			((RadioButton)widget).setEnabled(enabled);
			if(!enabled)
				((RadioButton)widget).setChecked(false);
		}
		else if(widget instanceof CheckBox){
			((CheckBox)widget).setEnabled(enabled);
			if(!enabled)
				((CheckBox)widget).setChecked(false);
		}
		else if(widget instanceof Button)
			((Button)widget).setEnabled(enabled);
		else if(widget instanceof ListBox){
			((ListBox)widget).setEnabled(enabled);
			if(!enabled)
				((ListBox)widget).setSelectedIndex(0);
		}
		else if(widget instanceof TextArea){
			((TextArea)widget).setEnabled(enabled);
			if(!enabled)
				((TextArea)widget).setText(null);
		}
		else if(widget instanceof TextBox){
			((TextBox)widget).setEnabled(enabled);
			if(!enabled)
				((TextBox)widget).setText(null);
		}
		else if(widget instanceof RuntimeGroupWidget)
			((RuntimeGroupWidget)widget).setEnabled(enabled);
	}

	public void setLocked(boolean locked){

		this.locked = locked;

		/*if(widget instanceof RadioButton){
			((RadioButton)widget).setEnabled(locked);
			if(!locked)
				((RadioButton)widget).setChecked(false);
		}
		else if(widget instanceof CheckBox){
			((CheckBox)widget).setEnabled(locked);
			if(!locked)
				((CheckBox)widget).setChecked(false);
		}
		else if(widget instanceof Button)
			((Button)widget).setEnabled(locked);
		else if(widget instanceof ListBox){
			((ListBox)widget).setEnabled(locked);
			if(!locked)
				((ListBox)widget).setSelectedIndex(0);
		}
		else if(widget instanceof TextArea){
			((TextArea)widget).setEnabled(locked);
			if(!locked)
				((TextArea)widget).setText(null);
		}
		else if(widget instanceof TextBox){
			((TextBox)widget).setEnabled(locked);
			if(!locked)
				((TextBox)widget).setText(null);
		}
		else*/ if(widget instanceof RuntimeGroupWidget)
			((RuntimeGroupWidget)widget).setLocked(locked);
	}

	private String getTextBoxAnswer(){
		String value = ((TextBox)widget).getText();

		try{
			if(questionDef.isDate() && value != null && value.trim().length() > 0){
				if(questionDef.getDataType() == QuestionDef.QTN_TYPE_TIME)
					value = FormUtil.getTimeSubmitFormat().format(FormUtil.getTimeDisplayFormat().parse(value));
				else
					value = FormUtil.getDateTimeSubmitFormat().format(FormUtil.getDateTimeDisplayFormat().parse(value));
			}
		}
		catch(Exception ex){
			value = null;
		}

		return value;
	}

	public void saveValue(FormDef formDef){
		if(questionDef == null){
			if(widget instanceof RuntimeGroupWidget)
				((RuntimeGroupWidget)widget).saveValue(formDef);

			return;
		}

		String defaultValue = questionDef.getDefaultValueSubmit();

		if(widget instanceof TextBox && questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE){
			OptionDef optionDef = questionDef.getOptionWithText(((TextBox)widget).getText());
			if(optionDef != null)
				questionDef.setAnswer(optionDef.getVariableName());
			else
				questionDef.setAnswer(null);

			//Fire fox clears default values when the widget is disabled. So put it as the answer manually.
			if(defaultValue != null && defaultValue.trim().length() > 0 && !((TextBox)widget).isEnabled()){
				if(questionDef.getAnswer() == null || questionDef.getAnswer().trim().length() == 0)
					questionDef.setAnswer(defaultValue);
			}
		}
		else if(widget instanceof TextBox){
			questionDef.setAnswer(getTextBoxAnswer());

			//Fire fox clears default values when the widget is disabled. So put it as the answer manually.
			if(defaultValue != null && defaultValue.trim().length() > 0 && !((TextBox)widget).isEnabled()){
				if(questionDef.getAnswer() == null || questionDef.getAnswer().trim().length() == 0)
					questionDef.setAnswer(defaultValue);
			}
		}
		else if(widget instanceof TextArea){
			questionDef.setAnswer(((TextArea)widget).getText());

			//Fire fox clears default values when the widget is disabled. So put it as the answer manually.
			if(defaultValue != null && defaultValue.trim().length() > 0 && !((TextArea)widget).isEnabled()){
				if(questionDef.getAnswer() == null || questionDef.getAnswer().trim().length() == 0)
					questionDef.setAnswer(defaultValue);
			}
		}
		else if(widget instanceof ListBox){
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
					questionDef.getDataType() == QuestionDef.QTN_TYPE_BOOLEAN){
				String value = null;
				ListBox lb = (ListBox)widget;
				if(lb.getSelectedIndex() >= 0)
					value = lb.getValue(lb.getSelectedIndex());
				questionDef.setAnswer(value);
			}

			//Fire fox clears default values when the widget is disabled. So put it as the answer manually.
			if(defaultValue != null && defaultValue.trim().length() > 0 && !((ListBox)widget).isEnabled()){
				if(questionDef.getAnswer() == null || questionDef.getAnswer().trim().length() == 0)
					questionDef.setAnswer(defaultValue);
			}
		}
		else if(widget instanceof RadioButton){ //Should be before CheckBox
			if(questionDef.getDataType() != QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || childWidgets == null)
				return;

			String value = null;
			for(int index=0; index < childWidgets.size(); index++){
				RuntimeWidgetWrapper childWidget = childWidgets.get(index);
				String binding = childWidget.getBinding();
				if(((RadioButton)((RuntimeWidgetWrapper)childWidget).getWrappedWidget()).isChecked() && binding != null){
					value = binding;
					break;
				}
			}

			questionDef.setAnswer(value);
		}
		else if(widget instanceof CheckBox){
			if(questionDef.getDataType() != QuestionDef.QTN_TYPE_LIST_MULTIPLE || childWidgets == null)
				return;

			String value = "";
			for(int index=0; index < childWidgets.size(); index++){
				RuntimeWidgetWrapper childWidget = childWidgets.get(index);
				String binding = childWidget.getBinding();
				if(((CheckBox)((RuntimeWidgetWrapper)childWidget).getWrappedWidget()).isChecked() && binding != null){
					if(value.length() != 0)
						value += " ";
					value += binding;
				}
			}

			questionDef.setAnswer(value);
		}
		else if(widget instanceof RuntimeGroupWidget)
			((RuntimeGroupWidget)widget).saveValue(formDef);

		questionDef.updateNodeValue(formDef);
	}

	public void addChildWidget(RuntimeWidgetWrapper childWidget){
		if(childWidgets == null)
			childWidgets = new ArrayList<RuntimeWidgetWrapper>();
		childWidgets.add(childWidget);

		String defaultValue = questionDef.getDefaultValue();
		if(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE && 
				widget instanceof RadioButton && defaultValue != null){ 
			if(childWidgets.size() == questionDef.getOptions().size()){
				for(int index=0; index < childWidgets.size(); index++){
					RuntimeWidgetWrapper kidWidget = childWidgets.get(index);
					if(defaultValue.equals(kidWidget.getBinding())){
						((RadioButton)((RuntimeWidgetWrapper)kidWidget).getWrappedWidget()).setChecked(true);
						break;
					}
				}
			}
		}

		((CheckBox)childWidget.getWrappedWidget()).addClickListener(new ClickListener(){
			public void onClick(Widget sender){
				if(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE)
					questionDef.setAnswer(((RuntimeWidgetWrapper)sender.getParent().getParent()).getBinding());
				else{
					String answer = "";
					for(int index=0; index < childWidgets.size(); index++){
						RuntimeWidgetWrapper childWidget = childWidgets.get(index);
						String binding = childWidget.getBinding();
						if(((CheckBox)((RuntimeWidgetWrapper)childWidget).getWrappedWidget()).isChecked() && binding != null){
							if(answer.length() > 0)
								answer += " , ";
							answer += binding;
						}
					}
					questionDef.setAnswer(answer);
				}
				isValid();
				editListener.onValueChanged(questionDef);
			}
		});
	}

	public Widget getWrappedWidget(){
		return widget;
	}


	//These taken from question data.
	public boolean isValid(){
		if(widget instanceof Label || widget instanceof Button || questionDef == null ||
				(widget instanceof CheckBox && childWidgets == null))
			return true;

		if(questionDef.isRequired() && !this.isAnswered()){
			if(panel.getWidgetCount() < 2)
				panel.add(errorImage);

			errorImage.setTitle(LocaleText.get("requiredErrorMsg"));
			return false;
		}
		
		if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT){
			boolean valid = ((RuntimeGroupWidget)widget).isValid();
			if(!valid)
				return false;
		}

		if(validationRule != null && !validationRule.isValid()){
			if(panel.getWidgetCount() < 2)
				panel.add(errorImage);

			errorImage.setTitle(validationRule.getErrorMessage());
			return false;
		}
		/*FormDef formDef = null;
		ValidationRule rule = new ValidationRule();
		if(!rule.isValid(formDef)){

		}*/

		if(panel.getWidgetCount() > 1)
			panel.remove(errorImage);
		return true;
	}

	public boolean isAnswered(){
		return getAnswer() != null && getAnswer().toString().trim().length() > 0;
	}

	private Object getAnswer() {
		if(questionDef == null)
			return null;
		
		return questionDef.getAnswer();
	}

	public boolean setFocus(){
		if(questionDef != null && (!questionDef.isVisible() || !questionDef.isEnabled() || questionDef.isLocked()))
			return false;
		
		//Browser does not seem to set focus to check boxes and radio buttons
		
		/*if(widget instanceof RadioButton)
			((RadioButton)widget).setFocus(true);
		else if(widget instanceof CheckBox)
			((CheckBox)widget).setFocus(true);
		else*/ if(widget instanceof ListBox)
			((ListBox)widget).setFocus(true);
		else if(widget instanceof TextArea){
			((TextArea)widget).setFocus(true);
			((TextArea)widget).selectAll();
		}
		else if(widget instanceof TextBox){
			((TextBox)widget).setFocus(true);
			((TextBox)widget).selectAll();
		}
		else if(widget instanceof RuntimeGroupWidget)
			((RuntimeGroupWidget)widget).setFocus();
		else
			return false;
		return true;
	}

	public void clearValue(){
		if(widget instanceof RadioButton)
			((RadioButton)widget).setChecked(false);
		else if(widget instanceof CheckBox)
			((CheckBox)widget).setChecked(false);
		else if(widget instanceof ListBox)
			((ListBox)widget).setSelectedIndex(-1);
		else if(widget instanceof TextArea)
			((TextArea)widget).setText(null);
		else if(widget instanceof TextBox)
			((TextBox)widget).setText(null);
		else if(widget instanceof DatePicker)
			((DatePicker)widget).setText(null);
		else if(widget instanceof Image)
			((Image)widget).setUrl(null);
		else if(widget instanceof RuntimeGroupWidget)
			((RuntimeGroupWidget)widget).clearValue();

		if(questionDef != null)
			questionDef.setAnswer(null);
	}

	public void onEnabledChanged(boolean enabled) {
		if(!enabled)
			clearValue();

		setEnabled(enabled);
	}

	public void onLockedChanged(boolean locked) {
		if(locked)
			clearValue();

		setLocked(locked);
	}

	public void onRequiredChanged(boolean required) {
		if(!required && panel.getWidgetCount() > 1)
			panel.remove(errorImage);
		else if(required && panel.getWidgetCount() < 2)
			panel.add(errorImage);
	}

	public void onVisibleChanged(boolean visible) {
		if(!visible)
			clearValue();

		setVisible(visible);
	}

	public void onBindingChanged(String newValue){
		if(newValue != null && newValue.trim().length() > 0)
			binding = newValue;
	}

	public QuestionDef getQuestionDef(){
		return questionDef;
	}

	public void onDataTypeChanged(int dataType){

	}

	public ValidationRule getValidationRule() {
		return validationRule;
	}

	public void setValidationRule(ValidationRule validationRule) {
		this.validationRule = validationRule;
	}

	public void onOptionsChanged(List<OptionDef> optionList){
		loadQuestion();

		/*if(questionDef == null)
			return;

		questionDef.setAnswer(null);
		if(widget instanceof TextBox)
			((TextBox)widget).setText(null);
		else if(widget instanceof ListBox)
			((ListBox)widget).setSelectedIndex(-1);*/
	}
	
	public RuntimeWidgetWrapper getInvalidWidget(){
		if(widget instanceof RuntimeGroupWidget)
			return ((RuntimeGroupWidget)widget).getInvalidWidget();
		return this;
	}
	
	public boolean isFocusable(){
		Widget wg = getWrappedWidget();
		return (wg instanceof TextBox || wg instanceof TextArea || wg instanceof DatePicker ||
				wg instanceof CheckBox || wg instanceof RadioButton || 
				wg instanceof RuntimeGroupWidget || wg instanceof ListBox);
	}
}
