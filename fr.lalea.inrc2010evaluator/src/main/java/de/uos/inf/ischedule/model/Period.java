/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

import java.util.ArrayList;

import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 * The class <code>Period</code> defines a period between two dates. Time is not considered,
 * only date (i.e. day, month, year). The start-date and end-date are included in the period.
 *  
 * @author David Meignan
 */
public class Period {

	/**
	 * The list of days contained in the period.
	 */
	protected ArrayList<LocalDate> days = new ArrayList<LocalDate>();
	
	/**
	 * Constructs a period between two dates.
	 * 
	 * @param startYear the year of the start date.
	 * @param startMonth the month of the start date (1 for January).
	 * @param startDayOfMonth the day of month of the start date.
	 * @param endYear the year of the end date.
	 * @param endMonth the month of the end date (1 for January).
	 * @param endDayOfMonth the day of month of the end date.
	 * 
	 * @throws IllegalArgumentExeption if the end date is anterior to the start date.
	 */
	public Period(int startYear, int startMonth, int startDayOfMonth,
			int endYear, int endMonth, int endDayOfMonth) {
		LocalDate startDate = new LocalDate(startYear, startMonth, startDayOfMonth);
		LocalDate endDate = new LocalDate(endYear, endMonth, endDayOfMonth);
		if (startDate.isAfter(endDate))
			throw new IllegalArgumentException();
		LocalDate date = startDate;
		while (date.isBefore(endDate) || date.isEqual(endDate)) {
			days.add(date);
			date = date.plusDays(1);
		}
	}
	
	/**
	 * Returns the number of days contained in this period.
	 * Start date and end date are included.
	 * 
	 * @return the number of days contained in this period.
	 */
	public int size() {
		return days.size();
	}
	
	/**
	 * Returns the date from a day index in the period.
	 * 
	 * @param dayIndex the day index.
	 * @return the date from the day index of the period.
	 * @throws IllegalArgumentException if the day index is out-of-bounds.
	 */
	public LocalDate getDate(int dayIndex) {
		if (dayIndex < 0 || dayIndex >= days.size())
			throw new IllegalArgumentException();
		return days.get(dayIndex);
	}
	
	/**
	 * returns the day-index in the period from a date.
	 * 
	 * @param date the date.
	 * @return the day-index in the period that corresponds to the date.
	 * @throws IllegalArgumentException if the day index is out-of-range.
	 */
	public int getDayIndex(LocalDate date) {
		if (date.isAfter(days.get(days.size()-1)) ||
				date.isBefore(days.get(0)))
			throw new IllegalArgumentException();
		return Days.daysBetween(days.get(0), date).getDays();
	}
	
	/**
	 * Returns <code>true</code> if the period contains the date, 
	 * <code>false</code> otherwise.
	 * 
	 * @param date the date to check.
	 * @return <code>true</code> if the period contains the date, 
	 * <code>false</code> otherwise.
	 */
	public boolean contains(LocalDate date) {
		return !(date.isAfter(days.get(days.size()-1)) ||
				date.isBefore(days.get(0)));
	}
	
	/**
	 * Returns the day of week for the date at the specified index of the period.
	 * Values of ISO8601 constants for day-of-weeks are used.
	 * Values from <code>org.joda.time.DateTimeConstants</code> class can be used.
	 * 
	 * @param dayIndex the day index of the period.
	 * @return the  for the date at the specified index of the period.
	 * @throws IllegalArgumentException  if the day index is greater than
	 * the number of days of the period.
	 */
	public int getDayOfWeek(int dayIndex) {
		if (dayIndex >= days.size())
			throw new IllegalArgumentException();
		return days.get(dayIndex).getDayOfWeek();
	}
	
}
