package org.purc.purcforms.client.controller;

import org.purc.purcforms.client.widget.DesignGroupWidget;
import org.purc.purcforms.client.widget.DesignWidgetWrapper;


/**
 * 
 * @author daniel
 *
 */
public class WidgetPropertySetter {

	public static final byte PROP_FORE_COLOR = 1;
	public static final byte PROP_WIDTH = 2;
	public static final byte PROP_HEIGHT = 3;
	public static final byte PROP_LEFT= 4;
	public static final byte PROP_TOP = 5;
	public static final byte PROP_FONT_FAMILY = 6;
	public static final byte PROP_FONT_WEIGHT = 7;
	public static final byte PROP_FONT_STYLE = 8;
	public static final byte PROP_FONT_SIZE = 9;
	public static final byte PROP_TEXT_DECORATION = 10;
	public static final byte PROP_TEXT_ALIGN = 11;
	public static final byte PROP_BACKGROUND_COLOR = 12;
	public static final byte PROP_BORDER_STYLE = 13;
	public static final byte PROP_BORDER_COLOR = 14;
	public static final byte PROP_BORDER_WIDTH = 15;
	
	
	public static boolean setProperty(byte property, FormDesignerDragController selectedDragController, String value){

		int count = selectedDragController.getSelectedWidgetCount();
		if(count == 0)
			return false;
		
		for(int index = 0; index < count; index++){
			DesignWidgetWrapper widget = (DesignWidgetWrapper)selectedDragController.getSelectedWidgetAt(index);
			if(widget.getWrappedWidget() instanceof DesignGroupWidget)
				((DesignGroupWidget)widget.getWrappedWidget()).onWidgetPropertyChanged(property, value);
			else{
				switch(property){
					case PROP_FORE_COLOR:
						widget.setForeColor(value);
						break;
					case PROP_WIDTH:
						widget.setWidth(value);
						break;
					case PROP_HEIGHT:
						widget.setHeight(value);
						break;
					case PROP_LEFT:
						widget.setLeft(value);
						break;
					case PROP_TOP:
						widget.setTop(value);
						break;
					case PROP_FONT_FAMILY:
						widget.setFontFamily(value);
						break;
					case PROP_FONT_WEIGHT:
						widget.setFontWeight(value);
						break;
					case PROP_FONT_STYLE:
						widget.setFontStyle(value);
						break;
					case PROP_FONT_SIZE:
						widget.setFontSize(value);
						break;
					case PROP_TEXT_DECORATION:
						widget.setTextDecoration(value);
						break;
					case PROP_TEXT_ALIGN:
						widget.setTextAlign(value);
						break;
					case PROP_BACKGROUND_COLOR:
						widget.setBackgroundColor(value);
						break;
					case PROP_BORDER_STYLE:
						widget.setBorderStyle(value);
						break;
					case PROP_BORDER_COLOR:
						widget.setBorderColor(value);
						break;
					case PROP_BORDER_WIDTH:
						widget.setBorderWidth(value);
						break;
				}
			}
		}
		
		return true;
	}
}
