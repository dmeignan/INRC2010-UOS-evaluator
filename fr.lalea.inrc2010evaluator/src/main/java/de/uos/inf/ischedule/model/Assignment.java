/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

import org.joda.time.LocalDate;

/**
 * Assignment of an employee to a shift at a given date.
 * 
 * @author David Meignan
 */
public class Assignment {

	/**
	 * Employee assigned.
	 */
	protected Employee employee;
	
	/**
	 * Shift assigned to the employee.
	 */
	protected Shift shift;
	
	/**
	 * Date of the assignment. Time is not considered, only date (i.e. day, month,
	 * year).
	 */
	protected LocalDate date;

	/**
	 * Constructs an assignment.
	 * 
	 * @param employee the employee assigned. 
	 * @param shift the shift assigned to the employee.
	 * @param year the year of the assignment's date.
	 * @param month the month of the assignment's date (1 for January).
	 * @param dayOfMonth the day of the month of the assignment's date.
	 * 
	 * @throws IllegalArgumentException if the employee or the shift is <code>null</code>.
	 */
	public Assignment(Employee employee, Shift shift, int year, int month, int dayOfMonth) {
		if (employee == null || shift == null) {
			throw new IllegalArgumentException();
		}
		this.employee = employee;
		this.shift = shift;
		this.date = new LocalDate(year, month, dayOfMonth);
	}
	
	/**
	 * Constructs an assignment.
	 * 
	 * @param employee the employee assigned.
	 * @param shift the shift assigned to the employee.
	 * @param date the date of the assignment. Only year, month and day fields
	 * are relevant.
	 */
	public Assignment(Employee employee, Shift shift, LocalDate date) {
		if (employee == null || shift == null) {
			throw new IllegalArgumentException();
		}
		this.employee = employee;
		this.shift = shift;
		this.date = date;
	}

	/**
	 * Returns the date of the assignment.
	 * 
	 * @return the date of the assignment.
	 */
	public LocalDate getDate() {
		return date;
	}
	
	/**
	 * Returns <code>true</code> if the date of the assignment corresponds to the
	 * date passed in parameter, <code>false</code> otherwise. Only year, 
	 * month and day-of-month fields of the date are considered.
	 * 
	 * @param date the date to compare.
	 * @return <code>true</code> if the date of the assignment corresponds to the
	 * date passed in parameter, <code>false</code> otherwise.
	 */
	public boolean isOn(LocalDate date) {
		if (this.date.equals(date))
			return true;
		return false;
	}

	/**
	 * Returns the employee of the assignment.
	 * 
	 * @return the employee of the assignment.
	 */
	public Employee getEmployee() {
		return employee;
	}
	
	/**
	 * Returns the shift of the assignment.
	 * 
	 * @return the shift of the assignment.
	 */
	public Shift getShift() {
		return shift;
	}
}
