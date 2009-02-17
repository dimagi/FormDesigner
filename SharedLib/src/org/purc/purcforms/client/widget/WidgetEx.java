package org.purc.purcforms.client.widget;

import org.purc.purcforms.client.model.QuestionDef;
import org.zenika.widget.client.datePicker.DatePicker;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
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
	
	protected Widget widget;
	
	protected String color;
	protected String fontWeight;
	protected String fontStyle;
	protected String fontSize;
	protected String textDecoration;
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
	
	protected HorizontalPanel panel = new HorizontalPanel();
	protected QuestionDef questionDef;


	public WidgetEx(){
		
	}
	
	public WidgetEx(WidgetEx widget){
		widget.storePosition();
		
		this.left = widget.left;
		this.top = widget.top;
		this.width = widget.width; //getWidth();
		this.height = widget.height; //getHeight();
		this.parentBinding = widget.parentBinding;
		this.binding = widget.binding;
		this.tabIndex = widget.tabIndex;
		
		copyWidget(widget);

		setForeColor(widget.getForeColor());
		setFontWeight(widget.getFontWeight());
		setFontStyle(widget.getFontStyle());
		setFontSize(widget.getFontSize());
		setTextDecoration(widget.getTextDecoration());
		setBackgroundColor(widget.getBackgroundColor());
		setBorderStyle(widget.getBorderStyle());
		setBorderWidth(widget.getBorderWidth());
		setBorderColor(widget.getBorderColor());
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
			this.widget = new DatePicker();
		else if(widget.widget instanceof TextBox)
			this.widget = new TextBox();
		else if(widget.widget instanceof Label){
			this.widget = new Label(((Label)widget.widget).getText());
			((Label)this.widget).setWordWrap(false);
		}

		if(height != null)
			widget.setHeight(height);
		if(width != null)
			widget.setWidth(width);
	}
	
	public Widget getWrappedWidget(){
		return widget;
	}
	
	public void setWidth(String width){
		DOM.setStyleAttribute(widget.getElement(), "width",width);
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
	
	public void setTextDecoration(String textDecoration){
		try{
			DOM.setStyleAttribute(widget.getElement(), "textDecoration", textDecoration);
			this.textDecoration = textDecoration;
		}
		catch(Exception ex){
			//ex.printStackTrace();
		}
	}
	
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
	
	public String getTextDecoration(){
		String s = DOM.getStyleAttribute(widget.getElement(), "textDecoration");
		if(s == null || s.trim().length() == 0)
			s = textDecoration;
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
		 
		 value = node.getAttribute("textDecoration");
		 if(value != null && value.trim().length() > 0)
			 widget.setTextDecoration(value);
		 
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
	
	public void refreshSize(){
		setHeight(getHeight());
		setWidth(getWidth());
	}
}
