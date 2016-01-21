/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import de.uos.inf.ischedule.model.heuristic.SwapMove;
import de.uos.inf.ischedule.util.Messages;


/**
 * This constraint ensures that an employee is not assigned to a night shift the day before
 * a free weekend.
 * 
 * TODO the first weekend may potentially be missing if it is cut in the planning horizon.
 * TODO See ConsecutiveWorkingWeekendsAttribute for a fix.
 * 
 * @author David Meignan
 */
public class NoNightShiftBeforeFreeWeekendsConstraint implements Constraint {

	/**
	 * The set of night shifts to consider.
	 */
	protected TreeSet<Shift> nightShifts;
	
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
	private NoNightShiftBeforeFreeWeekendsConstraintEvaluator evaluator = null;
	
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
	 * Constructs a no-night-shift-before-free-weekends constraint.
	 * 
	 * @param weekendDefinition the definition of the weekend.
	 * @param nightShifts the list of night shifts.
	 * @param scope the contract on which the constraint applies.
	 * @param active the active property of the constraint.
	 * @param defaultWeightValue the default weight value of the constraint.
	 * 
	 * @throws IllegalArgumentException if the rank-index or the default weight
	 * value is negative, or the set of night-shifts, scope or weekend definition is 
	 * <code>null</code>.
	 */
	public NoNightShiftBeforeFreeWeekendsConstraint(
			Collection<Shift> nightShifts, Contract scope,
			boolean active, int defaultWeightValue) {
		if (defaultWeightValue < 0)
			throw new IllegalArgumentException();
		if (scope == null || nightShifts == null)
			throw new IllegalArgumentException();
		
		this.nightShifts = new TreeSet<Shift>(nightShifts);
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
			evaluator = new NoNightShiftBeforeFreeWeekendsConstraintEvaluator(problem);
		return evaluator;
	}

	/**
	 * Evaluator of the constraint.
	 */
	class NoNightShiftBeforeFreeWeekendsConstraintEvaluator extends
			ConstraintEvaluator {

		/**
		 * Indexes of start and end dates of weekends.
		 */
		ArrayList<Integer> weekendStartIndexes;
		ArrayList<Integer> weekendEndIndexes;
		
		/**
		 * List of employee indexes on which the constraint applies.
		 */
		ArrayList<Integer> constrainedEmployeeIndexes;

		/**
		 * Creates an evaluator of the constraint.
		 * 
		 * TODO the first weekend may potentially be missing if it is cut in the planning horizon.
		 * TODO See ConsecutiveWorkingWeekendsAttribute for a fix.
		 * 
		 * @param problem the shift scheduling problem.
		 */
		public NoNightShiftBeforeFreeWeekendsConstraintEvaluator(
				ShiftSchedulingProblem problem) {
			// Weekend start day indexes
			weekendStartIndexes = new ArrayList<Integer>();
			weekendEndIndexes = new ArrayList<Integer>();
			for (int dayIndex=0; dayIndex<problem.schedulingPeriod.days.size();
					dayIndex++) {
				if (problem.schedulingPeriod.getDayOfWeek(dayIndex) == 
						scope.weekendType.getStartDayOfWeek()) {
					// Remove weekend starting at index 0
					if (dayIndex != 0) {
						weekendStartIndexes.add(dayIndex);
						int weekendEndIndex = dayIndex
								+ scope.weekendType.getDuration()-1;
						// Adjust if weekend cut
						if (weekendEndIndex >= problem.schedulingPeriod.size())
							weekendEndIndex = problem.schedulingPeriod.size()-1;
						weekendEndIndexes.add(weekendEndIndex);
					}
				}
			}
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
			
			int inadequateNightShifts = 0;
			// Iterates on employees
			for (int employeeIndex: constrainedEmployeeIndexes) {
				// Iterates on weekend start
				for (int i=0; i<weekendStartIndexes.size(); i++) {
					// Check free weekend and night shift before
					int startDay = weekendStartIndexes.get(i);
					// Note that the list of weekends does not contain
					// weekend starting at day-index 0 
					Shift beforeWeekendAssignment = solution.assignments
							.get(startDay-1).get(employeeIndex);
					if (	beforeWeekendAssignment != null &&
							nightShifts.contains(beforeWeekendAssignment) &&
							!isWorkingWeekend(solution, i, employeeIndex)
							) {
						inadequateNightShifts++;
					}
				}
			}
			// Return cost by weight
			return inadequateNightShifts*weightValue;
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
			
			// Return cost when a shift is assigned to an empty weekend with a night
			// shift before.

			// Check scope of constraint
			if (!constrainedEmployeeIndexes.contains(employeeIndex))
				return 0;
			
			// Check if assignment day index in a weekend
			int weekendIndex = weekendIndex(assignmentDayIndex);
			if (weekendIndex == -1)
				return 0;
			
			// Check if planning day before weekend
			if (weekendStartIndexes.get(weekendIndex) == 0)
				return 0;
			
			// Check if empty weekend
			if (isWorkingWeekend(solution, weekendIndex, employeeIndex))
				return 0;
			
			// Check night shift before weekend
			Shift beforeWeekendAssignment = solution.assignments
					.get(weekendStartIndexes.get(weekendIndex)-1).get(employeeIndex);
			if (beforeWeekendAssignment != null &&
					nightShifts.contains(beforeWeekendAssignment) )
				return weightValue;
			return 0;
		}
		
		/**
		 * Returns the index of the weekend containing the day-index. Returns 
		 * <code>-1</code> if the day-index is not a weekend day.
		 * 
		 * @param dayIndex the day-index.
		 * @return the index of the weekend containing the day-index. Returns 
		 * <code>-1</code> if the day-index is not a weekend day.
		 */
		private int weekendIndex(int dayIndex) {
			for (int i=0; i<weekendStartIndexes.size(); i++) {
				int startIndex = weekendStartIndexes.get(i);
				int endIndex = weekendEndIndexes.get(i);
				if (dayIndex >= startIndex && dayIndex <= endIndex) {
					// In the weekend
					return i;
				}
				if (dayIndex < endIndex)
					return -1;
			}
			return -1;
		}

		/**
		 * Returns <code>true</code> if the weekend contains at least one working day,
		 * <code>false</code> otherwise.
		 * 
		 * @param solution the solution to check.
		 * @param weekendIndex the weekend index.
		 * @param employeeIndex the employee index
		 * @return <code>true</code> if the weekend contains at least one working day,
		 * <code>false</code> otherwise.
		 */
		private boolean isWorkingWeekend(Solution solution, 
				int weekendIndex, int employeeIndex) {
			int startIndex = weekendStartIndexes.get(weekendIndex);
			int endIndex = weekendEndIndexes.get(weekendIndex);
			for (int dayIndex=startIndex; dayIndex<=endIndex; dayIndex++) {
				if (solution.assignments.get(dayIndex).get(employeeIndex) != null) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Returns <code>true</code> if the weekend contains at least one working day
		 * taking into account swap move, <code>false</code> otherwise.
		 * 
		 * @param solution the solution to check.
		 * @param weekendIndex the weekend index.
		 * @param employeeIndex the employee index
		 * @return <code>true</code> if the weekend contains at least one working day
		 * taking into account swap move, <code>false</code> otherwise.
		 */
		private boolean isWorkingWeekend(Solution solution, 
				int weekendIndex, int employeeIndex, SwapMove swap) {
			int startIndex = weekendStartIndexes.get(weekendIndex);
			int endIndex = weekendEndIndexes.get(weekendIndex);
			for (int dayIndex=startIndex; dayIndex<=endIndex; dayIndex++) {
				if (swap.getResultingAssignment(solution, dayIndex, employeeIndex) != null) {
					return true;
				}
			}
			return false;
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
			
			// Compute previous and new penalties for night-shift before free weekend
			// covered by swap 
			int initialPartialPenalty = 0;
			int swapPartialPenalty = 0;
			// Iterates on weekends
			for (int i=0; i<weekendStartIndexes.size(); i++) {
				int startIndex = weekendStartIndexes.get(i);
				int endIndex = weekendEndIndexes.get(i);
				if (
						(swapMove.getStartDayIndex() <= endIndex &&
						swapMove.getEndDayIndex() >= startIndex-1) ) {
					// Check free weekend and night shift
					// Employee 1
					if (employee1Constrained) {
						// Note that the list of weekends does not contain
						// weekend starting at day-index 0 
						Shift beforeWeekendAssignment = solution.assignments
								.get(startIndex-1).get(swapMove.getEmployee1Index());
						if (	beforeWeekendAssignment != null &&
								nightShifts.contains(beforeWeekendAssignment) &&
								!isWorkingWeekend(solution, i, 
										swapMove.getEmployee1Index())
								) {
							initialPartialPenalty++;
						}
						beforeWeekendAssignment = swapMove.getResultingAssignment(
								solution, startIndex-1, swapMove.getEmployee1Index());
						if (	beforeWeekendAssignment != null &&
								nightShifts.contains(beforeWeekendAssignment) &&
								!isWorkingWeekend(solution, i, 
										swapMove.getEmployee1Index(), swapMove)
								) {
							swapPartialPenalty++;
						}
					}
					// Employee 2
					if (employee2Constrained) {
						// Note that the list of weekends does not contain
						// weekend starting at day-index 0 
						Shift beforeWeekendAssignment = solution.assignments
								.get(startIndex-1).get(swapMove.getEmployee2Index());
						if (	beforeWeekendAssignment != null &&
								nightShifts.contains(beforeWeekendAssignment) &&
								!isWorkingWeekend(solution, i, 
										swapMove.getEmployee2Index())
								) {
							initialPartialPenalty++;
						}
						beforeWeekendAssignment = swapMove.getResultingAssignment(
								solution, startIndex-1, swapMove.getEmployee2Index());
						if (	beforeWeekendAssignment != null &&
								nightShifts.contains(beforeWeekendAssignment) &&
								!isWorkingWeekend(solution, i, 
										swapMove.getEmployee2Index(), swapMove)
								) {
							swapPartialPenalty++;
						}
					}
				}
				if (swapMove.getEndDayIndex() <= endIndex)
					break;
			}
			return (swapPartialPenalty-initialPartialPenalty)*weightValue;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getConstraint()
		 */
		@Override
		public Constraint getConstraint() {
			return NoNightShiftBeforeFreeWeekendsConstraint.this;
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
				// Iterates on weekend start
				for (int i=0; i<weekendStartIndexes.size(); i++) {
					// Check free weekend and night shift before
					int startDay = weekendStartIndexes.get(i);
					// Note that the list of weekends does not contain
					// weekend starting at day-index 0 
					Shift beforeWeekendAssignment = solution.assignments
							.get(startDay-1).get(employeeIndex);
					if (	beforeWeekendAssignment != null &&
							nightShifts.contains(beforeWeekendAssignment) &&
							!isWorkingWeekend(solution, i, employeeIndex)
							) {
						ConstraintViolation violation = new ConstraintViolation(
								NoNightShiftBeforeFreeWeekendsConstraint.this);
						violation.setCost(weightValue);
						violation.setMessage(Messages.getString("NoNightShiftBeforeFreeWeekendsConstraint.nightShiftBeforeFreeWeekend")); //$NON-NLS-1$
						violation.addAssignmentRangeInScope(
								solution.employees.get(employeeIndex),
								solution.problem.getSchedulingPeriod().getDate(startDay-1),
								solution.problem.getSchedulingPeriod().getDate(
										weekendEndIndexes.get(i)));
						violations.add(violation);
					}
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
			
			// Compute previous and new penalties for night-shift before free weekend
			// covered by swap
			
			// Iterates on weekends
			for (int i=0; i<weekendStartIndexes.size(); i++) {
				int startIndex = weekendStartIndexes.get(i);
				int endIndex = weekendEndIndexes.get(i);
				if (
						(swapMove.getStartDayIndex() <= endIndex &&
						swapMove.getEndDayIndex() >= startIndex-1) ) {
					// Check free weekend and night shift
					// Employee 1
					if (employee1Constrained) {
						boolean initialPenalized;
						boolean swapPenalized;
						// Note that the list of weekends does not contain
						// weekend starting at day-index 0 
						Shift beforeWeekendAssignment = solution.assignments
								.get(startIndex-1).get(swapMove.getEmployee1Index());
						initialPenalized = (beforeWeekendAssignment != null &&
								nightShifts.contains(beforeWeekendAssignment) &&
								!isWorkingWeekend(solution, i, 
										swapMove.getEmployee1Index())
								);
						beforeWeekendAssignment = swapMove.getResultingAssignment(
								solution, startIndex-1, swapMove.getEmployee1Index());
						swapPenalized = (beforeWeekendAssignment != null &&
								nightShifts.contains(beforeWeekendAssignment) &&
								!isWorkingWeekend(solution, i, 
										swapMove.getEmployee1Index(), swapMove)
								);
						if (initialPenalized && !swapPenalized) {
							diff[0]++;
						} else if (swapPenalized && !initialPenalized) {
							diff[1]++;
						}
					}
					// Employee 2
					if (employee2Constrained) {
						boolean initialPenalized;
						boolean swapPenalized;
						// Note that the list of weekends does not contain
						// weekend starting at day-index 0 
						Shift beforeWeekendAssignment = solution.assignments
								.get(startIndex-1).get(swapMove.getEmployee2Index());
						initialPenalized = (beforeWeekendAssignment != null &&
								nightShifts.contains(beforeWeekendAssignment) &&
								!isWorkingWeekend(solution, i, 
										swapMove.getEmployee2Index())
								);
						beforeWeekendAssignment = swapMove.getResultingAssignment(
								solution, startIndex-1, swapMove.getEmployee2Index());
						swapPenalized = (beforeWeekendAssignment != null &&
								nightShifts.contains(beforeWeekendAssignment) &&
								!isWorkingWeekend(solution, i, 
										swapMove.getEmployee2Index(), swapMove)
								);
						if (initialPenalized && !swapPenalized) {
							diff[0]++;
						} else if (swapPenalized && !initialPenalized) {
							diff[1]++;
						}
					}
				}
				if (swapMove.getEndDayIndex() <= endIndex)
					break;
			}
			return diff;
		}
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getName()
	 */
	@Override
	public String getName() {
		return Messages.getString("NoNightShiftBeforeFreeWeekendsConstraint.name"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getConstraintCostLabel()
	 */
	@Override
	public String getCostLabel() {
		return Messages.getString("NoNightShiftBeforeFreeWeekendsConstraint.costLabel"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription()
	 */
	@Override
	public String getHTMLDescription() {
		return Messages.getString("NoNightShiftBeforeFreeWeekendsConstraint.description"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription(de.uos.inf.ischedule.model.Employee)
	 */
	@Override
	public String getHTMLDescription(Employee employee) {
		if (!cover(employee))
			return null;
		String desc = Messages.getString(
				"NoNightShiftBeforeFreeWeekendsConstraint.descriptionEmployee"); //$NON-NLS-1$
		return desc;
	}
	
	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLParametersDescriptions(de.uos.inf.ischedule.model.ShiftSchedulingProblem)
	 */
	@Override
	public List<String> getHTMLParametersDescriptions(
			ShiftSchedulingProblem problem) {
		return null;
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
