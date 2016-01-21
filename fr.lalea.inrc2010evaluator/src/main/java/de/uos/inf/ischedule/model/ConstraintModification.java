/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

/**
 * Description of a constraint modification used for <code>ConstraintsChangeListener</code>.
 * 
 * @author David Meignan
 */
public class ConstraintModification {

	/**
	 * The constraint that has been modified.
	 */
	protected Constraint constraintSource;
	
	/**
	 * Creates a constraint modification.
	 * 
	 * @param source the constraint that has been modified.
	 */
	public ConstraintModification(Constraint source) {
		this.constraintSource = source;
	}
	
	/**
	 * Returns the constraint that has been modified.
	 * 
	 * @return the constraint that has been modified.
	 */
	public Constraint getSource() {
		return constraintSource;
	}
	
}
