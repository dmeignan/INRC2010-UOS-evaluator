/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

import java.util.List;


/**
 * Constraint for a shift scheduling problem instance.
 * 
 * @author David Meignan
 */
public interface Constraint {

	/**
	 * Returns the local-specific name of the constraints.
	 * 
	 * @return the local-specific name of the constraints.
	 */
	public String getName();
	
	/**
	 * Returns <code>true</code> if the constraint is active, <code>false</code>
	 * otherwise.
	 * 
	 * @return <code>true</code> if the constraint is active, <code>false</code>
	 * otherwise.
	 */
	public boolean isActive();
	
	/**
	 * Returns the default weight value of the constraint that is applicable when 
	 * no specific weight value is defined within the scope of the constraint.
	 * 
	 * @return the default weight value of the constraint that is applicable when 
	 * no specific weight value is defined within the scope of the constraint.
	 */
	public int getDefaultWeightValue();
	
	/**
	 * Returns the evaluator of the constraint.
	 * 
	 * @param problem the problem on which the evaluation is based.
	 * 
	 * @return the evaluator of the constraint.
	 */
	public ConstraintEvaluator getEvaluator(ShiftSchedulingProblem problem);
	
	/**
	 * returns a label for the cost of the constraint.
	 * 
	 * @return a label for the cost of the constraint.
	 */
	public String getCostLabel();
	
	/**
	 * Returns a description of the constraint that may use HTML tag. 
	 * 
	 * @return a description of the constraint that may use HTML tag. 
	 */
	public String getHTMLDescription();
	
	/**
	 * Returns a description of the constraint for a specific employee.
	 * Returns <code>null</code> if the constraint does not apply to the
	 * employee.
	 * 
	 * @return a description of the constraint for a specific employee.
	 * Returns <code>null</code> if the constraint does not apply to the
	 * employee.
	 */
	public String getHTMLDescription(Employee employee);

	/**
	 * Returns a description of the parameters of the constraint that complete
	 * the general description of the constraint.
	 * 
	 * @param problem the problem for which the constraint is defined.
	 * 
	 * @return a description of the parameters of the constraint.
	 */
	public List<String> getHTMLParametersDescriptions(ShiftSchedulingProblem problem);

	/**
	 * Returns <code>true</code> if the constraint applies on the given employee,
	 * <code>false</code> otherwise.
	 * 
	 * @param employee the employee.
	 * @return <code>true</code> if the constraint applies on the given employee,
	 * <code>false</code> otherwise.
	 */
	public boolean cover(Employee employee);
	
}
