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
 * This constraint defines a maximum number of consecutive working days for an employee.
 * 
 * @author David Meignan
 */
public class MaxConsecutiveWorkingDaysConstraint implements Constraint {

	/**
	 * Maximum number of consecutive working days for an employee.
	 */
	protected int maxConsecutiveWorkingDays;

	/**
	 * Contract for which the constraint applies.
	 */
	protected Contract scope;
	
	/**
	 * Activation of the constraint.
	 */
	protected boolean active;

	/**
	 * The weight value of the constraint.
	 */
	protected int weightValue;
	
	/**
	 * Evaluator of the constraint.
	 */
	private MaxConsecutiveWorkingDaysConstraintEvaluator evaluator = null;
	
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
	 * Constructs a maximum-number-of-consecutive-working-days constraint.
	 * 
	 * @param maxConsecutiveWorkingDays the maximum number of consecutive working days.
	 * @param scope the contract on which the constraint applies.
	 * @param active the active property of the constraint.
	 * @param rankIndex the rank-index of the constraints.
	 * @param defaultWeightValue the default weight value of the constraint.
	 * 
	 * @throws IllegalArgumentException if the maximum number of days
	 * is negative or null, or the rank-index or the default weight value is negative,
	 * or the scope is <code>null</code>.
	 */
	public MaxConsecutiveWorkingDaysConstraint(int maxConsecutiveWorkingDays, Contract scope,
			boolean active, int defaultWeightValue) {
		if (maxConsecutiveWorkingDays < 1 || defaultWeightValue < 0)
			throw new IllegalArgumentException();
		if (scope == null)
			throw new IllegalArgumentException();
		
		this.maxConsecutiveWorkingDays = maxConsecutiveWorkingDays;
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
			evaluator = new MaxConsecutiveWorkingDaysConstraintEvaluator(problem);
		return evaluator;
	}

	/**
	 * Evaluator of the constraint.
	 */
	class MaxConsecutiveWorkingDaysConstraintEvaluator extends
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
		public MaxConsecutiveWorkingDaysConstraintEvaluator(ShiftSchedulingProblem problem) {
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
				int consecutiveWorkingDays = 0;
				for (int dayIndex=0; dayIndex<solution.assignments.size(); dayIndex++) {
					if (solution.assignments.get(dayIndex).get(employeeIndex) != null) {
						consecutiveWorkingDays++;
						if (consecutiveWorkingDays > maxConsecutiveWorkingDays)
							excess++;
					} else {
						consecutiveWorkingDays = 0;
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
			// Check active and weight value
			if (!active || weightValue <= 0)
				return 0;
			
			// Check scope of constraint
			if (!constrainedEmployeeIndexes.contains(employeeIndex))
				return 0;
			
			int consecutiveWorkingDays = 1;
			// Count up
			for (int dayIndex=assignmentDayIndex+1; dayIndex<solution.assignments.size();
					dayIndex++) {
				if (solution.assignments.get(dayIndex).get(employeeIndex) != null)
					consecutiveWorkingDays++;
				else
					break;
			}
			// Count down
			for (int dayIndex=assignmentDayIndex-1; dayIndex>=0;
					dayIndex--) {
				if (solution.assignments.get(dayIndex).get(employeeIndex) != null)
					consecutiveWorkingDays++;
				else
					break;
			}
			// Note that the cost may be underestimated
			if (consecutiveWorkingDays > maxConsecutiveWorkingDays)
				return weightValue;
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
						.get(startDayIndex-1).get(swapMove.getEmployee1Index()) != null)
					startDayIndex--;
				endDayIndex = swapMove.getEndDayIndex();
				while(endDayIndex < solution.assignments.size()-1 && solution.assignments
						.get(endDayIndex+1).get(swapMove.getEmployee1Index()) != null)
					endDayIndex++;
				// Count initial excess
				int consecutiveWorkingDays = 0;
				for (int dayIndex=startDayIndex; dayIndex<=endDayIndex; dayIndex++) {
					if (solution.assignments.get(dayIndex)
							.get(swapMove.getEmployee1Index()) != null) {
						consecutiveWorkingDays++;
						if (consecutiveWorkingDays > maxConsecutiveWorkingDays)
							initialExcess++;
					} else {
						consecutiveWorkingDays = 0;
					}
				}
				// Count swap excess
				consecutiveWorkingDays = 0;
				for (int dayIndex=startDayIndex; dayIndex<=endDayIndex; dayIndex++) {
					if (swapMove.getResultingAssignment(
							solution, dayIndex, swapMove.getEmployee1Index()) != null) {
						consecutiveWorkingDays++;
						if (consecutiveWorkingDays > maxConsecutiveWorkingDays)
							swapExcess++;
					} else {
						consecutiveWorkingDays = 0;
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
						.get(startDayIndex-1).get(swapMove.getEmployee2Index()) != null)
					startDayIndex--;
				endDayIndex = swapMove.getEndDayIndex();
				while(endDayIndex < solution.assignments.size()-1 && solution.assignments
						.get(endDayIndex+1).get(swapMove.getEmployee2Index()) != null)
					endDayIndex++;
				// Count initial excess
				int consecutiveWorkingDays = 0;
				for (int dayIndex=startDayIndex; dayIndex<=endDayIndex; dayIndex++) {
					if (solution.assignments.get(dayIndex)
							.get(swapMove.getEmployee2Index()) != null) {
						consecutiveWorkingDays++;
						if (consecutiveWorkingDays > maxConsecutiveWorkingDays)
							initialExcess++;
					} else {
						consecutiveWorkingDays = 0;
					}
				}
				// Count swap excess
				consecutiveWorkingDays = 0;
				for (int dayIndex=startDayIndex; dayIndex<=endDayIndex; dayIndex++) {
					if (swapMove.getResultingAssignment(
							solution, dayIndex, swapMove.getEmployee2Index()) != null) {
						consecutiveWorkingDays++;
						if (consecutiveWorkingDays > maxConsecutiveWorkingDays)
							swapExcess++;
					} else {
						consecutiveWorkingDays = 0;
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
			return MaxConsecutiveWorkingDaysConstraint.this;
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
				int consecutiveWorkingDays = 0;
				int startWorkingDayIndex = 0;
				for (int dayIndex=0; dayIndex<solution.assignments.size(); dayIndex++) {
					if (solution.assignments.get(dayIndex).get(employeeIndex) != null) {
						if (consecutiveWorkingDays == 0)
							startWorkingDayIndex = dayIndex;
						consecutiveWorkingDays++;
					} else {
						if (consecutiveWorkingDays > maxConsecutiveWorkingDays) {
							ConstraintViolation violation = new ConstraintViolation(
									MaxConsecutiveWorkingDaysConstraint.this);
							violation.setCost(weightValue*(consecutiveWorkingDays-maxConsecutiveWorkingDays));
							violation.setMessage(Messages.getString("MaxConsecutiveWorkingDaysConstraint.maxConsecutiveWorkingDaysExceeded")); //$NON-NLS-1$
							violation.addAssignmentRangeInScope(
									solution.employees.get(employeeIndex), 
									solution.problem.getSchedulingPeriod().getDate(startWorkingDayIndex),
									solution.problem.getSchedulingPeriod().getDate(dayIndex-1));
							violations.add(violation);
						}
						consecutiveWorkingDays = 0;
					}
				}
				if (consecutiveWorkingDays > maxConsecutiveWorkingDays) {
					ConstraintViolation violation = new ConstraintViolation(
							MaxConsecutiveWorkingDaysConstraint.this);
					violation.setCost(weightValue*(consecutiveWorkingDays-maxConsecutiveWorkingDays));
					violation.setMessage(Messages.getString("MaxConsecutiveWorkingDaysConstraint.maxConsecutiveWorkingDaysExceeded")); //$NON-NLS-1$
					violation.addAssignmentRangeInScope(
							solution.employees.get(employeeIndex), 
							solution.problem.getSchedulingPeriod().getDate(startWorkingDayIndex),
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
			int[] diff = new int[]{0, 0};
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
						.get(startDayIndex-1).get(swapMove.getEmployee1Index()) != null)
					startDayIndex--;
				endDayIndex = swapMove.getEndDayIndex();
				while(endDayIndex < solution.assignments.size()-1 && solution.assignments
						.get(endDayIndex+1).get(swapMove.getEmployee1Index()) != null)
					endDayIndex++;
				// Count initial excess
				int initialExcess = 0;
				int consecutiveWorkingDays = 0;
				for (int dayIndex=startDayIndex; dayIndex<=endDayIndex; dayIndex++) {
					if (solution.assignments.get(dayIndex)
							.get(swapMove.getEmployee1Index()) != null) {
						consecutiveWorkingDays++;
						if (consecutiveWorkingDays > maxConsecutiveWorkingDays)
							initialExcess++;
					} else {
						consecutiveWorkingDays = 0;
					}
				}
				// Count swap excess
				int swapExcess = 0;
				consecutiveWorkingDays = 0;
				for (int dayIndex=startDayIndex; dayIndex<=endDayIndex; dayIndex++) {
					if (swapMove.getResultingAssignment(
							solution, dayIndex, swapMove.getEmployee1Index()) != null) {
						consecutiveWorkingDays++;
						if (consecutiveWorkingDays > maxConsecutiveWorkingDays)
							swapExcess++;
					} else {
						consecutiveWorkingDays = 0;
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
						.get(startDayIndex-1).get(swapMove.getEmployee2Index()) != null)
					startDayIndex--;
				endDayIndex = swapMove.getEndDayIndex();
				while(endDayIndex < solution.assignments.size()-1 && solution.assignments
						.get(endDayIndex+1).get(swapMove.getEmployee2Index()) != null)
					endDayIndex++;
				// Count initial excess
				int initialExcess = 0;
				int consecutiveWorkingDays = 0;
				for (int dayIndex=startDayIndex; dayIndex<=endDayIndex; dayIndex++) {
					if (solution.assignments.get(dayIndex)
							.get(swapMove.getEmployee2Index()) != null) {
						consecutiveWorkingDays++;
						if (consecutiveWorkingDays > maxConsecutiveWorkingDays)
							initialExcess++;
					} else {
						consecutiveWorkingDays = 0;
					}
				}
				// Count swap excess
				int swapExcess = 0;
				consecutiveWorkingDays = 0;
				for (int dayIndex=startDayIndex; dayIndex<=endDayIndex; dayIndex++) {
					if (swapMove.getResultingAssignment(
							solution, dayIndex, swapMove.getEmployee2Index()) != null) {
						consecutiveWorkingDays++;
						if (consecutiveWorkingDays > maxConsecutiveWorkingDays)
							swapExcess++;
					} else {
						consecutiveWorkingDays = 0;
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
		return Messages.getString("MaxConsecutiveWorkingDaysConstraint.name"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getConstraintCostLabel()
	 */
	@Override
	public String getCostLabel() {
		return Messages.getString("MaxConsecutiveWorkingDaysConstraint.costLabel"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription()
	 */
	@Override
	public String getHTMLDescription() {
		return Messages.getString("MaxConsecutiveWorkingDaysConstraint.description"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription(de.uos.inf.ischedule.model.Employee)
	 */
	@Override
	public String getHTMLDescription(Employee employee) {
		if (!cover(employee))
			return null;
		String desc = Messages.getString(
				"MaxConsecutiveWorkingDaysConstraint.descriptionEmployee"); //$NON-NLS-1$
		desc = desc.replaceAll("\\$1", Integer.toString(maxConsecutiveWorkingDays)); //$NON-NLS-1$
		return desc;
	}
	
	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLParametersDescriptions(de.uos.inf.ischedule.model.ShiftSchedulingProblem)
	 */
	@Override
	public List<String> getHTMLParametersDescriptions(
			ShiftSchedulingProblem problem) {
		String paramDesc = Messages.getString(
				"MaxConsecutiveWorkingDaysConstraint.parametersDescription"); //$NON-NLS-1$
		paramDesc = paramDesc.replaceAll("\\$1", scope.getName()); //$NON-NLS-1$
		String imgURL = IconUtils.getImageURL(scope.getIconPath());
		paramDesc = paramDesc.replaceAll("\\$2", imgURL); //$NON-NLS-1$
		paramDesc = paramDesc.replaceAll("\\$3", Integer.toString(maxConsecutiveWorkingDays)); //$NON-NLS-1$
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
