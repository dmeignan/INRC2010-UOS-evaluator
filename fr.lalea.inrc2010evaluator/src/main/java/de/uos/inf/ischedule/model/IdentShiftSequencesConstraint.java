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
 * This constraint ensures that the shift does not change in a sequence of working days.
 * 
 * @author David Meignan
 */
public class IdentShiftSequencesConstraint implements Constraint {

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
	private IdentShiftSequencesConstraintEvaluator evaluator = null;
	
	/**
	 * Constructs the constraint.
	 * 
	 * @param active the active property of the constraint.
	 * @param defaultWeightValue the default weight value of the constraint.
	 * 
	 * @throws IllegalArgumentException if the default weight
	 * value is negative.
	 */
	public IdentShiftSequencesConstraint(boolean active, 
			int defaultWeightValue) {
		if (defaultWeightValue < 0)
			throw new IllegalArgumentException();
		
		this.active = active;
		this.weightValue = defaultWeightValue;
	}
	
	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getName()
	 */
	@Override
	public String getName() {
		return Messages.getString("IdentShiftSequencesConstraint.name"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getConstraintCostLabel()
	 */
	@Override
	public String getCostLabel() {
		return Messages.getString("IdentShiftSequencesConstraint.costLabel"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription()
	 */
	@Override
	public String getHTMLDescription() {
		return Messages.getString("IdentShiftSequencesConstraint.description"); //$NON-NLS-1$
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
		return Messages.getString("IdentShiftSequencesConstraint.descriptionEmployee"); //$NON-NLS-1$
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
			evaluator = new IdentShiftSequencesConstraintEvaluator(problem);
		return evaluator;
	}

	class IdentShiftSequencesConstraintEvaluator extends
			ConstraintEvaluator {

		/**
		 * Creates an evaluator of the constraint.
		 * 
		 * @param problem the problem for which the constraint is evaluated.
		 */
		public IdentShiftSequencesConstraintEvaluator(
				ShiftSchedulingProblem problem) {
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getConstraint()
		 */
		@Override
		public Constraint getConstraint() {
			return IdentShiftSequencesConstraint.this;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getCost(de.uos.inf.ischedule.model.Solution)
		 */
		@Override
		public int getCost(Solution solution) {
			// Check active and weight value
			if (!active || weightValue <= 0)
				return 0;
			
			int changes = 0;
			for (int employeeIndex=0; employeeIndex<solution.employees.size(); 
					employeeIndex++) {
				for (int dayIndex=0; dayIndex<solution.assignments.size()-1; dayIndex++) {
					if (
							solution.assignments.get(dayIndex).get(employeeIndex) != null &&
							solution.assignments.get(dayIndex+1).get(employeeIndex) != null &&
							solution.assignments.get(dayIndex).get(employeeIndex) !=
									solution.assignments.get(dayIndex+1).get(employeeIndex))
						changes++;
				}
			}
			
			return changes*weightValue;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getEstimatedAssignmentCost(de.uos.inf.ischedule.model.Solution, int, de.uos.inf.ischedule.model.Shift, int)
		 */
		@Override
		public int getEstimatedAssignmentCost(Solution solution,
				int employeeIndex, Shift shift, int assignmentDayIndex) {
			if (!active || weightValue <= 0)
				return 0;
			
			int changes = 0;
			
			// Change with previous day
			if (assignmentDayIndex > 0 &&
					solution.assignments.get(assignmentDayIndex-1).get(employeeIndex) != null &&
					shift != null &&
					solution.assignments.get(assignmentDayIndex-1).get(employeeIndex) != shift)
				changes++;
			
			// Change with next day
			if (assignmentDayIndex < solution.assignments.size()-1 &&
					solution.assignments.get(assignmentDayIndex+1).get(employeeIndex) != null &&
					shift != null &&
					solution.assignments.get(assignmentDayIndex+1).get(employeeIndex) != shift)
				changes++;
			
			return changes*weightValue;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getSwapMoveCostDifference(de.uos.inf.ischedule.model.Solution, de.uos.inf.ischedule.model.heuristic.SwapMove)
		 */
		@Override
		public int getSwapMoveCostDifference(Solution solution,
				SwapMove swapMove) {
			if (!active || weightValue <= 0)
				return 0;
			
			int initialChanges = 0;
			int swapChanges = 0;
			
			// Change at the first swap day (with previous day)
			if (swapMove.getStartDayIndex() > 0) {
				// Initial changes
				if (
						solution.assignments.get(swapMove.getStartDayIndex()-1)
							.get(swapMove.getEmployee1Index()) != null &&
						solution.assignments.get(swapMove.getStartDayIndex())
							.get(swapMove.getEmployee1Index()) != null &&
						solution.assignments.get(swapMove.getStartDayIndex()-1)
							.get(swapMove.getEmployee1Index()) !=
							solution.assignments.get(swapMove.getStartDayIndex())
							.get(swapMove.getEmployee1Index()))
					initialChanges++;
				if (
						solution.assignments.get(swapMove.getStartDayIndex()-1)
							.get(swapMove.getEmployee2Index()) != null &&
						solution.assignments.get(swapMove.getStartDayIndex())
							.get(swapMove.getEmployee2Index()) != null &&
						solution.assignments.get(swapMove.getStartDayIndex()-1)
							.get(swapMove.getEmployee2Index()) !=
							solution.assignments.get(swapMove.getStartDayIndex())
							.get(swapMove.getEmployee2Index()))
					initialChanges++;
						
				// Swap changes
				if (
						solution.assignments.get(swapMove.getStartDayIndex()-1)
							.get(swapMove.getEmployee1Index()) != null &&
						solution.assignments.get(swapMove.getStartDayIndex())
							.get(swapMove.getEmployee2Index()) != null &&
						solution.assignments.get(swapMove.getStartDayIndex()-1)
							.get(swapMove.getEmployee1Index()) !=
							solution.assignments.get(swapMove.getStartDayIndex())
							.get(swapMove.getEmployee2Index()))
					swapChanges++;
				if (
						solution.assignments.get(swapMove.getStartDayIndex()-1)
							.get(swapMove.getEmployee2Index()) != null &&
						solution.assignments.get(swapMove.getStartDayIndex())
							.get(swapMove.getEmployee1Index()) != null &&
						solution.assignments.get(swapMove.getStartDayIndex()-1)
							.get(swapMove.getEmployee2Index()) !=
							solution.assignments.get(swapMove.getStartDayIndex())
							.get(swapMove.getEmployee1Index()))
					swapChanges++;
			}
			
			// Change at the last swap day (with next day)
			if (swapMove.getEndDayIndex() < solution.assignments.size()-1) {
				// Initial changes
				if (
						solution.assignments.get(swapMove.getEndDayIndex())
							.get(swapMove.getEmployee1Index()) != null &&
						solution.assignments.get(swapMove.getEndDayIndex()+1)
							.get(swapMove.getEmployee1Index()) != null &&
						solution.assignments.get(swapMove.getEndDayIndex())
							.get(swapMove.getEmployee1Index()) !=
							solution.assignments.get(swapMove.getEndDayIndex()+1)
							.get(swapMove.getEmployee1Index()))
					initialChanges++;
				if (
						solution.assignments.get(swapMove.getEndDayIndex())
							.get(swapMove.getEmployee2Index()) != null &&
						solution.assignments.get(swapMove.getEndDayIndex()+1)
							.get(swapMove.getEmployee2Index()) != null &&
						solution.assignments.get(swapMove.getEndDayIndex())
							.get(swapMove.getEmployee2Index()) !=
							solution.assignments.get(swapMove.getEndDayIndex()+1)
							.get(swapMove.getEmployee2Index()))
					initialChanges++;
						
				// Swap changes
				if (
						solution.assignments.get(swapMove.getEndDayIndex())
							.get(swapMove.getEmployee2Index()) != null &&
						solution.assignments.get(swapMove.getEndDayIndex()+1)
							.get(swapMove.getEmployee1Index()) != null &&
						solution.assignments.get(swapMove.getEndDayIndex())
							.get(swapMove.getEmployee2Index()) !=
							solution.assignments.get(swapMove.getEndDayIndex()+1)
							.get(swapMove.getEmployee1Index()))
					swapChanges++;
				if (
						solution.assignments.get(swapMove.getEndDayIndex())
							.get(swapMove.getEmployee1Index()) != null &&
						solution.assignments.get(swapMove.getEndDayIndex()+1)
							.get(swapMove.getEmployee2Index()) != null &&
						solution.assignments.get(swapMove.getEndDayIndex())
							.get(swapMove.getEmployee1Index()) !=
							solution.assignments.get(swapMove.getEndDayIndex()+1)
							.get(swapMove.getEmployee2Index()))
					swapChanges++;
			}
			
			return (swapChanges-initialChanges)*weightValue;
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
			if (!active || weightValue <= 0)
				return violations;
			
			for (int employeeIndex=0; employeeIndex<solution.employees.size(); 
					employeeIndex++) {
				for (int dayIndex=0; dayIndex<solution.assignments.size()-1; dayIndex++) {
					if (
							solution.assignments.get(dayIndex).get(employeeIndex) != null &&
							solution.assignments.get(dayIndex+1).get(employeeIndex) != null &&
							solution.assignments.get(dayIndex).get(employeeIndex) !=
									solution.assignments.get(dayIndex+1).get(employeeIndex)) {
						ConstraintViolation violation = new ConstraintViolation(
								IdentShiftSequencesConstraint.this);
						violation.setCost(weightValue);
						violation.setMessage(Messages.getString("IdentShiftSequencesConstraint.shiftChange")); //$NON-NLS-1$
						violation.addAssignmentRangeInScope(
								solution.employees.get(employeeIndex), 
								solution.problem.getSchedulingPeriod().getDate(dayIndex),
								solution.problem.getSchedulingPeriod().getDate(dayIndex+1));
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
			
			if (!active || weightValue <= 0)
				return diff;
			
			// Change at the first swap day (with previous day)
			if (swapMove.getStartDayIndex() > 0) {

				boolean initialUnsat = (
						solution.assignments.get(swapMove.getStartDayIndex()-1)
							.get(swapMove.getEmployee1Index()) != null &&
						solution.assignments.get(swapMove.getStartDayIndex())
							.get(swapMove.getEmployee1Index()) != null &&
						solution.assignments.get(swapMove.getStartDayIndex()-1)
							.get(swapMove.getEmployee1Index()) !=
							solution.assignments.get(swapMove.getStartDayIndex())
							.get(swapMove.getEmployee1Index()));
				
				boolean swapUnsat = (
						solution.assignments.get(swapMove.getStartDayIndex()-1)
							.get(swapMove.getEmployee1Index()) != null &&
						solution.assignments.get(swapMove.getStartDayIndex())
							.get(swapMove.getEmployee2Index()) != null &&
						solution.assignments.get(swapMove.getStartDayIndex()-1)
							.get(swapMove.getEmployee1Index()) !=
							solution.assignments.get(swapMove.getStartDayIndex())
							.get(swapMove.getEmployee2Index()));
				
				if (initialUnsat != swapUnsat) {
					if (initialUnsat) {
						// Solved by the move
						diff[0]++;
					} else {
						// Unsatisfied by the move
						diff[1]++;
					}
				}
				
				initialUnsat = (
						solution.assignments.get(swapMove.getStartDayIndex()-1)
							.get(swapMove.getEmployee2Index()) != null &&
						solution.assignments.get(swapMove.getStartDayIndex())
							.get(swapMove.getEmployee2Index()) != null &&
						solution.assignments.get(swapMove.getStartDayIndex()-1)
							.get(swapMove.getEmployee2Index()) !=
							solution.assignments.get(swapMove.getStartDayIndex())
							.get(swapMove.getEmployee2Index()));
						
				swapUnsat = (
						solution.assignments.get(swapMove.getStartDayIndex()-1)
							.get(swapMove.getEmployee2Index()) != null &&
						solution.assignments.get(swapMove.getStartDayIndex())
							.get(swapMove.getEmployee1Index()) != null &&
						solution.assignments.get(swapMove.getStartDayIndex()-1)
							.get(swapMove.getEmployee2Index()) !=
							solution.assignments.get(swapMove.getStartDayIndex())
							.get(swapMove.getEmployee1Index()));

				if (initialUnsat != swapUnsat) {
					if (initialUnsat) {
						// Solved by the move
						diff[0]++;
					} else {
						// Unsatisfied by the move
						diff[1]++;
					}
				}
				
			}
			
			// Change at the last swap day (with next day)
			if (swapMove.getEndDayIndex() < solution.assignments.size()-1) {
				
				// Initial changes
				boolean initialUnsat = (
						solution.assignments.get(swapMove.getEndDayIndex())
							.get(swapMove.getEmployee1Index()) != null &&
						solution.assignments.get(swapMove.getEndDayIndex()+1)
							.get(swapMove.getEmployee1Index()) != null &&
						solution.assignments.get(swapMove.getEndDayIndex())
							.get(swapMove.getEmployee1Index()) !=
							solution.assignments.get(swapMove.getEndDayIndex()+1)
							.get(swapMove.getEmployee1Index()));
				
				boolean swapUnsat = (
						solution.assignments.get(swapMove.getEndDayIndex())
							.get(swapMove.getEmployee2Index()) != null &&
						solution.assignments.get(swapMove.getEndDayIndex()+1)
							.get(swapMove.getEmployee1Index()) != null &&
						solution.assignments.get(swapMove.getEndDayIndex())
							.get(swapMove.getEmployee2Index()) !=
							solution.assignments.get(swapMove.getEndDayIndex()+1)
							.get(swapMove.getEmployee1Index()));

				if (initialUnsat != swapUnsat) {
					if (initialUnsat) {
						// Solved by the move
						diff[0]++;
					} else {
						// Unsatisfied by the move
						diff[1]++;
					}
				}
				
				initialUnsat = (
						solution.assignments.get(swapMove.getEndDayIndex())
							.get(swapMove.getEmployee2Index()) != null &&
						solution.assignments.get(swapMove.getEndDayIndex()+1)
							.get(swapMove.getEmployee2Index()) != null &&
						solution.assignments.get(swapMove.getEndDayIndex())
							.get(swapMove.getEmployee2Index()) !=
							solution.assignments.get(swapMove.getEndDayIndex()+1)
							.get(swapMove.getEmployee2Index()));
				
				swapUnsat = (
						solution.assignments.get(swapMove.getEndDayIndex())
							.get(swapMove.getEmployee1Index()) != null &&
						solution.assignments.get(swapMove.getEndDayIndex()+1)
							.get(swapMove.getEmployee2Index()) != null &&
						solution.assignments.get(swapMove.getEndDayIndex())
							.get(swapMove.getEmployee1Index()) !=
							solution.assignments.get(swapMove.getEndDayIndex()+1)
							.get(swapMove.getEmployee2Index()));

				if (initialUnsat != swapUnsat) {
					if (initialUnsat) {
						// Solved by the move
						diff[0]++;
					} else {
						// Unsatisfied by the move
						diff[1]++;
					}
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
