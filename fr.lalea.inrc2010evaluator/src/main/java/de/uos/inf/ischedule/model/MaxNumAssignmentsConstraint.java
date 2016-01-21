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
 * This constraint defines a maximum number of assignments for an employee
 * on the entire planning horizon.
 * 
 * @author David Meignan
 */
public class MaxNumAssignmentsConstraint implements Constraint {

	/**
	 * Maximum number of assignments for an employee on the entire planning horizon.
	 */
	protected int maxNumAssignment;

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
	private MaxNumAssignmentsConstraintEvaluator evaluator = null;
	
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
	 * Constructs a maximum-number-of-assignments constraint.
	 * 
	 * @param maxNumAssignment the maximum number of assignments.
	 * @param scope the contract on which the constraint applies.
	 * @param active the active property of the constraint.
	 * @param defaultWeightValue the default weight value of the constraint.
	 * 
	 * @throws IllegalArgumentException if the maximum number of assignments,
	 * the rank-index or the default weight value is negative, or the scope
	 * is <code>null</code>.
	 */
	public MaxNumAssignmentsConstraint(int maxNumAssignment, Contract scope,
			boolean active, int defaultWeightValue) {
		if (maxNumAssignment < 0 || defaultWeightValue < 0)
			throw new IllegalArgumentException();
		if (scope == null)
			throw new IllegalArgumentException();
		
		this.maxNumAssignment = maxNumAssignment;
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
			evaluator = new MaxNumAssignmentsConstraintEvaluator(problem);
		return evaluator;
	}

	/**
	 * Evaluator of the constraint.
	 */
	class MaxNumAssignmentsConstraintEvaluator extends
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
		public MaxNumAssignmentsConstraintEvaluator(ShiftSchedulingProblem problem) {
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
				int workingDays = solution.workingDays(employeeIndex);
				if (workingDays > maxNumAssignment)
					excess += workingDays-maxNumAssignment;
			}
			// Return cost by weight
			return excess*weightValue;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getEstimatedAssignmentCost(de.uos.inf.ischedule.model.Solution, int, de.uos.inf.ischedule.model.Shift, int)
		 */
		@Override
		public int getEstimatedAssignmentCost(Solution solution,
				int employeeIndex, Shift shift, int dayIndex) {
			// Check active and weight value
			if (!active || weightValue <= 0)
				return 0;
			
			// Check scope of constraint
			if (!constrainedEmployeeIndexes.contains(employeeIndex))
				return 0;
			
			int workingDays = solution.workingDays(employeeIndex);
			if (workingDays+1 > maxNumAssignment)
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
				// Count initial and swap working days
				int initialWorkingDays = 0;
				int swapWorkingDays = 0;
				for (int dayIndex=0; dayIndex<solution.assignments.size(); dayIndex++) {
					if (dayIndex<swapMove.getStartDayIndex() || 
							dayIndex>swapMove.getEndDayIndex()) {
						if (solution.assignments.get(dayIndex)
								.get(swapMove.getEmployee1Index()) != null) {
							initialWorkingDays++;
							swapWorkingDays++;
						}
					} else {
						if (solution.assignments.get(dayIndex)
								.get(swapMove.getEmployee1Index()) != null) {
							initialWorkingDays++;
						}
						if (solution.assignments.get(dayIndex)
								.get(swapMove.getEmployee2Index()) != null) {
							swapWorkingDays++;
						}
					}
				}
				if (initialWorkingDays > maxNumAssignment)
					initialExcess += initialWorkingDays-maxNumAssignment;
				if (swapWorkingDays > maxNumAssignment)
					swapExcess += swapWorkingDays-maxNumAssignment;
			}
			
			// Employee 2
			if (employee2Constrained) {
				// Count initial and swap working days
				int initialWorkingDays = 0;
				int swapWorkingDays = 0;
				for (int dayIndex=0; dayIndex<solution.assignments.size(); dayIndex++) {
					if (dayIndex<swapMove.getStartDayIndex() || 
							dayIndex>swapMove.getEndDayIndex()) {
						if (solution.assignments.get(dayIndex)
								.get(swapMove.getEmployee2Index()) != null) {
							initialWorkingDays++;
							swapWorkingDays++;
						}
					} else {
						if (solution.assignments.get(dayIndex)
								.get(swapMove.getEmployee2Index()) != null) {
							initialWorkingDays++;
						}
						if (solution.assignments.get(dayIndex)
								.get(swapMove.getEmployee1Index()) != null) {
							swapWorkingDays++;
						}
					}
				}
				if (initialWorkingDays > maxNumAssignment)
					initialExcess += initialWorkingDays-maxNumAssignment;
				if (swapWorkingDays > maxNumAssignment)
					swapExcess += swapWorkingDays-maxNumAssignment;
			}
			
			return (swapExcess-initialExcess)*weightValue;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getConstraint()
		 */
		@Override
		public Constraint getConstraint() {
			return MaxNumAssignmentsConstraint.this;
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
				int workingDays = solution.workingDays(employeeIndex);
				if (workingDays > maxNumAssignment) {
					ConstraintViolation violation = new ConstraintViolation(
							MaxNumAssignmentsConstraint.this);
					violation.setCost(weightValue*(workingDays-maxNumAssignment));
					violation.setMessage(Messages.getString("MaxNumAssignmentsConstraint.maxAssignmentsExceeded")); //$NON-NLS-1$
					violation.addFullEmployeeInScope(
							solution.employees.get(employeeIndex));
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
				// Count initial and swap working days
				int initialWorkingDays = 0;
				int swapWorkingDays = 0;
				for (int dayIndex=0; dayIndex<solution.assignments.size(); dayIndex++) {
					if (dayIndex<swapMove.getStartDayIndex() || 
							dayIndex>swapMove.getEndDayIndex()) {
						if (solution.assignments.get(dayIndex)
								.get(swapMove.getEmployee1Index()) != null) {
							initialWorkingDays++;
							swapWorkingDays++;
						}
					} else {
						if (solution.assignments.get(dayIndex)
								.get(swapMove.getEmployee1Index()) != null) {
							initialWorkingDays++;
						}
						if (solution.assignments.get(dayIndex)
								.get(swapMove.getEmployee2Index()) != null) {
							swapWorkingDays++;
						}
					}
				}
				int initialExcess = 0;
				int swapExcess = 0;
				if (initialWorkingDays > maxNumAssignment)
					initialExcess = initialWorkingDays-maxNumAssignment;
				if (swapWorkingDays > maxNumAssignment)
					swapExcess += swapWorkingDays-maxNumAssignment;
				if (initialExcess > swapExcess) {
					diff[0] += initialExcess-swapExcess;
				} else if (swapExcess > initialExcess) {
					diff[1] += swapExcess-initialExcess;
				}
			}
			
			// Employee 2
			if (employee2Constrained) {
				// Count initial and swap working days
				int initialWorkingDays = 0;
				int swapWorkingDays = 0;
				for (int dayIndex=0; dayIndex<solution.assignments.size(); dayIndex++) {
					if (dayIndex<swapMove.getStartDayIndex() || 
							dayIndex>swapMove.getEndDayIndex()) {
						if (solution.assignments.get(dayIndex)
								.get(swapMove.getEmployee2Index()) != null) {
							initialWorkingDays++;
							swapWorkingDays++;
						}
					} else {
						if (solution.assignments.get(dayIndex)
								.get(swapMove.getEmployee2Index()) != null) {
							initialWorkingDays++;
						}
						if (solution.assignments.get(dayIndex)
								.get(swapMove.getEmployee1Index()) != null) {
							swapWorkingDays++;
						}
					}
				}
				int initialExcess = 0;
				int swapExcess = 0;
				if (initialWorkingDays > maxNumAssignment)
					initialExcess += initialWorkingDays-maxNumAssignment;
				if (swapWorkingDays > maxNumAssignment)
					swapExcess += swapWorkingDays-maxNumAssignment;
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
		return Messages.getString("MaxNumAssignmentsConstraint.name"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getConstraintCostLabel()
	 */
	@Override
	public String getCostLabel() {
		return Messages.getString("MaxNumAssignmentsConstraint.costLabel"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription()
	 */
	@Override
	public String getHTMLDescription() {
		return Messages.getString("MaxNumAssignmentsConstraint.description"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription(de.uos.inf.ischedule.model.Employee)
	 */
	@Override
	public String getHTMLDescription(Employee employee) {
		if (!cover(employee))
			return null;
		String desc = Messages.getString(
				"MaxNumAssignmentsConstraint.descriptionEmployee"); //$NON-NLS-1$
		desc = desc.replaceAll("\\$1", Integer.toString(maxNumAssignment)); //$NON-NLS-1$
		return desc;
	}
	
	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLParametersDescriptions(de.uos.inf.ischedule.model.ShiftSchedulingProblem)
	 */
	@Override
	public List<String> getHTMLParametersDescriptions(
			ShiftSchedulingProblem problem) {
		String paramDesc = Messages.getString(
				"MaxNumAssignmentsConstraint.parametersDescription"); //$NON-NLS-1$
		paramDesc = paramDesc.replaceAll("\\$1", scope.getName()); //$NON-NLS-1$
		String imgURL = IconUtils.getImageURL(scope.getIconPath());
		paramDesc = paramDesc.replaceAll("\\$2", imgURL); //$NON-NLS-1$
		paramDesc = paramDesc.replaceAll("\\$3", Integer.toString(maxNumAssignment)); //$NON-NLS-1$
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
