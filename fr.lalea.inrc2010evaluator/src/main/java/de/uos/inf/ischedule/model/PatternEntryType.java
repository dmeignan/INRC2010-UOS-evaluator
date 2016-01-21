/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

/**
 * Type of assignment to express a pattern.
 * 
 * @author David Meignan
 *
 */
public enum PatternEntryType {

	WORKED_SHIFT,			// Any shift (but not a free day)
	SPECIFIC_WORKED_SHIFT,	// Specific shift
	NO_ASSIGNMENT,			// Free day
	UNSPECIFIED_ASSIGNMENT	// Any shift or a free day
	
}
