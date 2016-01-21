/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

/**
 * A <code>Schedule</code> is a set of assignments of employees to shifts,
 * defined from a given scheduling period. A <code>Schedule</code> cannot be
 * evaluated. It needs to be converted to a <code>Solution</code>.
 * 
 * @author David Meignan
 */
public class Schedule {

	/**
	 * ID of the schedule.
	 */
	protected String id;
	
	/**
	 * Label of the schedule.
	 */
	protected String description;
	
	/**
	 * Shift scheduling problem instance.
	 * If the problem is <code>null</code>, the schedule cannot be converted to
	 * a solution.
	 */
	protected ShiftSchedulingProblem problem;
	
	/**
	 * Set of assignments defined by the schedule.
	 */
	protected ArrayList<Assignment> assignments = new ArrayList<Assignment>();
	
	/**
	 * Constructs a schedule.
	 * 
	 * @param id the ID of the schedule.
	 * @param description the description of the schedule.
	 * @param shiftSchedulingProblem the shift scheduling problem.
	 * @throws IllegalArgumentException if the ID, the description, or the
	 * scheduling period is <code>null</code>.
	 */
	public Schedule(String id, String description,
			ShiftSchedulingProblem shiftSchedulingProblem,
			Period schedulingPeriod) {
		if (id == null || description == null || 
				schedulingPeriod == null)
			throw new IllegalArgumentException();
		this.id = id;
		this.description = description;
		this.problem = shiftSchedulingProblem;
	}
	
	/**
	 * Returns a collection view of the assignments.
	 * 
	 * @return a collection view of the assignments.
	 */
	public List<Assignment> assignments() {
		return new AssignmentCollection();
	}
	
	private class AssignmentCollection extends AbstractList<Assignment> {

		/* (non-Javadoc)
		 * @see java.util.AbstractList#get(int)
		 */
		@Override
		public Assignment get(int index) {
			return assignments.get(index);
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size() {
			return assignments.size();
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractList#add(java.lang.Object)
		 */
		@Override
		public boolean add(Assignment e) {
			return assignments.add(e);
		}

	}

	/**
	 * Returns the ID of the schedule.
	 * 
	 * @return the ID of the schedule.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the description of the schedule.
	 * 
	 * @return the description of the schedule.
	 */
	public Object getDescription() {
		return description;
	}
	
	/**
	 * Creates and returns a solution that corresponds to this schedule.
	 * 
	 * @return a solution that corresponds to this schedule.
	 * @throws IllegalArgumentException if the schedule cannot be converted
	 * into a solution. A schedule without shift scheduling problem cannot be
	 * converted.
	 */
	public Solution toSolution() {
		if (problem == null)
			throw new IllegalArgumentException();
		// Create empty solution
		Solution solution = new Solution(problem);
		// Add assignments
		for (Assignment assignment: assignments) {
			int dayIndex = problem.schedulingPeriod
					.getDayIndex(assignment.getDate());
			int employeeIndex = problem.employees.indexOf(
					assignment.employee);
			if (employeeIndex == -1)
				throw new IllegalArgumentException();
			if (!solution.unassignedSlots.get(dayIndex)
					.remove(assignment.shift)) {
				// Over-staffing!
			}
			if (solution.assignments.get(dayIndex).get(employeeIndex) != null) {
				// Solution does not allow multiple assignments per day
				throw new IllegalArgumentException(
						"Solution does not allow multiple assignments per day for " +
						"the same employee.");
			}
			solution.assignments.get(dayIndex).set(employeeIndex, assignment.shift);
		}
		// Return solution
		return solution;
	}

	/**
	 * Returns the ID of the problem for which this schedule is defined. 
	 * Returns <code>null</code> if no problem instance is associated to this schedule.
	 * 
	 * @return the ID of the problem for which this schedule is defined. 
	 */
	public String getProblemId() {
		if (problem == null)
			return null;
		return problem.getId();
	}
	
	/**
	 * Returns the first found shift assigned to an employee at the given day-index.
	 * Returns <code>null</code> if no assignment has been found for employee
	 * at the day-index.
	 * 
	 * @param dayIndex the day-index of the assignment.
	 * @param employee the employee.
	 * 
	 * @return the first found shift assigned to an employee at the given day-index.
	 * Returns <code>null</code> if no assignment has been found for employee
	 * at the day-index.
	 * 
	 * @throws NullPointerException if the employee is <code>null</code>.
	 * @throws IndexOutOfBoundsException if the day-index is out of bounds.
	 */
	public Shift getAssignment(int dayIndex, Employee employee) {
		LocalDate date = problem.getSchedulingPeriod().getDate(dayIndex);
		for (Assignment assignment: assignments) {
			if (assignment.employee == employee &&
					assignment.isOn(date)) {
				return assignment.getShift();
			}
		}
		return null;
	}

}
