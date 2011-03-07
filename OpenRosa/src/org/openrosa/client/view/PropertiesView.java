package org.openrosa.client.view;

import org.openrosa.client.Context;
import org.openrosa.client.controller.ILocaleSelectionListener;
import org.openrosa.client.model.Calculation;
import org.openrosa.client.model.FormDef;
import org.openrosa.client.model.GroupDef;
import org.openrosa.client.model.IFormElement;
import org.openrosa.client.model.OptionDef;
import org.openrosa.client.model.QuestionDef;
import org.openrosa.client.util.FormDesignerUtil;
import org.openrosa.client.util.Itext;
import org.openrosa.client.util.ItextLocale;
import org.openrosa.client.widget.DescTemplateWidget;
import org.openrosa.client.controller.FormDesignerController;
import org.openrosa.client.controller.IFormActionListener;
import org.openrosa.client.controller.IFormChangeListener;
import org.openrosa.client.controller.IFormSelectionListener;
import org.openrosa.client.controller.ItemSelectionListener;
import org.openrosa.client.locale.LocaleText;
import org.openrosa.client.util.FormUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;


/**
 * View responsible for displaying and hence allow editing of 
 * form, page, question, or question option properties.
 * 
 * @author daniel
 *
 */
public class PropertiesView extends Composite implements IFormSelectionListener, ItemSelectionListener, ILocaleSelectionListener{

	/** List box index for no selected data type. */
	private static final byte DT_INDEX_NONE = -1;

	/** List box index for text data type. */
	private static final byte DT_INDEX_TEXT = 0;

	/** List box index for number data type. */
	private static final byte DT_INDEX_NUMBER = 1;

	/** List box index for decimal data type. */
	private static final byte DT_INDEX_DECIMAL = 2;

	/** List box index for date data type. */
	private static final byte DT_INDEX_DATE = 3;

	/** List box index for time data type. */
	private static final byte DT_INDEX_TIME = 4;

	/** List box index for dateTime data type. */
	private static final byte DT_INDEX_DATE_TIME = 5;

	/** List box index for single select data type. */
	private static final byte DT_INDEX_SINGLE_SELECT = 6;

	/** List box index for multiple select data type. */
	private static final byte DT_INDEX_MULTIPLE_SELECT = 7;

	/** List box index for repeat data type. */
	private static final byte DT_INDEX_REPEAT = 8;

	/** List box index for image data type. */
	private static final byte DT_INDEX_IMAGE = 9;

	/** List box index for video data type. */
	private static final byte DT_INDEX_VIDEO = 10;

	/** List box index for audio data type. */
	private static final byte DT_INDEX_AUDIO = 11;

	/** List box index for gps data type. */
	private static final byte DT_INDEX_GPS = 12;
	
	/** List box index for barcode data type. */
	private static final byte DT_INDEX_BARCODE = 13;
	
	/** List box index for group data type. */
	private static final byte DT_INDEX_LABEL = 14;
	
	/** List box index for group data type. */
	private static final byte DT_INDEX_GROUP = 15;
	
	/** List box index for boolean data type. */
	private static final byte DT_INDEX_BOOLEAN = 17;
	
	/** List box index for single select dynamic data type. */
	private static final byte DT_INDEX_SINGLE_SELECT_DYNAMIC = 18;

	/** Table used for organizing widgets in a table format. */
	private FlexTable table = new FlexTable();

	/** Widget for displaying the list of data types. */
	private ListBox cbDataType = new ListBox(false);

	/** Widget for setting the visibility property. */
	private CheckBox chkVisible = new CheckBox();


	/** Widget for setting the locked property. */
	private CheckBox chkLocked = new CheckBox();

	/** Widget for setting the required property. */
	private CheckBox chkRequired = new CheckBox();

	/** Widget for setting the text property. */
	private TextArea txtDefaultLabel = new TextArea();

	/** Widget for setting the help text property. */
	private TextBox txtHelpText = new TextBox();

	/** Widget for setting the binding property. */
	private TextBox txtBinding = new TextBox();

	/** Widget for setting the Question ID property. */
	private TextBox qtnID = new TextBox();

	/** Widget for setting the default value property. ONLY FOR QUESTIONDEF! */
	private TextBox txtDefaultValue = new TextBox();

	private TextBox txtCalculation = new TextBox();

	/** Widget for setting the form key property. */
	private TextBox txtFormKey = new TextBox();

	/** The selected object which could be FormDef, PageDef, QuestionDef or OptionDef */
	private IFormElement propertiesObj;

	/** Listener to form change events. */
	private IFormChangeListener formChangeListener;

	/** Widget for defining skip rules. */
	private SkipRulesView skipRulesView = new SkipRulesView();

	/** Widget for defining validation rules. */
	private ValidationRulesView validationRulesView = new ValidationRulesView();

	private QuestionItextView itextView = new QuestionItextView();

	/** Listener to form action events. */
	private IFormActionListener formActionListener;

	Label lblDefaultLabel = new Label("Default Label Text");
	Label lblQtnID = new Label("ID");
	Label lblHelpText = new Label("Default Help Text");
	Label lblType = new Label(LocaleText.get("type"));
	Label lblBinding = new Label(LocaleText.get("binding"));
	Label lblVisible = new Label(LocaleText.get("visible"));
	Label lblEnabled = new Label(LocaleText.get("enabled"));
	Label lblLocked = new Label(LocaleText.get("locked"));
	Label lblRequired = new Label(LocaleText.get("required"));
	Label lblDefaultValue = new Label(LocaleText.get("defaultValue"));
	Label lblCalculate = new Label(LocaleText.get("calculation"));
	Label lblFormKey = new Label(LocaleText.get("formKey"));

	//Tab panel for holding skip, validation logic and dynamic lists.
	DecoratedTabPanel tabs = new DecoratedTabPanel();


	/**
	 * Creates a new instance of the properties view widget.
	 */
	public PropertiesView(){


		/*Label lblText = new Label(LocaleText.get("text"));
		Label lblHelpText = new Label(LocaleText.get("helpText"));
		Label lblType = new Label(LocaleText.get("type"));
		Label lblBinding = new Label(LocaleText.get("binding"));
		Label lblVisible = new Label(LocaleText.get("visible"));
		Label lblEnabled = new Label(LocaleText.get("enabled"));
		Label lblLocked = new Label(LocaleText.get("locked"));
		Label lblRequired = new Label(LocaleText.get("required"));
		Label lblDefault = new Label(LocaleText.get("defaultValue"));
		Label lblCalculate = new Label(LocaleText.get("calculation"));*/

		table.setWidget(0, 0, lblQtnID);
		table.setWidget(1, 0, lblDefaultLabel);
		table.setWidget(2, 0, lblHelpText);
		table.setWidget(3, 0, lblDefaultValue);
		table.setWidget(4, 0, lblType);
		table.setWidget(5, 0, lblVisible);
//		table.setWidget(6, 0, lblEnabled);
		//table.setWidget(7, 0, lblLocked);
		table.setWidget(7, 0, lblRequired);
		table.setWidget(8, 0, lblBinding);
		table.setWidget(9, 0, lblCalculate);

		table.setWidget(10, 0, lblFormKey);

		table.setWidget(0, 1, qtnID);
		table.setWidget(1, 1, txtDefaultLabel);
		table.setWidget(2, 1, txtHelpText);
		table.setWidget(3, 1, txtDefaultValue);
		table.setWidget(4, 1, cbDataType);
		table.setWidget(5, 1, chkVisible);
		//table.setWidget(7, 1, chkLocked);
		table.setWidget(7, 1, chkRequired);
		table.setWidget(8, 1, txtBinding);

		HorizontalPanel panel = new HorizontalPanel();
		panel.add(txtCalculation);
		FormUtil.maximizeWidget(txtCalculation);
		FormUtil.maximizeWidget(panel);
		table.setWidget(9, 1, panel);
		//panel.setVisible(false);

		panel = new HorizontalPanel();
		FormUtil.maximizeWidget(panel);
		table.setWidget(10, 1, panel);
		//panel.setVisible(false);


		//table.setStyleName("cw-FlexTable");
		//table.setStylePrimaryName("cw-FlexTable");
		table.getElement().getStyle().setBorderStyle(BorderStyle.SOLID);
		table.getElement().getStyle().setBorderColor("black");
		table.getElement().getStyle().setBorderWidth(0, Unit.PX);
		table.getElement().getStyle().setPadding(0, Unit.PX);

//		cbDataType.addItem("");
		cbDataType.addItem(LocaleText.get("qtnTypeText"));
		cbDataType.addItem(LocaleText.get("qtnTypeNumber"));
		cbDataType.addItem(LocaleText.get("qtnTypeDecimal"));
		cbDataType.addItem(LocaleText.get("qtnTypeDate"));
		cbDataType.addItem(LocaleText.get("qtnTypeTime"));
		cbDataType.addItem(LocaleText.get("qtnTypeDateTime"));
		cbDataType.addItem(LocaleText.get("qtnTypeSingleSelect"));
		cbDataType.addItem(LocaleText.get("qtnTypeMultSelect"));
		cbDataType.addItem(LocaleText.get("qtnTypeRepeat"));
		cbDataType.addItem(LocaleText.get("qtnTypePicture"));
		cbDataType.addItem(LocaleText.get("qtnTypeVideo"));
		cbDataType.addItem(LocaleText.get("qtnTypeAudio"));

		cbDataType.addItem(LocaleText.get("qtnTypeGPS"));
		cbDataType.addItem(LocaleText.get("qtnTypeBarcode"));
		cbDataType.addItem("Label");
		cbDataType.addItem("Group");
		cbDataType.addItem("------------");
		cbDataType.addItem(LocaleText.get("qtnTypeBoolean"));
		cbDataType.addItem(LocaleText.get("qtnTypeSingleSelectDynamic"));

		FlexCellFormatter cellFormatter = table.getFlexCellFormatter();
		cellFormatter.setHorizontalAlignment(14, 1, HasHorizontalAlignment.ALIGN_CENTER);

		//		table.setWidth("100%");
		//		cellFormatter.setWidth(0, 0, "20%");
		//cellFormatter.setColSpan(0, 0, 2);

		//cellFormatter.setWidth(9, 0, "20"+PurcConstants.UNITS);
		//cellFormatter.setWidth(9, 1, "20"+PurcConstants.UNITS);

		qtnID.setWidth("100%");
		txtDefaultLabel.setWidth("100%");
		txtHelpText.setWidth("100%");
		txtBinding.setWidth("100%");
		txtDefaultValue.setWidth("100%");
		cbDataType.setWidth("100%");
		txtFormKey.setWidth("100%");

		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.setSpacing(0);
		verticalPanel.add(table);

		tabs.add(itextView, "Itext");
		tabs.add(skipRulesView, LocaleText.get("skipLogic"));
		tabs.add(validationRulesView, LocaleText.get("validationLogic"));
		tabs.selectTab(0);
		
		table.setWidget(12, 0, tabs);
		table.getFlexCellFormatter().setColSpan(12, 0, 2);
		//verticalPanel.add(pnl);
		//FormUtil.maximizeWidget(tabs);

		//FormUtil.maximizeWidget(verticalPanel);
		verticalPanel.setWidth("100%");
		initWidget(verticalPanel);

//		setupEventListeners();

		cbDataType.setSelectedIndex(-1);

//		enableQuestionOnlyProperties(false);
		qtnID.setVisible(false);
		lblQtnID.setVisible(false);
		txtDefaultLabel.setVisible(false);
		lblDefaultLabel.setVisible(false);
		//txtDescTemplate.setVisible(false);
		//btnDescTemplate.setVisible(false);
//		enableDescriptionTemplate(false);
		txtCalculation.setVisible(false);
		lblCalculate.setVisible(false);

		tabs.setVisible(false);
		txtBinding.setVisible(false);
		lblBinding.setVisible(false);
		txtFormKey.setVisible(false);
		lblFormKey.setVisible(false);

		qtnID.setTitle(LocaleText.get("questionIdDesc"));
		txtDefaultLabel.setTitle(LocaleText.get("questionTextDesc"));
		txtHelpText.setTitle(LocaleText.get("questionDescDesc"));
		txtBinding.setTitle(LocaleText.get("questionIdDesc"));
		txtDefaultValue.setTitle(LocaleText.get("defaultValDesc"));
		cbDataType.setTitle(LocaleText.get("questionTypeDesc"));

//		DOM.sinkEvents(getElement(), Event.ONKEYDOWN | DOM.getEventsSunk(getElement()));

		cellFormatter = table.getFlexCellFormatter();
		txtBinding.setEnabled(false);
		
		createHandlers();
		
//		Context.addLocaleSelectionListener(this);
		
		setHeight("100%");
	}

	
	public void changeSelectedObject(IFormElement objectDef){
		propertiesObj = objectDef;
		if(objectDef instanceof FormDef) setFormProperties((FormDef)objectDef);
		else if (objectDef instanceof GroupDef) setGroupProperties((GroupDef) objectDef);
		else if (objectDef instanceof QuestionDef) setQuestionProperties((QuestionDef) objectDef);
		else if (objectDef instanceof OptionDef) setOptionDefProperties(objectDef);
		itextView.setItemID(objectDef.getItextId());
	}
	

	
	private void createHandlers(){
		//Create listener/event handlers for each widget
		qtnID.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				propertiesObj.setItextId(qtnID.getText());
			}});
		
		txtHelpText.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				propertiesObj.setHelpText(txtHelpText.getText());
			}});
		
		txtDefaultLabel.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				propertiesObj.setText(txtDefaultLabel.getText());
			}});
		
		//this one should only apply to QuestionDefs!
		txtDefaultValue.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				((QuestionDef) propertiesObj).setDefaultValue(txtDefaultValue.getText());
			}});
		
		//Combo boxes
		cbDataType.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event){
				updateDataType();
			}});
		
		cbDataType.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				updateDataType();
			}});
		
		chkVisible.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				propertiesObj.setVisible(event.getValue());
			}});
		
		chkRequired.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				propertiesObj.setRequired(event.getValue());
			}});
	}
	
	/**
	 * Updates the selected object with the new data type as typed by the user.
	 */
	private void updateDataType(){
		if(propertiesObj == null)
			return;

		boolean deleteKids = false;
		int index = cbDataType.getSelectedIndex();
		IFormElement questionDef = (IFormElement)propertiesObj;
		if((questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
				questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE) &&
				!(index == DT_INDEX_SINGLE_SELECT || index == DT_INDEX_MULTIPLE_SELECT)){
			if(questionDef.getChildCount() > 0 && !Window.confirm(LocaleText.get("changeWidgetTypePrompt"))){
				index = (questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE) ? DT_INDEX_SINGLE_SELECT : DT_INDEX_MULTIPLE_SELECT;
				cbDataType.setSelectedIndex(index);
				return;
			}
			deleteKids = true;
		}
		else if((questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT) &&
				!(index == DT_INDEX_REPEAT)){
			if(!Window.confirm(LocaleText.get("changeWidgetTypePrompt"))){
				index = DT_INDEX_REPEAT;
				cbDataType.setSelectedIndex(index);
				return;
			}
			deleteKids = true;
		}

		int prevDataType = questionDef.getDataType();
		
		//cbDataType.setSelectedIndex(index);
		setQuestionDataType((IFormElement)propertiesObj);
		propertiesObj = (IFormElement)formChangeListener.onFormItemChanged((Object)propertiesObj);
		if(deleteKids)
			formChangeListener.onDeleteChildren(propertiesObj);
		
		Context.getEventBus().fireDataTypeChangeEvent(questionDef, prevDataType);
	}

	/**
	 * Sets the data type of a question definition object basing on selection
	 * in the type selection list box widget.
	 * 
	 * @param questionDef the question definition object.
	 */
	private void setQuestionDataType(IFormElement questionDef){
		int dataType = QuestionDef.QTN_TYPE_TEXT;

		switch(cbDataType.getSelectedIndex()){
		case DT_INDEX_NUMBER:
			dataType = QuestionDef.QTN_TYPE_NUMERIC;
			break;
		case DT_INDEX_DECIMAL:
			dataType = QuestionDef.QTN_TYPE_DECIMAL;
			break;
		case DT_INDEX_DATE:
			dataType = QuestionDef.QTN_TYPE_DATE;
			break;
		case DT_INDEX_TIME:
			dataType = QuestionDef.QTN_TYPE_TIME;
			break;
		case DT_INDEX_DATE_TIME:
			dataType = QuestionDef.QTN_TYPE_DATE_TIME;
			break;
		case DT_INDEX_BOOLEAN:
			dataType = QuestionDef.QTN_TYPE_BOOLEAN;
			break;
		case DT_INDEX_SINGLE_SELECT:
			dataType = QuestionDef.QTN_TYPE_LIST_EXCLUSIVE;
			break;
		case DT_INDEX_MULTIPLE_SELECT:
			dataType = QuestionDef.QTN_TYPE_LIST_MULTIPLE;
			break;
		case DT_INDEX_REPEAT:
			dataType = QuestionDef.QTN_TYPE_REPEAT;
			break;
		case DT_INDEX_IMAGE:
			dataType = QuestionDef.QTN_TYPE_IMAGE;
			break;
		case DT_INDEX_VIDEO:
			dataType = QuestionDef.QTN_TYPE_VIDEO;
			break;
		case DT_INDEX_AUDIO:
			dataType = QuestionDef.QTN_TYPE_AUDIO;
			break;
		case DT_INDEX_SINGLE_SELECT_DYNAMIC:
			dataType = QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC;
			break;
		case DT_INDEX_GPS:
			dataType = QuestionDef.QTN_TYPE_GPS;
			break;
		case DT_INDEX_BARCODE:
			dataType = QuestionDef.QTN_TYPE_BARCODE;
			break;
		case DT_INDEX_LABEL:
			dataType = QuestionDef.QTN_TYPE_LABEL;
			break;
		case DT_INDEX_GROUP:
			dataType = QuestionDef.QTN_TYPE_GROUP;
			break;
		}

		/*if(dataType == QuestionDef.QTN_TYPE_REPEAT && 
				questionDef.getDataType() != QuestionDef.QTN_TYPE_REPEAT &&
				questionDef instanceof QuestionDef)
			((QuestionDef)questionDef).setRepeatQtnsDef(new RepeatQtnsDef((QuestionDef)questionDef));*/

		questionDef.setDataType(dataType);

	}
	
	/**
	 * @param qtn
	 * @return the selection index for the Question Data Type combo box shown on the properties view.
	 */
	private static int getCBIndexFromQtnDataType(QuestionDef qtn){
		int qdt = qtn.getDataType();
		int retIndex = -1;

		switch(qdt){
		case QuestionDef.QTN_TYPE_TEXT:
			retIndex = DT_INDEX_TEXT;
			break;
		case QuestionDef.QTN_TYPE_NUMERIC:
			retIndex = DT_INDEX_NUMBER;
			break;
		case QuestionDef.QTN_TYPE_DECIMAL:
			retIndex = DT_INDEX_DECIMAL;
			break;
		case QuestionDef.QTN_TYPE_DATE:
			retIndex = DT_INDEX_DATE;
			break;
		case QuestionDef.QTN_TYPE_TIME:
			retIndex = DT_INDEX_TIME;
			break;
		case QuestionDef.QTN_TYPE_DATE_TIME:
			retIndex = DT_INDEX_DATE_TIME;
			break;
		case QuestionDef.QTN_TYPE_BOOLEAN:
			retIndex = DT_INDEX_BOOLEAN;
			break;
		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
			retIndex = DT_INDEX_SINGLE_SELECT;
			break;
		case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
			retIndex = DT_INDEX_MULTIPLE_SELECT;
			break;
		case QuestionDef.QTN_TYPE_REPEAT:
			retIndex = DT_INDEX_REPEAT;
			break;
		case QuestionDef.QTN_TYPE_IMAGE:
			retIndex = DT_INDEX_IMAGE;
			break;
		case QuestionDef.QTN_TYPE_VIDEO:
			retIndex = DT_INDEX_VIDEO;
			break;
		case QuestionDef.QTN_TYPE_AUDIO:
			retIndex = DT_INDEX_AUDIO;
			break;
		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC:
			retIndex = DT_INDEX_SINGLE_SELECT_DYNAMIC;
			break;
		case QuestionDef.QTN_TYPE_GPS:
			retIndex = DT_INDEX_GPS;
			break;
		case QuestionDef.QTN_TYPE_BARCODE:
			retIndex = DT_INDEX_BARCODE;
			break;
		case QuestionDef.QTN_TYPE_LABEL:
			retIndex = DT_INDEX_LABEL;
			break;
		case QuestionDef.QTN_TYPE_GROUP:
			retIndex = DT_INDEX_GROUP;
			break;
		}
		
		return retIndex;
	}
	
	/**
	 * Sets values for widgets which deal with form definition properties.
	 * 
	 * @param formDef the form definition object.
	 */
	private void setFormProperties(FormDef formDef){
		changeEverythingVisible(false);

		txtDefaultLabel.setVisible(true);
		lblDefaultLabel.setVisible(true);
		lblQtnID.setVisible(true);
		qtnID.setVisible(true);

		txtDefaultLabel.setText(formDef.getName());
		txtBinding.setText(formDef.getDataNodesetPath());
		txtFormKey.setText(formDef.getFormKey());
		qtnID.setText(formDef.getBinding());
		//skipRulesView.setFormDef(formDef);

	}

	/**
	 * Sets values for widgets which deal with page definition properties.
	 * 
	 * @param pageDef the page definition object.
	 */
	private void setGroupProperties(GroupDef groupObj){
		changeEverythingVisible(false); //hide all
		
		//make things visible
		lblQtnID.setVisible(true);
		qtnID.setVisible(true);
		lblDefaultLabel.setVisible(true);
		txtDefaultLabel.setVisible(true);
		lblHelpText.setVisible(true);
		txtHelpText.setVisible(true);
		lblType.setVisible(true);
		cbDataType.setVisible(true);
		lblVisible.setVisible(true);
		chkVisible.setVisible(true);
		lblRequired.setVisible(true);
		chkRequired.setVisible(true);
		lblBinding.setVisible(true);
		txtBinding.setVisible(true);
		tabs.setVisible(true);

		//set initial values
		qtnID.setText(groupObj.getBinding());
		txtDefaultLabel.setText(groupObj.getText());
		txtHelpText.setText(groupObj.getHelpText());
		cbDataType.setSelectedIndex(DT_INDEX_GROUP);
		chkVisible.setValue(groupObj.isVisible());
		chkRequired.setValue(groupObj.isRequired());
		txtBinding.setText(groupObj.getDataNodesetPath());
	}

	/**
	 * Sets values for widgets which deal with question definition properties.
	 * 
	 * @param questionDef the question definition object.
	 */
	private void setQuestionProperties(QuestionDef questionDef){
		changeEverythingVisible(false);

		
		lblQtnID.setVisible(true);
		qtnID.setVisible(true);
		lblDefaultLabel.setVisible(true);
		txtDefaultLabel.setVisible(true);
		lblHelpText.setVisible(true);
		txtHelpText.setVisible(true);
		lblDefaultValue.setVisible(true);
		txtDefaultValue.setVisible(true);
		
		lblType.setVisible(true);
		cbDataType.setVisible(true);
		
		lblVisible.setVisible(true);
		chkVisible.setVisible(true);
		lblRequired.setVisible(true);
		chkRequired.setVisible(true);

		lblBinding.setVisible(true);
		txtBinding.setVisible(true);
		
		tabs.setVisible(true);
		
		qtnID.setText(questionDef.getBinding());
		txtDefaultLabel.setText(questionDef.getText());
		txtBinding.setText(questionDef.getDataNodesetPath());
		txtHelpText.setText(questionDef.getHelpText());
		txtDefaultValue.setText(questionDef.getDefaultValue());

		chkVisible.setValue(questionDef.isVisible());
		chkLocked.setValue(questionDef.isLocked());
		chkRequired.setValue(questionDef.isRequired());
		
		
		cbDataType.setSelectedIndex(getCBIndexFromQtnDataType(questionDef));
		
//		setQuestionDataType(questionDef);

//		String calculationExpression = null;
//		Calculation calculation = Context.getFormDef().getCalculation(questionDef);
//		if(calculation != null)
//			calculationExpression = calculation.getCalculateExpression();
//		txtCalculation.setText(calculationExpression);

		//Skip logic processing is a bit slow and hence we wanna update the 
		//UI with the rest of simple quick properties as we process skip logic
		DeferredCommand.addCommand(new Command(){
			public void execute() {
				skipRulesView.setQuestionDef((IFormElement)propertiesObj);
				
				if(propertiesObj instanceof QuestionDef){
					validationRulesView.setQuestionDef((QuestionDef)propertiesObj);
				}
				else{
					validationRulesView.setQuestionDef(null);
				}
				
				if(propertiesObj instanceof IFormElement){
					itextView.setItemID(((IFormElement)propertiesObj).getItextId());
				}
			}
		});
	}
	
	private void setOptionDefProperties(IFormElement optionDef){
		changeEverythingVisible(false);

		
		lblQtnID.setVisible(true);
		qtnID.setVisible(true);
		lblDefaultLabel.setVisible(true);
		txtDefaultLabel.setVisible(true);
		lblDefaultValue.setVisible(true);
		txtDefaultValue.setVisible(true);
		
		lblType.setVisible(true);
		cbDataType.setVisible(true);
		
		lblBinding.setVisible(true);
		txtBinding.setVisible(true);
		
		tabs.setVisible(true);
		
		qtnID.setText(optionDef.getItextId());
		txtDefaultLabel.setText(optionDef.getText());
		txtBinding.setText(optionDef.getDataNodesetPath());
		txtDefaultValue.setText(optionDef.getDefaultValue());

		
		DeferredCommand.addCommand(new Command(){
			public void execute() {
				skipRulesView.setQuestionDef((IFormElement)propertiesObj);
				
				if(propertiesObj instanceof QuestionDef){
					validationRulesView.setQuestionDef((QuestionDef)propertiesObj);
				}
				else{
					validationRulesView.setQuestionDef(null);
				}
				
				if(propertiesObj instanceof IFormElement){
					itextView.setItemID(((IFormElement)propertiesObj).getItextId());
				}
			}
		});
	}
	
	
	
	/**
	 * Sets whether to enable question property widgets.
	 * 
	 * @param visible - True for make visible, false for everything invisibles
	 */
	private void changeEverythingVisible(boolean visible){
		//boolean enable = (enabled && !Context.isStructureReadOnly()) ? true : false;

		cbDataType.setVisible(visible);
		chkVisible.setVisible(visible);
		chkLocked.setVisible(visible);
		chkRequired.setVisible(visible);
		txtDefaultValue.setVisible(visible);
		txtHelpText.setVisible(visible); //We allow localisation of help text.

		//We do not just wanna show this but rather want to enable them.
//		skipRulesView.setEnabled(enable2);
//		validationRulesView.setEnabled(enable2);
//		itextView.setEnabled(enable2);

		lblType.setVisible(visible);
		lblVisible.setVisible(visible);
		lblEnabled.setVisible(visible);
		lblLocked.setVisible(visible);
		lblRequired.setVisible(visible);
		lblDefaultValue.setVisible(visible);
		lblHelpText.setVisible(visible);
		lblDefaultValue.setVisible(visible);

		//btnDescTemplate.setVisible(enable2);
		txtCalculation.setVisible(visible);
		lblCalculate.setVisible(visible);
		txtCalculation.setVisible(visible);

		tabs.setVisible(visible);

		clearProperties();
	}
	
	/**
	 * Clears values from all widgets.
	 */
	public void clearProperties(){
		cbDataType.setSelectedIndex(DT_INDEX_NONE);
		chkVisible.setValue(false);
		chkLocked.setValue(false);
		chkRequired.setValue(false);
		txtDefaultValue.setText(null);
		txtHelpText.setText(null);
		txtDefaultLabel.setText(null);
		txtBinding.setText(null);
		txtCalculation.setText(null);
		txtFormKey.setText(null);
		qtnID.setText(null);
	}
	
	private FormDesignerController getFDC(){
		return FormDesignerController.getFormDesignerController();
	}


	@Override
	public void onFormItemSelected(Object formItem) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onItemSelected(Object sender, Object item) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onStartItemSelection(Object sender) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onLocaleSelected(ItextLocale locale) {
		// TODO Auto-generated method stub
		
	}
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Sets up event listeners.
	 */
//	private void setupEventListeners(){
//		//Check boxes.k
//		chkVisible.addClickHandler(new ClickHandler(){
//			public void onClick(ClickEvent event){
//				((QuestionDef)propertiesObj).setVisible(chkVisible.getValue() == true);
//				propertiesObj = formChangeListener.onFormItemChanged(propertiesObj);
//			}
//		});
//
//		chkLocked.addClickHandler(new ClickHandler(){
//			public void onClick(ClickEvent event){
//				((QuestionDef)propertiesObj).setLocked(chkLocked.getValue() == true);
//				propertiesObj = formChangeListener.onFormItemChanged(propertiesObj);
//			}
//		});
//
//		chkRequired.addClickHandler(new ClickHandler(){
//			public void onClick(ClickEvent event){
//				((QuestionDef)propertiesObj).setRequired(chkRequired.getValue() == true);
//				propertiesObj = formChangeListener.onFormItemChanged(propertiesObj);
//			}
//		});
//
//		//Text boxes.
//		txtDefaultValue.addChangeHandler(new ChangeHandler(){
//			public void onChange(ChangeEvent event){
//				updateDefaultValue();
//			}
//		});
//		txtDefaultValue.addKeyUpHandler(new KeyUpHandler(){
//			public void onKeyUp(KeyUpEvent event) {
//				updateDefaultValue();
//			}
//		});
//
//		txtHelpText.addChangeHandler(new ChangeHandler(){
//			public void onChange(ChangeEvent event){
//				updateHelpText();
//			}
//		});
//		txtHelpText.addKeyUpHandler(new KeyUpHandler(){
//			public void onKeyUp(KeyUpEvent event) {
//				updateHelpText();
//			}
//		});
//
//
//		txtBinding.addChangeHandler(new ChangeHandler(){
//			public void onChange(ChangeEvent event){
//				updateBinding();
//			}
//		});
//		txtBinding.addKeyUpHandler(new KeyUpHandler(){
//			public void onKeyUp(KeyUpEvent event) {
//				String s = txtBinding.getText();
//
//				s = s.replace("%", "");
//				s = s.replace("(", "");
//				s = s.replace("!", "");
//				s = s.replace("&", "");
//				//s = s.replace(".", ""); //Looks like this is an allowed character in xml node names.
//				s = s.replace("'", "");
//				s = s.replace("\"", "");
//				s = s.replace("$", "");
//				s = s.replace("#", "");
//
//				txtBinding.setText(s);
//				updateBinding();
//			}
//		});
//
//
//
//		txtText.addChangeHandler(new ChangeHandler(){
//			public void onChange(ChangeEvent event){
//				IFormElement objectDef = (IFormElement)propertiesObj;
//				objectDef.setText(txtText.getText());
//				
////				
////				String orgText = getSelObjetOriginalText();
////				updateText();
////				updateSelObjBinding(orgText);
//			}
//		});
////		txtText.addKeyUpHandler(new KeyUpHandler(){
////			public void onKeyUp(KeyUpEvent event) {
////				String orgText = getSelObjetOriginalText();
////				updateText();
////				updateSelObjBinding(orgText);
////				//qtnID.setFocus(true);
////			}
////		});
//
//
//
//		qtnID.addKeyUpHandler(new KeyUpHandler(){
//			public void onKeyUp(KeyUpEvent event) {
//				updateID();
//			}
//		});
//
//
//		txtDescTemplate.addChangeHandler(new ChangeHandler(){
//			public void onChange(ChangeEvent event){
//				updateDescTemplate();
//			}
//		});
//		txtDescTemplate.addKeyUpHandler(new KeyUpHandler(){
//			public void onKeyUp(KeyUpEvent event) {
//				updateDescTemplate();
//			}
//		});
//
//		txtCalculation.addChangeHandler(new ChangeHandler(){
//			public void onChange(ChangeEvent event){
//				updateCalculation();
//			}
//		});
//		txtCalculation.addKeyUpHandler(new KeyUpHandler(){
//			public void onKeyUp(KeyUpEvent event) {
//				updateCalculation();
//			}
//		});
//

//
//		txtFormKey.addChangeHandler(new ChangeHandler(){
//			public void onChange(ChangeEvent event){
//				updateFormKey();
//			}
//		});
//		txtFormKey.addKeyUpHandler(new KeyUpHandler(){
//			public void onKeyUp(KeyUpEvent event) {
//				updateFormKey();
//			}
//		});
//	}

//	private String getSelObjetOriginalText(){
//		if(propertiesObj instanceof FormDef)
//			return ((FormDef)propertiesObj).getName();
//		else if(propertiesObj instanceof QuestionDef)
//			return ((QuestionDef)propertiesObj).getText();
//		else if(propertiesObj instanceof OptionDef )
//			return ((OptionDef)propertiesObj).getText();
//		else if(propertiesObj instanceof GroupDef )
//			return ((GroupDef)propertiesObj).getText();
//		return null;
//	}
//
//	private void updateSelObjBinding(String orgText){
//
//		if(orgText == null)
//			return;
//
//		String orgTextDefBinding = FormDesignerUtil.getXmlTagName(getTextWithoutDecTemplate(orgText));
//
//		if(propertiesObj != null && Context.allowBindEdit() && !Context.isStructureReadOnly()){
//			String text = getTextWithoutDecTemplate(txtDefaultLabel.getText().trim());
//			String name = FormDesignerUtil.getXmlTagName(text);
//			if(propertiesObj instanceof FormDef && ((FormDef)propertiesObj).getVariableName().equals(orgTextDefBinding) /*startsWith("newform")*/){
//				((FormDef)propertiesObj).setVariableName(name);
//				txtBinding.setText(name);
//
//				if(((FormDef)propertiesObj).getItextId().equals(orgTextDefBinding)){
//					((FormDef)propertiesObj).setItextId(name);
//					qtnID.setText(name);
//				}
//			}
//			else if(propertiesObj instanceof GroupDef && ((GroupDef)propertiesObj).getBinding().equals(orgTextDefBinding) /*startsWith("newform")*/){
//				((GroupDef)propertiesObj).setBinding(name);
//				txtBinding.setText(name);
//
//				if(((GroupDef)propertiesObj).getItextId().equals(orgTextDefBinding)){
//					((GroupDef)propertiesObj).setItextId(name);
//					qtnID.setText(name);
//				}
//			}
//			else if(propertiesObj instanceof QuestionDef && ((QuestionDef)propertiesObj).getBinding().equals(orgTextDefBinding) /*startsWith("question")*/){
//				((QuestionDef)propertiesObj).setVariableName(name);
//				txtBinding.setText(name);
//
//				if(((QuestionDef)propertiesObj).getItextId().equals(orgTextDefBinding)){
//					((QuestionDef)propertiesObj).setItextId(name);
//					qtnID.setText(name);
//				}
//			}
//			else if(propertiesObj instanceof OptionDef && ((OptionDef)propertiesObj).getBinding().equals(orgTextDefBinding) /*.startsWith("option")*/){
//				((OptionDef)propertiesObj).setBinding(name);
//				txtBinding.setText(name);
//
//				if(((OptionDef)propertiesObj).getItextId().equals(orgTextDefBinding)){
//					((OptionDef)propertiesObj).setItextId(name);
//					qtnID.setText(name);
//				}
//			}
//		}
//	}
//
//
//	/**
//	 * Gets text without the description template, for a given text.
//	 * 
//	 * @param text the text to parse.
//	 * @return the text without the description template.
//	 */
//	private String getTextWithoutDecTemplate(String text){
//		if(text.contains("${")){
//			if(text.indexOf("}$") < text.length() - 2)
//				text = text.substring(0,text.indexOf("${")) + text.substring(text.indexOf("}$") + 2);
//			else
//				text = text.substring(0,text.indexOf("${"));
//		}
//		return text;
//	}
//
//
//	/**
//	 * Checks if a given character is allowed to begin an xml node name.
//	 * 
//	 * @param keyCode the character code.
//	 * @return true if is allowed, else false.
//	 */
//	private boolean isAllowedXmlNodeNameStartChar(char keyCode){
//		return ((keyCode >= 'a' && keyCode <= 'z') || (keyCode >= 'A' && keyCode <= 'Z') || isControlChar(keyCode));
//	}
//
//	/**
//	 * Checks if a character is allowed in an xml node name.
//	 * 
//	 * @param keyCode the character code.
//	 * @return true if allowed, else false.
//	 */
//	private boolean isAllowedXmlNodeNameChar(char keyCode){
//		return isAllowedXmlNodeNameStartChar(keyCode) || Character.isDigit(keyCode) || keyCode == '-' || keyCode == '_' || keyCode == '.';
//	}
//
//	/**
//	 * Check if a character is a control character. Examples of control characters are
//	 * ALT, CTRL, ESCAPE, DELETE, SHIFT, HOME, PAGE_UP, BACKSPACE, ENTER, TAB, LEFT, and more.
//	 * 
//	 * @param keyCode the character code.
//	 * @return true if yes, else false.
//	 */
//	private boolean isControlChar(char keyCode){
//		int code = keyCode;
//		return (code == KeyCodes.KEY_ALT || code == KeyCodes.KEY_BACKSPACE ||
//				code == KeyCodes.KEY_CTRL || code == KeyCodes.KEY_DELETE ||
//				code == KeyCodes.KEY_DOWN || code == KeyCodes.KEY_END ||
//				code == KeyCodes.KEY_ENTER || code == KeyCodes.KEY_ESCAPE ||
//				code == KeyCodes.KEY_HOME || code == KeyCodes.KEY_LEFT ||
//				code == KeyCodes.KEY_PAGEDOWN || code == KeyCodes.KEY_PAGEUP ||
//				code == KeyCodes.KEY_RIGHT || code == KeyCodes.KEY_SHIFT ||
//				code == KeyCodes.KEY_TAB || code == KeyCodes.KEY_UP);
//	}
//
//	/**
//	 * Updates the selected object with the new text as typed by the user.
//	 */
//	private void updateText(){
//		if(propertiesObj == null){
//			GWT.log("propertiesObj is null, won't update properties!");
//			return;
//		}
//
//		if(propertiesObj instanceof QuestionDef)
//			((QuestionDef)propertiesObj).setText(txtDefaultLabel.getText());
//		else if(propertiesObj instanceof OptionDef)
//			((OptionDef)propertiesObj).setText(txtDefaultLabel.getText());
//		else if(propertiesObj instanceof GroupDef)
//			((GroupDef)propertiesObj).setName(txtDefaultLabel.getText());
//		else if(propertiesObj instanceof FormDef)
//			((FormDef)propertiesObj).setName(txtDefaultLabel.getText());
//
//		propertiesObj = formChangeListener.onFormItemChanged(propertiesObj);
//	}
//
//
//	private void updateFormKey(){
//		if(propertiesObj == null)
//			return;
//
//		if(propertiesObj instanceof FormDef)
//			((FormDef)propertiesObj).setFormKey(txtFormKey.getText());
//
//		propertiesObj = formChangeListener.onFormItemChanged(propertiesObj);
//	}
//
//
//	/**
//	 * Updates the selected object with the new description template as typed by the user.
//	 */
//	private void updateDescTemplate(){
//		if(propertiesObj == null)
//			return;
//
//		else if(propertiesObj instanceof FormDef){
//			((FormDef)propertiesObj).setDescriptionTemplate(txtDescTemplate.getText());
//			propertiesObj = formChangeListener.onFormItemChanged(propertiesObj);
//		}
//	}
//
//
//	private void updateCalculation(){
//		if(propertiesObj == null)
//			return;
//
//		assert(propertiesObj instanceof QuestionDef);
//		Context.getFormDef().updateCalculation((QuestionDef)propertiesObj, txtCalculation.getText());
//	}
//
//
//	/**
//	 * Updates the selected object with the new binding as typed by the user.
//	 */
//	private void updateBinding(){
//		if(propertiesObj == null)
//			return;
//
//		if(txtBinding.getText().trim().length() == 0)
//			return;
//
//		if(propertiesObj instanceof QuestionDef)
//			((QuestionDef)propertiesObj).setVariableName(txtBinding.getText());
//		else if(propertiesObj instanceof OptionDef)
//			((OptionDef)propertiesObj).setBinding(txtBinding.getText());
//		else if(propertiesObj instanceof FormDef)
//			((FormDef)propertiesObj).setVariableName(txtBinding.getText());
////		else if(propertiesObj instanceof PageDef){
////			try{
////				((PageDef)propertiesObj).setPageNo(Integer.parseInt(txtBinding.getText()));
////			}catch(Exception ex){
////				return;
////			}
////		}
//
//		propertiesObj = formChangeListener.onFormItemChanged(propertiesObj);
//	}
//
//	/**
//	 * Updates the selected object with the new help text as typed by the user.
//	 */
//	private void updateHelpText(){
//		if(propertiesObj == null)
//			return;
//
//		((IFormElement)propertiesObj).setHelpText(txtHelpText.getText());
//		propertiesObj = formChangeListener.onFormItemChanged(propertiesObj);
//	}
//
//	/**
//	 * Updates the selected object with the new itext id as typed by the user.
//	 */
//	private void updateID(){
//		if(propertiesObj == null)
//			return;
//
//		if(qtnID.getText().trim().length() == 0)
//			return;
//
//		if(propertiesObj instanceof QuestionDef)
//			((QuestionDef)propertiesObj).setItextId(qtnID.getText());
//		else if(propertiesObj instanceof OptionDef)
//			((OptionDef)propertiesObj).setItextId(qtnID.getText());
//		else if(propertiesObj instanceof FormDef)
//			((FormDef)propertiesObj).setItextId(qtnID.getText());
//		else if(propertiesObj instanceof GroupDef)
//			((GroupDef)propertiesObj).setItextId(qtnID.getText());
//	}
//
//	/**
//	 * Updates the selected object with the new default value as typed by the user.
//	 */
//	private void updateDefaultValue(){
//		if(propertiesObj == null)
//			return;
//
//		((QuestionDef)propertiesObj).setDefaultValue(txtDefaultValue.getText());
//		propertiesObj = formChangeListener.onFormItemChanged(propertiesObj);
//	}
//
//
//
	/**
	 * Sets the listener for form change events.
	 * 
	 * @param formChangeListener the listener.
	 */
	public void setFormChangeListener(IFormChangeListener formChangeListener){
		this.formChangeListener = formChangeListener;
	}

//
//	/**
//	 * Sets values for widgets which deal with question option definition properties.
//	 * 
//	 * @param optionDef the option definition object.
//	 */
//	private void setQuestionOptionProperties(OptionDef optionDef){
//		enableQuestionOnlyProperties(false);
//		//txtDescTemplate.setVisible(false);
//		//btnDescTemplate.setVisible(false);
//		enableDescriptionTemplate(false);
//		txtCalculation.setVisible(false);
//		btnCalculation.setVisible(false);
//		lblCalculate.setVisible(false);
//
//		txtDefaultLabel.setText(optionDef.getText());
//		txtBinding.setText(optionDef.getBinding());
//		qtnID.setText(optionDef.getItextId());
//		//skipRulesView.updateSkipRule();
//	}
//

//
//	/**
//	 * Selects the current question's data type in the data types drop down listbox.
//	 * 
//	 * @param type the current question's data type.
//	 */
//	private void setDataType(int type){
//		int index = DT_INDEX_NONE;
//
//		switch(type){
//		case QuestionDef.QTN_TYPE_DATE:
//			index = DT_INDEX_DATE;
//			break;
//		case QuestionDef.QTN_TYPE_BOOLEAN:
//			index = DT_INDEX_BOOLEAN;
//			break;
//		case QuestionDef.QTN_TYPE_DATE_TIME:
//			index = DT_INDEX_DATE_TIME;
//			break;
//		case QuestionDef.QTN_TYPE_DECIMAL:
//			index = DT_INDEX_DECIMAL;
//			break;
//		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
//			index = DT_INDEX_SINGLE_SELECT;
//			break;
//		case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
//			index = DT_INDEX_MULTIPLE_SELECT;
//			break;
//		case QuestionDef.QTN_TYPE_NUMERIC:
//			index = DT_INDEX_NUMBER;
//			break;
//		case QuestionDef.QTN_TYPE_REPEAT:
//			index = DT_INDEX_REPEAT;
//			break;
//		case QuestionDef.QTN_TYPE_TEXT:
//			index = DT_INDEX_TEXT;
//			break;
//		case QuestionDef.QTN_TYPE_TIME:
//			index = DT_INDEX_TIME;
//			break;
//		case QuestionDef.QTN_TYPE_IMAGE:
//			index = DT_INDEX_IMAGE;
//			break;
//		case QuestionDef.QTN_TYPE_VIDEO:
//			index = DT_INDEX_VIDEO;
//			break;
//		case QuestionDef.QTN_TYPE_AUDIO:
//			index = DT_INDEX_AUDIO;
//			break;
//		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC:
//			index = DT_INDEX_SINGLE_SELECT_DYNAMIC;
//			break;
//		case QuestionDef.QTN_TYPE_GPS:
//			index = DT_INDEX_GPS;
//			break;
//		case QuestionDef.QTN_TYPE_BARCODE:
//			index = DT_INDEX_BARCODE;
//			break;
//		case QuestionDef.QTN_TYPE_LABEL:
//			index = DT_INDEX_LABEL;
//			break;
//		case QuestionDef.QTN_TYPE_GROUP:
//				index = DT_INDEX_GROUP;
//				break;
//		}
//
//		cbDataType.setSelectedIndex(index);
//	}
//

//
//	/**
//	 * @see org.openrosa.client.controller.IFormSelectionListener#onFormItemSelected(java.lang.Object)
//	 */
//	public void onFormItemSelected(Object formItem) {
//		propertiesObj = formItem;
//
//		clearProperties();
//
//		//For now these may be options for boolean question types (Yes & No)
//		if(formItem == null){
//			enableQuestionOnlyProperties(false);
//			txtDefaultLabel.setVisible(false);
//			lblText.setVisible(false);
//			qtnID.setVisible(false);
//			lblQtnID.setVisible(false);
//			//txtDescTemplate.setVisible(false);
//			//btnDescTemplate.setVisible(false);
//			enableDescriptionTemplate(false);
//
//			txtBinding.setVisible(false);
//			lblBinding.setVisible(false);
//
//			return;
//		}
//
//		boolean visible = Context.allowBindEdit() && !Context.isStructureReadOnly();
//		txtBinding.setVisible(visible);
//		lblBinding.setVisible(visible);
//
//		if(formItem instanceof FormDef)
//			setFormProperties((FormDef)formItem);
//		else if(formItem instanceof GroupDef)
//			setPageProperties((GroupDef)formItem);
//		else if(formItem instanceof QuestionDef)
//			setQuestionProperties((QuestionDef)formItem);
//		else if(formItem instanceof OptionDef){
//			setQuestionOptionProperties((OptionDef)formItem);
//
//			//Since option bindings are not xml node names, we may allow their
//			//edits as they are not structure breaking.
//			visible = !Context.isStructureReadOnly();
//			txtBinding.setVisible(visible);
//			lblBinding.setVisible(visible);
//		}
//	}
//
//	/**
//	 * Sets focus to the first input widget.
//	 */
//	public void setFocus(){
////		txtText.setFocus(true);
////		txtText.selectAll();
//	}
//
	/**
	 * @see com.google.gwt.user.client.WindowResizeListener#onWindowResized(int, int)
	 */
	public void onWindowResized(int width, int height){
		setWidth("100%");
		setHeight("100%");
		validationRulesView.onWindowResized(width, height);
	}
//
//	/**
//	 * Retrieves changes from all widgets and updates the selected object.
//	 */
//	public void commitChanges(){
//		skipRulesView.updateSkipRule();
//		validationRulesView.updateValidationRule();
//		itextView.update();
//	}
//
//	/**
//	 * @see org.openrosa.client.controller.ItemSelectionListener#onItemSelected(Object, Object)
//	 */
//	public void onItemSelected(Object sender, Object item) {
//		if(sender == btnDescTemplate){
//
//			item = "${" + item + "}$";
//
//			if(propertiesObj instanceof QuestionDef){
//				txtDefaultLabel.setText(txtDefaultLabel.getText() + " " + txtDescTemplate.getText() + item);
//				updateText();
////				txtText.setFocus(true);
//			}
//			else{
//				txtDescTemplate.setText(txtDescTemplate.getText() + item);
//				updateDescTemplate(); //Added for IE which does not properly throw change events for the desc template textbox
////				txtDescTemplate.setFocus(true);
//			}
//		}
//		else if(sender == btnCalculation){
//			assert(propertiesObj instanceof QuestionDef);
//			txtCalculation.setText(txtCalculation.getText() + item);
//			updateCalculation(); //Added for IE which does not properly throw change events for the desc template textbox
////			txtCalculation.setFocus(true);
//		}
//	}
//
//	/**
//	 * @see org.openrosa.client.controller.ItemSelectionListener#onStartItemSelection(Object)
//	 */
//	public void onStartItemSelection(Object sender) {
//
//	}
//
	/**
	 * Sets the listener to form action events.
	 * 
	 * @param formActionListener the listener.
	 */
	public void setFormActionListener(IFormActionListener formActionListener){
		this.formActionListener = formActionListener;
	}
//
////	@Override
////	public void onBrowserEvent(Event event) {
////		switch (DOM.eventGetType(event)) {
////		case Event.ONKEYDOWN:
////			if(!isVisible())
////				return;
////
////			int keyCode = event.getKeyCode();
////			if(event.getCtrlKey()){
////				if(keyCode == 'N' || keyCode == 'n'){
////					formActionListener.addNewItem();
////					DOM.eventPreventDefault(event);
////				}
////				else if(keyCode == KeyCodes.KEY_RIGHT){
////					formActionListener.moveToChild();
////					DOM.eventPreventDefault(event);
////				}
////				else if(keyCode == KeyCodes.KEY_LEFT){
////					formActionListener.moveToParent();
////					DOM.eventPreventDefault(event);
////				}
////				else if(keyCode == KeyCodes.KEY_UP){
////					formActionListener.moveUp();
////					DOM.eventPreventDefault(event);
////				}
////				else if(keyCode == KeyCodes.KEY_DOWN){
////					formActionListener.moveDown();
////					DOM.eventPreventDefault(event);
////				}
////			}
////		}
////	}
//
//	private void enableDescriptionTemplate(boolean enable){
//
//		//desc template is as of now not yet used by JR
//		enable = false;
//
//		//txtDescTemplate.setVisible(enable);
//		btnDescTemplate.setVisible(enable);
//		lblDescTemplate.setVisible(enable);
//		//txtDescTemplate.getParent().setVisible(enable);
//		//lblDescTemplate.setVisible(enable);
//
//		FlexCellFormatter cellFormatter = table.getFlexCellFormatter();
//		cellFormatter.setVisible(9, 0, enable);
//		cellFormatter.setVisible(9, 1, enable);
//
//		//form key
//		cellFormatter.setVisible(10, 0, enable);
//		cellFormatter.setVisible(10, 1, enable);
//		
//		//table.removeStyleName("cw-FlexTable");
//
//		//txtDescTemplate.getParent().setVisible(enable);
//	}
//	
//
//	public void onLocaleSelected(ItextLocale locale) {
//		lblText.setText(LocaleText.get("text") + " (" + locale.getName() + ")");
//		
//	}
	
	
	
}
