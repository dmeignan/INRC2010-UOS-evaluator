/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

/**
 * A shift-type groups similar shifts. 
 * 
 * @author David Meignan
 */
public class ShiftType {

	/**
	 * ID of the shift-type.
	 */
	protected String id;
	
	/**
	 * Label of the shift-type.
	 */
	protected String description;
	
	/**
	 * Constructs a shift-type.
	 * 
	 * @param id the ID of the shift-type.
	 * @param description the label of the shift-type.
	 * 
	 * @throws IllegalArgumentException if the ID or the description is <code>null</code>.
	 */
	public ShiftType(String id, String description) {
		if (id == null || description == null)
			throw new IllegalArgumentException();
		this.id = id;
		this.description = description;
	}
}
