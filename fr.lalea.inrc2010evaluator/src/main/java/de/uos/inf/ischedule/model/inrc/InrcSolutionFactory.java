/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.inrc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import de.uos.inf.ischedule.model.Assignment;
import de.uos.inf.ischedule.model.Schedule;
import de.uos.inf.ischedule.model.ShiftSchedulingProblem;
import de.uos.inf.ischedule.model.inrc.InrcSolution.InrcAssignment;

/**
 * This class allows instantiating a <code>Schedule</code> from INRC data,
 * and conversely, creating <code>InrcSolution</code> object from instance of 
 * <code>Schedule</code>.
 * 
 * @author David Meignan
 */
public class InrcSolutionFactory {

	/**
	 * Constructs a schedule from INRC data.
	 *  
	 * @param inrcSolutionData the data of the schedule.
	 * @param problem the problem instance of the schedule.
	 * @return a schedule.
	 * 
	 * @throws IllegalArgumentException if solution's data or problem
	 * is <code>null</code>.
	 */
	public static Schedule getSchedule(InrcSolution inrcSolutionData, 
			ShiftSchedulingProblem problem) {
		if (inrcSolutionData == null || problem == null)
			throw new IllegalArgumentException();
		
		// Create schedule
		Schedule schedule = new Schedule(
				inrcSolutionData.schedulingPeriodID,
				inrcSolutionData.competitor.toString(),
				problem, problem.getSchedulingPeriod());
		
		// Add assignments
		if (inrcSolutionData.assignment != null) {
			for (InrcAssignment inrcAssignment: inrcSolutionData.assignment) {
				Assignment assignment = new Assignment(
						problem.getEmployee(inrcAssignment.employee),
						problem.getShift(inrcAssignment.shiftType),
						inrcAssignment.date.getYear(),
						inrcAssignment.date.getMonth(),
						inrcAssignment.date.getDay()
						);
				schedule.assignments().add(assignment);
			}
		}
		
		return schedule;
	}
	
	/**
	 * Constructs INRC solution from a schedule.
	 *  
	 * @param schedule the data of the schedule.
	 * @return a schedule.
	 * 
	 * @throws IllegalArgumentException if the schedule
	 * is <code>null</code>.
	 */
	public static InrcSolution getInrcSolution(Schedule schedule,
			int softConstraintsCost) {
		
		InrcSolution inrcSolution = new  InrcSolution();
		inrcSolution.setSchedulingPeriodID(schedule.getProblemId());
		inrcSolution.setCompetitor(schedule.getDescription());
		inrcSolution.setSoftConstraintsPenalty(new BigInteger(
				Integer.toString(softConstraintsCost) ));
		
		// Assignments
		for (Assignment assignment: schedule.assignments()) {
			InrcAssignment inrcAssignment = new InrcAssignment();
			// Set date
			XMLGregorianCalendar gc = null;
			try {
				gc = DatatypeFactory.newInstance()
						.newXMLGregorianCalendar();
				gc.setYear(assignment.getDate().getYear());
				gc.setMonth(assignment.getDate().getMonthOfYear());
				gc.setDay(assignment.getDate().getDayOfMonth());
			} catch (DatatypeConfigurationException e) {
				e.printStackTrace();
			}
			gc.setTimezone(DatatypeConstants.FIELD_UNDEFINED);  
			gc.setTime(DatatypeConstants.FIELD_UNDEFINED,
					DatatypeConstants.FIELD_UNDEFINED,
					DatatypeConstants.FIELD_UNDEFINED);  
			inrcAssignment.setDate(gc);
			// Set shift and employee
			inrcAssignment.setEmployee(assignment.getEmployee().getId());
			inrcAssignment.setShiftType(assignment.getShift().getId());
			inrcSolution.getAssignment().add(inrcAssignment);
		}
		
		return inrcSolution;
	}
	
	/**
	 * Save a solution in a XML file using the INRC format.
	 *  
	 * @param schedule the schedule to save.
	 * @param file the file in which the solution has to be saved.
	 * @throws JAXBException if marshal operation failed.
	 */
	public static void saveSolutionXML(InrcSolution inrcSolution,
			File xmlFile) throws JAXBException {
		// Write XML of solution
		JAXBContext jc = JAXBContext.newInstance(
				"de.uos.inf.ischedule.model.inrc");
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(inrcSolution, xmlFile);
	}
	
	/**
	 * Save a schedule in a XML file using the INRC format.
	 *  
	 * @param schedule the schedule to save.
	 * @param softConstraintCost the cost of soft constraints.
	 * @param xmlFile the file in which the solution has to be saved.
	 * @throws JAXBException if marshal operation failed.
	 */
	public static void saveSolutionXML(Schedule schedule, int softConstraintCost,
			File xmlFile) throws JAXBException {
		// Convert into INRC solution
		InrcSolution inrcSolution = InrcSolutionFactory.getInrcSolution(
				schedule, softConstraintCost);
		// Write XML of solution
		saveSolutionXML(inrcSolution, xmlFile);
	}
	
	/**
	 * Loads a schedule from a XML file in the INRC format.
	 * 
	 * @param scheduleFile the solution file.
	 * @param problem the problem of the schedule.
	 * @return a schedule.
	 * @throws JAXBException if marshal operation failed.
	 */
	public static Schedule loadXMLSchedule(File scheduleFile,
			ShiftSchedulingProblem problem) throws JAXBException {
		InrcSolution inrcSolution = null;
		JAXBContext jc = JAXBContext.newInstance(
					"de.uos.inf.ischedule.model.inrc");
		Unmarshaller u = jc.createUnmarshaller();
		inrcSolution = (InrcSolution) u.unmarshal(scheduleFile);
		Schedule schedule = InrcSolutionFactory.getSchedule(inrcSolution, problem);
		return schedule;
	}
	
	/**
	 * Loads a schedule from a TXT file in the INRC format.
	 * 
	 * @param scheduleFile the solution file.
	 * @param problem the problem of the schedule.
	 * @return a schedule.
	 * @throws IOException if parsing operation failed.
	 */
	public static Schedule loadTXTSchedule(File scheduleFile,
			ShiftSchedulingProblem problem) throws IOException {
		Schedule schedule = null;
		BufferedReader reader = null;
		try {
			// Open reader
			reader = new BufferedReader(new FileReader(scheduleFile));
			// Read header
			String problemID = readProblemID(reader);
			String[] competitorAndCost = readCompetitorAndCost(reader);
			readAssignmentNumber(reader);
			// Create schedule
			schedule = new Schedule(
					problemID,
					competitorAndCost[0],
					problem,
					problem.getSchedulingPeriod()
					);
			// Read assignments
			String[] assignment = readNextAssignment(reader);
			while (assignment != null) {
				// Add assignment
				Assignment a = new Assignment(
						problem.getEmployee(assignment[0]),
						problem.getShift(assignment[1]),
						Integer.parseInt(assignment[2]),
						Integer.parseInt(assignment[3]),
						Integer.parseInt(assignment[4])
						);
				schedule.assignments().add(a);
				assignment = readNextAssignment(reader);
			}
		} finally {
			// Close reader
			if (reader != null)
					reader.close();
		}
		return schedule;
	}

	/**
	 * Returns the number of assignments in the schedule.
	 * 
	 * @param reader the file reader.
	 * @return the number of assignments in the schedule.
	 * @throws IOException if read operation fails.
	 */
	private static String readAssignmentNumber(BufferedReader reader) throws IOException {
		String line = nextLine(reader);
		String[] fields = line.split("=");
		return fields[1].trim().replaceAll(";", "");
	}

	/**
	 * Returns the next assignment from the file.
	 * 
	 * @param reader the file reader.
	 * @return the next assignment from the file.
	 * @throws IOException if read operation fails.
	 */
	private static String[] readNextAssignment(BufferedReader reader) throws IOException {
		String line = nextLine(reader);
		if (line == null)
			return null;
		String[] fields = line.split(",");
		String[] assignment = new String[5];
		// Employee
		assignment[0] = fields[1].trim();
		// Shift
		assignment[1] = fields[2].trim().replaceAll(";", "");
		// Year
		String[] dateFields = fields[0].split("-");
		assignment[2] = dateFields[0].trim();
		// Month
		assignment[3] = dateFields[1].trim();
		// Day
		assignment[4] = dateFields[2].trim();
		return assignment;
	}

	/**
	 * Reads the names of the competitors and the cost value of the solution.
	 * 
	 * @param reader the file reader.
	 * @return the names of the competitors and the cost value of the solution.
	 * @throws IOException if read operation fails.
	 */
	private static String[] readCompetitorAndCost(BufferedReader reader) throws IOException {
		String line = nextLine(reader);
		String[] fields = line.split(",");
		fields[0] = fields[0].trim();
		fields[1] = fields[1].trim().replaceAll(";", "");
		return fields;
	}
	
	/**
	 * Reads the problem ID in the header of the solution file.
	 * 
	 * @param reader the file reader.
	 * @return the problem ID.
	 * @throws IOException if read operation fails.
	 */
	private static String readProblemID(BufferedReader reader) throws IOException {
		String line = nextLine(reader);
		String[] fields = line.split("=");
		return fields[1].trim().replaceAll(";", "");
	}

	/**
	 * Read the next line in the file, skipping comments and empty lines.
	 * 
	 * @param reader the file reader.
	 * @return the next line in the file.
	 * @throws IOException if read operation fails.
	 */
	private static String nextLine(BufferedReader reader) throws IOException {
		String line = reader.readLine();
		if (line == null)
			return null;
		line = line.trim();
		while(line.startsWith("//") || line.isEmpty()) {
			line = reader.readLine();
			line = line.trim();
		}
		return line;
	}
}
