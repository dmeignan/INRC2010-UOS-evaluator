/**
 * Copyright 2016, Meignan Consulting (Deutschland)
 * Author: David Meignan
 */
package fr.lalea.inrc2010evaluator;

import java.io.File;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

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
public class EvaluatorParameters {
	
	/**
	 * Problem file.
	 */
	@Parameter(names = { "-p", "-problem"}, description = "XML file of the "
			+ "problem instance.", required = true)
	private String problemFilePath;
	
	/**
	 * Solution file.
	 */
	@Parameter(names = { "-s", "-solution"}, description = "XML file of the "
			+ "solution.", required = true)
	private String solutionFilePath;
	
	
	/**
	 * Validates the parameters and throw an exception if one of the
	 * parameter is not valid.
	 * 
	 * @throws ParameterException if a parameter value is not valid.
	 */
	public void validate() throws ParameterException {
		// Check if files exists
		if (problemFilePath == null || solutionFilePath == null) {
			throw new ParameterException("Files cannot have null value.");
		}
		File problemFile = new File(problemFilePath);
		File solutionFile = new File(problemFilePath);
		try {
			if (!problemFile.isFile()) {
				throw new ParameterException("The problem file cannot be found"
						+ ".");
			}
			if (!solutionFile.isFile()) {
				throw new ParameterException("The solution file cannot be found"
						+ ".");
			}
		} catch (SecurityException e) {
			throw new ParameterException("The problem file or solution file"
					+ "cannot be accessed.");
		}
	}


	/**
	 * Returns the problem file.
	 * 
	 * @return the problem file.
	 */
	public File getProblemFile() {
		return new File(problemFilePath);
	}
	
	/**
	 * Returns the solution file.
	 * 
	 * @return the solution file.
	 */
	public File getSolutionFile() {
		return new File(solutionFilePath);
	}
}
