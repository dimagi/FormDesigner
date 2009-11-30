package org.purc.purcforms.client.widget;

import java.util.Date;

import org.zenika.widget.client.util.DateUtil;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ChangeListenerCollection;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Widget;



/**
 * Main class of the DatePicker. It extends the TextBox widget and manages a Date object.
 * When it is clicked, it opens a PopupCalendar on which we can select a new date. <br>
 * Example of use :  <br>
 * <code>
 * DatePicker datePicker = new DatePicker();<br>
 * RootPanel.get().add(datePicker);<br>
 * </code>
 * You can specify a theme (see the CSS file DatePickerStyle.css) and
 * the date to initialize the date picker.
 * Enjoy xD
 * 
 * Modified later by James to include change listening
 * @author Nicolas Wetzel (nicolas.wetzel@zenika.com)
 * @author Jean-Philippe Dournel
 * @author Loïc Bertholet
 * @author James Heggs (jheggs@axonbirch.com)
 * 
 */
public class DatePickerEx extends TextBoxWidget implements ClickListener, ChangeListener, KeyboardListener {
	private PopupCalendarEx popup;
	private Date selectedDate;
	// the oldest date that can be selected
	private Date oldestDate;
	// the youngest date that can be selected
	private Date youngestDate;
	private DateTimeFormat dateFormatter;
	
	private ChangeListenerCollection changeListeners;
	
	{
		dateFormatter = DateUtil.getDateTimeFormat();
		popup = new PopupCalendarEx(this);
		changeListeners = new ChangeListenerCollection();
	}

	/**
	 * Default constructor. It creates a DatePicker which shows the current
	 * month.
	 */
	public DatePickerEx() {
		super();
		setText("");	
		sinkEvents(Event.ONCHANGE | Event.ONKEYPRESS);
		addClickListener(this);
		addChangeListener(this);
		addKeyboardListener(this);
	}

	/**
	 * Create a DatePicker which show a specific Date.
	 * @param selectedDate Date to show
	 */
	public DatePickerEx(Date selectedDate) {
		this();
		this.selectedDate = selectedDate;
		synchronizeFromDate();
	}
	
	/**
	 * Create a DatePicker which uses a specific theme.
	 * @param theme Theme name
	 */
	public DatePickerEx(String theme) {
		this();
		setTheme(theme);
	}
	
	/**
	 * Create a DatePicker which specifics date and theme.
	 * @param selectedDate Date to show
	 * @param theme Theme name
	 */
	public DatePickerEx(Date selectedDate, String theme) {
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

		changeListeners.fireChange(this);
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
		switch (DOM.eventGetType(event)) {
		case Event.ONBLUR:
			popup.hidePopupCalendar();
			break;
		default:
			break;

		}
		super.onBrowserEvent(event);
	}
	
	/**
	 * @see com.google.gwt.user.client.ui.ClickListener#onClick(com.google.gwt.user.client.ui.Widget)
	 */
	public void onClick(Widget sender) {
		showPopup();
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
	public void onKeyPress(Widget sender, char keyCode, int modifiers) {
		switch (keyCode) {
		case KEY_ENTER:
			parseDate();
			showPopup();
			break;
		case KEY_ESCAPE:
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
		if (this.selectedDate != null) {
			popup.setDisplayedMonth(this.selectedDate);
		}
		popup.setPopupPosition(this.getAbsoluteLeft()+150, this.getAbsoluteTop());
		popup.displayMonth();
		doAfterShowPopup(popup);
	}
	
	/**
	 * Call when the calendar pop-up is shown. Can be overwritten.
	 * @param popup
	 */
	protected void doAfterShowPopup(PopupCalendarEx popup) {
		// nothing special to do
		
		// popup position can be updated so that it is displayed "above" the TexrBox
		// popup.setPopupPosition(popup.getPopupLeft(), popup.getPopupTop() - 150);
		
		// there is also a known bug if the DatePicker is in a modal MyGWT Dialog
		// DOM.setIntStyleAttribute(popup.getElement(), "zIndex", MyDOM.getZIndex());
		// Element p = DOM.getParent(popup.getElement());
		// int index = DOM.getChildIndex(p, popup.getElement());
		// p = DOM.getChild(p, --index);
		// DOM.setIntStyleAttribute(p, "zIndex", MyDOM.getZIndex() - 2);
	}

	/**
	 * Parse the date entered in the DatePicker.
	 */
	private void parseDate() {
		if (getText() == null || getText().length() == 0) {
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
	public void addChangeListener(ChangeListener listener) {
		super.addChangeListener(listener);
		if (changeListeners == null) {
			changeListeners = new ChangeListenerCollection();
		}
		changeListeners.add(listener);
	}

	/**
	 * @see com.google.gwt.user.client.ui.TextBoxBase#removeChangeListener(com.google.gwt.user.client.ui.ChangeListener)
	 */
	public void removeChangeListener(ChangeListener listener) {
		super.removeChangeListener(listener);
		if (changeListeners != null) {
			changeListeners.remove(listener);
		}
	}
}
