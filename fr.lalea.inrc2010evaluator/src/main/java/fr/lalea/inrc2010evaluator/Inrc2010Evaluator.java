/**
 * Copyright 2016, Meignan Consulting (Deutschland)
 * Author: David Meignan
 */
package fr.lalea.inrc2010evaluator;

import java.util.ArrayList;

import com.beust.jcommander.JCommander;

import de.uos.inf.ischedule.model.ConstraintViolation;
import de.uos.inf.ischedule.model.Schedule;
import de.uos.inf.ischedule.model.ShiftSchedulingProblem;
import de.uos.inf.ischedule.model.Solution;
import de.uos.inf.ischedule.model.SolutionEvaluation;
import de.uos.inf.ischedule.model.inrc.InrcProblemFactory;
import de.uos.inf.ischedule.model.inrc.InrcSolutionFactory;

/**
 * Evaluator for the INRC2010 benchmark, based on the model of 
 * [Lü and Hao 2009] and used in [Meignan et al. 2015], [Meignan 2014] and 
 * [Meignan 2015].
 * 
 * 
 * [Lü and Hao 2009] Z. Lü, J.-K. Hao, "Adaptive neighborhood search for nurse 
 * rostering", European Journal of Operational Research, pp. 865-876, vol. 218,
 * 2012. DOI: 10.1016/j.ejor.2011.12.016
 *  
 * [Meignan et al. 2015] D. Meignan, S. Schwarze and S. Voß, "Improving 
 * Local-Search Metaheuristics Through Look-Ahead Policies", Annals of 
 * Mathematics and Artificial Intelligence. DOI: 10.1007/s10472-015-9453-y
 * 
 * [Meignan 2014] D. Meignan, "A Heuristic Approach to Schedule Reoptimization 
 * in the Context of Interactive Optimization", In Proceedings of the 2014 
 * Conference on Genetic and Evolutionary Computation (GECCO'14), pp. 461-468, 
 * 2014. DOI: 10.1145/2576768.2598213
 * 
 * [Meignan 2015] D. Meignan, "An Experimental Investigation of Reoptimization 
 * for Shift Scheduling", In Proceedings of the 11th Metaheuristics 
 * International Conference (MIC'15), Agadir, Morocco, June 7-10, 2015.
 * 
 * @author David Meignan
 */
public class Inrc2010Evaluator {

	/**
	 * Runs the evaluator for one solution and print the evaluation.
	 * 
	 * The required arguments are:
	 * <ul>
	 * <li><code>-p [PROBLEM]</code> The XML file of the problem instance,</li>
	 * <li><code>-s [SOLUTION]</code> The XML file of the solution to evaluate.
	 * </li>
	 * </ul>
	 * 
	 * @param args the arguments that are managed by the 
	 * <code>EvaluatorParameter</code> class.
	 */
	public static void main(String[] args) {
		
		// Parse parameters
		EvaluatorParameters params = new EvaluatorParameters();
		try {
			new JCommander(params, args);
			params.validate();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
		// Parse problem file
		ShiftSchedulingProblem problem = null;
		try {
			problem = InrcProblemFactory.loadProblem(params.getProblemFile());
		} catch (Exception e) {
			System.err.println("Problem-file parsing failed.");
			System.exit(1);
		}
		
		// Parse solution file
		Solution solution = null;
		try {
			Schedule s = InrcSolutionFactory.loadXMLSchedule(
					params.getSolutionFile(), problem);
			solution = s.toSolution();
		} catch (Exception e) {
			System.err.println("Solution-file parsing failed.");
			System.exit(1);
		}
		
		// Print the evaluation
		printEvaluation(solution);
		System.out.println();
		printUnsatisfiedConstraints(solution);
		System.exit(0);
	}

	/**
	 * Prints the constraints that are violated in the solution.
	 * 
	 * @param solution the evaluated solution.
	 */
	private static void printUnsatisfiedConstraints(Solution solution) {
		ArrayList<ConstraintViolation> cv = solution.getConstraintViolations();
		System.out.println("Number of constraint unsatisfied: " + cv.size());
		for (ConstraintViolation v: cv) {
			System.out.print(v.getMessage());
			System.out.print("\t Scope: ");
			System.out.print(v.getConstraintViolationScopeDescription());
			System.out.print("\t Cost: ");
			System.out.println(v.getCost());
		}
	}

	/**
	 * Prints the evaluation value of the solution.
	 * 
	 * @param solution the evaluated solution.
	 */
	private static void printEvaluation(Solution solution) {
		SolutionEvaluation evaluation = solution.getEvaluation();
		if (evaluation.getNbRanks() == 2) {
			System.out.println("Cost of hard constraints: "+ evaluation.getCost(0));
			System.out.println("Cost of soft constraints: "+ evaluation.getCost(1));
		} else {
			for (int rank=1; rank<=evaluation.getNbRanks(); rank++) {
				System.out.println("Cost of the constraints of rank "+ rank + ": " +
						evaluation.getCost(rank-1));
			}
		}
	}

}
