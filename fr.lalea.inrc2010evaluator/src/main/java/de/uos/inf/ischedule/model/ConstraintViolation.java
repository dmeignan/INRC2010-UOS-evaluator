/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

import java.util.ArrayList;

import org.joda.time.LocalDate;

import de.uos.inf.ischedule.util.Messages;

/**
 * A <code>ConstraintViolation</code> identifies the position of the
 * violation of a constraint, and provides a local-specific description
 * of it.
 * 
 * @author David Meignan
 */
public class ConstraintViolation {

	/**
	 * The scope of the constraint violation
	 */
	private ArrayList<ConstraintViolationRange> scope;
	
	/**
	 * The constraint
	 */
	private Constraint constraint;
	
	/**
	 * The message indicating the type of constraint violation
	 */
	private String message;
	
	/**
	 * The cost specific to this constraint violation
	 */
	private int cost;
	
	/**
	 * Creates a constraint violation. The initial message is empty,
	 * the initial cost is <code>0</code>, and the initial scope 
	 * is empty.
	 * 
	 * @param constraint the constraint unsatisfied.
	 */
	protected ConstraintViolation(Constraint constraint) {
		this.constraint = constraint;
		this.cost = 0;
		this.message = "";
		this.scope = new ArrayList<ConstraintViolationRange>();
	}
	
	/**
	 * Returns the message of the constraint violation.
	 * 
	 * @return the message of the constraint violation.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Set the message of the constraint violation.
	 * 
	 * @param message the message to set.
	 */
	protected void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Returns the cost related to the constraint violation.
	 * 
	 * @return the cost related to the constraint violation.
	 */
	public int getCost() {
		return cost;
	}

	/**
	 * Set the cost related to the constraint violation.
	 * 
	 * @param cost the cost to set.
	 */
	protected void setCost(int cost) {
		this.cost = cost;
	}

	/**
	 * Returns the constraint unsatisfied.
	 * 
	 * @return the constraint unsatisfied.
	 */
	public Constraint getConstraint() {
		return constraint;
	}

	/**
	 * Adds the given employee in the scope of the constraint violation.
	 * 
	 * @param employee the employee to be included in the scope of the constraint violation.
	 */
	protected void addFullEmployeeInScope(Employee employee) {
		scope.add(new FullEmployeeConstraintViolationRange(employee));
	}
	
	/**
	 * Adds the given day-index in the scope of the constraint violation.
	 * 
	 * @param dayIndex the day-index to be included in the scope of the constraint violation.
	 */
	protected void addFullDayInScope(LocalDate day) {
		scope.add(new FullDayConstraintViolationRange(day));
	}
	
	/**
	 * Adds a specific assignment in the scope of the constraint violation.
	 * 
	 * @param employee the employee of the assignment to be included.
	 * @param dayIndex the day-index of the assignment to be included.
	 */
	protected void addAssignmentInScope(Employee employee, LocalDate day) {
		scope.add(new AssignmentConstraintViolationRange(employee, day));
	}
	
	/**
	 * Adds a range of day for a specific employee in the scope of the
	 * constraint employee.
	 * 
	 * @param employee the employee.
	 * @param startDayIndex the starting day index of the range.
	 * @param endDayIndex the ending day index of the range.
	 */
	protected void addAssignmentRangeInScope(Employee employee, LocalDate startDay,
			LocalDate endDay) {
		scope.add(new DayRangeConstraintViolationRange(employee,
				startDay, endDay));
	}
	
	/**
	 * Returns a textual description of the scope of the constraint violation.
	 * 
	 * @return a textual description of the scope of the constraint violation.
	 */
	public String getConstraintViolationScopeDescription() {
		return scope.toString();
	}
	
	/**
	 * Returns <code>true</code> if the constraint violation concerns the entire day
	 * given in parameter.
	 * 
	 * @param day the day.
	 * @return <code>true</code> if the constraint violation concerns the entire day
	 * given in parameter.
	 */
	public boolean coverFullDay(LocalDate day) {
		for (ConstraintViolationRange cover: scope) {
			if (cover.coverFullDay(day))
				return true;
		}
		return false;
	}
	
	/**
	 * Returns <code>true</code> if the constraint violation concerns the entire employee
	 * given in parameter.
	 * 
	 * @param employee the employee.
	 * @return <code>true</code> if the constraint violation concerns the entire employee
	 * given in parameter.
	 */
	public boolean coverFullEmployee(Employee employee) {
		for (ConstraintViolationRange cover: scope) {
			if (cover.coverFullEmployee(employee))
				return true;
		}
		return false;
	}
	
	/**
	 * Returns <code>true</code> if the constraint violation concerns the assignment specified
	 * in parameter.
	 * 
	 * @param day the day of the assignment.
	 * @param employee the employee of the assignment.
	 * @return <code>true</code> if the constraint violation concerns the assignment specified
	 * in parameter.
	 */
	public boolean coverAssignment(LocalDate day, Employee employee) {
		for (ConstraintViolationRange cover: scope) {
			if (cover.coverAssignment(day, employee))
				return true;
		}
		return false;
	}
	
	/**
	 * A <code>ConstraintViolationRange</code> defines a range on which
	 * the constraint violation apply.
	 */
	abstract class ConstraintViolationRange {
		public abstract boolean coverFullDay(LocalDate day);
		
		public abstract boolean coverFullEmployee(Employee employee);
		
		public abstract boolean coverAssignment(LocalDate day, Employee employee);
	}
	
	class FullEmployeeConstraintViolationRange extends ConstraintViolationRange {
		
		/**
		 * The employee for which the range is defined.
		 */
		Employee employee;
		
		/**
		 * Constructs a range on a full employee.
		 * 
		 * @param employee the employee that defines the range.
		 */
		public FullEmployeeConstraintViolationRange(Employee employee) {
			this.employee = employee;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("employee ");
			builder.append(employee.getName());
			return builder.toString();
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintViolation.ConstraintViolationRange#coverFullDay(org.joda.time.DateTime)
		 */
		@Override
		public boolean coverFullDay(LocalDate day) {
			return false;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintViolation.ConstraintViolationRange#coverFullEmployee(de.uos.inf.ischedule.model.Employee)
		 */
		@Override
		public boolean coverFullEmployee(Employee employee) {
			return this.employee == employee;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintViolation.ConstraintViolationRange#coverAssignment(org.joda.time.DateTime, de.uos.inf.ischedule.model.Employee)
		 */
		@Override
		public boolean coverAssignment(LocalDate day, Employee employee) {
			return this.employee == employee;
		}
		
	}
	
	class FullDayConstraintViolationRange extends ConstraintViolationRange {
		
		/**
		 * The day for which the range is defined.
		 */
		LocalDate day;
		
		/**
		 * Constructs a range on a full day.
		 * 
		 * @param day the day that defines the range.
		 */
		public FullDayConstraintViolationRange(LocalDate day) {
			this.day = day;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("day ");
			builder.append(Messages.getShortDateString(day));
			return builder.toString();
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintViolation.ConstraintViolationRange#coverFullDay(org.joda.time.DateTime)
		 */
		@Override
		public boolean coverFullDay(LocalDate day) {
			if (
					this.day.getYear() == day.getYear() &&
					this.day.getMonthOfYear() == day.getMonthOfYear() &&
					this.day.getDayOfMonth() == day.getDayOfMonth()
					)
				return true;
			return false;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintViolation.ConstraintViolationRange#coverFullEmployee(de.uos.inf.ischedule.model.Employee)
		 */
		@Override
		public boolean coverFullEmployee(Employee employee) {
			return false;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintViolation.ConstraintViolationRange#coverAssignment(org.joda.time.DateTime, de.uos.inf.ischedule.model.Employee)
		 */
		@Override
		public boolean coverAssignment(LocalDate day, Employee employee) {
			if (
					this.day.getYear() == day.getYear() &&
					this.day.getMonthOfYear() == day.getMonthOfYear() &&
					this.day.getDayOfMonth() == day.getDayOfMonth()
					)
				return true;
			return false;
		}
		
	}
	
	class AssignmentConstraintViolationRange extends ConstraintViolationRange {
		
		/**
		 * The employee of the assignment for which the range is defined.
		 */
		Employee employee;
		
		/**
		 * The day of the assignment for which the range is defined.
		 */
		LocalDate day;
		
		/**
		 * Constructs a range for a specific assignment.
		 * 
		 * @param employee the employee of the assignment.
		 * @param day the day of the assignment.
		 */
		public AssignmentConstraintViolationRange(Employee employee,
				LocalDate day) {
			this.employee = employee;
			this.day = day;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("employee ");
			builder.append(employee.getName());
			builder.append(", on ");
			builder.append(Messages.getShortDateString(day));
			return builder.toString();
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintViolation.ConstraintViolationRange#coverFullDay(org.joda.time.DateTime)
		 */
		@Override
		public boolean coverFullDay(LocalDate day) {
			return false;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintViolation.ConstraintViolationRange#coverFullEmployee(de.uos.inf.ischedule.model.Employee)
		 */
		@Override
		public boolean coverFullEmployee(Employee employee) {
			return false;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintViolation.ConstraintViolationRange#coverAssignment(org.joda.time.DateTime, de.uos.inf.ischedule.model.Employee)
		 */
		@Override
		public boolean coverAssignment(LocalDate day, Employee employee) {
			if (
					this.employee == employee &&
					this.day.getYear() == day.getYear() &&
					this.day.getMonthOfYear() == day.getMonthOfYear() &&
					this.day.getDayOfMonth() == day.getDayOfMonth()
					)
				return true;
			return false;
		}
	}
	
	class DayRangeConstraintViolationRange extends ConstraintViolationRange {
		
		/**
		 * The employee for which the range is defined.
		 */
		Employee employee;
		
		/**
		 * The day on which the range starts.
		 */
		LocalDate startDay;
		
		/**
		 * The day on which the range ends.
		 */
		LocalDate endDay;
		
		/**
		 * Constructs a range for a specific assignment.
		 * 
		 * @param employee the employee of the assignment.
		 * @param dayIndex the day-index of the assignment.
		 */
		public DayRangeConstraintViolationRange(Employee employee,
				LocalDate startDay, LocalDate endDay) {
			this.employee = employee;
			this.startDay = startDay;
			this.endDay = endDay;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("employee ");
			builder.append(employee.getName());
			builder.append(" from ");
			builder.append(Messages.getShortDateString(startDay));
			builder.append(" to ");
			builder.append(Messages.getShortDateString(endDay));
			return builder.toString();
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintViolation.ConstraintViolationRange#coverFullDay(org.joda.time.DateTime)
		 */
		@Override
		public boolean coverFullDay(LocalDate day) {
			return false;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintViolation.ConstraintViolationRange#coverFullEmployee(de.uos.inf.ischedule.model.Employee)
		 */
		@Override
		public boolean coverFullEmployee(Employee employee) {
			return false;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintViolation.ConstraintViolationRange#coverAssignment(org.joda.time.DateTime, de.uos.inf.ischedule.model.Employee)
		 */
		@Override
		public boolean coverAssignment(LocalDate day, Employee employee) {
			if (employee == this.employee) {
				if (day.isEqual(endDay) || day.isEqual(startDay) ||
						(day.isAfter(startDay) && day.isBefore(endDay))
						) {
					return true;
				}
			}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getMessage());
		builder.append(", cost: ");
		builder.append(Integer.toString(this.getCost()));
		builder.append(", scope: ");
		builder.append(this.getConstraintViolationScopeDescription());
		return builder.toString();
	}
	
	/**
	 * Returns a full description of the constraint violation using HTML
	 * formatting, but without <code>\<HTML\></code> tag.
	 * 
	 * TODO Adjust description formatting
	 * 
	 * @return a full description of the constraint violation using HTML
	 * formatting.
	 */
	public String getFullHtmlDescription() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getMessage());
		builder.append("<FONT size=-2>");
		builder.append("<BR>cost: ");
		builder.append(Integer.toString(this.getCost()));
		builder.append("</FONT>");
//		builder.append("<BR>scope: ");
//		builder.append(this.getConstraintViolationScopeDescription());
		return builder.toString();
	}

}
