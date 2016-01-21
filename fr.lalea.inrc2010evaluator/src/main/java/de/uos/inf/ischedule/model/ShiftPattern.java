/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTimeConstants;

import de.uos.inf.ischedule.util.Messages;

/**
 * A <code>ShiftPattern</code> is a sequence of shift, and allows to express
 * constraints on shift sequences.
 * 
 * @author David Meignan
 */
public class ShiftPattern {

	/**
	 * ID of the shift pattern.
	 */
	protected String id;
	
	/**
	 * Determines if the pattern is specific for a day of week or if
	 * it applies on all day of week.
	 */
	protected boolean dayOfWeekSpecific = false;
	
	/**
	 * Start day of the pattern if the pattern is specific for a day of week.
	 * Values of ISO8601 constants for day-of-weeks are used.
	 * Values from <code>org.joda.time.DateTimeConstants</code> class can be used.
	 */
	protected int startDay = -1;
	
	/**
	 * Sequence of shift or shift-type of the pattern.
	 */
	protected ArrayList<ShiftPatternEntry> entries = new ArrayList<ShiftPatternEntry>();

	/**
	 * Construct a shift-pattern. By default the pattern has no start-day.
	 * 
	 * @param id the ID of the pattern.
	 * @throws IllegalArgumentException if the ID is <code>null</code>.
	 */
	public ShiftPattern(String id) {
		if (id == null)
			throw new IllegalArgumentException();
		this.id = id;
	}
	
	/**
	 * Returns a collection view of pattern-entries.
	 * 
	 * @return a collection view of pattern-entries.
	 */
	public List<ShiftPatternEntry> entries() {
		return new EntryCollection();
	}
	
	/**
	 * List view of entries of the pattern. This custom implementation 
	 * controls modification operations.
	 */
	private class EntryCollection extends AbstractList<ShiftPatternEntry> {

		/* (non-Javadoc)
		 * @see java.util.AbstractList#get(int)
		 */
		@Override
		public ShiftPatternEntry get(int index) {
			return entries.get(index);
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size() {
			return entries.size();
		}
		
		/* (non-Javadoc)
		 * @see java.util.AbstractList#add(int, java.lang.Object)
		 */
		@Override
		public void add(int index, ShiftPatternEntry element) {
			entries.add(index, element);
		}
	}
	
	/**
	 * Returns the ID of the pattern.
	 * 
	 * @return the ID of the pattern.
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Returns the day-of-week on which the pattern start, or 
	 * <code>-1</code> if the pattern applies to all day of week.
	 * Values of ISO8601 constants for day-of-weeks are used.
	 * Values from <code>org.joda.time.DateTimeConstants</code> class can be used.
	 * 
	 * @return the day-of-week on which the pattern start, or 
	 * <code>-1</code> if the pattern applies to all day of week.
	 */
	public int getStartDay() {
		return startDay;
	}

	/**
	 * Sets the start day-of-week on which the pattern start.
	 * Values of ISO8601 constants for day-of-weeks are used.
	 * Values from <code>org.joda.time.DateTimeConstants</code> class can be used.
	 * 
	 * @param startDay the start day-of-week on which the pattern start.
	 * @throws IllegalArgumentException if the day-of-week value does not
	 * correspond to one of the constant value for day-of-week.
	 */
	public void setStartDay(int startDay) {
		if (startDay != DateTimeConstants.MONDAY &&
				startDay != DateTimeConstants.TUESDAY &&
				startDay != DateTimeConstants.WEDNESDAY &&
				startDay != DateTimeConstants.THURSDAY &&
				startDay != DateTimeConstants.FRIDAY &&
				startDay != DateTimeConstants.SATURDAY &&
				startDay != DateTimeConstants.SUNDAY)
			throw new IllegalArgumentException();
		this.startDay = startDay;
		dayOfWeekSpecific = true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(entries.toString());
		if (dayOfWeekSpecific) {
			switch (startDay) {
			case 1:
				builder.append(Messages.getString("ShiftPattern.startingMonday")); //$NON-NLS-1$
				break;
			case 2:
				builder.append(Messages.getString("ShiftPattern.startingTuesday")); //$NON-NLS-1$
				break;
			case 3:
				builder.append(Messages.getString("ShiftPattern.startingWednesday")); //$NON-NLS-1$
				break;
			case 4:
				builder.append(Messages.getString("ShiftPattern.startingThursday")); //$NON-NLS-1$
				break;
			case 5:
				builder.append(Messages.getString("ShiftPattern.startingFriday")); //$NON-NLS-1$
				break;
			case 6:
				builder.append(Messages.getString("ShiftPattern.startingSaturday")); //$NON-NLS-1$
				break;
			case 7:
				builder.append(Messages.getString("ShiftPattern.startingSunday")); //$NON-NLS-1$
				break;
			}
		}
		return builder.toString();
	}
	
}
