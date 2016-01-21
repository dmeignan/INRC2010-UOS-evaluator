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
 * This constraint ensures that an employee works complete weekends.
 * 
 * TODO the first weekend may potentially be missing if it is cut in the planning horizon.
 * TODO See ConsecutiveWorkingWeekendsAttribute for a fix.
 * 
 * @author David Meignan
 */
public class CompleteWeekendsConstraint implements Constraint {

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
	private CompleteWeekendsConstraintEvaluator evaluator = null;
	
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
	 * Constructs a complete-working-weekends constraint.
	 * 
	 * @param weekendDefinition the definition of the weekend.
	 * @param scope the contract on which the constraint applies.
	 * @param active the active property of the constraint.
	 * @param defaultWeightValue the default weight value of the constraint.
	 * 
	 * @throws IllegalArgumentException if the rank-index or the default weight 
	 * value is negative, or the scope or weekend definition is <code>null</code>.
	 */
	public CompleteWeekendsConstraint(
			Contract scope,
			boolean active, int defaultWeightValue) {
		if (defaultWeightValue < 0)
			throw new IllegalArgumentException();
		if (scope == null)
			throw new IllegalArgumentException();
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
			evaluator = new CompleteWeekendsConstraintEvaluator(problem);
		return evaluator;
	}
	/**
	 * Evaluator of the constraint.
	 */
	class CompleteWeekendsConstraintEvaluator extends
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
		public CompleteWeekendsConstraintEvaluator(ShiftSchedulingProblem problem) {
			// Weekend start day indexes
			// Note that weekends of 1-day length (cut at scheduling period) are not 
			// taken into account.
			weekendStartIndexes = new ArrayList<Integer>();
			weekendEndIndexes = new ArrayList<Integer>();
			for (int dayIndex=0; dayIndex<problem.schedulingPeriod.days.size();
					dayIndex++) {
				if (problem.schedulingPeriod.getDayOfWeek(dayIndex) == 
						scope.weekendType.getStartDayOfWeek()) {
					int weekendEndIndex = dayIndex
							+ scope.weekendType.getDuration()-1;
					// Adjust if weekend cut
					if (weekendEndIndex >= problem.schedulingPeriod.size())
						weekendEndIndex = problem.schedulingPeriod.size()-1;
					// Add weekend if length greater than 1
					if (dayIndex != weekendEndIndex) {
						weekendStartIndexes.add(dayIndex);
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
			
			int penalty = 0;
			// Iterates on employees
			for (int employeeIndex: constrainedEmployeeIndexes) {
				// Iterates on weekend
				for (int i=0; i<weekendStartIndexes.size(); i++) {
					int startIndex = weekendStartIndexes.get(i);
					int endIndex = weekendEndIndexes.get(i);
					boolean[] weekendWorkPattern = new boolean[endIndex-startIndex+1];
					// Iterates on days of the weekend
					for (int dayIndex=startIndex; dayIndex<=endIndex;
							dayIndex++) {
						if (solution.assignments.get(dayIndex).get(employeeIndex)
								!= null) {
							weekendWorkPattern[dayIndex-startIndex] = true;
						} else {
							weekendWorkPattern[dayIndex-startIndex] = false;
						}
					}
					penalty += inrcWeekendPenalty(weekendWorkPattern);
				}
			}
			// Return cost by weight
			return penalty*weightValue;
		}

		/**
		 * Returns the penalty (cost without weight) of the weekend according to the INRC
		 * specification of incomplete weekend constraint.
		 * 
		 * @param weekendWorkPattern the weekend working pattern.
		 * @return the cost of the weekend.
		 */
		private int inrcWeekendPenalty(boolean[] weekendWorkPattern) {
			if (weekendWorkPattern.length == 2) {
				if (weekendWorkPattern[1] && weekendWorkPattern[0]) {
					// W - W
					return 0;
				} else if (weekendWorkPattern[1] && !weekendWorkPattern[0]) {
					// W - F
					return 1;
				} else if (!weekendWorkPattern[1] && weekendWorkPattern[0]) {
					// F - W
					return 1;
				} else if (!weekendWorkPattern[1] && !weekendWorkPattern[0]) {
					// F - F
					return 0;
				}
			} else if (weekendWorkPattern.length == 3) {
				if (weekendWorkPattern[2] && weekendWorkPattern[1] && weekendWorkPattern[0]) {
					// W - W - W
					return 0;
				} else if (weekendWorkPattern[2] && weekendWorkPattern[1] && !weekendWorkPattern[0]) {
					// W - W - F
					return 1;
				} else if (weekendWorkPattern[2] && !weekendWorkPattern[1] && weekendWorkPattern[0]) {
					// W - F - W
					return 4;
				} else if (weekendWorkPattern[2] && !weekendWorkPattern[1] && !weekendWorkPattern[0]) {
					// W - F - F
					return 2;
				} else if (!weekendWorkPattern[2] && weekendWorkPattern[1] && weekendWorkPattern[0]) {
					// F - W - W
					return 1;
				} else if (!weekendWorkPattern[2] && weekendWorkPattern[1] && !weekendWorkPattern[0]) {
					// F - W - F
					return 2;
				} else if (!weekendWorkPattern[2] && !weekendWorkPattern[1] && weekendWorkPattern[0]) {
					// F - F - W
					return 2;
				} else if (!weekendWorkPattern[2] && !weekendWorkPattern[1] && !weekendWorkPattern[0]) {
					// F - F - F
					return 0;
				}
				
			} else if (weekendWorkPattern.length == 4) {
				if (weekendWorkPattern[3] && weekendWorkPattern[2] && weekendWorkPattern[1] && weekendWorkPattern[0]) {
					// W - W - W - W
					return 0;
				} else if (weekendWorkPattern[3] && weekendWorkPattern[2] && weekendWorkPattern[1] && weekendWorkPattern[0]) {
					// W - W - W - F
					return 1;
				} else if (weekendWorkPattern[3] && weekendWorkPattern[2] && weekendWorkPattern[1] && weekendWorkPattern[0]) {
					// W - W - F - W
					return 5;
				} else if (weekendWorkPattern[3] && weekendWorkPattern[2] && weekendWorkPattern[1] && weekendWorkPattern[0]) {
					// W - W - F - F
					return 2;
				} else if (weekendWorkPattern[3] && weekendWorkPattern[2] && weekendWorkPattern[1] && weekendWorkPattern[0]) {
					// W - F - W - W
					return 5;
				} else if (weekendWorkPattern[3] && weekendWorkPattern[2] && weekendWorkPattern[1] && weekendWorkPattern[0]) {
					// W - F - W - F
					return 6;
				} else if (weekendWorkPattern[3] && weekendWorkPattern[2] && weekendWorkPattern[1] && weekendWorkPattern[0]) {
					// W - F - F - W
					return 6;
				} else if (weekendWorkPattern[3] && weekendWorkPattern[2] && weekendWorkPattern[1] && weekendWorkPattern[0]) {
					// W - F - F - F
					return 3;
				} else if (weekendWorkPattern[3] && weekendWorkPattern[2] && weekendWorkPattern[1] && weekendWorkPattern[0]) {
					// F - W - W - W
					return 1;
				} else if (weekendWorkPattern[3] && weekendWorkPattern[2] && weekendWorkPattern[1] && weekendWorkPattern[0]) {
					// F - W - W - F
					return 2;
				} else if (weekendWorkPattern[3] && weekendWorkPattern[2] && weekendWorkPattern[1] && weekendWorkPattern[0]) {
					// F - W - F - W
					return 6;
				} else if (weekendWorkPattern[3] && weekendWorkPattern[2] && weekendWorkPattern[1] && weekendWorkPattern[0]) {
					// F - W - F - F
					return 3;
				} else if (weekendWorkPattern[3] && weekendWorkPattern[2] && weekendWorkPattern[1] && weekendWorkPattern[0]) {
					// F - F - W - W
					return 2;
				} else if (weekendWorkPattern[3] && weekendWorkPattern[2] && weekendWorkPattern[1] && weekendWorkPattern[0]) {
					// F - F - W - F
					return 3;
				} else if (weekendWorkPattern[3] && weekendWorkPattern[2] && weekendWorkPattern[1] && weekendWorkPattern[0]) {
					// F - F - F - W
					return 3;
				} else if (weekendWorkPattern[3] && weekendWorkPattern[2] && weekendWorkPattern[1] && weekendWorkPattern[0]) {
					// F - F - F - F
					return 0;
				}
			}
			throw new IllegalArgumentException();
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
			
			// Return a negative cost if a weekend is completed
			
			// Check employee
			if (constrainedEmployeeIndexes.contains(employeeIndex)) {
				// Check if day index in a weekend
				for (int i=0; i<weekendStartIndexes.size(); i++) {
					int startIndex = weekendStartIndexes.get(i);
					int endIndex = weekendEndIndexes.get(i);
					if (assignmentDayIndex >= startIndex && assignmentDayIndex <= endIndex) {
						// In the weekend
						int nbWorkingDays = 0;
						for (int dayIndex=startIndex; dayIndex<=endIndex; dayIndex++) {
							if (solution.assignments.get(dayIndex).get(employeeIndex)
									!= null)
								nbWorkingDays++;
						}
						int weekendEffectiveDuration = endIndex-startIndex+1;
						if (nbWorkingDays == weekendEffectiveDuration-1) {
							return -1*weightValue;
						}
						return 0;
					}
					// Break because it is not necessary to check other weekends
					if (assignmentDayIndex < endIndex)
						return 0;
				}
			}
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
			
			// Compute previous and new penalties for weekends covered by swap 
			int initialPartialPenalty = 0;
			int swapPartialPenalty = 0;
			// Iterates on weekends
			for (int i=0; i<weekendStartIndexes.size(); i++) {
				int startIndex = weekendStartIndexes.get(i);
				int endIndex = weekendEndIndexes.get(i);
				if (
						(swapMove.getStartDayIndex() <= endIndex &&
						swapMove.getEndDayIndex() >= startIndex) ) {
					// Swap covers the weekend
					boolean[] initialWeekendWorkPattern = 
							new boolean[endIndex-startIndex+1];
					boolean[] swapWeekendWorkPattern = 
							new boolean[endIndex-startIndex+1];
					// For employee 1
					if (employee1Constrained) {
						// Iterates on days of the weekend
						for (int dayIndex=startIndex; dayIndex<=endIndex;
								dayIndex++) {
							if (solution.assignments.get(dayIndex).get(
									swapMove.getEmployee1Index()) != null) {
								initialWeekendWorkPattern[dayIndex-startIndex] = true;
							} else {
								initialWeekendWorkPattern[dayIndex-startIndex] = false;
							}
							if (swapMove.getResultingAssignment(
									solution, dayIndex, swapMove.getEmployee1Index()) != null) {
								swapWeekendWorkPattern[dayIndex-startIndex] = true;
							} else {
								swapWeekendWorkPattern[dayIndex-startIndex] = false;
							}
						}
						initialPartialPenalty += 
								inrcWeekendPenalty(initialWeekendWorkPattern);
						swapPartialPenalty += 
								inrcWeekendPenalty(swapWeekendWorkPattern);
						
					}
					// For employee 2
					if (employee2Constrained) {
						// Iterates on days of the weekend
						for (int dayIndex=startIndex; dayIndex<=endIndex;
								dayIndex++) {
							if (solution.assignments.get(dayIndex).get(
									swapMove.getEmployee2Index()) != null) {
								initialWeekendWorkPattern[dayIndex-startIndex] = true;
							} else {
								initialWeekendWorkPattern[dayIndex-startIndex] = false;
							}
							if (swapMove.getResultingAssignment(
									solution, dayIndex, swapMove.getEmployee2Index()) != null) {
								swapWeekendWorkPattern[dayIndex-startIndex] = true;
							} else {
								swapWeekendWorkPattern[dayIndex-startIndex] = false;
							}
						}
						initialPartialPenalty += 
								inrcWeekendPenalty(initialWeekendWorkPattern);
						swapPartialPenalty += 
								inrcWeekendPenalty(swapWeekendWorkPattern);
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
			return CompleteWeekendsConstraint.this;
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
				// Iterates on weekend
				for (int i=0; i<weekendStartIndexes.size(); i++) {
					int startIndex = weekendStartIndexes.get(i);
					int endIndex = weekendEndIndexes.get(i);
					boolean[] weekendWorkPattern = new boolean[endIndex-startIndex+1];
					// Iterates on days of the weekend
					for (int dayIndex=startIndex; dayIndex<=endIndex;
							dayIndex++) {
						if (solution.assignments.get(dayIndex).get(employeeIndex)
								!= null) {
							weekendWorkPattern[dayIndex-startIndex] = true;
						} else {
							weekendWorkPattern[dayIndex-startIndex] = false;
						}
					}
					int penalty = inrcWeekendPenalty(weekendWorkPattern);
					if (penalty > 0) {
						ConstraintViolation violation = new ConstraintViolation(
								CompleteWeekendsConstraint.this);
						violation.setCost(weightValue*penalty);
						violation.setMessage(Messages.getString("CompleteWeekendsConstraint.incompleteWeekend")); //$NON-NLS-1$
						violation.addAssignmentRangeInScope(
								solution.employees.get(employeeIndex),
								solution.problem.getSchedulingPeriod().getDate(startIndex),
								solution.problem.getSchedulingPeriod().getDate(endIndex));
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
			
			// Check if move modifies work patterns
			if (!swapMove.modifyWorkingPattern(solution))
				return diff;
			
			// Compute previous and new penalties for weekends covered by swap 
			int initialPartialPenalty;
			int swapPartialPenalty;
			// Iterates on weekends
			for (int i=0; i<weekendStartIndexes.size(); i++) {
				int startIndex = weekendStartIndexes.get(i);
				int endIndex = weekendEndIndexes.get(i);
				if (
						(swapMove.getStartDayIndex() <= endIndex &&
						swapMove.getEndDayIndex() >= startIndex) ) {
					// Swap covers the weekend
					boolean[] initialWeekendWorkPattern = 
							new boolean[endIndex-startIndex+1];
					boolean[] swapWeekendWorkPattern = 
							new boolean[endIndex-startIndex+1];
					// For employee 1
					if (employee1Constrained) {
						// Iterates on days of the weekend
						for (int dayIndex=startIndex; dayIndex<=endIndex;
								dayIndex++) {
							if (solution.assignments.get(dayIndex).get(
									swapMove.getEmployee1Index()) != null) {
								initialWeekendWorkPattern[dayIndex-startIndex] = true;
							} else {
								initialWeekendWorkPattern[dayIndex-startIndex] = false;
							}
							if (swapMove.getResultingAssignment(
									solution, dayIndex, swapMove.getEmployee1Index()) != null) {
								swapWeekendWorkPattern[dayIndex-startIndex] = true;
							} else {
								swapWeekendWorkPattern[dayIndex-startIndex] = false;
							}
						}
						initialPartialPenalty = 
								inrcWeekendPenalty(initialWeekendWorkPattern);
						swapPartialPenalty = 
								inrcWeekendPenalty(swapWeekendWorkPattern);
						if (initialPartialPenalty > swapPartialPenalty) {
							diff[0] += initialPartialPenalty-swapPartialPenalty;
						} else if (swapPartialPenalty > initialPartialPenalty) {
							diff[1] += swapPartialPenalty-initialPartialPenalty;
						}
						
					}
					// For employee 2
					if (employee2Constrained) {
						// Iterates on days of the weekend
						for (int dayIndex=startIndex; dayIndex<=endIndex;
								dayIndex++) {
							if (solution.assignments.get(dayIndex).get(
									swapMove.getEmployee2Index()) != null) {
								initialWeekendWorkPattern[dayIndex-startIndex] = true;
							} else {
								initialWeekendWorkPattern[dayIndex-startIndex] = false;
							}
							if (swapMove.getResultingAssignment(
									solution, dayIndex, swapMove.getEmployee2Index()) != null) {
								swapWeekendWorkPattern[dayIndex-startIndex] = true;
							} else {
								swapWeekendWorkPattern[dayIndex-startIndex] = false;
							}
						}
						initialPartialPenalty = 
								inrcWeekendPenalty(initialWeekendWorkPattern);
						swapPartialPenalty = 
								inrcWeekendPenalty(swapWeekendWorkPattern);
						if (initialPartialPenalty > swapPartialPenalty) {
							diff[0] += initialPartialPenalty-swapPartialPenalty;
						} else if (swapPartialPenalty > initialPartialPenalty) {
							diff[1] += swapPartialPenalty-initialPartialPenalty;
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
		return Messages.getString("CompleteWeekendsConstraint.name"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getConstraintCostLabel()
	 */
	@Override
	public String getCostLabel() {
		return Messages.getString("CompleteWeekendsConstraint.costLabel"); //$NON-NLS-1$
	}


	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription()
	 */
	@Override
	public String getHTMLDescription() {
		return Messages.getString("CompleteWeekendsConstraint.description"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription(de.uos.inf.ischedule.model.Employee)
	 */
	@Override
	public String getHTMLDescription(Employee employee) {
		if (!cover(employee))
			return null;
		String desc = Messages.getString(
				"CompleteWeekendsConstraint.descriptionEmployee"); //$NON-NLS-1$
		return desc;
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLParametersDescriptions(de.uos.inf.ischedule.model.ShiftSchedulingProblem)
	 */
	@Override
	public List<String> getHTMLParametersDescriptions(
			ShiftSchedulingProblem problem) {
		String paramDesc = Messages.getString(
				"CompleteWeekendsConstraint.parametersDescription"); //$NON-NLS-1$
		paramDesc = paramDesc.replaceAll("\\$1", scope.getName()); //$NON-NLS-1$
		String imgURL = IconUtils.getImageURL(scope.getIconPath());
		paramDesc = paramDesc.replaceAll("\\$2", imgURL); //$NON-NLS-1$
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
