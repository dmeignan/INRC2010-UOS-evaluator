/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

import java.util.Arrays;
import java.util.List;

/**
 * A <code>SolutionEvaluation</code> represents the cost of a solution.
 * Each constraint rank has a cost value. Comparison of two evaluations
 * of solution uses lexicographic order.
 * Note that an evaluation is immutable.
 * 
 * @author David Meignan
 */
public class SolutionEvaluation implements Comparable<SolutionEvaluation> {
	
	/**
	 * Cost values by rank.
	 */
	private int[] costValues;

	/**
	 * Constructs an evaluation of a solution. Cost values for each rank are
	 * set to <code>Integer.MAX_VALUE</code>.
	 */
	private SolutionEvaluation(int nbRanks) {
		costValues = new int[nbRanks];
	}
	
	/**
	 * Constructs an evaluation from an array of integer values. 
	 * @param costs the cost for the initialization of each rank.
	 */
	public SolutionEvaluation(int[] costs) {
		costValues = new int[costs.length];
		for (int i=0; i<costs.length; i++) {
			costValues[i] = costs[i];
		}
	}
	
	/**
	 * Constructs an evaluation from a list of integer values. 
	 * @param costs the cost for the initialization of each rank.
	 */
	public SolutionEvaluation(List<Integer> costs) {
		costValues = new int[costs.size()];
		for (int i=0; i<costs.size(); i++) {
			costValues[i] = costs.get(i);
		}
	}
	
	/**
	 * Constructs an evaluation by copy.
	 * @param evaluation the evaluation to copy.
	 */
	public SolutionEvaluation(SolutionEvaluation evaluation) {
		costValues = new int[evaluation.costValues.length];
		for (int i=0; i<evaluation.costValues.length; i++) {
			costValues[i] = evaluation.costValues[i];
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SolutionEvaluation o) {
		if (o == null)
			throw new IllegalArgumentException();
		if (o.costValues.length != costValues.length)
			throw new IllegalArgumentException();
		for (int rankIndex=0; rankIndex<costValues.length; rankIndex++) {
//			int rankComp = Integer.compare( // TODO not 1.6 compatible
//					costValues[rankIndex],
//					o.costValues[rankIndex]);
			int rankComp = Integer.valueOf(costValues[rankIndex])
					.compareTo(Integer.valueOf(o.costValues[rankIndex]));
			if (rankComp != 0)
				return rankComp;
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(costValues);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SolutionEvaluation other = (SolutionEvaluation) obj;
		if (costValues == null) {
			if (other.costValues != null)
				return false;
		} else if (!Arrays.equals(costValues, other.costValues))
			return false;
		return true;
	}
	
	/**
	 * Returns the number of ranks of the evaluation.
	 * 
	 * @return the number of ranks of the evaluation.
	 */
	public int getNbRanks() {
		return costValues.length;
	}
	
	/**
	 * returns the total cost for constraints of a given rank. 
	 * 
	 * @param rankIndex the rank index of constraints.
	 * @return the total cost for constraints of a given rank.
	 * @throws IndexOutOfBoundsException if the rank index is out of range. 
	 */
	public int getCost(int rankIndex) {
		return costValues[rankIndex];
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Arrays.toString(costValues);
	}

	/**
	 * Returns a copy of this evaluation with the specified evaluation added.
	 * 
	 * @param evaluation the evaluation to add.
	 * @return a copy of this evaluation with the specified evaluation added.
	 * @throws NullPointerException if the parameter is <code>null</code>.
	 * @throws IndexOutOfBoundsException if the number of ranks is different.
	 */
	public SolutionEvaluation plus(SolutionEvaluation evaluation) {
		// Create a copy of the evaluation
		SolutionEvaluation result = new SolutionEvaluation(
				costValues.length);
		// Add values
		for (int rankIndex=0; rankIndex<costValues.length; rankIndex++) {
			result.costValues[rankIndex] = costValues[rankIndex]+
					evaluation.costValues[rankIndex];
		}
		return result;
	}

}
