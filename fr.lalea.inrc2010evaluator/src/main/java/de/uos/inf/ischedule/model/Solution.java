/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * A <code>Solution</code> is a schedule that can be evaluated. 
 * In a solution an employee can only be assigned to one shift per day. 
 * This constraint simplify the representation of a solution. The set of 
 * assignments are represented as a matrix (Date, Employee). Days off or 
 * empty-assignments correspond to <code>null</code> values in the matrix.
 * Note that a solution is not updated when the problem changes. If the problem changes
 * after instantiating a solution, it may result in inconsistencies in the solution.
 * 
 * @author David Meignan
 */
public class Solution {

	/**
	 * Shift scheduling problem instance.
	 */
	public ShiftSchedulingProblem problem;
	
	/**
	 * Set of assignments grouped by day.
	 * The first dimension represents the planning horizon the second one is
	 * the employees. The indexes of employees is given by the list <code>employees</code>
	 * in this class (and not the list of employees in the problem).
	 * This parameter is <code>public</code> for heuristics and constraints.
	 */
	public ArrayList<ArrayList<Shift>> assignments;
	
	/**
	 * Set of unassigned shift-slots per day.
	 * The first dimension is the set of days.
	 * This parameter is <code>public</code> for heuristics and constraints.
	 */
	public ArrayList<ArrayList<Shift>> unassignedSlots;
	
	/**
	 * List of employees for the assignments.
	 * This parameter is <code>public</code> for heuristics and constraints
	 * and should be shared by different solutions.
	 */
	public ArrayList<Employee> employees;
	
	/**
	 * Evaluation of the solution.
	 * These parameters are <code>public</code> for direct modification
	 * by heuristics.
	 */
	public SolutionEvaluation evaluation;
	public boolean evaluated = false;
	public ArrayList<ConstraintViolation> constraintViolations;
	
	/**
	 * Creates a empty solution.
	 * A solution should be created from a schedule, another solution or a heuristic.
	 * Note that it is better to used the constructor with a solution in parameter
	 * to avoid creating multiple lists of evaluators.
	 * 
	 * @param problem the shift scheduling problem instance.
	 * 
	 * @throws IllegalArgumentException if the problem is <code>null</code> or
	 * inadequate for a solution.
	 */
	public Solution(ShiftSchedulingProblem problem) {
		if (problem == null)
			throw new IllegalArgumentException();
		if (problem.employees.size() == 0)
			throw new IllegalArgumentException();
		if (problem.getMaxConstraintsRankIndex() == -1)
			throw new IllegalArgumentException();
		
		// Problem instance
		this.problem = problem;
		
		// List of employees
		employees = new ArrayList<Employee>(problem.employees);
		
		// Assignments
		int planningSize = problem.schedulingPeriod.size();
		assignments = new ArrayList<ArrayList<Shift>>(planningSize);
		for (int dayIndex=0; dayIndex<planningSize; dayIndex++) {
			ArrayList<Shift> dayAssignment = new ArrayList<Shift>(employees.size());
			for (int employeeIndex=0; employeeIndex<employees.size(); employeeIndex++) {
				dayAssignment.add(null);
			}
			assignments.add(dayAssignment);
		}
		
		// Unassigned slots
		unassignedSlots = new ArrayList<ArrayList<Shift>>(planningSize);
		for (int dayIndex=0; dayIndex<planningSize; dayIndex++) {
			ArrayList<Shift> daySlot = getShiftSlots(problem, dayIndex);
			unassignedSlots.add(daySlot);
		}
		
		// Evaluation
		evaluation = null;
		evaluated = false;
		constraintViolations = null;
	}
	
	/**
	 * Creates an empty solution or a copy of the solution given in parameter.
	 * A solution should be created from a schedule, another solution or a 
	 * heuristic. If the parameter <code>copy</code> is <code>true</code>, 
	 * the solution in parameter will be copied, otherwise only evaluators 
	 * and list of employees will be reused.
	 * 
	 * @param s the solution to be copied, or used for reference to the list of employees
	 * and evaluators.
	 * @param copy <code>true</code> to copy the assignments of the solution given
	 * in parameter, <code>false</code> to only reuse the list of employees and evaluators.
	 * @throws IllegalArgumentException if the solution in parameter is 
	 * <code>null</code>.
	 */
	public Solution(Solution s, boolean copy) {
		if (s == null)
			throw new IllegalArgumentException();
		// Problem instance
		this.problem = s.problem;
		
		// List of employees
		this.employees = s.employees;
		
		// Assignments
		int planningSize = problem.schedulingPeriod.size();
		assignments = new ArrayList<ArrayList<Shift>>(planningSize);
		for (int dayIndex=0; dayIndex<planningSize; dayIndex++) {
			ArrayList<Shift> dayAssignment = new ArrayList<Shift>(employees.size());
			for (int employeeIndex=0; employeeIndex<employees.size(); employeeIndex++) {
				if (!copy)
					dayAssignment.add(null);
				else
					dayAssignment.add(
							s.assignments.get(dayIndex).get(employeeIndex));
			}
			assignments.add(dayAssignment);
		}
		
		// Unassigned slots
		unassignedSlots = new ArrayList<ArrayList<Shift>>(planningSize);
		for (int dayIndex=0; dayIndex<planningSize; dayIndex++) {
			if (!copy) {
				ArrayList<Shift> daySlots = getShiftSlots(problem, dayIndex);
				unassignedSlots.add(daySlots);
			} else {
				ArrayList<Shift> daySlots = new ArrayList<Shift>(
						s.unassignedSlots.get(dayIndex));
				unassignedSlots.add(daySlots);
			}
		}
		
		// Evaluation
		this.evaluation = s.evaluation;
		this.evaluated = s.evaluated;
		this.constraintViolations = s.constraintViolations;
	}

	/**
	 * Returns the list of shift slots at a given date.
	 * 
	 * @param problem the shift scheduling problem.
	 * @param dayIndex the day's index of the date.
	 * @return the list of shift slots at a given date.
	 */
	protected static ArrayList<Shift> getShiftSlots(ShiftSchedulingProblem problem,
			int dayIndex) {
		ArrayList<Shift> slots = new ArrayList<Shift>();
		for (Shift shift: problem.shifts) {
			int demand = problem.getDemand(shift, dayIndex);
			for (int s=0; s<demand; s++) {
				slots.add(shift);
			}
		}
		return slots;
	}
	
	/**
	 * Creates and returns a schedule from this solution.
	 * 
	 * @param id the id of the schedule to be created.
	 * @param description the description of the schedule to be created.
	 * @return the schedule represented by this solution.
	 * @throws IllegalArgumentException if the solution is not consistent with the
	 * shift scheduling problem instance, or if the ID or the description is 
	 * <code>null</code>.
	 */
	public Schedule toSchedule(String id, String description) {
		if (!isProblemConsistent())
			throw new IllegalArgumentException();
		if (id == null || description == null)
			throw new IllegalArgumentException();
		Schedule schedule = new Schedule(
				id, description,
				problem,
				problem.schedulingPeriod
				);
		// Add assignments
		for (int employeeIndex=0; employeeIndex<employees.size(); employeeIndex++) {
			for (int dayIndex=0; dayIndex<assignments.size(); dayIndex++) {
				if (assignments.get(dayIndex).get(employeeIndex) != null) {
					schedule.assignments().add(new Assignment(
							employees.get(employeeIndex),
							assignments.get(dayIndex).get(employeeIndex),
							problem.schedulingPeriod.getDate(dayIndex)
							));
				}
			}
		}
		return schedule;
	}
	
	/**
	 * Checks that the schedule is consistent with the problem instance.
	 * Returns <code>true</code> if the schedule is consistent, <code>false</code>
	 * otherwise. This method only checks existence of employees and shift, as well
	 * as the size of the planning horizon, to ensure the conversion to a schedule.
	 * 
	 * @return <code>true</code> if the schedule is consistent, <code>false</code>
	 * otherwise.
	 */
	public boolean isProblemConsistent() {
		// Check planning horizon
		if (problem.schedulingPeriod.size() != assignments.size())
			return false;
		// Check assignment
		for (int employeeIndex=0; employeeIndex<employees.size(); employeeIndex++) {
			boolean employeeCheck = false; // Only check employee when assigned
			for (int dayIndex=0; dayIndex<assignments.size(); dayIndex++) {
				Shift shift = assignments.get(dayIndex).get(employeeIndex);
				if (shift != null) {
					if (!problem.shifts.contains(shift))
						return false;
					if (!employeeCheck) {
						employeeCheck = true;
						if (!problem.employees.contains(employees.get(employeeIndex)))
							return false;
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * Returns the evaluation of the solution.
	 * 
	 * @return the evaluation of the solution.
	 */
	public synchronized SolutionEvaluation getEvaluation() {
		if (evaluated)
			return evaluation;
		evaluateSolution();
		return evaluation;
	}

	/**
	 * Evaluates or re-evaluate the solution.
	 */
	private void evaluateSolution() {
		int[] rValues = new int[problem.getMaxConstraintsRankIndex()+1];
		Arrays.fill(rValues, 0);
		for (int rankIndex=0; rankIndex<rValues.length; rankIndex++) {
			for (Constraint constraint: problem.constraints(rankIndex)) {
				rValues[rankIndex] +=
						constraint.getEvaluator(problem).getCost(this);
			}
		}
		evaluated = true;
		evaluation = new SolutionEvaluation(rValues);
	}
	
	/**
	 * Invalidates the evaluation of the solution.
	 */
	public synchronized void invalidateEvaluation() {
		evaluation = null;
		evaluated = false;
		constraintViolations = null;
	}
	

	/**
	 * Returns the number of working days of an employee.
	 * 
	 * @param employeeIndex the employee's index.
	 * @return the number of working days of the employee.
	 */
	public int workingDays(int employeeIndex) {
		int workingDays = 0;
		for (int dayIndex=0; dayIndex<assignments.size(); dayIndex++) {
			if (assignments.get(dayIndex).get(employeeIndex) != null) {
				workingDays++;
			}
		}
		return workingDays;
	}

	/**
	 * Returns the distance (Hamming distance between assignments) between
	 * this solution and the solution given in parameter.
	 * 
	 * @param other the solution compared.
	 * @return the Hamming distance between assignments.
	 * @throws IllegalArgumentException if the solution given in parameter
	 * is not comparable with this solution.
	 */
	public int distanceTo(Solution other) {
		if (other == null)
			throw new IllegalArgumentException();
		int distance = 0;
		for (int dayIndex=0; dayIndex<assignments.size(); dayIndex++) {
			for (int employeeIndex=0; employeeIndex<employees.size(); employeeIndex++) {
				if (assignments.get(dayIndex).get(employeeIndex) !=
						other.assignments.get(dayIndex).get(employeeIndex))
					distance++;
			}
		}
		return distance;
	}
	
	/**
	 * Tests if the solution in parameter has the same assignment than the solution.
	 * Returns <code>true</code> if the assignments are the same, <code>false</code>
	 * otherwise.
	 * 
	 * @param other the solution to compare.
	 * @return <code>true</code> if the assignments are the same, <code>false</code>
	 * otherwise.
	 * @throws NullPointerException if the solution passed in parameter is <code>null</code>.
	 */
	public boolean equalAssignments(Solution other) {
		for (int dayIndex=0; dayIndex<assignments.size(); dayIndex++) {
			for (int employeeIndex=0; employeeIndex<employees.size(); employeeIndex++) {
				if (assignments.get(dayIndex).get(employeeIndex) !=
						other.assignments.get(dayIndex).get(employeeIndex))
					return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns the complete list of constraint violations for the solution.
	 * Returns an empty list if all constraints are satisfied.
	 * 
	 * @return the complete list of constraint violations for the solution.
	 */
	public synchronized ArrayList<ConstraintViolation> getConstraintViolations() {
		if (constraintViolations == null) {
			constraintViolations = new ArrayList<ConstraintViolation>();
			for (int rankIndex=0; rankIndex<=problem.getMaxConstraintsRankIndex(); rankIndex++) {
				for (Constraint constraint: problem.constraints(rankIndex)) {
					constraintViolations.addAll(
							constraint.getEvaluator(problem).getConstraintViolations(this));
				}
			}
		}
		return constraintViolations;
	}
	
}
