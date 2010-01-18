package org.purc.purcforms.client.widget;

import java.util.Date;

import org.purc.purcforms.client.util.FormUtil;
import org.zenika.widget.client.util.DateUtil;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;


/**
 * Popup used by the datePicker. It represents a calendar and allows the user to
 * select a date. It is localizable thanks to the DateTimerFormat class(GWT
 * class) and the DateLocale class.
 * 
 * @author Nicolas Wetzel (nicolas.wetzel@zenika.com)
 * @author Jean-Philippe Dournel
 */
public class PopupCalendarEx extends PopupPanel {
	private boolean leave;
	private String theme;
	private final DatePickerEx datePicker;
	private DateTimeFormat dayNameFormat;
	private DateTimeFormat monthFormat;
	private DateTimeFormat dayNumberFormat;
	private Label currentMonth;
	private Grid daysGrid;
	private Date displayedMonth;

	{
		this.leave = true;
		this.theme = "blue";
		this.dayNameFormat = DateTimeFormat.getFormat("E");
		this.monthFormat = DateTimeFormat.getFormat("MMMM yyyy");
		this.dayNumberFormat = DateTimeFormat.getFormat("d");
		this.daysGrid = new Grid(7, 7);
	}

	/**
	 * Create a calendar popup. You have to call the displayMonth method to
	 * display the the popup.
	 * 
	 * @param datePicker
	 *            The date picker on which the popup is attached
	 */
	public PopupCalendarEx(DatePickerEx datePicker) {
		super(true);
		this.datePicker = datePicker;
		this.setStyleName(theme + "-date-picker");
		VerticalPanel panel = new VerticalPanel();
		this.add(panel);

		sinkEvents(Event.ONBLUR);

		drawMonthLine(panel);
		drawWeekLine(panel);
		drawDayGrid(panel);
	}
	
	/**
	 * Return the month displayed by the PopupCalendar.
	 * @return a Date pointing to the month
	 */
	public Date getDisplayedMonth() {
		return displayedMonth;
	}

	/**
	 * Set the month which is display by the PopupCalendar.
	 * @param displayedMonth The Date to display
	 */
	public void setDisplayedMonth(Date displayedMonth) {
		this.displayedMonth = displayedMonth;
	}
	
	/**
	 * Return the theme used by the PopupCalendar.
	 * @return Name of the theme
	 */
	public String getTheme() {
		return this.theme;
	}

	/**
	 * Set the theme used by the PopupCalendar.
	 * @param theme Name of the theme
	 */
	public void setTheme(String theme) {
		this.theme = theme;
		this.setStyleName(theme + "-date-picker");
	}	

	/**
	 * Refresh the PopupCalendar and show it.
	 */
	public void displayMonth() {
		if (this.displayedMonth == null) {
			if (datePicker.getSelectedDate() != null)
				this.displayedMonth = datePicker.getSelectedDate();
			else {
				this.displayedMonth = new Date();
			}
		}
		this.drawLabelMoisAnnee();
		this.drawDaysGridContent(this.displayedMonth);
		show();
	}

	/**
	 * This method is destined to be used by the DatePicker in case of focus lost.
	 * It creates a delay before the popup hides to allows the popup to catch
	 * a click and eventually update the Date of the DatePicker.
	 */
	public void hidePopupCalendar() {
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				Timer t = new Timer() {
					public void run() {
						if (leave) {
							hide();
						} else {
							leave = true;
						}
					}
				};
				t.schedule(300);
			}

		});
	}

	/**
	 * Draw the monthLine with contains navigations buttons (change the month
	 * and the year) and displayed the displayed month.
	 * 
	 * @param panel
	 *            The panel contained in the popup
	 */
	private void drawMonthLine(Panel panel) {
		Grid monthLine = new Grid(1, 5);
		monthLine.setStyleName(theme + "-" + "month-line");
		CellFormatter monthCellFormatter = monthLine.getCellFormatter();

		Label previousYear = new Label("«");
		FormUtil.setElementFontSizeAndFamily(previousYear.getElement());
		previousYear.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				leave = false;
				PopupCalendarEx.this.changeMonth(-12);
			}
		});
		monthLine.setWidget(0, 0, previousYear);
		Label previousMonth = new Label("‹");
		FormUtil.setElementFontSizeAndFamily(previousMonth.getElement());
		previousMonth.addClickListener(new ClickListener() {
			public void onClick(com.google.gwt.user.client.ui.Widget sender) {
				leave = false;
				PopupCalendarEx.this.changeMonth(-1);
			};
		});
		monthLine.setWidget(0, 1, previousMonth);
		monthCellFormatter.setWidth(0, 2, "60%");
		currentMonth = new Label();
		FormUtil.setElementFontSizeAndFamily(currentMonth.getElement());
		currentMonth.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				leave = false;
			}
		});
		monthLine.setWidget(0, 2, currentMonth);
		Label nextMonth = new Label("›");
		FormUtil.setElementFontSizeAndFamily(nextMonth.getElement());
		nextMonth.addClickListener(new ClickListener() {
			public void onClick(com.google.gwt.user.client.ui.Widget sender) {
				leave = false;
				PopupCalendarEx.this.changeMonth(1);
			};
		});
		monthLine.setWidget(0, 3, nextMonth);
		Label nextYear = new Label("»");
		FormUtil.setElementFontSizeAndFamily(nextYear.getElement());
		nextYear.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				leave = false;
				PopupCalendarEx.this.changeMonth(12);
			}
		});
		monthLine.setWidget(0, 4, nextYear);
		panel.add(monthLine);
	}

	/**
	 * Draw the week line which displays first letter of week days. example : S
	 * M T ....etc
	 * 
	 * @param panel
	 *            The panel contained in the popup
	 */
	private void drawWeekLine(Panel panel) {
		Grid weekLine = new Grid(1, 7);
		CellFormatter cf = weekLine.getCellFormatter();
		
		weekLine.setStyleName(theme + "-" + "week-line");
		Date weekFirstday = DateUtil.getWeekFirstDay();
		for (int i = 0; i < 7; i++) {
			weekLine.setText(0, i, dayNameFormat.format(
					DateUtil.addDays(weekFirstday, i)).substring(0, 1)
					.toUpperCase());
			
			FormUtil.setElementFontSizeAndFamily(cf.getElement(0, i));
		}
		panel.add(weekLine);
	}

	/**
	 * Display the grid which contains the days. When a day is clicked, it
	 * updates the Date contained in the DatePicker.
	 * @param panel
	 *            The panel contained in the popup
	 */
	private void drawDayGrid(Panel panel) {
		this.daysGrid.addTableListener(new TableListener() {
			public void onCellClicked(SourcesTableEvents sender, int row,
					int cell) {
				Date selectedDay = DateUtil.addDays(
						getDaysGridOrigin(displayedMonth), row * 7 + cell);
				if (datePicker.canBeSelected(selectedDay)) {
					datePicker.setSelectedDate(selectedDay);
					datePicker.synchronizeFromDate();
					PopupCalendarEx.this.hide();
					leave = true;
				}
			};
		});
		daysGrid.setStyleName(theme + "-" + "day-grid");
		panel.add(daysGrid);
	}
	
	/**
	 * Update the Label which shows the displayed month (in the month line).
	 */
	private void drawLabelMoisAnnee() {
		currentMonth.setText(monthFormat.format(this.displayedMonth).toLowerCase());
		FormUtil.setElementFontSizeAndFamily(currentMonth.getElement());
	}

	/**
	 * Draw the days into the days grid. Days drawn are the days of the displayed month
	 * and few days after and before the displayed month.
	 * @param displayedMonth Date of the displayed month
	 */
	private void drawDaysGridContent(Date displayedMonth) {
		CellFormatter cfJours = daysGrid.getCellFormatter();
		Date cursor = this.getDaysGridOrigin(displayedMonth);
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 7; j++) {
				daysGrid.setText(i, j, dayNumberFormat.format(cursor));
				cfJours.removeStyleName(i, j, theme + "-" + "selected");
				cfJours.removeStyleName(i, j, theme + "-"
						+ "current-month-selected");
				cfJours.removeStyleName(i, j, theme + "-" + "other-day");
				cfJours.removeStyleName(i, j, theme + "-"
						+ "current-month-other-day");
				cfJours.removeStyleName(i, j, theme + "-" + "week-end");
				cfJours.removeStyleName(i, j, theme + "-"
						+ "current-month-week-end");
				cfJours.removeStyleName(i, j, theme + "-"
						+ "cant-be-selected");
				
				if (!datePicker.canBeSelected(cursor))
					cfJours.addStyleName(i, j, theme + "-"
									+ "cant-be-selected");
				else if (datePicker.getSelectedDate() != null
						&& DateUtil.areEquals(datePicker.getSelectedDate(), cursor))
					if (displayedMonth.getMonth() == cursor.getMonth())
						cfJours.addStyleName(i, j, theme + "-"
								+ "current-month-selected");
					else
						cfJours.addStyleName(i, j, theme + "-" + "selected");
				else if (DateUtil.isInWeekEnd(cursor))
					if (displayedMonth.getMonth() == cursor.getMonth())
						cfJours.addStyleName(i, j, theme + "-"
								+ "current-month-week-end");
					else
						cfJours.addStyleName(i, j, theme + "-" + "week-end");
				else if (displayedMonth.getMonth() == cursor.getMonth())
					cfJours.addStyleName(i, j, theme + "-"
							+ "current-month-other-day");
				else
					cfJours.addStyleName(i, j, theme + "-" + "other-day");

				cursor = DateUtil.addDays(cursor, 1);
				
				FormUtil.setElementFontSizeAndFamily(cfJours.getElement(i, j));
			}
		}
	}

	/**
	 * Change the displayed month.
	 * @param i Number of month to add to the displayed month
	 */
	protected void changeMonth(int i) {
		this.displayedMonth = DateUtil.addMonths(this.displayedMonth, i);
		this.displayMonth();
	}

	/**
	 * Return the first day to display. If the month first day is after the 5th
	 * day of the week, it return the first day of the week. Else, it returns
	 * the first day of the week before.
	 * 
	 * @param displayedMonth
	 * @return The first day to display in the grid
	 */
	private Date getDaysGridOrigin(Date displayedMonth) {
		int currentYear = displayedMonth.getYear();
		int currentMonth = displayedMonth.getMonth();
		CellFormatter cfJours = daysGrid.getCellFormatter();
		Date monthFirstDay = new Date(currentYear, currentMonth, 1);
		int indice = DateUtil.getWeekDayIndex(monthFirstDay);
		Date origineTableau;
		if (indice > 4) {
			origineTableau = DateUtil.getWeekFirstDay(monthFirstDay);
		} else {
			origineTableau = DateUtil.getWeekFirstDay(DateUtil.addDays(
					monthFirstDay, -7));
		}
		return origineTableau;
	}
}
