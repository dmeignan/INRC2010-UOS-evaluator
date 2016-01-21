/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.uos.inf.ischedule.model.heuristic.SwapMove;
import de.uos.inf.ischedule.util.IconUtils;
import de.uos.inf.ischedule.util.Messages;


/**
 * This constraint ensures that an employee is assigned to identical shifts on weekends.
 * 
 * TODO the first weekend may potentially be missing if it is cut in the planning horizon.
 * TODO See ConsecutiveWorkingWeekendsAttribute for a fix.
 * 
 * @author David Meignan
 */
public class IdentShiftsDuringWeekendsConstraint implements Constraint {

	/**
	 * Indicates if the constraint only applies on complete weekends.
	 */
	protected boolean completeWeekends;
	
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
	private IdentShiftsDuringWeekendsConstraintEvaluator evaluator = null;
	
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
	 * Constructs a identical-shift-during-weekends constraint.
	 * 
	 * @param weekendDefinition the definition of the weekend.
	 * @param scope the contract on which the constraint applies.
	 * @param active the active property of the constraint.
	 * @param defaultWeightValue the default weight value of the constraint.
	 * @param completeWeekends if <code>true</code> the constraint applies only on
	 * complete working weekends.
	 * 
	 * @throws IllegalArgumentException if the rank-index or the default weight
	 * value is negative, or the scope or weekend definition is <code>null</code>.
	 */
	public IdentShiftsDuringWeekendsConstraint(
			Contract scope,
			boolean active, int defaultWeightValue,
			boolean completeWeekends) {
		if (defaultWeightValue < 0)
			throw new IllegalArgumentException();
		if (scope == null)
			throw new IllegalArgumentException();
		
		this.scope = scope;
		this.active = active;
		this.weightValue = defaultWeightValue;
		this.completeWeekends = completeWeekends;
	}


	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getEvaluator(de.uos.inf.ischedule.model.ShiftSchedulingProblem)
	 */
	@Override
	public ConstraintEvaluator getEvaluator(ShiftSchedulingProblem problem) {
		if (evaluator == null)
			evaluator = new IdentShiftsDuringWeekendsConstraintEvaluator(problem);
		return evaluator;
	}
	
	/**
	 * Evaluator of the constraint.
	 */
	class IdentShiftsDuringWeekendsConstraintEvaluator extends
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
		public IdentShiftsDuringWeekendsConstraintEvaluator(ShiftSchedulingProblem problem) {
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
			
			int differences = 0;
			// Iterates on employees
			for (int employeeIndex: constrainedEmployeeIndexes) {
				// Iterates on weekend start
				for (int i=0; i<weekendStartIndexes.size(); i++) {
					int startIndex = weekendStartIndexes.get(i);
					int endIndex = weekendEndIndexes.get(i);
					boolean[] matched = new boolean[(endIndex-startIndex+1)];
					Arrays.fill(matched, false);
					int weekendDiff = 0;
					for (int dayIndex=startIndex; dayIndex<=endIndex; dayIndex++) {
						if (!matched[dayIndex-startIndex]) {
							Shift shift = solution.assignments
									.get(dayIndex).get(employeeIndex);
							if (shift != null) {
								for (int dayIndex2=startIndex; dayIndex2<=endIndex; dayIndex2++) {
									Shift shift2 = solution.assignments
											.get(dayIndex2).get(employeeIndex);
									if (shift == shift2) {
										matched[dayIndex2-startIndex] = true;
									} else {
										weekendDiff++;
									}
								}
							} else if (completeWeekends) {
								// Free weekend or partially worked weekend
								weekendDiff = 0;
								break;
							}
						}
					}
					differences += weekendDiff;
				}
			}
			// Return cost by weight
			return differences*weightValue;
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
			
			// Returns the weight value if another shift is already assigned
			// in the weekend.
			
			// Check employee
			if (constrainedEmployeeIndexes.contains(employeeIndex)) {
				// Check if day is on weekend
				for (int i=0; i<weekendStartIndexes.size(); i++) {
					int startIndex = weekendStartIndexes.get(i);
					int endIndex = weekendEndIndexes.get(i);
					if (assignmentDayIndex >= startIndex && assignmentDayIndex <= endIndex) {
						// In the weekend
						int nbDiff = 0;
						for (int dayIndex=startIndex; dayIndex<=endIndex; dayIndex++) {
							Shift existingAssignment = 
									solution.assignments.get(dayIndex).get(employeeIndex);
							if ( (existingAssignment != null) &&
									(existingAssignment != shift) )
								nbDiff++;
						}
						return nbDiff*weightValue;
					}
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
					// Swap cover weekend
					boolean[] matched = new boolean[(endIndex-startIndex+1)];
					// Employee 1
					if (employee1Constrained) {
						// Initial partial penalty
						Arrays.fill(matched, false);
						int weekendPenalty = 0;
						for (int dayIndex=startIndex; dayIndex<=endIndex; dayIndex++) {
							if (!matched[dayIndex-startIndex]) {
								Shift shift = solution.assignments
										.get(dayIndex).get(swapMove.getEmployee1Index());
								if (shift != null) {
									for (int dayIndex2=startIndex; dayIndex2<=endIndex; dayIndex2++) {
										Shift shift2 = solution.assignments
												.get(dayIndex2).get(swapMove.getEmployee1Index());
										if (shift == shift2) {
											matched[dayIndex2-startIndex] = true;
										} else {
											weekendPenalty++;
										}
									}
								} else if (completeWeekends) {
									weekendPenalty = 0;
									break;
								}
							}
						}
						initialPartialPenalty += weekendPenalty;
						// Swap partial penalty
						Arrays.fill(matched, false);
						weekendPenalty = 0;
						for (int dayIndex=startIndex; dayIndex<=endIndex; dayIndex++) {
							if (!matched[dayIndex-startIndex]) {
								Shift shift = swapMove.getResultingAssignment(
										solution, dayIndex, swapMove.getEmployee1Index());
								if (shift != null) {
									for (int dayIndex2=startIndex; dayIndex2<=endIndex; dayIndex2++) {
										Shift shift2 = swapMove.getResultingAssignment(
												solution, dayIndex2, swapMove.getEmployee1Index());
										if (shift == shift2) {
											matched[dayIndex2-startIndex] = true;
										} else {
											weekendPenalty++;
										}
									}
								} else if (completeWeekends) {
									weekendPenalty = 0;
									break;
								}
							}
						}
						swapPartialPenalty += weekendPenalty;
					}
					// Employee 2
					if (employee2Constrained) {
						// Initial partial penalty
						Arrays.fill(matched, false);
						int weekendPenalty = 0;
						for (int dayIndex=startIndex; dayIndex<=endIndex; dayIndex++) {
							if (!matched[dayIndex-startIndex]) {
								Shift shift = solution.assignments
										.get(dayIndex).get(swapMove.getEmployee2Index());
								if (shift != null) {
									for (int dayIndex2=startIndex; dayIndex2<=endIndex; dayIndex2++) {
										Shift shift2 = solution.assignments
												.get(dayIndex2).get(swapMove.getEmployee2Index());
										if (shift == shift2) {
											matched[dayIndex2-startIndex] = true;
										} else {
											weekendPenalty++;
										}
									}
								} else if (completeWeekends) {
									weekendPenalty = 0;
									break;
								}
							}
						}
						initialPartialPenalty += weekendPenalty;
						// Swap partial penalty
						Arrays.fill(matched, false);
						weekendPenalty = 0;
						for (int dayIndex=startIndex; dayIndex<=endIndex; dayIndex++) {
							if (!matched[dayIndex-startIndex]) {
								Shift shift = swapMove.getResultingAssignment(
										solution, dayIndex, swapMove.getEmployee2Index());
								if (shift != null) {
									for (int dayIndex2=startIndex; dayIndex2<=endIndex; dayIndex2++) {
										Shift shift2 = swapMove.getResultingAssignment(
												solution, dayIndex2, swapMove.getEmployee2Index());
										if (shift == shift2) {
											matched[dayIndex2-startIndex] = true;
										} else {
											weekendPenalty++;
										}
									}
								} else if (completeWeekends) {
									weekendPenalty = 0;
									break;
								}
							}
						}
						swapPartialPenalty += weekendPenalty;
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
			return IdentShiftsDuringWeekendsConstraint.this;
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
				for (int i=0; i<weekendStartIndexes.size(); i++) {
					int differences = 0;
					int startIndex = weekendStartIndexes.get(i);
					int endIndex = weekendEndIndexes.get(i);
					boolean[] matched = new boolean[(endIndex-startIndex+1)];
					Arrays.fill(matched, false);
					for (int dayIndex=startIndex; dayIndex<=endIndex; dayIndex++) {
						if (!matched[dayIndex-startIndex]) {
							Shift shift = solution.assignments
									.get(dayIndex).get(employeeIndex);
							if (shift != null) {
								for (int dayIndex2=startIndex; dayIndex2<=endIndex; dayIndex2++) {
									Shift shift2 = solution.assignments
											.get(dayIndex2).get(employeeIndex);
									if (shift == shift2) {
										matched[dayIndex2-startIndex] = true;
									} else {
										differences++;
									}
								}
							} else if (completeWeekends) {
								// Free weekend or partially worked weekend
								differences = 0;
								break;
							}
						}
					}
					if (differences > 0) {
						ConstraintViolation violation = new ConstraintViolation(
								IdentShiftsDuringWeekendsConstraint.this);
						violation.setCost(weightValue*differences);
						violation.setMessage(Messages.getString("IdentShiftsDuringWeekendsConstraint.differentShiftsWeekend")); //$NON-NLS-1$
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
					// Swap cover weekend
					boolean[] matched = new boolean[(endIndex-startIndex+1)];
					// Employee 1
					if (employee1Constrained) {
						// Initial partial penalty
						Arrays.fill(matched, false);
						int weekendPenalty = 0;
						for (int dayIndex=startIndex; dayIndex<=endIndex; dayIndex++) {
							if (!matched[dayIndex-startIndex]) {
								Shift shift = solution.assignments
										.get(dayIndex).get(swapMove.getEmployee1Index());
								if (shift != null) {
									for (int dayIndex2=startIndex; dayIndex2<=endIndex; dayIndex2++) {
										Shift shift2 = solution.assignments
												.get(dayIndex2).get(swapMove.getEmployee1Index());
										if (shift == shift2) {
											matched[dayIndex2-startIndex] = true;
										} else {
											weekendPenalty++;
										}
									}
								} else if (completeWeekends) {
									weekendPenalty = 0;
									break;
								}
							}
						}
						initialPartialPenalty = weekendPenalty;
						// Swap partial penalty
						Arrays.fill(matched, false);
						weekendPenalty = 0;
						for (int dayIndex=startIndex; dayIndex<=endIndex; dayIndex++) {
							if (!matched[dayIndex-startIndex]) {
								Shift shift = swapMove.getResultingAssignment(
										solution, dayIndex, swapMove.getEmployee1Index());
								if (shift != null) {
									for (int dayIndex2=startIndex; dayIndex2<=endIndex; dayIndex2++) {
										Shift shift2 = swapMove.getResultingAssignment(
												solution, dayIndex2, swapMove.getEmployee1Index());
										if (shift == shift2) {
											matched[dayIndex2-startIndex] = true;
										} else {
											weekendPenalty++;
										}
									}
								} else if (completeWeekends) {
									weekendPenalty = 0;
									break;
								}
							}
						}
						swapPartialPenalty = weekendPenalty;
						
						if (initialPartialPenalty > swapPartialPenalty) {
							diff[0] += initialPartialPenalty-swapPartialPenalty;
						} else if (swapPartialPenalty > initialPartialPenalty) {
							diff[1] += swapPartialPenalty-initialPartialPenalty;
						}
					}
					// Employee 2
					if (employee2Constrained) {
						// Initial partial penalty
						Arrays.fill(matched, false);
						int weekendPenalty = 0;
						for (int dayIndex=startIndex; dayIndex<=endIndex; dayIndex++) {
							if (!matched[dayIndex-startIndex]) {
								Shift shift = solution.assignments
										.get(dayIndex).get(swapMove.getEmployee2Index());
								if (shift != null) {
									for (int dayIndex2=startIndex; dayIndex2<=endIndex; dayIndex2++) {
										Shift shift2 = solution.assignments
												.get(dayIndex2).get(swapMove.getEmployee2Index());
										if (shift == shift2) {
											matched[dayIndex2-startIndex] = true;
										} else {
											weekendPenalty++;
										}
									}
								} else if (completeWeekends) {
									weekendPenalty = 0;
									break;
								}
							}
						}
						initialPartialPenalty = weekendPenalty;
						// Swap partial penalty
						Arrays.fill(matched, false);
						weekendPenalty = 0;
						for (int dayIndex=startIndex; dayIndex<=endIndex; dayIndex++) {
							if (!matched[dayIndex-startIndex]) {
								Shift shift = swapMove.getResultingAssignment(
										solution, dayIndex, swapMove.getEmployee2Index());
								if (shift != null) {
									for (int dayIndex2=startIndex; dayIndex2<=endIndex; dayIndex2++) {
										Shift shift2 = swapMove.getResultingAssignment(
												solution, dayIndex2, swapMove.getEmployee2Index());
										if (shift == shift2) {
											matched[dayIndex2-startIndex] = true;
										} else {
											weekendPenalty++;
										}
									}
								} else if (completeWeekends) {
									weekendPenalty = 0;
									break;
								}
							}
						}
						swapPartialPenalty = weekendPenalty;
						
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
		return Messages.getString("IdentShiftsDuringWeekendsConstraint.name"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getConstraintCostLabel()
	 */
	@Override
	public String getCostLabel() {
		return Messages.getString("IdentShiftsDuringWeekendsConstraint.costLabel"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription()
	 */
	@Override
	public String getHTMLDescription() {
		return Messages.getString("IdentShiftsDuringWeekendsConstraint.description"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription(de.uos.inf.ischedule.model.Employee)
	 */
	@Override
	public String getHTMLDescription(Employee employee) {
		if (!cover(employee))
			return null;
		return Messages.getString(
				"IdentShiftsDuringWeekendsConstraint.descriptionEmployee"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLParametersDescriptions(de.uos.inf.ischedule.model.ShiftSchedulingProblem)
	 */
	@Override
	public List<String> getHTMLParametersDescriptions(
			ShiftSchedulingProblem problem) {
		String paramDesc = Messages.getString(
				"IdentShiftsDuringWeekendsConstraint.parametersDescription"); //$NON-NLS-1$
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
