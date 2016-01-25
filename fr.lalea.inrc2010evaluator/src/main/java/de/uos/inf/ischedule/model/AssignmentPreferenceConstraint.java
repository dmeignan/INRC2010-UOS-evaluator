/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.uos.inf.ischedule.model.heuristic.SwapMove;
import de.uos.inf.ischedule.util.Messages;

/**
 * This constraint ensures the assignment of preferred shifts.
 * 
 * @author David Meignan
 */
public class AssignmentPreferenceConstraint implements Constraint {

	/**
	 * Activation of the constraint.
	 */
	private boolean active;

	/**
	 * The weight value of the constraint.
	 */
	protected int weightValue;
	
	/**
	 * Set of preferred assignments.
	 */
	private ArrayList<AssignmentPreference> preferredAssignments =
			new ArrayList<AssignmentPreference>();
	
	/**
	 * Set of unwanted assignments.
	 */
	private ArrayList<AssignmentPreference> unwantedAssignments =
			new ArrayList<AssignmentPreference>();

	/**
	 * Evaluator of the constraint.
	 */
	private AssignmentPreferenceConstraintEvaluator evaluator = null;
	
	/**
	 * Constructs an assignment-preferences constraint.
	 * 
	 * @param active the active property of the constraint.
	 * @param defaultWeightValue the default weight value of the constraint.
	 * 
	 * @throws IllegalArgumentException if the default weight
	 * value is negative.
	 */
	public AssignmentPreferenceConstraint(boolean active, 
			int defaultWeightValue) {
		if (defaultWeightValue < 0)
			throw new IllegalArgumentException();
		
		this.active = active;
		this.weightValue = defaultWeightValue;
	}
	
	/**
	 * Adds a preferred assignment. If the reciprocal preference (unwanted assignment)
	 * exists, it is removed.
	 * 
	 * @param shift the preferred shift or <code>null</code> for a preferred
	 * day off.
	 * @param employee the employee on which the preference applies.
	 * @param dayIndex the day-index of the preference.
	 * @return <code>true</code> if the list of preference has been modified
	 * by the addition of the preference. Returns <code>false</code> if the preference
	 * was already present.
	 */
	public boolean addPreferredAssignment(Shift shift, Employee employee,
			int dayIndex) {
		// Check existing preferences
		for (AssignmentPreference pref: preferredAssignments) {
			if (pref.getShift() == shift &&
					pref.getEmployee() == employee &&
					pref.getDayIndex() == dayIndex)
				return false; // The preference already exists
		}
		Iterator<AssignmentPreference> prefIt = unwantedAssignments.iterator();
		while(prefIt.hasNext()) {
			AssignmentPreference pref = prefIt.next();
			if (pref.getShift() == shift &&
					pref.getEmployee() == employee &&
					pref.getDayIndex() == dayIndex) {
				prefIt.remove();
				break;
			}
		}
		// Add preference
		preferredAssignments.add(new AssignmentPreference(
				true,
				shift,
				employee,
				dayIndex
				));
		evaluator = null;
		return true;
	}
	
	/**
	 * Removes a preferred assignment.
	 * 
	 * @param shift the preferred shift to remove or <code>null</code> for a preferred
	 * day off preference to remove.
	 * @param employee the employee on which the preference applies.
	 * @param dayIndex the day-index of the preference.
	 * @return <code>true</code> if the list of preference has been modified
	 * by the removal of the preference. Returns <code>false</code> if no preference
	 * has been removed.
	 */
	public boolean removePreferredAssignment(Shift shift, Employee employee,
			int dayIndex) {
		// Check existing preferences
		AssignmentPreference preferenceToRemove = null;
		for (AssignmentPreference pref: preferredAssignments) {
			if (pref.getShift() == shift &&
					pref.getEmployee() == employee &&
					pref.getDayIndex() == dayIndex) {
				preferenceToRemove = pref;
			}
		}
		if (preferenceToRemove == null)
			return false;
		else {
			evaluator = null;
			return preferredAssignments.remove(preferenceToRemove);
		}
	}
	
	/**
	 * Clears all preferred assignment preferences.
	 */
	public void clearPreferences() {
		if (preferredAssignments.isEmpty())
			return;
		preferredAssignments.clear();
		evaluator = null;
	}
	
	/**
	 * Remove all preferences associated to an employee for a specific day.
	 * 
	 * @param employee the employee for which preferences have to be removed.
	 * @param dayIndex the day index for which preferences have to be removed.
	 * @return <code>true</code> if the list of preference has been modified
	 * by the removal of preferences. Returns <code>false</code> if no preference
	 * has been removed.
	 */
	public boolean removeAllPreferences(Employee employee, int dayIndex) {
		boolean removedPreference = false;
		// Check preferred
		Iterator<AssignmentPreference> prefIt = preferredAssignments.iterator();
		while(prefIt.hasNext()) {
			AssignmentPreference pref = prefIt.next();
			if (pref.getEmployee() == employee &&
					pref.getDayIndex() == dayIndex) {
				prefIt.remove();
				removedPreference = true;
			}
		}
		// Check unwanted
		prefIt = unwantedAssignments.iterator();
		while(prefIt.hasNext()) {
			AssignmentPreference pref = prefIt.next();
			if (pref.getEmployee() == employee &&
					pref.getDayIndex() == dayIndex) {
				prefIt.remove();
				removedPreference = true;
			}
		}
		// Reset evaluator if preference removed
		if (removedPreference)
			evaluator = null;
		return removedPreference;
	}
	
	/**
	 * Adds a unwanted assignment. If the reciprocal preference (preferred assignment)
	 * exists, it is removed.
	 * 
	 * @param shift the unwanted shift or <code>null</code> for a preferred
	 * day off.
	 * @param employee the employee on which the preference applies.
	 * @param dayIndex the day-index of the preference.
	 * @return <code>true</code> if the list of preference has been modified
	 * by the addition of the preference. Returns <code>false</code> if the preference
	 * was already present.
	 */
	public boolean addUnwantedAssignment(Shift shift, Employee employee,
			int dayIndex) {
		// Check existing preferences
		for (AssignmentPreference pref: unwantedAssignments) {
			if (pref.getShift() == shift &&
					pref.getEmployee() == employee &&
					pref.getDayIndex() == dayIndex)
				return false; // The preference already exists
		}
		Iterator<AssignmentPreference> prefIt = preferredAssignments.iterator();
		while(prefIt.hasNext()) {
			AssignmentPreference pref = prefIt.next();
			if (pref.getShift() == shift &&
					pref.getEmployee() == employee &&
					pref.getDayIndex() == dayIndex) {
				prefIt.remove();
				break;
			}
		}
		// Add preference
		unwantedAssignments.add(new AssignmentPreference(
				false,
				shift,
				employee,
				dayIndex
				));
		evaluator = null;
		return true;
	}
	
	/**
	 * Removes a unwanted assignment.
	 * 
	 * @param shift the unwanted shift to remove or <code>null</code> for a unwanted
	 * day off preference to remove.
	 * @param employee the employee on which the preference applies.
	 * @param dayIndex the day-index of the preference.
	 * @return <code>true</code> if the list of preference has been modified
	 * by the removal of the preference. Returns <code>false</code> if no preference
	 * has been removed.
	 */
	public boolean removeUnwantedAssignment(Shift shift, Employee employee,
			int dayIndex) {
		// Check existing preferences
		AssignmentPreference preferenceToRemove = null;
		for (AssignmentPreference pref: unwantedAssignments) {
			if (pref.getShift() == shift &&
					pref.getEmployee() == employee &&
					pref.getDayIndex() == dayIndex) {
				preferenceToRemove = pref;
			}
		}
		if (preferenceToRemove == null)
			return false;
		else {
			evaluator = null;
			return unwantedAssignments.remove(preferenceToRemove);
		}
	}
	
	/**
	 * Returns a collection view of the preferred assignments.
	 * 
	 * @return a collection view of the preferred assignments.
	 */
	public List<AssignmentPreference> preferredAssignments() {
		return new PreferredAssignmentCollection();
	}
	
	/**
	 * Collection view of preferred assignments.
	 */
	private class PreferredAssignmentCollection extends AbstractList<AssignmentPreference> {

		/* (non-Javadoc)
		 * @see java.util.AbstractList#get(int)
		 */
		@Override
		public AssignmentPreference get(int idx) {
			return preferredAssignments.get(idx);
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size() {
			return preferredAssignments.size();
		}
		
	}
	
	/**
	 * Returns a collection view of unwanted assignments.
	 * 
	 * @return a collection view of unwanted assignments.
	 */
	public List<AssignmentPreference> unwantedAssignments() {
		return new UnwantedAssignmentCollection();
	}
	
	/**
	 * Collection view of preferred assignments.
	 */
	private class UnwantedAssignmentCollection extends AbstractList<AssignmentPreference> {

		/* (non-Javadoc)
		 * @see java.util.AbstractList#get(int)
		 */
		@Override
		public AssignmentPreference get(int idx) {
			return unwantedAssignments.get(idx);
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size() {
			return unwantedAssignments.size();
		}
		
	}
	
	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getName()
	 */
	@Override
	public String getName() {
		return Messages.getString("AssignmentPreferenceConstraint.name"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getConstraintCostLabel()
	 */
	@Override
	public String getCostLabel() {
		return Messages.getString("AssignmentPreferenceConstraint.costLabel"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription()
	 */
	@Override
	public String getHTMLDescription() {
		return Messages.getString("AssignmentPreferenceConstraint.description"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription(de.uos.inf.ischedule.model.Employee)
	 */
	@Override
	public String getHTMLDescription(Employee employee) {
		String paramDesc = Messages.getString(
				"AssignmentPreferenceConstraint.descriptionEmployee"); //$NON-NLS-1$
		paramDesc = paramDesc.replaceAll("\\$1", Integer.toString(preferredAssignments.size())); //$NON-NLS-1$
		paramDesc = paramDesc.replaceAll("\\$2", Integer.toString(unwantedAssignments.size())); //$NON-NLS-1$
		return paramDesc;
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLParametersDescriptions(de.uos.inf.ischedule.model.ShiftSchedulingProblem)
	 */
	@Override
	public List<String> getHTMLParametersDescriptions(
			ShiftSchedulingProblem problem) {
		String paramDesc = Messages.getString(
				"AssignmentPreferenceConstraint.parametersDescription"); //$NON-NLS-1$
		paramDesc = paramDesc.replaceAll("\\$1", Integer.toString(preferredAssignments.size())); //$NON-NLS-1$
		paramDesc = paramDesc.replaceAll("\\$2", Integer.toString(unwantedAssignments.size())); //$NON-NLS-1$
		ArrayList<String> descList = new  ArrayList<String>();
		descList.add(paramDesc);
		return descList;
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
			evaluator = new AssignmentPreferenceConstraintEvaluator(problem);
		return evaluator;
	}
	
	/**
	 * Evaluator of the constraint.
	 */
	public class AssignmentPreferenceConstraintEvaluator extends
			ConstraintEvaluator {

		/**
		 * List of preferences by employee and day-index.
		 * First dimension is day-index, and second one is employee index.
		 */
		ArrayList<ArrayList<ArrayList<Shift>>> preferredAssignmentsLists;
		ArrayList<ArrayList<ArrayList<Shift>>> unwantedAssignmentsLists;
		
		/**
		 * Creates the evaluator.
		 * 
		 * @param problem the shift scheduling problem.
		 */
		public AssignmentPreferenceConstraintEvaluator(
				ShiftSchedulingProblem problem) {
			// List of requests for faster evaluation from indexes
			preferredAssignmentsLists = new 
					ArrayList<ArrayList<ArrayList<Shift>>>();
			unwantedAssignmentsLists = new 
					ArrayList<ArrayList<ArrayList<Shift>>>();
			for (int dayIndex=0; dayIndex<problem.schedulingPeriod.size(); dayIndex++) {
				ArrayList<ArrayList<Shift>> dayPreferredLists = 
						new ArrayList<ArrayList<Shift>>();
				ArrayList<ArrayList<Shift>> dayUnwantedLists = 
						new ArrayList<ArrayList<Shift>>();
				preferredAssignmentsLists.add(dayPreferredLists);
				unwantedAssignmentsLists.add(dayUnwantedLists);
				for (Employee employee: problem.employees) {
					ArrayList<Shift> employeePreferredLists =
							new ArrayList<Shift>();
					ArrayList<Shift> employeeUnwantedLists =
							new ArrayList<Shift>();
					dayPreferredLists.add(employeePreferredLists);
					dayUnwantedLists.add(employeeUnwantedLists);
					for (AssignmentPreference preferred: preferredAssignments) {
						if (preferred.getEmployee() == employee &&
								preferred.getDayIndex() == dayIndex) {
							employeePreferredLists.add(preferred.getShift());
						}
					}
					for (AssignmentPreference unwanted: unwantedAssignments) {
						if (unwanted.getEmployee() == employee &&
								unwanted.getDayIndex() == dayIndex) {
							employeeUnwantedLists.add(unwanted.getShift());
						}
					}
				}
			}
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getConstraint()
		 */
		@Override
		public Constraint getConstraint() {
			return AssignmentPreferenceConstraint.this;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getCost(de.uos.inf.ischedule.model.Solution)
		 */
		@Override
		public int getCost(Solution solution) {
			// Check active and weight value
			if (!active || weightValue <= 0)
				return 0;
			
			int unsatisfiedPreference = 0;
			// Check requests by day and employee
			for (int dayIndex=0; dayIndex<solution.assignments.size(); dayIndex++) {
				for (int employeeIndex=0; employeeIndex<solution.employees.size(); 
						employeeIndex++) {
					Shift assignment = solution.assignments.get(dayIndex).get(employeeIndex);
					ArrayList<Shift> preferredShifts = preferredAssignmentsLists.get(dayIndex)
							.get(employeeIndex);
					if (!preferredShifts.isEmpty() && !preferredShifts.contains(assignment)) {
						unsatisfiedPreference++;
					}
					ArrayList<Shift> unwantedShifts = unwantedAssignmentsLists.get(dayIndex)
							.get(employeeIndex);
					if (unwantedShifts.contains(assignment)) {
						unsatisfiedPreference++;
					}
				}
			}
			// Return total cost
			return unsatisfiedPreference*weightValue;
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
			int unsatisfiedPreference = 0;
			
			ArrayList<Shift> preferredShifts = preferredAssignmentsLists.get(assignmentDayIndex)
					.get(employeeIndex);
			if (!preferredShifts.isEmpty() && !preferredShifts.contains(shift)) {
				unsatisfiedPreference++;
			}
			
			ArrayList<Shift> unwantedShifts = unwantedAssignmentsLists.get(assignmentDayIndex)
					.get(employeeIndex);
			if (unwantedShifts.contains(shift)) {
				unsatisfiedPreference++;
			}
			
			return unsatisfiedPreference*weightValue;
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
			
			int unsatisfiedPreferenceDifference = 0;
			// Iterate on day-index of block
			for (int dayIndex=swapMove.getStartDayIndex(); 
					dayIndex<swapMove.getStartDayIndex()+swapMove.getBlockSize();
					dayIndex++) {
				int previousUnsatisfied = 0;
				int newUnsatisfied = 0;
				
				ArrayList<Shift> preferredShiftsEmployee1 = preferredAssignmentsLists
						.get(dayIndex)
						.get(swapMove.getEmployee1Index());
				ArrayList<Shift> preferredShiftsEmployee2 = preferredAssignmentsLists
						.get(dayIndex)
						.get(swapMove.getEmployee2Index());
				Shift assignmentEmployee1 = solution.assignments.get(dayIndex)
						.get(swapMove.getEmployee1Index());
				Shift assignmentEmployee2 = solution.assignments.get(dayIndex)
						.get(swapMove.getEmployee2Index());
				
				if (!preferredShiftsEmployee1.isEmpty() 
						&& !preferredShiftsEmployee1.contains(assignmentEmployee1)) {
					previousUnsatisfied++;
				}
				if (!preferredShiftsEmployee2.isEmpty() 
						&& !preferredShiftsEmployee2.contains(assignmentEmployee2)) {
					previousUnsatisfied++;
				}
				if (!preferredShiftsEmployee1.isEmpty() 
						&& !preferredShiftsEmployee1.contains(assignmentEmployee2)) {
					newUnsatisfied++;
				}
				if (!preferredShiftsEmployee2.isEmpty() 
						&& !preferredShiftsEmployee2.contains(assignmentEmployee1)) {
					newUnsatisfied++;
				}
				
				ArrayList<Shift> unwantedShiftsEmployee1 = unwantedAssignmentsLists
						.get(dayIndex)
						.get(swapMove.getEmployee1Index());
				ArrayList<Shift> unwantedShiftsEmployee2 = unwantedAssignmentsLists
						.get(dayIndex)
						.get(swapMove.getEmployee2Index());
				
				if (unwantedShiftsEmployee1.contains(assignmentEmployee1)) {
					previousUnsatisfied++;
				}
				if (unwantedShiftsEmployee2.contains(assignmentEmployee2)) {
					previousUnsatisfied++;
				}
				if (unwantedShiftsEmployee1.contains(assignmentEmployee2)) {
					newUnsatisfied++;
				}
				if (unwantedShiftsEmployee2.contains(assignmentEmployee1)) {
					newUnsatisfied++;
				}
				
				unsatisfiedPreferenceDifference += newUnsatisfied-previousUnsatisfied;
			}
			return unsatisfiedPreferenceDifference*weightValue;
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
			
			for (int dayIndex=0; dayIndex<solution.assignments.size(); dayIndex++) {
				for (int employeeIndex=0; employeeIndex<solution.employees.size(); 
						employeeIndex++) {
					Shift assignment = solution.assignments.get(dayIndex)
							.get(employeeIndex);
					ArrayList<Shift> preferredShifts = preferredAssignmentsLists.get(dayIndex)
							.get(employeeIndex);
					if (!preferredShifts.isEmpty() && !preferredShifts.contains(assignment)) {
						ConstraintViolation violation = new ConstraintViolation(
								AssignmentPreferenceConstraint.this);
						violation.setCost(weightValue);
						violation.setMessage(Messages.getString("AssignmentPreferenceConstraint.unsatisfiedPreferredShift")); //$NON-NLS-1$
						violation.addAssignmentInScope(
								solution.employees.get(employeeIndex), 
								solution.problem.getSchedulingPeriod().getDate(dayIndex));
						violations.add(violation);
					}
					ArrayList<Shift> unwantedShifts = unwantedAssignmentsLists.get(dayIndex)
							.get(employeeIndex);
					if (!unwantedShifts.isEmpty() && unwantedShifts.contains(assignment)) {
						ConstraintViolation violation = new ConstraintViolation(
								AssignmentPreferenceConstraint.this);
						violation.setCost(weightValue);
						violation.setMessage(Messages.getString("AssignmentPreferenceConstraint.unsatisfiedUnwantedShift")); //$NON-NLS-1$
						violation.addAssignmentInScope(
								solution.employees.get(employeeIndex), 
								solution.problem.getSchedulingPeriod().getDate(dayIndex));
						violations.add(violation);
					}
				}
			}
			return violations;
		}
		
		/**
		 * Returns <code>true</code> if there is at least one preferred assignment for
		 * the given day-index and employee index, returns <code>false</code> otherwise.
		 * This does not take into account the fact that the constraint is not active or
		 * the weight is null.
		 * 
		 * @param dayIndex the day-index.
		 * @param employeeIndex the employee index.
		 * @return <code>true</code> if there is at least one preferred assignment for
		 * the given day-index and employee index, returns <code>false</code> otherwise.
		 */
		public boolean hasPreferredAssignment(int dayIndex, int employeeIndex) {
			ArrayList<Shift> preferredAssignments = preferredAssignmentsLists
					.get(dayIndex).get(employeeIndex);
			return (preferredAssignments != null && !preferredAssignments.isEmpty());
		}
		
		/**
		 * Returns <code>true</code> if the specified shift is a preferred assignment for
		 * the given day index and employee index, returns <code>false</code> otherwise.
		 * This does not take into account the fact that the constraint is not active or
		 * the weight is null.
		 * 
		 * @param dayIndex the day index.
		 * @param employeeIndex the employee index.
		 * @param shift the shift.
		 * @return <code>true</code> if the specified shift is a preferred assignment for
		 * the given day index and employee index.
		 */
		public boolean isPreferredAssignment(int dayIndex, int employeeIndex, Shift shift) {
			ArrayList<Shift> preferredAssignments = preferredAssignmentsLists
					.get(dayIndex).get(employeeIndex);
			return (preferredAssignments != null && preferredAssignments.contains(shift));
		}
		
		/**
		 * Returns <code>true</code> if there is at least one unwanted assignment for
		 * the given day-index and employee index, returns <code>false</code> otherwise.
		 * This does not take into account the fact that the constraint is not active or
		 * the weight is null.
		 * 
		 * @param dayIndex the day-index.
		 * @param employeeIndex the employee index.
		 * @return <code>true</code> if there is at least one unwanted assignment for
		 * the given day-index and employee index, returns <code>false</code> otherwise.
		 */
		public boolean hasUnwantedAssignment(int dayIndex, int employeeIndex) {
			ArrayList<Shift> unwantedAssignments = unwantedAssignmentsLists
					.get(dayIndex).get(employeeIndex);
			return (unwantedAssignments != null && !unwantedAssignments.isEmpty());
		}
		
		/**
		 * Returns <code>true</code> if the specified shift is a unwanted assignment for
		 * the given day index and employee index, returns <code>false</code> otherwise.
		 * This does not take into account the fact that the constraint is not active or
		 * the weight is null.
		 * 
		 * @param dayIndex the day index.
		 * @param employeeIndex the employee index.
		 * @param shift the shift.
		 * @return <code>true</code> if the specified shift is a unwanted assignment for
		 * the given day index and employee index.
		 */
		public boolean isUnwantedAssignment(int dayIndex, int employeeIndex, Shift shift) {
			ArrayList<Shift> unwantedAssignments = unwantedAssignmentsLists
					.get(dayIndex).get(employeeIndex);
			return (unwantedAssignments != null && unwantedAssignments.contains(shift));
		}

		/**
		 * Returns <code>true</code> if the given shift satisfy the assignment
		 * preferences. Returns <code>false</code> otherwise.
		 * 
		 * @param dayIndex the day index of the assignment.
		 * @param employeeIndex the employee index.
		 * @param shift the shift.
		 * @return <code>true</code> if the given shift satisfy the assignment
		 * preferences. Returns <code>false</code> otherwise.
		 */
		public boolean satisfyPreference(int dayIndex, int employeeIndex,
				Shift shift) {
			if (isUnwantedAssignment(dayIndex, employeeIndex, shift))
				return false;
			if (hasPreferredAssignment(dayIndex, employeeIndex) &&
					!isPreferredAssignment(dayIndex, employeeIndex, shift))
				return false;
			return true;
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
			
			// Iterate on day-index of block
			for (int dayIndex=swapMove.getStartDayIndex(); 
					dayIndex<swapMove.getStartDayIndex()+swapMove.getBlockSize();
					dayIndex++) {
				boolean previousUnsatisfied;
				boolean newUnsatisfied;
				
				ArrayList<Shift> preferredShiftsEmployee1 = preferredAssignmentsLists
						.get(dayIndex)
						.get(swapMove.getEmployee1Index());
				ArrayList<Shift> preferredShiftsEmployee2 = preferredAssignmentsLists
						.get(dayIndex)
						.get(swapMove.getEmployee2Index());
				Shift assignmentEmployee1 = solution.assignments.get(dayIndex)
						.get(swapMove.getEmployee1Index());
				Shift assignmentEmployee2 = solution.assignments.get(dayIndex)
						.get(swapMove.getEmployee2Index());
				
				previousUnsatisfied = (!preferredShiftsEmployee1.isEmpty() 
						&& !preferredShiftsEmployee1.contains(assignmentEmployee1));
				newUnsatisfied = (!preferredShiftsEmployee1.isEmpty() 
						&& !preferredShiftsEmployee1.contains(assignmentEmployee2));
				if (previousUnsatisfied && !newUnsatisfied) {
					diff[0]++;
				} else if (!previousUnsatisfied && newUnsatisfied) {
					diff[1]++;
				}
				
				previousUnsatisfied = (!preferredShiftsEmployee2.isEmpty() 
						&& !preferredShiftsEmployee2.contains(assignmentEmployee2));
				newUnsatisfied = (!preferredShiftsEmployee2.isEmpty() 
						&& !preferredShiftsEmployee2.contains(assignmentEmployee1));
				if (previousUnsatisfied && !newUnsatisfied) {
					diff[0]++;
				} else if (!previousUnsatisfied && newUnsatisfied) {
					diff[1]++;
				}
				
				ArrayList<Shift> unwantedShiftsEmployee1 = unwantedAssignmentsLists
						.get(dayIndex)
						.get(swapMove.getEmployee1Index());
				ArrayList<Shift> unwantedShiftsEmployee2 = unwantedAssignmentsLists
						.get(dayIndex)
						.get(swapMove.getEmployee2Index());
				
				previousUnsatisfied = (unwantedShiftsEmployee1.contains(
						assignmentEmployee1));
				newUnsatisfied = (unwantedShiftsEmployee1.contains(
						assignmentEmployee2));
				if (previousUnsatisfied && !newUnsatisfied) {
					diff[0]++;
				} else if (!previousUnsatisfied && newUnsatisfied) {
					diff[1]++;
				}
				
				previousUnsatisfied = (unwantedShiftsEmployee2.contains(
						assignmentEmployee2));
				
				newUnsatisfied = (unwantedShiftsEmployee2.contains(
						assignmentEmployee1));
				if (previousUnsatisfied && !newUnsatisfied) {
					diff[0]++;
				} else if (!previousUnsatisfied && newUnsatisfied) {
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
