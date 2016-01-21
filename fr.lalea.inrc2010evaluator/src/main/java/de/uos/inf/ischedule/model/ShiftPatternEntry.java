/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

import de.uos.inf.ischedule.util.Messages;

/**
 * Entry for a shift pattern.
 * 
 * @author David Meignan
 */
public class ShiftPatternEntry {

	/**
	 * Type of assignment.
	 */
	protected PatternEntryType assignmentType;
	
	/**
	 * Definition of the shift when the type of assignment is
	 * a specific shift.
	 */
	protected Shift shift;

	/**
	 * Constructs a pattern entry.
	 * 
	 * @param assignmentType the type of assignment for this entry.
	 * @param shift the shift when the type of assignment is 
	 * <code>SPECIFIC_WORKED_SHIFT</code>.
	 * 
	 * @throws IllegalArgumentException if the type of assignment is
	 * <code>null</code>, or the shift value is inconsistent.
	 */
	public ShiftPatternEntry(PatternEntryType assignmentType, Shift shift) {
		if (assignmentType == null) {
			throw new IllegalArgumentException();
		}
		if (assignmentType == PatternEntryType.SPECIFIC_WORKED_SHIFT &&
				shift == null) {
			throw new IllegalArgumentException();
		}
		if (assignmentType != PatternEntryType.SPECIFIC_WORKED_SHIFT &&
				shift != null) {
			throw new IllegalArgumentException();
		}
		this.assignmentType = assignmentType;
		this.shift = shift;
	}
	
	/**
	 * Returns the type of the pattern entry.
	 * 
	 * @return the type of the pattern entry.
	 */
	public PatternEntryType getType() {
		return assignmentType;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (assignmentType == PatternEntryType.WORKED_SHIFT) {
			return Messages.getString("ShiftPatternEntry.workedShift"); //$NON-NLS-1$
		} else if (assignmentType == PatternEntryType.SPECIFIC_WORKED_SHIFT) {
			return shift.getLabel();
		} else if (assignmentType == PatternEntryType.NO_ASSIGNMENT) {
			return Messages.getString("ShiftPatternEntry.dayOff"); //$NON-NLS-1$
		} else if (assignmentType == PatternEntryType.UNSPECIFIED_ASSIGNMENT) {
			return Messages.getString("ShiftPatternEntry.anyAssignment"); //$NON-NLS-1$
		}
		return "?"; //$NON-NLS-1$
	}
	
	
}
