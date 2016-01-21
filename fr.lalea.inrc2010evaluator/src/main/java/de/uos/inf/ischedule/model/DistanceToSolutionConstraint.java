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
 * Constraint for the minimization of the distance to a solution.
 * 
 * @author David Meignan
 */
public class DistanceToSolutionConstraint implements Constraint {

	/**
	 * Activation of the constraint.
	 */
	protected boolean active;

	/**
	 * The weight value of the constraint.
	 */
	protected int weightValue;
	
	/**
	 * The solution with which the distance must be minimized.
	 */
	protected Schedule initialSolution = null;

	/**
	 * Evaluator of the constraint.
	 */
	private DistanceToSolutionConstraintEvaluator evaluator = null;
	
	/**
	 * Constructs an distance-to-solution constraint.
	 * 
	 * @param active the active property of the constraint.
	 * @param defaultWeightValue the default weight value of the constraint.
	 * 
	 * @throws IllegalArgumentException if the default weight
	 * value is negative.
	 */
	public DistanceToSolutionConstraint(boolean active, 
			int defaultWeightValue) {
		if (defaultWeightValue < 0)
			throw new IllegalArgumentException();
		
		this.active = active;
		this.weightValue = defaultWeightValue;
	}
	
	/**
	 * Sets the solution with which the distance is minimized.
	 * 
	 * @param initialSolution the solution with which the distance is minimized. 
	 */
	public void setInitialSolution(Schedule initialSolution) {
		this.initialSolution = initialSolution;
		evaluator = null;
	}
	
	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getName()
	 */
	@Override
	public String getName() {
		return Messages.getString("DistanceToSolutionConstraint.name"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getConstraintCostLabel()
	 */
	@Override
	public String getCostLabel() {
		return Messages.getString("DistanceToSolutionConstraint.costLabel"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription()
	 */
	@Override
	public String getHTMLDescription() {
		return Messages.getString("DistanceToSolutionConstraint.description"); //$NON-NLS-1$
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
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription(de.uos.inf.ischedule.model.Employee)
	 */
	@Override
	public String getHTMLDescription(Employee employee) {
		return Messages.getString("DistanceToSolutionConstraint.descriptionEmployee"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#isActive()
	 */
	@Override
	public boolean isActive() {
		return active;
	}

	/**
	 * Activates or deactivates the constraint.
	 * 
	 * @param active the new state of the constraint.
	 */
	public void setActive(boolean active) {
		if (this.active == active)
			return;
		this.active = active;
		evaluator = null;
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getDefaultWeightValue()
	 */
	@Override
	public int getDefaultWeightValue() {
		return weightValue;
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getEvaluator(de.uos.inf.ischedule.model.ShiftSchedulingProblem)
	 */
	@Override
	public ConstraintEvaluator getEvaluator(ShiftSchedulingProblem problem) {
		if (evaluator == null)
			evaluator = new DistanceToSolutionConstraintEvaluator(problem);
		return evaluator;
	}

	class DistanceToSolutionConstraintEvaluator extends
			ConstraintEvaluator {

		ArrayList<ArrayList<Shift>> initialAssignments;
		
		/**
		 * Creates an evaluator of the constraint.
		 * 
		 * @param problem the problem for which the constraint is evaluated.
		 */
		public DistanceToSolutionConstraintEvaluator(
				ShiftSchedulingProblem problem) {
			// Generate the set of initial assignments
			if (initialSolution == null) {
				initialAssignments = null;
			} else {
				initialAssignments = new ArrayList<ArrayList<Shift>>();
				for (int dayIndex=0; dayIndex<problem.schedulingPeriod.size(); 
						dayIndex++) {
					ArrayList<Shift> dayAssignments = 
							new ArrayList<Shift>();
					initialAssignments.add(dayAssignments);
					for (Employee employee: problem.employees) {
						dayAssignments.add(
								initialSolution.getAssignment(dayIndex, employee));
					}
				}
			}
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getConstraint()
		 */
		@Override
		public Constraint getConstraint() {
			return DistanceToSolutionConstraint.this;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getCost(de.uos.inf.ischedule.model.Solution)
		 */
		@Override
		public int getCost(Solution solution) {
			// Check active and weight value
			if (!active || weightValue <= 0 || initialAssignments == null)
				return 0;
			
			int distance = 0;
			for (int dayIndex=0; dayIndex<solution.assignments.size(); dayIndex++) {
				for (int employeeIndex=0; employeeIndex<solution.employees.size(); 
						employeeIndex++) {
					if (solution.assignments.get(dayIndex).get(employeeIndex)
							!= initialAssignments.get(dayIndex).get(employeeIndex))
						distance++;
				}
			}
			
			return distance*weightValue;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getEstimatedAssignmentCost(de.uos.inf.ischedule.model.Solution, int, de.uos.inf.ischedule.model.Shift, int)
		 */
		@Override
		public int getEstimatedAssignmentCost(Solution solution,
				int employeeIndex, Shift shift, int assignmentDayIndex) {
			if (!active || weightValue <= 0 || initialAssignments == null)
				return 0;
			
			if (shift != initialAssignments.get(assignmentDayIndex).get(employeeIndex))
				return weightValue;
			return 0;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getSwapMoveCostDifference(de.uos.inf.ischedule.model.Solution, de.uos.inf.ischedule.model.heuristic.SwapMove)
		 */
		@Override
		public int getSwapMoveCostDifference(Solution solution,
				SwapMove swapMove) {
			if (!active || weightValue <= 0 || initialAssignments == null)
				return 0;
			
			int distanceDifference = 0;
			// Iterate on day-index of block
			for (int dayIndex=swapMove.getStartDayIndex(); 
					dayIndex<swapMove.getStartDayIndex()+swapMove.getBlockSize();
					dayIndex++) {
				int previousDistance = 0;
				int newDistance = 0;
				
				if (solution.assignments.get(dayIndex)
						.get(swapMove.getEmployee1Index())
						!= initialAssignments.get(dayIndex)
						.get(swapMove.getEmployee1Index()))
					previousDistance++;
				if (solution.assignments.get(dayIndex)
						.get(swapMove.getEmployee2Index())
						!= initialAssignments.get(dayIndex)
						.get(swapMove.getEmployee2Index()))
					previousDistance++;
				if (solution.assignments.get(dayIndex)
						.get(swapMove.getEmployee2Index())
						!= initialAssignments.get(dayIndex)
						.get(swapMove.getEmployee1Index()))
					newDistance++;
				if (solution.assignments.get(dayIndex)
						.get(swapMove.getEmployee1Index())
						!= initialAssignments.get(dayIndex)
						.get(swapMove.getEmployee2Index()))
					newDistance++;
				distanceDifference += newDistance-previousDistance;
			}
			return distanceDifference*weightValue;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getConstraintViolations(de.uos.inf.ischedule.model.Solution)
		 */
		@Override
		public ArrayList<ConstraintViolation> getConstraintViolations(
				Solution solution) {
			ArrayList<ConstraintViolation> violations = 
					new ArrayList<ConstraintViolation>();
			// Check active and weight value
			if (!active || weightValue <= 0 || initialAssignments == null)
				return violations;
			
			for (int dayIndex=0; dayIndex<solution.assignments.size(); dayIndex++) {
				for (int employeeIndex=0; employeeIndex<solution.employees.size(); 
						employeeIndex++) {
					if (solution.assignments.get(dayIndex).get(employeeIndex)
							!= initialAssignments.get(dayIndex).get(employeeIndex)) {
						ConstraintViolation violation = new ConstraintViolation(
								DistanceToSolutionConstraint.this);
						violation.setCost(weightValue);
						violation.setMessage(Messages.getString("DistanceToSolutionConstraint.assignmentChange")); //$NON-NLS-1$
						violation.addAssignmentInScope(
								solution.employees.get(employeeIndex), 
								solution.problem.getSchedulingPeriod().getDate(dayIndex));
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
			
			if (!active || weightValue <= 0 || initialAssignments == null)
				return diff;
			
			// Iterate on day-index of block
			for (int dayIndex=swapMove.getStartDayIndex(); 
					dayIndex<swapMove.getStartDayIndex()+swapMove.getBlockSize();
					dayIndex++) {
				boolean previousDiff;
				boolean newDiff;
				
				previousDiff = (solution.assignments.get(dayIndex)
						.get(swapMove.getEmployee1Index())
						!= initialAssignments.get(dayIndex)
						.get(swapMove.getEmployee1Index()));
				newDiff = (solution.assignments.get(dayIndex)
						.get(swapMove.getEmployee2Index())
						!= initialAssignments.get(dayIndex)
						.get(swapMove.getEmployee1Index()));
				if (previousDiff && !newDiff) {
					diff[0]++;
				} else if (!previousDiff && newDiff) {
					diff[1]++;
				}
				
				previousDiff = (solution.assignments.get(dayIndex)
						.get(swapMove.getEmployee2Index())
						!= initialAssignments.get(dayIndex)
						.get(swapMove.getEmployee2Index()));
				newDiff = (solution.assignments.get(dayIndex)
						.get(swapMove.getEmployee1Index())
						!= initialAssignments.get(dayIndex)
						.get(swapMove.getEmployee2Index()));
				if (previousDiff && !newDiff) {
					diff[0]++;
				} else if (!previousDiff && newDiff) {
					diff[1]++;
				}
				
			}
			return diff;
		}
		
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#cover(de.uos.inf.ischedule.model.Employee)
	 */
	@Override
	public boolean cover(Employee employee) {
		return true;
	}

}
