/**
 * Copyright 2016, Meignan Consulting (Deutschland)
 * Author: David Meignan
 */
package fr.lalea.inrc2010evaluator;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;

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
public class ConstraintsTest {

	@org.junit.Test
	public void testConstraintsEvaluation() {

		ArrayList<String> descriptions = new ArrayList<String>();
		ArrayList<String> problemFilePaths = new ArrayList<String>();
		ArrayList<String> solutionFilePaths = new ArrayList<String>();
		ArrayList<int[]> expectedSolutionCosts = new ArrayList<int[]>();
		
		String basePath = "src/test/resources/inrc2010/constraint_unit_tests/";
		
		// Coverage
		
		descriptions.add("Coverage constraint.");
		problemFilePaths.add(basePath+"test_01_coverage_constraint.xml");
		solutionFilePaths.add(basePath+"test_01_coverage_constraint_solution_01.xml");
		expectedSolutionCosts.add(new int[]{0,0});
		
		descriptions.add("Coverage constraint.");
		problemFilePaths.add(basePath+"test_01_coverage_constraint.xml");
		solutionFilePaths.add(basePath+"test_01_coverage_constraint_solution_02.xml");
		expectedSolutionCosts.add(new int[]{2,0});
		
		descriptions.add("Coverage constraint.");
		problemFilePaths.add(basePath+"test_01_coverage_constraint.xml");
		solutionFilePaths.add(basePath+"test_01_coverage_constraint_solution_03.xml");
		expectedSolutionCosts.add(new int[]{1,0});
		
		// Skills
		
		descriptions.add("Skill constraint.");
		problemFilePaths.add(basePath+"test_02_skill_constraint.xml");
		solutionFilePaths.add(basePath+"test_02_skill_constraint_solution_01.xml");
		expectedSolutionCosts.add(new int[]{0,0});
		
		descriptions.add("Skill constraint.");
		problemFilePaths.add(basePath+"test_02_skill_constraint.xml");
		solutionFilePaths.add(basePath+"test_02_skill_constraint_solution_02.xml");
		expectedSolutionCosts.add(new int[]{0,11});
		
		// Requests 
		
		descriptions.add("Requests constraint - days off.");
		problemFilePaths.add(basePath+"test_03A_request_constraint.xml");
		solutionFilePaths.add(basePath+"test_03A_request_constraint_solution_01.xml");
		expectedSolutionCosts.add(new int[]{0,0});
		
		descriptions.add("Requests constraint - days off.");
		problemFilePaths.add(basePath+"test_03A_request_constraint.xml");
		solutionFilePaths.add(basePath+"test_03A_request_constraint_solution_02.xml");
		expectedSolutionCosts.add(new int[]{0,10});
		
		descriptions.add("Requests constraint - days on.");
		problemFilePaths.add(basePath+"test_03B_request_constraint.xml");
		solutionFilePaths.add(basePath+"test_03B_request_constraint_solution_01.xml");
		expectedSolutionCosts.add(new int[]{0,0});
		
		descriptions.add("Requests constraint - days on.");
		problemFilePaths.add(basePath+"test_03B_request_constraint.xml");
		solutionFilePaths.add(basePath+"test_03B_request_constraint_solution_02.xml");
		expectedSolutionCosts.add(new int[]{0,10});

		descriptions.add("Requests constraint - shifts off.");
		problemFilePaths.add(basePath+"test_03C_request_constraint.xml");
		solutionFilePaths.add(basePath+"test_03C_request_constraint_solution_01.xml");
		expectedSolutionCosts.add(new int[]{0,0});
		
		descriptions.add("Requests constraint - shifts off.");
		problemFilePaths.add(basePath+"test_03C_request_constraint.xml");
		solutionFilePaths.add(basePath+"test_03C_request_constraint_solution_02.xml");
		expectedSolutionCosts.add(new int[]{0,10});

		descriptions.add("Requests constraint - shifts on.");
		problemFilePaths.add(basePath+"test_03D_request_constraint.xml");
		solutionFilePaths.add(basePath+"test_03D_request_constraint_solution_01.xml");
		expectedSolutionCosts.add(new int[]{0,0});
		
		descriptions.add("Requests constraint - shifts on.");
		problemFilePaths.add(basePath+"test_03D_request_constraint.xml");
		solutionFilePaths.add(basePath+"test_03D_request_constraint_solution_02.xml");
		expectedSolutionCosts.add(new int[]{0,10});
		
		// Number of assignments
		
		descriptions.add("Number of assignments.");
		problemFilePaths.add(basePath+"test_04_num_assignments_constraint.xml");
		solutionFilePaths.add(basePath+"test_04_num_assignments_constraint_solution_01.xml");
		expectedSolutionCosts.add(new int[]{0,0});
		
		descriptions.add("Number of assignments.");
		problemFilePaths.add(basePath+"test_04_num_assignments_constraint.xml");
		solutionFilePaths.add(basePath+"test_04_num_assignments_constraint_solution_02.xml");
		expectedSolutionCosts.add(new int[]{0,220});
		
		// consecutive assignments
		
		descriptions.add("Consecutive assignments.");
		problemFilePaths.add(basePath+"test_05_consecutive_assignments_constraint.xml");
		solutionFilePaths.add(basePath+"test_05_consecutive_assignments_constraint_solution_01.xml");
		expectedSolutionCosts.add(new int[]{0,0});
		
		descriptions.add("Consecutive assignments.");
		problemFilePaths.add(basePath+"test_05_consecutive_assignments_constraint.xml");
		solutionFilePaths.add(basePath+"test_05_consecutive_assignments_constraint_solution_02.xml");
		expectedSolutionCosts.add(new int[]{0,220});
		
		// consecutive days off
		
		descriptions.add("Consecutive days off.");
		problemFilePaths.add(basePath+"test_06_consecutive_days_off_constraint.xml");
		solutionFilePaths.add(basePath+"test_06_consecutive_days_off_constraint_solution_01.xml");
		expectedSolutionCosts.add(new int[]{0,0});
		
		descriptions.add("Consecutive days off.");
		problemFilePaths.add(basePath+"test_06_consecutive_days_off_constraint.xml");
		solutionFilePaths.add(basePath+"test_06_consecutive_days_off_constraint_solution_02.xml");
		expectedSolutionCosts.add(new int[]{0,110});

		descriptions.add("Consecutive days off.");
		problemFilePaths.add(basePath+"test_06_consecutive_days_off_constraint.xml");
		solutionFilePaths.add(basePath+"test_06_consecutive_days_off_constraint_solution_03.xml");
		expectedSolutionCosts.add(new int[]{0,200});

		descriptions.add("Consecutive days off.");
		problemFilePaths.add(basePath+"test_06_consecutive_days_off_constraint.xml");
		solutionFilePaths.add(basePath+"test_06_consecutive_days_off_constraint_solution_04.xml");
		expectedSolutionCosts.add(new int[]{0,110});
		
		descriptions.add("Consecutive days off.");
		problemFilePaths.add(basePath+"test_06B_consecutive_days_off_constraint.xml");
		solutionFilePaths.add(basePath+"test_06B_consecutive_days_off_constraint_solution_01.xml");
		expectedSolutionCosts.add(new int[]{0,100});
		
		// Complete weekends
		
		descriptions.add("Complete weekends");
		problemFilePaths.add(basePath+"test_07_complete_weekends_constraint.xml");
		solutionFilePaths.add(basePath+"test_07_complete_weekends_constraint_solution_01.xml");
		expectedSolutionCosts.add(new int[]{0,0});
		
		descriptions.add("Complete weekends");
		problemFilePaths.add(basePath+"test_07_complete_weekends_constraint.xml");
		solutionFilePaths.add(basePath+"test_07_complete_weekends_constraint_solution_02.xml");
		expectedSolutionCosts.add(new int[]{0,410});

		descriptions.add("Complete weekends");
		problemFilePaths.add(basePath+"test_07_complete_weekends_constraint.xml");
		solutionFilePaths.add(basePath+"test_07_complete_weekends_constraint_solution_03.xml");
		expectedSolutionCosts.add(new int[]{0,200});

		descriptions.add("Complete weekends");
		problemFilePaths.add(basePath+"test_07_complete_weekends_constraint.xml");
		solutionFilePaths.add(basePath+"test_07_complete_weekends_constraint_solution_04.xml");
		expectedSolutionCosts.add(new int[]{0,100});
		

		// Same shift on weekends
		
		descriptions.add("Same shift on weekends");
		problemFilePaths.add(basePath+"test_08A_same_shift_weekend_constraint.xml");
		solutionFilePaths.add(basePath+"test_08A_same_shift_weekend_constraint_solution_01.xml");
		expectedSolutionCosts.add(new int[]{0,0});
		
		descriptions.add("Same shift on weekends");
		problemFilePaths.add(basePath+"test_08A_same_shift_weekend_constraint.xml");
		solutionFilePaths.add(basePath+"test_08A_same_shift_weekend_constraint_solution_02.xml");
		expectedSolutionCosts.add(new int[]{0,220});

		descriptions.add("Same shift on weekends");
		problemFilePaths.add(basePath+"test_08B_same_shift_weekend_constraint.xml");
		solutionFilePaths.add(basePath+"test_08B_same_shift_weekend_constraint_solution_01.xml");
		expectedSolutionCosts.add(new int[]{0,0});

		descriptions.add("Same shift on weekends");
		problemFilePaths.add(basePath+"test_08B_same_shift_weekend_constraint.xml");
		solutionFilePaths.add(basePath+"test_08B_same_shift_weekend_constraint_solution_02.xml");
		expectedSolutionCosts.add(new int[]{0,320});
		
		// Disabled no night shift before free weekends
		
		descriptions.add("Disabled no night shift before free weekends");
		problemFilePaths.add(basePath+"test_09_no_night_shift_constraint.xml");
		solutionFilePaths.add(basePath+"test_09_no_night_shift_constraint_solution_01.xml");
		expectedSolutionCosts.add(new int[]{0,0});
		
		descriptions.add("Disabled no night shift before free weekends");
		problemFilePaths.add(basePath+"test_09_no_night_shift_constraint.xml");
		solutionFilePaths.add(basePath+"test_09_no_night_shift_constraint_solution_02.xml");
		expectedSolutionCosts.add(new int[]{0,0});

		// Consecutive working weekends
		
		descriptions.add("Consecutive working weekends");
		problemFilePaths.add(basePath+"test_10_consecutive_working_weekends_constraint.xml");
		solutionFilePaths.add(basePath+"test_10_consecutive_working_weekends_constraint_solution_01.xml");
		expectedSolutionCosts.add(new int[]{0,0});
		
		descriptions.add("Consecutive working weekends");
		problemFilePaths.add(basePath+"test_10_consecutive_working_weekends_constraint.xml");
		solutionFilePaths.add(basePath+"test_10_consecutive_working_weekends_constraint_solution_02.xml");
		expectedSolutionCosts.add(new int[]{0,110});

		// Unwanted shift patterns
		
		descriptions.add("Unwanted shift patterns");
		problemFilePaths.add(basePath+"test_11_unwanted_shift_patterns_constraint.xml");
		solutionFilePaths.add(basePath+"test_11_unwanted_shift_patterns_constraint_solution_01.xml");
		expectedSolutionCosts.add(new int[]{0,0});
		
		descriptions.add("Unwanted shift patterns");
		problemFilePaths.add(basePath+"test_11_unwanted_shift_patterns_constraint.xml");
		solutionFilePaths.add(basePath+"test_11_unwanted_shift_patterns_constraint_solution_02.xml");
		expectedSolutionCosts.add(new int[]{0,20});
		
		descriptions.add("Unwanted shift patterns");
		problemFilePaths.add(basePath+"test_11_unwanted_shift_patterns_constraint.xml");
		solutionFilePaths.add(basePath+"test_11_unwanted_shift_patterns_constraint_solution_03.xml");
		expectedSolutionCosts.add(new int[]{0,120});
		
		// Evaluate each solution
		
		for (int testIdx=0; testIdx<problemFilePaths.size(); testIdx++) {
			String description = descriptions.get(testIdx);
			SolutionEvaluation evaluation = evaluateSolution(problemFilePaths.get(testIdx),
					solutionFilePaths.get(testIdx));
			assertEquals(description, 
					new SolutionEvaluation(expectedSolutionCosts.get(testIdx)), evaluation);
		}
		
	}

	/**
	 * Returns the evaluation of a solution.
	 * 
	 * @param problemFilePath the problem file.
	 * @param solutionFilePath the solution file.
	 * @return the evaluation of the solution.
	 */
	private static SolutionEvaluation evaluateSolution(String problemFilePath, 
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
		
		return solution.getEvaluation();
	}

}
