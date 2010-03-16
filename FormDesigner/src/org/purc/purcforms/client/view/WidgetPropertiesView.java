package org.purc.purcforms.client.view;

import java.util.List;

import org.purc.purcforms.client.PurcConstants;
import org.purc.purcforms.client.controller.IFormSelectionListener;
import org.purc.purcforms.client.controller.WidgetPropertyChangeListener;
import org.purc.purcforms.client.controller.WidgetPropertySetter;
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

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;


/**
 * View responsible for displaying and hence allow editing of properties of the 
 * selected widget on the design surface.
 * 
 * @author daniel
 *
 */
public class WidgetPropertiesView extends Composite implements WidgetSelectionListener, IFormSelectionListener{

	/** Widget for organising widgets in a tabular format. */
	private FlexTable table = new FlexTable();

	/** The currently selected widget which derives from DesignGroupView */
	private DesignGroupView viewWidget;

	/** The currently selected widget whose properties we are displaying. */
	private DesignWidgetWrapper widget;

	/** The previously selected widget whose properties we had displayed before the current. */
	private DesignWidgetWrapper prevWidget;

	/** The binding for the previous widget. */
	private String prevBinding;

	/** Widget for setting the text property. */
	private TextBox txtText = new TextBox();

	/** Widget for setting the binding property. */
	private TextBox txtBinding = new TextBox();

	/** Widget for setting the child binding property. */
	private TextBox txtChildBinding = new TextBox();

	/** Widget for setting the enabled property. */
	private CheckBox chkEnabled = new CheckBox();

	/** Widget for setting the visible property. */
	private CheckBox chkVisible = new CheckBox();

	/** Widget for setting the width property. */
	private TextBox txtWidth = new TextBox();

	/** Widget for setting the height property. */
	private TextBox txtHeight = new TextBox();

	/** Widget for setting the left property. */
	private TextBox txtLeft = new TextBox();

	/** Widget for setting the top property. */
	private TextBox txtTop = new TextBox();

	/** Widget for setting the help text property. */
	private TextBox txtHelpText = new TextBox();

	/** Widget for setting the binding property. */
	private SuggestBox sgstBinding = new SuggestBox(new MultiWordSuggestOracle(),txtBinding);

	/** Widget for setting the child binding property. */
	private SuggestBox sgstChildBinding = new SuggestBox(new MultiWordSuggestOracle(),txtChildBinding);

	/** Widget for setting the tab index property. */
	private TextBox txtTabIndex = new TextBox();

	/** Widget for setting the fore colour property. */
	private TextBox txtForeColor  = new TextBox();

	/** Widget for setting the back ground colour property. */
	private TextBox txtBackgroundColor  = new TextBox();

	/** Widget for setting the border colour property. */
	private TextBox txtBorderColor  = new TextBox();

	/** Widget for setting the fore colour property. */
	private SuggestBox sgstForeColor = new SuggestBox(new MultiWordSuggestOracle(),txtForeColor);

	/** Widget for setting the background colour property. */
	private SuggestBox sgstBackgroundColor;

	/** Widget for setting the border colour property. */
	private SuggestBox sgstBorderColor;

	/** Widget for setting the font weight property. */
	private ListBox lbFontWeight = new ListBox(false);

	/** Widget for setting the font style property. */
	private ListBox lbFontStyle = new ListBox(false);

	/** Widget for setting the font size property. */
	private TextBox txtFontSize= new TextBox();

	/** Widget for setting the font family property. */
	private TextBox txtFontFamily= new TextBox();

	/** Widget for setting the text decoration property. */
	private ListBox lbTextDecoration = new ListBox(false);

	/** Widget for setting the text align property. */
	private ListBox lbTextAlign = new ListBox(false);

	/** Widget for setting the border style property. */
	private ListBox lbBorderStyle = new ListBox(false);

	/** Widget for setting the border width property. */
	private TextBox txtBorderWidth = new TextBox();

	/** Widget for setting the is repeat property. */
	private ListBox cbRepeat = new ListBox(false);

	/** Widget for setting the external source property. */
	private TextBox txtExternalSource = new TextBox();

	/** Widget for setting the display property. */
	private TextBox txtDisplayField = new TextBox();

	/** Widget for setting the value field property. */
	private TextBox txtValueField = new TextBox();
	
	/** Widget for setting the filter field property. */
	private TextBox txtFilterField = new TextBox();
	
	/** Widget for setting the id property. */
	private TextBox txtId= new TextBox();

	/** The current form definition object. */
	private FormDef formDef;

	/** The question definition object for the currently selected widget. */
	QuestionDef questionDef;

	private WidgetPropertyChangeListener widgetPropertyChangeListener;

	private boolean loadedBindings = false;
	

	/**
	 * Creates a new instance of the widget properties view.
	 */
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
		table.setWidget(++index, 0, new Label(LocaleText.get("filterField")));

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
		table.setWidget(++index, 0, new Label(LocaleText.get("id")));

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
		table.setWidget(++index, 1, txtFilterField);

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
		table.setWidget(++index, 1, txtId);

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
		txtFilterField.setWidth("100%");

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
		txtId.setWidth("100%");

		table.setStyleName("cw-FlexTable");
		table.setWidth("100%");
		FlexCellFormatter cellFormatter = table.getFlexCellFormatter();
		cellFormatter.setWidth(0, 0, "30%");

		for(int i=0; i<table.getRowCount(); i++)
			cellFormatter.setHorizontalAlignment(i, 0, HasHorizontalAlignment.ALIGN_RIGHT);

		initWidget(table);
		setupEvents();
		txtChildBinding.setEnabled(false);

		FormUtil.allowNumericOnly(txtWidth,false);
		FormUtil.allowNumericOnly(txtHeight,false);
		FormUtil.allowNumericOnly(txtLeft,false);
		FormUtil.allowNumericOnly(txtTop,false);
		FormUtil.allowNumericOnly(txtTabIndex,false);

		enableLabelProperties(false);

		cbRepeat.addItem("true");
		cbRepeat.addItem("false");
	}

	/**
	 * Initialises style property widgets.
	 */
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

	/**
	 * Sets up event listeners.
	 */
	private void setupEvents(){
		txtText.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				updateText();
			}
		});

		txtText.addKeyUpHandler(new KeyUpHandler(){
			public void onKeyUp(KeyUpEvent event) {
				updateText();
			}
		});

		txtHelpText.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				updateHelpText();
			}
		});
		txtHelpText.addKeyUpHandler(new KeyUpHandler(){
			public void onKeyUp(KeyUpEvent event) {
				updateHelpText();
			}
		});

		txtWidth.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				updateWidth();
			}
		});
		txtWidth.addKeyUpHandler(new KeyUpHandler(){
			public void onKeyUp(KeyUpEvent event) {
				updateWidth();
			}
		});

		txtHeight.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				updateHeight();
			}
		});
		txtHeight.addKeyUpHandler(new KeyUpHandler(){
			public void onKeyUp(KeyUpEvent event) {
				updateHeight();
			}
		});

		txtLeft.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				updateLeft();
			}
		});
		txtLeft.addKeyUpHandler(new KeyUpHandler(){
			public void onKeyUp(KeyUpEvent event) {
				updateLeft();
			}
		});

		txtTop.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				updateTop();
			}
		});
		txtTop.addKeyUpHandler(new KeyUpHandler(){
			public void onKeyUp(KeyUpEvent event) {
				updateTop();
			}
		});

		txtBinding.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				updateBinding(widget,null);
			}
		});

		txtChildBinding.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				if(txtChildBinding.getText().trim().length() == 0)
					updateChildBinding();
			}
		});

		txtChildBinding.addFocusHandler(new FocusHandler(){
			public void onFocus(FocusEvent event){
				txtChildBinding.selectAll();
			}
		});

		txtBinding.addFocusHandler(new FocusHandler(){
			public void onFocus(FocusEvent event){
				txtBinding.selectAll();
			}
			public void onLostFocus(Widget sender){
				updateBinding(prevWidget,prevBinding);
			}
		});

		sgstBinding.addSelectionHandler(new SelectionHandler(){
			public void onSelection(SelectionEvent event){
				updateBinding();
			}
		});

		sgstChildBinding.addSelectionHandler(new SelectionHandler(){
			public void onSelection(SelectionEvent event){
				updateChildBinding();
			}
		});

		txtTabIndex.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				updateTabIndex();
			}
		});

		txtTabIndex.addKeyUpHandler(new KeyUpHandler(){
			public void onKeyUp(KeyUpEvent event) {
				updateTabIndex();
			}
		});

		txtExternalSource.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				updateExternalSource();
			}
		});

		cbRepeat.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				updateIsRepeat();
			}
		});

		txtExternalSource.addKeyUpHandler(new KeyUpHandler(){
			public void onKeyUp(KeyUpEvent event) {
				updateExternalSource();
			}
		});

		txtDisplayField.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				updateDisplayField();
			}
		});

		txtDisplayField.addKeyUpHandler(new KeyUpHandler(){
			public void onKeyUp(KeyUpEvent event) {
				updateDisplayField();
			}
		});

		txtValueField.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				updateValueField();
			}
		});

		txtValueField.addKeyUpHandler(new KeyUpHandler(){
			public void onKeyUp(KeyUpEvent event) {
				updateValueField();
			}
		});
		
		txtFilterField.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				updateFilterField();
			}
		});

		txtFilterField.addKeyUpHandler(new KeyUpHandler(){
			public void onKeyUp(KeyUpEvent event) {
				updateFilterField();
			}
		});
		
		txtId.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				updateId();
			}
		});

		txtId.addKeyUpHandler(new KeyUpHandler(){
			public void onKeyUp(KeyUpEvent event) {
				updateId();
			}
		});



		txtForeColor.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				if(widget != null)
					widget.setForeColor(txtForeColor.getText());
				else
					widgetPropertyChangeListener.onWidgetPropertyChanged(WidgetPropertySetter.PROP_FORE_COLOR, txtForeColor.getText());
			}
		});
		sgstForeColor.addSelectionHandler(new SelectionHandler(){
			public void onSelection(SelectionEvent event){
				if(widget != null)
					widget.setForeColor(txtForeColor.getText());
				else
					widgetPropertyChangeListener.onWidgetPropertyChanged(WidgetPropertySetter.PROP_FORE_COLOR, txtForeColor.getText());
			}
		});
		txtBackgroundColor.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				if(widget != null)
					widget.setBackgroundColor(txtBackgroundColor.getText());
				else if(viewWidget != null)
					viewWidget.setBackgroundColor(txtBackgroundColor.getText());
				else
					widgetPropertyChangeListener.onWidgetPropertyChanged(WidgetPropertySetter.PROP_BACKGROUND_COLOR, txtBackgroundColor.getText());
			}
		});
		sgstBackgroundColor.addSelectionHandler(new SelectionHandler(){
			public void onSelection(SelectionEvent event){
				if(widget != null)			
					widget.setBackgroundColor(txtBackgroundColor.getText());
				else if(viewWidget != null)
					viewWidget.setBackgroundColor(txtBackgroundColor.getText());
				else
					widgetPropertyChangeListener.onWidgetPropertyChanged(WidgetPropertySetter.PROP_BACKGROUND_COLOR, txtBackgroundColor.getText());
			}
		});
		txtBorderColor.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				if(widget != null)	
					widget.setBorderColor(txtBorderColor.getText());
				else if(viewWidget != null && viewWidget instanceof DesignGroupWidget)
					((DesignWidgetWrapper)viewWidget.getParent().getParent()).setBorderColor(txtBorderColor.getText());
				else
					widgetPropertyChangeListener.onWidgetPropertyChanged(WidgetPropertySetter.PROP_BORDER_COLOR, txtBorderColor.getText());
			}
		});
		sgstBorderColor.addSelectionHandler(new SelectionHandler(){
			public void onSelection(SelectionEvent event){
				if(widget != null)
					widget.setBorderColor(txtBorderColor.getText());
				else if(viewWidget != null && viewWidget instanceof DesignGroupWidget)
					((DesignWidgetWrapper)viewWidget.getParent().getParent()).setBorderColor(txtBorderColor.getText());
				else
					widgetPropertyChangeListener.onWidgetPropertyChanged(WidgetPropertySetter.PROP_BORDER_COLOR, txtBorderColor.getText());
			}
		});
		txtFontSize.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				if(widget != null)
					widget.setFontSize(txtFontSize.getText()+PurcConstants.UNITS);
				else
					widgetPropertyChangeListener.onWidgetPropertyChanged(WidgetPropertySetter.PROP_FONT_SIZE, txtFontSize.getText()+PurcConstants.UNITS);
			}
		});
		txtFontSize.addKeyUpHandler(new KeyUpHandler(){
			public void onKeyUp(KeyUpEvent event) {
				if(widget != null)
					widget.setFontSize(txtFontSize.getText()+PurcConstants.UNITS);
				else
					widgetPropertyChangeListener.onWidgetPropertyChanged(WidgetPropertySetter.PROP_FONT_SIZE, txtFontSize.getText()+PurcConstants.UNITS);
			}
		});
		txtFontFamily.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				if(widget != null)
					widget.setFontFamily(txtFontFamily.getText());
				else
					widgetPropertyChangeListener.onWidgetPropertyChanged(WidgetPropertySetter.PROP_FONT_FAMILY, txtFontFamily.getText());
			}
		});
		txtFontFamily.addKeyUpHandler(new KeyUpHandler(){
			public void onKeyUp(KeyUpEvent event) {
				if(widget != null)
					widget.setFontFamily(txtFontFamily.getText());
				else
					widgetPropertyChangeListener.onWidgetPropertyChanged(WidgetPropertySetter.PROP_FONT_FAMILY, txtFontFamily.getText());
			}
		});
		txtBorderWidth.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				if(widget != null)	
					widget.setBorderWidth(txtBorderWidth.getText());
				else if(viewWidget != null && viewWidget instanceof DesignGroupWidget)
					((DesignWidgetWrapper)viewWidget.getParent().getParent()).setBorderWidth(txtBorderWidth.getText());
				else
					widgetPropertyChangeListener.onWidgetPropertyChanged(WidgetPropertySetter.PROP_BORDER_WIDTH, txtBorderWidth.getText());
			}
		});
		txtBorderWidth.addKeyUpHandler(new KeyUpHandler(){
			public void onKeyUp(KeyUpEvent event) {
				if(widget != null)	
					widget.setBorderWidth(txtBorderWidth.getText());
				else if(viewWidget != null && viewWidget instanceof DesignGroupWidget)
					((DesignWidgetWrapper)viewWidget.getParent().getParent()).setBorderWidth(txtBorderWidth.getText());
				else
					widgetPropertyChangeListener.onWidgetPropertyChanged(WidgetPropertySetter.PROP_BORDER_WIDTH, txtBorderWidth.getText());
			}
		});
		lbTextDecoration.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				if(widget != null)
					widget.setTextDecoration(lbTextDecoration.getItemText(lbTextDecoration.getSelectedIndex()));
				else
					widgetPropertyChangeListener.onWidgetPropertyChanged(WidgetPropertySetter.PROP_TEXT_DECORATION, lbTextDecoration.getItemText(lbTextDecoration.getSelectedIndex()));
			}
		});
		lbTextAlign.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				if(widget != null)
					widget.setTextAlign(lbTextAlign.getItemText(lbTextAlign.getSelectedIndex()));
				else
					widgetPropertyChangeListener.onWidgetPropertyChanged(WidgetPropertySetter.PROP_TEXT_ALIGN, lbTextAlign.getItemText(lbTextAlign.getSelectedIndex()));
			}
		});
		lbFontStyle.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				if(widget != null)
					widget.setFontStyle(lbFontStyle.getItemText(lbFontStyle.getSelectedIndex()));
				else
					widgetPropertyChangeListener.onWidgetPropertyChanged(WidgetPropertySetter.PROP_FONT_STYLE, lbFontStyle.getItemText(lbFontStyle.getSelectedIndex()));
			}
		});
		lbFontWeight.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				if(widget != null)
					widget.setFontWeight(lbFontWeight.getItemText(lbFontWeight.getSelectedIndex()));
				else
					widgetPropertyChangeListener.onWidgetPropertyChanged(WidgetPropertySetter.PROP_FONT_WEIGHT, lbFontWeight.getItemText(lbFontWeight.getSelectedIndex()));
			}
		});
		lbBorderStyle.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				if(widget != null)
					widget.setBorderStyle(lbBorderStyle.getItemText(lbBorderStyle.getSelectedIndex()));
				else if(viewWidget != null && viewWidget instanceof DesignGroupWidget)
					((DesignWidgetWrapper)viewWidget.getParent().getParent()).setBorderStyle(lbBorderStyle.getItemText(lbBorderStyle.getSelectedIndex()));
				else
					widgetPropertyChangeListener.onWidgetPropertyChanged(WidgetPropertySetter.PROP_BORDER_STYLE, lbBorderStyle.getItemText(lbBorderStyle.getSelectedIndex()));
			}
		});
	}

	/**
	 * 
	 * @param widget
	 * @param binding
	 */
	private void updateBinding(DesignWidgetWrapper widget,String binding){
		if(widget != null){
			Widget wdgt = widget.getWrappedWidget();
			if(wdgt instanceof Label || wdgt instanceof Hyperlink || wdgt instanceof TabBar)
				widget.setBinding(binding == null ? sgstBinding.getText().trim() : binding);
			else if(txtBinding.getText().trim().length() == 0)
				updateBinding();
		}	
	}

	/**
	 * Updates the selected widget with the new text as typed by the user.
	 */
	private void updateText(){
		//if(widget != null && txtText.getText().trim().length() > 0) //No setting of empty strings as text.
		if(widget != null /*&& txtText.getText().length() > 0*/) //We now allow setting of empty strings as text.
			widget.setText(txtText.getText());
	}

	/**
	 * Updates the selected widget with the new help text as typed by the user.
	 */
	private void updateHelpText(){
		if(widget != null)
			widget.setTitle(txtHelpText.getText());
	}

	/**
	 * Updates the selected widget with the new external source value as typed by the user.
	 */
	private void updateExternalSource(){
		if(widget != null)
			widget.setExternalSource(txtExternalSource.getText());
	}

	/**
	 * Updates the selected widget with the new display field as typed by the user.
	 */
	private void updateDisplayField(){
		if(widget != null)
			widget.setDisplayField(txtDisplayField.getText());
	}

	/**
	 * Updates the selected widget with the new value field as typed by the user.
	 */
	private void updateValueField(){
		if(widget != null)
			widget.setValueField(txtValueField.getText());
	}
	
	/**
	 * Updates the selected widget with the new filter field as typed by the user.
	 */
	private void updateFilterField(){
		if(widget != null)
			widget.setFilterField(txtFilterField.getText());
	}
	
	/**
	 * Updates the selected widget with the new id as typed by the user.
	 */
	private void updateId(){
		if(widget != null)
			widget.setId(txtId.getText());
	}

	/**
	 * Updates the selected widget with the new isRepeat value as ticked by the user.
	 */
	private void updateIsRepeat(){
		if(widget != null)
			widget.setRepeated(cbRepeat.getSelectedIndex() == 0);
	}

	/**
	 * Updates the selected widget with the new child binding as typed by the user.
	 */
	private void updateChildBinding(){
		if(widget != null){
			OptionDef optionDef = questionDef.getOptionWithText(txtChildBinding.getText());
			if(optionDef == null){
				widget.setBinding(txtChildBinding.getText());
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
		}
	}

	/**
	 * Updates the selected widget with the new binding as typed by the user.
	 */
	private void updateBinding(){
		txtChildBinding.setEnabled(false);
		if(widget != null && formDef != null){
			questionDef = formDef.getQuestionWithText(txtBinding.getText());
			if(questionDef == null){
				String text = txtBinding.getText();
				widget.setBinding(text);
				return;
			}

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

	/**
	 * Updates the selected widget with the new width as typed by the user.
	 */
	private void updateWidth(){
		if(txtWidth.getText().trim().length() > 0){
			if(widget != null)		
				widget.setWidth(txtWidth.getText()+PurcConstants.UNITS);
			else if(viewWidget != null){
				if(viewWidget instanceof DesignSurfaceView)
					((DesignSurfaceView)viewWidget).setWidth(txtWidth.getText()+PurcConstants.UNITS);
				else
					viewWidget.setWidth(txtWidth.getText()+PurcConstants.UNITS);
			}
			else
				widgetPropertyChangeListener.onWidgetPropertyChanged(WidgetPropertySetter.PROP_WIDTH, txtWidth.getText()+PurcConstants.UNITS);
		}
	}

	/**
	 * Updates the selected widget with the new height as typed by the user.
	 */
	private void updateHeight(){
		if(txtHeight.getText().trim().length() > 0){
			if(widget != null)
				widget.setHeight(txtHeight.getText()+PurcConstants.UNITS);
			else if(viewWidget != null){
				if(viewWidget instanceof DesignSurfaceView)
					((DesignSurfaceView)viewWidget).setHeight(txtHeight.getText()+PurcConstants.UNITS);
				else
					viewWidget.setHeight(txtHeight.getText()+PurcConstants.UNITS);
			}
			else
				widgetPropertyChangeListener.onWidgetPropertyChanged(WidgetPropertySetter.PROP_HEIGHT, txtHeight.getText()+PurcConstants.UNITS);
		}
	}

	/**
	 * Updates the selected widget with the new left as typed by the user.
	 */
	private void updateLeft(){
		if(txtLeft.getText().trim().length() > 0){
			if(widget != null)
				widget.setLeft(txtLeft.getText()+PurcConstants.UNITS);
			else
				widgetPropertyChangeListener.onWidgetPropertyChanged(WidgetPropertySetter.PROP_LEFT, txtLeft.getText()+PurcConstants.UNITS);
		}
	}

	/**
	 * Updates the selected widget with the new top as typed by the user.
	 */
	private void updateTop(){
		if(txtTop.getText().trim().length() > 0){
			if(widget != null)
				widget.setTop(txtTop.getText()+PurcConstants.UNITS);
			else
				widgetPropertyChangeListener.onWidgetPropertyChanged(WidgetPropertySetter.PROP_TOP, txtTop.getText()+PurcConstants.UNITS);
		}
	}

	/**
	 * Updates the selected widget with the new tab index as typed by the user.
	 */
	private void updateTabIndex(){
		if(txtTabIndex.getText().trim().length() > 0){
			if(widget != null)
				widget.setTabIndex(Integer.parseInt(txtTabIndex.getText()));
			else if(viewWidget != null && viewWidget instanceof DesignGroupWidget)
				((DesignWidgetWrapper)viewWidget.getParent().getParent()).setTabIndex(Integer.parseInt(txtTabIndex.getText()));
		}
	}

	/**
	 * @see org.purc.purcforms.client.controller.WidgetSelectionListener#onWidgetSelected(Widget, boolean)
	 */
	public void onWidgetSelected(Widget widget, boolean multipleSel) {

		if(widget instanceof DesignWidgetWrapper){
			prevWidget = this.widget;
			prevBinding = sgstBinding.getText().trim();
			this.widget = (DesignWidgetWrapper)widget;
			viewWidget = null;
			
			if(!loadedBindings && formDef != null)
				setupFormDef(formDef);
		}
		else{
			viewWidget = (DesignGroupView)widget;
			prevWidget = this.widget;
			this.widget = null;
		}

		//Removed from here for smooth updating where value has not changed
		/*txtText.setText(null);
		txtHelpText.setText(null);
		txtBinding.setText(null);
		txtHeight.setText(null);
		txtWidth.setText(null);
		chkVisible.setValue(false);
		chkEnabled.setValues(false);
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
			else{
				txtHelpText.setText(txtText.getText());
				updateHelpText();
			}

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
			
			value = this.widget.getFilterField();
			if(value != null && value.trim().length() > 0)
				txtFilterField.setText(value);
			else
				txtFilterField.setText(null);
			
			value = this.widget.getId();
			if(value != null && value.trim().length() > 0)
				txtId.setText(value);
			else
				txtId.setText(null);

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

	/**
	 * Sets properties that makes sense only for DesignGroupView widgets.
	 */
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

	/**
	 * Clears all widget values.
	 */
	private void clearProperties(){
		txtText.setText(null);
		txtHelpText.setText(null);
		txtBinding.setText(null);
		txtHeight.setText(null);
		txtWidth.setText(null);
		chkVisible.setValue(false);
		chkEnabled.setValue(false);
		sgstBinding.setText(null);
		sgstChildBinding.setText(null);
		txtTop.setText(null);
		txtLeft.setText(null);
		txtTabIndex.setText(null);
		txtExternalSource.setText(null);
		txtDisplayField.setText(null);
		txtValueField.setText(null);
		txtFilterField.setText(null);
		txtId.setText(null);
		cbRepeat.setSelectedIndex(-1);
		enableLabelProperties(false);
	}

	/**
	 * Checks if the selected widget should have a parent binding property.
	 * 
	 * @return true if yes, else false.
	 */
	private boolean hasParentBinding(){
		return (widget.getWrappedWidget() instanceof RadioButton) || (widget.getWrappedWidget() instanceof CheckBox)
		|| (widget.getWrappedWidget() instanceof Button);
	}

	/**
	 * Loads the child binding widget with a list of bindings based on the
	 * selected parent binding.
	 */
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
				FormUtil.loadOptions(options,oracle);
				txtChildBinding.setEnabled(true);
			}
		}
	}

	/**
	 * Sets the current form definition object.
	 * 
	 * @param formDef the form definition object.
	 */
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
		
		loadedBindings = (formDef.getQuestionCount() > 0);
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormSelectionListener#onFormItemSelected(Object)
	 */
	public void onFormItemSelected(Object formItem) {
		if(formItem == null)
			return;

		if(formItem instanceof FormDef)
			setupFormDef((FormDef)formItem);
	}

	/**
	 * Reloads the list of bindings basing on the curent form definition object.
	 */
	public void refresh(){
		setupFormDef(formDef);
	}

	/**
	 * Sets whether to enabled label widget properties. These are properties that 
	 * make sense only when the selected design widget is a wrapping a Label.
	 * 
	 * @param enable true to enable, else false to disable.
	 */
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

	public void setWidgetPropertyChangeListener(WidgetPropertyChangeListener widgetPropertyChangeListener){
		this.widgetPropertyChangeListener = widgetPropertyChangeListener;
	}
}
