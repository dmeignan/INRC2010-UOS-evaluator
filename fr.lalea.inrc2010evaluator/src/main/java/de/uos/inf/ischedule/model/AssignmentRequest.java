/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

import org.joda.time.LocalDate;

/**
 * An <code>AssignmentRequest</code> represents a working day, day off or shift request
 * for a specific day. 
 * 
 * @author David Meignan
 */
public class AssignmentRequest {

	/**
	 * Type of the request.
	 */
	protected RequestType type;
	
	/**
	 * Date of the request. Only date fields are relevant (i.e. day, month, year).
	 */
	protected LocalDate date;
	
	/**
	 * Shift requested, if the request concerned a specific shift.
	 */
	protected Shift shift;
	
	/**
	 * Weight/priority of the request.
	 */
	protected int priority;

	/**
	 * Constructs an assignment request.
	 * 
	 * @param type the type of the request.
	 * @param year the year of the start date.
	 * @param month the month of the start date (1 for January).
	 * @param dayOfMonth the day of month of the start date.
	 * @param shift the shift requested if the request concerned a specific shift,
	 * or <code>null</code>.
	 * @param priority the weight of the request.
	 * 
	 * @throws IllegalArgumentException if the weight is negative or the shift is not
	 * consistent with the request type.
	 */
	public AssignmentRequest(RequestType type,
			int year, int month, int dayOfMonth, 
			Shift shift, int priority) {
		if (priority < 0) {
			throw new IllegalArgumentException();
		}
		if (type == RequestType.SHIFT_OFF_REQUEST) {
			if (shift == null)
				throw new IllegalArgumentException();
		} else if (type == RequestType.SHIFT_ON_REQUEST) {
			if (shift == null)
				throw new IllegalArgumentException();
		} else if (type == RequestType.DAY_ON_REQUEST) {
			if (shift != null)
				throw new IllegalArgumentException();
		} else if (type == RequestType.DAY_OFF_REQUEST) {
			if (shift != null)
				throw new IllegalArgumentException();
		}
		
		this.type = type;
		date = new LocalDate(year, month, dayOfMonth);
		this.shift = shift;
		this.priority = priority;
	}
	
	/**
	 * Returns the type of the request.
	 * 
	 * @return the type of the request.
	 */
	public RequestType getType() {
		return type;
	}
	
	/**
	 * Returns <code>true</code> if the request is set for the given date,
	 * <code>false</code> otherwise.
	 *  
	 * @param date the date.
	 * @return <code>true</code> if the request is set for the given date,
	 * <code>false</code> otherwise.
	 */
	public boolean isOn(LocalDate date) {
		if (this.date.isEqual(date))
			return true;
		return false;
	}
	
	/**
	 * Returns the priority (or weight) of the request.
	 * 
	 * @return the priority (or weight) of the request.
	 */
	public int getPriority() {
		return priority;
	}
	
}
