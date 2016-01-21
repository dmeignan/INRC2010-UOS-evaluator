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
 * This constraint defines a minimum number of consecutive working days for an employee.
 * 
 * @author David Meignan
 */
public class MinConsecutiveWorkingDaysConstraint implements Constraint {

	/**
	 * Minimum number of consecutive working days for an employee.
	 */
	protected int minConsecutiveWorkingDays;

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
	private MinConsecutiveWorkingDaysConstraintEvaluator evaluator = null;
	
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
	 * Constructs a minimum-number-of-consecutive-working-days constraint.
	 * 
	 * @param minConsecutiveWorkingDays the minimum number of consecutive working days.
	 * @param scope the contract on which the constraint applies.
	 * @param active the active property of the constraint.
	 * @param defaultWeightValue the default weight value of the constraint.
	 * 
	 * @throws IllegalArgumentException if the minimum number of days
	 * is negative or null, or the rank-index or the default weight value is negative,
	 * or the scope is <code>null</code>.
	 */
	public MinConsecutiveWorkingDaysConstraint(int minConsecutiveWorkingDays, Contract scope,
			boolean active, int defaultWeightValue) {
		if (minConsecutiveWorkingDays <= 1 || defaultWeightValue < 0)
			throw new IllegalArgumentException();
		if (scope == null)
			throw new IllegalArgumentException();
		
		this.minConsecutiveWorkingDays = minConsecutiveWorkingDays;
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
			evaluator = new MinConsecutiveWorkingDaysConstraintEvaluator(problem);
		return evaluator;
	}

	/**
	 * Evaluator of the constraint.
	 */
	class MinConsecutiveWorkingDaysConstraintEvaluator extends
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
		public MinConsecutiveWorkingDaysConstraintEvaluator(ShiftSchedulingProblem problem) {
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
			
			int deficit = 0;
			// Iterates on employees
			for (int employeeIndex: constrainedEmployeeIndexes) {
				int consecutiveWorkingDays = 0;
				for (int dayIndex=0; dayIndex<solution.assignments.size(); dayIndex++) {
					if (solution.assignments.get(dayIndex).get(employeeIndex) != null) {
						consecutiveWorkingDays++;
					} else {
						if (consecutiveWorkingDays > 0 &&
								consecutiveWorkingDays<minConsecutiveWorkingDays) {
							deficit += minConsecutiveWorkingDays-consecutiveWorkingDays;
						}
						consecutiveWorkingDays = 0;
					}
				}
				if (consecutiveWorkingDays > 0 &&
						consecutiveWorkingDays<minConsecutiveWorkingDays) {
					deficit += minConsecutiveWorkingDays-consecutiveWorkingDays;
				}
			}
			// Return cost by weight
			return deficit*weightValue;
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
			
			// Returns a negative cost when the addition of the assignment
			// remove some consecutive working days below minimum value
			
			// Check scope of constraint
			if (!constrainedEmployeeIndexes.contains(employeeIndex))
				return 0;
			
			// Count after
			int consecutiveWorkingDaysAfter = 0;
			for (int dayIndex=assignmentDayIndex+1; dayIndex<solution.assignments.size();
					dayIndex++) {
				if (solution.assignments.get(dayIndex).get(employeeIndex) != null)
					consecutiveWorkingDaysAfter++;
				else
					break;
			}
			// Count before
			int consecutiveWorkingDaysBefore = 0;
			for (int dayIndex=assignmentDayIndex-1; dayIndex>=0;
					dayIndex--) {
				if (solution.assignments.get(dayIndex).get(employeeIndex) != null)
					consecutiveWorkingDaysBefore++;
				else
					break;
			}
			
			if (consecutiveWorkingDaysAfter == 0 &&
					consecutiveWorkingDaysBefore == 0) {
				// No extension
				return 0;
			} else if (consecutiveWorkingDaysAfter == 0) {
				// Extension of working days before
				if (consecutiveWorkingDaysBefore < minConsecutiveWorkingDays) {
					return (-1)*weightValue;
				} else {
					return 0;
				}
			} else if (consecutiveWorkingDaysBefore == 0) {
				// Extension of working days after
				if (consecutiveWorkingDaysAfter < minConsecutiveWorkingDays) {
					return (-1)*weightValue;
				} else {
					return 0;
				}
			} else {
				// Extension of both after and before working days
				// Previous penalty
				int previousPenalty = 0;
				if (consecutiveWorkingDaysBefore < minConsecutiveWorkingDays) {
					previousPenalty += minConsecutiveWorkingDays-consecutiveWorkingDaysBefore;
				}
				if (consecutiveWorkingDaysAfter < minConsecutiveWorkingDays) {
					previousPenalty += minConsecutiveWorkingDays-consecutiveWorkingDaysAfter;
				}
				// New penalty
				int newPenalty = 0;
				if ((consecutiveWorkingDaysBefore+consecutiveWorkingDaysAfter+1)
						< minConsecutiveWorkingDays) {
					newPenalty += minConsecutiveWorkingDays-
							(consecutiveWorkingDaysBefore+consecutiveWorkingDaysAfter+1);
				}
				// Difference
				return weightValue*(newPenalty-previousPenalty);
			}
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
			int initialDeficit = 0;
			int swapDeficit = 0;
			
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
				// Count initial deficit
				int consecutiveWorkingDays = 0;
				for (int dayIndex=startDayIndex; dayIndex<=endDayIndex; dayIndex++) {
					if (solution.assignments.get(dayIndex)
							.get(swapMove.getEmployee1Index()) != null) {
						consecutiveWorkingDays++;
					} else {
						if (consecutiveWorkingDays > 0 &&
								consecutiveWorkingDays<minConsecutiveWorkingDays) {
							initialDeficit += minConsecutiveWorkingDays-consecutiveWorkingDays;
						}
						consecutiveWorkingDays = 0;
					}
				}
				if (consecutiveWorkingDays > 0 &&
						consecutiveWorkingDays<minConsecutiveWorkingDays) {
					initialDeficit += minConsecutiveWorkingDays-consecutiveWorkingDays;
				}
				// Count swap deficit
				consecutiveWorkingDays = 0;
				for (int dayIndex=startDayIndex; dayIndex<=endDayIndex; dayIndex++) {
					if (swapMove.getResultingAssignment(
							solution, dayIndex, swapMove.getEmployee1Index()) != null) {
						consecutiveWorkingDays++;
					} else {
						if (consecutiveWorkingDays > 0 &&
								consecutiveWorkingDays<minConsecutiveWorkingDays) {
							swapDeficit += minConsecutiveWorkingDays-consecutiveWorkingDays;
						}
						consecutiveWorkingDays = 0;
					}
				}
				if (consecutiveWorkingDays > 0 &&
						consecutiveWorkingDays<minConsecutiveWorkingDays) {
					swapDeficit += minConsecutiveWorkingDays-consecutiveWorkingDays;
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
				// Count initial deficit
				int consecutiveWorkingDays = 0;
				for (int dayIndex=startDayIndex; dayIndex<=endDayIndex; dayIndex++) {
					if (solution.assignments.get(dayIndex)
							.get(swapMove.getEmployee2Index()) != null) {
						consecutiveWorkingDays++;
					} else {
						if (consecutiveWorkingDays > 0 &&
								consecutiveWorkingDays<minConsecutiveWorkingDays) {
							initialDeficit += minConsecutiveWorkingDays-consecutiveWorkingDays;
						}
						consecutiveWorkingDays = 0;
					}
				}
				if (consecutiveWorkingDays > 0 &&
						consecutiveWorkingDays<minConsecutiveWorkingDays) {
					initialDeficit += minConsecutiveWorkingDays-consecutiveWorkingDays;
				}
				// Count swap deficit
				consecutiveWorkingDays = 0;
				for (int dayIndex=startDayIndex; dayIndex<=endDayIndex; dayIndex++) {
					if (swapMove.getResultingAssignment(
							solution, dayIndex, swapMove.getEmployee2Index()) != null) {
						consecutiveWorkingDays++;
					} else {
						if (consecutiveWorkingDays > 0 &&
								consecutiveWorkingDays<minConsecutiveWorkingDays) {
							swapDeficit += minConsecutiveWorkingDays-consecutiveWorkingDays;
						}
						consecutiveWorkingDays = 0;
					}
				}
				if (consecutiveWorkingDays > 0 &&
						consecutiveWorkingDays<minConsecutiveWorkingDays) {
					swapDeficit += minConsecutiveWorkingDays-consecutiveWorkingDays;
				}
			}
			return (swapDeficit-initialDeficit)*weightValue;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getConstraint()
		 */
		@Override
		public Constraint getConstraint() {
			return MinConsecutiveWorkingDaysConstraint.this;
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
						if (consecutiveWorkingDays > 0 &&
								consecutiveWorkingDays<minConsecutiveWorkingDays) {
							ConstraintViolation violation = new ConstraintViolation(
									MinConsecutiveWorkingDaysConstraint.this);
							violation.setCost(weightValue*(minConsecutiveWorkingDays-consecutiveWorkingDays));
							violation.setMessage(Messages.getString("MinConsecutiveWorkingDaysConstraint.minConsecutiveWorkingDaysUnsatisfied")); //$NON-NLS-1$
							violation.addAssignmentRangeInScope(
									solution.employees.get(employeeIndex), 
									solution.problem.getSchedulingPeriod().getDate(startWorkingDayIndex),
									solution.problem.getSchedulingPeriod().getDate(dayIndex-1));
							violations.add(violation);
						}
						consecutiveWorkingDays = 0;
					}
				}
				if (consecutiveWorkingDays > 0 &&
						consecutiveWorkingDays<minConsecutiveWorkingDays) {
					ConstraintViolation violation = new ConstraintViolation(
							MinConsecutiveWorkingDaysConstraint.this);
					violation.setCost(weightValue*(minConsecutiveWorkingDays-consecutiveWorkingDays));
					violation.setMessage(Messages.getString("MinConsecutiveWorkingDaysConstraint.minConsecutiveWorkingDaysUnsatisfied")); //$NON-NLS-1$
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
				// Count initial deficit
				int initialDeficit = 0;
				int consecutiveWorkingDays = 0;
				for (int dayIndex=startDayIndex; dayIndex<=endDayIndex; dayIndex++) {
					if (solution.assignments.get(dayIndex)
							.get(swapMove.getEmployee1Index()) != null) {
						consecutiveWorkingDays++;
					} else {
						if (consecutiveWorkingDays > 0 &&
								consecutiveWorkingDays<minConsecutiveWorkingDays) {
							initialDeficit += minConsecutiveWorkingDays-consecutiveWorkingDays;
						}
						consecutiveWorkingDays = 0;
					}
				}
				if (consecutiveWorkingDays > 0 &&
						consecutiveWorkingDays<minConsecutiveWorkingDays) {
					initialDeficit += minConsecutiveWorkingDays-consecutiveWorkingDays;
				}
				// Count swap deficit
				int swapDeficit = 0;
				consecutiveWorkingDays = 0;
				for (int dayIndex=startDayIndex; dayIndex<=endDayIndex; dayIndex++) {
					if (swapMove.getResultingAssignment(
							solution, dayIndex, swapMove.getEmployee1Index()) != null) {
						consecutiveWorkingDays++;
					} else {
						if (consecutiveWorkingDays > 0 &&
								consecutiveWorkingDays<minConsecutiveWorkingDays) {
							swapDeficit += minConsecutiveWorkingDays-consecutiveWorkingDays;
						}
						consecutiveWorkingDays = 0;
					}
				}
				if (consecutiveWorkingDays > 0 &&
						consecutiveWorkingDays<minConsecutiveWorkingDays) {
					swapDeficit += minConsecutiveWorkingDays-consecutiveWorkingDays;
				}
				if (initialDeficit > swapDeficit) {
					diff[0] += initialDeficit-swapDeficit;
				} else if (swapDeficit > initialDeficit) {
					diff[1] += swapDeficit-initialDeficit;
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
				// Count initial deficit
				int initialDeficit = 0;
				int consecutiveWorkingDays = 0;
				for (int dayIndex=startDayIndex; dayIndex<=endDayIndex; dayIndex++) {
					if (solution.assignments.get(dayIndex)
							.get(swapMove.getEmployee2Index()) != null) {
						consecutiveWorkingDays++;
					} else {
						if (consecutiveWorkingDays > 0 &&
								consecutiveWorkingDays<minConsecutiveWorkingDays) {
							initialDeficit += minConsecutiveWorkingDays-consecutiveWorkingDays;
						}
						consecutiveWorkingDays = 0;
					}
				}
				if (consecutiveWorkingDays > 0 &&
						consecutiveWorkingDays<minConsecutiveWorkingDays) {
					initialDeficit += minConsecutiveWorkingDays-consecutiveWorkingDays;
				}
				// Count swap deficit
				int swapDeficit = 0;
				consecutiveWorkingDays = 0;
				for (int dayIndex=startDayIndex; dayIndex<=endDayIndex; dayIndex++) {
					if (swapMove.getResultingAssignment(
							solution, dayIndex, swapMove.getEmployee2Index()) != null) {
						consecutiveWorkingDays++;
					} else {
						if (consecutiveWorkingDays > 0 &&
								consecutiveWorkingDays<minConsecutiveWorkingDays) {
							swapDeficit += minConsecutiveWorkingDays-consecutiveWorkingDays;
						}
						consecutiveWorkingDays = 0;
					}
				}
				if (consecutiveWorkingDays > 0 &&
						consecutiveWorkingDays<minConsecutiveWorkingDays) {
					swapDeficit += minConsecutiveWorkingDays-consecutiveWorkingDays;
				}
				if (initialDeficit > swapDeficit) {
					diff[0] += initialDeficit-swapDeficit;
				} else if (swapDeficit > initialDeficit) {
					diff[1] += swapDeficit-initialDeficit;
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
		return Messages.getString("MinConsecutiveWorkingDaysConstraint.name"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getConstraintCostLabel()
	 */
	@Override
	public String getCostLabel() {
		return Messages.getString("MinConsecutiveWorkingDaysConstraint.costLabel"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription()
	 */
	@Override
	public String getHTMLDescription() {
		return Messages.getString("MinConsecutiveWorkingDaysConstraint.description"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription(de.uos.inf.ischedule.model.Employee)
	 */
	@Override
	public String getHTMLDescription(Employee employee) {
		if (!cover(employee))
			return null;
		String desc = Messages.getString(
				"MinConsecutiveWorkingDaysConstraint.descriptionEmployee"); //$NON-NLS-1$
		desc = desc.replaceAll("\\$1", Integer.toString(minConsecutiveWorkingDays)); //$NON-NLS-1$
		return desc;
	}
	
	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLParametersDescriptions(de.uos.inf.ischedule.model.ShiftSchedulingProblem)
	 */
	@Override
	public List<String> getHTMLParametersDescriptions(
			ShiftSchedulingProblem problem) {
		String paramDesc = Messages.getString(
				"MinConsecutiveWorkingDaysConstraint.parametersDescription"); //$NON-NLS-1$
		paramDesc = paramDesc.replaceAll("\\$1", scope.getName()); //$NON-NLS-1$
		String imgURL = IconUtils.getImageURL(scope.getIconPath());
		paramDesc = paramDesc.replaceAll("\\$2", imgURL); //$NON-NLS-1$
		paramDesc = paramDesc.replaceAll("\\$3", Integer.toString(minConsecutiveWorkingDays)); //$NON-NLS-1$
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
