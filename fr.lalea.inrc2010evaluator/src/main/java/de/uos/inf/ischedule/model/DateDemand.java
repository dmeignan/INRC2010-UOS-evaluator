/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

import org.joda.time.LocalDate;

/**
 * A <code>DateDemand</code> is a demand for a shift on a specific date.
 * Only one <code>DateDemand</code> should be defined for a given pair
 * [date, shift]. A date demand overwrite the demand for the same shift
 * specified for the day of week.
 * 
 * @author David Meignan
 */
public class DateDemand {

	/**
	 * Date of the demand. Time is not considered, only date (i.e. day, month,
	 * year).
	 */
	protected LocalDate date;
	
	/**
	 * Shift of the demand.
	 */
	protected Shift shift;
	
	/**
	 * Number of employee required for the demand.
	 */
	protected int demand;
	
	/**
	 * Constructs a date demand.
	 * 
	 * @param year the year of the start date.
	 * @param month the month of the start date (1 for January).
	 * @param dayOfMonth the day of month of the start date.
	 * @param shift the shift of the demand.
	 * @param demand the required employee for the demand.
	 */
	public DateDemand(int year, int month, int dayOfMonth,
			Shift shift, int demand) {
		date = new LocalDate(year, month, dayOfMonth);
		this.shift = shift;
		this.demand = demand;
	}
	
	/**
	 * Returns <code>true</code> if the date of the demand corresponds to the
	 * date passed in parameter, <code>false</code> otherwise. Only year, 
	 * month and day-of-month fields of the date are considered.
	 * 
	 * @param date the date to compare.
	 * @return <code>true</code> if the date of the demand corresponds to the
	 * date passed in parameter, <code>false</code> otherwise.
	 */
	public boolean isOn(LocalDate date) {
		if (this.date.isEqual(date))
			return true;
		return false;
	}

}
