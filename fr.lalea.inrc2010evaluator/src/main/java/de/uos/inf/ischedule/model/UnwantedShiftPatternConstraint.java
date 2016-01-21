/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import de.uos.inf.ischedule.model.heuristic.SwapMove;
import de.uos.inf.ischedule.util.IconUtils;
import de.uos.inf.ischedule.util.Messages;

/**
 * This constraint defines an unwanted shift pattern.
 * 
 * @author David Meignan
 */
public class UnwantedShiftPatternConstraint implements Constraint {

	/**
	 * Unwanted shift pattern.
	 */
	protected ShiftPattern unwantedPattern;

	/**
	 * Contracts for which the constraint applies.
	 */
	protected ArrayList<Contract> scope = new ArrayList<Contract>();
	
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
	private UnwantedShiftPatternConstraintEvaluator evaluator = null;
	
	/**
	 * Constructs an unwanted shift pattern constraint.
	 * 
	 * @param unwantedPattern the unwanted shift pattern.
	 * @param active the active property of the constraint.
	 * @param rankIndex the rank index of the constraint.
	 * @param weightValue the weight value of the constraint.
	 * 
	 * @throws IllegalArgumentException if the unwanted shift pattern is <code>null</code>,
	 * or rank-index is negative, or weight value is negative.
	 */
	public UnwantedShiftPatternConstraint(ShiftPattern unwantedPattern,
			boolean active, int weightValue) {
		if (unwantedPattern == null)
			throw new IllegalArgumentException();
		if (weightValue < 0)
			throw new IllegalArgumentException();
		
		this.unwantedPattern = unwantedPattern;
		this.active = active;
		this.weightValue = weightValue;
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
	 * Returns a collection view of the scope of the constraint.
	 * @return a collection view of the scope of the constraint.
	 */
	public List<Contract> scope() {
		return new ContractCollection();
	}

	/**
	 * Collection view of the scope of the constraint. This custom implementation 
	 * controls modification operations.
	 */
	private class ContractCollection extends AbstractList<Contract> {

		/* (non-Javadoc)
		 * @see java.util.AbstractList#get(int)
		 */
		@Override
		public Contract get(int idx) {
			return scope.get(idx);
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size() {
			return scope.size();
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractList#add(int, java.lang.Object)
		 */
		@Override
		public void add(int index, Contract element) {
			scope.add(index, element);
		}
	}

	/**
	 * Return the unwanted shift-pattern.
	 * 
	 * @return the unwanted shift-pattern.
	 */
	public ShiftPattern getUnwantedPattern() {
		return unwantedPattern;
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getEvaluator(de.uos.inf.ischedule.model.ShiftSchedulingProblem)
	 */
	@Override
	public ConstraintEvaluator getEvaluator(ShiftSchedulingProblem problem) {
		if (evaluator == null)
			evaluator = new UnwantedShiftPatternConstraintEvaluator(problem);
		return evaluator;
	}
	
	/**
	 * Evaluator of the constraint.
	 */
	class UnwantedShiftPatternConstraintEvaluator extends
			ConstraintEvaluator {
		
		/**
		 * Set of day-index for which the pattern applies.
		 */
		ArrayList<Integer> patternStartDayIndexes;
		
		/**
		 * List of employee indexes for which the constraint applies.
		 */
		ArrayList<Integer> constrainedEmployeeIndexes;
		
		/**
		 * Creates an evaluator of the constraint.
		 * 
		 * @param problem the shift scheduling problem.
		 */
		public UnwantedShiftPatternConstraintEvaluator(ShiftSchedulingProblem problem) {
			// Constrained employees
			constrainedEmployeeIndexes = new ArrayList<Integer>();
			for (int employeeIndex=0; employeeIndex<problem.employees.size(); 
					employeeIndex++) {
				if (scope.contains(problem.employees.get(employeeIndex).contract)) {
					constrainedEmployeeIndexes.add(employeeIndex);
				}
			}
			// Pattern start days
			patternStartDayIndexes = new ArrayList<Integer>();
			for (int dayIndex=0; dayIndex<problem.schedulingPeriod.size(); dayIndex++) {
				// Check lenght of the pattern
				if (unwantedPattern.entries.size()+dayIndex <= 
						problem.schedulingPeriod.size()) {
					if (unwantedPattern.dayOfWeekSpecific) {
						if (unwantedPattern.startDay == 
								problem.schedulingPeriod.getDayOfWeek(dayIndex)) {
							patternStartDayIndexes.add(dayIndex);
						}
					} else {
						patternStartDayIndexes.add(dayIndex);
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
			
			int matches = 0;
			// Iterates on employees
			for (int employeeIndex: constrainedEmployeeIndexes) {
				// Iterate on start days
				for (int patternStartDayIndex: patternStartDayIndexes) {
					if (matchPattern(solution,
							patternStartDayIndex,
							employeeIndex,
							unwantedPattern.entries)) {
						matches++;
					}
				}
			}
			return matches*weightValue;
		}

		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getEstimatedAssignmentCost(de.uos.inf.ischedule.model.Solution, int, de.uos.inf.ischedule.model.Shift, int)
		 */
		@Override
		public int getEstimatedAssignmentCost(Solution solution,
				int employeeIndex, Shift shift, int assignmentDayIndex) {
			// Check scope
			if (!constrainedEmployeeIndexes.contains(employeeIndex))
				return 0;
			
			int patternMatchs = 0;
			// Check if dayIndex in a pattern
			for (int patternStartDayIndex: patternStartDayIndexes) {
				int patternEndDayIndex = patternStartDayIndex+
						unwantedPattern.entries.size()-1;
				if (assignmentDayIndex >= patternStartDayIndex &&
						assignmentDayIndex <= patternEndDayIndex) {
					// Check completion of a pattern
					boolean match = true;
					for (int entryIndex=0; entryIndex<unwantedPattern
							.entries.size(); entryIndex++) {
						ShiftPatternEntry entry = unwantedPattern.entries.get(entryIndex);
						Shift assignment = null;
						if (entryIndex+patternStartDayIndex == assignmentDayIndex) {
							assignment = shift;
						} else {
							assignment = solution.assignments.get(
									entryIndex+patternStartDayIndex).get(employeeIndex);
						}
						// Check entry
						if (entry.assignmentType == PatternEntryType.WORKED_SHIFT) {
							if (assignment == null) {
								match = false;
								break;
							}
						} else if (entry.assignmentType == PatternEntryType.SPECIFIC_WORKED_SHIFT) {
							if (assignment != entry.shift) {
								match = false;
								break;
							}
						} else if (entry.assignmentType == PatternEntryType.NO_ASSIGNMENT) {
							if (assignment != null) {
								match = false;
								break;
							}
						} else if (entry.assignmentType == PatternEntryType.UNSPECIFIED_ASSIGNMENT) {
							// All type of assignment allowed
						}
					}
					if (match) {
						patternMatchs++;
					}
				} else if (patternStartDayIndex > assignmentDayIndex) {
					break;
				}
			}
			return patternMatchs*weightValue;
		}
		
		/**
		 * Check if the pattern match the assignments of an employee
		 * from a given day-index. Returns <code>true</code> if the pattern
		 * matches, <code>false</code> otherwise.
		 * Note that length of pattern must be check beforehand in order to avoid
		 * out-of-range errors.
		 * 
		 * @param solution the solution.
		 * @param dayIndex the starting day-index of the pattern.
		 * @param employeeIndex the index of the employee.
		 * @param entries the entries of the pattern.
		 * @return Returns <code>true</code> if the pattern
		 * match, <code>false</code> otherwise.
		 */
		private boolean matchPattern(Solution solution, int dayIndex, int employeeIndex,
				List<ShiftPatternEntry> entries) {
			for (int entryIndex=0; entryIndex<entries.size(); entryIndex++) {
				ShiftPatternEntry entry = entries.get(entryIndex);
				Shift assignment = solution.assignments.get(
						entryIndex+dayIndex).get(employeeIndex);
				if (entry.assignmentType == PatternEntryType.WORKED_SHIFT) {
					if (assignment == null)
						return false;
				} else if (entry.assignmentType == PatternEntryType.SPECIFIC_WORKED_SHIFT) {
					if (assignment != entry.shift)
						return false;
				} else if (entry.assignmentType == PatternEntryType.NO_ASSIGNMENT) {
					if (assignment != null)
						return false;
				} else if (entry.assignmentType == PatternEntryType.UNSPECIFIED_ASSIGNMENT) {
					// All type of assignment allowed
				}
			}
			return true;
		}
		
		/**
		 * Check if the pattern match the assignments of an employee
		 * from a given day-index, taking into account a swap-move.
		 * Returns <code>true</code> if the pattern
		 * matches, <code>false</code> otherwise.
		 * Note that length of pattern must be check beforehand in order to avoid
		 * out-of-range errors.
		 * 
		 * @param solution the solution.
		 * @param dayIndex the starting day-index of the pattern.
		 * @param employeeIndex the index of the employee.
		 * @param entries the entries of the pattern.
		 * @param swap the swap move.
		 * @return Returns <code>true</code> if the pattern
		 * match, <code>false</code> otherwise.
		 */
		private boolean matchPattern(Solution solution, int dayIndex, int employeeIndex,
				List<ShiftPatternEntry> entries, SwapMove swap) {
			for (int entryIndex=0; entryIndex<entries.size(); entryIndex++) {
				ShiftPatternEntry entry = entries.get(entryIndex);
				Shift assignment = swap.getResultingAssignment(solution, 
						entryIndex+dayIndex, employeeIndex);
				if (entry.assignmentType == PatternEntryType.WORKED_SHIFT) {
					if (assignment == null)
						return false;
				} else if (entry.assignmentType == PatternEntryType.SPECIFIC_WORKED_SHIFT) {
					if (assignment != entry.shift)
						return false;
				} else if (entry.assignmentType == PatternEntryType.NO_ASSIGNMENT) {
					if (assignment != null)
						return false;
				} else if (entry.assignmentType == PatternEntryType.UNSPECIFIED_ASSIGNMENT) {
					// All type of assignment allowed
				}
			}
			return true;
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
			
			// Compute previous and new penalties for night-shift before free weekend
			// covered by swap 
			int initialMatches = 0;
			int swapMatches = 0;
			
			// Iterate on start days
			for (int patternStartDayIndex: patternStartDayIndexes) {
				int patternEndDayIndex = patternStartDayIndex+
						unwantedPattern.entries.size()-1;
				// Check if cover with swap move
				if (
						(swapMove.getStartDayIndex() <= patternEndDayIndex &&
						swapMove.getEndDayIndex() >= patternStartDayIndex) ) {
					// Employee 1
					if (matchPattern(solution,
							patternStartDayIndex,
							swapMove.getEmployee1Index(),
							unwantedPattern.entries)) {
						initialMatches++;
					}
					if (matchPattern(solution,
							patternStartDayIndex,
							swapMove.getEmployee1Index(),
							unwantedPattern.entries,
							swapMove)) {
						swapMatches++;
					}
					// Employee 2
					if (matchPattern(solution,
							patternStartDayIndex,
							swapMove.getEmployee2Index(),
							unwantedPattern.entries)) {
						initialMatches++;
					}
					if (matchPattern(solution,
							patternStartDayIndex,
							swapMove.getEmployee2Index(),
							unwantedPattern.entries,
							swapMove)) {
						swapMatches++;
					}
				}
				if (swapMove.getEndDayIndex() <= patternStartDayIndex)
					break;
			}
			
			return (swapMatches-initialMatches)*weightValue;
		}
		
		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.ConstraintEvaluator#getConstraint()
		 */
		@Override
		public Constraint getConstraint() {
			return UnwantedShiftPatternConstraint.this;
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
				// Iterate on start days
				for (int patternStartDayIndex: patternStartDayIndexes) {
					if (matchPattern(solution,
							patternStartDayIndex,
							employeeIndex,
							unwantedPattern.entries)) {
						ConstraintViolation violation = new ConstraintViolation(
								UnwantedShiftPatternConstraint.this);
						violation.setCost(weightValue);
						String message = Messages.getString("UnwantedShiftPatternConstraint.unwantedShiftPattern"); //$NON-NLS-1$
						message = message.replaceAll("\\$1", unwantedPattern.toString()); //$NON-NLS-1$
						violation.setMessage(message);
						violation.addAssignmentRangeInScope(
								solution.employees.get(employeeIndex), 
								solution.problem.getSchedulingPeriod().getDate(patternStartDayIndex),
								solution.problem.getSchedulingPeriod().getDate(
										patternStartDayIndex+unwantedPattern.entries.size()-1));
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
			
			// Compute previous and new penalties for night-shift before free weekend
			// covered by swap 
			
			// Iterate on start days
			for (int patternStartDayIndex: patternStartDayIndexes) {
				int patternEndDayIndex = patternStartDayIndex+
						unwantedPattern.entries.size()-1;
				// Check if cover with swap move
				if (
						(swapMove.getStartDayIndex() <= patternEndDayIndex &&
						swapMove.getEndDayIndex() >= patternStartDayIndex) ) {
					boolean initialMatch;
					boolean swapMatch;
					
					// Employee 1
					initialMatch = (matchPattern(solution,
							patternStartDayIndex,
							swapMove.getEmployee1Index(),
							unwantedPattern.entries));
					swapMatch = (matchPattern(solution,
							patternStartDayIndex,
							swapMove.getEmployee1Index(),
							unwantedPattern.entries,
							swapMove));
					if (initialMatch && !swapMatch) {
						diff[0]++;
					} else if (!initialMatch && swapMatch) {
						diff[1]++;
					}
					
					// Employee 2
					initialMatch = (matchPattern(solution,
							patternStartDayIndex,
							swapMove.getEmployee2Index(),
							unwantedPattern.entries));
					swapMatch = (matchPattern(solution,
							patternStartDayIndex,
							swapMove.getEmployee2Index(),
							unwantedPattern.entries,
							swapMove));
					if (initialMatch && !swapMatch) {
						diff[0]++;
					} else if (!initialMatch && swapMatch) {
						diff[1]++;
					}
				}
				if (swapMove.getEndDayIndex() <= patternStartDayIndex)
					break;
			}
			
			return diff;
		}
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getName()
	 */
	@Override
	public String getName() {
		return Messages.getString("UnwantedShiftPatternConstraint.name"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getConstraintCostLabel()
	 */
	@Override
	public String getCostLabel() {
		return Messages.getString("UnwantedShiftPatternConstraint.costLabel"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription()
	 */
	@Override
	public String getHTMLDescription() {
		return Messages.getString("UnwantedShiftPatternConstraint.description"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLDescription(de.uos.inf.ischedule.model.Employee)
	 */
	@Override
	public String getHTMLDescription(Employee employee) {
		String paramDesc = Messages.getString(
				"UnwantedShiftPatternConstraint.descriptionEmployee"); //$NON-NLS-1$
		paramDesc = paramDesc.replaceAll("\\$1", unwantedPattern.toString()); //$NON-NLS-1$
		return paramDesc;
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#getHTMLParametersDescriptions(de.uos.inf.ischedule.model.ShiftSchedulingProblem)
	 */
	@Override
	public List<String> getHTMLParametersDescriptions(
			ShiftSchedulingProblem problem) {
		String paramDesc = Messages.getString(
				"UnwantedShiftPatternConstraint.parametersDescription"); //$NON-NLS-1$
		paramDesc = paramDesc.replaceAll("\\$1", unwantedPattern.toString()); //$NON-NLS-1$
		String contractList = ""; //$NON-NLS-1$
		for (int contractIdx=0; contractIdx<scope.size(); contractIdx++) {
			String contractDesc = Messages.getString(
					"UnwantedShiftPatternConstraint.parametersDescription.CoveredContract"); //$NON-NLS-1$
			contractDesc = contractDesc.replaceAll("\\$1", scope.get(contractIdx).getName()); //$NON-NLS-1$
			String imgURL = IconUtils.getImageURL(scope.get(contractIdx).getIconPath());
			contractDesc = contractDesc.replaceAll("\\$2", imgURL); //$NON-NLS-1$
			if (contractIdx < scope.size()-1)
				contractDesc = contractDesc+Messages.getString(
						"UnwantedShiftPatternConstraint.parametersDescription.CoveredContractSeparator"); //$NON-NLS-1$
			contractList = contractList+contractDesc;
		}
		paramDesc = paramDesc.replaceAll("\\$2", contractList); //$NON-NLS-1$
		ArrayList<String> descList = new  ArrayList<String>();
		descList.add(paramDesc);
		return descList;
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.Constraint#cover(de.uos.inf.ischedule.model.Employee)
	 */
	@Override
	public boolean cover(Employee employee) {
		if (employee == null)
			return false;
		return (scope.contains(employee.getContract()));
	}
	
	/**
	 * Returns the shift pattern that is verified by the constraint.
	 * 
	 * @return the shift pattern that is verified by the constraint.
	 */
	public ShiftPattern getShiftPattern() {
		return unwantedPattern;
	}

}
