/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

import org.joda.time.DateTimeConstants;

/**
 * A <code>DayOfWeekDemand</code> is a demand for a shift on a day of week.
 * Only one <code>DayOfWeekDemand</code> should be defined for a given pair
 * [day of week, shift].
 * 
 * @author David Meignan
 */
public class DayOfWeekDemand {

	/**
	 * Day of the demand. Values of ISO8601 constants for day-of-weeks are used.
	 * Values from <code>org.joda.time.DateTimeConstants</code> class can be used.
	 */
	protected int dayOfWeek;
	
	/**
	 * Shift of the demand.
	 */
	protected Shift shift;
	
	/**
	 * Number of employee required for the demand.
	 */
	protected int demand;

	/**
	 * Constructs a day-of-week demand.
	 * 
	 * @param dayOfWeek the day of week of the demand. Values of ISO8601 
	 * constants for day-of-weeks are used. They are stored in 
	 * <code>org.joda.time.DateTimeConstants</code> class.
	 * @param shift the shift of the demand.
	 * @param demand the required employee for the demand.
	 * @throws IllegalArgumentException if the day-of-week value does not
	 * correspond to one of the constant value for day-of-week.
	 */
	public DayOfWeekDemand(int dayOfWeek, Shift shift, int demand) {
		if (dayOfWeek != DateTimeConstants.MONDAY &&
				dayOfWeek != DateTimeConstants.TUESDAY &&
				dayOfWeek != DateTimeConstants.WEDNESDAY &&
				dayOfWeek != DateTimeConstants.THURSDAY &&
				dayOfWeek != DateTimeConstants.FRIDAY &&
				dayOfWeek != DateTimeConstants.SATURDAY &&
				dayOfWeek != DateTimeConstants.SUNDAY)
			throw new IllegalArgumentException();
		this.dayOfWeek = dayOfWeek;
		this.shift = shift;
		this.demand = demand;
	}

}
