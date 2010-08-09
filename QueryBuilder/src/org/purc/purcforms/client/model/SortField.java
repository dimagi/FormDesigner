package org.purc.purcforms.client.model;

import java.io.Serializable;


/**
 * 
 * @author daniel
 *
 */
public class SortField implements Serializable {

	/**
	 * Generated serialization ID.
	 */
	private static final long serialVersionUID = 8275519805118414698L;
	
	public static final int SORT_NULL = 0;
	public static final int SORT_ASCENDING = 1;
	public static final int SORT_DESCENDING = 2;
	
	private String name;
	private int sortOrder;
	
	public SortField(){
		
	}

	public SortField(String name, int sortOrder) {
		super();
		this.name = name;
		this.sortOrder = sortOrder;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}
}
