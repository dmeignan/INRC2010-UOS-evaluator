/**
 * Copyright 2016, Meignan Consulting (Deutschland)
 * Author: David Meignan
 */
package fr.lalea.inrc2010evaluator;

import com.beust.jcommander.Parameter;

/**
 * Parameters of the evaluator.
 * 
 * Parsing of parameters is performed by the JCommander library. The two 
 * required parameters are:
 * <ul>
 * <li><code>-p</code> The XML file of the problem instance,</li>
 * <li><code>-s</code> The XML file of the solution to evaluate.</li>
 * </ul>
 * 
 * @author David Meignan
 */
public class EvaluatorParameter {
	
	@Parameter(names = { "-s", "-randomSeed"}, description = "The seed value for the random number generator. This value will be " +
			"incremented by 1 for each run when mutiple runs are specified.")
	private Integer randomSeedStartingValue = 0;
	
}
