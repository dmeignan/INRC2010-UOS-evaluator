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
 * This constraint defines a minimum number of consecutive working weekends for an employee.
 * 
 * TODO the first weekend may potentially be missing if it is cut in the planning horizon.
 * TODO See ConsecutiveWorkingWeekendsAttribute for a fix.
 * 
 * @author David Meignan
 */
public class MinConsecutiveWorkingWeekendsConstraint implements Constraint {

	/**
	 * Minimum number of consecutive working weekends for an employee.
	 */
	protected int minConsecutiveWorkingWeekends;
	
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
	private MinConsecutiveWorkingWeekendsConstraintEvaluator evaluator = null;
	
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
	 * Constructs a minimum-number-of-consecutive-working-weekends constraint.
	 * 
	 * @param minConsecutiveWorkingWeekends the minimum number of consecutive working weekends.
	 * @param weekendDefinition the definition of the weekend.
	 * @param scope the contract on which the constraint applies.
	 * @param active the active property of the constraint.
	 * @param defaultWeightValue the default weight value of the constraint.
	 * 
	 * @throws IllegalArgumentException if the minimum number of weekends
	 * is negative or null, or the rank-index or the default weight value is negative,
	 * or the scope or weekend definition is <code>null</code>.
	 */
	public MinConsecutiveWorkingWeekendsConstraint(int minConsecutiveWorkingWeekends,
			Contract scope,
			boolean active, int defaultWeightValue) {
		if (minConsecutiveWorkingWeekends <= 1 || defaultWeightValue < 0)
			throw new IllegalArgumentException();
		if (scope == null)
			throw new IllegalArgumentException();
		
		this.minConsecutiveWorkingWeekends = minConsecutiveWorkingWeekends;
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
			evaluator = new MinConsecutiveWorkingWeekendsConstraintEvaluator(problem);
		return evaluator;
	}

	/**
	 * Evaluator of the constraint.
	 */
	class MinConsecutiveWorkingWeekendsConstraintEvaluator extends
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
		public MinConsecutiveWorkingWeekendsConstraintEvaluator(
				ShiftSchedulingProblem problem) {
			// Weekend start day indexes
			weekendStartIndexes = new ArrayList<Integer>();
			weekendEndIndexes = new ArrayList<Integer>();
			for (int dayIndex=0; dayIndex<problem.schedulingPeriod.days.size();
					dayIndex++) {
				if (problem.schedulingPeriod.getDayOfWeek(dayIndex) == 
						scope.weekendType.getStartDayOfWeek()) {
					weekendStartIndexes.add(dayIndex);
					int weekendEndIndex = dayIndex
							+ scope.weekendType.getDuration()-1;
					// Adjust if weekend cut
					if (weekendEndIndex >= problem.schedulingPeriod.size())
						weekendEndIndex = problem.schedulingPeriod.size()-1;
					weekendEndIndexes.add(weekendEndIndex);
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
			
			int deficit = 0;
			// Iterates on employees
			for (int employeeIndex: constrainedEmployeeIndexes) {
				int consecutiveWeekend = 0;
				// Iterates on weekend start
				for (int i=0; i<weekendStartIndexes.size(); i++) {
					// Check if working weekend
					if (isWorkingWeekend(solution, i, employeeIndex)) {
						consecutiveWeekend++;
					} else {
						if (consecutiveWeekend > 0 &&
								consecutiveWeekend < minConsecutiveWorkingWeekends) {
							deficit += minConsecutiveWorkingWeekends-consecutiveWeekend;
						}
						consecutiveWeekend = 0;
					}
				}
				if (consecutiveWeekend > 0 &&
						consecutiveWeekend < minConsecutiveWorkingWeekends) {
					deficit += minConsecutiveWorkingWeekends-consecutiveWeekend;
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
				int employeeIndex, Shift shift, int dayIndex) {
			return 0;
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

			// Check if move modifies work patterns
			if (!swapMove.modifyWorkingPattern(solution))
				return 0;
			
			// Compute initial and swap excess
			int initialDeficit = 0;
			int swapDeficit = 0;
			
			// Employee 1
			if (employee1Constrained) {
				int consecutiveWeekend = 0;
				// Compute initial deficit
				for (int i=0; i<weekendStartIndexes.size(); i++) {
					// Check if working weekend
					if (isWorkingWeekend(solution, i, swapMove.getEmployee1Index())) {
						consecutiveWeekend++;
					} else {
						if (consecutiveWeekend > 0 &&
								consecutiveWeekend < minConsecutiveWorkingWeekends) {
							initialDeficit += minConsecutiveWorkingWeekends-consecutiveWeekend;
						}
						consecutiveWeekend = 0;
					}
				}
				if (consecutiveWeekend > 0 &&
						consecutiveWeekend < minConsecutiveWorkingWeekends) {
					initialDeficit += minConsecutiveWorkingWeekends-consecutiveWeekend;
				}
				// Compute swap deficit
				consecutiveWeekend = 0;
				for (int i=0; i<weekendStartIndexes.size(); i++) {
					if (isWorkingWeekend(solution, i, swapMove.getEmployee1Index(),
							swapMove)) {
						consecutiveWeekend++;
					} else {
						if (consecutiveWeekend > 0 &&
								consecutiveWeekend < minConsecutiveWorkingWeekends) {
							swapDeficit += minConsecutiveWorkingWeekends-consecutiveWeekend;
						}
						consecutiveWeekend = 0;
					}
				}
				if (consecutiveWeekend > 0 &&
						consecutiveWeekend < minConsecutiveWorkingWeekends) {
					swapDeficit += minConsecutiveWorkingWeekends-consecutiveWeekend;
				}
			}
			
			// Employee 2
			if (employee2Constrained) {
				int consecutiveWeekend = 0;
				// Compute initial deficit
				for (int i=0; i<weekendStartIndexes.size(); i++) {
					// Check if working weekend
					if (isWorkingWeekend(solution, i, swapMove.getEmployee2Index())) {
						consecutiveWeekend++;
					} else {
						if (consecutiveWeekend > 0 &&
								consecutiveWeekend < minConsecutiveWorkingWeekends) {
							initialDeficit += minConsecutiveWorkingWeekends-consecutiveWeekend;
						}
						consecutiveWeekend = 0;
					}
				}
				if (consecutiveWeekend > 0 &&
						consecutiveWeekend < minConsecutiveWorkingWeekends) {
					initialDeficit += minConsecutiveWorkingWeekends-consecutiveWeekend;
				}
				// Compute swap deficit
				consecutiveWeekend = 0;
				for (int i=0; i<weekendStartIndexes.size(); i++) {
					if (isWorkingWeekend(solution, i, swapMove.getEmployee2Index(),
							swapMove)) {
						consecutiveWeekend++;
					} else {
						if (consecutiveWeekend > 0 &&
								consecutiveWeekend < minConsecutiveWorkingWeekends) {
							swapDeficit += minConsecutiveWorkingWeekends-consecutiveWeekend;
						}
						consecutiveWeekend = 0;
					}
				}
				if (consecutiveWeekend > 0 &&
						consecutiveWeekend < minConsecutiveWorkingWeekends) {
					swapDeficit += minConsecutiveWorkingWeekends-consecutiveWeekend;
				}
			}
			
			return (swapDeficit-initialDeficit)*weightValue;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getConstraint()
		 */
		@Override
		public Constraint getConstraint() {
			return MinConsecutiveWorkingWeekendsConstraint.this;
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
				int consecutiveWeekend = 0;
				int startWorkingWeekend = 0;
				// Iterates on weekend start
				for (int i=0; i<weekendStartIndexes.size(); i++) {
					// Check if working weekend
					if (isWorkingWeekend(solution, i, employeeIndex)) {
						if (consecutiveWeekend == 0)
							startWorkingWeekend = i;
						consecutiveWeekend++;
					} else {
						if (consecutiveWeekend > 0 &&
								consecutiveWeekend < minConsecutiveWorkingWeekends) {
							ConstraintViolation violation = new ConstraintViolation(
									MinConsecutiveWorkingWeekendsConstraint.this);
							violation.setCost(weightValue*(minConsecutiveWorkingWeekends-consecutiveWeekend));
							violation.setMessage(Messages.getString("MinConsecutiveWorkingWeekendsConstraint.minConsecutiveWorkingWeekendUnsatisfied")); //$NON-NLS-1$
							for (int weRange=startWorkingWeekend; weRange<i; weRange++) {
								violation.addAssignmentRangeInScope(
										solution.employees.get(employeeIndex),
										solution.problem.getSchedulingPeriod().getDate(weekendStartIndexes.get(weRange)),
										solution.problem.getSchedulingPeriod().getDate(weekendEndIndexes.get(weRange)));
							}
							violations.add(violation);
						}
						consecutiveWeekend = 0;
					}
				}
				if (consecutiveWeekend > 0 &&
						consecutiveWeekend < minConsecutiveWorkingWeekends) {
					ConstraintViolation violation = new ConstraintViolation(
							MinConsecutiveWorkingWeekendsConstraint.this);
					violation.setCost(weightValue*(minConsecutiveWorkingWeekends-consecutiveWeekend));
					violation.setMessage(Messages.getString("MinConsecutiveWorkingWeekendsConstraint.minConsecutiveWorkingWeekendUnsatisfied")); //$NON-NLS-1$
					for (int weRange=startWorkingWeekend; weRange<weekendEndIndexes.size(); weRange++) {
						violation.addAssignmentRangeInScope(
								solution.employees.get(employeeIndex),
								solution.problem.getSchedulingPeriod().getDate(weekendStartIndexes.get(weRange)),
								solution.problem.getSchedulingPeriod().getDate(weekendEndIndexes.get(weRange)));
					}
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
				// Compute initial deficit
				int initialDeficit = 0;
				int consecutiveWeekend = 0;
				for (int i=0; i<weekendStartIndexes.size(); i++) {
					// Check if working weekend
					if (isWorkingWeekend(solution, i, swapMove.getEmployee1Index())) {
						consecutiveWeekend++;
					} else {
						if (consecutiveWeekend > 0 &&
								consecutiveWeekend < minConsecutiveWorkingWeekends) {
							initialDeficit += minConsecutiveWorkingWeekends-consecutiveWeekend;
						}
						consecutiveWeekend = 0;
					}
				}
				if (consecutiveWeekend > 0 &&
						consecutiveWeekend < minConsecutiveWorkingWeekends) {
					initialDeficit += minConsecutiveWorkingWeekends-consecutiveWeekend;
				}
				// Compute swap deficit
				int swapDeficit = 0;
				consecutiveWeekend = 0;
				for (int i=0; i<weekendStartIndexes.size(); i++) {
					if (isWorkingWeekend(solution, i, swapMove.getEmployee1Index(),
							swapMove)) {
						consecutiveWeekend++;
					} else {
						if (consecutiveWeekend > 0 &&
								consecutiveWeekend < minConsecutiveWorkingWeekends) {
							swapDeficit += minConsecutiveWorkingWeekends-consecutiveWeekend;
						}
						consecutiveWeekend = 0;
					}
				}
				if (consecutiveWeekend > 0 &&
						consecutiveWeekend < minConsecutiveWorkingWeekends) {
					swapDeficit += minConsecutiveWorkingWeekends-consecutiveWeekend;
				}
				if (initialDeficit > swapDeficit) {
					diff[0] += initialDeficit-swapDeficit;
				} else if (swapDeficit > initialDeficit) {
					diff[1] += swapDeficit-initialDeficit;
				}
			}
			
			// Employee 2
			if (employee2Constrained) {
				// Compute initial deficit
				int initialDeficit = 0;
				int consecutiveWeekend = 0;
				for (int i=0; i<weekendStartIndexes.size(); i++) {
					// Check if working weekend
					if (isWorkingWeekend(solution, i, swapMove.getEmployee2Index())) {
						consecutiveWeekend++;
					} else {
						if (consecutiveWeekend > 0 &&
								consecutiveWeekend < minConsecutiveWorkingWeekends) {
							initialDeficit += minConsecutiveWorkingWeekends-consecutiveWeekend;
						}
						consecutiveWeekend = 0;
					}
				}
				if (consecutiveWeekend > 0 &&
						consecutiveWeekend < minConsecutiveWorkingWeekends) {
					initialDeficit += minConsecutiveWorkingWeekends-consecutiveWeekend;
				}
				// Compute swap deficit
				int swapDeficit = 0;
				consecutiveWeekend = 0;
				for (int i=0; i<weekendStartIndexes.size(); i++) {
					if (isWorkingWeekend(solution, i, swapMove.getEmployee2Index(),
							swapMove)) {
						consecutiveWeekend++;
					} else {
						if (consecutiveWeekend > 0 &&
								consecutiveWeekend < minConsecutiveWorkingWeekends) {
							swapDeficit += minConsecutiveWorkingWeekends-consecutiveWeekend;
						}
						consecutiveWeekend = 0;
					}
				}
				if (consecutiveWeekend > 0 &&
						consecutiveWeekend < minConsecutiveWorkingWeekends) {
					swapDeficit += minConsecutiveWorkingWeekends-consecutiveWeekend;
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
		return Messages.getString("MinConsecutiveWorkingWeekendsConstraint.name"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getConstraintCostLabel()
	 */
	@Override
	public String getCostLabel() {
		return Messages.getString("MinConsecutiveWorkingWeekendsConstraint.costLabel"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription()
	 */
	@Override
	public String getHTMLDescription() {
		return Messages.getString("MinConsecutiveWorkingWeekendsConstraint.description"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription(de.uos.inf.ischedule.model.Employee)
	 */
	@Override
	public String getHTMLDescription(Employee employee) {
		if (!cover(employee))
			return null;
		String desc = Messages.getString(
				"MinConsecutiveWorkingWeekendsConstraint.descriptionEmployee"); //$NON-NLS-1$
		desc = desc.replaceAll("\\$1", Integer.toString(minConsecutiveWorkingWeekends)); //$NON-NLS-1$
		return desc;
	}
	
	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLParametersDescriptions(de.uos.inf.ischedule.model.ShiftSchedulingProblem)
	 */
	@Override
	public List<String> getHTMLParametersDescriptions(
			ShiftSchedulingProblem problem) {
		String paramDesc = Messages.getString(
				"MinConsecutiveWorkingWeekendsConstraint.parametersDescription"); //$NON-NLS-1$
		paramDesc = paramDesc.replaceAll("\\$1", scope.getName()); //$NON-NLS-1$
		String imgURL = IconUtils.getImageURL(scope.getIconPath());
		paramDesc = paramDesc.replaceAll("\\$2", imgURL); //$NON-NLS-1$
		paramDesc = paramDesc.replaceAll("\\$3", Integer.toString(minConsecutiveWorkingWeekends)); //$NON-NLS-1$
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
