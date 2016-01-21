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
 * This constraint ensures assignment requests.
 * 
 * @author David Meignan
 */
public class AssignmentRequestConstraint implements Constraint {

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
	private AssignmentRequestConstraintEvaluator evaluator = null;
	
	/**
	 * The type of request that are disabled.
	 */
	private ArrayList<RequestType> disableTypes = new ArrayList<RequestType>();
	
	/**
	 * Enable or disable a type of request.
	 * 
	 * @param requestType the type of request to enable or disable.
	 * @param enable <code>true</code> to enable and <code>true</code> to disable
	 * the type of request.
	 */
	public void setEnableRequests(RequestType requestType, boolean enable) {
		if (requestType == null)
			return;
		if (disableTypes.contains(requestType)) {
			if (enable)
				disableTypes.remove(requestType);
		} else {
			if (!enable)
				disableTypes.add(requestType);
		}
		evaluator = null;
	}
	
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
	 * Constructs an assignment-requests constraint.
	 * 
	 * @param active the active property of the constraint.
	 * @param defaultWeightValue the default weight value of the constraint.
	 * 
	 * @throws IllegalArgumentException if the rank-index or default weight
	 * value is negative.
	 */
	public AssignmentRequestConstraint(boolean active,
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
			evaluator = new AssignmentRequestConstraintEvaluator(problem);
		return evaluator;
	}
	
	/**
	 * Evaluator of the constraint.
	 */
	class AssignmentRequestConstraintEvaluator extends
			ConstraintEvaluator {

		/**
		 * List of requests by day-index and employee.
		 */
		ArrayList<ArrayList<ArrayList<AssignmentRequest>>> requestLists;
		
		/**
		 * Creates the evaluator.
		 * 
		 * @param problem the shift scheduling problem.
		 */
		public AssignmentRequestConstraintEvaluator(ShiftSchedulingProblem problem) {
			// List of requests for faster evaluation from indexes
			requestLists = new ArrayList<ArrayList<ArrayList<AssignmentRequest>>>();
			for (int dayIndex=0; dayIndex<problem.schedulingPeriod.size(); dayIndex++) {
				ArrayList<ArrayList<AssignmentRequest>> dayRequestLists = 
						new ArrayList<ArrayList<AssignmentRequest>>();
				requestLists.add(dayRequestLists);
				for (Employee employee: problem.employees) {
					ArrayList<AssignmentRequest> employeeRequestLists =
							new ArrayList<AssignmentRequest>();
					dayRequestLists.add(employeeRequestLists);
					for (AssignmentRequest request: employee.requests) {
						// Add requests for the day index
						if (problem.getSchedulingPeriod().getDayIndex(request.date)
								== dayIndex) {
							if (!disableTypes.contains(request.getType()))
								employeeRequestLists.add(request);
						}
					}
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
			
			int unsatisfiedRequest = 0;
			// Check requests by day and employee
			for (int dayIndex=0; dayIndex<solution.assignments.size(); dayIndex++) {
				for (int employeeIndex=0; employeeIndex<solution.employees.size(); 
						employeeIndex++) {
					unsatisfiedRequest += unsatisfiedRequests(
							solution.assignments.get(dayIndex).get(employeeIndex),
							employeeIndex, dayIndex);
				}
			}
			// Return total cost
			return unsatisfiedRequest*weightValue;
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
			
			int unsatisfied = 0;
			for (AssignmentRequest request: requestLists.get(assignmentDayIndex)
					.get(employeeIndex)) {
				if (request.type == RequestType.DAY_ON_REQUEST) {
					// Negative cost on day-on request
					unsatisfied -= request.priority;
				} else if (request.type == RequestType.DAY_OFF_REQUEST) {
					// Cost of day off
					unsatisfied += request.priority;
				} else if (request.type == RequestType.SHIFT_ON_REQUEST) {
					// check shift-on
					if (shift != request.shift)
						unsatisfied += request.priority;
				} else if (request.type == RequestType.SHIFT_OFF_REQUEST) {
					// check shift-off
					if (shift == request.shift)
						unsatisfied += request.priority;
				}
			}
			return unsatisfied*weightValue;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getSwapMoveCostDifference(de.uos.inf.ischedule.model.Solution, de.uos.inf.ischedule.model.heuristic.SwapMove)
		 */
		@Override
		public int getSwapMoveCostDifference(Solution solution,
				SwapMove swapMove) {
			// Check active state and (global) weight value of the constraint
			if (!active || weightValue <= 0)
				return 0;
			
			int unsatisfiedRequestDifference = 0;
			// Iterate on day-index of block
			for (int dayIndex=swapMove.getStartDayIndex(); 
					dayIndex<swapMove.getStartDayIndex()+swapMove.getBlockSize();
					dayIndex++) {
				int previousUnsatisfied = 0;
				int newUnsatisfied = 0;
				previousUnsatisfied += unsatisfiedRequests(
						solution.assignments.get(dayIndex)
						.get(swapMove.getEmployee1Index()),
						swapMove.getEmployee1Index(), dayIndex);
				previousUnsatisfied += unsatisfiedRequests(
						solution.assignments.get(dayIndex)
						.get(swapMove.getEmployee2Index()),
						swapMove.getEmployee2Index(), dayIndex);
				newUnsatisfied += unsatisfiedRequests(
						solution.assignments.get(dayIndex)
						.get(swapMove.getEmployee2Index()),
						swapMove.getEmployee1Index(), dayIndex);
				newUnsatisfied += unsatisfiedRequests(
						solution.assignments.get(dayIndex)
						.get(swapMove.getEmployee1Index()),
						swapMove.getEmployee2Index(), dayIndex);
				unsatisfiedRequestDifference += newUnsatisfied-previousUnsatisfied;
			}
			return unsatisfiedRequestDifference*weightValue;
		}
		
		/**
		 * Returns the number of unsatisfied request (sum of priority values)
		 * for the given assignment.
		 * 
		 * @param assignment the shift for which requests are evaluated.
		 * @param employeeIndex the employee index.
		 * @param dayIndex the day index.
		 * @return the number of unsatisfied request for the given assignment.
		 */
		private int unsatisfiedRequests(Shift assignment,
				int employeeIndex, int dayIndex) {
			int unsatisfied = 0;
			for (AssignmentRequest request: requestLists.get(dayIndex)
					.get(employeeIndex)) {
				if (request.type == RequestType.DAY_ON_REQUEST) {
					// check day-on
					if (assignment == null)
						unsatisfied += request.priority;
				} else if (request.type == RequestType.DAY_OFF_REQUEST) {
					// check day off
					if (assignment != null)
						unsatisfied += request.priority;
				} else if (request.type == RequestType.SHIFT_ON_REQUEST) {
					// check shift-on
					if (assignment != request.shift)
						unsatisfied += request.priority;
				} else if (request.type == RequestType.SHIFT_OFF_REQUEST) {
					// check shift-off
					if (assignment == request.shift)
						unsatisfied += request.priority;
				}
			}
			return unsatisfied;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getConstraint()
		 */
		@Override
		public Constraint getConstraint() {
			return AssignmentRequestConstraint.this;
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
					for (AssignmentRequest request: requestLists.get(dayIndex)
							.get(employeeIndex)) {
						if (request.priority > 0) {	// Do not count requests with null weight/priority
							if (request.type == RequestType.DAY_ON_REQUEST) {
								// check day-on
								if (assignment == null) {
									ConstraintViolation violation = new ConstraintViolation(
											AssignmentRequestConstraint.this);
									violation.setCost(weightValue*request.priority);
									violation.setMessage(Messages.getString("AssignmentRequestConstraint.unsatisfiedDayOnRequest")); //$NON-NLS-1$
									violation.addAssignmentInScope(
											solution.employees.get(employeeIndex), 
											solution.problem.getSchedulingPeriod().getDate(dayIndex));
									violations.add(violation);
								}
							} else if (request.type == RequestType.DAY_OFF_REQUEST) {
								// check day off
								if (assignment != null) {
									ConstraintViolation violation = new ConstraintViolation(
											AssignmentRequestConstraint.this);
									violation.setCost(weightValue*request.priority);
									violation.setMessage(Messages.getString("AssignmentRequestConstraint.unsatisfiedDayOffRequest")); //$NON-NLS-1$
									violation.addAssignmentInScope(
											solution.employees.get(employeeIndex), 
											solution.problem.getSchedulingPeriod().getDate(dayIndex));
									violations.add(violation);
								}
							} else if (request.type == RequestType.SHIFT_ON_REQUEST) {
								// check shift-on
								if (assignment != request.shift) {
									ConstraintViolation violation = new ConstraintViolation(
											AssignmentRequestConstraint.this);
									violation.setCost(weightValue*request.priority);
									violation.setMessage(Messages.getString("AssignmentRequestConstraint.unsatisfiedShiftOnRequest")); //$NON-NLS-1$
									violation.addAssignmentInScope(
											solution.employees.get(employeeIndex), 
											solution.problem.getSchedulingPeriod().getDate(dayIndex));
									violations.add(violation);
								}
							} else if (request.type == RequestType.SHIFT_OFF_REQUEST) {
								// check shift-off
								if (assignment == request.shift) {
									ConstraintViolation violation = new ConstraintViolation(
											AssignmentRequestConstraint.this);
									violation.setCost(weightValue*request.priority);
									violation.setMessage(Messages.getString("AssignmentRequestConstraint.unsatisfiedShiftOffRequest")); //$NON-NLS-1$
									violation.addAssignmentInScope(
											solution.employees.get(employeeIndex), 
											solution.problem.getSchedulingPeriod().getDate(dayIndex));
									violations.add(violation);
								}
							}
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
			
			// Iterate on day-index of block
			for (int dayIndex=swapMove.getStartDayIndex(); 
					dayIndex<swapMove.getStartDayIndex()+swapMove.getBlockSize();
					dayIndex++) {
				int previousUnsatisfied = 0;
				int newUnsatisfied = 0;
				
				previousUnsatisfied = unsatisfiedRequests(
						solution.assignments.get(dayIndex)
						.get(swapMove.getEmployee1Index()),
						swapMove.getEmployee1Index(), dayIndex);
				newUnsatisfied = unsatisfiedRequests(
						solution.assignments.get(dayIndex)
						.get(swapMove.getEmployee2Index()),
						swapMove.getEmployee1Index(), dayIndex);
				if (previousUnsatisfied > newUnsatisfied) {
					diff[0] += previousUnsatisfied-newUnsatisfied;
				} else if (newUnsatisfied > previousUnsatisfied) {
					diff[1] += newUnsatisfied-previousUnsatisfied;
				}
				
				previousUnsatisfied = unsatisfiedRequests(
						solution.assignments.get(dayIndex)
						.get(swapMove.getEmployee2Index()),
						swapMove.getEmployee2Index(), dayIndex);
				
				newUnsatisfied = unsatisfiedRequests(
						solution.assignments.get(dayIndex)
						.get(swapMove.getEmployee1Index()),
						swapMove.getEmployee2Index(), dayIndex);
				if (previousUnsatisfied > newUnsatisfied) {
					diff[0] += previousUnsatisfied-newUnsatisfied;
				} else if (newUnsatisfied > previousUnsatisfied) {
					diff[1] += newUnsatisfied-previousUnsatisfied;
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
		return Messages.getString("AssignmentRequestConstraint.name"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getConstraintCostLabel()
	 */
	@Override
	public String getCostLabel() {
		return Messages.getString("AssignmentRequestConstraint.costLabel"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription()
	 */
	@Override
	public String getHTMLDescription() {
		return Messages.getString("AssignmentRequestConstraint.description"); //$NON-NLS-1$
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
		String paramDesc = Messages.getString(
				"AssignmentRequestConstraint.descriptionEmployee"); //$NON-NLS-1$
		int dayOffRequests = 0;
		int dayOnRequests = 0;
		int shiftOffRequests = 0;
		int shiftOnRequests = 0;
		for (AssignmentRequest request: employee.requests()) {
			if (request.getType() == RequestType.DAY_OFF_REQUEST)
				dayOffRequests++;
			else if (request.getType() == RequestType.DAY_ON_REQUEST)
				dayOnRequests++;
			else if (request.getType() == RequestType.SHIFT_OFF_REQUEST)
				shiftOffRequests++;
			else if (request.getType() == RequestType.SHIFT_ON_REQUEST)
				shiftOnRequests++;
		}
		paramDesc = paramDesc.replaceAll("\\$1", Integer.toString(dayOffRequests)); //$NON-NLS-1$
		paramDesc = paramDesc.replaceAll("\\$2", Integer.toString(dayOnRequests)); //$NON-NLS-1$
		paramDesc = paramDesc.replaceAll("\\$3", Integer.toString(shiftOffRequests)); //$NON-NLS-1$
		paramDesc = paramDesc.replaceAll("\\$4", Integer.toString(shiftOnRequests)); //$NON-NLS-1$
		return paramDesc;
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#cover(de.uos.inf.ischedule.model.Employee)
	 */
	@Override
	public boolean cover(Employee employee) {
		return (!employee.requests().isEmpty());
	}

}
