package org.beanmaker.util;

public class IdNamePair implements Comparable<IdNamePair> {

	private final String id;
	private final String name;
	
	public IdNamePair(final int id, final String name) {
		if (id < 0)
			throw new IllegalArgumentException("id must be zero or positive");
		
		this.id = Integer.toString(id);
		this.name = name;
	}
	
	public IdNamePair(final long id, final String name) {
		if (id < 0)
			throw new IllegalArgumentException("id must be zero or positive");
		
		this.id = Long.toString(id);
		this.name = name;
	}
	
	public IdNamePair(final String id, final String name) {
		this.id = id;
		this.name = name;
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	// Assumes only one "please select ..." element with id "0", throws an IllegalStateException otherwise.
	@Override
	public int compareTo(final IdNamePair idNamePair) {
		if (id.equals("0") && idNamePair.id.equals("0"))
			throw new IllegalStateException("More than one 'please select' field in IdNamePair collection.");

		if (id.equals("0"))
			return -1;
		if (idNamePair.id.equals("0"))
			return 1;

		return name.compareTo(idNamePair.name);
	}
}

