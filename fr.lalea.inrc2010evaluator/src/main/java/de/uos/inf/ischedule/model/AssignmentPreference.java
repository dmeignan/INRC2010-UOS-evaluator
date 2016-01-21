/**
 * Copyright 2013-2014, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

/**
 * An assignment preference is a preferred or unwanted assignment
 * on a specific day for an employee.
 * A preference is immutable.
 */ 
public class AssignmentPreference {
	
	/**
	 * Indicates if the preference is a preferred or unwanted assignment
	 */
	private boolean isPreferred;
	
	/**
	 * The shift of the preference or <code>null</code> for a preference
	 * on a day off
	 */
	private Shift shift;
	
	/**
	 * The employee on which the preference applies
	 */
	private Employee employee;
	
	/**
	 * The day-index of the preference
	 */
	private int dayIndex;

	/**
	 * Constructs a preference.
	 * 
	 * @param isPreferred the type of the preference.
	 * @param shift the shift of the preference.
	 * @param employee the employee of the preference.
	 * @param dayIndex the day-index of the preference.
	 * 
	 * @throws IllegalArgumentException if employee is <code>null</code> or
	 * day-index is negative.
	 */
	public AssignmentPreference(boolean isPreferred,
			Shift shift, Employee employee, int dayIndex) {
		if (employee == null) {
			throw new IllegalArgumentException("Employee of an assignment" +
					" preference cannot be null.");
		}
		if (dayIndex < 0) {
			throw new IllegalArgumentException("Day-index of an assignment" +
					" preference cannot be negative.");
		}
		// Copy arguments
		this.isPreferred = isPreferred;
		this.shift = shift;
		this.employee = employee;
		this.dayIndex = dayIndex;
	}
	
	/**
	 * Returns <code>true</code> if the preference is a preferred assignment or
	 * false if the preference is a unwanted assignment.
	 * 
	 * @return <code>true</code> if the preference is a preferred assignment or
	 * false if the preference is a unwanted assignment.
	 */
	public boolean isPreferred() {
		return isPreferred;
	}

	/**
	 * Returns the shift of the preference, or <code>null</code> if the preference
	 * is on a day off.
	 * 
	 * @return the shift of the preference, or <code>null</code> if the preference
	 * is on a day off.
	 */
	public Shift getShift() {
		return shift;
	}

	/**
	 * Returns the employee on which the preference applies.
	 * 
	 * @return the employee on which the preference applies.
	 */
	public Employee getEmployee() {
		return employee;
	}

	/**
	 * Returns the day-index of the preference.
	 * 
	 * @return the day-index of the preference.
	 */
	public int getDayIndex() {
		return dayIndex;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append((isPreferred)?("true"):("false"));
		builder.append("\t");
		builder.append(employee.getId());
		builder.append("\t");
		builder.append(Integer.toString(dayIndex));
		builder.append("\t");
		builder.append((shift==null)?("No assignment"):(shift.getId()));
		builder.append("\t");
		return builder.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AssignmentPreference other = (AssignmentPreference) obj;
		if (dayIndex != other.dayIndex)
			return false;
		if (employee == null) {
			if (other.employee != null)
				return false;
		} else if (!employee.equals(other.employee))
			return false;
		if (isPreferred != other.isPreferred)
			return false;
		if (shift == null) {
			if (other.shift != null)
				return false;
		} else if (!shift.equals(other.shift))
			return false;
		return true;
	}
	
}