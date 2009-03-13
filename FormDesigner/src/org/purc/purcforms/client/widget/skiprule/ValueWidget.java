package org.purc.purcforms.client.widget.skiprule;

import java.util.Vector;

import org.purc.purcforms.client.controller.ItemSelectionListener;
import org.purc.purcforms.client.model.OptionDef;
import org.purc.purcforms.client.model.PurcConstants;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.widget.DatePickerWidget;
import org.purc.purcforms.client.widget.SelectItemCommand;
import org.zenika.widget.client.datePicker.DatePicker;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusListenerAdapter;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author daniel
 *
 */
public class ValueWidget extends Composite implements ItemSelectionListener, PopupListener{

	private static final String EMPTY_VALUE = "_____";
	private static final String BETWEEN_WIDGET_SEPARATOR = "   and   ";
	private static final String BETWEEN_VALUE_SEPARATOR = " and ";
	private static final String LIST_SEPARATOR = " , ";
	
	private QuestionDef questionDef;
	private int operator = PurcConstants.OPERATOR_NULL;
	
	private HorizontalPanel horizontalPanel;
	private TextBox txtValue1 = new TextBox();
	private TextBox txtValue2 = new TextBox();
	private Label lblAnd = new Label(BETWEEN_WIDGET_SEPARATOR);
	private Hyperlink valueHyperlink;
	private PopupPanel popup;
	private KeyboardListenerAdapter keyboardListener1;
	private KeyboardListenerAdapter keyboardListener2;
	private QuestionDef prevQuestionDef;
	
	public ValueWidget(){
		setupWidgets();
	}
	
	public void setQuestionDef(QuestionDef questionDef){
		prevQuestionDef = this.questionDef;
		this.questionDef = questionDef;
	}
	
	public void setOperator(int operator){
		if(this.operator != operator){ 
			if(this.operator == PurcConstants.OPERATOR_IS_NULL)
				valueHyperlink.setText(EMPTY_VALUE);
			
		    /*if((this.operator == PurcConstants.OPERATOR_IN_LIST || this.operator == PurcConstants.OPERATOR_NOT_IN_LIST) &&
			  !(operator == PurcConstants.OPERATOR_IN_LIST || operator == PurcConstants.OPERATOR_NOT_IN_LIST))
		    	valueHyperlink.setText(EMPTY_VALUE);*/
		}
		
		this.operator = operator;
		
		if(operator == PurcConstants.OPERATOR_IS_NULL)
			valueHyperlink.setText("");
		else if(operator == PurcConstants.OPERATOR_BETWEEN || operator == PurcConstants.OPERATOR_NOT_BETWEEN)
			valueHyperlink.setText(EMPTY_VALUE + BETWEEN_VALUE_SEPARATOR + EMPTY_VALUE);
	}
	
	private void setupWidgets(){
		horizontalPanel = new HorizontalPanel();;
		
		valueHyperlink = new Hyperlink(EMPTY_VALUE,null);
		horizontalPanel.add(valueHyperlink);

		valueHyperlink.addClickListener(new ClickListener(){
			public void onClick(Widget sender){
				startEdit();
			}
		});
		
		//Cuases wiered behaviour when editing with between operator
		/*txtValue1.addFocusListener(new FocusListenerAdapter(){
			public void onLostFocus(Widget sender){
				stopEdit();
			}
		});*/
		
		setupTextListeners();
		
		initWidget(horizontalPanel);
	}
	
	private void setupTextListeners(){
		txtValue1.addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyPress(Widget sender, char keyCode, int modifiers) {
				if(keyCode == KeyboardListener.KEY_ENTER)
					stopEdit(true);
			}
		});
		
		txtValue2.addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyPress(Widget sender, char keyCode, int modifiers) {
				if(keyCode == KeyboardListener.KEY_ENTER)
					stopEdit(true);
			}
		});
		
		if(!(operator == PurcConstants.OPERATOR_BETWEEN || operator == PurcConstants.OPERATOR_NOT_BETWEEN)){
			txtValue1.addFocusListener(new FocusListenerAdapter(){
				public void onLostFocus(Widget sender){
					stopEdit(true);
				}
			});
			txtValue2.addFocusListener(new FocusListenerAdapter(){
				public void onLostFocus(Widget sender){
					stopEdit(true);
				}
			});
			
			//if(txtValue1 instanceof DatePicker){
				txtValue1.addChangeListener(new ChangeListener(){
					public void onChange(Widget sender){
						stopEdit(true);
					}
				});
				txtValue2.addChangeListener(new ChangeListener(){
					public void onChange(Widget sender){
						stopEdit(true);
					}
				});
			//}
		}
	}
	
	private void startEdit(){
		if(questionDef.getDataType() == QuestionDef.QTN_TYPE_BOOLEAN){
			MenuBar menuBar = new MenuBar(true);
			menuBar.addItem(QuestionDef.TRUE_DISPLAY_VALUE,true, new SelectItemCommand(QuestionDef.TRUE_DISPLAY_VALUE,this));
			menuBar.addItem(QuestionDef.FALSE_DISPLAY_VALUE,true, new SelectItemCommand(QuestionDef.FALSE_DISPLAY_VALUE,this));
			
			popup = new PopupPanel(true,false);
			popup.setWidget(menuBar);
			popup.setPopupPosition(valueHyperlink.getAbsoluteLeft(), valueHyperlink.getAbsoluteTop());
		    popup.show();
		}
		else if( (questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE) &&
				(operator == PurcConstants.OPERATOR_EQUAL || operator == PurcConstants.OPERATOR_NOT_EQUAL) ){
			
			MenuBar menuBar = new MenuBar(true);
			
			int size = 0, maxSize = 0; String text;
			Vector options = questionDef.getOptions();
			for(int i=0; i<options.size(); i++){
				OptionDef optionDef = (OptionDef)options.elementAt(i);
				text = optionDef.getText();
				size = text.length();
				if(maxSize < size)
					maxSize = size;
				menuBar.addItem(text,true, new SelectItemCommand(optionDef,this));
			}
			
			/*ScrollPanel scrollPanel = new ScrollPanel();
			scrollPanel.setWidget(menuBar);
			scrollPanel.setHeight("200px");
			scrollPanel.setWidth((maxSize*11)+"px");*/
			
			int height = questionDef.getOptions().size()*40;
			if(height > 200)
				height = 200;
			
			ScrollPanel scrollPanel = new ScrollPanel();
			scrollPanel.setWidget(menuBar);
			scrollPanel.setHeight(height+"px"); //"200px"
			scrollPanel.setWidth((maxSize*12)+"px");
			
			popup = new PopupPanel(true,false);
			popup.setWidget(scrollPanel);
			popup.setPopupPosition(valueHyperlink.getAbsoluteLeft(), valueHyperlink.getAbsoluteTop());
		    popup.show();
		}
		else if( (questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE) &&
				(operator == PurcConstants.OPERATOR_IN_LIST || operator == PurcConstants.OPERATOR_NOT_IN_LIST) ){
			
			String values = valueHyperlink.getText();
			String[] vals = null;
			if(!values.equals(EMPTY_VALUE))
				vals = values.split(LIST_SEPARATOR);
			
			int size = 0, maxSize = 0; String text;
			VerticalPanel panel = new VerticalPanel();
			Vector options = questionDef.getOptions();
			for(int i=0; i<options.size(); i++){
				OptionDef optionDef = (OptionDef)options.elementAt(i);
				
				text = optionDef.getText();
				size = text.length();
				if(maxSize < size)
					maxSize = size;
				
				CheckBox checkbox = new CheckBox(text);
				if(InArray(vals,text))
					checkbox.setChecked(true);
				panel.add(checkbox);
			}
			
			int height = questionDef.getOptions().size()*40;
			if(height > 200)
				height = 200;
			
			ScrollPanel scrollPanel = new ScrollPanel();
			scrollPanel.setWidget(panel);
			scrollPanel.setHeight(height+"px"); //"200px"
			scrollPanel.setWidth((maxSize*12)+"px");
			
			popup = new PopupPanel(true,false);
			popup.addPopupListener(this);
			popup.setWidget(scrollPanel);
			popup.setPopupPosition(valueHyperlink.getAbsoluteLeft(), valueHyperlink.getAbsoluteTop());
		    popup.show();
		}
		else{
			//horizontalPanel.remove(valueHyperlink);
			//horizontalPanel.add(txtValue1);
			
			//if(!valueHyperlink.getText().equals(EMPTY_VALUE))
			//	txtValue1.setText(valueHyperlink.getText());
			
			txtValue1.removeKeyboardListener(keyboardListener1);
			txtValue2.removeKeyboardListener(keyboardListener2);
			
			if(questionDef.getDataType() ==  QuestionDef.QTN_TYPE_DATE){
				txtValue1 = new DatePickerWidget();
				txtValue2 = new DatePickerWidget();
			}
			else{
				txtValue1 = new TextBox();
				txtValue2 = new TextBox();
			}
			
			setupTextListeners();
			
			horizontalPanel.remove(valueHyperlink);
			horizontalPanel.add(txtValue1);
			
			if(!valueHyperlink.getText().equals(EMPTY_VALUE) && (prevQuestionDef == questionDef || prevQuestionDef == null))
				txtValue1.setText(valueHyperlink.getText());
				
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_NUMERIC || questionDef.getDataType() == QuestionDef.QTN_TYPE_DECIMAL){
				keyboardListener1 = FormDesignerUtil.getAllowNumericOnlyKeyboardListener(txtValue1, questionDef.getDataType() == QuestionDef.QTN_TYPE_NUMERIC ? false : true);
				keyboardListener2 = FormDesignerUtil.getAllowNumericOnlyKeyboardListener(txtValue2, questionDef.getDataType() == QuestionDef.QTN_TYPE_NUMERIC ? false : true);
				
				txtValue1.addKeyboardListener(keyboardListener1);
				txtValue2.addKeyboardListener(keyboardListener2);
			}
			
			txtValue1.setFocus(true);
			txtValue1.setFocus(true);
			
			if(operator ==  PurcConstants.OPERATOR_BETWEEN ||
					operator ==  PurcConstants.OPERATOR_NOT_BETWEEN){
				horizontalPanel.add(lblAnd);
				horizontalPanel.add(txtValue2);
				
				String val = txtValue1.getText();
				if(val.contains(BETWEEN_VALUE_SEPARATOR)){
					int pos = val.indexOf(BETWEEN_VALUE_SEPARATOR);
					String s = val.substring(0, pos);
					if(s.equals(EMPTY_VALUE))
						s = "";
					txtValue1.setText(s);
				    if(pos+BETWEEN_VALUE_SEPARATOR.length() != val.length()){
				    	pos = pos + BETWEEN_VALUE_SEPARATOR.length();
				    	s = val.substring(pos,val.length());
				    	if(s.equals(EMPTY_VALUE)){
							s = "";
							if(txtValue1.getText().trim().length() > 0)
								txtValue2.setFocus(true);
				    	}
				    	txtValue2.setText(s);
				    }
				    else
				    	txtValue2.setText("");
				}
			}
		}
	}
	
	private boolean InArray(String[] array, String item){
		if(array == null)
			return false;
		
		for(int i=0; i<array.length; i++){
			if(array[i].equals(item))
				return true;
		}
		return false;
	}
	
	public void stopEdit(boolean updateValue){
		String val1 = txtValue1.getText();
		if(val1.trim().length() == 0){
			val1 = EMPTY_VALUE;
			if(txtValue1 instanceof DatePicker)
				return;
		}
		
		String val2 = txtValue2.getText();
		if(val2.trim().length() == 0){
			val2 = EMPTY_VALUE;
			if(txtValue2 instanceof DatePicker)
				return;
		}
		
		String val = val1 + 
		((operator == PurcConstants.OPERATOR_BETWEEN || 
				operator == PurcConstants.OPERATOR_NOT_BETWEEN) ? (BETWEEN_VALUE_SEPARATOR + val2 ): "");
		
		if(updateValue)
			valueHyperlink.setText(val);
		
		horizontalPanel.remove(txtValue1);
		horizontalPanel.remove(txtValue2);
		horizontalPanel.remove(lblAnd);
		horizontalPanel.add(valueHyperlink);
	}
	
	public void onItemSelected(Object sender, Object item) {
		if(sender instanceof SelectItemCommand){
			popup.hide();
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
					questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE)
				valueHyperlink.setText(((OptionDef)item).getText());
			else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_BOOLEAN)
				valueHyperlink.setText((String)item);
		}
	}
	
	public void onStartItemSelection(Object sender){
		
	}
	
	public void onPopupClosed(PopupPanel sender, boolean autoClosed){
		String value = "";
		VerticalPanel panel = (VerticalPanel)popup.getWidget();
		int count = panel.getWidgetCount();
		for(int i=0; i<count; i++){
			CheckBox checkbox = (CheckBox)panel.getWidget(i);
			if(checkbox.isChecked()){
				if(value.length() > 0)
					value += LIST_SEPARATOR;
				value += checkbox.getText();
			}
		}
		if(value.length() == 0)
			value = EMPTY_VALUE;
		valueHyperlink.setText(value);
	}
	
	public String getValue(){
		String val = valueHyperlink.getText();
		if(val.equals(EMPTY_VALUE))
			return null;
		
		if(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE){
			OptionDef optionDef = questionDef.getOptionWithText(val);
			if(optionDef != null)
				val = optionDef.getVariableName();
			else
				val = null;
		}
		else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE){
			String[] options = val.split(LIST_SEPARATOR);
			if(options == null || options.length == 0)
				val = null;
			else{
				val = "";
				for(int i=0; i<options.length; i++){
					OptionDef optionDef = questionDef.getOptionWithText(options[i]);
					if(optionDef != null){
						if(val.length() > 0)
							val += LIST_SEPARATOR;
						val += optionDef.getVariableName();
					}
				}
			}
		}
		
		return val;
	}
	
	public void setValue(String value){
		String sValue = value;
		if(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE){
			OptionDef optionDef = ((OptionDef)questionDef.getOptionWithValue(value));
			if(optionDef != null)
				sValue = optionDef.getText();
		}
		else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE){
			String[] options = sValue.split(LIST_SEPARATOR);
			if(options == null || options.length == 0)
				sValue = null;
			else{
				sValue = "";
				for(int i=0; i<options.length; i++){
					OptionDef optionDef = questionDef.getOptionWithValue(options[i]);
					if(optionDef != null){
						if(sValue.length() > 0)
							sValue += LIST_SEPARATOR;
						sValue += optionDef.getText();
					}
				}
			}
		}
		valueHyperlink.setText(sValue);
	}
}
