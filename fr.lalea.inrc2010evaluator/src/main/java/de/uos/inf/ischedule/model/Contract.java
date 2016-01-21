/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model;

/**
 * A <code>Contract</code> allows to define a set of constraints or work-regulations
 * for the employees.
 * 
 * @author David Meignan
 */
public class Contract implements Comparable<Contract> {

	/**
	 * ID of the contract.
	 */
	protected String id;
	
	/**
	 * Label of the contract.
	 */
	protected String name;
	
	/**
	 * Type of weekend.
	 */
	protected WeekendType weekendType;
	
	/**
	 * The icon's path to represent the contract.
	 */
	private String iconPath;
	
	/**
	 * Constructs a contract.
	 * 
	 * @param id the ID of the contract.
	 * @param description the label of the contract.
	 * @param weekendType the type of weekend..
	 * @param iconPath the icon's path associated with the contract.
	 * 
	 * @throws IllegalArgumentException if the ID or the description is <code>null</code>.
	 */
	public Contract(String id, String description, WeekendType weekendType,
			String iconPath) {
		if (id == null || description == null || weekendType == null)
			throw new IllegalArgumentException();
		this.id = id;
		this.name = description;
		this.weekendType = weekendType;
		this.iconPath = iconPath;
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
		Contract other = (Contract) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Contract other) {
		if (other == null)
			throw new NullPointerException();
		if (other.id == null || id == null)
			throw new IllegalArgumentException();
		return id.compareTo(other.id);
	}


	/**
	 * Returns the type of weekend for the contract.
	 * 
	 * @return the type of weekend for the contract.
	 */
	public WeekendType getWeekendType() {
		return weekendType;
	}

	/**
	 * Returns the icon's path related to the contract.
	 * 
	 * @return the icon's path related to the contract.
	 */
	public String getIconPath() {
		return iconPath;
	}
	
	/**
	 * Returns the name/label of the contract.
	 * 
	 * @return the name/label of the contract.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the ID of the contract.
	 * 
	 * @return the ID of the contract.
	 */
	public String getID() {
		return id;
	}
}
