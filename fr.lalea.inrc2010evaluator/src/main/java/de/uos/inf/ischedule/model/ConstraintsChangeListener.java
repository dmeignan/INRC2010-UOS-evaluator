/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

/**
 * Defines an object which listens for modification of the set of constraints.
 * 
 * @author David Meignan
 */
public interface ConstraintsChangeListener {

	/**
	 * Indicates that a constraint has been changed.
	 * 
	 * @param m the description of the modification.
	 */
	public void constraintsChanged(ConstraintModification m);
	
}
