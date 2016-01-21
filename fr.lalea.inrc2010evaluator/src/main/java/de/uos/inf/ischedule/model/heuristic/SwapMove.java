/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.heuristic;

import java.util.Arrays;

import de.uos.inf.ischedule.model.Constraint;
import de.uos.inf.ischedule.model.Shift;
import de.uos.inf.ischedule.model.Solution;
import de.uos.inf.ischedule.model.SolutionEvaluation;

/**
 * A swap-move represents an exchange of shift assignments between
 * two employees. The set of shift assignments that are exchanged is
 * a block of consecutive assignments of a given length. Employees
 * are identified by indexes (relative to the solution representation). 
 * 
 * @author David Meignan
 */
public class SwapMove {

	/**
	 * Index of the first employee of the swap move.
	 */
	protected  int employee1Index;
	
	/**
	 * Index of the second employee of the swap move.
	 */
	protected  int employee2Index;
	
	/**
	 * Start day-index of the move.
	 */
	protected  int startDayIndex;
	
	/**
	 * Block size of the move.
	 */
	protected  int blockSize;

	/**
	 * Creates a swap-move.
	 * 
	 * @param employee1Index the index of the first employee.
	 * @param employee2Index the index of the second employee.
	 * @param startDayIndex the index of the starting day of the move.
	 * @param blockSize the block-size of the move.
	 * 
	 * @throws IllegalArgumentException if indexes of employees are equal.
	 */
	public SwapMove(int employee1Index, int employee2Index, int startDayIndex,
			int blockSize) {
		if (employee1Index == employee2Index)
			throw new IllegalArgumentException("A swap move cannot be created " +
					"on only one employee.");
		this.employee1Index = employee1Index;
		this.employee2Index = employee2Index;
		this.startDayIndex = startDayIndex;
		this.blockSize = blockSize;
	}

	/**
	 * Returns the index of the first employee of the move.
	 * 
	 * @return the index of the first employee of the move.
	 */
	public int getEmployee1Index() {
		return employee1Index;
	}

	/**
	 * Sets the index of the first employee of the move.
	 * 
	 * @param employee1Index the index of the first employee of the move.
	 * @throws IllegalArgumentException if the value is lower than <code>0</code> or index
	 * is the same that the second employee.
	 */
	public void setEmployee1Index(int employee1Index) {
		if (employee1Index < 0 || employee1Index == employee2Index)
			throw new IllegalArgumentException();
		this.employee1Index = employee1Index;
	}

	/**
	 * Returns the index of the second employee of the move.
	 * 
	 * @return the index of the second employee of the move.
	 */
	public int getEmployee2Index() {
		return employee2Index;
	}

	/**
	 * Sets the index of the second employee of the move.
	 * 
	 * @param employee2Index the index of the second employee of the move.
	 * @throws IllegalArgumentException if the value is lower than <code>0</code>
	 * or index of the employee is the same that the first employee.
	 */
	public void setEmployee2Index(int employee2Index) {
		if (employee2Index < 0 || employee1Index == employee2Index)
			throw new IllegalArgumentException();
		this.employee2Index = employee2Index;
	}

	/**
	 * Returns the day-index of the start of the move.
	 * 
	 * @return the day-index of the start of the move.
	 */
	public int getStartDayIndex() {
		return startDayIndex;
	}

	/**
	 * Sets the day-index of the start of the move.
	 * 
	 * @param startDayIndex the day-index of the start of the move.
	 * @throws IllegalArgumentException if the value is lower than <code>0</code>.
	 */
	public void setStartDayIndex(int startDayIndex) {
		if (startDayIndex < 0)
			throw new IllegalArgumentException();
		this.startDayIndex = startDayIndex;
	}

	/**
	 * Returns the day-index of the end of the move-block (included in the block).
	 * 
	 * @return the day-index of the end of the move-block.
	 */
	public int getEndDayIndex() {
		return startDayIndex+blockSize-1;
	}

	/**
	 * Returns the block size of the move.
	 * 
	 * @return the block size of the move.
	 */
	public int getBlockSize() {
		return blockSize;
	}

	/**
	 * Sets the block size of the move.
	 * 
	 * @param blockSize the block size of the move.
	 * @throws IllegalArgumentException if the value is lower than <code>1</code>.
	 */
	public void setBlockSize(int blockSize) {
		if (blockSize < 1)
			throw new IllegalArgumentException();
		this.blockSize = blockSize;
	}
	
	/**
	 * Returns the solution's assignment of an employee at the given day's index taking
	 * into account the swap-move. If the swap-move does not cover the  
	 * employee and day, the assignment defined by the solution is returned.
	 * 
	 * @param solution the solution where assignments are defined.
	 * @param dayIndex the index of the day of the assignment.
	 * @param employeeIndex the index of the employee for which the assignment
	 * has to be returned.
	 * @return the assignment of an employee at the given day's index taking
	 * into account the swap-move.
	 * 
	 * @throws NullPointerException if the solution is <code>null</code>.
	 * @throws IndexOutOfBoundsException if the move, the day's index, or the
	 * employee's index is out of range.
	 */
	public Shift getResultingAssignment(Solution solution, int dayIndex,
			int employeeIndex) {
		if ( (employeeIndex == getEmployee1Index() ||
				employeeIndex == getEmployee2Index()) &&
				dayIndex >= getStartDayIndex() &&
				dayIndex <= getEndDayIndex()) {
			// In swap-move
			if (employeeIndex == getEmployee1Index()) {
				// Is employee 1, return assignment of employee 2
				return solution.assignments.get(dayIndex).get(getEmployee2Index());
			} else {
				// Is employee 2, return assignment of employee 1
				return solution.assignments.get(dayIndex).get(getEmployee1Index());
			}
		} else {
			return solution.assignments.get(dayIndex).get(employeeIndex);
		}
	}
	
	/**
	 * Applies the swap-move to the given solution.
	 * Note that this method is not thread-safe for the solution.
	 * 
	 * @param solution the solution on which the swap-move is applied.
	 * 
	 * @throws NullPointerException if the solution is <code>null</code>.
	 * @throws IndexOutOfBoundsException if the move is out of range.
	 */
	public void applyTo(Solution solution) {
		// Modify evaluation
		if (solution.evaluated) {
			// Get evaluation difference
			SolutionEvaluation swapEvaluationDiff = getEvaluationDifference(solution);
			// Modify evaluation
			solution.evaluation = solution.evaluation.plus(swapEvaluationDiff);
			solution.constraintViolations = null;
		}
		// Apply swap on assignment
		for (int dayIndex=startDayIndex; dayIndex<=getEndDayIndex(); dayIndex++) {
			Shift initialAssignmentEmployee1 = solution.assignments.get(dayIndex)
					.get(employee1Index);
			Shift initialAssignmentEmployee2 = solution.assignments.get(dayIndex)
					.get(employee2Index);
			solution.assignments.get(dayIndex)
					.set(employee1Index, initialAssignmentEmployee2);
			solution.assignments.get(dayIndex)
					.set(employee2Index, initialAssignmentEmployee1);
			solution.constraintViolations = null;
		}
	}

	/**
	 * Returns the evaluation of the solution if the move is applied.
	 * 
	 * @param solution the evaluation of the solution if the move is applied.
	 */
	public SolutionEvaluation evaluate(Solution solution) {
		return solution.getEvaluation().plus(getEvaluationDifference(solution));
	}
	
	/**
	 * Returns the difference, in the evaluation of the specified solution, the swap
	 * move induces if it is applied.
	 * 
	 * @param solution the solution for which the swap-move have to be evaluated. 
	 * @return the difference in the evaluation of the specified solution, the swap
	 * move induces if it is applied.
	 * @throws NullPointerException if the parameter is <code>null</code>.
	 * @throws IndexOutOfBoundsException if the move is out of range.
	 */
	private SolutionEvaluation getEvaluationDifference(Solution solution) {
		int[] rValues = new int[solution.problem.getMaxConstraintsRankIndex()+1];
		Arrays.fill(rValues, 0);
		for (int rankIndex=0; rankIndex<rValues.length; rankIndex++) {
			for (Constraint constraint: solution.problem.constraints(rankIndex)) {
				rValues[rankIndex] +=
						constraint.getEvaluator(solution.problem)
						.getSwapMoveCostDifference(solution, this);
			}
		}
		return new SolutionEvaluation(rValues);
	}
	
	/**
	 * Returns the distance of the resulting solution if the move is applied to
	 * the given solution.
	 * 
	 * @param solution the solution on which the distance the move induce is
	 * computed.
	 * @return the distance of the resulting solution if the move is applied to
	 * the given solution.
	 */
	public int getResultingDistance(Solution solution) {
		int distance = 0;
		for (int dayIndex=startDayIndex; dayIndex<=getEndDayIndex(); dayIndex++) {
			Shift initialAssignmentEmployee1 = solution.assignments.get(dayIndex)
					.get(employee1Index);
			Shift initialAssignmentEmployee2 = solution.assignments.get(dayIndex)
					.get(employee2Index);
			if (initialAssignmentEmployee1 != initialAssignmentEmployee2)
				distance += 2;
		}
		return distance;
	}
	
	/**
	 * Returns <code>true</code> if applying the move modifies assignments
	 * of the given solution, <code>false</code> otherwise.
	 * 
	 * @param solution the solution on which the move is evaluated.
	 * @return <code>true</code> if applying the move modifies assignments
	 * of the given solution, <code>false</code> otherwise.
	 */
	public boolean modifyAssignment(Solution solution) {
		if (solution == null)
			return false;
		for (int dayIndex=startDayIndex; dayIndex<=getEndDayIndex(); dayIndex++) {
			if (solution.assignments.get(dayIndex).get(employee1Index) !=
					solution.assignments.get(dayIndex).get(employee2Index))
				return true;
		}
		return false;
	}
	
	/**
	 * Returns <code>true</code> if applying the move modifies the working pattern
	 * of an employee, <code>false</code> otherwise. The working pattern of an employee 
	 * is modified when a working day in the initial solution become a free day by applying
	 * the move, or conversely. This method does NOT checks changes of working shifts.
	 * 
	 * @param solution the solution on which the move is tested.
	 * @return <code>true</code> if the move modifies the working pattern
	 * of an employee, <code>false</code> otherwise.
	 */
	public boolean modifyWorkingPattern(Solution solution) {
		if (solution == null)
			return false;
		for (int dayIndex=startDayIndex; dayIndex<=getEndDayIndex(); dayIndex++) {
			Shift assignment1 = solution.assignments.get(dayIndex).get(employee1Index);
			Shift assignment2 = solution.assignments.get(dayIndex).get(employee2Index);
			if ((assignment1 != null && assignment2 == null) ||
					(assignment1 == null && assignment2 != null))
				return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Swap move between ");
		builder.append(employee1Index);
		builder.append(" and ");
		builder.append(employee2Index);
		builder.append(", from ");
		builder.append(startDayIndex);
		builder.append(" to ");
		builder.append(startDayIndex+blockSize-1);
		return builder.toString();
	}

}
