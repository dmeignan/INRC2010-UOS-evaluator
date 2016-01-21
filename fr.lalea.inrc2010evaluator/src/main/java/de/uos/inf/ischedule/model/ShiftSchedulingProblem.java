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
 * The <code>ShiftSchedulingProblem</code> class represents a shift-scheduling problem instance.
 * A <code>ShiftSchedulingProblem</code> object contains all data required to define a
 * shift scheduling problem instance.
 * 
 * @author David Meignan
 */
public class ShiftSchedulingProblem {
	
	/**
	 * ID of the problem instance.
	 */
	protected String id;
	
	/**
	 * Label of the problem instance to display.
	 */
	protected String description;
	
	/**
	 * Period for which a schedule have to be found.
	 */
	protected Period schedulingPeriod;
	
	/**
	 * Existing schedule for the scheduling period, in case of
	 * re-scheduling (optional).
	 */
	protected Schedule existingSchedule = null;
	
	/**
	 * Schedule preceding the scheduling period (optional).
	 */
	protected Schedule precedingSchedule = null;
	
	/**
	 * Set of demands (required number of employees) defined on days of week.
	 */
	protected ArrayList<DayOfWeekDemand> dayDemands = new ArrayList<DayOfWeekDemand>();
	
	/**
	 * Set of demands (required number of employees) for specific dates.
	 */
	protected ArrayList<DateDemand> dateDemands = new ArrayList<DateDemand>();
	
	/**
	 * Set of shift types.
	 */
	protected ArrayList<ShiftType> shiftTypes = new ArrayList<ShiftType>();
	
	/**
	 * Set of shifts.
	 */
	protected ArrayList<Shift> shifts = new ArrayList<Shift>();
	
	/**
	 * Set of skills.
	 */
	protected ArrayList<Skill> skills = new ArrayList<Skill>();
	
	/**
	 * Set of employees.
	 */
	protected ArrayList<Employee> employees = new ArrayList<Employee>();
	
	/**
	 * Set of contracts for employees.
	 */
	protected ArrayList<Contract> contracts = new ArrayList<Contract>();
	
	/**
	 * Set of constraints and parameters of these constraints.
	 * Constraints are grouped by rank.
	 */
	private ArrayList<ArrayList<Constraint>> constraints =
			new ArrayList<ArrayList<Constraint>>();
	
	/**
	 * Constructs a shift scheduling problem.
	 * 
	 * @param id the ID of the problem.
	 * @param description the label of the problem.
	 * @param schedulingPeriod the period to be scheduled.
	 * 
	 * @throws IllegalArgumentException if the ID, the description or the scheduling period
	 * is <code>null</code>.
	 */
	public ShiftSchedulingProblem(String id, String description,
			Period schedulingPeriod) {
		this.id = id;
		this.description = description;
		this.schedulingPeriod = schedulingPeriod;
	}

	/**
	 * Returns the ID of the problem.
	 * 
	 * @return the ID of the problem.
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Returns a collection view of skills.
	 * 
	 * @return a collection view of skills.
	 */
	public List<Skill> skills() {
		return new SkillCollection();
	}
	
	/**
	 * Returns a collection view of shift-types.
	 * 
	 * @return a collection view of shift-types.
	 */
	public List<ShiftType> shiftTypes() {
		return new ShiftTypeCollection();
	}
	
	/**
	 * Returns a collection view of shifts.
	 * 
	 * @return a collection view of shifts.
	 */
	public List<Shift> shifts() {
		return new ShiftCollection();
	}
	
	/**
	 * Returns a collection view of contracts.
	 * 
	 * @return a collection view of contracts.
	 */
	public List<Contract> contracts() {
		return new ContractCollection();
	}
	
	/**
	 * Returns a collection view of employees.
	 * 
	 * @return a collection view of employees.
	 */
	public List<Employee> employees() {
		return new EmployeeCollection();
	}
	
	/**
	 * Returns a collection view of day-of-the-week demands.
	 * 
	 * @return a collection view of day-of-the-week demands.
	 */
	public List<DayOfWeekDemand> dayOfWeekDemands() {
		return new DayOfWeekDemandCollection();
	}
	
	/**
	 * Returns a collection view of date demands.
	 * 
	 * @return a collection view of date demands.
	 */
	public List<DateDemand> dateDemands() {
		return new DateDemandCollection();
	}
	
	/**
	 * Returns a collection view of constraints of a given rank.
	 * 
	 * @param rankIndex the rank index of constraints.
	 * @return a collection view of constraints.
	 */
	public List<Constraint> constraints(int rankIndex) {
		return new ConstraintCollection(rankIndex);
	}
	
	/**
	 * Returns a collection view of constraints of a specific type (i.e. implementation).
	 * Only constraints of the specified class are contained in the returned
	 * collection.
	 * 
	 * @param constraintClass the class of constraints to be considered.
	 * @return a collection view of constraints with a specific implementation.
	 */
	public <C extends Constraint> List<C> constraints(
			Class<C> constraintClass) {
		return new ConstraintOfTypeCollection<C>(constraintClass);
	}
	
	/**
	 * Returns the maximum rank-index value of the constraints. Returns <code>-1</code> if no
	 * constraints has been defined.
	 * 
	 * @return  the maximum rank-index value.
	 */
	public int getMaxConstraintsRankIndex() {
		return constraints.size()-1;	
	}
	
	/**
	 * Sets the rank index of a constraint. Returns <code>true</code> if the rank index
	 * of the constraint has been modified, <code>false</code> if the rank index is
	 * unchanged or the constraint is not in the list of constraints.
	 * 
	 * @param constraint the constraint for which the rank has to be modified.
	 * @param rankIndex the new rank index of the constraint.
	 * @return  <code>true</code> if the rank index
	 * of the constraint has been modified, <code>false</code> if the rank index is
	 * unchanged or the constraint is not in the list of constraints.
	 * @throws IllegalArgumentException if the rank index is negative or the constraint
	 * is <code>null</code>.
	 */
	public boolean setConstraintRankIndex(Constraint constraint, int rankIndex) {
		if (rankIndex < 0 || constraint == null)
			throw new IllegalArgumentException();
		// Search the constraint
		for (int previousRankIdx=0; previousRankIdx<constraints.size();
				previousRankIdx++) {
			if (constraints.get(previousRankIdx).contains(constraint)) {
				if (previousRankIdx == rankIndex)
					return false;
				constraints.get(previousRankIdx).remove(constraint);
				constraints(rankIndex).add(constraint);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the rank-index of a constraints. Returns <code>-1</code> if the
	 * constraint is not included in the list of the constraints.
	 * 
	 * @param constraint the constraint for which the rank index has to be returned.
	 * @return the rank-index of a constraints. Returns <code>-1</code> if the
	 * constraint is not included in the list of the constraints.
	 */
	public int getConstraintRankIndex(Constraint constraint) {
		if (constraint == null)
			return -1;
		for (int rankIndex = 0; rankIndex < constraints.size(); rankIndex++) {
			if (constraints.get(rankIndex).contains(constraint))
				return rankIndex;
		}
		return -1;
	}
	
	/**
	 * Collection view of skills. This custom implementation controls modification
	 * operations.
	 */
	private class SkillCollection extends AbstractList<Skill> {

		/* (non-Javadoc)
		 * @see java.util.AbstractList#get(int)
		 */
		@Override
		public Skill get(int idx) {
			return skills.get(idx);
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size() {
			return skills.size();
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractList#add(int, java.lang.Object)
		 */
		@Override
		public void add(int index, Skill element) {
			skills.add(index, element);
		}

	}
	
	/**
	 * Collection view of shift-types. This custom implementation controls modification
	 * operations.
	 */
	private class ShiftTypeCollection extends AbstractList<ShiftType> {

		/* (non-Javadoc)
		 * @see java.util.AbstractList#get(int)
		 */
		@Override
		public ShiftType get(int index) {
			return shiftTypes.get(index);
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size() {
			return shiftTypes.size();
		}
		
		/* (non-Javadoc)
		 * @see java.util.AbstractList#add(int, java.lang.Object)
		 */
		@Override
		public void add(int index, ShiftType element) {
			shiftTypes.add(index, element);
		}
	}
	
	/**
	 * Collection view of shifts. This custom implementation controls modification
	 * operations.
	 */
	private class ShiftCollection extends AbstractList<Shift> {

		/* (non-Javadoc)
		 * @see java.util.AbstractList#get(int)
		 */
		@Override
		public Shift get(int index) {
			return shifts.get(index);
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size() {
			return shifts.size();
		}
		
		/* (non-Javadoc)
		 * @see java.util.AbstractList#add(int, java.lang.Object)
		 */
		@Override
		public void add(int index, Shift element) {
			shifts.add(index, element);
		}
	}
	
	/**
	 * Collection view of contract. This custom implementation controls modification
	 * operations.
	 */
	private class ContractCollection extends AbstractList<Contract> {

		/* (non-Javadoc)
		 * @see java.util.AbstractList#get(int)
		 */
		@Override
		public Contract get(int idx) {
			return contracts.get(idx);
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size() {
			return contracts.size();
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractList#add(int, java.lang.Object)
		 */
		@Override
		public void add(int index, Contract element) {
			contracts.add(index, element);
		}
	}
	
	/**
	 * Collection view of employee. This custom implementation controls modification
	 * operations.
	 */
	private class EmployeeCollection extends AbstractList<Employee> {

		/* (non-Javadoc)
		 * @see java.util.AbstractList#get(int)
		 */
		@Override
		public Employee get(int idx) {
			return employees.get(idx);
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size() {
			return employees.size();
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractList#add(int, java.lang.Object)
		 */
		@Override
		public void add(int index, Employee element) {
			employees.add(index, element);
		}
	}
	
	/**
	 * Collection view of day-of-week demands. This custom implementation controls modification
	 * operations.
	 */
	private class DayOfWeekDemandCollection extends AbstractList<DayOfWeekDemand> {

		/* (non-Javadoc)
		 * @see java.util.AbstractList#get(int)
		 */
		@Override
		public DayOfWeekDemand get(int idx) {
			return dayDemands.get(idx);
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size() {
			return dayDemands.size();
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractList#add(int, java.lang.Object)
		 */
		@Override
		public void add(int index, DayOfWeekDemand element) {
			// Check existing demand for duplicate
			for (DayOfWeekDemand existingDemand: dayDemands) {
				if (existingDemand.shift == element.shift &&
						existingDemand.dayOfWeek == element.dayOfWeek)
					throw new IllegalArgumentException();
			}
			dayDemands.add(index, element);
		}
	}
	
	/**
	 * Collection view of date demands. This custom implementation controls modification
	 * operations.
	 */
	private class DateDemandCollection extends AbstractList<DateDemand> {

		/* (non-Javadoc)
		 * @see java.util.AbstractList#get(int)
		 */
		@Override
		public DateDemand get(int idx) {
			return dateDemands.get(idx);
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size() {
			return dateDemands.size();
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractList#add(int, java.lang.Object)
		 */
		@Override
		public void add(int index, DateDemand element) {
			// Check existing demand for duplicate
			for (DateDemand existingDemand: dateDemands) {
				if (existingDemand.shift == element.shift &&
						existingDemand.isOn(element.date))
					throw new IllegalArgumentException();
			}
			dateDemands.add(index, element);
		}
	}
	
	/**
	 * Collection view of constraints. This custom implementation controls modification
	 * operations.
	 * TODO manage removal operations so unnecessary ranks are removed.
	 */
	private class ConstraintCollection extends AbstractList<Constraint> {

		/**
		 * The list of constraints of the view.
		 */
		protected ArrayList<Constraint> constraintView;
		
		/**
		 * The rank index of the view.
		 */
		protected int rankIndex;
		
		/**
		 * Creates a collection view of constraints of a given rank.
		 * 
		 * @param rankIndex the rank index of constraints.
		 */
		public ConstraintCollection(int rankIndex) {
			if (rankIndex >= constraints.size()) {
				constraintView = null;
			} else {
				constraintView = constraints.get(rankIndex);				
			}
			this.rankIndex = rankIndex;
		}
		
		/* (non-Javadoc)
		 * @see java.util.AbstractList#get(int)
		 */
		@Override
		public Constraint get(int idx) {
			return constraintView.get(idx);
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size() {
			if (constraintView == null)
				return 0;
			return constraintView.size();
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractList#add(int, java.lang.Object)
		 */
		@Override
		public void add(int index, Constraint element) {
			if (element == null)
				throw new NullPointerException();
			if (constraintView == null) {
				// Create a new rank of constraints
				constraintView = new ArrayList<Constraint>();
				while(constraints.size() < rankIndex) {
					constraints.add(new ArrayList<Constraint>());
				}
				constraints.add(constraintView);
			}
			constraintView.add(index, element);
		}
	}
	

	/**
	 * Collection view of constraints of a specific type (i.e. implementation). This 
	 * custom implementation of the collection controls modification
	 * operations.
	 */
	private class ConstraintOfTypeCollection<C extends Constraint> extends 
			AbstractList<C> {

		protected ArrayList<C> constraintView;
		
		/**
		 * Construct the collection view from the class of constraint to select.
		 * 
		 * @param constraintClass the class of constraint to select in the view.
		 */
		@SuppressWarnings("unchecked")
		public ConstraintOfTypeCollection(Class<C> constraintClass) {
			constraintView = new ArrayList<C>();
			// Select constraints
			for (int rankIndex=0; rankIndex<constraints.size(); rankIndex++) {
				for (Constraint constraint: constraints.get(rankIndex)) {
					if (constraintClass.isInstance(constraint)) {
						constraintView.add((C) constraint);
					}
				}
			}
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractList#get(int)
		 */
		@Override
		public C get(int index) {
			return constraintView.get(index);
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size() {
			return constraintView.size();
		}
		
	}
	
	/**
	 * Returns a shift-type from its ID.
	 * 
	 * @param shiftTypeId the ID of the shift-type to be returned.
	 * @return the shift-type with the corresponding ID or <code>null</code>
	 * if no shift-type have the ID.
	 */
	public ShiftType getShiftType(String shiftTypeId) {
		for (ShiftType shiftType: shiftTypes) {
			if (shiftType.id.compareTo(shiftTypeId) == 0) {
				return shiftType;
			}
		}
		return null;
	}

	/**
	 * Returns a skill from its ID.
	 * 
	 * @param skillId the ID of the skill to be returned.
	 * @return the skill with the corresponding ID or <code>null</code>
	 * if no skill have the ID.
	 */
	public Skill getSkill(String skillId) {
		for (Skill skill: skills) {
			if (skill.id.compareTo(skillId) == 0) {
				return skill;
			}
		}
		return null;
	}

	/**
	 * Returns a contract from its ID.
	 * 
	 * @param contractId the ID of the contract to be returned.
	 * @return the contract with the corresponding ID or <code>null</code>
	 * if no contract have the ID.
	 */
	public Contract getContract(String contractId) {
		for (Contract contract: contracts) {
			if (contract.id.compareTo(contractId) == 0) {
				return contract;
			}
		}
		return null;
	}

	/**
	 * Returns a shift from its ID.
	 * 
	 * @param shiftId the ID of the shift to be returned.
	 * 
	 * @return the shift with the corresponding ID or <code>null</code>
	 * if no shift have the ID.
	 */
	public Shift getShift(String shiftId) {
		for (Shift shift: shifts) {
			if (shift.id.compareTo(shiftId) == 0) {
				return shift;
			}
		}
		return null;
	}

	/**
	 * Returns a employee from its ID.
	 * 
	 * @param employeeId the ID of the employee to be returned.
	 * 
	 * @return the employee with the corresponding ID or <code>null</code>
	 * if no employee have the ID.
	 */
	public Employee getEmployee(String employeeId) {
		for (Employee employee: employees) {
			if (employee.id.compareTo(employeeId) == 0) {
				return employee;
			}
		}
		return null;
	}
	

	/**
	 * Returns the scheduling period.
	 * 
	 * @return the scheduling period.
	 */
	public Period getSchedulingPeriod() {
		return schedulingPeriod;
	}
	
	/**
	 * Returns the demand for a shift at a given date of the scheduling period.
	 * 
	 * @param shift the shift for which the demand is returned.
	 * @param dayIndex the day-index of the date in the planning horizon.
	 * @return the demand for a shift at a given date of the scheduling period.
	 * @throws IllegalArgumentException if the shift is <code>null</code> or the
	 * day-index is out-of-bounds.
	 */
	public int getDemand(Shift shift, int dayIndex) {
		return getDemand(shift, schedulingPeriod.getDate(dayIndex));
	}
	
	/**
	 * Returns the demand for a shift at a given date of the scheduling period.
	 * 
	 * @param shift the shift for which the demand is returned.
	 * @param date the date.
	 * @return the demand for a shift at a given date of the scheduling period.
	 * @throws IllegalArgumentException if the shift is <code>null</code> or
	 * date is out of the scheduling period.
	 */
	public int getDemand(Shift shift, LocalDate date) {
		if (shift == null)
			throw new IllegalArgumentException();
		if (!schedulingPeriod.contains(date))
			throw new IllegalArgumentException();
		int demand = 0;
		// Day of week demand
		for (DayOfWeekDemand dayDemand: dayDemands) {
			if (dayDemand.shift == shift &&
					dayDemand.dayOfWeek == date.getDayOfWeek())
				demand = dayDemand.demand;
		}
		// Date specific demand (overwrite day-of-week demand)
		for (DateDemand dateDemand: dateDemands) {
			if (dateDemand.shift == shift &&
					dateDemand.isOn(date))
				demand = dateDemand.demand;
		}
		return demand;
	}
	
	/**
	 * Returns the total number of required employees for a given day index.
	 * 
	 * @param dayIndex the day index for which the total demand is returned.
	 * 
	 * @return  the total number of required employees for a given day index.
	 * 
	 * @throws IndexOutOfBoundsException if the day index is out of range.
	 */
	public int getTotalDemand(int dayIndex) {
		int demand = 0;
		for (Shift shift: shifts) {
			demand += getDemand(shift, dayIndex);
		}
		return demand;
	}
	
	/**
	 * Returns the index of the day-of-week from a day index in the planning horizon.
	 * Values of ISO8601 constants for day-of-weeks are used. They are defined in 
	 * <code>org.joda.time.DateTimeConstants</code>.
	 * 
	 * @param dayIndex the day index in the planning horizon.
	 * @return the index of the day-of-week corresponding to the day index.
	 */
	public int toDayOfWeek(int dayIndex) {
		return schedulingPeriod.getDate(dayIndex).getDayOfWeek();
	}

}
