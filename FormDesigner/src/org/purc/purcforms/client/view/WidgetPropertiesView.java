package org.purc.purcforms.client.view;

import java.util.List;

import org.purc.purcforms.client.controller.IFormSelectionListener;
import org.purc.purcforms.client.controller.WidgetSelectionListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.OptionDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.util.StyleUtil;
import org.purc.purcforms.client.widget.DesignGroupWidget;
import org.purc.purcforms.client.widget.DesignWidgetWrapper;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusListenerAdapter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestionEvent;
import com.google.gwt.user.client.ui.SuggestionHandler;
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;


/**
 * Contains the model of the displayed XForms document.
 * 
 * @author daniel
 *
 */
public class WidgetPropertiesView extends Composite implements WidgetSelectionListener, IFormSelectionListener{

	private FlexTable table = new FlexTable(); //Grid(7,2);
	private ScrollPanel scrollPanel = new ScrollPanel();
	private DesignGroupView viewWidget;
	private DesignWidgetWrapper widget;
	private DesignWidgetWrapper prevWidget;
	private String prevBinding;

	private TextBox txtText = new TextBox();
	private TextBox txtBinding = new TextBox();
	private TextBox txtChildBinding = new TextBox();
	private CheckBox chkEnabled = new CheckBox();
	private CheckBox chkVisible = new CheckBox();
	private TextBox txtWidth = new TextBox();
	private TextBox txtHeight = new TextBox();
	private TextBox txtLeft = new TextBox();
	private TextBox txtTop = new TextBox();
	private TextBox txtHelpText = new TextBox();
	private SuggestBox sgstBinding = new SuggestBox(new MultiWordSuggestOracle(),txtBinding);
	private SuggestBox sgstChildBinding = new SuggestBox(new MultiWordSuggestOracle(),txtChildBinding);
	private TextBox txtTabIndex = new TextBox();

	private TextBox txtForeColor  = new TextBox();
	private TextBox txtBackgroundColor  = new TextBox();
	private TextBox txtBorderColor  = new TextBox();
	private SuggestBox sgstForeColor = new SuggestBox(new MultiWordSuggestOracle(),txtForeColor);
	private SuggestBox sgstBackgroundColor;
	private SuggestBox sgstBorderColor;
	private ListBox lbFontWeight = new ListBox(false);
	private ListBox lbFontStyle = new ListBox(false);
	private TextBox txtFontSize= new TextBox();
	private TextBox txtFontFamily= new TextBox();
	private ListBox lbTextDecoration = new ListBox(false);
	private ListBox lbTextAlign = new ListBox(false);
	private ListBox lbBorderStyle = new ListBox(false);
	private TextBox txtBorderWidth = new TextBox();
	private ListBox cbRepeat = new ListBox(false);
	private TextBox txtExternalSource = new TextBox();
	private TextBox txtDisplayField = new TextBox();
	private TextBox txtValueField = new TextBox();

	private FormDef formDef;
	QuestionDef questionDef;

	public WidgetPropertiesView() {

		initStyles();

		int index = -1;
		table.setWidget(++index, 0, new Label(LocaleText.get("text")));
		table.setWidget(++index, 0, new Label(LocaleText.get("toolTip")));
		table.setWidget(++index, 0, new Label(LocaleText.get("binding")));
		table.setWidget(++index, 0, new Label(LocaleText.get("childBinding")));
		table.setWidget(++index, 0, new Label(LocaleText.get("width")));
		table.setWidget(++index, 0, new Label(LocaleText.get("height")));
		table.setWidget(++index, 0, new Label(LocaleText.get("enabled")));
		table.setWidget(++index, 0, new Label(LocaleText.get("visible")));
		table.setWidget(++index, 0, new Label(LocaleText.get("left")));
		table.setWidget(++index, 0, new Label(LocaleText.get("top")));
		table.setWidget(++index, 0, new Label(LocaleText.get("tabIndex")));
		table.setWidget(++index, 0, new Label(LocaleText.get("repeat")));

		table.setWidget(++index, 0, new Label(LocaleText.get("externalSource")));
		table.setWidget(++index, 0, new Label(LocaleText.get("displayField")));
		table.setWidget(++index, 0, new Label(LocaleText.get("valueField")));

		table.setWidget(++index, 0, new Label(LocaleText.get("fontFamily")));
		table.setWidget(++index, 0, new Label(LocaleText.get("foreColor")));
		table.setWidget(++index, 0, new Label(LocaleText.get("fontWeight")));
		table.setWidget(++index, 0, new Label(LocaleText.get("fontStyle")));
		table.setWidget(++index, 0, new Label(LocaleText.get("fontSize")));
		table.setWidget(++index, 0, new Label(LocaleText.get("textDecoration")));
		table.setWidget(++index, 0, new Label(LocaleText.get("textAlign")));
		table.setWidget(++index, 0, new Label(LocaleText.get("backgroundColor")));
		table.setWidget(++index, 0, new Label(LocaleText.get("borderStyle")));
		table.setWidget(++index, 0, new Label(LocaleText.get("borderWidth")));
		table.setWidget(++index, 0, new Label(LocaleText.get("borderColor")));

		index = -1;
		table.setWidget(++index, 1,txtText );
		table.setWidget(++index, 1,txtHelpText );
		table.setWidget(++index, 1, sgstBinding);
		table.setWidget(++index, 1, sgstChildBinding);
		table.setWidget(++index, 1,txtWidth);
		table.setWidget(++index, 1,txtHeight);
		table.setWidget(++index, 1, chkEnabled);
		table.setWidget(++index, 1, chkVisible);
		table.setWidget(++index, 1, txtLeft);
		table.setWidget(++index, 1, txtTop);
		table.setWidget(++index, 1, txtTabIndex);
		table.setWidget(++index, 1, cbRepeat);

		table.setWidget(++index, 1, txtExternalSource);
		table.setWidget(++index, 1, txtDisplayField);
		table.setWidget(++index, 1, txtValueField);

		table.setWidget(++index, 1, txtFontFamily);
		table.setWidget(++index, 1, sgstForeColor);
		table.setWidget(++index, 1, lbFontWeight);
		table.setWidget(++index, 1, lbFontStyle);
		table.setWidget(++index, 1, txtFontSize);
		table.setWidget(++index, 1, lbTextDecoration);
		table.setWidget(++index, 1, lbTextAlign);
		table.setWidget(++index, 1, sgstBackgroundColor);
		table.setWidget(++index, 1, lbBorderStyle);
		table.setWidget(++index, 1, txtBorderWidth);
		table.setWidget(++index, 1, sgstBorderColor);

		txtText.setWidth("100%");
		txtHelpText.setWidth("100%");
		txtChildBinding.setWidth("100%");
		txtBinding.setWidth("100%");
		txtWidth.setWidth("100%");
		txtHeight.setWidth("100%");
		txtLeft.setWidth("100%");
		txtTop.setWidth("100%");
		sgstChildBinding.setWidth("100%");
		sgstBinding.setWidth("100%");
		txtTabIndex.setWidth("100%");
		cbRepeat.setWidth("100%");
		txtExternalSource.setWidth("100%");
		txtDisplayField.setWidth("100%");
		txtValueField.setWidth("100%");

		sgstForeColor.setWidth("100%");
		lbFontWeight.setWidth("100%");
		lbFontStyle.setWidth("100%");
		txtFontSize.setWidth("100%");
		txtFontFamily.setWidth("100%");
		lbTextDecoration.setWidth("100%");
		lbTextAlign.setWidth("100%");
		sgstBackgroundColor.setWidth("100%");
		lbBorderStyle.setWidth("100%");
		txtBorderWidth.setWidth("100%");
		sgstBorderColor.setWidth("100%");

		table.setStyleName("cw-FlexTable");
		table.setWidth("100%");
		FlexCellFormatter cellFormatter = table.getFlexCellFormatter();
		cellFormatter.setWidth(0, 0, "30%");

		for(int i=0; i<table.getRowCount(); i++)
			cellFormatter.setHorizontalAlignment(i, 0, HasHorizontalAlignment.ALIGN_RIGHT);

		scrollPanel.setWidget(table);
		initWidget(scrollPanel);
		setupEvents();
		txtChildBinding.setEnabled(false);

		FormDesignerUtil.allowNumericOnly(txtWidth,false);
		FormDesignerUtil.allowNumericOnly(txtHeight,false);
		FormDesignerUtil.allowNumericOnly(txtLeft,false);
		FormDesignerUtil.allowNumericOnly(txtTop,false);
		FormDesignerUtil.allowNumericOnly(txtTabIndex,false);

		enableLabelProperties(false);

		cbRepeat.addItem("true");
		cbRepeat.addItem("false");
	}

	private void initStyles(){
		StyleUtil.loadColorNames((MultiWordSuggestOracle)sgstForeColor.getSuggestOracle());
		sgstBackgroundColor = new SuggestBox(sgstForeColor.getSuggestOracle(),txtBackgroundColor);
		sgstBorderColor = new SuggestBox(sgstForeColor.getSuggestOracle(),txtBorderColor);

		StyleUtil.loadFontWeights(lbFontWeight);
		StyleUtil.loadFontStyles(lbFontStyle);
		StyleUtil.loadTextDecoration(lbTextDecoration);
		StyleUtil.loadTextAlign(lbTextAlign);
		StyleUtil.loadBorderStyles(lbBorderStyle);
	}

	private void setupEvents(){
		txtText.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				updateText();
			}
		});

		txtText.addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				updateText();
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
		});

		txtWidth.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				updateWidth();
			}
		});
		txtWidth.addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				updateWidth();
			}
		});

		txtHeight.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				updateHeight();
			}
		});
		txtHeight.addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				updateHeight();
			}
		});

		txtLeft.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				updateLeft();
			}
		});
		txtLeft.addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				updateLeft();
			}
		});

		txtTop.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				updateTop();
			}
		});
		txtTop.addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				updateTop();
			}
		});

		txtBinding.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				updateBinding(widget,null);
			}
		});

		txtChildBinding.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				if(txtChildBinding.getText().trim().length() == 0)
					updateChildBinding();
			}
		});

		txtChildBinding.addFocusListener(new FocusListenerAdapter(){
			public void onFocus(Widget sender){
				txtChildBinding.selectAll();
			}
		});

		txtBinding.addFocusListener(new FocusListenerAdapter(){
			public void onFocus(Widget sender){
				txtBinding.selectAll();
			}
			public void onLostFocus(Widget sender){
				updateBinding(prevWidget,prevBinding);
			}
		});

		sgstBinding.addEventHandler(new SuggestionHandler(){
			public void onSuggestionSelected(SuggestionEvent event){
				updateBinding();
			}
		});

		sgstChildBinding.addEventHandler(new SuggestionHandler(){
			public void onSuggestionSelected(SuggestionEvent event){
				updateChildBinding();
			}
		});

		txtTabIndex.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				updateTabIndex();
			}
		});

		txtTabIndex.addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				updateTabIndex();
			}
		});

		txtExternalSource.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				updateExternalSource();
			}
		});

		cbRepeat.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				updateIsRepeat();
			}
		});

		txtExternalSource.addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				updateExternalSource();
			}
		});

		txtDisplayField.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				updateDisplayField();
			}
		});

		txtDisplayField.addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				updateDisplayField();
			}
		});

		txtValueField.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				updateValueField();
			}
		});

		txtValueField.addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				updateValueField();
			}
		});


		txtForeColor.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				if(widget != null)
					widget.setForeColor(txtForeColor.getText());
			}
		});
		sgstForeColor.addEventHandler(new SuggestionHandler(){
			public void onSuggestionSelected(SuggestionEvent event){
				if(widget != null)
					widget.setForeColor(txtForeColor.getText());
			}
		});
		txtBackgroundColor.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				if(widget != null)
					widget.setBackgroundColor(txtBackgroundColor.getText());
				else if(viewWidget != null)
					viewWidget.setBackgroundColor(txtBackgroundColor.getText());
			}
		});
		sgstBackgroundColor.addEventHandler(new SuggestionHandler(){
			public void onSuggestionSelected(SuggestionEvent event){
				if(widget != null)
					widget.setBackgroundColor(txtBackgroundColor.getText());
				else if(viewWidget != null)
					viewWidget.setBackgroundColor(txtBackgroundColor.getText());
			}
		});
		txtBorderColor.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				if(widget != null)
					widget.setBorderColor(txtBorderColor.getText());
				else if(viewWidget != null && viewWidget instanceof DesignGroupWidget)
					((DesignWidgetWrapper)viewWidget.getParent().getParent()).setBorderColor(txtBorderColor.getText());
			}
		});
		sgstBorderColor.addEventHandler(new SuggestionHandler(){
			public void onSuggestionSelected(SuggestionEvent event){
				if(widget != null)
					widget.setBorderColor(txtBorderColor.getText());
				else if(viewWidget != null && viewWidget instanceof DesignGroupWidget)
					((DesignWidgetWrapper)viewWidget.getParent().getParent()).setBorderColor(txtBorderColor.getText());
			}
		});
		txtFontSize.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				if(widget != null)
					widget.setFontSize(txtFontSize.getText());
			}
		});
		txtFontSize.addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				if(widget != null)
					widget.setFontSize(txtFontSize.getText());
			}
		});
		txtFontFamily.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				if(widget != null)
					widget.setFontFamily(txtFontFamily.getText());
			}
		});
		txtFontFamily.addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				if(widget != null)
					widget.setFontFamily(txtFontFamily.getText());
			}
		});
		txtBorderWidth.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				if(widget != null)
					widget.setBorderWidth(txtBorderWidth.getText());
				else if(viewWidget != null && viewWidget instanceof DesignGroupWidget)
					((DesignWidgetWrapper)viewWidget.getParent().getParent()).setBorderWidth(txtBorderWidth.getText());
			}
		});
		txtBorderWidth.addKeyboardListener(new KeyboardListenerAdapter(){
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				if(widget != null)
					widget.setBorderWidth(txtBorderWidth.getText());
				else if(viewWidget != null && viewWidget instanceof DesignGroupWidget)
					((DesignWidgetWrapper)viewWidget.getParent().getParent()).setBorderWidth(txtBorderWidth.getText());
			}
		});
		lbTextDecoration.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				if(widget != null)
					widget.setTextDecoration(lbTextDecoration.getItemText(lbTextDecoration.getSelectedIndex()));
			}
		});
		lbTextAlign.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				if(widget != null)
					widget.setTextAlign(lbTextAlign.getItemText(lbTextAlign.getSelectedIndex()));
			}
		});
		lbFontStyle.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				if(widget != null)
					widget.setFontStyle(lbFontStyle.getItemText(lbFontStyle.getSelectedIndex()));
			}
		});
		lbFontWeight.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				if(widget != null)
					widget.setFontWeight(lbFontWeight.getItemText(lbFontWeight.getSelectedIndex()));
			}
		});
		lbBorderStyle.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				if(widget != null)
					widget.setBorderStyle(lbBorderStyle.getItemText(lbBorderStyle.getSelectedIndex()));
				else if(viewWidget != null && viewWidget instanceof DesignGroupWidget)
					((DesignWidgetWrapper)viewWidget.getParent().getParent()).setBorderStyle(lbBorderStyle.getItemText(lbBorderStyle.getSelectedIndex()));
			}
		});
	}

	private void updateBinding(DesignWidgetWrapper widget,String binding){
		if(widget != null){
			Widget wdgt = widget.getWrappedWidget();
			if(wdgt instanceof Label || wdgt instanceof Hyperlink || wdgt instanceof TabBar)
				widget.setBinding(binding == null ? sgstBinding.getText().trim() : binding);
			else if(txtBinding.getText().trim().length() == 0)
				updateBinding();
		}	
	}

	private void updateText(){
		//if(widget != null && txtText.getText().trim().length() > 0) //No setting of empty strings as text.
		if(widget != null /*&& txtText.getText().length() > 0*/) //We now allow setting of empty strings as text.
			widget.setText(txtText.getText());
	}

	private void updateHelpText(){
		if(widget != null)
			widget.setTitle(txtHelpText.getText());
	}

	private void updateExternalSource(){
		if(widget != null)
			widget.setExternalSource(txtExternalSource.getText());
	}

	private void updateDisplayField(){
		if(widget != null)
			widget.setDisplayField(txtDisplayField.getText());
	}

	private void updateValueField(){
		if(widget != null)
			widget.setValueField(txtValueField.getText());
	}

	private void updateIsRepeat(){
		if(widget != null)
			widget.setRepeated(cbRepeat.getSelectedIndex() == 0);
	}

	private void updateChildBinding(){
		if(widget != null){
			OptionDef optionDef = questionDef.getOptionWithText(txtChildBinding.getText());
			if(optionDef == null){
				String text = txtChildBinding.getText();
				//if("browse".equalsIgnoreCase(text) || "clear".equalsIgnoreCase(text))
				widget.setBinding(text);
				return;
			}

			widget.setBinding(optionDef.getVariableName());

			if(((widget.getWrappedWidget() instanceof RadioButton) && ((RadioButton)widget.getWrappedWidget()).getText().equals("RadioButton")) ||
					((widget.getWrappedWidget() instanceof CheckBox) && ((CheckBox)widget.getWrappedWidget()).getText().equals("CheckBox"))){
				txtText.setText(txtChildBinding.getText());
				updateText();
			}
			if(txtHelpText.getText().trim().length() == 0 || txtHelpText.getText().equals("CheckBox") || txtHelpText.getText().equals("RadioButton")){
				txtHelpText.setText(txtChildBinding.getText());
				updateHelpText();
			}
			/*String binding = null;
			if(hasParentBinding()){
				OptionDef optionDef = questionDef.getOptionWithText(txtBinding.getText());
				if(optionDef == null)
					return;
				binding = optionDef.getText();
			}
			else{*/			
		}
	}

	private void updateBinding(){
		txtChildBinding.setEnabled(false);
		if(widget != null && formDef != null){
			questionDef = formDef.getQuestionWithText(txtBinding.getText());
			if(questionDef == null){
				String text = txtBinding.getText();
				//if(text.equals("submit")||text.equals("addnew")||text.equals("remove")
				//		||text.equals("browse")||text.equals("clear"))
				widget.setBinding(text);
				return;
			}

			//widget.setBinding(formDef.getQuestionBinding(questionDef));


			if(hasParentBinding()){
				widget.setParentBinding(questionDef.getVariableName());
				updateQuestionOptionsOracle();
			}
			else{
				widget.setBinding(questionDef.getVariableName());
				widget.setQuestionDef(questionDef);

				if((widget.getWrappedWidget() instanceof Label) && ((Label)widget.getWrappedWidget()).getText().equals("Label")){
					txtText.setText(txtBinding.getText());
					updateText();
				}
				if(txtHelpText.getText().trim().length() == 0 || txtHelpText.getText().equals("Label")){
					txtHelpText.setText(txtBinding.getText());
					updateHelpText();
				}
			}
		}
	}

	private void updateWidth(){
		if(txtWidth.getText().trim().length() > 0){
			if(widget != null)
				widget.setWidth(txtWidth.getText()+"px");
			else if(viewWidget != null){
				if(viewWidget instanceof DesignSurfaceView)
					((DesignSurfaceView)viewWidget).setWidth(txtWidth.getText()+"px");
				else
					viewWidget.setWidth(txtWidth.getText()+"px");
			}
		}
	}

	private void updateHeight(){
		if(txtHeight.getText().trim().length() > 0){
			if(widget != null)
				widget.setHeight(txtHeight.getText()+"px");
			else if(viewWidget != null){
				if(viewWidget instanceof DesignSurfaceView)
					((DesignSurfaceView)viewWidget).setHeight(txtHeight.getText()+"px");
				else
					viewWidget.setHeight(txtHeight.getText()+"px");
			}
		}
	}

	private void updateLeft(){
		if(widget != null && txtLeft.getText().trim().length() > 0)
			widget.setLeft(txtLeft.getText()+"px");
	}

	private void updateTop(){
		if(widget != null && txtTop.getText().trim().length() > 0)
			widget.setTop(txtTop.getText()+"px");
	}

	private void updateTabIndex(){
		if(widget != null && txtTabIndex.getText().trim().length() > 0)
			widget.setTabIndex(Integer.parseInt(txtTabIndex.getText()));
		else if(viewWidget != null && viewWidget instanceof DesignGroupWidget)
			((DesignWidgetWrapper)viewWidget.getParent().getParent()).setTabIndex(Integer.parseInt(txtTabIndex.getText()));
	}

	public void onWidgetSelected(Widget widget, boolean multipleSel) {

		if(widget instanceof DesignWidgetWrapper){
			prevWidget = this.widget;
			prevBinding = sgstBinding.getText().trim();
			this.widget = (DesignWidgetWrapper)widget;
			viewWidget = null;
		}
		else{
			viewWidget = (DesignGroupView)widget;
			prevWidget = this.widget;
			this.widget = null;
		}

		//Removed from here for smooth updateing where value has not changed
		/*txtText.setText(null);
		txtHelpText.setText(null);
		txtBinding.setText(null);
		txtHeight.setText(null);
		txtWidth.setText(null);
		chkVisible.setChecked(false);
		chkEnabled.setChecked(false);
		sgstBinding.setText(null);
		txtTop.setText(null);
		txtLeft.setText(null);*/

		if(this.widget != null){
			if(this.widget.getWrappedWidget() instanceof TabBar)
				clearProperties();

			txtText.setText(this.widget.getText());
			txtBinding.setText(this.widget.getBinding());

			if(this.widget.getWrappedWidget() instanceof TabBar)
				return;

			String value = this.widget.getHeight();
			if(value != null && value.trim().length() > 0)
				txtHeight.setText(value.substring(0, value.length()-2));
			else
				txtHeight.setText(null);

			value = this.widget.getWidth();
			if(value != null && value.trim().length() > 0)
				txtWidth.setText(value.substring(0, value.length()-2));
			else
				txtWidth.setText(null);

			value = this.widget.getTitle();
			if(value != null && value.trim().length() > 0)
				txtHelpText.setText(value);
			else
				txtHelpText.setText(txtText.getText());

			value = this.widget.getExternalSource();
			if(value != null && value.trim().length() > 0)
				txtExternalSource.setText(value);
			else
				txtExternalSource.setText(null);

			value = this.widget.getDisplayField();
			if(value != null && value.trim().length() > 0)
				txtDisplayField.setText(value);
			else
				txtDisplayField.setText(null);

			value = this.widget.getValueField();
			if(value != null && value.trim().length() > 0)
				txtValueField.setText(value);
			else
				txtValueField.setText(null);

			cbRepeat.setSelectedIndex(this.widget.isRepeated() ? 0 : 1);

			txtChildBinding.setText(null);
			if(this.widget.getWrappedWidget() instanceof CheckBox || (this.widget.getWrappedWidget() instanceof Button &&
					"browse".equalsIgnoreCase(this.widget.getBinding())||"clear".equalsIgnoreCase(this.widget.getBinding()) ||
					"search".equalsIgnoreCase(this.widget.getBinding()))){
				value = this.widget.getParentBinding();
				if(value != null && value.trim().length() > 0 && formDef != null){
					questionDef = formDef.getQuestion(value);
					if(questionDef != null)
						sgstBinding.setText(questionDef.getText()); 
					else if(value != null && value.equals("submit") && this.widget.getWrappedWidget() instanceof Button)
						txtBinding.setText(value);
					else
						txtBinding.setText(null);
				}
				else
					txtBinding.setText(null);

				value = this.widget.getBinding();
				if(questionDef != null && value != null && value.trim().length() > 0){
					OptionDef optionDef = questionDef.getOptionWithValue(value);
					if(optionDef != null)
						sgstChildBinding.setText(optionDef.getText()); 
					else if(this.widget.getWrappedWidget() instanceof Button)
						sgstChildBinding.setText(value);
					else
						txtChildBinding.setText(null);
				}
				else
					txtChildBinding.setText(null);
			}
			else{
				value = this.widget.getBinding();
				if(formDef != null){
					questionDef = formDef.getQuestion(value);
					if(questionDef != null)
						txtBinding.setText(questionDef.getText());
					else{
						if("submit".equalsIgnoreCase(value)||"addnew".equalsIgnoreCase(value)||"remove".equalsIgnoreCase(value)
								|| "browse".equalsIgnoreCase(value) || "clear".equalsIgnoreCase(value) || "cancel".equalsIgnoreCase(value) ||
								(this.widget.getWrappedWidget() instanceof Label || this.widget.getWrappedWidget() instanceof Hyperlink) ||
								"search".equalsIgnoreCase(value) || this.widget.getWrappedWidget() instanceof TabBar)
							txtBinding.setText(value);
						else
							txtBinding.setText(null);
					}
				}
				else if(!(this.widget.getWrappedWidget() instanceof TabBar || this.widget.getWrappedWidget() instanceof Label))
					txtBinding.setText(null);
			}

			value = this.widget.getLeft();
			if(value != null && value.trim().length() > 0)
				txtLeft.setText(value.substring(0, value.length()-2));
			else
				txtLeft.setText(null);

			value = this.widget.getTop();
			if(value != null && value.trim().length() > 0)
				txtTop.setText(value.substring(0, value.length()-2));
			else
				txtTop.setText(null);

			txtTabIndex.setText(String.valueOf(this.widget.getTabIndex()));

			txtChildBinding.setEnabled((hasParentBinding() && this.sgstBinding.getText().trim().length() > 0));

			if(!txtChildBinding.isEnabled())
				txtChildBinding.setText(null);

			enableLabelProperties(this.widget.getWrappedWidget() instanceof Label);
		}
		else{
			clearProperties();
			setViewProperties();
		}
	}

	private void setViewProperties(){
		if(viewWidget != null){
			txtWidth.setText(String.valueOf(FormUtil.convertDimensionToInt(viewWidget.getWidth())));
			txtHeight.setText(String.valueOf(FormUtil.convertDimensionToInt(viewWidget.getHeight())));
			txtBackgroundColor.setText(viewWidget.getBackgroundColor());
			
			if(viewWidget instanceof DesignGroupWidget){
				DesignWidgetWrapper designWidgetWrapper = (DesignWidgetWrapper)viewWidget.getParent().getParent();
				
				StyleUtil.setBorderStyleIndex(designWidgetWrapper.getBorderStyle(), lbBorderStyle);
				txtBorderColor.setText(designWidgetWrapper.getBorderColor());
				txtBorderWidth.setText(FormUtil.convertDimensionToInt(designWidgetWrapper.getBorderWidth())+"");
				txtTabIndex.setText(designWidgetWrapper.getTabIndex()+"");
			}
		}
	}

	private void clearProperties(){
		txtText.setText(null);
		txtHelpText.setText(null);
		txtBinding.setText(null);
		txtHeight.setText(null);
		txtWidth.setText(null);
		chkVisible.setChecked(false);
		chkEnabled.setChecked(false);
		sgstBinding.setText(null);
		sgstChildBinding.setText(null);
		txtTop.setText(null);
		txtLeft.setText(null);
		txtTabIndex.setText(null);
		txtExternalSource.setText(null);
		txtDisplayField.setText(null);
		txtValueField.setText(null);
		cbRepeat.setSelectedIndex(-1);
		enableLabelProperties(false);
	}

	private boolean hasParentBinding(){
		return (widget.getWrappedWidget() instanceof RadioButton) || (widget.getWrappedWidget() instanceof CheckBox)
		|| (widget.getWrappedWidget() instanceof Button);
	}

	private void updateQuestionOptionsOracle(){
		MultiWordSuggestOracle oracle = (MultiWordSuggestOracle)sgstChildBinding.getSuggestOracle();
		oracle.clear();
		if(widget.getWrappedWidget() instanceof Button){
			oracle.add("browse");
			oracle.add("clear");
			oracle.add("search");
			txtChildBinding.setEnabled(true);
		}
		else{
			List options  = questionDef.getOptions();
			if(options != null){
				FormDesignerUtil.loadOptions(options,oracle);
				txtChildBinding.setEnabled(true);
			}
		}
	}

	public void setupFormDef(FormDef formDef){
		this.formDef = formDef;

		MultiWordSuggestOracle oracle = (MultiWordSuggestOracle)sgstBinding.getSuggestOracle();
		oracle.clear();
		for(int i=0; i<formDef.getPageCount(); i++)
			FormDesignerUtil.loadQuestions(formDef.getPageAt(i).getQuestions(),null,oracle,false);
		oracle.add("submit");
		oracle.add("addnew");
		oracle.add("remove");
		oracle.add("browse");
		oracle.add("clear");
		oracle.add("cancel");
		oracle.add("search");

		//sgstBinding.
		//table.remove(sgstBinding);
		//sgstBinding.r
		//sgstBinding = new SuggestBox(oracle,txtBinding);
		//table.setWidget(2, 1, sgstBinding);
		//selectFirstQuestion();



		/*sgstField.addFocusListener(new FocusListenerAdapter(){
			public void onLostFocus(Widget sender){
				stopSelection();
			}
		});*/
	}

	public void onFormItemSelected(Object formItem) {
		if(formItem == null){
			//enableQuestionOnlyProperties(false);
			//txtText.setEnabled(false);
			//txtDescTemplate.setEnabled(false);
			return;
		}

		if(formItem instanceof FormDef)
			setupFormDef((FormDef)formItem);
		/*else if(formItem instanceof PageDef)
			setPageProperties((PageDef)formItem);
		else if(formItem instanceof QuestionDef)
			setQuestionProperties((QuestionDef)formItem);
		else if(formItem instanceof OptionDef)
			setQuestionOptionProperties((OptionDef)formItem);*/
	}

	public void refresh(){
		setupFormDef(formDef);
	}

	private void enableLabelProperties(boolean enable){
		enable = true;

		txtForeColor.setEnabled(enable);
		lbFontWeight.setEnabled(enable);
		lbFontStyle.setEnabled(enable);
		txtFontSize.setEnabled(enable);
		txtFontFamily.setEnabled(enable);
		lbTextDecoration.setEnabled(enable);
		lbTextAlign.setEnabled(enable);
		txtBackgroundColor.setEnabled(enable);
		lbBorderStyle.setEnabled(enable);
		txtBorderWidth.setEnabled(enable);
		txtBorderColor.setEnabled(enable);

		if(!enable){
			txtForeColor.setText(null);
			lbFontWeight.setSelectedIndex(-1);
			lbFontStyle.setSelectedIndex(-1);
			txtFontSize.setText(null);
			txtFontFamily.setText(null);
			lbTextDecoration.setSelectedIndex(-1);
			lbTextAlign.setSelectedIndex(-1);
			txtBackgroundColor.setText(null);
			lbBorderStyle.setSelectedIndex(-1);
			txtBorderWidth.setText(null);
			txtBorderColor.setText(null);
		}
		else if(widget != null){
			txtForeColor.setText(widget.getForeColor());
			StyleUtil.setFontWeightIndex(widget.getFontWeight(), lbFontWeight);
			StyleUtil.setFontStyleIndex(widget.getFontStyle(), lbFontStyle);
			txtFontSize.setText(FormUtil.convertDimensionToInt(widget.getFontSize())+"");
			txtFontFamily.setText(widget.getFontFamily());
			StyleUtil.setTextDecorationIndex(widget.getTextDecoration(), lbTextDecoration);
			StyleUtil.setTextAlignIndex(widget.getTextAlign(), lbTextAlign);
			txtBackgroundColor.setText(widget.getBackgroundColor());
			StyleUtil.setBorderStyleIndex(widget.getBorderStyle(), lbBorderStyle);
			txtBorderWidth.setText(FormUtil.convertDimensionToInt(widget.getBorderWidth())+"");
			txtBorderColor.setText(widget.getBorderColor());
		}
		else if(widget == null){
			txtForeColor.setText(null);
			lbFontWeight.setSelectedIndex(-1);
			lbFontStyle.setSelectedIndex(-1);
			txtFontSize.setText(null);
			txtFontFamily.setText(null);
			lbTextDecoration.setSelectedIndex(-1);
			lbTextAlign.setSelectedIndex(-1);
			txtBackgroundColor.setText(null);
			lbBorderStyle.setSelectedIndex(-1);
			txtBorderWidth.setText(null);
			txtBorderColor.setText(null);
		}
	}
}
