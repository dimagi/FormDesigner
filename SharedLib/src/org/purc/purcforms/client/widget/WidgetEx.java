package org.purc.purcforms.client.widget;

import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.util.FormUtil;
import org.zenika.widget.client.datePicker.DatePicker;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;


/**
 * 
 * @author daniel
 *
 */
public class WidgetEx extends Composite{
	
	public static final String WIDGET_TYPE_CHECKBOX = "CheckBox";
	public static final String WIDGET_TYPE_RADIOBUTTON = "RadioButton";
	public static final String WIDGET_TYPE_TEXTBOX = "TextBox";
	public static final String WIDGET_TYPE_TEXTAREA = "TextArea";
	public static final String WIDGET_TYPE_GROUPBOX = "GroupBox";
	public static final String WIDGET_TYPE_BUTTON = "Button";
	public static final String WIDGET_TYPE_REPEATSECTION = "RepeatSection";
	public static final String WIDGET_TYPE_LISTBOX = "ListBox";
	public static final String WIDGET_TYPE_LABEL = "Label";
	public static final String WIDGET_TYPE_DATEPICKER = "DatePicker";
	public static final String WIDGET_TYPE_IMAGE = "Picture";
	public static final String WIDGET_TYPE_VIDEO_AUDIO = "VideoAudio";
	
	public static final String WIDGET_PROPERTY_TOP = "Top";
	public static final String WIDGET_PROPERTY_LEFT = "Left";
	public static final String WIDGET_PROPERTY_WIDGETTYPE = "WidgetType";
	public static final String WIDGET_PROPERTY_HELPTEXT = "HelpText";
	public static final String WIDGET_PROPERTY_PARENTBINDING = "ParentBinding";
	public static final String WIDGET_PROPERTY_BINDING = "Binding";
	public static final String WIDGET_PROPERTY_TEXT = "Text";
	public static final String WIDGET_PROPERTY_WIDTH = "Width";
	public static final String WIDGET_PROPERTY_HEIGHT = "Height";
	public static final String WIDGET_PROPERTY_EXTERNALSOURCE = "ExternalSource";
	public static final String WIDGET_PROPERTY_DISPLAYFIELD = "DisplayField";
	public static final String WIDGET_PROPERTY_VALUEFIELD = "ValueField";
	public static final String WIDGET_PROPERTY_TABINDEX = "TabIndex";
	public static final String WIDGET_PROPERTY_REPEATED = "Repeated";
	public static final String WIDGET_PROPERTY_TEXT_ALIGN = "TextAlign";
	public static final String WIDGET_PROPERTY_HEADER_LABEL = "HeaderLabel";
	
	public static final String WIDGET_PROPERTY_BACKGROUND_COLOR = "backgroundColor";
	
	public static final String REPEATED_TRUE_VALUE = "1";
	
	protected Widget widget;
	
	protected String color;
	protected String fontFamily;
	protected String fontWeight;
	protected String fontStyle;
	protected String fontSize;
	protected String textDecoration;
	protected String textAlign;
	protected String backgroundColor;
	protected String borderStyle;
	protected String borderWidth;
	protected String borderColor;
	
	protected String left;
	protected String top;
	protected String width;
	protected String height;
	protected String binding;
	protected String parentBinding;
	protected int tabIndex;
	
	protected String externalSource;
	protected String displayField;
	protected String valueField;
	
	protected boolean isRepeated = false;
	
	protected HorizontalPanel panel = new HorizontalPanel();
	protected QuestionDef questionDef;


	public WidgetEx(){
		
	}
	
	public WidgetEx(WidgetEx widget){
		copyWidgetProperties(widget);
	}
	
	public void copyWidgetProperties(WidgetEx widget){
		widget.storePosition();
		
		this.left = widget.left;
		this.top = widget.top;
		this.width = widget.width; //getWidth();
		this.height = widget.height; //getHeight();
		this.parentBinding = widget.parentBinding;
		this.binding = widget.binding;
		this.tabIndex = widget.tabIndex;
		this.externalSource = widget.externalSource;
		this.displayField = widget.displayField;
		this.valueField = widget.valueField;
		this.isRepeated = widget.isRepeated;
		
		copyWidget(widget);

		setForeColor(widget.getForeColor());
		setFontWeight(widget.getFontWeight());
		setFontStyle(widget.getFontStyle());
		setFontSize(widget.getFontSize());
		setFontFamily(widget.getFontFamily());
		setTextDecoration(widget.getTextDecoration());
		setTextAlign(widget.getTextAlign());
		setBackgroundColor(widget.getBackgroundColor());
		setBorderStyle(widget.getBorderStyle());
		setBorderWidth(widget.getBorderWidth());
		setBorderColor(widget.getBorderColor());
		
		setTitle(widget.getTitle());
		setText(widget.getText());
	}
	
	protected void copyWidget(WidgetEx widget){
		if(widget.widget instanceof RadioButton)
			this.widget = new RadioButton(((RadioButton)widget.widget).getName(),((RadioButton)widget.widget).getText());
		else if(widget.widget instanceof CheckBox)
			this.widget = new CheckBox(((CheckBox)widget.widget).getText());
		else if(widget.widget instanceof Button)
			this.widget = new Button(((Button)widget.widget).getText());
		else if(widget.widget instanceof ListBox)
			this.widget = new ListBox(((ListBox)widget.widget).isMultipleSelect());
		else if(widget.widget instanceof TextArea)
			this.widget = new TextArea();
		else if(widget.widget instanceof DatePicker)
			this.widget = new DatePickerWidget();
		else if(widget.widget instanceof TextBox)
			this.widget = new TextBox();
		else if(widget.widget instanceof Label){
			this.widget = new Label(((Label)widget.widget).getText());
			((Label)this.widget).setWordWrap(false);
		}
		else if(widget.widget instanceof Image){
			this.widget = new Image();
			Image image = (Image)widget.widget;
			((Image)this.widget).setUrl(image.getUrl());
			((Image)this.widget).setVisibleRect(image.getOriginLeft(),image.getOriginTop(),FormUtil.convertDimensionToInt(width) /*image.getWidth()*/,FormUtil.convertDimensionToInt(height) /*image.getHeight()*/);
			//((Image)this.widget).setVisibleRect(image.getOriginLeft(),image.getOriginTop(),image.getWidth(),image.getHeight());
		}
		else if(widget.widget instanceof Hyperlink)
			this.widget = new Hyperlink(((Hyperlink)widget.widget).getText(),null);

		if(height != null)
			widget.setHeight(height);
		if(width != null)
			widget.setWidth(width);
	}
	
	public String getText(){
		if(widget instanceof RadioButton)
			return ((RadioButton)widget).getText();
		else if(widget instanceof CheckBox)
			return ((CheckBox)widget).getText();
		else if(widget instanceof Button)
			return ((Button)widget).getText();
		else if(widget instanceof Label)
			return ((Label)widget).getText();
		else if(widget instanceof TextArea)
			return ((TextArea)widget).getText();
		else if(widget instanceof TextBox)
			return ((TextBox)widget).getText();
		else if(widget instanceof Hyperlink)
			return ((Hyperlink)widget).getText();
		return null;
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
		else if(widget instanceof TextArea)
			((TextArea)widget).setText(text);
		else if(widget instanceof TextBox)
			((TextBox)widget).setText(text);
		else if(widget instanceof Hyperlink)
			((Hyperlink)widget).setText(text);
		else if(widget instanceof TabBar && text != null && text.trim().length() > 0)
			((TabBar)widget).setTabHTML(((TabBar)widget).getSelectedTab(), "<span style='white-space:nowrap'>" + text + "</span>");

	}
	
	public String getTitle(){
		if(widget instanceof RadioButton)
			return ((RadioButton)widget).getTitle();
		else if(widget instanceof CheckBox)
			return ((CheckBox)widget).getTitle();
		else if(widget instanceof Button)
			return ((Button)widget).getTitle();
		else if(widget instanceof ListBox)
			return ((ListBox)widget).getTitle();
		else if(widget instanceof TextArea)
			return ((TextArea)widget).getTitle();
		else if(widget instanceof DatePicker)
			return ((DatePicker)widget).getTitle();
		else if(widget instanceof TextBox)
			return ((TextBox)widget).getTitle();
		else if(widget instanceof Label)
			return ((Label)widget).getTitle();
		else if(widget instanceof Image)
			return ((Image)widget).getTitle();
		else if(widget instanceof Hyperlink)
			return ((Hyperlink)widget).getTitle();
		return null;
	}

	
	public void setTitle(String title){
		if(widget instanceof RadioButton)
			((RadioButton)widget).setTitle(title);
		else if(widget instanceof CheckBox)
			((CheckBox)widget).setTitle(title);
		else if(widget instanceof Button)
			((Button)widget).setTitle(title);
		else if(widget instanceof ListBox)
			((ListBox)widget).setTitle(title);
		else if(widget instanceof TextArea)
			((TextArea)widget).setTitle(title);
		else if(widget instanceof DatePicker)
			((DatePicker)widget).setTitle(title);
		else if(widget instanceof TextBox)
			((TextBox)widget).setTitle(title);
		else if(widget instanceof Label)
			((Label)widget).setTitle(title);
		else if(widget instanceof Image)
			((Image)widget).setTitle(title);
		else if(widget instanceof Hyperlink)
			((Hyperlink)widget).setTitle(title);
	}
	
	public Widget getWrappedWidget(){
		return widget;
	}
	
	public Widget getWrappedWidgetEx(){
		return panel.getWidget(0);
	}
	
	public void setWidth(String width){
		if("100%".equalsIgnoreCase(this.width)) //Temporary hack for group header label which should always have a width of 100%
			return;
		
		DOM.setStyleAttribute(widget.getElement(), "width",width);
		DOM.setStyleAttribute(getElement(), "width", width); //For setting width of group labels which is 100% and hence their wrapper also need to be at 100%
		this.width = width;
	}

	public void setHeight(String height){
		DOM.setStyleAttribute(widget.getElement(), "height",height);
		this.height = height;
	}

	public String getWidth(){
		String s = DOM.getStyleAttribute(widget.getElement(), "width");
		if(s == null || s.trim().length() == 0)
			s = width;
		return s;
	}

	public String getHeight(){
		String s = DOM.getStyleAttribute(widget.getElement(), "height");
		if(s == null || s.trim().length() == 0)
			s = height;
		return s;
	}
	
	public void setForeColor(String color){
		try{
			DOM.setStyleAttribute(widget.getElement(), "color", color);
			this.color = color; 
		}
		catch(Exception ex){
			//ex.printStackTrace();
		}
	}

	public void setFontWeight(String fontWeight){
		try{
			DOM.setStyleAttribute(widget.getElement(), "fontWeight", fontWeight);
			this.fontWeight = fontWeight;
		}
		catch(Exception ex){
			//ex.printStackTrace();
		}
	}

	public void setFontStyle(String fontStyle){
		try{
			DOM.setStyleAttribute(widget.getElement(), "fontStyle", fontStyle);
			this.fontStyle = fontStyle;
		}
		catch(Exception ex){
			//ex.printStackTrace();
		}
	}

	public void setFontSize(String fontSize){
		try{
			DOM.setStyleAttribute(widget.getElement(), "fontSize", fontSize);
			this.fontSize = fontSize;
		}
		catch(Exception ex){
			//ex.printStackTrace();
		}
	}
	
	public void setFontFamily(String fontFamily){
		try{
			DOM.setStyleAttribute(widget.getElement(), "fontFamily", fontFamily);
			this.fontFamily = fontFamily;
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void setTextDecoration(String textDecoration){
		try{
			DOM.setStyleAttribute(widget.getElement(), "textDecoration", textDecoration);
			this.textDecoration = textDecoration;
		}
		catch(Exception ex){
			//ex.printStackTrace();
		}
	}
	
	public void setTextAlign(String textAlign){
		try{
			DOM.setStyleAttribute(widget.getElement(), "textAlign", textAlign);
			this.textAlign = textAlign;
		}
		catch(Exception ex){
			//ex.printStackTrace();
		}
	}
	
	//DOM.setStyleAttribute(wrapper.getWrappedWidget().getElement(),"textAlign", "center");

	
	public void setBackgroundColor(String backgroundColor){
		try{
			DOM.setStyleAttribute(widget.getElement(), "backgroundColor", backgroundColor);
			this.backgroundColor = backgroundColor;
		}
		catch(Exception ex){
			//ex.printStackTrace();
		}
	}
	
	public void setBorderStyle(String borderStyle){
		try{
			DOM.setStyleAttribute(widget.getElement(), "borderStyle", borderStyle);
			this.borderStyle = borderStyle;
		}
		catch(Exception ex){
			//ex.printStackTrace();
		}
	}
	
	public void setBorderWidth(String borderWidth){
		try{
			DOM.setStyleAttribute(widget.getElement(), "borderWidth", borderWidth);
			this.borderWidth = borderWidth;
		}
		catch(Exception ex){
			//ex.printStackTrace();
		}
	}
	
	public void setBorderColor(String borderColor){
		try{
			DOM.setStyleAttribute(widget.getElement(), "borderColor", borderColor);
			this.borderColor = borderColor;
		}
		catch(Exception ex){
			//ex.printStackTrace();
		}
	}

	
	public String getForeColor(){
		String s = DOM.getStyleAttribute(widget.getElement(), "color");
		if(s == null || s.trim().length() == 0)
			s = color;
		return s;
	}
	
	public String getFontWeight(){
		String s = DOM.getStyleAttribute(widget.getElement(), "fontWeight");
		if(s == null || s.trim().length() == 0)
			s = fontWeight;
		return s;
	}
	
	public String getFontStyle(){
		String s = DOM.getStyleAttribute(widget.getElement(), "fontStyle");
		if(s == null || s.trim().length() == 0)
			s = fontStyle;
		return s;
	}
	
	public String getFontSize(){
		String s = DOM.getStyleAttribute(widget.getElement(), "fontSize");
		if(s == null || s.trim().length() == 0)
			s = fontSize;
		return s;
	}
	
	public String getFontFamily(){
		String s = DOM.getStyleAttribute(widget.getElement(), "fontFamily");
		if(s == null || s.trim().length() == 0){
			if(fontFamily == null || fontFamily.trim().length() == 0)
				fontFamily = FormUtil.getDefaultFontFamily();
			s = fontFamily;
		}
		return s;
	}
	
	public String getTextDecoration(){
		String s = DOM.getStyleAttribute(widget.getElement(), "textDecoration");
		if(s == null || s.trim().length() == 0)
			s = textDecoration;
		return s;
	}
	
	public String getTextAlign(){
		String s = DOM.getStyleAttribute(widget.getElement(), "textAlign");
		if(s == null || s.trim().length() == 0)
			s = textAlign;
		return s;
	}
	
	public String getBackgroundColor(){
		String s = DOM.getStyleAttribute(widget.getElement(), "backgroundColor");
		if(s == null || s.trim().length() == 0)
			s = backgroundColor;
		return s;
	}
	
	public String getBorderStyle(){
		String s = DOM.getStyleAttribute(widget.getElement(), "borderStyle");
		if(s == null || s.trim().length() == 0)
			s = borderStyle;
		return s;
	}
	
	public String getBorderWidth(){
		String s = DOM.getStyleAttribute(widget.getElement(), "borderWidth");
		if(s == null || s.trim().length() == 0)
			s = borderWidth;
		return s;
	}
	
	public String getBorderColor(){
		String s = DOM.getStyleAttribute(widget.getElement(), "borderColor");
		if(s == null || s.trim().length() == 0)
			s = borderColor;
		return s;
	}
	
	public static void loadLabelProperties(Element node,WidgetEx widget){
		 String value = node.getAttribute("color");
		 if(value != null && value.trim().length() > 0)
			 widget.setForeColor(value);
		 
		 value = node.getAttribute("fontWeight");
		 if(value != null && value.trim().length() > 0)
			 widget.setFontWeight(value);
		 
		 value = node.getAttribute("fontStyle");
		 if(value != null && value.trim().length() > 0)
			 widget.setFontStyle(value);
		 
		 value = node.getAttribute("fontSize");
		 if(value != null && value.trim().length() > 0)
			 widget.setFontSize(value);
		 
		 value = node.getAttribute("fontFamily");
		 if(value != null && value.trim().length() > 0)
			 widget.setFontFamily(value);
		 
		 value = node.getAttribute("textDecoration");
		 if(value != null && value.trim().length() > 0)
			 widget.setTextDecoration(value);
		 
		 value = node.getAttribute("textAlign");
		 if(value != null && value.trim().length() > 0)
			 widget.setTextAlign(value);
		 
		 value = node.getAttribute("backgroundColor");
		 if(value != null && value.trim().length() > 0)
			 widget.setBackgroundColor(value);
		 
		 value = node.getAttribute("borderStyle");
		 if(value != null && value.trim().length() > 0)
			 widget.setBorderStyle(value);
		 
		 value = node.getAttribute("borderWidth");
		 if(value != null && value.trim().length() > 0)
			 widget.setBorderWidth(value);
		 
		 value = node.getAttribute("borderColor");
		 if(value != null && value.trim().length() > 0)
			 widget.setBorderColor(value);
		 
		 value = node.getAttribute("textAlign");
		 if(value != null && value.trim().length() > 0)
			 widget.setTextAlign(value);
	 }
	
	public String getLeft(){
		String s = DOM.getStyleAttribute(getElement(), "left");
		if(s == null || s.trim().length() == 0)
			s = left;
		else
			left = s;
		return s;
	}

	public String getTop(){
		String s = DOM.getStyleAttribute(getElement(), "top");
		if(s == null || s.trim().length() == 0)
			s = top;
		else
			top = s;
		return s;
	}
	
	public void storePosition(){
		left = getLeft();
		top  = getTop();
		width = getWidth();
		height = getHeight();
	}
	
	public void restorePosition(){
		setLeft(left);
		setTop(top);
		setWidth(width);
		setHeight(height);
	}
	
	public void refreshSize(){
		setHeight(getHeight());
		setWidth(getWidth());
	}
	
	public String getExternalSource(){
		return externalSource;
	}
	
	public void setExternalSource(String externalSource){
		this.externalSource = externalSource;
	}
	
	public String getDisplayField(){
		return displayField;
	}
	
	public void setDisplayField(String displayField){
		this.displayField = displayField;
	}
	
	public String getValueField(){
		return valueField;
	}
	
	public void setValueField(String valueField){
		this.valueField = valueField;
	}
	
	public String getBinding(){
		return binding;
	}

	public void setBinding(String binding){
		this.binding = binding;
	}

	public void setParentBinding(String parentBinding){
		this.parentBinding = parentBinding;
	}

	public String getParentBinding(){
		return parentBinding;
	}
	
	public boolean isRepeated(){
		return isRepeated;
	}
	
	public void setRepeated(boolean isRepeated){
		this.isRepeated = isRepeated;
	}
	
	public String getTheOffsetHeight(){  
		return String.valueOf(getOffsetHeight());
	}

	public String getTheOffsetWidth(){  
		return String.valueOf(getOffsetWidth());
	}

	public void setLeft(String sLeft){
		DOM.setStyleAttribute(getElement(), "left",sLeft);
	}

	public void setTop(String sTop){
		DOM.setStyleAttribute(getElement(), "top",sTop);
	}
	
	public int getTabIndex(){
		return tabIndex;
	}
	
	public void setTabIndex(int tabIndex){
		this.tabIndex = tabIndex;
	}
}
