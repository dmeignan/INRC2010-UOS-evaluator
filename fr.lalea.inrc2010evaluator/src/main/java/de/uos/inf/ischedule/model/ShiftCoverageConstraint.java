/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import de.uos.inf.ischedule.model.heuristic.SwapMove;
import de.uos.inf.ischedule.util.Messages;

/**
 * This constraint ensure that all shift demands are covered.
 * 
 * @author David Meignan
 */
public class ShiftCoverageConstraint implements Constraint {

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
	private ShiftCoverageConstraintEvaluator evaluator = null;
	
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
	 * Constructs a coverage constraint.
	 *  
	 * @param active the active property of the constraint.
	 * @param defaultWeightValue the default weight value of the constraint.
	 * 
	 * @throws IllegalArgumentException if the rank-index or the weight value is
	 * negative.
	 */
	public ShiftCoverageConstraint(boolean active, 
			int defaultWeightValue) {
		if (defaultWeightValue < 0)
			throw new IllegalArgumentException();
		this.active = active;
		this.weightValue = defaultWeightValue;
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getEvaluator(de.uos.inf.ischedule.model.ShiftSchedulingProblem)
	 */
	@Override
	public ConstraintEvaluator getEvaluator(ShiftSchedulingProblem problem) {
		if (evaluator == null)
			evaluator = new ShiftCoverageConstraintEvaluator(problem);
		return evaluator;
	}

	/**
	 * Evaluator of the constraint.
	 */
	class ShiftCoverageConstraintEvaluator extends
			ConstraintEvaluator {

		/**
		 * Set of shift-demand per day.
		 * The first dimension is the set of days.
		 */
		public ArrayList<TreeMap<Shift, Integer>> shiftDemands;
		
		/**
		 * Creates an evaluator for the given problem instance.
		 * 
		 * @param problem the problem instance.
		 */
		public ShiftCoverageConstraintEvaluator(ShiftSchedulingProblem problem) {
			shiftDemands = new ArrayList<TreeMap<Shift, Integer>>();
			for (int dayIndex=0; dayIndex<problem.schedulingPeriod.size();
					dayIndex++) {
				TreeMap<Shift, Integer> dayDemands = new TreeMap<Shift, Integer>();
				shiftDemands.add(dayDemands);
				for (Shift shift: problem.shifts) {
					int demand = problem.getDemand(shift, dayIndex);
					dayDemands.put(shift, demand);
				}
			}
		}
		
		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getCost(de.uos.inf.ischedule.model.Solution)
		 */
		@Override
		public int getCost(Solution solution) {
			// Count the number of unassigned shift-slots
			int unassigned = 0;
			for (int dayIndex=0; dayIndex<solution.unassignedSlots.size();
					dayIndex++) {
				unassigned += solution.unassignedSlots.get(dayIndex).size();
			}
			// Check if over-staffing
			int overstaffing = 0;
			for (int dayIndex=0; dayIndex<solution.unassignedSlots.size();
					dayIndex++) {
				for (Shift shift: solution.problem.shifts) {
					int shiftDemand = shiftDemands.get(dayIndex).get(shift);
					int shiftAssignmentCount = 0;
					for (int employeeIndex=0; employeeIndex<solution.employees.size();
							employeeIndex++) {
						if (solution.assignments.get(dayIndex)
								.get(employeeIndex) == shift) {
							shiftAssignmentCount++;
						}
					}
					if (shiftDemand < shiftAssignmentCount) {
						overstaffing += shiftAssignmentCount-shiftDemand;
					}
				}
			}
			return unassigned+overstaffing;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getEstimatedAssignmentCost(de.uos.inf.ischedule.model.Solution, int, de.uos.inf.ischedule.model.Shift, int)
		 */
		@Override
		public int getEstimatedAssignmentCost(Solution solution,
				int employeeIndex, Shift shift, int dayIndex) {
			return -1;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getSwapMoveCostDifference(de.uos.inf.ischedule.model.Solution, de.uos.inf.ischedule.model.heuristic.SwapMove)
		 */
		@Override
		public int getSwapMoveCostDifference(Solution solution,
				SwapMove swapMove) {
			return 0;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getConstraint()
		 */
		@Override
		public Constraint getConstraint() {
			return ShiftCoverageConstraint.this;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getConstraintViolations(de.uos.inf.ischedule.model.Solution)
		 */
		@Override
		public ArrayList<ConstraintViolation> getConstraintViolations(
				Solution solution) {
			ArrayList<ConstraintViolation> violations = new ArrayList<ConstraintViolation>();
			if (!active)
				return violations;
			

			for (int dayIndex=0; dayIndex<solution.unassignedSlots.size();
					dayIndex++) {
				for (int u=0; u<solution.unassignedSlots.get(dayIndex).size(); u++) {
					ConstraintViolation violation = new ConstraintViolation(
							ShiftCoverageConstraint.this);
					violation.setCost(weightValue);
					violation.setMessage(Messages.getString("ShiftCoverageConstraint.underStaffing")); //$NON-NLS-1$
					violation.addFullDayInScope( 
							solution.problem.getSchedulingPeriod().getDate(dayIndex));
					violations.add(violation);
				}
			}
			// Check if over-staffing
			for (int dayIndex=0; dayIndex<solution.unassignedSlots.size();
					dayIndex++) {
				for (Shift shift: solution.problem.shifts) {
					int shiftDemand = shiftDemands.get(dayIndex).get(shift);
					int shiftAssignmentCount = 0;
					for (int employeeIndex=0; employeeIndex<solution.employees.size();
							employeeIndex++) {
						if (solution.assignments.get(dayIndex)
								.get(employeeIndex) == shift) {
							shiftAssignmentCount++;
						}
					}
					if (shiftDemand < shiftAssignmentCount) {
						ConstraintViolation violation = new ConstraintViolation(
								ShiftCoverageConstraint.this);
						violation.setCost(weightValue);
						violation.setMessage(Messages.getString("ShiftCoverageConstraint.overStaffing")); //$NON-NLS-1$
						violation.addFullDayInScope( 
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
			return new int[]{0, 0};
		}
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getName()
	 */
	@Override
	public String getName() {
		return Messages.getString("ShiftCoverageConstraint.name"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getConstraintCostLabel()
	 */
	@Override
	public String getCostLabel() {
		return Messages.getString("ShiftCoverageConstraint.costLabel"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription()
	 */
	@Override
	public String getHTMLDescription() {
		return Messages.getString("ShiftCoverageConstraint.description"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription(de.uos.inf.ischedule.model.Employee)
	 */
	@Override
	public String getHTMLDescription(Employee employee) {
		return null;
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
		return true;
	}

}
