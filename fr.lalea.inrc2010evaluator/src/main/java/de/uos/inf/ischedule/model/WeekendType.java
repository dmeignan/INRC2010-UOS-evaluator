/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

import org.joda.time.DateTimeConstants;

/**
 * Type of weekends.
 * 
 * @author David Meignan
 */
public enum WeekendType {

	SATURDAY_SUNDAY(2, DateTimeConstants.SATURDAY, DateTimeConstants.SUNDAY),
	FRIDAY_SATURDAY_SUNDAY(3, DateTimeConstants.FRIDAY, DateTimeConstants.SUNDAY),
	SATURDAY_SUNDAY_MONDAY(3, DateTimeConstants.SATURDAY, DateTimeConstants.MONDAY),
	FRIDAY_SATURDAY_SUNDAY_MONDAY(4, DateTimeConstants.FRIDAY, DateTimeConstants.MONDAY);
	
	private final int duration; // in days
	private final int startDayOfWeek;
	private final int endDayOfWeek;
	
	WeekendType(int duration, int startDayOfWeek, int endDayOfWeek) {
		this.duration = duration;
		this.startDayOfWeek = startDayOfWeek;
		this.endDayOfWeek = endDayOfWeek;
	}
	
	/**
	 * Returns the duration in days of the weekend-type.
	 * 
	 * @return the duration in days of the weekend-type.
	 */
	public int getDuration() {
		return duration;
	}
	
	/**
	 * Returns the starting day of week of the weekend type.
	 * Values of ISO8601 constants for day-of-weeks are used.
	 * Values from <code>org.joda.time.DateTimeConstants</code> class can be used.
	 * 
	 * @return the starting day of week of the weekend type.
	 */
	public int getStartDayOfWeek() {
		return startDayOfWeek;
	}
	
	/**
	 * Returns the ending day of week of the weekend type.
	 * Values of ISO8601 constants for day-of-weeks are used.
	 * Values from <code>org.joda.time.DateTimeConstants</code> class can be used.
	 * 
	 * @return the ending day of week of the weekend type.
	 */
	public int getEndDayOfWeek() {
		return endDayOfWeek;
	}
	
	/**
	 * Returns <code>true</code> if the given day-of-week is on the
	 * weekend, returns <code>false</code> otherwise.
	 * 
	 * @param dayOfWeek the day-of-week.
	 * @return  <code>true</code> if the given day-of-week is on the
	 * weekend, returns <code>false</code> otherwise.
	 */
	public boolean isOnWeekend(int dayOfWeek) {
		switch (this) {
		case SATURDAY_SUNDAY:
			if (dayOfWeek == DateTimeConstants.SATURDAY ||
					dayOfWeek == DateTimeConstants.SUNDAY)
				return true;
			return false;
		case FRIDAY_SATURDAY_SUNDAY:
			if (dayOfWeek == DateTimeConstants.FRIDAY ||
					dayOfWeek == DateTimeConstants.SATURDAY ||
					dayOfWeek == DateTimeConstants.SUNDAY)
				return true;
			return false;
		case SATURDAY_SUNDAY_MONDAY:
			if (dayOfWeek == DateTimeConstants.SATURDAY ||
					dayOfWeek == DateTimeConstants.SUNDAY ||
					dayOfWeek == DateTimeConstants.MONDAY)
				return true;
			return false;
		case FRIDAY_SATURDAY_SUNDAY_MONDAY:
			if (dayOfWeek == DateTimeConstants.FRIDAY ||
					dayOfWeek == DateTimeConstants.SATURDAY ||
					dayOfWeek == DateTimeConstants.SUNDAY ||
					dayOfWeek == DateTimeConstants.MONDAY)
				return true;
			return false;
		}
		return false;
	}
}
