/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

import java.util.ArrayList;
import java.util.List;

import de.uos.inf.ischedule.model.heuristic.SwapMove;
import de.uos.inf.ischedule.util.Messages;


/**
 * This constraint ensures that shifts are assigned to employees with required skills.
 * 
 * @author David Meignan
 */
public class SkillCoverageConstraint implements Constraint {

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
	private SkillCoverageConstraintEvaluator evaluator = null;
	
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
	 * Constructs a skill-coverage constraint.
	 * 
	 * @param scope the contract on which the constraint applies.
	 * @param active the active property of the constraint.
	 * @param rankIndex the rank-index of the constraints.
	 * @param defaultWeightValue the default weight value of the constraint.
	 * 
	 * @throws IllegalArgumentException if the rank-index or the default weight 
	 * value is negative, or the scope is <code>null</code>.
	 */
	public SkillCoverageConstraint(
			Contract scope,	boolean active, int defaultWeightValue) {
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
			evaluator = new SkillCoverageConstraintEvaluator(problem);
		return evaluator;
	}

	/**
	 * Evaluator of the constraint.
	 */
	class SkillCoverageConstraintEvaluator extends
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
		public SkillCoverageConstraintEvaluator(ShiftSchedulingProblem problem) {
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
			
			int missingSkills = 0;
			// Iterates on employees
			for (int employeeIndex: constrainedEmployeeIndexes) {
				// Iterate on days
				for (int dayIndex=0; dayIndex<solution.assignments.size(); dayIndex++) {
					Shift assignment = solution.assignments.get(dayIndex)
							.get(employeeIndex);
					if (assignment != null) {
						missingSkills += assignment.missingSkills(
								solution.employees.get(employeeIndex));
					}
				}
			}
			// Return cost by weight
			return missingSkills*weightValue;
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
			// Check scope
			if (!constrainedEmployeeIndexes.contains(employeeIndex))
				return 0;
			// Verify skills
			return shift.missingSkills(
					solution.employees.get(employeeIndex))*weightValue;
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
			
			// Compute initial and swap missing skills
			int initialMissingSkills = 0;
			int swapMissingSkills = 0;
			
			// Employee 1
			if (employee1Constrained) {
				// Initial
				for (int dayIndex=swapMove.getStartDayIndex(); 
						dayIndex<swapMove.getStartDayIndex()+swapMove.getBlockSize();
						dayIndex++) {
					Shift assignment = solution.assignments.get(dayIndex)
							.get(swapMove.getEmployee1Index());
					if (assignment != null) {
						initialMissingSkills += assignment.missingSkills(
								solution.employees.get(swapMove.getEmployee1Index()));
					}
				}
				// Swap
				for (int dayIndex=swapMove.getStartDayIndex(); 
						dayIndex<swapMove.getStartDayIndex()+swapMove.getBlockSize();
						dayIndex++) {
					Shift assignment = solution.assignments.get(dayIndex)
							.get(swapMove.getEmployee2Index());
					if (assignment != null) {
						swapMissingSkills += assignment.missingSkills(
								solution.employees.get(swapMove.getEmployee1Index()));
					}
				}
			}
			
			// Employee 2
			if (employee2Constrained) {
				// Initial
				for (int dayIndex=swapMove.getStartDayIndex(); 
						dayIndex<swapMove.getStartDayIndex()+swapMove.getBlockSize();
						dayIndex++) {
					Shift assignment = solution.assignments.get(dayIndex)
							.get(swapMove.getEmployee2Index());
					if (assignment != null) {
						initialMissingSkills += assignment.missingSkills(
								solution.employees.get(swapMove.getEmployee2Index()));
					}
				}
				// Swap
				for (int dayIndex=swapMove.getStartDayIndex(); 
						dayIndex<swapMove.getStartDayIndex()+swapMove.getBlockSize();
						dayIndex++) {
					Shift assignment = solution.assignments.get(dayIndex)
							.get(swapMove.getEmployee1Index());
					if (assignment != null) {
						swapMissingSkills += assignment.missingSkills(
								solution.employees.get(swapMove.getEmployee2Index()));
					}
				}
			}
			
			return (swapMissingSkills-initialMissingSkills)*weightValue;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getConstraint()
		 */
		@Override
		public Constraint getConstraint() {
			return SkillCoverageConstraint.this;
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
				for (int dayIndex=0; dayIndex<solution.assignments.size(); dayIndex++) {
					Shift assignment = solution.assignments.get(dayIndex)
							.get(employeeIndex);
					if (assignment != null) {
						int missingSkills = assignment.missingSkills(
								solution.employees.get(employeeIndex));
						if (missingSkills > 0) {
							ConstraintViolation violation = new ConstraintViolation(
									SkillCoverageConstraint.this);
							violation.setCost(weightValue*missingSkills);
							violation.setMessage(Messages.getString("SkillCoverageConstraint.insufficientSkills")); //$NON-NLS-1$
							violation.addAssignmentInScope(
									solution.employees.get(employeeIndex), 
									solution.problem.getSchedulingPeriod().getDate(dayIndex));
							violations.add(violation);
						}
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
			
			// Employee 1
			if (employee1Constrained) {
				for (int dayIndex=swapMove.getStartDayIndex(); 
						dayIndex<swapMove.getStartDayIndex()+swapMove.getBlockSize();
						dayIndex++) {
					int initialMissingSkills = 0;
					int swapMissingSkills = 0;
					Shift assignment = solution.assignments.get(dayIndex)
							.get(swapMove.getEmployee1Index());
					if (assignment != null) {
						initialMissingSkills = assignment.missingSkills(
								solution.employees.get(swapMove.getEmployee1Index()));
					}
					assignment = solution.assignments.get(dayIndex)
							.get(swapMove.getEmployee2Index());
					if (assignment != null) {
						swapMissingSkills = assignment.missingSkills(
								solution.employees.get(swapMove.getEmployee1Index()));
					}
					if (initialMissingSkills > 0 && swapMissingSkills == 0) {
						diff[0]++;
					} else if (initialMissingSkills == 0 && swapMissingSkills > 0) {
						diff[1]++;
					}
				}
			}
			
			// Employee 2
			if (employee2Constrained) {
				for (int dayIndex=swapMove.getStartDayIndex(); 
						dayIndex<swapMove.getStartDayIndex()+swapMove.getBlockSize();
						dayIndex++) {
					int initialMissingSkills = 0;
					int swapMissingSkills = 0;
					Shift assignment = solution.assignments.get(dayIndex)
							.get(swapMove.getEmployee2Index());
					if (assignment != null) {
						initialMissingSkills = assignment.missingSkills(
								solution.employees.get(swapMove.getEmployee2Index()));
					}
					assignment = solution.assignments.get(dayIndex)
							.get(swapMove.getEmployee1Index());
					if (assignment != null) {
						swapMissingSkills = assignment.missingSkills(
								solution.employees.get(swapMove.getEmployee2Index()));
					}
					if (initialMissingSkills > 0 && swapMissingSkills == 0) {
						diff[0]++;
					} else if (initialMissingSkills == 0 && swapMissingSkills > 0) {
						diff[1]++;
					}
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
		return Messages.getString("SkillCoverageConstraint.name"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getConstraintCostLabel()
	 */
	@Override
	public String getCostLabel() {
		return Messages.getString("SkillCoverageConstraint.costLabel"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription()
	 */
	@Override
	public String getHTMLDescription() {
		return Messages.getString("SkillCoverageConstraint.description"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription(de.uos.inf.ischedule.model.Employee)
	 */
	@Override
	public String getHTMLDescription(Employee employee) {
		if (!cover(employee))
			return null;
		return Messages.getString(
				"SkillCoverageConstraint.descriptionEmployee"); //$NON-NLS-1$
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
