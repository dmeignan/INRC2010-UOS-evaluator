/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

/**
 * A skill is a competence of employee that is required for certain shifts. 
 * 
 * @author David Meignan
 */
public class Skill implements Comparable<Skill> {

	/**
	 * ID of the skill.
	 */
	protected String id;
	
	/**
	 * Label of the skill.
	 */
	protected String description;

	/**
	 * Constructs a skill.
	 * 
	 * @param id the ID of the skill.
	 * @param description the label of the skill.
	 * 
	 * @throws IllegalArgumentException if the ID or the description is <code>null</code>.
	 */
	public Skill(String id, String description) {
		if (id == null || description == null)
			throw new IllegalArgumentException();
		this.id = id;
		this.description = description;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Skill o) {
		return this.id.compareTo(o.id);
	}
	
}
