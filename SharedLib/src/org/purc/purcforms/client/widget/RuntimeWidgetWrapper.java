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

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;


/**
 * Wraps a widget and gives it capability to be used at run time for data collection.
 * 
 * @author daniel
 *
 */
public class RuntimeWidgetWrapper extends WidgetEx implements QuestionChangeListener{

	/** Widget to display error message icon when the widget's validation fails. */
	protected Image errorImage;

	/** Collection of RadioButton and CheckBox wrapped widgets for a given question. */
	protected List<RuntimeWidgetWrapper> childWidgets;

	/** Listener to edit events. */
	protected EditListener editListener;

	private ImageResource errorImageProto;

	/** Flag that tells whether this widget is locked and hence doesn't allow editing. */
	private boolean locked = false;

	/** The widget's validation rule. */
	private ValidationRule validationRule;

	private static int id = 0;

	/**
	 * Creates a copy of the widget.
	 * 
	 * @param widget the widget to copy.
	 */
	public RuntimeWidgetWrapper(RuntimeWidgetWrapper widget){
		super(widget);

		editListener = widget.getEditListener();
		errorImage = FormUtil.createImage(widget.getErrorImage());
		errorImageProto = widget.getErrorImage();
		errorImage.setTitle(LocaleText.get("requiredErrorMsg"));

		if(widget.getValidationRule() != null)
			validationRule = new ValidationRule(widget.getValidationRule());

		panel.add(this.widget);
		initWidget(panel);
		setupEventListeners();

		if(widget.questionDef != null){ //TODO For long list of options may need to share list
			//If we have a validation rule, then it already has a question copy
			//which we should use if validations are to fire expecially for repeats
			if(validationRule != null)
				questionDef = validationRule.getFormDef().getQuestion(widget.questionDef.getId());
			else
				questionDef = new QuestionDef(widget.questionDef,widget.questionDef.getParent());
		}
	}

	public RuntimeWidgetWrapper(Widget widget,ImageResource errorImageProto,EditListener editListener){
		this.widget = widget;

		if(!(widget instanceof TabBar)){
			this.errorImageProto = errorImageProto;
			this.errorImage = FormUtil.createImage(errorImageProto);
			this.editListener = editListener;

			panel.add(widget);
			initWidget(panel);
			setupEventListeners();
			errorImage.setTitle(LocaleText.get("requiredErrorMsg"));

			DOM.sinkEvents(getElement(),DOM.getEventsSunk(getElement()) | Event.MOUSEEVENTS /*| Event.ONCONTEXTMENU | Event.KEYEVENTS*/);
		}
	}

	public ImageResource getErrorImage(){
		return errorImageProto;
	}

	/**
	 * Gets the question edit listener.
	 * 
	 * @return the edit listener.
	 */
	public EditListener getEditListener(){
		return editListener;
	}

	@Override
	public void onBrowserEvent(Event event) {
		if(locked){
			event.preventDefault();
			event.stopPropagation();
		}

		/*if(widget instanceof RadioButton && DOM.eventGetType(event) == Event.ONMOUSEUP){
			if(((RadioButton)widget).getValue() == true){
				event.stopPropagation();
				event.preventDefault();
				((RadioButton)widget).setValue(false);
				return;
			}
		}*/
	}

	/**
	 * Sets up events listeners.
	 */
	private void setupEventListeners(){
		if(widget instanceof DatePickerEx){
			((DatePickerEx)widget).addBlurHandler(new BlurHandler(){
				public void onBlur(BlurEvent event){
					((DatePickerEx)widget).selectAll();
				}
			});
		}

		if(widget instanceof TextBox)
			setupTextBoxEventListeners();
		else if(widget instanceof DateTimeWidget)
			setupDateTimeEventListeners();
		else if(widget instanceof CheckBox){
			((CheckBox)widget).addKeyDownHandler(new KeyDownHandler(){
				public void onKeyDown(KeyDownEvent event) {
					int keyCode = event.getNativeKeyCode();
					if(keyCode == KeyCodes.KEY_ENTER || keyCode == KeyCodes.KEY_DOWN
							|| keyCode == KeyCodes.KEY_RIGHT)
						editListener.onMoveToNextWidget((RuntimeWidgetWrapper)panel.getParent());
					else if(keyCode == KeyCodes.KEY_UP || keyCode == KeyCodes.KEY_LEFT)
						editListener.onMoveToPrevWidget((RuntimeWidgetWrapper)panel.getParent());
				}
			}); 
		}
		else if(widget instanceof ListBox){
			((ListBox)widget).addChangeHandler(new ChangeHandler(){
				public void onChange(ChangeEvent event){
					questionDef.setAnswer(((ListBox)widget).getValue(((ListBox)widget).getSelectedIndex()));
					isValid();
					editListener.onValueChanged((RuntimeWidgetWrapper)panel.getParent());
				}
			});

			((ListBox)widget).addKeyDownHandler(new KeyDownHandler(){
				public void onKeyDown(KeyDownEvent event) {
					int keyCode = event.getNativeKeyCode();
					if(keyCode == KeyCodes.KEY_ENTER || keyCode == KeyCodes.KEY_RIGHT)
						editListener.onMoveToNextWidget((RuntimeWidgetWrapper)panel.getParent());
					else if(keyCode == KeyCodes.KEY_LEFT)
						editListener.onMoveToPrevWidget((RuntimeWidgetWrapper)panel.getParent());
				}//TODO Do we really wanna alter the behaviour of the arrow keys for list boxes?
			});
		}

		DOM.sinkEvents(getElement(),DOM.getEventsSunk(getElement()) | Event.MOUSEEVENTS | Event.ONCONTEXTMENU | Event.KEYEVENTS);
	}

	public void addSuggestBoxChangeEvent(){
		if(widget instanceof TextBox){
			((TextBox)widget).addChangeHandler(new ChangeHandler(){
				public void onChange(ChangeEvent event){
					onSuggestBoxChange();
				}
			});
		}
	}

	private void onSuggestBoxChange(){
		if(questionDef != null){
			OptionDef optionDef = questionDef.getOptionWithText(getTextBoxAnswer());
			if(optionDef != null)
				questionDef.setAnswer(optionDef.getVariableName());
			else{
				questionDef.setAnswer(null);
				setText(null);
			}
			isValid();
			editListener.onValueChanged((RuntimeWidgetWrapper)panel.getParent());
		}
		else
			((TextBox)widget).setText(null);
	}

	/**
	 * Sets up text box event listeners.
	 */
	private void setupTextBoxEventListeners(){
		if(widget.getParent() instanceof SuggestBox){
			if(widget.getParent() instanceof SuggestBox){
				((SuggestBox)widget.getParent()).addSelectionHandler(new SelectionHandler(){
					public void onSelection(SelectionEvent event){
						onSuggestBoxChange();
					}
				});
			}

			addSuggestBoxChangeEvent();
		}
		else{
			((TextBox)widget).addChangeHandler(new ChangeHandler(){
				public void onChange(ChangeEvent event){
					//questionDef.setAnswer(((TextBox)widget).getText());
					if(questionDef != null){
						questionDef.setAnswer(getTextBoxAnswer());
						isValid();
						editListener.onValueChanged((RuntimeWidgetWrapper)panel.getParent());
					}

					if(widget instanceof DatePickerWidget)
						editListener.onMoveToNextWidget((RuntimeWidgetWrapper)panel.getParent());

					//Window.alert(widget.getElement().getId());
					//Window.alert(FormUtil.getElementValue(DOM.getElementById(widget.getElement().getId())));

					/*if("question1".equals(widget.getElement().getId())){
						DateTimeFormat format = FormUtil.getDateDisplayFormat();
						String value1 = format.format(new Date());
						String value2 = FormUtil.getElementValue(DOM.getElementById(widget.getElement().getId()));
						FormUtil.setElementValue(DOM.getElementById("question5"), value1, value2);
					}*/
				}
			});
		}

		((TextBox)widget).addKeyUpHandler(new KeyUpHandler(){
			public void onKeyUp(KeyUpEvent event) {
				if(event.getNativeKeyCode() == KeyCodes.KEY_TAB)
					return;

				if(questionDef != null && !(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC)){
					questionDef.setAnswer(getTextBoxAnswer());

					isValid();

					editListener.onValueChanged((RuntimeWidgetWrapper)panel.getParent());
				}
			}
		});

		((TextBox)widget).addKeyDownHandler(new KeyDownHandler(){
			public void onKeyDown(KeyDownEvent event) {
				int keyCode = event.getNativeKeyCode();
				if(keyCode == KeyCodes.KEY_ENTER || keyCode == KeyCodes.KEY_DOWN)
					editListener.onMoveToNextWidget((RuntimeWidgetWrapper)panel.getParent());
				else if(keyCode == KeyCodes.KEY_UP)
					editListener.onMoveToPrevWidget((RuntimeWidgetWrapper)panel.getParent());
			}
		});

		((TextBox)widget).addKeyPressHandler(new KeyPressHandler(){
			public void onKeyPress(KeyPressEvent event) {
				int keyCode = event.getCharCode();
				if((externalSource != null && externalSource.trim().length() > 0) && 
						(displayField == null || displayField.trim().length() == 0) &&
						(valueField == null || valueField.trim().length() == 0) ){

					if(keyCode == KeyCodes.KEY_TAB || keyCode == KeyCodes.KEY_ENTER){
						//editListener.onMoveToNextWidget((RuntimeWidgetWrapper)panel.getParent());
						return;
					}

					((TextBox) event.getSource()).cancelKey(); 
					while(panel.getWidgetCount() > 1)
						panel.remove(1);

					if(keyCode == (char) KeyCodes.KEY_DELETE || keyCode == (char) KeyCodes.KEY_BACKSPACE){
						((TextBox) event.getSource()).setText("");
						if(questionDef != null)
							questionDef.setAnswer(null);

						return;
					}

					Label label = new Label("");
					label.setVisible(false);
					panel.add(label);
					FormUtil.searchExternal(externalSource,String.valueOf(event.getCharCode()), widget.getElement(), label.getElement(), widget.getElement(),filterField);
				}
			}
		});
	}


	/**
	 * Sets up date time event listeners.
	 */
	private void setupDateTimeEventListeners(){
		((DateTimeWidget)widget).addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				//questionDef.setAnswer(((TextBox)widget).getText());
				if(questionDef != null){
					questionDef.setAnswer(getTextBoxAnswer());
					isValid();
					editListener.onValueChanged((RuntimeWidgetWrapper)panel.getParent());
				}

				if(widget instanceof DatePickerWidget)
					editListener.onMoveToNextWidget((RuntimeWidgetWrapper)panel.getParent());
			}
		});

		((DateTimeWidget)widget).addKeyUpHandler(new KeyUpHandler(){
			public void onKeyUp(KeyUpEvent event) {
				if(questionDef != null && !(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC)){
					questionDef.setAnswer(getTextBoxAnswer());

					isValid();

					editListener.onValueChanged((RuntimeWidgetWrapper)panel.getParent());
				}
			}
		});
	}


	/**
	 * Sets the question for the widget.
	 * 
	 * @param questionDef the question definition object.
	 * @param loadWidget set to true to load widget values, else false.
	 */
	public void setQuestionDef(QuestionDef questionDef ,boolean loadWidget){
		this.questionDef = questionDef;

		if(loadWidget)
			loadQuestion();
	}

	/**
	 * Loads values for the widget.
	 */
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
		else if(type == QuestionDef.QTN_TYPE_LIST_MULTIPLE && defaultValue != null 
				&& defaultValue.trim().length() > 0&& binding != null 
				&& binding.trim().length() > 0 && widget instanceof CheckBox){
			if(defaultValue.contains(binding))
				((CheckBox)widget).setValue(true);
		}
		else if(type == QuestionDef.QTN_TYPE_DATE_TIME && widget instanceof DateTimeWidget){
			if(defaultValue != null && defaultValue.trim().length() > 0 && questionDef.isDate()){
				if(QuestionDef.isDateFunction(defaultValue))
					defaultValue = questionDef.getDefaultValueDisplay();
				else
					defaultValue = fromSubmit2DisplayDate(defaultValue);

				((DateTimeWidget)widget).setText(defaultValue);
			}
		}


		if(widget instanceof TextBoxBase){
			((TextBoxBase)widget).setText(""); //first init just incase we have default value

			if(defaultValue != null && defaultValue.trim().length() > 0){
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

					((TextBoxBase)widget).setText(defaultValue);

					setExternalSourceDisplayValue();
				}
			}
		}

		if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
			questionDef.setAnswer("0");

		//TODO Looks like this should be at the end after all widgets are loaded
		//isValid();

		if(!questionDef.isEnabled())
			setEnabled(false);
		if(!questionDef.isVisible())
			setVisible(false);
		if(questionDef.isLocked())
			setLocked(true);
	}


	public void setExternalSourceDisplayValue(){
		if(externalSource == null || externalSource.trim().length() == 0)
			return;

		if(questionDef == null || questionDef.getDataNode() == null)
			return;

		if(!(widget instanceof TextBox))
			return;

		String defaultValue = questionDef.getDefaultValue();
		if(defaultValue == null || defaultValue.trim().length() == 0)
			return;

		String displayValue = questionDef.getDataNode().getAttribute("displayValue");
		if(displayValue != null){
			while(panel.getWidgetCount() > 1)
				panel.remove(1);

			Label label = new Label(defaultValue);
			label.setVisible(false);
			panel.add(label);

			((TextBox)widget).setText(displayValue);
			
			//Used only once on form loading.
			questionDef.getDataNode().removeAttribute("displayValue");
		}
	}


	public void setAnswer(String answer){
		questionDef.setAnswer(answer);

		int type = questionDef.getDataType();

		if((type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC
				|| type == QuestionDef.QTN_TYPE_LIST_MULTIPLE)
				&& widget instanceof ListBox){
			List options  = questionDef.getOptions();
			int defaultValueIndex = 0;
			ListBox listBox = (ListBox)widget;

			if(options != null){
				for(int index = 0; index < options.size(); index++){
					OptionDef optionDef = (OptionDef)options.get(index);
					if(optionDef.getVariableName().equalsIgnoreCase(answer))
						defaultValueIndex = index+1;
				}
			}
			listBox.setSelectedIndex(defaultValueIndex);
		}
		else if(type == QuestionDef.QTN_TYPE_BOOLEAN && widget instanceof ListBox){
			ListBox listBox = (ListBox)widget;
			if(answer != null){
				if(answer.equalsIgnoreCase(QuestionDef.TRUE_VALUE))
					listBox.setSelectedIndex(1);
				else if(answer.equalsIgnoreCase(QuestionDef.FALSE_VALUE))
					listBox.setSelectedIndex(2);
			}
		}
		else if(type == QuestionDef.QTN_TYPE_LIST_MULTIPLE && answer != null 
				&& answer.trim().length() > 0&& binding != null 
				&& binding.trim().length() > 0 && widget instanceof CheckBox){
			if(answer.contains(binding))
				((CheckBox)widget).setValue(true);
		}
		else if(type == QuestionDef.QTN_TYPE_DATE_TIME && widget instanceof DateTimeWidget)
			((DateTimeWidget)widget).setText(answer);


		if(widget instanceof TextBoxBase){
			((TextBoxBase)widget).setText(""); //first init just incase we have default value

			if(answer != null && answer.trim().length() > 0){
				if(type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE){
					OptionDef optionDef = questionDef.getOptionWithValue(answer);
					if(optionDef != null)
						((TextBox)widget).setText(optionDef.getText());
				}
				else{
					if(answer.trim().length() > 0 && questionDef.isDate() && questionDef.isDateFunction(answer))
						answer = questionDef.getDefaultValueDisplay();
					else if(answer.trim().length() > 0 && questionDef.isDate())
						answer = fromSubmit2DisplayDate(answer);

					((TextBoxBase)widget).setText(answer);
				}
			}
		}
	}


	/**
	 * Converts a date,time or dateTime from its xml submit format to display format.
	 * 
	 * @param value the text value in submit format.
	 * @return the value in its display format.
	 */
	private String fromSubmit2DisplayDate(String value){
		try{
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_TIME)
				return FormUtil.getTimeDisplayFormat().format(FormUtil.getTimeSubmitFormat().parse(value));
			else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_DATE_TIME)
				return FormUtil.getDateTimeDisplayFormat().format(FormUtil.getDateTimeSubmitFormat().parse(value));
			else
				return FormUtil.getDateDisplayFormat().format(FormUtil.getDateSubmitFormat().parse(value));
		}catch(Exception ex){}
		return null;
	}

	/**
	 * Sets whether this widget is enabled.
	 * 
	 * @param enabled <code>true</code> to enable the widget, <code>false</code>
	 *        to disable it.
	 */
	public void setEnabled(boolean enabled){
		if(widget instanceof RadioButton){
			((RadioButton)widget).setEnabled(enabled);
			if(!enabled)
				((RadioButton)widget).setValue(false);
		}
		else if(widget instanceof CheckBox){
			((CheckBox)widget).setEnabled(enabled);
			if(!enabled)
				((CheckBox)widget).setValue(false);
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
		else if(widget instanceof DateTimeWidget){
			((DateTimeWidget)widget).setEnabled(enabled);
			if(!enabled)
				((DateTimeWidget)widget).setText(null);
		}
		else if(widget instanceof RuntimeGroupWidget)
			((RuntimeGroupWidget)widget).setEnabled(enabled);
	}

	/**
	 * Determines if to allow editing of the widget value.
	 * 
	 * @param locked set to true to prevent editing of the widget value.
	 */
	public void setLocked(boolean locked){
		this.locked = locked;

		//Give a visual clue that this widget is locked.
		DOM.setStyleAttribute(widget.getElement(), "opacity", locked ? "0.6" : "100");

		if(widget instanceof RuntimeGroupWidget)
			((RuntimeGroupWidget)widget).setLocked(locked);
	}

	/**
	 * Checks if this widget does not allow changing of its value.
	 * 
	 * @return true if it does not allow, else false.
	 */
	public boolean isLocked(){
		return locked;
	}


	/**
	 * Gets the user answer from a TextBox widget.
	 * 
	 * @return the text answer.
	 */
	private String getTextBoxAnswer(){
		String value = null;
		if(widget instanceof TextBox)
			value = ((TextBox)widget).getText();
		else if(widget instanceof TimeWidget)
			value = ((TimeWidget)widget).getText();
		else if(widget instanceof DateTimeWidget)
			value = ((DateTimeWidget)widget).getText();

		try{
			if(questionDef.isDate() && value != null && value.trim().length() > 0){
				if(questionDef.getDataType() == QuestionDef.QTN_TYPE_TIME){
					value = FormUtil.getTimeSubmitFormat().format(FormUtil.getTimeDisplayFormat().parse(value));

					// ISO 8601 requires a colon in time zone offset (Java doesn't
					// include the colon, so we need to insert it
					if("yyyy-MM-dd'T'HH:mm:ssZ".equals(FormUtil.getTimeSubmitFormat().getPattern()))
						value = value.substring(0, 22) + ":" + value.substring(22);
				}
				else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_DATE_TIME){
					value = FormUtil.getDateTimeSubmitFormat().format(FormUtil.getDateTimeDisplayFormat().parse(value));

					// ISO 8601 requires a colon in time zone offset (Java doesn't
					// include the colon, so we need to insert it
					if("yyyy-MM-dd'T'HH:mm:ssZ".equals(FormUtil.getDateTimeSubmitFormat().getPattern()))
						value = value.substring(0, 22) + ":" + value.substring(22);
				}
				else 
					value = FormUtil.getDateSubmitFormat().format(FormUtil.getDateDisplayFormat().parse(value));
			}
		}
		catch(Exception ex){
			//If we get a problem parsing date, just return null.
			value = null;

			if(panel.getWidgetCount() < 2)
				panel.add(errorImage);

			String format = FormUtil.getDateDisplayFormat().getPattern();
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_TIME)
				format = FormUtil.getTimeDisplayFormat().getPattern();
			else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_DATE_TIME)
				format = FormUtil.getDateTimeDisplayFormat().getPattern();

			errorImage.setTitle(LocaleText.get("wrongFormat") + " " + format);
		}

		return value;
	}

	/**
	 * Retrieves the value from the widget to the question definition object for this widget.
	 * 
	 * @param formDef the form to which this widget's question belongs.
	 */
	public void saveValue(FormDef formDef){
		if(questionDef == null){
			if(widget instanceof RuntimeGroupWidget)
				((RuntimeGroupWidget)widget).saveValue(formDef);

			return;
		}

		//These are not used for filling any answers. HTML is used for audio and video
		if((widget instanceof Label || widget instanceof Button) && !(widget instanceof HTML))
			return;

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
			String answer = getTextBoxAnswer();
			if(externalSource != null && externalSource.trim().length() > 0 /*&&
					questionDef.getDataType() == QuestionDef.QTN_TYPE_NUMERIC*/){ //the internal save (non display) value needs to also work for non numerics.
				//answer = null; //TODO This seems to cause some bugs where numeric questions seem un answered. 

				if(panel.getWidgetCount() == 2){
					Widget wid = panel.getWidget(1);
					if(wid instanceof Label)
						answer = ((Label)wid).getText();
				}
			}

			questionDef.setAnswer(answer);

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
			if(!(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
					questionDef.getDataType() == QuestionDef.QTN_TYPE_BOOLEAN) || childWidgets == null)
				return;

			String value = null;

			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_BOOLEAN)
				value = questionDef.getAnswer();
			else{
				for(int index=0; index < childWidgets.size(); index++){
					RuntimeWidgetWrapper childWidget = childWidgets.get(index);
					String binding = childWidget.getBinding();
					if(((RadioButton)((RuntimeWidgetWrapper)childWidget).getWrappedWidget()).getValue() == true && binding != null){
						value = binding;
						break;
					}
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
				if(((CheckBox)((RuntimeWidgetWrapper)childWidget).getWrappedWidget()).getValue() == true && binding != null){
					if(value.length() != 0)
						value += " ";
					value += binding;
				}
			}

			questionDef.setAnswer(value);
		}
		else if(widget instanceof DateTimeWidget){
			questionDef.setAnswer(getTextBoxAnswer());

			//Fire fox clears default values when the widget is disabled. So put it as the answer manually.
			if(defaultValue != null && defaultValue.trim().length() > 0 && !((DateTimeWidget)widget).isEnabled()){
				if(questionDef.getAnswer() == null || questionDef.getAnswer().trim().length() == 0)
					questionDef.setAnswer(defaultValue);
			}
		}
		else if(widget instanceof RuntimeGroupWidget)
			((RuntimeGroupWidget)widget).saveValue(formDef);

		//Repeat widgets have a value for row count which does not go anywhere in the model
		if(!(widget instanceof RuntimeGroupWidget))
			questionDef.updateNodeValue(formDef);
	}

	/**
	 * Adds a CheckBox or RadioButton widget for the question of this widget.
	 * 
	 * @param childWidget the CheckBox or RadioButton widget.
	 */
	public void addChildWidget(final RuntimeWidgetWrapper childWidget){
		if(childWidgets == null)
			childWidgets = new ArrayList<RuntimeWidgetWrapper>();
		childWidgets.add(childWidget);

		String defaultValue = questionDef.getDefaultValue();
		int type = questionDef.getDataType();
		if((type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
				type == QuestionDef.QTN_TYPE_LIST_MULTIPLE)
				&& widget instanceof CheckBox && defaultValue != null){ 
			if(childWidgets.size() == questionDef.getOptions().size()){
				for(int index=0; index < childWidgets.size(); index++){
					RuntimeWidgetWrapper kidWidget = childWidgets.get(index);
					if((type == QuestionDef.QTN_TYPE_LIST_MULTIPLE && defaultValue.contains(kidWidget.getBinding())) ||
							(type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE && defaultValue.equals(kidWidget.getBinding()))){

						((CheckBox)((RuntimeWidgetWrapper)kidWidget).getWrappedWidget()).setValue(true);
						if(type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE)
							break; //for this we can't select more than one.
					}
				}
			}
		}

		((CheckBox)childWidget.getWrappedWidget()).addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event){
				if(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE){
					if(((CheckBox)event.getSource()).getValue() == true)
						questionDef.setAnswer(((RuntimeWidgetWrapper)((Widget)event.getSource()).getParent().getParent()).getBinding());
					else
						questionDef.setAnswer(null);
				}
				else{
					String answer = "";
					for(int index=0; index < childWidgets.size(); index++){
						RuntimeWidgetWrapper childWidget = childWidgets.get(index);
						String binding = childWidget.getBinding();
						if(((CheckBox)((RuntimeWidgetWrapper)childWidget).getWrappedWidget()).getValue() == true && binding != null){
							if(answer.length() > 0)
								answer += " , ";
							answer += binding;
						}
					}
					questionDef.setAnswer(answer);
				}
				isValid();
				editListener.onValueChanged((RuntimeWidgetWrapper)panel.getParent());
			}
		});


		//As for now, am not yet sure why below is what i need to turn off
		//radio button selections using space bar.
		if(childWidget.getWrappedWidget() instanceof RadioButton){
			((RadioButton)childWidget.getWrappedWidget()).addKeyUpHandler(new KeyUpHandler(){
				public void onKeyUp(KeyUpEvent event) {
					if(event.getNativeKeyCode() == 32)
						((RadioButton)childWidget.getWrappedWidget()).setValue(false);
				}
			});
		}
	}

	/**
	 * Get's the widget that is wrapped by this widget.
	 */
	public Widget getWrappedWidget(){
		return widget;
	}


	//These taken from question data.
	public boolean isValid(){
		if(widget instanceof Label || widget instanceof Button || questionDef == null || 
				(widget instanceof CheckBox && childWidgets == null)){

			if(widget instanceof RuntimeGroupWidget)
				return ((RuntimeGroupWidget)widget).isValid();

			return true;
		}

		if(questionDef.isRequired() && !this.isAnswered()){
			if(panel.getWidgetCount() < 2)
				panel.add(errorImage);

			errorImage.setTitle(LocaleText.get("requiredErrorMsg"));
			return false;
		}

		if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT){
			boolean valid = false;
			if((widget instanceof RuntimeGroupWidget))
				valid = ((RuntimeGroupWidget)widget).isValid();
			if(!valid)
				return false;
		}

		//For some reason the validation rule object, before saving, has a formdef different
		//from the one we are using and hence need to update it
		if(validationRule != null){
			FormDef formDef = questionDef.getParentFormDef();
			if(formDef != validationRule.getFormDef())
				validationRule.setFormDef(formDef);
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

		//Date, Time & DateTime parse text input and give an answer of null if the entered
		//value is not valid and hence we need to show the error flag.
		if((widget instanceof TextBox && questionDef.getAnswer() == null && ((TextBox)widget).getText().trim().length() > 0) ||
				(widget instanceof TimeWidget && questionDef.getAnswer() == null && ((TimeWidget)widget).getText().trim().length() > 0) ||
				(widget instanceof DateTimeWidget && questionDef.getAnswer() == null && ((DateTimeWidget)widget).getText().trim().length() > 0)){

			if(panel.getWidgetCount() < 2)
				panel.add(errorImage);
			return false;
		}

		if(panel.getWidgetCount() > 1)
			panel.remove(errorImage);
		return true;
	}

	/**
	 * Check if the question represented by this widget has been answered.
	 * 
	 * @return true if answered, else false.
	 */
	public boolean isAnswered(){
		return getAnswer() != null && getAnswer().toString().trim().length() > 0;
	}

	/**
	 * Gets the answer for the question wrapped by the widget.
	 * 
	 * @return the answer.
	 */
	private Object getAnswer() {
		if(questionDef == null)
			return null;

		return questionDef.getAnswer();
	}

	/**
	 * Sets input focus to the widget.
	 * 
	 * @return true if the widget accepts input focus.
	 */
	public boolean setFocus(){
		if(questionDef != null && (!questionDef.isVisible() || !questionDef.isEnabled() || questionDef.isLocked()))
			return false;

		//Browser does not seem to set focus to check boxes and radio buttons

		if(widget instanceof RadioButton)
			((RadioButton)widget).setFocus(true);
		else if(widget instanceof CheckBox)
			((CheckBox)widget).setFocus(true);
		else if(widget instanceof ListBox)
			((ListBox)widget).setFocus(true);
		else if(widget instanceof TextArea){
			((TextArea)widget).setFocus(true);
			((TextArea)widget).selectAll();
		}
		else if(widget instanceof TextBox){
			((TextBox)widget).setFocus(true);
			((TextBox)widget).selectAll();
		}
		else if(widget instanceof DateTimeWidget)
			((DateTimeWidget)widget).setFocus(true);
		else if(widget instanceof RuntimeGroupWidget)
			((RuntimeGroupWidget)widget).setFocus();
		else
			return false;
		return true;
	}

	/**
	 * Clears the value or answer entered for this widget.
	 */
	public void clearValue(){
		if(widget instanceof RadioButton)
			((RadioButton)widget).setValue(false);
		else if(widget instanceof CheckBox)
			((CheckBox)widget).setValue(false);
		else if(widget instanceof ListBox)
			((ListBox)widget).setSelectedIndex(-1);
		else if(widget instanceof TextArea)
			((TextArea)widget).setText(null);
		else if(widget instanceof TextBox)
			((TextBox)widget).setText(null);
		else if(widget instanceof DatePickerEx)
			((DatePickerEx)widget).setText(null);
		else if(widget instanceof DateTimeWidget)
			((DateTimeWidget)widget).setText(null);
		else if(widget instanceof Image)
			((Image)widget).setUrl(null);
		else if(widget instanceof RuntimeGroupWidget)
			((RuntimeGroupWidget)widget).clearValue();

		if(questionDef != null)
			questionDef.setAnswer(null);
	}

	/**
	 * @see org.purc.purcforms.client.controller.QuestionChangeListener#onEnabledChanged(QuestionDef, boolean)
	 */
	public void onEnabledChanged(QuestionDef sender,boolean enabled) {
		if(!enabled)
			clearValue();

		setEnabled(enabled);
	}

	/**
	 * @see org.purc.purcforms.client.controller.QuestionChangeListener#onLockedChanged(QuestionDef, boolean)
	 */
	public void onLockedChanged(QuestionDef sender,boolean locked) {
		if(locked)
			clearValue();

		setLocked(locked);
	}

	/**
	 * @see org.purc.purcforms.client.controller.QuestionChangeListener#onRequiredChanged(QuestionDef, boolean)
	 */
	public void onRequiredChanged(QuestionDef sender,boolean required) {
		//As for now we do not set error messages on labels.
		if(!(widget instanceof Label)){
			if(!required && panel.getWidgetCount() > 1)
				panel.remove(errorImage);
			else if(required && panel.getWidgetCount() < 2 && !isAnswered())
				panel.add(errorImage);
		}
	}

	/**
	 * @see org.purc.purcforms.client.controller.QuestionChangeListener#onVisibleChanged(QuestionDef, boolean)
	 */
	public void onVisibleChanged(QuestionDef sender,boolean visible) {
		if(!visible)
			clearValue();

		setVisible(visible);
	}

	/**
	 * @see org.purc.purcforms.client.controller.QuestionChangeListener#onBindingChanged(QuestionDef, String)
	 */
	public void onBindingChanged(QuestionDef sender,String newValue){
		if(newValue != null && newValue.trim().length() > 0)
			binding = newValue;
	}

	/**
	 * Gets the question wrapped by this widget.
	 * 
	 * @return the question definition object.
	 */
	public QuestionDef getQuestionDef(){
		return questionDef;
	}

	/**
	 * @see org.purc.purcforms.client.controller.QuestionChangeListener#onDataTypeChanged(QuestionDef, int)
	 */
	public void onDataTypeChanged(QuestionDef sender,int dataType){

	}

	/**
	 * Gets this widget's validation rule.
	 *
	 * @return the widget's validation rule.
	 */
	public ValidationRule getValidationRule() {
		return validationRule;
	}

	/**
	 * Sets the widget's validation rule.
	 * 
	 * @param validationRule the validation rule.
	 */
	public void setValidationRule(ValidationRule validationRule) {
		this.validationRule = validationRule;
	}

	/**
	 * @see org.purc.purcforms.client.controller.QuestionChangeListener#onOptionsChanged(QuestionDef, List)
	 */
	public void onOptionsChanged(QuestionDef sender,List<OptionDef> optionList){
		loadQuestion();
	}

	public RuntimeWidgetWrapper getInvalidWidget(){
		if(widget instanceof RuntimeGroupWidget)
			return ((RuntimeGroupWidget)widget).getInvalidWidget();
		return this;
	}

	/**
	 * Checks if this widget accepts input focus.
	 * 
	 * @return true if the widget accepts input focus, else false.
	 */
	public boolean isFocusable(){
		Widget wg = getWrappedWidget();
		return (wg instanceof TextBox || wg instanceof TextArea || wg instanceof DatePickerEx ||
				wg instanceof CheckBox || wg instanceof RadioButton || 
				wg instanceof RuntimeGroupWidget || wg instanceof ListBox
				|| wg instanceof DateTimeWidget);
	}


	public void moveToNextWidget(){
		editListener.onMoveToNextWidget((RuntimeWidgetWrapper)panel.getParent());
	}

	public void setBinding(String binding){
		super.setBinding(binding);
		if(getId() == null || getId().trim().length() == 0)
			setId();
	}

	public void setParentBinding(String parentBinding){
		super.setParentBinding(parentBinding);
		setId();
	}

	private void setId(){
		if(!(widget instanceof TextBox))
			return;

		String id = "";

		if(binding != null)
			id += binding;

		if(parentBinding != null)
			id += parentBinding;

		if(id.trim().length() > 0)
			widget.getElement().setId(id);
	}

	public boolean isEditable(){
		return (widget instanceof TextBox || widget instanceof TextArea || widget instanceof ListBox || widget instanceof CheckBox);
	}

	public void setId(String id){
		super.setId(id);
		widget.getElement().setId(id);
	}
}
