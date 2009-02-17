package org.purc.purcforms.client.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.purc.purcforms.client.controller.QuestionChangeListener;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.OptionDef;
import org.purc.purcforms.client.model.QuestionDef;
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
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SuggestBox;
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

	public RuntimeWidgetWrapper(){
		
	}
	
	public RuntimeWidgetWrapper(RuntimeWidgetWrapper widget){
		super(widget);
		
		this.editListener = widget.getEditListener();
		this.errorImage = widget.getErrorImage().createImage();
		this.errorImageProto = widget.getErrorImage();
		errorImage.setTitle("Please answer this required question.");
		
		panel.add(this.widget);
		initWidget(panel);
		setupEventListeners();
				
		if(widget.questionDef != null)
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
		errorImage.setTitle("Please answer this required question.");
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

		if(widget instanceof TextBox){
			((TextBox)widget).addChangeListener(new ChangeListener(){
				public void onChange(Widget sender){
					questionDef.setAnswer(((TextBox)widget).getText());
					isValid();
					editListener.onValueChanged(null, null, questionDef.getAnswer());
				}
			});

			((TextBox)widget).addKeyboardListener(new KeyboardListenerAdapter(){
				public void onKeyDown(Widget sender, char keyCode, int modifiers) {
					if(keyCode == KeyboardListener.KEY_ENTER || keyCode == KeyboardListener.KEY_DOWN)
						editListener.onMoveToNextWidget((RuntimeWidgetWrapper)panel.getParent());
					else if(keyCode == KeyboardListener.KEY_UP)
						editListener.onMoveToPrevWidget((RuntimeWidgetWrapper)panel.getParent());
				}
			});
		}
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
					editListener.onValueChanged(null, null, questionDef.getAnswer());
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
	}

	public String getBinding(){
		return binding;
	}

	public void setBinding(String binding){
		this.binding = binding;
	}

	public void setQuestionDef(QuestionDef questionDef){
		this.questionDef = questionDef;
		loadQuestion();
	}
	
	public void loadQuestion(){
		if(questionDef == null)
			return;
		
		questionDef.addChangeListener(this);
		questionDef.setAnswer(questionDef.getDefaultValue());

		String defaultValue = questionDef.getDefaultValue();

		if((questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
				questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE)
				&& widget instanceof ListBox){
			int defaultValueIndex = 0;
			ListBox listBox = (ListBox)widget;
			Vector options  = questionDef.getOptions();
			listBox.addItem("","");
			for(int index = 0; index < options.size(); index++){
				OptionDef optionDef = (OptionDef)options.get(index);
				listBox.addItem(optionDef.getText(), optionDef.getVariableName());
				if(optionDef.getVariableName().equalsIgnoreCase(defaultValue))
					defaultValueIndex = index+1;
			}
			listBox.setSelectedIndex(defaultValueIndex);
		}
		else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE && widget instanceof TextBox){ 
			MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
			FormUtil.loadOptions(questionDef.getOptions(),oracle);
			SuggestBox sgstBox = new SuggestBox(oracle,(TextBox)widget);
			panel.remove(widget);
			panel.add(sgstBox);
			sgstBox.setTabIndex(((TextBox)widget).getTabIndex());
		}
		else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_BOOLEAN && widget instanceof ListBox){
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
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE){
				OptionDef optionDef = questionDef.getOptionWithValue(defaultValue);
				if(optionDef != null)
					((TextBox)widget).setText(optionDef.getText());
			}
			else
				((TextBox)widget).setText(defaultValue);
		}

		isValid();

		if(!questionDef.isEnabled())
			setEnabled(false);
		if(!questionDef.isVisible())
			setVisible(false);
		if(questionDef.isLocked())
			setLocked(true);
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

	public void saveValue(FormDef formDef){
		if(widget instanceof TextBox && questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE){
			OptionDef optionDef = questionDef.getOptionWithText(((TextBox)widget).getText());
			if(optionDef != null)
				questionDef.setAnswer(optionDef.getVariableName());
			else
				questionDef.setAnswer(null);
		}
		else if(widget instanceof TextBox)
			questionDef.setAnswer(((TextBox)widget).getText());
		else if(widget instanceof ListBox){
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
					questionDef.getDataType() == QuestionDef.QTN_TYPE_BOOLEAN){
				String value = null;
				ListBox lb = (ListBox)widget;
				if(lb.getSelectedIndex() >= 0)
					value = lb.getValue(lb.getSelectedIndex());
				questionDef.setAnswer(value);
			}
		}
		else if(widget instanceof RadioButton){ //Should be before CheckBox
			if(questionDef == null || questionDef.getDataType() != QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || childWidgets == null)
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
			if(questionDef == null || questionDef.getDataType() != QuestionDef.QTN_TYPE_LIST_MULTIPLE || childWidgets == null)
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
		
		if(questionDef != null)
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
				editListener.onValueChanged(null, null, questionDef.getAnswer());
			}
		});
	}

	public Widget getWrappedWidget(){
		return widget;
	}


	//These taken from question data.
	public boolean isValid(){
		if(widget instanceof Label || widget instanceof Button ||
				(widget instanceof CheckBox && childWidgets == null))
			return true;

		if(questionDef.isRequired() && !this.isAnswered()){
			if(panel.getWidgetCount() < 2)
				panel.add(errorImage);
			return false;
		}
		if(panel.getWidgetCount() > 1)
			panel.remove(errorImage);
		return true;
	}

	private boolean isAnswered(){
		return getAnswer() != null && getAnswer().toString().trim().length() > 0;
	}

	private Object getAnswer() {
		return questionDef.getAnswer();
	}

	public boolean setFocus(){
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
		else
			return false;
		return true;
	}

	public void onEnabledChanged(boolean enabled) {
		setEnabled(enabled);
	}

	public void onLockedChanged(boolean locked) {
		setLocked(locked);
	}

	public void onRequiredChanged(boolean required) {
		if(!required && panel.getWidgetCount() > 1)
			panel.remove(errorImage);
		else if(required && panel.getWidgetCount() < 2)
			panel.add(errorImage);
	}

	public void onVisibleChanged(boolean visible) {
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
}
