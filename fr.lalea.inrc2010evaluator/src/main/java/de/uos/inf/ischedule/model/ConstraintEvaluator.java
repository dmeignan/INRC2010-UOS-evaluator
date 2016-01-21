/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

import java.util.ArrayList;

import de.uos.inf.ischedule.model.heuristic.SwapMove;

/**
 * A <code>ConstraintEvaluator</code> evaluates solutions
 * for a specific constraint. Note that an evaluator is not updated when
 * the problem instance changes. If the problem changes after instantiating 
 * an evaluator, it may result in inconsistencies in the evaluation.
 * 
 * @author David Meignan
 */
public abstract class ConstraintEvaluator {

	/**
	 * Returns the constraint evaluated.
	 * 
	 * @return the constraint evaluated.
	 */
	public abstract Constraint getConstraint();
	
	/**
	 * Computes and returns the total cost of the constraint for a given solution.
	 * 
	 * @param solution the solution to evaluate.
	 * @return the total cost of the constraint for the solution.
	 * @throws NullPointerException if the solution is <code>null</code>.
	 */
	public abstract int getCost(Solution solution);
	
	/**
	 * Estimates the cost of adding the specified assignment to the given 
	 * partial solution. The returned cost can be negative.
	 * 
	 * @param solution a partial solution.
	 * @param employeeIndex the index of the employee of the assignment.
	 * @param shift shift of the assignment.
	 * @param assignmentDayIndex day index of the assignment.
	 * @return an estimation of the cost of adding the specified assignment
	 * to the given partial solution.
	 * @throws NullPointerException if the solution or the shift is 
	 * <code>null</code>.
	 * @throws IndexOutOfBoundsException if the employee-index or the 
	 * day-index is out-of-range.
	 */
	public abstract int getEstimatedAssignmentCost(Solution solution,
			int employeeIndex, Shift shift, int assignmentDayIndex);
	
	/**
	 * Returns the difference of cost the move induces.
	 * 
	 * @param solution the solution on which the move is evaluated.
	 * @param swapMove the swap-move.
	 * @return the difference of cost the move induces.
	 * @throws NullPointerException if the solution or swap-move
	 * is <code>null</code>.
	 * @throws IndexOutOfBoundsException if one of the swap-move parameters 
	 * is out-of-range.
	 */
	public abstract int getSwapMoveCostDifference(Solution solution,
			SwapMove swapMove);
	
	/**
	 * Returns the difference in terms of constraint satisfaction the move
	 * induces. In the returned array, the first value is the number of
	 * constraints solved by the move, and the second value is the number
	 * of new constraints unsatisfied.
	 * 
	 * @param solution the solution on which the move is evaluated.
	 * @param swapMove the swap move to evaluate.
	 * @return the difference in terms of constraint satisfaction the move
	 * induces.
	 */
	public abstract int[] getConstraintSatisfactionDifference(
			Solution solution, SwapMove swapMove);
	
	/**
	 * Returns the set of constraint violations for the given solution.
	 * Returns an empty list if there is no constraint violations related
	 * to this evaluator.
	 * 
	 * @param solution the solution for which constraint violations are computed.
	 * @return the set of constraint violations for the given solution.
	 */
	public abstract ArrayList<ConstraintViolation> getConstraintViolations(Solution solution);
	
}
