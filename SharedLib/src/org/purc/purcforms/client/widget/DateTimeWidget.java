package org.purc.purcforms.client.widget;

import java.util.Date;

import org.purc.purcforms.client.util.FormUtil;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;


/**
 * The widget for editing DateTime question types.
 * 
 * @author daniel
 *
 */
public class DateTimeWidget extends Composite{

	private HorizontalPanel panel = new HorizontalPanel();
	private DatePickerWidget dateWidget = new DatePickerWidget();
	private TimeWidget timeWidget = new TimeWidget();

	
	/**
	 * Creates a new instance of the date and time widget.
	 */
	public DateTimeWidget(){

		initWidget(panel);

		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		panel.setSpacing(0);

		panel.add(dateWidget);
		panel.add(timeWidget);
		
		panel.setCellWidth(timeWidget, "48%");
		panel.setCellWidth(dateWidget, "52%");
		
		dateWidget.setWidth("100%");
		timeWidget.setWidth("100%");
		
		dateWidget.setHeight("100%");
		timeWidget.setHeight("100%");

		sinkEvents(Event.getTypeInt(KeyDownEvent.getType().getName()));
		
		addEventHandlers();
	}


	/**
	 * Adds the event handlers for this widget.
	 */
	private void addEventHandlers(){
		((TextBox)dateWidget).addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				if(getParent().getParent() instanceof RuntimeWidgetWrapper)
					timeWidget.setFocus(true); //((RuntimeWidgetWrapper)getParent().getParent()).moveToNextWidget();
			}
		});
	}


	@Override
	public void onBrowserEvent(Event event){
		if(DOM.eventGetType(event) == Event.ONKEYDOWN){
			if(event.getKeyCode() == KeyCodes.KEY_ENTER ){
				if(event.getTarget() != timeWidget.getElement()){
					dateWidget.close();
					event.preventDefault();
					event.stopPropagation();
					timeWidget.setFocus(true);
					return;
				}
				else if(getParent().getParent() instanceof RuntimeWidgetWrapper)
					((RuntimeWidgetWrapper)getParent().getParent()).moveToNextWidget();
			}
		}

		super.onBrowserEvent(event);
	}

	
	/**
	 * Sets the widget tab index.
	 * 
	 * @param index the tab index to set.
	 */
	public void setTabIndex(int index) {
		dateWidget.setTabIndex(index);
		timeWidget.setTabIndex(index);
	}

	
	/**
	 * Gets the widget tab index;
	 * 
	 * @return the tab index
	 */
	public int getTabIndex(){
		return dateWidget.getTabIndex();
	}

	/**
	 * Gets the display text for the widget.
	 * 
	 * @return the text.
	 */
	public String getText(){
		if(dateWidget.getText().trim().length() == 0 && timeWidget.getText().trim().length() == 0)
			return "";

		return dateWidget.getText() + " " + timeWidget.getTextWithMask();
	}

	
	/**
	 * Sets the display text for the widget.
	 * 
	 * @param text the text to set.
	 */
	public void setText(String text){
		if(text == null || text.trim().length() == 0){
			dateWidget.setText(null);
			timeWidget.setText(null);
		}
		else{
			Date date = FormUtil.getDateTimeSubmitFormat().parse(text);
			dateWidget.setText(FormUtil.getDateDisplayFormat().format(date));
			timeWidget.setText(FormUtil.getTimeDisplayFormat().format(date));
		}
	}

	
	/**
	 * @see com.google.gwt.user.client.ui.FocusWidget#setFocus(boolean)
	 */
	public void setFocus(boolean focused){
		dateWidget.setFocus(focused);
	}

	
	/**
	 * @see com.google.gwt.user.client.ui.FocusWidget#setEnabled(boolean)
	 * @param enabled
	 */
	public void setEnabled(boolean enabled){
		dateWidget.setEnabled(enabled);
		timeWidget.setEnabled(enabled);
	}
	
	/**
	 * Sets any css style for the widget.
	 * 
	 * @param name the style name.
	 * @param value the style value.
	 */
	public void setStyle(String name, String value){
		DOM.setStyleAttribute(dateWidget.getElement(), "cursor", value);
		DOM.setStyleAttribute(timeWidget.getElement(), "cursor", value);
	}
	
	/**
	 * @see com.google.gwt.user.client.ui.FocusWidget#isEnabled()
	 */
	public boolean isEnabled(){
		return dateWidget.isEnabled();
	}
	
	
	public void addChangeHandler(ChangeHandler handler) {
		dateWidget.addChangeHandler(handler);
		timeWidget.addChangeHandler(handler);
	}
	
	
	public void addKeyUpHandler(KeyUpHandler handler) {
		dateWidget.addKeyUpHandler(handler);
		timeWidget.addKeyUpHandler(handler);
	}
	
	
	public void setFontFamily(String fontFamily){
		try{
			DOM.setStyleAttribute(dateWidget.getElement(), "fontFamily", fontFamily);
			DOM.setStyleAttribute(timeWidget.getElement(), "fontFamily", fontFamily);
		}
		catch(Exception ex){
			//ex.printStackTrace();
		}
	}
	
	public void setFontSize(String fontSize){
		try{
			DOM.setStyleAttribute(dateWidget.getElement(), "fontSize", fontSize);
			DOM.setStyleAttribute(timeWidget.getElement(), "fontSize", fontSize);
		}
		catch(Exception ex){
			//ex.printStackTrace();
		}
	}
}
