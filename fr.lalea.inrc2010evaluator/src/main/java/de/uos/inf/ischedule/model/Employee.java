/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

/**
 * An employee that can be assigned in a schedule.
 * 
 * @author David Meignan
 */
public class Employee {

	/**
	 * ID of the employee.
	 */
	protected String id;
	
	/**
	 * Name or label of the employee.
	 */
	protected String name;
	
	/**
	 * Specification of the contract of the employee for the
	 * shift scheduling problem.
	 */
	protected Contract contract;
	
	/**
	 * Skills of the employee.
	 */
	protected ArrayList<Skill> skills = new ArrayList<Skill>();
	
	/**
	 * Set of assignment requests of the employee.
	 */
	protected ArrayList<AssignmentRequest> requests = 
			new ArrayList<AssignmentRequest>();
	
	/**
	 * Constructs an employee.
	 * 
	 * @param id the ID of the employee.
	 * @param name the name of the employee.
	 * @param contract the contract of the employee.
	 */
	public Employee(String id, String name, Contract contract) {
		this.id = id;
		this.name = name;
		this.contract = contract;
	}

	/**
	 * Returns the contract of the employee.
	 * 
	 * @return the contract of the employee.
	 */
	public Contract getContract() {
		return contract;
	}
	
	/**
	 * Returns a collection view of skills of the employee.
	 * 
	 * @return a collection view of skills of the employee.
	 */
	public List<Skill> skills() {
		return new SkillCollection();
	}
	
	/**
	 * Returns a collection view of the requests of the employee.
	 * 
	 * @return a collection view of the requests of the employee.
	 */
	public List<AssignmentRequest> requests() {
		return new RequestCollection();
	}
	
	/**
	 * Collection view of skills. This custom implementation controls modification
	 * operations.
	 */
	private class SkillCollection extends AbstractList<Skill> {

		/* (non-Javadoc)
		 * @see java.util.AbstractList#get(int)
		 */
		@Override
		public Skill get(int idx) {
			return skills.get(idx);
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size() {
			return skills.size();
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractList#add(int, java.lang.Object)
		 */
		@Override
		public void add(int index, Skill element) {
			skills.add(index, element);
		}
	}
	
	/**
	 * Collection view of requests. This custom implementation controls modification
	 * operations.
	 */
	private class RequestCollection extends AbstractList<AssignmentRequest> {

		/* (non-Javadoc)
		 * @see java.util.AbstractList#get(int)
		 */
		@Override
		public AssignmentRequest get(int idx) {
			return requests.get(idx);
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size() {
			return requests.size();
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractList#add(int, java.lang.Object)
		 */
		@Override
		public void add(int index, AssignmentRequest element) {
			requests.add(index, element);
		}
	}

	/**
	 * Returns the ID of the employee.
	 * 
	 * @return the ID of the employee.
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Returns the name of the employee.
	 * 
	 * @return the name of the employee.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns <code>true</code> if the employee has a day off request
	 * for the specified day. Returns <code>false</code> otherwise.
	 * 
	 * @param day the day.
	 * @return <code>true</code> if the employee has a day off request
	 * for the specified day index. Returns <code>false</code> otherwise.
	 */
	public boolean hasDayOffRequest(LocalDate date) {
		for (AssignmentRequest request: requests) {
			if (request.getType() == RequestType.DAY_OFF_REQUEST &&
					request.isOn(date)) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
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
		Employee other = (Employee) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
