/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.inrc;

import java.awt.Color;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.joda.time.DateTimeConstants;

import de.uos.inf.ischedule.model.AssignmentRequest;
import de.uos.inf.ischedule.model.AssignmentRequestConstraint;
import de.uos.inf.ischedule.model.CompleteWeekendsConstraint;
import de.uos.inf.ischedule.model.Contract;
import de.uos.inf.ischedule.model.DateDemand;
import de.uos.inf.ischedule.model.DayOfWeekDemand;
import de.uos.inf.ischedule.model.Employee;
import de.uos.inf.ischedule.model.IdentShiftsDuringWeekendsConstraint;
import de.uos.inf.ischedule.model.MaxConsecutiveDaysOffConstraint;
import de.uos.inf.ischedule.model.MaxConsecutiveWorkingDaysConstraint;
import de.uos.inf.ischedule.model.MaxConsecutiveWorkingWeekendsConstraint;
import de.uos.inf.ischedule.model.MaxNumAssignmentsConstraint;
import de.uos.inf.ischedule.model.MinConsecutiveDaysOffConstraint;
import de.uos.inf.ischedule.model.MinConsecutiveWorkingDaysConstraint;
import de.uos.inf.ischedule.model.MinConsecutiveWorkingWeekendsConstraint;
import de.uos.inf.ischedule.model.MinNumAssignmentsConstraint;
import de.uos.inf.ischedule.model.NoNightShiftBeforeFreeWeekendsConstraint;
import de.uos.inf.ischedule.model.PatternEntryType;
import de.uos.inf.ischedule.model.Period;
import de.uos.inf.ischedule.model.RequestType;
import de.uos.inf.ischedule.model.Shift;
import de.uos.inf.ischedule.model.ShiftCoverageConstraint;
import de.uos.inf.ischedule.model.ShiftPattern;
import de.uos.inf.ischedule.model.ShiftPatternEntry;
import de.uos.inf.ischedule.model.ShiftSchedulingProblem;
import de.uos.inf.ischedule.model.ShiftType;
import de.uos.inf.ischedule.model.Skill;
import de.uos.inf.ischedule.model.SkillCoverageConstraint;
import de.uos.inf.ischedule.model.UnwantedShiftPatternConstraint;
import de.uos.inf.ischedule.model.WeekendType;
import de.uos.inf.ischedule.model.inrc.InrcContracts.InrcContract;
import de.uos.inf.ischedule.model.inrc.InrcCoverRequirements.InrcDateSpecificCover;
import de.uos.inf.ischedule.model.inrc.InrcCoverRequirements.InrcDayOfWeekCover;
import de.uos.inf.ischedule.model.inrc.InrcDayOffRequests.InrcDayOff;
import de.uos.inf.ischedule.model.inrc.InrcDayOnRequests.InrcDayOn;
import de.uos.inf.ischedule.model.inrc.InrcEmployees.InrcEmployee;
import de.uos.inf.ischedule.model.inrc.InrcPatterns.InrcPattern;
import de.uos.inf.ischedule.model.inrc.InrcPatterns.InrcPattern.InrcPatternEntries.InrcPatternEntry;
import de.uos.inf.ischedule.model.inrc.InrcShiftOffRequests.InrcShiftOff;
import de.uos.inf.ischedule.model.inrc.InrcShiftOnRequests.InrcShiftOn;
import de.uos.inf.ischedule.model.inrc.InrcShiftTypes.InrcShift;
import de.uos.inf.ischedule.util.ColorGradient;

/**
 * This class allows instantiating a <code>ShiftSchedulingProblem</code> from INRC data,
 * and conversely, creating <code>InrcSchedulingPeriod</code> object from instance of 
 * <code>ShiftSchedulingProblem</code>.
 * 
 * @author David Meignan
 */
public class InrcProblemFactory {

	/**
	 * Returns a shift scheduling problem instance from INRC data.
	 * 
	 * @param inrcProblemData the shift scheduling problem data.
	 * @return a shift scheduling problem instance.
	 * 
	 * @throws IllegalArgumentException if the parameter value is <code>null</code>.
	 */
	public static ShiftSchedulingProblem getShiftSchedulingProblem(
			InrcSchedulingPeriod inrcProblemData) {
		if (inrcProblemData == null)
			throw new IllegalArgumentException();
		
		// Scheduling period
		Period schedulingPeriod = new Period(
				inrcProblemData.startDate.getYear(),
				inrcProblemData.startDate.getMonth(),
				inrcProblemData.startDate.getDay(),
				inrcProblemData.endDate.getYear(),
				inrcProblemData.endDate.getMonth(),
				inrcProblemData.endDate.getDay());
		
		// Scheduling problem
		ShiftSchedulingProblem problem = new ShiftSchedulingProblem(
				inrcProblemData.id,
				inrcProblemData.id,
				schedulingPeriod);
		
		// Skills
		if (inrcProblemData.skills != null &&
				inrcProblemData.skills.skill != null) {
			for (String inrcSkill: inrcProblemData.skills.skill) {
				Skill skill = new Skill(inrcSkill, inrcSkill);
				problem.skills().add(skill);
			}
		}
		
		// Shift-types (one shift-type per shift)
		for (InrcShift inrcShift: inrcProblemData.shiftTypes.shift) {
			ShiftType shiftType = new ShiftType(
					inrcShift.id,
					(inrcShift.description==null)?(inrcShift.id):(inrcShift.description)
					);
			problem.shiftTypes().add(shiftType);
		}
		
		// Shifts
		ArrayList<Color> gradientStops = new ArrayList<Color>();
		gradientStops.add(new Color(142,144,152));
		gradientStops.add(new Color(139,168,180));
		gradientStops.add(new Color(209,201,161));
		gradientStops.add(new Color(218,208,192));
		gradientStops.add(new Color(142,144,152));
		ColorGradient gradient = new ColorGradient(gradientStops);
		
		for (InrcShift inrcShift: inrcProblemData.shiftTypes.shift) {
			ShiftType shiftType = problem.getShiftType(inrcShift.id);
			Shift shift = new Shift(
					inrcShift.id,
					(inrcShift.description==null)?(inrcShift.id):(inrcShift.description),
					inrcShift.startTime.getHour(),
					inrcShift.startTime.getMinute(),
					inrcShift.endTime.getHour(),
					inrcShift.endTime.getMinute(),
					shiftType
					);
			// Required skills
			if (inrcShift.skills != null && inrcShift.skills.skill != null) {
				for (String inrcSkill: inrcShift.skills.skill) {
					shift.requiredSkills().add(
							problem.getSkill(inrcSkill));
				}
			}
			// Color
			double midMinuteTime = 0;
			if (inrcShift.startTime.getHour() > inrcShift.endTime.getHour()) {
				midMinuteTime = ( ((double)inrcShift.startTime.getHour()) +
						((double)inrcShift.endTime.getHour()) + 24. ) / 2.;
				if (midMinuteTime >= 24.)
					midMinuteTime -= 24.;
				midMinuteTime *= 60;
				midMinuteTime += ( ((double)inrcShift.startTime.getMinute()) +
						((double)inrcShift.endTime.getMinute()) ) / 2.;
			} else {
				midMinuteTime = ( ((double)inrcShift.startTime.getHour()) +
						((double)inrcShift.endTime.getHour()) ) / 2.;
				midMinuteTime *= 60;
				midMinuteTime += ( ((double)inrcShift.startTime.getMinute()) +
						((double)inrcShift.endTime.getMinute()) ) / 2.;
				
			}
			double position = midMinuteTime/1440.;
			shift.setColor(gradient.getColorAt(position));
			
			// Add shift
			problem.shifts().add(shift);
		}
		
		// Contract
		// Icons for contracts
		ArrayList<String> contractIconPaths = new ArrayList<String>();
		contractIconPaths.add("icons/user_blue.png");
		contractIconPaths.add("icons/user_green.png");
		contractIconPaths.add("icons/user_orange.png");
		contractIconPaths.add("icons/user_red.png");
		contractIconPaths.add("icons/user_gray.png");
		int contractIconIndex = 0;
		for (InrcContract inrcContract: inrcProblemData.contracts.contract) {
			// Get weekend type
			WeekendType weekendType = null;
			if (inrcContract.weekendDefinition == InrcWeekend.SATURDAY_SUNDAY) {
				weekendType = WeekendType.SATURDAY_SUNDAY;
			} else if (inrcContract.weekendDefinition == InrcWeekend.FRIDAY_SATURDAY_SUNDAY) {
				weekendType = WeekendType.FRIDAY_SATURDAY_SUNDAY;
			} else if (inrcContract.weekendDefinition == InrcWeekend.SATURDAY_SUNDAY_MONDAY) {
				weekendType = WeekendType.SATURDAY_SUNDAY_MONDAY;
			}  else if (inrcContract.weekendDefinition == InrcWeekend.FRIDAY_SATURDAY_SUNDAY_MONDAY) {
				weekendType = WeekendType.FRIDAY_SATURDAY_SUNDAY_MONDAY;
			} 
			// Create contract
			Contract contract = new Contract(
					inrcContract.id, 
					(inrcContract.description==null)?(inrcContract.id)
							:(inrcContract.description.toString()),
							weekendType,
							contractIconPaths.get(contractIconIndex%contractIconPaths.size()));
			contractIconIndex++;
			problem.contracts().add(contract);
		}
		
		// Employee
		for (InrcEmployee inrcEmployee: inrcProblemData.employees.employee) {
			Contract contract = problem.getContract(inrcEmployee.contractID);
			Employee employee = new Employee(
					inrcEmployee.id,
					(inrcEmployee.name==null)?(inrcEmployee.id):(inrcEmployee.name),
					contract
					);
			// Skills
			if (inrcEmployee.skills != null) {
				for (String inrcSkill: inrcEmployee.skills.getSkill()) {
					employee.skills().add(
							problem.getSkill(inrcSkill));
				}
			}
			problem.employees().add(employee);
		}
		
		// Day-of-week and date demands
		for (Object inrcCovers: inrcProblemData.coverRequirements
				.dayOfWeekCoverOrDateSpecificCover) {
			if (inrcCovers instanceof InrcDayOfWeekCover) {
				InrcDayOfWeekCover inrcDayCovers = (InrcDayOfWeekCover) inrcCovers;
				int day = -1;
				if (inrcDayCovers.day == InrcWeekDay.MONDAY) {
					day = DateTimeConstants.MONDAY;
				} else if (inrcDayCovers.day == InrcWeekDay.TUESDAY) {
					day = DateTimeConstants.TUESDAY;
				} else if (inrcDayCovers.day == InrcWeekDay.WEDNESDAY) {
					day = DateTimeConstants.WEDNESDAY;
				} else if (inrcDayCovers.day == InrcWeekDay.THURSDAY) {
					day = DateTimeConstants.THURSDAY;
				} else if (inrcDayCovers.day == InrcWeekDay.FRIDAY) {
					day = DateTimeConstants.FRIDAY;
				} else if (inrcDayCovers.day == InrcWeekDay.SATURDAY) {
					day = DateTimeConstants.SATURDAY;
				} else if (inrcDayCovers.day == InrcWeekDay.SUNDAY) {
					day = DateTimeConstants.SUNDAY;
				}
				for (InrcCover inrcCover: inrcDayCovers.cover) {
					DayOfWeekDemand demand = new DayOfWeekDemand(
							day,
							problem.getShift(inrcCover.shift),
							inrcCover.preferred.intValue());
					problem.dayOfWeekDemands().add(demand);
				}
			} else {
				InrcDateSpecificCover inrcDateCovers = (InrcDateSpecificCover) inrcCovers;
				for (InrcCover inrcCover: inrcDateCovers.cover) {
					DateDemand demand = new DateDemand(
							inrcDateCovers.date.getYear(),
							inrcDateCovers.date.getMonth(),
							inrcDateCovers.date.getDay(),
							problem.getShift(inrcCover.shift),
							inrcCover.preferred.intValue());
					problem.dateDemands().add(demand);
				}
			}
		}
		
		// Employee requests
		
		// Day-on requests
		if (inrcProblemData.dayOnRequests != null && 
				inrcProblemData.dayOnRequests.dayOn != null) {
			for (InrcDayOn dayOnRequest: inrcProblemData.dayOnRequests.dayOn) {
				Employee employee = problem.getEmployee(dayOnRequest.employeeID);
				if (dayOnRequest.weight.intValue() > 0) {
					employee.requests().add(new AssignmentRequest(
							RequestType.DAY_ON_REQUEST,
							dayOnRequest.date.getYear(),
							dayOnRequest.date.getMonth(),
							dayOnRequest.date.getDay(),
							null,
							dayOnRequest.weight.intValue()
							));
				}
			}
		}
		// Day off requests
		if (inrcProblemData.dayOffRequests != null && 
				inrcProblemData.dayOffRequests.dayOff != null) {
			for (InrcDayOff dayOffRequest: inrcProblemData.dayOffRequests.dayOff) {
				Employee employee = problem.getEmployee(dayOffRequest.employeeID);
				if (dayOffRequest.weight.intValue() > 0) {
					employee.requests().add(new AssignmentRequest(
							RequestType.DAY_OFF_REQUEST,
							dayOffRequest.date.getYear(),
							dayOffRequest.date.getMonth(),
							dayOffRequest.date.getDay(),
							null,
							dayOffRequest.weight.intValue()
							));
				}
			}
		}
		// Shift-on requests
		if (inrcProblemData.shiftOnRequests != null &&
				inrcProblemData.shiftOnRequests.shiftOn != null) {
			for (InrcShiftOn shiftOnRequest: inrcProblemData.shiftOnRequests.shiftOn) {
				Employee employee = problem.getEmployee(shiftOnRequest.employeeID);
				if (shiftOnRequest.weight.intValue() > 0) {
					employee.requests().add(new AssignmentRequest(
							RequestType.SHIFT_ON_REQUEST,
							shiftOnRequest.date.getYear(),
							shiftOnRequest.date.getMonth(),
							shiftOnRequest.date.getDay(),
							problem.getShift(shiftOnRequest.shiftTypeID),
							shiftOnRequest.weight.intValue()
							));
				}
			}
		}
		// Shift-off requests
		if (inrcProblemData.shiftOffRequests != null &&
				inrcProblemData.shiftOffRequests.shiftOff != null) {
			for (InrcShiftOff shiftOffRequest: inrcProblemData.shiftOffRequests.shiftOff) {
				Employee employee = problem.getEmployee(shiftOffRequest.employeeID);
				if (shiftOffRequest.weight.intValue() > 0) {
					employee.requests().add(new AssignmentRequest(
							RequestType.SHIFT_OFF_REQUEST,
							shiftOffRequest.date.getYear(),
							shiftOffRequest.date.getMonth(),
							shiftOffRequest.date.getDay(),
							problem.getShift(shiftOffRequest.shiftTypeID),
							shiftOffRequest.weight.intValue()
							));
				}
			}
		}
		
		// Constraints
		
		//Coverage constraints (hard)
		problem.constraints(0).add(
				new ShiftCoverageConstraint(true, 1));
		
		// Maximum number of assignments
		for (InrcContract inrcContract: inrcProblemData.contracts.getContract()) {
			if (inrcContract.maxNumAssignments != null &&
					inrcContract.maxNumAssignments.on) {
				Contract contract = problem.getContract(inrcContract.id);
				MaxNumAssignmentsConstraint maxNumAssignmentConstraint = new 
						MaxNumAssignmentsConstraint(
								inrcContract.maxNumAssignments.value.intValue(),
								contract,
								inrcContract.maxNumAssignments.on,
								inrcContract.maxNumAssignments.weight.intValue()
								);
				problem.constraints(1).add(maxNumAssignmentConstraint);
			}
		}
		
		// Minimum number of assignments
		for (InrcContract inrcContract: inrcProblemData.contracts.getContract()) {
			if (inrcContract.minNumAssignments != null &&
					inrcContract.minNumAssignments.on) {
				Contract contract = problem.getContract(inrcContract.id);
				MinNumAssignmentsConstraint minNumAssignmentConstraint = new 
						MinNumAssignmentsConstraint(
								inrcContract.minNumAssignments.value.intValue(),
								contract,
								inrcContract.minNumAssignments.on,
								inrcContract.minNumAssignments.weight.intValue()
								);
				problem.constraints(1).add(minNumAssignmentConstraint);
			}
		}
		
		// Maximum number of consecutive working days
		for (InrcContract inrcContract: inrcProblemData.contracts.getContract()) {
			if (inrcContract.maxConsecutiveWorkingDays != null &&
					inrcContract.maxConsecutiveWorkingDays.on) {
				Contract contract = problem.getContract(inrcContract.id);
				MaxConsecutiveWorkingDaysConstraint maxConsecutiveWorkingDaysConstraint = new 
						MaxConsecutiveWorkingDaysConstraint(
								inrcContract.maxConsecutiveWorkingDays.value.intValue(),
								contract,
								inrcContract.maxConsecutiveWorkingDays.on,
								inrcContract.maxConsecutiveWorkingDays.weight.intValue()
								);
				problem.constraints(1).add(maxConsecutiveWorkingDaysConstraint);
			}
		}
		
		// Minimum number of consecutive working days
		for (InrcContract inrcContract: inrcProblemData.contracts.getContract()) {
			if (inrcContract.minConsecutiveWorkingDays != null &&
					inrcContract.minConsecutiveWorkingDays.on) {
				Contract contract = problem.getContract(inrcContract.id);
				if (inrcContract.minConsecutiveWorkingDays.value.intValue() > 1) {
					MinConsecutiveWorkingDaysConstraint minConsecutiveWorkingDaysConstraint = new 
							MinConsecutiveWorkingDaysConstraint(
									inrcContract.minConsecutiveWorkingDays.value.intValue(),
									contract,
									inrcContract.minConsecutiveWorkingDays.on,
									inrcContract.minConsecutiveWorkingDays.weight.intValue()
									);
					problem.constraints(1).add(minConsecutiveWorkingDaysConstraint);
				}
			}
		}
		
		// Maximum number of consecutive free days
		for (InrcContract inrcContract: inrcProblemData.contracts.getContract()) {
			if (inrcContract.maxConsecutiveFreeDays != null &&
					inrcContract.maxConsecutiveFreeDays.on) {
				Contract contract = problem.getContract(inrcContract.id);
				MaxConsecutiveDaysOffConstraint maxConsecutiveFreeDaysConstraint = new 
						MaxConsecutiveDaysOffConstraint(
								inrcContract.maxConsecutiveFreeDays.value.intValue(),
								contract,
								inrcContract.maxConsecutiveFreeDays.on,
								inrcContract.maxConsecutiveFreeDays.weight.intValue()
								);
				problem.constraints(1).add(maxConsecutiveFreeDaysConstraint);
			}
		}
		
		// Minimum number of consecutive free days
		for (InrcContract inrcContract: inrcProblemData.contracts.getContract()) {
			if (inrcContract.minConsecutiveFreeDays != null &&
					inrcContract.minConsecutiveFreeDays.on) {
				Contract contract = problem.getContract(inrcContract.id);
				if (inrcContract.minConsecutiveFreeDays.value.intValue() > 1) {
					MinConsecutiveDaysOffConstraint minConsecutiveFreeDaysConstraint = new 
							MinConsecutiveDaysOffConstraint(
									inrcContract.minConsecutiveFreeDays.value.intValue(),
									contract,
									inrcContract.minConsecutiveFreeDays.on,
									inrcContract.minConsecutiveFreeDays.weight.intValue()
									);
					problem.constraints(1).add(minConsecutiveFreeDaysConstraint);
				}
			}
		}
		
		// Maximum number of consecutive working weekends
		for (InrcContract inrcContract: inrcProblemData.contracts.getContract()) {
			if (inrcContract.maxConsecutiveWorkingWeekends != null &&
					inrcContract.maxConsecutiveWorkingWeekends.on) {
				Contract contract = problem.getContract(inrcContract.id);
				MaxConsecutiveWorkingWeekendsConstraint maxConsecutiveWorkingWeekendsConstraint = new 
						MaxConsecutiveWorkingWeekendsConstraint(
								inrcContract.maxConsecutiveWorkingWeekends.value.intValue(),
								contract,
								inrcContract.maxConsecutiveWorkingWeekends.on,
								inrcContract.maxConsecutiveWorkingWeekends.weight.intValue()
								);
				problem.constraints(1).add(maxConsecutiveWorkingWeekendsConstraint);
			}
		}
		
		// Minimum number of consecutive working weekends
		for (InrcContract inrcContract: inrcProblemData.contracts.getContract()) {
			if (inrcContract.minConsecutiveWorkingWeekends != null &&
					inrcContract.minConsecutiveWorkingWeekends.on) {
				Contract contract = problem.getContract(inrcContract.id);
				if (inrcContract.minConsecutiveWorkingWeekends.value.intValue() > 1) {
					MinConsecutiveWorkingWeekendsConstraint minConsecutiveWorkingWeekendsConstraint = new 
							MinConsecutiveWorkingWeekendsConstraint(
									inrcContract.minConsecutiveWorkingWeekends.value.intValue(),
									contract,
									inrcContract.minConsecutiveWorkingWeekends.on,
									inrcContract.minConsecutiveWorkingWeekends.weight.intValue()
									);
					problem.constraints(1).add(minConsecutiveWorkingWeekendsConstraint);
				}
			}
		}
		
		// Maximum number of working weekends in four weeks
		for (InrcContract inrcContract: inrcProblemData.contracts.getContract()) {
			if (inrcContract.maxWorkingWeekendsInFourWeeks != null &&
					inrcContract.maxWorkingWeekendsInFourWeeks.on) {
				System.err.println("The constraint \"MaxWorkingWeekendsInFourWeeks\" is" +
						"not specified in INRC model!");
			}
		}
		
		// Complete weekends
		for (InrcContract inrcContract: inrcProblemData.contracts.getContract()) {
			if (inrcContract.completeWeekends != null &&
					inrcContract.completeWeekends.value) {
				Contract contract = problem.getContract(inrcContract.id);
				CompleteWeekendsConstraint completeWeekendsConstraint = new 
						CompleteWeekendsConstraint(
								contract,
								inrcContract.completeWeekends.value,
								inrcContract.completeWeekends.weight.intValue()
								);
				problem.constraints(1).add(completeWeekendsConstraint);
			}
		}
		
		// Identical shift type during weekends
		for (InrcContract inrcContract: inrcProblemData.contracts.getContract()) {
			if (inrcContract.identicalShiftTypesDuringWeekend != null &&
					inrcContract.identicalShiftTypesDuringWeekend.value) {
				Contract contract = problem.getContract(inrcContract.id);
				IdentShiftsDuringWeekendsConstraint identShiftDuringWeekendsConstraint = new 
						IdentShiftsDuringWeekendsConstraint(
								contract,
								inrcContract.identicalShiftTypesDuringWeekend.value,
								inrcContract.identicalShiftTypesDuringWeekend.weight.intValue(),
								true
								);
				problem.constraints(1).add(identShiftDuringWeekendsConstraint);
			}
		}
				
		// No night shift before free weekend
		for (InrcContract inrcContract: inrcProblemData.contracts.getContract()) {
			if (inrcContract.noNightShiftBeforeFreeWeekend != null &&
					inrcContract.noNightShiftBeforeFreeWeekend.value) {
				Contract contract = problem.getContract(inrcContract.id);
				// Add night shifts
				ArrayList<Shift> nightShifts = new ArrayList<Shift>();
				for (Shift shift: problem.shifts()) {
					if (shift.isNightShift())
						nightShifts.add(shift);
				}
				if (inrcContract.noNightShiftBeforeFreeWeekend.value) {
					// TODO
					/*
					System.err.println("The constraint 'NoNightShiftBeforeFreeWeekends' has been removed " +
							"from the INRC'10 dataset.");
					*/
				}
				NoNightShiftBeforeFreeWeekendsConstraint noNightShiftBeforeFreeWeekendsConstraint = new 
						NoNightShiftBeforeFreeWeekendsConstraint(
								nightShifts,
								contract,
								false, // Normally: inrcContract.noNightShiftBeforeFreeWeekend.value,
								inrcContract.noNightShiftBeforeFreeWeekend.weight.intValue()
								);
				problem.constraints(1).add(noNightShiftBeforeFreeWeekendsConstraint);
			}
		}
		
		// Unwanted shift patterns
		ArrayList<UnwantedShiftPatternConstraint> unwantedPatterns = 
				new ArrayList<UnwantedShiftPatternConstraint>();
		if (inrcProblemData.patterns != null && 
				inrcProblemData.patterns.pattern != null) {
			for (InrcPattern inrcPattern: inrcProblemData.patterns.pattern) {
				int weight = Integer.parseInt(inrcPattern.weight);
				ShiftPattern pattern = new ShiftPattern(inrcPattern.id);
				// Pattern entries
				ArrayList<ShiftPatternEntry> entries = new ArrayList<ShiftPatternEntry>();
				for (int i=0; i<inrcPattern.patternEntries.patternEntry.size(); i++) {
					entries.add(null);
				}
				for (InrcPatternEntry inrcPatternEntry: 
					inrcPattern.patternEntries.patternEntry) {
					ShiftPatternEntry entry = null;
					if (inrcPatternEntry.shiftType.compareToIgnoreCase("None") == 0) {
						entry = new ShiftPatternEntry(PatternEntryType.NO_ASSIGNMENT,
								null);
					} else if (inrcPatternEntry.shiftType.compareToIgnoreCase("Any") == 0) {
						entry = new ShiftPatternEntry(PatternEntryType.WORKED_SHIFT,
								null);
					} else {
						entry = new ShiftPatternEntry(PatternEntryType.SPECIFIC_WORKED_SHIFT,
								problem.getShift(inrcPatternEntry.shiftType));
					}
					entries.set(Integer.parseInt(inrcPatternEntry.index), entry);
					if (Integer.parseInt(inrcPatternEntry.index) == 0) {
						// set start day of the pattern
						if (inrcPatternEntry.day.compareToIgnoreCase("Any") == 0) {
							// No start day-of-the-week
						} else if (inrcPatternEntry.day.compareToIgnoreCase("Monday") == 0) {
							pattern.setStartDay(DateTimeConstants.MONDAY);
						} else if (inrcPatternEntry.day.compareToIgnoreCase("Tuesday") == 0) {
							pattern.setStartDay(DateTimeConstants.TUESDAY);
						} else if (inrcPatternEntry.day.compareToIgnoreCase("Wednesday") == 0) {
							pattern.setStartDay(DateTimeConstants.WEDNESDAY);
						} else if (inrcPatternEntry.day.compareToIgnoreCase("Thursday") == 0) {
							pattern.setStartDay(DateTimeConstants.THURSDAY);
						} else if (inrcPatternEntry.day.compareToIgnoreCase("Friday") == 0) {
							pattern.setStartDay(DateTimeConstants.FRIDAY);
						} else if (inrcPatternEntry.day.compareToIgnoreCase("Saturday") == 0) {
							pattern.setStartDay(DateTimeConstants.SATURDAY);
						} else if (inrcPatternEntry.day.compareToIgnoreCase("Sunday") == 0) {
							pattern.setStartDay(DateTimeConstants.SUNDAY);
						}
					}
				}
				pattern.entries().addAll(entries);
				// Constraint
				UnwantedShiftPatternConstraint unwantedShiftPatternConstraint = new 
						UnwantedShiftPatternConstraint(
								pattern, true, weight);
				unwantedPatterns.add(unwantedShiftPatternConstraint);
				problem.constraints(1).add(unwantedShiftPatternConstraint);
			}
		}
		// Scope of shift-pattern constraints
		for (InrcContract inrcContract: inrcProblemData.contracts.getContract()) {
			if (inrcContract.unwantedPatterns != null &&
					inrcContract.unwantedPatterns.pattern != null) {
				Contract contract = problem.getContract(inrcContract.id);
				for (String inrcPatternID: inrcContract.unwantedPatterns.pattern) {
					UnwantedShiftPatternConstraint unwantedShiftPatternConstraint = null;
					for (UnwantedShiftPatternConstraint c: unwantedPatterns) {
						if (c.getUnwantedPattern().getId().compareToIgnoreCase(inrcPatternID)
								== 0) {
							unwantedShiftPatternConstraint = c;
							break;
						}
					}
					unwantedShiftPatternConstraint.scope().add(contract);
				}
			}
		}
		
		// Assignment requests (Day off, working day, shift-off, and shift-on)
		// Check existing request
		boolean containRequests = false;
		for (Employee employee: problem.employees()) {
			if (!employee.requests().isEmpty()) {
				containRequests = true;
				break;
			}
		}
		problem.constraints(1).add(
				new AssignmentRequestConstraint(containRequests, 1));
		
		// Skill coverage constraints
		for (InrcContract inrcContract: inrcProblemData.contracts.getContract()) {
			if (inrcContract.alternativeSkillCategory != null &&
					inrcContract.alternativeSkillCategory.value) {
				Contract contract = problem.getContract(inrcContract.id);
				SkillCoverageConstraint skillCoverageConstraint = new 
						SkillCoverageConstraint(
								contract,
								inrcContract.alternativeSkillCategory.value,
								inrcContract.alternativeSkillCategory.weight.intValue()
								);
				problem.constraints(1).add(skillCoverageConstraint);
			}
		}
		
		return problem;
	}
	
	/**
	 * Loads a problem from a XML file in the INRC format.
	 * 
	 * @param problemFile the problem file.
	 * @return a shift scheduling problem.
	 * @throws JAXBException if unmarshal operation fail.
	 */
	public static ShiftSchedulingProblem loadProblem(File problemFile) throws JAXBException {
		ShiftSchedulingProblem problem = null;
		// Read/unmarshal data of the problem
		JAXBContext jc = JAXBContext.newInstance("de.uos.inf.ischedule.model.inrc");
		Unmarshaller u = jc.createUnmarshaller();
		InrcSchedulingPeriod sp = (InrcSchedulingPeriod)
		    u.unmarshal(problemFile);
		// Creates shift scheduling problem
		problem = InrcProblemFactory.getShiftSchedulingProblem(sp);
		return problem;
	}

	/**
	 * Loads a problem from an input stream to an XML resource.
	 * 
	 * @param resourceStream the resource file where data of the problem are stored.
	 * @return a shift scheduling problem.
	 * @throws JAXBException if unmarshal operation fail.
	 */
	public static ShiftSchedulingProblem loadProblem(InputStream resourceStream) 
			throws JAXBException {
		ShiftSchedulingProblem problem = null;
		// Read/unmarshal data of the problem
		JAXBContext jc = JAXBContext.newInstance("de.uos.inf.ischedule.model.inrc");
		Unmarshaller u = jc.createUnmarshaller();
		InrcSchedulingPeriod sp = (InrcSchedulingPeriod)
		    u.unmarshal(resourceStream);
		// Creates shift scheduling problem
		problem = InrcProblemFactory.getShiftSchedulingProblem(sp);
		return problem;
	}
}
