/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

import java.util.ArrayList;
import java.util.List;

import de.uos.inf.ischedule.model.heuristic.SwapMove;
import de.uos.inf.ischedule.util.IconUtils;
import de.uos.inf.ischedule.util.Messages;


/**
 * This constraint defines a maximum number of consecutive days off for an employee.
 * 
 * @author David Meignan
 */
public class MaxConsecutiveDaysOffConstraint implements Constraint {

	/**
	 * Maximum number of consecutive days off for an employee.
	 */
	protected int maxConsecutiveDaysOff;

	/**
	 * Contract for which the constraint applies.
	 */
	protected Contract scope;
	
	/**
	 * Activation of the constraint.
	 */
	protected boolean active;

	/**
	 * The default weight value of the constraint.
	 */
	protected int weightValue;
	
	/**
	 * Evaluator of the constraint.
	 */
	private MaxConsecutiveDaysOffConstraintEvaluator evaluator = null;
	
	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.ConstraintInstance#isActive()
	 */
	@Override
	public boolean isActive() {
		return active;
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.ConstraintInstance#getDefaultWeightValue()
	 */
	@Override
	public int getDefaultWeightValue() {
		return weightValue;
	}

	/**
	 * Constructs a maximum-number-of-consecutive-free-days constraint.
	 * 
	 * @param maxConsecutiveFreeDays the maximum number of consecutive free days.
	 * @param scope the contract on which the constraint applies.
	 * @param active the active property of the constraint.
	 * @param defaultWeightValue the default weight value of the constraint.
	 * 
	 * @throws IllegalArgumentException if the maximum number of days
	 * is negative or null, or the rank-index or the default weight value is negative,
	 * or the scope is <code>null</code>.
	 */
	public MaxConsecutiveDaysOffConstraint(int maxConsecutiveFreeDays, Contract scope,
			boolean active, int defaultWeightValue) {
		if (maxConsecutiveFreeDays < 1 || defaultWeightValue < 0)
			throw new IllegalArgumentException();
		if (scope == null)
			throw new IllegalArgumentException();
		
		this.maxConsecutiveDaysOff = maxConsecutiveFreeDays;
		this.scope = scope;
		this.active = active;
		this.weightValue = defaultWeightValue;
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getEvaluator(de.uos.inf.ischedule.model.ShiftSchedulingProblem)
	 */
	@Override
	public ConstraintEvaluator getEvaluator(ShiftSchedulingProblem problem) {
		if (evaluator == null)
			evaluator = new MaxConsecutiveDaysOffConstraintEvaluator(problem);
		return evaluator;
	}

	/**
	 * Evaluator of the constraint.
	 */
	class MaxConsecutiveDaysOffConstraintEvaluator extends
			ConstraintEvaluator {
		
		/**
		 * List of employee indexes on which the constraint applies.
		 */
		ArrayList<Integer> constrainedEmployeeIndexes;

		/**
		 * Creates an evaluator of the constraint.
		 * 
		 * @param problem the shift scheduling problem.
		 */
		public MaxConsecutiveDaysOffConstraintEvaluator(ShiftSchedulingProblem problem) {
			// Constrained employees
			constrainedEmployeeIndexes = new ArrayList<Integer>();
			for (int employeeIndex=0; employeeIndex<problem.employees.size(); 
					employeeIndex++) {
				if (problem.employees.get(employeeIndex).contract == scope) {
					constrainedEmployeeIndexes.add(employeeIndex);
				}
			}
		}
		
		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getCost(de.uos.inf.ischedule.model.Solution)
		 */
		@Override
		public int getCost(Solution solution) {
			// Check active and weight value
			if (!active || weightValue <= 0)
				return 0;
			
			int excess = 0;
			// Iterates on employees
			for (int employeeIndex: constrainedEmployeeIndexes) {
				int consecutiveFreeDays = 0;
				for (int dayIndex=0; dayIndex<solution.assignments.size(); dayIndex++) {
					if (solution.assignments.get(dayIndex).get(employeeIndex) == null) {
						consecutiveFreeDays++;
						if (consecutiveFreeDays > maxConsecutiveDaysOff)
							excess++;
					} else {
						consecutiveFreeDays = 0;
					}
				}
			}
			// Return cost by weight
			return excess*weightValue;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getEstimatedAssignmentCost(de.uos.inf.ischedule.model.Solution, int, de.uos.inf.ischedule.model.Shift, int)
		 */
		@Override
		public int getEstimatedAssignmentCost(Solution solution,
				int employeeIndex, Shift shift, int assignmentDayIndex) {
			return 0;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getSwapMoveCostDifference(de.uos.inf.ischedule.model.Solution, de.uos.inf.ischedule.model.heuristic.SwapMove)
		 */
		@Override
		public int getSwapMoveCostDifference(Solution solution,
				SwapMove swapMove) {
			// Check active state and weight value of the constraint
			if (!active || weightValue <= 0)
				return 0;
			
			// Check constrained employees
			boolean employee1Constrained =
					constrainedEmployeeIndexes.contains(swapMove.getEmployee1Index());
			boolean employee2Constrained =
					constrainedEmployeeIndexes.contains(swapMove.getEmployee2Index());
			if (!employee1Constrained && !employee2Constrained)
				return 0;

			// Check if move modifies work patterns
			if (!swapMove.modifyWorkingPattern(solution))
				return 0;
			
			// Compute initial and swap excess
			int initialExcess = 0;
			int swapExcess = 0;
			
			// Employee 1
			if (employee1Constrained) {
				int startDayIndex;
				int endDayIndex;
				// Partial evaluation interval
				startDayIndex = swapMove.getStartDayIndex();
				while(startDayIndex > 0 && solution.assignments
						.get(startDayIndex-1).get(swapMove.getEmployee1Index()) == null)
					startDayIndex--;
				endDayIndex = swapMove.getEndDayIndex();
				while(endDayIndex < solution.assignments.size()-1 && solution.assignments
						.get(endDayIndex+1).get(swapMove.getEmployee1Index()) == null)
					endDayIndex++;
				// Count initial excess
				int consecutiveFreeDays = 0;
				for (int dayIndex=startDayIndex; dayIndex<=endDayIndex; dayIndex++) {
					if (solution.assignments.get(dayIndex)
							.get(swapMove.getEmployee1Index()) == null) {
						consecutiveFreeDays++;
						if (consecutiveFreeDays > maxConsecutiveDaysOff)
							initialExcess++;
					} else {
						consecutiveFreeDays = 0;
					}
				}
				// Count swap excess
				consecutiveFreeDays = 0;
				for (int dayIndex=startDayIndex; dayIndex<=endDayIndex; dayIndex++) {
					if (swapMove.getResultingAssignment(
							solution, dayIndex, swapMove.getEmployee1Index()) == null) {
						consecutiveFreeDays++;
						if (consecutiveFreeDays > maxConsecutiveDaysOff)
							swapExcess++;
					} else {
						consecutiveFreeDays = 0;
					}
				}
			}
			
			// Employee 2
			if (employee2Constrained) {
				int startDayIndex;
				int endDayIndex;
				// Partial evaluation interval
				startDayIndex = swapMove.getStartDayIndex();
				while(startDayIndex > 0 && solution.assignments
						.get(startDayIndex-1).get(swapMove.getEmployee2Index()) == null)
					startDayIndex--;
				endDayIndex = swapMove.getEndDayIndex();
				while(endDayIndex < solution.assignments.size()-1 && solution.assignments
						.get(endDayIndex+1).get(swapMove.getEmployee2Index()) == null)
					endDayIndex++;
				// Count initial excess
				int consecutiveFreeDays = 0;
				for (int dayIndex=startDayIndex; dayIndex<=endDayIndex; dayIndex++) {
					if (solution.assignments.get(dayIndex)
							.get(swapMove.getEmployee2Index()) == null) {
						consecutiveFreeDays++;
						if (consecutiveFreeDays > maxConsecutiveDaysOff)
							initialExcess++;
					} else {
						consecutiveFreeDays = 0;
					}
				}
				// Count swap excess
				consecutiveFreeDays = 0;
				for (int dayIndex=startDayIndex; dayIndex<=endDayIndex; dayIndex++) {
					if (swapMove.getResultingAssignment(
							solution, dayIndex, swapMove.getEmployee2Index()) == null) {
						consecutiveFreeDays++;
						if (consecutiveFreeDays > maxConsecutiveDaysOff)
							swapExcess++;
					} else {
						consecutiveFreeDays = 0;
					}
				}
			}
			
			return (swapExcess-initialExcess)*weightValue;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getConstraint()
		 */
		@Override
		public Constraint getConstraint() {
			return MaxConsecutiveDaysOffConstraint.this;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getConstraintViolations(de.uos.inf.ischedule.model.Solution)
		 */
		@Override
		public ArrayList<ConstraintViolation> getConstraintViolations(
				Solution solution) {
			ArrayList<ConstraintViolation> violations = new ArrayList<ConstraintViolation>();
			if (!active || weightValue <= 0)
				return violations;
			
			for (int employeeIndex: constrainedEmployeeIndexes) {
				int consecutiveFreeDays = 0;
				int startFreeDayIndex = 0;
				for (int dayIndex=0; dayIndex<solution.assignments.size(); dayIndex++) {
					if (solution.assignments.get(dayIndex).get(employeeIndex) == null) {
						if (consecutiveFreeDays == 0)
							startFreeDayIndex = dayIndex;
						consecutiveFreeDays++;
					} else {
						if (consecutiveFreeDays > maxConsecutiveDaysOff) {
							ConstraintViolation violation = new ConstraintViolation(
									MaxConsecutiveDaysOffConstraint.this);
							violation.setCost(weightValue*(consecutiveFreeDays-maxConsecutiveDaysOff));
							violation.setMessage(Messages.getString("MaxConsecutiveDaysOffConstraint.maxConsecutiveDaysOffExceeded")); //$NON-NLS-1$
							violation.addAssignmentRangeInScope(
									solution.employees.get(employeeIndex),
									solution.problem.getSchedulingPeriod().getDate(startFreeDayIndex),
									solution.problem.getSchedulingPeriod().getDate(dayIndex-1));
							violations.add(violation);
						}
						consecutiveFreeDays = 0;
					}
				}
				if (consecutiveFreeDays > maxConsecutiveDaysOff) {
					ConstraintViolation violation = new ConstraintViolation(
							MaxConsecutiveDaysOffConstraint.this);
					violation.setCost(weightValue*(consecutiveFreeDays-maxConsecutiveDaysOff));
					violation.setMessage(Messages.getString("MaxConsecutiveDaysOffConstraint.maxConsecutiveDaysOffExceeded")); //$NON-NLS-1$
					violation.addAssignmentRangeInScope(
							solution.employees.get(employeeIndex), 
							solution.problem.getSchedulingPeriod().getDate(startFreeDayIndex),
							solution.problem.getSchedulingPeriod().getDate(solution.assignments.size()-1));
					violations.add(violation);
				}
			}
			
			return violations;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getConstraintSatisfactionDifference(de.uos.inf.ischedule.model.Solution, de.uos.inf.ischedule.model.heuristic.SwapMove)
		 */
		@Override
		public int[] getConstraintSatisfactionDifference(Solution solution,
				SwapMove swapMove) {
			int[] diff = new int[] {0, 0};
			
			// Check active state and weight value of the constraint
			if (!active || weightValue <= 0)
				return diff;
			
			// Check constrained employees
			boolean employee1Constrained =
					constrainedEmployeeIndexes.contains(swapMove.getEmployee1Index());
			boolean employee2Constrained =
					constrainedEmployeeIndexes.contains(swapMove.getEmployee2Index());
			if (!employee1Constrained && !employee2Constrained)
				return diff;

			// Check if move modifies work patterns
			if (!swapMove.modifyWorkingPattern(solution))
				return diff;
			
			// Employee 1
			if (employee1Constrained) {
				int startDayIndex;
				int endDayIndex;
				// Partial evaluation interval
				startDayIndex = swapMove.getStartDayIndex();
				while(startDayIndex > 0 && solution.assignments
						.get(startDayIndex-1).get(swapMove.getEmployee1Index()) == null)
					startDayIndex--;
				endDayIndex = swapMove.getEndDayIndex();
				while(endDayIndex < solution.assignments.size()-1 && solution.assignments
						.get(endDayIndex+1).get(swapMove.getEmployee1Index()) == null)
					endDayIndex++;
				// Count initial excess
				int initialExcess = 0;
				int consecutiveFreeDays = 0;
				for (int dayIndex=startDayIndex; dayIndex<=endDayIndex; dayIndex++) {
					if (solution.assignments.get(dayIndex)
							.get(swapMove.getEmployee1Index()) == null) {
						consecutiveFreeDays++;
						if (consecutiveFreeDays > maxConsecutiveDaysOff)
							initialExcess++;
					} else {
						consecutiveFreeDays = 0;
					}
				}
				// Count swap excess
				int swapExcess = 0;
				consecutiveFreeDays = 0;
				for (int dayIndex=startDayIndex; dayIndex<=endDayIndex; dayIndex++) {
					if (swapMove.getResultingAssignment(
							solution, dayIndex, swapMove.getEmployee1Index()) == null) {
						consecutiveFreeDays++;
						if (consecutiveFreeDays > maxConsecutiveDaysOff)
							swapExcess++;
					} else {
						consecutiveFreeDays = 0;
					}
				}
				if (initialExcess > swapExcess) {
					diff[0] += initialExcess-swapExcess;
				} else if (swapExcess > initialExcess) {
					diff[1] += swapExcess-initialExcess;
				}
			}
			
			// Employee 2
			if (employee2Constrained) {
				int startDayIndex;
				int endDayIndex;
				// Partial evaluation interval
				startDayIndex = swapMove.getStartDayIndex();
				while(startDayIndex > 0 && solution.assignments
						.get(startDayIndex-1).get(swapMove.getEmployee2Index()) == null)
					startDayIndex--;
				endDayIndex = swapMove.getEndDayIndex();
				while(endDayIndex < solution.assignments.size()-1 && solution.assignments
						.get(endDayIndex+1).get(swapMove.getEmployee2Index()) == null)
					endDayIndex++;
				// Count initial excess
				int initialExcess = 0;
				int consecutiveFreeDays = 0;
				for (int dayIndex=startDayIndex; dayIndex<=endDayIndex; dayIndex++) {
					if (solution.assignments.get(dayIndex)
							.get(swapMove.getEmployee2Index()) == null) {
						consecutiveFreeDays++;
						if (consecutiveFreeDays > maxConsecutiveDaysOff)
							initialExcess++;
					} else {
						consecutiveFreeDays = 0;
					}
				}
				// Count swap excess
				int swapExcess = 0;
				consecutiveFreeDays = 0;
				for (int dayIndex=startDayIndex; dayIndex<=endDayIndex; dayIndex++) {
					if (swapMove.getResultingAssignment(
							solution, dayIndex, swapMove.getEmployee2Index()) == null) {
						consecutiveFreeDays++;
						if (consecutiveFreeDays > maxConsecutiveDaysOff)
							swapExcess++;
					} else {
						consecutiveFreeDays = 0;
					}
				}
				if (initialExcess > swapExcess) {
					diff[0] += initialExcess-swapExcess;
				} else if (swapExcess > initialExcess) {
					diff[1] += swapExcess-initialExcess;
				}
			}
			
			return diff;
		}
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getName()
	 */
	@Override
	public String getName() {
		return Messages.getString("MaxConsecutiveDaysOffConstraint.name"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getConstraintCostLabel()
	 */
	@Override
	public String getCostLabel() {
		return Messages.getString("MaxConsecutiveDaysOffConstraint.costLabel"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription()
	 */
	@Override
	public String getHTMLDescription() {
		return Messages.getString("MaxConsecutiveDaysOffConstraint.description"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription(de.uos.inf.ischedule.model.Employee)
	 */
	@Override
	public String getHTMLDescription(Employee employee) {
		if (!cover(employee))
			return null;
		String desc = Messages.getString(
				"MaxConsecutiveDaysOffConstraint.descriptionEmployee"); //$NON-NLS-1$
		desc = desc.replaceAll("\\$1", Integer.toString(maxConsecutiveDaysOff)); //$NON-NLS-1$
		return desc;
	}
	
	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLParametersDescriptions(de.uos.inf.ischedule.model.ShiftSchedulingProblem)
	 */
	@Override
	public List<String> getHTMLParametersDescriptions(
			ShiftSchedulingProblem problem) {
		String paramDesc = Messages.getString(
				"MaxConsecutiveDaysOffConstraint.parametersDescription"); //$NON-NLS-1$
		paramDesc = paramDesc.replaceAll("\\$1", scope.getName()); //$NON-NLS-1$
		String imgURL = IconUtils.getImageURL(scope.getIconPath());
		paramDesc = paramDesc.replaceAll("\\$2", imgURL); //$NON-NLS-1$
		paramDesc = paramDesc.replaceAll("\\$3", Integer.toString(maxConsecutiveDaysOff)); //$NON-NLS-1$
		ArrayList<String> descList = new  ArrayList<String>();
		descList.add(paramDesc);
		return descList;
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#cover(de.uos.inf.ischedule.model.Employee)
	 */
	@Override
	public boolean cover(Employee employee) {
		if (employee == null)
			return false;
		return (employee.getContract() == scope);
	}


}
