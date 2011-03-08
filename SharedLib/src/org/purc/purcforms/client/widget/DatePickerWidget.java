package org.purc.purcforms.client.widget;

import java.util.Date;

import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.util.FormUtil;
import org.zenika.widget.client.util.DateUtil;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ChangeListenerCollection;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * The date picker widget.
 * 
 * @author daniel
 *
 */
public class DatePickerWidget extends DatePickerEx implements KeyPressHandler, ClickHandler{

	private PopupCalendarEx popup;
	private Date selectedDate;
	// the oldest date that can be selected
	private Date oldestDate;
	// the youngest date that can be selected
	private Date youngestDate;
	private DateTimeFormat dateFormatter;

	{
		DateTimeFormat dateTimeFormat = FormUtil.getDateDisplayFormat();
		if(dateTimeFormat == null)
			dateTimeFormat = DateUtil.getDateTimeFormat();
		dateFormatter = dateTimeFormat;
		popup = new PopupCalendarEx(this);
		new ChangeListenerCollection();
	}

	/**
	 * Default constructor. It creates a DatePicker which shows the current
	 * month.
	 */
	public DatePickerWidget() {
		super();
		setText("");	
		sinkEvents(Event.ONCHANGE | Event.ONKEYPRESS);
		addClickHandler(this);
		addChangeHandler(this);
		addKeyPressHandler(this);
	}

	/**
	 * Create a DatePicker which show a specific Date.
	 * @param selectedDate Date to show
	 */
	public DatePickerWidget(Date selectedDate) {
		this();
		this.selectedDate = selectedDate;
		synchronizeFromDate();
	}

	/**
	 * Create a DatePicker which uses a specific theme.
	 * @param theme Theme name
	 */
	public DatePickerWidget(String theme) {
		this();
		setTheme(theme);
	}

	/**
	 * Create a DatePicker which specifics date and theme.
	 * @param selectedDate Date to show
	 * @param theme Theme name
	 */
	public DatePickerWidget(Date selectedDate, String theme) {
		this(selectedDate);
		setTheme(theme);
	}

	/**
	 * Return the Date contained in the DatePicker.
	 * @return The Date
	 */
	public Date getSelectedDate() {
		return selectedDate;
	}

	/**
	 * Set the Date of the datePicker and synchronize it with the display.
	 * @param value
	 */
	public void setSelectedDate(Date value) {
		this.selectedDate = value;

		synchronizeFromDate();

		//onChange(this);
		//fireEvent(Document.get().createChangeEvent());
		//changeListeners.fireChange(this);
		
		//DomEvent.fireNativeEvent(Document.get().createChangeEvent(), this);
		
		FormUtil.fireChangeEvent(getElement());
	}

	/**
	 * Return the theme name.
	 * @return Theme name
	 */
	public String getTheme() {
		return popup.getTheme();
	}

	/**
	 * Set the theme name.
	 * @param theme Theme name
	 */
	public void setTheme(String theme) {
		popup.setTheme(theme);
	}

	/**
	 * @see com.google.gwt.user.client.ui.TextBoxBase#onBrowserEvent(com.google.gwt.user.client.Event)
	 */
	public void onBrowserEvent(Event event) {
		if(getParent().getParent() instanceof RuntimeWidgetWrapper &&
				((RuntimeWidgetWrapper)getParent().getParent()).isLocked()){
			return;
		}
		
		switch (DOM.eventGetType(event)) {
		case Event.ONBLUR:
			popup.hidePopupCalendar();
			//fireChangeEvent(this); //Commented out because it puts focus to widget next to date whenever a form does validation 
			break;
		default:
			break;

		}
		super.onBrowserEvent(event);
	}
	
	public void fireChangeEvent(final HasHandlers handlerSource) {
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				Timer t = new Timer() {
					public void run() {
						DomEvent.fireNativeEvent(Document.get().createChangeEvent(), handlerSource);
					}
				};
				t.schedule(300);
			}
		});
	}

	/**
	 * @see com.google.gwt.user.client.ui.ClickListener#onClick(com.google.gwt.user.client.ui.Widget)
	 */
	public void onClick(ClickEvent event) {
		//showPopup();
	}

	/**
	 * @see com.google.gwt.user.client.ui.ChangeListener#onChange(com.google.gwt.user.client.ui.Widget)
	 */
	public void onChange(Widget sender) {
		parseDate();
	}

	/**
	 * @see com.google.gwt.user.client.ui.KeyboardListener#onKeyPress(com.google.gwt.user.client.ui.Widget,
	 *      char, int)
	 */
	public void onKeyPress(KeyPressEvent event) {
		switch (event.getCharCode()) {
		case KeyCodes.KEY_ENTER:
			parseDate();
			showPopup();
			break;
		case KeyCodes.KEY_ESCAPE:
			if (popup.isVisible())
				popup.hidePopupCalendar();
			break;
		default:
			break;
		}
	}

	/**
	 * @see com.google.gwt.user.client.ui.KeyboardListener#onKeyDown(com.google.gwt.user.client.ui.Widget, char, int)
	 */
	public void onKeyDown(Widget sender, char keyCode, int modifiers) {
		// Nothing to do		
	}

	/**
	 * @see com.google.gwt.user.client.ui.KeyboardListener#onKeyUp(com.google.gwt.user.client.ui.Widget, char, int)
	 */
	public void onKeyUp(Widget sender, char keyCode, int modifiers) {
		// Nothing to do		
	}

	/**
	 * Display the date in the DatePicker.
	 */
	public void synchronizeFromDate() {
		if (this.selectedDate != null) {
			this.setText(dateFormatter.format(this.selectedDate));
		} else {
			this.setText("");
		}
	}

	/**
	 * Display the PopupCalendar.
	 */
	private void showPopup() {
		if(getParent().getParent() instanceof RuntimeWidgetWrapper){
			QuestionDef questionDef = ((RuntimeWidgetWrapper)getParent().getParent()).getQuestionDef();
			if(questionDef != null && !questionDef.isLocked()){
				if (this.selectedDate != null)
					popup.setDisplayedMonth(this.selectedDate);
				popup.setPopupPosition(this.getAbsoluteLeft()+150, this.getAbsoluteTop());
				popup.displayMonth();
			}
		}
	}

	/**
	 * Parse the date entered in the DatePicker.
	 */
	private void parseDate() {
		if (getText() == null || getText().trim().length() == 0) {
			selectedDate = null;
		} else {
			try {
				Date parsedDate = dateFormatter.parse(getText());
				if (canBeSelected(parsedDate))
					selectedDate = parsedDate;
			} catch (IllegalArgumentException e) {
				// Do something ?
			}
		}
		synchronizeFromDate();
	}

	/**
	 * Return true if the selectedDay is between datepicker's interval dates.
	 * 
	 * @param selectedDay
	 * @return boolean
	 */
	public boolean canBeSelected(Date selectedDay) {
		if (this.getOldestDate() != null
				&& selectedDay.after(this.getOldestDate()))
			return false;

		if (this.getYoungestDate() != null
				&& !DateUtil.addDays(selectedDay, 1).after(this.getYoungestDate()))
			return false;

		return true;
	}

	public Date getOldestDate() {
		return oldestDate;
	}

	public void setOldestDate(Date oldestDate) {
		this.oldestDate = oldestDate;
	}

	public Date getYoungestDate() {
		return youngestDate;
	}

	public void setYoungestDate(Date youngestDate) {
		this.youngestDate = youngestDate;
	}

	/**
	 * @see com.google.gwt.user.client.ui.TextBoxBase#addChangeListener(com.google.gwt.user.client.ui.ChangeListener)
	 */
	public HandlerRegistration addChangeHandler(ChangeHandler listener) {
		return super.addChangeHandler(listener);
		/*if (changeListeners == null) {
			changeListeners = new ChangeListenerCollection();
		}
		changeListeners.add(listener);*/
	}

	/**
	 * @see com.google.gwt.user.client.ui.TextBoxBase#removeChangeListener(com.google.gwt.user.client.ui.ChangeListener)
	 */
	public void removeChangeListener(ChangeListener listener) {
		super.removeChangeListener(listener);
		/*if (changeListeners != null) {
			changeListeners.remove(listener);
		}*/
	}
	
	public void close(){
		if(RootPanel.get().getWidgetCount() > 0)
			RootPanel.get().remove(0);
	}
}
