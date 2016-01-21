/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

import java.awt.Color;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalTime;

/**
 * A shift represents a time-frame in the day for which employees with 
 * certain skills are required.
 * 
 * @author David Meignan
 */
public class Shift implements Comparable<Shift> {

	/**
	 * ID of the shift.
	 */
	protected String id;
	
	/**
	 * Label of the shift.
	 */
	protected String description;
	
	/**
	 * Start time of the shift. Only time values are relevant (i.e. hour, minute).
	 */
	protected LocalTime startTime;
	
	/**
	 * End time of the shift. Only time values are relevant (i.e. hour, minute), however
	 * the date is set to the day after starting date when the shift covers midnight.
	 */
	protected LocalTime endTime;
	
	/**
	 * Determines if the shift is a night shift (i.e. cover midnight).
	 */
	protected boolean nightShift;
	
	/**
	 * Type of the shift.
	 */
	protected ShiftType shiftType;
	
	/**
	 * Required skills.
	 */
	protected ArrayList<Skill> requiredSkills = new ArrayList<Skill>();
	
	/**
	 * Color for the representation of the shift.
	 */
	protected Color shiftColor = DEFAULT_SHIFT_COLOR;
	public static final Color DEFAULT_SHIFT_COLOR = Color.white;

	/**
	 * Constructs a shift.
	 * 
	 * @param id the ID of the shift.
	 * @param description the label of the shift.
	 * @param startHour the start hour of the shift (0 to 23).
	 * @param startMinute the start minute of the shift (0 to 59).
	 * @param endHour the end hour of the shift (0 to 23).
	 * @param endMinute the end minute of the shift (0 to 59).
	 * @param shiftType the shift type.
	 */
	public Shift(String id, String description, int startHour,
			int startMinute, int endHour, int endMinute, ShiftType shiftType) {
		this.id = id;
		this.description = description;
		startTime = new LocalTime(startHour, startMinute);
		endTime = new LocalTime(endHour, endMinute);
		if (startTime.isBefore(endTime))
			nightShift = true;
		else
			nightShift = false;
	}
	
	/**
	 * Returns a collection view of required skills.
	 * 
	 * @return a collection view of skills.
	 */
	public List<Skill> requiredSkills() {
		return new SkillCollection();
	}
	
	/**
	 * Collection view of skills. This custom implementation controls modification
	 * operations.
	 */
	private class SkillCollection extends AbstractList<Skill> {

		/* (non-Javadoc)
		 * @see java.util.AbstractList#get(int)
		 */
		@Override
		public Skill get(int idx) {
			return requiredSkills.get(idx);
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size() {
			return requiredSkills.size();
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractList#add(int, java.lang.Object)
		 */
		@Override
		public void add(int index, Skill element) {
			requiredSkills.add(index, element);
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractList#set(int, java.lang.Object)
		 */
		@Override
		public Skill set(int index, Skill element) {
			return requiredSkills.set(index, element);
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractList#remove(int)
		 */
		@Override
		public Skill remove(int index) {
			return requiredSkills.remove(index);
		}
	}

	/**
	 * Returns the ID of the shift.
	 * 
	 * @return the ID of the shift.
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Shift o) {
		return this.id.compareTo(o.id);
	}
	
	/**
	 * Returns <code>true</code> if the employee has all required skills
	 * of this shift. Returns <code>false</code> if at least one required
	 * skill is not attached to the employee.
	 * 
	 * @param employee the employee to be checked.
	 * @return <code>true</code> if the employee has all required skills
	 * of this shift. Returns <code>false</code> if at least one required
	 * skill is not attached to the employee.
	 * @throws IllegalArgumentException if the employee is <code>null</code>.
	 */
	public boolean hasRequiredSkills(Employee employee) {
		for (Skill requiredSkill: requiredSkills) {
			if (!employee.skills.contains(requiredSkill))
				return false;
		}
		return true;
	}
	
	/**
	 * Returns <code>true</code> if the shift cover midnight, <code>false</code>
	 * otherwise.
	 * 
	 * @return <code>true</code> if the shift cover midnight, <code>false</code>
	 * otherwise.
	 */
	public boolean isNightShift() {
		return nightShift;
	}
	
	/**
	 * Returns the number of missing skills of the employee according to the
	 * required skills of the shift. If the employee has all required skills or
	 * the shift has no required skill, returns <code>0</code>.
	 * 
	 * @param employee the employee for which the number of missing skills is returned.
	 * @return the number of missing skills of the employee according to the
	 * required skills of the shift.
	 */
	public int missingSkills(Employee employee) {
		int missing = 0;
		for (Skill requiredSkill: requiredSkills) {
			if (!employee.skills.contains(requiredSkill))
				missing++;
		}
		return missing;
	}

	/**
	 * Returns a short label or the name of the shift.
	 * 
	 * @return a short label or the name of the shift.
	 */
	public String getLabel() {
		return description;
	}
	
	/**
	 * Sets the color for the representation for the shift.
	 * 
	 * @param color 
	 * @throws IllegalArgumentException if the color is <code>null</code>.
	 */
	public void setColor(Color color) {
		if (color == null)
			throw new IllegalArgumentException("The color of a shift " +
					"cannot be null.");
		shiftColor = color;
	}
	
	/**
	 * Returns the color for the representation of the shift.
	 * 
	 * @return the color for the representation of the shift.
	 */
	public Color getColor() {
		return shiftColor;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
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
		Shift other = (Shift) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
