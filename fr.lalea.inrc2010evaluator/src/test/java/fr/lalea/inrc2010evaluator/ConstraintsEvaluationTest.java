/**
 * Copyright 2016, Meignan Consulting (Deutschland)
 * Author: David Meignan
 */
package fr.lalea.inrc2010evaluator;

import static org.junit.Assert.*;

import java.io.File;

import de.uos.inf.ischedule.model.Schedule;
import de.uos.inf.ischedule.model.ShiftSchedulingProblem;
import de.uos.inf.ischedule.model.Solution;
import de.uos.inf.ischedule.model.SolutionEvaluation;
import de.uos.inf.ischedule.model.inrc.InrcProblemFactory;
import de.uos.inf.ischedule.model.inrc.InrcSolutionFactory;

/**
 * JUnit for testing the evaluation of INRC constraints.
 * 
 * @author David Meignan
 */
public class ConstraintsEvaluationTest {

	@org.junit.Test
	public void testConstraintsEvaluation() {

		SolutionEvaluation evaluation;
		
		// Demand coverage
		
		evaluation = evaluateSolution(
				"src/test/resources/inrc2010/test_instances/test_01_coverage_constraint.xml",
				"src/test/resources/inrc2010/test_instances/test_01_coverage_constraint_solution_01.xml"
				);
//		System.out.println("Evaluation test_01_coverage_constraint_solution_01.xml: "+
//				evaluation.toString());
		assertEquals(
				evaluation, new SolutionEvaluation(new int[] {0,0}));
		
		
		evaluation = evaluateSolution(
				"src/test/resources/inrc2010/test_instances/test_01_coverage_constraint.xml",
				"src/test/resources/inrc2010/test_instances/test_01_coverage_constraint_solution_02.xml"
				);
//		System.out.println("Evaluation test_01_coverage_constraint_solution_02.xml: "+
//				evaluation.toString());
		assertEquals(
				evaluation, new SolutionEvaluation(new int[] {2,0}));
		
		evaluation = evaluateSolution(
				"src/test/resources/inrc2010/test_instances/test_01_coverage_constraint.xml",
				"src/test/resources/inrc2010/test_instances/test_01_coverage_constraint_solution_03.xml"
				);
//		System.out.println("Evaluation test_01_coverage_constraint_solution_03.xml: "+
//				evaluation.toString());
		assertEquals(
				evaluation, new SolutionEvaluation(new int[] {1,0}));
		
		// Skill coverage
		
		evaluation = evaluateSolution(
				"src/test/resources/inrc2010/test_instances/test_02_skill_constraint.xml",
				"src/test/resources/inrc2010/test_instances/test_02_skill_constraint_solution_01.xml"
				);
//		System.out.println("Evaluation test_02_skill_constraint_solution_01.xml: "+
//				evaluation.toString());
		assertEquals(
				evaluation, new SolutionEvaluation(new int[] {0,0}));
		

		evaluation = evaluateSolution(
				"src/test/resources/inrc2010/test_instances/test_02_skill_constraint.xml",
				"src/test/resources/inrc2010/test_instances/test_02_skill_constraint_solution_02.xml"
				);
//		System.out.println("Evaluation test_02_skill_constraint_solution_02.xml: "+
//				evaluation.toString());
		assertEquals(
				evaluation, new SolutionEvaluation(new int[] {0,3}));
		
		// Requests
		
		evaluation = evaluateSolution(
				"src/test/resources/inrc2010/test_instances/test_03_request_constraint.xml",
				"src/test/resources/inrc2010/test_instances/test_03_request_constraint_solution_01.xml"
				);
//		System.out.println("Evaluation test_03_request_constraint_solution_01.xml: "+
//				evaluation.toString());
		assertEquals(
				evaluation, new SolutionEvaluation(new int[] {0,0}));
		
		evaluation = evaluateSolution(
				"src/test/resources/inrc2010/test_instances/test_03_request_constraint.xml",
				"src/test/resources/inrc2010/test_instances/test_03_request_constraint_solution_02.xml"
				);
//		System.out.println("Evaluation test_03_request_constraint_solution_02.xml: "+
//				evaluation.toString());
		assertEquals(
				evaluation, new SolutionEvaluation(new int[] {0,1111}));
		
		// Max. and min. number of assignments
		
		evaluation = evaluateSolution(
				"src/test/resources/inrc2010/test_instances/test_04_num_assignments_constraint.xml",
				"src/test/resources/inrc2010/test_instances/test_04_num_assignments_constraint_solution_01.xml"
				);
//		System.out.println("Evaluation test_04_num_assignments_constraint_solution_01.xml: "+
//				evaluation.toString());
		assertEquals(
				evaluation, new SolutionEvaluation(new int[] {0,0}));
		
		evaluation = evaluateSolution(
				"src/test/resources/inrc2010/test_instances/test_04_num_assignments_constraint.xml",
				"src/test/resources/inrc2010/test_instances/test_04_num_assignments_constraint_solution_02.xml"
				);
//		System.out.println("Evaluation test_04_num_assignments_constraint_solution_02.xml: "+
//				evaluation.toString());
		assertEquals(
				evaluation, new SolutionEvaluation(new int[] {0,22}));
		
		// Max. and min. consecutive working days
		
		evaluation = evaluateSolution(
				"src/test/resources/inrc2010/test_instances/test_05_consecutive_assignments_constraint.xml",
				"src/test/resources/inrc2010/test_instances/test_05_consecutive_assignments_constraint_solution_01.xml"
				);
//		System.out.println("Evaluation test_05_consecutive_assignments_constraint_solution_01.xml: "+
//				evaluation.toString());
		assertEquals(
				evaluation, new SolutionEvaluation(new int[] {0,0}));
		
		evaluation = evaluateSolution(
				"src/test/resources/inrc2010/test_instances/test_05_consecutive_assignments_constraint.xml",
				"src/test/resources/inrc2010/test_instances/test_05_consecutive_assignments_constraint_solution_02.xml"
				);
//		System.out.println("Evaluation test_05_consecutive_assignments_constraint_solution_02.xml: "+
//				evaluation.toString());
		assertEquals(
				evaluation, new SolutionEvaluation(new int[] {0,22}));
		
		// Max. and min. consecutive day-offs
		
		evaluation = evaluateSolution(
				"src/test/resources/inrc2010/test_instances/test_06_consecutive_days_off_constraint.xml",
				"src/test/resources/inrc2010/test_instances/test_06_consecutive_days_off_constraint_solution_01.xml"
				);
//		System.out.println("Evaluation test_06_consecutive_day_offs_constraint_solution_01.xml: "+
//				evaluation.toString());
		assertEquals(
				evaluation, new SolutionEvaluation(new int[] {0,0}));
		
		evaluation = evaluateSolution(
				"src/test/resources/inrc2010/test_instances/test_06_consecutive_days_off_constraint.xml",
				"src/test/resources/inrc2010/test_instances/test_06_consecutive_days_off_constraint_solution_02.xml"
				);
//		System.out.println("Evaluation test_06_consecutive_day_offs_constraint_solution_02.xml: "+
//				evaluation.toString());
		assertEquals(
				evaluation, new SolutionEvaluation(new int[] {0,11}));

		// Complete weekends
		
		evaluation = evaluateSolution(
				"src/test/resources/inrc2010/test_instances/test_07_complete_weekends_constraint.xml",
				"src/test/resources/inrc2010/test_instances/test_07_complete_weekends_constraint_solution_01.xml"
				);
//		System.out.println("Evaluation test_07_complete_weekends_constraint_solution_01.xml: "+
//				evaluation.toString());
		assertEquals(
				evaluation, new SolutionEvaluation(new int[] {0,0}));
		
		evaluation = evaluateSolution(
				"src/test/resources/inrc2010/test_instances/test_07_complete_weekends_constraint.xml",
				"src/test/resources/inrc2010/test_instances/test_07_complete_weekends_constraint_solution_02.xml"
				);
//		System.out.println("Evaluation test_07_complete_weekends_constraint_solution_02.xml: "+
//				evaluation.toString());
		assertEquals(
				evaluation, new SolutionEvaluation(new int[] {0,41}));
		
		// Identical shift during weekends
		
		evaluation = evaluateSolution(
				"src/test/resources/inrc2010/test_instances/test_08_same_shift_weekend_constraint.xml",
				"src/test/resources/inrc2010/test_instances/test_08_same_shift_weekend_constraint_solution_01.xml"
				);
//		System.out.println("Evaluation test_08_same_shift_weekend_constraint_solution_01.xml: "+
//				evaluation.toString());
		assertEquals(
				evaluation, new SolutionEvaluation(new int[] {0,0}));
		
		evaluation = evaluateSolution(
				"src/test/resources/inrc2010/test_instances/test_08_same_shift_weekend_constraint.xml",
				"src/test/resources/inrc2010/test_instances/test_08_same_shift_weekend_constraint_solution_02.xml"
				);
//		System.out.println("Evaluation test_08_same_shift_weekend_constraint_solution_02.xml: "+
//				evaluation.toString());
		assertEquals(
				evaluation, new SolutionEvaluation(new int[] {0,32}));

		// No night shift before free weekends
		
		evaluation = evaluateSolution(
				"src/test/resources/inrc2010/test_instances/test_09_no_night_shift_constraint.xml",
				"src/test/resources/inrc2010/test_instances/test_09_no_night_shift_constraint_solution_01.xml"
				);
//		System.out.println("Evaluation test_09_no_night_shift_constraint_solution_01.xml: "+
//				evaluation.toString());
		assertEquals(
				evaluation, new SolutionEvaluation(new int[] {0,0}));
		
		evaluation = evaluateSolution(
				"src/test/resources/inrc2010/test_instances/test_09_no_night_shift_constraint.xml",
				"src/test/resources/inrc2010/test_instances/test_09_no_night_shift_constraint_solution_02.xml"
				);
//		System.out.println("Evaluation test_09_no_night_shift_constraint_solution_02.xml: "+
//				evaluation.toString());
		assertEquals(
				evaluation, new SolutionEvaluation(new int[] {0,0})); 	// constraint deactivated
																		// Normally: new int[] {0,10}

		// Max. and min. number of consecutive working weekends
		
		evaluation = evaluateSolution(
				"src/test/resources/inrc2010/test_instances/test_10_consecutive_working_weekends_constraint.xml",
				"src/test/resources/inrc2010/test_instances/test_10_consecutive_working_weekends_constraint_solution_01.xml"
				);
//		System.out.println("Evaluation test_10_consecutive_working_weekends_constraint_solution_01.xml: "+
//				evaluation.toString());
		assertEquals(
				evaluation, new SolutionEvaluation(new int[] {0,0}));
		
		evaluation = evaluateSolution(
				"src/test/resources/inrc2010/test_instances/test_10_consecutive_working_weekends_constraint.xml",
				"src/test/resources/inrc2010/test_instances/test_10_consecutive_working_weekends_constraint_solution_02.xml"
				);
//		System.out.println("Evaluation test_10_consecutive_working_weekends_constraint_solution_02.xml: "+
//				evaluation.toString());
		assertEquals(
				evaluation, new SolutionEvaluation(new int[] {0,11}));

		// Unwanted shift pattern
		
		evaluation = evaluateSolution(
				"src/test/resources/inrc2010/test_instances/test_11_unwanted_shift_patterns_constraint.xml",
				"src/test/resources/inrc2010/test_instances/test_11_unwanted_shift_patterns_constraint_solution_01.xml"
				);
//		System.out.println("Evaluation test_11_unwanted_shift_patterns_constraint_solution_01.xml: "+
//				evaluation.toString());
		assertEquals(
				evaluation, new SolutionEvaluation(new int[] {0,0}));
		
		evaluation = evaluateSolution(
				"src/test/resources/inrc2010/test_instances/test_11_unwanted_shift_patterns_constraint.xml",
				"src/test/resources/inrc2010/test_instances/test_11_unwanted_shift_patterns_constraint_solution_02.xml"
				);
//		System.out.println("Evaluation test_11_unwanted_shift_patterns_constraint_solution_02.xml: "+
//				evaluation.toString());
		assertEquals(
				evaluation, new SolutionEvaluation(new int[] {0,12}));
		
	}

	private SolutionEvaluation evaluateSolution(String problemFilePath,
			String solutionFilePath) {

		// Parse problem file
		ShiftSchedulingProblem problem = null;
		try {
			problem = InrcProblemFactory.loadProblem(new File(problemFilePath));
		} catch (Exception e) {
			System.err.println("Problem-file parsing failed.");
			System.exit(1);
		}
		
		// Parse solution file
		Solution solution = null;
		try {
			Schedule s = InrcSolutionFactory.loadXMLSchedule(
					new File(solutionFilePath), problem);
			solution = s.toSolution();
		} catch (Exception e) {
			System.err.println("Solution-file parsing failed.");
			System.exit(1);
		}
		
//		// Print evaluation details
//		System.out.println("Solution evaluation:\t"+solution.getEvaluation());
//		ArrayList<ConstraintViolation> constraintViolations =
//				solution.getConstraintViolations();
//		for (ConstraintViolation violation: constraintViolations) {
//			System.out.println("\t"+violation.getMessage()+
//					", cost: "+violation.getCost()+
//					", scope: "+violation.getConstraintViolationScopeDescription());
//		}
		
		return solution.getEvaluation();
	}

}
